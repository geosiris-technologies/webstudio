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

<!-- Pour avoir des listes triables -->
<script src="https://code.jquery.com/jquery-1.12.1.js"></script>

<!-- Pour avoir l'autocompletion -->

<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<!-- <script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script> -->
<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.css">

<!--  debut bootstrap -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<!-- <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"></script>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css"> -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>


<!-- <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.7.0/css/all.css" integrity="sha384-lZN37f5QGtY3VHgisS14W3ExzMWZxybE1SJSEsQp9S+oqd12jhcu+A56Ebc1zFSJ" crossorigin="anonymous"> -->
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.15.4/css/all.css" integrity="sha384-DyZ88mC6Up2uqS4h/KRgHuoeGwBcD4Ng9SiP4dIRy0EXTlnuz47vAwmeGwVChigm" crossorigin="anonymous">


<!-- Pour les spliters -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/split.js/1.3.5/split.min.js"></script>
<style><%@ include file="/ressources/css/splitStyle.css"%></style>

<link rel="icon" type="image/png" href="<%=request.getContextPath()%>/ressources/img/logos/logoWS_square.png" />


<!--  fin bootstrap -->

<!-- highlight.js -->
<script
  src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.10.0/highlight.min.js"
  ></script>
<link rel="stylesheet" href="/ressources/css/highlight/styles/dark.css">
<script>hljs.highlightAll();</script>
<!-- Threejs -->
<script type="importmap">
    {
        "imports": {
            "three": "https://unpkg.com/three@0.147.0/build/three.module.js",
            "three/addons/": "https://unpkg.com/three@0.147.0/examples/jsm/",
            "hljs": "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.10.0/highlight.min.js"
        }
    }
</script>
<script async src="https://unpkg.com/es-module-shims@1.6.2/dist/es-module-shims.js"></script>

<!-- JSZip -->
<script src="/ressources/script/modules/UI/lib/jszip.min.js" type="text/javascript"></script>

<script src="https://unpkg.com/cytoscape@3.3.0/dist/cytoscape.min.js" type="text/javascript"></script>

