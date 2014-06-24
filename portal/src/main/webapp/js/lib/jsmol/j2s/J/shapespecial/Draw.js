Clazz.declarePackage ("J.shapespecial");
Clazz.load (["java.lang.Enum", "J.shape.MeshCollection", "J.util.P3i", "$.V3"], "J.shapespecial.Draw", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "J.shapespecial.DrawMesh", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.C", "$.Escape", "$.JmolList", "$.Logger", "$.Measure", "$.MeshSurface", "$.P3", "$.SB", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.dmeshes = null;
this.thisMesh = null;
this.ptList = null;
this.offset = null;
this.nPoints = 0;
this.diameter = 0;
this.width = 0;
this.newScale = 0;
this.length = 0;
this.isCurve = false;
this.isArc = false;
this.isArrow = false;
this.isLine = false;
this.isVector = false;
this.isCircle = false;
this.isPerpendicular = false;
this.isCylinder = false;
this.isVertices = false;
this.isPlane = false;
this.isReversed = false;
this.isRotated45 = false;
this.isCrossed = false;
this.isValid = false;
this.noHead = false;
this.isBarb = false;
this.indicatedModelIndex = -1;
this.modelInfo = null;
this.makePoints = false;
this.nidentifiers = 0;
this.nbitsets = 0;
this.plane = null;
this.bsAllModels = null;
this.polygon = null;
this.vData = null;
this.intersectID = null;
this.boundBox = null;
this.lineData = null;
this.slabData = null;
this.vAB = null;
this.vAC = null;
this.ptXY = null;
Clazz.instantialize (this, arguments);
}, J.shapespecial, "Draw", J.shape.MeshCollection);
Clazz.prepareFields (c$, function () {
this.dmeshes =  new Array (4);
this.offset =  new J.util.V3 ();
this.vAB =  new J.util.V3 ();
this.vAC =  new J.util.V3 ();
this.ptXY =  new J.util.P3i ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.shapespecial.Draw, []);
this.htObjects =  new java.util.Hashtable ();
});
Clazz.overrideMethod (c$, "allocMesh", 
function (thisID, m) {
var index = this.meshCount++;
this.meshes = this.dmeshes = J.util.ArrayUtil.ensureLength (this.dmeshes, this.meshCount * 2);
this.currentMesh = this.thisMesh = this.dmeshes[index] = (m == null ?  new J.shapespecial.DrawMesh (thisID, this.colix, index) : m);
this.currentMesh.color = this.color;
this.currentMesh.index = index;
if (thisID != null && thisID !== "+PREVIOUS_MESH+" && this.htObjects != null) this.htObjects.put (thisID.toUpperCase (), this.currentMesh);
}, "~S,J.shape.Mesh");
$_M(c$, "setPropertySuper", 
function (propertyName, value, bs) {
this.currentMesh = this.thisMesh;
this.setPropMC (propertyName, value, bs);
this.thisMesh = this.currentMesh;
}, "~S,~O,J.util.BS");
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shapespecial.Draw, "initShape", []);
this.myType = "draw";
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if ("init" === propertyName) {
this.initDraw ();
this.setPropertySuper ("init", value, bs);
return;
}if ("length" === propertyName) {
this.length = (value).floatValue ();
return;
}if ("fixed" === propertyName) {
this.isFixed = (value).booleanValue ();
return;
}if ("intersect" === propertyName) {
if (Clazz.instanceOf (value, String)) this.intersectID = value;
 else this.boundBox = value;
return;
}if ("slab" === propertyName) {
var meshIndex = this.getIndexFromName (value);
if (meshIndex < 0) {
return;
}var m = this.meshes[meshIndex];
if (m.checkByteCount != 1) return;
this.slabData = J.util.MeshSurface.newSlab (m.vertices, m.vertexCount,  Clazz.newFloatArray (m.vertexCount, 0), m.polygonIndexes, m.polygonCount, 1);
return;
}if ("lineData" === propertyName) {
this.lineData =  new J.util.JmolList ();
if (this.indicatedModelIndex < 0) this.indicatedModelIndex = this.viewer.getCurrentModelIndex ();
var fdata = value;
var n = Clazz.doubleToInt (fdata.length / 6);
for (var i = 0, pt = 0; i < n; i++) this.lineData.addLast ([J.util.P3.new3 (fdata[pt++], fdata[pt++], fdata[pt++]), J.util.P3.new3 (fdata[pt++], fdata[pt++], fdata[pt++])]);

return;
}if ("modelIndex" === propertyName) {
this.indicatedModelIndex = (value).intValue ();
if (this.indicatedModelIndex < 0 || this.indicatedModelIndex >= this.viewer.getModelCount ()) return;
this.vData.addLast ([Integer.$valueOf (4), (this.modelInfo = [this.indicatedModelIndex, 0])]);
return;
}if ("planedef" === propertyName) {
this.plane = value;
if (this.intersectID != null || this.boundBox != null || this.slabData != null) return;
if (this.isCircle || this.isArc) this.isPlane = true;
this.vData.addLast ([Integer.$valueOf (1), J.util.P3.new3 (NaN, NaN, NaN)]);
return;
}if ("perp" === propertyName) {
this.isPerpendicular = true;
return;
}if ("cylinder" === propertyName) {
this.isCylinder = true;
return;
}if ("plane" === propertyName) {
this.isPlane = true;
return;
}if ("curve" === propertyName) {
this.isCurve = true;
return;
}if ("arrow" === propertyName) {
this.isArrow = true;
return;
}if ("line" === propertyName) {
this.isLine = true;
this.isCurve = true;
return;
}if ("arc" === propertyName) {
this.isCurve = true;
this.isArc = true;
if (this.isArrow) {
this.isArrow = false;
this.isVector = true;
}return;
}if ("circle" === propertyName) {
this.isCircle = true;
return;
}if ("vector" === propertyName) {
this.isArrow = true;
this.isVector = true;
return;
}if ("vertices" === propertyName) {
this.isVertices = true;
return;
}if ("reverse" === propertyName) {
this.isReversed = true;
return;
}if ("nohead" === propertyName) {
this.noHead = true;
return;
}if ("isbarb" === propertyName) {
this.isBarb = true;
return;
}if ("rotate45" === propertyName) {
this.isRotated45 = true;
return;
}if ("crossed" === propertyName) {
this.isCrossed = true;
return;
}if ("points" === propertyName) {
this.newScale = (value).floatValue () / 100;
if (this.newScale == 0) this.newScale = 1;
return;
}if ("scale" === propertyName) {
this.newScale = (value).floatValue () / 100;
if (this.newScale == 0) this.newScale = 0.01;
if (this.thisMesh != null) {
this.scale (this.thisMesh, this.newScale);
this.thisMesh.initialize (1073741964, null, null);
}return;
}if ("diameter" === propertyName) {
this.diameter = (value).intValue ();
return;
}if ("width" === propertyName) {
this.width = (value).floatValue ();
return;
}if ("identifier" === propertyName) {
var thisID = value;
var meshIndex = this.getIndexFromName (thisID);
if (meshIndex >= 0) {
this.vData.addLast ([Integer.$valueOf (2), [meshIndex, this.isReversed ? 1 : 0, this.isVertices ? 1 : 0]]);
this.isReversed = this.isVertices = false;
this.nidentifiers++;
} else {
J.util.Logger.error ("draw identifier " + value + " not found");
this.isValid = false;
}return;
}if ("polygon" === propertyName) {
this.polygon = value;
if (this.polygon == null) this.polygon =  new J.util.JmolList ();
return;
}if ("coord" === propertyName) {
this.vData.addLast ([Integer.$valueOf (1), value]);
if (this.indicatedModelIndex >= 0) this.modelInfo[1]++;
return;
}if ("offset" === propertyName) {
this.offset = J.util.V3.newV (value);
if (this.thisMesh != null) this.thisMesh.offset (this.offset);
return;
}if ("atomSet" === propertyName) {
if (J.util.BSUtil.cardinalityOf (value) == 0) return;
var bsAtoms = value;
this.vData.addLast ([Integer.$valueOf (3), bsAtoms]);
this.nbitsets++;
if (this.isCircle && this.diameter == 0 && this.width == 0) this.width = this.viewer.calcRotationRadiusBs (bsAtoms) * 2.0;
return;
}if ("modelBasedPoints" === propertyName) {
this.vData.addLast ([Integer.$valueOf (5), value]);
return;
}if ("set" === propertyName) {
if (this.thisMesh == null) {
this.allocMesh (null, null);
this.thisMesh.colix = this.colix;
this.thisMesh.color = this.color;
}this.thisMesh.isValid = (this.isValid ? this.setDrawing (value) : false);
if (this.thisMesh.isValid) {
if (this.thisMesh.vertexCount > 2 && this.length != 3.4028235E38 && this.newScale == 1) this.newScale = this.length;
this.scale (this.thisMesh, this.newScale);
this.thisMesh.initialize (1073741964, null, null);
J.shapespecial.Draw.setAxes (this.thisMesh);
this.thisMesh.title = this.title;
this.thisMesh.visible = true;
}this.nPoints = -1;
this.vData = null;
this.lineData = null;
return;
}if (propertyName === "deleteModelAtoms") {
this.deleteModels (((value)[2])[0]);
return;
}this.setPropertySuper (propertyName, value, bs);
}, "~S,~O,J.util.BS");
$_M(c$, "deleteModels", 
function (modelIndex) {
for (var i = this.meshCount; --i >= 0; ) {
var m = this.dmeshes[i];
if (m == null) continue;
var deleteMesh = (m.modelIndex == modelIndex);
if (m.modelFlags != null) {
m.deleteAtoms (modelIndex);
deleteMesh = (m.modelFlags.length () == 0);
if (!deleteMesh) continue;
}if (deleteMesh) {
this.meshCount--;
this.deleteMeshElement (i);
} else if (this.meshes[i].modelIndex > modelIndex) {
this.meshes[i].modelIndex--;
}}
this.resetObjects ();
}, "~N");
$_M(c$, "deleteMeshElement", 
function (i) {
if (this.meshes[i] === this.currentMesh) this.currentMesh = this.thisMesh = null;
this.meshes = this.dmeshes = J.util.ArrayUtil.deleteElements (this.meshes, i, 1);
}, "~N");
$_M(c$, "initDraw", 
($fz = function () {
this.boundBox = null;
this.bsAllModels = null;
this.colix = 5;
this.color = 0xFFFFFFFF;
this.diameter = 0;
this.explicitID = false;
this.indicatedModelIndex = -1;
this.intersectID = null;
this.isCurve = this.isArc = this.isArrow = this.isPlane = this.isCircle = this.isCylinder = this.isLine = false;
this.isFixed = this.isReversed = this.isRotated45 = this.isCrossed = this.noHead = this.isBarb = false;
this.isPerpendicular = this.isVertices = this.isVector = false;
this.isValid = true;
this.length = 3.4028235E38;
this.lineData = null;
this.newScale = 0;
this.nidentifiers = this.nbitsets = 0;
this.offset = null;
this.plane = null;
this.polygon = null;
this.slabData = null;
this.vData =  new J.util.JmolList ();
this.width = 0;
this.setPropertySuper ("thisID", "+PREVIOUS_MESH+", null);
}, $fz.isPrivate = true, $fz));
$_M(c$, "resetObjects", 
function () {
this.htObjects.clear ();
for (var i = 0; i < this.meshCount; i++) {
var m = this.meshes[i];
m.index = i;
this.htObjects.put (m.thisID.toUpperCase (), m);
}
});
Clazz.overrideMethod (c$, "getPropertyData", 
function (property, data) {
if (property === "getCenter") {
var id = data[0];
var index = (data[1]).intValue ();
var modelIndex = (data[2]).intValue ();
data[2] = this.getSpinCenter (id, index, modelIndex);
return (data[2] != null);
}if (property === "getSpinAxis") {
var id = data[0];
var index = (data[1]).intValue ();
data[2] = this.getSpinAxis (id, index);
return (data[2] != null);
}return this.getPropDataMC (property, data);
}, "~S,~A");
Clazz.overrideMethod (c$, "getProperty", 
function (property, index) {
if (property === "command") return this.getCommand (this.thisMesh);
if (property === "type") return Integer.$valueOf (this.thisMesh == null ? J.shapespecial.Draw.EnumDrawType.NONE.id : this.thisMesh.drawType.id);
return this.getPropMC (property);
}, "~S,~N");
$_M(c$, "getSpinCenter", 
($fz = function (axisID, vertexIndex, modelIndex) {
var id;
var pt = axisID.indexOf ("[");
var pt2;
if (pt > 0) {
id = axisID.substring (0, pt);
if ((pt2 = axisID.lastIndexOf ("]")) < pt) pt2 = axisID.length;
try {
vertexIndex = Integer.parseInt (axisID.substring (pt + 1, pt2));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
} else {
id = axisID;
}var m = this.getMesh (id);
if (m == null || m.vertices == null) return null;
if (vertexIndex == 2147483647) return J.util.P3.new3 (m.index + 1, this.meshCount, m.vertexCount);
if (vertexIndex != -2147483648) vertexIndex = m.getVertexIndexFromNumber (vertexIndex);
return (vertexIndex >= 0 ? m.vertices[vertexIndex] : m.ptCenters == null || modelIndex < 0 || modelIndex >= m.ptCenters.length ? m.ptCenter : m.ptCenters[modelIndex]);
}, $fz.isPrivate = true, $fz), "~S,~N,~N");
$_M(c$, "getSpinAxis", 
($fz = function (axisID, modelIndex) {
var m = this.getMesh (axisID);
return (m == null || m.vertices == null ? null : m.ptCenters == null || modelIndex < 0 ? m.axis : m.axes[modelIndex]);
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "setDrawing", 
($fz = function (connections) {
if (this.thisMesh == null) this.allocMesh (null, null);
this.thisMesh.clear ("draw");
this.thisMesh.diameter = this.diameter;
this.thisMesh.width = this.width;
if (this.intersectID != null || this.boundBox != null) this.setIntersectData ();
 else if (this.slabData != null) this.setSlabData ();
if (this.polygon == null && (this.lineData != null ? this.lineData.size () == 0 : (this.vData.size () == 0) == (connections == null)) || !this.isArrow && connections != null) return false;
var modelCount = this.viewer.getModelCount ();
if (this.polygon != null || this.lineData != null || this.indicatedModelIndex < 0 && (this.isFixed || this.isArrow || this.isCurve || this.isCircle || this.isCylinder || modelCount == 1)) {
this.thisMesh.modelIndex = (this.lineData == null ? this.viewer.getCurrentModelIndex () : this.indicatedModelIndex);
this.thisMesh.isFixed = (this.isFixed || this.lineData == null && this.thisMesh.modelIndex < 0 && modelCount > 1);
if (this.isFixed && modelCount > 1) this.thisMesh.modelIndex = -1;
 else if (this.lineData == null && this.thisMesh.modelIndex < 0) this.thisMesh.modelIndex = 0;
this.thisMesh.ptCenters = null;
this.thisMesh.modelFlags = null;
this.thisMesh.drawTypes = null;
this.thisMesh.drawVertexCounts = null;
this.thisMesh.connections = connections;
if (this.polygon != null) {
if (this.polygon.size () == 0) return false;
this.thisMesh.isTriangleSet = true;
this.thisMesh.vertices = this.polygon.get (0);
this.thisMesh.polygonIndexes = this.polygon.get (1);
this.thisMesh.drawVertexCount = this.thisMesh.vertexCount = this.thisMesh.vertices.length;
this.thisMesh.polygonCount = this.thisMesh.polygonIndexes.length;
for (var i = 0; i < this.thisMesh.polygonCount; i++) {
for (var j = 0; j < 3; j++) if (this.thisMesh.polygonIndexes[i][j] >= this.thisMesh.vertexCount) return false;

}
this.thisMesh.drawType = J.shapespecial.Draw.EnumDrawType.POLYGON;
this.thisMesh.checkByteCount = 1;
} else if (this.lineData != null) {
this.thisMesh.lineData = this.lineData;
} else {
this.thisMesh.setPolygonCount (1);
if (this.setPoints (-1, -1)) this.setPoints (-1, this.nPoints);
this.setPolygon (0);
}} else {
this.thisMesh.modelIndex = -1;
this.thisMesh.setPolygonCount (modelCount);
this.thisMesh.ptCenters =  new Array (modelCount);
this.thisMesh.modelFlags =  new J.util.BS ();
this.thisMesh.drawTypes =  new Array (modelCount);
this.thisMesh.drawVertexCounts =  Clazz.newIntArray (modelCount, 0);
this.thisMesh.vertexCount = 0;
if (this.indicatedModelIndex >= 0) {
this.setPoints (-1, 0);
this.thisMesh.drawType = J.shapespecial.Draw.EnumDrawType.MULTIPLE;
this.thisMesh.drawVertexCount = -1;
this.thisMesh.modelFlags.set (this.indicatedModelIndex);
this.indicatedModelIndex = -1;
} else {
var bsModels = this.viewer.getVisibleFramesBitSet ();
for (var iModel = 0; iModel < modelCount; iModel++) {
if (bsModels.get (iModel) && this.setPoints (iModel, -1)) {
this.setPoints (iModel, this.nPoints);
this.setPolygon (iModel);
this.thisMesh.setCenter (iModel);
this.thisMesh.drawTypes[iModel] = this.thisMesh.drawType;
this.thisMesh.drawVertexCounts[iModel] = this.thisMesh.drawVertexCount;
this.thisMesh.drawType = J.shapespecial.Draw.EnumDrawType.MULTIPLE;
this.thisMesh.drawVertexCount = -1;
this.thisMesh.modelFlags.set (iModel);
} else {
this.thisMesh.drawTypes[iModel] = J.shapespecial.Draw.EnumDrawType.NONE;
this.thisMesh.polygonIndexes[iModel] =  Clazz.newIntArray (0, 0);
}}
}}this.thisMesh.isVector = this.isVector;
this.thisMesh.noHead = this.noHead;
this.thisMesh.isBarb = this.isBarb;
this.thisMesh.width = (this.thisMesh.drawType === J.shapespecial.Draw.EnumDrawType.CYLINDER || this.thisMesh.drawType === J.shapespecial.Draw.EnumDrawType.CIRCULARPLANE ? -Math.abs (this.width) : this.width);
this.thisMesh.setCenter (-1);
if (this.offset != null) this.thisMesh.offset (this.offset);
if (this.thisMesh.thisID == null) {
this.thisMesh.thisID = this.thisMesh.drawType.$$name + (++this.nUnnamed);
this.htObjects.put (this.thisMesh.thisID, this.thisMesh);
}this.clean ();
return true;
}, $fz.isPrivate = true, $fz), "~A");
Clazz.overrideMethod (c$, "clean", 
function () {
for (var i = this.meshCount; --i >= 0; ) if (this.meshes[i] == null || this.meshes[i].vertexCount == 0 && this.meshes[i].connections == null && this.meshes[i].lineData == null) this.deleteMeshI (i);

});
$_M(c$, "setIntersectData", 
($fz = function () {
if (this.boundBox != null) {
if (this.plane == null) {
}} else if (this.plane != null && this.intersectID != null) {
var vData =  new J.util.JmolList ();
var data = [this.intersectID, this.plane, vData, null];
this.viewer.getShapePropertyData (24, "intersectPlane", data);
if (vData.size () == 0) return;
this.indicatedModelIndex = (data[3]).intValue ();
this.lineData = vData;
}}, $fz.isPrivate = true, $fz));
$_M(c$, "setSlabData", 
($fz = function () {
if (this.plane != null) {
this.slabData.getIntersection (0, this.plane, null, null, null, null, null, false, true, 135266319, false);
this.polygon =  new J.util.JmolList ();
this.polygon.addLast (this.slabData.vertices);
this.polygon.addLast (this.slabData.polygonIndexes);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "addPoint", 
($fz = function (newPt, iModel) {
var isOK = (iModel < 0 || this.bsAllModels.get (iModel));
if (this.makePoints) {
if (!isOK) return;
this.ptList[this.nPoints] = J.util.P3.newP (newPt);
if (newPt.z == 3.4028235E38 || newPt.z == -3.4028235E38) this.thisMesh.haveXyPoints = true;
} else if (iModel >= 0) {
this.bsAllModels.set (iModel);
}this.nPoints++;
}, $fz.isPrivate = true, $fz), "J.util.P3,~N");
$_M(c$, "setPoints", 
($fz = function (iModel, n) {
this.makePoints = (n >= 0);
if (this.makePoints) {
this.ptList =  new Array (Math.max (5, n));
if (this.bsAllModels == null) this.bsAllModels = this.viewer.getVisibleFramesBitSet ();
}this.nPoints = 0;
var nData = this.vData.size ();
var modelIndex = 0;
var bs;
var bsModel = (iModel < 0 ? null : this.viewer.getModelUndeletedAtomsBitSet (iModel));
for (var i = 0; i < nData; i++) {
var info = this.vData.get (i);
switch ((info[0]).intValue ()) {
case 4:
var modelInfo = info[1];
modelIndex = modelInfo[0];
this.nPoints = modelInfo[1];
var nVertices = Math.max (this.nPoints, 3);
var n0 = this.thisMesh.vertexCount;
if (this.nPoints > 0) {
var p = this.thisMesh.polygonIndexes[modelIndex] =  Clazz.newIntArray (nVertices, 0);
for (var j = 0; j < this.nPoints; j++) {
info = this.vData.get (++i);
p[j] = this.thisMesh.addV (info[1]);
}
for (var j = this.nPoints; j < 3; j++) {
p[j] = n0 + this.nPoints - 1;
}
this.thisMesh.drawTypes[modelIndex] = J.shapespecial.Draw.EnumDrawType.getType (this.nPoints);
this.thisMesh.drawVertexCounts[modelIndex] = this.nPoints;
this.thisMesh.modelFlags.set (modelIndex);
}break;
case 1:
this.addPoint (info[1], (this.makePoints ? iModel : -1));
break;
case 3:
bs = J.util.BSUtil.copy (info[1]);
if (bsModel != null) bs.and (bsModel);
if (bs.length () > 0) this.addPoint (this.viewer.getAtomSetCenter (bs), (this.makePoints ? iModel : -1));
break;
case 2:
var idInfo = info[1];
var m = this.dmeshes[idInfo[0]];
var isReversed = (idInfo[1] == 1);
var isVertices = (idInfo[2] == 1);
if (m.modelIndex > 0 && m.modelIndex != iModel) return false;
if (this.bsAllModels == null) this.bsAllModels =  new J.util.BS ();
if (this.isPlane && !this.isCircle || this.isPerpendicular || isVertices) {
if (isReversed) {
if (iModel < 0 || iModel >= m.polygonCount) for (var ipt = m.drawVertexCount; --ipt >= 0; ) this.addPoint (m.vertices[ipt], iModel);

 else if (m.polygonIndexes[iModel] != null) for (var ipt = m.drawVertexCounts[iModel]; --ipt >= 0; ) this.addPoint (m.vertices[m.polygonIndexes[iModel][ipt]], iModel);

} else {
if (iModel < 0 || iModel >= m.polygonCount) for (var ipt = 0; ipt < m.drawVertexCount; ipt++) this.addPoint (m.vertices[ipt], iModel);

 else if (m.polygonIndexes[iModel] != null) for (var ipt = 0; ipt < m.drawVertexCounts[iModel]; ipt++) this.addPoint (m.vertices[m.polygonIndexes[iModel][ipt]], iModel);

}} else {
if (iModel < 0 || m.ptCenters == null || m.ptCenters[iModel] == null) this.addPoint (m.ptCenter, iModel);
 else this.addPoint (m.ptCenters[iModel], iModel);
}break;
case 5:
var modelBasedPoints = info[1];
if (this.bsAllModels == null) this.bsAllModels =  new J.util.BS ();
for (var j = 0; j < modelBasedPoints.length; j++) if (iModel < 0 || j == iModel) {
var point = J.util.Escape.unescapePointOrBitsetOrMatrixOrArray (modelBasedPoints[j]);
this.bsAllModels.set (j);
if (Clazz.instanceOf (point, J.util.P3)) {
this.addPoint (point, j);
} else if (Clazz.instanceOf (point, J.util.BS)) {
bs = point;
if (bsModel != null) bs.and (bsModel);
if (bs.length () > 0) this.addPoint (this.viewer.getAtomSetCenter (bs), j);
}}
break;
}
}
if (this.makePoints && this.isCrossed && this.nPoints == 4) {
var pt = this.ptList[1];
this.ptList[1] = this.ptList[2];
this.ptList[2] = pt;
}return (this.nPoints > 0);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setPolygon", 
($fz = function (nPoly) {
var nVertices = this.nPoints;
var drawType = J.shapespecial.Draw.EnumDrawType.POINT;
if (this.isArc) {
if (nVertices >= 2) {
drawType = J.shapespecial.Draw.EnumDrawType.ARC;
} else {
this.isArc = false;
this.isVector = false;
this.isCurve = false;
this.isArrow = true;
}}if (this.isCircle) {
this.length = 0;
if (nVertices == 2) this.isPlane = true;
if (!this.isPlane) drawType = J.shapespecial.Draw.EnumDrawType.CIRCLE;
if (this.width == 0) this.width = 1;
} else if ((this.isCurve || this.isArrow) && nVertices >= 2 && !this.isArc) {
drawType = (this.isLine ? J.shapespecial.Draw.EnumDrawType.LINE_SEGMENT : this.isCurve ? J.shapespecial.Draw.EnumDrawType.CURVE : J.shapespecial.Draw.EnumDrawType.ARROW);
}if (this.isVector && !this.isArc) {
if (nVertices > 2) nVertices = 2;
 else if (this.plane == null && nVertices != 2) this.isVector = false;
}if (this.thisMesh.haveXyPoints) {
this.isPerpendicular = false;
if (nVertices == 3 && this.isPlane) this.isPlane = false;
this.length = 3.4028235E38;
this.thisMesh.diameter = 0;
} else if (nVertices == 2 && this.isVector) {
this.ptList[1].add (this.ptList[0]);
}var dist = 0;
if (this.isArc || this.plane != null && this.isCircle) {
if (this.plane != null) {
dist = J.util.Measure.distanceToPlane (this.plane, this.ptList[0]);
this.vAC.set (-this.plane.x, -this.plane.y, -this.plane.z);
this.vAC.normalize ();
if (dist < 0) this.vAC.scale (-1);
if (this.isCircle) {
this.vAC.scale (0.005);
this.ptList[0].sub (this.vAC);
this.vAC.scale (2);
}this.vAC.add (this.ptList[0]);
this.ptList[1] = J.util.P3.newP (this.vAC);
drawType = (this.isArrow ? J.shapespecial.Draw.EnumDrawType.ARROW : this.isArc ? J.shapespecial.Draw.EnumDrawType.ARC : J.shapespecial.Draw.EnumDrawType.CIRCULARPLANE);
}if (this.isArc) {
dist = Math.abs (dist);
if (nVertices > 3) {
} else if (nVertices == 3) {
this.ptList[3] = J.util.P3.newP (this.ptList[2]);
this.ptList[2] = J.shapespecial.Draw.randomPoint ();
} else {
if (nVertices == 2) {
this.ptList[2] = J.shapespecial.Draw.randomPoint ();
}this.ptList[3] = J.util.P3.new3 (0, 360, 0);
}if (this.plane != null) this.ptList[3].z *= dist;
nVertices = 4;
}this.plane = null;
} else if (drawType === J.shapespecial.Draw.EnumDrawType.POINT) {
var pt;
var center =  new J.util.P3 ();
var normal =  new J.util.V3 ();
if (nVertices == 2 && this.plane != null) {
this.ptList[1] = J.util.P3.newP (this.ptList[0]);
var vTemp =  new J.util.V3 ();
J.util.Measure.getPlaneProjection (this.ptList[1], this.plane, this.ptList[1], vTemp);
nVertices = -2;
if (this.isArrow) drawType = J.shapespecial.Draw.EnumDrawType.ARROW;
this.plane = null;
}if (nVertices == 3 && this.isPlane && !this.isPerpendicular) {
pt = J.util.P3.newP (this.ptList[1]);
pt.sub (this.ptList[0]);
pt.scale (0.5);
this.ptList[3] = J.util.P3.newP (this.ptList[2]);
this.ptList[2].add (pt);
this.ptList[3].sub (pt);
nVertices = 4;
} else if (nVertices >= 3 && !this.isPlane && this.isPerpendicular) {
J.util.Measure.calcNormalizedNormal (this.ptList[0], this.ptList[1], this.ptList[2], normal, this.vAB, this.vAC);
center =  new J.util.P3 ();
J.util.Measure.calcAveragePointN (this.ptList, nVertices, center);
dist = (this.length == 3.4028235E38 ? this.ptList[0].distance (center) : this.length);
normal.scale (dist);
this.ptList[0].setT (center);
this.ptList[1].setT (center);
this.ptList[1].add (normal);
nVertices = 2;
} else if (nVertices == 2 && this.isPerpendicular) {
J.util.Measure.calcAveragePoint (this.ptList[0], this.ptList[1], center);
dist = (this.length == 3.4028235E38 ? this.ptList[0].distance (center) : this.length);
if (this.isPlane && this.length != 3.4028235E38) dist /= 2;
if (this.isPlane && this.isRotated45) dist *= 1.4142;
J.util.Measure.getNormalToLine (this.ptList[0], this.ptList[1], normal);
normal.scale (dist);
if (this.isPlane) {
this.ptList[2] = J.util.P3.newP (center);
this.ptList[2].sub (normal);
pt = J.util.P3.newP (center);
pt.add (normal);
J.util.Measure.calcNormalizedNormal (this.ptList[0], this.ptList[1], this.ptList[2], normal, this.vAB, this.vAC);
normal.scale (dist);
this.ptList[3] = J.util.P3.newP (center);
this.ptList[3].add (normal);
this.ptList[1].setT (center);
this.ptList[1].sub (normal);
this.ptList[0].setT (pt);
if (this.isRotated45) {
J.util.Measure.calcAveragePoint (this.ptList[0], this.ptList[1], this.ptList[0]);
J.util.Measure.calcAveragePoint (this.ptList[1], this.ptList[2], this.ptList[1]);
J.util.Measure.calcAveragePoint (this.ptList[2], this.ptList[3], this.ptList[2]);
J.util.Measure.calcAveragePoint (this.ptList[3], pt, this.ptList[3]);
}nVertices = 4;
} else {
this.ptList[0].setT (center);
this.ptList[1].setT (center);
this.ptList[0].sub (normal);
this.ptList[1].add (normal);
}if (this.isArrow && nVertices != -2) this.isArrow = false;
} else if (nVertices == 2 && this.length != 3.4028235E38) {
J.util.Measure.calcAveragePoint (this.ptList[0], this.ptList[1], center);
normal.setT (this.ptList[1]);
normal.sub (center);
normal.scale (0.5 / normal.length () * (this.length == 0 ? 0.01 : this.length));
if (this.length == 0) center.setT (this.ptList[0]);
this.ptList[0].setT (center);
this.ptList[1].setT (this.ptList[0]);
this.ptList[0].sub (normal);
this.ptList[1].add (normal);
}if (nVertices > 4) nVertices = 4;
switch (nVertices) {
case -2:
nVertices = 2;
break;
case 1:
break;
case 2:
drawType = (this.isArc ? J.shapespecial.Draw.EnumDrawType.ARC : this.isPlane && this.isCircle ? J.shapespecial.Draw.EnumDrawType.CIRCULARPLANE : this.isCylinder ? J.shapespecial.Draw.EnumDrawType.CYLINDER : J.shapespecial.Draw.EnumDrawType.LINE);
break;
default:
drawType = (this.thisMesh.connections == null ? J.shapespecial.Draw.EnumDrawType.PLANE : J.shapespecial.Draw.EnumDrawType.ARROW);
}
}this.thisMesh.drawType = drawType;
this.thisMesh.drawVertexCount = nVertices;
if (nVertices == 0) return;
var nVertices0 = this.thisMesh.vertexCount;
for (var i = 0; i < nVertices; i++) {
this.thisMesh.addV (this.ptList[i]);
}
var npoints = (nVertices < 3 ? 3 : nVertices);
this.thisMesh.setPolygonCount (nPoly + 1);
this.thisMesh.polygonIndexes[nPoly] =  Clazz.newIntArray (npoints, 0);
for (var i = 0; i < npoints; i++) {
this.thisMesh.polygonIndexes[nPoly][i] = nVertices0 + (i < nVertices ? i : nVertices - 1);
}
return;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "scale", 
function (mesh, newScale) {
var dmesh = mesh;
if (newScale == 0 || dmesh.vertexCount == 0 && dmesh.connections == null || dmesh.scale == newScale) return;
var f = newScale / dmesh.scale;
dmesh.scale = newScale;
dmesh.isScaleSet = true;
if (dmesh.isRenderScalable ()) return;
var diff =  new J.util.V3 ();
var iptlast = -1;
var ipt = 0;
for (var i = dmesh.polygonCount; --i >= 0; ) {
var center = (dmesh.isVector ? dmesh.vertices[0] : dmesh.ptCenters == null ? dmesh.ptCenter : dmesh.ptCenters[i]);
if (center == null) return;
if (dmesh.polygonIndexes[i] == null) continue;
iptlast = -1;
for (var iV = dmesh.polygonIndexes[i].length; --iV >= 0; ) {
ipt = dmesh.polygonIndexes[i][iV];
if (ipt == iptlast) continue;
iptlast = ipt;
diff.sub2 (dmesh.vertices[ipt], center);
diff.scale (f);
diff.add (center);
dmesh.vertices[ipt].setT (diff);
}
}
}, "J.shape.Mesh,~N");
c$.setAxes = $_M(c$, "setAxes", 
($fz = function (m) {
m.axis = J.util.V3.new3 (0, 0, 0);
m.axes =  new Array (m.polygonCount > 0 ? m.polygonCount : 1);
if (m.vertices == null) return;
var n = 0;
for (var i = m.polygonCount; --i >= 0; ) {
var p = m.polygonIndexes[i];
m.axes[i] =  new J.util.V3 ();
if (p == null || p.length == 0) {
} else if (m.drawVertexCount == 2 || m.drawVertexCount < 0 && m.drawVertexCounts[i] == 2) {
m.axes[i].sub2 (m.vertices[p[0]], m.vertices[p[1]]);
n++;
} else {
J.util.Measure.calcNormalizedNormal (m.vertices[p[0]], m.vertices[p[1]], m.vertices[p[2]], m.axes[i], m.vAB, m.vAC);
n++;
}m.axis.add (m.axes[i]);
}
if (n == 0) return;
m.axis.scale (1 / n);
}, $fz.isPrivate = true, $fz), "J.shapespecial.DrawMesh");
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bs) {
for (var i = 0; i < this.meshCount; i++) {
var m = this.dmeshes[i];
if (m == null) {
continue;
}m.visibilityFlags = (m.isValid && m.visible ? this.myVisibilityFlag : 0);
if (m.modelIndex >= 0 && !bs.get (m.modelIndex) || m.modelFlags != null && !J.util.BSUtil.haveCommon (bs, m.modelFlags)) {
m.visibilityFlags = 0;
} else if (m.modelFlags != null) {
m.bsMeshesVisible.clearAll ();
m.bsMeshesVisible.or (m.modelFlags);
m.bsMeshesVisible.and (bs);
}}
}, "J.util.BS");
Clazz.overrideMethod (c$, "checkObjectClicked", 
function (x, y, action, bsVisible, drawPicking) {
var isPickingMode = (this.viewer.getPickingMode () == 4);
var isSpinMode = (this.viewer.getPickingMode () == 5);
if (!isPickingMode && !drawPicking && !isSpinMode || J.util.C.isColixTranslucent (this.colix)) return null;
if (!this.findPickedObject (x, y, false, bsVisible)) return null;
var v = this.pickedMesh.vertices[this.pickedMesh.polygonIndexes[this.pickedModel][this.pickedVertex]];
var modelIndex = this.pickedMesh.modelIndex;
var bs = (this.pickedMesh).modelFlags;
if (modelIndex < 0 && bs != null && J.util.BSUtil.cardinalityOf (bs) == 1) modelIndex = bs.nextSetBit (0);
if (drawPicking && !isPickingMode) {
if (action != 0) this.setStatusPicked (-2, v);
return this.getPickedPoint (v, modelIndex);
}if (action == 0 || this.pickedMesh.polygonIndexes[this.pickedModel][0] == this.pickedMesh.polygonIndexes[this.pickedModel][1]) {
return (action == 0 ? this.getPickedPoint (v, modelIndex) : null);
}var isClockwise = this.viewer.isBound (action, 42);
if (this.pickedVertex == 0) {
this.viewer.startSpinningAxis (this.pickedMesh.vertices[this.pickedMesh.polygonIndexes[this.pickedModel][1]], this.pickedMesh.vertices[this.pickedMesh.polygonIndexes[this.pickedModel][0]], isClockwise);
} else {
this.viewer.startSpinningAxis (this.pickedMesh.vertices[this.pickedMesh.polygonIndexes[this.pickedModel][0]], this.pickedMesh.vertices[this.pickedMesh.polygonIndexes[this.pickedModel][1]], isClockwise);
}return this.getPickedPoint (null, 0);
}, "~N,~N,~N,J.util.BS,~B");
Clazz.overrideMethod (c$, "checkObjectHovered", 
function (x, y, bsVisible) {
if (!this.viewer.getDrawHover ()) return false;
if (J.util.C.isColixTranslucent (this.colix)) return false;
if (!this.findPickedObject (x, y, false, bsVisible)) return false;
if (this.gdata.isDisplayAntialiased ()) {
x <<= 1;
y <<= 1;
}var s = (this.pickedMesh.title == null ? this.pickedMesh.thisID : this.pickedMesh.title[0]);
if (s.length > 1 && s.charAt (0) == '>') s = s.substring (1);
this.viewer.hoverOnPt (x, y, s, this.pickedMesh.thisID, this.pickedPt);
return true;
}, "~N,~N,J.util.BS");
Clazz.overrideMethod (c$, "checkObjectDragged", 
function (prevX, prevY, x, y, dragAction, bsVisible) {
if (this.viewer.getPickingMode () != 4) return false;
var moveAll = this.viewer.isBound (dragAction, 8);
var movePoint = this.viewer.isBound (dragAction, 9);
if (!moveAll && !movePoint) return false;
if (prevX == -2147483648) return this.findPickedObject (x, y, true, bsVisible);
if (prevX == 2147483647) {
this.pickedMesh = null;
return false;
}if (this.pickedMesh == null) return false;
var dm = this.pickedMesh;
this.move2D (dm, dm.polygonIndexes[this.pickedModel], this.pickedVertex, x, y, moveAll);
this.thisMesh = dm;
return true;
}, "~N,~N,~N,~N,~N,J.util.BS");
$_M(c$, "move2D", 
($fz = function (mesh, vertexes, iVertex, x, y, moveAll) {
if (vertexes == null || vertexes.length == 0) return;
if (this.gdata.isAntialiased ()) {
x <<= 1;
y <<= 1;
}var pt =  new J.util.P3 ();
var ptVertex = vertexes[iVertex];
var coord = J.util.P3.newP (mesh.altVertices == null ? mesh.vertices[ptVertex] : mesh.altVertices[ptVertex]);
var newcoord =  new J.util.P3 ();
var move =  new J.util.V3 ();
this.viewer.transformPt3f (coord, pt);
pt.x = x;
pt.y = y;
this.viewer.unTransformPoint (pt, newcoord);
move.setT (newcoord);
move.sub (coord);
if (mesh.isTriangleSet) iVertex = ptVertex;
var n = (!moveAll ? iVertex + 1 : mesh.isTriangleSet ? mesh.vertices.length : vertexes.length);
var bsMoved =  new J.util.BS ();
for (var i = (moveAll ? 0 : iVertex); i < n; i++) if (moveAll || i == iVertex) {
var k = (mesh.isTriangleSet ? i : vertexes[i]);
if (bsMoved.get (k)) continue;
bsMoved.set (k);
mesh.vertices[k].add (move);
}
if (mesh.altVertices != null) mesh.recalcAltVertices = true;
mesh.setCenters ();
}, $fz.isPrivate = true, $fz), "J.shapespecial.DrawMesh,~A,~N,~N,~N,~B");
$_M(c$, "findPickedObject", 
($fz = function (x, y, isPicking, bsVisible) {
var dmin2 = 100;
if (this.gdata.isAntialiased ()) {
x <<= 1;
y <<= 1;
dmin2 <<= 1;
}this.pickedModel = 0;
this.pickedVertex = 0;
this.pickedMesh = null;
for (var i = 0; i < this.meshCount; i++) {
var m = this.dmeshes[i];
if (m.visibilityFlags != 0) {
var mCount = (m.isTriangleSet ? m.polygonCount : m.modelFlags == null ? 1 : this.viewer.getModelCount ());
for (var iModel = mCount; --iModel >= 0; ) {
if (m.modelFlags != null && !m.modelFlags.get (iModel) || m.polygonIndexes == null || !m.isTriangleSet && (iModel >= m.polygonIndexes.length || m.polygonIndexes[iModel] == null)) continue;
for (var iVertex = (m.isTriangleSet ? 3 : m.polygonIndexes[iModel].length); --iVertex >= 0; ) {
try {
var iv = m.polygonIndexes[iModel][iVertex];
var pt = (m.altVertices == null ? m.vertices[iv] : m.altVertices[iv]);
var d2 = this.coordinateInRange (x, y, pt, dmin2, this.ptXY);
if (d2 >= 0) {
this.pickedMesh = m;
dmin2 = d2;
this.pickedModel = iModel;
this.pickedVertex = iVertex;
this.pickedPt = pt;
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println (e);
} else {
throw e;
}
}
}
}
}}
return (this.pickedMesh != null);
}, $fz.isPrivate = true, $fz), "~N,~N,~B,J.util.BS");
$_M(c$, "getCommand", 
function (mesh) {
if (mesh != null) return this.getCommand2 (mesh, mesh.modelIndex);
var sb =  new J.util.SB ();
var key = (this.explicitID && this.previousMeshID != null && J.util.TextFormat.isWild (this.previousMeshID) ? this.previousMeshID.toUpperCase () : null);
if (key != null && key.length == 0) key = null;
for (var i = 0; i < this.meshCount; i++) {
var m = this.meshes[i];
if (key == null || J.util.TextFormat.isMatch (m.thisID.toUpperCase (), key, true, true)) sb.append (this.getCommand2 (m, m.modelIndex));
}
return sb.toString ();
}, "J.shape.Mesh");
$_M(c$, "getCommand2", 
function (mesh, iModel) {
var dmesh = mesh;
if (dmesh.drawType === J.shapespecial.Draw.EnumDrawType.NONE && dmesh.lineData == null && dmesh.drawVertexCount == 0 && dmesh.drawVertexCounts == null) return "";
var str =  new J.util.SB ();
var modelCount = this.viewer.getModelCount ();
if (!dmesh.isFixed && iModel >= 0 && modelCount > 1) J.shape.Shape.appendCmd (str, "frame " + this.viewer.getModelNumberDotted (iModel));
str.append ("  draw ID ").append (J.util.Escape.eS (dmesh.thisID));
if (dmesh.isFixed) str.append (" fixed");
if (iModel < 0) iModel = 0;
if (dmesh.noHead) str.append (" noHead");
 else if (dmesh.isBarb) str.append (" barb");
if (dmesh.scale != 1 && dmesh.isScaleSet && (dmesh.haveXyPoints || dmesh.connections != null || dmesh.drawType === J.shapespecial.Draw.EnumDrawType.CIRCLE || dmesh.drawType === J.shapespecial.Draw.EnumDrawType.ARC)) str.append (" scale ").appendF (dmesh.scale);
if (dmesh.width != 0) str.append (" diameter ").appendF ((dmesh.drawType === J.shapespecial.Draw.EnumDrawType.CYLINDER ? Math.abs (dmesh.width) : dmesh.drawType === J.shapespecial.Draw.EnumDrawType.CIRCULARPLANE ? Math.abs (dmesh.width * dmesh.scale) : dmesh.width));
 else if (dmesh.diameter > 0) str.append (" diameter ").appendI (dmesh.diameter);
if (dmesh.lineData != null) {
str.append ("  lineData [");
var n = dmesh.lineData.size ();
for (var j = 0; j < n; ) {
var pts = dmesh.lineData.get (j);
var s = J.util.Escape.eP (pts[0]);
str.append (s.substring (1, s.length - 1));
str.append (",");
s = J.util.Escape.eP (pts[1]);
str.append (s.substring (1, s.length - 1));
if (++j < n) str.append (", ");
}
str.append ("]");
} else {
var nVertices = dmesh.drawVertexCount > 0 || dmesh.drawVertexCounts == null ? dmesh.drawVertexCount : dmesh.drawVertexCounts[iModel >= 0 ? iModel : 0];
switch (dmesh.drawTypes == null ? dmesh.drawType : dmesh.drawTypes[iModel]) {
case J.shapespecial.Draw.EnumDrawType.NONE:
case J.shapespecial.Draw.EnumDrawType.MULTIPLE:
break;
case J.shapespecial.Draw.EnumDrawType.POLYGON:
str.append (" POLYGON ").appendI (nVertices);
break;
case J.shapespecial.Draw.EnumDrawType.PLANE:
if (nVertices == 4) str.append (" PLANE");
break;
case J.shapespecial.Draw.EnumDrawType.LINE_SEGMENT:
str.append (" LINE");
break;
case J.shapespecial.Draw.EnumDrawType.ARC:
str.append (dmesh.isVector ? " ARROW ARC" : " ARC");
break;
case J.shapespecial.Draw.EnumDrawType.ARROW:
str.append (dmesh.isVector ? " VECTOR" : " ARROW");
if (dmesh.connections != null) str.append (" connect ").append (J.util.Escape.eAI (dmesh.connections));
break;
case J.shapespecial.Draw.EnumDrawType.CIRCLE:
str.append (" CIRCLE");
break;
case J.shapespecial.Draw.EnumDrawType.CURVE:
str.append (" CURVE");
break;
case J.shapespecial.Draw.EnumDrawType.CIRCULARPLANE:
case J.shapespecial.Draw.EnumDrawType.CYLINDER:
str.append (" CYLINDER");
break;
case J.shapespecial.Draw.EnumDrawType.POINT:
nVertices = 1;
break;
case J.shapespecial.Draw.EnumDrawType.LINE:
nVertices = 2;
break;
}
if (dmesh.modelIndex < 0 && !dmesh.isFixed) {
for (var i = 0; i < modelCount; i++) if (J.shapespecial.Draw.isPolygonDisplayable (dmesh, i)) {
if (nVertices == 0) nVertices = dmesh.drawVertexCounts[i];
str.append (" [ " + i);
var s = J.shapespecial.Draw.getVertexList (dmesh, i, nVertices);
if (s.indexOf ("NaN") >= 0) return "";
str.append (s);
str.append (" ] ");
}
} else if (dmesh.drawType === J.shapespecial.Draw.EnumDrawType.POLYGON) {
for (var i = 0; i < dmesh.vertexCount; i++) str.append (" ").append (J.util.Escape.eP (dmesh.vertices[i]));

str.append (" ").appendI (dmesh.polygonCount);
for (var i = 0; i < dmesh.polygonCount; i++) if (dmesh.polygonIndexes[i] == null) str.append (" [0 0 0 0]");
 else str.append (" ").append (J.util.Escape.eAI (dmesh.polygonIndexes[i]));

} else {
var s = J.shapespecial.Draw.getVertexList (dmesh, iModel, nVertices);
if (s.indexOf ("NaN") >= 0) return "";
str.append (s);
}}if (dmesh.mat4 != null) {
var v =  new J.util.V3 ();
dmesh.mat4.get (v);
str.append (" offset ").append (J.util.Escape.eP (v));
}if (dmesh.title != null) {
var s = "";
for (var i = 0; i < dmesh.title.length; i++) s += "|" + dmesh.title[i];

str.append (J.util.Escape.eS (s.substring (1)));
}str.append (";\n");
J.shape.Shape.appendCmd (str, dmesh.getState ("draw"));
J.shape.Shape.appendCmd (str, J.shape.Shape.getColorCommandUnk ("draw", dmesh.colix, this.translucentAllowed));
return str.toString ();
}, "J.shape.Mesh,~N");
c$.isPolygonDisplayable = $_M(c$, "isPolygonDisplayable", 
function (mesh, i) {
return (i < mesh.polygonIndexes.length && mesh.polygonIndexes[i] != null && mesh.polygonIndexes[i].length > 0);
}, "J.shape.Mesh,~N");
c$.getVertexList = $_M(c$, "getVertexList", 
($fz = function (mesh, iModel, nVertices) {
var str = "";
try {
if (iModel >= mesh.polygonIndexes.length) iModel = 0;
var adjustPt = (mesh.isVector && mesh.drawType !== J.shapespecial.Draw.EnumDrawType.ARC);
for (var i = 0; i < nVertices; i++) {
var pt = mesh.vertices[mesh.polygonIndexes[iModel][i]];
if (pt.z == 3.4028235E38 || pt.z == -3.4028235E38) {
str += (i == 0 ? " " : " ,") + "[" + Clazz.floatToInt (pt.x) + " " + Clazz.floatToInt (pt.y) + (pt.z < 0 ? " %]" : "]");
} else if (adjustPt && i == 1) {
var pt1 = J.util.P3.newP (pt);
pt1.sub (mesh.vertices[mesh.polygonIndexes[iModel][0]]);
str += " " + J.util.Escape.eP (pt1);
} else {
str += " " + J.util.Escape.eP (pt);
}}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("Unexpected error in Draw.getVertexList");
} else {
throw e;
}
}
return str;
}, $fz.isPrivate = true, $fz), "J.shapespecial.DrawMesh,~N,~N");
Clazz.overrideMethod (c$, "getShapeDetail", 
function () {
var V =  new J.util.JmolList ();
for (var i = 0; i < this.meshCount; i++) {
var mesh = this.dmeshes[i];
if (mesh.vertexCount == 0) continue;
var info =  new java.util.Hashtable ();
info.put ("fixed", mesh.ptCenters == null ? Boolean.TRUE : Boolean.FALSE);
info.put ("ID", (mesh.thisID == null ? "<noid>" : mesh.thisID));
info.put ("drawType", mesh.drawType.$$name);
if (mesh.diameter > 0) info.put ("diameter", Integer.$valueOf (mesh.diameter));
if (mesh.width != 0) info.put ("width", Float.$valueOf (mesh.width));
info.put ("scale", Float.$valueOf (mesh.scale));
if (mesh.drawType === J.shapespecial.Draw.EnumDrawType.MULTIPLE) {
var m =  new J.util.JmolList ();
var modelCount = this.viewer.getModelCount ();
for (var k = 0; k < modelCount; k++) {
if (mesh.ptCenters[k] == null) continue;
var mInfo =  new java.util.Hashtable ();
mInfo.put ("modelIndex", Integer.$valueOf (k));
mInfo.put ("command", this.getCommand2 (mesh, k));
mInfo.put ("center", mesh.ptCenters[k]);
var nPoints = mesh.drawVertexCounts[k];
mInfo.put ("vertexCount", Integer.$valueOf (nPoints));
if (nPoints > 1) mInfo.put ("axis", mesh.axes[k]);
var v =  new J.util.JmolList ();
for (var ipt = 0; ipt < nPoints; ipt++) v.addLast (mesh.vertices[mesh.polygonIndexes[k][ipt]]);

mInfo.put ("vertices", v);
if (mesh.drawTypes[k] === J.shapespecial.Draw.EnumDrawType.LINE) {
var d = mesh.vertices[mesh.polygonIndexes[k][0]].distance (mesh.vertices[mesh.polygonIndexes[k][1]]);
mInfo.put ("length_Ang", Float.$valueOf (d));
}m.addLast (mInfo);
}
info.put ("models", m);
} else {
info.put ("command", this.getCommand (mesh));
info.put ("center", mesh.ptCenter);
if (mesh.drawVertexCount > 1) info.put ("axis", mesh.axis);
var v =  new J.util.JmolList ();
for (var j = 0; j < mesh.vertexCount; j++) v.addLast (mesh.vertices[j]);

info.put ("vertices", v);
if (mesh.drawType === J.shapespecial.Draw.EnumDrawType.LINE) info.put ("length_Ang", Float.$valueOf (mesh.vertices[0].distance (mesh.vertices[1])));
}V.addLast (info);
}
return V;
});
Clazz.overrideMethod (c$, "getShapeState", 
function () {
var s =  new J.util.SB ();
s.append ("\n");
J.shape.Shape.appendCmd (s, this.myType + " delete");
for (var i = 0; i < this.meshCount; i++) {
var mesh = this.dmeshes[i];
if (mesh.vertexCount == 0 && mesh.lineData == null) continue;
s.append (this.getCommand2 (mesh, mesh.modelIndex));
if (!mesh.visible) s.append (" " + this.myType + " ID " + J.util.Escape.eS (mesh.thisID) + " off;\n");
}
return s.toString ();
});
c$.randomPoint = $_M(c$, "randomPoint", 
function () {
return J.util.P3.new3 (Math.random (), Math.random (), Math.random ());
});
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
this.id = 0;
this.$$name = null;
Clazz.instantialize (this, arguments);
}, J.shapespecial.Draw, "EnumDrawType", Enum);
Clazz.makeConstructor (c$, 
function (a, b) {
this.id = a;
this.$$name = b;
}, "~N,~S");
c$.getType = $_M(c$, "getType", 
function (a) {
switch (a) {
case 1:
return J.shapespecial.Draw.EnumDrawType.POINT;
case 2:
return J.shapespecial.Draw.EnumDrawType.LINE;
case 4:
return J.shapespecial.Draw.EnumDrawType.PLANE;
default:
return J.shapespecial.Draw.EnumDrawType.NONE;
}
}, "~N");
Clazz.defineEnumConstant (c$, "MULTIPLE", 0, [-1, "multiple"]);
Clazz.defineEnumConstant (c$, "NONE", 1, [0, "none"]);
Clazz.defineEnumConstant (c$, "POINT", 2, [1, "point"]);
Clazz.defineEnumConstant (c$, "LINE", 3, [2, "line"]);
Clazz.defineEnumConstant (c$, "PLANE", 4, [4, "plane"]);
Clazz.defineEnumConstant (c$, "CYLINDER", 5, [14, "cylinder"]);
Clazz.defineEnumConstant (c$, "ARROW", 6, [15, "arrow"]);
Clazz.defineEnumConstant (c$, "CIRCLE", 7, [16, "circle"]);
Clazz.defineEnumConstant (c$, "CURVE", 8, [17, "curve"]);
Clazz.defineEnumConstant (c$, "CIRCULARPLANE", 9, [18, "circularPlane"]);
Clazz.defineEnumConstant (c$, "ARC", 10, [19, "arc"]);
Clazz.defineEnumConstant (c$, "LINE_SEGMENT", 11, [20, "lineSegment"]);
Clazz.defineEnumConstant (c$, "POLYGON", 12, [21, "polygon"]);
c$ = Clazz.p0p ();
Clazz.defineStatics (c$,
"PT_COORD", 1,
"PT_IDENTIFIER", 2,
"PT_BITSET", 3,
"PT_MODEL_INDEX", 4,
"PT_MODEL_BASED_POINTS", 5,
"MAX_OBJECT_CLICK_DISTANCE_SQUARED", 100);
});
