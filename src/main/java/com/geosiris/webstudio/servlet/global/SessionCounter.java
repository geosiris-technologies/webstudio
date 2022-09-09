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
package com.geosiris.webstudio.servlet.global;

import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.concurrent.atomic.AtomicInteger;

public class SessionCounter implements HttpSessionListener {

	private static final AtomicInteger activeSessions = new AtomicInteger();

    public SessionCounter() {
        super();
    }

    public static int getActiveSessions() {
        return activeSessions.get();
    }
    
    public static void newConnexion() {
    	activeSessions.incrementAndGet();
    }

    public void sessionCreated(final HttpSessionEvent event) {
    }
    
    public void sessionDestroyed(final HttpSessionEvent event) {
    	if(event.getSession().getAttribute(SessionUtility.SESSION_USER_NAME) != null
    			&& (event.getSession().getAttribute(SessionUtility.SESSION_USER_NAME)+"").length() > 0) {
    		activeSessions.decrementAndGet();
    	}
    }

}
