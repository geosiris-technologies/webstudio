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
import {SurfaceLoader, __XYZ_POINT_RGX__} from "./surfaceLoader.js";

export class PolylineLoader extends SurfaceLoader{
    constructor(polylineFileContent) {
        super();
        
        this.lines = []
        
        var fileContentArray = polylineFileContent.split(/\n/);

        for(var lineIdx = 0; lineIdx < fileContentArray.length; lineIdx++){
            var line = fileContentArray[lineIdx];
            if(line.length > 0){
                try{
                    var polyPoints = [...line.matchAll(__XYZ_POINT_RGX__)].map(
                        m => [parseFloat(m.groups["x"]), parseFloat(m.groups["y"]), parseFloat(m.groups["z"])]
                    )
                    this.points.push(...polyPoints);
                    var polyline = [];
                    polyline.push(polyPoints[0]);
                    for(var i=1; i<polyPoints.length-1; i++){
                        polyline.push(polyPoints[i]);
                        polyline.push(polyPoints[i]); // for the next segment
                    }
                    polyline.push(polyPoints[polyPoints.length - 1]);
                    this.lines.push(...polyline);
                }catch(e){console.log(e);}
            }
        }
        this.computeBarycenter();
    }
}