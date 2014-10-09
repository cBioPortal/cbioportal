Clazz.declarePackage ("JS");
Clazz.load (["java.util.Hashtable", "JU.BS", "JS.VTemp"], "JS.SmilesGenerator", ["JU.Lst", "$.SB", "JS.InvalidSmilesException", "$.SmilesAromatic", "$.SmilesAtom", "$.SmilesBond", "$.SmilesParser", "$.SmilesSearch", "JU.BNode", "$.BSUtil", "$.Elements", "$.JmolMolecule", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atoms = null;
this.ac = 0;
this.bsSelected = null;
this.bsAromatic = null;
this.explicitH = false;
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
}, JS, "SmilesGenerator");
Clazz.prepareFields (c$, function () {
this.vTemp =  new JS.VTemp ();
this.bsBondsUp =  new JU.BS ();
this.bsBondsDn =  new JU.BS ();
this.htRingsSequence =  new java.util.Hashtable ();
this.htRings =  new java.util.Hashtable ();
});
Clazz.defineMethod (c$, "getSmiles", 
function (atoms, ac, bsSelected, explicitH) {
var i = bsSelected.nextSetBit (0);
if (i < 0) return "";
this.atoms = atoms;
this.ac = ac;
this.bsSelected = bsSelected = JU.BSUtil.copy (bsSelected);
this.explicitH = explicitH;
return this.getSmilesComponent (atoms[i], bsSelected, false);
}, "~A,~N,JU.BS,~B");
Clazz.defineMethod (c$, "getBioSmiles", 
function (atoms, ac, bsSelected, allowUnmatchedRings, addCrossLinks, comment) {
this.atoms = atoms;
this.ac = ac;
var sb =  new JU.SB ();
var bs = JU.BSUtil.copy (bsSelected);
if (comment != null) sb.append ("//* Jmol bioSMILES ").append (comment.$replace ('*', '_')).append (" *//");
var end = "\n";
var bsIgnore =  new JU.BS ();
var lastComponent = null;
var s;
var vLinks =  new JU.Lst ();
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
throw  new JS.InvalidSmilesException ("//* error: " + e.getMessage () + " *//");
} else {
throw e;
}
}
if (!allowUnmatchedRings && !this.htRingsSequence.isEmpty ()) {
this.dumpRingKeys (sb, this.htRingsSequence);
throw  new JS.InvalidSmilesException ("//* ?ring error? *//");
}s = sb.toString ();
if (s.endsWith (".\n")) s = s.substring (0, s.length - 2);
return s;
}, "~A,~N,JU.BS,~B,~B,~S");
Clazz.defineMethod (c$, "addBracketedBioName", 
 function (sb, atom, atomName) {
sb.append ("[");
if (atomName != null && Clazz.instanceOf (atom, JU.BNode)) {
var a = atom;
var chain = a.getChainIDStr ();
sb.append (a.getGroup3 (false));
if (!atomName.equals (".0")) sb.append (atomName).append ("#").appendI (a.getElementNumber ());
sb.append ("//* ").appendI (a.getResno ());
if (chain.length > 0) sb.append (":").append (chain);
sb.append (" *//");
} else {
sb.append (JU.Elements.elementNameFromNumber (atom.getElementNumber ()));
}sb.append ("]");
}, "JU.SB,JU.Node,~S");
Clazz.defineMethod (c$, "getSmilesComponent", 
 function (atom, bs, allowConnectionsToOutsideWorld) {
if (!this.explicitH && atom.getElementNumber () == 1 && atom.getEdges ().length > 0) atom = this.atoms[atom.getBondedAtomIndex (0)];
this.bsSelected = JU.JmolMolecule.getBranchBitSet (this.atoms, atom.getIndex (), JU.BSUtil.copy (bs), null, -1, true, false);
bs.andNot (this.bsSelected);
this.bsIncludingH = JU.BSUtil.copy (this.bsSelected);
if (!this.explicitH) for (var j = this.bsSelected.nextSetBit (0); j >= 0; j = this.bsSelected.nextSetBit (j + 1)) {
var a = this.atoms[j];
if (a.getElementNumber () == 1 && a.getIsotopeNumber () == 0) this.bsSelected.clear (j);
}
if (this.bsSelected.cardinality () > 2) {
var search = null;
search = JS.SmilesParser.getMolecule ("A[=&@]A", true);
search.jmolAtoms = this.atoms;
if (Clazz.instanceOf (this.atoms, Array)) search.bioAtoms = this.atoms;
search.setSelected (this.bsSelected);
search.jmolAtomCount = this.ac;
search.ringDataMax = 7;
search.setRingData (null);
this.bsAromatic = search.bsAromatic;
this.ringSets = search.ringSets;
this.setBondDirections ();
} else {
this.bsAromatic =  new JU.BS ();
}this.bsToDo = JU.BSUtil.copy (this.bsSelected);
var sb =  new JU.SB ();
for (var i = this.bsToDo.nextSetBit (0); i >= 0; i = this.bsToDo.nextSetBit (i + 1)) if (this.atoms[i].getCovalentBondCount () > 4) {
this.getSmiles (sb, this.atoms[i], allowConnectionsToOutsideWorld, false, this.explicitH);
atom = null;
}
if (atom != null) while ((atom = this.getSmiles (sb, atom, allowConnectionsToOutsideWorld, true, this.explicitH)) != null) {
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
while ((atom = this.getSmiles (sb, atom, allowConnectionsToOutsideWorld, true, this.explicitH)) != null) {
}
}
if (!this.htRings.isEmpty ()) {
this.dumpRingKeys (sb, this.htRings);
throw  new JS.InvalidSmilesException ("//* ?ring error? *//\n" + sb);
}return sb.toString ();
}, "JU.Node,JU.BS,~B");
Clazz.defineMethod (c$, "getBondStereochemistry", 
 function (bond, atomFrom) {
if (bond == null) return '\0';
var i = bond.index;
var isFirst = (atomFrom == null || bond.getAtomIndex1 () == atomFrom.getIndex ());
return (this.bsBondsUp.get (i) ? (isFirst ? '/' : '\\') : this.bsBondsDn.get (i) ? (isFirst ? '\\' : '/') : '\0');
}, "JU.Edge,JU.Node");
Clazz.defineMethod (c$, "setBondDirections", 
 function () {
var bsDone =  new JU.BS ();
var edges =  Clazz.newArray (2, 3, null);
for (var i = this.bsSelected.nextSetBit (0); i >= 0; i = this.bsSelected.nextSetBit (i + 1)) {
var atom1 = this.atoms[i];
var bonds = atom1.getEdges ();
for (var k = 0; k < bonds.length; k++) {
var bond = bonds[k];
var index = bond.index;
if (bsDone.get (index)) continue;
var atom2 = bond.getOtherAtomNode (atom1);
if (bond.getCovalentOrder () != 2 || JS.SmilesSearch.isRingBond (this.ringSets, i, atom2.getIndex ())) continue;
bsDone.set (index);
var b0 = null;
var a0 = null;
var i0 = 0;
var atom12 = [atom1, atom2];
if (JU.Logger.debugging) JU.Logger.debug (atom1 + " == " + atom2);
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
var isOpposite = JS.SmilesSearch.isDiaxial (atom12[i0], atom12[j], a0, a1, this.vTemp, 0);
if (c1 == '\0' || (c1 != c0) == isOpposite) {
var isUp = (c0 == '\\' && isOpposite || c0 == '/' && !isOpposite);
if (isUp == (b1.getAtomIndex1 () != a1.getIndex ())) this.bsBondsUp.set (bi);
 else this.bsBondsDn.set (bi);
} else {
JU.Logger.error ("BOND STEREOCHEMISTRY ERROR");
}if (JU.Logger.debugging) JU.Logger.debug (this.getBondStereochemistry (b0, atom12[0]) + " " + a0.getIndex () + " " + a1.getIndex () + " " + this.getBondStereochemistry (b1, atom12[j]));
}

}
}
});
Clazz.defineMethod (c$, "getSmiles", 
 function (sb, atom, allowConnectionsToOutsideWorld, allowBranches, explicitH) {
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
var v =  new JU.Lst ();
var bond0 = null;
var bondPrev = null;
var bonds = atom.getEdges ();
var aH = null;
var stereoFlag = (isAromatic ? 10 : 0);
var stereo =  new Array (7);
if (JU.Logger.debugging) JU.Logger.debug (sb.toString ());
if (bonds != null) for (var i = bonds.length; --i >= 0; ) {
var bond = bonds[i];
if (!bond.isCovalent ()) continue;
var atom1 = bonds[i].getOtherAtomNode (atom);
var index1 = atom1.getIndex ();
if (index1 == prevIndex) {
bondPrev = bonds[i];
continue;
}var isH = !explicitH && (atom1.getElementNumber () == 1 && atom1.getIsotopeNumber () == 0);
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
strBond = JS.SmilesBond.getBondOrderString (bondPrev.getCovalentOrder ());
if (this.prevSp2Atoms == null) sp2Atoms[nSp2Atoms++] = this.prevAtom;
 else nSp2Atoms = 2;
}nSp2Atoms += nH;
var nMax = 0;
var bsBranches =  new JU.BS ();
if (allowBranches) for (var i = 0; i < v.size (); i++) {
var bond = v.get (i);
var a = bond.getOtherAtomNode (atom);
var n = a.getCovalentBondCount () - (explicitH ? 0 : a.getCovalentHydrogenCount ());
var order = bond.getCovalentOrder ();
if (order == 1 && n == 1 && i < v.size () - (bond0 == null ? 1 : 0)) {
bsBranches.set (bond.index);
} else if ((order > 1 || n > nMax) && !this.htRings.containsKey (JS.SmilesGenerator.getRingKey (a.getIndex (), atomIndex))) {
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
var sMore =  new JU.SB ();
for (var i = 0; i < v.size (); i++) {
var bond = v.get (i);
if (!bsBranches.get (bond.index)) continue;
var a = bond.getOtherAtomNode (atom);
var s2 =  new JU.SB ();
s2.append ("(");
this.prevAtom = atom;
this.prevSp2Atoms = null;
var bond0t = bond0;
this.getSmiles (s2, a, allowConnectionsToOutsideWorld, allowBranches, explicitH);
bond0 = bond0t;
s2.append (")");
if (sMore.indexOf (s2.toString ()) >= 0) stereoFlag = 10;
sMore.appendSB (s2);
v.remove (i--);
if (stereoFlag < 7) stereo[stereoFlag++] = a;
if (nSp2Atoms < 5) sp2Atoms[nSp2Atoms++] = a;
}
var index2 = (orderNext == 2 ? atomNext.getIndex () : -1);
if (nH > 1 || isAromatic || index2 < 0 || JS.SmilesSearch.isRingBond (this.ringSets, atomIndex, index2)) {
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
strBond = JS.SmilesBond.getBondOrderString (bond.order);
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
}var charge = atom.getFormalCharge ();
var isotope = atom.getIsotopeNumber ();
var valence = atom.getValence ();
var atomName = atom.getAtomName ();
var groupType = (Clazz.instanceOf (atom, JU.BNode) ? (atom).getBioStructureTypeName () : "");
if (JU.Logger.debugging) sb.append ("\n//* " + atom + " *//\t");
if (isExtension && groupType.length != 0 && atomName.length != 0) this.addBracketedBioName (sb, atom, "." + atomName);
 else sb.append (JS.SmilesAtom.getAtomLabel (atomicNumber, isotope, valence, charge, nH, isAromatic, atat != null ? atat : this.checkStereoPairs (atom, atomIndex, stereo, stereoFlag)));
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
}, "JU.SB,JU.Node,~B,~B,~B");
Clazz.defineMethod (c$, "sortInorganic", 
 function (atom, v) {
var atomIndex = atom.getIndex ();
var n = v.size ();
var axialPairs =  new JU.Lst ();
var bonds =  new JU.Lst ();
var a1;
var a2;
var bond1;
var bond2;
var bsDone =  new JU.BS ();
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
if (JS.SmilesSearch.isDiaxial (atom, atom, a1, a2, this.vTemp, -0.95)) {
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
return JS.SmilesGenerator.getStereoFlag (atom, stereo, n, this.vTemp);
}, "JU.Node,JU.Lst");
Clazz.defineMethod (c$, "checkStereoPairs", 
 function (atom, atomIndex, stereo, stereoFlag) {
if (stereoFlag < 4) return "";
if (stereoFlag == 4 && (atom.getElementNumber ()) == 6) {
var s = "";
for (var i = 0; i < 4; i++) if ((s = this.addStereoCheck (atomIndex, stereo, i, s)) == null) {
stereoFlag = 10;
break;
}
}return (stereoFlag > 6 ? "" : JS.SmilesGenerator.getStereoFlag (atom, stereo, stereoFlag, this.vTemp));
}, "JU.Node,~N,~A,~N");
c$.getStereoFlag = Clazz.defineMethod (c$, "getStereoFlag", 
 function (atom0, atoms, nAtoms, v) {
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
return (JS.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 1, atom1, atom2, atom3, atom4, atom5, atom6, v) ? "@" : "@@");
case 2:
case 4:
if (atom3 == null || atom4 == null) return "";
var d = JS.SmilesAromatic.getNormalThroughPoints (atom1, atom2, atom3, v.vTemp, v.vA, v.vB);
if (Math.abs (JS.SmilesSearch.distanceToPlane (v.vTemp, d, atom4)) < 0.2) {
chiralClass = 8;
if (JS.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 1, atom1, atom2, atom3, atom4, atom5, atom6, v)) return "@SP1";
if (JS.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 2, atom1, atom2, atom3, atom4, atom5, atom6, v)) return "@SP2";
if (JS.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 3, atom1, atom2, atom3, atom4, atom5, atom6, v)) return "@SP3";
} else {
return (JS.SmilesSearch.checkStereochemistryAll (false, atom0, chiralClass, 1, atom1, atom2, atom3, atom4, atom5, atom6, v) ? "@" : "@@");
}}
return "";
}, "JU.Node,~A,~N,JS.VTemp");
Clazz.defineMethod (c$, "addStereoCheck", 
 function (atomIndex, stereo, i, s) {
var n = stereo[i].getAtomicAndIsotopeNumber ();
var nx = stereo[i].getCovalentBondCount ();
var nh = (n == 6 && !this.explicitH ? stereo[i].getCovalentHydrogenCount () : 0);
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
}, "~N,~A,~N,~S");
Clazz.defineMethod (c$, "getRingCache", 
 function (i0, i1, ht) {
var key = JS.SmilesGenerator.getRingKey (i0, i1);
var o = ht.get (key);
var s = (o == null ? null : o[0]);
if (s == null) {
ht.put (key, [s = JS.SmilesParser.getRingPointer (++this.nPairs), Integer.$valueOf (i1)]);
if (JU.Logger.debugging) JU.Logger.debug ("adding for " + i0 + " ring key " + this.nPairs + ": " + key);
} else {
ht.remove (key);
if (JU.Logger.debugging) JU.Logger.debug ("using ring key " + key);
}return s;
}, "~N,~N,java.util.Map");
Clazz.defineMethod (c$, "dumpRingKeys", 
 function (sb, ht) {
JU.Logger.info (sb.toString () + "\n\n");
for (var key, $key = ht.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) JU.Logger.info ("unmatched ring key: " + key);

}, "JU.SB,java.util.Map");
c$.getRingKey = Clazz.defineMethod (c$, "getRingKey", 
function (i0, i1) {
return Math.min (i0, i1) + "_" + Math.max (i0, i1);
}, "~N,~N");
});
