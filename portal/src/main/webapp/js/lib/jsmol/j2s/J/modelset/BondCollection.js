Clazz.declarePackage ("J.modelset");
Clazz.load (["J.modelset.AtomCollection", "J.util.BS"], "J.modelset.BondCollection", ["java.lang.Float", "J.modelset.Bond", "$.BondIteratorSelected", "$.HBond", "J.util.ArrayUtil", "$.BSUtil", "$.C", "$.JmolEdge", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.molecules = null;
this.moleculeCount = 0;
this.bonds = null;
this.bondCount = 0;
this.numCached = null;
this.freeBonds = null;
this.haveWarned = false;
this.defaultCovalentMad = 0;
this.bsAromaticSingle = null;
this.bsAromaticDouble = null;
this.bsAromatic = null;
this.haveHiddenBonds = false;
Clazz.instantialize (this, arguments);
}, J.modelset, "BondCollection", J.modelset.AtomCollection);
Clazz.prepareFields (c$, function () {
this.numCached =  Clazz.newIntArray (5, 0);
this.freeBonds =  new Array (5);
{
for (var i = 5; --i > 0; ) this.freeBonds[i] =  new Array (200);

}this.bsAromatic =  new J.util.BS ();
});
Clazz.overrideMethod (c$, "releaseModelSet", 
function () {
this.releaseModelSetBC ();
});
$_M(c$, "releaseModelSetBC", 
function () {
this.bonds = null;
this.freeBonds = null;
this.releaseModelSetAC ();
});
$_M(c$, "resetMolecules", 
function () {
this.molecules = null;
this.moleculeCount = 0;
});
$_M(c$, "getBondAt", 
function (bondIndex) {
return this.bonds[bondIndex];
}, "~N");
$_M(c$, "getBondIteratorForType", 
function (bondType, bsAtoms) {
return  new J.modelset.BondIteratorSelected (this.bonds, this.bondCount, bondType, bsAtoms, this.viewer.getBoolean (603979812));
}, "~N,J.util.BS");
$_M(c$, "getBondIterator", 
function (bsBonds) {
return  new J.modelset.BondIteratorSelected (this.bonds, this.bondCount, 131071, bsBonds, false);
}, "J.util.BS");
$_M(c$, "getBondAtom1", 
function (i) {
return this.bonds[i].atom1;
}, "~N");
$_M(c$, "getBondAtom2", 
function (i) {
return this.bonds[i].atom2;
}, "~N");
$_M(c$, "getBondRadius", 
function (i) {
return this.bonds[i].getRadius ();
}, "~N");
$_M(c$, "getBondOrder", 
function (i) {
return this.bonds[i].order;
}, "~N");
$_M(c$, "getBondColix1", 
function (i) {
return this.bonds[i].getColix1 ();
}, "~N");
$_M(c$, "getBondColix2", 
function (i) {
return this.bonds[i].getColix2 ();
}, "~N");
$_M(c$, "getBondModelIndex", 
function (i) {
return this.bonds[i].atom1.modelIndex;
}, "~N");
$_M(c$, "getBondCountInModel", 
function (modelIndex) {
var n = 0;
for (var i = this.bondCount; --i >= 0; ) if (this.bonds[i].atom1.modelIndex == modelIndex) n++;

return n;
}, "~N");
$_M(c$, "getBondsForSelectedAtoms", 
function (bsAtoms, bondSelectionModeOr) {
var bs =  new J.util.BS ();
for (var iBond = 0; iBond < this.bondCount; ++iBond) {
var bond = this.bonds[iBond];
var isSelected1 = bsAtoms.get (bond.atom1.index);
var isSelected2 = bsAtoms.get (bond.atom2.index);
if (( new Boolean (!bondSelectionModeOr & isSelected1 & isSelected2).valueOf ()) || ( new Boolean (bondSelectionModeOr & ( new Boolean (isSelected1 | isSelected2).valueOf ())).valueOf ())) bs.set (iBond);
}
return bs;
}, "J.util.BS,~B");
$_M(c$, "bondAtoms", 
function (atom1, atom2, order, mad, bsBonds, energy, addGroup, isNew) {
var bond = this.getOrAddBond (atom1, atom2, order, mad, bsBonds, energy, true);
if (isNew) {
bond.order |= 131072;
if (addGroup) {
atom1.group = atom2.group;
atom1.group.addAtoms (atom1.index);
}}return bond;
}, "J.modelset.Atom,J.modelset.Atom,~N,~N,J.util.BS,~N,~B,~B");
$_M(c$, "getOrAddBond", 
($fz = function (atom, atomOther, order, mad, bsBonds, energy, overrideBonding) {
var i;
if (order == 131071 || order == 65535) order = 1;
if (atom.isBonded (atomOther)) {
i = atom.getBond (atomOther).index;
if (overrideBonding) {
this.bonds[i].setOrder (order);
this.bonds[i].setMad (mad);
if (Clazz.instanceOf (this.bonds[i], J.modelset.HBond)) (this.bonds[i]).energy = energy;
}} else {
if (this.bondCount == this.bonds.length) this.bonds = J.util.ArrayUtil.arrayCopyObject (this.bonds, this.bondCount + 250);
i = this.setBond (this.bondCount++, this.bondMutually (atom, atomOther, order, mad, energy)).index;
}if (bsBonds != null) bsBonds.set (i);
return this.bonds[i];
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,J.modelset.Atom,~N,~N,J.util.BS,~N,~B");
$_M(c$, "setBond", 
function (index, bond) {
return this.bonds[bond.index = index] = bond;
}, "~N,J.modelset.Bond");
$_M(c$, "bondMutually", 
function (atom, atomOther, order, mad, energy) {
var bond;
if (J.modelset.Bond.isOrderH (order)) {
bond =  new J.modelset.HBond (atom, atomOther, order, mad, 0, energy);
} else {
bond =  new J.modelset.Bond (atom, atomOther, order, mad, 0);
}this.addBondToAtom (atom, bond);
this.addBondToAtom (atomOther, bond);
return bond;
}, "J.modelset.Atom,J.modelset.Atom,~N,~N,~N");
$_M(c$, "addBondToAtom", 
($fz = function (atom, bond) {
if (atom.bonds == null) {
atom.bonds =  new Array (1);
atom.bonds[0] = bond;
} else {
atom.bonds = this.addToBonds (bond, atom.bonds);
}}, $fz.isPrivate = true, $fz), "J.modelset.Atom,J.modelset.Bond");
$_M(c$, "addToBonds", 
($fz = function (newBond, oldBonds) {
var newBonds;
if (oldBonds == null) {
if (this.numCached[1] > 0) newBonds = this.freeBonds[1][--this.numCached[1]];
 else newBonds =  new Array (1);
newBonds[0] = newBond;
} else {
var oldLength = oldBonds.length;
var newLength = oldLength + 1;
if (newLength < 5 && this.numCached[newLength] > 0) newBonds = this.freeBonds[newLength][--this.numCached[newLength]];
 else newBonds =  new Array (newLength);
newBonds[oldLength] = newBond;
for (var i = oldLength; --i >= 0; ) newBonds[i] = oldBonds[i];

if (oldLength < 5 && this.numCached[oldLength] < 200) this.freeBonds[oldLength][this.numCached[oldLength]++] = oldBonds;
}return newBonds;
}, $fz.isPrivate = true, $fz), "J.modelset.Bond,~A");
$_M(c$, "addHBond", 
function (atom1, atom2, order, energy) {
if (this.bondCount == this.bonds.length) this.bonds = J.util.ArrayUtil.arrayCopyObject (this.bonds, this.bondCount + 250);
return this.setBond (this.bondCount++, this.bondMutually (atom1, atom2, order, 1, energy)).index;
}, "J.modelset.Atom,J.modelset.Atom,~N,~N");
c$.getBondOrderFull = $_M(c$, "getBondOrderFull", 
function (bondingRadiusA, bondingRadiusB, distance2, minBondDistance2, bondTolerance) {
if (bondingRadiusA == 0 || bondingRadiusB == 0 || distance2 < minBondDistance2) return 0;
var maxAcceptable = bondingRadiusA + bondingRadiusB + bondTolerance;
var maxAcceptable2 = maxAcceptable * maxAcceptable;
return (distance2 > maxAcceptable2 ? 0 : 1);
}, "~N,~N,~N,~N,~N");
$_M(c$, "checkValencesAndBond", 
function (atomA, atomB, order, mad, bsBonds) {
if (atomA.getCurrentBondCount () > 20 || atomB.getCurrentBondCount () > 20) {
if (!this.haveWarned) J.util.Logger.warn ("maximum auto bond count reached");
this.haveWarned = true;
return false;
}var formalChargeA = atomA.getFormalCharge ();
if (formalChargeA != 0) {
var formalChargeB = atomB.getFormalCharge ();
if ((formalChargeA < 0 && formalChargeB < 0) || (formalChargeA > 0 && formalChargeB > 0)) return false;
}if (atomA.altloc != atomB.altloc && atomA.altloc != '\0' && atomB.altloc != '\0') return false;
this.getOrAddBond (atomA, atomB, order, mad, bsBonds, 0, false);
return true;
}, "J.modelset.Atom,J.modelset.Atom,~N,~N,J.util.BS");
$_M(c$, "deleteAllBonds2", 
function () {
this.viewer.setShapeProperty (1, "reset", null);
for (var i = this.bondCount; --i >= 0; ) {
this.bonds[i].deleteAtomReferences ();
this.bonds[i] = null;
}
this.bondCount = 0;
});
$_M(c$, "getDefaultMadFromOrder", 
function (order) {
return (J.modelset.Bond.isOrderH (order) ? 1 : (order & 32768) != 0 ? Clazz.doubleToInt (Math.floor (this.viewer.getFloat (570425406) * 2000)) : this.defaultCovalentMad);
}, "~N");
$_M(c$, "deleteConnections", 
function (minDistance, maxDistance, order, bsA, bsB, isBonds, matchNull, minDistanceSquared, maxDistanceSquared) {
var minDistanceIsFractionRadius = (minDistance < 0);
var maxDistanceIsFractionRadius = (maxDistance < 0);
var dAB = 0;
var dABcalc = 0;
if (minDistanceIsFractionRadius) minDistance = -minDistance;
if (maxDistanceIsFractionRadius) maxDistance = -maxDistance;
var bsDelete =  new J.util.BS ();
var nDeleted = 0;
var newOrder = order |= 131072;
if (!matchNull && J.modelset.Bond.isOrderH (order)) order = 30720;
var bsBonds;
if (isBonds) {
bsBonds = bsA;
} else {
bsBonds =  new J.util.BS ();
for (var i = bsA.nextSetBit (0); i >= 0; i = bsA.nextSetBit (i + 1)) {
var a = this.atoms[i];
if (a.bonds != null) for (var j = a.bonds.length; --j >= 0; ) if (bsB.get (a.getBondedAtomIndex (j))) bsBonds.set (a.bonds[j].index);

}
}for (var i = bsBonds.nextSetBit (0); i < this.bondCount && i >= 0; i = bsBonds.nextSetBit (i + 1)) {
var bond = this.bonds[i];
var atom1 = bond.atom1;
var atom2 = bond.atom2;
var distanceSquared = atom1.distanceSquared (atom2);
if (minDistanceIsFractionRadius || maxDistanceIsFractionRadius) {
dAB = atom1.distance (atom2);
dABcalc = atom1.getBondingRadiusFloat () + atom2.getBondingRadiusFloat ();
}if ((minDistanceIsFractionRadius ? dAB < dABcalc * minDistance : distanceSquared < minDistanceSquared) || (maxDistanceIsFractionRadius ? dAB > dABcalc * maxDistance : distanceSquared > maxDistanceSquared)) continue;
if (matchNull || newOrder == (bond.order & -257 | 131072) || (order & bond.order & 30720) != 0) {
bsDelete.set (i);
nDeleted++;
}}
if (nDeleted > 0) this.dBm (bsDelete, false);
return [0, nDeleted];
}, "~N,~N,~N,J.util.BS,J.util.BS,~B,~B,~N,~N");
$_M(c$, "dBm", 
function (bsBonds, isFullModel) {
(this).deleteBonds (bsBonds, isFullModel);
}, "J.util.BS,~B");
$_M(c$, "dBb", 
function (bsBond, isFullModel) {
var iDst = bsBond.nextSetBit (0);
if (iDst < 0) return;
this.resetMolecules ();
var modelIndexLast = -1;
var n = bsBond.cardinality ();
for (var iSrc = iDst; iSrc < this.bondCount; ++iSrc) {
var bond = this.bonds[iSrc];
if (n > 0 && bsBond.get (iSrc)) {
n--;
if (!isFullModel) {
var modelIndex = bond.atom1.modelIndex;
if (modelIndex != modelIndexLast) (this).models[modelIndexLast = modelIndex].resetBoundCount ();
}bond.deleteAtomReferences ();
} else {
this.setBond (iDst++, bond);
}}
for (var i = this.bondCount; --i >= iDst; ) this.bonds[i] = null;

this.bondCount = iDst;
var sets = this.viewer.getShapeProperty (1, "sets");
if (sets != null) for (var i = 0; i < sets.length; i++) J.util.BSUtil.deleteBits (sets[i], bsBond);

J.util.BSUtil.deleteBits (this.bsAromatic, bsBond);
}, "J.util.BS,~B");
$_M(c$, "resetAromatic", 
function () {
for (var i = this.bondCount; --i >= 0; ) {
var bond = this.bonds[i];
if (bond.isAromatic ()) bond.setOrder (515);
}
});
$_M(c$, "assignAromaticBonds", 
function () {
this.assignAromaticBondsBs (true, null);
});
$_M(c$, "assignAromaticBondsBs", 
function (isUserCalculation, bsBonds) {
if (!isUserCalculation) this.bsAromatic =  new J.util.BS ();
this.bsAromaticSingle =  new J.util.BS ();
this.bsAromaticDouble =  new J.util.BS ();
var isAll = (bsBonds == null);
var i0 = (isAll ? this.bondCount - 1 : bsBonds.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsBonds.nextSetBit (i + 1))) {
var bond = this.bonds[i];
if (this.bsAromatic.get (i)) bond.setOrder (515);
switch (bond.order & -131073) {
case 515:
this.bsAromatic.set (i);
break;
case 513:
this.bsAromaticSingle.set (i);
break;
case 514:
this.bsAromaticDouble.set (i);
break;
}
}
var bond;
isAll = (bsBonds == null);
i0 = (isAll ? this.bondCount - 1 : bsBonds.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsBonds.nextSetBit (i + 1))) {
bond = this.bonds[i];
if (!bond.is (515) || this.bsAromaticDouble.get (i) || this.bsAromaticSingle.get (i)) continue;
if (!this.assignAromaticDouble (bond)) this.assignAromaticSingle (bond);
}
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsBonds.nextSetBit (i + 1))) {
bond = this.bonds[i];
if (this.bsAromaticDouble.get (i)) {
if (!bond.is (514)) {
this.bsAromatic.set (i);
bond.setOrder (514);
}} else if (this.bsAromaticSingle.get (i) || bond.isAromatic ()) {
if (!bond.is (513)) {
this.bsAromatic.set (i);
bond.setOrder (513);
}}}
this.assignAromaticNandO (bsBonds);
this.bsAromaticSingle = null;
this.bsAromaticDouble = null;
}, "~B,J.util.BS");
$_M(c$, "assignAromaticDouble", 
($fz = function (bond) {
var bondIndex = bond.index;
if (this.bsAromaticSingle.get (bondIndex)) return false;
if (this.bsAromaticDouble.get (bondIndex)) return true;
this.bsAromaticDouble.set (bondIndex);
if (!this.assignAromaticSingleForAtom (bond.atom1, bondIndex) || !this.assignAromaticSingleForAtom (bond.atom2, bondIndex)) {
this.bsAromaticDouble.clear (bondIndex);
return false;
}return true;
}, $fz.isPrivate = true, $fz), "J.modelset.Bond");
$_M(c$, "assignAromaticSingle", 
($fz = function (bond) {
var bondIndex = bond.index;
if (this.bsAromaticDouble.get (bondIndex)) return false;
if (this.bsAromaticSingle.get (bondIndex)) return true;
this.bsAromaticSingle.set (bondIndex);
if (!this.assignAromaticDoubleForAtom (bond.atom1) || !this.assignAromaticDoubleForAtom (bond.atom2)) {
this.bsAromaticSingle.clear (bondIndex);
return false;
}return true;
}, $fz.isPrivate = true, $fz), "J.modelset.Bond");
$_M(c$, "assignAromaticSingleForAtom", 
($fz = function (atom, notBondIndex) {
var bonds = atom.bonds;
if (bonds == null || this.assignAromaticSingleHetero (atom)) return false;
for (var i = bonds.length; --i >= 0; ) {
var bond = bonds[i];
var bondIndex = bond.index;
if (bondIndex == notBondIndex || !bond.isAromatic () || this.bsAromaticSingle.get (bondIndex)) continue;
if (this.bsAromaticDouble.get (bondIndex) || !this.assignAromaticSingle (bond)) {
return false;
}}
return true;
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,~N");
$_M(c$, "assignAromaticDoubleForAtom", 
($fz = function (atom) {
var bonds = atom.bonds;
if (bonds == null) return false;
var haveDouble = this.assignAromaticSingleHetero (atom);
var lastBond = -1;
for (var i = bonds.length; --i >= 0; ) {
if (this.bsAromaticDouble.get (bonds[i].index)) haveDouble = true;
if (bonds[i].isAromatic ()) lastBond = i;
}
for (var i = bonds.length; --i >= 0; ) {
var bond = bonds[i];
var bondIndex = bond.index;
if (!bond.isAromatic () || this.bsAromaticDouble.get (bondIndex) || this.bsAromaticSingle.get (bondIndex)) continue;
if (!haveDouble && this.assignAromaticDouble (bond)) haveDouble = true;
 else if ((haveDouble || i < lastBond) && !this.assignAromaticSingle (bond)) {
return false;
}}
return haveDouble;
}, $fz.isPrivate = true, $fz), "J.modelset.Atom");
$_M(c$, "assignAromaticSingleHetero", 
($fz = function (atom) {
var n = atom.getElementNumber ();
switch (n) {
case 6:
case 7:
case 8:
case 16:
break;
default:
return true;
}
var nAtoms = atom.getValence ();
switch (n) {
case 6:
return (nAtoms == 4);
case 7:
case 8:
return (nAtoms == 10 - n && atom.getFormalCharge () < 1);
case 16:
return (nAtoms == 18 - n && atom.getFormalCharge () < 1);
}
return false;
}, $fz.isPrivate = true, $fz), "J.modelset.Atom");
$_M(c$, "assignAromaticNandO", 
($fz = function (bsSelected) {
var bond;
var isAll = (bsSelected == null);
var i0 = (isAll ? this.bondCount - 1 : bsSelected.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsSelected.nextSetBit (i + 1))) {
bond = this.bonds[i];
if (!bond.is (513)) continue;
var atom1;
var atom2 = bond.atom2;
var n1;
var n2 = atom2.getElementNumber ();
if (n2 == 7 || n2 == 8) {
n1 = n2;
atom1 = atom2;
atom2 = bond.atom1;
n2 = atom2.getElementNumber ();
} else {
atom1 = bond.atom1;
n1 = atom1.getElementNumber ();
}if (n1 != 7 && n1 != 8) continue;
var valence = atom1.getValence ();
if (valence < 0) continue;
var bondorder = atom1.getCovalentBondCount ();
var charge = atom1.getFormalCharge ();
switch (n1) {
case 7:
if (valence == 3 && bondorder == 3 && charge < 1 && n2 == 6 && atom2.getValence () == 3) bond.setOrder (514);
break;
case 8:
if (valence == 1 && charge == 0 && (n2 == 14 || n2 == 16)) bond.setOrder (514);
break;
}
}
}, $fz.isPrivate = true, $fz), "J.util.BS");
$_M(c$, "getAtomBitsMDb", 
function (tokType, specInfo) {
var bs;
switch (tokType) {
default:
return this.getAtomBitsMDa (tokType, specInfo);
case 1678770178:
bs =  new J.util.BS ();
var bsBonds = specInfo;
for (var i = bsBonds.nextSetBit (0); i >= 0; i = bsBonds.nextSetBit (i + 1)) {
bs.set (this.bonds[i].atom1.index);
bs.set (this.bonds[i].atom2.index);
}
return bs;
case 1048585:
bs =  new J.util.BS ();
for (var i = this.bondCount; --i >= 0; ) if (this.bonds[i].isAromatic ()) {
bs.set (this.bonds[i].atom1.index);
bs.set (this.bonds[i].atom2.index);
}
return bs;
}
}, "~N,~O");
$_M(c$, "setBondOrder", 
function (bondIndex, type) {
var bondOrder = type.charCodeAt (0) - 48;
var bond = this.bonds[bondIndex];
switch (type) {
case '0':
case '1':
case '2':
case '3':
break;
case 'p':
case 'm':
bondOrder = J.util.JmolEdge.getBondOrderNumberFromOrder (bond.getCovalentOrder ()).charCodeAt (0) - 48 + (type == 'p' ? 1 : -1);
if (bondOrder > 3) bondOrder = 1;
 else if (bondOrder < 0) bondOrder = 3;
break;
default:
return null;
}
var bsAtoms =  new J.util.BS ();
try {
if (bondOrder == 0) {
var bs =  new J.util.BS ();
bs.set (bond.index);
bsAtoms.set (bond.getAtomIndex1 ());
bsAtoms.set (bond.getAtomIndex2 ());
this.dBm (bs, false);
return bsAtoms;
}bond.setOrder (bondOrder | 131072);
this.removeUnnecessaryBonds (bond.atom1, false);
this.removeUnnecessaryBonds (bond.atom2, false);
bsAtoms.set (bond.getAtomIndex1 ());
bsAtoms.set (bond.getAtomIndex2 ());
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("Exception in seBondOrder: " + e.toString ());
} else {
throw e;
}
}
return bsAtoms;
}, "~N,~S");
$_M(c$, "removeUnnecessaryBonds", 
function (atom, deleteAtom) {
var bs =  new J.util.BS ();
var bsBonds =  new J.util.BS ();
var bonds = atom.bonds;
if (bonds == null) return;
for (var i = 0; i < bonds.length; i++) if (bonds[i].isCovalent ()) {
var atom2 = bonds[i].getOtherAtom (atom);
if (atom2.getElementNumber () == 1) bs.set (bonds[i].getOtherAtom (atom).index);
} else {
bsBonds.set (bonds[i].index);
}
if (bsBonds.nextSetBit (0) >= 0) this.dBm (bsBonds, false);
if (deleteAtom) bs.set (atom.index);
if (bs.nextSetBit (0) >= 0) this.viewer.deleteAtoms (bs, false);
}, "J.modelset.Atom,~B");
$_M(c$, "displayBonds", 
function (bs, isDisplay) {
if (!isDisplay) this.haveHiddenBonds = true;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) if (i < this.bondCount && this.bonds[i].mad != 0) this.bonds[i].setShapeVisibility (isDisplay);

}, "J.modelset.Bond.BondSet,~B");
$_M(c$, "setBondParametersBS", 
function (modelIndex, iBond, bsBonds, rad, pymolValence, argb, trans) {
if (bsBonds == null) this.setBondParameters (modelIndex, iBond, rad, pymolValence, argb, trans);
 else for (var i = bsBonds.nextSetBit (0); i >= 0; i = bsBonds.nextSetBit (i + 1)) this.setBondParameters (modelIndex, i, rad, pymolValence, argb, trans);

}, "~N,~N,J.util.BS,~N,~N,~N,~N");
$_M(c$, "setBondParameters", 
function (modelIndex, i, rad, pymolValence, argb, trans) {
if (i < 0 || i >= this.bondCount) return;
var b = this.bonds[i];
if (modelIndex >= 0 && b.atom1.modelIndex != modelIndex) return;
if (!Float.isNaN (rad)) b.mad = Clazz.floatToShort (rad * 2000);
var colix = b.colix;
if (argb != 2147483647) colix = J.util.C.getColix (argb);
if (!Float.isNaN (trans)) b.colix = J.util.C.getColixTranslucent3 (colix, trans != 0, trans);
 else if (b.colix != colix) b.colix = J.util.C.copyColixTranslucency (b.colix, colix);
if (pymolValence == 1) b.order &= -65537;
 else if (pymolValence == 0) b.order |= 65536;
}, "~N,~N,~N,~N,~N,~N");
Clazz.defineStatics (c$,
"BOND_GROWTH_INCREMENT", 250,
"MAX_BONDS_LENGTH_TO_CACHE", 5,
"MAX_NUM_TO_CACHE", 200);
});
