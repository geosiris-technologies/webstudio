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
<div class="modal fade" id="modal_ETP">
    <script type="module">
        import { loadUrisIn3DVue, loadETPObjectList,
                    populate_getRelated
        } from "/ressources/script/modules/UI/modals/etp.js";
        import {sendForm} from "/ressources/script/modules/UI/modals/modalEntityManager.js";

        document.getElementById("but_loadETPObjectList").onclick = function(){
            loadETPObjectList('ETPRequest_import_Form-etp_object_list', 
                                                'ETPRequest_import_Form');
        }
        document.getElementById("submit_PutDataObject").onclick = function(){
            sendForm('ETPRequest_send_Form', 'modal_ETP', 'rolling_ETP', false, false, false, false);
        }
        document.getElementById("submit_getRelated").onclick = function(){
            populate_getRelated('ETPRequest_getRelated_Form', 'ETPRequest_getRelated_objList', 'ETPRequest_ImportRelated_Form');
        }
        document.getElementById("submit_getDataArray").onclick = function(){
            sendForm('ETPRequest_getDataArray_Form', 'modal_ETP', 'rolling_ETP', false, false, false);
        }
        document.getElementById("submit_putDataspace").onclick = function(){
            sendForm('ETPRequest_putDataspace_Form', 'modal_ETP', 'rolling_ETP', false, false, false);
            // $('form#ETPRequest_putDataspace_Form').submit(function(e){
            //     e.preventDefault();
            // });
        }
    </script>

    <div class="modal-dialog modal-dialog-centered modal-xl-customGeosiris">
        <div class="modal-content">

            <!-- Modal Header -->
            <div class="modal-header">
                <h4 class="modal-title">ETP Request</h4>
                <div id="rolling_ETP" class="spinner-border text-success" role="status"
                    style="display: none">
                    <span class="sr-only">Waiting for response...</span>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <ul class="nav nav-tabs">
                    <li class="nav-item">
                        <a class="nav-link active" data-bs-toggle="tab" href="#ETPRequest_import">Get resources</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" data-bs-toggle="tab" href="#ETPRequest_send">Put data object</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" data-bs-toggle="tab" href="#ETPRequest_getRelated">Get related resources</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" data-bs-toggle="tab" href="#ETPRequest_putDataspace">Put dataspace</a>
                    </li>
                </ul>

                <div class="progress-bar progress-bar-striped progress-bar-animated bg-success modal_progress_bar" 
                            role="progressbar"
                            aria-valuenow="100" 
                            aria-valuemin="0" 
                            aria-valuemax="100"
                            id="ETPRequest_send_progressBar"></div>


                <div class="tab-content">
                    <div id="modal_ETP_form_div"></div>

                    <hr/>

<!--
    ____                           __
   /  _/___ ___  ____  ____  _____/ /_
   / // __ `__ \/ __ \/ __ \/ ___/ __/
 _/ // / / / / / /_/ / /_/ / /  / /_
/___/_/ /_/ /_/ .___/\____/_/   \__/
             /_/ 
-->
                    <div id="ETPRequest_import" class="container tab-pane active">
                        <div class="alert alert-danger msg-error" role="alert">
                            Shown data is outaded. Please re-reconnect to ETP server and refresh it.
                        </div>
                        <br>
                        <form id="ETPRequest_import_Form" name="ETPForm" method="post" action="ETPRequest" accept-charset="utf-8">
                            <input class="form-control" type="text" required name="request" value="getresources" hidden="hidden" />
                            <div class="form-row">
                                <label><input class="checkbox form-check-input" type="checkbox" name="ask_aknowledge">Ask acknowledge</label>
                            </div>
                        </form>
                        <button class="btn btn-primary geosiris-btn-etp"
                                    id="but_loadETPObjectList">List objects (GetResources)</button>
                        <div id="ETPRequest_import_Form-etp_object_list"></div>
                    </div>

<!-- 
    ____        __  ____        __        ____  __      _           __
   / __ \__  __/ /_/ __ \____ _/ /_____ _/ __ \/ /_    (_)__  _____/ /_
  / /_/ / / / / __/ / / / __ `/ __/ __ `/ / / / __ \  / / _ \/ ___/ __/
 / ____/ /_/ / /_/ /_/ / /_/ / /_/ /_/ / /_/ / /_/ / / /  __/ /__/ /_
/_/    \__,_/\__/_____/\__,_/\__/\__,_/\____/_.___/_/ /\___/\___/\__/
                                                 /___/ 
-->
                    
                    <div id="ETPRequest_send" class="container tab-pane"><br>
                        <div>
                            <div class="form-check form-check-inline">
                                <input type="checkbox" class="form-check-input" name="checkUpRelations" id="epcETPRequest_send_checkUpRelations" checked>
                                <label class="form-check-label" for="epcETPRequest_send_checkUpRelations">Auto-check upward relations</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="checkbox" class="form-check-input" name="checkDownRelations" id="epcETPRequest_send_checkDownRelations" >
                                <label class="form-check-label" for="epcETPRequest_send_checkDownRelations">Auto-check downward relations</label>
                            </div>
                        </div>
                        
                        <form id="ETPRequest_send_Form" name="ETPRequest_send_Form" method="POST" action="ETPRequest" enctype="multipart/form-data">
                            <input class="form-control" type="text" required name="request" value="putdataobjects" hidden="hidden" />
                            <div class="form-row">
                                <label><input class="checkbox form-check-input" type="checkbox" name="ask_aknowledge">Ask acknowledge</label>
                            </div>
                            <div class="modal_tab_table" id="ETPRequest_send_workspace_objList" ></div>
                            
                        </form>
                        <input class="btn btn-primary geosiris-btn-etp" name="submit" type="submit" 
                                value="Export (PutDataObjects)" id="submit_PutDataObject"/>
                    </div>

<!--

-->

                    <div id="ETPRequest_putDataspace" class="container tab-pane"><br>
                        <form id="ETPRequest_putDataspace_Form" name="ETPRequest_putDataspace_Form" method="POST" action="ETPRequest" enctype="multipart/form-data">
                            <input class="form-control" type="text" required name="request" value="putdataspace" hidden="hidden" />
                            <div class="input-group">
                                <label class="input-group-text" type="label">Dataspace name</label>
                                <input class="form-control" type="text" name="newDataspace" >
                            </div>
                        </form>
                        <div class="input-group">
                            <button class="btn btn-primary geosiris-btn-etp" value="Create Dataspace"
                                id="submit_putDataspace">Send</button>
                        </div>
                        <!-- <input class="btn btn-primary geosiris-btn-etp" name="submit" type="submit"
                            value="Create Dataspace" id="submit_putDataspace"/> -->
                    </div>

<!-- 
   ______     __  ____                           __      __           __   __           ___       ___
  / ____/__  / /_/ __ \___  _____     ________  / /___ _/ /____  ____/ /  / /_____     / _/      /  /
 / / __/ _ \/ __/ /_/ / _ \/ ___/    / ___/ _ \/ / __ `/ __/ _ \/ __  /  / __/ __ \   / /        / /
/ /_/ /  __/ /_/ _, _/  __(__  )    / /  /  __/ / /_/ / /_/  __/ /_/ /  / /_/ /_/ /  / / _ _ _  / /
\____/\___/\__/_/ |_|\___/____(_)  /_/   \___/_/\__,_/\__/\___/\__,_/   \__/\____/  / / (_|_|_)/ /
                                                                                   /__/      /__/
 -->

                    <div id="ETPRequest_getRelated" class="container tab-pane" >
                        <br>
                        <div class="modal_tab_pane_content">
                            <div id="ETPRequest_getRelated_FormRequest">
                                <div class="input-group">
                                    <form id="ETPRequest_getRelated_Form" name="ETPForm" method="post" action="ETPRequest" accept-charset="utf-8">
                                        <input type="text" required="" name="request" value="getrelated" hidden="hidden">
                                        <div class="form-row">
                                            <label><input class="checkbox form-check-input" type="checkbox" name="ask_aknowledge">Ask acknowledge</label>
                                        </div>

                                        <div class="input-group">
                                            <label name="ETPRequest_getRelated_scopekind" class="input-group-text" type="label">Scope kind</label>
                                            <select class="form-control" id="ETPRequest_getRelated_scopekind" name="scope">
                                                <option value="sources">Sources</option>
                                                <option value="targets">Targets</option>
                                                <option value="sourcesOrSelf">Sources or Self</option>
                                                <option value="targetsOrSelf">Targets or Self</option>
                                            </select>
                                            <label name="ETPRequest_getRelated_depth" class="input-group-text" type="label">Depth</label>
                                            <input class="form-control" type="number" name="depth" id="ETPRequest_getRelated_depth" value="1" min="1">
                                        <input class="btn btn-primary geosiris-btn-etp" name="submit" type="button" value="Get Related" id="submit_getRelated" title=""></div>
                                        <div class="modal_tab_table" id="ETPRequest_getRelated_workspace_objList"></div>
                                    </form>
                                </div>

                                <h3>Related Objects</h3>
                                <form id="ETPRequest_ImportRelated_Form" name="ETPForm" method="post" action="ETPRequest" accept-charset="utf-8" style="display: none">
                                    <input class="form-control" type="text" required name="request"value="import" hidden="hidden" />
                                    <div id="ETPRequest_getRelated_objList"></div>

                                </form>
                            </div>
                        </div>
                    </div>


<!--
   ______     __     ____        __           ___
  / ____/__  / /_   / __ \____ _/ /_____ _   /   |  ______________ ___  __
 / / __/ _ \/ __/  / / / / __ `/ __/ __ `/  / /| | / ___/ ___/ __ `/ / / /
/ /_/ /  __/ /_   / /_/ / /_/ / /_/ /_/ /  / ___ |/ /  / /  / /_/ / /_/ /
\____/\___/\__/  /_____/\__,_/\__/\__,_/  /_/  |_/_/  /_/   \__,_/\__, /
                                                                 /____/ 
-->

                    <div id="ETPRequest_getDataArray" class="container tab-pane"><br>
                        <div>
                            <div class="form-check form-check-inline">
                                <input type="checkbox" class="form-check-input" name="checkUpRelations" id="epcETPRequest_getDataArray_checkUpRelations" checked>
                                <label class="form-check-label" for="epcETPRequest_getDataArray_checkUpRelations">Auto-check upward relations</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="checkbox" class="form-check-input" name="checkDownRelations" id="epcETPRequest_getDataArray_checkDownRelations" >
                                <label class="form-check-label" for="epcETPRequest_getDataArray_checkDownRelations">Auto-check downward relations</label>
                            </div>
                        </div>
                        
                        <form id="ETPRequest_getDataArray_Form" name="ETPRequest_getDataArray_Form" method="POST" action="ETPRequest" enctype="multipart/form-data">
                            <input class="form-control" type="text" required name="request"value="putdataobjects" hidden="hidden" />
                            <div class="form-row">
                                <label><input class="checkbox form-check-input" type="checkbox" name="ask_aknowledge">Ask acknowledge</label>
                            </div>
                            <div class="modal_tab_table" id="ETPRequest_getDataArray_workspace_objList" ></div>
                            
                        </form>
                        <input class="btn btn-primary geosiris-btn-etp" name="submit" type="submit" value="Export (PutDataObjects)"
                                 id="submit_getDataArray">
                    </div>

<!-- 
                /******************************************************/
 -->
                </div>
            </div>
        </div>
    </div>
    
</div>