Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelset.Group"], "J.modelsetbio.Monomer", ["java.lang.Float", "J.constant.EnumStructure", "J.util.Logger", "$.Measure", "$.P3", "$.Quaternion", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bioPolymer = null;
this.offsets = null;
this.monomerIndex = 0;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "Monomer", J.modelset.Group);
c$.have = $_M(c$, "have", 
function (offsets, n) {
return (offsets[n] & 0xFF) != 0xFF;
}, "~A,~N");
$_M(c$, "set2", 
function (chain, group3, seqcode, firstAtomIndex, lastAtomIndex, interestingAtomOffsets) {
this.setGroup (chain, group3, seqcode, firstAtomIndex, lastAtomIndex);
this.offsets = interestingAtomOffsets;
var offset = this.offsets[0] & 0xFF;
if (offset != 255) this.leadAtomIndex = firstAtomIndex + offset;
return this;
}, "J.modelset.Chain,~S,~N,~N,~N,~A");
Clazz.overrideMethod (c$, "getGroups", 
function () {
return this.bioPolymer.getGroups ();
});
$_M(c$, "setBioPolymer", 
function (polymer, index) {
this.bioPolymer = polymer;
this.monomerIndex = index;
}, "J.modelsetbio.BioPolymer,~N");
Clazz.overrideMethod (c$, "getSelectedMonomerCount", 
function () {
return this.bioPolymer.getSelectedMonomerCount ();
});
Clazz.overrideMethod (c$, "getSelectedMonomerIndex", 
function () {
return (this.monomerIndex >= 0 && this.bioPolymer.isMonomerSelected (this.monomerIndex) ? this.monomerIndex : -1);
});
$_M(c$, "getBioPolymer", 
function () {
return this.bioPolymer;
});
Clazz.overrideMethod (c$, "getBioPolymerLength", 
function () {
return this.bioPolymer == null ? 0 : this.bioPolymer.monomerCount;
});
$_M(c$, "getMonomerIndex", 
function () {
return this.monomerIndex;
});
$_M(c$, "getBioPolymerIndexInModel", 
function () {
return (this.bioPolymer == null ? -1 : this.bioPolymer.bioPolymerIndexInModel);
});
c$.scanForOffsets = $_M(c$, "scanForOffsets", 
function (firstAtomIndex, specialAtomIndexes, interestingAtomIDs) {
var interestingCount = interestingAtomIDs.length;
var offsets =  Clazz.newByteArray (interestingCount, 0);
for (var i = interestingCount; --i >= 0; ) {
var atomIndex;
var atomID = interestingAtomIDs[i];
if (atomID < 0) {
atomIndex = specialAtomIndexes[~atomID];
} else {
atomIndex = specialAtomIndexes[atomID];
if (atomIndex < 0) return null;
}var offset;
if (atomIndex < 0) offset = 255;
 else {
offset = atomIndex - firstAtomIndex;
if (offset < 0 || offset > 254) {
J.util.Logger.warn ("Monomer.scanForOffsets i=" + i + " atomID=" + atomID + " atomIndex:" + atomIndex + " firstAtomIndex:" + firstAtomIndex + " offset out of 0-254 range. Groups aren't organized correctly. Is this really a protein?: " + offset);
if (atomID < 0) {
offset = 255;
} else {
}}}offsets[i] = offset;
}
return offsets;
}, "~N,~A,~A");
$_M(c$, "setStructure", 
function (proteinstructure) {
}, "J.modelsetbio.ProteinStructure");
$_M(c$, "getProteinStructure", 
function () {
return null;
});
Clazz.overrideMethod (c$, "getProteinStructureType", 
function () {
return J.constant.EnumStructure.NONE;
});
$_M(c$, "isHelix", 
function () {
return false;
});
$_M(c$, "isSheet", 
function () {
return false;
});
Clazz.overrideMethod (c$, "setStrucNo", 
function (id) {
}, "~N");
$_M(c$, "getAtomFromOffsetIndex", 
function (offsetIndex) {
if (offsetIndex > this.offsets.length) return null;
var offset = this.offsets[offsetIndex] & 0xFF;
if (offset == 255) return null;
return this.chain.getAtom (this.firstAtomIndex + offset);
}, "~N");
$_M(c$, "getSpecialAtom", 
function (interestingIDs, specialAtomID) {
for (var i = interestingIDs.length; --i >= 0; ) {
var interestingID = interestingIDs[i];
if (interestingID < 0) interestingID = -interestingID;
if (specialAtomID == interestingID) {
var offset = this.offsets[i] & 0xFF;
if (offset == 255) return null;
return this.chain.getAtom (this.firstAtomIndex + offset);
}}
return null;
}, "~A,~N");
$_M(c$, "getSpecialAtomPoint", 
function (interestingIDs, specialAtomID) {
for (var i = interestingIDs.length; --i >= 0; ) {
var interestingID = interestingIDs[i];
if (interestingID < 0) interestingID = -interestingID;
if (specialAtomID == interestingID) {
var offset = this.offsets[i] & 0xFF;
if (offset == 255) return null;
return this.chain.getAtom (this.firstAtomIndex + offset);
}}
return null;
}, "~A,~N");
Clazz.overrideMethod (c$, "isLeadAtom", 
function (atomIndex) {
return atomIndex == this.leadAtomIndex;
}, "~N");
Clazz.overrideMethod (c$, "getLeadAtom", 
function () {
return this.getAtomFromOffsetIndex (0);
});
$_M(c$, "getWingAtom", 
function () {
return this.getAtomFromOffsetIndex (1);
});
$_M(c$, "getInitiatorAtom", 
function () {
return this.getLeadAtom ();
});
$_M(c$, "getTerminatorAtom", 
function () {
return this.getLeadAtom ();
});
$_M(c$, "findNearestAtomIndex", 
function (x, y, closest, madBegin, madEnd) {
}, "~N,~N,~A,~N,~N");
Clazz.overrideMethod (c$, "calcBioParameters", 
function () {
return this.bioPolymer.calcParameters ();
});
Clazz.overrideMethod (c$, "haveParameters", 
function () {
return this.bioPolymer.haveParameters;
});
$_M(c$, "getMyInfo", 
function () {
var info = this.getGroupInfo (this.groupIndex);
info.put ("chain", this.chain.getIDStr ());
var seqNum = this.getResno ();
if (seqNum > 0) info.put ("sequenceNumber", Integer.$valueOf (seqNum));
var insCode = this.getInsertionCode ();
if (insCode.charCodeAt (0) != 0) info.put ("insertionCode", "" + insCode);
var f = this.getGroupParameter (1112539145);
if (!Float.isNaN (f)) info.put ("phi", Float.$valueOf (f));
f = this.getGroupParameter (1112539146);
if (!Float.isNaN (f)) info.put ("psi", Float.$valueOf (f));
f = this.getGroupParameter (1112539141);
if (!Float.isNaN (f)) info.put ("mu", Float.$valueOf (f));
f = this.getGroupParameter (1112539152);
if (!Float.isNaN (f)) info.put ("theta", Float.$valueOf (f));
var structure = this.getProteinStructure ();
if (structure != null) {
info.put ("structureId", Integer.$valueOf (structure.strucNo));
info.put ("structureType", structure.type.getBioStructureTypeName (false));
}info.put ("shapeVisibilityFlags", Integer.$valueOf (this.shapeVisibilityFlags));
return info;
});
Clazz.overrideMethod (c$, "getStructureId", 
function () {
var structure = this.getProteinStructure ();
return (structure == null ? "" : structure.type.getBioStructureTypeName (false));
});
$_M(c$, "getConformation", 
function (atoms, bsConformation, conformationIndex) {
var ch = '\u0000';
for (var i = this.firstAtomIndex; i <= this.lastAtomIndex; i++) {
var atom = atoms[i];
var altloc = atom.getAlternateLocationID ();
if (altloc == '\0') continue;
if (conformationIndex >= 0 && altloc != ch) {
ch = altloc;
conformationIndex--;
}if (conformationIndex < 0 && altloc != ch) bsConformation.clear (i);
}
}, "~A,J.util.BS,~N");
$_M(c$, "updateOffsetsForAlternativeLocations", 
function (atoms, bsSelected) {
for (var offsetIndex = this.offsets.length; --offsetIndex >= 0; ) {
var offset = this.offsets[offsetIndex] & 0xFF;
if (offset == 255) continue;
var iThis = this.firstAtomIndex + offset;
var atom = atoms[iThis];
var thisID = atom.getAtomID ();
if ((atom.getAlternateLocationID ()).charCodeAt (0) == 0) continue;
var nScan = this.lastAtomIndex - this.firstAtomIndex;
for (var i = 1; i <= nScan; i++) {
var iNew = iThis + i;
if (iNew > this.lastAtomIndex) iNew -= nScan + 1;
var offsetNew = iNew - this.firstAtomIndex;
if (offsetNew < 0 || offsetNew > 255 || iNew == iThis || !bsSelected.get (iNew)) continue;
var atomID = atoms[iNew].getAtomID ();
if (atomID != thisID || atomID == 0 && !atoms[iNew].getAtomName ().equals (atom.getAtomName ())) continue;
if (J.util.Logger.debugging) J.util.Logger.debug ("Chain.udateOffsetsForAlternativeLocation " + atoms[iNew] + " was " + atom);
this.offsets[offsetIndex] = offsetNew;
break;
}
}
}, "~A,J.util.BS");
$_M(c$, "getMonomerSequenceAtoms", 
function (bsInclude, bsResult) {
this.selectAtoms (bsResult);
bsResult.and (bsInclude);
}, "J.util.BS,J.util.BS");
c$.checkOptional = $_M(c$, "checkOptional", 
function (offsets, atom, firstAtomIndex, index) {
if (J.modelsetbio.Monomer.have (offsets, atom)) return true;
if (index < 0) return false;
offsets[atom] = (index - firstAtomIndex);
return true;
}, "~A,~N,~N,~N");
$_M(c$, "getQuaternionFrameCenter", 
function (qtype) {
return null;
}, "~S");
$_M(c$, "getHelixData2", 
function (tokType, qType, mStep) {
var iPrev = this.monomerIndex - mStep;
var prev = (mStep < 1 || this.monomerIndex <= 0 ? null : this.bioPolymer.monomers[iPrev]);
var q2 = this.getQuaternion (qType);
var q1 = (mStep < 1 ? J.util.Quaternion.getQuaternionFrameV (J.viewer.JC.axisX, J.viewer.JC.axisY, J.viewer.JC.axisZ, false) : prev == null ? null : prev.getQuaternion (qType));
if (q1 == null || q2 == null) return this.getHelixData (tokType, qType, mStep);
var a = (mStep < 1 ? J.util.P3.new3 (0, 0, 0) : prev.getQuaternionFrameCenter (qType));
var b = this.getQuaternionFrameCenter (qType);
if (a == null || b == null) return this.getHelixData (tokType, qType, mStep);
return J.util.Measure.computeHelicalAxis (tokType == 135176 ? "helixaxis" + this.getUniqueID () : null, tokType, a, b, q2.div (q1));
}, "~N,~S,~N");
$_M(c$, "getUniqueID", 
function () {
var cid = this.getChainID ();
var a = this.getLeadAtom ();
var id = (a == null ? "" : "_" + a.getModelIndex ()) + "_" + this.getResno () + (cid == 0 ? "" : "_" + cid);
var aid = (a == null ? '\0' : this.getLeadAtom ().getAlternateLocationID ());
if (aid != '\0') id += "_" + aid;
return id;
});
Clazz.overrideMethod (c$, "isCrossLinked", 
function (g) {
for (var i = this.firstAtomIndex; i <= this.lastAtomIndex; i++) if (this.getCrossLinkGroup (i, null, g)) return true;

return false;
}, "J.modelset.Group");
Clazz.overrideMethod (c$, "getCrossLinkLead", 
function (vReturn) {
for (var i = this.firstAtomIndex; i <= this.lastAtomIndex; i++) if (this.getCrossLink (i, vReturn) && vReturn == null) return true;

return false;
}, "J.util.JmolList");
$_M(c$, "getCrossLink", 
function (i, vReturn) {
return this.getCrossLinkGroup (i, vReturn, null);
}, "~N,J.util.JmolList");
$_M(c$, "getCrossLinkGroup", 
($fz = function (i, vReturn, group) {
var atom = this.chain.getAtom (i);
var bonds = atom.getBonds ();
var ibp = this.getBioPolymerIndexInModel ();
if (ibp < 0 || bonds == null) return false;
var haveCrossLink = false;
var checkPrevious = (vReturn == null && group == null);
for (var j = 0; j < bonds.length; j++) {
var a = bonds[j].getOtherAtom (atom);
var g = a.getGroup ();
if (group != null && g !== group) continue;
var iPolymer = g.getBioPolymerIndexInModel ();
var igroup = g.getMonomerIndex ();
if (checkPrevious) {
if (iPolymer == ibp && igroup == this.monomerIndex - 1) return true;
} else if (iPolymer >= 0 && igroup >= 0 && (iPolymer != ibp || igroup < this.monomerIndex - 1 || igroup > this.monomerIndex + 1)) {
haveCrossLink = true;
if (group != null) break;
vReturn.addLast (Integer.$valueOf (g.leadAtomIndex));
}}
return haveCrossLink;
}, $fz.isPrivate = true, $fz), "~N,J.util.JmolList,J.modelset.Group");
Clazz.overrideMethod (c$, "isConnectedPrevious", 
function () {
return true;
});
});
