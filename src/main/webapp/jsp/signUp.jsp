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
<html lang="en-US">
<head>
	<meta charset="utf-8">

	<title>WebStudio sign up</title>
	
	<%@ include file="/jsp/htmlParts/bootStrapIncludes.jsp" %>

	<style>
		<%@ include file ="/ressources/css/connexion.css"%>
	</style>

	<script type="module" src="/ressources/script/modules/requests/uiRequests.js">
		window.sendSignUp = sendSignUp;
	</script>
</head>
<body>
<div class="container">
	<div class="d-flex justify-content-center h-100">
		<div class="card">
			<div class="card-header">
				<h3>Sign Up</h3>
				<!-- <div class="d-flex justify-content-end social_icon">
					<span><em class="fab fa-facebook-square"></em></span>
					<span><em class="fab fa-google-plus-square"></em></span>
					<span><em class="fab fa-twitter-square"></em></span>
				</div> -->
			</div>
			<div class="card-body">
				<% 
					String connexion_info = (String) request.getAttribute("response");
					if(connexion_info!=null && connexion_info.length() > 0){
						out.print("<span class='err_info'>" + connexion_info + "</span>");
					}
				%>
				<form method="post" action="signup" accept-charset="utf-8" id="form_signUp">
					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><em class="fas fa-user"></em></span>
						</div>
						<input type="text" class="form-control" placeholder="User name" name="login" required>
						
					</div>
					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><em class="fas fa-key"></em></span>
						</div>
						<input type="password" encrypt="bcrypt" class="form-control" placeholder="Password" name="password" required>
					</div>
					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><em class="fas fa-key"></em></span>
						</div>
						<input type="password" encrypt="bcrypt" class="form-control" placeholder="Confirm password" name="password" required>
					</div>
					
					
					<span style="display:none" class="err_info" id="err_pwd"></span>

					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><em class="fas fa-envelope"></em></span>
						</div>
						<input type="text" id="mail" name="mail" placeholder="Mail adress" class="form-control">
					</div>
				</form>

				<div class="form-group">
					<button class="btn float-right login_btn" onclick="sendSignUp();">Sign up</button>
				</div>

				<span><a href="editor">Back to Editor</a></span>
			</div>
		</div>
	</div>
</div>

</body>
</html>