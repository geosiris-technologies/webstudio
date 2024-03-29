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
import {SurfaceLoader, __XYZ_POINT_REGEXP__} from "./surfaceLoader.js";

const __mesh_tr_regexp__ = new RegExp("\\s+(?<p0>\\d+)\\s+(?<p1>\\d+)\\s+(?<p2>\\d+)\\s*(?<trIdx>\\d+)\\s*", "");

export class MeshLoader extends SurfaceLoader{
    constructor(meshFileContent) {
        super();

        var fileContentArray = meshFileContent.split(/\n/);

        this.points = []
        this.trianglesIdx = []
        this.triangles = []
        this.lines = []

        var pointIdxOffset = 0;

        var sizeFound = false;
        var pointsStart = false;
        var pointsFinished = false;

        for(var lineIdx = 0; lineIdx < fileContentArray.length-1; lineIdx++){
            var line = fileContentArray[lineIdx];
            if(line.length > 0){
                var matchPoints = __XYZ_POINT_REGEXP__.exec(line);
                // TODO : ça marche pas pour les points !
                if(!(pointsFinished) && matchPoints != null){
                    if(!pointsStart){ // Only for the first point
                        try{
                            pointIdxOffset = 1;//matchPoints.groups["idx"];
                            console.log("offset = " + parseFloat(pointIdxOffset));

                        }catch(Exception){}
                    }
                    pointsStart = true;
                    this.points.push([parseFloat(matchPoints.groups["x"]), parseFloat(matchPoints.groups["y"]), parseFloat(matchPoints.groups["z"])]);
                    
                }else if(pointsStart) {
                    pointsFinished = true;
                    var matchTriangle = __mesh_tr_regexp__.exec(line);
                    if(matchTriangle != null){
                        this.trianglesIdx.push([parseInt(matchTriangle.groups["p0"]) - pointIdxOffset, parseInt(matchTriangle.groups["p1"]) - pointIdxOffset, parseInt(matchTriangle.groups["p2"]) - pointIdxOffset]);
                    }else{
                        /*console.log("|"+line+"| ");
                        console.log(__mesh_tr_regexp__.exec(line));
                        console.log(__XYZ_POINT_REGEXP__.exec(line));*/
                    }
                }else{
                    /*console.log("===");
                    console.log("|"+line+"|");
                    console.log(matchPoints);
                    console.log("===");*/
                }
                
            }
        }
        console.log(this.points.length);
        // console.log(this.triangles);
        this.computeLines();
        this.computeTriangles();
        this.computeBarycenter();
    }
}