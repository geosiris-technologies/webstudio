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
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">

	<title>WebStudio Create user</title>
	
	<%@ include file="/jsp/htmlParts/bootStrapIncludes.jsp" %>

	<style>
		<%@ include file ="/ressources/css/connexion.css"%>
	</style>

	<script type="module">
		import {sendCreateUser} from "/ressources/script/modules/requests/uiRequest.js";

		document.getElementById("but_createUser").onclick = function(event){
			sendCreateUser();
		}
	</script>

</head>
<body>

	<div class="container">
		<div class="d-flex justify-content-center h-100">
			<div class="card">
				<div class="card-header">
					<h3>Create user</h3>
					<!-- <div class="d-flex justify-content-end social_icon">
						<span><i class="fab fa-facebook-square"></i></span>
						<span><i class="fab fa-google-plus-square"></i></span>
						<span><i class="fab fa-twitter-square"></i></span>
					</div> -->
				</div>
				<div class="card-body">
					<% 
						String connexion_info = (String) request.getAttribute("message");
						if(connexion_info!=null && connexion_info.length() > 0){
							out.print("<span class='err_info'>" + connexion_info + "</span>");
						}
					%>
					<form method="post" action="createuser" accept-charset="utf-8" id="form_CreateUser">
						<div class="input-group form-group">
							<div class="input-group-prepend">
								<span class="input-group-text"><i class="fas fa-user"></i></span>
							</div>
							<input type="text" class="form-control" placeholder="User name" name="login" required>
							
						</div>
						<div class="input-group form-group">
							<div class="input-group-prepend">
								<span class="input-group-text"><i class="fas fa-key"></i></span>
							</div>
							<input type="password" encrypt="bcrypt" class="form-control" placeholder="Password" name="password" required>
						</div>
						<div class="input-group form-group">
							<div class="input-group-prepend">
								<span class="input-group-text"><i class="fas fa-key"></i></span>
							</div>
							<input type="password" encrypt="bcrypt" class="form-control" placeholder="Confirm password" name="password" required>
						</div>


						<div class="input-group form-group">
							<div class="input-group-prepend">
								<span class="input-group-text"><i class="fas fa-users"></i></span>
							</div>
							<select name="group" class="form-control">
								<option value="user">user</option>
								<option value="geosiris">geosiris (admin)</option>
							</select>
						</div>
						
						
						<span style="display:none" class="err_info" id="err_pwd"></span>

						<div class="input-group form-group">
							<div class="input-group-prepend">
								<span class="input-group-text"><i class="fas fa-envelope"></i></span>
							</div>
							<input type="text" id="mail" name="mail" placeholder="User mail adress" class="form-control">
						</div>
					</form>

					<div class="form-group">
						<button id="but_createUser" class="btn float-right login_btn">Create user</button>
					</div>

					<span><a href="editor">Back to Editor</a></span>
				</div>
			</div>
		</div>
	</div>

</body>
</html>