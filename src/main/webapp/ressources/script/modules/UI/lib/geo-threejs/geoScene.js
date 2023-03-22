import * as THREE from 'three';
import {createToggler, createRadio, createScaler} from "./uiUtils.js";
import { createMesh, createSegments, createPointCloud, randomColor, centerCamera } from './utils.js';

class GeoObject{

    static EVT_OBJ_POINTS_VISIBILITY = "evt_geo_object_points_visibility";
    static EVT_OBJ_LINES_VISIBILITY  = "evt_geo_object_lines_visibility";
    static EVT_OBJ_BBOX_VISIBILITY  = "evt_geo_object_bbox_visibility";
    static EVT_OBJ_FACES_VISIBILITY  = "evt_geo_object_faces_visibility";

    static EVT_OBJ_SCALED   = "evt_geo_object_scaled";
    static EVT_OBJ_ADD      = "evt_geo_object_add";
    static EVT_OBJ_REMOVED  = "evt_geo_object_removed";


    constructor(surface_loader, name="unkown surface"){
        this.name = name;
        this.surface_loader = surface_loader;

        this.rawPoints = this.surface_loader.points;

        this.points = createPointCloud(this.surface_loader.points);
        if(this.surface_loader.lines != null && this.surface_loader.lines.length > 0)
            this.lines = createSegments(this.surface_loader.lines);
        else
            this.lines = null;

        if(this.surface_loader.triangles != null && this.surface_loader.triangles.length > 0)
            this.faces = createMesh(this.surface_loader.triangles);
        else
            this.lines = null;

        this.updateBbox(null);
    }

    updateBbox(scene){
        if(this.bbox != null && scene != null){
            scene.remove(this.bbox);
        }
        this.bbox = createSegments(this.computeBbox(this.surface_loader.points));
        if(scene != null){
            scene.add(this.bbox);
        }
    }

    togglePoints(enable = true){
        this.points.visible = enable;
        const const_this = this;
        document.dispatchEvent( new CustomEvent(GeoObject.EVT_OBJ_POINTS_VISIBILITY, {
                                                        detail: {
                                                            obj: const_this,
                                                            value: const_this.points.visible
                                                        }
        }));
    }

    toggleLines(enable = true){
        if(this.lines != null){
            this.lines.visible = enable;
            const const_this = this;
            document.dispatchEvent( new CustomEvent(GeoObject.EVT_OBJ_LINES_VISIBILITY, {
                                                            detail: {
                                                                obj: const_this,
                                                                value: const_this.lines.visible
                                                            }
            }));
        }
    }

    toggleFaces(enable = true){
        if(this.faces != null){
            this.faces.visible = enable;
            const const_this = this;
            document.dispatchEvent( new CustomEvent(GeoObject.EVT_OBJ_FACES_VISIBILITY, {
                                                            detail: {
                                                                obj: const_this,
                                                                value: const_this.faces.visible
                                                            }
            }));
        }
    }


    toggleBbox(enable = true){
        if(this.bbox != null){
            this.bbox.visible = enable;
            const const_this = this;
            document.dispatchEvent( new CustomEvent(GeoObject.EVT_OBJ_BBOX_VISIBILITY, {
                                                            detail: {
                                                                obj: const_this,
                                                                value: const_this.bbox.visible
                                                            }
            }));
        }
    }

    isPointsVisible(){
        return this.points.visible;
    }

    isLinesVisible(){
        if(this.lines != null){
            return this.lines.visible;
        }
    }

    isFacesVisible(){
        if(this.faces != null){
            return this.faces.visible;
        }
    }

    isBboxVisible(){
        if(this.bbox != null){
            return this.bbox.visible;
        }
    }

    hasLines(){
        return this.lines != null;
    }

    hasFaces(){
        return this.faces != null;
    }

    computeBbox(){
        var minX = null;
        var minY = null;
        var minZ = null;
        var maxX = null;
        var maxY = null;
        var maxZ = null;

        this.rawPoints.forEach(pointArray => {
            if(minX == null || pointArray[0] < minX){
                minX = pointArray[0];
            }
            if(minY == null || pointArray[1] < minY){
                minY = pointArray[1];
            }
            if(minZ == null || pointArray[2] < minZ){
                minZ = pointArray[2];
            }
            if(maxX == null || pointArray[0] > maxX){
                maxX = pointArray[0];
            }
            if(maxY == null || pointArray[1] > maxY){
                maxY = pointArray[1];
            }
            if(maxZ == null || pointArray[2] > maxZ){
                maxZ = pointArray[2];
            }
        });


        return [
            [minX, minY, minZ],  [minX, minY, maxZ],
            [minX, minY, minZ],  [minX, maxY, minZ],
            [minX, minY, minZ],  [maxX, minY, minZ],

            [maxX, maxY, maxZ],  [minX, maxY, maxZ],
            [maxX, maxY, maxZ],  [maxX, minY, maxZ],
            [maxX, maxY, maxZ],  [maxX, maxY, minZ],

            [minX, maxY, maxZ],  [minX, minY, maxZ],
            [minX, maxY, maxZ],  [minX, maxY, minZ],

            [maxX, minY, maxZ],  [maxX, minY, minZ],
            [maxX, minY, maxZ],  [minX, minY, maxZ],

            [maxX, maxY, minZ],  [maxX, minY, minZ],
            [maxX, maxY, minZ],  [minX, maxY, minZ]
        ]
    }

    toggleObject(enable = true){
        this.togglePoints(enable);
        this.toggleBbox(false); // default do not show bbox
        if(this.lines != null){
            this.toggleLines(enable);
        }
        if(this.faces != null){
            this.toggleFaces(enable);
        }
    }

    removeFromScene(scene){
        scene.remove(this.points);
        scene.remove(this.bbox);
        if(this.lines != null){
            scene.remove(this.lines);
        }
        if(this.faces != null){
            scene.remove(this.faces);
        }
        const const_this = this;
        document.dispatchEvent( new CustomEvent(GeoObject.EVT_OBJ_REMOVED, {
                                                        detail: {
                                                            obj: const_this
                                                        }
        }));
    }

    addToScene(scene){
        scene.add(this.points);
        scene.add(this.bbox);
        this.toggleBbox(false);
        if(this.lines != null){
            scene.add(this.lines);
        }
        if(this.faces != null){
            scene.add(this.faces);
        }
        const const_this = this;
        document.dispatchEvent( new CustomEvent(GeoObject.EVT_OBJ_ADD, {
                                                        detail: {
                                                            obj: const_this
                                                        }
        }));
    }

    reverseOrient(scene){
        if(this.faces != null){
            var f_Visible = this.isFacesVisible();
            var material = this.faces.material;
            this.surface_loader.reverseTrianglesOrientation();
            scene.remove(this.faces);
            this.faces = createMesh(this.surface_loader.triangles, null, material);
            scene.add(this.faces);

            this.faces.visible = f_Visible;
            const const_this = this;
            document.dispatchEvent( new CustomEvent(GeoObject.EVT_OBJ_FACES_VISIBILITY, {
                                                            detail: {
                                                                obj: const_this,
                                                                value: const_this.faces.visible
                                                            }
            }));
        }
    }

    flipY(scene){
        this.scale(this.points.scale.x, - this.points.scale.y, this.points.scale.z, false);
    }

    scale(x, y, z, keepSign=true){
        if(keepSign){
            x = (this.points.scale.x > 0 != x > 0) ? -x : x;
            y = (this.points.scale.y > 0 != y > 0) ? -y : y;
            z = (this.points.scale.z > 0 != z > 0) ? -z : z;
        }

        this.points.scale.set( x, y, z );
        this.bbox.scale.set( x, y, z );

        if(this.lines != null){
            this.lines.scale.set( x, y, z );
        }
        if(this.faces != null){
            this.faces.scale.set( x, y, z );
        }

        const const_this = this;
        document.dispatchEvent( new CustomEvent(GeoObject.EVT_OBJ_SCALED, {
                                                        detail: {
                                                            obj: const_this,
                                                            value: [x, y, z]
                                                        }
        }));
    }

    scaleX(x, keepSign=true){
        if(keepSign){
            x = (this.points.scale.x > 0 != x > 0) ? -x : x;
        }
        this.scale(x, this.points.scale.y, this.points.scale.z);
    }
    scaleY(y, keepSign=true){
        if(keepSign){
            y = (this.points.scale.y > 0 != y > 0) ? -y : y;
        }
        this.scale(this.points.scale.x, y, this.points.scale.z);
    }
    scaleZ(z, keepSign=true){
        if(keepSign){
            z = (this.points.scale.z > 0 != z > 0) ? -z : z;
        }
        this.scale(this.points.scale.x, this.points.scale.y, z);
    }

    getScaleX(){
        return this.points.scale.x;
    }
    getScaleY(){
        return this.points.scale.y;
    }
    getScaleZ(){
        return this.points.scale.z;
    }

    move(point_array){
        this.points.translateX(point_array[0]);
        this.points.translateY(point_array[1]);
        this.points.translateZ(point_array[2]);

        this.bbox.translateX(point_array[0]);
        this.bbox.translateY(point_array[1]);
        this.bbox.translateZ(point_array[2]);

        if(this.lines != null){
            this.lines.translateX(point_array[0]);
            this.lines.translateY(point_array[1]);
            this.lines.translateZ(point_array[2]);
        }

        if(this.faces != null){
            this.faces.translateX(point_array[0]);
            this.faces.translateY(point_array[1]);
            this.faces.translateZ(point_array[2]);
        }
    }

    lookAtMeBarycenter(controls){
        centerCamera(controls, this.surface_loader.barycenter);
    }

    lookAtMe(controls){
        var bb = new THREE.Box3()
        bb.setFromObject(this.points);
        //console.log(bb.getCenter(controls.target));
        centerCamera(controls, bb.getCenter(controls.target).toArray());
        //centerCamera(controls, this.surface_loader.points[0]);
    }
}

class GeoScene{

    static EVT_OBJ_ADD = "evt_geo_scene_obj_add";
    static EVT_OBJ_RM = "evt_geo_scene_obj_remove";
    static EVT_CLEAR = "evt_geo_scene_clear";
    static EVT_RESET_CONTROLS = "evt_geo_reset_controls";

    constructor(threejs_scene, controls) {
        this._objectList = [];
        this._theejs_scene = threejs_scene;
        this._controls = controls;
        this.scaleFactors = [1.0, 1.0, 1.0]
    }

    // add a GeoObject in the scene
    addObject(geo_object){
        this._objectList.push(geo_object);
        geo_object.scaleX(this.scaleFactors[0]);
        geo_object.scaleY(this.scaleFactors[1]);
        geo_object.scaleZ(this.scaleFactors[2]);
        geo_object.addToScene(this._theejs_scene);

        document.dispatchEvent( new CustomEvent(GeoScene.EVT_OBJ_ADD, {
                                                        detail: {
                                                            obj: geo_object
                                                        }
        }));
    }

    removeObject(geo_object){
        var idx = -1;

        for(var i=0; i<this._objectList.length; i++){
            if(this._objectList[i] === geo_object){
                idx = i;
                break;
            }
        }

        if(idx>=0){
            this._objectList.splice(idx, 1);
            geo_object.removeFromScene(this._theejs_scene);
            document.dispatchEvent( new CustomEvent(GeoScene.EVT_OBJ_RM, {
                                                        detail: {
                                                            obj: geo_object
                                                        }
            }));
        }else{
            console.log("not found ")
        }

        
    }

    togglePoints(enable = true){
        this._objectList.forEach(obj => obj.togglePoints(enable));
    }

    toggleLines(enable = true){
        this._objectList.forEach(obj => obj.toggleLines(enable));
    }

    toggleFaces(enable = true){
        this._objectList.forEach(obj => obj.toggleFaces(enable));
    }

    toggleBbox(enable = true){
        this._objectList.forEach(obj => obj.toggleBbox(enable));
    }

    clearScene(){
        const const_this = this;
        this._objectList.forEach(obj => obj.removeFromScene(const_this._theejs_scene));
        this._objectList = [];

        document.dispatchEvent( new CustomEvent(GeoScene.EVT_CLEAR, {detail: {scene: const_this._theejs_scene}}));
    }

    moveWorld(point_array){
        centerCamera(this._controls, [0,0,0]);
        this._objectList.forEach(obj => {
            console.log(point_array)
            obj.move(point_array);
        });
    }


}


class GeoSceneUi {

    constructor(geo_scene, parentElt, eltType = "div"){
        this.geo_scene = geo_scene;
        this.parentElt = parentElt;

        this.div = document.createElement(eltType);
        this.parentElt.appendChild(this.div);

        // events : 
        const const_this = this;
        document.addEventListener(GeoScene.EVT_OBJ_ADD, (e) => const_this.updateUi());
        document.addEventListener(GeoScene.EVT_OBJ_RM, (e) => const_this.updateUi());
        document.addEventListener(GeoScene.EVT_CLEAR, (e) => const_this.updateUi());

        this.sceneObj_list = null;
        // ui update
        this.createUi();
    }

    changeCamUp(v_arr){
        this.geo_scene._controls.object.up.set(v_arr[0], v_arr[1], v_arr[2] );
        this.parentElt.dispatchEvent( new CustomEvent(GeoScene.EVT_RESET_CONTROLS, {
                                                        detail: {
                                                            ui: this
                                                        }
        }));
    }

    createUi(){
        const const_this = this;
        this.div.style.border = "1px solid black";

        var label = document.createTextNode("Scene");
        this.div.appendChild(label);

        var scene_clear = document.createElement("i");
        scene_clear.className = "far fa-trash-alt";
        scene_clear.style.cursor = 'pointer';
        scene_clear.style.margin = "2px";
        scene_clear.title = "Clear scene"
        scene_clear.onclick = function(){
            const_this.geo_scene.clearScene();
        };
        this.div.appendChild(scene_clear);

        // Up vector
        var radioGrp = document.createElement("div");
        radioGrp.className = "form-check";
        radioGrp.appendChild(document.createTextNode("Camera Up vector"));

        
        radioGrp.onchange = function f_changeCamUp(radioEvt){
            if(radioEvt.target.value == "X")
                const_this.changeCamUp([1,0,0]);
            if(radioEvt.target.value == "Y")
                const_this.changeCamUp([0,1,0]);
            if(radioEvt.target.value == "Z")
                const_this.changeCamUp([0,0,1]);
        }

        var currentCamUp = this.geo_scene._controls.object.up;

        var radioX = createRadio("radio_scene_cam_up", "X", "X", currentCamUp.x == 1);
        radioX.style.color = "red";
        radioX.value = const_this.geo_scene.scaleFactors[0];
        radioGrp.appendChild(radioX);

        var radioY = createRadio("radio_scene_cam_up", "Y", "Y", currentCamUp.y == 1);
        radioY.style.color = "green";
        radioY.value = const_this.geo_scene.scaleFactors[1];
        radioGrp.appendChild(radioY);

        var radioZ = createRadio("radio_scene_cam_up", "Z", "Z", currentCamUp.z == 1);
        radioZ.style.color = "blue";
        radioZ.value = const_this.geo_scene.scaleFactors[2];
        radioGrp.appendChild(radioZ);

        this.div.appendChild(radioGrp);

        // salers 
        {
            // scale X
            const lbl_scaleX = document.createElement("label");
            lbl_scaleX.appendChild(document.createTextNode("X"))
            const in_scaleX = createScaler();
            in_scaleX.title = "Scale X";
            in_scaleX.onchange = function(){
                    const_this.geo_scene._objectList.forEach(geoObj => {
                        if(geoObj.getScaleX() != in_scaleX.value){ // to prevent change when recieving event change
                            geoObj.scaleX(in_scaleX.value);
                            const_this.geo_scene.scaleFactors[0] = in_scaleX.value;
                        }
                    });
                
            };

            // scale Y
            const lbl_scaleY = document.createElement("label");
            lbl_scaleY.appendChild(document.createTextNode("Y"))
            const in_scaleY = createScaler();
            in_scaleY.title = "Scale Y";
            in_scaleY.onchange = function(){
                    const_this.geo_scene._objectList.forEach(geoObj => {
                        if(geoObj.getScaleY() != in_scaleY.value){ // to prevent change when recieving event change
                            geoObj.scaleY(in_scaleY.value);
                            const_this.geo_scene.scaleFactors[1] = in_scaleY.value;
                        }
                    });
            };

            // scale Y
            const lbl_scaleZ = document.createElement("label");
            lbl_scaleZ.appendChild(document.createTextNode("Z"))
            const in_scaleZ = createScaler();
            in_scaleZ.title = "Scale Z";
            in_scaleZ.onchange = function(){
                    const_this.geo_scene._objectList.forEach(geoObj => {
                        if(geoObj.getScaleZ() != in_scaleZ.value){ // to prevent change when recieving event change
                            geoObj.scaleZ(in_scaleZ.value);
                            const_this.geo_scene.scaleFactors[2] = in_scaleZ.value;
                        }
                    });
            };

            document.addEventListener(GeoObject.EVT_OBJ_SCALED, (e) => {
                if(e.detail.obj == const_this.obj) {
                    in_scaleX.value = e.detail.value[0];
                    in_scaleY.value = e.detail.value[1];
                    in_scaleZ.value = e.detail.value[2];
                }
            });

            // scale group
            const div_scale = document.createElement("div");
            div_scale.className = "form-group";
            div_scale.style.display = "inline";
            div_scale.appendChild(in_scaleX);
            div_scale.appendChild(lbl_scaleX);
            div_scale.appendChild(in_scaleY);
            div_scale.appendChild(lbl_scaleY);
            div_scale.appendChild(in_scaleZ);
            div_scale.appendChild(lbl_scaleZ);

            this.div.appendChild(div_scale);
        }


        this.updateUi();
    }

    updateUi(){
        const const_this = this;
        while(this.sceneObj_list != null && this.sceneObj_list.firstChild){
            this.sceneObj_list.firstChild.remove();
        }

        // Scene objects
        this.sceneObj_list = document.createElement("ul");
        this.sceneObj_list.style.border = "1px solid black";
        this.sceneObj_list.style.marginBottom  = "0px";
        this.div.appendChild(this.sceneObj_list);

        this.geo_scene._objectList.forEach( function(element, index) {
            new GeoObjectUi(element, const_this.geo_scene, const_this.sceneObj_list, "li");
        });
    }

}


class GeoObjectUi{

    constructor(geo_obj, geo_scene, parentElt, eltType = "div"){
        this.obj = geo_obj;
        this.geo_scene = geo_scene;
        this.parentElt = parentElt;

        this.div = document.createElement(eltType);
        this.parentElt.appendChild(this.div);

        // ui update
        this.updateUi();
    }

    updateUi(){
        const const_this = this;

        while(this.div.firstChild){
            this.div.firstChild.remove();
        }


        const sub_part_elt = document.createElement("ul");
        sub_part_elt.style.display = "none";
        const sub_part_toggler = createToggler("fas fa-chevron-circle-right", "blue", "blue", 
            ()=> {
                sub_part_elt.style.display = "";
                sub_part_toggler.className = "fas fa-chevron-circle-down";
            },  
            ()=> {
                sub_part_elt.style.display = "none"
                sub_part_toggler.className = "fas fa-chevron-circle-right";
            }, 
            sub_part_elt.style.display != "none");
        sub_part_toggler.title = "show/hide object actions";
        this.div.appendChild(sub_part_toggler);

        var nameElt = document.createTextNode(this.obj.name);
        this.div.appendChild(nameElt);



        var lookAt = document.createElement("i");
        lookAt.className = "fas fa-bullseye";
        lookAt.title = "Center view";
        lookAt.style.cursor = 'pointer';
        lookAt.style.margin = "2px";
        lookAt.onclick = function(){
            const_this.obj.lookAtMe(const_this.geo_scene._controls);
            //const_this.geo_scene.moveWorld([-const_this.obj.surface_loader.points[0][0], -const_this.obj.surface_loader.points[0][1], -const_this.obj.surface_loader.points[0][2]]);
        };
        this.div.appendChild(lookAt);

        var toggleVisibility = createToggler("fas fa-eye", "black", "red", 
            ()=> {
                const_this.obj.toggleObject(true);
            },  
            ()=> {
                const_this.obj.toggleObject(false);
            }, 
            this.obj.isPointsVisible() || this.obj.isLinesVisible() || this.obj.isFacesVisible());
        toggleVisibility.title = "Toggle surface visualisation"
        this.div.appendChild(toggleVisibility);

        var remover = document.createElement("i");
        remover.className = "far fa-trash-alt";
        remover.style.cursor = 'pointer';
        remover.style.margin = "2px";
        remover.title = "Delete object"
        remover.onclick = function(){
            const_this.geo_scene.removeObject(const_this.obj);
        };
        this.div.appendChild(remover);


        this.div.appendChild(sub_part_elt);


        // Object : 
        var object_li = document.createElement("li");
        sub_part_elt.appendChild(object_li);
        object_li.appendChild(document.createTextNode("Object : "));


        var verticalFlip = document.createElement("i");
        verticalFlip.className = "fas fa-arrows-alt-v";
        verticalFlip.title = "flip up";
        verticalFlip.style.cursor = 'pointer';
        verticalFlip.style.marginLeft = "4px";
        verticalFlip.style.marginRight = "4px";
        verticalFlip.onclick = function(){
            const_this.obj.flipY();
        };
        object_li.appendChild(verticalFlip);


        // salers 
        /*{
            // scale X
            const lbl_scaleX = document.createElement("label");
            lbl_scaleX.appendChild(document.createTextNode("X"))
            const in_scaleX = createScaler();
            in_scaleX.title = "Scale X";
            in_scaleX.onchange = function(){
                if(const_this.obj.getScaleX() != in_scaleX.value) // to prevent change when recieving event change
                    const_this.obj.scaleX(in_scaleX.value);
            };

            // scale Y
            const lbl_scaleY = document.createElement("label");
            lbl_scaleY.appendChild(document.createTextNode("Y"))
            const in_scaleY = createScaler();
            in_scaleY.title = "Scale Y";
            in_scaleY.onchange = function(){
                if(const_this.obj.getScaleY() != in_scaleY.value) // to prevent change when recieving event change
                    const_this.obj.scaleY(in_scaleY.value);
            };

            // scale Y
            const lbl_scaleZ = document.createElement("label");
            lbl_scaleZ.appendChild(document.createTextNode("Z"))
            const in_scaleZ = createScaler();
            in_scaleZ.title = "Scale Z";
            in_scaleZ.onchange = function(){
                if(const_this.obj.getScaleZ() != in_scaleZ.value) // to prevent change when recieving event change
                    const_this.obj.scaleZ(in_scaleZ.value);
            };

            document.addEventListener(GeoObject.EVT_OBJ_SCALED, (e) => {
                if(e.detail.obj == const_this.obj) {
                    in_scaleX.value = e.detail.value[0];
                    in_scaleY.value = e.detail.value[1];
                    in_scaleZ.value = e.detail.value[2];
                }
            });

            // scale group
            const div_scale = document.createElement("div");
            div_scale.className = "form-group";
            div_scale.style.display = "inline";
            div_scale.appendChild(in_scaleX);
            div_scale.appendChild(lbl_scaleX);
            div_scale.appendChild(in_scaleY);
            div_scale.appendChild(lbl_scaleY);
            div_scale.appendChild(in_scaleZ);
            div_scale.appendChild(lbl_scaleZ);

            object_li.appendChild(div_scale);
        }*/

        // Points : 
        {
            var points_li = document.createElement("li");
            sub_part_elt.appendChild(points_li);
            points_li.appendChild(document.createTextNode("Points : "));
    
            const points_toggler = createToggler("fas fa-dot-circle", "black", "red", ()=> {const_this.obj.togglePoints(true)},  ()=> {const_this.obj.togglePoints(false)}, const_this.obj.isPointsVisible());
            points_li.appendChild(points_toggler);
            document.addEventListener(GeoObject.EVT_OBJ_POINTS_VISIBILITY, (e) => {
                if(e.detail.obj == const_this.obj) {
                    points_toggler.toggleNoCallback(e.detail.value);
                }
            });
    
            const color_pick_points = document.createElement("input");
            color_pick_points.type = "color";
            color_pick_points.value = "#" + const_this.obj.points.material.color.getHexString();
            color_pick_points.title = "Points color";
            color_pick_points.style.cursor = 'pointer';
            color_pick_points.style.margin = "2px";
            color_pick_points.onchange = function(){
                const_this.obj.points.material.color = new THREE.Color(color_pick_points.value);
            };
            points_li.appendChild(color_pick_points);
    
            const point_size = document.createElement("input");
            point_size.type = "number";
            point_size.min = "0.1";
            point_size.max = "120";
            point_size.step = "0.1";
            point_size.value = const_this.obj.points.material.size;
            point_size.title = "Point size";
            point_size.style.cursor = 'pointer';
            point_size.style.margin = "2px";
            point_size.onchange = function(){
                const_this.obj.points.material.size = point_size.value;
            };
            points_li.appendChild(point_size);
        }


        if(this.obj.hasLines()){
            // Lines
            var lines_li = document.createElement("li");
            sub_part_elt.appendChild(lines_li);
            lines_li.appendChild(document.createTextNode("Lines : "));

            const lines_toggler = createToggler("far fa-square", "black", "red", ()=> {const_this.obj.toggleLines(true)},  ()=> {const_this.obj.toggleLines(false)}, const_this.obj.isLinesVisible());
            lines_li.appendChild(lines_toggler);
            document.addEventListener(GeoObject.EVT_OBJ_LINES_VISIBILITY, (e) => {
                if(e.detail.obj == const_this.obj) {
                    lines_toggler.toggleNoCallback(e.detail.value);
                }
            });

            const color_pick_lines = document.createElement("input");
            color_pick_lines.type = "color";
            color_pick_lines.value = "#" + const_this.obj.lines.material.color.getHexString();
            color_pick_lines.title = "Lines color";
            color_pick_lines.style.cursor = 'pointer';
            color_pick_lines.style.margin = "2px";
            color_pick_lines.onchange = function(){
                const_this.obj.lines.material.color = new THREE.Color(color_pick_lines.value);
            };
            lines_li.appendChild(color_pick_lines);

            /*
            // Line width is always 1 due to webGL limitations
            const line_size = document.createElement("input");
            line_size.type = "number";
            line_size.min = "0.1";
            line_size.max = "120";
            line_size.step = "0.1";
            line_size.value = const_this.obj.lines.material.linewidth;
            line_size.title = "Point size";
            line_size.style.cursor = 'pointer';
            line_size.style.margin = "2px";
            line_size.onchange = function(){
                console.log(const_this.obj.lines.material);
                const_this.obj.lines.material.linewidth = line_size.value;
            };
            lines_li.appendChild(line_size);*/
        }


        if(this.obj.hasFaces()){
            // Faces
            var faces_li = document.createElement("li");
            sub_part_elt.appendChild(faces_li);
            faces_li.appendChild(document.createTextNode("Faces : "));

            const faces_toggler = createToggler("fas fa-square", "black", "red", ()=> {const_this.obj.toggleFaces(true)},  ()=> {const_this.obj.toggleFaces(false)}, const_this.obj.isFacesVisible());
            faces_li.appendChild(faces_toggler);
            document.addEventListener(GeoObject.EVT_OBJ_FACES_VISIBILITY, (e) => {
                if(e.detail.obj == const_this.obj) {
                    faces_toggler.toggleNoCallback(e.detail.value);
                }
            });

            const color_pick_faces = document.createElement("input");
            color_pick_faces.type = "color";
            color_pick_faces.value = "#" + const_this.obj.faces.material.color.getHexString();
            color_pick_faces.title = "Faces color";
            color_pick_faces.style.cursor = 'pointer';
            color_pick_faces.style.margin = "2px";
            color_pick_faces.onchange = function(){
                const_this.obj.faces.material.color = new THREE.Color(color_pick_faces.value);
            };
            faces_li.appendChild(color_pick_faces);

            var orientFlip = document.createElement("i");
            orientFlip.className = "fas fa-undo";
            orientFlip.title = "flip faces orientation";
            orientFlip.style.cursor = 'pointer';
            orientFlip.style.margin = "2px";
            orientFlip.onclick = function(){
                const_this.obj.reverseOrient(const_this.geo_scene._theejs_scene);
            };
            faces_li.appendChild(orientFlip);
        }


        {
            // bbox
            var bbox_li = document.createElement("li");
            sub_part_elt.appendChild(bbox_li);
            bbox_li.appendChild(document.createTextNode("Bbox : "));

            const bbox_toggler = createToggler("far fa-square", "black", "red", ()=> {const_this.obj.toggleBbox(true)},  ()=> {const_this.obj.toggleBbox(false)}, const_this.obj.isBboxVisible());
            bbox_li.appendChild(bbox_toggler);
            document.addEventListener(GeoObject.EVT_OBJ_bbox_VISIBILITY, (e) => {
                if(e.detail.obj == const_this.obj) {
                    bbox_toggler.toggleNoCallback(e.detail.value);
                }
            });

            const color_pick_bbox = document.createElement("input");
            color_pick_bbox.type = "color";
            color_pick_bbox.value = "#" + const_this.obj.bbox.material.color.getHexString();
            color_pick_bbox.title = "Bbox color";
            color_pick_bbox.style.cursor = 'pointer';
            color_pick_bbox.style.margin = "2px";
            color_pick_bbox.onchange = function(){
                const_this.obj.bbox.material.color = new THREE.Color(color_pick_bbox.value);
            };
            bbox_li.appendChild(color_pick_bbox);
        }
        
    }
}




export {GeoScene, GeoObject, GeoSceneUi};