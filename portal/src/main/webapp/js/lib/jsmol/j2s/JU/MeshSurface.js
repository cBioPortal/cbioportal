Clazz.declarePackage ("JU");
Clazz.load (null, "JU.MeshSurface", ["java.lang.Float", "java.util.Hashtable", "JU.AU", "$.BS", "$.Lst", "$.Measure", "$.P3", "$.P4", "$.PT", "$.SB", "JU.BSUtil", "$.BoxInfo", "$.C", "$.Escape", "$.Geodesic", "$.TempArray"], function () {
c$ = Clazz.decorateAsClass (function () {
this.spanningVectors = null;
this.meshType = null;
this.vc = 0;
this.vs = null;
this.vvs = null;
this.vertexSource = null;
this.pc = 0;
this.pis = null;
this.polygonTranslucencies = null;
this.isTriangleSet = false;
this.haveQuads = false;
this.colix = 0;
this.colixBack = 0;
this.isColorSolid = true;
this.offset = null;
this.altVertices = null;
this.pcs = null;
this.vcs = null;
this.normals = null;
this.normalsTemp = null;
this.normalCount = 0;
this.normixCount = 0;
this.bsPolygons = null;
this.mat4 = null;
this.surfaceSet = null;
this.vertexSets = null;
this.nSets = 0;
this.checkCount = 2;
this.lastColor = 0;
this.lastColix = 0;
this.iA = 0;
this.iB = 0;
this.iC = 0;
this.polygonCount0 = 0;
this.vertexCount0 = 0;
this.bsSlabDisplay = null;
this.bsSlabGhost = null;
this.bsTransPolygons = null;
this.slabMeshType = 0;
this.slabColix = 0;
this.bsDisplay = null;
this.slabOptions = null;
this.doClear = false;
this.doGhost = false;
this.doCap = false;
this.iD = 0;
this.iE = 0;
this.mergeVertexCount0 = 0;
this.mergePolygonCount0 = 0;
this.isMerged = false;
Clazz.instantialize (this, arguments);
}, JU, "MeshSurface");
Clazz.makeConstructor (c$, 
function () {
});
c$.newMesh = Clazz.defineMethod (c$, "newMesh", 
function (isAlt, vertices, vertexCount, polygonIndexes, normals, nNormals) {
var ms =  new JU.MeshSurface ();
ms.pis = polygonIndexes;
if (isAlt) ms.altVertices = vertices;
 else ms.vs = vertices;
ms.vc = (vertexCount == 0 ? vertices.length : vertexCount);
ms.normals = normals;
ms.normalCount = (nNormals == 0 && normals != null ? normals.length : nNormals);
return ms;
}, "~B,~A,~N,~A,~A,~N");
c$.newSlab = Clazz.defineMethod (c$, "newSlab", 
function (vertices, vertexCount, vertexValues, polygonIndexes, polygonCount, checkCount) {
var ms =  new JU.MeshSurface ();
ms.vs = vertices;
ms.vvs = vertexValues;
ms.vc = vertexCount;
ms.pis = polygonIndexes;
ms.pc = polygonCount;
ms.checkCount = checkCount;
return ms;
}, "~A,~N,~A,~A,~N,~N");
Clazz.defineMethod (c$, "getVertices", 
function () {
return (this.altVertices == null ? this.vs : this.altVertices);
});
Clazz.defineMethod (c$, "getFaces", 
function () {
return this.pis;
});
Clazz.defineMethod (c$, "setColix", 
function (colix) {
this.colix = colix;
}, "~N");
Clazz.defineMethod (c$, "setColixBack", 
function (colix) {
this.colixBack = colix;
}, "~N");
Clazz.defineMethod (c$, "addV", 
function (vertex, asCopy) {
if (this.vc == 0) this.vs =  new Array (25);
 else if (this.vc == this.vs.length) this.vs = JU.AU.doubleLength (this.vs);
this.vs[this.vc] = (asCopy ? JU.P3.newP (vertex) : vertex);
return this.vc++;
}, "JU.T3,~B");
Clazz.defineMethod (c$, "addTriangle", 
function (vertexA, vertexB, vertexC) {
this.addPolygon ([vertexA, vertexB, vertexC], null);
}, "~N,~N,~N");
Clazz.defineMethod (c$, "addQuad", 
function (vertexA, vertexB, vertexC, vertexD) {
this.haveQuads = true;
this.addPolygon ([vertexA, vertexB, vertexC, vertexD], null);
}, "~N,~N,~N,~N");
Clazz.defineMethod (c$, "setPolygonCount", 
function (polygonCount) {
this.pc = polygonCount;
if (polygonCount < 0) return;
if (this.pis == null || polygonCount > this.pis.length) this.pis = JU.AU.newInt2 (polygonCount);
}, "~N");
Clazz.defineMethod (c$, "addVCVal", 
function (vertex, value, asCopy) {
if (this.vc == 0) this.vvs =  Clazz.newFloatArray (25, 0);
 else if (this.vc >= this.vvs.length) this.vvs = JU.AU.doubleLengthF (this.vvs);
this.vvs[this.vc] = value;
return this.addV (vertex, asCopy);
}, "JU.T3,~N,~B");
Clazz.defineMethod (c$, "addTriangleCheck", 
function (vertexA, vertexB, vertexC, check, check2, color) {
return (this.vs == null || this.vvs != null && (Float.isNaN (this.vvs[vertexA]) || Float.isNaN (this.vvs[vertexB]) || Float.isNaN (this.vvs[vertexC])) || Float.isNaN (this.vs[vertexA].x) || Float.isNaN (this.vs[vertexB].x) || Float.isNaN (this.vs[vertexC].x) ? -1 : this.addPolygonV3 (vertexA, vertexB, vertexC, check, check2, color, null));
}, "~N,~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "addPolygonV3", 
 function (vertexA, vertexB, vertexC, check, check2, color, bs) {
return (this.checkCount == 2 ? this.addPolygonC ([vertexA, vertexB, vertexC, check, check2], color, bs) : this.addPolygon ([vertexA, vertexB, vertexC, check], bs));
}, "~N,~N,~N,~N,~N,~N,JU.BS");
Clazz.defineMethod (c$, "addPolygonC", 
function (polygon, color, bs) {
if (color != 0) {
if (this.pcs == null || this.pc == 0) this.lastColor = 0;
var colix = (color == this.lastColor ? this.lastColix : (this.lastColix = JU.C.getColix (this.lastColor = color)));
this.setPolygonColix (this.pc, colix);
}return this.addPolygon (polygon, bs);
}, "~A,~N,JU.BS");
Clazz.defineMethod (c$, "addPolygon", 
 function (polygon, bs) {
var n = this.pc;
if (this.pc == 0) this.pis = JU.AU.newInt2 (25);
 else if (this.pc == this.pis.length) this.pis = JU.AU.doubleLength (this.pis);
if (bs != null) bs.set (this.pc);
this.pis[this.pc++] = polygon;
return n;
}, "~A,JU.BS");
Clazz.defineMethod (c$, "setPolygonColix", 
 function (index, colix) {
if (this.pcs == null) {
this.pcs =  Clazz.newShortArray (25, 0);
} else if (index >= this.pcs.length) {
this.pcs = JU.AU.doubleLengthShort (this.pcs);
}this.pcs[index] = colix;
}, "~N,~N");
Clazz.defineMethod (c$, "invalidatePolygons", 
function () {
for (var i = this.pc; --i >= this.mergePolygonCount0; ) if ((this.bsSlabDisplay == null || this.bsSlabDisplay.get (i)) && !this.setABC (i)) this.pis[i] = null;

});
Clazz.defineMethod (c$, "setABC", 
function (i) {
if (this.bsSlabDisplay != null && !this.bsSlabDisplay.get (i) && (this.bsSlabGhost == null || !this.bsSlabGhost.get (i))) return false;
var vertexIndexes = this.pis[i];
if (vertexIndexes == null || vertexIndexes.length < 3) return false;
this.iA = vertexIndexes[0];
this.iB = vertexIndexes[1];
this.iC = vertexIndexes[2];
return this.vvs == null || !(Float.isNaN (this.vvs[this.iA]) || Float.isNaN (this.vvs[this.iB]) || Float.isNaN (this.vvs[this.iC]));
}, "~N");
Clazz.defineMethod (c$, "setTranslucentVertices", 
function (bsVertices) {
}, "JU.BS");
Clazz.defineMethod (c$, "setSlab", 
function (bsDisplay, bsGhost, type, color, translucency) {
this.bsSlabDisplay = bsDisplay;
this.bsSlabGhost = bsGhost;
this.slabMeshType = (type.equalsIgnoreCase ("mesh") ? 1073742018 : 1073741938);
this.slabColix = JU.C.getColixTranslucent3 (JU.C.getColixS (color), true, translucency);
}, "JU.BS,JU.BS,~S,~S,~N");
Clazz.defineMethod (c$, "getSlabColor", 
function () {
return (this.bsSlabGhost == null ? null : JU.C.getHexCode (this.slabColix));
});
Clazz.defineMethod (c$, "getSlabTranslucency", 
function () {
return (this.bsSlabGhost == null ? null : "" + JU.C.getColixTranslucencyFractional (this.slabColix));
});
Clazz.defineMethod (c$, "getSlabType", 
function () {
return (this.bsSlabGhost != null && this.slabMeshType == 1073742018 ? "mesh" : null);
});
Clazz.defineMethod (c$, "resetTransPolygons", 
function () {
var isTranslucent = JU.C.isColixTranslucent (this.colix);
var translucentLevel = JU.C.getColixTranslucencyFractional (this.colix);
for (var i = 0; i < this.pc; i++) if (this.bsTransPolygons.get (i)) {
if (!this.setABC (i)) continue;
this.vcs[this.iA] = JU.C.getColixTranslucent3 (this.vcs[this.iA], isTranslucent, translucentLevel);
this.vcs[this.iB] = JU.C.getColixTranslucent3 (this.vcs[this.iB], isTranslucent, translucentLevel);
this.vcs[this.iC] = JU.C.getColixTranslucent3 (this.vcs[this.iC], isTranslucent, translucentLevel);
}
this.bsTransPolygons = null;
this.polygonTranslucencies = null;
});
Clazz.defineMethod (c$, "resetSlab", 
function () {
this.slabPolygons (JU.TempArray.getSlabObjectType (1048587, null, false, null), false);
});
c$.getCapSlabObject = Clazz.defineMethod (c$, "getCapSlabObject", 
function (s, isCap) {
try {
if (s.indexOf ("array") == 0) {
var pts = JU.PT.split (s.substring (6, s.length - 1), ",");
return JU.TempArray.getSlabObjectType (1679429641, [JU.Escape.uP (pts[0]), JU.Escape.uP (pts[1]), JU.Escape.uP (pts[2]), JU.Escape.uP (pts[3])], isCap, null);
}var plane = JU.Escape.uP (s);
if (Clazz.instanceOf (plane, JU.P4)) return JU.TempArray.getSlabObjectType (135266319, plane, isCap, null);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return null;
}, "~S,~B");
Clazz.defineMethod (c$, "slabPolygonsList", 
function (slabInfo, allowCap) {
for (var i = 0; i < slabInfo.size (); i++) if (!this.slabPolygons (slabInfo.get (i), allowCap)) break;

}, "JU.Lst,~B");
Clazz.defineMethod (c$, "slabPolygons", 
function (slabObject, allowCap) {
if (this.polygonCount0 < 0) return false;
var slabType = (slabObject[0]).intValue ();
if (slabType == 1048587 || slabType == 1073741872) {
if (this.bsSlabDisplay != null && (this.polygonCount0 != 0 || this.vertexCount0 != 0)) {
this.pc = this.polygonCount0;
this.vc = this.vertexCount0;
this.polygonCount0 = this.vertexCount0 = 0;
this.normixCount = (this.isTriangleSet ? this.pc : this.vc);
this.bsSlabDisplay.setBits (0, (this.pc == 0 ? this.vc : this.pc));
this.slabOptions =  new JU.SB ().append (this.meshType + " slab none");
this.bsSlabGhost = null;
this.slabMeshType = 1048587;
}if (slabType == 1048587) return false;
}var slabbingObject = slabObject[1];
var andCap = (slabObject[2]).booleanValue () && !(slabType == 1073741872);
if (andCap && !allowCap) return false;
var colorData = slabObject[3];
var isGhost = (colorData != null);
if (this.bsSlabDisplay == null || this.polygonCount0 == 0 && this.vertexCount0 == 0) {
this.polygonCount0 = this.pc;
this.vertexCount0 = this.vc;
this.bsSlabDisplay = JU.BSUtil.setAll (this.pc == 0 ? this.vc : this.pc);
this.bsSlabGhost = null;
if (this.pc == 0 && this.vc == 0) return false;
} else if (this.isMerged) {
if (this.pc == 0) this.bsSlabDisplay.setBits (this.mergeVertexCount0, this.vc);
 else this.bsSlabDisplay.setBits (this.mergePolygonCount0, this.pc);
}if (isGhost) {
if (this.bsSlabGhost == null) this.bsSlabGhost =  new JU.BS ();
this.slabMeshType = (colorData[0]).intValue ();
this.slabColix = (colorData[1]).shortValue ();
andCap = false;
this.colix = JU.C.getColixTranslucent3 (this.colix, false, 0);
}var sb =  new JU.SB ();
sb.append (andCap ? " cap " : " slab ");
if (isGhost) {
sb.append ("translucent ").appendF (JU.C.getColixTranslucencyFractional (this.slabColix)).append (" ");
var s = JU.C.getHexCode (this.slabColix);
if (s != null) sb.append (s).append (" ");
if (this.slabMeshType == 1073742018) sb.append ("mesh ");
}switch (slabType) {
case 1073741872:
sb.append ("brillouin");
this.slabBrillouin (slabbingObject);
break;
case 3:
this.getIntersection (0, null, null, null, null, slabbingObject, null, andCap, false, 3, isGhost);
break;
case 135266319:
var plane = slabbingObject;
sb.append (JU.Escape.eP4 (plane));
this.getIntersection (0, plane, null, null, null, null, null, andCap, false, 135266319, isGhost);
break;
case 1614417948:
case 1679429641:
var box = slabbingObject;
sb.append ("within ").append (JU.Escape.eAP (box));
var faces = JU.BoxInfo.getFacesFromCriticalPoints (box);
for (var i = 0; i < faces.length; i++) {
this.getIntersection (0, faces[i], null, null, null, null, null, andCap, false, 135266319, isGhost);
}
break;
case 135270408:
this.getIntersection (0, null, null, null, slabbingObject, null, null, false, false, 32, isGhost);
break;
case 135266325:
case 1073742114:
case 1073742018:
var o = slabbingObject;
var distance = (o[0]).floatValue ();
switch (slabType) {
case 135266325:
var points = o[1];
var bs = o[2];
sb.append ("within ").appendF (distance).append (bs == null ? JU.Escape.e (points) : JU.Escape.e (bs));
this.getIntersection (distance, null, points, null, null, null, null, andCap, false, (distance > 0 ? 1276118018 : 1073742154), isGhost);
break;
case 1073742114:
if (this.vvs == null) return false;
var distanceMax = (o[1]).floatValue ();
sb.append ("within range ").appendF (distance).append (" ").appendF (distanceMax);
bs = (distanceMax < distance ? JU.BSUtil.copy (this.bsSlabDisplay) : null);
this.getIntersection (distance, null, null, null, null, null, null, andCap, false, 32, isGhost);
var bsA = (bs == null ? null : JU.BSUtil.copy (this.bsSlabDisplay));
JU.BSUtil.copy2 (bs, this.bsSlabDisplay);
this.getIntersection (distanceMax, null, null, null, null, null, null, andCap, false, 64, isGhost);
if (bsA != null) this.bsSlabDisplay.or (bsA);
break;
case 1073742018:
var mesh = o[1];
this.getIntersection (0, null, null, null, null, null, mesh, andCap, false, distance < 0 ? 32 : 64, isGhost);
break;
}
break;
}
var newOptions = sb.toString ();
if (this.slabOptions == null) this.slabOptions =  new JU.SB ();
if (this.slabOptions.indexOf (newOptions) < 0) this.slabOptions.append (this.slabOptions.length () > 0 ? "; " : "").append (this.meshType).append (newOptions);
return true;
}, "~A,~B");
Clazz.defineMethod (c$, "slabBrillouin", 
function (unitCellVectors) {
return;
}, "~A");
Clazz.defineMethod (c$, "addIntersectionVertex", 
function (vertex, value, source, set, mapEdge, i1, i2) {
var key = (i1 > i2 ? i2 + "_" + i1 : i1 + "_" + i2);
if (i1 >= 0) {
var v = mapEdge.get (key);
if (v != null) {
return v.intValue ();
}}if (this.vertexSource != null) {
if (this.vc >= this.vertexSource.length) this.vertexSource = JU.AU.doubleLengthI (this.vertexSource);
this.vertexSource[this.vc] = source;
}if (this.vertexSets != null) {
if (this.vc >= this.vertexSets.length) this.vertexSets = JU.AU.doubleLengthI (this.vertexSets);
this.vertexSets[this.vc] = set;
}var i = this.addVCVal (vertex, value, true);
mapEdge.put (key, Integer.$valueOf (i));
return i;
}, "JU.T3,~N,~N,~N,java.util.Map,~N,~N");
Clazz.defineMethod (c$, "getIntersection", 
function (distance, plane, ptCenters, vData, fData, bsSource, meshSurface, andCap, doClean, tokType, isGhost) {
var isSlab = (vData == null);
var pts = null;
if (fData == null) {
if (tokType == 3 && bsSource != null) {
if (this.vertexSource == null) return;
fData =  Clazz.newFloatArray (this.vc, 0);
for (var i = 0; i < this.vc; i++) if ((fData[i] = this.vertexSource[i]) == -1) System.out.println ("meshsurface hmm");

} else {
fData = this.vvs;
}}var mapEdge =  new java.util.Hashtable ();
if (ptCenters != null || isGhost) andCap = false;
var values =  Clazz.newFloatArray (2, 0);
var fracs =  Clazz.newFloatArray (2, 0);
var absD = Math.abs (distance);
var d1;
var d2;
var d3;
var valA;
var valB;
var valC;
var sourceA = 0;
var sourceB = 0;
var sourceC = 0;
var setA = 0;
var iPts = (andCap ?  new JU.Lst () : null);
if (this.pc == 0) {
for (var i = this.mergeVertexCount0; i < this.vc; i++) {
if (Float.isNaN (fData[i]) || JU.MeshSurface.checkSlab (tokType, this.vs[i], fData[i], distance, plane, ptCenters, bsSource) > 0) this.bsSlabDisplay.clear (i);
}
return;
}var iLast = this.pc;
for (var i = this.mergePolygonCount0; i < iLast; i++) {
if (!this.setABC (i)) continue;
var bsSlab = (this.bsSlabGhost != null && this.bsSlabGhost.get (i) ? this.bsSlabGhost : this.bsSlabDisplay);
var check1 = this.pis[i][3];
var check2 = (this.checkCount == 2 ? this.pis[i][4] : 0);
var vA = this.vs[this.iA];
var vB = this.vs[this.iB];
var vC = this.vs[this.iC];
valA = fData[this.iA];
valB = fData[this.iB];
valC = fData[this.iC];
if (this.vertexSource != null) {
sourceA = this.vertexSource[this.iA];
sourceB = this.vertexSource[this.iB];
sourceC = this.vertexSource[this.iC];
}if (this.vertexSets != null) setA = this.vertexSets[this.iA];
d1 = JU.MeshSurface.checkSlab (tokType, vA, valA, (bsSource == null ? distance : sourceA), plane, ptCenters, bsSource);
d2 = JU.MeshSurface.checkSlab (tokType, vB, valB, (bsSource == null ? distance : sourceB), plane, ptCenters, bsSource);
d3 = JU.MeshSurface.checkSlab (tokType, vC, valC, (bsSource == null ? distance : sourceC), plane, ptCenters, bsSource);
var test1 = (d1 != 0 && d1 < 0 ? 1 : 0) + (d2 != 0 && d2 < 0 ? 2 : 0) + (d3 != 0 && d3 < 0 ? 4 : 0);
switch (test1) {
default:
case 7:
case 0:
break;
case 1:
case 6:
if (ptCenters == null) pts = [JU.MeshSurface.interpolatePoint (vA, vB, -d1, d2, valA, valB, values, fracs, 0), JU.MeshSurface.interpolatePoint (vA, vC, -d1, d3, valA, valC, values, fracs, 1)];
 else pts = [this.interpolateSphere (vA, vB, -d1, -d2, absD, valA, valB, values, fracs, 0), this.interpolateSphere (vA, vC, -d1, -d3, absD, valA, valC, values, fracs, 1)];
break;
case 2:
case 5:
if (ptCenters == null) pts = [JU.MeshSurface.interpolatePoint (vB, vA, -d2, d1, valB, valA, values, fracs, 1), JU.MeshSurface.interpolatePoint (vB, vC, -d2, d3, valB, valC, values, fracs, 0)];
 else pts = [this.interpolateSphere (vB, vA, -d2, -d1, absD, valB, valA, values, fracs, 1), this.interpolateSphere (vB, vC, -d2, -d3, absD, valB, valC, values, fracs, 0)];
break;
case 3:
case 4:
if (ptCenters == null) pts = [JU.MeshSurface.interpolatePoint (vC, vA, -d3, d1, valC, valA, values, fracs, 0), JU.MeshSurface.interpolatePoint (vC, vB, -d3, d2, valC, valB, values, fracs, 1)];
 else pts = [this.interpolateSphere (vC, vA, -d3, -d1, absD, valC, valA, values, fracs, 0), this.interpolateSphere (vC, vB, -d3, -d2, absD, valC, valB, values, fracs, 1)];
break;
}
this.doClear = true;
this.doGhost = isGhost;
this.doCap = andCap;
var bs;
if (isSlab) {
switch (test1) {
case 0:
this.doCap = false;
break;
case 7:
continue;
case 1:
case 6:
var tossBC = (test1 == 1);
if (tossBC || isGhost) {
if (!this.getDE (fracs, 0, this.iA, this.iB, this.iC, tossBC)) break;
if (this.iD < 0) this.iD = this.addIntersectionVertex (pts[0], values[0], sourceA, setA, mapEdge, this.iA, this.iB);
if (this.iE < 0) this.iE = this.addIntersectionVertex (pts[1], values[1], sourceA, setA, mapEdge, this.iA, this.iC);
bs = (tossBC ? bsSlab : this.bsSlabGhost);
this.addPolygonV3 (this.iA, this.iD, this.iE, check1 & 5 | 2, check2, 0, bs);
if (!isGhost) break;
}if (!this.getDE (fracs, 1, this.iA, this.iC, this.iB, tossBC)) break;
bs = (tossBC ? this.bsSlabGhost : bsSlab);
if (this.iE < 0) {
this.iE = this.addIntersectionVertex (pts[0], values[0], sourceB, setA, mapEdge, this.iA, this.iB);
this.addPolygonV3 (this.iE, this.iB, this.iC, check1 & 3, check2, 0, bs);
}if (this.iD < 0) {
this.iD = this.addIntersectionVertex (pts[1], values[1], sourceC, setA, mapEdge, this.iA, this.iC);
this.addPolygonV3 (this.iD, this.iE, this.iC, check1 & 4 | 1, check2, 0, bs);
}break;
case 5:
case 2:
var tossAC = (test1 == 2);
if (tossAC || isGhost) {
if (!this.getDE (fracs, 0, this.iB, this.iC, this.iA, tossAC)) break;
bs = (tossAC ? bsSlab : this.bsSlabGhost);
if (this.iE < 0) this.iE = this.addIntersectionVertex (pts[0], values[0], sourceB, setA, mapEdge, this.iB, this.iA);
if (this.iD < 0) this.iD = this.addIntersectionVertex (pts[1], values[1], sourceB, setA, mapEdge, this.iB, this.iC);
this.addPolygonV3 (this.iE, this.iB, this.iD, check1 & 3 | 4, check2, 0, bs);
if (!isGhost) break;
}if (!this.getDE (fracs, 1, this.iB, this.iA, this.iC, tossAC)) break;
bs = (tossAC ? this.bsSlabGhost : bsSlab);
if (this.iD < 0) {
this.iD = this.addIntersectionVertex (pts[0], values[0], sourceA, setA, mapEdge, this.iB, this.iA);
this.addPolygonV3 (this.iA, this.iD, this.iC, check1 & 5, check2, 0, bs);
}if (this.iE < 0) {
this.iE = this.addIntersectionVertex (pts[1], values[1], sourceC, setA, mapEdge, this.iB, this.iC);
this.addPolygonV3 (this.iD, this.iE, this.iC, check1 & 2 | 1, check2, 0, bs);
}break;
case 4:
case 3:
var tossAB = (test1 == 4);
if (tossAB || isGhost) {
if (!this.getDE (fracs, 0, this.iC, this.iA, this.iB, tossAB)) break;
if (this.iD < 0) this.iD = this.addIntersectionVertex (pts[0], values[0], sourceC, setA, mapEdge, this.iA, this.iC);
if (this.iE < 0) this.iE = this.addIntersectionVertex (pts[1], values[1], sourceC, setA, mapEdge, this.iB, this.iC);
bs = (tossAB ? bsSlab : this.bsSlabGhost);
this.addPolygonV3 (this.iD, this.iE, this.iC, check1 & 6 | 1, check2, 0, bs);
if (!isGhost) break;
}if (!this.getDE (fracs, 1, this.iC, this.iB, this.iA, tossAB)) break;
bs = (tossAB ? this.bsSlabGhost : bsSlab);
if (this.iE < 0) {
this.iE = this.addIntersectionVertex (pts[0], values[0], sourceA, setA, mapEdge, this.iA, this.iC);
this.addPolygonV3 (this.iA, this.iB, this.iE, check1 & 5, check2, 0, bs);
}if (this.iD < 0) {
this.iD = this.addIntersectionVertex (pts[1], values[1], sourceB, setA, mapEdge, this.iB, this.iC);
this.addPolygonV3 (this.iE, this.iB, this.iD, check1 & 2 | 4, check2, 0, bs);
}break;
}
if (this.doClear) {
bsSlab.clear (i);
if (this.doGhost) this.bsSlabGhost.set (i);
}if (this.doCap) {
iPts.addLast ([this.iD, this.iE]);
}} else if (pts != null) {
vData.addLast (pts);
}}
if (andCap && iPts.size () > 0) {
var center =  new JU.P3 ();
for (var i = iPts.size (); --i >= 0; ) {
var ipts = iPts.get (i);
center.add (this.vs[ipts[0]]);
center.add (this.vs[ipts[1]]);
}
center.scale (0.5 / iPts.size ());
var v0 = this.addIntersectionVertex (center, 0, -1, setA, mapEdge, -1, -1);
for (var i = iPts.size (); --i >= 0; ) {
var ipts = iPts.get (i);
this.addPolygonV3 (ipts[0], v0, ipts[1], 0, 0, 0, this.bsSlabDisplay);
}
}if (!doClean) return;
var bsv =  new JU.BS ();
var bsp =  new JU.BS ();
for (var i = 0; i < this.pc; i++) {
if (this.pis[i] == null) continue;
bsp.set (i);
for (var j = 0; j < 3; j++) bsv.set (this.pis[i][j]);

}
var n = 0;
var nPoly = bsp.cardinality ();
if (nPoly != this.pc) {
var map =  Clazz.newIntArray (this.vc, 0);
for (var i = 0; i < this.vc; i++) if (bsv.get (i)) map[i] = n++;

var vTemp =  new Array (n);
n = 0;
for (var i = 0; i < this.vc; i++) if (bsv.get (i)) vTemp[n++] = this.vs[i];

var pTemp = JU.AU.newInt2 (nPoly);
nPoly = 0;
for (var i = 0; i < this.pc; i++) if (this.pis[i] != null) {
for (var j = 0; j < 3; j++) this.pis[i][j] = map[this.pis[i][j]];

pTemp[nPoly++] = this.pis[i];
}
this.vs = vTemp;
this.vc = n;
this.pis = pTemp;
this.pc = nPoly;
}}, "~N,JU.P4,~A,JU.Lst,~A,JU.BS,JU.MeshSurface,~B,~B,~N,~B");
c$.setPoint = Clazz.defineMethod (c$, "setPoint", 
 function (fracs, i, i0, i1) {
return (fracs[i] == 0 ? i0 : fracs[i] == 1 ? i1 : -1);
}, "~A,~N,~N,~N");
Clazz.defineMethod (c$, "getDE", 
 function (fracs, fD, i1, i2, i3, toss23) {
this.iD = JU.MeshSurface.setPoint (fracs, fD, i1, i2);
this.iE = JU.MeshSurface.setPoint (fracs, 1 - fD, i1, i3);
if (this.iD == i1 && this.iE == i1) {
this.doClear = toss23;
this.doCap = false;
return false;
}if (this.iD == i2 && this.iE == i3) {
this.doClear = !toss23;
return false;
}if (this.iD == i1 || this.iE == i1) {
this.doClear = toss23;
if (this.iD < 0) {
this.iD = (toss23 ? i2 : i3);
} else if (this.iE < 0) {
this.iE = (toss23 ? i3 : i2);
}return this.doCap;
}this.doGhost = false;
return true;
}, "~A,~N,~N,~N,~N,~B");
c$.checkSlab = Clazz.defineMethod (c$, "checkSlab", 
 function (tokType, v, val, distance, plane, ptCenters, bs) {
var d;
switch (tokType) {
case 3:
return (val >= 0 && bs.get (Clazz.floatToInt (val)) ? 1 : -1);
case 32:
d = distance - val;
break;
case 64:
d = val - distance;
break;
case 135266319:
d = JU.Measure.distanceToPlane (plane, v);
break;
case 1276118018:
d = JU.MeshSurface.minDist (v, ptCenters) - distance;
break;
default:
d = -JU.MeshSurface.minDist (v, ptCenters) - distance;
break;
}
return (Math.abs (d) < 0.0001 ? 0 : d);
}, "~N,JU.T3,~N,~N,JU.P4,~A,JU.BS");
c$.minDist = Clazz.defineMethod (c$, "minDist", 
 function (pt, ptCenters) {
var dmin = 2147483647;
for (var i = ptCenters.length; --i >= 0; ) {
var d = ptCenters[i].distance (pt);
if (d < dmin) dmin = d;
}
return dmin;
}, "JU.T3,~A");
Clazz.defineMethod (c$, "interpolateSphere", 
 function (v1, v2, d1, d2, absD, val1, val2, values, fracs, i) {
return JU.MeshSurface.interpolateFraction (v1, v2, JU.MeshSurface.getSphericalInterpolationFraction (absD, d1, d2, v1.distance (v2)), val1, val2, values, fracs, i);
}, "JU.T3,JU.T3,~N,~N,~N,~N,~N,~A,~A,~N");
c$.interpolatePoint = Clazz.defineMethod (c$, "interpolatePoint", 
 function (v1, v2, d1, d2, val1, val2, values, fracs, i) {
return JU.MeshSurface.interpolateFraction (v1, v2, d1 / (d1 + d2), val1, val2, values, fracs, i);
}, "JU.T3,JU.T3,~N,~N,~N,~N,~A,~A,~N");
c$.interpolateFraction = Clazz.defineMethod (c$, "interpolateFraction", 
 function (v1, v2, f, val1, val2, values, fracs, i) {
if (f < 0.0001) f = 0;
 else if (f > 0.9999) f = 1;
fracs[i] = f;
values[i] = (val2 - val1) * f + val1;
return JU.P3.new3 (v1.x + (v2.x - v1.x) * f, v1.y + (v2.y - v1.y) * f, v1.z + (v2.z - v1.z) * f);
}, "JU.T3,JU.T3,~N,~N,~N,~A,~A,~N");
c$.getSphericalInterpolationFraction = Clazz.defineMethod (c$, "getSphericalInterpolationFraction", 
function (r, valueA, valueB, d) {
var ra = Math.abs (r + valueA) / d;
var rb = Math.abs (r + valueB) / d;
r /= d;
var ra2 = ra * ra;
var q = ra2 - rb * rb + 1;
var p = 4 * (r * r - ra2);
var factor = (ra < rb ? 1 : -1);
return (((q) + factor * Math.sqrt (q * q + p)) / 2);
}, "~N,~N,~N,~N");
c$.getSphereData = Clazz.defineMethod (c$, "getSphereData", 
function (lvl) {
JU.Geodesic.createGeodesic (lvl);
var vertexCount = JU.Geodesic.getVertexCount (lvl);
var f = JU.Geodesic.getFaceVertexes (lvl);
var nFaces = Clazz.doubleToInt (f.length / 3);
var faces = JU.AU.newInt2 (nFaces);
for (var i = 0, fpt = 0; i < nFaces; i++) {
faces[i] = [f[fpt++], f[fpt++], f[fpt++]];
}
var vectors =  new Array (vertexCount);
for (var i = 0; i < vertexCount; i++) vectors[i] = JU.Geodesic.getVertexVector (i);

return JU.MeshSurface.newMesh (true, vectors, 0, faces, vectors, 0);
}, "~N");
Clazz.defineStatics (c$,
"SEED_COUNT", 25);
});
