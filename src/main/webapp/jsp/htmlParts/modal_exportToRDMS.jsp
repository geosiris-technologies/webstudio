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
<div class="modal fade" id="modal_exportToRDMS">
	<div class="modal-dialog modal-dialog-centered modal-xl">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Export to RDMS</h4>
				<div id="rolling_ExportToRDMS" class="spinner-border text-success" role="status" style="display: none">
					<span class="sr-only">Exporting...</span>
				</div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>

			<div class="tab-content">
				<div class="container tab-pane active"><br>
					<div>
						<div class="custom-control custom-checkbox">
							<input type="checkbox" class="custom-control-input" name="checkUpRelations" id="epcExportToRDMS_checkUpRelations" checked>
							<label class="custom-control-label" for="epcExportToRDMS_checkUpRelations">Auto-check upward relations</label>
						</div>
						<div class="custom-control custom-checkbox">
							<input type="checkbox" class="custom-control-input" name="checkDownRelations" id="epcExportToRDMS_checkDownRelations" >
							<label class="custom-control-label" for="epcExportToRDMS_checkDownRelations">Auto-check downward relations</label>
						</div>
						
						
						<div class="progress-bar progress-bar-striped progress-bar-animated bg-success" 
						role="progressbar" 
						style="width: 100%;height: 15px;" 
						aria-valuenow="100" 
						aria-valuemin="0" 
						aria-valuemax="100"
						id="exportToRDMS_progressBar"></div>
						
					</div>
					<form id="exportToRDMSForm" name="exportToRDMSForm" method="POST" action="ExportToRDMS" enctype="multipart/form-data">

						<div class="modal-body tableInModal" id="ExportToRDMS_EPC_table" ></div>
						
						<hr/>

						<label name="exportToRDMS_lbl_serverURI" for="exportToRDMS_serverURI">Serveur URI</label> 
						<input class="form-control" type="text" required id="exportToRDMS_serverURI" name="serverURI" style="width:100%" />

						
					</form>
					<input class="btn btn-primary" name="submit" type="submit" value="Export" 
							onclick="sendForm('exportToRDMSForm', 'modal_exportToRDMS', 'rolling_ExportToRDMS', false, false)">
				</div>
			</div>
		</div>
	</div>
</div>


<script>

function createExportToRDMSView(jsonContent, checkboxesName, f_OnCheckToggle){
	document.getElementById("exportToRDMS_serverURI").value = "http://" + window.location.hostname + ":3999"
	var tableDOR = createTableFromData(
		jsonContent, 
		["num", "title", "type", "uuid", "schemaVersion"], 
		["Num", "Title", "Type", "UUID", "SchemaVersion"], 
		null, null, null);

	transformTabToFormCheckable(tableDOR, jsonContent.map(elt => elt.uuid), checkboxesName, f_OnCheckToggle);
	return tableDOR;
}

function updateExportToRDMSTableContent(relations){
	document.getElementById("exportToRDMS_progressBar").style.display ="";
	var tableContent = [];
	for(uuid in relations){
		tableContent.push(relations[uuid]);
	}

	const checkboxesName = "ExportToRDMSUUID";

	// on doit convertir en liste pour creer le tableau
	const table = createExportToRDMSView(tableContent, checkboxesName, 
		function(checkedValue, uuid){
			if(checkedValue && relations[uuid] != null){
				checkAllRelations(uuid, relations, checkboxesName, checkedValue, 
                                    "exportToRDMS_progressBar", 
                                    "epcExportToRDMS_checkUpRelations", 
                                    "epcExportToRDMS_checkDownRelations");
			}else{
								//console.log("err > checkedValue " + checkedValue  + " -- " + uuid);
							}
						}
						);

	
	const elt_ExportToRDMS_EPC_table = document.getElementById("ExportToRDMS_EPC_table");
	elt_ExportToRDMS_EPC_table.appendChild(table);
	refreshHighlightedOpenedObjects();
	document.getElementById("exportToRDMS_progressBar").style.display ="none";
}


$("#modal_exportToRDMS").on('show.bs.modal', function(){
   	// OnModalShow
   	// On met a jour le tableau
   	document.getElementById("exportToRDMS_progressBar").style.display = "";
   	const elt_ExportToRDMS_EPC_table = document.getElementById("ExportToRDMS_EPC_table");
   	while (elt_ExportToRDMS_EPC_table.firstChild) {
   		elt_ExportToRDMS_EPC_table.removeChild(elt_ExportToRDMS_EPC_table.firstChild);
   	}

   	var xmlHttp = new XMLHttpRequest();
   	xmlHttp.open( "GET", "ResqmlEPCRelationship", true );

   	xmlHttp.onload = function(){
   		try{
   			const relations = JSON.parse(xmlHttp.responseText);
   			updateExportToRDMSTableContent(relations);
   		}catch(e){
            console.log(e);
            console.log(">>>");
   			console.log(xmlHttp.responseText);
   		}
   	};
   	xmlHttp.send(null);
   });
</script>