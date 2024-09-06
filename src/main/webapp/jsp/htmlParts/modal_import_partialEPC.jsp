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
<div class="modal fade" id="modal_import_partialEPC">
	<script type="module">
		import {__ID_CONSOLE__} from "/ressources/script/modules/common/variables.js"
		import {sendForm} from "/ressources/script/modules/UI/modals/modalEntityManager.js";
		import {resquestValidation, epc_partial_importer} from "/ressources/script/modules/energyml/epcContentManager.js";
		
		document.getElementById("import_epc_erase_workspace").onchange = function (event){
			document.querySelector("#importEpcFormDisk input[name='import']").value = "" + (! event.target.checked);
			document.querySelector("#importEpcFormURL input[name='import']").value = "" + (! event.target.checked);
		}

		document.getElementById("submit_importEPCFromDisk").onclick = function(){
			sendForm('importEpcFormDisk', 'modal_import_partialEPC', 'rolling_import_partialEPC', false, false, true, true)
				.then(function(){
					resquestValidation(__ID_CONSOLE__, null);
				});
		}
		document.getElementById("submit_importEPCFromURL").onclick = function(){
			sendForm('importEpcFormURL', 'modal_import_partialEPC', 'rolling_import_partialEPC', false, false, true, true)
				.then(function(){
					resquestValidation(__ID_CONSOLE__, null);
				});
		}

		/* Partial import */
		const inputFile = document.getElementById("import_partialEPC_FromDisk_inputFile");
		const inputFileContent = document.getElementById("import_partialEPC_FromDisk_inputFile_content");
		inputFile.onchange = function(){
			while(inputFileContent.firstChild){
                inputFileContent.firstChild.remove();
            }
            epc_partial_importer(inputFile, inputFileContent, __ID_CONSOLE__, 
            	() => {
            		return document.getElementById("import_epc_erase_workspace").checked;
            	}
            );
		}
	</script>

	<div class="modal-dialog modal-dialog-centered modal-xl-customGeosiris">
		<div class="modal-content" style="overflow: auto;">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Import file in workspace</h4>
				<div id="rolling_import_partialEPC" class="spinner-border text-success" role="status"
					style="display: none">
					<span class="sr-only">Importing...</span>
				</div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
			</div>

			<div class="form-check">
				<input type="checkbox" class="form-check-input" id="import_epc_erase_workspace">
				<label class="form-check-label">Erase workspace content (all current data will be lost !)</label>
			</div>

			<ul class="nav nav-tabs">
			    <li class="nav-item">
			        <a class="nav-link active" data-bs-toggle="tab" href="#importFileFromDisk">Import EPC from disk</a>
			    </li>
			    <li class="nav-item">
			        <a class="nav-link" data-bs-toggle="tab" href="#import_partialEPC_FromURL">Import EPC from URL</a>
			    </li>
			    <li class="nav-item">
			        <a class="nav-link" data-bs-toggle="tab" href="#import_partialEPC_FromDisk">Import Partial EPC from disk</a>
			    </li>
			</ul>

 			<div class="tab-content">
 				<div id="importFileFromDisk" class="container tab-pane active"><br>
					<form id="importEpcFormDisk" name="importEpcForm" method="POST" enctype="multipart/form-data"
						action="FileReciever">
						<div class="modal-body">
							<input type="text" name="import" value="true" hidden="hidden">
							<label  name="epcInputLabel">EPC file path</label> 
							<input class="form-control border" type="file" required name="epcInputFile" accept=".epc, .xml" multiple>
						</div>
						<div class="modal-body">
							<input class="btn btn-primary" name="submit" type="button"
								id="submit_importEPCFromDisk"
								value="Import">
						</div>
					</form>
				</div>
				
				<div id="import_partialEPC_FromURL" class="container tab-pane"><br>
					<form id="importEpcFormURL" name="importEpcForm" method="POST" enctype="multipart/form-data"
						action="FileReciever">
						<div class="modal-body">
							<input type="text" name="import" value="true" hidden="hidden">
							<label name="epcInputLabel">EPC file url location</label> 
							<input class="form-control border" type="url" 
       										placeholder="https://example.com?file=myEpc.epc" name="epcInputURL">
						</div>
						<div class="modal-body">
							<input class="btn btn-primary" name="submit" type="button"
								id="submit_importEPCFromURL"
								value="Import">
						</div>
					</form>
				</div>

				<div id="import_partialEPC_FromDisk" class="container tab-pane"><br>
					<div class="modal-body">
						<label name="epcInputLabel">EPC file</label> 
						<input id="import_partialEPC_FromDisk_inputFile" class="form-control border" type="file" name="epcInputURL" multiple>
						<p>Element highlighted are already present in your workspace</p>
					</div>
					<div class="modal-body" id="import_partialEPC_FromDisk_inputFile_content">
					</div>
				</div>
			</div>
			
		</div>
	</div>
</div>