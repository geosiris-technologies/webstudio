import * as THREE from 'three';

export function createCubeExplodedPlanes(size=1.0, exploadFactor=0.8, colorPlane=0x6e6d6a){
    let material = new THREE.MeshBasicMaterial({
        side: THREE.DoubleSide,
        color: colorPlane
        //transparent: true,
        //opacity: 0,
        //depthTest: false
    });
    let geometry = new THREE.PlaneGeometry(size * exploadFactor, size * exploadFactor);
    let securitySize = size + 0.002;


    // up and Down
    let p1 = new THREE.Mesh(geometry.clone(), material.clone());
    p1.position.z = securitySize * 0.5;
    let p6 = new THREE.Mesh(geometry.clone(), material.clone());
    p6.position.z = - securitySize * 0.5;

    let p2 = new THREE.Mesh(geometry.clone(), material.clone());
    p2.rotation.y = Math.PI / 2;
    p2.position.x = securitySize * 0.5;
    let p5 = new THREE.Mesh(geometry.clone(), material.clone());
    p5.rotation.y = Math.PI / 2;
    p5.position.x = - securitySize * 0.5;

    let p3 = new THREE.Mesh(geometry.clone(), material.clone());
    p3.rotation.x = Math.PI / 2;
    p3.position.y = securitySize * 0.5;
    let p4 = new THREE.Mesh(geometry.clone(), material.clone());
    p4.rotation.x = Math.PI / 2;
    p4.position.y = - securitySize * 0.5;

    return [p1, p2, p3, p4, p5, p6];
}

export function createTextures(text_array, backgroundColor, textColor) {
    let materials = [];
    var textureCanvas = document.createElement("canvas");
    let size = 100;
    textureCanvas.width = size;
    textureCanvas.height = size;

    var ctx = textureCanvas.getContext("2d");
    ctx.textBaseline = 'middle';
    ctx.textAlign = 'center';
    ctx.font = "15px sans-serif";

    let textureLoader = new THREE.TextureLoader();
    for(var i = 0; i < text_array.length; i++) {
        // background
        if(Array.isArray(backgroundColor)){
            ctx.fillStyle = backgroundColor[i];
        }else{
            ctx.fillStyle = backgroundColor;
        }
        ctx.fillRect(0, 0, size, size);

        // text
        if(Array.isArray(textColor)){
            ctx.fillStyle = textColor[i];
        }else{
            ctx.fillStyle = textColor;
        }
        ctx.fillText(text_array[i], size / 2, size / 2);

        materials.push(new THREE.MeshBasicMaterial({
            map: textureLoader.load(textureCanvas.toDataURL())
        }));
    }
    return materials;
}

export class CompassCube{

    constructor(size, colorPlane=0x6e6d6a, colorCube=0xababab){
        this.planes = createCubeExplodedPlanes(size, 0.8, colorPlane);

        let fullCubeMaterial = new THREE.MeshBasicMaterial({
            side: THREE.DoubleSide,
            color: colorCube
        });
        //let cubeLabels = ['UP', 'RIGHT', 'FRONT', 'BACK', 'LEFT', 'DOWN'];
        let cubeLabels = [ 'RIGHT', 'LEFT', 'BACK', 'FRONT', 'UP', 'DOWN'];
        this.cube = new THREE.Mesh(new THREE.BoxGeometry(size, size, size), createTextures(cubeLabels, "#"+colorCube.toString(16), "black"));
    }

    addToScene(scene){
        //this.planes.forEach(plane => scene.add(plane));
        scene.add(this.cube);
    }
}