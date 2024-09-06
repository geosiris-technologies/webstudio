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
<div class="modal fade" id="modal_FIRPView" >
	<script type="module">
		import {updateFIRPView} from "/ressources/script/modules/UI/modals/firpVue.js";

		document.getElementById("but_updateFirpView").onclick = function(){
			updateFIRPView();
		}
	</script>
	<div class="modal-dialog modal-dialog-centered modal-xl-customGeosiris">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Relations view</h4>
				<div id="rolling_FIRPView" class="spinner-border text-success" role="status" style="display: none"> </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
				
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
								id="modal_FIRPView_progressBar"></div>
						
					</div>
				
					<div class="container" style=" height: 100%">
 						<div class="bordered-black-1" style="height:5%;">
							<h3>FIRP View</h3>
						</div>
						<div class="bordered-black-1" style="height:5%;display: flex;">
							<div class="bordered-black-1" style="width: 50%">FIRP View</div>
							<div class="bordered-black-1" style="width: 50%">Model View</div>
						</div>
						<div style="height:60%; position: inherit; display: flex;">
							<div class="bordered-black-1"  style="padding: 0px; height: 100%; width: 50%;">
								<div id="modal_FIRPView_treeDiv" class="modal_tree_view"></div>
							</div>
							<div class="bordered-black-1" style="padding: 0px; height: 100%;width: 50%;">
								<div id="modal_ModelView_treeDiv" class="modal_tree_view"></div>
							</div>
						</div>
						<div id="modal_FIRPView_citationView" class="bordered-black-1" style="height:30%; overflow: auto; position: inherit;">
						</div>
					</div>
				</div>
			</div> <!-- modal body -->
			<div class="modal-footer">
				<button type="button" id="but_updateFirpView" class=" btn btn-primary">Update data</button>
			</div>
		</div>
	</div>
</div>