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
<div class="modal fade" id="modal_getOSDUManifest">
	<script type="module">
		import {downloadJsonAsFile, openContentInNewTab} from "/ressources/script/modules/UI/htmlUtils.js"
		import {sendPostForm_Promise} from "/ressources/script/modules/requests/requests.js"

		document.getElementById("but_getOSDUManifest").onclick = function(){
			document.getElementById('rolling_getOSDUManifest').style.display = "";
    		sendPostForm_Promise(document.getElementById('getOSDUManifestForm'), '/OSDU_Manifest', true).then(
    			function(responseJson){
    				document.getElementById('rolling_getOSDUManifest').style.display = "none";
    				document.getElementById("modal_getOSDUManifest_close").click();

    				openContentInNewTab(responseJson);
    				downloadJsonAsFile(responseJson, "manifest.json");

    			});
    	}
	</script>
	<div class="modal-dialog modal-dialog-centered">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Generate OSDU manifest</h4>
				<div id="rolling_getOSDUManifest" class="spinner-border text-success" role="status"
					style="display: none">
					<span class="sr-only">generating...</span>
				</div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" id="modal_getOSDUManifest_close"></button>
			</div>
			
			<div class="modal-body">
				<form id="getOSDUManifestForm" name="getOSDUManifestForm" method="POST">
					<div class="form-row">
                        <label>Access Token<input class="form-control" type="text" 
                        									required name="token" style="width:100%"
                        									/>
                        </label>
                    </div>
					<div class="form-row">
                        <label>Dataset service host<input class="form-control" type="text" 
                        									required name="host" style="width:100%"
                        									value=""
                        									/>
                        </label>
                    </div>
					<div class="form-row">
                        <label>Data Partition id<input class="form-control" type="text"
                        									required name="data_partition_id" style="width:100%"
                        									value="osdu"
                         									/>
                        </label>
                    </div>
					<input class="btn btn-primary" type="button" value="Generate"
						id="but_getOSDUManifest">
				</form>
			</div>	
			
		</div>
	</div>
</div>