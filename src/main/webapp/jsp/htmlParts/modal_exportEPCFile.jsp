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
<div class="modal fade" id="modal_exportEPC">
	<script type="module">
		import {doExportEPC} from "/ressources/script/modules/UI/modals/misc.js";

		document.getElementById("exportEpcSubmitBut").onclick = function(){
			doExportEPC();
		}
	</script>
	<div class="modal-dialog modal-dialog-centered">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Export EPC</h4>
				<div id="rolling_export" class="spinner-border text-success" role="status"
					style="display: none">
					<span class="sr-only">Exporting...</span>
				</div>
				<button type="button" class="close" data-dismiss="modal" id="closeBut_export">&times;</button>
			</div>

 			<div class="tab-content">
				<div id="exportFileToDisk" class="container tab-pane active"><br>
					<div class="modal-body">
						<form id="exportEPCFormToDisk" name="exportEPCFormToDisk" method="post" action="ExportEPCFile" accept-charset="utf-8">
							<label  name="epcInputLabel">EPC file name</label>
							<input class="form-control" type="text" id="exportEpcFilePath" required name="epcFilePath" style="width:100%"/>
							<div class="custom-control custom-radio" style="margin-left: px; margin-right:2px;">
								<input type="radio" class="custom-control-input" id="export-radio-version-classic" required name="exportVersion" value="classic" checked>
								<label class="custom-control-label" for="export-radio-version-classic">Classical</label>
							</div>
							<div class="custom-control custom-radio" style="margin-left:2px; margin-right:2px;">
								<input type="radio" class="custom-control-input" id="export-radio-version-expand" required name="exportVersion" value="expand">
								<label class="custom-control-label" for="export-radio-version-expand">Expanded</label>
							</div>
							<input class="btn btn-primary" name="submit"  id="exportEpcSubmitBut" value="Export">
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>