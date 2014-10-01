Clazz.declarePackage ("JM");
Clazz.load (["JU.BS", "$.SB"], "JM.Model", ["java.util.Hashtable", "JU.AU", "JU.BSUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.ms = null;
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
this.ac = 0;
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
this.biosymmetry = null;
this.orientation = null;
this.structureTainted = false;
this.isJmolDataFrame = false;
this.frameDelay = 0;
this.simpleCage = null;
this.dssrCache = null;
Clazz.instantialize (this, arguments);
}, JM, "Model");
Clazz.prepareFields (c$, function () {
this.loadScript =  new JU.SB ();
this.bsAtoms =  new JU.BS ();
this.bsAtomsDeleted =  new JU.BS ();
this.chains =  new Array (8);
});
Clazz.defineMethod (c$, "getModelSet", 
function () {
return this.ms;
});
Clazz.defineMethod (c$, "isModelkit", 
function () {
return this.isModelKit;
});
Clazz.defineMethod (c$, "getTrueAtomCount", 
function () {
return this.bsAtoms.cardinality () - this.bsAtomsDeleted.cardinality ();
});
Clazz.defineMethod (c$, "setSelectedTrajectory", 
function (i) {
this.selectedTrajectory = i;
}, "~N");
Clazz.defineMethod (c$, "getSelectedTrajectory", 
function () {
return this.selectedTrajectory;
});
Clazz.defineMethod (c$, "resetBoundCount", 
function () {
this.bondCount = -1;
});
Clazz.defineMethod (c$, "getBondCount", 
function () {
if (this.bondCount >= 0) return this.bondCount;
var bonds = this.ms.bo;
this.bondCount = 0;
for (var i = this.ms.bondCount; --i >= 0; ) if (bonds[i].atom1.mi == this.modelIndex) this.bondCount++;

return this.bondCount;
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.defineMethod (c$, "set", 
function (modelSet, modelIndex, trajectoryBaseIndex, jmolData, properties, auxiliaryInfo) {
this.ms = modelSet;
this.dataSourceFrame = this.modelIndex = modelIndex;
this.isTrajectory = (trajectoryBaseIndex >= 0);
this.trajectoryBaseIndex = (this.isTrajectory ? trajectoryBaseIndex : modelIndex);
if (auxiliaryInfo == null) {
auxiliaryInfo =  new java.util.Hashtable ();
}this.auxiliaryInfo = auxiliaryInfo;
if (auxiliaryInfo.containsKey ("biosymmetryCount")) {
this.biosymmetryCount = (auxiliaryInfo.get ("biosymmetryCount")).intValue ();
this.biosymmetry = auxiliaryInfo.get ("biosymmetry");
}this.properties = properties;
if (jmolData == null) {
this.jmolFrameType = "modelSet";
} else {
this.jmolData = jmolData;
this.isJmolDataFrame = true;
auxiliaryInfo.put ("jmolData", jmolData);
auxiliaryInfo.put ("title", jmolData);
this.jmolFrameType = (jmolData.indexOf ("ramachandran") >= 0 ? "ramachandran" : jmolData.indexOf ("quaternion") >= 0 ? "quaternion" : "data");
}return this;
}, "JM.ModelSet,~N,~N,~S,java.util.Properties,java.util.Map");
Clazz.defineMethod (c$, "setNAltLocs", 
function (nAltLocs) {
this.nAltLocs = nAltLocs;
}, "~N");
Clazz.defineMethod (c$, "setNInsertions", 
function (nInsertions) {
this.nInsertions = nInsertions;
}, "~N");
Clazz.defineMethod (c$, "getModelNumberDotted", 
function () {
return this.ms.getModelNumberDotted (this.modelIndex);
});
Clazz.defineMethod (c$, "getModelTitle", 
function () {
return this.ms.getModelTitle (this.modelIndex);
});
Clazz.defineMethod (c$, "isStructureTainted", 
function () {
return this.structureTainted;
});
Clazz.defineMethod (c$, "getChains", 
function () {
return this.chains;
});
Clazz.defineMethod (c$, "getChainCount", 
function (countWater) {
if (this.chainCount > 1 && !countWater) for (var i = 0; i < this.chainCount; i++) if (this.chains[i].chainID == 0) return this.chainCount - 1;

return this.chainCount;
}, "~B");
Clazz.defineMethod (c$, "getGroupCountHetero", 
function (isHetero) {
var n = 0;
for (var i = this.chainCount; --i >= 0; ) for (var j = this.chains[i].groupCount; --j >= 0; ) if (this.chains[i].groups[j].isHetero () == isHetero) n++;


return n;
}, "~B");
Clazz.defineMethod (c$, "calcSelectedGroupsCount", 
function (bsSelected) {
for (var i = this.chainCount; --i >= 0; ) this.chains[i].calcSelectedGroupsCount (bsSelected);

}, "JU.BS");
Clazz.defineMethod (c$, "getGroupCount", 
function () {
if (this.groupCount < 0) {
this.groupCount = 0;
for (var i = this.chainCount; --i >= 0; ) this.groupCount += this.chains[i].getGroupCount ();

}return this.groupCount;
});
Clazz.defineMethod (c$, "getChainAt", 
function (i) {
return (i < this.chainCount ? this.chains[i] : null);
}, "~N");
Clazz.defineMethod (c$, "getChain", 
function (chainID) {
for (var i = this.chainCount; --i >= 0; ) {
var chain = this.chains[i];
if (chain.chainID == chainID) return chain;
}
return null;
}, "~N");
Clazz.defineMethod (c$, "fixIndices", 
function (modelIndex, nAtomsDeleted, bsDeleted) {
this.fixIndicesM (modelIndex, nAtomsDeleted, bsDeleted);
}, "~N,~N,JU.BS");
Clazz.defineMethod (c$, "fixIndicesM", 
function (modelIndex, nAtomsDeleted, bsDeleted) {
if (this.dataSourceFrame > modelIndex) this.dataSourceFrame--;
if (this.trajectoryBaseIndex > modelIndex) this.trajectoryBaseIndex--;
this.firstAtomIndex -= nAtomsDeleted;
for (var i = 0; i < this.chainCount; i++) this.chains[i].fixIndices (nAtomsDeleted, bsDeleted);

JU.BSUtil.deleteBits (this.bsAtoms, bsDeleted);
JU.BSUtil.deleteBits (this.bsAtomsDeleted, bsDeleted);
}, "~N,~N,JU.BS");
Clazz.defineMethod (c$, "freeze", 
function () {
this.freezeM ();
});
Clazz.defineMethod (c$, "freezeM", 
function () {
this.chains = JU.AU.arrayCopyObject (this.chains, this.chainCount);
this.groupCount = -1;
this.getGroupCount ();
for (var i = 0; i < this.chainCount; ++i) this.chains[i].groups = JU.AU.arrayCopyObject (this.chains[i].groups, this.chains[i].groupCount);

});
Clazz.defineMethod (c$, "getPdbData", 
function (vwr, type, ctype, isDraw, bsSelected, out, tokens, pdbCONECT, bsWritten) {
}, "JV.Viewer,~S,~S,~B,JU.BS,JU.OC,~A,JU.SB,JU.BS");
Clazz.defineMethod (c$, "getDefaultLargePDBRendering", 
function (sb, maxAtoms) {
}, "JU.SB,~N");
Clazz.defineMethod (c$, "getBioBranches", 
function (bioBranches) {
return bioBranches;
}, "JU.Lst");
Clazz.defineMethod (c$, "getGroupsWithin", 
function (nResidues, bs, bsResult) {
}, "~N,JU.BS,JU.BS");
Clazz.defineMethod (c$, "getSequenceBits", 
function (specInfo, bs, bsResult) {
}, "~S,JU.BS,JU.BS");
Clazz.defineMethod (c$, "getRasmolHydrogenBonds", 
function (bsA, bsB, vHBonds, nucleicOnly, nMax, dsspIgnoreHydrogens, bsHBonds) {
}, "JU.BS,JU.BS,JU.Lst,~B,~N,~B,JU.BS");
Clazz.defineMethod (c$, "clearRasmolHydrogenBonds", 
function (bsAtoms) {
}, "JU.BS");
Clazz.defineMethod (c$, "clearBioPolymers", 
function () {
});
Clazz.defineMethod (c$, "calcSelectedMonomersCount", 
function (bsSelected) {
}, "JU.BS");
Clazz.defineMethod (c$, "calculatePolymers", 
function (groups, groupCount, baseGroupIndex, modelsExcluded, checkConnections) {
}, "~A,~N,~N,JU.BS,~B");
Clazz.defineMethod (c$, "getAllPolymerInfo", 
function (bs, finalInfo, modelVector) {
}, "JU.BS,java.util.Map,JU.Lst");
Clazz.defineMethod (c$, "getBioPolymerCount", 
function () {
return 0;
});
Clazz.defineMethod (c$, "getPolymerPointsAndVectors", 
function (bs, vList, isTraceAlpha, sheetSmoothing) {
}, "JU.BS,JU.Lst,~B,~N");
Clazz.defineMethod (c$, "getPolymerLeadMidPoints", 
function (iPolymer) {
return null;
}, "~N");
Clazz.defineMethod (c$, "recalculateLeadMidpointsAndWingVectors", 
function () {
});
Clazz.defineMethod (c$, "calculateStructures", 
function (asDSSP, doReport, dsspIgnoreHydrogen, setStructure, includeAlpha) {
return "";
}, "~B,~B,~B,~B,~B");
Clazz.defineMethod (c$, "setStructureList", 
function (structureList) {
}, "java.util.Map");
Clazz.defineMethod (c$, "getChimeInfo", 
function (sb, nHetero) {
this.getChimeInfoM (sb, nHetero);
}, "JU.SB,~N");
Clazz.defineMethod (c$, "getChimeInfoM", 
function (sb, nHetero) {
sb.append ("\nNumber of Atoms ..... " + (this.ms.ac - nHetero));
if (nHetero > 0) sb.append (" (" + nHetero + ")");
sb.append ("\nNumber of Bonds ..... " + this.ms.bondCount);
sb.append ("\nNumber of Models ...... " + this.ms.mc);
}, "JU.SB,~N");
Clazz.defineMethod (c$, "calculateStruts", 
function (modelSet, bs1, bs2) {
return 0;
}, "JM.ModelSet,JU.BS,JU.BS");
Clazz.defineMethod (c$, "calculateStraightness", 
function (vwr, ctype, qtype, mStep) {
}, "JV.Viewer,~S,~S,~N");
Clazz.defineMethod (c$, "selectSeqcodeRange", 
function (seqcodeA, seqcodeB, chainID, bs, caseSensitive) {
}, "~N,~N,~N,JU.BS,~B");
Clazz.defineMethod (c$, "setConformation", 
function (bsConformation) {
}, "JU.BS");
Clazz.defineMethod (c$, "getPdbConformation", 
function (bsConformation, conformationIndex) {
return false;
}, "JU.BS,~N");
Clazz.defineMethod (c$, "getProteinStructureState", 
function (bsAtoms, taintedOnly, needPhiPsi, mode) {
return null;
}, "JU.BS,~B,~B,~N");
Clazz.defineMethod (c$, "getFullPDBHeader", 
function () {
return null;
});
});
