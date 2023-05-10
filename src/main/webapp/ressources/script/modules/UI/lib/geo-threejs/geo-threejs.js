/*
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
*/
import * as THREE from 'three';

import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import { TrackballControls } from 'three/addons/controls/TrackballControls.js';

import { createCube, centerCamera, getPointRayIntersection, createSphere } from './utils.js';
import { GeoObject, GeoScene, GeoSceneUi } from './geoScene.js';

// Loaders
import { OffLoader } from './offLoader.js';
import { ObjLoader } from './objLoader.js';
import { XYZLoader } from './xyzLoader.js';
import { MeshLoader } from './meshLoader.js';
import { PolylineLoader } from './polylineLoader.js';

export function fun_import_surface(geotjs, fileContent, file_name=""){
    if(file_name.endsWith(".off")){
        console.log("reading off");
        geotjs.geoScene.addObject(new GeoObject(new OffLoader(fileContent), file_name));
    }else if(file_name.endsWith(".xyz")){
        console.log("reading xyz");
        geotjs.geoScene.addObject(new GeoObject(new XYZLoader(fileContent), file_name));
    }else if(file_name.endsWith(".mesh")){
        console.log("reading mesh");
        geotjs.geoScene.addObject(new GeoObject(new MeshLoader(fileContent), file_name));
    }else if(file_name.endsWith(".polyline")){
        console.log("reading polyline");
        geotjs.geoScene.addObject(new GeoObject(new PolylineLoader(fileContent), file_name));
    }else{
        console.log("reading obj");
        geotjs.geoScene.addObject(new GeoObject(new ObjLoader(fileContent), file_name));
    }
    geotjs.animate();
}


export class GeoThreeJS{

    constructor(width, height, createSceneUi=true){
        this.width = width;
        this.height = height;
        this.createSceneUi = createSceneUi;
        this.DO_ANIMATE = true;

        this.scene = new THREE.Scene();

        this.canvasElt = document.createElement("div");

        this.isAnimated = false;

        this.domElt = document.createElement("div");
        this.domElt.appendChild(this.canvasElt);

        if(this.createSceneUi){
            this.canvasElt.className = "geothreejs-canvas";
            var threeJSLabel = document.createElement("a");
            threeJSLabel.href = "https://threejs.org/";
            threeJSLabel.className = "geothreejs-canvas-label";
            threeJSLabel.appendChild(document.createTextNode("ThreeJS view"));
            this.canvasElt.appendChild(threeJSLabel);

            this.sceneUIElt = document.createElement("div");
            this.sceneUIElt.className = "geothreejs-scene-ui";
            this.domElt.appendChild(this.sceneUIElt);
        }else{
            this.canvasElt.className = "geothreejs-canvas-no-ui";
        }
        
        this.camera = null;
        this.renderer = null;
        this.controls = null;

        this.geoScene = null;
        this.geoUi = null;

        // createView function must be called after to have the UI
    }

    setupCamera(up = [0,0,1]){
        if( this.camera ){ // removing old cam from the scene
            this.scene.remove(this.camera);
        }
        this.camera = new THREE.PerspectiveCamera( 75, this.width / this.height, 0.2, 50000 );
        this.camera.position.set( 0, -30, 50 );
        this.camera.up.set( up[0], up[1], up[2] );
        this.camera.lookAt( 0, 0, 10 );
        this.scene.add(this.camera);
    }

    setupRenderer(){
        this.renderer = new THREE.WebGLRenderer({
            alpha: true,
            antialias: true
        });
        this.renderer.setSize( this.width, this.height );
        this.canvasElt.appendChild(this.renderer.domElement);
    }

    setupControls(){
        if(this.controls != null)
            this.controls.dispose();
        //this.controls = new TrackballControls(this.camera, this.renderer.domElement);
        this.controls = new OrbitControls (this.camera, this.renderer.domElement);
        this.controls.mouseButtons = {
            LEFT: THREE.MOUSE.ROTATE,
            MIDDLE: THREE.MOUSE.DOLLY,
            RIGHT: THREE.MOUSE.PAN
        }
        this.controls.update();
    }

    animate(){
        this.isAnimated = true;
        const const_this = this;
        try{
            requestAnimationFrame( () => const_this.animate() );
            this.controls.update();
            if(this.DO_ANIMATE){
                this.renderer.render( this.scene, this.camera );
            }
        }catch(Exception){console.log(Exception)}
    }

    createView(parent){
        const const_this = this;
        parent.appendChild(this.domElt);
        this.setupRenderer();
        this.setupCamera();
        this.setupControls();

        if(this.createSceneUi){
            this.geoScene = new GeoScene(this.scene, this.controls);
            this.geoUi = new GeoSceneUi(this.geoScene, this.sceneUIElt);
            this.sceneUIElt.addEventListener(GeoScene.EVT_RESET_CONTROLS, (e) => {
                const_this.setupControls();
            });
        }

        
        return this.domElt;
    }

    importSurface(fileContent, fileName){
        fun_import_surface(this, fileContent, fileName);
    }
}