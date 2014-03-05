Clazz.declarePackage ("J.modelset");
Clazz.load (["J.util.BS", "$.SB"], "J.modelset.Model", ["java.util.Hashtable", "J.util.ArrayUtil", "$.BSUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.modelSet = null;
this.modelIndex = 0;
this.fileIndex = 0;
this.hydrogenCount = 0;
this.isBioModel = false;
this.isPdbWithMultipleBonds = false;
this.hasRasmolHBonds = false;
this.loadState = "";
this.loadScript = null;
this.isModelKit = false;
this.dataFrames = null;
this.dataSourceFrame = -1;
this.jmolData = null;
this.jmolFrameType = null;
this.firstAtomIndex = 0;
this.atomCount = 0;
this.bsAtoms = null;
this.bsAtomsDeleted = null;
this.trajectoryBaseIndex = 0;
this.isTrajectory = false;
this.selectedTrajectory = -1;
this.bondCount = -1;
this.firstMoleculeIndex = 0;
this.moleculeCount = 0;
this.nAltLocs = 0;
this.nInsertions = 0;
this.groupCount = -1;
this.chainCount = 0;
this.chains = null;
this.biosymmetryCount = 0;
this.auxiliaryInfo = null;
this.properties = null;
this.defaultRotationRadius = 0;
this.defaultStructure = null;
this.orientation = null;
this.structureTainted = false;
this.isJmolDataFrame = false;
this.frameDelay = 0;
this.simpleCage = null;
Clazz.instantialize (this, arguments);
}, J.modelset, "Model");
Clazz.prepareFields (c$, function () {
this.loadScript =  new J.util.SB ();
this.bsAtoms =  new J.util.BS ();
this.bsAtomsDeleted =  new J.util.BS ();
this.chains =  new Array (8);
});
$_M(c$, "getModelSet", 
function () {
return this.modelSet;
});
$_M(c$, "isModelkit", 
function () {
return this.isModelKit;
});
$_M(c$, "getTrueAtomCount", 
function () {
return this.bsAtoms.cardinality () - this.bsAtomsDeleted.cardinality ();
});
$_M(c$, "setSelectedTrajectory", 
function (i) {
this.selectedTrajectory = i;
}, "~N");
$_M(c$, "getSelectedTrajectory", 
function () {
return this.selectedTrajectory;
});
$_M(c$, "resetBoundCount", 
function () {
this.bondCount = -1;
});
$_M(c$, "getBondCount", 
function () {
if (this.bondCount >= 0) return this.bondCount;
var bonds = this.modelSet.bonds;
this.bondCount = 0;
for (var i = this.modelSet.bondCount; --i >= 0; ) if (bonds[i].atom1.modelIndex == this.modelIndex) this.bondCount++;

return this.bondCount;
});
Clazz.makeConstructor (c$, 
function (modelSet, modelIndex, trajectoryBaseIndex, jmolData, properties, auxiliaryInfo) {
this.modelSet = modelSet;
this.dataSourceFrame = this.modelIndex = modelIndex;
this.isTrajectory = (trajectoryBaseIndex >= 0);
this.trajectoryBaseIndex = (this.isTrajectory ? trajectoryBaseIndex : modelIndex);
if (auxiliaryInfo == null) {
auxiliaryInfo =  new java.util.Hashtable ();
}this.auxiliaryInfo = auxiliaryInfo;
if (auxiliaryInfo.containsKey ("biosymmetryCount")) this.biosymmetryCount = (auxiliaryInfo.get ("biosymmetryCount")).intValue ();
this.properties = properties;
if (jmolData == null) {
this.jmolFrameType = "modelSet";
} else {
this.jmolData = jmolData;
this.isJmolDataFrame = true;
auxiliaryInfo.put ("jmolData", jmolData);
auxiliaryInfo.put ("title", jmolData);
this.jmolFrameType = (jmolData.indexOf ("ramachandran") >= 0 ? "ramachandran" : jmolData.indexOf ("quaternion") >= 0 ? "quaternion" : "data");
}}, "J.modelset.ModelSet,~N,~N,~S,java.util.Properties,java.util.Map");
$_M(c$, "setNAltLocs", 
function (nAltLocs) {
this.nAltLocs = nAltLocs;
}, "~N");
$_M(c$, "setNInsertions", 
function (nInsertions) {
this.nInsertions = nInsertions;
}, "~N");
$_M(c$, "getModelNumberDotted", 
function () {
return this.modelSet.getModelNumberDotted (this.modelIndex);
});
$_M(c$, "getModelTitle", 
function () {
return this.modelSet.getModelTitle (this.modelIndex);
});
$_M(c$, "isStructureTainted", 
function () {
return this.structureTainted;
});
$_M(c$, "getChains", 
function () {
return this.chains;
});
$_M(c$, "getChainCount", 
function (countWater) {
if (this.chainCount > 1 && !countWater) for (var i = 0; i < this.chainCount; i++) if (this.chains[i].chainID == 0) return this.chainCount - 1;

return this.chainCount;
}, "~B");
$_M(c$, "getGroupCountHetero", 
function (isHetero) {
var n = 0;
for (var i = this.chainCount; --i >= 0; ) for (var j = this.chains[i].groupCount; --j >= 0; ) if (this.chains[i].groups[j].isHetero () == isHetero) n++;


return n;
}, "~B");
$_M(c$, "calcSelectedGroupsCount", 
function (bsSelected) {
for (var i = this.chainCount; --i >= 0; ) this.chains[i].calcSelectedGroupsCount (bsSelected);

}, "J.util.BS");
$_M(c$, "getGroupCount", 
function () {
if (this.groupCount < 0) {
this.groupCount = 0;
for (var i = this.chainCount; --i >= 0; ) this.groupCount += this.chains[i].getGroupCount ();

}return this.groupCount;
});
$_M(c$, "getChainAt", 
function (i) {
return (i < this.chainCount ? this.chains[i] : null);
}, "~N");
$_M(c$, "getChain", 
function (chainID) {
for (var i = this.chainCount; --i >= 0; ) {
var chain = this.chains[i];
if (chain.chainID == chainID) return chain;
}
return null;
}, "~N");
$_M(c$, "fixIndices", 
function (modelIndex, nAtomsDeleted, bsDeleted) {
this.fixIndicesM (modelIndex, nAtomsDeleted, bsDeleted);
}, "~N,~N,J.util.BS");
$_M(c$, "fixIndicesM", 
function (modelIndex, nAtomsDeleted, bsDeleted) {
if (this.dataSourceFrame > modelIndex) this.dataSourceFrame--;
if (this.trajectoryBaseIndex > modelIndex) this.trajectoryBaseIndex--;
this.firstAtomIndex -= nAtomsDeleted;
for (var i = 0; i < this.chainCount; i++) this.chains[i].fixIndices (nAtomsDeleted, bsDeleted);

J.util.BSUtil.deleteBits (this.bsAtoms, bsDeleted);
J.util.BSUtil.deleteBits (this.bsAtomsDeleted, bsDeleted);
}, "~N,~N,J.util.BS");
$_M(c$, "freeze", 
function () {
this.freezeM ();
});
$_M(c$, "freezeM", 
function () {
this.chains = J.util.ArrayUtil.arrayCopyObject (this.chains, this.chainCount);
this.groupCount = -1;
this.getGroupCount ();
for (var i = 0; i < this.chainCount; ++i) this.chains[i].groups = J.util.ArrayUtil.arrayCopyObject (this.chains[i].groups, this.chains[i].groupCount);

});
$_M(c$, "getPdbData", 
function (viewer, type, ctype, isDraw, bsSelected, sb, tokens, pdbCONECT, bsWritten) {
}, "J.viewer.Viewer,~S,~S,~B,J.util.BS,J.io.OutputStringBuilder,~A,J.util.SB,J.util.BS");
$_M(c$, "getDefaultLargePDBRendering", 
function (sb, maxAtoms) {
}, "J.util.SB,~N");
$_M(c$, "getBioBranches", 
function (bioBranches) {
return bioBranches;
}, "J.util.JmolList");
$_M(c$, "getGroupsWithin", 
function (nResidues, bs, bsResult) {
}, "~N,J.util.BS,J.util.BS");
$_M(c$, "getSequenceBits", 
function (specInfo, bs, bsResult) {
}, "~S,J.util.BS,J.util.BS");
$_M(c$, "getRasmolHydrogenBonds", 
function (bsA, bsB, vHBonds, nucleicOnly, nMax, dsspIgnoreHydrogens, bsHBonds) {
}, "J.util.BS,J.util.BS,J.util.JmolList,~B,~N,~B,J.util.BS");
$_M(c$, "clearRasmolHydrogenBonds", 
function (bsAtoms) {
}, "J.util.BS");
$_M(c$, "clearBioPolymers", 
function () {
});
$_M(c$, "calcSelectedMonomersCount", 
function (bsSelected) {
}, "J.util.BS");
$_M(c$, "calculatePolymers", 
function (groups, groupCount, baseGroupIndex, modelsExcluded, checkConnections) {
}, "~A,~N,~N,J.util.BS,~B");
$_M(c$, "getAllPolymerInfo", 
function (bs, finalInfo, modelVector) {
}, "J.util.BS,java.util.Map,J.util.JmolList");
$_M(c$, "getBioPolymerCount", 
function () {
return 0;
});
$_M(c$, "getPolymerPointsAndVectors", 
function (bs, vList, isTraceAlpha, sheetSmoothing) {
}, "J.util.BS,J.util.JmolList,~B,~N");
$_M(c$, "getPolymerLeadMidPoints", 
function (iPolymer) {
return null;
}, "~N");
$_M(c$, "recalculateLeadMidpointsAndWingVectors", 
function () {
});
$_M(c$, "calculateStructures", 
function (asDSSP, doReport, dsspIgnoreHydrogen, setStructure, includeAlpha) {
return "";
}, "~B,~B,~B,~B,~B");
$_M(c$, "setStructureList", 
function (structureList) {
}, "java.util.Map");
$_M(c$, "getChimeInfo", 
function (sb, nHetero) {
this.getChimeInfoM (sb, nHetero);
}, "J.util.SB,~N");
$_M(c$, "getChimeInfoM", 
function (sb, nHetero) {
sb.append ("\nNumber of Atoms ..... " + (this.modelSet.atomCount - nHetero));
if (nHetero > 0) sb.append (" (" + nHetero + ")");
sb.append ("\nNumber of Bonds ..... " + this.modelSet.bondCount);
sb.append ("\nNumber of Models ...... " + this.modelSet.modelCount);
}, "J.util.SB,~N");
$_M(c$, "calculateStruts", 
function (modelSet, bs1, bs2) {
return 0;
}, "J.modelset.ModelSet,J.util.BS,J.util.BS");
$_M(c$, "calculateStraightness", 
function (viewer, ctype, qtype, mStep) {
}, "J.viewer.Viewer,~S,~S,~N");
$_M(c$, "selectSeqcodeRange", 
function (seqcodeA, seqcodeB, chainID, bs, caseSensitive) {
}, "~N,~N,~N,J.util.BS,~B");
$_M(c$, "setConformation", 
function (bsConformation) {
}, "J.util.BS");
$_M(c$, "getPdbConformation", 
function (bsConformation, conformationIndex) {
return false;
}, "J.util.BS,~N");
$_M(c$, "getProteinStructureState", 
function (bsAtoms, taintedOnly, needPhiPsi, mode) {
return null;
}, "J.util.BS,~B,~B,~N");
$_M(c$, "getFullPDBHeader", 
function () {
return null;
});
});
