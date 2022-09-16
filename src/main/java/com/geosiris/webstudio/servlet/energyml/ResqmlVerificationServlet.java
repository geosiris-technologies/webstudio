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
package com.geosiris.webstudio.servlet.energyml;

import com.geosiris.webstudio.logs.LogMessage;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.utils.ResqmlVerification;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Servlet implementation class ResqmlVerification
 */
@WebServlet("/ResqmlVerification")
public class ResqmlVerificationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResqmlVerificationServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }
        HttpSession session = request.getSession(false);

        StringBuilder answer = new StringBuilder();
        Map<String, Object> map = SessionUtility.getResqmlObjects(session);
        String uuid = request.getParameter("uuid");

        List<LogMessage> logs;

        if (uuid != null) {
            logs = ResqmlVerification.doVerification(uuid, map);
        } else {
            logs = ResqmlVerification.doVerification(map);
        }

        answer.append("[");

        for (LogMessage lm : logs) {
            answer.append((ServerLogMessage.parseLogMessage(lm)).toJSON()).append(",");
        }
        if (answer.toString().endsWith(",")) {
            answer = new StringBuilder(answer.substring(0, answer.length() - 1));
        }
        answer.append("]");

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(answer.toString());
        out.flush();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

}
