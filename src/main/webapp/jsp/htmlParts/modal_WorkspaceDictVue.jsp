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
<div class="modal fade" id="modal_WorkspaceDict" >
	<script type="module">
		import {filterJsonDictUI, refreshWorkspaceDictVue} from "/ressources/script/modules/UI/modals/propertiesVue.js";

		document.getElementById("FilterWorkspaceDict").onkeypress = function(event){
			onEnterPressed(event, function(){
									filterJsonDictUI('container_WorkspaceDict', 
															"counter_WorkspaceDict", 
															document.getElementById('FilterWorkspaceDict').value, 
															document.getElementById('caseSenstive_WorkspaceDict').checked, 
															document.getElementById('splitWords_WorkspaceDict').checked);
								});
		}
		document.getElementById("but_FilterWorkspaceDict").onclick = function(event){
			filterJsonDictUI('container_WorkspaceDict', 
									"counter_WorkspaceDict", 
									document.getElementById('FilterWorkspaceDict').value, 
									document.getElementById('caseSenstive_WorkspaceDict').checked, 
									document.getElementById('splitWords_WorkspaceDict').checked);
		}
		document.getElementById("but_updateWorkspaceDict").onclick = function(event){
			refreshWorkspaceDictVue();
		}
		
		document.getElementById("FilterWorkspaceDict_clear").onclick = function(){
			document.getElementById('FilterWorkspaceDict').value=''; document.getElementById('but_FilterWorkspaceDict').click();
		}
		document.getElementById("FilterWorkspaceDict_clear").onmouseover = function(event){
			event.target.className = event.target.className.replace(/fas/g,'far')
		}
		document.getElementById("FilterWorkspaceDict_clear").onmouseout = function(event){
			event.target.className = event.target.className.replace(/far/g,'fas')
		}

		// Auto refresh on modal show
		$("#modal_WorkspaceDict").on('show.bs.modal', function(){
		       refreshWorkspaceDictVue();
		});
	</script>
	<div class="modal-dialog modal-dialog-centered modal-xl-customGeosiris">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Workspace tree</h4>
				<div id="rolling_WorkspaceDict" class="spinner-border text-success" role="status" style="display: none"> </div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				
			</div>

			<div class="modal-body" style="overflow: hidden;">
	 			<div class="tab-content" style="height: 100%">
					<div>
						<div class="progress-bar progress-bar-striped progress-bar-animated bg-success" 
								role="progressbar" 
								style="height: 15px; display: none" 
								aria-valuenow="100" 
								aria-valuemin="0" 
								aria-valuemax="100"
								id="modal_WorkspaceDict_progressBar"></div>
						
					</div>
				
					<div class="container-fluid">
						<div class="input-group mb-3">
							<div class="input-group-prepend">
								<span class="form-control" style="max-width: 160px;">Nb elements :</span>
							</div>
							<span class="form-control" id="counter_WorkspaceDict" style="max-width: 100px;">0</span>
							<span class="form-control" style="max-width: 190px;">Filter contents :</span>
							<input class="form-control" id="FilterWorkspaceDict" type="search" placeholder="Filter by content" /> 
							<span class="inputTextClearBut fas fa-times-circle" id="FilterWorkspaceDict_clear"></span>
							<div class="input-group-append">
								<div class="input-group-text form-control">
									<div class="custom-control custom-checkbox">
										<input type="checkbox" class="custom-control-input" id="splitWords_WorkspaceDict" >
										<label class="custom-control-label" for="splitWords_WorkspaceDict">Split phrase in words</label>
									</div>
								</div>
							</div>
							<div class="input-group-append">
								<div class="input-group-text form-control">
									<div class="custom-control custom-checkbox">
										<input type="checkbox" class="custom-control-input" id="caseSenstive_WorkspaceDict" >
										<label class="custom-control-label" for="caseSenstive_WorkspaceDict">Case sensitive</label>
									</div>
								</div>
							</div>
							<div class="input-group-append">
								<button class="btn btn-success form-control" 
									id="but_FilterWorkspaceDict">Go
								</button>
							</div>
						</div>
					</div>
					<div class="containerJsonTree" id="container_WorkspaceDict"></div>
				</div>
			</div> <!-- modal body -->
			<div class="modal-footer">
				<button type="button" id="but_updateWorkspaceDict" class=" btn btn-primary">Update data</button>
			</div>
		</div>
	</div>
</div>