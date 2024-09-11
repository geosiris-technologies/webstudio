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
<div class="modal fade" id="modal_changeDorReference">
	<script type="module">
		import { dochangeDorReference } from "/ressources/script/modules/UI/modals/misc.js";

		document.getElementById("changeDorReferenceSubmitBut").onclick = function(){
			dochangeDorReference();
		}
	</script>
	<div class="modal-dialog modal-dialog-centered">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Modify DOR references to specific element</h4>
				<div id="rolling_changeDorReference" class="spinner-border text-success" role="status"
					style="display: none">
					<span class="sr-only">changeDorReferenceing...</span>
				</div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" id="closeBut_changeDorReference"></button>
			</div>

 			<div class="tab-content">
				<div id="changeDorReference_content" class="container tab-pane active"><br>
					<div class="modal-body">
						<form id="changeDorReference" name="changeDorReference" method="post" action="changeDorReferenceFile" accept-charset="utf-8">
							<label  name="epcInputLabel" for="changeDorReferenceFilePath_root">Current object referenced</label>
							<input class="form-control" type="text" id="changeDorReferenceFilePath_root" required name="Root_Uuid" style="width:100%"/>

							<label  name="epcInputLabel" for="changeDorReferenceFilePath_target">New object to reference</label>
							<input class="form-control" type="text" id="changeDorReferenceFilePath_target" required name="input" style="width:100%"/>

							<input class="btn btn-primary" id="changeDorReferenceSubmitBut" value="Go">
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>