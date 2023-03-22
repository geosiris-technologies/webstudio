<!--
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
-->
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page import ="java.util.Map" %>
<% 
	String userName = (String) request.getAttribute("login");
	String userGrp  = (String) request.getAttribute("user_grp");
	String GEOSIRIS_ENV_PRODUCTION_TYPE = (String) request.getAttribute("GEOSIRIS_ENV_PRODUCTION_TYPE");
	String GEOSIRIS_ENV_CONFIG_TYPE = (String) request.getAttribute("GEOSIRIS_ENV_CONFIG_TYPE");
%>

<%!
public Boolean restrictedExperiment(String prod_type){
	return prod_type.compareToIgnoreCase("full") != 0;
}
%>

<!DOCTYPE html>
<html lang="en-US">
<head>

<meta charset="UTF-8">

<title>Resqml Editor</title>

<%@ include file="/jsp/htmlParts/graphViewIncludes.jsp" %>
<%@ include file="/jsp/htmlParts/bootStrapIncludes.jsp" %>

<link rel="stylesheet" type="text/css" href="ressources/css/table.css" media="all"/>
<link rel="stylesheet" type="text/css" href="ressources/css/epcView.css" media="all"/>
<link rel="stylesheet" type="text/css" href="ressources/css/etp.css" media="all"/>
<link rel="stylesheet" type="text/css" href="ressources/css/console.css" media="all"/>
<link rel="stylesheet" type="text/css" href="ressources/css/snackbar.css" media="all"/>
<link rel="stylesheet" type="text/css" href="ressources/css/vue3D.css" media="all"/>
<link rel="stylesheet" type="text/css" href="ressources/css/geoThreeJS.css" media="all"/>

<% 
	// TODO : changer pour faire des fichiers specifiques pour les tests plutot que mettre cette variable
	out.print("<script>var GEOSIRIS_ENV_PRODUCTION_TYPE='"+GEOSIRIS_ENV_PRODUCTION_TYPE+"';</script>");
%>

<script type="module">
	import {initWebStudioView} from "/ressources/script/modules/main.js";
	import {filterTable} from "/ressources/script/modules/UI/table.js";
	import {onEnterPressed} from "/ressources/script/modules/UI/htmlUtils.js";
	import {setUserName, initSessionTimeOut} from "/ressources/script/modules/UI/ui.js";

	$(window).on('load', initWebStudioView);

	document.getElementById("filter_tableFilter_EPCView").onclick = function(){
		filterTable('epcTableContent', document.getElementById('tableFilter_EPCView').value, document.getElementById('caseSenstive_EPCView').checked);
	}
	document.getElementById("tableFilter_EPCView").onkeypress = function(event){
		onEnterPressed(event, function(){filterTable('epcTableContent', document.getElementById('tableFilter_EPCView').value, document.getElementById('caseSenstive_EPCView').checked);} );	
	}

	document.getElementById("span_tabulationScroller_left").onclick = function(event){
		document.getElementById(__ID_EPC_TABS_HEADER__).scrollLeft -= 30;
	}
	document.getElementById("span_tabulationScroller_right").onclick = function(event){
		document.getElementById(__ID_EPC_TABS_HEADER__).scrollLeft += 30;
	}


	$(document).ready(function() {
		// Initialisation du timeout
		var spanTimeout = document.createElement("span");
		spanTimeout.id = "spanTimeout";
		spanTimeout.className = "dropdown-item ";
		document.getElementById("sessionInfoMenu").appendChild(spanTimeout);
		initSessionTimeOut(spanTimeout.id);


		// Initialisation du compteur de session
		var spanSessionCounter = document.createElement("span");
		spanSessionCounter.id = "sessionCounter";
		spanSessionCounter.className = "dropdown-item ";
		document.getElementById("sessionInfoMenu").appendChild(spanSessionCounter);
		
		<% if(userName==null || userName.length() <= 0){ %>

		<%
		}else{
			// Initialisation du timeout
			out.print("setUserName('"+userName+"', '"+userGrp+"', 'sessionCounter');");
			//out.print("initSessionTimeOut(null);");
		}
		%>

	});
</script>

</head>

<body>

	<%@ include file="/jsp/htmlParts/bandeau.jsp" %>

	<div id="page-loader" style="width: 100%; height:100%; text-align: center; margin-top: 25vh; font-size: xx-large;">
	    <div class="spinner-border" style="width: 30rem; height: 30rem;" role="status">
	      <span class="sr-only" style="width: 100px; height: 100px;">Loading...</span>
	    </div>
	    <p>loading</p>
	</div>


	<div id="main" style="display: none;">
		<div id="mainSplitter" class="row gutters" >
					
			<div id="workspaceObjectListPanel" class="col vue-left-part">
				<div>
	 				<div class="container-fluid">
						<div class="input-group mb-3">
							<div class="input-group-prepend">
								<button class="btn btn-primary form-control" style="padding: revert;" data-toggle="modal" data-target="#modal_createRootElt">
									<span class="fas fa-plus-circle" style="font-size: 20pt;" title="Create new root element"></span>
								</button>
							</div>
							<input class="form-control" id="tableFilter_EPCView" type="search" placeholder="Filter by title, uuid, type or version" /> 
							<span id="searchclear" class="inputTextClearBut fas fa-times-circle" 
									onclick="document.getElementById('tableFilter_EPCView').value=''; document.getElementById('filter_tableFilter_EPCView').click();"
									onmouseover="this.className = this.className.replace(/fas/g,'far')"
									onmouseout="this.className = this.className.replace(/far/g,'fas')"
							></span>
							<div class="input-group-append">
								<div class="input-group-text form-control">
									<div class="custom-control custom-checkbox">
										<input type="checkbox" class="custom-control-input" id="caseSenstive_EPCView" >
										<label class="custom-control-label" for="caseSenstive_EPCView">Case sensitive</label>
									</div>
								</div>
							</div>
							<div class="input-group-append">
								<button class="btn btn-success form-control" 
									id="filter_tableFilter_EPCView">Go
								</button>
							</div>
						</div>
					</div>
				</div>
				<div id="resqmlObjectstableDiv" rws_update_id="__ID_EPC_TABLE_DIV__" class="scrollbar-ripe-malinka"><!-- Here resqml table --></div>
			</div>

			<div id="objectsVuePanel" class="col vue-right-part">
				<span class="fas fa-chevron-left tabulationScroller tabulationScroller_left" id="span_tabulationScroller_left"></span>

				<!-- conteneur du menu de tabulation -->
				<ul id="___" rws_update_id="__ID_EPC_TABS_HEADER__" class="nav nav-tabs" role="tablist"></ul> 

				<span class="fas fa-chevron-right tabulationScroller tabulationScroller_right" id="span_tabulationScroller_right"></span>
				<div rws_update_id="__ID_EPC_TABS_CONTAINER__" class="tab-content"></div>
			</div>
			<!-- <div id="logConsole" class="collapse">
				coucou
			</div> -->
		</div> 
	</div>
		
    
    
	<%@ include file="/jsp/htmlParts/console.jsp" %>

</body>
</html>