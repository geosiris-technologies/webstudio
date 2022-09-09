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
<!-- Modal -->
<div class="modal fade" id="modal_resqmlGraphView">
	<script type="module">
		import {
    			toggleNodeFromUUID, updateGraphStyle, removeAllNodes, 
    			showOnlyOpened, start_graph, reload_graph
		} from "/ressources/script/modules/UI/graphView.js";

		document.getElementById("cyNodeUuidToAdd").onkeypress = function(event){
			onEnterPressed(event, function(){toggleNodeFromUUID(document.getElementById('cyNodeUuidToAdd').value);} );
		}
		document.getElementById("but_toggleNodeFromUUID").onclick = function(){
			toggleNodeFromUUID(document.getElementById('cyNodeUuidToAdd').value);
		}
		document.getElementById("but_startGraph").onclick = function(){
			start_graph();
		}
		document.getElementById("but_showOnlOpened").onclick = function(){
			showOnlyOpened();
		}
		document.getElementById("but_reloadGraph").onclick = function(){
			reload_graph();
		}
		document.getElementById("cbCyEdgeLabel").onclick = function(){
			updateGraphStyle();
		}
		document.getElementById("cbCyEdgeArrow").onclick = function(){
			updateGraphStyle();
		}
		document.getElementById("but_grapheRemoveAllNodes").onclick = function(){
			removeAllNodes();
		}
	</script>
	<div class="modal-dialog modal-dialog-centered modal-xl-customGeosiris">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">EPC graph</h4>
				<div id="rolling_resqmlGraphView" class="spinner-border text-success" role="status" style="display: none">
					<span class="sr-only">Loading...</span>
				</div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>

 			<div class="tab-content" style="height:90%">
 				<div class="container-fluid">
 					<div class="input-group mb-3">
						<input class="form-control" id="cyNodeUuidToAdd" type="search" 
								placeholder="Add elements by their name or uuid (partials inputs works)"/>
						<div class="input-group-append">
							<button class="btn btn-success" id="but_toggleNodeFromUUID">Go</button>
						</div>
					</div>
 				</div>
 				<div class="container-fluid">
 					<div class="input-group mb-3">
 						<div class="input-group-prepend">
							<button class="btn btn-primary" id="but_startGraph">Update data from EPC</button>
 						</div>
 						<div class="input-group-append">
							<button class="btn btn-info" id="but_showOnlOpened">Show only selected ressources</button>
 						</div>
 						<div class="input-group-append">
							<button class="btn btn-dark" id="but_reloadGraph">Automate nodes position</button>
 						</div>
 						<div class="input-group-append">
 							<div class="input-group-text">
								<div class="custom-control custom-checkbox" style="margin-left:2px; margin-right:2px;">
									<input type="checkbox" class="custom-control-input" id="cbCyEdgeLabel" checked="checked">
									<label class="custom-control-label" for="cbCyEdgeLabel">Show edges label</label>
								</div>
							</div>
						</div>
 						<div class="input-group-append">
 							<div class="input-group-text">
								<div class="custom-control custom-checkbox " style="margin-left:2px; margin-right:2px;">
									<input type="checkbox" class="custom-control-input" id="cbCyEdgeArrow" checked="checked" >
									<label class="custom-control-label" for="cbCyEdgeArrow">Show edges arrow</label>
								</div>
							</div>
						</div>
						<div class="input-group-append">
	    					<div class="input-group-text">
								<span style="margin-left:2px; margin-right:2px;">Left double click action : </span>
								<div class="custom-control custom-radio" style="margin-left: px; margin-right:2px;">
									<input type="radio" class="custom-control-input" id="cy-radio-mode-adding" name="cy-radio-mode" value="adding" checked>
									<label class="custom-control-label" for="cy-radio-mode-adding">Add neighborhood</label>
								</div>
								<div class="custom-control custom-radio" style="margin-left:2px; margin-right:2px;">
									<input type="radio" class="custom-control-input" id="cy-radio-mode-removing" name="cy-radio-mode" value="removing">
									<label class="custom-control-label" for="cy-radio-mode-removing">Remove node</label>
								</div>
							</div>
						</div>
 						<div class="input-group-append">
	    					<div class="input-group-text">
								<span style="margin-left:2px; margin-right:2px;">Right double click action : open object view</span>
							</div>
 						</div>
 						<div class="input-group-append">
							<button class="btn btn-danger" id="but_grapheRemoveAllNodes">Clear graph</button>
 						</div>
 					</div>
 				</div>

 				<div class="container-fluid" style="height:85%">
 					<div class="row" style="height:100%">
		 				<!-- <div class="col-xl-9" style="height:100%">
							<div id="cyGrapher"></div>						
		 				</div>
		 				<div class="col-xl-3" id="cyLister" style="height:100%">
							<div id="graphElementChecked"></div>
						</div> -->
						<div id="cyGrapher"></div>
						<div id="graphElementChecked"></div>
					</div>
				</div>
			<p>Graph powered with <a href="https://js.cytoscape.org/" target="blank">Cytoscape</a><p>
			</div>

		</div>
	</div>
</div>