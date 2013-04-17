Clazz.declarePackage ("J.util");
Clazz.load (["J.util.Elements"], "J.util.JmolMolecule", ["J.util.ArrayUtil", "$.BS", "$.BSUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.nodes = null;
this.moleculeIndex = 0;
this.modelIndex = 0;
this.indexInModel = 0;
this.firstAtomIndex = 0;
this.atomCount = 0;
this.nElements = 0;
this.elementCounts = null;
this.altElementCounts = null;
this.elementNumberMax = 0;
this.altElementMax = 0;
this.mf = null;
this.atomList = null;
Clazz.instantialize (this, arguments);
}, J.util, "JmolMolecule");
Clazz.prepareFields (c$, function () {
this.elementCounts =  Clazz.newIntArray (J.util.Elements.elementNumberMax, 0);
this.altElementCounts =  Clazz.newIntArray (J.util.Elements.altElementMax, 0);
});
Clazz.makeConstructor (c$, 
function () {
});
c$.getMolecules = $_M(c$, "getMolecules", 
function (atoms, bsModelAtoms, biobranches, bsExclude) {
var bsToTest = null;
var bsBranch =  new J.util.BS ();
var thisModelIndex = -1;
var indexInModel = 0;
var moleculeCount = 0;
var molecules =  new Array (4);
if (bsExclude == null) bsExclude =  new J.util.BS ();
for (var i = 0; i < atoms.length; i++) if (!bsExclude.get (i) && !bsBranch.get (i)) {
if (atoms[i].isDeleted ()) {
bsExclude.set (i);
continue;
}var modelIndex = atoms[i].getModelIndex ();
if (modelIndex != thisModelIndex) {
thisModelIndex = modelIndex;
indexInModel = 0;
bsToTest = bsModelAtoms[modelIndex];
}bsBranch = J.util.JmolMolecule.getBranchBitSet (atoms, i, bsToTest, biobranches, -1, true, true);
if (bsBranch.nextSetBit (0) >= 0) {
molecules = J.util.JmolMolecule.addMolecule (molecules, moleculeCount++, atoms, i, bsBranch, modelIndex, indexInModel++, bsExclude);
}}
return J.util.JmolMolecule.allocateArray (molecules, moleculeCount);
}, "~A,~A,J.util.JmolList,J.util.BS");
c$.getBranchBitSet = $_M(c$, "getBranchBitSet", 
function (atoms, atomIndex, bsToTest, biobranches, atomIndexNot, allowCyclic, allowBioResidue) {
var bs = J.util.BSUtil.newBitSet (atoms.length);
if (atomIndex < 0) return bs;
if (atomIndexNot >= 0) bsToTest.clear (atomIndexNot);
return (J.util.JmolMolecule.getCovalentlyConnectedBitSet (atoms, atoms[atomIndex], bsToTest, allowCyclic, allowBioResidue, biobranches, bs) ? bs :  new J.util.BS ());
}, "~A,~N,J.util.BS,J.util.JmolList,~N,~B,~B");
c$.addMolecule = $_M(c$, "addMolecule", 
function (molecules, iMolecule, atoms, iAtom, bsBranch, modelIndex, indexInModel, bsExclude) {
bsExclude.or (bsBranch);
if (iMolecule == molecules.length) molecules = J.util.JmolMolecule.allocateArray (molecules, iMolecule * 2 + 1);
molecules[iMolecule] = J.util.JmolMolecule.initialize (atoms, iMolecule, iAtom, bsBranch, modelIndex, indexInModel);
return molecules;
}, "~A,~N,~A,~N,J.util.BS,~N,~N,J.util.BS");
c$.getMolecularFormula = $_M(c$, "getMolecularFormula", 
function (atoms, bsSelected, includeMissingHydrogens) {
var m =  new J.util.JmolMolecule ();
m.nodes = atoms;
m.atomList = bsSelected;
return m.getMolecularFormula (includeMissingHydrogens);
}, "~A,J.util.BS,~B");
$_M(c$, "getMolecularFormula", 
function (includeMissingHydrogens) {
if (this.mf != null) return this.mf;
this.getElementAndAtomCount (includeMissingHydrogens);
var mf = "";
var sep = "";
var nX;
for (var i = 1; i <= this.elementNumberMax; i++) {
nX = this.elementCounts[i];
if (nX != 0) {
mf += sep + J.util.Elements.elementSymbolFromNumber (i) + " " + nX;
sep = " ";
}}
for (var i = 1; i <= this.altElementMax; i++) {
nX = this.altElementCounts[i];
if (nX != 0) {
mf += sep + J.util.Elements.elementSymbolFromNumber (J.util.Elements.altElementNumberFromIndex (i)) + " " + nX;
sep = " ";
}}
return mf;
}, "~B");
c$.initialize = $_M(c$, "initialize", 
($fz = function (nodes, moleculeIndex, firstAtomIndex, atomList, modelIndex, indexInModel) {
var jm =  new J.util.JmolMolecule ();
jm.nodes = nodes;
jm.firstAtomIndex = firstAtomIndex;
jm.atomList = atomList;
jm.atomCount = atomList.cardinality ();
jm.moleculeIndex = moleculeIndex;
jm.modelIndex = modelIndex;
jm.indexInModel = indexInModel;
return jm;
}, $fz.isPrivate = true, $fz), "~A,~N,~N,J.util.BS,~N,~N");
$_M(c$, "getElementAndAtomCount", 
($fz = function (includeMissingHydrogens) {
if (this.atomList == null) {
this.atomList =  new J.util.BS ();
this.atomList.setBits (0, this.nodes.length);
}this.elementCounts =  Clazz.newIntArray (J.util.Elements.elementNumberMax, 0);
this.altElementCounts =  Clazz.newIntArray (J.util.Elements.altElementMax, 0);
this.atomCount = this.atomList.cardinality ();
for (var i = this.atomList.nextSetBit (0); i >= 0; i = this.atomList.nextSetBit (i + 1)) {
var n = this.nodes[i].getAtomicAndIsotopeNumber ();
if (n < J.util.Elements.elementNumberMax) {
this.elementCounts[n]++;
if (this.elementCounts[n] == 1) this.nElements++;
this.elementNumberMax = Math.max (this.elementNumberMax, n);
if (includeMissingHydrogens) {
var nH = this.nodes[i].getImplicitHydrogenCount ();
if (nH > 0) {
if (this.elementCounts[1] == 0) this.nElements++;
this.elementCounts[1] += nH;
}}} else {
n = J.util.Elements.altElementIndexFromNumber (n);
this.altElementCounts[n]++;
if (this.altElementCounts[n] == 1) this.nElements++;
this.altElementMax = Math.max (this.altElementMax, n);
}}
}, $fz.isPrivate = true, $fz), "~B");
c$.getCovalentlyConnectedBitSet = $_M(c$, "getCovalentlyConnectedBitSet", 
($fz = function (atoms, atom, bsToTest, allowCyclic, allowBioResidue, biobranches, bsResult) {
var atomIndex = atom.getIndex ();
if (!bsToTest.get (atomIndex)) return allowCyclic;
if (!allowBioResidue && atom.getBioStructureTypeName ().length > 0) return allowCyclic;
bsToTest.clear (atomIndex);
if (biobranches != null && !bsResult.get (atomIndex)) {
for (var i = biobranches.size (); --i >= 0; ) {
var b = biobranches.get (i);
if (b.get (atomIndex)) {
bsResult.or (b);
bsToTest.andNot (b);
for (var j = b.nextSetBit (0); j >= 0; j = b.nextSetBit (j + 1)) {
var atom1 = atoms[j];
bsToTest.set (j);
J.util.JmolMolecule.getCovalentlyConnectedBitSet (atoms, atom1, bsToTest, allowCyclic, allowBioResidue, biobranches, bsResult);
bsToTest.clear (j);
}
break;
}}
}bsResult.set (atomIndex);
var bonds = atom.getEdges ();
if (bonds == null) return true;
for (var i = bonds.length; --i >= 0; ) {
var bond = bonds[i];
if (bond.isCovalent () && !J.util.JmolMolecule.getCovalentlyConnectedBitSet (atoms, bond.getOtherAtomNode (atom), bsToTest, allowCyclic, allowBioResidue, biobranches, bsResult)) return false;
}
return true;
}, $fz.isPrivate = true, $fz), "~A,J.util.JmolNode,J.util.BS,~B,~B,J.util.JmolList,J.util.BS");
c$.allocateArray = $_M(c$, "allocateArray", 
($fz = function (molecules, len) {
return (len == molecules.length ? molecules : J.util.ArrayUtil.arrayCopyObject (molecules, len));
}, $fz.isPrivate = true, $fz), "~A,~N");
});
