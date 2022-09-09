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
<html>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">

	<title>WebStudio connexion</title>
	
	<%@ include file="/jsp/htmlParts/bootStrapIncludes.jsp" %>

	<style>
	<%@ include file ="/ressources/css/connexion.css"%>
</style>


</head>
<body>

	<div class="container">
		<div class="d-flex justify-content-center h-100">
			<div class="card">
				<div class="card-header">
					<h3>Upload H5 file Minio</h3>
					<div class="d-flex justify-content-end social_icon">
					<!-- <span><i class="fab fa-facebook-square"></i></span>
					<span><i class="fab fa-google-plus-square"></i></span>
					<span><i class="fab fa-twitter-square"></i></span> -->
				</div>
			</div>
			<div class="card-body">
				<form method="post" action="h5reciever" accept-charset="utf-8" enctype="multipart/form-data">
					<div class="input-group form-group">
						<div class="input-group-prepend">
							<span class="input-group-text"><i class="fas fa-file"></i></span>
						</div>
						
						<div class="custom-file">
							<input name="h5InputFile" class="custom-file-input" type="file" accept=".h5" multiple id="inputH5"/>
							<label class="custom-file-label" for="inputH5">Choose file</label>
						</div>
					</div>

					<div class="form-group">
						<input type="submit" value="Upload" class="btn float-right login_btn">
					</div>
				</form>
			</div>
		</div>
	</div>

</body>
</html>