Clazz.declarePackage ("J.smiles");
Clazz.load (["java.util.Hashtable", "J.smiles.SmilesSearch", "J.util.BS"], "J.smiles.SmilesGenerator", ["J.smiles.InvalidSmilesException", "$.SmilesAromatic", "$.SmilesAtom", "$.SmilesBond", "$.SmilesParser", "J.util.BSUtil", "$.Elements", "$.JmolList", "$.JmolMolecule", "$.Logger", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atoms = null;
this.atomCount = 0;
this.bsSelected = null;
this.bsAromatic = null;
this.ringSets = null;
this.vTemp = null;
this.nPairs = 0;
this.bsBondsUp = null;
this.bsBondsDn = null;
this.bsToDo = null;
this.prevAtom = null;
this.prevSp2Atoms = null;
this.htRingsSequence = null;
this.htRings = null;
this.bsIncludingH = null;
Clazz.instantialize (this, arguments);
}, J.smiles, "SmilesGenerator");
Clazz.prepareFields (c$, function () {
this.vTemp =  new J.smiles.SmilesSearch.VTemp ();
this.bsBondsUp =  new J.util.BS ();
this.bsBondsDn =  new J.util.BS ();
this.htRingsSequence =  new java.util.Hashtable ();
this.htRings =  new java.util.Hashtable ();
});
$_M(c$, "getSmiles", 
function (atoms, atomCount, bsSelected) {
var i = bsSelected.nextSetBit (0);
if (i < 0) return "";
this.atoms = atoms;
this.atomCount = atomCount;
this.bsSelected = bsSelected = J.util.BSUtil.copy (bsSelected);
return this.getSmilesComponent (atoms[i], bsSelected, false);
}, "~A,~N,J.util.BS");
$_M(c$, "getBioSmiles", 
function (atoms, atomCount, bsSelected, allowUnmatchedRings, addCrossLinks, comment) {
this.atoms = atoms;
this.atomCount = atomCount;
var sb =  new J.util.SB ();
var bs = J.util.BSUtil.copy (bsSelected);
if (comment != null) sb.append ("//* Jmol bioSMILES ").append (comment.$replace ('*', '_')).append (" *//");
var end = "\n";
var bsIgnore =  new J.util.BS ();
var lastComponent = null;
var s;
var vLinks =  new J.util.JmolList ();
try {
var len = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var a = atoms[i];
var ch = a.getGroup1 ('?');
var bioStructureName = a.getBioStructureTypeName ();
var unknown = (ch.equals ("?"));
if (end != null) {
if (sb.length () > 0) sb.append (end);
end = null;
len = 0;
if (bioStructureName.length > 0) {
var id = a.getChainID ();
if (id != 0) {
s = "//* chain " + a.getChainIDStr () + " " + bioStructureName + " " + a.getResno () + " *// ";
len = s.length;
sb.append (s);
}sb.append ("~").appendC (bioStructureName.charAt (0)).append ("~");
len++;
} else {
s = this.getSmilesComponent (a, bs, true);
if (s.equals (lastComponent)) {
end = "";
} else {
lastComponent = s;
var groupName = a.getGroup3 (true);
if (groupName != null) sb.append ("//* ").append (groupName).append (" *//");
sb.append (s);
end = ".\n";
}continue;
}}if (len >= 75) {
sb.append ("\n  ");
len = 2;
}if (unknown) {
this.addBracketedBioName (sb, a, bioStructureName.length > 0 ? ".0" : null);
} else {
sb.append (ch);
}len++;
var i0 = a.getOffsetResidueAtom ("0", 0);
if (addCrossLinks) {
a.getCrossLinkLeadAtomIndexes (vLinks);
for (var j = 0; j < vLinks.size (); j++) {
sb.append (":");
s = this.getRingCache (i0, vLinks.get (j).intValue (), this.htRingsSequence);
sb.append (s);
len += 1 + s.length;
}
vLinks.clear ();
}a.getGroupBits (bsIgnore);
bs.andNot (bsIgnore);
var i2 = a.getOffsetResidueAtom ("0", 1);
if (i2 < 0 || !bs.get (i2)) {
sb.append (" //* ").appendI (a.getResno ()).append (" *//");
if (i2 < 0 && (i2 = bs.nextSetBit (i + 1)) < 0) break;
if (len > 0) end = ".\n";
}i = i2 - 1;
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println (e.toString ());
return "";
} else {
throw e;
}
}
if (!allowUnmatchedRings && !this.htRingsSequence.isEmpty ()) {
this.dumpRingKeys (sb, this.htRingsSequence);
throw  new J.smiles.InvalidSmilesException ("//* ?ring error? *//");
}s = sb.toString ();
if (s.endsWith (".\n")) s = s.substring (0, s.length - 2);
return s;
}, "~A,~N,J.util.BS,~B,~B,~S");
$_M(c$, "addBracketedBioName", 
($fz = function (sb, a, atomName) {
sb.append ("[");
if (atomName != null) {
var chain = a.getChainIDStr ();
sb.append (a.getGroup3 (false));
if (!atomName.equals (".0")) sb.append (atomName).append ("#").appendI (a.getElementNumber ());
sb.append ("//* ").appendI (a.getResno ());
if (chain.length > 0) sb.append (":").append (chain);
sb.append (" *//");
} else {
sb.append (J.util.Elements.elementNameFromNumber (a.getElementNumber ()));
}sb.append ("]");
}, $fz.isPrivate = true, $fz), "J.util.SB,J.util.JmolNode,~S");
$_M(c$, "getSmilesComponent", 
($fz = function (atom, bs, allowConnectionsToOutsideWorld) {
if (atom.getElementNumber () == 1 && atom.getEdges ().length > 0) atom = this.atoms[atom.getBondedAtomIndex (0)];
this.bsSelected = J.util.JmolMolecule.getBranchBitSet (this.atoms, atom.getIndex (), J.util.BSUtil.copy (bs), null, -1, true, false);
bs.andNot (this.bsSelected);
this.bsIncludingH = J.util.BSUtil.copy (this.bsSelected);
for (var j = this.bsSelected.nextSetBit (0); j >= 0; j = this.bsSelected.nextSetBit (j + 1)) {
var a = this.atoms[j];
if (a.getElementNumber () == 1 && a.getIsotopeNumber () == 0) this.bsSelected.clear (j);
}
if (this.bsSelected.cardinality () > 2) {
var search = null;
search = J.smiles.SmilesParser.getMolecule ("A[=&@]A", true);
search.jmolAtoms = this.atoms;
search.setSelected (this.bsSelected);
search.jmolAtomCount = this.atomCount;
search.ringDataMax = 7;
search.setRingData (null);
this.bsAromatic = search.bsAromatic;
this.ringSets = search.ringSets;
this.setBondDirections ();
} else {
this.bsAromatic =  new J.util.BS ();
}this.bsToDo = J.util.BSUtil.copy (this.bsSelected);
var sb =  new J.util.SB ();
for (var i = this.bsToDo.nextSetBit (0); i >= 0; i = this.bsToDo.nextSetBit (i + 1)) if (this.atoms[i].getCovalentBondCount () > 4) {
this.getSmiles (sb, this.atoms[i], allowConnectionsToOutsideWorld, false);
atom = null;
}
if (atom != null) while ((atom = this.getSmiles (sb, atom, allowConnectionsToOutsideWorld, true)) != null) {
}
while (this.bsToDo.cardinality () > 0 || !this.htRings.isEmpty ()) {
var e = this.htRings.values ().iterator ();
if (e.hasNext ()) {
atom = this.atoms[(e.next ()[1]).intValue ()];
if (!this.bsToDo.get (atom.getIndex ())) break;
} else {
atom = this.atoms[this.bsToDo.nextSetBit (0)];
}sb.append (".");
this.prevSp2Atoms = null;
this.prevAtom = null;
while ((atom = this.getSmiles (sb, atom, allowConnectionsToOutsideWorld, true)) != null) {
}
}
if (!this.htRings.isEmpty ()) {
this.dumpRingKeys (sb, this.htRings);
throw  new J.smiles.InvalidSmilesException ("//* ?ring error? *//\n" + sb);
}return sb.toString ();
}, $fz.isPrivate = true, $fz), "J.util.JmolNode,J.util.BS,~B");
$_M(c$, "getBondStereochemistry", 
($fz = function (bond, atomFrom) {
if (bond == null) return '\0';
var i = bond.index;
var isFirst = (atomFrom == null || bond.getAtomIndex1 () == atomFrom.getIndex ());
return (this.bsBondsUp.get (i) ? (isFirst ? '/' : '\\') : this.bsBondsDn.get (i) ? (isFirst ? '\\' : '/') : '\0');
}, $fz.isPrivate = true, $fz), "J.util.JmolEdge,J.util.JmolNode");
$_M(c$, "setBondDirections", 
($fz = function () {
var bsDone =  new J.util.BS ();
var edges =  Clazz.newArray (2, 3, null);
for (var i = this.bsSelected.nextSetBit (0); i >= 0; i = this.bsSelected.nextSetBit (i + 1)) {
var atom1 = this.atoms[i];
var bonds = atom1.getEdges ();
for (var k = 0; k < bonds.length; k++) {
var bond = bonds[k];
var index = bond.index;
if (bsDone.get (index)) continue;
var atom2 = bond.getOtherAtomNode (atom1);
if (bond.getCovalentOrder () != 2 || J.smiles.SmilesSearch.isRingBond (this.ringSets, i, atom2.getIndex ())) continue;
bsDone.set (index);
var b0 = null;
var a0 = null;
var i0 = 0;
var atom12 = [atom1, atom2];
if (J.util.Logger.debugging) J.util.Logger.debug (atom1 + " == " + atom2);
var edgeCount = 1;
for (var j = 0; j < 2 && edgeCount > 0 && edgeCount < 3; j++) {
edgeCount = 0;
var atomA = atom12[j];
var bb = atomA.getEdges ();
for (var b = 0; b < bb.length; b++) {
if (bb[b].getCovalentOrder () != 1) continue;
edges[j][edgeCount++] = bb[b];
if (this.getBondStereochemistry (bb[b], atomA) != '\0') {
b0 = bb[b];
i0 = j;
}}
}
if (edgeCount == 3 || edgeCount == 0) continue;
if (b0 == null) {
i0 = 0;
b0 = edges[i0][0];
this.bsBondsUp.set (b0.index);
}var c0 = this.getBondStereochemistry (b0, atom12[i0]);
a0 = b0.getOtherAtomNode (atom12[i0]);
if (a0 == null) continue;
for (var j = 0; j < 2; j++) for (var jj = 0; jj < 2; jj++) {
var b1 = edges[j][jj];
if (b1 == null || b1 === b0) continue;
var bi = b1.index;
var a1 = b1.getOtherAtomNode (atom12[j]);
if (a1 == null) continue;
var c1 = this.getBondStereochemistry (b1, atom12[j]);
var isOpposite = J.smiles.SmilesSearch.isDiaxial (atom12[i0], atom12[j], a0, a1, this.vTemp, 0);
if (c1 == '\0' || (c1 != c0) == isOpposite) {
var isUp = (c0 == '\\' && isOpposite || c0 == '/' && !isOpposite);
if (isUp == (b1.getAtomIndex1 () != a1.getIndex ())) this.bsBondsUp.set (bi);
 else this.bsBondsDn.set (bi);
} else {
J.util.Logger.error ("BOND STEREOCHEMISTRY ERROR");
}if (J.util.Logger.debugging) J.util.Logger.debug (this.getBondStereochemistry (b0, atom12[0]) + " " + a0.getIndex () + " " + a1.getIndex () + " " + this.getBondStereochemistry (b1, atom12[j]));
}

}
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "getSmiles", 
($fz = function (sb, atom, allowConnectionsToOutsideWorld, allowBranches) {
var atomIndex = atom.getIndex ();
if (!this.bsToDo.get (atomIndex)) return null;
this.bsToDo.clear (atomIndex);
var isExtension = (!this.bsSelected.get (atomIndex));
var prevIndex = (this.prevAtom == null ? -1 : this.prevAtom.getIndex ());
var isAromatic = this.bsAromatic.get (atomIndex);
var havePreviousSp2Atoms = (this.prevSp2Atoms != null);
var sp2Atoms = this.prevSp2Atoms;
var nSp2Atoms = 0;
var atomicNumber = atom.getElementNumber ();
var nH = 0;
var v =  new J.util.JmolList ();
var bond0 = null;
var bondPrev = null;
var bonds = atom.getEdges ();
var aH = null;
var stereoFlag = (isAromatic ? 10 : 0);
var stereo =  new Array (7);
if (J.util.Logger.debugging) J.util.Logger.debug (sb.toString ());
if (bonds != null) for (var i = bonds.length; --i >= 0; ) {
var bond = bonds[i];
if (!bond.isCovalent ()) continue;
var atom1 = bonds[i].getOtherAtomNode (atom);
var index1 = atom1.getIndex ();
if (index1 == prevIndex) {
bondPrev = bonds[i];
continue;
}var isH = (atom1.getElementNumber () == 1 && atom1.getIsotopeNumber () == 0);
if (!this.bsIncludingH.get (index1)) {
if (!isH && allowConnectionsToOutsideWorld && this.bsSelected.get (atomIndex)) this.bsToDo.set (index1);
 else continue;
}if (isH) {
aH = atom1;
nH++;
if (nH > 1) stereoFlag = 10;
} else {
v.addLast (bonds[i]);
}}
var strBond = null;
if (sp2Atoms == null) sp2Atoms =  new Array (5);
if (bondPrev != null) {
strBond = J.smiles.SmilesBond.getBondOrderString (bondPrev.getCovalentOrder ());
if (this.prevSp2Atoms == null) sp2Atoms[nSp2Atoms++] = this.prevAtom;
 else nSp2Atoms = 2;
}nSp2Atoms += nH;
var nMax = 0;
var bsBranches =  new J.util.BS ();
if (allowBranches) for (var i = 0; i < v.size (); i++) {
var bond = v.get (i);
var a = bond.getOtherAtomNode (atom);
var n = a.getCovalentBondCount () - a.getCovalentHydrogenCount ();
var order = bond.getCovalentOrder ();
if (order == 1 && n == 1 && i < v.size () - (bond0 == null ? 1 : 0)) {
bsBranches.set (bond.index);
} else if ((order > 1 || n > nMax) && !this.htRings.containsKey (J.smiles.SmilesGenerator.getRingKey (a.getIndex (), atomIndex))) {
nMax = (order > 1 ? 1000 + order : n);
bond0 = bond;
}}
var atomNext = (bond0 == null ? null : bond0.getOtherAtomNode (atom));
var orderNext = (bond0 == null ? 0 : bond0.getCovalentOrder ());
if (stereoFlag < 7 && bondPrev != null) {
if (bondPrev.getCovalentOrder () == 2 && orderNext == 2 && this.prevSp2Atoms != null && this.prevSp2Atoms[1] != null) {
stereo[stereoFlag++] = this.prevSp2Atoms[0];
stereo[stereoFlag++] = this.prevSp2Atoms[1];
} else {
stereo[stereoFlag++] = this.prevAtom;
}}if (stereoFlag < 7 && nH == 1) stereo[stereoFlag++] = aH;
var deferStereo = (orderNext == 1 && this.prevSp2Atoms == null);
var chBond = this.getBondStereochemistry (bondPrev, this.prevAtom);
var sMore =  new J.util.SB ();
for (var i = 0; i < v.size (); i++) {
var bond = v.get (i);
if (!bsBranches.get (bond.index)) continue;
var a = bond.getOtherAtomNode (atom);
var s2 =  new J.util.SB ();
s2.append ("(");
this.prevAtom = atom;
this.prevSp2Atoms = null;
var bond0t = bond0;
this.getSmiles (s2, a, allowConnectionsToOutsideWorld, allowBranches);
bond0 = bond0t;
s2.append (")");
if (sMore.indexOf (s2.toString ()) >= 0) stereoFlag = 10;
sMore.appendSB (s2);
v.remove (i--);
if (stereoFlag < 7) stereo[stereoFlag++] = a;
if (nSp2Atoms < 5) sp2Atoms[nSp2Atoms++] = a;
}
var index2 = (orderNext == 2 ? atomNext.getIndex () : -1);
if (nH > 1 || isAromatic || index2 < 0 || J.smiles.SmilesSearch.isRingBond (this.ringSets, atomIndex, index2)) {
nSp2Atoms = -1;
}if (nSp2Atoms < 0) sp2Atoms = null;
if (strBond != null || chBond != '\0') {
if (chBond != '\0') strBond = "" + chBond;
sb.append (strBond);
}var atat = null;
if (!allowBranches && (v.size () == 5 || v.size () == 6)) atat = this.sortInorganic (atom, v);
for (var i = 0; i < v.size (); i++) {
var bond = v.get (i);
if (bond === bond0) continue;
var a = bond.getOtherAtomNode (atom);
var s = this.getRingCache (atomIndex, a.getIndex (), this.htRings);
strBond = J.smiles.SmilesBond.getBondOrderString (bond.order);
if (!deferStereo) {
chBond = this.getBondStereochemistry (bond, atom);
if (chBond != '\0') strBond = "" + chBond;
}sMore.append (strBond);
sMore.append (s);
if (stereoFlag < 7) stereo[stereoFlag++] = a;
if (sp2Atoms != null && nSp2Atoms < 5) sp2Atoms[nSp2Atoms++] = a;
}
if (havePreviousSp2Atoms && stereoFlag == 2 && orderNext == 2 && atomNext.getCovalentBondCount () == 3) {
bonds = atomNext.getEdges ();
for (var k = 0; k < bonds.length; k++) {
if (bonds[k].isCovalent () && atomNext.getBondedAtomIndex (k) != atomIndex) stereo[stereoFlag++] = this.atoms[atomNext.getBondedAtomIndex (k)];
}
nSp2Atoms = 0;
} else if (atomNext != null && stereoFlag < 7) {
stereo[stereoFlag++] = atomNext;
}var valence = atom.getValence ();
var charge = atom.getFormalCharge ();
var isotope = atom.getIsotopeNumber ();
var atomName = atom.getAtomName ();
var groupType = atom.getBioStructureTypeName ();
if (J.util.Logger.debugging) sb.append ("\n//* " + atom + " *//\t");
if (isExtension && groupType.length != 0 && atomName.length != 0) this.addBracketedBioName (sb, atom, "." + atomName);
 else sb.append (J.smiles.SmilesAtom.getAtomLabel (atomicNumber, isotope, valence, charge, nH, isAromatic, atat != null ? atat : this.checkStereoPairs (atom, atomIndex, stereo, stereoFlag)));
sb.appendSB (sMore);
if (bond0 == null) return null;
if (orderNext == 2 && (nSp2Atoms == 1 || nSp2Atoms == 2)) {
if (sp2Atoms[0] == null) sp2Atoms[0] = atom;
if (sp2Atoms[1] == null) sp2Atoms[1] = atom;
} else {
sp2Atoms = null;
nSp2Atoms = 0;
}this.prevSp2Atoms = sp2Atoms;
this.prevAtom = atom;
return atomNext;
}, $fz.isPrivate = true, $fz), "J.util.SB,J.util.JmolNode,~B,~B");
$_M(c$, "sortInorganic", 
($fz = function (atom, v) {
var atomIndex = atom.getIndex ();
var n = v.size ();
var axialPairs =  new J.util.JmolList ();
var bonds =  new J.util.JmolList ();
var a1;
var a2;
var bond1;
var bond2;
var bsDone =  new J.util.BS ();
var pair0 = null;
var stereo =  new Array (6);
var isOK = true;
var s = "";
for (var i = 0; i < n; i++) {
bond1 = v.get (i);
stereo[0] = a1 = bond1.getOtherAtomNode (atom);
if (i == 0) s = this.addStereoCheck (atomIndex, stereo, 0, "");
 else if (isOK && this.addStereoCheck (atomIndex, stereo, 0, s) != null) isOK = false;
if (bsDone.get (i)) continue;
bsDone.set (i);
var isAxial = false;
for (var j = i + 1; j < n; j++) {
if (bsDone.get (j)) continue;
bond2 = v.get (j);
a2 = bond2.getOtherAtomNode (atom);
if (J.smiles.SmilesSearch.isDiaxial (atom, atom, a1, a2, this.vTemp, -0.95)) {
axialPairs.addLast ([bond1, bond2]);
isAxial = true;
bsDone.set (j);
break;
}}
if (!isAxial) bonds.addLast (bond1);
}
var nPairs = axialPairs.size ();
if (isOK || n == 6 && nPairs != 3 || n == 5 && nPairs == 0) return "";
pair0 = axialPairs.get (0);
bond1 = pair0[0];
stereo[0] = bond1.getOtherAtomNode (atom);
v.clear ();
v.addLast (bond1);
if (nPairs > 1) bonds.addLast (axialPairs.get (1)[0]);
if (nPairs == 3) bonds.addLast (axialPairs.get (2)[0]);
if (nPairs > 1) bonds.addLast (axialPairs.get (1)[1]);
if (nPairs == 3) bonds.addLast (axialPairs.get (2)[1]);
for (var i = 0; i < bonds.size (); i++) {
bond1 = bonds.get (i);
v.addLast (bond1);
stereo[i + 1] = bond1.getOtherAtomNode (atom);
}
v.addLast (pair0[1]);
return J.smiles.SmilesGenerator.getStereoFlag (atom, stereo, n, this.vTemp);
}, $fz.isPrivate = true, $fz), "J.util.JmolNode,J.util.JmolList");
$_M(c$, "checkStereoPairs", 
($fz = function (atom, atomIndex, stereo, stereoFlag) {
if (stereoFlag < 4) return "";
if (stereoFlag == 4 && (atom.getElementNumber ()) == 6) {
var s = "";
for (var i = 0; i < 4; i++) if ((s = this.addStereoCheck (atomIndex, stereo, i, s)) == null) {
stereoFlag = 10;
break;
}
}return (stereoFlag > 6 ? "" : J.smiles.SmilesGenerator.getStereoFlag (atom, stereo, stereoFlag, this.vTemp));
}, $fz.isPrivate = true, $fz), "J.util.JmolNode,~N,~A,~N");
c$.getStereoFlag = $_M(c$, "getStereoFlag", 
($fz = function (atom0, atoms, nAtoms, v) {
var atom1 = atoms[0];
var atom2 = atoms[1];
var atom3 = atoms[2];
var atom4 = atoms[3];
var atom5 = atoms[4];
var atom6 = atoms[5];
var chiralClass = 4;
switch (nAtoms) {
default:
case 5:
case 6:
return (J.smiles.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 1, atom1, atom2, atom3, atom4, atom5, atom6, v) ? "@" : "@@");
case 2:
case 4:
if (atom3 == null || atom4 == null) return "";
var d = J.smiles.SmilesAromatic.getNormalThroughPoints (atom1, atom2, atom3, v.vTemp, v.vA, v.vB);
if (Math.abs (J.smiles.SmilesSearch.distanceToPlane (v.vTemp, d, atom4)) < 0.2) {
chiralClass = 8;
if (J.smiles.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 1, atom1, atom2, atom3, atom4, atom5, atom6, v)) return "@SP1";
if (J.smiles.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 2, atom1, atom2, atom3, atom4, atom5, atom6, v)) return "@SP2";
if (J.smiles.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 3, atom1, atom2, atom3, atom4, atom5, atom6, v)) return "@SP3";
} else {
return (J.smiles.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 1, atom1, atom2, atom3, atom4, atom5, atom6, v) ? "@" : "@@");
}}
return "";
}, $fz.isPrivate = true, $fz), "J.util.JmolNode,~A,~N,J.smiles.SmilesSearch.VTemp");
$_M(c$, "addStereoCheck", 
($fz = function (atomIndex, stereo, i, s) {
var n = stereo[i].getAtomicAndIsotopeNumber ();
var nx = stereo[i].getCovalentBondCount ();
var nh = (n == 6 ? stereo[i].getCovalentHydrogenCount () : 0);
if (n == 6 ? nx != 4 || nh != 3 : nx > 1) return s;
var sa = ";" + n + "/" + nh + "/" + nx + ",";
if (s.indexOf (sa) >= 0) {
if (nh == 3) {
var ndt = 0;
for (var j = 0; j < nx && ndt < 3; j++) {
var ia = stereo[i].getBondedAtomIndex (j);
if (ia == atomIndex) continue;
ndt += this.atoms[ia].getAtomicAndIsotopeNumber ();
}
if (ndt > 3) return s;
}return null;
}return s + sa;
}, $fz.isPrivate = true, $fz), "~N,~A,~N,~S");
$_M(c$, "getRingCache", 
($fz = function (i0, i1, ht) {
var key = J.smiles.SmilesGenerator.getRingKey (i0, i1);
var o = ht.get (key);
var s = (o == null ? null : o[0]);
if (s == null) {
ht.put (key, [s = J.smiles.SmilesParser.getRingPointer (++this.nPairs), Integer.$valueOf (i1)]);
if (J.util.Logger.debugging) J.util.Logger.debug ("adding for " + i0 + " ring key " + this.nPairs + ": " + key);
} else {
ht.remove (key);
if (J.util.Logger.debugging) J.util.Logger.debug ("using ring key " + key);
}return s;
}, $fz.isPrivate = true, $fz), "~N,~N,java.util.Map");
$_M(c$, "dumpRingKeys", 
($fz = function (sb, ht) {
J.util.Logger.info (sb.toString () + "\n\n");
for (var key, $key = ht.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) J.util.Logger.info ("unmatched ring key: " + key);

}, $fz.isPrivate = true, $fz), "J.util.SB,java.util.Map");
c$.getRingKey = $_M(c$, "getRingKey", 
function (i0, i1) {
return Math.min (i0, i1) + "_" + Math.max (i0, i1);
}, "~N,~N");
});
