Clazz.declarePackage ("J.shapesurface");
Clazz.load (["J.jvxl.api.MeshDataServer", "J.shape.MeshCollection", "J.util.P3i", "$.P4"], "J.shapesurface.Isosurface", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "J.io.JmolBinary", "J.jvxl.data.JvxlCoder", "$.JvxlData", "$.MeshData", "J.jvxl.readers.SurfaceGenerator", "J.shape.Mesh", "J.shapesurface.IsosurfaceMesh", "J.util.ArrayUtil", "$.AxisAngle4f", "$.BS", "$.C", "$.ColorUtil", "$.Escape", "$.JmolList", "$.Logger", "$.Matrix3f", "$.MeshSurface", "$.P3", "$.Parser", "$.Quaternion", "$.SB", "$.TextFormat", "$.V3", "J.viewer.JC", "$.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.isomeshes = null;
this.thisMesh = null;
this.actualID = null;
this.iHaveBitSets = false;
this.explicitContours = false;
this.atomIndex = 0;
this.moNumber = 0;
this.moLinearCombination = null;
this.defaultColix = 0;
this.meshColix = 0;
this.center = null;
this.scale3d = 0;
this.isPhaseColored = false;
this.isColorExplicit = false;
this.scriptAppendix = "";
this.sg = null;
this.jvxlData = null;
this.withinDistance2 = 0;
this.isWithinNot = false;
this.withinPoints = null;
this.cutoffRange = null;
this.allowMesh = true;
this.script = null;
this.iHaveModelIndex = false;
this.nLCAO = 0;
this.lcaoDir = null;
this.privateKey = 0;
this.associateNormals = false;
this.ptXY = null;
this.keyXy = null;
Clazz.instantialize (this, arguments);
}, J.shapesurface, "Isosurface", J.shape.MeshCollection, J.jvxl.api.MeshDataServer);
Clazz.prepareFields (c$, function () {
this.isomeshes =  new Array (4);
this.lcaoDir =  new J.util.P4 ();
this.ptXY =  new J.util.P3i ();
});
Clazz.overrideMethod (c$, "allocMesh", 
function (thisID, m) {
var index = this.meshCount++;
this.meshes = this.isomeshes = J.util.ArrayUtil.ensureLength (this.isomeshes, this.meshCount * 2);
this.currentMesh = this.thisMesh = this.isomeshes[index] = (m == null ?  new J.shapesurface.IsosurfaceMesh (thisID, this.colix, index) : m);
this.currentMesh.index = index;
if (this.sg != null) this.sg.setJvxlData (this.jvxlData = this.thisMesh.jvxlData);
}, "~S,J.shape.Mesh");
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shapesurface.Isosurface, "initShape", []);
this.myType = "isosurface";
this.newSg ();
});
$_M(c$, "newSg", 
function () {
this.sg =  new J.jvxl.readers.SurfaceGenerator (this.viewer, this, null, this.jvxlData =  new J.jvxl.data.JvxlData ());
this.sg.getParams ().showTiming = this.viewer.getBoolean (603979934);
this.sg.setVersion ("Jmol " + J.viewer.Viewer.getJmolVersion ());
});
$_M(c$, "clearSg", 
function () {
this.sg = null;
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
this.setPropI (propertyName, value, bs);
}, "~S,~O,J.util.BS");
$_M(c$, "setPropI", 
function (propertyName, value, bs) {
if ("cache" === propertyName) {
if (this.currentMesh == null) return;
var id = this.currentMesh.thisID;
var imodel = this.currentMesh.modelIndex;
this.viewer.cachePut ("cache://isosurface_" + id, this.getPropI ("jvxlDataXml"));
this.deleteMeshI (this.currentMesh.index);
this.setPropI ("init", null, null);
this.setPropI ("thisID", id, null);
this.setPropI ("modelIndex", Integer.$valueOf (imodel), null);
this.setPropI ("fileName", "cache://isosurface_" + id, null);
this.setPropI ("readFile", null, null);
this.setPropI ("finalize", "isosurface ID " + J.util.Escape.eS (id) + (imodel >= 0 ? " modelIndex " + imodel : "") + " /*file*/" + J.util.Escape.eS ("cache://isosurface_" + id), null);
this.setPropI ("clear", null, null);
return;
}if ("delete" === propertyName) {
this.setPropertySuper (propertyName, value, bs);
if (!this.explicitID) this.nLCAO = this.nUnnamed = 0;
this.currentMesh = this.thisMesh = null;
return;
}if ("remapInherited" === propertyName) {
for (var i = this.meshCount; --i >= 0; ) {
if (this.isomeshes[i] != null && "#inherit;".equals (this.isomeshes[i].colorCommand)) this.isomeshes[i].remapColors (this.viewer, null, NaN);
}
return;
}if ("remapColor" === propertyName) {
if (this.thisMesh != null) this.thisMesh.remapColors (this.viewer, value, this.translucentLevel);
return;
}if ("thisID" === propertyName) {
if (this.actualID != null) value = this.actualID;
this.setPropertySuper ("thisID", value, null);
return;
}if ("params" === propertyName) {
if (this.thisMesh != null) {
this.ensureMeshSource ();
this.thisMesh.checkAllocColixes ();
var data = value;
var colixes = data[0];
var atomMap = null;
if (colixes != null) {
for (var i = 0; i < colixes.length; i++) {
var colix = colixes[i];
var f = 0;
if (f > 0.01) colix = J.util.C.getColixTranslucent3 (colix, true, f);
colixes[i] = colix;
}
atomMap =  Clazz.newIntArray (bs.length (), 0);
for (var pt = 0, i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1), pt++) atomMap[i] = pt;

}this.thisMesh.setVertexColixesForAtoms (this.viewer, colixes, atomMap, bs);
this.thisMesh.setVertexColorMap ();
}return;
}if ("atomcolor" === propertyName) {
if (this.thisMesh != null) {
this.ensureMeshSource ();
this.thisMesh.colorVertices (J.util.C.getColixO (value), bs, true);
}return;
}if ("pointSize" === propertyName) {
if (this.thisMesh != null) {
this.thisMesh.volumeRenderPointSize = (value).floatValue ();
}return;
}if ("vertexcolor" === propertyName) {
if (this.thisMesh != null) {
this.thisMesh.colorVertices (J.util.C.getColixO (value), bs, false);
}return;
}if ("colorPhase" === propertyName) {
var colors = value;
if (this.thisMesh != null) {
this.thisMesh.colorPhased = true;
this.thisMesh.colix = this.thisMesh.jvxlData.minColorIndex = J.util.C.getColix ((colors[0]).intValue ());
this.thisMesh.jvxlData.maxColorIndex = J.util.C.getColix ((colors[1]).intValue ());
this.thisMesh.jvxlData.isBicolorMap = true;
this.thisMesh.jvxlData.colorDensity = false;
this.thisMesh.isColorSolid = false;
this.thisMesh.remapColors (this.viewer, null, this.translucentLevel);
}return;
}if ("color" === propertyName) {
var color = J.util.C.getHexCode (J.util.C.getColixO (value));
if (this.thisMesh != null) {
this.thisMesh.jvxlData.baseColor = color;
this.thisMesh.isColorSolid = true;
this.thisMesh.polygonColixes = null;
this.thisMesh.colorEncoder = null;
this.thisMesh.vertexColorMap = null;
} else if (!J.util.TextFormat.isWild (this.previousMeshID)) {
for (var i = this.meshCount; --i >= 0; ) {
this.isomeshes[i].jvxlData.baseColor = color;
this.isomeshes[i].isColorSolid = true;
this.isomeshes[i].polygonColixes = null;
this.isomeshes[i].colorEncoder = null;
this.isomeshes[i].vertexColorMap = null;
}
}this.setPropertySuper (propertyName, value, bs);
return;
}if ("nocontour" === propertyName) {
if (this.thisMesh != null) {
this.thisMesh.deleteContours ();
}return;
}if ("fixed" === propertyName) {
this.isFixed = (value).booleanValue ();
this.setMeshI ();
return;
}if ("newObject" === propertyName) {
if (this.thisMesh != null) this.thisMesh.clearType (this.thisMesh.meshType, false);
return;
}if ("moveIsosurface" === propertyName) {
if (this.thisMesh != null) {
this.thisMesh.updateCoordinates (value, null);
this.thisMesh.altVertices = null;
}return;
}if ("refreshTrajectories" === propertyName) {
for (var i = this.meshCount; --i >= 0; ) if (this.meshes[i].connections != null && this.meshes[i].modelIndex == ((value)[0]).intValue ()) (this.meshes[i]).updateCoordinates ((value)[2], (value)[1]);

return;
}if ("modelIndex" === propertyName) {
if (!this.iHaveModelIndex) {
this.modelIndex = (value).intValue ();
this.isFixed = (this.modelIndex < 0);
this.sg.setModelIndex (Math.abs (this.modelIndex));
}return;
}if ("lcaoCartoon" === propertyName || "lonePair" === propertyName || "radical" === propertyName) {
var info = value;
if (!this.explicitID) {
this.setPropertySuper ("thisID", null, null);
}if (!this.sg.setParameter ("lcaoCartoonCenter", info[2])) this.drawLcaoCartoon (info[0], info[1], info[3], ("lonePair" === propertyName ? 2 : "radical" === propertyName ? 1 : 0));
return;
}if ("select" === propertyName) {
if (this.iHaveBitSets) return;
}if ("ignore" === propertyName) {
if (this.iHaveBitSets) return;
}if ("meshcolor" === propertyName) {
var rgb = (value).intValue ();
this.meshColix = J.util.C.getColix (rgb);
if (this.thisMesh != null) this.thisMesh.meshColix = this.meshColix;
return;
}if ("offset" === propertyName) {
var offset = J.util.P3.newP (value);
if (offset.equals (J.viewer.JC.center)) offset = null;
if (this.thisMesh != null) {
this.thisMesh.rotateTranslate (null, offset, true);
this.thisMesh.altVertices = null;
}return;
}if ("rotate" === propertyName) {
var pt4 = value;
if (this.thisMesh != null) {
this.thisMesh.rotateTranslate (J.util.Quaternion.newP4 (pt4), null, true);
this.thisMesh.altVertices = null;
}return;
}if ("bsDisplay" === propertyName) {
this.bsDisplay = value;
return;
}if ("displayWithin" === propertyName) {
var o = value;
this.displayWithinDistance2 = (o[0]).floatValue ();
this.isDisplayWithinNot = (this.displayWithinDistance2 < 0);
this.displayWithinDistance2 *= this.displayWithinDistance2;
this.displayWithinPoints = o[3];
if (this.displayWithinPoints.size () == 0) this.displayWithinPoints = this.viewer.getAtomPointVector (o[2]);
return;
}if ("finalize" === propertyName) {
if (this.thisMesh != null) {
var cmd = value;
if (cmd != null && !cmd.startsWith ("; isosurface map")) {
this.thisMesh.setDiscreteColixes (this.sg.getParams ().contoursDiscrete, this.sg.getParams ().contourColixes);
this.setJvxlInfo ();
}this.setScriptInfo (cmd);
}this.clearSg ();
return;
}if ("privateKey" === propertyName) {
this.privateKey = (value).doubleValue ();
return;
}if ("connections" === propertyName) {
if (this.currentMesh != null) {
this.connections = value;
if (this.connections[0] >= 0 && this.connections[0] < this.viewer.getAtomCount ()) this.currentMesh.connections = this.connections;
 else this.connections = this.currentMesh.connections = null;
}return;
}if ("cutoffRange" === propertyName) {
this.cutoffRange = value;
return;
}if ("slab" === propertyName) {
if (Clazz.instanceOf (value, Integer)) {
if (this.thisMesh != null) this.thisMesh.jvxlData.slabValue = (value).intValue ();
return;
}if (this.thisMesh != null) {
var slabInfo = value;
var tok = (slabInfo[0]).intValue ();
switch (tok) {
case 1073742018:
var data = slabInfo[1];
var m = this.getMesh (data[1]);
if (m == null) return;
data[1] = m;
break;
}
this.slabPolygons (slabInfo);
return;
}}if ("cap" === propertyName) {
if (this.thisMesh != null && this.thisMesh.polygonCount != 0) {
this.thisMesh.slabPolygons (value, true);
this.thisMesh.initialize (this.thisMesh.lighting, null, null);
return;
}}if ("map" === propertyName) {
if (this.sg != null) this.sg.getParams ().isMapped = true;
this.setProperty ("squareData", Boolean.FALSE, null);
if (this.thisMesh == null || this.thisMesh.vertexCount == 0) return;
}if ("deleteVdw" === propertyName) {
for (var i = this.meshCount; --i >= 0; ) if (this.isomeshes[i].bsVdw != null && (bs == null || bs.intersects (this.isomeshes[i].bsVdw))) this.deleteMeshI (i);

this.currentMesh = this.thisMesh = null;
return;
}if ("mapColor" === propertyName || "readFile" === propertyName) {
if (value == null) {
value = this.viewer.getBufferedReaderOrErrorMessageFromName (this.sg.getFileName (), null, true);
if (Clazz.instanceOf (value, String)) {
J.util.Logger.error ("Isosurface: could not open file " + this.sg.getFileName () + " -- " + value);
return;
}if (!(Clazz.instanceOf (value, java.io.BufferedReader))) try {
value = J.io.JmolBinary.getBufferedReader (value, "ISO-8859-1");
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
}} else if ("atomIndex" === propertyName) {
this.atomIndex = (value).intValue ();
if (this.thisMesh != null) this.thisMesh.atomIndex = this.atomIndex;
} else if ("center" === propertyName) {
this.center.setT (value);
} else if ("colorRGB" === propertyName) {
var rgb = (value).intValue ();
this.defaultColix = J.util.C.getColix (rgb);
} else if ("contour" === propertyName) {
this.explicitContours = true;
} else if ("functionXY" === propertyName) {
if (this.sg.isStateDataRead ()) this.setScriptInfo (null);
} else if ("init" === propertyName) {
this.newSg ();
} else if ("getSurfaceSets" === propertyName) {
if (this.thisMesh != null) {
this.thisMesh.jvxlData.thisSet = (value).intValue ();
this.thisMesh.calculatedVolume = null;
this.thisMesh.calculatedArea = null;
}} else if ("localName" === propertyName) {
value = this.viewer.getOutputStream (value, null);
propertyName = "outputStream";
} else if ("molecularOrbital" === propertyName) {
if (Clazz.instanceOf (value, Integer)) {
this.moNumber = (value).intValue ();
this.moLinearCombination = null;
} else {
this.moLinearCombination = value;
this.moNumber = 0;
}if (!this.isColorExplicit) this.isPhaseColored = true;
} else if ("phase" === propertyName) {
this.isPhaseColored = true;
} else if ("plane" === propertyName) {
} else if ("pocket" === propertyName) {
} else if ("scale3d" === propertyName) {
this.scale3d = (value).floatValue ();
if (this.thisMesh != null) {
this.thisMesh.scale3d = this.thisMesh.jvxlData.scale3d = this.scale3d;
this.thisMesh.altVertices = null;
}} else if ("title" === propertyName) {
if (Clazz.instanceOf (value, String) && "-".equals (value)) value = null;
this.setPropertySuper (propertyName, value, bs);
value = this.title;
} else if ("withinPoints" === propertyName) {
var o = value;
this.withinDistance2 = (o[0]).floatValue ();
this.isWithinNot = (this.withinDistance2 < 0);
this.withinDistance2 *= this.withinDistance2;
this.withinPoints = o[3];
if (this.withinPoints.size () == 0) this.withinPoints = this.viewer.getAtomPointVector (o[2]);
} else if (("nci" === propertyName || "orbital" === propertyName) && this.sg != null) {
this.sg.getParams ().testFlags = (this.viewer.getTestFlag (2) ? 2 : 0);
}if (this.sg != null && this.sg.setProp (propertyName, value, bs)) {
if (this.sg.isValid ()) return;
propertyName = "delete";
}if ("init" === propertyName) {
this.explicitID = false;
this.scriptAppendix = "";
var script = (Clazz.instanceOf (value, String) ? value : null);
var pt = (script == null ? -1 : script.indexOf ("# ID="));
this.actualID = (pt >= 0 ? J.util.Parser.getQuotedStringAt (script, pt) : null);
this.setPropertySuper ("thisID", "+PREVIOUS_MESH+", null);
if (script != null && !(this.iHaveBitSets = this.getScriptBitSets (script, null))) this.sg.setParameter ("select", bs);
this.initializeIsosurface ();
this.sg.setModelIndex (this.isFixed ? -1 : this.modelIndex);
return;
}if ("clear" === propertyName) {
this.discardTempData (true);
return;
}if ("colorDensity" === propertyName) {
if (value != null && this.currentMesh != null) this.currentMesh.volumeRenderPointSize = (value).floatValue ();
return;
}if (propertyName === "deleteModelAtoms") {
var modelIndex = ((value)[2])[0];
var firstAtomDeleted = ((value)[2])[1];
var nAtomsDeleted = ((value)[2])[2];
for (var i = this.meshCount; --i >= 0; ) {
var m = this.meshes[i];
if (m == null) continue;
if (m.connections != null) {
var iAtom = m.connections[0];
if (iAtom >= firstAtomDeleted + nAtomsDeleted) m.connections[0] = iAtom - nAtomsDeleted;
 else if (iAtom >= firstAtomDeleted) m.connections = null;
}m.connections = null;
if (m.modelIndex == modelIndex) {
this.meshCount--;
if (m === this.currentMesh) this.currentMesh = this.thisMesh = null;
this.meshes = this.isomeshes = J.util.ArrayUtil.deleteElements (this.meshes, i, 1);
} else if (m.modelIndex > modelIndex) {
m.modelIndex--;
if (m.atomIndex >= firstAtomDeleted) m.atomIndex -= nAtomsDeleted;
}}
return;
}this.setPropertySuper (propertyName, value, bs);
}, "~S,~O,J.util.BS");
$_M(c$, "ensureMeshSource", 
($fz = function () {
var haveColors = (this.thisMesh.vertexSource != null);
if (haveColors) for (var i = this.thisMesh.vertexCount; --i >= 0; ) if (this.thisMesh.vertexSource[i] < 0) {
haveColors = false;
break;
}
if (!haveColors) {
var source = this.thisMesh.vertexSource;
var vertexColixes = this.thisMesh.vertexColixes;
var colix = (this.thisMesh.isColorSolid ? this.thisMesh.colix : 0);
this.setProperty ("init", null, null);
this.setProperty ("map", Boolean.FALSE, null);
this.setProperty ("property",  Clazz.newFloatArray (this.viewer.getAtomCount (), 0), null);
if (colix != 0) {
this.thisMesh.colorCommand = "color isosurface " + J.util.C.getHexCode (colix);
this.setProperty ("color", Integer.$valueOf (J.util.C.getArgb (colix)), null);
}if (source != null) {
for (var i = this.thisMesh.vertexCount; --i >= 0; ) if (source[i] < 0) source[i] = this.thisMesh.vertexSource[i];

this.thisMesh.vertexSource = source;
this.thisMesh.vertexColixes = vertexColixes;
}}}, $fz.isPrivate = true, $fz));
$_M(c$, "slabPolygons", 
function (slabInfo) {
this.thisMesh.slabPolygons (slabInfo, false);
this.thisMesh.reinitializeLightingAndColor (this.viewer);
}, "~A");
$_M(c$, "setPropertySuper", 
($fz = function (propertyName, value, bs) {
if (propertyName === "thisID" && this.currentMesh != null && this.currentMesh.thisID != null && this.currentMesh.thisID.equals (value)) {
this.checkExplicit (value);
return;
}this.currentMesh = this.thisMesh;
this.setPropMC (propertyName, value, bs);
this.thisMesh = this.currentMesh;
this.jvxlData = (this.thisMesh == null ? null : this.thisMesh.jvxlData);
if (this.sg != null) this.sg.setJvxlData (this.jvxlData);
}, $fz.isPrivate = true, $fz), "~S,~O,J.util.BS");
Clazz.overrideMethod (c$, "getPropertyData", 
function (property, data) {
if (property === "colorEncoder") {
var mesh = this.getMesh (data[0]);
if (mesh == null || (data[1] = mesh.colorEncoder) == null) return false;
return true;
}if (property === "intersectPlane") {
var mesh = this.getMesh (data[0]);
if (mesh == null) return false;
data[3] = Integer.$valueOf (mesh.modelIndex);
mesh.getIntersection (0, data[1], null, data[2], null, null, null, false, false, 135266319, false);
return true;
}if (property === "getBoundingBox") {
var id = data[0];
var m = this.getMesh (id);
if (m == null || m.vertices == null) return false;
data[2] = m.jvxlData.boundingBox;
if (m.mat4 != null) {
var d =  new Array (2);
d[0] = J.util.P3.newP (m.jvxlData.boundingBox[0]);
d[1] = J.util.P3.newP (m.jvxlData.boundingBox[1]);
var v =  new J.util.V3 ();
m.mat4.get (v);
d[0].add (v);
d[1].add (v);
data[2] = d;
}return true;
}if (property === "unitCell") {
var m = this.getMesh (data[0]);
return (m != null && (data[1] = m.getUnitCell ()) != null);
}if (property === "getCenter") {
var index = (data[1]).intValue ();
if (index == -2147483648) {
var id = data[0];
var m = this.getMesh (id);
if (m == null || m.vertices == null) return false;
var p = J.util.P3.newP (m.jvxlData.boundingBox[0]);
p.add (m.jvxlData.boundingBox[1]);
p.scale (0.5);
if (m.mat4 != null) {
var v =  new J.util.V3 ();
m.mat4.get (v);
p.add (v);
}data[2] = p;
return true;
}}return this.getPropDataMC (property, data);
}, "~S,~A");
Clazz.overrideMethod (c$, "getProperty", 
function (property, index) {
return this.getPropI (property);
}, "~S,~N");
$_M(c$, "getPropI", 
function (property) {
var ret = this.getPropMC (property);
if (ret != null) return ret;
if (property === "dataRange") return (this.thisMesh == null || this.jvxlData.jvxlPlane != null && this.thisMesh.colorEncoder == null ? null : [this.jvxlData.mappedDataMin, this.jvxlData.mappedDataMax, (this.jvxlData.isColorReversed ? this.jvxlData.valueMappedToBlue : this.jvxlData.valueMappedToRed), (this.jvxlData.isColorReversed ? this.jvxlData.valueMappedToRed : this.jvxlData.valueMappedToBlue)]);
if (property === "moNumber") return Integer.$valueOf (this.moNumber);
if (property === "moLinearCombination") return this.moLinearCombination;
if (property === "nSets") return Integer.$valueOf (this.thisMesh == null ? 0 : this.thisMesh.nSets);
if (property === "area") return (this.thisMesh == null ? Float.$valueOf (NaN) : this.calculateVolumeOrArea (true));
if (property === "volume") return (this.thisMesh == null ? Float.$valueOf (NaN) : this.calculateVolumeOrArea (false));
if (this.thisMesh == null) return null;
if (property === "cutoff") return Float.$valueOf (this.jvxlData.cutoff);
if (property === "minMaxInfo") return [this.jvxlData.dataMin, this.jvxlData.dataMax];
if (property === "plane") return this.jvxlData.jvxlPlane;
if (property === "contours") return this.thisMesh.getContours ();
if (property === "jvxlDataXml" || property === "jvxlMeshXml") {
var meshData = null;
this.jvxlData.slabInfo = null;
if (property === "jvxlMeshXml" || this.jvxlData.vertexDataOnly || this.thisMesh.bsSlabDisplay != null && this.thisMesh.bsSlabGhost == null) {
meshData =  new J.jvxl.data.MeshData ();
this.fillMeshData (meshData, 1, null);
meshData.polygonColorData = J.shapesurface.Isosurface.getPolygonColorData (meshData.polygonCount, meshData.polygonColixes, meshData.bsSlabDisplay);
} else if (this.thisMesh.bsSlabGhost != null) {
this.jvxlData.slabInfo = this.thisMesh.slabOptions.toString ();
}var sb =  new J.util.SB ();
this.getMeshCommand (sb, this.thisMesh.index);
this.thisMesh.setJvxlColorMap (true);
return J.jvxl.data.JvxlCoder.jvxlGetFile (this.jvxlData, meshData, this.title, "", true, 1, sb.toString (), null);
}if (property === "jvxlFileInfo") {
this.thisMesh.setJvxlColorMap (false);
return J.jvxl.data.JvxlCoder.jvxlGetInfo (this.jvxlData);
}if (property === "command") {
var key = this.previousMeshID.toUpperCase ();
var isWild = J.util.TextFormat.isWild (key);
var sb =  new J.util.SB ();
for (var i = this.meshCount; --i >= 0; ) {
var id = this.meshes[i].thisID.toUpperCase ();
if (id.equals (key) || isWild && J.util.TextFormat.isMatch (id, key, true, true)) this.getMeshCommand (sb, i);
}
return sb.toString ();
}return null;
}, "~S");
$_M(c$, "calculateVolumeOrArea", 
($fz = function (isArea) {
if (isArea) {
if (this.thisMesh.calculatedArea != null) return this.thisMesh.calculatedArea;
} else {
if (this.thisMesh.calculatedVolume != null) return this.thisMesh.calculatedVolume;
}var meshData =  new J.jvxl.data.MeshData ();
this.fillMeshData (meshData, 1, null);
meshData.nSets = this.thisMesh.nSets;
meshData.vertexSets = this.thisMesh.vertexSets;
if (!isArea && this.thisMesh.jvxlData.colorDensity) {
var f = this.thisMesh.jvxlData.voxelVolume;
f *= (this.thisMesh.bsSlabDisplay == null ? this.thisMesh.vertexCount : this.thisMesh.bsSlabDisplay.cardinality ());
return this.thisMesh.calculatedVolume = Float.$valueOf (f);
}var ret = meshData.calculateVolumeOrArea (this.thisMesh.jvxlData.thisSet, isArea, false);
if (isArea) this.thisMesh.calculatedArea = ret;
 else this.thisMesh.calculatedVolume = ret;
return ret;
}, $fz.isPrivate = true, $fz), "~B");
c$.getPolygonColorData = $_M(c$, "getPolygonColorData", 
function (ccount, colixes, bsSlabDisplay) {
if (colixes == null) return null;
var list1 =  new J.util.SB ();
var count = 0;
var colix = 0;
var done = false;
for (var i = 0; i < ccount || (done = true) == true; i++) {
if (!done && bsSlabDisplay != null && !bsSlabDisplay.get (i)) continue;
if (done || colixes[i] != colix) {
if (count != 0) list1.append (" ").appendI (count).append (" ").appendI ((colix == 0 ? 0 : J.util.C.getArgb (colix)));
if (done) break;
colix = colixes[i];
count = 1;
} else {
count++;
}}
list1.append ("\n");
return list1.toString ();
}, "~N,~A,J.util.BS");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
this.clean ();
var sb =  new J.util.SB ();
sb.append ("\n");
for (var i = 0; i < this.meshCount; i++) this.getMeshCommand (sb, i);

return sb.toString ();
});
$_M(c$, "getMeshCommand", 
($fz = function (sb, i) {
var imesh = this.meshes[i];
if (imesh == null || imesh.scriptCommand == null) return;
var cmd = imesh.scriptCommand;
var modelCount = this.viewer.getModelCount ();
if (modelCount > 1) J.shape.Shape.appendCmd (sb, "frame " + this.viewer.getModelNumberDotted (imesh.modelIndex));
cmd = J.util.TextFormat.simpleReplace (cmd, ";; isosurface map", " map");
cmd = J.util.TextFormat.simpleReplace (cmd, "; isosurface map", " map");
cmd = cmd.$replace ('\t', ' ');
cmd = J.util.TextFormat.simpleReplace (cmd, ";#", "; #");
var pt = cmd.indexOf ("; #");
if (pt >= 0) cmd = cmd.substring (0, pt);
if (imesh.connections != null) cmd += " connect " + J.util.Escape.eAI (imesh.connections);
cmd = J.util.TextFormat.trim (cmd, ";");
if (imesh.linkedMesh != null) cmd += " LINK";
if (this.myType === "lcaoCartoon" && imesh.atomIndex >= 0) cmd += " ATOMINDEX " + imesh.atomIndex;
J.shape.Shape.appendCmd (sb, cmd);
var id = this.myType + " ID " + J.util.Escape.eS (imesh.thisID);
if (imesh.jvxlData.thisSet >= 0) J.shape.Shape.appendCmd (sb, id + " set " + (imesh.jvxlData.thisSet + 1));
if (imesh.mat4 != null) J.shape.Shape.appendCmd (sb, id + " move " + J.util.Escape.matrixToScript (imesh.mat4));
if (imesh.scale3d != 0) J.shape.Shape.appendCmd (sb, id + " scale3d " + imesh.scale3d);
if (imesh.jvxlData.slabValue != -2147483648) J.shape.Shape.appendCmd (sb, id + " slab " + imesh.jvxlData.slabValue);
if (imesh.slabOptions != null) J.shape.Shape.appendCmd (sb, imesh.slabOptions.toString ());
if (cmd.charAt (0) != '#') {
if (this.allowMesh) J.shape.Shape.appendCmd (sb, imesh.getState (this.myType));
if (!imesh.isColorSolid && J.util.C.isColixTranslucent (imesh.colix)) J.shape.Shape.appendCmd (sb, "color " + this.myType + " " + J.shape.Shape.getTranslucentLabel (imesh.colix));
if (imesh.colorCommand != null && !imesh.colorCommand.equals ("#inherit;")) {
J.shape.Shape.appendCmd (sb, imesh.colorCommand);
}var colorArrayed = (imesh.isColorSolid && imesh.polygonColixes != null);
if (imesh.isColorSolid && !colorArrayed) {
J.shape.Shape.appendCmd (sb, J.shape.Shape.getColorCommandUnk (this.myType, imesh.colix, this.translucentAllowed));
} else if (imesh.jvxlData.isBicolorMap && imesh.colorPhased) {
J.shape.Shape.appendCmd (sb, "color isosurface phase " + J.shape.Shape.encodeColor (imesh.jvxlData.minColorIndex) + " " + J.shape.Shape.encodeColor (imesh.jvxlData.maxColorIndex));
}if (imesh.vertexColorMap != null) for (var entry, $entry = imesh.vertexColorMap.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var bs = entry.getValue ();
if (!bs.isEmpty ()) J.shape.Shape.appendCmd (sb, "color " + this.myType + " " + J.util.Escape.eBS (bs) + " " + entry.getKey ());
}
}}, $fz.isPrivate = true, $fz), "J.util.SB,~N");
$_M(c$, "getScriptBitSets", 
($fz = function (script, bsCmd) {
this.script = script;
var i;
this.iHaveModelIndex = false;
this.modelIndex = -1;
if (script != null && (i = script.indexOf ("MODEL({")) >= 0) {
var j = script.indexOf ("})", i);
if (j > 0) {
var bs = J.util.Escape.uB (script.substring (i + 3, j + 1));
this.modelIndex = (bs == null ? -1 : bs.nextSetBit (0));
this.iHaveModelIndex = (this.modelIndex >= 0);
}}if (script == null) return false;
this.getCapSlabInfo (script);
i = script.indexOf ("# ({");
if (i < 0) return false;
var j = script.indexOf ("})", i);
if (j < 0) return false;
var bs = J.util.Escape.uB (script.substring (i + 2, j + 2));
if (bsCmd == null) this.sg.setParameter ("select", bs);
 else bsCmd[0] = bs;
if ((i = script.indexOf ("({", j)) < 0) return true;
j = script.indexOf ("})", i);
if (j < 0) return false;
bs = J.util.Escape.uB (script.substring (i + 1, j + 1));
if (bsCmd == null) this.sg.setParameter ("ignore", bs);
 else bsCmd[1] = bs;
if ((i = script.indexOf ("/({", j)) == j + 2) {
if ((j = script.indexOf ("})", i)) < 0) return false;
bs = J.util.Escape.uB (script.substring (i + 3, j + 1));
if (bsCmd == null) this.viewer.setTrajectoryBs (bs);
 else bsCmd[2] = bs;
}return true;
}, $fz.isPrivate = true, $fz), "~S,~A");
$_M(c$, "getCapSlabInfo", 
function (script) {
var i = script.indexOf ("# SLAB=");
if (i >= 0) this.sg.setParameter ("slab", J.util.MeshSurface.getCapSlabObject (J.util.Parser.getQuotedStringAt (script, i), false));
i = script.indexOf ("# CAP=");
if (i >= 0) this.sg.setParameter ("slab", J.util.MeshSurface.getCapSlabObject (J.util.Parser.getQuotedStringAt (script, i), true));
}, "~S");
$_M(c$, "initializeIsosurface", 
($fz = function () {
if (!this.iHaveModelIndex) this.modelIndex = this.viewer.getCurrentModelIndex ();
this.atomIndex = -1;
this.bsDisplay = null;
this.center = J.util.P3.new3 (3.4028235E38, 3.4028235E38, 3.4028235E38);
this.colix = 5;
this.connections = null;
this.cutoffRange = null;
this.defaultColix = this.meshColix = 0;
this.displayWithinPoints = null;
this.explicitContours = false;
this.isFixed = (this.modelIndex < 0);
this.isPhaseColored = this.isColorExplicit = false;
this.linkedMesh = null;
if (this.modelIndex < 0) this.modelIndex = 0;
this.scale3d = 0;
this.title = null;
this.translucentLevel = 0;
this.withinPoints = null;
this.initState ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "initState", 
($fz = function () {
this.associateNormals = true;
this.sg.initState ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "setMeshI", 
($fz = function () {
this.thisMesh.visible = true;
if ((this.thisMesh.atomIndex = this.atomIndex) >= 0) this.thisMesh.modelIndex = this.viewer.getAtomModelIndex (this.atomIndex);
 else if (this.isFixed) this.thisMesh.modelIndex = -1;
 else if (this.modelIndex >= 0) this.thisMesh.modelIndex = this.modelIndex;
 else this.thisMesh.modelIndex = this.viewer.getCurrentModelIndex ();
this.thisMesh.scriptCommand = this.script;
this.thisMesh.ptCenter.setT (this.center);
this.thisMesh.scale3d = (this.thisMesh.jvxlData.jvxlPlane == null ? 0 : this.scale3d);
}, $fz.isPrivate = true, $fz));
$_M(c$, "discardTempData", 
function (discardAll) {
if (!discardAll) return;
this.title = null;
if (this.thisMesh == null) return;
this.thisMesh.surfaceSet = null;
}, "~B");
$_M(c$, "getDefaultColix", 
($fz = function () {
if (this.defaultColix != 0) return this.defaultColix;
if (!this.sg.isCubeData ()) return this.colix;
var argb = (this.sg.getCutoff () >= 0 ? -11525984 : -6283184);
return J.util.C.getColix (argb);
}, $fz.isPrivate = true, $fz));
$_M(c$, "drawLcaoCartoon", 
($fz = function (z, x, rotAxis, nElectrons) {
var lcaoCartoon = this.sg.setLcao ();
var rotRadians = rotAxis.x + rotAxis.y + rotAxis.z;
this.defaultColix = J.util.C.getColix (this.sg.getColor (1));
var colixNeg = J.util.C.getColix (this.sg.getColor (-1));
var y =  new J.util.V3 ();
var isReverse = (lcaoCartoon.length > 0 && lcaoCartoon.charAt (0) == '-');
if (isReverse) lcaoCartoon = lcaoCartoon.substring (1);
var sense = (isReverse ? -1 : 1);
y.cross (z, x);
if (rotRadians != 0) {
var a =  new J.util.AxisAngle4f ();
if (rotAxis.x != 0) a.setVA (x, rotRadians);
 else if (rotAxis.y != 0) a.setVA (y, rotRadians);
 else a.setVA (z, rotRadians);
var m =  new J.util.Matrix3f ();
m.setAA (a);
m.transform (x);
m.transform (y);
m.transform (z);
}if (this.thisMesh == null && this.nLCAO == 0) this.nLCAO = this.meshCount;
var id = (this.thisMesh == null ? (nElectrons > 0 ? "lp" : "lcao") + (++this.nLCAO) + "_" + lcaoCartoon : this.thisMesh.thisID);
if (this.thisMesh == null) this.allocMesh (id, null);
if (lcaoCartoon.equals ("px")) {
this.thisMesh.thisID += "a";
var meshA = this.thisMesh;
this.createLcaoLobe (x, sense, nElectrons);
if (nElectrons > 0) return;
this.setProperty ("thisID", id + "b", null);
this.createLcaoLobe (x, -sense, nElectrons);
this.thisMesh.colix = colixNeg;
this.linkedMesh = this.thisMesh.linkedMesh = meshA;
return;
}if (lcaoCartoon.equals ("py")) {
this.thisMesh.thisID += "a";
var meshA = this.thisMesh;
this.createLcaoLobe (y, sense, nElectrons);
if (nElectrons > 0) return;
this.setProperty ("thisID", id + "b", null);
this.createLcaoLobe (y, -sense, nElectrons);
this.thisMesh.colix = colixNeg;
this.linkedMesh = this.thisMesh.linkedMesh = meshA;
return;
}if (lcaoCartoon.equals ("pz")) {
this.thisMesh.thisID += "a";
var meshA = this.thisMesh;
this.createLcaoLobe (z, sense, nElectrons);
if (nElectrons > 0) return;
this.setProperty ("thisID", id + "b", null);
this.createLcaoLobe (z, -sense, nElectrons);
this.thisMesh.colix = colixNeg;
this.linkedMesh = this.thisMesh.linkedMesh = meshA;
return;
}if (lcaoCartoon.equals ("pza") || lcaoCartoon.indexOf ("sp") == 0 || lcaoCartoon.indexOf ("d") == 0 || lcaoCartoon.indexOf ("lp") == 0) {
this.createLcaoLobe (z, sense, nElectrons);
return;
}if (lcaoCartoon.equals ("pzb")) {
this.createLcaoLobe (z, -sense, nElectrons);
return;
}if (lcaoCartoon.equals ("pxa")) {
this.createLcaoLobe (x, sense, nElectrons);
return;
}if (lcaoCartoon.equals ("pxb")) {
this.createLcaoLobe (x, -sense, nElectrons);
return;
}if (lcaoCartoon.equals ("pya")) {
this.createLcaoLobe (y, sense, nElectrons);
return;
}if (lcaoCartoon.equals ("pyb")) {
this.createLcaoLobe (y, -sense, nElectrons);
return;
}if (lcaoCartoon.equals ("spacefill") || lcaoCartoon.equals ("cpk")) {
this.createLcaoLobe (null, 2 * this.viewer.getAtomRadius (this.atomIndex), nElectrons);
return;
}this.createLcaoLobe (null, 1, nElectrons);
return;
}, $fz.isPrivate = true, $fz), "J.util.V3,J.util.V3,J.util.V3,~N");
$_M(c$, "createLcaoLobe", 
($fz = function (lobeAxis, factor, nElectrons) {
this.initState ();
if (J.util.Logger.debugging) {
J.util.Logger.debug ("creating isosurface ID " + this.thisMesh.thisID);
}if (lobeAxis == null) {
this.setProperty ("sphere", Float.$valueOf (factor / 2), null);
} else {
this.lcaoDir.x = lobeAxis.x * factor;
this.lcaoDir.y = lobeAxis.y * factor;
this.lcaoDir.z = lobeAxis.z * factor;
this.lcaoDir.w = 0.7;
this.setProperty (nElectrons == 2 ? "lp" : nElectrons == 1 ? "rad" : "lobe", this.lcaoDir, null);
}this.thisMesh.colix = this.defaultColix;
this.setScriptInfo (null);
}, $fz.isPrivate = true, $fz), "J.util.V3,~N,~N");
Clazz.overrideMethod (c$, "invalidateTriangles", 
function () {
this.thisMesh.invalidatePolygons ();
});
Clazz.overrideMethod (c$, "setOutputStream", 
function (binaryDoc, os) {
binaryDoc.setOutputStream (os, this.viewer, this.privateKey);
}, "J.api.JmolDocument,java.io.OutputStream");
Clazz.overrideMethod (c$, "fillMeshData", 
function (meshData, mode, mesh) {
if (meshData == null) {
if (this.thisMesh == null) this.allocMesh (null, null);
if (!this.thisMesh.isMerged) this.thisMesh.clearType (this.myType, this.sg.getIAddGridPoints ());
this.thisMesh.connections = this.connections;
this.thisMesh.colix = this.getDefaultColix ();
this.thisMesh.meshColix = this.meshColix;
if (this.isPhaseColored || this.thisMesh.jvxlData.isBicolorMap) this.thisMesh.isColorSolid = false;
return;
}if (mesh == null) mesh = this.thisMesh;
if (mesh == null) return;
switch (mode) {
case 1:
meshData.mergeVertexCount0 = mesh.mergeVertexCount0;
meshData.vertices = mesh.vertices;
meshData.vertexSource = mesh.vertexSource;
meshData.vertexValues = mesh.vertexValues;
meshData.vertexCount = mesh.vertexCount;
meshData.vertexIncrement = mesh.vertexIncrement;
meshData.polygonCount = mesh.polygonCount;
meshData.polygonIndexes = mesh.polygonIndexes;
meshData.polygonColixes = mesh.polygonColixes;
meshData.bsSlabDisplay = mesh.bsSlabDisplay;
meshData.bsSlabGhost = mesh.bsSlabGhost;
meshData.slabColix = mesh.slabColix;
meshData.slabMeshType = mesh.slabMeshType;
meshData.polygonCount0 = mesh.polygonCount0;
meshData.vertexCount0 = mesh.vertexCount0;
meshData.slabOptions = mesh.slabOptions;
return;
case 2:
if (mesh.vertexColixes == null || mesh.vertexCount > mesh.vertexColixes.length) mesh.vertexColixes =  Clazz.newShortArray (mesh.vertexCount, 0);
meshData.vertexColixes = mesh.vertexColixes;
return;
case 3:
mesh.surfaceSet = meshData.surfaceSet;
mesh.vertexSets = meshData.vertexSets;
mesh.nSets = meshData.nSets;
return;
case 4:
mesh.vertices = meshData.vertices;
mesh.vertexValues = meshData.vertexValues;
mesh.vertexCount = meshData.vertexCount;
mesh.vertexIncrement = meshData.vertexIncrement;
mesh.vertexSource = meshData.vertexSource;
mesh.polygonCount = meshData.polygonCount;
mesh.polygonIndexes = meshData.polygonIndexes;
mesh.polygonColixes = meshData.polygonColixes;
mesh.bsSlabDisplay = meshData.bsSlabDisplay;
mesh.bsSlabGhost = meshData.bsSlabGhost;
mesh.slabColix = meshData.slabColix;
mesh.slabMeshType = meshData.slabMeshType;
mesh.polygonCount0 = meshData.polygonCount0;
mesh.vertexCount0 = meshData.vertexCount0;
mesh.mergeVertexCount0 = meshData.mergeVertexCount0;
mesh.slabOptions = meshData.slabOptions;
return;
}
}, "J.jvxl.data.MeshData,~N,J.shapesurface.IsosurfaceMesh");
Clazz.overrideMethod (c$, "notifySurfaceGenerationCompleted", 
function () {
this.setMeshI ();
this.setBsVdw ();
this.thisMesh.insideOut = this.sg.isInsideOut ();
this.thisMesh.vertexSource = this.sg.getVertexSource ();
this.thisMesh.spanningVectors = this.sg.getSpanningVectors ();
this.thisMesh.calculatedArea = null;
this.thisMesh.calculatedVolume = null;
var params = this.sg.getParams ();
if (!this.thisMesh.isMerged) this.thisMesh.initialize (this.sg.isFullyLit () ? 1073741964 : 1073741958, null, this.sg.getPlane ());
if (!params.allowVolumeRender) this.thisMesh.jvxlData.allowVolumeRender = false;
this.thisMesh.setColorsFromJvxlData (this.sg.getParams ().colorRgb);
if (this.thisMesh.jvxlData.slabInfo != null) this.viewer.runScript ("isosurface " + this.thisMesh.jvxlData.slabInfo);
if (this.sg.getParams ().psi_monteCarloCount > 0) this.thisMesh.diameter = -1;
});
Clazz.overrideMethod (c$, "notifySurfaceMappingCompleted", 
function () {
if (!this.thisMesh.isMerged) {
this.thisMesh.initialize (this.sg.isFullyLit () ? 1073741964 : 1073741958, null, this.sg.getPlane ());
this.thisMesh.setJvxlDataRendering ();
}this.setBsVdw ();
this.thisMesh.isColorSolid = false;
this.thisMesh.colorDensity = this.jvxlData.colorDensity;
this.thisMesh.volumeRenderPointSize = this.jvxlData.pointSize;
this.thisMesh.colorEncoder = this.sg.getColorEncoder ();
this.thisMesh.getContours ();
if (this.thisMesh.jvxlData.nContours != 0 && this.thisMesh.jvxlData.nContours != -1) this.explicitContours = true;
if (this.explicitContours && this.thisMesh.jvxlData.jvxlPlane != null) this.thisMesh.havePlanarContours = true;
this.setPropertySuper ("token", Integer.$valueOf (this.explicitContours ? 1073742046 : 1073741938), null);
this.setPropertySuper ("token", Integer.$valueOf (this.explicitContours ? 1073741898 : 1073742039), null);
var slabInfo = this.sg.getSlabInfo ();
if (slabInfo != null) {
this.thisMesh.slabPolygonsList (slabInfo, false);
this.thisMesh.reinitializeLightingAndColor (this.viewer);
}this.thisMesh.setColorCommand ();
});
$_M(c$, "setBsVdw", 
($fz = function () {
var bs = this.sg.geVdwBitSet ();
if (bs == null) return;
if (this.thisMesh.bsVdw == null) this.thisMesh.bsVdw =  new J.util.BS ();
this.thisMesh.bsVdw.or (bs);
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "calculateGeodesicSurface", 
function (bsSelected, envelopeRadius) {
return this.viewer.calculateSurface (bsSelected, envelopeRadius);
}, "J.util.BS,~N");
Clazz.overrideMethod (c$, "getSurfacePointIndexAndFraction", 
function (cutoff, isCutoffAbsolute, x, y, z, offset, vA, vB, valueA, valueB, pointA, edgeVector, isContourType, fReturn) {
return 0;
}, "~N,~B,~N,~N,~N,J.util.P3i,~N,~N,~N,~N,J.util.P3,J.util.V3,~B,~A");
Clazz.overrideMethod (c$, "addVertexCopy", 
function (vertexXYZ, value, assocVertex) {
if (this.cutoffRange != null && (value < this.cutoffRange[0] || value > this.cutoffRange[1])) return -1;
return (this.withinPoints != null && !J.shape.Mesh.checkWithin (vertexXYZ, this.withinPoints, this.withinDistance2, this.isWithinNot) ? -1 : this.thisMesh.addVertexCopy (vertexXYZ, value, assocVertex, this.associateNormals));
}, "J.util.P3,~N,~N");
Clazz.overrideMethod (c$, "addTriangleCheck", 
function (iA, iB, iC, check, check2, isAbsolute, color) {
return (iA < 0 || iB < 0 || iC < 0 || isAbsolute && !J.jvxl.data.MeshData.checkCutoff (iA, iB, iC, this.thisMesh.vertexValues) ? -1 : this.thisMesh.addTriangleCheck (iA, iB, iC, check, check2, color));
}, "~N,~N,~N,~N,~N,~B,~N");
$_M(c$, "setScriptInfo", 
function (strCommand) {
var script = (strCommand == null ? this.sg.getScript () : strCommand);
var pt = (script == null ? -1 : script.indexOf ("; isosurface map"));
if (pt == 0) {
if (this.thisMesh.scriptCommand == null) return;
pt = this.thisMesh.scriptCommand.indexOf ("; isosurface map");
if (pt >= 0) this.thisMesh.scriptCommand = this.thisMesh.scriptCommand.substring (0, pt);
this.thisMesh.scriptCommand += script;
return;
}this.thisMesh.title = this.sg.getTitle ();
this.thisMesh.dataType = this.sg.getParams ().dataType;
this.thisMesh.scale3d = this.sg.getParams ().scale3d;
if (script != null) {
if (script.charAt (0) == ' ') {
script = this.myType + " ID " + J.util.Escape.eS (this.thisMesh.thisID) + script;
pt = script.indexOf ("; isosurface map");
}}if (pt > 0 && this.scriptAppendix.length > 0) this.thisMesh.scriptCommand = script.substring (0, pt) + this.scriptAppendix + script.substring (pt);
 else this.thisMesh.scriptCommand = script + this.scriptAppendix;
if (!this.explicitID && script != null && (pt = script.indexOf ("# ID=")) >= 0) this.thisMesh.thisID = J.util.Parser.getQuotedStringAt (script, pt);
}, "~S");
Clazz.overrideMethod (c$, "addRequiredFile", 
function (fileName) {
fileName = " # /*file*/\"" + fileName + "\"";
if (this.scriptAppendix.indexOf (fileName) < 0) this.scriptAppendix += fileName;
}, "~S");
$_M(c$, "setJvxlInfo", 
($fz = function () {
if (this.sg.getJvxlData () !== this.jvxlData || this.sg.getJvxlData () !== this.thisMesh.jvxlData) this.jvxlData = this.thisMesh.jvxlData = this.sg.getJvxlData ();
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "getShapeDetail", 
function () {
var V =  new J.util.JmolList ();
for (var i = 0; i < this.meshCount; i++) {
var info =  new java.util.Hashtable ();
var mesh = this.isomeshes[i];
if (mesh == null || mesh.vertices == null || mesh.vertexCount == 0 && mesh.polygonCount == 0) continue;
this.addMeshInfo (mesh, info);
V.addLast (info);
}
return V;
});
$_M(c$, "addMeshInfo", 
function (mesh, info) {
info.put ("ID", (mesh.thisID == null ? "<noid>" : mesh.thisID));
info.put ("visible", Boolean.$valueOf (mesh.visible));
info.put ("vertexCount", Integer.$valueOf (mesh.vertexCount));
if (mesh.calculatedVolume != null) info.put ("volume", mesh.calculatedVolume);
if (mesh.calculatedArea != null) info.put ("area", mesh.calculatedArea);
if (mesh.ptCenter.x != 3.4028235E38) info.put ("center", mesh.ptCenter);
if (mesh.mat4 != null) info.put ("mat4", mesh.mat4);
if (mesh.scale3d != 0) info.put ("scale3d", Float.$valueOf (mesh.scale3d));
info.put ("xyzMin", mesh.jvxlData.boundingBox[0]);
info.put ("xyzMax", mesh.jvxlData.boundingBox[1]);
var s = J.jvxl.data.JvxlCoder.jvxlGetInfo (mesh.jvxlData);
if (s != null) info.put ("jvxlInfo", s.$replace ('\n', ' '));
info.put ("modelIndex", Integer.$valueOf (mesh.modelIndex));
info.put ("color", J.util.ColorUtil.colorPointFromInt2 (J.util.C.getArgb (mesh.colix)));
if (mesh.colorEncoder != null) info.put ("colorKey", mesh.colorEncoder.getColorKey ());
if (mesh.title != null) info.put ("title", mesh.title);
if (mesh.jvxlData.contourValues != null || mesh.jvxlData.contourValuesUsed != null) info.put ("contours", mesh.getContourList (this.viewer));
}, "J.shapesurface.IsosurfaceMesh,java.util.Map");
Clazz.overrideMethod (c$, "getPlane", 
function (x) {
return null;
}, "~N");
Clazz.overrideMethod (c$, "getValue", 
function (x, y, z, ptyz) {
return 0;
}, "~N,~N,~N,~N");
Clazz.overrideMethod (c$, "checkObjectHovered", 
function (x, y, bsVisible) {
if (this.keyXy != null && x >= this.keyXy[0] && y >= this.keyXy[1] && x < this.keyXy[2] && y < this.keyXy[3]) {
this.hoverKey (x, y);
return true;
}if (!this.viewer.getDrawHover ()) return false;
var s = this.findValue (x, y, false, bsVisible);
if (s == null) return false;
if (this.gdata.isDisplayAntialiased ()) {
x <<= 1;
y <<= 1;
}this.viewer.hoverOnPt (x, y, s, this.pickedMesh.thisID, this.pickedPt);
return true;
}, "~N,~N,J.util.BS");
$_M(c$, "hoverKey", 
($fz = function (x, y) {
try {
var s;
var f = 1 - 1.0 * (y - this.keyXy[1]) / (this.keyXy[3] - this.keyXy[1]);
if (this.thisMesh.showContourLines) {
var vContours = this.thisMesh.getContours ();
if (vContours == null) {
if (this.thisMesh.jvxlData.contourValues == null) return;
var i = Clazz.doubleToInt (Math.floor (f * this.thisMesh.jvxlData.contourValues.length));
if (i < 0 || i > this.thisMesh.jvxlData.contourValues.length) return;
s = "" + this.thisMesh.jvxlData.contourValues[i];
} else {
var i = Clazz.doubleToInt (Math.floor (f * vContours.length));
if (i < 0 || i > vContours.length) return;
s = "" + (vContours[i].get (2)).floatValue ();
}} else {
var g = this.thisMesh.colorEncoder.quantize (f, true);
f = this.thisMesh.colorEncoder.quantize (f, false);
s = "" + g + " - " + f;
}if (this.gdata.isAntialiased ()) {
x <<= 1;
y <<= 1;
}this.viewer.hoverOnPt (x, y, s, null, null);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}, $fz.isPrivate = true, $fz), "~N,~N");
Clazz.overrideMethod (c$, "checkObjectClicked", 
function (x, y, action, bsVisible, drawPicking) {
if (!drawPicking) return null;
if (!this.viewer.isBound (action, 18)) return null;
var dmin2 = 100;
if (this.gdata.isAntialiased ()) {
x <<= 1;
y <<= 1;
dmin2 <<= 1;
}var imesh = -1;
var jmaxz = -1;
var jminz = -1;
var maxz = -2147483648;
var minz = 2147483647;
var pickFront = true;
for (var i = 0; i < this.meshCount; i++) {
var m = this.isomeshes[i];
if (!this.isPickable (m, bsVisible)) continue;
var centers = (pickFront ? m.vertices : m.getCenters ());
if (centers == null) continue;
for (var j = centers.length; --j >= 0; ) {
var v = centers[j];
if (v == null) continue;
var d2 = this.coordinateInRange (x, y, v, dmin2, this.ptXY);
if (d2 >= 0) {
if (this.ptXY.z < minz) {
if (pickFront) imesh = i;
minz = this.ptXY.z;
jminz = j;
}if (this.ptXY.z > maxz) {
if (!pickFront) imesh = i;
maxz = this.ptXY.z;
jmaxz = j;
}}}
}
if (imesh < 0) return null;
this.pickedMesh = this.isomeshes[imesh];
this.setPropertySuper ("thisID", this.pickedMesh.thisID, null);
var iFace = this.pickedVertex = (pickFront ? jminz : jmaxz);
var ptRet =  new J.util.P3 ();
ptRet.setT ((pickFront ? this.pickedMesh.vertices[this.pickedVertex] : (this.pickedMesh).centers[iFace]));
this.pickedModel = this.pickedMesh.modelIndex;
this.setStatusPicked (-4, ptRet);
return this.getPickedPoint (ptRet, this.pickedModel);
}, "~N,~N,~N,J.util.BS,~B");
$_M(c$, "isPickable", 
($fz = function (m, bsVisible) {
return m.visibilityFlags != 0 && (m.modelIndex < 0 || bsVisible.get (m.modelIndex)) && !J.util.C.isColixTranslucent (m.colix);
}, $fz.isPrivate = true, $fz), "J.shapesurface.IsosurfaceMesh,J.util.BS");
$_M(c$, "findValue", 
($fz = function (x, y, isPicking, bsVisible) {
var dmin2 = 100;
if (this.gdata.isAntialiased ()) {
x <<= 1;
y <<= 1;
dmin2 <<= 1;
}var pickedVertex = -1;
var pickedContour = null;
var m = null;
for (var i = 0; i < this.meshCount; i++) {
m = this.isomeshes[i];
if (!this.isPickable (m, bsVisible)) continue;
var vs = m.jvxlData.vContours;
var ilast = (m.firstRealVertex < 0 ? 0 : m.firstRealVertex);
var pickedJ = 0;
if (vs != null && vs.length > 0) {
for (var j = 0; j < vs.length; j++) {
var vc = vs[j];
var n = vc.size () - 1;
for (var k = 6; k < n; k++) {
var v = vc.get (k);
var d2 = this.coordinateInRange (x, y, v, dmin2, this.ptXY);
if (d2 >= 0) {
dmin2 = d2;
pickedContour = vc;
pickedJ = j;
this.pickedMesh = m;
this.pickedPt = v;
}}
}
if (pickedContour != null) return pickedContour.get (2).toString () + (J.util.Logger.debugging ? " " + pickedJ : "");
} else if (m.jvxlData.jvxlPlane != null && m.vertexValues != null) {
var vertices = (m.mat4 == null && m.scale3d == 0 ? m.vertices : m.getOffsetVertices (m.jvxlData.jvxlPlane));
for (var k = m.vertexCount; --k >= ilast; ) {
var v = vertices[k];
var d2 = this.coordinateInRange (x, y, v, dmin2, this.ptXY);
if (d2 >= 0) {
dmin2 = d2;
pickedVertex = k;
this.pickedMesh = m;
this.pickedPt = v;
}}
if (pickedVertex != -1) break;
} else if (m.vertexValues != null) {
for (var k = m.vertexCount; --k >= ilast; ) {
var v = m.vertices[k];
var d2 = this.coordinateInRange (x, y, v, dmin2, this.ptXY);
if (d2 >= 0) {
dmin2 = d2;
pickedVertex = k;
this.pickedMesh = m;
this.pickedPt = v;
}}
if (pickedVertex != -1) break;
}}
return (pickedVertex == -1 ? null : (J.util.Logger.debugging ? "$" + m.thisID + "[" + (pickedVertex + 1) + "] " + m.vertices[pickedVertex] + ": " : m.thisID + ": ") + m.vertexValues[pickedVertex]);
}, $fz.isPrivate = true, $fz), "~N,~N,~B,J.util.BS");
$_M(c$, "getCmd", 
function (index) {
var sb =  new J.util.SB ().append ("\n");
this.getMeshCommand (sb, index);
return (sb.toString ());
}, "~N");
Clazz.defineStatics (c$,
"MAX_OBJECT_CLICK_DISTANCE_SQUARED", 100);
});
