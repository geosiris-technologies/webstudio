import {SurfaceLoader} from "./surfaceLoader.js";

const __xyz_point_rgx__ = "[-+]?([0-9]*[.])?[0-9]+([eE][-+]?\\d+)?";
const __xyz_point_regexp__ = new RegExp("\\s*(?<x>"+__xyz_point_rgx__ + ")\\s+(?<y>"+__xyz_point_rgx__ + ")\\s+(?<z>"+__xyz_point_rgx__ + ")\\s*", "");

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

        for(var lineIdx = 0; lineIdx < fileContentArray.length-1; lineIdx++){
            var line = fileContentArray[lineIdx];
            if(line.length > 0){
                if(!sizeFound){
                    console.log(line)
                    sizeFound = __xyz_point_regexp__.exec(line) != null;
                    // console.log(sizeFound)
                }else{
                    var matchPoints = __xyz_point_regexp__.exec(line);
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