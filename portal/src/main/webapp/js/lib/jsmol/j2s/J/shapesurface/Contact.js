Clazz.declarePackage ("J.shapesurface");
Clazz.load (["J.shapesurface.Isosurface", "J.atomdata.RadiusData", "J.constant.EnumVdw", "J.util.P3", "$.V3"], "J.shapesurface.Contact", ["java.lang.Boolean", "$.Double", "$.Float", "java.util.Hashtable", "J.atomdata.AtomData", "J.constant.EnumHBondType", "J.jvxl.data.MeshData", "$.VolumeData", "J.script.T", "J.util.BS", "$.BSUtil", "$.ColorUtil", "$.ContactPair", "$.Escape", "$.JmolList", "$.Logger", "$.Measure", "$.MeshSurface"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atoms = null;
this.atomCount = 0;
this.minData = 0;
this.maxData = 0;
this.vZ = null;
this.vY = null;
this.vX = null;
this.pt1 = null;
this.pt2 = null;
Clazz.instantialize (this, arguments);
}, J.shapesurface, "Contact", J.shapesurface.Isosurface);
Clazz.prepareFields (c$, function () {
this.vZ =  new J.util.V3 ();
this.vY =  new J.util.V3 ();
this.vX =  new J.util.V3 ();
this.pt1 =  new J.util.P3 ();
this.pt2 =  new J.util.P3 ();
});
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shapesurface.Contact, "initShape", []);
this.myType = "contact";
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if ("set" === propertyName) {
this.setContacts (value, !this.viewer.getTestFlag (4));
return;
}if ("init" === propertyName) {
this.translucentLevel = 0;
}this.setPropI (propertyName, value, bs);
}, "~S,~O,J.util.BS");
$_M(c$, "setContacts", 
($fz = function (value, doEditCpList) {
var contactType = (value[0]).intValue ();
var displayType = (value[1]).intValue ();
var colorDensity = (value[2]).booleanValue ();
var colorByType = (value[3]).booleanValue ();
var bsA = value[4];
var bsB = value[5];
var rd = value[6];
var saProbeRadius = (value[7]).floatValue ();
var parameters = value[8];
var command = value[9];
if (Float.isNaN (saProbeRadius)) saProbeRadius = 0;
if (rd == null) rd =  new J.atomdata.RadiusData (null, saProbeRadius, J.atomdata.RadiusData.EnumType.OFFSET, J.constant.EnumVdw.AUTO);
if (colorDensity) {
switch (displayType) {
case 1073741961:
case 1276117510:
case 135266319:
displayType = 1276117510;
break;
case 4106:
case 1073742036:
case 3145756:
case 1073742136:
break;
case 1074790451:
colorDensity = false;
break;
}
}var bs;
this.atomCount = this.viewer.getAtomCount ();
this.atoms = this.viewer.getModelSet ().atoms;
var intramolecularMode = Clazz.floatToInt (parameters == null || parameters.length < 2 ? 0 : parameters[1]);
var ptSize = (colorDensity && parameters != null && parameters[0] < 0 ? Math.abs (parameters[0]) : 0.15);
if (J.util.Logger.debugging) {
J.util.Logger.debug ("Contact intramolecularMode " + intramolecularMode);
J.util.Logger.debug ("Contacts for " + bsA.cardinality () + ": " + J.util.Escape.eBS (bsA));
J.util.Logger.debug ("Contacts to " + bsB.cardinality () + ": " + J.util.Escape.eBS (bsB));
}this.setPropI ("newObject", null, null);
this.thisMesh.setMerged (true);
this.thisMesh.nSets = 0;
this.thisMesh.info = null;
var params = this.sg.getParams ();
var func = null;
switch (displayType) {
case 1073741961:
func = "(a>b?a:b)";
break;
case 135266319:
case 1074790451:
func = "a-b";
break;
case 4106:
func = "a+b";
break;
}
var volumeData;
switch (displayType) {
case 1073742036:
colorByType = false;
bs = J.util.BSUtil.copy (bsA);
bs.or (bsB);
if (parameters[0] < 0) parameters[0] = 0;
params.colorDensity = colorDensity;
params.bsSelected = bs;
params.bsSolvent = bsB;
this.sg.setParameter ("parameters", parameters);
this.setPropI ("nci", Boolean.TRUE, null);
break;
case 1073742136:
case 3145756:
colorByType = false;
this.thisMesh.nSets = 1;
this.newSurface (3145756, null, bsA, bsB, rd, null, null, colorDensity, null, saProbeRadius);
break;
case 1074790451:
colorByType = false;
this.thisMesh.nSets = 1;
this.newSurface (554176565, null, bsA, bsB, rd, null, null, false, null, 0);
volumeData = this.sg.getVolumeData ();
this.sg.initState ();
this.newSurface (135266319, null, bsA, bsB, rd, parameters, func, colorDensity, volumeData, 0);
this.mergeMesh (null);
break;
case 1073741961:
case 1276117510:
colorByType = false;
this.newSurface (1276117510, null, bsA, bsB, rd, null, null, colorDensity, null, 0);
if (displayType == 1073741961) {
this.sg.initState ();
this.newSurface (1276117510, null, bsB, bsA, rd, parameters, func, colorDensity, null, 0);
this.mergeMesh (null);
} else {
var meshData =  new J.jvxl.data.MeshData ();
this.fillMeshData (meshData, 1, null);
meshData.getSurfaceSet ();
this.fillMeshData (meshData, 3, null);
}break;
case 4106:
case 135266319:
var volume = 0;
var pairs = this.getPairs (bsA, bsB, rd, intramolecularMode, doEditCpList);
this.thisMesh.info = pairs;
volume += this.combineSurfaces (pairs, contactType, displayType, parameters, func, colorDensity, colorByType);
this.thisMesh.calculatedVolume = Float.$valueOf (volume);
this.mergeMesh (null);
break;
}
this.thisMesh.setMerged (false);
this.thisMesh.jvxlData.vertexDataOnly = true;
this.thisMesh.reinitializeLightingAndColor (this.viewer);
if (contactType != 1073742036) {
this.thisMesh.bsVdw =  new J.util.BS ();
this.thisMesh.bsVdw.or (bsA);
this.thisMesh.bsVdw.or (bsB);
}this.setPropI ("finalize", command, null);
if (colorDensity) {
this.setPropI ("pointSize", Float.$valueOf (ptSize), null);
} else {
this.setPropI ("token", Integer.$valueOf (1073741964), null);
}if (this.thisMesh.slabOptions != null) {
this.thisMesh.slabOptions = null;
this.thisMesh.polygonCount0 = -1;
}this.discardTempData (true);
var defaultColor = null;
switch (contactType) {
case 1612189718:
defaultColor = "lightgreen";
break;
case 1073741881:
defaultColor = "yellow";
break;
case 3145756:
defaultColor = "skyblue";
break;
}
var ce = null;
if (colorByType) {
ce = this.viewer.getColorEncoder ("rwb");
ce.setRange (-0.5, 0.5, false);
} else if (defaultColor != null) {
this.setPropI ("color", Integer.$valueOf (J.util.ColorUtil.getArgbFromString (defaultColor)), null);
} else if (displayType == 1073742036) {
ce = this.viewer.getColorEncoder ("bgr");
ce.setRange (-0.03, 0.03, false);
} else {
ce = this.viewer.getColorEncoder ("rgb");
if (colorDensity) ce.setRange (-0.3, 0.3, false);
 else ce.setRange (-0.5, 1, false);
}if (ce != null) this.thisMesh.remapColors (this.viewer, ce, this.translucentLevel);
}, $fz.isPrivate = true, $fz), "~A,~B");
$_M(c$, "combineSurfaces", 
($fz = function (pairs, contactType, displayType, parameters, func, isColorDensity, colorByType) {
var volumeData =  new J.jvxl.data.VolumeData ();
var logLevel = J.util.Logger.getLogLevel ();
J.util.Logger.setLogLevel (0);
var resolution = this.sg.getParams ().resolution;
var nContacts = pairs.size ();
var volume = 0;
if (displayType == 1073741961 && resolution == 3.4028235E38) resolution = (nContacts > 1000 ? 3 : 10);
for (var i = nContacts; --i >= 0; ) {
var cp = pairs.get (i);
var oldScore = cp.score;
var isVdwClash = (displayType == 135266319 && (contactType == 1649412120 || contactType == 0) && cp.setForVdwClash (true));
if (isVdwClash) cp.score = 0;
if (contactType != 0 && cp.contactType != contactType) continue;
var nV = this.thisMesh.vertexCount;
this.thisMesh.nSets++;
if (contactType != 0 || cp.contactType != 1649412120) volume += cp.volume;
this.setVolumeData (displayType, volumeData, cp, resolution, nContacts);
switch (displayType) {
case 1073741961:
this.newSurface (displayType, cp, null, null, null, null, func, isColorDensity, volumeData, 0);
cp.switchAtoms ();
this.newSurface (displayType, cp, null, null, null, null, null, isColorDensity, volumeData, 0);
break;
case 1276117510:
case 135266319:
case 4106:
this.newSurface (displayType, cp, null, null, null, parameters, func, isColorDensity, volumeData, 0);
if (isVdwClash && cp.setForVdwClash (false)) {
if (colorByType) nV = this.setColorByScore (cp.score, nV);
cp.score = oldScore;
volume += cp.volume;
this.newSurface (displayType, cp, null, null, null, parameters, func, isColorDensity, volumeData, 0);
}break;
}
if (i > 0 && (i % 1000) == 0 && logLevel == 4) {
J.util.Logger.setLogLevel (4);
J.util.Logger.info ("contact..." + i);
J.util.Logger.setLogLevel (0);
}if (colorByType) this.setColorByScore ((cp.contactType == 1612189718 ? 4 : cp.score), nV);
}
J.util.Logger.setLogLevel (logLevel);
return volume;
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N,~N,~A,~O,~B,~B");
$_M(c$, "setColorByScore", 
($fz = function (score, nV) {
for (var iv = this.thisMesh.vertexCount; --iv >= nV; ) this.thisMesh.vertexValues[iv] = score;

return this.thisMesh.vertexCount;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "getPairs", 
($fz = function (bsA, bsB, rd, intramolecularMode, doEditCpList) {
var list =  new J.util.JmolList ();
var ad =  new J.atomdata.AtomData ();
ad.radiusData = rd;
var bs = J.util.BSUtil.copy (bsA);
bs.or (bsB);
if (bs.isEmpty ()) return list;
ad.bsSelected = bs;
var isMultiModel = (this.atoms[bs.nextSetBit (0)].modelIndex != this.atoms[bs.length () - 1].modelIndex);
var isSelf = bsA.equals (bsB);
this.viewer.fillAtomData (ad, 2 | (isMultiModel ? 16 : 0) | 4);
var maxRadius = 0;
for (var ib = bsB.nextSetBit (0); ib >= 0; ib = bsB.nextSetBit (ib + 1)) if (ad.atomRadius[ib] > maxRadius) maxRadius = ad.atomRadius[ib];

var iter = this.viewer.getSelectedAtomIterator (bsB, isSelf, false, isMultiModel);
for (var ia = bsA.nextSetBit (0); ia >= 0; ia = bsA.nextSetBit (ia + 1)) {
var atomA = this.atoms[ia];
var vdwA = atomA.getVanderwaalsRadiusFloat (this.viewer, J.constant.EnumVdw.AUTO);
if (isMultiModel) this.viewer.setIteratorForPoint (iter, -1, ad.atomXyz[ia], ad.atomRadius[ia] + maxRadius);
 else this.viewer.setIteratorForAtom (iter, ia, ad.atomRadius[ia] + maxRadius);
while (iter.hasNext ()) {
var ib = iter.next ();
if (isMultiModel && !bsB.get (ib)) continue;
var atomB = this.atoms[ib];
var isSameMolecule = (ad.atomMolecule[ia] == ad.atomMolecule[ib]);
if (ia == ib || isSameMolecule && atomA.isWithinFourBonds (atomB)) continue;
switch (intramolecularMode) {
case 0:
break;
case 1:
case 2:
if (isSameMolecule != (intramolecularMode == 1)) continue;
}
var vdwB = atomB.getVanderwaalsRadiusFloat (this.viewer, J.constant.EnumVdw.AUTO);
var ra = ad.atomRadius[ia];
var rb = ad.atomRadius[ib];
var d = atomA.distance (atomB);
if (d > ra + rb) continue;
var cp =  new J.util.ContactPair (this.atoms, ia, ib, ra, rb, vdwA, vdwB);
if (cp.score < 0) J.shapesurface.Contact.getVdwClashRadius (cp, ra - vdwA, vdwA, vdwB, d);
var typeA = J.constant.EnumHBondType.getType (atomA);
var typeB = (typeA === J.constant.EnumHBondType.NOT ? J.constant.EnumHBondType.NOT : J.constant.EnumHBondType.getType (atomB));
var isHBond = J.constant.EnumHBondType.isPossibleHBond (typeA, typeB);
var hbondCutoff = (atomA.getElementNumber () == 1 || atomB.getElementNumber () == 1 ? -1.2 : -1.0);
if (isHBond && cp.score < hbondCutoff) isHBond = false;
if (isHBond && cp.score < 0) cp.contactType = 1612189718;
list.addLast (cp);
}
}
iter.release ();
iter = null;
if (!doEditCpList) return list;
var n = list.size () - 1;
var bsBad =  new J.util.BS ();
for (var i = 0; i < n; i++) {
var cp1 = list.get (i);
for (var j = i + 1; j <= n; j++) {
var cp2 = list.get (j);
for (var m = 0; m < 2; m++) {
for (var p = 0; p < 2; p++) {
switch (J.shapesurface.Contact.checkCp (cp1, cp2, m, p)) {
case 1:
bsBad.set (i);
break;
case 2:
bsBad.set (j);
break;
default:
}
}
}
}
}
for (var i = bsBad.length (); --i >= 0; ) if (bsBad.get (i)) list.remove (i);

if (J.util.Logger.debugging) for (var i = 0; i < list.size (); i++) J.util.Logger.debug (list.get (i).toString ());

J.util.Logger.info ("Contact pairs: " + list.size ());
return list;
}, $fz.isPrivate = true, $fz), "J.util.BS,J.util.BS,J.atomdata.RadiusData,~N,~B");
c$.checkCp = $_M(c$, "checkCp", 
($fz = function (cp1, cp2, i1, i2) {
if (cp1.myAtoms[i1] !== cp2.myAtoms[i2]) return 0;
var clash1 = (cp1.pt.distance (cp2.myAtoms[1 - i2]) < cp2.radii[1 - i2]);
var clash2 = (cp2.pt.distance (cp1.myAtoms[1 - i1]) < cp1.radii[1 - i1]);
return (!clash1 && !clash2 ? 0 : cp1.score > cp2.score ? 1 : 2);
}, $fz.isPrivate = true, $fz), "J.util.ContactPair,J.util.ContactPair,~N,~N");
$_M(c$, "newSurface", 
($fz = function (displayType, cp, bs1, bs2, rd, parameters, func, isColorDensity, volumeData, sasurfaceRadius) {
var params = this.sg.getParams ();
params.isSilent = true;
if (cp == null) {
bs2.andNot (bs1);
if (bs1.isEmpty () || bs2.isEmpty ()) return;
} else {
params.contactPair = cp;
}var iSlab0 = 0;
var iSlab1 = 0;
this.sg.initState ();
switch (displayType) {
case 1073742136:
case 3145756:
case 554176565:
case 1276117510:
case 1073741961:
var rdA;
var rdB;
if (displayType == 3145756) {
rdA = J.shapesurface.Contact.rdVDW;
rdB =  new J.atomdata.RadiusData (null, (rd.factorType === J.atomdata.RadiusData.EnumType.OFFSET ? rd.value * 2 : (rd.value - 1) * 2 + 1), rd.factorType, rd.vdwType);
} else {
rdA = rdB = rd;
}params.colorDensity = isColorDensity;
if (isColorDensity) {
this.setPropI ("cutoffRange", [-100.0, 0], null);
}if (cp == null) {
params.atomRadiusData = rdA;
params.bsIgnore = J.util.BSUtil.copyInvert (bs1, this.atomCount);
params.bsSelected = bs1;
params.bsSolvent = null;
}params.volumeData = volumeData;
this.setPropI ("sasurface", Float.$valueOf (sasurfaceRadius), null);
this.setPropI ("map", Boolean.TRUE, null);
if (cp == null) {
params.atomRadiusData = rdB;
params.bsIgnore = J.util.BSUtil.copyInvert (bs2, this.atomCount);
params.bsSelected = bs2;
}params.volumeData = volumeData;
this.setPropI ("sasurface", Float.$valueOf (sasurfaceRadius), null);
switch (displayType) {
case 1073741961:
case 1276117510:
iSlab0 = -100;
break;
case 1073742136:
case 3145756:
if (isColorDensity) iSlab0 = -100;
break;
case 554176565:
iSlab1 = -100;
}
break;
case 135266319:
case 4106:
if (displayType == 4106) this.sg.setParameter ("parameters", parameters);
if (cp == null) {
params.atomRadiusData = rd;
params.bsIgnore = J.util.BSUtil.copyInvert (bs2, this.atomCount);
params.bsIgnore.andNot (bs1);
}params.func = func;
params.intersection = [bs1, bs2];
params.volumeData = volumeData;
params.colorDensity = isColorDensity;
if (isColorDensity) this.setPropI ("cutoffRange", [-5.0, 0], null);
this.setPropI ("sasurface", Float.$valueOf (0), null);
this.setPropI ("map", Boolean.TRUE, null);
params.volumeData = volumeData;
this.setPropI ("sasurface", Float.$valueOf (0), null);
if (displayType != 4106) iSlab0 = -100;
}
if (iSlab0 != iSlab1) this.thisMesh.slabPolygons (J.util.MeshSurface.getSlabWithinRange (iSlab0, iSlab1), false);
if (displayType != 3145756) this.thisMesh.setMerged (true);
}, $fz.isPrivate = true, $fz), "~N,J.util.ContactPair,J.util.BS,J.util.BS,J.atomdata.RadiusData,~A,~O,~B,J.jvxl.data.VolumeData,~N");
$_M(c$, "setVolumeData", 
($fz = function (type, volumeData, cp, resolution, nPairs) {
this.pt1.setT (cp.myAtoms[0]);
this.pt2.setT (cp.myAtoms[1]);
this.vX.sub2 (this.pt2, this.pt1);
var dAB = this.vX.length ();
var dYZ = (cp.radii[0] * cp.radii[0] + dAB * dAB - cp.radii[1] * cp.radii[1]) / (2 * dAB * cp.radii[0]);
dYZ = 2.1 * (cp.radii[0] * Math.sin (Math.acos (dYZ)));
J.util.Measure.getNormalToLine (this.pt1, this.pt2, this.vZ);
this.vZ.scale (dYZ);
this.vY.cross (this.vZ, this.vX);
this.vY.normalize ();
this.vY.scale (dYZ);
if (type != 4106) {
this.vX.normalize ();
this.pt1.scaleAdd2 ((dAB - cp.radii[1]) * 0.95, this.vX, this.pt1);
this.pt2.scaleAdd2 ((cp.radii[0] - dAB) * 0.95, this.vX, this.pt2);
this.vX.sub2 (this.pt2, this.pt1);
}if (resolution == 3.4028235E38) resolution = (nPairs > 100 ? 3 : 10);
var nX = Math.max (5, Clazz.doubleToInt (Math.floor (this.pt1.distance (this.pt2) * resolution + 1)));
if ((nX % 2) == 0) nX++;
var nYZ = Math.max (7, Clazz.doubleToInt (Math.floor (dYZ * resolution + 1)));
if ((nYZ % 2) == 0) nYZ++;
volumeData.setVoxelCounts (nX, nYZ, nYZ);
this.pt1.scaleAdd2 (-0.5, this.vY, this.pt1);
this.pt1.scaleAdd2 (-0.5, this.vZ, this.pt1);
volumeData.setVolumetricOrigin (this.pt1.x, this.pt1.y, this.pt1.z);
this.vX.scale (1 / (nX - 1));
this.vY.scale (1 / (nYZ - 1));
this.vZ.scale (1 / (nYZ - 1));
volumeData.setVolumetricVector (0, this.vX.x, this.vX.y, this.vX.z);
volumeData.setVolumetricVector (1, this.vY.x, this.vY.y, this.vY.z);
volumeData.setVolumetricVector (2, this.vZ.x, this.vZ.y, this.vZ.z);
}, $fz.isPrivate = true, $fz), "~N,J.jvxl.data.VolumeData,J.util.ContactPair,~N,~N");
$_M(c$, "mergeMesh", 
($fz = function (md) {
this.thisMesh.merge (md);
if (this.minData == 3.4028235E38) {
} else if (this.jvxlData.mappedDataMin == 3.4028235E38) {
this.jvxlData.mappedDataMin = this.minData;
this.jvxlData.mappedDataMax = this.maxData;
} else {
this.jvxlData.mappedDataMin = Math.min (this.minData, this.jvxlData.mappedDataMin);
this.jvxlData.mappedDataMax = Math.max (this.maxData, this.jvxlData.mappedDataMax);
}this.minData = this.jvxlData.mappedDataMin;
this.maxData = this.jvxlData.mappedDataMax;
this.jvxlData.valueMappedToBlue = this.minData;
this.jvxlData.valueMappedToRed = this.maxData;
}, $fz.isPrivate = true, $fz), "J.jvxl.data.MeshData");
Clazz.overrideMethod (c$, "addMeshInfo", 
function (mesh, info) {
if (mesh.info == null) return;
var pairInfo =  new J.util.JmolList ();
info.put ("pairInfo", pairInfo);
var list = mesh.info;
for (var i = 0; i < list.size (); i++) {
var cpInfo =  new java.util.Hashtable ();
pairInfo.addLast (cpInfo);
var cp = list.get (i);
cpInfo.put ("type", J.script.T.nameOf (cp.contactType));
cpInfo.put ("volume", Double.$valueOf (cp.volume));
cpInfo.put ("vdwVolume", Double.$valueOf (cp.vdwVolume));
if (!Float.isNaN (cp.xVdwClash)) {
cpInfo.put ("xVdwClash", Double.$valueOf (cp.xVdwClash));
}cpInfo.put ("score", Double.$valueOf (cp.score));
cpInfo.put ("atoms", cp.myAtoms);
cpInfo.put ("radii", cp.radii);
cpInfo.put ("vdws", cp.vdws);
}
}, "J.shapesurface.IsosurfaceMesh,java.util.Map");
c$.getVdwClashRadius = $_M(c$, "getVdwClashRadius", 
($fz = function (cp, x0, vdwA, vdwB, d) {
var sum = vdwA + vdwB;
var dif2 = vdwA - vdwB;
dif2 *= dif2;
var v0_nopi = x0 * x0 * (sum + 1.3333333333333333 * x0 - dif2 / sum);
cp.vdwVolume = cp.volume - v0_nopi * 3.141592653589793;
var a = (sum - d);
var b = d + 2 * sum - 3 * dif2 / d;
var c = v0_nopi * 12;
var a2 = a * a;
var a3 = a * a2;
var b2 = b * b;
var b3 = b * b2;
var f = -a * 2 / 3 - b / 6;
var g = (4 * a2 - 4 * a * b + b2) / 36;
var v = a3 / 27 - a2 * b / 18 + a * b2 / 36 - b3 / 216 + c / 4;
var u = -c / 432 * (8 * a3 - 12 * a2 * b + 6 * a * b2 - b3 + 27 * c);
var theta = Math.atan2 (Math.sqrt (u), v);
var vvu = Math.pow (v * v + u, 0.16666666666666666);
var costheta = Math.cos (theta / 3);
var x;
x = f + (g / vvu + vvu) * costheta;
if (x > 0) {
cp.xVdwClash = ((x / 2));
}}, $fz.isPrivate = true, $fz), "J.util.ContactPair,~N,~N,~N,~N");
c$.rdVDW = c$.prototype.rdVDW =  new J.atomdata.RadiusData (null, 1, J.atomdata.RadiusData.EnumType.FACTOR, J.constant.EnumVdw.AUTO);
});
