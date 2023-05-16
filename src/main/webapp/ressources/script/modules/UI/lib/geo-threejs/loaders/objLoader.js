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
import {SurfaceLoader, __XYZ_COORD_RGX__} from "./surfaceLoader.js";

const __obj_point_regexp__ = new RegExp("\\s*[vV]\\s+(?<x>"+__XYZ_COORD_RGX__ + ")\\s+(?<y>"+__XYZ_COORD_RGX__ + ")\\s+(?<z>"+__XYZ_COORD_RGX__ + ")\\s*", "");
const __obj_tr_point_rgx__ = "\\d+(/\\d+)*";
const __obj_tr_regexp__ = new RegExp("\\s*f\\s+(?<p0>"+__obj_tr_point_rgx__+")\\s+(?<p1>"+__obj_tr_point_rgx__+")\\s+(?<p2>"+__obj_tr_point_rgx__+")\\s*", "");

export class ObjLoader extends SurfaceLoader{
    constructor(objFileContent) {
        super();

        var fileContentArray = objFileContent.split(/\n/);

        this.points = []
        this.trianglesIdx = []
        this.triangles = []
        this.lines = []

        var pointsStart = false;
        var pointsFinished = false;

        for(var lineIdx = 0; lineIdx < fileContentArray.length-1; lineIdx++){
            var line = fileContentArray[lineIdx];
            if(line.length > 0){
                var matchPoints = __obj_point_regexp__.exec(line);
                // TODO : ça marche pas pour les points !
                if(!(pointsFinished) && matchPoints != null){
                    pointsStart = true;
                    this.points.push([parseFloat(matchPoints.groups["x"]), parseFloat(matchPoints.groups["y"]), parseFloat(matchPoints.groups["z"])]);
                }else if(pointsStart) {
                    pointsFinished = true;
                    var matchTriangle = __obj_tr_regexp__.exec(line);
                    if(matchTriangle != null){
                        // index of point starts at 1
                        this.trianglesIdx.push([parseInt(matchTriangle.groups["p0"] - 1), parseInt(matchTriangle.groups["p1"] - 1), parseInt(matchTriangle.groups["p2"] - 1)]);
                    }else{}
                }else{}
            }
        }
        this.computeLines();
        this.computeTriangles();
        this.computeBarycenter();
    }
}