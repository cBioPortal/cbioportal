Clazz.declarePackage ("J.smiles");
Clazz.load (["J.util.JmolMolecule", "$.BS", "$.JmolList", "$.V3"], "J.smiles.SmilesSearch", ["java.util.Arrays", "$.Hashtable", "J.smiles.SmilesAromatic", "$.SmilesAtom", "$.SmilesBond", "$.SmilesMeasure", "$.SmilesParser", "J.util.ArrayUtil", "$.BSUtil", "$.Logger", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.patternAtoms = null;
this.pattern = null;
this.jmolAtoms = null;
this.jmolAtomCount = 0;
this.bsSelected = null;
this.bsRequired = null;
this.firstMatchOnly = false;
this.matchAllAtoms = false;
this.isSmarts = false;
this.isSmilesFind = false;
this.subSearches = null;
this.haveSelected = false;
this.haveBondStereochemistry = false;
this.haveAtomStereochemistry = false;
this.needRingData = false;
this.needAromatic = true;
this.needRingMemberships = false;
this.ringDataMax = -2147483648;
this.measures = null;
this.flags = 0;
this.ringSets = null;
this.bsAromatic = null;
this.bsAromatic5 = null;
this.bsAromatic6 = null;
this.lastChainAtom = null;
this.asVector = false;
this.getMaps = false;
this.top = null;
this.isSilent = false;
this.isRingCheck = false;
this.selectedAtomCount = 0;
this.ringData = null;
this.ringCounts = null;
this.ringConnections = null;
this.bsFound = null;
this.htNested = null;
this.nNested = 0;
this.nestedBond = null;
this.vReturn = null;
this.bsReturn = null;
this.ignoreStereochemistry = false;
this.noAromatic = false;
this.aromaticDouble = false;
this.bsCheck = null;
this.v = null;
Clazz.instantialize (this, arguments);
}, J.smiles, "SmilesSearch", J.util.JmolMolecule);
Clazz.prepareFields (c$, function () {
this.patternAtoms =  new Array (16);
this.measures =  new J.util.JmolList ();
this.bsAromatic =  new J.util.BS ();
this.bsAromatic5 =  new J.util.BS ();
this.bsAromatic6 =  new J.util.BS ();
this.top = this;
this.bsFound =  new J.util.BS ();
this.bsReturn =  new J.util.BS ();
this.v =  new J.smiles.SmilesSearch.VTemp ();
});
$_M(c$, "toString", 
function () {
var sb =  new J.util.SB ().append (this.pattern);
sb.append ("\nmolecular formula: " + this.getMolecularFormula (true));
return sb.toString ();
});
$_M(c$, "setSelected", 
function (bs) {
if (bs == null) {
bs = J.util.BSUtil.newBitSet (this.jmolAtomCount);
bs.setBits (0, this.jmolAtomCount);
}this.bsSelected = bs;
}, "J.util.BS");
$_M(c$, "setAtomArray", 
function () {
if (this.patternAtoms.length > this.atomCount) this.patternAtoms = J.util.ArrayUtil.arrayCopyObject (this.patternAtoms, this.atomCount);
this.nodes = this.patternAtoms;
});
$_M(c$, "addAtom", 
function () {
if (this.atomCount >= this.patternAtoms.length) this.patternAtoms = J.util.ArrayUtil.doubleLength (this.patternAtoms);
var sAtom =  new J.smiles.SmilesAtom ().setIndex (this.atomCount);
this.patternAtoms[this.atomCount] = sAtom;
this.atomCount++;
return sAtom;
});
$_M(c$, "addNested", 
function (pattern) {
if (this.top.htNested == null) this.top.htNested =  new java.util.Hashtable ();
this.setNested (++this.top.nNested, pattern);
return this.top.nNested;
}, "~S");
$_M(c$, "clear", 
function () {
this.bsReturn.clearAll ();
this.nNested = 0;
this.htNested = null;
this.nestedBond = null;
this.clearBsFound (-1);
});
$_M(c$, "setNested", 
function (iNested, o) {
this.top.htNested.put ("_" + iNested, o);
}, "~N,~O");
$_M(c$, "getNested", 
function (iNested) {
return this.top.htNested.get ("_" + iNested);
}, "~N");
$_M(c$, "getMissingHydrogenCount", 
function () {
var n = 0;
var nH;
for (var i = 0; i < this.atomCount; i++) if ((nH = this.patternAtoms[i].missingHydrogenCount) >= 0) n += nH;

return n;
});
$_M(c$, "setRingData", 
function (bsA) {
if (this.needAromatic) this.needRingData = true;
var noAromatic = ((this.flags & 1) != 0);
this.needAromatic = new Boolean (this.needAromatic & ( new Boolean ((bsA == null) & !noAromatic).valueOf ())).valueOf ();
if (!this.needAromatic) {
this.bsAromatic.clearAll ();
if (bsA != null) this.bsAromatic.or (bsA);
if (!this.needRingMemberships && !this.needRingData) return;
}this.getRingData (this.needRingData, this.flags, null);
}, "J.util.BS");
$_M(c$, "getRingData", 
function (needRingData, flags, vRings) {
var aromaticStrict = ((flags & 4) != 0);
var aromaticDefined = ((flags & 8) != 0);
if (aromaticStrict && vRings == null) vRings = J.util.ArrayUtil.createArrayOfArrayList (4);
if (aromaticDefined && this.needAromatic) {
this.bsAromatic = J.smiles.SmilesAromatic.checkAromaticDefined (this.jmolAtoms, this.bsSelected);
aromaticStrict = false;
}if (this.ringDataMax < 0) this.ringDataMax = 8;
if (aromaticStrict && this.ringDataMax < 6) this.ringDataMax = 6;
if (needRingData) {
this.ringCounts =  Clazz.newIntArray (this.jmolAtomCount, 0);
this.ringConnections =  Clazz.newIntArray (this.jmolAtomCount, 0);
this.ringData =  new Array (this.ringDataMax + 1);
}this.ringSets =  new J.util.SB ();
var s = "****";
while (s.length < this.ringDataMax) s += s;

var v5 = null;
for (var i = 3; i <= this.ringDataMax; i++) {
if (i > this.jmolAtomCount) continue;
var smarts = "*1" + s.substring (0, i - 2) + "*1";
var search = J.smiles.SmilesParser.getMolecule (smarts, true);
var vR = this.subsearch (search, false, true);
if (vRings != null && i <= 5) {
var v =  new J.util.JmolList ();
for (var j = vR.size (); --j >= 0; ) v.addLast (vR.get (j));

vRings[i - 3] = v;
}if (this.needAromatic) {
if (!aromaticDefined && (!aromaticStrict || i == 5 || i == 6)) for (var r = vR.size (); --r >= 0; ) {
var bs = vR.get (r);
if (aromaticDefined || J.smiles.SmilesAromatic.isFlatSp2Ring (this.jmolAtoms, this.bsSelected, bs, (aromaticStrict ? 0.1 : 0.01))) this.bsAromatic.or (bs);
}
if (aromaticStrict) {
switch (i) {
case 5:
v5 = vR;
break;
case 6:
if (aromaticDefined) this.bsAromatic = J.smiles.SmilesAromatic.checkAromaticDefined (this.jmolAtoms, this.bsAromatic);
 else J.smiles.SmilesAromatic.checkAromaticStrict (this.jmolAtoms, this.bsAromatic, v5, vR);
vRings[3] =  new J.util.JmolList ();
this.setAromatic56 (v5, this.bsAromatic5, 5, vRings[3]);
this.setAromatic56 (vR, this.bsAromatic6, 6, vRings[3]);
break;
}
}}if (needRingData) {
this.ringData[i] =  new J.util.BS ();
for (var k = 0; k < vR.size (); k++) {
var r = vR.get (k);
this.ringData[i].or (r);
for (var j = r.nextSetBit (0); j >= 0; j = r.nextSetBit (j + 1)) this.ringCounts[j]++;

}
}}
if (needRingData) {
for (var i = this.bsSelected.nextSetBit (0); i >= 0; i = this.bsSelected.nextSetBit (i + 1)) {
var atom = this.jmolAtoms[i];
var bonds = atom.getEdges ();
if (bonds != null) for (var k = bonds.length; --k >= 0; ) if (this.ringCounts[atom.getBondedAtomIndex (k)] > 0) this.ringConnections[i]++;

}
}}, "~B,~N,~A");
$_M(c$, "setAromatic56", 
($fz = function (vRings, bs56, n56, vAromatic56) {
for (var k = 0; k < vRings.size (); k++) {
var r = vRings.get (k);
this.v.bsTemp.clearAll ();
this.v.bsTemp.or (r);
this.v.bsTemp.and (this.bsAromatic);
if (this.v.bsTemp.cardinality () == n56) {
bs56.or (r);
if (vAromatic56 != null) vAromatic56.addLast (r);
}}
}, $fz.isPrivate = true, $fz), "J.util.JmolList,J.util.BS,~N,J.util.JmolList");
$_M(c$, "subsearch", 
function (search, firstAtomOnly, isRingCheck) {
search.ringSets = this.ringSets;
search.jmolAtoms = this.jmolAtoms;
search.jmolAtomCount = this.jmolAtomCount;
search.bsSelected = this.bsSelected;
search.htNested = this.htNested;
search.isSmilesFind = this.isSmilesFind;
search.bsCheck = this.bsCheck;
search.isSmarts = true;
search.bsAromatic = this.bsAromatic;
search.bsAromatic5 = this.bsAromatic5;
search.bsAromatic6 = this.bsAromatic6;
search.ringData = this.ringData;
search.ringCounts = this.ringCounts;
search.ringConnections = this.ringConnections;
if (firstAtomOnly) {
search.bsRequired = null;
search.firstMatchOnly = false;
search.matchAllAtoms = false;
} else if (isRingCheck) {
search.bsRequired = null;
search.isSilent = true;
search.isRingCheck = true;
search.asVector = true;
search.matchAllAtoms = false;
} else {
search.haveSelected = this.haveSelected;
search.bsRequired = this.bsRequired;
search.firstMatchOnly = this.firstMatchOnly;
search.matchAllAtoms = this.matchAllAtoms;
search.getMaps = this.getMaps;
search.asVector = this.asVector;
search.vReturn = this.vReturn;
search.bsReturn = this.bsReturn;
}return search.search (firstAtomOnly);
}, "J.smiles.SmilesSearch,~B,~B");
$_M(c$, "search", 
function (firstAtomOnly) {
this.ignoreStereochemistry = ((this.flags & 2) != 0);
this.noAromatic = ((this.flags & 1) != 0);
this.aromaticDouble = ((this.flags & 16) != 0);
if (J.util.Logger.debugging && !this.isSilent) J.util.Logger.debug ("SmilesSearch processing " + this.pattern);
if (this.vReturn == null && (this.asVector || this.getMaps)) this.vReturn =  new J.util.JmolList ();
if (this.bsSelected == null) {
this.bsSelected = J.util.BSUtil.newBitSet (this.jmolAtomCount);
this.bsSelected.setBits (0, this.jmolAtomCount);
}this.selectedAtomCount = this.bsSelected.cardinality ();
if (this.subSearches != null) {
for (var i = 0; i < this.subSearches.length; i++) {
if (this.subSearches[i] == null) continue;
this.subsearch (this.subSearches[i], false, false);
if (this.firstMatchOnly) {
if (this.vReturn == null ? this.bsReturn.nextSetBit (0) >= 0 : this.vReturn.size () > 0) break;
}}
} else if (this.atomCount > 0) {
this.checkMatch (null, -1, -1, firstAtomOnly);
}return (this.asVector || this.getMaps ? this.vReturn : this.bsReturn);
}, "~B");
$_M(c$, "checkMatch", 
($fz = function (patternAtom, atomNum, iAtom, firstAtomOnly) {
var jmolAtom;
var jmolBonds;
if (patternAtom == null) {
if (this.nestedBond == null) {
this.clearBsFound (-1);
} else {
this.bsReturn.clearAll ();
}} else {
if (this.bsFound.get (iAtom) || !this.bsSelected.get (iAtom)) return true;
jmolAtom = this.jmolAtoms[iAtom];
if (!this.isRingCheck) {
if (patternAtom.atomsOr != null) {
for (var ii = 0; ii < patternAtom.nAtomsOr; ii++) if (!this.checkMatch (patternAtom.atomsOr[ii], atomNum, iAtom, firstAtomOnly)) return false;

return true;
}if (patternAtom.primitives == null) {
if (!this.checkPrimitiveAtom (patternAtom, iAtom)) return true;
} else {
for (var i = 0; i < patternAtom.nPrimitives; i++) if (!this.checkPrimitiveAtom (patternAtom.primitives[i], iAtom)) return true;

}}jmolBonds = jmolAtom.getEdges ();
for (var i = patternAtom.getBondCount (); --i >= 0; ) {
var patternBond = patternAtom.getBond (i);
if (patternBond.getAtomIndex2 () != patternAtom.index) continue;
var atom1 = patternBond.getAtom1 ();
var matchingAtom = atom1.getMatchingAtom ();
switch (patternBond.order) {
case 96:
case 112:
if (!this.checkMatchBond (patternAtom, atom1, patternBond, iAtom, matchingAtom, null)) return true;
break;
default:
var k = 0;
for (; k < jmolBonds.length; k++) if ((jmolBonds[k].getAtomIndex1 () == matchingAtom || jmolBonds[k].getAtomIndex2 () == matchingAtom) && jmolBonds[k].isCovalent ()) break;

if (k == jmolBonds.length) return true;
if (!this.checkMatchBond (patternAtom, atom1, patternBond, iAtom, matchingAtom, jmolBonds[k])) return true;
}
}
this.patternAtoms[patternAtom.index].setMatchingAtom (iAtom);
if (J.util.Logger.debugging && !this.isSilent) J.util.Logger.debug ("pattern atom " + atomNum + " " + patternAtom);
this.bsFound.set (iAtom);
}if (!this.continueMatch (atomNum, iAtom, firstAtomOnly)) return false;
if (iAtom >= 0) this.clearBsFound (iAtom);
return true;
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesAtom,~N,~N,~B");
$_M(c$, "continueMatch", 
($fz = function (atomNum, iAtom, firstAtomOnly) {
var jmolAtom;
var jmolBonds;
if (++atomNum < this.atomCount) {
var newPatternAtom = this.patternAtoms[atomNum];
var newPatternBond = (iAtom >= 0 ? newPatternAtom.getBondTo (null) : atomNum == 0 ? this.nestedBond : null);
if (newPatternBond == null) {
var bs = J.util.BSUtil.copy (this.bsFound);
if (newPatternAtom.notBondedIndex >= 0) {
var pa = this.patternAtoms[newPatternAtom.notBondedIndex];
var a = this.jmolAtoms[pa.getMatchingAtom ()];
if (pa.bioType == '\0') {
jmolBonds = a.getEdges ();
for (var k = 0; k < jmolBonds.length; k++) bs.set (jmolBonds[k].getOtherAtomNode (a).getIndex ());

} else {
var ii = a.getOffsetResidueAtom ("0", 1);
if (ii >= 0) bs.set (ii);
ii = a.getOffsetResidueAtom ("0", -1);
if (ii >= 0) bs.set (ii);
}}var skipGroup = (iAtom >= 0 && newPatternAtom.isBioAtom && (newPatternAtom.atomName == null || newPatternAtom.residueChar != null));
for (var j = this.bsSelected.nextSetBit (0); j >= 0; j = this.bsSelected.nextSetBit (j + 1)) {
if (!bs.get (j) && !this.checkMatch (newPatternAtom, atomNum, j, firstAtomOnly)) return false;
if (skipGroup) {
var j1 = this.jmolAtoms[j].getOffsetResidueAtom (newPatternAtom.atomName, 1);
if (j1 >= 0) j = j1 - 1;
}}
this.bsFound = bs;
return true;
}jmolAtom = this.jmolAtoms[newPatternBond.getAtom1 ().getMatchingAtom ()];
switch (newPatternBond.order) {
case 96:
var nextGroupAtom = jmolAtom.getOffsetResidueAtom (newPatternAtom.atomName, 1);
if (nextGroupAtom >= 0) {
var bs = J.util.BSUtil.copy (this.bsFound);
jmolAtom.getGroupBits (this.bsFound);
if (!this.checkMatch (newPatternAtom, atomNum, nextGroupAtom, firstAtomOnly)) return false;
this.bsFound = bs;
}return true;
case 112:
var vLinks =  new J.util.JmolList ();
jmolAtom.getCrossLinkLeadAtomIndexes (vLinks);
var bs = J.util.BSUtil.copy (this.bsFound);
jmolAtom.getGroupBits (this.bsFound);
for (var j = 0; j < vLinks.size (); j++) if (!this.checkMatch (newPatternAtom, atomNum, vLinks.get (j).intValue (), firstAtomOnly)) return false;

this.bsFound = bs;
return true;
}
jmolBonds = jmolAtom.getEdges ();
if (jmolBonds != null) for (var j = 0; j < jmolBonds.length; j++) if (!this.checkMatch (newPatternAtom, atomNum, jmolAtom.getBondedAtomIndex (j), firstAtomOnly)) return false;

this.clearBsFound (iAtom);
return true;
}if (!this.ignoreStereochemistry && !this.checkStereochemistry ()) return true;
var bs =  new J.util.BS ();
var nMatch = 0;
for (var j = 0; j < this.atomCount; j++) {
var i = this.patternAtoms[j].getMatchingAtom ();
if (!firstAtomOnly && this.top.haveSelected && !this.patternAtoms[j].selected) continue;
nMatch++;
bs.set (i);
if (this.patternAtoms[j].isBioAtom && this.patternAtoms[j].atomName == null) this.jmolAtoms[i].getGroupBits (bs);
if (firstAtomOnly) break;
if (!this.isSmarts && this.patternAtoms[j].missingHydrogenCount > 0) this.getHydrogens (this.jmolAtoms[i], bs);
}
if (this.bsRequired != null && !this.bsRequired.intersects (bs)) return true;
if (this.matchAllAtoms && bs.cardinality () != this.selectedAtomCount) return true;
if (this.bsCheck != null) {
if (firstAtomOnly) {
this.bsCheck.clearAll ();
for (var j = 0; j < this.atomCount; j++) {
this.bsCheck.set (this.patternAtoms[j].getMatchingAtom ());
}
if (this.bsCheck.cardinality () != this.atomCount) return true;
} else {
if (bs.cardinality () != this.atomCount) return true;
}}this.bsReturn.or (bs);
if (this.getMaps) {
var map =  Clazz.newIntArray (nMatch, 0);
for (var j = 0, nn = 0; j < this.atomCount; j++) {
if (!firstAtomOnly && this.top.haveSelected && !this.patternAtoms[j].selected) continue;
map[nn++] = this.patternAtoms[j].getMatchingAtom ();
}
this.vReturn.addLast (map);
return !this.firstMatchOnly;
}if (this.asVector) {
var isOK = true;
for (var j = this.vReturn.size (); --j >= 0 && isOK; ) isOK = !((this.vReturn.get (j)).equals (bs));

if (!isOK) return true;
this.vReturn.addLast (bs);
}if (this.isRingCheck) {
this.ringSets.append (" ");
for (var k = atomNum * 3 + 2; --k > atomNum; ) this.ringSets.append ("-").appendI (this.patternAtoms[(k <= atomNum * 2 ? atomNum * 2 - k + 1 : k - 1) % atomNum].getMatchingAtom ());

this.ringSets.append ("- ");
return true;
}if (this.firstMatchOnly) return false;
return (bs.cardinality () != this.selectedAtomCount);
}, $fz.isPrivate = true, $fz), "~N,~N,~B");
$_M(c$, "clearBsFound", 
($fz = function (iAtom) {
if (iAtom < 0) {
if (this.bsCheck == null) {
this.bsFound.clearAll ();
}} else this.bsFound.clear (iAtom);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getHydrogens", 
($fz = function (atom, bsHydrogens) {
var b = atom.getEdges ();
var k = -1;
for (var i = 0; i < b.length; i++) if (this.jmolAtoms[atom.getBondedAtomIndex (i)].getElementNumber () == 1) {
k = atom.getBondedAtomIndex (i);
if (bsHydrogens == null) break;
bsHydrogens.set (k);
}
return (k >= 0 ? this.jmolAtoms[k] : null);
}, $fz.isPrivate = true, $fz), "J.util.JmolNode,J.util.BS");
$_M(c$, "checkPrimitiveAtom", 
($fz = function (patternAtom, iAtom) {
var atom = this.jmolAtoms[iAtom];
var foundAtom = patternAtom.not;
while (true) {
var n;
if (patternAtom.iNested > 0) {
var o = this.getNested (patternAtom.iNested);
if (Clazz.instanceOf (o, J.smiles.SmilesSearch)) {
var search = o;
if (patternAtom.isBioAtom) search.nestedBond = patternAtom.getBondTo (null);
o = this.subsearch (search, true, false);
if (o == null) o =  new J.util.BS ();
if (!patternAtom.isBioAtom) this.setNested (patternAtom.iNested, o);
}foundAtom = (patternAtom.not != ((o).get (iAtom)));
break;
}if (patternAtom.isBioAtom) {
if (patternAtom.atomName != null && (patternAtom.isLeadAtom () ? !atom.isLeadAtom () : !patternAtom.atomName.equals (atom.getAtomName ().toUpperCase ()))) break;
if (patternAtom.residueName != null && !patternAtom.residueName.equals (atom.getGroup3 (false).toUpperCase ())) break;
if (patternAtom.residueChar != null) {
if (patternAtom.isDna () && !atom.isDna () || patternAtom.isRna () && !atom.isRna () || patternAtom.isProtein () && !atom.isProtein () || patternAtom.isNucleic () && !atom.isNucleic ()) break;
var s = atom.getGroup1 ('\0').toUpperCase ();
var isOK = patternAtom.residueChar.equals (s);
switch (patternAtom.residueChar.charAt (0)) {
case 'N':
isOK = patternAtom.isNucleic () ? atom.isNucleic () : isOK;
break;
case 'R':
isOK = patternAtom.isNucleic () ? atom.isPurine () : isOK;
break;
case 'Y':
isOK = patternAtom.isNucleic () ? atom.isPyrimidine () : isOK;
break;
}
if (!isOK) break;
}if (patternAtom.elementNumber >= 0 && patternAtom.elementNumber != atom.getElementNumber ()) break;
if (patternAtom.notCrossLinked && atom.getCrossLinkLeadAtomIndexes (null)) break;
} else {
if (patternAtom.jmolIndex >= 0 && atom.getIndex () != patternAtom.jmolIndex) break;
if (patternAtom.atomType != null && !patternAtom.atomType.equals (atom.getAtomType ())) break;
if (patternAtom.elementNumber >= 0 && patternAtom.elementNumber != atom.getElementNumber ()) break;
var isAromatic = patternAtom.isAromatic ();
if (!this.noAromatic && !patternAtom.aromaticAmbiguous && isAromatic != this.bsAromatic.get (iAtom)) break;
if ((n = patternAtom.getAtomicMass ()) != -2147483648) {
var isotope = atom.getIsotopeNumber ();
if (n >= 0 && n != isotope || n < 0 && isotope != 0 && -n != isotope) {
break;
}}if ((n = patternAtom.getCharge ()) != -2147483648 && n != atom.getFormalCharge ()) break;
n = patternAtom.getCovalentHydrogenCount () + patternAtom.missingHydrogenCount;
if (n >= 0 && n != atom.getCovalentHydrogenCount ()) break;
n = patternAtom.implicitHydrogenCount;
if (n != -2147483648) {
var nH = atom.getImplicitHydrogenCount ();
if (n == -1 ? nH == 0 : n != nH) break;
}if (patternAtom.degree > 0 && patternAtom.degree != atom.getCovalentBondCount ()) break;
if (patternAtom.nonhydrogenDegree > 0 && patternAtom.nonhydrogenDegree != atom.getCovalentBondCount () - atom.getCovalentHydrogenCount ()) break;
if (patternAtom.valence > 0 && patternAtom.valence != atom.getValence ()) break;
if (patternAtom.connectivity > 0 && patternAtom.connectivity != atom.getCovalentBondCount () + atom.getImplicitHydrogenCount ()) break;
if (this.ringData != null && patternAtom.ringSize >= -1) {
if (patternAtom.ringSize <= 0) {
if ((this.ringCounts[iAtom] == 0) != (patternAtom.ringSize == 0)) break;
} else {
var rd = this.ringData[patternAtom.ringSize == 500 ? 5 : patternAtom.ringSize == 600 ? 6 : patternAtom.ringSize];
if (rd == null || !rd.get (iAtom)) break;
if (!this.noAromatic) if (patternAtom.ringSize == 500) {
if (!this.bsAromatic5.get (iAtom)) break;
} else if (patternAtom.ringSize == 600) {
if (!this.bsAromatic6.get (iAtom)) break;
}}}if (this.ringData != null && patternAtom.ringMembership >= -1) {
if (patternAtom.ringMembership == -1 ? this.ringCounts[iAtom] == 0 : this.ringCounts[iAtom] != patternAtom.ringMembership) break;
}if (patternAtom.ringConnectivity >= 0) {
n = this.ringConnections[iAtom];
if (patternAtom.ringConnectivity == -1 && n == 0 || patternAtom.ringConnectivity != -1 && n != patternAtom.ringConnectivity) break;
}}foundAtom = !foundAtom;
break;
}
return foundAtom;
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesAtom,~N");
$_M(c$, "checkMatchBond", 
($fz = function (patternAtom, atom1, patternBond, iAtom, matchingAtom, bond) {
if (patternBond.bondsOr != null) {
for (var ii = 0; ii < patternBond.nBondsOr; ii++) if (this.checkMatchBond (patternAtom, atom1, patternBond.bondsOr[ii], iAtom, matchingAtom, bond)) return true;

return false;
}if (patternBond.primitives == null) {
if (!this.checkPrimitiveBond (patternBond, iAtom, matchingAtom, bond)) return false;
} else {
for (var i = 0; i < patternBond.nPrimitives; i++) if (!this.checkPrimitiveBond (patternBond.primitives[i], iAtom, matchingAtom, bond)) return false;

}patternBond.matchingBond = bond;
return true;
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesAtom,J.smiles.SmilesAtom,J.smiles.SmilesBond,~N,~N,J.util.JmolEdge");
$_M(c$, "checkPrimitiveBond", 
($fz = function (patternBond, iAtom1, iAtom2, bond) {
var bondFound = false;
switch (patternBond.order) {
case 96:
return (patternBond.isNot != (this.jmolAtoms[iAtom2].getOffsetResidueAtom ("0", 1) == this.jmolAtoms[iAtom1].getOffsetResidueAtom ("0", 0)));
case 112:
return (patternBond.isNot != this.jmolAtoms[iAtom1].isCrossLinked (this.jmolAtoms[iAtom2]));
}
var isAromatic1 = (!this.noAromatic && this.bsAromatic.get (iAtom1));
var isAromatic2 = (!this.noAromatic && this.bsAromatic.get (iAtom2));
var order = bond.getCovalentOrder ();
if (isAromatic1 && isAromatic2) {
switch (patternBond.order) {
case 17:
case 65:
bondFound = J.smiles.SmilesSearch.isRingBond (this.ringSets, iAtom1, iAtom2);
break;
case 1:
bondFound = !this.isSmarts || !J.smiles.SmilesSearch.isRingBond (this.ringSets, iAtom1, iAtom2);
break;
case 2:
bondFound = !this.isSmarts || this.aromaticDouble && (order == 2 || order == 514);
break;
case 769:
case 1025:
case 81:
case -1:
bondFound = true;
break;
}
} else {
switch (patternBond.order) {
case 81:
case -1:
bondFound = true;
break;
case 1:
case 257:
case 513:
bondFound = (order == 1 || order == 1041 || order == 1025);
break;
case 769:
bondFound = (order == (this.isSmilesFind ? 33 : 1));
break;
case 1025:
bondFound = (order == (this.isSmilesFind ? 97 : 1));
break;
case 2:
bondFound = (order == 2);
break;
case 3:
bondFound = (order == 3);
break;
case 65:
bondFound = J.smiles.SmilesSearch.isRingBond (this.ringSets, iAtom1, iAtom2);
break;
}
}return bondFound != patternBond.isNot;
}, $fz.isPrivate = true, $fz), "J.smiles.SmilesBond,~N,~N,J.util.JmolEdge");
c$.isRingBond = $_M(c$, "isRingBond", 
function (ringSets, i, j) {
return (ringSets != null && ringSets.indexOf ("-" + i + "-" + j + "-") >= 0);
}, "J.util.SB,~N,~N");
$_M(c$, "checkStereochemistry", 
($fz = function () {
for (var i = 0; i < this.measures.size (); i++) if (!this.measures.get (i).check ()) return false;

if (this.haveAtomStereochemistry) {
if (J.util.Logger.debugging) J.util.Logger.debug ("checking stereochemistry...");
var atom1 = null;
var atom2 = null;
var atom3 = null;
var atom4 = null;
var atom5 = null;
var atom6 = null;
var sAtom1 = null;
var sAtom2 = null;
var jn;
for (var i = 0; i < this.atomCount; i++) {
var sAtom = this.patternAtoms[i];
var atom0 = this.jmolAtoms[sAtom.getMatchingAtom ()];
var nH = sAtom.missingHydrogenCount;
if (nH < 0) nH = 0;
var chiralClass = sAtom.getChiralClass ();
if (chiralClass == -2147483648) continue;
var order = sAtom.getChiralOrder ();
if (this.isSmilesFind && (atom0.getAtomSite () >> 8) != chiralClass) return false;
atom4 = null;
if (J.util.Logger.debugging) J.util.Logger.debug ("...type " + chiralClass + " for pattern atom " + sAtom + " " + atom0);
switch (chiralClass) {
case 2:
var isAllene = true;
if (isAllene) {
sAtom1 = sAtom.getBond (0).getOtherAtom (sAtom);
sAtom2 = sAtom.getBond (1).getOtherAtom (sAtom);
if (sAtom1 == null || sAtom2 == null) continue;
var sAtom1a = sAtom;
var sAtom2a = sAtom;
while (sAtom1.getBondCount () == 2 && sAtom2.getBondCount () == 2 && sAtom1.getValence () == 4 && sAtom2.getValence () == 4) {
var b = sAtom1.getBondNotTo (sAtom1a, true);
sAtom1a = sAtom1;
sAtom1 = b.getOtherAtom (sAtom1);
b = sAtom2.getBondNotTo (sAtom2a, true);
sAtom2a = sAtom2;
sAtom2 = b.getOtherAtom (sAtom2);
}
sAtom = sAtom1;
}jn =  new Array (6);
jn[4] =  new J.smiles.SmilesAtom ().setIndex (604);
var nBonds = sAtom.getBondCount ();
for (var k = 0; k < nBonds; k++) {
sAtom1 = sAtom.bonds[k].getOtherAtom (sAtom);
if (sAtom.bonds[k].matchingBond.getCovalentOrder () == 2) {
if (sAtom2 == null) sAtom2 = sAtom1;
} else if (jn[0] == null) {
jn[0] = this.getJmolAtom (sAtom1.getMatchingAtom ());
} else {
jn[1] = this.getJmolAtom (sAtom1.getMatchingAtom ());
}}
if (sAtom2 == null) continue;
nBonds = sAtom2.getBondCount ();
if (nBonds < 2 || nBonds > 3) continue;
for (var k = 0; k < nBonds; k++) {
sAtom1 = sAtom2.bonds[k].getOtherAtom (sAtom2);
if (sAtom2.bonds[k].matchingBond.getCovalentOrder () == 2) {
} else if (jn[2] == null) {
jn[2] = this.getJmolAtom (sAtom1.getMatchingAtom ());
} else {
jn[3] = this.getJmolAtom (sAtom1.getMatchingAtom ());
}}
if (this.isSmilesFind) {
if (jn[1] == null) this.getX (sAtom, jn, 1, false, isAllene);
if (jn[3] == null) this.getX (sAtom2, jn, 3, false, false);
if (!this.setSmilesCoordinates (atom0, sAtom, sAtom2, jn)) return false;
}if (jn[1] == null) this.getX (sAtom, jn, 1, true, false);
if (jn[3] == null) this.getX (sAtom2, jn, 3, true, false);
if (!J.smiles.SmilesSearch.checkStereochemistryAll (sAtom.not, atom0, chiralClass, order, jn[0], jn[1], jn[2], jn[3], null, null, this.v)) return false;
continue;
case 4:
case 8:
case 5:
case 6:
atom1 = this.getJmolAtom (sAtom.getMatchingBondedAtom (0));
switch (nH) {
case 0:
atom2 = this.getJmolAtom (sAtom.getMatchingBondedAtom (1));
break;
case 1:
atom2 = this.getHydrogens (this.getJmolAtom (sAtom.getMatchingAtom ()), null);
if (sAtom.isFirst) {
var a = atom2;
atom2 = atom1;
atom1 = a;
}break;
default:
continue;
}
atom3 = this.getJmolAtom (sAtom.getMatchingBondedAtom (2 - nH));
atom4 = this.getJmolAtom (sAtom.getMatchingBondedAtom (3 - nH));
atom5 = this.getJmolAtom (sAtom.getMatchingBondedAtom (4 - nH));
atom6 = this.getJmolAtom (sAtom.getMatchingBondedAtom (5 - nH));
if (this.isSmilesFind && !this.setSmilesCoordinates (atom0, sAtom, sAtom2, [atom1, atom2, atom3, atom4, atom5, atom6])) return false;
if (!J.smiles.SmilesSearch.checkStereochemistryAll (sAtom.not, atom0, chiralClass, order, atom1, atom2, atom3, atom4, atom5, atom6, this.v)) return false;
continue;
}
}
}if (this.haveBondStereochemistry) {
for (var k = 0; k < this.atomCount; k++) {
var sAtom1 = this.patternAtoms[k];
var sAtom2 = null;
var sAtomDirected1 = null;
var sAtomDirected2 = null;
var dir1 = 0;
var dir2 = 0;
var bondType = 0;
var b;
var nBonds = sAtom1.getBondCount ();
var isAtropisomer = false;
for (var j = 0; j < nBonds; j++) {
b = sAtom1.getBond (j);
var isAtom2 = (b.getAtom2 () === sAtom1);
var type = b.order;
switch (type) {
case 769:
case 1025:
case 2:
if (isAtom2) continue;
sAtom2 = b.getAtom2 ();
bondType = type;
isAtropisomer = (type != 2);
if (isAtropisomer) dir1 = (b.isNot ? -1 : 1);
break;
case 257:
case 513:
sAtomDirected1 = (isAtom2 ? b.getAtom1 () : b.getAtom2 ());
dir1 = (isAtom2 != (type == 257) ? 1 : -1);
break;
}
}
if (isAtropisomer) {
b = sAtom1.getBondNotTo (sAtom2, false);
if (b == null) return false;
sAtomDirected1 = b.getOtherAtom (sAtom1);
b = sAtom2.getBondNotTo (sAtom1, false);
if (b == null) return false;
sAtomDirected2 = b.getOtherAtom (sAtom2);
} else {
if (sAtom2 == null || dir1 == 0) continue;
nBonds = sAtom2.getBondCount ();
for (var j = 0; j < nBonds && dir2 == 0; j++) {
b = sAtom2.getBond (j);
var isAtom2 = (b.getAtom2 () === sAtom2);
var type = b.order;
switch (type) {
case 257:
case 513:
sAtomDirected2 = (isAtom2 ? b.getAtom1 () : b.getAtom2 ());
dir2 = (isAtom2 != (type == 257) ? 1 : -1);
break;
}
}
if (dir2 == 0) continue;
}if (this.isSmilesFind) this.setSmilesBondCoordinates (sAtom1, sAtom2, bondType);
var dbAtom1 = this.getJmolAtom (sAtom1.getMatchingAtom ());
var dbAtom2 = this.getJmolAtom (sAtom2.getMatchingAtom ());
var dbAtom1a = this.getJmolAtom (sAtomDirected1.getMatchingAtom ());
var dbAtom2a = this.getJmolAtom (sAtomDirected2.getMatchingAtom ());
if (dbAtom1a == null || dbAtom2a == null) return false;
J.smiles.SmilesMeasure.setTorsionData (dbAtom1a, dbAtom1, dbAtom2, dbAtom2a, this.v, isAtropisomer);
if (isAtropisomer) {
dir2 = (bondType == 769 ? 1 : -1);
var f = this.v.vTemp1.dot (this.v.vTemp2);
if (f < 0.05 || f > 0.95 || this.v.vNorm1.dot (this.v.vNorm2) * dir1 * dir2 > 0) return false;
} else {
if (this.v.vTemp1.dot (this.v.vTemp2) * dir1 * dir2 < 0) return false;
}}
}return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getX", 
($fz = function (sAtom, jn, pt, haveCoordinates, needHSwitch) {
var atom = this.getJmolAtom (sAtom.getMatchingAtom ());
var doSwitch = sAtom.isFirst || pt == 3;
if (haveCoordinates) {
if (this.isSmarts) {
var b = atom.getEdges ();
for (var i = 0; i < b.length; i++) {
if (b[i].getCovalentOrder () == 2) continue;
var a = this.jmolAtoms[atom.getBondedAtomIndex (i)];
if (a === jn[pt - 1]) continue;
jn[pt] = a;
break;
}
}if (jn[pt] == null) {
var v =  new J.util.V3 ();
var n = 0;
for (var i = 0; i < 4; i++) {
if (jn[i] == null) continue;
n++;
v.sub (jn[i]);
}
if (v.length () == 0) {
v.setT ((jn[4]));
doSwitch = false;
} else {
v.scaleAdd2 (n + 1, this.getJmolAtom (sAtom.getMatchingAtom ()), v);
doSwitch = this.isSmilesFind || doSwitch;
}jn[pt] =  new J.smiles.SmilesAtom ().setIndex (-1);
(jn[pt]).setT (v);
}}if (jn[pt] == null) {
jn[pt] = this.getHydrogens (atom, null);
if (needHSwitch) doSwitch = true;
}if (jn[pt] != null && doSwitch) {
var a = jn[pt];
jn[pt] = jn[pt - 1];
jn[pt - 1] = a;
}}, $fz.isPrivate = true, $fz), "J.smiles.SmilesAtom,~A,~N,~B,~B");
c$.checkStereochemistryAll = $_M(c$, "checkStereochemistryAll", 
function (isNot, atom0, chiralClass, order, atom1, atom2, atom3, atom4, atom5, atom6, v) {
switch (chiralClass) {
default:
case 2:
case 4:
return (isNot == (J.smiles.SmilesSearch.getHandedness (atom2, atom3, atom4, atom1, v) != order));
case 5:
return (isNot == (!J.smiles.SmilesSearch.isDiaxial (atom0, atom0, atom5, atom1, v, -0.95) || J.smiles.SmilesSearch.getHandedness (atom2, atom3, atom4, atom1, v) != order));
case 6:
if (isNot != (!J.smiles.SmilesSearch.isDiaxial (atom0, atom0, atom6, atom1, v, -0.95))) return false;
J.smiles.SmilesSearch.getPlaneNormals (atom2, atom3, atom4, atom5, v);
if (isNot != (v.vNorm1.dot (v.vNorm2) < 0 || v.vNorm2.dot (v.vNorm3) < 0)) return false;
v.vNorm2.setT (atom0);
v.vNorm2.sub (atom1);
return (isNot == ((v.vNorm1.dot (v.vNorm2) < 0 ? 2 : 1) == order));
case 8:
J.smiles.SmilesSearch.getPlaneNormals (atom1, atom2, atom3, atom4, v);
return (v.vNorm1.dot (v.vNorm2) < 0 ? isNot == (order != 3) : v.vNorm2.dot (v.vNorm3) < 0 ? isNot == (order != 2) : isNot == (order != 1));
}
}, "~B,J.util.JmolNode,~N,~N,J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.smiles.SmilesSearch.VTemp");
$_M(c$, "getJmolAtom", 
($fz = function (i) {
return (i < 0 || i >= this.jmolAtoms.length ? null : this.jmolAtoms[i]);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setSmilesBondCoordinates", 
($fz = function (sAtom1, sAtom2, bondType) {
var dbAtom1 = this.jmolAtoms[sAtom1.getMatchingAtom ()];
var dbAtom2 = this.jmolAtoms[sAtom2.getMatchingAtom ()];
dbAtom1.set (-1, 0, 0);
dbAtom2.set (1, 0, 0);
if (bondType == 2) {
var nBonds = 0;
var dir1 = 0;
var bonds = dbAtom1.getEdges ();
for (var k = bonds.length; --k >= 0; ) {
var bond = bonds[k];
var atom = bond.getOtherAtomNode (dbAtom1);
if (atom === dbAtom2) continue;
atom.set (-1, (nBonds++ == 0) ? -1 : 1, 0);
var mode = (bond.getAtomIndex2 () == dbAtom1.getIndex () ? nBonds : -nBonds);
switch (bond.order) {
case 1025:
dir1 = mode;
break;
case 1041:
dir1 = -mode;
}
}
var dir2 = 0;
nBonds = 0;
var atoms =  new Array (2);
bonds = dbAtom2.getEdges ();
for (var k = bonds.length; --k >= 0; ) {
var bond = bonds[k];
var atom = bond.getOtherAtomNode (dbAtom2);
if (atom === dbAtom1) continue;
atoms[nBonds] = atom;
atom.set (1, (nBonds++ == 0) ? 1 : -1, 0);
var mode = (bond.getAtomIndex2 () == dbAtom2.getIndex () ? nBonds : -nBonds);
switch (bond.order) {
case 1025:
dir2 = mode;
break;
case 1041:
dir2 = -mode;
}
}
if ((dir1 * dir2 > 0) == (Math.abs (dir1) % 2 == Math.abs (dir2) % 2)) {
var y = (atoms[0]).y;
(atoms[0]).y = (atoms[1]).y;
(atoms[1]).y = y;
}} else {
var bonds = dbAtom1.getEdges ();
var dir = 0;
for (var k = bonds.length; --k >= 0; ) {
var bond = bonds[k];
if (bond.getOtherAtomNode (dbAtom1) === dbAtom2) {
dir = (bond.order == 33 ? 1 : -1);
break;
}}
for (var k = bonds.length; --k >= 0; ) {
var bond = bonds[k];
var atom = bond.getOtherAtomNode (dbAtom1);
if (atom !== dbAtom2) atom.set (-1, 1, 0);
}
bonds = dbAtom2.getEdges ();
for (var k = bonds.length; --k >= 0; ) {
var bond = bonds[k];
var atom = bond.getOtherAtomNode (dbAtom2);
if (atom !== dbAtom1) atom.set (1, 1, -dir / 2.0);
}
}}, $fz.isPrivate = true, $fz), "J.smiles.SmilesAtom,J.smiles.SmilesAtom,~N");
$_M(c$, "setSmilesCoordinates", 
($fz = function (atom, sAtom, sAtom2, cAtoms) {
var atomSite = atom.getAtomSite ();
if (atomSite == -2147483648) return false;
var chiralClass = atomSite >> 8;
var chiralOrder = atomSite & 0xFF;
var a2 = (chiralClass == 2 || chiralClass == 3 ? a2 = this.jmolAtoms[sAtom2.getMatchingAtom ()] : null);
atom.set (0, 0, 0);
atom = this.jmolAtoms[sAtom.getMatchingAtom ()];
atom.set (0, 0, 0);
var map = this.getMappedAtoms (atom, a2, cAtoms);
switch (chiralClass) {
case 2:
case 4:
if (chiralOrder == 2) {
var i = map[0];
map[0] = map[1];
map[1] = i;
}cAtoms[map[0]].set (0, 0, 1);
cAtoms[map[1]].set (1, 0, -1);
cAtoms[map[2]].set (0, 1, -1);
cAtoms[map[3]].set (-1, -1, -1);
break;
case 8:
switch (chiralOrder) {
case 1:
cAtoms[map[0]].set (1, 0, 0);
cAtoms[map[1]].set (0, 1, 0);
cAtoms[map[2]].set (-1, 0, 0);
cAtoms[map[3]].set (0, -1, 0);
break;
case 2:
cAtoms[map[0]].set (1, 0, 0);
cAtoms[map[1]].set (-1, 0, 0);
cAtoms[map[2]].set (0, 1, 0);
cAtoms[map[3]].set (0, -1, 0);
break;
case 3:
cAtoms[map[0]].set (1, 0, 0);
cAtoms[map[1]].set (0, 1, 0);
cAtoms[map[2]].set (0, -1, 0);
cAtoms[map[3]].set (-1, 0, 0);
break;
}
break;
case 5:
case 6:
var n = map.length;
if (chiralOrder == 2) {
var i = map[0];
map[0] = map[n - 1];
map[n - 1] = i;
}cAtoms[map[0]].set (0, 0, 1);
cAtoms[map[n - 1]].set (0, 0, -1);
cAtoms[map[1]].set (1, 0, 0);
cAtoms[map[2]].set (0, 1, 0);
cAtoms[map[3]].set (-1, 0, 0);
if (n == 6) cAtoms[map[4]].set (0, -1, 0);
break;
}
return true;
}, $fz.isPrivate = true, $fz), "J.util.JmolNode,J.smiles.SmilesAtom,J.smiles.SmilesAtom,~A");
$_M(c$, "getMappedAtoms", 
function (atom, a2, cAtoms) {
var map =  Clazz.newIntArray (cAtoms[4] == null ? 4 : cAtoms[5] == null ? 5 : 6, 0);
for (var i = 0; i < map.length; i++) map[i] = (cAtoms[i] == null ? 104 + i * 100 : cAtoms[i].getIndex ());

var k;
var bonds = atom.getEdges ();
var b2 = (a2 == null ? null : a2.getEdges ());
for (var i = 0; i < map.length; i++) {
for (k = 0; k < bonds.length; k++) if (bonds[k].getOtherAtomNode (atom) === cAtoms[i]) break;

if (k < bonds.length) {
map[i] = (k * 10 + 100) + i;
} else if (a2 != null) {
for (k = 0; k < b2.length; k++) if (b2[k].getOtherAtomNode (a2) === cAtoms[i]) break;

if (k < b2.length) map[i] = (k * 10 + 300) + i;
}}
java.util.Arrays.sort (map);
for (var i = 0; i < map.length; i++) {
map[i] = map[i] % 10;
}
return map;
}, "J.util.JmolNode,J.util.JmolNode,~A");
c$.isDiaxial = $_M(c$, "isDiaxial", 
function (atomA, atomB, atom1, atom2, v, f) {
v.vA.setT (atomA);
v.vB.setT (atomB);
v.vA.sub (atom1);
v.vB.sub (atom2);
v.vA.normalize ();
v.vB.normalize ();
return (v.vA.dot (v.vB) < f);
}, "J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.smiles.SmilesSearch.VTemp,~N");
c$.getHandedness = $_M(c$, "getHandedness", 
($fz = function (a, b, c, pt, v) {
var d = J.smiles.SmilesAromatic.getNormalThroughPoints (a, b, c, v.vTemp, v.vA, v.vB);
return (J.smiles.SmilesSearch.distanceToPlane (v.vTemp, d, pt) > 0 ? 1 : 2);
}, $fz.isPrivate = true, $fz), "J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.smiles.SmilesSearch.VTemp");
c$.getPlaneNormals = $_M(c$, "getPlaneNormals", 
($fz = function (atom1, atom2, atom3, atom4, v) {
J.smiles.SmilesAromatic.getNormalThroughPoints (atom1, atom2, atom3, v.vNorm1, v.vTemp1, v.vTemp2);
J.smiles.SmilesAromatic.getNormalThroughPoints (atom2, atom3, atom4, v.vNorm2, v.vTemp1, v.vTemp2);
J.smiles.SmilesAromatic.getNormalThroughPoints (atom3, atom4, atom1, v.vNorm3, v.vTemp1, v.vTemp2);
}, $fz.isPrivate = true, $fz), "J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.util.JmolNode,J.smiles.SmilesSearch.VTemp");
c$.distanceToPlane = $_M(c$, "distanceToPlane", 
function (norm, w, pt) {
return (norm == null ? NaN : (norm.x * pt.x + norm.y * pt.y + norm.z * pt.z + w) / Math.sqrt (norm.x * norm.x + norm.y * norm.y + norm.z * norm.z));
}, "J.util.V3,~N,J.util.P3");
$_M(c$, "createTopoMap", 
function (bsAromatic) {
if (bsAromatic == null) bsAromatic =  new J.util.BS ();
var nAtomsMissing = this.getMissingHydrogenCount ();
var atoms =  new Array (this.atomCount + nAtomsMissing);
this.jmolAtoms = atoms;
var ptAtom = 0;
var bsFixH =  new J.util.BS ();
for (var i = 0; i < this.atomCount; i++) {
var sAtom = this.patternAtoms[i];
var cclass = sAtom.getChiralClass ();
var n = sAtom.missingHydrogenCount;
if (n < 0) n = 0;
var atom = atoms[ptAtom] =  new J.smiles.SmilesAtom ().setAll (0, ptAtom, cclass == -2147483648 ? cclass : (cclass << 8) + sAtom.getChiralOrder (), sAtom.elementNumber, sAtom.getCharge ());
atom.atomName = sAtom.atomName;
atom.residueName = sAtom.residueName;
atom.residueChar = sAtom.residueChar;
atom.isBioAtom = sAtom.isBioAtom;
atom.$isLeadAtom = sAtom.$isLeadAtom;
atom.setAtomicMass (sAtom.getAtomicMass ());
if (sAtom.isAromatic ()) bsAromatic.set (ptAtom);
if (!sAtom.isFirst && n == 1 && cclass > 0) bsFixH.set (ptAtom);
sAtom.setMatchingAtom (ptAtom++);
var bonds =  new Array (sAtom.getBondCount () + n);
atom.setBonds (bonds);
while (--n >= 0) {
var atomH = atoms[ptAtom] =  new J.smiles.SmilesAtom ().setAll (0, ptAtom, 0, 1, 0);
ptAtom++;
atomH.setBonds ( new Array (1));
var b =  new J.smiles.SmilesBond (atom, atomH, 1, false);
J.util.Logger.info ("" + b);
}
}
for (var i = 0; i < this.atomCount; i++) {
var sAtom = this.patternAtoms[i];
var i1 = sAtom.getMatchingAtom ();
var atom1 = atoms[i1];
var n = sAtom.getBondCount ();
for (var j = 0; j < n; j++) {
var sBond = sAtom.getBond (j);
var firstAtom = (sBond.getAtom1 () === sAtom);
if (firstAtom) {
var order = 1;
switch (sBond.order) {
case 769:
order = 33;
break;
case 1025:
order = 97;
break;
case 257:
order = 1025;
break;
case 513:
order = 1041;
break;
case 112:
case 96:
order = sBond.order;
break;
case 1:
order = 1;
break;
case 17:
order = 514;
break;
case 2:
order = 2;
break;
case 3:
order = 3;
break;
}
var atom2 = atoms[sBond.getAtom2 ().getMatchingAtom ()];
var b =  new J.smiles.SmilesBond (atom1, atom2, order, false);
atom2.bondCount--;
J.util.Logger.info ("" + b);
} else {
var atom2 = atoms[sBond.getAtom1 ().getMatchingAtom ()];
var b = atom2.getBondTo (atom1);
atom1.addBond (b);
}}
}
for (var i = bsFixH.nextSetBit (0); i >= 0; i = bsFixH.nextSetBit (i + 1)) {
var bonds = atoms[i].getEdges ();
var b = bonds[0];
bonds[0] = bonds[1];
bonds[1] = b;
}
}, "J.util.BS");
$_M(c$, "setTop", 
function (parent) {
if (parent == null) this.top = this;
 else this.top = parent.getTop ();
}, "J.smiles.SmilesSearch");
$_M(c$, "getTop", 
function () {
return (this.top === this ? this : this.top.getTop ());
});
$_M(c$, "getSelections", 
function () {
var ht = this.top.htNested;
if (ht == null || this.jmolAtoms.length == 0) return;
var htNew =  new java.util.Hashtable ();
for (var entry, $entry = ht.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var key = entry.getValue ().toString ();
if (key.startsWith ("select")) {
var bs = (htNew.containsKey (key) ? htNew.get (key) : this.jmolAtoms[0].findAtomsLike (key.substring (6)));
if (bs == null) bs =  new J.util.BS ();
htNew.put (key, bs);
entry.setValue (bs);
}}
});
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
this.vTemp = null;
this.vA = null;
this.vB = null;
this.vTemp1 = null;
this.vTemp2 = null;
this.vNorm1 = null;
this.vNorm2 = null;
this.vNorm3 = null;
this.bsTemp = null;
Clazz.instantialize (this, arguments);
}, J.smiles.SmilesSearch, "VTemp");
Clazz.prepareFields (c$, function () {
this.vTemp =  new J.util.V3 ();
this.vA =  new J.util.V3 ();
this.vB =  new J.util.V3 ();
this.vTemp1 =  new J.util.V3 ();
this.vTemp2 =  new J.util.V3 ();
this.vNorm1 =  new J.util.V3 ();
this.vNorm2 =  new J.util.V3 ();
this.vNorm3 =  new J.util.V3 ();
this.bsTemp =  new J.util.BS ();
});
c$ = Clazz.p0p ();
Clazz.defineStatics (c$,
"INITIAL_ATOMS", 16);
});
