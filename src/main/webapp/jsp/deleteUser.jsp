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

	<title>WebStudio delete user</title>
	
	<%@ include file="/jsp/htmlParts/bootStrapIncludes.jsp" %>

	<style>
		<%@ include file ="/ressources/css/connexion.css"%>
	</style>

	<script type="module">
		import {createUserTableView} from "/ressources/script/modules/requests/uiRequest.js";

		<% String userList = ((String) request.getAttribute("userList")).replaceAll("\n", ""); %>

		document.getElementById("deleteUserTable").appendChild(
				createUserTableView("login", 
										null,
										JSON.parse('<% out.print(""+userList+""); %>')
										));
	</script>

</head>

<body>

<div class="container">
	<div class="d-flex justify-content-center h-100">
		<div class="card">
			<div class="card-header">
				<h3>Delete user</h3>
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
						out.print("<span id='connexion_info'>" + connexion_info + "</span>");
					}
				%>
				<form method="post" action="deleteuser" accept-charset="utf-8">
					<!-- <div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><i class="fas fa-user"></i></span>
						</div>
						
						<input type="text" class="form-control" placeholder="username" name="login">
						
					</div> -->
					<div class="input-group form-group">
						<div id="deleteUserTable"></div>
					</div>

					<div class="form-group">
						<input type="submit" value="Delete" class="btn float-right login_btn">
					</div>
				</form>
				<span><a href="editor">Back to Editor</a></span>
			</div>
		</div>
	</div>
</div>

</body>
</html>