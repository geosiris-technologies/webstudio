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
<div class="modal fade" id="modal_createRootElt">
	<script type="module">
		import { createNewElt } from "/ressources/script/modules/UI/modals/misc.js";

		document.getElementById("but_createNewRootElt").onclick = function(){
			createNewElt();
		}
	</script>
	<div class="modal-dialog modal-dialog-centered">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Create Resqml root element </h4>
				<div id="rolling_createRootElt" class="spinner-border text-success" role="status"
					style="display: none">
					<span class="sr-only">creating root element...</span>
				</div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			
			<div class="modal-body">
				<div>
					<label>Package name</label>
					<select id="modal_createRootElt_pkgChooserSelect" class="form-control"></select>
					<label>Package version</label>
					<div id="modal_createRootElt_versionChooserDiv"></div>
				</div>
				<form id="createRootEltForm" name="createRootEltForm" method="post" action="ObjectEdit">
					<label>Top level Object</label>
					<input type="text" name="command" value="create" hidden="hidden">
					<select id="selectorCreateRootElt" name="type" class="form-control">
					</select>
					<input class="btn btn-primary" name="submit" type="button"
						id="but_createNewRootElt"
						value="Create element">
				</form>
			</div>	
			
		</div>
	</div>
</div>