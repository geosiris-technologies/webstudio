/*
Copyright 2019 GEOSIRIS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.geosiris.webstudio.servlet;

import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.utils.SessionUtility;
import com.geosiris.webstudio.utils.Utility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Servlet implementation class GetResources
 */
@WebServlet("/GetResources")
public class GetResources extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(GetResources.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetResources() {
		super();
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}

		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write("[]");
        out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}

		Integer depth = 1;
		String from = null;
		HashMap<String, String> where = new HashMap<String, String>();


		if(ServletFileUpload.isMultipartContent(request)) {
			DiskFileItemFactory factory = new DiskFileItemFactory();

			ServletFileUpload upload = new ServletFileUpload(factory);

			try {
				FileItemIterator iterator = upload.getItemIterator(request);

				while (iterator.hasNext()) {
					FileItemStream item = iterator.next();
					InputStream stream = item.openStream();

					if (item.isFormField() ) {
						String value = Streams.asString(stream);
						if("from".compareToIgnoreCase(item.getFieldName()) == 0) {
							from = value;
						}else if("depth".compareToIgnoreCase(item.getFieldName()) == 0) {
							try { depth = Integer.parseInt(value);}
							catch (Exception ignore){}
						}else if("where".compareToIgnoreCase(item.getFieldName()) == 0) {
							try{
								String left = value.substring(0, value.indexOf("="));
								String right = value.substring(value.indexOf("=") + 1);
								where.put(left, right);
							}catch (Exception ignore){}
						}
					}
				}
			}catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}else {
			for(String k : request.getParameterMap().keySet()) {
				String value = request.getParameterMap().get(k)[0];
				if("from".compareToIgnoreCase(k) == 0) {
					from = value;
				}else if("depth".compareToIgnoreCase(k) == 0) {
					try { depth = Integer.parseInt(value);}
					catch (Exception ignore){}
				}else if("where".compareToIgnoreCase(k) == 0) {
					try{
						String left = value.substring(0, value.indexOf("="));
						String right = value.substring(value.indexOf("=") + 1);
						where.put(left, right);
					}catch (Exception ignore){}
				}
			}
		}
		GetResourceRequest getResReq = new GetResourceRequest(from, depth, where);

		String jsonList = Utility.getEPCContentAsJSON(processRequest(SessionUtility.getResqmlObjects(request.getSession(false)), getResReq));

		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(jsonList);
        out.flush();
	}


	private static List<Object> processRequest(Map<String, Object> objectList, GetResourceRequest request){
		List<Object> res = new ArrayList<Object>();

		// Stack des objects à verifier, <depth, Object>
		Stack<Pair<Integer, Object>> stack = new Stack<>();
		HashSet<String> viewedObject = new HashSet<String>();

		List<String> typesToKeep = new ArrayList<String>();
		for(String whereKey : request.getWhere().keySet()) {
			if(whereKey.compareToIgnoreCase("#type")==0) {
				typesToKeep.add(request.getWhere().get(whereKey));
			}
		}

		// Premier filtre sur le from
		if(request.getFrom()!=null && request.getFrom().length()>0) {
			for(String uuid : objectList.keySet()) {
				if(uuid.compareTo(request.getFrom())==0) {
					stack.push(new Pair<Integer, Object>(request.getDepth(), objectList.get(uuid)));
				}
			}
		}else {
			for(final Object o : objectList.values()) {
				stack.push(new Pair<Integer, Object>(request.getDepth(), o));
			}
		}

		while(!stack.isEmpty()) {
			Pair<Integer, Object> po = stack.pop();
			String uuid = (String) ObjectController.getObjectAttributeValue(po.r(), "uuid");
			if(viewedObject.contains(uuid)) {
				viewedObject.add(uuid);

				if(request.isMatch(po.r())) {
					res.add(po.r());
				}
				if(po.l()>0) {
					// Si pas au bout du chemin, on continue
					List<Pair<String, String>> allReferencedObjects = EPCGenericManager.getAllReferencedObjects(uuid, objectList);
					for(String r_uuid : allReferencedObjects.stream().map(p -> p.r()).collect(Collectors.toList())) {
						stack.push(new Pair<Integer, Object>(po.l()-1, objectList.get(r_uuid)));
					}
					
					List<Object> referencers = EPCGenericManager.getAllReferencersObjects(uuid, objectList);
					for(Object r_obj : referencers) {
						stack.push(new Pair<Integer, Object>(po.l()-1, r_obj));
					}
				}
			}
		}

		return res;
	}

	public static class GetResourceRequest {
		private static final Pattern p_from = Pattern.compile("[fF][rR][oO][mM]\\(([^)]+)\\)");

		private static final Pattern p_where = Pattern.compile("[wW][hH][eE][rR][eE]\\((.+)\\)\\s*[\\&$]");		
		private static final String p_where_attributes_value = ".*[^\\s]";//"[#\\p{L}\\.\\s\\*\\+]*[#\\p{L}\\.]";
		private static final Pattern p_where_attributes = Pattern.compile("\\s*([#\\w\\.]+)\\s*=\\s*(" + p_where_attributes_value + ")\\s*");

		private static final Pattern p_depth = Pattern.compile("[dD][eE][pP][tT][hH]\\((\\d+)\\)");

		private String from;
		private Integer depth;
		private Map<String, String> where;

		public GetResourceRequest(String from, Integer depth, Map<String, String> where) {
			this.from = from;
			this.depth = depth;
			this.where = where;
		}

		public GetResourceRequest(String request) {
			from = null;
			depth = 1;
			where = new HashMap<String, String>();

			Matcher m0 = p_from.matcher(request);
			if(m0.find()) {
				from = m0.group(1);
			}

			Matcher m1 = p_where.matcher(request);
			while (m1.find()) {
				Matcher m1_b = p_where_attributes.matcher(m1.group(1));
				while (m1_b.find()) {
					where.put(m1_b.group(1), m1_b.group(2));
				}
			}

			Matcher m2 = p_depth.matcher(request);
			if(m2.find()) {
				depth = Integer.parseInt(m2.group(1));
			}			
		}

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public void setDepth(Integer depth) {
			this.depth = depth;
		}

		public Integer getDepth() {
			return depth;
		}

		public Map<String, String> getWhere() {
			return where;
		}

		public Boolean isMatch(Object o) {
			if(o!=null) {
				for(String key : where.keySet()) {
					if(key.compareToIgnoreCase("#type")==0){
						Pattern p_f_type = Pattern.compile(where.get(key));
						Matcher m = p_f_type.matcher(o.getClass().getSimpleName());
						if(!m.find()) {
							return false;
						}
					}else {
						try {
							String value = (String) ObjectController.getObjectAttributeValue(o, key);
							if(value.compareToIgnoreCase(where.get(key))!=0) {
								return false;
							}
						}catch (Exception e) {
							logger.error(e.getMessage(), e);
							return false;
						}
					}
				}
				return true;
			}else
				return false;
		}

		@Override
		public String toString() {
			String value = "from("+from+")&where(";
			for(String k : where.keySet()) {
				value+=k+"="+where.get(k)+"; ";
			}
			value += ")&depth("+ depth +")";

			return value;
		}
	}

	public static void main(String[] argv) {
		String request = "from(UUID-95aze-a555zea-aze)&where(#type=int.*, .kind=project, citation.Title =JeanFrançois Rainaud)&depth(2)";

		logger.info(new GetResourceRequest(request));

		Pattern p_type = Pattern.compile("TriangulatedSetRepresentation");
		Matcher m = p_type.matcher("TriangulatedSetRepresentation");
		if(m.find()) {
			logger.info("true");
		}else {
			logger.info("false");
		}

		String request2 = "from()&where(#type=[iI]nt.*, .kind=project, citation.Title =JeanFrançois Rainaud)&depth(2)";
		Map<String, Object> objectList = new HashMap<String, Object>();
		objectList.put("UUID-95aze-a555zea-aze", 3);
		objectList.put("UUID-95aze-a555zeae", 3.2);
		GetResourceRequest resq = new GetResourceRequest(request2);
		processRequest(objectList, resq );

	}

}
