import * as THREE from 'three';

const v3 = THREE.Vector3;

export function randomColor(){
    return Math.random() * 0xffffff;
}

export function createPointCloud(array, col_mat = randomColor(), material = null, point_size = 1.0){
    if(material == null)
        material = new THREE.PointsMaterial( { color: col_mat, size : point_size } );
    const geometry = new THREE.BufferGeometry().setFromPoints( array.map(parray => new v3(parray[0], parray[1], parray[2])) );
    const pointCloud = new THREE.Points( geometry, material );
    return pointCloud
}

export function createSegments(array, col_mat = randomColor(), material = null, line_width = 1.0){
    if(material == null)
        material = new THREE.LineBasicMaterial( { color: col_mat, linewidth: line_width } );
    const geometry = new THREE.BufferGeometry().setFromPoints( array.map(parray => new v3(parray[0], parray[1], parray[2])));
    const segments = new THREE.LineSegments( geometry, material );
    return segments
}


export function createMesh(array, colMat = randomColor(), material = null){
    if(material == null)
        material = new THREE.MeshBasicMaterial( { 
            color: colMat, 
            side: THREE.DoubleSide,
        } );
    const geometry = new THREE.BufferGeometry();
    const vertices = new Float32Array(array);
    geometry.setAttribute( 'position', new THREE.BufferAttribute( vertices, 3 ) );
    const surf = new THREE.Mesh( geometry, material );
    return surf;
}


export function createLineTable(vArray, col_mat){
    if(col_mat==null)
        col_mat = 0x0000ff
    const material = new THREE.LineBasicMaterial( { color: col_mat } );

    const geometry = new THREE.BufferGeometry().setFromPoints( vArray );
    const line = new THREE.Line( geometry, material );
    return line
}

export function createCube(){
    const geometry = new THREE.BoxGeometry( 2, 2, 2 );
    const material = new THREE.MeshBasicMaterial( { color: 0x00ff00 } );
    const cube = new THREE.Object3D();
    
    const cubeSurf = new THREE.Mesh( geometry, material );
    cube.add(cubeSurf);

    var l0 = createLineTable([new v3(-1,-1,-1), new v3(1,-1,-1), new v3(1,1,-1), new v3(-1,1,-1), new v3(-1,-1,-1)], 0xff0000)
    var l1 = createLineTable([new v3(-1,-1, 1), new v3(1,-1, 1), new v3(1,1, 1), new v3(-1,1, 1), new v3(-1,-1, 1)], 0xff0000)

    var l2 = createLineTable([new v3(-1,-1,-1), new v3(-1,-1, 1)], 0xff0000)
    var l3 = createLineTable([new v3( 1,-1,-1), new v3( 1,-1, 1)], 0xff0000)
    var l4 = createLineTable([new v3( 1, 1,-1), new v3( 1, 1, 1)], 0xff0000)
    var l5 = createLineTable([new v3(-1, 1,-1), new v3(-1, 1, 1)], 0xff0000)
    
    cube.add(l0)
    cube.add(l1)
    cube.add(l2)
    cube.add(l3)
    cube.add(l4)
    cube.add(l5)

    return cube;
}


export function createSphere(p, r, col_mat= 0x0000ff){
    const geometry = new THREE.SphereGeometry( r );
    const material = new THREE.MeshBasicMaterial( { color: col_mat } );
    const sphere = new THREE.Mesh( geometry, material );
    if(Array.isArray(p)){
        p = new v3(p[0], p[1], p[2]);
    }
    sphere.translateX(p.x);
    sphere.translateY(p.y);
    sphere.translateZ(p.z);
    
    return sphere;
}


export function getPointRayIntersection(mouseX, mouseY, camera, obj_list){
    var raycaster = new THREE.Raycaster();
    var mouse = new THREE.Vector2(mouseX, mouseY);
    raycaster.setFromCamera(mouse, camera);
    var intersects = raycaster.intersectObjects(obj_list, true); //array
    if (intersects.length > 0) {
        return intersects[0].point;
    }
    return null;
}

export function centerCamera(controls, point_array){
    controls.object.position.set( point_array[0], point_array[1], point_array[2] + 10 );
    controls.target.set( point_array[0], point_array[1], point_array[2] );
    controls.update();
}



/*


console.log(material)
    var uniforms = {};
function fragmentShader()
{
  return `
      void main()
      {
         gl_FragColor = vec4( ` + material.color.r +","+ material.color.g +","+ material.color.b +", 1.0" + `);
      }
  `;
}

function vertexShader()
{
    return `
    void main()
    {
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    }
    `;
}
    material =  new THREE.ShaderMaterial({
                    uniforms         : uniforms,
                    fragmentShader    : fragmentShader(),
                    vertexShader        : vertexShader(),
                });
*/