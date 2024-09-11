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
<%@ include file="/jsp/htmlParts/modal_import_partialEPC.jsp"%>
<%@ include file="/jsp/htmlParts/modal_closeEPCFile.jsp"%>
<%@ include file="/jsp/htmlParts/modal_exportEPCFile.jsp"%>
<%@ include file="/jsp/htmlParts/modal_createNewRootElement.jsp"%>
<%@ include file="/jsp/htmlParts/modal_changeDorReference.jsp"%>
<%@ include file="/jsp/htmlParts/modal_PropertiesDictVue.jsp"%>
<%@ include file="/jsp/htmlParts/modal_WorkspaceDictVue.jsp"%>
<%@ include file="/jsp/htmlParts/resqmlGraphView.jsp"%>
<%@ include file="/jsp/htmlParts/modal_bugReport.jsp"%>
<%@ include file="/jsp/htmlParts/modal_wellcome.jsp"%>
<%@ include file="/jsp/htmlParts/modal_file_editor.jsp"%>
<%@ include file="/jsp/htmlParts/other_modals.jsp"%>

<%@ include file="/jsp/htmlParts/modal_FIRPView.jsp"%>
<%@ include file="/jsp/htmlParts/modal_ETP.jsp"%>

<%@ include file="/jsp/htmlParts/modal_3D_view.jsp"%>

<!-- <%@ include file="/jsp/htmlParts/modal_getOSDUManifest.jsp"%> -->

<style type="text/css">
    <%@ include file ="/ressources/css/modal.css"%>
</style>

<script type="module">
	import {__ID_CONSOLE__} from "/ressources/script/modules/common/variables.js";
	import {resquestCorrection, resquestValidation} from "/ressources/script/modules/energyml/epcContentManager.js";
	import {saveAllResqmlObjectContent} from "/ressources/script/modules/requests/uiRequest.js";
	import {reverseVueOrientation} from "/ressources/script/modules/UI/ui.js";
	import {refreshWorkspace} from "/ressources/script/modules/UI/eventHandler.js"

	document.getElementById("action-correction-dor").onclick = function(){
		resquestCorrection(__ID_CONSOLE__, null, 'dor');
	}
	document.getElementById("action-correction-versionString").onclick = function(){
		resquestCorrection(__ID_CONSOLE__, null, 'versionString');
	}
	document.getElementById("action-correction-schemaVersion").onclick = function(){
		resquestCorrection(__ID_CONSOLE__, null, 'SchemaVersion');
	}
	document.getElementById("but_reverse_vue").onclick = function(){
		reverseVueOrientation();
	}
	document.getElementById("but_save_all_Open_Objects").onclick = function(){
		saveAllResqmlObjectContent();
	}
	document.getElementById("but_validate_workspace").onclick = function(){
		resquestValidation(__ID_CONSOLE__, null);
	}
	document.getElementById("action-load-sample-epc").onclick = function(){
		const formData  = new FormData();
		formData.append("loadDefault", "true");
		fetch("FileReciever", {
			method: 'POST',
			body: formData
		}).then( (response) => {
			refreshWorkspace();
		});
	}
</script>

<nav class="navbar navbar-expand-sm fixed-top navbar-light bg-light">
	<div class="collapse navbar-collapse" id="navbar1">
		<ul class="navbar-nav">
			<li class="nav-item dropdown">
				<a class="nav-link dropdown-toggle" href="#" id="navbarDropdown"
				role="button" data-bs-toggle="dropdown" aria-haspopup="true"
				aria-expanded="false"> File </a>
				<ul class="dropdown-menu" aria-labelledby="navbarDropdown">
                    <li><a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#modal_import_partialEPC">Import file in workspace (EPC/XML/...) [ctrl + i]</a></li>
                    <li><a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#modal_exportEPC">Export workspace to EPC file [ctrl + e]</a></li>
                    <li><a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#modal_WorkspaceDict">Show workspace tree</a></li>

                    <li><div class="dropdown-divider"></div></li>

                        <li><a class="dropdown-item" id="but_validate_workspace" onclick="">Validate workspace</a></li>
                            <li><a class="dropdown-item" id="but_save_all_Open_Objects">Save all open elements [ctrl + s]</a></li>
                                <li><a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#modal_closeEPC">Clear workspace (remove all loaded data)</a></li>

                                    <li><div class="dropdown-divider"></div></li>

                                    <li><a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#modal_3D_view">3D View</a></li>
                                    <li><a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#modal_FIRPView">FIRP view</a></li>
                                    <li><a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#modal_ETP">ETP requests</a></li>
                                    <li><a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#modal_PropertiesDict">Show Common properties dictionary</a></li>
                                    <li><div class="dropdown-divider"></div></li>

                </ul>
			</li>
		</ul>
		<ul class="navbar-nav">
			<li class="nav-item dropdown">
				<a class="nav-link dropdown-toggle" href="#" id="navbarDropdown_Operations"
				role="button" data-bs-toggle="dropdown" aria-haspopup="true"
				aria-expanded="false"> Edit </a>
				<div class="dropdown-menu" aria-labelledby="navbarDropdown_Operations">
					<a class="dropdown-item" data-bs-toggle="modal" data-bs-target="#modal_changeDorReference">Change DOR references</a>
					<a class="dropdown-item" id="action-correction-dor">Auto-correct DOR informations</a>
					<a class="dropdown-item" id="action-correction-versionString">Remove VersionString</a>
					<a class="dropdown-item" id="action-correction-schemaVersion">Correct SchemaVersion</a>
					
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" id="action-load-sample-epc">Load sample EPC file</a>
				</div>
			</li>
            <!-- <li> <button onclick="testFun()">Test</button> </li> -->
		</ul>

		<ul class="navbar-nav ">
			<li  class="nav-item">
				<a data-bs-toggle="collapse" class="nav-link" id="rws__CL_CONSOLE_TOGGLE_BUT"
				data-bs-target="#rws__ID_CONSOLE_MODAL__" 
				onclick='if(this.textContent.includes("Hide")){this.textContent = "Show Console";}else{this.textContent = "Hide Console";}'>Show Console</a> 
			</li>
			<li  class="nav-item">
				<a data-bs-toggle="modal"  class="nav-link"
				data-bs-target="#modal_resqmlGraphView">EPC graph</a>
			</li>
		</ul>

		<ul class="navbar-nav me-auto">
			<li class="nav-item dropdown">
				<a class="nav-link dropdown-toggle" href="#" id="navbarDropdown_About"
				role="button" data-bs-toggle="dropdown" aria-haspopup="true"
				aria-expanded="false"> About </a>
				<div class="dropdown-menu" aria-labelledby="navbarDropdown_About">
					<a class="nav-link dropdown-item fab fa-github" href="https://github.com/geosiris-technologies/webstudio-platform" target="_blank">WebStudio platform</a>
					<a class="nav-link dropdown-item fab fa-github" href="https://github.com/geosiris-technologies/webstudio" target="_blank">WebStudio</a>
				</div>
			</li>
		</ul>

        <ul class="navbar-nav me-auto">
        	<li class="nav-item navbar-text" id="sessionCounter_item">
        		<span class="nav-link fas fa-users" style="display: none" id="sessionCounter" title="Other connected users count"></a>
        		</li>
        		<li class="nav-item active">
        			<h2 class="ws_title" id="ws_top_title">Energyml WebStudio</h2>
        		</li>
        		<li class="nav-item navbar-text">
        			<div id="taskInProgressRoling" class="spinner-border text-success"
        			role="status" style="display: none">
        			<span class="sr-only">Loading...</span>
        		</div>
        	</li>
        </ul>
        <ul class="navbar-nav me-2">
        	<li class="nav-item"><img alt="img_geosiris" height=30
        		src="/ressources/img/logos/logo_geosiris.png">
        	</li>
        	<li class="nav-item">
        		<a class="nav-link" href="http://geosiris.com" target="_blank">G&eacute;osiris</a>
        	</li>
        	<li class="nav-item"><img alt="img_XLIM" height=30
        		src="https://www.xlim.fr/sites/default/files/xlim_logotype_rvb.png">
        	</li>
        	<li class="nav-item">
        		<a class="nav-link" href="https://xlim-sic.labo.univ-poitiers.fr/jerboa" target="_blank">Jerboa</a>
        	</li>

        	<li class="nav-item">
				<span class="navbar-text" id="ws_sessionMetrics"></span>
			</li>
          
			<a class="nav-link" data-bs-toggle="modal" data-bs-target="#modal_bug_report"><span class="fas fa-bug"></span> Report bug</a>
			
      <li class="nav-item dropdown" id="sessionInfo">
				<a class="nav-link dropdown-toggle fas fa-user" href="#"
				role="button" data-bs-toggle="dropdown" aria-haspopup="true"
				aria-expanded="false"
				id="sessionInfoBut"></a>
				<div class="dropdown-menu dropdown-menu-right" aria-labelledby="navbarDropdown" id="sessionInfoMenu">
					<div>WebStudio v${GEOSIRIS_ENV_WEBSTUDIO_VERSION}-${GEOSIRIS_ENV_PRODUCTION_TYPE}-${GEOSIRIS_ENV_CONFIG_TYPE}</br><div>
					<div class="dropdown-divider"></div>
					<div><a id="but_reverse_vue" class="dropdown-item">Reverse vue</a><div>
					<div class="dropdown-divider"></div>
				</div>
			
			</li>
		</ul>
	</div>
</nav>