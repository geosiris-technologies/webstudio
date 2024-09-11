<script type="module">
    import {deleteResqmlObject_list} from "/ressources/script/modules/requests/uiRequest.js";
    import {addJsonData} from "/ressources/script/modules/energyml/JsonElementVue.js"

    document.getElementById("modal_delete_warning").updateContent = function(uuids_to_remove, uuid_rels){
        const cst_uuids_to_remove = uuids_to_remove
        document.getElementById("modal_delete_warning_cancel").onclick = function(evt){
            $('#modal_delete_warning').modal('hide');
        }
        document.getElementById("modal_delete_warning_soft").onclick = function(evt){
            var soft_uuids = cst_uuids_to_remove.filter(x => !uuid_rels.hasOwnProperty(x));
            if(soft_uuids.length > 0){
                deleteResqmlObject_list(soft_uuids);
            }
            $('#modal_delete_warning').modal('hide');
        }
        document.getElementById("modal_delete_warning_force").onclick = function(evt){
            deleteResqmlObject_list(cst_uuids_to_remove);
            $('#modal_delete_warning').modal('hide');
        }

        var _c = document.getElementById("modal_delete_warning_content");
        while(_c.firstChild) _c.firstChild.remove();
        var elt_titre = document.createElement("h2");
        elt_titre.appendChild(document.createTextNode("Objects to remove referenced by kept objects : "));
        _c.appendChild(elt_titre);
        addJsonData(uuid_rels, _c, false, null);

        /*
        _c.appendChild(elt_content);

        if(elt_buttons!=null)
            var _b = document.getElementById("modal_delete_warning_content");
            while(_b.firstChild) _b.firstChild.remove();
            _b.appendChild(elt_buttons);
        }*/
        $('#modal_delete_warning').modal('show');
    }
</script>

<div class="modal fade" id="modal_delete_warning">
    <div class="modal-dialog modal-dialog-centered modal-xl-customGeosiris">
        <div class="modal-content">
            <!-- Modal Header -->
            <div class="modal-header">
                <h4 class="modal-title">3D view</h4>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div id="modal_delete_warning_content"></div>
                <div id="modal_delete_warning_buttons">
                    <div class="btn-group">
                        <button class=" btn btn-outline-dark objButtonAction" id="modal_delete_warning_cancel">Cancel</button>
                        <button class=" btn btn-outline-primary objButtonAction" id="modal_delete_warning_soft" title="Remove only object without kept dependancies">Soft remove</button>
                        <button class=" btn btn-outline-danger objButtonAction" id="modal_delete_warning_force">Force remove</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>