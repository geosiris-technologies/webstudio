import {SurfaceLoader} from "./surfaceLoader.js";

const __obj_point_rgx__ = "[-+]?([0-9]*[.])?[0-9]+([eE][-+]?\\d+)?";
const __obj_point_regexp__ = new RegExp("\\s*[vV]\\s+(?<x>"+__obj_point_rgx__ + ")\\s+(?<y>"+__obj_point_rgx__ + ")\\s+(?<z>"+__obj_point_rgx__ + ")\\s*", "");
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