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
<script type="module">
	import {__ID_CONSOLE__} from "/ressources/script/modules/common/variables.js";
	import {sendForm} from "/ressources/script/modules/UI/modals/modalEntityManager.js";
	import {resquestValidation} from "/ressources/script/modules/energyml/epcContentManager.js";
	
	document.getElementById("submit_load_epc_distant").onclick = function(){
		sendForm('loadEpcForm_URL', 'modal_loadEPC', 'rolling_load', true).then(
									function(){resquestValidation(__ID_CONSOLE__, null);});
	}
	document.getElementById("submit_load_epc_local").onclick = function(){
		sendForm('loadEpcForm_DISK', 'modal_loadEPC', 'rolling_load', true).then(
									function(){resquestValidation(__ID_CONSOLE__, null);});
	}
</script>

<div class="modal fade" id="modal_loadEPC">
	<div class="modal-dialog modal-dialog-centered">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Load EPC</h4>
				<div id="rolling_load" class="spinner-border text-success" role="status"
					style="display: none">
					<span class="sr-only">Loading...</span>
				</div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>

			
			<ul class="nav nav-tabs">
			    <li class="nav-item">
			        <a class="nav-link active" data-toggle="tab" href="#loadFileFromDisk">Load from disk</a>
			    </li>
			    <li class="nav-item">
			        <a class="nav-link" data-toggle="tab" href="#loadFileFromURL">Load from URL</a>
			    </li>
			</ul>

 			<div class="tab-content">
				<div id="loadFileFromDisk" class="container tab-pane active"><br>
					<form id="loadEpcForm_DISK" name="loadEpcForm" method="POST" enctype="multipart/form-data"
						action="FileReciever">
						<div class="modal-body form-group">
							<label for="epcLOADInputFile" id="epcInputLabel" name="epcInputLabel">EPC file path</label> 
							<input class="form-control-file border" type="file" required id="epcLOADInputFile" name="epcInputFile" accept=".epc, .xml, .h5" multiple>
							<!-- <input class="form-control-file border" type="text" id="epcInputURL" name="epcInputURL"> -->
						</div>
						<div class="modal-body form-group">
							<input id="submit_load_epc_local" class="btn btn-primary" type="button" name="submit" value="Load">
						</div>
					</form>
				</div>
				
				<div id="loadFileFromURL" class="container tab-pane"><br>
					<form id="loadEpcForm_URL" name="loadEpcForm" method="POST" enctype="multipart/form-data"
						action="FileReciever">
						<div class="modal-body form-group">
							<label for="epcLOADInputURL" id="epcInputLabel" name="epcInputLabel">EPC file url location</label> 
							<!-- <input class="form-control-file border" type="file" id="epcInputFile" name="epcInputFile" accept=".epc"> -->
							<input class="form-control-file border" type="url" 
       										placeholder="https://example.com?file=myEpc.epc" id="epcLOADInputURL" name="epcInputURL">
						</div>
						<div class="modal-body form-group">
							<!-- <input class="btn btn-primary" type="submit"
								onclick="getElementById('rolling_load').style.display='';" value="Load"> -->
								<input name="submit" id="submit_load_epc_distant" class="btn btn-primary" type="button" value="Load">
						</div>
					</form>
				</div>
			</div>
			
		</div>
	</div>
	
</div>