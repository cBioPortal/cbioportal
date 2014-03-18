Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelset.Model"], "J.modelsetbio.BioModel", ["java.lang.Float", "java.util.Hashtable", "J.constant.EnumStructure", "J.modelset.AtomCollection", "J.modelsetbio.Resolver", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.Escape", "$.JmolList", "$.SB", "$.TextFormat", "J.viewer.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bioPolymerCount = 0;
this.bioPolymers = null;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "BioModel", J.modelset.Model);
Clazz.makeConstructor (c$, 
function (modelSet, modelIndex, trajectoryBaseIndex, jmolData, properties, auxiliaryInfo) {
Clazz.superConstructor (this, J.modelsetbio.BioModel, [modelSet, modelIndex, trajectoryBaseIndex, jmolData, properties, auxiliaryInfo]);
this.isBioModel = true;
this.clearBioPolymers ();
}, "J.modelset.ModelSet,~N,~N,~S,java.util.Properties,java.util.Map");
Clazz.overrideMethod (c$, "freeze", 
function () {
this.freezeM ();
this.bioPolymers = J.util.ArrayUtil.arrayCopyObject (this.bioPolymers, this.bioPolymerCount);
});
$_M(c$, "addSecondaryStructure", 
function (type, structureID, serialID, strandCount, startChainID, startSeqcode, endChainID, endSeqcode, istart, iend, bsAssigned) {
for (var i = this.bioPolymerCount; --i >= 0; ) if (Clazz.instanceOf (this.bioPolymers[i], J.modelsetbio.AlphaPolymer)) (this.bioPolymers[i]).addStructure (type, structureID, serialID, strandCount, startChainID, startSeqcode, endChainID, endSeqcode, istart, iend, bsAssigned);

}, "J.constant.EnumStructure,~S,~N,~N,~N,~N,~N,~N,~N,~N,J.util.BS");
Clazz.overrideMethod (c$, "calculateStructures", 
function (asDSSP, doReport, dsspIgnoreHydrogen, setStructure, includeAlpha) {
if (this.bioPolymerCount == 0 || !setStructure && !asDSSP) return "";
this.modelSet.proteinStructureTainted = this.structureTainted = true;
if (setStructure) for (var i = this.bioPolymerCount; --i >= 0; ) if (!asDSSP || this.bioPolymers[i].getGroups ()[0].getNitrogenAtom () != null) this.bioPolymers[i].clearStructures ();

if (!asDSSP || includeAlpha) for (var i = this.bioPolymerCount; --i >= 0; ) if (Clazz.instanceOf (this.bioPolymers[i], J.modelsetbio.AlphaPolymer)) (this.bioPolymers[i]).calculateStructures (includeAlpha);

return (asDSSP ? this.bioPolymers[0].calculateDssp (this.bioPolymers, this.bioPolymerCount, null, doReport, dsspIgnoreHydrogen, setStructure) : "");
}, "~B,~B,~B,~B,~B");
Clazz.overrideMethod (c$, "setConformation", 
function (bsConformation) {
if (this.nAltLocs > 0) for (var i = this.bioPolymerCount; --i >= 0; ) this.bioPolymers[i].setConformation (bsConformation);

}, "J.util.BS");
Clazz.overrideMethod (c$, "getPdbConformation", 
function (bsConformation, conformationIndex) {
if (this.nAltLocs > 0) for (var i = this.bioPolymerCount; --i >= 0; ) this.bioPolymers[i].getConformation (bsConformation, conformationIndex);

return true;
}, "J.util.BS,~N");
Clazz.overrideMethod (c$, "getBioPolymerCount", 
function () {
return this.bioPolymerCount;
});
Clazz.overrideMethod (c$, "calcSelectedMonomersCount", 
function (bsSelected) {
for (var i = this.bioPolymerCount; --i >= 0; ) this.bioPolymers[i].calcSelectedMonomersCount (bsSelected);

}, "J.util.BS");
$_M(c$, "getBioPolymer", 
function (polymerIndex) {
return this.bioPolymers[polymerIndex];
}, "~N");
Clazz.overrideMethod (c$, "getDefaultLargePDBRendering", 
function (sb, maxAtoms) {
var bs =  new J.util.BS ();
if (this.getBondCount () == 0) bs = this.bsAtoms;
if (bs !== this.bsAtoms) for (var i = 0; i < this.bioPolymerCount; i++) this.bioPolymers[i].getRange (bs);

if (bs.nextSetBit (0) < 0) return;
var bs2 =  new J.util.BS ();
if (bs === this.bsAtoms) {
bs2 = bs;
} else {
for (var i = 0; i < this.bioPolymerCount; i++) if (this.bioPolymers[i].getType () == 0) this.bioPolymers[i].getRange (bs2);

}if (bs2.nextSetBit (0) >= 0) sb.append ("select ").append (J.util.Escape.eBS (bs2)).append (";backbone only;");
if (this.atomCount <= maxAtoms) return;
sb.append ("select ").append (J.util.Escape.eBS (bs)).append (" & connected; wireframe only;");
if (bs !== this.bsAtoms) {
bs2.clearAll ();
bs2.or (this.bsAtoms);
bs2.andNot (bs);
if (bs2.nextSetBit (0) >= 0) sb.append ("select " + J.util.Escape.eBS (bs2) + " & !connected;stars 0.5;");
}}, "J.util.SB,~N");
Clazz.overrideMethod (c$, "fixIndices", 
function (modelIndex, nAtomsDeleted, bsDeleted) {
this.fixIndicesM (modelIndex, nAtomsDeleted, bsDeleted);
for (var i = 0; i < this.bioPolymerCount; i++) this.bioPolymers[i].recalculateLeadMidpointsAndWingVectors ();

}, "~N,~N,J.util.BS");
Clazz.overrideMethod (c$, "calculateStruts", 
function (modelSet, bs1, bs2) {
var vCA =  new J.util.JmolList ();
var a1 = null;
var bsCheck;
if (bs1.equals (bs2)) {
bsCheck = bs1;
} else {
bsCheck = J.util.BSUtil.copy (bs1);
bsCheck.or (bs2);
}var atoms = modelSet.atoms;
var viewer = modelSet.viewer;
bsCheck.and (viewer.getModelUndeletedAtomsBitSet (this.modelIndex));
for (var i = bsCheck.nextSetBit (0); i >= 0; i = bsCheck.nextSetBit (i + 1)) if (atoms[i].isVisible (0) && atoms[i].atomID == 2 && atoms[i].getGroupID () != 5) vCA.addLast ((a1 = atoms[i]));

if (vCA.size () == 0) return 0;
var thresh = viewer.getFloat (570425408);
var mad = Clazz.floatToShort (viewer.getFloat (570425406) * 2000);
var delta = viewer.getInt (553648184);
var strutsMultiple = viewer.getBoolean (603979955);
var struts = this.getBioPolymer (a1.getPolymerIndexInModel ()).calculateStruts (modelSet, bs1, bs2, vCA, thresh, delta, strutsMultiple);
for (var i = 0; i < struts.size (); i++) {
var o = struts.get (i);
modelSet.bondAtoms (o[0], o[1], 32768, mad, null, 0, false, true);
}
return struts.size ();
}, "J.modelset.ModelSet,J.util.BS,J.util.BS");
Clazz.overrideMethod (c$, "setStructureList", 
function (structureList) {
this.bioPolymers = J.util.ArrayUtil.arrayCopyObject (this.bioPolymers, this.bioPolymerCount);
for (var i = this.bioPolymerCount; --i >= 0; ) this.bioPolymers[i].setStructureList (structureList);

}, "java.util.Map");
Clazz.overrideMethod (c$, "calculateStraightness", 
function (viewer, ctype, qtype, mStep) {
for (var p = 0; p < this.bioPolymerCount; p++) this.bioPolymers[p].getPdbData (viewer, ctype, qtype, mStep, 2, null, null, false, false, false, null, null, null,  new J.util.BS ());

}, "J.viewer.Viewer,~S,~S,~N");
Clazz.overrideMethod (c$, "getPolymerPointsAndVectors", 
function (bs, vList, isTraceAlpha, sheetSmoothing) {
var last = 2147483646;
for (var ip = 0; ip < this.bioPolymerCount; ip++) last = this.bioPolymers[ip].getPolymerPointsAndVectors (last, bs, vList, isTraceAlpha, sheetSmoothing);

}, "J.util.BS,J.util.JmolList,~B,~N");
Clazz.overrideMethod (c$, "getPolymerLeadMidPoints", 
function (iPolymer) {
return this.bioPolymers[iPolymer].getLeadMidpoints ();
}, "~N");
Clazz.overrideMethod (c$, "recalculateLeadMidpointsAndWingVectors", 
function () {
for (var ip = 0; ip < this.bioPolymerCount; ip++) this.bioPolymers[ip].recalculateLeadMidpointsAndWingVectors ();

});
Clazz.overrideMethod (c$, "getBioBranches", 
function (biobranches) {
var bsBranch;
for (var j = 0; j < this.bioPolymerCount; j++) {
bsBranch =  new J.util.BS ();
this.bioPolymers[j].getRange (bsBranch);
var iAtom = bsBranch.nextSetBit (0);
if (iAtom >= 0) {
if (biobranches == null) biobranches =  new J.util.JmolList ();
biobranches.addLast (bsBranch);
}}
return biobranches;
}, "J.util.JmolList");
Clazz.overrideMethod (c$, "getGroupsWithin", 
function (nResidues, bs, bsResult) {
for (var i = this.bioPolymerCount; --i >= 0; ) this.bioPolymers[i].getRangeGroups (nResidues, bs, bsResult);

}, "~N,J.util.BS,J.util.BS");
Clazz.overrideMethod (c$, "getSequenceBits", 
function (specInfo, bs, bsResult) {
var lenInfo = specInfo.length;
for (var ip = 0; ip < this.bioPolymerCount; ip++) {
var sequence = this.bioPolymers[ip].getSequence ();
var j = -1;
while ((j = sequence.indexOf (specInfo, ++j)) >= 0) this.bioPolymers[ip].getPolymerSequenceAtoms (j, lenInfo, bs, bsResult);

}
}, "~S,J.util.BS,J.util.BS");
Clazz.overrideMethod (c$, "selectSeqcodeRange", 
function (seqcodeA, seqcodeB, chainID, bs, caseSensitive) {
var id;
for (var i = this.chainCount; --i >= 0; ) {
var chain = this.chains[i];
if (chainID == -1 || chainID == (id = chain.chainID) || !caseSensitive && id < 256 && chainID == J.modelset.AtomCollection.chainToUpper (id)) for (var index = 0; index >= 0; ) index = this.chains[i].selectSeqcodeRange (index, seqcodeA, seqcodeB, bs);

}
}, "~N,~N,~N,J.util.BS,~B");
Clazz.overrideMethod (c$, "getRasmolHydrogenBonds", 
function (bsA, bsB, vHBonds, nucleicOnly, nMax, dsspIgnoreHydrogens, bsHBonds) {
var doAdd = (vHBonds == null);
if (doAdd) vHBonds =  new J.util.JmolList ();
if (nMax < 0) nMax = 2147483647;
var asDSSP = (bsB == null);
var bp;
var bp1;
if (asDSSP && this.bioPolymerCount > 0) {
this.bioPolymers[0].calculateDssp (this.bioPolymers, this.bioPolymerCount, vHBonds, false, dsspIgnoreHydrogens, false);
} else {
for (var i = this.bioPolymerCount; --i >= 0; ) {
bp = this.bioPolymers[i];
var type = bp.getType ();
if ((nucleicOnly || type != 1) && type != 2) continue;
var isRNA = bp.isRna ();
var isAmino = (type == 1);
if (isAmino) bp.calcRasmolHydrogenBonds (null, bsA, bsB, vHBonds, nMax, null, true, false);
for (var j = this.bioPolymerCount; --j >= 0; ) {
if ((bp1 = this.bioPolymers[j]) != null && (isRNA || i != j) && type == bp1.getType ()) {
bp1.calcRasmolHydrogenBonds (bp, bsA, bsB, vHBonds, nMax, null, true, false);
}}
}
}if (vHBonds.size () == 0 || !doAdd) return;
this.hasRasmolHBonds = true;
for (var i = 0; i < vHBonds.size (); i++) {
var bond = vHBonds.get (i);
var atom1 = bond.getAtom1 ();
var atom2 = bond.getAtom2 ();
if (atom1.isBonded (atom2)) continue;
var index = this.modelSet.addHBond (atom1, atom2, bond.order, bond.getEnergy ());
if (bsHBonds != null) bsHBonds.set (index);
}
}, "J.util.BS,J.util.BS,J.util.JmolList,~B,~N,~B,J.util.BS");
Clazz.overrideMethod (c$, "clearRasmolHydrogenBonds", 
function (bsAtoms) {
var bsDelete =  new J.util.BS ();
this.hasRasmolHBonds = false;
var models = this.modelSet.models;
var bonds = this.modelSet.bonds;
for (var i = this.modelSet.bondCount; --i >= 0; ) {
var bond = bonds[i];
var atom1 = bond.getAtom1 ();
var m = models[atom1.modelIndex];
if (!m.isBioModel || m.trajectoryBaseIndex != this.modelIndex || (bond.order & 28672) == 0) continue;
if (bsAtoms != null && !bsAtoms.get (atom1.index)) {
this.hasRasmolHBonds = true;
continue;
}bsDelete.set (i);
}
if (bsDelete.nextSetBit (0) >= 0) this.modelSet.deleteBonds (bsDelete, false);
}, "J.util.BS");
Clazz.overrideMethod (c$, "calculatePolymers", 
function (groups, groupCount, baseGroupIndex, modelsExcluded, checkConnections) {
if (groups == null) {
groups = this.modelSet.getGroups ();
groupCount = groups.length;
}if (modelsExcluded != null) for (var i = 0; i < groupCount; ++i) {
var group = groups[i];
if (Clazz.instanceOf (group, J.modelsetbio.Monomer)) {
var monomer = group;
if (monomer.getBioPolymer () != null && (!modelsExcluded.get (monomer.getModelIndex ()))) monomer.setBioPolymer (null, -1);
}}
for (var i = baseGroupIndex; i < groupCount; ++i) {
var g = groups[i];
var model = g.getModel ();
if (!model.isBioModel || !(Clazz.instanceOf (g, J.modelsetbio.Monomer))) continue;
var doCheck = checkConnections && !this.modelSet.isJmolDataFrameForModel (this.modelSet.atoms[g.firstAtomIndex].modelIndex);
var bp = ((g).getBioPolymer () == null ? J.modelsetbio.Resolver.allocateBioPolymer (groups, i, doCheck) : null);
if (bp == null || bp.monomerCount == 0) continue;
(model).addBioPolymer (bp);
i += bp.monomerCount - 1;
}
}, "~A,~N,~N,J.util.BS,~B");
$_M(c$, "addBioPolymer", 
($fz = function (polymer) {
if (this.bioPolymers.length == 0) this.clearBioPolymers ();
if (this.bioPolymerCount == this.bioPolymers.length) this.bioPolymers = J.util.ArrayUtil.doubleLength (this.bioPolymers);
polymer.bioPolymerIndexInModel = this.bioPolymerCount;
this.bioPolymers[this.bioPolymerCount++] = polymer;
}, $fz.isPrivate = true, $fz), "J.modelsetbio.BioPolymer");
Clazz.overrideMethod (c$, "clearBioPolymers", 
function () {
this.bioPolymers =  new Array (8);
this.bioPolymerCount = 0;
});
Clazz.overrideMethod (c$, "getAllPolymerInfo", 
function (bs, finalInfo, modelVector) {
var modelInfo =  new java.util.Hashtable ();
var info =  new J.util.JmolList ();
for (var ip = 0; ip < this.bioPolymerCount; ip++) {
var polyInfo = this.bioPolymers[ip].getPolymerInfo (bs);
if (!polyInfo.isEmpty ()) info.addLast (polyInfo);
}
if (info.size () > 0) {
modelInfo.put ("modelIndex", Integer.$valueOf (this.modelIndex));
modelInfo.put ("polymers", info);
modelVector.addLast (modelInfo);
}}, "J.util.BS,java.util.Map,J.util.JmolList");
Clazz.overrideMethod (c$, "getChimeInfo", 
function (sb, nHetero) {
var n = 0;
var models = this.modelSet.models;
var modelCount = this.modelSet.modelCount;
var atomCount = this.modelSet.getAtomCount ();
var atoms = this.modelSet.atoms;
sb.append ("\nMolecule name ....... " + this.modelSet.getModelSetAuxiliaryInfoValue ("COMPND"));
sb.append ("\nSecondary Structure . PDB Data Records");
sb.append ("\nBrookhaven Code ..... " + this.modelSet.modelSetName);
for (var i = modelCount; --i >= 0; ) n += models[i].getChainCount (false);

sb.append ("\nNumber of Chains .... " + n);
n = 0;
for (var i = modelCount; --i >= 0; ) n += models[i].getGroupCountHetero (false);

nHetero = 0;
for (var i = modelCount; --i >= 0; ) nHetero += models[i].getGroupCountHetero (true);

sb.append ("\nNumber of Groups .... " + n);
if (nHetero > 0) sb.append (" (" + nHetero + ")");
for (var i = atomCount; --i >= 0; ) if (atoms[i].isHetero ()) nHetero++;

this.getChimeInfoM (sb, nHetero);
var nH = 0;
var nS = 0;
var nT = 0;
var id;
var lastid = -1;
for (var i = 0; i < atomCount; i++) {
if (atoms[i].modelIndex != 0) break;
if ((id = atoms[i].getStrucNo ()) != lastid && id != 0) {
lastid = id;
switch (atoms[i].getProteinStructureType ()) {
case J.constant.EnumStructure.HELIX:
nH++;
break;
case J.constant.EnumStructure.SHEET:
nS++;
break;
case J.constant.EnumStructure.TURN:
nT++;
break;
}
}}
sb.append ("\nNumber of Helices ... " + nH);
sb.append ("\nNumber of Strands ... " + nS);
sb.append ("\nNumber of Turns ..... " + nT);
}, "J.util.SB,~N");
Clazz.overrideMethod (c$, "getProteinStructureState", 
function (bsAtoms, taintedOnly, needPhiPsi, mode) {
var showMode = (mode == 3);
var pdbFileMode = (mode == 1);
var scriptMode = (mode == 0);
var bs = null;
var cmd =  new J.util.SB ();
var sbTurn =  new J.util.SB ();
var sbHelix =  new J.util.SB ();
var sbSheet =  new J.util.SB ();
var type = J.constant.EnumStructure.NONE;
var subtype = J.constant.EnumStructure.NONE;
var id = 0;
var iLastAtom = 0;
var iLastModel = -1;
var lastId = -1;
var res1 = 0;
var res2 = 0;
var sid = "";
var group1 = "";
var group2 = "";
var chain1 = "";
var chain2 = "";
var n = 0;
var nHelix = 0;
var nTurn = 0;
var nSheet = 0;
var bsTainted = null;
var models = this.modelSet.models;
var atoms = this.modelSet.atoms;
var atomCount = this.modelSet.getAtomCount ();
if (taintedOnly) {
if (!this.modelSet.proteinStructureTainted) return "";
bsTainted =  new J.util.BS ();
for (var i = this.firstAtomIndex; i < atomCount; i++) if (models[atoms[i].modelIndex].isStructureTainted ()) bsTainted.set (i);

bsTainted.set (atomCount);
}for (var i = 0; i <= atomCount; i++) if (i == atomCount || bsAtoms == null || bsAtoms.get (i)) {
if (taintedOnly && !bsTainted.get (i)) continue;
id = 0;
if (i == atomCount || (id = atoms[i].getStrucNo ()) != lastId) {
if (bs != null) {
switch (type) {
case J.constant.EnumStructure.HELIX:
case J.constant.EnumStructure.TURN:
case J.constant.EnumStructure.SHEET:
n++;
if (scriptMode) {
var iModel = atoms[iLastAtom].modelIndex;
var comment = "    \t# model=" + this.modelSet.getModelNumberDotted (iModel);
if (iLastModel != iModel) {
iLastModel = iModel;
cmd.append ("  structure none ").append (J.util.Escape.eBS (this.modelSet.getModelAtomBitSetIncludingDeleted (iModel, false))).append (comment).append (";\n");
}comment += " & (" + res1 + " - " + res2 + ")";
var stype = subtype.getBioStructureTypeName (false);
cmd.append ("  structure ").append (stype).append (" ").append (J.util.Escape.eBS (bs)).append (comment).append (";\n");
} else {
var str;
var nx;
var sb;
switch (type) {
case J.constant.EnumStructure.HELIX:
nx = ++nHelix;
if (sid == null || pdbFileMode) sid = J.util.TextFormat.formatStringI ("%3N %3N", "N", nx);
str = "HELIX  %ID %3GROUPA %1CA %4RESA  %3GROUPB %1CB %4RESB";
sb = sbHelix;
var stype = null;
switch (subtype) {
case J.constant.EnumStructure.HELIX:
case J.constant.EnumStructure.HELIXALPHA:
stype = "  1";
break;
case J.constant.EnumStructure.HELIX310:
stype = "  5";
break;
case J.constant.EnumStructure.HELIXPI:
stype = "  3";
break;
}
if (stype != null) str += stype;
break;
case J.constant.EnumStructure.SHEET:
nx = ++nSheet;
if (sid == null || pdbFileMode) {
sid = J.util.TextFormat.formatStringI ("%3N %3A 0", "N", nx);
sid = J.util.TextFormat.formatStringS (sid, "A", "S" + nx);
}str = "SHEET  %ID %3GROUPA %1CA%4RESA  %3GROUPB %1CB%4RESB";
sb = sbSheet;
break;
case J.constant.EnumStructure.TURN:
default:
nx = ++nTurn;
if (sid == null || pdbFileMode) sid = J.util.TextFormat.formatStringI ("%3N %3N", "N", nx);
str = "TURN   %ID %3GROUPA %1CA%4RESA  %3GROUPB %1CB%4RESB";
sb = sbTurn;
break;
}
str = J.util.TextFormat.formatStringS (str, "ID", sid);
str = J.util.TextFormat.formatStringS (str, "GROUPA", group1);
str = J.util.TextFormat.formatStringS (str, "CA", chain1);
str = J.util.TextFormat.formatStringI (str, "RESA", res1);
str = J.util.TextFormat.formatStringS (str, "GROUPB", group2);
str = J.util.TextFormat.formatStringS (str, "CB", chain2);
str = J.util.TextFormat.formatStringI (str, "RESB", res2);
sb.append (str);
if (showMode) sb.append (" strucno= ").appendI (lastId);
sb.append ("\n");
}}
bs = null;
}if (id == 0 || bsAtoms != null && needPhiPsi && (Float.isNaN (atoms[i].getGroupParameter (1112539145)) || Float.isNaN (atoms[i].getGroupParameter (1112539146)))) continue;
}var ch = atoms[i].getChainIDStr ();
if (bs == null) {
bs =  new J.util.BS ();
res1 = atoms[i].getResno ();
group1 = atoms[i].getGroup3 (false);
chain1 = ch;
}type = atoms[i].getProteinStructureType ();
subtype = atoms[i].getProteinStructureSubType ();
sid = atoms[i].getProteinStructureTag ();
bs.set (i);
lastId = id;
res2 = atoms[i].getResno ();
group2 = atoms[i].getGroup3 (false);
chain2 = ch;
iLastAtom = i;
}
if (n > 0) cmd.append ("\n");
return (scriptMode ? cmd.toString () : sbHelix.appendSB (sbSheet).appendSB (sbTurn).appendSB (cmd).toString ());
}, "J.util.BS,~B,~B,~N");
Clazz.overrideMethod (c$, "getFullPDBHeader", 
function () {
if (this.modelIndex < 0) return "";
var info = this.auxiliaryInfo.get ("fileHeader");
if (info != null) return info;
info = this.modelSet.viewer.getCurrentFileAsString ();
var ichMin = info.length;
for (var i = J.modelsetbio.BioModel.pdbRecords.length; --i >= 0; ) {
var ichFound;
var strRecord = J.modelsetbio.BioModel.pdbRecords[i];
switch (ichFound = (info.startsWith (strRecord) ? 0 : info.indexOf ("\n" + strRecord))) {
case -1:
continue;
case 0:
this.auxiliaryInfo.put ("fileHeader", "");
return "";
default:
if (ichFound < ichMin) ichMin = ++ichFound;
}
}
info = info.substring (0, ichMin);
this.auxiliaryInfo.put ("fileHeader", info);
return info;
});
Clazz.overrideMethod (c$, "getPdbData", 
function (viewer, type, ctype, isDraw, bsSelected, sb, tokens, pdbCONECT, bsWritten) {
var bothEnds = false;
var qtype = (ctype != 'R' ? 'r' : type.length > 13 && type.indexOf ("ramachandran ") >= 0 ? type.charAt (13) : 'R');
if (qtype == 'r') qtype = viewer.getQuaternionFrame ();
var mStep = viewer.getInt (553648146);
var derivType = (type.indexOf ("diff") < 0 ? 0 : type.indexOf ("2") < 0 ? 1 : 2);
if (!isDraw) {
sb.append ("REMARK   6 Jmol PDB-encoded data: " + type + ";");
if (ctype != 'R') {
sb.append ("  quaternionFrame = \"" + qtype + "\"");
bothEnds = true;
}sb.append ("\nREMARK   6 Jmol Version ").append (J.viewer.Viewer.getJmolVersion ()).append ("\n");
if (ctype == 'R') sb.append ("REMARK   6 Jmol data min = {-180 -180 -180} max = {180 180 180} unScaledXyz = xyz * {1 1 1} + {0 0 0} plotScale = {100 100 100}\n");
 else sb.append ("REMARK   6 Jmol data min = {-1 -1 -1} max = {1 1 1} unScaledXyz = xyz * {0.1 0.1 0.1} + {0 0 0} plotScale = {100 100 100}\n");
}for (var p = 0; p < this.bioPolymerCount; p++) this.bioPolymers[p].getPdbData (viewer, ctype, qtype, mStep, derivType, this.bsAtoms, bsSelected, bothEnds, isDraw, p == 0, tokens, sb, pdbCONECT, bsWritten);

}, "J.viewer.Viewer,~S,~S,~B,J.util.BS,J.io.OutputStringBuilder,~A,J.util.SB,J.util.BS");
Clazz.defineStatics (c$,
"pdbRecords", ["ATOM  ", "MODEL ", "HETATM"]);
});
