Clazz.declarePackage ("J.modelset");
Clazz.load (["java.lang.Float", "J.util.BS", "$.V3"], "J.modelset.AtomCollection", ["java.lang.Character", "java.util.Arrays", "$.Hashtable", "J.atomdata.RadiusData", "J.constant.EnumPalette", "$.EnumStructure", "$.EnumVdw", "J.geodesic.EnvelopeCalculation", "J.modelset.Group", "$.LabelToken", "J.script.T", "J.util.ArrayUtil", "$.AxisAngle4f", "$.BSUtil", "$.Elements", "$.Escape", "$.JmolList", "$.Logger", "$.Matrix3f", "$.Measure", "$.P3", "$.Parser", "$.TextFormat", "$.Vibration", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.g3d = null;
this.atoms = null;
this.atomCount = 0;
this.atomNames = null;
this.atomTypes = null;
this.atomSerials = null;
this.vibrations = null;
this.occupancies = null;
this.bfactor100s = null;
this.partialCharges = null;
this.ionicRadii = null;
this.hydrophobicities = null;
this.atomTensorList = null;
this.atomTensors = null;
this.surfaceDistance100s = null;
this.haveStraightness = false;
this.bsHidden = null;
this.maxBondingRadius = 1.4E-45;
this.maxVanderwaalsRadius = 1.4E-45;
this.hasBfactorRange = false;
this.bfactor100Lo = 0;
this.bfactor100Hi = 0;
this.surfaceDistanceMax = 0;
this.bsSurface = null;
this.nSurfaceAtoms = 0;
this.bspf = null;
this.preserveState = true;
this.tainted = null;
this.canSkipLoad = true;
this.bsEmpty = null;
this.bsFoundRectangle = null;
this.aaRet = null;
if (!Clazz.isClassDefined ("J.modelset.AtomCollection.AtomSorter")) {
J.modelset.AtomCollection.$AtomCollection$AtomSorter$ ();
}
this.bsModulated = null;
Clazz.instantialize (this, arguments);
}, J.modelset, "AtomCollection");
Clazz.prepareFields (c$, function () {
this.bsHidden =  new J.util.BS ();
this.bsEmpty =  new J.util.BS ();
this.bsFoundRectangle =  new J.util.BS ();
});
$_M(c$, "releaseModelSet", 
function () {
this.releaseModelSetAC ();
});
$_M(c$, "releaseModelSetAC", 
function () {
this.atoms = null;
this.viewer = null;
this.g3d = null;
this.bspf = null;
this.surfaceDistance100s = null;
this.bsSurface = null;
this.tainted = null;
this.atomNames = null;
this.atomTypes = null;
this.atomSerials = null;
this.vibrations = null;
this.occupancies = null;
this.bfactor100s = null;
this.partialCharges = null;
this.ionicRadii = null;
this.atomTensors = null;
});
$_M(c$, "mergeAtomArrays", 
function (mergeModelSet) {
this.tainted = mergeModelSet.tainted;
this.atomNames = mergeModelSet.atomNames;
this.atomTypes = mergeModelSet.atomTypes;
this.atomSerials = mergeModelSet.atomSerials;
this.vibrations = mergeModelSet.vibrations;
this.occupancies = mergeModelSet.occupancies;
this.bfactor100s = mergeModelSet.bfactor100s;
this.ionicRadii = mergeModelSet.ionicRadii;
this.partialCharges = mergeModelSet.partialCharges;
this.atomTensors = mergeModelSet.atomTensors;
this.atomTensorList = mergeModelSet.atomTensorList;
this.bsModulated = mergeModelSet.bsModulated;
this.setHaveStraightness (false);
this.surfaceDistance100s = null;
}, "J.modelset.AtomCollection");
$_M(c$, "setHaveStraightness", 
function (TF) {
this.haveStraightness = TF;
}, "~B");
$_M(c$, "getHaveStraightness", 
function () {
return this.haveStraightness;
});
$_M(c$, "getAtomPointVector", 
function (bs) {
var v =  new J.util.JmolList ();
if (bs != null) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
v.addLast (this.atoms[i]);
}
}return v;
}, "J.util.BS");
$_M(c$, "getAtomCount", 
function () {
return this.atomCount;
});
$_M(c$, "modelSetHasVibrationVectors", 
function () {
return (this.vibrations != null);
});
$_M(c$, "getAtomTypes", 
function () {
return this.atomTypes;
});
$_M(c$, "getPartialCharges", 
function () {
return this.partialCharges;
});
$_M(c$, "getIonicRadii", 
function () {
return this.ionicRadii;
});
$_M(c$, "getBFactors", 
function () {
return this.bfactor100s;
});
$_M(c$, "getHydrophobicity", 
function () {
return this.hydrophobicities;
});
$_M(c$, "setBsHidden", 
function (bs) {
this.bsHidden = bs;
}, "J.util.BS");
$_M(c$, "isAtomHidden", 
function (iAtom) {
return this.bsHidden.get (iAtom);
}, "~N");
$_M(c$, "getAtomInfo", 
function (i, format) {
return (format == null ? this.atoms[i].getInfo () : J.modelset.LabelToken.formatLabel (this.viewer, this.atoms[i], format));
}, "~N,~S");
$_M(c$, "getAtomInfoXYZ", 
function (i, useChimeFormat) {
return this.atoms[i].getInfoXYZ (useChimeFormat);
}, "~N,~B");
$_M(c$, "getElementSymbol", 
function (i) {
return this.atoms[i].getElementSymbol ();
}, "~N");
$_M(c$, "getElementNumber", 
function (i) {
return this.atoms[i].getElementNumber ();
}, "~N");
$_M(c$, "getElementName", 
function (i) {
return J.util.Elements.elementNameFromNumber (this.atoms[i].getAtomicAndIsotopeNumber ());
}, "~N");
$_M(c$, "getAtomName", 
function (i) {
return this.atoms[i].getAtomName ();
}, "~N");
$_M(c$, "getAtomNumber", 
function (i) {
return this.atoms[i].getAtomNumber ();
}, "~N");
$_M(c$, "getAtomPoint3f", 
function (i) {
return this.atoms[i];
}, "~N");
$_M(c$, "getAtomRadius", 
function (i) {
return this.atoms[i].getRadius ();
}, "~N");
$_M(c$, "getAtomVdwRadius", 
function (i, type) {
return this.atoms[i].getVanderwaalsRadiusFloat (this.viewer, type);
}, "~N,J.constant.EnumVdw");
$_M(c$, "getAtomColix", 
function (i) {
return this.atoms[i].getColix ();
}, "~N");
$_M(c$, "getAtomChain", 
function (i) {
return this.atoms[i].getChainIDStr ();
}, "~N");
$_M(c$, "getQuaternion", 
function (i, qtype) {
return (i < 0 ? null : this.atoms[i].group.getQuaternion (qtype));
}, "~N,~S");
$_M(c$, "getHelixData", 
function (bs, tokType) {
var iAtom = bs.nextSetBit (0);
return (iAtom < 0 ? "null" : this.atoms[iAtom].group.getHelixData (tokType, this.viewer.getQuaternionFrame (), this.viewer.getInt (553648146)));
}, "J.util.BS,~N");
$_M(c$, "getAtomIndexFromAtomNumber", 
function (atomNumber, bsVisibleFrames) {
for (var i = 0; i < this.atomCount; i++) {
var atom = this.atoms[i];
if (atom.getAtomNumber () == atomNumber && bsVisibleFrames.get (atom.modelIndex)) return i;
}
return -1;
}, "~N,J.util.BS");
$_M(c$, "setFormalCharges", 
function (bs, formalCharge) {
if (bs != null) for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
this.atoms[i].setFormalCharge (formalCharge);
this.taintAtom (i, 4);
}
}, "J.util.BS,~N");
$_M(c$, "getAtomicCharges", 
function () {
var charges =  Clazz.newFloatArray (this.atomCount, 0);
for (var i = this.atomCount; --i >= 0; ) charges[i] = this.atoms[i].getElementNumber ();

return charges;
});
$_M(c$, "getRadiusVdwJmol", 
function (atom) {
return J.util.Elements.getVanderwaalsMar (atom.getElementNumber (), J.constant.EnumVdw.JMOL) / 1000;
}, "J.modelset.Atom");
$_M(c$, "getMaxVanderwaalsRadius", 
function () {
if (this.maxVanderwaalsRadius == 1.4E-45) this.findMaxRadii ();
return this.maxVanderwaalsRadius;
});
$_M(c$, "findMaxRadii", 
function () {
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
var bondingRadius = atom.getBondingRadiusFloat ();
if (bondingRadius > this.maxBondingRadius) this.maxBondingRadius = bondingRadius;
var vdwRadius = atom.getVanderwaalsRadiusFloat (this.viewer, J.constant.EnumVdw.AUTO);
if (vdwRadius > this.maxVanderwaalsRadius) this.maxVanderwaalsRadius = vdwRadius;
}
});
$_M(c$, "clearBfactorRange", 
function () {
this.hasBfactorRange = false;
});
$_M(c$, "calcBfactorRange", 
($fz = function (bs) {
if (this.hasBfactorRange) return;
this.bfactor100Lo = 2147483647;
this.bfactor100Hi = -2147483648;
if (bs == null) {
for (var i = 0; i < this.atomCount; i++) this.setBf (i);

} else {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) this.setBf (i);

}this.hasBfactorRange = true;
}, $fz.isPrivate = true, $fz), "J.util.BS");
$_M(c$, "setBf", 
($fz = function (i) {
var bf = this.atoms[i].getBfactor100 ();
if (bf < this.bfactor100Lo) this.bfactor100Lo = bf;
 else if (bf > this.bfactor100Hi) this.bfactor100Hi = bf;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getBfactor100Lo", 
function () {
if (!this.hasBfactorRange) {
if (this.viewer.global.rangeSelected) {
this.calcBfactorRange (this.viewer.getSelectionSet (false));
} else {
this.calcBfactorRange (null);
}}return this.bfactor100Lo;
});
$_M(c$, "getBfactor100Hi", 
function () {
this.getBfactor100Lo ();
return this.bfactor100Hi;
});
$_M(c$, "getSurfaceDistanceMax", 
function () {
if (this.surfaceDistance100s == null) this.calcSurfaceDistances ();
return this.surfaceDistanceMax;
});
$_M(c$, "calculateVolume", 
function (bs, vType) {
var volume = 0;
if (bs != null) for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) volume += this.atoms[i].getVolume (this.viewer, vType);

return volume;
}, "J.util.BS,J.constant.EnumVdw");
$_M(c$, "getSurfaceDistance100", 
function (atomIndex) {
if (this.nSurfaceAtoms == 0) return -1;
if (this.surfaceDistance100s == null) this.calcSurfaceDistances ();
return this.surfaceDistance100s[atomIndex];
}, "~N");
$_M(c$, "calcSurfaceDistances", 
($fz = function () {
this.calculateSurface (null, -1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "calculateSurface", 
function (bsSelected, envelopeRadius) {
if (envelopeRadius < 0) envelopeRadius = 3.0;
var ec =  new J.geodesic.EnvelopeCalculation (this.viewer, this.atomCount, null);
ec.calculate ( new J.atomdata.RadiusData (null, envelopeRadius, J.atomdata.RadiusData.EnumType.ABSOLUTE, null), 3.4028235E38, bsSelected, J.util.BSUtil.copyInvert (bsSelected, this.atomCount), false, false, false, true);
var points = ec.getPoints ();
this.surfaceDistanceMax = 0;
this.bsSurface = ec.getBsSurfaceClone ();
this.surfaceDistance100s =  Clazz.newIntArray (this.atomCount, 0);
this.nSurfaceAtoms = J.util.BSUtil.cardinalityOf (this.bsSurface);
if (this.nSurfaceAtoms == 0 || points == null || points.length == 0) return points;
var radiusAdjust = (envelopeRadius == 3.4028235E38 ? 0 : envelopeRadius);
for (var i = 0; i < this.atomCount; i++) {
if (this.bsSurface.get (i)) {
this.surfaceDistance100s[i] = 0;
} else {
var dMin = 3.4028235E38;
var atom = this.atoms[i];
for (var j = points.length; --j >= 0; ) {
var d = Math.abs (points[j].distance (atom) - radiusAdjust);
if (d < 0 && J.util.Logger.debugging) J.util.Logger.debug ("draw d" + j + " " + J.util.Escape.eP (points[j]) + " \"" + d + " ? " + atom.getInfo () + "\"");
dMin = Math.min (d, dMin);
}
var d = this.surfaceDistance100s[i] = Clazz.doubleToInt (Math.floor (dMin * 100));
this.surfaceDistanceMax = Math.max (this.surfaceDistanceMax, d);
}}
return points;
}, "J.util.BS,~N");
$_M(c$, "setAtomCoord2", 
function (bs, tokType, xyzValues) {
var xyz = null;
var values = null;
var v = null;
var type = 0;
var nValues = 1;
if (Clazz.instanceOf (xyzValues, J.util.P3)) {
xyz = xyzValues;
} else if (Clazz.instanceOf (xyzValues, J.util.JmolList)) {
v = xyzValues;
if ((nValues = v.size ()) == 0) return;
type = 1;
} else if (J.util.Escape.isAP (xyzValues)) {
values = xyzValues;
if ((nValues = values.length) == 0) return;
type = 2;
} else {
return;
}var n = 0;
if (bs != null) for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
switch (type) {
case 1:
if (n >= nValues) return;
xyz = v.get (n++);
break;
case 2:
if (n >= nValues) return;
xyz = values[n++];
break;
}
switch (tokType) {
case 1146095626:
this.setAtomCoord (i, xyz.x, xyz.y, xyz.z);
break;
case 1146095627:
this.atoms[i].setFractionalCoordTo (xyz, true);
this.taintAtom (i, 2);
break;
case 1146095629:
this.atoms[i].setFractionalCoordTo (xyz, false);
this.taintAtom (i, 2);
break;
case 1146095631:
this.setAtomVibrationVector (i, xyz);
break;
}
}
}, "J.util.BS,~N,~O");
$_M(c$, "setAtomVibrationVector", 
($fz = function (atomIndex, vib) {
this.setVibrationVector (atomIndex, vib);
this.taintAtom (atomIndex, 12);
}, $fz.isPrivate = true, $fz), "~N,J.util.Tuple3f");
$_M(c$, "setAtomCoord", 
function (atomIndex, x, y, z) {
if (atomIndex < 0 || atomIndex >= this.atomCount) return;
var a = this.atoms[atomIndex];
a.set (x, y, z);
this.fixTrajectory (a);
this.taintAtom (atomIndex, 2);
}, "~N,~N,~N,~N");
$_M(c$, "fixTrajectory", 
($fz = function (a) {
var m = a.modelIndex;
var mc = this;
var isTraj = mc.isTrajectory (m);
if (!isTraj) return;
var isFrac = mc.unitCells != null && mc.unitCells[m].getCoordinatesAreFractional ();
var pt = mc.trajectorySteps.get (m)[a.index - mc.models[m].firstAtomIndex];
pt.set (a.x, a.y, a.z);
if (isFrac) mc.unitCells[m].toFractional (pt, true);
}, $fz.isPrivate = true, $fz), "J.modelset.Atom");
$_M(c$, "setAtomCoordRelative", 
function (atomIndex, x, y, z) {
if (atomIndex < 0 || atomIndex >= this.atomCount) return;
var a = this.atoms[atomIndex];
a.x += x;
a.y += y;
a.z += z;
this.fixTrajectory (a);
this.taintAtom (atomIndex, 2);
}, "~N,~N,~N,~N");
$_M(c$, "setAtomsCoordRelative", 
function (bs, x, y, z) {
if (bs != null) for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) this.setAtomCoordRelative (i, x, y, z);

}, "J.util.BS,~N,~N,~N");
$_M(c$, "setAPa", 
function (bs, tok, iValue, fValue, sValue, values, list) {
var n = 0;
if (values != null && values.length == 0 || bs == null) return;
var isAll = (values != null && values.length == this.atomCount || list != null && list.length == this.atomCount);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (isAll) n = i;
if (values != null) {
if (n >= values.length) return;
fValue = values[n++];
iValue = Clazz.floatToInt (fValue);
} else if (list != null) {
if (n >= list.length) return;
sValue = list[n++];
}var atom = this.atoms[i];
switch (tok) {
case 1087375362:
this.taintAtom (i, 0);
this.setAtomName (i, sValue);
break;
case 1095763969:
this.taintAtom (i, 13);
this.setAtomNumber (i, iValue);
break;
case 1087375361:
this.taintAtom (i, 1);
this.setAtomType (i, sValue);
break;
case 1112541185:
case 1112541205:
this.setAtomCoord (i, fValue, atom.y, atom.z);
break;
case 1112541186:
case 1112541206:
this.setAtomCoord (i, atom.x, fValue, atom.z);
break;
case 1112541187:
case 1112541207:
this.setAtomCoord (i, atom.x, atom.y, fValue);
break;
case 1112541202:
case 1112541203:
case 1112541204:
this.setVibrationVector2 (i, tok, fValue);
break;
case 1112541188:
case 1112541189:
case 1112541190:
atom.setFractionalCoord (tok, fValue, true);
this.taintAtom (i, 2);
break;
case 1112541191:
case 1112541192:
case 1112541193:
atom.setFractionalCoord (tok, fValue, false);
this.taintAtom (i, 2);
break;
case 1095763976:
case 1087375365:
this.setElement (atom, iValue);
break;
case 1632634889:
atom.setFormalCharge (iValue);
this.taintAtom (i, 4);
break;
case 1114638362:
if (this.setHydrophobicity (i, fValue)) this.taintAtom (i, 5);
break;
case 1826248715:
case 1288701960:
this.viewer.setAtomLabel (sValue, i);
break;
case 1129318401:
if (iValue < 2) iValue = Clazz.doubleToInt (Math.floor (100 * fValue));
if (this.setOccupancy (i, iValue)) this.taintAtom (i, 7);
break;
case 1112541196:
if (this.setPartialCharge (i, fValue)) this.taintAtom (i, 8);
break;
case 1112541195:
if (this.setIonicRadius (i, fValue)) this.taintAtom (i, 6);
break;
case 1666189314:
case 1113200651:
if (fValue < 0) fValue = 0;
 else if (fValue > 16) fValue = 16;
atom.madAtom = (Clazz.floatToShort (fValue * 2000));
break;
case 1114638363:
this.viewer.setSelectedAtom (atom.index, (fValue != 0));
break;
case 1112541199:
if (this.setBFactor (i, fValue)) this.taintAtom (i, 9);
break;
case 1095763988:
atom.setValence (iValue);
this.taintAtom (i, 10);
break;
case 1649412120:
if (atom.setRadius (fValue)) this.taintAtom (i, 11);
 else this.untaint (i, 11);
break;
default:
J.util.Logger.error ("unsettable atom property: " + J.script.T.nameOf (tok));
break;
}
}
if (tok == 1114638363) this.viewer.setSelectedAtom (-1, false);
}, "J.util.BS,~N,~N,~N,~S,~A,~A");
$_M(c$, "setElement", 
function (atom, atomicNumber) {
this.taintAtom (atom.index, 3);
atom.setAtomicAndIsotopeNumber (atomicNumber);
atom.setPaletteID (J.constant.EnumPalette.CPK.id);
atom.setColixAtom (this.viewer.getColixAtomPalette (atom, J.constant.EnumPalette.CPK.id));
}, "J.modelset.Atom,~N");
$_M(c$, "getVibrationCoord", 
function (atomIndex, c) {
if (this.vibrations == null || this.vibrations[atomIndex] == null) return 0;
switch (c) {
case 'X':
return this.vibrations[atomIndex].x;
case 'Y':
return this.vibrations[atomIndex].y;
default:
return this.vibrations[atomIndex].z;
}
}, "~N,~S");
$_M(c$, "getVibration", 
function (atomIndex, forceNew) {
var v = (this.vibrations == null ? null : this.vibrations[atomIndex]);
return (v == null && forceNew ?  new J.util.Vibration () : v);
}, "~N,~B");
$_M(c$, "setVibrationVector", 
function (atomIndex, vib) {
if (Float.isNaN (vib.x) || Float.isNaN (vib.y) || Float.isNaN (vib.z)) return;
if (this.vibrations == null || this.vibrations.length < atomIndex) this.vibrations =  new Array (this.atoms.length);
if (Clazz.instanceOf (vib, J.util.Vibration)) {
this.vibrations[atomIndex] = vib;
} else {
if (this.vibrations[atomIndex] == null) this.vibrations[atomIndex] =  new J.util.Vibration ();
this.vibrations[atomIndex].setT (vib);
}this.atoms[atomIndex].setVibrationVector ();
}, "~N,J.util.Tuple3f");
$_M(c$, "setVibrationVector2", 
($fz = function (atomIndex, tok, fValue) {
var v = this.getVibration (atomIndex, true);
switch (tok) {
case 1112541202:
v.x = fValue;
break;
case 1112541203:
v.y = fValue;
break;
case 1112541204:
v.z = fValue;
break;
}
this.setAtomVibrationVector (atomIndex, v);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "setAtomName", 
function (atomIndex, name) {
var id = J.viewer.JC.lookupSpecialAtomID (name);
this.atoms[atomIndex].atomID = id;
if (id > 0 && (this).models[this.atoms[atomIndex].modelIndex].isBioModel) return;
if (this.atomNames == null) this.atomNames =  new Array (this.atoms.length);
this.atomNames[atomIndex] = name;
}, "~N,~S");
$_M(c$, "setAtomType", 
function (atomIndex, type) {
if (this.atomTypes == null) this.atomTypes =  new Array (this.atoms.length);
this.atomTypes[atomIndex] = type;
}, "~N,~S");
$_M(c$, "setAtomNumber", 
function (atomIndex, atomno) {
if (this.atomSerials == null) {
this.atomSerials =  Clazz.newIntArray (this.atoms.length, 0);
}this.atomSerials[atomIndex] = atomno;
return true;
}, "~N,~N");
$_M(c$, "setOccupancy", 
function (atomIndex, occupancy) {
if (this.occupancies == null) {
if (occupancy == 100) return false;
this.occupancies =  Clazz.newByteArray (this.atoms.length, 0);
for (var i = this.atoms.length; --i >= 0; ) this.occupancies[i] = 100;

}this.occupancies[atomIndex] = (occupancy > 255 ? 255 : occupancy < 0 ? 0 : occupancy);
return true;
}, "~N,~N");
$_M(c$, "setPartialCharge", 
function (atomIndex, partialCharge) {
if (Float.isNaN (partialCharge)) return false;
if (this.partialCharges == null) {
if (partialCharge == 0 && !Float.$valueOf (partialCharge).equals (J.modelset.AtomCollection.MINUSZERO)) return false;
this.partialCharges =  Clazz.newFloatArray (this.atoms.length, 0);
}this.partialCharges[atomIndex] = partialCharge;
return true;
}, "~N,~N");
$_M(c$, "setIonicRadius", 
function (atomIndex, radius) {
if (Float.isNaN (radius)) return false;
if (this.ionicRadii == null) {
this.ionicRadii =  Clazz.newFloatArray (this.atoms.length, 0);
}this.ionicRadii[atomIndex] = radius;
return true;
}, "~N,~N");
$_M(c$, "setBFactor", 
function (atomIndex, bfactor) {
if (Float.isNaN (bfactor)) return false;
if (this.bfactor100s == null) {
if (bfactor == 0 && this.bfactor100s == null) return false;
this.bfactor100s =  Clazz.newShortArray (this.atoms.length, 0);
}this.bfactor100s[atomIndex] = Clazz.doubleToShort ((bfactor < -327.68 ? -327.68 : bfactor > 327.67 ? 327.67 : bfactor) * 100 + (bfactor < 0 ? -0.5 : 0.5));
return true;
}, "~N,~N");
$_M(c$, "setHydrophobicity", 
function (atomIndex, value) {
if (Float.isNaN (value)) return false;
if (this.hydrophobicities == null) {
this.hydrophobicities =  Clazz.newFloatArray (this.atoms.length, 0);
for (var i = 0; i < this.atoms.length; i++) this.hydrophobicities[i] = J.util.Elements.getHydrophobicity (this.atoms[i].getGroupID ());

}this.hydrophobicities[atomIndex] = value;
return true;
}, "~N,~N");
$_M(c$, "setAtomData", 
function (type, name, dataString, isDefault) {
var fData = null;
var bs = null;
switch (type) {
case 2:
this.loadCoordinates (dataString, false, !isDefault);
return;
case 12:
this.loadCoordinates (dataString, true, true);
return;
case 14:
fData =  Clazz.newFloatArray (this.atomCount, 0);
bs = J.util.BSUtil.newBitSet (this.atomCount);
break;
}
var lines = J.util.Parser.markLines (dataString, ';');
var n = 0;
try {
var nData = J.util.Parser.parseInt (dataString.substring (0, lines[0] - 1));
for (var i = 1; i <= nData; i++) {
var tokens = J.util.Parser.getTokens (J.util.Parser.parseTrimmed (dataString.substring (lines[i], lines[i + 1] - 1)));
var atomIndex = J.util.Parser.parseInt (tokens[0]) - 1;
if (atomIndex < 0 || atomIndex >= this.atomCount) continue;
var atom = this.atoms[atomIndex];
n++;
var pt = tokens.length - 1;
var x = J.util.Parser.parseFloatStr (tokens[pt]);
switch (type) {
case 14:
fData[atomIndex] = x;
bs.set (atomIndex);
continue;
case 13:
this.setAtomNumber (atomIndex, Clazz.floatToInt (x));
break;
case 0:
this.setAtomName (atomIndex, tokens[pt]);
break;
case 1:
this.setAtomType (atomIndex, tokens[pt]);
break;
case 3:
atom.setAtomicAndIsotopeNumber (Clazz.floatToInt (x));
atom.setPaletteID (J.constant.EnumPalette.CPK.id);
atom.setColixAtom (this.viewer.getColixAtomPalette (atom, J.constant.EnumPalette.CPK.id));
break;
case 4:
atom.setFormalCharge (Clazz.floatToInt (x));
break;
case 5:
this.setHydrophobicity (atomIndex, x);
break;
case 6:
this.setIonicRadius (atomIndex, x);
break;
case 8:
this.setPartialCharge (atomIndex, x);
break;
case 9:
this.setBFactor (atomIndex, x);
break;
case 10:
atom.setValence (Clazz.floatToInt (x));
break;
case 11:
atom.setRadius (x);
break;
}
this.taintAtom (atomIndex, type);
}
if (type == 14 && n > 0) this.viewer.setData (name, [name, fData, bs, Integer.$valueOf (1)], 0, 0, 0, 0, 0);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("AtomCollection.loadData error: " + e);
} else {
throw e;
}
}
}, "~N,~S,~S,~B");
$_M(c$, "loadCoordinates", 
($fz = function (data, isVibrationVectors, doTaint) {
var lines = J.util.Parser.markLines (data, ';');
var v = (isVibrationVectors ?  new J.util.V3 () : null);
try {
var nData = J.util.Parser.parseInt (data.substring (0, lines[0] - 1));
for (var i = 1; i <= nData; i++) {
var tokens = J.util.Parser.getTokens (J.util.Parser.parseTrimmed (data.substring (lines[i], lines[i + 1])));
var atomIndex = J.util.Parser.parseInt (tokens[0]) - 1;
var x = J.util.Parser.parseFloatStr (tokens[3]);
var y = J.util.Parser.parseFloatStr (tokens[4]);
var z = J.util.Parser.parseFloatStr (tokens[5]);
if (isVibrationVectors) {
v.set (x, y, z);
this.setAtomVibrationVector (atomIndex, v);
} else {
this.setAtomCoord (atomIndex, x, y, z);
if (!doTaint) this.untaint (atomIndex, 2);
}}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("Frame.loadCoordinate error: " + e);
} else {
throw e;
}
}
}, $fz.isPrivate = true, $fz), "~S,~B,~B");
$_M(c$, "validateBspf", 
function (isValid) {
if (this.bspf != null) this.bspf.validate (isValid);
}, "~B");
$_M(c$, "validateBspfForModel", 
function (modelIndex, isValid) {
if (this.bspf != null) this.bspf.validateModel (modelIndex, isValid);
}, "~N,~B");
$_M(c$, "setPreserveState", 
function (TF) {
this.preserveState = TF;
}, "~B");
c$.getUserSettableType = $_M(c$, "getUserSettableType", 
function (dataType) {
var isExplicit = (dataType.indexOf ("property_") == 0);
var check = (isExplicit ? dataType.substring (9) : dataType);
for (var i = 0; i < 14; i++) if (J.modelset.AtomCollection.userSettableValues[i].equalsIgnoreCase (check)) return i;

return (isExplicit ? 14 : -1);
}, "~S");
$_M(c$, "getTaintedAtoms", 
function (type) {
return this.tainted == null ? null : this.tainted[type];
}, "~N");
$_M(c$, "taintAtoms", 
function (bsAtoms, type) {
this.canSkipLoad = false;
if (!this.preserveState) return;
for (var i = bsAtoms.nextSetBit (0); i >= 0; i = bsAtoms.nextSetBit (i + 1)) this.taintAtom (i, type);

}, "J.util.BS,~N");
$_M(c$, "taintAtom", 
function (atomIndex, type) {
if (!this.preserveState) return;
if (this.tainted == null) this.tainted =  new Array (14);
if (this.tainted[type] == null) this.tainted[type] = J.util.BSUtil.newBitSet (this.atomCount);
this.tainted[type].set (atomIndex);
if (type == 2) this.validateBspfForModel ((this).models[this.atoms[atomIndex].modelIndex].trajectoryBaseIndex, false);
}, "~N,~N");
$_M(c$, "untaint", 
($fz = function (atomIndex, type) {
if (!this.preserveState) return;
if (this.tainted == null || this.tainted[type] == null) return;
this.tainted[type].clear (atomIndex);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setTaintedAtoms", 
function (bs, type) {
if (!this.preserveState) return;
if (bs == null) {
if (this.tainted == null) return;
this.tainted[type] = null;
return;
}if (this.tainted == null) this.tainted =  new Array (14);
if (this.tainted[type] == null) this.tainted[type] = J.util.BSUtil.newBitSet (this.atomCount);
J.util.BSUtil.copy2 (bs, this.tainted[type]);
}, "J.util.BS,~N");
$_M(c$, "unTaintAtoms", 
function (bs, type) {
if (this.tainted == null || this.tainted[type] == null) return;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) this.tainted[type].clear (i);

if (this.tainted[type].nextSetBit (0) < 0) this.tainted[type] = null;
}, "J.util.BS,~N");
$_M(c$, "findNearest2", 
function (x, y, closest, bsNot, min) {
var champion = null;
for (var i = this.atomCount; --i >= 0; ) {
if (bsNot != null && bsNot.get (i)) continue;
var contender = this.atoms[i];
if (contender.isClickable () && this.isCursorOnTopOf (contender, x, y, min, champion)) champion = contender;
}
closest[0] = champion;
}, "~N,~N,~A,J.util.BS,~N");
$_M(c$, "isCursorOnTopOf", 
function (contender, x, y, radius, champion) {
return contender.screenZ > 1 && !this.g3d.isClippedZ (contender.screenZ) && this.g3d.isInDisplayRange (contender.screenX, contender.screenY) && contender.isCursorOnTopOf (x, y, radius, champion);
}, "J.modelset.Atom,~N,~N,~N,J.modelset.Atom");
$_M(c$, "findAtomsInRectangle", 
function (rect, bsModels) {
this.bsFoundRectangle.and (this.bsEmpty);
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
if (bsModels.get (atom.modelIndex) && atom.isVisible (0) && rect.contains (atom.screenX, atom.screenY)) this.bsFoundRectangle.set (i);
}
return this.bsFoundRectangle;
}, "J.util.Rectangle,J.util.BS");
$_M(c$, "fillADa", 
function (atomData, mode) {
atomData.atomXyz = this.atoms;
atomData.atomCount = this.atomCount;
atomData.atomicNumber =  Clazz.newIntArray (this.atomCount, 0);
var includeRadii = ((mode & 2) != 0);
if (includeRadii) atomData.atomRadius =  Clazz.newFloatArray (this.atomCount, 0);
var isMultiModel = ((mode & 16) != 0);
for (var i = 0; i < this.atomCount; i++) {
var atom = this.atoms[i];
if (atom.isDeleted () || !isMultiModel && atomData.modelIndex >= 0 && atom.modelIndex != atomData.firstModelIndex) {
if (atomData.bsIgnored == null) atomData.bsIgnored =  new J.util.BS ();
atomData.bsIgnored.set (i);
continue;
}atomData.atomicNumber[i] = atom.getElementNumber ();
atomData.lastModelIndex = atom.modelIndex;
if (includeRadii) atomData.atomRadius[i] = this.getWorkingRadius (atom, atomData);
}
}, "J.atomdata.AtomData,~N");
$_M(c$, "getWorkingRadius", 
($fz = function (atom, atomData) {
var r = 0;
var rd = atomData.radiusData;
switch (rd.factorType) {
case J.atomdata.RadiusData.EnumType.ABSOLUTE:
r = rd.value;
break;
case J.atomdata.RadiusData.EnumType.FACTOR:
case J.atomdata.RadiusData.EnumType.OFFSET:
switch (rd.vdwType) {
case J.constant.EnumVdw.IONIC:
r = atom.getBondingRadiusFloat ();
break;
case J.constant.EnumVdw.ADPMAX:
r = atom.getADPMinMax (true);
break;
case J.constant.EnumVdw.ADPMIN:
r = atom.getADPMinMax (false);
break;
default:
r = atom.getVanderwaalsRadiusFloat (this.viewer, atomData.radiusData.vdwType);
}
if (rd.factorType === J.atomdata.RadiusData.EnumType.FACTOR) r *= rd.value;
 else r += rd.value;
}
return r + rd.valueExtended;
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,J.atomdata.AtomData");
$_M(c$, "calculateHydrogens", 
function (bs, nTotal, doAll, justCarbon, vConnect) {
var z =  new J.util.V3 ();
var x =  new J.util.V3 ();
var hAtoms =  new Array (this.atomCount);
var bsDeleted = this.viewer.getDeletedAtoms ();
var pt;
var nH = 0;
if (bs != null) for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (bsDeleted != null && bsDeleted.get (i)) continue;
var atom = this.atoms[i];
var atomicNumber = atom.getElementNumber ();
if (justCarbon && atomicNumber != 6) continue;
var dHX = (atomicNumber <= 6 ? 1.1 : atomicNumber <= 10 ? 1.0 : 1.3);
switch (atomicNumber) {
case 7:
case 8:
dHX = 1.0;
break;
case 6:
}
if (doAll && atom.getCovalentHydrogenCount () > 0) continue;
var n = this.getImplicitHydrogenCount (atom, false);
if (n == 0) continue;
var targetValence = this.aaRet[0];
var hybridization = this.aaRet[2];
var nBonds = this.aaRet[3];
hAtoms[i] =  new Array (n);
var hPt = 0;
if (nBonds == 0) {
switch (n) {
case 4:
z.set (0.635, 0.635, 0.635);
pt = J.util.P3.newP (z);
pt.add (atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
case 3:
z.set (-0.635, -0.635, 0.635);
pt = J.util.P3.newP (z);
pt.add (atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
case 2:
z.set (-0.635, 0.635, -0.635);
pt = J.util.P3.newP (z);
pt.add (atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
case 1:
z.set (0.635, -0.635, -0.635);
pt = J.util.P3.newP (z);
pt.add (atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
}
} else {
switch (n) {
default:
break;
case 3:
this.getHybridizationAndAxes (i, atomicNumber, z, x, "sp3b", false, true);
pt =  new J.util.P3 ();
pt.scaleAdd2 (dHX, z, atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
this.getHybridizationAndAxes (i, atomicNumber, z, x, "sp3c", false, true);
pt =  new J.util.P3 ();
pt.scaleAdd2 (dHX, z, atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
this.getHybridizationAndAxes (i, atomicNumber, z, x, "sp3d", false, true);
pt =  new J.util.P3 ();
pt.scaleAdd2 (dHX, z, atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
break;
case 2:
var isEne = (hybridization == 2 || atomicNumber == 5 || nBonds == 1 && targetValence == 4 || atomicNumber == 7 && this.isAdjacentSp2 (atom));
this.getHybridizationAndAxes (i, atomicNumber, z, x, (isEne ? "sp2b" : targetValence == 3 ? "sp3c" : "lpa"), false, true);
pt = J.util.P3.newP (z);
pt.scaleAdd2 (dHX, z, atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
this.getHybridizationAndAxes (i, atomicNumber, z, x, (isEne ? "sp2c" : targetValence == 3 ? "sp3d" : "lpb"), false, true);
pt = J.util.P3.newP (z);
pt.scaleAdd2 (dHX, z, atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
break;
case 1:
switch (targetValence - nBonds) {
case 1:
if (atomicNumber == 8 && atom === atom.getGroup ().getCarbonylOxygenAtom ()) {
hAtoms[i] = null;
continue;
}if (this.getHybridizationAndAxes (i, atomicNumber, z, x, (hybridization == 2 || atomicNumber == 5 || atomicNumber == 7 && this.isAdjacentSp2 (atom) ? "sp2c" : "sp3d"), true, false) != null) {
pt = J.util.P3.newP (z);
pt.scaleAdd2 (dHX, z, atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
} else {
hAtoms[i] =  new Array (0);
}break;
case 2:
this.getHybridizationAndAxes (i, atomicNumber, z, x, (targetValence == 4 ? "sp2c" : "sp2b"), false, false);
pt = J.util.P3.newP (z);
pt.scaleAdd2 (dHX, z, atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
break;
case 3:
this.getHybridizationAndAxes (i, atomicNumber, z, x, "spb", false, true);
pt = J.util.P3.newP (z);
pt.scaleAdd2 (dHX, z, atom);
hAtoms[i][hPt++] = pt;
if (vConnect != null) vConnect.addLast (atom);
break;
}
}
}nH += hPt;
}
nTotal[0] = nH;
return hAtoms;
}, "J.util.BS,~A,~B,~B,J.util.JmolList");
$_M(c$, "isAdjacentSp2", 
($fz = function (atom) {
var bonds = atom.bonds;
for (var i = 0; i < bonds.length; i++) {
var b2 = bonds[i].getOtherAtom (atom).bonds;
for (var j = 0; j < b2.length; j++) switch (b2[j].order) {
case 515:
case 514:
case 2:
case 3:
return true;
}

}
return false;
}, $fz.isPrivate = true, $fz), "J.modelset.Atom");
$_M(c$, "getImplicitHydrogenCount", 
function (atom, allowNegative) {
var targetValence = atom.getTargetValence ();
if (targetValence < 0) return 0;
var charge = atom.getFormalCharge ();
if (this.aaRet == null) this.aaRet =  Clazz.newIntArray (4, 0);
this.aaRet[0] = targetValence;
this.aaRet[1] = charge;
this.aaRet[2] = 0;
this.aaRet[3] = atom.getCovalentBondCount ();
var model = (this).models[atom.modelIndex];
var s = (model.isBioModel && !model.isPdbWithMultipleBonds ? atom.group.getGroup3 () : null);
if (s != null && charge == 0) {
if (J.viewer.JC.getAminoAcidValenceAndCharge (s, atom.getAtomName (), this.aaRet)) {
targetValence = this.aaRet[0];
charge = this.aaRet[1];
}}if (charge != 0) {
targetValence += (targetValence == 4 ? -Math.abs (charge) : charge);
this.aaRet[0] = targetValence;
}var n = targetValence - atom.getValence ();
return (n < 0 && !allowNegative ? 0 : n);
}, "J.modelset.Atom,~B");
$_M(c$, "fixFormalCharges", 
function (bs) {
var n = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var a = this.atoms[i];
var nH = this.getImplicitHydrogenCount (a, true);
if (nH != 0) {
var c0 = a.getFormalCharge ();
var c = c0 - nH;
a.setFormalCharge (c);
this.taintAtom (i, 4);
if (J.util.Logger.debugging) J.util.Logger.debug ("atom " + a + " formal charge " + c0 + " -> " + c);
n++;
}}
return n;
}, "J.util.BS");
$_M(c$, "getHybridizationAndAxes", 
function (atomIndex, atomicNumber, z, x, lcaoTypeRaw, hybridizationCompatible, doAlignZ) {
var lcaoType = (lcaoTypeRaw.length > 0 && lcaoTypeRaw.charAt (0) == '-' ? lcaoTypeRaw.substring (1) : lcaoTypeRaw);
if (lcaoTypeRaw.indexOf ("d") >= 0 && !lcaoTypeRaw.endsWith ("sp3d")) return this.getHybridizationAndAxesD (atomIndex, z, x, lcaoType);
var atom = this.atoms[atomIndex];
if (atomicNumber == 0) atomicNumber = atom.getElementNumber ();
var attached = this.getAttached (atom, 4, hybridizationCompatible);
var nAttached = attached.length;
var pt = lcaoType.charCodeAt (lcaoType.length - 1) - 97;
if (pt < 0 || pt > 6) pt = 0;
var vTemp =  new J.util.V3 ();
z.set (0, 0, 0);
x.set (0, 0, 0);
var v =  new Array (4);
for (var i = 0; i < nAttached; i++) {
v[i] = J.util.V3.newVsub (atom, attached[i]);
v[i].normalize ();
z.add (v[i]);
}
if (nAttached > 0) x.setT (v[0]);
var isPlanar = false;
if (nAttached >= 3) {
if (x.angle (v[1]) < 2.984513) vTemp.cross (x, v[1]);
 else vTemp.cross (x, v[2]);
vTemp.normalize ();
var vTemp2 =  new J.util.V3 ();
if (v[1].angle (v[2]) < 2.984513) vTemp2.cross (v[1], v[2]);
 else vTemp2.cross (x, v[2]);
vTemp2.normalize ();
isPlanar = (Math.abs (vTemp2.dot (vTemp)) >= 0.95);
}var isSp3 = (lcaoType.indexOf ("sp3") == 0);
var isSp2 = (!isSp3 && lcaoType.indexOf ("sp2") == 0);
var isSp = (!isSp3 && !isSp2 && lcaoType.indexOf ("sp") == 0);
var isP = (lcaoType.indexOf ("p") == 0);
var isLp = (lcaoType.indexOf ("lp") == 0);
var hybridization = null;
if (hybridizationCompatible) {
if (nAttached == 0) return null;
if (isSp3) {
if (pt > 3 || nAttached > 4) return null;
} else if (isSp2) {
if (pt > 2 || nAttached > 3) return null;
} else if (isSp) {
if (pt > 1 || nAttached > 2) return null;
}switch (nAttached) {
case 1:
if (atomicNumber == 1 && !isSp3) return null;
if (isSp3) {
hybridization = "sp3";
break;
}switch (attached[0].getCovalentBondCount ()) {
case 1:
if (attached[0].getValence () != 2) {
hybridization = "sp";
break;
}case 2:
hybridization = (isSp ? "sp" : "sp2");
break;
case 3:
if (!isSp2 && !isP) return null;
hybridization = "sp2";
break;
}
break;
case 2:
if (z.length () < 0.1) {
if (lcaoType.indexOf ("2") >= 0 || lcaoType.indexOf ("3") >= 0) return null;
hybridization = "sp";
break;
}hybridization = (isSp3 ? "sp3" : "sp2");
if (lcaoType.indexOf ("sp") == 0) {
break;
}if (isLp) {
hybridization = "lp";
break;
}hybridization = lcaoType;
break;
default:
if (isPlanar) {
hybridization = "sp2";
} else {
if (isLp && nAttached == 3) {
hybridization = "lp";
break;
}hybridization = "sp3";
}}
if (hybridization == null) return null;
if (lcaoType.indexOf ("p") == 0) {
if (hybridization === "sp3") return null;
} else if (lcaoType.indexOf (hybridization) < 0) {
return null;
}}if (pt < nAttached && !lcaoType.startsWith ("p") && !lcaoType.startsWith ("l")) {
z.sub2 (attached[pt], atom);
z.normalize ();
return hybridization;
}switch (nAttached) {
case 0:
if (lcaoType.equals ("sp3c") || lcaoType.equals ("sp2d") || lcaoType.equals ("lpa")) {
z.set (-0.5, -0.7, 1);
x.set (1, 0, 0);
} else if (lcaoType.equals ("sp3b") || lcaoType.equals ("lpb")) {
z.set (0.5, -0.7, -1.0);
x.set (1, 0, 0);
} else if (lcaoType.equals ("sp3a")) {
z.set (0, 1, 0);
x.set (1, 0, 0);
} else {
z.set (0, 0, 1);
x.set (1, 0, 0);
}break;
case 1:
vTemp.setT (J.modelset.AtomCollection.vRef);
x.cross (vTemp, z);
if (isSp3) {
for (var i = 0; i < attached[0].bonds.length; i++) {
if (attached[0].bonds[i].isCovalent () && attached[0].getBondedAtomIndex (i) != atom.index) {
x.sub2 (attached[0], attached[0].bonds[i].getOtherAtom (attached[0]));
x.cross (z, x);
if (x.length () == 0) continue;
x.cross (x, z);
break;
}}
x.normalize ();
if (Float.isNaN (x.x)) {
x.setT (J.modelset.AtomCollection.vRef);
x.cross (x, z);
}vTemp.cross (z, x);
vTemp.normalize ();
z.normalize ();
x.scaleAdd2 (2.828, x, z);
if (pt != 3) {
x.normalize ();
var a = J.util.AxisAngle4f.new4 (z.x, z.y, z.z, (pt == 2 ? 1 : -1) * 2.09439507);
var m =  new J.util.Matrix3f ();
m.setIdentity ();
m.setAA (a);
m.transform (x);
}z.setT (x);
x.cross (vTemp, z);
break;
}vTemp.cross (x, z);
switch (attached[0].getCovalentBondCount ()) {
case 1:
if (attached[0].getValence () != 2) {
break;
}case 2:
var isCumulated = false;
var a0 = attached[0];
x.setT (z);
vTemp.setT (J.modelset.AtomCollection.vRef);
while (a0 != null && a0.getCovalentBondCount () == 2) {
var bonds = a0.bonds;
var a = null;
isCumulated = !isCumulated;
for (var i = 0; i < bonds.length; i++) if (bonds[i].isCovalent ()) {
a = bonds[i].getOtherAtom (a0);
if (a !== atom) {
vTemp.sub2 (a, a0);
break;
}}
vTemp.cross (vTemp, x);
if (vTemp.length () > 0.1 || a.getCovalentBondCount () != 2) break;
atom = a0;
a0 = a;
}
if (vTemp.length () > 0.1) {
z.cross (vTemp, x);
z.normalize ();
if (pt == 1) z.scale (-1);
z.scale (J.modelset.AtomCollection.sqrt3_2);
z.scaleAdd2 (0.5, x, z);
if (isP) {
vTemp.cross (z, x);
z.setT (vTemp);
vTemp.setT (x);
}x.cross (vTemp, z);
} else {
z.setT (x);
x.cross (J.modelset.AtomCollection.vRef, x);
}break;
case 3:
this.getHybridizationAndAxes (attached[0].index, 0, x, vTemp, "pz", false, doAlignZ);
vTemp.setT (x);
if (isSp2) {
x.cross (x, z);
if (pt == 1) x.scale (-1);
x.scale (J.modelset.AtomCollection.sqrt3_2);
z.scaleAdd2 (0.5, z, x);
} else {
vTemp.setT (z);
z.setT (x);
}x.cross (vTemp, z);
break;
}
break;
case 2:
if (z.length () < 0.1) {
if (!lcaoType.equals ("pz")) {
var a = attached[0];
var ok = (a.getCovalentBondCount () == 3);
if (!ok) ok = ((a = attached[1]).getCovalentBondCount () == 3);
if (ok) {
this.getHybridizationAndAxes (a.index, 0, x, z, "pz", false, doAlignZ);
if (lcaoType.equals ("px")) x.scale (-1);
z.setT (v[0]);
break;
}vTemp.setT (J.modelset.AtomCollection.vRef);
z.cross (vTemp, x);
vTemp.cross (z, x);
}z.setT (x);
x.cross (vTemp, z);
break;
}vTemp.cross (z, x);
if (isSp2) {
x.cross (z, vTemp);
break;
}if (isSp3 || isLp) {
vTemp.normalize ();
z.normalize ();
if (!lcaoType.equals ("lp")) {
if (pt == 0 || pt == 2) z.scaleAdd2 (-1.2, vTemp, z);
 else z.scaleAdd2 (1.2, vTemp, z);
}x.cross (z, vTemp);
break;
}x.cross (z, vTemp);
z.setT (vTemp);
if (z.z < 0) {
z.scale (-1);
x.scale (-1);
}break;
default:
if (isSp3) break;
if (!isPlanar) {
x.cross (z, x);
break;
}z.setT (vTemp);
if (z.z < 0 && doAlignZ) {
z.scale (-1);
x.scale (-1);
}}
x.normalize ();
z.normalize ();
return hybridization;
}, "~N,~N,J.util.V3,J.util.V3,~S,~B,~B");
$_M(c$, "getHybridizationAndAxesD", 
($fz = function (atomIndex, z, x, lcaoType) {
if (lcaoType.startsWith ("sp3d2")) lcaoType = "d2sp3" + (lcaoType.length == 5 ? "a" : lcaoType.substring (5));
if (lcaoType.startsWith ("sp3d")) lcaoType = "dsp3" + (lcaoType.length == 4 ? "a" : lcaoType.substring (4));
if (lcaoType.equals ("d2sp3") || lcaoType.equals ("dsp3")) lcaoType += "a";
var isTrigonal = lcaoType.startsWith ("dsp3");
var pt = lcaoType.charCodeAt (lcaoType.length - 1) - 97;
if (z != null && (!isTrigonal && (pt > 5 || !lcaoType.startsWith ("d2sp3")) || isTrigonal && pt > 4)) return null;
var atom = this.atoms[atomIndex];
var attached = this.getAttached (atom, 6, true);
if (attached == null) return (z == null ? null : "?");
var nAttached = attached.length;
if (nAttached < 3 && z != null) return null;
var isLP = (pt >= nAttached);
var nAngles = Clazz.doubleToInt (nAttached * (nAttached - 1) / 2);
var angles = J.util.ArrayUtil.newInt2 (nAngles);
var ntypes =  Clazz.newIntArray (3, 0);
var typePtrs =  Clazz.newIntArray (3, nAngles, 0);
var n = 0;
var _90 = 0;
var _120 = 1;
var _180 = 2;
var n120_atom0 = 0;
for (var i = 0; i < nAttached - 1; i++) for (var j = i + 1; j < nAttached; j++) {
var angle = J.util.Measure.computeAngleABC (attached[i], atom, attached[j], true);
var itype = (angle < 105 ? _90 : angle >= 150 ? _180 : _120);
typePtrs[itype][ntypes[itype]] = n;
ntypes[itype]++;
angles[n++] = [i, j];
if (i == 0 && itype == _120) n120_atom0++;
}

n = ntypes[_90] * 100 + ntypes[_120] * 10 + ntypes[_180];
if (z == null) {
switch (n) {
default:
return "";
case 0:
return "";
case 1:
return "linear";
case 100:
case 10:
return "bent";
case 111:
case 201:
return "T-shaped";
case 30:
case 120:
case 210:
case 300:
if (Math.abs (J.util.Measure.computeTorsion (attached[0], atom, attached[1], attached[2], true)) > 162) return "trigonal planar";
return "trigonal pyramidal";
case 330:
return (n120_atom0 % 2 == 1 ? "tetrahedral" : "uncapped trigonal pyramid");
case 60:
case 150:
case 240:
return "tetrahedral";
case 402:
return "square planar";
case 411:
case 501:
return "see-saw";
case 631:
return "trigonal bipyramidal";
case 802:
return "uncapped square pyramid";
case 1203:
return "octahedral";
}
}switch (n) {
default:
return null;
case 201:
break;
case 210:
case 330:
case 411:
case 631:
if (!isTrigonal) return null;
break;
case 300:
case 402:
case 501:
case 802:
case 1203:
if (isTrigonal) return null;
break;
}
if (isLP) {
var a;
var bs;
if (isTrigonal) {
switch (ntypes[_120]) {
case 0:
z.sub2 (attached[angles[typePtrs[_90][0]][0]], atom);
x.sub2 (attached[angles[typePtrs[_90][0]][1]], atom);
z.cross (z, x);
z.normalize ();
if (pt == 4) z.scale (-1);
bs = this.findNotAttached (nAttached, angles, typePtrs[_180], ntypes[_180]);
var i = bs.nextSetBit (0);
x.sub2 (attached[i], atom);
x.normalize ();
x.scale (0.5);
z.scaleAdd2 (J.modelset.AtomCollection.sqrt3_2, z, x);
pt = -1;
break;
case 1:
if (pt == 4) {
a = angles[typePtrs[_120][0]];
z.add2 (attached[a[0]], attached[a[1]]);
z.scaleAdd2 (-2, atom, z);
pt = -1;
} else {
bs = this.findNotAttached (nAttached, angles, typePtrs[_120], ntypes[_120]);
pt = bs.nextSetBit (0);
}break;
default:
bs = this.findNotAttached (nAttached, angles, typePtrs[_120], ntypes[_120]);
pt = bs.nextSetBit (0);
}
} else {
var isPlanar = false;
if (nAttached == 4) {
switch (ntypes[_180]) {
case 1:
bs = this.findNotAttached (nAttached, angles, typePtrs[_180], ntypes[_180]);
var i = bs.nextSetBit (0);
if (pt == 4) pt = i;
 else pt = bs.nextSetBit (i + 1);
break;
default:
isPlanar = true;
}
} else {
bs = this.findNotAttached (nAttached, angles, typePtrs[_180], ntypes[_180]);
var i = bs.nextSetBit (0);
for (var j = nAttached; j < pt && i >= 0; j++) i = bs.nextSetBit (i + 1);

if (i == -1) isPlanar = true;
 else pt = i;
}if (isPlanar) {
z.sub2 (attached[angles[typePtrs[_90][0]][0]], atom);
x.sub2 (attached[angles[typePtrs[_90][0]][1]], atom);
z.cross (z, x);
if (pt == 4) z.scale (-1);
pt = -1;
}}}if (pt >= 0) z.sub2 (attached[pt], atom);
if (isLP) z.scale (-1);
z.normalize ();
return (isTrigonal ? "dsp3" : "d2sp3");
}, $fz.isPrivate = true, $fz), "~N,J.util.V3,J.util.V3,~S");
$_M(c$, "getAttached", 
($fz = function (atom, nMax, doSort) {
var nAttached = atom.getCovalentBondCount ();
if (nAttached > nMax) return null;
var attached =  new Array (nAttached);
if (nAttached > 0) {
var bonds = atom.bonds;
var n = 0;
for (var i = 0; i < bonds.length; i++) if (bonds[i].isCovalent ()) attached[n++] = bonds[i].getOtherAtom (atom);

if (doSort) java.util.Arrays.sort (attached, Clazz.innerTypeInstance (J.modelset.AtomCollection.AtomSorter, this, null));
}return attached;
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,~N,~B");
$_M(c$, "findNotAttached", 
($fz = function (nAttached, angles, ptrs, nPtrs) {
var bs = J.util.BSUtil.newBitSet (nAttached);
bs.setBits (0, nAttached);
for (var i = 0; i < nAttached; i++) for (var j = 0; j < nPtrs; j++) {
var a = angles[ptrs[j]];
if (a[0] == i || a[1] == i) bs.clear (i);
}

return bs;
}, $fz.isPrivate = true, $fz), "~N,~A,~A,~N");
$_M(c$, "getAtomBitsMDa", 
function (tokType, specInfo) {
var bs =  new J.util.BS ();
var bsInfo;
var bsTemp;
var iSpec;
var i = 0;
switch (tokType) {
case 1095763969:
iSpec = (specInfo).intValue ();
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].getAtomNumber () == iSpec) bs.set (i);

break;
case 1087375362:
var names = "," + specInfo + ",";
for (i = this.atomCount; --i >= 0; ) {
var name = this.atoms[i].getAtomName ();
if (names.indexOf (name) >= 0) if (names.indexOf ("," + name + ",") >= 0) bs.set (i);
}
break;
case 1087375361:
var types = "," + specInfo + ",";
for (i = this.atomCount; --i >= 0; ) {
var type = this.atoms[i].getAtomType ();
if (types.indexOf (type) >= 0) if (types.indexOf ("," + type + ",") >= 0) bs.set (i);
}
break;
case 1048613:
iSpec = (specInfo).intValue ();
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].getGroupID () == iSpec) bs.set (i);

break;
case 1048609:
return J.util.BSUtil.copy (this.getChainBits ((specInfo).intValue ()));
case 1048614:
return J.util.BSUtil.copy (this.getSeqcodeBits ((specInfo).intValue (), true));
case 1613758470:
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isHetero ()) bs.set (i);

break;
case 1613758476:
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].getElementNumber () == 1) bs.set (i);

break;
case 3145744:
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isProtein ()) bs.set (i);

break;
case 3145764:
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isCarbohydrate ()) bs.set (i);

break;
case 137363468:
case 3145760:
var type = (tokType == 137363468 ? J.constant.EnumStructure.HELIX : J.constant.EnumStructure.SHEET);
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isWithinStructure (type)) bs.set (i);

break;
case 3145742:
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isNucleic ()) bs.set (i);

break;
case 3145732:
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isDna ()) bs.set (i);

break;
case 3145750:
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isRna ()) bs.set (i);

break;
case 3145746:
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isPurine ()) bs.set (i);

break;
case 3145748:
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isPyrimidine ()) bs.set (i);

break;
case 1087375365:
bsInfo = specInfo;
bsTemp =  new J.util.BS ();
for (i = bsInfo.nextSetBit (0); i >= 0; i = bsInfo.nextSetBit (i + 1)) bsTemp.set (this.getElementNumber (i));

for (i = this.atomCount; --i >= 0; ) if (bsTemp.get (this.getElementNumber (i))) bs.set (i);

break;
case 1095761938:
bsInfo = specInfo;
bsTemp =  new J.util.BS ();
for (i = bsInfo.nextSetBit (0); i >= 0; i = bsInfo.nextSetBit (i + 1)) bsTemp.set (this.atoms[i].atomSite);

for (i = this.atomCount; --i >= 0; ) if (bsTemp.get (this.atoms[i].atomSite)) bs.set (i);

break;
case 1073741824:
return this.getIdentifierOrNull (specInfo);
case 1048608:
var atomSpec = (specInfo).toUpperCase ();
if (atomSpec.indexOf ("\\?") >= 0) atomSpec = J.util.TextFormat.simpleReplace (atomSpec, "\\?", "\1");
for (i = this.atomCount; --i >= 0; ) if (this.isAtomNameMatch (this.atoms[i], atomSpec, false)) bs.set (i);

break;
case 1048607:
var spec = specInfo;
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i].isAlternateLocationMatch (spec)) bs.set (i);

break;
case 1048612:
return this.getSpecName (specInfo);
}
if (i < 0) return bs;
bsInfo = specInfo;
var iModel;
var iPolymer;
var i0 = bsInfo.nextSetBit (0);
if (i0 < 0) return bs;
i = 0;
switch (tokType) {
case 1087373318:
for (i = i0; i >= 0; i = bsInfo.nextSetBit (i + 1)) {
var j = this.atoms[i].getGroup ().selectAtoms (bs);
if (j > i) i = j;
}
break;
case 1095766028:
for (i = i0; i >= 0; i = bsInfo.nextSetBit (i + 1)) {
if (bs.get (i)) continue;
iModel = this.atoms[i].modelIndex;
bs.set (i);
for (var j = i; --j >= 0; ) if (this.atoms[j].modelIndex == iModel) bs.set (j);
 else break;

for (; ++i < this.atomCount; ) if (this.atoms[i].modelIndex == iModel) bs.set (i);
 else break;

}
break;
case 1087373316:
bsInfo = J.util.BSUtil.copy (specInfo);
for (i = bsInfo.nextSetBit (0); i >= 0; i = bsInfo.nextSetBit (i + 1)) {
var chain = this.atoms[i].getChain ();
chain.setAtomBitSet (bs);
bsInfo.andNot (bs);
}
break;
case 1095761935:
for (i = i0; i >= 0; i = bsInfo.nextSetBit (i + 1)) {
if (bs.get (i)) continue;
iPolymer = this.atoms[i].getPolymerIndexInModel ();
bs.set (i);
for (var j = i; --j >= 0; ) if (this.atoms[j].getPolymerIndexInModel () == iPolymer) bs.set (j);
 else break;

for (; ++i < this.atomCount; ) if (this.atoms[i].getPolymerIndexInModel () == iPolymer) bs.set (i);
 else break;

}
break;
case 1641025539:
for (i = i0; i >= 0; i = bsInfo.nextSetBit (i + 1)) {
if (bs.get (i)) continue;
var structure = this.atoms[i].getGroup ().getStructure ();
bs.set (i);
for (var j = i; --j >= 0; ) if (this.atoms[j].getGroup ().getStructure () === structure) bs.set (j);
 else break;

for (; ++i < this.atomCount; ) if (this.atoms[i].getGroup ().getStructure () === structure) bs.set (i);
 else break;

}
break;
}
if (i == 0) J.util.Logger.error ("MISSING getAtomBits entry for " + J.script.T.nameOf (tokType));
return bs;
}, "~N,~O");
$_M(c$, "getIdentifierOrNull", 
($fz = function (identifier) {
var bs = this.getSpecNameOrNull (identifier, false);
if (identifier.indexOf ("\\?") >= 0) identifier = J.util.TextFormat.simpleReplace (identifier, "\\?", "\1");
if (bs != null || identifier.indexOf ("?") > 0) return bs;
if (identifier.indexOf ("*") > 0) return this.getSpecNameOrNull (identifier, true);
var len = identifier.length;
var pt = 0;
while (pt < len && Character.isLetter (identifier.charAt (pt))) ++pt;

bs = this.getSpecNameOrNull (identifier.substring (0, pt), false);
if (pt == len) return bs;
if (bs == null) bs =  new J.util.BS ();
var pt0 = pt;
while (pt < len && Character.isDigit (identifier.charAt (pt))) ++pt;

var seqNumber = 0;
try {
seqNumber = Integer.parseInt (identifier.substring (pt0, pt));
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
return null;
} else {
throw nfe;
}
}
var insertionCode = ' ';
if (pt < len && identifier.charAt (pt) == '^') if (++pt < len) insertionCode = identifier.charAt (pt);
var seqcode = J.modelset.Group.getSeqcodeFor (seqNumber, insertionCode);
var bsInsert = this.getSeqcodeBits (seqcode, false);
if (bsInsert == null) {
if (insertionCode != ' ') bsInsert = this.getSeqcodeBits (Character.toUpperCase (identifier.charAt (pt)).charCodeAt (0), false);
if (bsInsert == null) return null;
pt++;
}bs.and (bsInsert);
if (pt >= len) return bs;
if (pt != len - 1) return null;
bs.and (this.getChainBits (identifier.charCodeAt (pt)));
return bs;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getSpecName", 
($fz = function (name) {
var bs = this.getSpecNameOrNull (name, false);
if (bs != null) return bs;
if (name.indexOf ("*") > 0) bs = this.getSpecNameOrNull (name, true);
return (bs == null ?  new J.util.BS () : bs);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getSpecNameOrNull", 
($fz = function (name, checkStar) {
var bs = null;
name = name.toUpperCase ();
if (name.indexOf ("\\?") >= 0) name = J.util.TextFormat.simpleReplace (name, "\\?", "\1");
for (var i = this.atomCount; --i >= 0; ) {
var g3 = this.atoms[i].getGroup3 (true);
if (g3 != null && g3.length > 0) {
if (J.util.TextFormat.isMatch (g3, name, checkStar, true)) {
if (bs == null) bs = J.util.BSUtil.newBitSet (i + 1);
bs.set (i);
while (--i >= 0 && this.atoms[i].getGroup3 (true).equals (g3)) bs.set (i);

i++;
}} else if (this.isAtomNameMatch (this.atoms[i], name, checkStar)) {
if (bs == null) bs = J.util.BSUtil.newBitSet (i + 1);
bs.set (i);
}}
return bs;
}, $fz.isPrivate = true, $fz), "~S,~B");
$_M(c$, "isAtomNameMatch", 
($fz = function (atom, strPattern, checkStar) {
return J.util.TextFormat.isMatch (atom.getAtomName ().toUpperCase (), strPattern, checkStar, false);
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,~S,~B");
$_M(c$, "getSeqcodeBits", 
function (seqcode, returnEmpty) {
var bs =  new J.util.BS ();
var seqNum = J.modelset.Group.getSeqNumberFor (seqcode);
var haveSeqNumber = (seqNum != 2147483647);
var isEmpty = true;
var insCode = J.modelset.Group.getInsertionCodeChar (seqcode);
switch (insCode) {
case '?':
for (var i = this.atomCount; --i >= 0; ) {
var atomSeqcode = this.atoms[i].getSeqcode ();
if (!haveSeqNumber || seqNum == J.modelset.Group.getSeqNumberFor (atomSeqcode) && J.modelset.Group.getInsertionCodeFor (atomSeqcode) != 0) {
bs.set (i);
isEmpty = false;
}}
break;
default:
for (var i = this.atomCount; --i >= 0; ) {
var atomSeqcode = this.atoms[i].getSeqcode ();
if (seqcode == atomSeqcode || !haveSeqNumber && seqcode == J.modelset.Group.getInsertionCodeFor (atomSeqcode) || insCode == '*' && seqNum == J.modelset.Group.getSeqNumberFor (atomSeqcode)) {
bs.set (i);
isEmpty = false;
}}
}
return (!isEmpty || returnEmpty ? bs : null);
}, "~N,~B");
$_M(c$, "getChainBits", 
function (chainID) {
var caseSensitive = chainID < 256 && this.viewer.getBoolean (603979822);
if (!caseSensitive) chainID = J.modelset.AtomCollection.chainToUpper (chainID);
var bs =  new J.util.BS ();
var bsDone = J.util.BSUtil.newBitSet (this.atomCount);
var id;
for (var i = bsDone.nextClearBit (0); i < this.atomCount; i = bsDone.nextClearBit (i + 1)) {
var chain = this.atoms[i].getChain ();
if (chainID == (id = chain.chainID) || !caseSensitive && chainID == J.modelset.AtomCollection.chainToUpper (id)) {
chain.setAtomBitSet (bs);
bsDone.or (bs);
} else {
chain.setAtomBitSet (bsDone);
}}
return bs;
}, "~N");
c$.chainToUpper = $_M(c$, "chainToUpper", 
function (chainID) {
{
return String.fromCharCode(chainID).toUpperCase().charCodeAt(0);
}}, "~N");
$_M(c$, "getAtomIndices", 
function (bs) {
var n = 0;
var indices =  Clazz.newIntArray (this.atomCount, 0);
for (var j = bs.nextSetBit (0); j >= 0 && j < this.atomCount; j = bs.nextSetBit (j + 1)) indices[j] = ++n;

return indices;
}, "J.util.BS");
$_M(c$, "getAtomsWithin", 
function (distance, plane) {
var bsResult =  new J.util.BS ();
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
var d = J.util.Measure.distanceToPlane (plane, atom);
if (distance > 0 && d >= -0.1 && d <= distance || distance < 0 && d <= 0.1 && d >= distance || distance == 0 && Math.abs (d) < 0.01) bsResult.set (atom.index);
}
return bsResult;
}, "~N,J.util.P4");
$_M(c$, "getAtomsWithinBs", 
function (distance, points, bsInclude) {
var bsResult =  new J.util.BS ();
if (points.length == 0 || bsInclude != null && bsInclude.cardinality () == 0) return bsResult;
if (bsInclude == null) bsInclude = J.util.BSUtil.setAll (points.length);
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
for (var j = bsInclude.nextSetBit (0); j >= 0; j = bsInclude.nextSetBit (j + 1)) if (atom.distance (points[j]) < distance) {
bsResult.set (i);
break;
}
}
return bsResult;
}, "~N,~A,J.util.BS");
$_M(c$, "getVisibleSet", 
function () {
var bs =  new J.util.BS ();
for (var i = this.atomCount; --i >= 0; ) if (this.atoms[i].isVisible (0)) bs.set (i);

return bs;
});
$_M(c$, "getClickableSet", 
function () {
var bs =  new J.util.BS ();
for (var i = this.atomCount; --i >= 0; ) if (this.atoms[i].isClickable ()) bs.set (i);

return bs;
});
$_M(c$, "isModulated", 
function (i) {
return this.bsModulated != null && this.bsModulated.get (i);
}, "~N");
$_M(c$, "deleteModelAtoms", 
function (firstAtomIndex, nAtoms, bsAtoms) {
this.atoms = J.util.ArrayUtil.deleteElements (this.atoms, firstAtomIndex, nAtoms);
this.atomCount = this.atoms.length;
for (var j = firstAtomIndex; j < this.atomCount; j++) {
this.atoms[j].index = j;
this.atoms[j].modelIndex--;
}
if (this.bsModulated != null) J.util.BSUtil.deleteBits (this.bsModulated, bsAtoms);
this.deleteAtomTensors (bsAtoms);
this.atomNames = J.util.ArrayUtil.deleteElements (this.atomNames, firstAtomIndex, nAtoms);
this.atomTypes = J.util.ArrayUtil.deleteElements (this.atomTypes, firstAtomIndex, nAtoms);
this.atomSerials = J.util.ArrayUtil.deleteElements (this.atomSerials, firstAtomIndex, nAtoms);
this.bfactor100s = J.util.ArrayUtil.deleteElements (this.bfactor100s, firstAtomIndex, nAtoms);
this.hasBfactorRange = false;
this.occupancies = J.util.ArrayUtil.deleteElements (this.occupancies, firstAtomIndex, nAtoms);
this.partialCharges = J.util.ArrayUtil.deleteElements (this.partialCharges, firstAtomIndex, nAtoms);
this.atomTensorList = J.util.ArrayUtil.deleteElements (this.atomTensorList, firstAtomIndex, nAtoms);
this.vibrations = J.util.ArrayUtil.deleteElements (this.vibrations, firstAtomIndex, nAtoms);
this.nSurfaceAtoms = 0;
this.bsSurface = null;
this.surfaceDistance100s = null;
if (this.tainted != null) for (var i = 0; i < 14; i++) J.util.BSUtil.deleteBits (this.tainted[i], bsAtoms);

}, "~N,~N,J.util.BS");
$_M(c$, "getAtomIdentityInfo", 
function (i, info) {
info.put ("_ipt", Integer.$valueOf (i));
info.put ("atomIndex", Integer.$valueOf (i));
info.put ("atomno", Integer.$valueOf (this.getAtomNumber (i)));
info.put ("info", this.getAtomInfo (i, null));
info.put ("sym", this.getElementSymbol (i));
}, "~N,java.util.Map");
$_M(c$, "getAtomTensorList", 
function (i) {
return (i < 0 || this.atomTensorList == null || i >= this.atomTensorList.length ? null : this.atomTensorList[i]);
}, "~N");
$_M(c$, "deleteAtomTensors", 
($fz = function (bsAtoms) {
if (this.atomTensors == null) return;
var toDelete =  new J.util.JmolList ();
for (var key, $key = this.atomTensors.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) {
var list = this.atomTensors.get (key);
for (var i = list.size (); --i >= 0; ) {
var t = list.get (i);
if (bsAtoms.get (t.atomIndex1) || t.atomIndex2 >= 0 && bsAtoms.get (t.atomIndex2)) list.remove (i);
}
if (list.size () == 0) toDelete.addLast (key);
}
for (var i = toDelete.size (); --i >= 0; ) this.atomTensors.remove (toDelete.get (i));

}, $fz.isPrivate = true, $fz), "J.util.BS");
$_M(c$, "setAtomTensors", 
function (atomIndex, list) {
if (list == null || list.size () == 0) return;
if (this.atomTensors == null) this.atomTensors =  new java.util.Hashtable ();
if (this.atomTensorList == null) this.atomTensorList =  new Array (this.atoms.length);
this.atomTensorList = J.util.ArrayUtil.ensureLength (this.atomTensorList, this.atoms.length);
this.atomTensorList[atomIndex] = J.modelset.AtomCollection.getTensorList (list);
for (var i = list.size (); --i >= 0; ) {
var t = list.get (i);
t.atomIndex1 = atomIndex;
t.atomIndex2 = -1;
t.modelIndex = this.atoms[atomIndex].modelIndex;
this.addTensor (t, t.type);
if (t.altType != null) this.addTensor (t, t.altType);
}
}, "~N,J.util.JmolList");
c$.getTensorList = $_M(c$, "getTensorList", 
($fz = function (list) {
var pt = -1;
var haveTLS = false;
var n = list.size ();
for (var i = n; --i >= 0; ) {
var t = list.get (i);
if (t.forThermalEllipsoid) pt = i;
 else if (t.iType == 2) haveTLS = true;
}
var a =  new Array ((pt >= 0 || !haveTLS ? 0 : 1) + n);
if (pt >= 0) {
a[0] = list.get (pt);
if (list.size () == 1) return a;
}if (haveTLS) {
pt = 0;
for (var i = n; --i >= 0; ) {
var t = list.get (i);
if (t.forThermalEllipsoid) continue;
a[++pt] = t;
}
} else {
for (var i = 0; i < n; i++) a[i] = list.get (i);

}return a;
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "getAtomTensor", 
function (i, type) {
var tensors = this.getAtomTensorList (i);
if (tensors != null && type != null) {
type = type.toLowerCase ();
for (var j = 0; j < tensors.length; j++) {
var t = tensors[j];
if (t != null && (type.equals (t.type) || type.equals (t.altType))) return t;
}
}return null;
}, "~N,~S");
$_M(c$, "addTensor", 
function (t, type) {
type = type.toLowerCase ();
var tensors = this.atomTensors.get (type);
if (tensors == null) this.atomTensors.put (type, tensors =  new J.util.JmolList ());
tensors.addLast (t);
}, "J.util.Tensor,~S");
$_M(c$, "getAllAtomTensors", 
function (type) {
if (this.atomTensors == null) return null;
if (type != null) return this.atomTensors.get (type.toLowerCase ());
var list =  new J.util.JmolList ();
for (var e, $e = this.atomTensors.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) list.addAll (e.getValue ());

return list;
}, "~S");
c$.$AtomCollection$AtomSorter$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.modelset.AtomCollection, "AtomSorter", null, java.util.Comparator);
Clazz.overrideMethod (c$, "compare", 
function (a, b) {
return (a.index > b.index ? 1 : a.index < b.index ? -1 : 0);
}, "J.modelset.Atom,J.modelset.Atom");
c$ = Clazz.p0p ();
};
c$.MINUSZERO = c$.prototype.MINUSZERO = Float.$valueOf (-0.0);
Clazz.defineStatics (c$,
"TAINT_ATOMNAME", 0,
"TAINT_ATOMTYPE", 1,
"TAINT_COORD", 2,
"TAINT_ELEMENT", 3,
"TAINT_FORMALCHARGE", 4,
"TAINT_HYDROPHOBICITY", 5,
"TAINT_IONICRADIUS", 6,
"TAINT_OCCUPANCY", 7,
"TAINT_PARTIALCHARGE", 8,
"TAINT_TEMPERATURE", 9,
"TAINT_VALENCE", 10,
"TAINT_VANDERWAALS", 11,
"TAINT_VIBRATION", 12,
"TAINT_ATOMNO", 13,
"TAINT_MAX", 14,
"userSettableValues", ["atomName", "atomType", "coord", "element", "formalCharge", "hydrophobicity", "ionic", "occupany", "partialCharge", "temperature", "valence", "vanderWaals", "vibrationVector", "atomNo"]);
{
if (J.modelset.AtomCollection.userSettableValues.length != 14) J.util.Logger.error ("AtomCollection.java userSettableValues is not length TAINT_MAX!");
}c$.sqrt3_2 = c$.prototype.sqrt3_2 = (Math.sqrt (3) / 2);
c$.vRef = c$.prototype.vRef = J.util.V3.new3 (3.14159, 2.71828, 1.41421);
Clazz.defineStatics (c$,
"almost180", 2.984513);
});
