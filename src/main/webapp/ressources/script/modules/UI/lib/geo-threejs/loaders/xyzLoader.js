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

export class XYZLoader extends SurfaceLoader{
    constructor(xyzFileContent) {
        super();

        var fileContentArray = xyzFileContent.split(/\n/);

        this.points = []
        this.trianglesIdx = []
        this.triangles = []
        this.lines = []

        var sizeFound = false;
        var pointsStart = false;
        var pointsFinished = false;

        for(var lineIdx = 0; lineIdx < fileContentArray.length; lineIdx++){
            var line = fileContentArray[lineIdx];
            if(line.length > 0){
                if(!sizeFound){
                    console.log(line)
                    sizeFound = __XYZ_POINT_REGEXP__.exec(line) != null;
                    // console.log(sizeFound)
                }else{
                    var matchPoints = __XYZ_POINT_REGEXP__.exec(line);
                    // TODO : ça marche pas pour les points !
                    if(!(pointsFinished) && matchPoints != null){
                        pointsStart = true;
                        this.points.push([parseFloat(matchPoints.groups["x"]), parseFloat(matchPoints.groups["y"]), parseFloat(matchPoints.groups["z"])]);
                    }
                }
            }
        }
        this.computeBarycenter();
    }
}