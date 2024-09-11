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
<div class="modal fade" id="modal_file_editor" >
	<script type="module">
		import {importMultipleFilesToWorkspace} from "/ressources/script/modules/energyml/epcContentManager.js"
		import {__ID_CONSOLE__} from "/ressources/script/modules/common/variables.js"

        document.getElementById("modal_file_editor_but_save").addEventListener("click", function(){
            var file_content = $("#modal_file_editor_content code")[0].innerText;
            if(file_content.length > 0){
                importMultipleFilesToWorkspace(
                    [
                        [file_content, "editedFile.xml"]
                    ],
                    __ID_CONSOLE__,
                    false
                );
            }
		});
	</script>
	<div class="modal-dialog modal-dialog-centered modal-xl-customGeosiris">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">File editor</h4>
				<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
			</div>

			<!-- Modal Body -->
			<div class="modal-body" style="overflow: hidden;">
	 			<div class="tab-content" style="height: 100%">
                    <div class="progress-bar progress-bar-striped progress-bar-animated bg-success"
                            role="progressbar"
                            style="height: 15px; display: none"
                            aria-valuenow="100"
                            aria-valuemin="0"
                            aria-valuemax="100"
                            id="modal_file_editor_progressBar"></div>

					<div class="container" id="modal_file_editor_content" style=" height: 100%">

					</div>
				</div>
			</div>

			<!-- modal body -->
			<div class="modal-footer">
				<button type="button" id="modal_file_editor_but_save" class=" btn btn-primary">Save data</button>
			</div>
		</div>
	</div>
</div>