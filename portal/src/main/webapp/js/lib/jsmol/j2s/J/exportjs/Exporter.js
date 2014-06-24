Clazz.declarePackage ("J.exportjs");
Clazz.load (["J.util.AxisAngle4f", "$.P3", "$.V3"], "J.exportjs.Exporter", ["java.lang.Float", "J.util.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.privateKey = 0;
this.jmolRenderer = null;
this.$output = null;
this.bw = null;
this.fileName = null;
this.commandLineOptions = null;
this.isToFile = false;
this.g3d = null;
this.backgroundColix = 0;
this.screenWidth = 0;
this.screenHeight = 0;
this.slabZ = 0;
this.depthZ = 0;
this.fixedRotationCenter = null;
this.referenceCenter = null;
this.cameraPosition = null;
this.cameraDistance = 0;
this.aperatureAngle = 0;
this.scalePixelsPerAngstrom = 0;
this.exportType = 0;
this.tempP1 = null;
this.tempP2 = null;
this.tempP3 = null;
this.center = null;
this.tempV1 = null;
this.tempV2 = null;
this.tempV3 = null;
this.tempA = null;
this.appletName = null;
this.nBytes = 0;
this.lineWidthMad = 0;
Clazz.instantialize (this, arguments);
}, J.exportjs, "Exporter");
Clazz.prepareFields (c$, function () {
this.tempP1 =  new J.util.P3 ();
this.tempP2 =  new J.util.P3 ();
this.tempP3 =  new J.util.P3 ();
this.center =  new J.util.P3 ();
this.tempV1 =  new J.util.V3 ();
this.tempV2 =  new J.util.V3 ();
this.tempV3 =  new J.util.V3 ();
this.tempA =  new J.util.AxisAngle4f ();
});
Clazz.makeConstructor (c$, 
function () {
});
$_M(c$, "setRenderer", 
function (jmolRenderer) {
this.jmolRenderer = jmolRenderer;
}, "J.api.JmolRendererInterface");
$_M(c$, "initializeOutput", 
function (viewer, privateKey, g3d, output) {
this.viewer = viewer;
this.appletName = J.util.TextFormat.split (viewer.getHtmlName (), '_')[0];
this.g3d = g3d;
this.privateKey = privateKey;
this.backgroundColix = viewer.getObjectColix (0);
this.center.setT (viewer.getRotationCenter ());
if ((this.screenWidth <= 0) || (this.screenHeight <= 0)) {
this.screenWidth = viewer.getScreenWidth ();
this.screenHeight = viewer.getScreenHeight ();
}this.slabZ = g3d.getSlab ();
this.depthZ = g3d.getDepth ();
var cameraFactors = viewer.getCameraFactors ();
this.referenceCenter = cameraFactors[0];
this.cameraPosition = cameraFactors[1];
this.fixedRotationCenter = cameraFactors[2];
this.cameraDistance = cameraFactors[3].x;
this.aperatureAngle = cameraFactors[3].y;
this.scalePixelsPerAngstrom = cameraFactors[3].z;
this.isToFile = (Clazz.instanceOf (output, String));
if (this.isToFile) {
this.fileName = output;
var pt = this.fileName.indexOf (":::");
if (pt > 0) {
this.commandLineOptions = this.fileName.substring (pt + 3);
this.fileName = this.fileName.substring (0, pt);
}try {
System.out.println ("__Exporter writing to " + viewer.getAbsolutePath (privateKey, this.fileName));
this.bw = viewer.openOutputChannel (privateKey, this.fileName, true);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
return false;
} else {
throw e;
}
}
} else {
this.$output = output;
}this.outputHeader ();
return true;
}, "J.viewer.Viewer,~N,J.util.GData,~O");
$_M(c$, "output", 
function (data) {
this.nBytes += data.length;
try {
if (this.bw == null) this.$output.append (data);
 else this.bw.write (data);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
}, "~S");
c$.setTempVertex = $_M(c$, "setTempVertex", 
function (pt, offset, ptTemp) {
ptTemp.setT (pt);
if (offset != null) ptTemp.add (offset);
}, "J.util.P3,J.util.P3,J.util.P3");
$_M(c$, "outputVertices", 
function (vertices, nVertices, offset) {
for (var i = 0; i < nVertices; i++) {
if (Float.isNaN (vertices[i].x)) continue;
this.outputVertex (vertices[i], offset);
this.output ("\n");
}
}, "~A,~N,J.util.P3");
$_M(c$, "outputVertex", 
function (pt, offset) {
J.exportjs.Exporter.setTempVertex (pt, offset, this.tempP1);
this.output (this.tempP1);
}, "J.util.P3,J.util.P3");
$_M(c$, "outputFooter", 
function () {
});
$_M(c$, "finalizeOutput", 
function () {
this.outputFooter ();
if (!this.isToFile) return (this.$output == null ? "" : this.$output.toString ());
try {
this.bw.flush ();
this.bw.close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
System.out.println (e.toString ());
return "ERROR EXPORTING FILE";
} else {
throw e;
}
}
return "OK " + this.nBytes + " " + this.jmolRenderer.getExportName () + " " + this.fileName;
});
$_M(c$, "getTriad", 
function (t) {
return J.exportjs.Exporter.round (t.x) + " " + J.exportjs.Exporter.round (t.y) + " " + J.exportjs.Exporter.round (t.z);
}, "J.util.Tuple3f");
c$.round = $_M(c$, "round", 
function (number) {
var s;
return (number == 0 ? "0" : number == 1 ? "1" : (s = "" + (Math.round (number * 1000) / 1000)).startsWith ("0.") ? s.substring (1) : s.startsWith ("-0.") ? "-" + s.substring (2) : s.endsWith (".0") ? s.substring (0, s.length - 2) : s);
}, "~N");
c$.round = $_M(c$, "round", 
function (pt) {
return J.exportjs.Exporter.round (pt.x) + " " + J.exportjs.Exporter.round (pt.y) + " " + J.exportjs.Exporter.round (pt.z);
}, "J.util.Tuple3f");
$_M(c$, "drawSurface", 
function (meshSurface, colix) {
var nVertices = meshSurface.vertexCount;
if (nVertices == 0) return;
var nFaces = 0;
var nPolygons = meshSurface.polygonCount;
var bsPolygons = meshSurface.bsPolygons;
var faceVertexMax = (meshSurface.haveQuads ? 4 : 3);
var indices = meshSurface.polygonIndexes;
var isAll = (bsPolygons == null);
if (isAll) {
for (var i = nPolygons; --i >= 0; ) nFaces += (faceVertexMax == 4 && indices[i].length == 4 ? 2 : 1);

} else {
for (var i = bsPolygons.nextSetBit (0); i >= 0; i = bsPolygons.nextSetBit (i + 1)) nFaces += (faceVertexMax == 4 && indices[i].length == 4 ? 2 : 1);

}if (nFaces == 0) return;
var vertices = meshSurface.getVertices ();
var normals = meshSurface.normals;
var colorSolid = (colix != 0);
var colixes = (colorSolid ? null : meshSurface.vertexColixes);
var polygonColixes = (colorSolid ? meshSurface.polygonColixes : null);
this.outputSurface (vertices, normals, colixes, indices, polygonColixes, nVertices, nPolygons, nFaces, bsPolygons, faceVertexMax, colix, meshSurface.offset);
}, "J.util.MeshSurface,~N");
$_M(c$, "outputSurface", 
function (vertices, normals, colixes, indices, polygonColixes, nVertices, nPolygons, nFaces, bsPolygons, faceVertexMax, colix, offset) {
}, "~A,~A,~A,~A,~A,~N,~N,~N,J.util.BS,~N,~N,J.util.P3");
$_M(c$, "drawFilledCircle", 
function (colixRing, colixFill, diameter, x, y, z) {
if (colixRing != 0) this.drawCircle (x, y, z, diameter, colixRing, false);
if (colixFill != 0) this.drawCircle (x, y, z, diameter, colixFill, true);
}, "~N,~N,~N,~N,~N,~N");
$_M(c$, "plotImage", 
function (x, y, z, image, bgcolix, width, height) {
}, "~N,~N,~N,java.awt.Image,~N,~N,~N");
$_M(c$, "plotText", 
function (x, y, z, colix, text, font3d) {
}, "~N,~N,~N,~N,~S,J.util.JmolFont");
Clazz.defineStatics (c$,
"degreesPerRadian", (57.29577951308232));
});
