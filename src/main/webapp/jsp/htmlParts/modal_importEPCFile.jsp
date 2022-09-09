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
<div class="modal fade" id="modal_importEPC">
	<script type="module">
		import {__ID_CONSOLE__} from "/ressources/script/modules/common/variables.js"
		import {sendForm} from "/ressources/script/modules/UI/modals/modalEntityManager.js";
		import {resquestValidation} from "/ressources/script/modules/energyml/epcContentManager.js";

		document.getElementById("submit_importEPCFromDisk").onclick = function(){
			sendForm('importEpcFormDisk', 'modal_importEPC', 'rolling_import')
				.then(function(){
					resquestValidation(__ID_CONSOLE__, null);
				});
		}
		document.getElementById("submit_importEPCFromURL").onclick = function(){
			sendForm('importEpcFormURL', 'modal_importEPC', 'rolling_import')
				.then(function(){
					resquestValidation(__ID_CONSOLE__, null);
				});
		}
	</script>

	<div class="modal-dialog modal-dialog-centered">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Import EPC</h4>
				<div id="rolling_import" class="spinner-border text-success" role="status"
					style="display: none">
					<span class="sr-only">Importing...</span>
				</div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>

			<ul class="nav nav-tabs">
			    <li class="nav-item">
			        <a class="nav-link active" data-toggle="tab" href="#importFileFromDisk">import from disk</a>
			    </li>
			    <li class="nav-item">
			        <a class="nav-link" data-toggle="tab" href="#importFileFromURL">import from URL</a>
			    </li>
			</ul>

 			<div class="tab-content">
				<div id="importFileFromDisk" class="container tab-pane active"><br>
					<form id="importEpcFormDisk" name="importEpcForm" method="POST" enctype="multipart/form-data"
						action="FileReciever">
						<div class="modal-body">
							<input type="text" name="import" value="true" hidden="hidden">
							<label  name="epcInputLabel">EPC file path</label> 
							<input class="form-control-file border form-control" type="file" required name="epcInputFile" accept=".epc, .xml" multiple>
						</div>
						<div class="modal-body">
							<input class="btn btn-primary" name="submit" type="button"
								id="submit_importEPCFromDisk"
								value="Import">
						</div>
					</form>
				</div>
				
				<div id="importFileFromURL" class="container tab-pane"><br>
					<form id="importEpcFormURL" name="importEpcForm" method="POST" enctype="multipart/form-data"
						action="FileReciever">
						<div class="modal-body">
							<input id="import" name="import" value="true" hidden="hidden">
							<label name="epcInputLabel">EPC file url location</label> 
							<input class="form-control-file border" type="url" 
       										placeholder="https://example.com?file=myEpc.epc" name="epcInputURL">
						</div>
						<div class="modal-body">
							<input class="btn btn-primary" name="submit" type="button"
								id="submit_importEPCFromURL"
								value="Import">
						</div>
					</form>
				</div>
			</div>
			
		</div>
	</div>
</div>