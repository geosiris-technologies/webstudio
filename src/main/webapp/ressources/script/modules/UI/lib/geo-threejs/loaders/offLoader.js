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

const __off_tr_regexp__ = new RegExp("\\s*(?<nbPoints>\\d+)\\s+(?<p0>\\d+)\\s+(?<p1>\\d+)\\s+(?<p2>\\d+).*", "");

export class OffLoader extends SurfaceLoader{
    constructor(offFileContent) {
        super();

        var fileContentArray = offFileContent.split(/\n/);

        this.points = []
        this.trianglesIdx = []
        this.triangles = []
        this.lines = []

        var sizeFound = false;
        var pointsStart = false;
        var pointsFinished = false;
        var nbPoints = 0;

        for(var lineIdx = 0; lineIdx < fileContentArray.length-1; lineIdx++){
            var line = fileContentArray[lineIdx];
            try{
                if(line.length > 0){
                    if(!sizeFound){
                        var sizesMatch = __XYZ_POINT_REGEXP__.exec(line);
                        if(sizesMatch != null){
                            sizeFound = true;
                            // console.log(sizeFound)
                            nbPoints = sizesMatch.groups["x"];
                        }
                    }else{
                        var matchPoints = __XYZ_POINT_REGEXP__.exec(line);
                        // TODO : ça marche pas pour les points !
                        if(!(pointsFinished) && matchPoints != null && nbPoints > this.points.length){
                            pointsStart = true;
                            this.points.push([parseFloat(matchPoints.groups["x"]), parseFloat(matchPoints.groups["y"]), parseFloat(matchPoints.groups["z"])]);
                        }else if(pointsStart) {
                            var indices = [...line.matchAll("\\d+")].flat();
//                            if(integers.length > 0){ // To avoid adding empty value if no match
//                                var nbFacePoint = Number(integers[0]);
//                                this.trianglesIdx.push(integers.slice(1).map(i => parseInt(i)));
//
//                            }
                            // first is face vertice count
                            for(var i=2; i<indices.length - 1; i++){
                                if(indices[1] < this.points.length && indices[i] < this.points.length && indices[i + 1] < this.points.length){
                                    this.trianglesIdx.push([indices[1], indices[i], indices[i+1]]);
                                }else{
                                    console.log([indices[1], indices[i], indices[i+1]])
                                }
                                //console.log([indices[0], indices[i], indices[i+1]]);
                            }
                        }else{
                            /*console.log("===");
                            console.log("|"+line+"|");
                            console.log(matchPoints);
                            console.log("===");*/
                        }
                    }
                }
            }catch(exception){
                console.log(fileContent);
                console.log(exception);
                throw exception;
            }
        }
        /*console.log(this.points);
        console.log(this.trianglesIdx);*/
        this.computeLines();
        this.computeTriangles();
        this.computeBarycenter();
    }
}