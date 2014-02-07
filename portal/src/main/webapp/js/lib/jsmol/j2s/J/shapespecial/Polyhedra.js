Clazz.declarePackage ("J.shapespecial");
Clazz.load (["J.shape.AtomShape", "J.util.P3", "$.V3"], "J.shapespecial.Polyhedra", ["java.lang.Boolean", "J.constant.EnumPalette", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.Escape", "$.Logger", "$.Measure", "$.Normix", "$.P3i", "$.SB"], function () {
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
if (!Clazz.isClassDefined ("J.shapespecial.Polyhedra.Polyhedron")) {
J.shapespecial.Polyhedra.$Polyhedra$Polyhedron$ ();
}
Clazz.instantialize (this, arguments);
}, J.shapespecial, "Polyhedra", J.shape.AtomShape);
Clazz.prepareFields (c$, function () {
this.otherAtoms =  new Array (151);
this.polyhedrons =  new Array (32);
this.normixesT =  Clazz.newShortArray (150, 0);
this.planesT =  Clazz.newByteArray (450, 0);
this.align1 =  new J.util.V3 ();
this.align2 =  new J.util.V3 ();
this.vAB =  new J.util.V3 ();
this.vAC =  new J.util.V3 ();
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
this.bsVertexCount =  new J.util.BS ();
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
this.polyhedrons = J.util.ArrayUtil.deleteElements (this.polyhedrons, i, 1);
} else if (this.polyhedrons[i].modelIndex > modelIndex) {
this.polyhedrons[i].modelIndex--;
}}
}this.setPropAS (propertyName, value, bs);
}, "~S,~O,J.util.BS");
$_M(c$, "setLighting", 
($fz = function (isFullyLit, bs) {
for (var i = this.polyhedronCount; --i >= 0; ) if (bs.get (this.polyhedrons[i].centralAtom.getIndex ())) {
var normixes = this.polyhedrons[i].normixes;
this.polyhedrons[i].isFullyLit = isFullyLit;
for (var j = normixes.length; --j >= 0; ) {
if (normixes[j] < 0 != isFullyLit) normixes[j] = ~normixes[j];
}
}
}, $fz.isPrivate = true, $fz), "~B,J.util.BS");
$_M(c$, "andBitSet", 
($fz = function (bs) {
var bsCenters =  new J.util.BS ();
for (var i = this.polyhedronCount; --i >= 0; ) bsCenters.set (this.polyhedrons[i].centralAtom.getIndex ());

bsCenters.and (bs);
return bsCenters;
}, $fz.isPrivate = true, $fz), "J.util.BS");
$_M(c$, "deletePolyhedra", 
($fz = function () {
var newCount = 0;
var pid = J.constant.EnumPalette.pidOf (null);
for (var i = 0; i < this.polyhedronCount; ++i) {
var p = this.polyhedrons[i];
var iAtom = p.centralAtom.getIndex ();
if (this.centers.get (iAtom)) this.setColixAndPalette (0, pid, iAtom);
 else this.polyhedrons[newCount++] = p;
}
for (var i = newCount; i < this.polyhedronCount; ++i) this.polyhedrons[i] = null;

this.polyhedronCount = newCount;
}, $fz.isPrivate = true, $fz));
$_M(c$, "setVisible", 
($fz = function (visible) {
for (var i = this.polyhedronCount; --i >= 0; ) {
var p = this.polyhedrons[i];
if (p != null && this.centers.get (p.centralAtom.getIndex ())) p.visible = visible;
}
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "buildPolyhedra", 
($fz = function () {
var useBondAlgorithm = this.radius == 0 || this.bondedOnly;
var iter = this.modelSet.getSelectedAtomIterator (null, false, false, false, false);
for (var i = this.centers.nextSetBit (0); i >= 0; i = this.centers.nextSetBit (i + 1)) {
var p = (this.haveBitSetVertices ? this.constructBitSetPolyhedron (i) : useBondAlgorithm ? this.constructBondsPolyhedron (i) : this.constructRadiusPolyhedron (i, iter));
if (p != null) {
if (this.polyhedronCount == this.polyhedrons.length) this.polyhedrons = J.util.ArrayUtil.doubleLength (this.polyhedrons);
this.polyhedrons[this.polyhedronCount++] = p;
}if (this.haveBitSetVertices) break;
}
iter.release ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "constructBondsPolyhedron", 
($fz = function (atomIndex) {
var atom = this.atoms[atomIndex];
var bonds = atom.getBonds ();
if (bonds == null) return null;
var bondCount = 0;
for (var i = bonds.length; --i >= 0; ) {
var bond = bonds[i];
var otherAtom = bond.getAtom1 () === atom ? bond.getAtom2 () : bond.getAtom1 ();
if (this.bsVertices != null && !this.bsVertices.get (otherAtom.getIndex ())) continue;
if (this.radius > 0 && bond.getAtom1 ().distance (bond.getAtom2 ()) > this.radius) continue;
this.otherAtoms[bondCount++] = otherAtom;
if (bondCount == 150) break;
}
if (bondCount < 3 || this.nVertices > 0 && !this.bsVertexCount.get (bondCount)) return null;
return this.validatePolyhedronNew (atom, bondCount, this.otherAtoms);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "constructBitSetPolyhedron", 
($fz = function (atomIndex) {
var otherAtomCount = 0;
for (var i = this.bsVertices.nextSetBit (0); i >= 0; i = this.bsVertices.nextSetBit (i + 1)) this.otherAtoms[otherAtomCount++] = this.atoms[i];

return this.validatePolyhedronNew (this.atoms[atomIndex], otherAtomCount, this.otherAtoms);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "constructRadiusPolyhedron", 
($fz = function (atomIndex, iter) {
var atom = this.atoms[atomIndex];
var otherAtomCount = 0;
this.viewer.setIteratorForAtom (iter, atomIndex, this.radius);
while (iter.hasNext ()) {
var other = this.atoms[iter.next ()];
if (this.bsVertices != null && !this.bsVertices.get (other.getIndex ()) || atom.distance (other) > this.radius) continue;
if (other.getAlternateLocationID () != atom.getAlternateLocationID () && (other.getAlternateLocationID ()).charCodeAt (0) != 0 && (atom.getAlternateLocationID ()).charCodeAt (0) != 0) continue;
if (otherAtomCount == 150) break;
this.otherAtoms[otherAtomCount++] = other;
}
if (otherAtomCount < 3 || this.nVertices > 0 && !this.bsVertexCount.get (otherAtomCount)) return null;
return this.validatePolyhedronNew (atom, otherAtomCount, this.otherAtoms);
}, $fz.isPrivate = true, $fz), "~N,J.api.AtomIndexIterator");
$_M(c$, "validatePolyhedronNew", 
($fz = function (centralAtom, vertexCount, otherAtoms) {
var normal =  new J.util.V3 ();
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
var bs = J.util.BSUtil.newBitSet (ptCenter);
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
if (J.util.Logger.debugging) {
J.util.Logger.debug ("Polyhedra distanceFactor for " + ptCenter + " atoms increased to " + factor + " in order to include " + (otherAtoms[i]).getInfo ());
}break;
}
}
var faceCatalog = "";
var facetCatalog = "";
for (var i = 0; i < ptCenter - 2; i++) for (var j = i + 1; j < ptCenter - 1; j++) for (var k = j + 1; k < ptCenter; k++) if (this.isPlanar (points[i], points[j], points[k], points[ptCenter])) faceCatalog += this.faceId (i, j, k);



for (var j = 0; j < ptCenter - 1; j++) for (var k = j + 1; k < ptCenter; k++) {
if (this.isAligned (points[j], points[k], points[ptCenter])) facetCatalog += this.faceId (j, k, -1);
}

var ptRef =  new J.util.P3 ();
if (this.bsTemp == null) this.bsTemp = J.util.Normix.newVertexBitSet ();
for (var i = 0; i < ptCenter - 2; i++) for (var j = i + 1; j < ptCenter - 1; j++) {
if (points[i].distance (points[j]) > distMax) continue;
for (var k = j + 1; k < ptCenter; k++) {
System.out.println ("checking poly " + i + " " + j + " " + k);
System.out.println ("checking poly " + points[i] + " " + points[j] + " " + points[k]);
if (points[i].distance (points[k]) > distMax || points[j].distance (points[k]) > distMax) continue;
System.out.println ("checking poly " + i + " " + j + " " + k + " ok ");
if (planeCount >= 147) {
J.util.Logger.error ("Polyhedron error: maximum face(147) -- reduce RADIUS or DISTANCEFACTOR");
return null;
}if (nPoints >= 150) {
J.util.Logger.error ("Polyhedron error: maximum vertex count(150) -- reduce RADIUS");
return null;
}var isFlat = (faceCatalog.indexOf (this.faceId (i, j, k)) >= 0);
var isWindingOK = (isFlat ? J.util.Measure.getNormalFromCenter (J.shapespecial.Polyhedra.randomPoint, points[i], points[j], points[k], false, normal) : J.util.Measure.getNormalFromCenter (points[ptCenter], points[i], points[j], points[k], true, normal));
normal.scale (this.isCollapsed && !isFlat ? this.faceCenterOffset : 0.001);
var nRef = nPoints;
ptRef.setT (points[ptCenter]);
if (this.isCollapsed && !isFlat) {
points[nPoints] = J.util.P3.newP (points[ptCenter]);
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
J.util.Measure.getNormalFromCenter (points[k], points[i], points[j], ptRef, false, normal);
this.normixesT[planeCount++] = (isFlat ? J.util.Normix.get2SidedNormix (normal, this.bsTemp) : J.util.Normix.getNormixV (normal, this.bsTemp));
}facet = this.faceId (i, k, -1);
if (this.isCollapsed || isFlat && facetCatalog.indexOf (facet) < 0) {
facetCatalog += facet;
this.planesT[ipt++] = (isWindingOK ? i : k);
this.planesT[ipt++] = nRef;
this.planesT[ipt++] = (isWindingOK ? k : i);
J.util.Measure.getNormalFromCenter (points[j], points[i], ptRef, points[k], false, normal);
this.normixesT[planeCount++] = (isFlat ? J.util.Normix.get2SidedNormix (normal, this.bsTemp) : J.util.Normix.getNormixV (normal, this.bsTemp));
}facet = this.faceId (j, k, -1);
if (this.isCollapsed || isFlat && facetCatalog.indexOf (facet) < 0) {
facetCatalog += facet;
this.planesT[ipt++] = nRef;
this.planesT[ipt++] = (isWindingOK ? j : k);
this.planesT[ipt++] = (isWindingOK ? k : j);
J.util.Measure.getNormalFromCenter (points[i], ptRef, points[j], points[k], false, normal);
this.normixesT[planeCount++] = (isFlat ? J.util.Normix.get2SidedNormix (normal, this.bsTemp) : J.util.Normix.getNormixV (normal, this.bsTemp));
}if (!isFlat) {
if (this.isCollapsed) {
nPoints++;
} else {
this.planesT[ipt++] = (isWindingOK ? i : j);
this.planesT[ipt++] = (isWindingOK ? j : i);
this.planesT[ipt++] = k;
this.normixesT[planeCount++] = J.util.Normix.getNormixV (normal, this.bsTemp);
}}}
}

return Clazz.innerTypeInstance (J.shapespecial.Polyhedra.Polyhedron, this, null, centralAtom, ptCenter, nPoints, planeCount, otherAtoms, this.normixesT, this.planesT);
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,~N,~A");
$_M(c$, "faceId", 
($fz = function (i, j, k) {
return (J.util.P3i.new3 (i, j, k)).toString ();
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "isAligned", 
($fz = function (pt1, pt2, pt3) {
this.align1.sub2 (pt1, pt3);
this.align2.sub2 (pt2, pt3);
var angle = this.align1.angle (this.align2);
return (angle < 0.01 || angle > 3.13);
}, $fz.isPrivate = true, $fz), "J.util.P3,J.util.P3,J.util.P3");
$_M(c$, "isPlanar", 
($fz = function (pt1, pt2, pt3, ptX) {
var norm =  new J.util.V3 ();
var w = J.util.Measure.getNormalThroughPoints (pt1, pt2, pt3, norm, this.vAB, this.vAC);
var d = J.util.Measure.distanceToPlaneV (norm, w, ptX);
return (Math.abs (d) < J.shapespecial.Polyhedra.minDistanceForPlanarity);
}, $fz.isPrivate = true, $fz), "J.util.P3,J.util.P3,J.util.P3,J.util.P3");
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bs) {
for (var i = this.polyhedronCount; --i >= 0; ) {
var p = this.polyhedrons[i];
p.visibilityFlags = (p.visible && bs.get (p.modelIndex) && !this.modelSet.isAtomHidden (p.centralAtom.getIndex ()) ? this.myVisibilityFlag : 0);
}
}, "J.util.BS");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
if (this.polyhedronCount == 0) return "";
var s =  new J.util.SB ();
for (var i = 0; i < this.polyhedronCount; i++) s.append (this.polyhedrons[i].getState ());

if (this.drawEdges == 2) J.shape.Shape.appendCmd (s, "polyhedra frontedges");
 else if (this.drawEdges == 1) J.shape.Shape.appendCmd (s, "polyhedra edges");
s.append (this.viewer.getAtomShapeState (this));
return s.toString ();
});
c$.$Polyhedra$Polyhedron$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.modelIndex = 0;
this.centralAtom = null;
this.vertices = null;
this.ptCenter = 0;
this.visible = false;
this.normixes = null;
this.planes = null;
this.visibilityFlags = 0;
this.collapsed = false;
this.myFaceCenterOffset = 0;
this.myDistanceFactor = 0;
this.isFullyLit = false;
Clazz.instantialize (this, arguments);
}, J.shapespecial.Polyhedra, "Polyhedron");
Clazz.makeConstructor (c$, 
function (a, b, c, d, e, f, g) {
this.collapsed = this.b$["J.shapespecial.Polyhedra"].isCollapsed;
this.centralAtom = a;
this.modelIndex = a.getModelIndex ();
this.ptCenter = b;
this.vertices =  new Array (c);
this.visible = true;
this.normixes =  Clazz.newShortArray (d, 0);
this.planes =  Clazz.newByteArray (d * 3, 0);
this.myFaceCenterOffset = this.b$["J.shapespecial.Polyhedra"].faceCenterOffset;
this.myDistanceFactor = this.b$["J.shapespecial.Polyhedra"].distanceFactor;
for (var h = c; --h >= 0; ) this.vertices[h] = e[h];

for (var i = d; --i >= 0; ) this.normixes[i] = f[i];

for (var j = d * 3; --j >= 0; ) this.planes[j] = g[j];

}, "J.modelset.Atom,~N,~N,~N,~A,~A,~A");
$_M(c$, "getState", 
function () {
var a =  new J.util.BS ();
for (var b = 0; b < this.ptCenter; b++) a.set ((this.vertices[b]).getIndex ());

return "  polyhedra ({" + this.centralAtom.getIndex () + "}) to " + J.util.Escape.eBS (a) + (this.collapsed ? " collapsed" : "") + " distanceFactor " + this.myDistanceFactor + " faceCenterOffset " + this.myFaceCenterOffset + (this.isFullyLit ? " fullyLit" : "") + ";" + (this.visible ? "" : "polyhedra off;") + "\n";
});
c$ = Clazz.p0p ();
};
Clazz.defineStatics (c$,
"DEFAULT_DISTANCE_FACTOR", 1.85,
"DEFAULT_FACECENTEROFFSET", 0.25,
"EDGES_NONE", 0,
"EDGES_ALL", 1,
"EDGES_FRONT", 2,
"MAX_VERTICES", 150,
"FACE_COUNT_MAX", 147);
c$.randomPoint = c$.prototype.randomPoint = J.util.P3.new3 (3141, 2718, 1414);
Clazz.defineStatics (c$,
"minDistanceForPlanarity", 0.1);
});
