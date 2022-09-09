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
<div class="modal fade" id="modal_closeEPC" role="dialog">
	<script type="module">
		import { closeActionNo, closeActionYes } from "/ressources/script/modules/UI/modals/misc.js";

		document.getElementById("but_closeEpcYes").onclick = function(){
			closeActionYes();
		}
		document.getElementById("but_closeEpcNo").onclick = function(){
			closeActionNo();
		}
	</script>
	<div class="modal-dialog modal-dialog-centered">
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
				<h4 class="modal-title">Close EPC</h4>
				<div id="rolling_close" class="spinner-border text-success" role="status"
					style="display: none">
					<span class="sr-only">closing...</span>
				</div>
				<button type="button" name="submit" class="close" data-dismiss="modal">&times;</button>
			</div>
			
			<div style="display:contents;">
				<p>Do you want to save all modification ?</p>
				<br/>
				<div class="btn-group" role="group" >
					<button type="button" name="submit" class="btn btn-primary"
							id="but_closeEpcYes"
							data-dismiss="modal">Yes</button>
							
					<button type="button" name="submit" class="btn btn-dark"
							id="but_closeEpcNo"
							data-dismiss="modal">No</button>
				</div>
			</div>
		</div>
	</div>
</div>