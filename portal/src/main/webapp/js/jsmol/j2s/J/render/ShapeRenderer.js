Clazz.declarePackage ("J.render");
Clazz.load (null, "J.render.ShapeRenderer", ["J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.g3d = null;
this.modelSet = null;
this.shape = null;
this.myVisibilityFlag = 0;
this.shapeID = 0;
this.colix = 0;
this.mad = 0;
this.madBeg = 0;
this.madMid = 0;
this.madEnd = 0;
this.exportType = 0;
this.isExport = false;
Clazz.instantialize (this, arguments);
}, J.render, "ShapeRenderer");
$_M(c$, "initRenderer", 
function () {
});
$_M(c$, "setViewerG3dShapeID", 
function (viewer, shapeID) {
this.viewer = viewer;
this.shapeID = shapeID;
this.myVisibilityFlag = J.viewer.JC.getShapeVisibilityFlag (shapeID);
this.initRenderer ();
}, "J.viewer.Viewer,~N");
$_M(c$, "renderShape", 
function (g3d, modelSet, shape) {
this.g3d = g3d;
this.modelSet = modelSet;
this.shape = shape;
this.exportType = g3d.getExportType ();
this.isExport = (this.exportType != 0);
var needsTranslucent = this.render ();
this.exportType = 0;
this.isExport = false;
return needsTranslucent;
}, "J.api.JmolRendererInterface,J.modelset.ModelSet,J.shape.Shape");
});
