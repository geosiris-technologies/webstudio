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
package com.geosiris.webstudio.utils;

import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.utils.ResqmlObjectControler.ModficationType;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class ResQMLConverter {
    public static Logger logger = LogManager.getLogger(ResQMLConverter.class);

	public static Object getCopy(HttpSession session, final Object resqmlObject, final Map<String, Object> epcObjects) {
		if(resqmlObject!=null) {
			Object target = null;
			try {
				target = Editor.pkgManager.createInstance(resqmlObject.getClass().getName(), epcObjects, null, "", true);
			}catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if(target != null) {
				final Object targetF = target;
				ObjectTree objTree = ObjectTree.createTree(resqmlObject);
				objTree.map((tree -> {
					String path = tree.getName().toLowerCase();
					if(path.compareTo(".schemaversion") != 0 // On ne copie pas le schema version
						&& path.compareTo(".uuid") != 0)	
						copyValue(session, tree, targetF, resqmlObject, epcObjects);
				}), true, true);
				return targetF;
			}
		}
		return null;
	}

	private static void copyValue(HttpSession session, ObjectTree source, Object target, final Object sourceObject, Map<String, Object> epcObjects) {
		if(source.getName().length()>0 //(source.getProperties()==null || source.getProperties().size()==0)
				&& target != null
				) {
			if(source.getName().toLowerCase().endsWith("citation.title")) {
				try {
					String oldTitle = (String) ObjectController.getObjectAttributeValue(sourceObject, "Uuid");
					ObjectController.editObjectAttribute(target, source.getName(), "Copy of " + oldTitle);
					logger.error("Modifying title : from " + oldTitle);
				}catch(Exception e) {logger.error(e.getMessage(), e);}
			}else {
				Object oldValue = null;
				try {
					oldValue = ObjectController.getObjectAttributeValue(target, source.getName());
				}catch(Exception ignore){}
				if(source.getData()!=null && !source.getData().getClass().getName().contains("energyml")) {
					logger.error("Editing copy value : " + source.getName() + " '" + oldValue +"' new val '"+source.getData()+"'");
					try {
						ResqmlObjectControler.modifyResqmlObjectFromParameter(session, target, source.getName(),
								ModficationType.EDITION, 
								source.getData(), 
								epcObjects);
					}catch(Exception e) {logger.error(e.getMessage(), e);}
				}
//				}else {
//					logger.error("old value was not null " + oldValue);
//				}
			}
		}else {
			logger.error("Target is null ");
		}
	}
}
