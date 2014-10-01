Clazz.declarePackage ("J.shapespecial");
Clazz.load (["J.shape.AtomShape", "JU.P3", "$.V3"], "J.shapespecial.Polyhedra", ["java.lang.Boolean", "JU.AU", "$.BS", "$.Measure", "$.P3i", "$.SB", "J.c.PAL", "J.shapespecial.Polyhedron", "JU.Logger", "$.Normix"], function () {
c$ = Clazz.decorateAsClass (function () {
this.otherAtoms = null;
this.polyhedronCount = 0;
this.polyhedrons = null;
this.drawEdges = 0;
this.radius = 0;
this.nVertices = 0;
this.faceCenterOffset = 0;
this.distanceFactor = 0;
this.isCollapsed = false;
this.iHaveCenterBitSet = false;
this.bondedOnly = false;
this.haveBitSetVertices = false;
this.centers = null;
this.bsVertices = null;
this.bsVertexCount = null;
this.normixesT = null;
this.planesT = null;
this.bsTemp = null;
this.align1 = null;
this.align2 = null;
this.vAB = null;
this.vAC = null;
Clazz.instantialize (this, arguments);
}, J.shapespecial, "Polyhedra", J.shape.AtomShape);
Clazz.prepareFields (c$, function () {
this.otherAtoms =  new Array (151);
this.polyhedrons =  new Array (32);
this.normixesT =  Clazz.newShortArray (150, 0);
this.planesT =  Clazz.newByteArray (450, 0);
this.align1 =  new JU.V3 ();
this.align2 =  new JU.V3 ();
this.vAB =  new JU.V3 ();
this.vAC =  new JU.V3 ();
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if ("init" === propertyName) {
this.faceCenterOffset = 0.25;
this.distanceFactor = 1.85;
this.radius = 0.0;
this.nVertices = 0;
this.bsVertices = null;
this.centers = null;
this.bsVertexCount =  new JU.BS ();
this.bondedOnly = this.isCollapsed = this.iHaveCenterBitSet = false;
this.haveBitSetVertices = false;
if (Boolean.TRUE === value) this.drawEdges = 0;
return;
}if ("generate" === propertyName) {
if (!this.iHaveCenterBitSet) {
this.centers = bs;
this.iHaveCenterBitSet = true;
}this.deletePolyhedra ();
this.buildPolyhedra ();
return;
}if ("collapsed" === propertyName) {
this.isCollapsed = (value).booleanValue ();
return;
}if ("nVertices" === propertyName) {
this.nVertices = (value).intValue ();
this.bsVertexCount.set (this.nVertices);
return;
}if ("centers" === propertyName) {
this.centers = value;
this.iHaveCenterBitSet = true;
return;
}if ("to" === propertyName) {
this.bsVertices = value;
return;
}if ("toBitSet" === propertyName) {
this.bsVertices = value;
this.haveBitSetVertices = true;
return;
}if ("faceCenterOffset" === propertyName) {
this.faceCenterOffset = (value).floatValue ();
return;
}if ("distanceFactor" === propertyName) {
this.distanceFactor = (value).floatValue ();
return;
}if ("bonds" === propertyName) {
this.bondedOnly = true;
return;
}if ("delete" === propertyName) {
if (!this.iHaveCenterBitSet) this.centers = bs;
this.deletePolyhedra ();
return;
}if ("on" === propertyName) {
if (!this.iHaveCenterBitSet) this.centers = bs;
this.setVisible (true);
return;
}if ("off" === propertyName) {
if (!this.iHaveCenterBitSet) this.centers = bs;
this.setVisible (false);
return;
}if ("noedges" === propertyName) {
this.drawEdges = 0;
return;
}if ("edges" === propertyName) {
this.drawEdges = 1;
return;
}if ("frontedges" === propertyName) {
this.drawEdges = 2;
return;
}if (propertyName.indexOf ("color") == 0) {
bs = ("colorThis" === propertyName && this.iHaveCenterBitSet ? this.centers : this.andBitSet (bs));
propertyName = "color";
}if (propertyName.indexOf ("translucency") == 0) {
bs = ("translucentThis".equals (value) && this.iHaveCenterBitSet ? this.centers : this.andBitSet (bs));
if (value.equals ("translucentThis")) value = "translucent";
}if ("token" === propertyName) {
this.setLighting ((value).intValue () == 1073741964, bs);
return;
}if ("radius" === propertyName) {
this.radius = (value).floatValue ();
return;
}if (propertyName === "deleteModelAtoms") {
var modelIndex = ((value)[2])[0];
for (var i = this.polyhedronCount; --i >= 0; ) {
if (this.polyhedrons[i].modelIndex == modelIndex) {
this.polyhedronCount--;
this.polyhedrons = JU.AU.deleteElements (this.polyhedrons, i, 1);
} else if (this.polyhedrons[i].modelIndex > modelIndex) {
this.polyhedrons[i].modelIndex--;
}}
}this.setPropAS (propertyName, value, bs);
}, "~S,~O,JU.BS");
Clazz.defineMethod (c$, "setLighting", 
 function (isFullyLit, bs) {
for (var i = this.polyhedronCount; --i >= 0; ) if (bs.get (this.polyhedrons[i].centralAtom.i)) {
var normixes = this.polyhedrons[i].normixes;
this.polyhedrons[i].isFullyLit = isFullyLit;
for (var j = normixes.length; --j >= 0; ) {
if (normixes[j] < 0 != isFullyLit) normixes[j] = ~normixes[j];
}
}
}, "~B,JU.BS");
Clazz.defineMethod (c$, "andBitSet", 
 function (bs) {
var bsCenters =  new JU.BS ();
for (var i = this.polyhedronCount; --i >= 0; ) bsCenters.set (this.polyhedrons[i].centralAtom.i);

bsCenters.and (bs);
return bsCenters;
}, "JU.BS");
Clazz.defineMethod (c$, "deletePolyhedra", 
 function () {
var newCount = 0;
var pid = J.c.PAL.pidOf (null);
for (var i = 0; i < this.polyhedronCount; ++i) {
var p = this.polyhedrons[i];
var iAtom = p.centralAtom.i;
if (this.centers.get (iAtom)) this.setColixAndPalette (0, pid, iAtom);
 else this.polyhedrons[newCount++] = p;
}
for (var i = newCount; i < this.polyhedronCount; ++i) this.polyhedrons[i] = null;

this.polyhedronCount = newCount;
});
Clazz.defineMethod (c$, "setVisible", 
 function (visible) {
for (var i = this.polyhedronCount; --i >= 0; ) {
var p = this.polyhedrons[i];
if (p != null && this.centers.get (p.centralAtom.i)) p.visible = visible;
}
}, "~B");
Clazz.defineMethod (c$, "buildPolyhedra", 
 function () {
var useBondAlgorithm = this.radius == 0 || this.bondedOnly;
var iter = this.ms.getSelectedAtomIterator (null, false, false, false, false);
for (var i = this.centers.nextSetBit (0); i >= 0; i = this.centers.nextSetBit (i + 1)) {
var p = (this.haveBitSetVertices ? this.constructBitSetPolyhedron (i) : useBondAlgorithm ? this.constructBondsPolyhedron (i) : this.constructRadiusPolyhedron (i, iter));
if (p != null) {
if (this.polyhedronCount == this.polyhedrons.length) this.polyhedrons = JU.AU.doubleLength (this.polyhedrons);
this.polyhedrons[this.polyhedronCount++] = p;
}if (this.haveBitSetVertices) break;
}
iter.release ();
});
Clazz.defineMethod (c$, "constructBondsPolyhedron", 
 function (atomIndex) {
var atom = this.atoms[atomIndex];
var bonds = atom.getBonds ();
if (bonds == null) return null;
var bondCount = 0;
for (var i = bonds.length; --i >= 0; ) {
var bond = bonds[i];
var otherAtom = bond.getAtom1 () === atom ? bond.getAtom2 () : bond.getAtom1 ();
if (this.bsVertices != null && !this.bsVertices.get (otherAtom.i)) continue;
if (this.radius > 0 && bond.getAtom1 ().distance (bond.getAtom2 ()) > this.radius) continue;
this.otherAtoms[bondCount++] = otherAtom;
if (bondCount == 150) break;
}
if (bondCount < 3 || this.nVertices > 0 && !this.bsVertexCount.get (bondCount)) return null;
return this.validatePolyhedronNew (atom, bondCount, this.otherAtoms);
}, "~N");
Clazz.defineMethod (c$, "constructBitSetPolyhedron", 
 function (atomIndex) {
var otherAtomCount = 0;
for (var i = this.bsVertices.nextSetBit (0); i >= 0; i = this.bsVertices.nextSetBit (i + 1)) this.otherAtoms[otherAtomCount++] = this.atoms[i];

return this.validatePolyhedronNew (this.atoms[atomIndex], otherAtomCount, this.otherAtoms);
}, "~N");
Clazz.defineMethod (c$, "constructRadiusPolyhedron", 
 function (atomIndex, iter) {
var atom = this.atoms[atomIndex];
var otherAtomCount = 0;
this.vwr.setIteratorForAtom (iter, atomIndex, this.radius);
while (iter.hasNext ()) {
var other = this.atoms[iter.next ()];
if (this.bsVertices != null && !this.bsVertices.get (other.i) || atom.distance (other) > this.radius) continue;
if (other.getAlternateLocationID () != atom.getAlternateLocationID () && (other.getAlternateLocationID ()).charCodeAt (0) != 0 && (atom.getAlternateLocationID ()).charCodeAt (0) != 0) continue;
if (otherAtomCount == 150) break;
this.otherAtoms[otherAtomCount++] = other;
}
if (otherAtomCount < 3 || this.nVertices > 0 && !this.bsVertexCount.get (otherAtomCount)) return null;
return this.validatePolyhedronNew (atom, otherAtomCount, this.otherAtoms);
}, "~N,J.api.AtomIndexIterator");
Clazz.defineMethod (c$, "validatePolyhedronNew", 
 function (centralAtom, vertexCount, otherAtoms) {
var normal =  new JU.V3 ();
var planeCount = 0;
var ipt = 0;
var ptCenter = vertexCount;
var nPoints = ptCenter + 1;
var distMax = 0;
var dAverage = 0;
var points =  new Array (450);
points[ptCenter] = otherAtoms[ptCenter] = centralAtom;
for (var i = 0; i < ptCenter; i++) {
points[i] = otherAtoms[i];
dAverage += points[ptCenter].distance (points[i]);
}
dAverage = dAverage / ptCenter;
var factor = this.distanceFactor;
var bs = JU.BS.newN (ptCenter);
var isOK = (dAverage == 0);
while (!isOK && factor < 10.0) {
distMax = dAverage * factor;
bs.setBits (0, ptCenter);
for (var i = 0; i < ptCenter - 2; i++) for (var j = i + 1; j < ptCenter - 1; j++) {
if (points[i].distance (points[j]) > distMax) continue;
for (var k = j + 1; k < ptCenter; k++) {
if (points[i].distance (points[k]) > distMax || points[j].distance (points[k]) > distMax) continue;
bs.clear (i);
bs.clear (j);
bs.clear (k);
}
}

isOK = true;
for (var i = 0; i < ptCenter; i++) if (bs.get (i)) {
isOK = false;
factor *= 1.05;
if (JU.Logger.debugging) {
JU.Logger.debug ("Polyhedra distanceFactor for " + ptCenter + " atoms increased to " + factor + " in order to include " + (otherAtoms[i]).getInfo ());
}break;
}
}
var faceCatalog = "";
var facetCatalog = "";
for (var i = 0; i < ptCenter - 2; i++) for (var j = i + 1; j < ptCenter - 1; j++) for (var k = j + 1; k < ptCenter; k++) if (this.isPlanar (points[i], points[j], points[k], points[ptCenter])) faceCatalog += this.faceId (i, j, k);



for (var j = 0; j < ptCenter - 1; j++) for (var k = j + 1; k < ptCenter; k++) {
if (this.isAligned (points[j], points[k], points[ptCenter])) facetCatalog += this.faceId (j, k, -1);
}

var ptRef =  new JU.P3 ();
if (this.bsTemp == null) this.bsTemp = JU.Normix.newVertexBitSet ();
for (var i = 0; i < ptCenter - 2; i++) for (var j = i + 1; j < ptCenter - 1; j++) {
if (points[i].distance (points[j]) > distMax) continue;
for (var k = j + 1; k < ptCenter; k++) {
if (points[i].distance (points[k]) > distMax || points[j].distance (points[k]) > distMax) continue;
if (planeCount >= 147) {
JU.Logger.error ("Polyhedron error: maximum face(147) -- reduce RADIUS or DISTANCEFACTOR");
return null;
}if (nPoints >= 150) {
JU.Logger.error ("Polyhedron error: maximum vertex count(150) -- reduce RADIUS");
return null;
}var isFlat = (faceCatalog.indexOf (this.faceId (i, j, k)) >= 0);
var isWindingOK = (isFlat ? JU.Measure.getNormalFromCenter (J.shapespecial.Polyhedra.randomPoint, points[i], points[j], points[k], false, normal) : JU.Measure.getNormalFromCenter (points[ptCenter], points[i], points[j], points[k], true, normal));
normal.scale (this.isCollapsed && !isFlat ? this.faceCenterOffset : 0.001);
var nRef = nPoints;
ptRef.setT (points[ptCenter]);
if (this.isCollapsed && !isFlat) {
points[nPoints] = JU.P3.newP (points[ptCenter]);
points[nPoints].add (normal);
otherAtoms[nPoints] = points[nPoints];
} else if (isFlat) {
ptRef.sub (normal);
nRef = ptCenter;
}var facet;
facet = this.faceId (i, j, -1);
if (this.isCollapsed || isFlat && facetCatalog.indexOf (facet) < 0) {
facetCatalog += facet;
this.planesT[ipt++] = (isWindingOK ? i : j);
this.planesT[ipt++] = (isWindingOK ? j : i);
this.planesT[ipt++] = nRef;
JU.Measure.getNormalFromCenter (points[k], points[i], points[j], ptRef, false, normal);
this.normixesT[planeCount++] = (isFlat ? JU.Normix.get2SidedNormix (normal, this.bsTemp) : JU.Normix.getNormixV (normal, this.bsTemp));
}facet = this.faceId (i, k, -1);
if (this.isCollapsed || isFlat && facetCatalog.indexOf (facet) < 0) {
facetCatalog += facet;
this.planesT[ipt++] = (isWindingOK ? i : k);
this.planesT[ipt++] = nRef;
this.planesT[ipt++] = (isWindingOK ? k : i);
JU.Measure.getNormalFromCenter (points[j], points[i], ptRef, points[k], false, normal);
this.normixesT[planeCount++] = (isFlat ? JU.Normix.get2SidedNormix (normal, this.bsTemp) : JU.Normix.getNormixV (normal, this.bsTemp));
}facet = this.faceId (j, k, -1);
if (this.isCollapsed || isFlat && facetCatalog.indexOf (facet) < 0) {
facetCatalog += facet;
this.planesT[ipt++] = nRef;
this.planesT[ipt++] = (isWindingOK ? j : k);
this.planesT[ipt++] = (isWindingOK ? k : j);
JU.Measure.getNormalFromCenter (points[i], ptRef, points[j], points[k], false, normal);
this.normixesT[planeCount++] = (isFlat ? JU.Normix.get2SidedNormix (normal, this.bsTemp) : JU.Normix.getNormixV (normal, this.bsTemp));
}if (!isFlat) {
if (this.isCollapsed) {
nPoints++;
} else {
this.planesT[ipt++] = (isWindingOK ? i : j);
this.planesT[ipt++] = (isWindingOK ? j : i);
this.planesT[ipt++] = k;
this.normixesT[planeCount++] = JU.Normix.getNormixV (normal, this.bsTemp);
}}}
}

return  new J.shapespecial.Polyhedron (centralAtom, ptCenter, nPoints, planeCount, otherAtoms, this.normixesT, this.planesT, this.isCollapsed, this.faceCenterOffset, this.distanceFactor);
}, "JM.Atom,~N,~A");
Clazz.defineMethod (c$, "faceId", 
 function (i, j, k) {
return (JU.P3i.new3 (i, j, k)).toString ();
}, "~N,~N,~N");
Clazz.defineMethod (c$, "isAligned", 
 function (pt1, pt2, pt3) {
this.align1.sub2 (pt1, pt3);
this.align2.sub2 (pt2, pt3);
var angle = this.align1.angle (this.align2);
return (angle < 0.01 || angle > 3.13);
}, "JU.P3,JU.P3,JU.P3");
Clazz.defineMethod (c$, "isPlanar", 
 function (pt1, pt2, pt3, ptX) {
var norm =  new JU.V3 ();
var w = JU.Measure.getNormalThroughPoints (pt1, pt2, pt3, norm, this.vAB, this.vAC);
var d = JU.Measure.distanceToPlaneV (norm, w, ptX);
return (Math.abs (d) < J.shapespecial.Polyhedra.minDistanceForPlanarity);
}, "JU.P3,JU.P3,JU.P3,JU.P3");
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bsModels) {
for (var i = this.polyhedronCount; --i >= 0; ) {
var p = this.polyhedrons[i];
p.visibilityFlags = (p.visible && bsModels.get (p.modelIndex) && !this.ms.isAtomHidden (p.centralAtom.i) ? this.vf : 0);
}
}, "JU.BS");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
if (this.polyhedronCount == 0) return "";
var s =  new JU.SB ();
for (var i = 0; i < this.polyhedronCount; i++) s.append (this.polyhedrons[i].getState ());

if (this.drawEdges == 2) J.shape.Shape.appendCmd (s, "polyhedra frontedges");
 else if (this.drawEdges == 1) J.shape.Shape.appendCmd (s, "polyhedra edges");
s.append (this.vwr.getAtomShapeState (this));
return s.toString ();
});
Clazz.defineStatics (c$,
"DEFAULT_DISTANCE_FACTOR", 1.85,
"DEFAULT_FACECENTEROFFSET", 0.25,
"EDGES_NONE", 0,
"EDGES_ALL", 1,
"EDGES_FRONT", 2,
"MAX_VERTICES", 150,
"FACE_COUNT_MAX", 147);
c$.randomPoint = c$.prototype.randomPoint = JU.P3.new3 (3141, 2718, 1414);
Clazz.defineStatics (c$,
"minDistanceForPlanarity", 0.1);
});
