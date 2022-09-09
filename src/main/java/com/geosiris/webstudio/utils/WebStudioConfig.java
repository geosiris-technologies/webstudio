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

import java.util.ArrayList;
import java.util.List;

public class WebStudioConfig {
	public static final String CONFIG_FILE_PATH_VAR_NAME = "WS_CONFIG_INI_FILE_PATH";


	public static List<String> getExcludedPackagesNames() {
		// ArrayList<String> excludedPkg = new ArrayList<String>();
		// if (ENV_VAR_PRODUCTION_TYPE.compareToIgnoreCase("restricted") == 0) {
		// excludedPkg.add("energyml.common2_3");
		// excludedPkg.add("energyml.common2_2");
		// excludedPkg.add("energyml.common_dev3x_2_3");
		// excludedPkg.add("energyml.resqml_dev3x_2_2");
		// excludedPkg.add("energyml.resqml_dev5x_2_2");
		// excludedPkg.add("energyml.resqml2_2");
		// excludedPkg.add("energyml.witsml2_2");
		// excludedPkg.add("geosiris.proposal2_2");
		// }
		// return excludedPkg;
		return new ArrayList<>();
	}
}
