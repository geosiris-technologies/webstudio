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

	<title>WebStudio user settings</title>
	
	<%@ include file="/jsp/htmlParts/bootStrapIncludes.jsp" %>

	<style>
		<%@ include file ="/ressources/css/connexion.css"%>
	</style>

	<script type="module">
		import {sendUserSettings} from "/ressources/script/modules/requests/uiRequest.js";

		document.getElementById("but_sendUserSettings").onclick = function(){
			sendUserSettings();
		}
	</script>
	
</head>
<body>
<div class="container">
	<div class="d-flex justify-content-center h-100">
		<div class="card">
			<div class="card-header">
				<h3>Settings</h3>
				<div class="d-flex justify-content-end social_icon">
					<!-- <span><i class="fab fa-facebook-square"></i></span>
					<span><i class="fab fa-google-plus-square"></i></span>
					<span><i class="fab fa-twitter-square"></i></span> -->
				</div>
			</div>
			<div class="card-body">
				<% 
					String login = (String) request.getAttribute("login");
					String mail  = (String) request.getAttribute("mail");


					String connexion_info = (String) request.getAttribute("response");
					if(connexion_info!=null && connexion_info.length() > 0){
						out.print("<span class='err_info'>" + connexion_info + "</span>");
					}
				%>


				<form method="post" action="usersettings" accept-charset="utf-8" id="form_UserSettings">
					<!-- USER LOGIN -->
					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text" title="login"><i class="fas fa-user"></i></span>
						</div>
						<%out.print("<input type='text' class='form-control' name='login' value='"+login+"' readonly='readonly' >"); %>
					</div>

					<!-- USER PWD -->
					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><i class="fas fa-key"></i></span>
						</div>
						<input type="password" encrypt="bcrypt" class="form-control" placeholder="Password*" name="password" required>
					</div>

					<!-- USER NEW PWD CONFIRMATION-->
					<span>Update your information</span>
					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><i class="fas fa-key"></i></span>
						</div>
						<input type="password" encrypt="bcrypt" class="form-control" placeholder="New password" name="newPassword" >
					</div>
					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><i class="fas fa-key"></i></span>
						</div>
						<input type="password" encrypt="bcrypt" class="form-control" placeholder="Confirm new password" name="newPassword" >
					</div>

					<span style="display:none" class="err_info" id="err_pwd"></span>

					<!-- USER MAIL -->
					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><i class="fas fa-envelope"></i></span>
						</div>
						<%out.print("<input type='text' class='form-control' name='mail' value='"+mail+"' >"); %>
					</div>
				</form>

				<div class="form-group">
					<button class="btn float-right login_btn" id="but_sendUserSettings">Update</button>
				</div>

				<span><a href="editor">Back to Editor</a></span>

			</div>
		</div>
	</div>
</div>

</body>
</html>