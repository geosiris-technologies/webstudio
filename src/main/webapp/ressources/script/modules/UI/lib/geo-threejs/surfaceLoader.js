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
export class SurfaceLoader {
    constructor(fileType){
        this.points = [] // points list a matrix of [ [xa, ya, za], ..., [xn, yn, zn] ]
        this.trianglesIdx = [] // list of points idx for each triangle: [ [t0_a, t0_b, t0_c], ..., [tn_a, tn_b, tn_c] ]
        this.triangles = [] // a flat list of points [ t0_a_x, t0_a_y, t0_a_z,  t0_b_x, t0_b_y, t0_b_z,  t0_c_x, t0_c_y, t0_c_z, 
                            //                        ..., 
                            //                                                                           tn_c_x, tn_c_y, tn_c_z]
        this.lines = [] // all pair lines [ l0_a_x, l0_a_y, l0_a_z,  l0_b_x, l0_b_y, l0_b_z,  
                        //                  l1_a_x, l1_a_y, l1_a_z, ..., 
                        //                                           ln_b_x, ln_b_y, ln_b_z]
        this.barycenter = [] // array of [x, y, z] the barycenter of the surface
    }

    computeLines(){
        this.lines = [];

        for(var ti=0; ti<this.trianglesIdx.length; ti++){
            this.lines.push(this.points[this.trianglesIdx[ti][0]]);
            this.lines.push(this.points[this.trianglesIdx[ti][1]]);
            this.lines.push(this.points[this.trianglesIdx[ti][1]]);
            this.lines.push(this.points[this.trianglesIdx[ti][2]]);
            this.lines.push(this.points[this.trianglesIdx[ti][2]]);
            this.lines.push(this.points[this.trianglesIdx[ti][0]]);
        }
        return this.lines;
    }

    computeTriangles(){
        this.triangles = [];

        for(var ti=0; ti<this.trianglesIdx.length; ti++){
            this.triangles.push(this.points[this.trianglesIdx[ti][0]])
            this.triangles.push(this.points[this.trianglesIdx[ti][1]])
            this.triangles.push(this.points[this.trianglesIdx[ti][2]])
        }
        this.triangles = this.triangles.flat();
        return this.triangles;
    }

    reverseTrianglesOrientation(){
        var trIdxNew = [];
        for(var ti=0; ti<this.trianglesIdx.length; ti++){
            trIdxNew.push([this.trianglesIdx[ti][2], this.trianglesIdx[ti][1], this.trianglesIdx[ti][0]]);
        }
        this.trianglesIdx = trIdxNew;
        this.computeTriangles();
    }

    computeBarycenter(){
        this.barycenter = [ this.points[0][0] / this.points.length, 
                            this.points[0][1] / this.points.length, 
                            this.points[0][2] / this.points.length];

        for(var pi=1; pi<this.points.length; pi++){
            this.barycenter[0] += this.points[pi][0] / this.points.length;
            this.barycenter[1] += this.points[pi][1] / this.points.length;
            this.barycenter[2] += this.points[pi][2] / this.points.length;
        }
        return this.barycenter;
    }
}