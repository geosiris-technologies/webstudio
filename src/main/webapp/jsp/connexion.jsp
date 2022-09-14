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
		<title>WebStudio connexion</title>
		<%@ include file="/jsp/htmlParts/bootStrapIncludes.jsp" %>
		<style>
		<%@ include file ="/ressources/css/connexion.css"%></style>
	</head>
	<body>
		<div class="container">
			<div class="d-flex justify-content-center h-100">
				<div class="card">
					<div class="card-top">
						<h2 class="ws_title">ResQML Web Studio</h2>
					</div>
					<div class="card-header">
						<h3>Sign In</h3>
						<div class="d-flex justify-content-end social_icon">
							<!-- <span><em class="fab fa-facebook-square"></em></span>
												<span><em class="fab fa-google-plus-square"></em></span>
							<span><em class="fab fa-twitter-square"></em></span> -->
						</div>
					</div>
					<div class="card-body">
						<% String connexion_info = (String) request.getAttribute("response"); if(connexion_info!=null && connexion_info.length() >0){
						out.print(" <span id='connexion_info'>" + connexion_info + "</span>");
						}
						%>
						<form method="post" action="connexion" accept-charset="utf-8" >
							<div class="input-group form-group">
								<div class="input-group-prepend">
									<span class="input-group-text">
										<em class="fas fa-user"></em>
									</span>
								</div>
								<input type="text" class="form-control" placeholder="username" name="login">
							</div>
							<div class="input-group form-group">
								<div class="input-group-prepend">
									<span class="input-group-text">
										<em class="fas fa-key"></em>
									</span>
								</div>
								<input type="password" encrypt="bcrypt" class="form-control" placeholder="password" name="password">
							</div>
							<div class="form-group">
								<input type="submit" value="Login" class="btn float-right login_btn">
							</div>
						</form>
					</div>
					<div class="card-footer">
						<div class="d-flex justify-content-center links">
							<a href="mailto:contact@geosiris.com">contact@geosiris.com</a>
						</div>
						<!-- <div class="d-flex justify-content-center">
									<a href="#">Forgot your password?</a>
						</div> -->
					</div>
					<div class="card-footer pageFooter">
						<a  href="http://geosiris.com" target="_blank">
							<img alt="img_geosiris" height=30 src="<%=request.getContextPath()%>/ressources/img/logos/logo_geosiris.png">
							G&eacute;osiris
						</a>
						<a  href="https://xlim-sic.labo.univ-poitiers.fr/jerboa" target="_blank">
							<img alt="img_XLIM" height=30 src="https://www.xlim.fr/sites/default/files/xlim_logotype_rvb.png">
							Jerboa
						</a>
					</div>
				</div>
			</div>
		</div>

		<!-- <div class="pageFooter">
			<a  href="http://geosiris.com" target="_blank">
				<img alt="img_geosiris" height=30 src="<%=request.getContextPath()%>/ressources/img/logos/logo_geosiris.png">
				G&eacute;osiris
			</a>
			<a  href="https://xlim-sic.labo.univ-poitiers.fr/jerboa" target="_blank">
				<img alt="img_XLIM" height=30 src="https://www.xlim.fr/sites/default/files/xlim_logotype_rvb.png">
				Jerboa
			</a>
		</div> -->

	</body>
</html>