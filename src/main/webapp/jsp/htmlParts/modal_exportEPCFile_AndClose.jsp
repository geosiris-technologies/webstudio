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
<div class="modal fade" id="modal_exportEPCAndClose">
	<script type="module">
		import {exportAndClose} from "/ressources/script/modules/requests/uiRequest.js";
		import {sendGetURLAndReload} from "/ressources/script/modules/UI/eventHandler.js";

		document.getElementById("but_EPC_exportYes").onclick = function(){
			document.getElementById('closeBut_exportAndClose').click(); exportAndClose(document.getElementById('epcFilePath_AndClose').value);
		}
		document.getElementById("but_EPC_exportNo").onclick = function(){
			document.getElementById('closeBut_exportAndClose').click(); sendGetURLAndReload('FileReciever?close=true', false);
		}
	</script>
	<div class="modal-dialog modal-dialog-centered">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">export EPC</h4>
				<div id="rolling_export_AndClose" class="spinner-border text-success" role="status"
					style="display: none">
					<span class="sr-only">exporting...</span>
				</div>
				<button id="closeBut_exportAndClose" type="button" class="close" data-dismiss="modal">&times;</button>
			</div>

			<ul class="nav nav-tabs">
			    <li class="nav-item">
			        <a class="nav-link active" data-toggle="tab" href="#exportFileToDisk_AndClose">export from disk</a>
			    </li>
			</ul>

 			<div class="tab-content">
				<div id="exportFileToDisk_AndClose" class="container tab-pane active"><br>
					<!-- <form id="exportEPCFormToDisk_AndClose" name="exportEPCFormToDisk" method="GET" enctype="multipart/form-data"
						action="ExportEPCFile"
						> -->
					<label class="form-control-range" id="epcInputLabel_AndClose" name="epcInputLabel">EPC file name</label> 
					<input type="text" id="epcFilePath_AndClose" required name="epcFilePath" style="width:100%" />
				
					<!-- <div class="custom-control custom-radio" style="margin-left: px; margin-right:2px;">
						<input type="radio" class="custom-control-input" id="export-radio-version-classic" required name="exportVersion" value="classic" checked>
						<label class="custom-control-label" for="export-radio-version-classic">Classical</label>
					</div>
					<div class="custom-control custom-radio" style="margin-left:2px; margin-right:2px;">
						<input type="radio" class="custom-control-input" id="export-radio-version-expand" required name="exportVersion" value="expand">
						<label class="custom-control-label" for="export-radio-version-expand">Expanded</label>
					</div> -->
							
					<button class="btn btn-primary" name="submit" type="button"
							id="but_EPC_exportYes">Export</button>
					
					<button class="btn btn-primary" name="submit" type="button" 
							id="but_EPC_exportNo">Do not export (un-exported elements will be lost)</button>
					
					<!-- </form> -->
				</div>
			</div>
		</div>
	</div>
</div>
