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
import {SurfaceLoader} from "./surfaceLoader.js";

const __off_point_rgx__ = "[-+]?([0-9]*[.])?[0-9]+([eE][-+]?\\d+)?";
const __off_point_regexp__ = new RegExp("\\s*(?<x>"+__off_point_rgx__ + ")\\s+(?<y>"+__off_point_rgx__ + ")\\s+(?<z>"+__off_point_rgx__ + ")\\s*", "");
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
            if(line.length > 0){
                if(!sizeFound){
                    var sizesMatch = __off_point_regexp__.exec(line);
                    if(sizesMatch != null){
                        sizeFound = true;
                        // console.log(sizeFound)
                        nbPoints = sizesMatch.groups["x"];
                    }
                }else{
                    var matchPoints = __off_point_regexp__.exec(line);
                    // TODO : ça marche pas pour les points !
                    if(!(pointsFinished) && matchPoints != null && nbPoints > this.points.length){
                        pointsStart = true;
                        this.points.push([parseFloat(matchPoints.groups["x"]), parseFloat(matchPoints.groups["y"]), parseFloat(matchPoints.groups["z"])]);
                    }else if(pointsStart) {
                        //console.log("pointsStart", pointsStart);
                        pointsFinished = true;
                        var matchTriangle = __off_tr_regexp__.exec(line);
                        if(matchTriangle != null){
                            this.trianglesIdx.push([parseInt(matchTriangle.groups["p0"]), parseInt(matchTriangle.groups["p1"]), parseInt(matchTriangle.groups["p2"])]);
                        }else{
                            /*console.log("|"+line+"| ");
                            console.log(__off_tr_regexp__.exec(line));
                            console.log(__off_point_regexp__.exec(line));*/
                        }
                    }else{
                        /*console.log("===");
                        console.log("|"+line+"|");
                        console.log(matchPoints);
                        console.log("===");*/
                    }
                }
            }
        }
        /*console.log(this.points);
        console.log(this.trianglesIdx);*/
        this.computeLines();
        this.computeTriangles();
        this.computeBarycenter();
    }
}