Clazz.declarePackage ("J.modelset");
Clazz.load (["J.util.JmolNode", "$.Point3fi", "J.constant.EnumPalette", "J.viewer.JC"], "J.modelset.Atom", ["java.lang.Float", "J.atomdata.RadiusData", "J.constant.EnumVdw", "J.util.C", "$.ColorUtil", "$.Elements", "$.Escape", "$.P3", "$.SB", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.altloc = '\0';
this.atomID = 0;
this.atomSite = 0;
this.group = null;
this.userDefinedVanDerWaalRadius = 0;
this.valence = 0;
this.atomicAndIsotopeNumber = 0;
this.atomSymmetry = null;
this.formalChargeAndFlags = 0;
this.madAtom = 0;
this.colixAtom = 0;
this.paletteID = 0;
this.bonds = null;
this.nBondsDisplayed = 0;
this.nBackbonesDisplayed = 0;
this.clickabilityFlags = 0;
this.shapeVisibilityFlags = 0;
Clazz.instantialize (this, arguments);
}, J.modelset, "Atom", J.util.Point3fi, J.util.JmolNode);
Clazz.prepareFields (c$, function () {
this.paletteID = J.constant.EnumPalette.CPK.id;
});
$_M(c$, "getAtomID", 
function () {
return this.atomID;
});
$_M(c$, "getBonds", 
function () {
return this.bonds;
});
$_M(c$, "setBonds", 
function (bonds) {
this.bonds = bonds;
}, "~A");
$_M(c$, "getNBackbonesDisplayed", 
function () {
return this.nBackbonesDisplayed;
});
Clazz.overrideConstructor (c$, 
function (modelIndex, atomIndex, xyz, radius, atomSymmetry, atomSite, atomicAndIsotopeNumber, formalCharge, isHetero) {
this.modelIndex = modelIndex;
this.atomSymmetry = atomSymmetry;
this.atomSite = atomSite;
this.index = atomIndex;
this.atomicAndIsotopeNumber = atomicAndIsotopeNumber;
if (isHetero) this.formalChargeAndFlags = 2;
if (formalCharge != 0 && formalCharge != -2147483648) this.setFormalCharge (formalCharge);
this.userDefinedVanDerWaalRadius = radius;
this.setT (xyz);
}, "~N,~N,J.util.P3,~N,J.util.BS,~N,~N,~N,~B");
$_M(c$, "setAltLoc", 
function (altLoc) {
this.altloc = altLoc;
}, "~S");
$_M(c$, "setShapeVisibilityFlags", 
function (flag) {
this.shapeVisibilityFlags = flag;
}, "~N");
$_M(c$, "setShapeVisibility", 
function (flag, isVisible) {
if (isVisible) {
this.shapeVisibilityFlags |= flag;
} else {
this.shapeVisibilityFlags &= ~flag;
}}, "~N,~B");
$_M(c$, "isCovalentlyBonded", 
function (atomOther) {
if (this.bonds != null) for (var i = this.bonds.length; --i >= 0; ) if (this.bonds[i].isCovalent () && this.bonds[i].getOtherAtom (this) === atomOther) return true;

return false;
}, "J.modelset.Atom");
$_M(c$, "isBonded", 
function (atomOther) {
if (this.bonds != null) for (var i = this.bonds.length; --i >= 0; ) if (this.bonds[i].getOtherAtom (this) === atomOther) return true;

return false;
}, "J.modelset.Atom");
$_M(c$, "getBond", 
function (atomOther) {
if (this.bonds != null) for (var i = this.bonds.length; --i >= 0; ) if (this.bonds[i].getOtherAtom (atomOther) != null) return this.bonds[i];

return null;
}, "J.modelset.Atom");
$_M(c$, "addDisplayedBond", 
function (stickVisibilityFlag, isVisible) {
this.nBondsDisplayed += (isVisible ? 1 : -1);
this.setShapeVisibility (stickVisibilityFlag, (this.nBondsDisplayed > 0));
}, "~N,~B");
$_M(c$, "addDisplayedBackbone", 
function (backboneVisibilityFlag, isVisible) {
this.nBackbonesDisplayed += (isVisible ? 1 : -1);
this.setShapeVisibility (backboneVisibilityFlag, isVisible);
}, "~N,~B");
$_M(c$, "deleteBond", 
function (bond) {
if (this.bonds != null) for (var i = this.bonds.length; --i >= 0; ) if (this.bonds[i] === bond) {
this.deleteBondAt (i);
return;
}
}, "J.modelset.Bond");
$_M(c$, "deleteBondAt", 
($fz = function (i) {
var newLength = this.bonds.length - 1;
if (newLength == 0) {
this.bonds = null;
return;
}var bondsNew =  new Array (newLength);
var j = 0;
for (; j < i; ++j) bondsNew[j] = this.bonds[j];

for (; j < newLength; ++j) bondsNew[j] = this.bonds[j + 1];

this.bonds = bondsNew;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "clearBonds", 
function () {
this.bonds = null;
});
Clazz.overrideMethod (c$, "getBondedAtomIndex", 
function (bondIndex) {
return this.bonds[bondIndex].getOtherAtom (this).index;
}, "~N");
$_M(c$, "setMadAtom", 
function (viewer, rd) {
this.madAtom = this.calculateMad (viewer, rd);
}, "J.viewer.Viewer,J.atomdata.RadiusData");
$_M(c$, "calculateMad", 
function (viewer, rd) {
if (rd == null) return 0;
var f = rd.value;
if (f == 0) return 0;
switch (rd.factorType) {
case J.atomdata.RadiusData.EnumType.SCREEN:
return Clazz.floatToShort (f);
case J.atomdata.RadiusData.EnumType.FACTOR:
case J.atomdata.RadiusData.EnumType.OFFSET:
var r = 0;
switch (rd.vdwType) {
case J.constant.EnumVdw.TEMP:
var tmax = viewer.getBfactor100Hi ();
r = (tmax > 0 ? this.getBfactor100 () / tmax : 0);
break;
case J.constant.EnumVdw.HYDRO:
r = Math.abs (this.getHydrophobicity ());
break;
case J.constant.EnumVdw.IONIC:
r = this.getBondingRadiusFloat ();
break;
case J.constant.EnumVdw.ADPMIN:
case J.constant.EnumVdw.ADPMAX:
r = this.getADPMinMax (rd.vdwType === J.constant.EnumVdw.ADPMAX);
break;
default:
r = this.getVanderwaalsRadiusFloat (viewer, rd.vdwType);
}
if (rd.factorType === J.atomdata.RadiusData.EnumType.FACTOR) f *= r;
 else f += r;
break;
case J.atomdata.RadiusData.EnumType.ABSOLUTE:
break;
}
var mad = Clazz.floatToShort (f < 0 ? f : f * 2000);
if (mad < 0 && f > 0) mad = 0;
return mad;
}, "J.viewer.Viewer,J.atomdata.RadiusData");
$_M(c$, "getADPMinMax", 
function (isMax) {
var tensors = this.getTensors ();
if (tensors == null) return 0;
var t = tensors[0];
if (t == null || t.iType != 1) return 0;
if (this.group.chain.model.modelSet.isModulated (this.index) && t.isUnmodulated) t = tensors[1];
return t.getFactoredValue (isMax ? 2 : 1);
}, "~B");
$_M(c$, "getTensors", 
function () {
return this.group.chain.model.modelSet.getAtomTensorList (this.index);
});
$_M(c$, "getRasMolRadius", 
function () {
return Math.abs (Clazz.doubleToInt (this.madAtom / 8));
});
Clazz.overrideMethod (c$, "getCovalentBondCount", 
function () {
if (this.bonds == null) return 0;
var n = 0;
var b;
for (var i = this.bonds.length; --i >= 0; ) if (((b = this.bonds[i]).order & 1023) != 0 && !b.getOtherAtom (this).isDeleted ()) ++n;

return n;
});
Clazz.overrideMethod (c$, "getCovalentHydrogenCount", 
function () {
if (this.bonds == null) return 0;
var n = 0;
for (var i = this.bonds.length; --i >= 0; ) {
if ((this.bonds[i].order & 1023) == 0) continue;
var a = this.bonds[i].getOtherAtom (this);
if (a.valence >= 0 && a.getElementNumber () == 1) ++n;
}
return n;
});
Clazz.overrideMethod (c$, "getEdges", 
function () {
return this.bonds;
});
$_M(c$, "setColixAtom", 
function (colixAtom) {
this.colixAtom = colixAtom;
}, "~N");
$_M(c$, "setPaletteID", 
function (paletteID) {
this.paletteID = paletteID;
}, "~N");
$_M(c$, "setTranslucent", 
function (isTranslucent, translucentLevel) {
this.colixAtom = J.util.C.getColixTranslucent3 (this.colixAtom, isTranslucent, translucentLevel);
}, "~B,~N");
$_M(c$, "isTranslucent", 
function () {
return J.util.C.isColixTranslucent (this.colixAtom);
});
Clazz.overrideMethod (c$, "getElementNumber", 
function () {
return J.util.Elements.getElementNumber (this.atomicAndIsotopeNumber);
});
Clazz.overrideMethod (c$, "getIsotopeNumber", 
function () {
return J.util.Elements.getIsotopeNumber (this.atomicAndIsotopeNumber);
});
Clazz.overrideMethod (c$, "getAtomicAndIsotopeNumber", 
function () {
return this.atomicAndIsotopeNumber;
});
$_M(c$, "setAtomicAndIsotopeNumber", 
function (n) {
if (n < 0 || (n & 127) >= J.util.Elements.elementNumberMax || n > 32767) n = 0;
this.atomicAndIsotopeNumber = n;
}, "~N");
$_M(c$, "getElementSymbolIso", 
function (withIsotope) {
return J.util.Elements.elementSymbolFromNumber (withIsotope ? this.atomicAndIsotopeNumber : this.atomicAndIsotopeNumber & 127);
}, "~B");
$_M(c$, "getElementSymbol", 
function () {
return this.getElementSymbolIso (true);
});
$_M(c$, "getAlternateLocationID", 
function () {
return this.altloc;
});
$_M(c$, "isAlternateLocationMatch", 
function (strPattern) {
if (strPattern == null) return (this.altloc == '\0');
if (strPattern.length != 1) return false;
var ch = strPattern.charAt (0);
return (ch == '*' || ch == '?' && this.altloc != '\0' || this.altloc == ch);
}, "~S");
$_M(c$, "isHetero", 
function () {
return (this.formalChargeAndFlags & 2) != 0;
});
$_M(c$, "hasVibration", 
function () {
return (this.formalChargeAndFlags & 1) != 0;
});
$_M(c$, "setFormalCharge", 
function (charge) {
this.formalChargeAndFlags = ((this.formalChargeAndFlags & 3) | ((charge == -2147483648 ? 0 : charge > 7 ? 7 : charge < -3 ? -3 : charge) << 2));
}, "~N");
$_M(c$, "setVibrationVector", 
function () {
this.formalChargeAndFlags |= 1;
});
Clazz.overrideMethod (c$, "getFormalCharge", 
function () {
return this.formalChargeAndFlags >> 2;
});
$_M(c$, "getOccupancy100", 
function () {
var occupancies = this.group.chain.model.modelSet.occupancies;
return occupancies == null ? 100 : occupancies[this.index];
});
$_M(c$, "getBfactor100", 
function () {
var bfactor100s = this.group.chain.model.modelSet.bfactor100s;
if (bfactor100s == null) return 0;
return bfactor100s[this.index];
});
$_M(c$, "getHydrophobicity", 
($fz = function () {
var values = this.group.chain.model.modelSet.hydrophobicities;
if (values == null) return J.util.Elements.getHydrophobicity (this.group.getGroupID ());
return values[this.index];
}, $fz.isPrivate = true, $fz));
$_M(c$, "setRadius", 
function (radius) {
return !Float.isNaN (this.userDefinedVanDerWaalRadius = (radius > 0 ? radius : NaN));
}, "~N");
$_M(c$, "deleteBonds", 
function (bsBonds) {
this.valence = -1;
if (this.bonds != null) for (var i = this.bonds.length; --i >= 0; ) {
var bond = this.bonds[i];
bond.getOtherAtom (this).deleteBond (bond);
bsBonds.set (bond.index);
}
this.bonds = null;
}, "J.util.BS");
Clazz.overrideMethod (c$, "isDeleted", 
function () {
return (this.valence < 0);
});
$_M(c$, "setValence", 
function (nBonds) {
if (this.isDeleted ()) return;
this.valence = (nBonds < 0 ? 0 : nBonds < 0xEF ? nBonds : 0xEF);
}, "~N");
Clazz.overrideMethod (c$, "getValence", 
function () {
if (this.isDeleted ()) return -1;
var n = this.valence;
if (n == 0 && this.bonds != null) for (var i = this.bonds.length; --i >= 0; ) n += this.bonds[i].getValence ();

return n;
});
Clazz.overrideMethod (c$, "getImplicitHydrogenCount", 
function () {
return this.group.chain.model.modelSet.getImplicitHydrogenCount (this, false);
});
$_M(c$, "getTargetValence", 
function () {
switch (this.getElementNumber ()) {
case 6:
case 14:
return 4;
case 5:
case 7:
case 15:
return 3;
case 8:
case 16:
return 2;
case 1:
case 9:
case 17:
case 35:
case 53:
return 1;
}
return -1;
});
$_M(c$, "getDimensionValue", 
function (dimension) {
return (dimension == 0 ? this.x : (dimension == 1 ? this.y : this.z));
}, "~N");
$_M(c$, "getVanderwaalsRadiusFloat", 
function (viewer, type) {
return (Float.isNaN (this.userDefinedVanDerWaalRadius) ? viewer.getVanderwaalsMarType (this.atomicAndIsotopeNumber, this.getVdwType (type)) / 1000 : this.userDefinedVanDerWaalRadius);
}, "J.viewer.Viewer,J.constant.EnumVdw");
$_M(c$, "getVdwType", 
($fz = function (type) {
switch (type) {
case J.constant.EnumVdw.AUTO:
type = this.group.chain.model.modelSet.getDefaultVdwType (this.modelIndex);
break;
case J.constant.EnumVdw.NOJMOL:
type = this.group.chain.model.modelSet.getDefaultVdwType (this.modelIndex);
if (type === J.constant.EnumVdw.AUTO_JMOL) type = J.constant.EnumVdw.AUTO_BABEL;
break;
}
return type;
}, $fz.isPrivate = true, $fz), "J.constant.EnumVdw");
$_M(c$, "getCovalentRadiusFloat", 
($fz = function () {
return J.util.Elements.getBondingRadiusFloat (this.atomicAndIsotopeNumber, 0);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getBondingRadiusFloat", 
function () {
var ionicRadii = this.group.chain.model.modelSet.ionicRadii;
var r = (ionicRadii == null ? 0 : ionicRadii[this.index]);
return (r == 0 ? J.util.Elements.getBondingRadiusFloat (this.atomicAndIsotopeNumber, this.getFormalCharge ()) : r);
});
$_M(c$, "getVolume", 
function (viewer, vType) {
var r1 = (vType == null ? this.userDefinedVanDerWaalRadius : NaN);
if (Float.isNaN (r1)) r1 = viewer.getVanderwaalsMarType (this.getElementNumber (), this.getVdwType (vType)) / 1000;
var volume = 0;
if (this.bonds != null) for (var j = 0; j < this.bonds.length; j++) {
if (!this.bonds[j].isCovalent ()) continue;
var atom2 = this.bonds[j].getOtherAtom (this);
var r2 = (vType == null ? atom2.userDefinedVanDerWaalRadius : NaN);
if (Float.isNaN (r2)) r2 = viewer.getVanderwaalsMarType (atom2.getElementNumber (), atom2.getVdwType (vType)) / 1000;
var d = this.distance (atom2);
if (d > r1 + r2) continue;
if (d + r1 <= r2) return 0;
var h = r1 - (r1 * r1 + d * d - r2 * r2) / (2.0 * d);
volume -= 1.0471975511965976 * h * h * (3 * r1 - h);
}
return (volume + 4.1887902047863905 * r1 * r1 * r1);
}, "J.viewer.Viewer,J.constant.EnumVdw");
$_M(c$, "getCurrentBondCount", 
function () {
return this.bonds == null ? 0 : this.bonds.length;
});
$_M(c$, "getColix", 
function () {
return this.colixAtom;
});
$_M(c$, "getPaletteID", 
function () {
return this.paletteID;
});
$_M(c$, "getRadius", 
function () {
return Math.abs (this.madAtom / (2000.0));
});
Clazz.overrideMethod (c$, "getIndex", 
function () {
return this.index;
});
Clazz.overrideMethod (c$, "getAtomSite", 
function () {
return this.atomSite;
});
$_M(c$, "setAtomSymmetry", 
function (bsSymmetry) {
this.atomSymmetry = bsSymmetry;
}, "J.util.BS");
$_M(c$, "getAtomSymmetry", 
function () {
return this.atomSymmetry;
});
$_M(c$, "setGroup", 
function (group) {
this.group = group;
}, "J.modelset.Group");
$_M(c$, "getGroup", 
function () {
return this.group;
});
Clazz.overrideMethod (c$, "getGroupBits", 
function (bs) {
this.group.selectAtoms (bs);
}, "J.util.BS");
Clazz.overrideMethod (c$, "getAtomName", 
function () {
return (this.atomID > 0 ? J.viewer.JC.getSpecialAtomName (this.atomID) : this.group.chain.model.modelSet.atomNames[this.index]);
});
Clazz.overrideMethod (c$, "getAtomType", 
function () {
var atomTypes = this.group.chain.model.modelSet.atomTypes;
var type = (atomTypes == null ? null : atomTypes[this.index]);
return (type == null ? this.getAtomName () : type);
});
$_M(c$, "getAtomNumber", 
function () {
var atomSerials = this.group.chain.model.modelSet.atomSerials;
return (atomSerials != null ? atomSerials[this.index] : this.index);
});
$_M(c$, "isInFrame", 
function () {
return ((this.shapeVisibilityFlags & 1) != 0);
});
$_M(c$, "getShapeVisibilityFlags", 
function () {
return this.shapeVisibilityFlags;
});
$_M(c$, "isShapeVisible", 
function (shapeVisibilityFlag) {
return ((this.shapeVisibilityFlags & shapeVisibilityFlag) != 0);
}, "~N");
$_M(c$, "getPartialCharge", 
function () {
var partialCharges = this.group.chain.model.modelSet.partialCharges;
return partialCharges == null ? 0 : partialCharges[this.index];
});
$_M(c$, "getSymmetryTranslation", 
function (symop, cellRange, nOps) {
var pt = symop;
for (var i = 0; i < cellRange.length; i++) if (this.atomSymmetry.get (pt += nOps)) return cellRange[i];

return 0;
}, "~N,~A,~N");
$_M(c$, "getCellTranslation", 
function (cellNNN, cellRange, nOps) {
var pt = nOps;
for (var i = 0; i < cellRange.length; i++) for (var j = 0; j < nOps; j++, pt++) if (this.atomSymmetry.get (pt) && cellRange[i] == cellNNN) return cellRange[i];


return 0;
}, "~N,~A,~N");
$_M(c$, "getSymmetryOperatorList", 
function () {
var str = "";
var f = this.group.chain.model.modelSet;
var nOps = f.getModelSymmetryCount (this.modelIndex);
if (nOps == 0 || this.atomSymmetry == null) return "";
var cellRange = f.getModelCellRange (this.modelIndex);
var pt = nOps;
var n = (cellRange == null ? 1 : cellRange.length);
for (var i = 0; i < n; i++) for (var j = 0; j < nOps; j++) if (this.atomSymmetry.get (pt++)) str += "," + (j + 1) + "" + cellRange[i];


return (str.length == 0 ? "" : str.substring (1));
});
Clazz.overrideMethod (c$, "getModelIndex", 
function () {
return this.modelIndex;
});
$_M(c$, "getMoleculeNumber", 
function (inModel) {
return (this.group.chain.model.modelSet.getMoleculeIndex (this.index, inModel) + 1);
}, "~B");
$_M(c$, "getFractionalCoord", 
($fz = function (ch, asAbsolute) {
var pt = this.getFractionalCoordPt (asAbsolute);
return (ch == 'X' ? pt.x : ch == 'Y' ? pt.y : pt.z);
}, $fz.isPrivate = true, $fz), "~S,~B");
$_M(c$, "getFractionalCoordPt", 
($fz = function (asAbsolute) {
var c = this.group.chain.model.modelSet.getUnitCell (this.modelIndex);
if (c == null) return this;
var pt = J.util.P3.newP (this);
c.toFractional (pt, asAbsolute);
return pt;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "getFractionalUnitCoord", 
($fz = function (ch) {
var pt = this.getFractionalUnitCoordPt (false);
return (ch == 'X' ? pt.x : ch == 'Y' ? pt.y : pt.z);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getFractionalUnitCoordPt", 
function (asCartesian) {
var c = this.group.chain.model.modelSet.getUnitCell (this.modelIndex);
if (c == null) return this;
var pt = J.util.P3.newP (this);
if (this.group.chain.model.isJmolDataFrame) {
c.toFractional (pt, false);
if (asCartesian) c.toCartesian (pt, false);
} else {
c.toUnitCell (pt, null);
if (!asCartesian) c.toFractional (pt, false);
}return pt;
}, "~B");
$_M(c$, "getFractionalUnitDistance", 
function (pt, ptTemp1, ptTemp2) {
var c = this.group.chain.model.modelSet.getUnitCell (this.modelIndex);
if (c == null) return this.distance (pt);
ptTemp1.setT (this);
ptTemp2.setT (pt);
if (this.group.chain.model.isJmolDataFrame) {
c.toFractional (ptTemp1, true);
c.toFractional (ptTemp2, true);
} else {
c.toUnitCell (ptTemp1, null);
c.toUnitCell (ptTemp2, null);
}return ptTemp1.distance (ptTemp2);
}, "J.util.P3,J.util.P3,J.util.P3");
$_M(c$, "setFractionalCoord", 
function (tok, fValue, asAbsolute) {
var c = this.group.chain.model.modelSet.getUnitCell (this.modelIndex);
if (c != null) c.toFractional (this, asAbsolute);
switch (tok) {
case 1112541191:
case 1112541188:
this.x = fValue;
break;
case 1112541192:
case 1112541189:
this.y = fValue;
break;
case 1112541193:
case 1112541190:
this.z = fValue;
break;
}
if (c != null) c.toCartesian (this, asAbsolute);
}, "~N,~N,~B");
$_M(c$, "setFractionalCoordTo", 
function (ptNew, asAbsolute) {
this.setFractionalCoordPt (this, ptNew, asAbsolute);
}, "J.util.P3,~B");
$_M(c$, "setFractionalCoordPt", 
function (pt, ptNew, asAbsolute) {
pt.setT (ptNew);
var c = this.group.chain.model.modelSet.getUnitCell (this.modelIndex);
if (c != null) c.toCartesian (pt, asAbsolute && !this.group.chain.model.isJmolDataFrame);
}, "J.util.P3,J.util.P3,~B");
$_M(c$, "isCursorOnTopOf", 
function (xCursor, yCursor, minRadius, competitor) {
var r = Clazz.doubleToInt (this.screenDiameter / 2);
if (r < minRadius) r = minRadius;
var r2 = r * r;
var dx = this.screenX - xCursor;
var dx2 = dx * dx;
if (dx2 > r2) return false;
var dy = this.screenY - yCursor;
var dy2 = dy * dy;
var dz2 = r2 - (dx2 + dy2);
if (dz2 < 0) return false;
if (competitor == null) return true;
var z = this.screenZ;
var zCompetitor = competitor.screenZ;
var rCompetitor = Clazz.doubleToInt (competitor.screenDiameter / 2);
if (z < zCompetitor - rCompetitor) return true;
var dxCompetitor = competitor.screenX - xCursor;
var dx2Competitor = dxCompetitor * dxCompetitor;
var dyCompetitor = competitor.screenY - yCursor;
var dy2Competitor = dyCompetitor * dyCompetitor;
var r2Competitor = rCompetitor * rCompetitor;
var dz2Competitor = r2Competitor - (dx2Competitor + dy2Competitor);
return (z - Math.sqrt (dz2) < zCompetitor - Math.sqrt (dz2Competitor));
}, "~N,~N,~N,J.modelset.Atom");
$_M(c$, "getInfo", 
function () {
return this.getIdentity (true);
});
$_M(c$, "getInfoXYZ", 
function (useChimeFormat) {
if (useChimeFormat) {
var group3 = this.getGroup3 (true);
var chainID = this.getChainID ();
var pt = this.getFractionalCoordPt (true);
return "Atom: " + (group3 == null ? this.getElementSymbol () : this.getAtomName ()) + " " + this.getAtomNumber () + (group3 != null && group3.length > 0 ? (this.isHetero () ? " Hetero: " : " Group: ") + group3 + " " + this.getResno () + (chainID != 0 && chainID != 32 ? " Chain: " + this.group.chain.getIDStr () : "") : "") + " Model: " + this.getModelNumber () + " Coordinates: " + this.x + " " + this.y + " " + this.z + (pt == null ? "" : " Fractional: " + pt.x + " " + pt.y + " " + pt.z);
}return this.getIdentityXYZ (true);
}, "~B");
$_M(c$, "getIdentityXYZ", 
function (allInfo) {
var pt = (this.group.chain.model.isJmolDataFrame ? this.getFractionalCoordPt (false) : this);
return this.getIdentity (allInfo) + " " + pt.x + " " + pt.y + " " + pt.z;
}, "~B");
$_M(c$, "getIdentity", 
function (allInfo) {
var info =  new J.util.SB ();
var group3 = this.getGroup3 (true);
if (group3 != null && group3.length > 0) {
info.append ("[");
info.append (group3);
info.append ("]");
var seqcodeString = this.getSeqcodeString ();
if (seqcodeString != null) info.append (seqcodeString);
var chainID = this.getChainID ();
if (chainID != 0 && chainID != 32) {
info.append (":");
var s = this.getChainIDStr ();
if (chainID >= 256) s = J.util.Escape.eS (s);
info.append (s);
}if (!allInfo) return info.toString ();
info.append (".");
}info.append (this.getAtomName ());
if (info.length () == 0) {
info.append (this.getElementSymbolIso (false));
info.append (" ");
info.appendI (this.getAtomNumber ());
}if (this.altloc.charCodeAt (0) != 0) {
info.append ("%");
info.appendC (this.altloc);
}if (this.group.chain.model.modelSet.modelCount > 1) {
info.append ("/");
info.append (this.getModelNumberForLabel ());
}info.append (" #");
info.appendI (this.getAtomNumber ());
return info.toString ();
}, "~B");
Clazz.overrideMethod (c$, "getGroup3", 
function (allowNull) {
var group3 = this.group.getGroup3 ();
return (allowNull || group3 != null && group3.length > 0 ? group3 : "UNK");
}, "~B");
Clazz.overrideMethod (c$, "getGroup1", 
function (c0) {
var c = this.group.getGroup1 ();
return (c != '\0' ? "" + c : c0 != '\0' ? "" + c0 : "");
}, "~S");
Clazz.overrideMethod (c$, "isProtein", 
function () {
return this.group.isProtein ();
});
$_M(c$, "isCarbohydrate", 
function () {
return this.group.isCarbohydrate ();
});
Clazz.overrideMethod (c$, "isNucleic", 
function () {
return this.group.isNucleic ();
});
Clazz.overrideMethod (c$, "isDna", 
function () {
return this.group.isDna ();
});
Clazz.overrideMethod (c$, "isRna", 
function () {
return this.group.isRna ();
});
Clazz.overrideMethod (c$, "isPurine", 
function () {
return this.group.isPurine ();
});
Clazz.overrideMethod (c$, "isPyrimidine", 
function () {
return this.group.isPyrimidine ();
});
$_M(c$, "getSeqcode", 
function () {
return this.group.seqcode;
});
Clazz.overrideMethod (c$, "getResno", 
function () {
return this.group.getResno ();
});
$_M(c$, "isClickable", 
function () {
if (!this.isVisible (0)) return false;
var flags = this.shapeVisibilityFlags | this.group.shapeVisibilityFlags;
return ((flags & this.clickabilityFlags) != 0);
});
$_M(c$, "getClickabilityFlags", 
function () {
return this.clickabilityFlags;
});
$_M(c$, "setClickable", 
function (flag) {
if (flag == 0) this.clickabilityFlags = 0;
 else this.clickabilityFlags |= flag;
}, "~N");
$_M(c$, "isVisible", 
function (flags) {
if (!this.isInFrame () || this.group.chain.model.modelSet.isAtomHidden (this.index)) return false;
if (flags != 0) return (this.isShapeVisible (flags));
flags = this.shapeVisibilityFlags;
if (this.group.shapeVisibilityFlags != J.modelset.Atom.BACKBONE_VISIBILITY_FLAG || this.isLeadAtom ()) flags |= this.group.shapeVisibilityFlags;
return ((flags & -2) != 0);
}, "~N");
Clazz.overrideMethod (c$, "isLeadAtom", 
function () {
return this.group.isLeadAtom (this.index);
});
$_M(c$, "getGroupParameter", 
function (tok) {
return this.group.getGroupParameter (tok);
}, "~N");
Clazz.overrideMethod (c$, "getChainID", 
function () {
return this.group.chain.chainID;
});
Clazz.overrideMethod (c$, "getChainIDStr", 
function () {
return this.group.chain.getIDStr ();
});
$_M(c$, "getSurfaceDistance100", 
function () {
return this.group.chain.model.modelSet.getSurfaceDistance100 (this.index);
});
$_M(c$, "getVibrationVector", 
function () {
return this.group.chain.model.modelSet.getVibration (this.index, false);
});
$_M(c$, "getVibrationCoord", 
function (ch) {
return this.group.chain.model.modelSet.getVibrationCoord (this.index, ch);
}, "~S");
$_M(c$, "getPolymerLength", 
function () {
return this.group.getBioPolymerLength ();
});
$_M(c$, "getPolymerIndexInModel", 
function () {
return this.group.getBioPolymerIndexInModel ();
});
$_M(c$, "getMonomerIndex", 
function () {
return this.group.getMonomerIndex ();
});
$_M(c$, "getSelectedGroupCountWithinChain", 
function () {
return this.group.chain.selectedGroupCount;
});
$_M(c$, "getSelectedGroupIndexWithinChain", 
function () {
return this.group.getSelectedGroupIndex ();
});
$_M(c$, "getSelectedMonomerCountWithinPolymer", 
function () {
return this.group.getSelectedMonomerCount ();
});
$_M(c$, "getSelectedMonomerIndexWithinPolymer", 
function () {
return this.group.getSelectedMonomerIndex ();
});
$_M(c$, "getChain", 
function () {
return this.group.chain;
});
$_M(c$, "getModelNumberForLabel", 
function () {
return this.group.chain.model.modelSet.getModelNumberForAtomLabel (this.modelIndex);
});
$_M(c$, "getModelNumber", 
function () {
return this.group.chain.model.modelSet.getModelNumber (this.modelIndex) % 1000000;
});
$_M(c$, "getModelFileIndex", 
function () {
return this.group.chain.model.fileIndex;
});
$_M(c$, "getModelFileNumber", 
function () {
return this.group.chain.model.modelSet.getModelFileNumber (this.modelIndex);
});
Clazz.overrideMethod (c$, "getBioStructureTypeName", 
function () {
return this.getProteinStructureType ().getBioStructureTypeName (true);
});
$_M(c$, "getProteinStructureType", 
function () {
return this.group.getProteinStructureType ();
});
$_M(c$, "getProteinStructureSubType", 
function () {
return this.group.getProteinStructureSubType ();
});
$_M(c$, "getStrucNo", 
function () {
return this.group.getStrucNo ();
});
$_M(c$, "getStructureId", 
function () {
return this.group.getStructureId ();
});
$_M(c$, "getProteinStructureTag", 
function () {
return this.group.getProteinStructureTag ();
});
$_M(c$, "getGroupID", 
function () {
return this.group.groupID;
});
$_M(c$, "getSeqcodeString", 
function () {
return this.group.getSeqcodeString ();
});
$_M(c$, "getInsertionCode", 
function () {
return this.group.getInsertionCode ();
});
Clazz.overrideMethod (c$, "equals", 
function (obj) {
return (this === obj);
}, "~O");
Clazz.overrideMethod (c$, "hashCode", 
function () {
return this.index;
});
$_M(c$, "findAromaticNeighbor", 
function (notAtomIndex) {
if (this.bonds == null) return null;
for (var i = this.bonds.length; --i >= 0; ) {
var bondT = this.bonds[i];
var a = bondT.getOtherAtom (this);
if (bondT.isAromatic () && a.index != notAtomIndex) return a;
}
return null;
}, "~N");
c$.atomPropertyInt = $_M(c$, "atomPropertyInt", 
function (atom, tokWhat) {
switch (tokWhat) {
case 1095763969:
return atom.getAtomNumber ();
case 1095761922:
return atom.atomID;
case 1095761923:
return atom.getIndex ();
case 1095761924:
return atom.getCovalentBondCount ();
case 1766856708:
return atom.group.chain.model.modelSet.viewer.getColorArgbOrGray (atom.getColix ());
case 1087375365:
case 1095763976:
return atom.getElementNumber ();
case 1095761927:
return atom.atomicAndIsotopeNumber;
case 1229984263:
return atom.getModelFileIndex () + 1;
case 1632634889:
return atom.getFormalCharge ();
case 1095761930:
return atom.getGroupID ();
case 1095761931:
return atom.group.getGroupIndex ();
case 1095766028:
return atom.getModelNumber ();
case -1095766028:
return atom.getModelFileNumber ();
case 1095761933:
return atom.modelIndex;
case 1095761934:
return atom.getMoleculeNumber (true);
case 1129318401:
return atom.getOccupancy100 ();
case 1095761935:
return atom.getGroup ().getBioPolymerIndexInModel () + 1;
case 1095761936:
return atom.getPolymerLength ();
case 1666189314:
return atom.getRasMolRadius ();
case 1095761937:
return atom.getResno ();
case 1095761938:
return atom.getAtomSite ();
case 1641025539:
return atom.getProteinStructureType ().getId ();
case 1238369286:
return atom.getProteinStructureSubType ().getId ();
case 1095761939:
return atom.getStrucNo ();
case 1095763988:
return atom.getValence ();
}
return 0;
}, "J.modelset.Atom,~N");
c$.atomPropertyFloat = $_M(c$, "atomPropertyFloat", 
function (viewer, atom, tokWhat) {
switch (tokWhat) {
case 1666189314:
return atom.getRadius ();
case 1114638363:
return (viewer.isAtomSelected (atom.index) ? 1 : 0);
case 1112539151:
atom.group.chain.model.modelSet.getSurfaceDistanceMax ();
return atom.getSurfaceDistance100 () / 100;
case 1112541199:
return atom.getBfactor100 () / 100;
case 1114638362:
return atom.getHydrophobicity ();
case 1313866247:
return atom.getVolume (viewer, J.constant.EnumVdw.AUTO);
case 1112539137:
return atom.getADPMinMax (true);
case 1112539138:
return atom.getADPMinMax (false);
case 1112541185:
case 1112541205:
return atom.x;
case 1112541186:
case 1112541206:
return atom.y;
case 1112541187:
case 1112541207:
return atom.z;
case 1112539140:
return atom.getCovalentRadiusFloat ();
case 1112541188:
return atom.getFractionalCoord ('X', true);
case 1112541189:
return atom.getFractionalCoord ('Y', true);
case 1112541190:
return atom.getFractionalCoord ('Z', true);
case 1112541191:
return atom.getFractionalCoord ('X', false);
case 1112541192:
return atom.getFractionalCoord ('Y', false);
case 1112541193:
return atom.getFractionalCoord ('Z', false);
case 1112539147:
return atom.screenX;
case 1112539148:
return atom.group.chain.model.modelSet.viewer.getScreenHeight () - atom.screenY;
case 1112539149:
return atom.screenZ;
case 1112541195:
return atom.getBondingRadiusFloat ();
case 1112539143:
return atom.getMass ();
case 1129318401:
return atom.getOccupancy100 () / 100;
case 1112541196:
return atom.getPartialCharge ();
case 1112539145:
case 1112539146:
case 1112539144:
if (atom.group.chain.model.isJmolDataFrame && atom.group.chain.model.jmolFrameType.startsWith ("plot ramachandran")) {
switch (tokWhat) {
case 1112539145:
return atom.getFractionalCoord ('X', false);
case 1112539146:
return atom.getFractionalCoord ('Y', false);
case 1112539144:
if (atom.group.chain.model.isJmolDataFrame && atom.group.chain.model.jmolFrameType.equals ("plot ramachandran")) {
var omega = atom.getFractionalCoord ('Z', false) - 180;
return (omega < -180 ? 360 + omega : omega);
}}
}return atom.getGroupParameter (tokWhat);
case 1112539141:
case 1112539152:
case 1112539150:
return atom.getGroupParameter (tokWhat);
case 1113200651:
return atom.getRadius ();
case 1115297793:
case 1113200642:
case 1113198595:
case 1113198596:
case 1113198597:
case 1113200646:
case 1113200647:
case 1113200649:
case 1113200650:
case 1113200652:
case 1650071565:
case 1113200654:
return viewer.getAtomShapeValue (tokWhat, atom.group, atom.index);
case 1112539153:
return atom.getFractionalUnitCoord ('X');
case 1112539154:
return atom.getFractionalUnitCoord ('Y');
case 1112539155:
return atom.getFractionalUnitCoord ('Z');
case 1649412120:
return atom.getVanderwaalsRadiusFloat (viewer, J.constant.EnumVdw.AUTO);
case 1112541202:
return atom.getVibrationCoord ('X');
case 1112541203:
return atom.getVibrationCoord ('Y');
case 1112541204:
return atom.getVibrationCoord ('Z');
case 1649410049:
var v = atom.getVibrationVector ();
return (v == null ? 0 : v.length () * viewer.getFloat (1649410049));
case 1112539142:
return viewer.getNMRCalculation ().getMagneticShielding (atom);
case 1112539139:
return viewer.getNMRCalculation ().getChemicalShift (atom);
}
return J.modelset.Atom.atomPropertyInt (atom, tokWhat);
}, "J.viewer.Viewer,J.modelset.Atom,~N");
$_M(c$, "getMass", 
($fz = function () {
var mass = this.getIsotopeNumber ();
return (mass > 0 ? mass : J.util.Elements.getAtomicMass (this.getElementNumber ()));
}, $fz.isPrivate = true, $fz));
c$.atomPropertyString = $_M(c$, "atomPropertyString", 
function (viewer, atom, tokWhat) {
var ch;
switch (tokWhat) {
case 1087373315:
ch = atom.getAlternateLocationID ();
return (ch == '\0' ? "" : "" + ch);
case 1087375362:
return atom.getAtomName ();
case 1087375361:
return atom.getAtomType ();
case 1087373316:
return atom.getChainIDStr ();
case 1087373320:
return atom.getGroup1 ('?');
case 1087373319:
return atom.getGroup1 ('\0');
case 1087373318:
return atom.getGroup3 (false);
case 1087375365:
return atom.getElementSymbolIso (true);
case 1087373321:
return atom.getIdentity (true);
case 1087373322:
ch = atom.getInsertionCode ();
return (ch == '\0' ? "" : "" + ch);
case 1826248715:
case 1288701960:
var s = atom.group.chain.model.modelSet.getAtomLabel (atom.getIndex ());
if (s == null) s = "";
return s;
case 1641025539:
return atom.getProteinStructureType ().getBioStructureTypeName (false);
case 1238369286:
return atom.getProteinStructureSubType ().getBioStructureTypeName (false);
case 1087373324:
return atom.getStructureId ();
case 1087373323:
return viewer.getHybridizationAndAxes (atom.index, null, null, "d");
case 1087375373:
return atom.getElementSymbolIso (false);
case 1089470478:
return atom.getSymmetryOperatorList ();
}
return "";
}, "J.viewer.Viewer,J.modelset.Atom,~N");
c$.atomPropertyTuple = $_M(c$, "atomPropertyTuple", 
function (atom, tok) {
switch (tok) {
case 1146095627:
return atom.getFractionalCoordPt (!atom.group.chain.model.isJmolDataFrame);
case 1146095629:
return atom.getFractionalCoordPt (false);
case 1146093582:
return (atom.group.chain.model.isJmolDataFrame ? atom.getFractionalCoordPt (false) : atom.getFractionalUnitCoordPt (false));
case 1146095628:
return J.util.P3.new3 (atom.screenX, atom.group.chain.model.modelSet.viewer.getScreenHeight () - atom.screenY, atom.screenZ);
case 1146095631:
var v = atom.getVibrationVector ();
if (v == null) v =  new J.util.V3 ();
return v;
case 1146095626:
return atom;
case 1766856708:
return J.util.ColorUtil.colorPointFromInt2 (atom.group.chain.model.modelSet.viewer.getColorArgbOrGray (atom.getColix ()));
}
return null;
}, "J.modelset.Atom,~N");
$_M(c$, "isWithinStructure", 
function (type) {
return this.group.isWithinStructure (type);
}, "J.constant.EnumStructure");
Clazz.overrideMethod (c$, "getOffsetResidueAtom", 
function (name, offset) {
return this.group.chain.model.modelSet.getGroupAtom (this, offset, name);
}, "~S,~N");
Clazz.overrideMethod (c$, "isCrossLinked", 
function (node) {
return this.group.isCrossLinked ((node).getGroup ());
}, "J.util.JmolNode");
Clazz.overrideMethod (c$, "getCrossLinkLeadAtomIndexes", 
function (vReturn) {
return this.group.getCrossLinkLead (vReturn);
}, "J.util.JmolList");
Clazz.overrideMethod (c$, "toString", 
function () {
return this.getInfo ();
});
$_M(c$, "isWithinFourBonds", 
function (atomOther) {
if (this.modelIndex != atomOther.modelIndex) return false;
if (this.isCovalentlyBonded (atomOther)) return true;
var bondsOther = atomOther.bonds;
for (var i = 0; i < bondsOther.length; i++) {
var atom2 = bondsOther[i].getOtherAtom (atomOther);
if (this.isCovalentlyBonded (atom2)) return true;
for (var j = 0; j < this.bonds.length; j++) if (this.bonds[j].getOtherAtom (this).isCovalentlyBonded (atom2)) return true;

}
return false;
}, "J.modelset.Atom");
Clazz.overrideMethod (c$, "findAtomsLike", 
function (atomExpression) {
return this.group.chain.model.modelSet.viewer.getAtomBitSet (atomExpression);
}, "~S");
Clazz.defineStatics (c$,
"VIBRATION_VECTOR_FLAG", 1,
"IS_HETERO_FLAG", 2,
"FLAG_MASK", 3,
"RADIUS_MAX", 16);
c$.BACKBONE_VISIBILITY_FLAG = c$.prototype.BACKBONE_VISIBILITY_FLAG = J.viewer.JC.getShapeVisibilityFlag (9);
});
