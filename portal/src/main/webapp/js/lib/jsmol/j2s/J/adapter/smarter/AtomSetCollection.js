Clazz.declarePackage ("J.adapter.smarter");
Clazz.load (["java.util.Hashtable", "J.util.P3"], "J.adapter.smarter.AtomSetCollection", ["java.lang.Boolean", "$.Float", "java.util.Collections", "$.Properties", "J.adapter.smarter.Atom", "$.Bond", "$.SmarterJmolAdapter", "J.api.Interface", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.Escape", "$.JmolList", "$.Logger", "$.Matrix3f", "$.Matrix4f", "$.P3i", "$.Parser", "$.SB", "$.Tensor", "$.TextFormat", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bsAtoms = null;
this.fileTypeName = null;
this.collectionName = null;
this.atomSetCollectionAuxiliaryInfo = null;
this.atomCount = 0;
this.atoms = null;
this.bondCount = 0;
this.bonds = null;
this.structureCount = 0;
this.structures = null;
this.atomSetCount = 0;
this.currentAtomSetIndex = -1;
this.atomSetNumbers = null;
this.atomSetAtomIndexes = null;
this.atomSetAtomCounts = null;
this.atomSetBondCounts = null;
this.atomSetAuxiliaryInfo = null;
this.latticeCells = null;
this.errorMessage = null;
this.coordinatesAreFractional = false;
this.isTrajectory = false;
this.trajectoryStepCount = 0;
this.trajectorySteps = null;
this.vibrationSteps = null;
this.trajectoryNames = null;
this.doFixPeriodic = false;
this.notionalUnitCell = null;
this.allowMultiple = false;
this.reader = null;
this.readerList = null;
this.vConnect = null;
this.connectNextAtomIndex = 0;
this.connectNextAtomSet = 0;
this.connectLast = null;
this.bsStructuredModels = null;
this.symmetryRange = 0;
this.doCentroidUnitCell = false;
this.centroidPacked = false;
this.ptSupercell = null;
this.fmatSupercell = null;
this.symmetry = null;
this.haveUnitCell = false;
this.doNormalize = true;
this.doPackUnitCell = false;
this.rminx = 0;
this.rminy = 0;
this.rminz = 0;
this.rmaxx = 0;
this.rmaxy = 0;
this.rmaxz = 0;
this.ptOffset = null;
this.unitCellOffset = null;
this.minXYZ = null;
this.maxXYZ = null;
this.minXYZ0 = null;
this.maxXYZ0 = null;
this.haveAnisou = false;
this.dtype = 3;
this.unitCellTranslations = null;
this.baseSymmetryAtomCount = 0;
this.checkLatticeOnly = false;
this.latticeOp = 0;
this.latticeOnly = false;
this.bondCount0 = 0;
this.bondIndex0 = 0;
this.applySymmetryToBonds = false;
this.checkSpecial = true;
this.checkAll = false;
this.ptTemp = null;
this.mTemp = null;
this.atomSymbolicMap = null;
this.haveMappedSerials = false;
Clazz.instantialize (this, arguments);
}, J.adapter.smarter, "AtomSetCollection");
Clazz.prepareFields (c$, function () {
this.atomSetCollectionAuxiliaryInfo =  new java.util.Hashtable ();
this.atoms =  new Array (256);
this.bonds =  new Array (256);
this.structures =  new Array (16);
this.atomSetNumbers =  Clazz.newIntArray (16, 0);
this.atomSetAtomIndexes =  Clazz.newIntArray (16, 0);
this.atomSetAtomCounts =  Clazz.newIntArray (16, 0);
this.atomSetBondCounts =  Clazz.newIntArray (16, 0);
this.atomSetAuxiliaryInfo =  new Array (16);
this.notionalUnitCell =  Clazz.newFloatArray (6, 0);
this.ptOffset =  new J.util.P3 ();
this.atomSymbolicMap =  new java.util.Hashtable ();
});
$_M(c$, "getFileTypeName", 
function () {
return this.fileTypeName;
});
$_M(c$, "getCollectionName", 
function () {
return this.collectionName;
});
$_M(c$, "setCollectionName", 
function (collectionName) {
if (collectionName != null) {
collectionName = collectionName.trim ();
if (collectionName.length == 0) return;
this.collectionName = collectionName;
}}, "~S");
$_M(c$, "getAtomSetCollectionAuxiliaryInfoMap", 
function () {
return this.atomSetCollectionAuxiliaryInfo;
});
$_M(c$, "clearGlobalBoolean", 
function (globalIndex) {
this.atomSetCollectionAuxiliaryInfo.remove (J.adapter.smarter.AtomSetCollection.globalBooleans[globalIndex]);
}, "~N");
$_M(c$, "setGlobalBoolean", 
function (globalIndex) {
this.setAtomSetCollectionAuxiliaryInfo (J.adapter.smarter.AtomSetCollection.globalBooleans[globalIndex], Boolean.TRUE);
}, "~N");
$_M(c$, "getGlobalBoolean", 
function (globalIndex) {
return (this.getAtomSetCollectionAuxiliaryInfo (J.adapter.smarter.AtomSetCollection.globalBooleans[globalIndex]) === Boolean.TRUE);
}, "~N");
$_M(c$, "getAtomCount", 
function () {
return this.atomCount;
});
$_M(c$, "getHydrogenAtomCount", 
function () {
var n = 0;
for (var i = 0; i < this.atomCount; i++) if (this.atoms[i].elementNumber == 1 || this.atoms[i].elementSymbol.equals ("H")) n++;

return n;
});
$_M(c$, "getAtoms", 
function () {
return this.atoms;
});
$_M(c$, "getAtom", 
function (i) {
return this.atoms[i];
}, "~N");
$_M(c$, "getBondCount", 
function () {
return this.bondCount;
});
$_M(c$, "getBonds", 
function () {
return this.bonds;
});
$_M(c$, "getBond", 
function (i) {
return this.bonds[i];
}, "~N");
$_M(c$, "getStructureCount", 
function () {
return this.structureCount;
});
$_M(c$, "getStructures", 
function () {
return this.structures;
});
$_M(c$, "getAtomSetCount", 
function () {
return this.atomSetCount;
});
$_M(c$, "getCurrentAtomSetIndex", 
function () {
return this.currentAtomSetIndex;
});
$_M(c$, "setCurrentAtomSetIndex", 
function (i) {
this.currentAtomSetIndex = i;
}, "~N");
$_M(c$, "setDoFixPeriodic", 
function () {
this.doFixPeriodic = true;
});
Clazz.makeConstructor (c$, 
function (fileTypeName, reader, array, list) {
this.fileTypeName = fileTypeName;
this.reader = reader;
this.allowMultiple = (reader == null || reader.desiredVibrationNumber < 0);
var p =  new java.util.Properties ();
p.put ("PATH_KEY", ".PATH");
p.put ("PATH_SEPARATOR", J.adapter.smarter.SmarterJmolAdapter.PATH_SEPARATOR);
this.setAtomSetCollectionAuxiliaryInfo ("properties", p);
if (array != null) {
var n = 0;
this.readerList =  new J.util.JmolList ();
for (var i = 0; i < array.length; i++) if (array[i].atomCount > 0 || array[i].reader != null && array[i].reader.mustFinalizeModelSet) this.appendAtomSetCollection (n++, array[i]);

if (n > 1) this.setAtomSetCollectionAuxiliaryInfo ("isMultiFile", Boolean.TRUE);
} else if (list != null) {
this.setAtomSetCollectionAuxiliaryInfo ("isMultiFile", Boolean.TRUE);
this.appendAtomSetCollectionList (list);
}}, "~S,J.adapter.smarter.AtomSetCollectionReader,~A,J.util.JmolList");
$_M(c$, "appendAtomSetCollectionList", 
($fz = function (list) {
var n = list.size ();
if (n == 0) {
this.errorMessage = "No file found!";
return;
}for (var i = 0; i < n; i++) {
var o = list.get (i);
if (Clazz.instanceOf (o, J.util.JmolList)) this.appendAtomSetCollectionList (o);
 else this.appendAtomSetCollection (i, o);
}
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "setTrajectory", 
function () {
if (!this.isTrajectory) {
this.trajectorySteps =  new J.util.JmolList ();
}this.isTrajectory = true;
this.addTrajectoryStep ();
});
$_M(c$, "appendAtomSetCollection", 
function (collectionIndex, collection) {
if (collection.reader != null && collection.reader.mustFinalizeModelSet) this.readerList.addLast (collection.reader);
var existingAtomsCount = this.atomCount;
this.setAtomSetCollectionAuxiliaryInfo ("loadState", collection.getAtomSetCollectionAuxiliaryInfo ("loadState"));
if (collection.bsAtoms != null) {
if (this.bsAtoms == null) this.bsAtoms =  new J.util.BS ();
for (var i = collection.bsAtoms.nextSetBit (0); i >= 0; i = collection.bsAtoms.nextSetBit (i + 1)) this.bsAtoms.set (existingAtomsCount + i);

}var clonedAtoms = 0;
var atomSetCount0 = this.atomSetCount;
for (var atomSetNum = 0; atomSetNum < collection.atomSetCount; atomSetNum++) {
this.newAtomSet ();
var info = this.atomSetAuxiliaryInfo[this.currentAtomSetIndex] = collection.atomSetAuxiliaryInfo[atomSetNum];
var atomInfo = info.get ("PDB_CONECT_firstAtom_count_max");
if (atomInfo != null) atomInfo[0] += existingAtomsCount;
this.setAtomSetAuxiliaryInfo ("title", collection.collectionName);
this.setAtomSetName (collection.getAtomSetName (atomSetNum));
for (var atomNum = 0; atomNum < collection.atomSetAtomCounts[atomSetNum]; atomNum++) {
try {
if (this.bsAtoms != null) this.bsAtoms.set (this.atomCount);
this.newCloneAtom (collection.atoms[clonedAtoms]);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
this.errorMessage = "appendAtomCollection error: " + e;
} else {
throw e;
}
}
clonedAtoms++;
}
this.atomSetNumbers[this.currentAtomSetIndex] = (collectionIndex < 0 ? this.currentAtomSetIndex + 1 : ((collectionIndex + 1) * 1000000) + collection.atomSetNumbers[atomSetNum]);
}
for (var bondNum = 0; bondNum < collection.bondCount; bondNum++) {
var bond = collection.bonds[bondNum];
this.addNewBondWithOrder (bond.atomIndex1 + existingAtomsCount, bond.atomIndex2 + existingAtomsCount, bond.order);
}
for (var i = J.adapter.smarter.AtomSetCollection.globalBooleans.length; --i >= 0; ) if (collection.getGlobalBoolean (i)) this.setGlobalBoolean (i);

for (var i = 0; i < collection.structureCount; i++) {
var s = collection.structures[i];
this.addStructure (s);
s.modelStartEnd[0] += atomSetCount0;
s.modelStartEnd[1] += atomSetCount0;
}
}, "~N,J.adapter.smarter.AtomSetCollection");
$_M(c$, "setNoAutoBond", 
function () {
this.setAtomSetCollectionAuxiliaryInfo ("noAutoBond", Boolean.TRUE);
});
$_M(c$, "freeze", 
function (reverseModels) {
if (this.atomSetCount == 1 && this.collectionName == null) this.collectionName = this.getAtomSetAuxiliaryInfoValue (0, "name");
if (reverseModels) this.reverseAtomSets ();
if (this.trajectoryStepCount > 1) this.finalizeTrajectory ();
this.getList (true);
this.getList (false);
for (var i = 0; i < this.atomSetCount; i++) {
this.setAtomSetAuxiliaryInfoForSet ("initialAtomCount", Integer.$valueOf (this.atomSetAtomCounts[i]), i);
this.setAtomSetAuxiliaryInfoForSet ("initialBondCount", Integer.$valueOf (this.atomSetBondCounts[i]), i);
}
}, "~B");
$_M(c$, "reverseAtomSets", 
($fz = function () {
this.reverseArray (this.atomSetAtomIndexes);
this.reverseArray (this.atomSetNumbers);
this.reverseArray (this.atomSetAtomCounts);
this.reverseArray (this.atomSetBondCounts);
J.adapter.smarter.AtomSetCollection.reverseList (this.trajectorySteps);
J.adapter.smarter.AtomSetCollection.reverseList (this.trajectoryNames);
J.adapter.smarter.AtomSetCollection.reverseList (this.vibrationSteps);
this.reverseObject (this.atomSetAuxiliaryInfo);
for (var i = 0; i < this.atomCount; i++) this.atoms[i].atomSetIndex = this.atomSetCount - 1 - this.atoms[i].atomSetIndex;

for (var i = 0; i < this.structureCount; i++) {
var m = this.structures[i].modelStartEnd[0];
if (m >= 0) {
this.structures[i].modelStartEnd[0] = this.atomSetCount - 1 - this.structures[i].modelStartEnd[1];
this.structures[i].modelStartEnd[1] = this.atomSetCount - 1 - m;
}}
for (var i = 0; i < this.bondCount; i++) this.bonds[i].atomSetIndex = this.atomSetCount - 1 - this.atoms[this.bonds[i].atomIndex1].atomSetIndex;

this.reverseSets (this.bonds, this.bondCount);
var lists = J.util.ArrayUtil.createArrayOfArrayList (this.atomSetCount);
for (var i = 0; i < this.atomSetCount; i++) lists[i] =  new J.util.JmolList ();

for (var i = 0; i < this.atomCount; i++) lists[this.atoms[i].atomSetIndex].addLast (this.atoms[i]);

var newIndex =  Clazz.newIntArray (this.atomCount, 0);
var n = this.atomCount;
for (var i = this.atomSetCount; --i >= 0; ) for (var j = lists[i].size (); --j >= 0; ) {
var a = this.atoms[--n] = lists[i].get (j);
newIndex[a.index] = n;
a.index = n;
}

for (var i = 0; i < this.bondCount; i++) {
this.bonds[i].atomIndex1 = newIndex[this.bonds[i].atomIndex1];
this.bonds[i].atomIndex2 = newIndex[this.bonds[i].atomIndex2];
}
for (var i = 0; i < this.atomSetCount; i++) {
var conect = this.getAtomSetAuxiliaryInfoValue (i, "PDB_CONECT_firstAtom_count_max");
if (conect == null) continue;
conect[0] = newIndex[conect[0]];
conect[1] = this.atomSetAtomCounts[i];
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "reverseSets", 
($fz = function (o, n) {
var lists = J.util.ArrayUtil.createArrayOfArrayList (this.atomSetCount);
for (var i = 0; i < this.atomSetCount; i++) lists[i] =  new J.util.JmolList ();

for (var i = 0; i < n; i++) {
var index = o[i].atomSetIndex;
if (index < 0) return;
lists[o[i].atomSetIndex].addLast (o[i]);
}
for (var i = this.atomSetCount; --i >= 0; ) for (var j = lists[i].size (); --j >= 0; ) o[--n] = lists[i].get (j);


}, $fz.isPrivate = true, $fz), "~A,~N");
$_M(c$, "reverseObject", 
($fz = function (o) {
var n = this.atomSetCount;
for (var i = Clazz.doubleToInt (n / 2); --i >= 0; ) J.util.ArrayUtil.swap (o, i, n - 1 - i);

}, $fz.isPrivate = true, $fz), "~A");
c$.reverseList = $_M(c$, "reverseList", 
($fz = function (list) {
if (list == null) return;
java.util.Collections.reverse (list);
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "reverseArray", 
($fz = function (a) {
var n = this.atomSetCount;
for (var i = Clazz.doubleToInt (n / 2); --i >= 0; ) J.util.ArrayUtil.swapInt (a, i, n - 1 - i);

}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "getList", 
($fz = function (isAltLoc) {
var i;
for (i = this.atomCount; --i >= 0; ) if (this.atoms[i] != null && (isAltLoc ? this.atoms[i].alternateLocationID : this.atoms[i].insertionCode) != '\0') break;

if (i < 0) return;
var lists =  new Array (this.atomSetCount);
for (i = 0; i < this.atomSetCount; i++) lists[i] = "";

var pt;
for (i = 0; i < this.atomCount; i++) {
if (this.atoms[i] == null) continue;
var id = (isAltLoc ? this.atoms[i].alternateLocationID : this.atoms[i].insertionCode);
if (id != '\0' && lists[pt = this.atoms[i].atomSetIndex].indexOf (id) < 0) lists[pt] += id;
}
var type = (isAltLoc ? "altLocs" : "insertionCodes");
for (i = 0; i < this.atomSetCount; i++) if (lists[i].length > 0) this.setAtomSetAuxiliaryInfoForSet (type, lists[i], i);

}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "finish", 
function () {
if (this.reader != null) this.reader.finalizeModelSet ();
 else if (this.readerList != null) for (var i = 0; i < this.readerList.size (); i++) this.readerList.get (i).finalizeModelSet ();

this.atoms = null;
this.atomSetAtomCounts =  Clazz.newIntArray (16, 0);
this.atomSetAuxiliaryInfo =  new Array (16);
this.atomSetCollectionAuxiliaryInfo =  new java.util.Hashtable ();
this.atomSetCount = 0;
this.atomSetNumbers =  Clazz.newIntArray (16, 0);
this.atomSymbolicMap =  new java.util.Hashtable ();
this.bonds = null;
this.connectLast = null;
this.currentAtomSetIndex = -1;
this.latticeCells = null;
this.notionalUnitCell = null;
this.readerList = null;
this.symmetry = null;
this.structures =  new Array (16);
this.structureCount = 0;
this.trajectorySteps = null;
this.vibrationSteps = null;
this.vConnect = null;
});
$_M(c$, "discardPreviousAtoms", 
function () {
for (var i = this.atomCount; --i >= 0; ) this.atoms[i] = null;

this.atomCount = 0;
this.clearSymbolicMap ();
this.atomSetCount = 0;
this.currentAtomSetIndex = -1;
for (var i = this.atomSetAuxiliaryInfo.length; --i >= 0; ) {
this.atomSetAtomCounts[i] = 0;
this.atomSetBondCounts[i] = 0;
this.atomSetAuxiliaryInfo[i] = null;
}
});
$_M(c$, "removeAtomSet", 
function (imodel) {
if (this.bsAtoms == null) {
this.bsAtoms =  new J.util.BS ();
this.bsAtoms.setBits (0, this.atomCount);
}var i0 = this.atomSetAtomIndexes[imodel];
var nAtoms = this.atomSetAtomCounts[imodel];
var i1 = i0 + nAtoms;
this.bsAtoms.clearBits (i0, i1);
for (var i = i1; i < this.atomCount; i++) this.atoms[i].atomSetIndex--;

for (var i = imodel + 1; i < this.atomSetCount; i++) {
this.atomSetAuxiliaryInfo[i - 1] = this.atomSetAuxiliaryInfo[i];
this.atomSetAtomIndexes[i - 1] = this.atomSetAtomIndexes[i];
this.atomSetBondCounts[i - 1] = this.atomSetBondCounts[i];
this.atomSetAtomCounts[i - 1] = this.atomSetAtomCounts[i];
this.atomSetNumbers[i - 1] = this.atomSetNumbers[i];
}
var n = 0;
for (var i = 0; i < this.structureCount; i++) {
var s = this.structures[i];
if (s.modelStartEnd[0] == imodel && s.modelStartEnd[1] == imodel) {
this.structures[i] = null;
n++;
}}
if (n > 0) {
var ss =  new Array (this.structureCount - n);
for (var i = 0, pt = 0; i < this.structureCount; i++) if (this.structures[i] != null) ss[pt++] = this.structures[i];

this.structures = ss;
}for (var i = 0; i < this.bondCount; i++) this.bonds[i].atomSetIndex = this.atoms[this.bonds[i].atomIndex1].atomSetIndex;

this.atomSetAuxiliaryInfo[--this.atomSetCount] = null;
}, "~N");
$_M(c$, "removeCurrentAtomSet", 
function () {
if (this.currentAtomSetIndex < 0) return;
this.currentAtomSetIndex--;
this.atomSetCount--;
});
$_M(c$, "newCloneAtom", 
function (atom) {
var clone = atom.getClone ();
this.addAtom (clone);
return clone;
}, "J.adapter.smarter.Atom");
$_M(c$, "cloneFirstAtomSet", 
function (atomCount) {
if (!this.allowMultiple) return;
this.newAtomSet ();
if (atomCount == 0) atomCount = this.atomSetAtomCounts[0];
for (var i = 0; i < atomCount; ++i) this.newCloneAtom (this.atoms[i]);

}, "~N");
$_M(c$, "cloneFirstAtomSetWithBonds", 
function (nBonds) {
if (!this.allowMultiple) return;
this.cloneFirstAtomSet (0);
var firstCount = this.atomSetAtomCounts[0];
for (var bondNum = 0; bondNum < nBonds; bondNum++) {
var bond = this.bonds[this.bondCount - nBonds];
this.addNewBondWithOrder (bond.atomIndex1 + firstCount, bond.atomIndex2 + firstCount, bond.order);
}
}, "~N");
$_M(c$, "cloneLastAtomSet", 
function () {
this.cloneLastAtomSetFromPoints (0, null);
});
$_M(c$, "cloneLastAtomSetFromPoints", 
function (atomCount, pts) {
if (!this.allowMultiple) return;
var count = (atomCount > 0 ? atomCount : this.getLastAtomSetAtomCount ());
var atomIndex = this.getLastAtomSetAtomIndex ();
this.newAtomSet ();
for (var i = 0; i < count; ++i) {
var atom = this.newCloneAtom (this.atoms[atomIndex++]);
if (pts != null) atom.setT (pts[i]);
}
}, "~N,~A");
$_M(c$, "getFirstAtomSetAtomCount", 
function () {
return this.atomSetAtomCounts[0];
});
$_M(c$, "getLastAtomSetAtomCount", 
function () {
return this.atomSetAtomCounts[this.currentAtomSetIndex];
});
$_M(c$, "getLastAtomSetAtomIndex", 
function () {
return this.atomCount - this.atomSetAtomCounts[this.currentAtomSetIndex];
});
$_M(c$, "addNewAtom", 
function () {
var atom =  new J.adapter.smarter.Atom ();
this.addAtom (atom);
return atom;
});
$_M(c$, "addAtom", 
function (atom) {
if (this.atomCount == this.atoms.length) {
if (this.atomCount > 200000) this.atoms = J.util.ArrayUtil.ensureLength (this.atoms, this.atomCount + 50000);
 else this.atoms = J.util.ArrayUtil.doubleLength (this.atoms);
}if (this.atomSetCount == 0) this.newAtomSet ();
atom.index = this.atomCount;
this.atoms[this.atomCount++] = atom;
atom.atomSetIndex = this.currentAtomSetIndex;
atom.atomSite = this.atomSetAtomCounts[this.currentAtomSetIndex]++;
}, "J.adapter.smarter.Atom");
$_M(c$, "addAtomWithMappedName", 
function (atom) {
this.addAtom (atom);
this.mapMostRecentAtomName ();
}, "J.adapter.smarter.Atom");
$_M(c$, "addAtomWithMappedSerialNumber", 
function (atom) {
this.addAtom (atom);
this.mapMostRecentAtomSerialNumber ();
}, "J.adapter.smarter.Atom");
$_M(c$, "addNewBondWithOrder", 
function (atomIndex1, atomIndex2, order) {
if (atomIndex1 < 0 || atomIndex1 >= this.atomCount || atomIndex2 < 0 || atomIndex2 >= this.atomCount) return null;
var bond =  new J.adapter.smarter.Bond (atomIndex1, atomIndex2, order);
this.addBond (bond);
return bond;
}, "~N,~N,~N");
$_M(c$, "addNewBondFromNames", 
function (atomName1, atomName2, order) {
return this.addNewBondWithOrder (this.getAtomIndexFromName (atomName1), this.getAtomIndexFromName (atomName2), order);
}, "~S,~S,~N");
$_M(c$, "addNewBondWithMappedSerialNumbers", 
function (atomSerial1, atomSerial2, order) {
return this.addNewBondWithOrder (this.getAtomIndexFromSerial (atomSerial1), this.getAtomIndexFromSerial (atomSerial2), order);
}, "~N,~N,~N");
$_M(c$, "addConnection", 
function (is) {
if (this.vConnect == null) {
this.connectLast = null;
this.vConnect =  new J.util.JmolList ();
}if (this.connectLast != null) {
if (is[0] == this.connectLast[0] && is[1] == this.connectLast[1] && is[2] != 2048) {
this.connectLast[2]++;
return;
}}this.vConnect.addLast (this.connectLast = is);
}, "~A");
$_M(c$, "connectAllBad", 
($fz = function (maxSerial) {
var firstAtom = this.connectNextAtomIndex;
for (var i = this.connectNextAtomSet; i < this.atomSetCount; i++) {
this.setAtomSetAuxiliaryInfoForSet ("PDB_CONECT_firstAtom_count_max", [firstAtom, this.atomSetAtomCounts[i], maxSerial], i);
if (this.vConnect != null) {
this.setAtomSetAuxiliaryInfoForSet ("PDB_CONECT_bonds", this.vConnect, i);
this.setGlobalBoolean (3);
}firstAtom += this.atomSetAtomCounts[i];
}
this.vConnect = null;
this.connectNextAtomSet = this.currentAtomSetIndex + 1;
this.connectNextAtomIndex = firstAtom;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "connectAll", 
function (maxSerial, isConnectStateBug) {
if (this.currentAtomSetIndex < 0) return;
if (isConnectStateBug) {
this.connectAllBad (maxSerial);
return;
}this.setAtomSetAuxiliaryInfo ("PDB_CONECT_firstAtom_count_max", [this.atomSetAtomIndexes[this.currentAtomSetIndex], this.atomSetAtomCounts[this.currentAtomSetIndex], maxSerial]);
if (this.vConnect == null) return;
var firstAtom = this.connectNextAtomIndex;
for (var i = this.connectNextAtomSet; i < this.atomSetCount; i++) {
this.setAtomSetAuxiliaryInfoForSet ("PDB_CONECT_bonds", this.vConnect, i);
this.setGlobalBoolean (3);
firstAtom += this.atomSetAtomCounts[i];
}
this.vConnect = null;
this.connectNextAtomSet = this.currentAtomSetIndex + 1;
this.connectNextAtomIndex = firstAtom;
}, "~N,~B");
$_M(c$, "addBond", 
function (bond) {
if (this.trajectoryStepCount > 0) return;
if (bond.atomIndex1 < 0 || bond.atomIndex2 < 0 || bond.order < 0 || this.atoms[bond.atomIndex1].atomSetIndex != this.atoms[bond.atomIndex2].atomSetIndex) {
if (J.util.Logger.debugging) {
J.util.Logger.debug (">>>>>>BAD BOND:" + bond.atomIndex1 + "-" + bond.atomIndex2 + " order=" + bond.order);
}return;
}if (this.bondCount == this.bonds.length) this.bonds = J.util.ArrayUtil.arrayCopyObject (this.bonds, this.bondCount + 1024);
this.bonds[this.bondCount++] = bond;
this.atomSetBondCounts[this.currentAtomSetIndex]++;
}, "J.adapter.smarter.Bond");
$_M(c$, "finalizeStructures", 
function () {
if (this.structureCount == 0) return;
this.bsStructuredModels =  new J.util.BS ();
var map =  new java.util.Hashtable ();
for (var i = 0; i < this.structureCount; i++) {
var s = this.structures[i];
if (s.modelStartEnd[0] == -1) {
s.modelStartEnd[0] = 0;
s.modelStartEnd[1] = this.atomSetCount - 1;
}this.bsStructuredModels.setBits (s.modelStartEnd[0], s.modelStartEnd[1] + 1);
if (s.strandCount == 0) continue;
var key = s.structureID + " " + s.modelStartEnd[0];
var v = map.get (key);
var count = (v == null ? 0 : v.intValue ()) + 1;
map.put (key, Integer.$valueOf (count));
}
for (var i = 0; i < this.structureCount; i++) {
var s = this.structures[i];
if (s.strandCount == 1) s.strandCount = map.get (s.structureID + " " + s.modelStartEnd[0]).intValue ();
}
});
$_M(c$, "addStructure", 
function (structure) {
if (this.structureCount == this.structures.length) this.structures = J.util.ArrayUtil.arrayCopyObject (this.structures, this.structureCount + 32);
this.structures[this.structureCount++] = structure;
}, "J.adapter.smarter.Structure");
$_M(c$, "addVibrationVectorWithSymmetry", 
function (iatom, vx, vy, vz, withSymmetry) {
if (!withSymmetry) {
this.addVibrationVector (iatom, vx, vy, vz);
return;
}var atomSite = this.atoms[iatom].atomSite;
var atomSetIndex = this.atoms[iatom].atomSetIndex;
for (var i = iatom; i < this.atomCount && this.atoms[i].atomSetIndex == atomSetIndex; i++) {
if (this.atoms[i].atomSite == atomSite) this.addVibrationVector (i, vx, vy, vz);
}
}, "~N,~N,~N,~N,~B");
$_M(c$, "addVibrationVector", 
function (iatom, x, y, z) {
if (!this.allowMultiple) iatom = iatom % this.atomCount;
this.atoms[iatom].vib = J.util.V3.new3 (x, y, z);
}, "~N,~N,~N,~N");
$_M(c$, "setAtomSetSpaceGroupName", 
function (spaceGroupName) {
this.setAtomSetAuxiliaryInfo ("spaceGroup", spaceGroupName + "");
}, "~S");
$_M(c$, "setCoordinatesAreFractional", 
function (tf) {
this.coordinatesAreFractional = tf;
this.setAtomSetAuxiliaryInfo ("coordinatesAreFractional", Boolean.$valueOf (tf));
if (tf) this.setGlobalBoolean (0);
}, "~B");
$_M(c$, "setSymmetryRange", 
function (factor) {
this.symmetryRange = factor;
this.setAtomSetCollectionAuxiliaryInfo ("symmetryRange", Float.$valueOf (factor));
}, "~N");
$_M(c$, "setLatticeCells", 
function (latticeCells, applySymmetryToBonds, doPackUnitCell, doCentroidUnitCell, centroidPacked, strSupercell, ptSupercell) {
this.latticeCells = latticeCells;
var isLatticeRange = (latticeCells[0] <= 555 && latticeCells[1] >= 555 && (latticeCells[2] == 0 || latticeCells[2] == 1 || latticeCells[2] == -1));
this.doNormalize = latticeCells[0] != 0 && (!isLatticeRange || latticeCells[2] == 1);
this.applySymmetryToBonds = applySymmetryToBonds;
this.doPackUnitCell = doPackUnitCell;
this.doCentroidUnitCell = doCentroidUnitCell;
this.centroidPacked = centroidPacked;
if (strSupercell != null) this.setSuperCell (strSupercell);
 else this.ptSupercell = ptSupercell;
}, "~A,~B,~B,~B,~B,~S,J.util.P3");
$_M(c$, "setSupercellFromPoint", 
function (pt) {
this.ptSupercell = pt;
J.util.Logger.info ("Using supercell " + J.util.Escape.eP (pt));
}, "J.util.P3");
$_M(c$, "setSuperCell", 
($fz = function (supercell) {
if (this.fmatSupercell != null) return;
this.fmatSupercell =  Clazz.newFloatArray (16, 0);
if (this.getSymmetry ().getMatrixFromString (supercell, this.fmatSupercell, true) == null) {
this.fmatSupercell = null;
return;
}J.util.Logger.info ("Using supercell \n" + J.util.Matrix4f.newA (this.fmatSupercell));
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getSymmetry", 
function () {
if (this.symmetry == null) this.symmetry = J.api.Interface.getOptionInterface ("symmetry.Symmetry");
return this.symmetry;
});
$_M(c$, "setNotionalUnitCell", 
function (info, matUnitCellOrientation, unitCellOffset) {
this.notionalUnitCell =  Clazz.newFloatArray (info.length, 0);
this.unitCellOffset = unitCellOffset;
for (var i = 0; i < info.length; i++) this.notionalUnitCell[i] = info[i];

this.haveUnitCell = true;
this.setAtomSetAuxiliaryInfo ("notionalUnitcell", this.notionalUnitCell);
this.setGlobalBoolean (2);
this.getSymmetry ().setUnitCell (this.notionalUnitCell);
if (unitCellOffset != null) {
this.symmetry.setOffsetPt (unitCellOffset);
this.setAtomSetAuxiliaryInfo ("unitCellOffset", unitCellOffset);
}if (matUnitCellOrientation != null) {
this.symmetry.setUnitCellOrientation (matUnitCellOrientation);
this.setAtomSetAuxiliaryInfo ("matUnitCellOrientation", matUnitCellOrientation);
}}, "~A,J.util.Matrix3f,J.util.P3");
$_M(c$, "addSpaceGroupOperation", 
function (xyz) {
this.getSymmetry ().setSpaceGroup (this.doNormalize);
return this.symmetry.addSpaceGroupOperation (xyz, 0);
}, "~S");
$_M(c$, "setLatticeParameter", 
function (latt) {
this.getSymmetry ().setSpaceGroup (this.doNormalize);
this.symmetry.setLattice (latt);
}, "~N");
$_M(c$, "applySymmetry", 
function (symmetry) {
if (symmetry != null) this.getSymmetry ().setSpaceGroupS (symmetry);
this.applySymmetryLattice (this.latticeCells[0], this.latticeCells[1], Math.abs (this.latticeCells[2]));
}, "J.api.SymmetryInterface");
$_M(c$, "applySymmetryLattice", 
($fz = function (maxX, maxY, maxZ) {
if (!this.coordinatesAreFractional || !this.getSymmetry ().haveSpaceGroup ()) return;
if (this.fmatSupercell != null) {
this.rminx = 3.4028235E38;
this.rminy = 3.4028235E38;
this.rminz = 3.4028235E38;
this.rmaxx = -3.4028235E38;
this.rmaxy = -3.4028235E38;
this.rmaxz = -3.4028235E38;
var ptx = this.setSym (0, 1, 2);
var pty = this.setSym (4, 5, 6);
var ptz = this.setSym (8, 9, 10);
this.minXYZ = J.util.P3i.new3 (Clazz.floatToInt (this.rminx), Clazz.floatToInt (this.rminy), Clazz.floatToInt (this.rminz));
this.maxXYZ = J.util.P3i.new3 (Clazz.floatToInt (this.rmaxx), Clazz.floatToInt (this.rmaxy), Clazz.floatToInt (this.rmaxz));
this.applyAllSymmetry ();
var iAtomFirst = this.getLastAtomSetAtomIndex ();
for (var i = iAtomFirst; i < this.atomCount; i++) this.symmetry.toCartesian (this.atoms[i], true);

this.symmetry = null;
this.setNotionalUnitCell ([0, 0, 0, 0, 0, 0, ptx.x, ptx.y, ptx.z, pty.x, pty.y, pty.z, ptz.x, ptz.y, ptz.z], null, this.getAtomSetAuxiliaryInfoValue (-1, "unitCellOffset"));
this.setAtomSetSpaceGroupName ("P1");
this.getSymmetry ().setSpaceGroup (this.doNormalize);
this.symmetry.addSpaceGroupOperation ("x,y,z", 0);
for (var i = iAtomFirst; i < this.atomCount; i++) this.symmetry.toFractional (this.atoms[i], true);

this.haveAnisou = false;
this.atomSetAuxiliaryInfo[this.currentAtomSetIndex].remove ("matUnitCellOrientation");
this.doPackUnitCell = false;
}this.minXYZ =  new J.util.P3i ();
this.maxXYZ = J.util.P3i.new3 (maxX, maxY, maxZ);
this.applyAllSymmetry ();
this.fmatSupercell = null;
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "setSym", 
($fz = function (i, j, k) {
var pt =  new J.util.P3 ();
pt.set (this.fmatSupercell[i], this.fmatSupercell[j], this.fmatSupercell[k]);
this.setSymmetryMinMax (pt);
this.symmetry.toCartesian (pt, false);
return pt;
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "setSymmetryMinMax", 
($fz = function (c) {
if (this.rminx > c.x) this.rminx = c.x;
if (this.rminy > c.y) this.rminy = c.y;
if (this.rminz > c.z) this.rminz = c.z;
if (this.rmaxx < c.x) this.rmaxx = c.x;
if (this.rmaxy < c.y) this.rmaxy = c.y;
if (this.rmaxz < c.z) this.rmaxz = c.z;
}, $fz.isPrivate = true, $fz), "J.util.P3");
$_M(c$, "isInSymmetryRange", 
($fz = function (c) {
return (c.x >= this.rminx && c.y >= this.rminy && c.z >= this.rminz && c.x <= this.rmaxx && c.y <= this.rmaxy && c.z <= this.rmaxz);
}, $fz.isPrivate = true, $fz), "J.util.P3");
c$.isWithinCell = $_M(c$, "isWithinCell", 
($fz = function (dtype, pt, minX, maxX, minY, maxY, minZ, maxZ) {
var slop = 0.02;
return (pt.x > minX - slop && pt.x < maxX + slop && (dtype < 2 || pt.y > minY - slop && pt.y < maxY + slop) && (dtype < 3 || pt.z > minZ - slop && pt.z < maxZ + slop));
}, $fz.isPrivate = true, $fz), "~N,J.util.P3,~N,~N,~N,~N,~N,~N");
$_M(c$, "setAnisoBorU", 
function (atom, data, type) {
this.haveAnisou = true;
atom.anisoBorU = data;
data[6] = type;
}, "J.adapter.smarter.Atom,~A,~N");
$_M(c$, "getAnisoBorU", 
function (atom) {
return atom.anisoBorU;
}, "J.adapter.smarter.Atom");
$_M(c$, "setTensors", 
function () {
if (!this.haveAnisou) return;
this.getSymmetry ();
for (var i = this.getLastAtomSetAtomIndex (); i < this.atomCount; i++) this.atoms[i].addTensor (this.symmetry.getTensor (this.atoms[i].anisoBorU), null, false);

});
$_M(c$, "setLatticeOnly", 
function (b) {
this.checkLatticeOnly = b;
}, "~B");
$_M(c$, "setBaseSymmetryAtomCount", 
function (n) {
this.baseSymmetryAtomCount = n;
}, "~N");
$_M(c$, "applyAllSymmetry", 
($fz = function () {
var noSymmetryCount = (this.baseSymmetryAtomCount == 0 ? this.getLastAtomSetAtomCount () : this.baseSymmetryAtomCount);
var iAtomFirst = this.getLastAtomSetAtomIndex ();
this.setTensors ();
this.bondCount0 = this.bondCount;
this.finalizeSymmetry (iAtomFirst, noSymmetryCount);
var operationCount = this.symmetry.getSpaceGroupOperationCount ();
this.getSymmetry ().setMinMaxLatticeParameters (this.minXYZ, this.maxXYZ);
this.dtype = Clazz.floatToInt (this.getSymmetry ().getUnitCellInfoType (6));
if (this.doCentroidUnitCell) this.setAtomSetCollectionAuxiliaryInfo ("centroidMinMax", [this.minXYZ.x, this.minXYZ.y, this.minXYZ.z, this.maxXYZ.x, this.maxXYZ.y, this.maxXYZ.z, (this.centroidPacked ? 1 : 0)]);
if (this.ptSupercell != null) {
this.setAtomSetAuxiliaryInfo ("supercell", this.ptSupercell);
switch (this.dtype) {
case 3:
this.minXYZ.z *= Clazz.floatToInt (Math.abs (this.ptSupercell.z));
this.maxXYZ.z *= Clazz.floatToInt (Math.abs (this.ptSupercell.z));
case 2:
this.minXYZ.y *= Clazz.floatToInt (Math.abs (this.ptSupercell.y));
this.maxXYZ.y *= Clazz.floatToInt (Math.abs (this.ptSupercell.y));
case 1:
this.minXYZ.x *= Clazz.floatToInt (Math.abs (this.ptSupercell.x));
this.maxXYZ.x *= Clazz.floatToInt (Math.abs (this.ptSupercell.x));
}
}if (this.doCentroidUnitCell || this.doPackUnitCell || this.symmetryRange != 0 && this.maxXYZ.x - this.minXYZ.x == 1 && this.maxXYZ.y - this.minXYZ.y == 1 && this.maxXYZ.z - this.minXYZ.z == 1) {
this.minXYZ0 = J.util.P3i.new3 (this.minXYZ.x, this.minXYZ.y, this.minXYZ.z);
this.maxXYZ0 = J.util.P3i.new3 (this.maxXYZ.x, this.maxXYZ.y, this.maxXYZ.z);
switch (this.dtype) {
case 3:
this.minXYZ.z--;
this.maxXYZ.z++;
case 2:
this.minXYZ.y--;
this.maxXYZ.y++;
case 1:
this.minXYZ.x--;
this.maxXYZ.x++;
}
}var nCells = (this.maxXYZ.x - this.minXYZ.x) * (this.maxXYZ.y - this.minXYZ.y) * (this.maxXYZ.z - this.minXYZ.z);
var cartesianCount = (this.checkSpecial ? noSymmetryCount * operationCount * nCells : this.symmetryRange > 0 ? noSymmetryCount * operationCount : this.symmetryRange < 0 ? 1 : 1);
var cartesians =  new Array (cartesianCount);
for (var i = 0; i < noSymmetryCount; i++) this.atoms[i + iAtomFirst].bsSymmetry = J.util.BSUtil.newBitSet (operationCount * (nCells + 1));

var pt = 0;
var unitCells =  Clazz.newIntArray (nCells, 0);
this.unitCellTranslations =  new Array (nCells);
var iCell = 0;
var cell555Count = 0;
var absRange = Math.abs (this.symmetryRange);
var checkSymmetryRange = (this.symmetryRange != 0);
var checkRangeNoSymmetry = (this.symmetryRange < 0);
var checkRange111 = (this.symmetryRange > 0);
if (checkSymmetryRange) {
this.rminx = 3.4028235E38;
this.rminy = 3.4028235E38;
this.rminz = 3.4028235E38;
this.rmaxx = -3.4028235E38;
this.rmaxy = -3.4028235E38;
this.rmaxz = -3.4028235E38;
}this.latticeOp = this.symmetry.getLatticeOp ();
this.checkAll = (this.atomSetCount == 1 && this.checkSpecial && this.latticeOp >= 0);
this.latticeOnly = (this.checkLatticeOnly && this.latticeOp >= 0);
var op = this.symmetry.getSpaceGroupOperation (0);
if (this.doPackUnitCell) this.ptOffset.set (0, 0, 0);
for (var tx = this.minXYZ.x; tx < this.maxXYZ.x; tx++) for (var ty = this.minXYZ.y; ty < this.maxXYZ.y; ty++) for (var tz = this.minXYZ.z; tz < this.maxXYZ.z; tz++) {
this.unitCellTranslations[iCell] = J.util.V3.new3 (tx, ty, tz);
unitCells[iCell++] = 555 + tx * 100 + ty * 10 + tz;
if (tx != 0 || ty != 0 || tz != 0 || cartesians.length == 0) continue;
for (pt = 0; pt < noSymmetryCount; pt++) {
var atom = this.atoms[iAtomFirst + pt];
var c = J.util.P3.newP (atom);
op.transform (c);
this.symmetry.toCartesian (c, false);
if (this.doPackUnitCell) {
this.symmetry.toUnitCell (c, this.ptOffset);
atom.setT (c);
this.symmetry.toFractional (atom, false);
}atom.bsSymmetry.set (iCell * operationCount);
atom.bsSymmetry.set (0);
if (checkSymmetryRange) this.setSymmetryMinMax (c);
if (pt < cartesianCount) cartesians[pt] = c;
}
if (checkRangeNoSymmetry) {
this.rminx -= absRange;
this.rminy -= absRange;
this.rminz -= absRange;
this.rmaxx += absRange;
this.rmaxy += absRange;
this.rmaxz += absRange;
}cell555Count = pt = this.symmetryAddAtoms (iAtomFirst, noSymmetryCount, 0, 0, 0, 0, pt, iCell * operationCount, cartesians);
}


if (checkRange111) {
this.rminx -= absRange;
this.rminy -= absRange;
this.rminz -= absRange;
this.rmaxx += absRange;
this.rmaxy += absRange;
this.rmaxz += absRange;
}iCell = 0;
for (var tx = this.minXYZ.x; tx < this.maxXYZ.x; tx++) for (var ty = this.minXYZ.y; ty < this.maxXYZ.y; ty++) for (var tz = this.minXYZ.z; tz < this.maxXYZ.z; tz++) {
iCell++;
if (tx != 0 || ty != 0 || tz != 0) pt = this.symmetryAddAtoms (iAtomFirst, noSymmetryCount, tx, ty, tz, cell555Count, pt, iCell * operationCount, cartesians);
}


if (iCell * noSymmetryCount == this.atomCount - iAtomFirst) this.appendAtomProperties (iCell);
this.setSymmetryOps ();
this.setAtomSetAuxiliaryInfo ("presymmetryAtomIndex", Integer.$valueOf (iAtomFirst));
this.setAtomSetAuxiliaryInfo ("presymmetryAtomCount", Integer.$valueOf (noSymmetryCount));
this.setAtomSetAuxiliaryInfo ("latticeDesignation", this.symmetry.getLatticeDesignation ());
this.setAtomSetAuxiliaryInfo ("unitCellRange", unitCells);
this.setAtomSetAuxiliaryInfo ("unitCellTranslations", this.unitCellTranslations);
this.notionalUnitCell =  Clazz.newFloatArray (6, 0);
this.coordinatesAreFractional = false;
this.setAtomSetAuxiliaryInfo ("hasSymmetry", Boolean.TRUE);
this.setGlobalBoolean (1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "finalizeSymmetry", 
($fz = function (iAtomFirst, noSymmetryCount) {
this.symmetry.setFinalOperations (this.atoms, iAtomFirst, noSymmetryCount, this.doNormalize);
var name = this.getAtomSetAuxiliaryInfoValue (-1, "spaceGroup");
if (name == null || name.equals ("unspecified!")) this.setAtomSetSpaceGroupName (this.symmetry.getSpaceGroupName ());
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setSymmetryOps", 
($fz = function () {
var operationCount = this.symmetry.getSpaceGroupOperationCount ();
if (operationCount > 0) {
var symmetryList =  new Array (operationCount);
for (var i = 0; i < operationCount; i++) symmetryList[i] = "" + this.symmetry.getSpaceGroupXyz (i, this.doNormalize);

this.setAtomSetAuxiliaryInfo ("symmetryOperations", symmetryList);
}this.setAtomSetAuxiliaryInfo ("symmetryCount", Integer.$valueOf (operationCount));
}, $fz.isPrivate = true, $fz));
$_M(c$, "setCheckSpecial", 
function (TF) {
this.checkSpecial = TF;
}, "~B");
$_M(c$, "symmetryAddAtoms", 
($fz = function (iAtomFirst, noSymmetryCount, transX, transY, transZ, baseCount, pt, iCellOpPt, cartesians) {
var isBaseCell = (baseCount == 0);
var addBonds = (this.bondCount0 > this.bondIndex0 && this.applySymmetryToBonds);
var atomMap = (addBonds ?  Clazz.newIntArray (noSymmetryCount, 0) : null);
if (this.doPackUnitCell) this.ptOffset.set (transX, transY, transZ);
var range2 = this.symmetryRange * this.symmetryRange;
var checkRangeNoSymmetry = (this.symmetryRange < 0);
var checkRange111 = (this.symmetryRange > 0);
var checkSymmetryMinMax = (isBaseCell && checkRange111);
checkRange111 = new Boolean (checkRange111 & !isBaseCell).valueOf ();
var nOperations = this.symmetry.getSpaceGroupOperationCount ();
if (nOperations == 1) this.checkSpecial = false;
var checkSymmetryRange = (checkRangeNoSymmetry || checkRange111);
var checkDistances = (this.checkSpecial || checkSymmetryRange);
var addCartesian = (this.checkSpecial || checkSymmetryMinMax);
if (checkRangeNoSymmetry) baseCount = noSymmetryCount;
var atomMax = iAtomFirst + noSymmetryCount;
var ptAtom =  new J.util.P3 ();
for (var iSym = 0; iSym < nOperations; iSym++) {
if (isBaseCell && iSym == 0 || this.latticeOnly && iSym > 0 && iSym != this.latticeOp) continue;
var pt0 = (this.checkSpecial ? pt : checkRange111 ? baseCount : 0);
for (var i = iAtomFirst; i < atomMax; i++) {
if (this.atoms[i].ignoreSymmetry) continue;
if (this.bsAtoms != null && !this.bsAtoms.get (i)) continue;
this.symmetry.newSpaceGroupPoint (iSym, this.atoms[i], ptAtom, transX, transY, transZ);
var special = null;
var cartesian = J.util.P3.newP (ptAtom);
this.symmetry.toCartesian (cartesian, false);
if (this.doPackUnitCell) {
this.symmetry.toUnitCell (cartesian, this.ptOffset);
ptAtom.setT (cartesian);
this.symmetry.toFractional (ptAtom, false);
if (!J.adapter.smarter.AtomSetCollection.isWithinCell (this.dtype, ptAtom, this.minXYZ0.x, this.maxXYZ0.x, this.minXYZ0.y, this.maxXYZ0.y, this.minXYZ0.z, this.maxXYZ0.z)) continue;
}if (checkSymmetryMinMax) this.setSymmetryMinMax (cartesian);
if (checkDistances) {
var minDist2 = 3.4028235E38;
if (checkSymmetryRange && !this.isInSymmetryRange (cartesian)) continue;
var j0 = (this.checkAll ? this.atomCount : pt0);
for (var j = j0; --j >= 0; ) {
var d2 = cartesian.distanceSquared (cartesians[j]);
if (this.checkSpecial && d2 < 0.0001) {
special = this.atoms[iAtomFirst + j];
if (special.atomName == null || special.atomName.equals (this.atoms[i].atomName)) break;
special = null;
}if (checkRange111 && j < baseCount && d2 < minDist2) minDist2 = d2;
}
if (checkRange111 && minDist2 > range2) continue;
}var atomSite = this.atoms[i].atomSite;
if (special != null) {
if (addBonds) atomMap[atomSite] = special.index;
special.bsSymmetry.set (iCellOpPt + iSym);
special.bsSymmetry.set (iSym);
} else {
if (addBonds) atomMap[atomSite] = this.atomCount;
var atom1 = this.newCloneAtom (this.atoms[i]);
atom1.setT (ptAtom);
atom1.atomSite = atomSite;
atom1.bsSymmetry = J.util.BSUtil.newAndSetBit (iCellOpPt + iSym);
atom1.bsSymmetry.set (iSym);
if (addCartesian) cartesians[pt++] = cartesian;
if (this.atoms[i].tensors != null) {
atom1.tensors = null;
for (var j = this.atoms[i].tensors.size (); --j >= 0; ) {
var t = this.atoms[i].tensors.get (j);
if (t == null) continue;
if (nOperations == 1) atom1.addTensor (J.util.Tensor.copyTensor (t), null, false);
 else this.addRotatedTensor (atom1, t, iSym, false);
}
}}}
if (addBonds) {
for (var bondNum = this.bondIndex0; bondNum < this.bondCount0; bondNum++) {
var bond = this.bonds[bondNum];
var atom1 = this.atoms[bond.atomIndex1];
var atom2 = this.atoms[bond.atomIndex2];
if (atom1 == null || atom2 == null) continue;
var iAtom1 = atomMap[atom1.atomSite];
var iAtom2 = atomMap[atom2.atomSite];
if (iAtom1 >= atomMax || iAtom2 >= atomMax) this.addNewBondWithOrder (iAtom1, iAtom2, bond.order);
}
}}
return pt;
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N,~N,~N,~N,~A");
$_M(c$, "addRotatedTensor", 
function (a, t, iSym, reset) {
if (this.ptTemp == null) {
this.ptTemp =  new J.util.P3 ();
this.mTemp =  new J.util.Matrix3f ();
}return a.addTensor (J.util.Tensor.getTensorFromEigenVectors (this.symmetry.rotateAxes (iSym, t.eigenVectors, this.ptTemp, this.mTemp), t.eigenValues, t.isIsotropic ? "iso" : t.type, t.id), null, reset);
}, "J.adapter.smarter.Atom,J.util.Tensor,~N,~B");
$_M(c$, "applySymmetryBio", 
function (biomts, notionalUnitCell, applySymmetryToBonds, filter) {
if (this.latticeCells != null && this.latticeCells[0] != 0) {
J.util.Logger.error ("Cannot apply biomolecule when lattice cells are indicated");
return;
}this.doNormalize = false;
this.symmetry = null;
this.getSymmetry ();
this.setNotionalUnitCell (notionalUnitCell, null, this.unitCellOffset);
this.getSymmetry ().setSpaceGroup (this.doNormalize);
this.addSpaceGroupOperation ("x,y,z");
this.setAtomSetSpaceGroupName ("biomolecule");
var len = biomts.size ();
this.applySymmetryToBonds = applySymmetryToBonds;
this.bondCount0 = this.bondCount;
var addBonds = (this.bondCount0 > this.bondIndex0 && applySymmetryToBonds);
var atomMap = (addBonds ?  Clazz.newIntArray (this.atomCount, 0) : null);
var iAtomFirst = this.getLastAtomSetAtomIndex ();
var atomMax = this.atomCount;
if (filter.indexOf ("#<") >= 0) {
len = Math.min (len, J.util.Parser.parseInt (filter.substring (filter.indexOf ("#<") + 2)) - 1);
filter = J.util.TextFormat.simpleReplace (filter, "#<", "_<");
}for (var iAtom = iAtomFirst; iAtom < atomMax; iAtom++) this.atoms[iAtom].bsSymmetry = J.util.BSUtil.newAndSetBit (0);

for (var i = 1; i < len; i++) {
if (filter.indexOf ("!#") >= 0) {
if (filter.indexOf ("!#" + (i + 1) + ";") >= 0) continue;
} else if (filter.indexOf ("#") >= 0 && filter.indexOf ("#" + (i + 1) + ";") < 0) {
continue;
}var mat = biomts.get (i);
for (var iAtom = iAtomFirst; iAtom < atomMax; iAtom++) {
if (this.bsAtoms != null && !this.bsAtoms.get (iAtom)) continue;
try {
var atomSite = this.atoms[iAtom].atomSite;
var atom1;
if (addBonds) atomMap[atomSite] = this.atomCount;
atom1 = this.newCloneAtom (this.atoms[iAtom]);
if (this.bsAtoms != null) this.bsAtoms.set (atom1.index);
atom1.atomSite = atomSite;
mat.transform (atom1);
atom1.bsSymmetry = J.util.BSUtil.newAndSetBit (i);
if (addBonds) {
for (var bondNum = this.bondIndex0; bondNum < this.bondCount0; bondNum++) {
var bond = this.bonds[bondNum];
var iAtom1 = atomMap[this.atoms[bond.atomIndex1].atomSite];
var iAtom2 = atomMap[this.atoms[bond.atomIndex2].atomSite];
if (iAtom1 >= atomMax || iAtom2 >= atomMax) this.addNewBondWithOrder (iAtom1, iAtom2, bond.order);
}
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
this.errorMessage = "appendAtomCollection error: " + e;
} else {
throw e;
}
}
}
mat.m03 /= notionalUnitCell[0];
mat.m13 /= notionalUnitCell[1];
mat.m23 /= notionalUnitCell[2];
if (this.symmetry != null && i > 0) this.symmetry.addSpaceGroupOperationM (mat);
}
var noSymmetryCount = atomMax - iAtomFirst;
this.setAtomSetAuxiliaryInfo ("presymmetryAtomIndex", Integer.$valueOf (iAtomFirst));
this.setAtomSetAuxiliaryInfo ("presymmetryAtomCount", Integer.$valueOf (noSymmetryCount));
this.setAtomSetAuxiliaryInfo ("biosymmetryCount", Integer.$valueOf (len));
if (this.symmetry != null) {
this.finalizeSymmetry (iAtomFirst, noSymmetryCount);
this.setSymmetryOps ();
}this.symmetry = null;
this.coordinatesAreFractional = false;
this.setAtomSetAuxiliaryInfo ("hasSymmetry", Boolean.TRUE);
this.setGlobalBoolean (1);
}, "J.util.JmolList,~A,~B,~S");
$_M(c$, "mapMostRecentAtomName", 
($fz = function () {
if (this.atomCount > 0) {
var index = this.atomCount - 1;
var atomName = this.atoms[index].atomName;
if (atomName != null) this.atomSymbolicMap.put (atomName, Integer.$valueOf (index));
}}, $fz.isPrivate = true, $fz));
$_M(c$, "clearSymbolicMap", 
function () {
this.atomSymbolicMap.clear ();
this.haveMappedSerials = false;
});
$_M(c$, "mapMostRecentAtomSerialNumber", 
($fz = function () {
if (this.atomCount == 0) return;
var index = this.atomCount - 1;
var atomSerial = this.atoms[index].atomSerial;
if (atomSerial != -2147483648) this.atomSymbolicMap.put (Integer.$valueOf (atomSerial), Integer.$valueOf (index));
this.haveMappedSerials = true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "createAtomSerialMap", 
function () {
if (this.haveMappedSerials || this.currentAtomSetIndex < 0) return;
for (var i = this.getLastAtomSetAtomCount (); i < this.atomCount; i++) {
var atomSerial = this.atoms[i].atomSerial;
if (atomSerial != -2147483648) this.atomSymbolicMap.put (Integer.$valueOf (atomSerial), Integer.$valueOf (i));
}
this.haveMappedSerials = true;
});
$_M(c$, "getAtomIndexFromName", 
function (atomName) {
return this.getMapIndex (atomName);
}, "~S");
$_M(c$, "getAtomIndexFromSerial", 
function (serialNumber) {
return this.getMapIndex (Integer.$valueOf (serialNumber));
}, "~N");
$_M(c$, "getMapIndex", 
($fz = function (nameOrNum) {
var value = this.atomSymbolicMap.get (nameOrNum);
return (value == null ? -1 : value.intValue ());
}, $fz.isPrivate = true, $fz), "~O");
$_M(c$, "setAtomSetCollectionAuxiliaryInfo", 
function (key, value) {
if (value == null) this.atomSetCollectionAuxiliaryInfo.remove (key);
 else this.atomSetCollectionAuxiliaryInfo.put (key, value);
}, "~S,~O");
$_M(c$, "setAtomSetCollectionPartialCharges", 
function (auxKey) {
if (!this.atomSetCollectionAuxiliaryInfo.containsKey (auxKey)) {
return false;
}var atomData = this.atomSetCollectionAuxiliaryInfo.get (auxKey);
for (var i = atomData.size (); --i >= 0; ) this.atoms[i].partialCharge = atomData.get (i).floatValue ();

J.util.Logger.info ("Setting partial charges type " + auxKey);
return true;
}, "~S");
$_M(c$, "mapPartialCharge", 
function (atomName, charge) {
this.atoms[this.getAtomIndexFromName (atomName)].partialCharge = charge;
}, "~S,~N");
$_M(c$, "getAtomSetCollectionAuxiliaryInfo", 
function (key) {
return this.atomSetCollectionAuxiliaryInfo.get (key);
}, "~S");
$_M(c$, "addTrajectoryStep", 
($fz = function () {
var trajectoryStep =  new Array (this.atomCount);
var haveVibrations = (this.atomCount > 0 && this.atoms[0] != null && !Float.isNaN (this.atoms[0].z));
var vibrationStep = (haveVibrations ?  new Array (this.atomCount) : null);
var prevSteps = (this.trajectoryStepCount == 0 ? null : this.trajectorySteps.get (this.trajectoryStepCount - 1));
for (var i = 0; i < this.atomCount; i++) {
var pt = J.util.P3.newP (this.atoms[i]);
if (this.doFixPeriodic && prevSteps != null) pt = J.adapter.smarter.AtomSetCollection.fixPeriodic (pt, prevSteps[i]);
trajectoryStep[i] = pt;
if (haveVibrations) vibrationStep[i] = this.atoms[i].vib;
}
if (haveVibrations) {
if (this.vibrationSteps == null) {
this.vibrationSteps =  new J.util.JmolList ();
for (var i = 0; i < this.trajectoryStepCount; i++) this.vibrationSteps.addLast (null);

}this.vibrationSteps.addLast (vibrationStep);
}this.trajectorySteps.addLast (trajectoryStep);
this.trajectoryStepCount++;
}, $fz.isPrivate = true, $fz));
c$.fixPeriodic = $_M(c$, "fixPeriodic", 
($fz = function (pt, pt0) {
pt.x = J.adapter.smarter.AtomSetCollection.fixPoint (pt.x, pt0.x);
pt.y = J.adapter.smarter.AtomSetCollection.fixPoint (pt.y, pt0.y);
pt.z = J.adapter.smarter.AtomSetCollection.fixPoint (pt.z, pt0.z);
return pt;
}, $fz.isPrivate = true, $fz), "J.util.P3,J.util.P3");
c$.fixPoint = $_M(c$, "fixPoint", 
($fz = function (x, x0) {
while (x - x0 > 0.9) {
x -= 1;
}
while (x - x0 < -0.9) {
x += 1;
}
return x;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "finalizeTrajectoryAs", 
function (trajectorySteps, vibrationSteps) {
this.trajectorySteps = trajectorySteps;
this.vibrationSteps = vibrationSteps;
this.trajectoryStepCount = trajectorySteps.size ();
this.finalizeTrajectory ();
}, "J.util.JmolList,J.util.JmolList");
$_M(c$, "finalizeTrajectory", 
($fz = function () {
if (this.trajectoryStepCount == 0) return;
var trajectory = this.trajectorySteps.get (0);
var vibrations = (this.vibrationSteps == null ? null : this.vibrationSteps.get (0));
var v =  new J.util.V3 ();
if (this.vibrationSteps != null && vibrations != null && vibrations.length < this.atomCount || trajectory.length < this.atomCount) {
this.errorMessage = "File cannot be loaded as a trajectory";
return;
}for (var i = 0; i < this.atomCount; i++) {
if (this.vibrationSteps != null) this.atoms[i].vib = (vibrations == null ? v : vibrations[i]);
if (trajectory[i] != null) this.atoms[i].setT (trajectory[i]);
}
this.setAtomSetCollectionAuxiliaryInfo ("trajectorySteps", this.trajectorySteps);
if (this.vibrationSteps != null) this.setAtomSetCollectionAuxiliaryInfo ("vibrationSteps", this.vibrationSteps);
}, $fz.isPrivate = true, $fz));
$_M(c$, "newAtomSet", 
function () {
this.newAtomSetClear (true);
});
$_M(c$, "newAtomSetClear", 
function (doClearMap) {
if (!this.allowMultiple && this.currentAtomSetIndex >= 0) this.discardPreviousAtoms ();
this.bondIndex0 = this.bondCount;
if (this.isTrajectory) {
this.discardPreviousAtoms ();
}this.currentAtomSetIndex = this.atomSetCount++;
if (this.atomSetCount > this.atomSetNumbers.length) {
this.atomSetAtomIndexes = J.util.ArrayUtil.doubleLengthI (this.atomSetAtomIndexes);
this.atomSetAtomCounts = J.util.ArrayUtil.doubleLengthI (this.atomSetAtomCounts);
this.atomSetBondCounts = J.util.ArrayUtil.doubleLengthI (this.atomSetBondCounts);
this.atomSetAuxiliaryInfo = J.util.ArrayUtil.doubleLength (this.atomSetAuxiliaryInfo);
}this.atomSetAtomIndexes[this.currentAtomSetIndex] = this.atomCount;
if (this.atomSetCount + this.trajectoryStepCount > this.atomSetNumbers.length) {
this.atomSetNumbers = J.util.ArrayUtil.doubleLengthI (this.atomSetNumbers);
}if (this.isTrajectory) {
this.atomSetNumbers[this.currentAtomSetIndex + this.trajectoryStepCount] = this.atomSetCount + this.trajectoryStepCount;
} else {
this.atomSetNumbers[this.currentAtomSetIndex] = this.atomSetCount;
}if (doClearMap) this.atomSymbolicMap.clear ();
this.setAtomSetAuxiliaryInfo ("title", this.collectionName);
}, "~B");
$_M(c$, "getAtomSetAtomIndex", 
function (i) {
return this.atomSetAtomIndexes[i];
}, "~N");
$_M(c$, "getAtomSetAtomCount", 
function (i) {
return this.atomSetAtomCounts[i];
}, "~N");
$_M(c$, "getAtomSetBondCount", 
function (i) {
return this.atomSetBondCounts[i];
}, "~N");
$_M(c$, "setAtomSetName", 
function (atomSetName) {
if (this.isTrajectory) {
this.setTrajectoryName (atomSetName);
return;
}this.setAtomSetAuxiliaryInfoForSet ("name", atomSetName, this.currentAtomSetIndex);
if (!this.allowMultiple) this.setCollectionName (atomSetName);
}, "~S");
$_M(c$, "setTrajectoryName", 
($fz = function (name) {
if (this.trajectoryStepCount == 0) return;
if (this.trajectoryNames == null) {
this.trajectoryNames =  new J.util.JmolList ();
}for (var i = this.trajectoryNames.size (); i < this.trajectoryStepCount; i++) this.trajectoryNames.addLast (null);

this.trajectoryNames.set (this.trajectoryStepCount - 1, name);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "setAtomSetNames", 
function (atomSetName, n, namedSets) {
for (var i = this.currentAtomSetIndex; --n >= 0 && i >= 0; --i) if (namedSets == null || !namedSets.get (i)) this.setAtomSetAuxiliaryInfoForSet ("name", atomSetName, i);

}, "~S,~N,J.util.BS");
$_M(c$, "setCurrentAtomSetNumber", 
function (atomSetNumber) {
this.setAtomSetNumber (this.currentAtomSetIndex + (this.isTrajectory ? this.trajectoryStepCount : 0), atomSetNumber);
}, "~N");
$_M(c$, "setAtomSetNumber", 
function (index, atomSetNumber) {
this.atomSetNumbers[index] = atomSetNumber;
}, "~N,~N");
$_M(c$, "setAtomSetModelProperty", 
function (key, value) {
this.setAtomSetModelPropertyForSet (key, value, this.currentAtomSetIndex);
}, "~S,~S");
$_M(c$, "setAtomSetModelPropertyForSet", 
function (key, value, atomSetIndex) {
var p = this.getAtomSetAuxiliaryInfoValue (atomSetIndex, "modelProperties");
if (p == null) this.setAtomSetAuxiliaryInfoForSet ("modelProperties", p =  new java.util.Properties (), atomSetIndex);
p.put (key, value);
}, "~S,~S,~N");
$_M(c$, "setAtomSetAtomProperty", 
function (key, data, atomSetIndex) {
if (!data.endsWith ("\n")) data += "\n";
if (atomSetIndex < 0) atomSetIndex = this.currentAtomSetIndex;
var p = this.getAtomSetAuxiliaryInfoValue (atomSetIndex, "atomProperties");
if (p == null) this.setAtomSetAuxiliaryInfoForSet ("atomProperties", p =  new java.util.Hashtable (), atomSetIndex);
p.put (key, data);
}, "~S,~S,~N");
$_M(c$, "appendAtomProperties", 
($fz = function (nTimes) {
var p = this.getAtomSetAuxiliaryInfoValue (-1, "atomProperties");
if (p == null) {
return;
}for (var entry, $entry = p.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var key = entry.getKey ();
var data = entry.getValue ();
var s =  new J.util.SB ();
for (var i = nTimes; --i >= 0; ) s.append (data);

p.put (key, s.toString ());
}
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setAtomSetPartialCharges", 
function (auxKey) {
if (!this.atomSetAuxiliaryInfo[this.currentAtomSetIndex].containsKey (auxKey)) {
return false;
}var atomData = this.getAtomSetAuxiliaryInfoValue (this.currentAtomSetIndex, auxKey);
for (var i = atomData.size (); --i >= 0; ) {
this.atoms[i].partialCharge = atomData.get (i).floatValue ();
}
return true;
}, "~S");
$_M(c$, "getAtomSetAuxiliaryInfoValue", 
function (index, key) {
return this.atomSetAuxiliaryInfo[index >= 0 ? index : this.currentAtomSetIndex].get (key);
}, "~N,~S");
$_M(c$, "setAtomSetAuxiliaryInfo", 
function (key, value) {
this.setAtomSetAuxiliaryInfoForSet (key, value, this.currentAtomSetIndex);
}, "~S,~O");
$_M(c$, "setAtomSetAuxiliaryInfoForSet", 
function (key, value, atomSetIndex) {
if (atomSetIndex < 0) return;
if (this.atomSetAuxiliaryInfo[atomSetIndex] == null) this.atomSetAuxiliaryInfo[atomSetIndex] =  new java.util.Hashtable ();
if (value == null) this.atomSetAuxiliaryInfo[atomSetIndex].remove (key);
 else this.atomSetAuxiliaryInfo[atomSetIndex].put (key, value);
}, "~S,~O,~N");
$_M(c$, "setAtomSetPropertyForSets", 
function (key, value, n) {
for (var idx = this.currentAtomSetIndex; --n >= 0 && idx >= 0; --idx) this.setAtomSetModelPropertyForSet (key, value, idx);

}, "~S,~S,~N");
$_M(c$, "cloneLastAtomSetProperties", 
function () {
this.cloneAtomSetProperties (this.currentAtomSetIndex - 1);
});
$_M(c$, "cloneAtomSetProperties", 
function (index) {
var p = this.getAtomSetAuxiliaryInfoValue (index, "modelProperties");
if (p != null) this.setAtomSetAuxiliaryInfoForSet ("modelProperties", p.clone (), this.currentAtomSetIndex);
}, "~N");
$_M(c$, "getAtomSetNumber", 
function (atomSetIndex) {
return this.atomSetNumbers[atomSetIndex >= this.atomSetCount ? 0 : atomSetIndex];
}, "~N");
$_M(c$, "getAtomSetName", 
function (atomSetIndex) {
if (this.trajectoryNames != null && atomSetIndex < this.trajectoryNames.size ()) return this.trajectoryNames.get (atomSetIndex);
if (atomSetIndex >= this.atomSetCount) atomSetIndex = this.atomSetCount - 1;
return this.getAtomSetAuxiliaryInfoValue (atomSetIndex, "name");
}, "~N");
$_M(c$, "getAtomSetAuxiliaryInfo", 
function (atomSetIndex) {
return this.atomSetAuxiliaryInfo[atomSetIndex >= this.atomSetCount ? this.atomSetCount - 1 : atomSetIndex];
}, "~N");
$_M(c$, "setAtomNames", 
function (atomIdNames) {
if (atomIdNames == null) return null;
var s;
for (var i = 0; i < this.atomCount; i++) if ((s = atomIdNames.getProperty (this.atoms[i].atomName)) != null) this.atoms[i].atomName = s;

return null;
}, "java.util.Properties");
$_M(c$, "setAtomSetEnergy", 
function (energyString, value) {
if (this.currentAtomSetIndex < 0) return;
J.util.Logger.info ("Energy for model " + (this.currentAtomSetIndex + 1) + " = " + energyString);
this.setAtomSetAuxiliaryInfo ("EnergyString", energyString);
this.setAtomSetAuxiliaryInfo ("Energy", Float.$valueOf (value));
this.setAtomSetModelProperty ("Energy", "" + value);
}, "~S,~N");
$_M(c$, "setAtomSetFrequency", 
function (pathKey, label, freq, units) {
freq += " " + (units == null ? "cm^-1" : units);
var name = (label == null ? "" : label + " ") + freq;
this.setAtomSetName (name);
this.setAtomSetModelProperty ("Frequency", freq);
if (label != null) this.setAtomSetModelProperty ("FrequencyLabel", label);
this.setAtomSetModelProperty (".PATH", (pathKey == null ? "" : pathKey + J.adapter.smarter.SmarterJmolAdapter.PATH_SEPARATOR + "Frequencies") + "Frequencies");
return name;
}, "~S,~S,~S,~S");
$_M(c$, "toCartesian", 
function (symmetry) {
for (var i = this.getLastAtomSetAtomIndex (); i < this.atomCount; i++) symmetry.toCartesian (this.atoms[i], true);

}, "J.api.SymmetryInterface");
$_M(c$, "getBondList", 
function () {
var info =  new Array (this.bondCount);
for (var i = 0; i < this.bondCount; i++) {
info[i] = [this.atoms[this.bonds[i].atomIndex1].atomName, this.atoms[this.bonds[i].atomIndex2].atomName, "" + this.bonds[i].order];
}
return info;
});
$_M(c$, "centralize", 
function () {
var pt =  new J.util.P3 ();
for (var i = 0; i < this.atomSetCount; i++) {
var n = this.atomSetAtomCounts[i];
var atom0 = this.atomSetAtomIndexes[i];
pt.set (0, 0, 0);
for (var j = atom0 + n; --j >= atom0; ) {
pt.x += this.atoms[j].x;
pt.y += this.atoms[j].y;
pt.z += this.atoms[j].z;
}
pt.scale (1 / n);
for (var j = atom0 + n; --j >= atom0; ) {
this.atoms[j].x -= pt.x;
this.atoms[j].y -= pt.y;
this.atoms[j].z -= pt.z;
}
}
});
$_M(c$, "mergeTrajectories", 
function (a) {
if (!this.isTrajectory || !a.isTrajectory || this.vibrationSteps != null) return;
for (var i = 0; i < a.trajectoryStepCount; i++) this.trajectorySteps.add (this.trajectoryStepCount++, a.trajectorySteps.get (i));

this.setAtomSetCollectionAuxiliaryInfo ("trajectorySteps", this.trajectorySteps);
}, "J.adapter.smarter.AtomSetCollection");
Clazz.defineStatics (c$,
"globalBooleans", ["someModelsHaveFractionalCoordinates", "someModelsHaveSymmetry", "someModelsHaveUnitcells", "someModelsHaveCONECT", "isPDB"],
"GLOBAL_FRACTCOORD", 0,
"GLOBAL_SYMMETRY", 1,
"GLOBAL_UNITCELLS", 2,
"GLOBAL_CONECT", 3,
"GLOBAL_ISPDB", 4,
"notionalUnitcellTags", ["a", "b", "c", "alpha", "beta", "gamma"]);
});
