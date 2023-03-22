import {SurfaceLoader} from "./surfaceLoader.js";

const __mesh_point_rgx__ = "[-+]?([0-9]*[.])?[0-9]+([eE][-+]?\\d+)?";
const __mesh_point_regexp__ = new RegExp("\\s*(?<x>"+__mesh_point_rgx__ + ")\\s+(?<y>"+__mesh_point_rgx__ + ")\\s+(?<z>"+__mesh_point_rgx__ + ")(\\s+(?<idx>"+__mesh_point_rgx__ + "))?\\s*", "");
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
                var matchPoints = __mesh_point_regexp__.exec(line);
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
                        console.log(__mesh_point_regexp__.exec(line));*/
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