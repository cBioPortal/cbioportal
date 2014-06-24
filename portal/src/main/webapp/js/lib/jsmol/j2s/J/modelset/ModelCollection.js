Clazz.declarePackage ("J.modelset");
Clazz.load (["J.modelset.BondCollection", "J.util.BS", "$.BoxInfo", "$.JmolList", "$.P3"], "J.modelset.ModelCollection", ["java.lang.Character", "$.Float", "java.util.Date", "$.Hashtable", "J.api.Interface", "J.bspt.Bspf", "J.constant.EnumPalette", "$.EnumVdw", "J.io.OutputStringBuilder", "$.XmlUtil", "J.modelset.Atom", "$.AtomIteratorWithinModel", "$.AtomIteratorWithinModelSet", "$.Bond", "$.HBond", "$.LabelToken", "$.Model", "J.util.ArrayUtil", "$.BSUtil", "$.Elements", "$.Escape", "$.JmolEdge", "$.JmolMolecule", "$.Logger", "$.P4", "$.Parser", "$.Quaternion", "$.SB", "$.TextFormat", "$.TriangleData", "$.V3", "J.viewer.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bsSymmetry = null;
this.modelSetName = null;
this.models = null;
this.modelCount = 0;
this.unitCells = null;
this.haveUnitCells = false;
this.modelNumbers = null;
this.modelFileNumbers = null;
this.modelNumbersForAtomLabel = null;
this.modelNames = null;
this.frameTitles = null;
this.elementsPresent = null;
this.isXYZ = false;
this.isPDB = false;
this.modelSetProperties = null;
this.modelSetAuxiliaryInfo = null;
this.someModelsHaveSymmetry = false;
this.someModelsHaveAromaticBonds = false;
this.someModelsHaveFractionalCoordinates = false;
this.ptTemp = null;
this.averageAtomPoint = null;
this.isBbcageDefault = false;
this.bboxModels = null;
this.bboxAtoms = null;
this.boxInfo = null;
this.stateScripts = null;
this.thisStateModel = 0;
this.trajectorySteps = null;
this.vibrationSteps = null;
this.selectedMolecules = null;
this.selectedMoleculeCount = 0;
this.showRebondTimes = true;
this.bsAll = null;
this.shapeManager = null;
this.ptTemp1 = null;
this.ptTemp2 = null;
this.proteinStructureTainted = false;
this.symTemp = null;
this.htPeaks = null;
this.vOrientations = null;
Clazz.instantialize (this, arguments);
}, J.modelset, "ModelCollection", J.modelset.BondCollection);
Clazz.prepareFields (c$, function () {
this.models =  new Array (1);
this.modelNumbers =  Clazz.newIntArray (1, 0);
this.modelFileNumbers =  Clazz.newIntArray (1, 0);
this.modelNumbersForAtomLabel =  new Array (1);
this.modelNames =  new Array (1);
this.frameTitles =  new Array (1);
this.ptTemp =  new J.util.P3 ();
this.averageAtomPoint =  new J.util.P3 ();
this.boxInfo =  new J.util.BoxInfo ();
{
this.boxInfo.addBoundBoxPoint (J.util.P3.new3 (-10, -10, -10));
this.boxInfo.addBoundBoxPoint (J.util.P3.new3 (10, 10, 10));
}this.stateScripts =  new J.util.JmolList ();
this.selectedMolecules =  new J.util.BS ();
this.ptTemp1 =  new J.util.P3 ();
this.ptTemp2 =  new J.util.P3 ();
});
$_M(c$, "mergeModelArrays", 
function (mergeModelSet) {
this.atoms = mergeModelSet.atoms;
this.bonds = mergeModelSet.bonds;
this.stateScripts = mergeModelSet.stateScripts;
this.proteinStructureTainted = mergeModelSet.proteinStructureTainted;
this.thisStateModel = -1;
this.bsSymmetry = mergeModelSet.bsSymmetry;
this.modelFileNumbers = mergeModelSet.modelFileNumbers;
this.modelNumbersForAtomLabel = mergeModelSet.modelNumbersForAtomLabel;
this.modelNames = mergeModelSet.modelNames;
this.modelNumbers = mergeModelSet.modelNumbers;
this.frameTitles = mergeModelSet.frameTitles;
this.mergeAtomArrays (mergeModelSet);
}, "J.modelset.ModelSet");
Clazz.overrideMethod (c$, "releaseModelSet", 
function () {
this.models = null;
this.bsSymmetry = null;
this.bsAll = null;
this.unitCells = null;
this.releaseModelSetBC ();
});
$_M(c$, "getUnitCell", 
function (modelIndex) {
if (!this.haveUnitCells || modelIndex < 0 || modelIndex >= this.modelCount) return null;
if (this.models[modelIndex].simpleCage != null) return this.models[modelIndex].simpleCage;
return (this.unitCells == null || modelIndex >= this.unitCells.length || !this.unitCells[modelIndex].haveUnitCell () ? null : this.unitCells[modelIndex]);
}, "~N");
$_M(c$, "setModelCage", 
function (modelIndex, simpleCage) {
if (modelIndex < 0 || modelIndex >= this.modelCount) return;
this.models[modelIndex].simpleCage = simpleCage;
this.haveUnitCells = true;
}, "~N,J.api.SymmetryInterface");
$_M(c$, "getPlaneIntersection", 
function (type, plane, scale, flags, modelIndex) {
var pts = null;
switch (type) {
case 1614417948:
var uc = this.getUnitCell (modelIndex);
if (uc == null) return null;
pts = uc.getCanonicalCopy (scale);
break;
case 1679429641:
pts = this.boxInfo.getCanonicalCopy (scale);
break;
}
var v =  new J.util.JmolList ();
v.addLast (pts);
return J.util.TriangleData.intersectPlane (plane, v, flags);
}, "~N,J.util.P4,~N,~N,~N");
$_M(c$, "getModelName", 
function (modelIndex) {
return this.modelCount < 1 ? "" : modelIndex >= 0 ? this.modelNames[modelIndex] : this.modelNumbersForAtomLabel[-1 - modelIndex];
}, "~N");
$_M(c$, "getModelTitle", 
function (modelIndex) {
return this.getModelAuxiliaryInfoValue (modelIndex, "title");
}, "~N");
$_M(c$, "getModelFileName", 
function (modelIndex) {
return this.getModelAuxiliaryInfoValue (modelIndex, "fileName");
}, "~N");
$_M(c$, "getModelFileType", 
function (modelIndex) {
return this.getModelAuxiliaryInfoValue (modelIndex, "fileType");
}, "~N");
$_M(c$, "setFrameTitle", 
function (bsFrames, title) {
if (Clazz.instanceOf (title, String)) {
for (var i = bsFrames.nextSetBit (0); i >= 0; i = bsFrames.nextSetBit (i + 1)) this.frameTitles[i] = title;

} else {
var list = title;
for (var i = bsFrames.nextSetBit (0), n = 0; i >= 0; i = bsFrames.nextSetBit (i + 1)) if (n < list.length) this.frameTitles[i] = list[n++];

}}, "J.util.BS,~O");
$_M(c$, "getFrameTitle", 
function (modelIndex) {
return (modelIndex >= 0 && modelIndex < this.modelCount ? this.frameTitles[modelIndex] : "");
}, "~N");
$_M(c$, "getModelNumberForAtomLabel", 
function (modelIndex) {
return this.modelNumbersForAtomLabel[modelIndex];
}, "~N");
$_M(c$, "calculatePolymers", 
function (groups, groupCount, baseGroupIndex, modelsExcluded) {
if (!this.isPDB) return;
var checkConnections = !this.viewer.getBoolean (603979892);
for (var i = 0; i < this.modelCount; i++) if ((modelsExcluded == null || !modelsExcluded.get (i)) && this.models[i].isBioModel) {
this.models[i].calculatePolymers (groups, groupCount, baseGroupIndex, modelsExcluded, checkConnections);
return;
}
}, "~A,~N,~N,J.util.BS");
$_M(c$, "getGroups", 
function () {
var n = 0;
for (var i = 0; i < this.modelCount; i++) n += this.models[i].getGroupCount ();

var groups =  new Array (n);
for (var i = 0, iGroup = 0; i < this.modelCount; i++) for (var j = 0; j < this.models[i].chainCount; j++) for (var k = 0; k < this.models[i].chains[j].groupCount; k++) {
groups[iGroup] = this.models[i].chains[j].groups[k];
groups[iGroup].groupIndex = iGroup;
iGroup++;
}


return groups;
});
$_M(c$, "getNotionalUnitcell", 
function () {
var c = this.getUnitCell (0);
return (c == null ? null : c.getNotionalUnitCell ());
});
$_M(c$, "setCrystallographicDefaults", 
function () {
return !this.isPDB && this.someModelsHaveSymmetry && this.someModelsHaveFractionalCoordinates;
});
$_M(c$, "getAverageAtomPoint", 
function () {
return this.averageAtomPoint;
});
$_M(c$, "getBoundBoxCenter", 
function (modelIndex) {
if (this.isJmolDataFrameForModel (modelIndex)) return  new J.util.P3 ();
return this.boxInfo.getBoundBoxCenter ();
}, "~N");
$_M(c$, "getBoundBoxCornerVector", 
function () {
return this.boxInfo.getBoundBoxCornerVector ();
});
$_M(c$, "getBboxVertices", 
function () {
return this.boxInfo.getBoundBoxVertices ();
});
$_M(c$, "getBoundBoxInfo", 
function () {
return this.boxInfo.getBoundBoxInfo ();
});
$_M(c$, "getBoundBoxModels", 
function () {
return this.bboxModels;
});
$_M(c$, "setBoundBox", 
function (pt1, pt2, byCorner, scale) {
this.isBbcageDefault = false;
this.bboxModels = null;
this.bboxAtoms = null;
this.boxInfo.setBoundBox (pt1, pt2, byCorner, scale);
}, "J.util.P3,J.util.P3,~B,~N");
$_M(c$, "getBoundBoxCommand", 
function (withOptions) {
if (!withOptions && this.bboxAtoms != null) return "boundbox " + J.util.Escape.eBS (this.bboxAtoms);
this.ptTemp.setT (this.boxInfo.getBoundBoxCenter ());
var bbVector = this.boxInfo.getBoundBoxCornerVector ();
var s = (withOptions ? "boundbox " + J.util.Escape.eP (this.ptTemp) + " " + J.util.Escape.eP (bbVector) + "\n#or\n" : "");
this.ptTemp.sub (bbVector);
s += "boundbox corners " + J.util.Escape.eP (this.ptTemp) + " ";
this.ptTemp.scaleAdd2 (2, bbVector, this.ptTemp);
var v = Math.abs (8 * bbVector.x * bbVector.y * bbVector.z);
s += J.util.Escape.eP (this.ptTemp) + " # volume = " + v;
return s;
}, "~B");
$_M(c$, "getDefaultVdwType", 
function (modelIndex) {
return (!this.models[modelIndex].isBioModel ? J.constant.EnumVdw.AUTO_BABEL : this.models[modelIndex].hydrogenCount == 0 ? J.constant.EnumVdw.AUTO_JMOL : J.constant.EnumVdw.AUTO_BABEL);
}, "~N");
$_M(c$, "setRotationRadius", 
function (modelIndex, angstroms) {
if (this.isJmolDataFrameForModel (modelIndex)) {
this.models[modelIndex].defaultRotationRadius = angstroms;
return false;
}return true;
}, "~N,~N");
$_M(c$, "calcRotationRadius", 
function (modelIndex, center) {
if (this.isJmolDataFrameForModel (modelIndex)) {
var r = this.models[modelIndex].defaultRotationRadius;
return (r == 0 ? 10 : r);
}var maxRadius = 0;
for (var i = this.atomCount; --i >= 0; ) {
if (this.isJmolDataFrameForAtom (this.atoms[i])) {
modelIndex = this.atoms[i].modelIndex;
while (i >= 0 && this.atoms[i].modelIndex == modelIndex) i--;

continue;
}var atom = this.atoms[i];
var distAtom = center.distance (atom);
var outerVdw = distAtom + this.getRadiusVdwJmol (atom);
if (outerVdw > maxRadius) maxRadius = outerVdw;
}
return (maxRadius == 0 ? 10 : maxRadius);
}, "~N,J.util.P3");
$_M(c$, "calcBoundBoxDimensions", 
function (bs, scale) {
if (bs != null && bs.nextSetBit (0) < 0) bs = null;
if (bs == null && this.isBbcageDefault || this.atomCount < 2) return;
this.bboxModels = this.getModelBitSet (this.bboxAtoms = J.util.BSUtil.copy (bs), false);
if (this.calcAtomsMinMax (bs, this.boxInfo) == this.atomCount) this.isBbcageDefault = true;
if (bs == null) {
this.averageAtomPoint.setT (this.getAtomSetCenter (null));
if (this.unitCells != null) this.calcUnitCellMinMax ();
}this.boxInfo.setBbcage (scale);
}, "J.util.BS,~N");
$_M(c$, "getBoxInfo", 
function (bs, scale) {
if (bs == null) return this.boxInfo;
var bi =  new J.util.BoxInfo ();
this.calcAtomsMinMax (bs, bi);
bi.setBbcage (scale);
return bi;
}, "J.util.BS,~N");
$_M(c$, "calcAtomsMinMax", 
function (bs, boxInfo) {
boxInfo.reset ();
var nAtoms = 0;
var isAll = (bs == null);
var i0 = (isAll ? this.atomCount - 1 : bs.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bs.nextSetBit (i + 1))) {
nAtoms++;
if (!this.isJmolDataFrameForAtom (this.atoms[i])) boxInfo.addBoundBoxPoint (this.atoms[i]);
}
return nAtoms;
}, "J.util.BS,J.util.BoxInfo");
$_M(c$, "calcUnitCellMinMax", 
($fz = function () {
for (var i = 0; i < this.modelCount; i++) {
if (!this.unitCells[i].getCoordinatesAreFractional ()) continue;
var vertices = this.unitCells[i].getUnitCellVertices ();
for (var j = 0; j < 8; j++) this.boxInfo.addBoundBoxPoint (vertices[j]);

}
}, $fz.isPrivate = true, $fz));
$_M(c$, "calcRotationRadiusBs", 
function (bs) {
var center = this.getAtomSetCenter (bs);
var maxRadius = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var atom = this.atoms[i];
var distAtom = center.distance (atom);
var outerVdw = distAtom + this.getRadiusVdwJmol (atom);
if (outerVdw > maxRadius) maxRadius = outerVdw;
}
return (maxRadius == 0 ? 10 : maxRadius);
}, "J.util.BS");
$_M(c$, "getCenterAndPoints", 
function (vAtomSets, addCenters) {
var bsAtoms1;
var bsAtoms2;
var n = (addCenters ? 1 : 0);
for (var ii = vAtomSets.size (); --ii >= 0; ) {
var bss = vAtomSets.get (ii);
bsAtoms1 = bss[0];
if (Clazz.instanceOf (bss[1], J.util.BS)) {
bsAtoms2 = bss[1];
n += Math.min (bsAtoms1.cardinality (), bsAtoms2.cardinality ());
} else {
n += Math.min (bsAtoms1.cardinality (), (bss[1]).length);
}}
var points =  Clazz.newArray (2, n, null);
if (addCenters) {
points[0][0] =  new J.util.P3 ();
points[1][0] =  new J.util.P3 ();
}for (var ii = vAtomSets.size (); --ii >= 0; ) {
var bss = vAtomSets.get (ii);
bsAtoms1 = bss[0];
if (Clazz.instanceOf (bss[1], J.util.BS)) {
bsAtoms2 = bss[1];
for (var i = bsAtoms1.nextSetBit (0), j = bsAtoms2.nextSetBit (0); i >= 0 && j >= 0; i = bsAtoms1.nextSetBit (i + 1), j = bsAtoms2.nextSetBit (j + 1)) {
points[0][--n] = this.atoms[i];
points[1][n] = this.atoms[j];
if (addCenters) {
points[0][0].add (this.atoms[i]);
points[1][0].add (this.atoms[j]);
}}
} else {
var coords = bss[1];
for (var i = bsAtoms1.nextSetBit (0), j = 0; i >= 0 && j < coords.length; i = bsAtoms1.nextSetBit (i + 1), j++) {
points[0][--n] = this.atoms[i];
points[1][n] = coords[j];
if (addCenters) {
points[0][0].add (this.atoms[i]);
points[1][0].add (coords[j]);
}}
}}
if (addCenters) {
points[0][0].scale (1 / (points[0].length - 1));
points[1][0].scale (1 / (points[1].length - 1));
}return points;
}, "J.util.JmolList,~B");
$_M(c$, "getAtomSetCenter", 
function (bs) {
var ptCenter = J.util.P3.new3 (0, 0, 0);
var nPoints = 0;
if (bs != null) for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (!this.isJmolDataFrameForAtom (this.atoms[i])) {
nPoints++;
ptCenter.add (this.atoms[i]);
}}
if (nPoints > 0) ptCenter.scale (1.0 / nPoints);
return ptCenter;
}, "J.util.BS");
$_M(c$, "setAPm", 
function (bs, tok, iValue, fValue, sValue, values, list) {
this.setAPa (bs, tok, iValue, fValue, sValue, values, list);
switch (tok) {
case 1095763988:
case 1632634889:
if (this.viewer.getBoolean (603979944)) this.assignAromaticBonds ();
break;
}
}, "J.util.BS,~N,~N,~N,~S,~A,~A");
$_M(c$, "addStateScript", 
function (script1, bsBonds, bsAtoms1, bsAtoms2, script2, addFrameNumber, postDefinitions) {
var iModel = this.viewer.getCurrentModelIndex ();
if (addFrameNumber) {
if (this.thisStateModel != iModel) script1 = "frame " + (iModel < 0 ? "all #" + iModel : this.getModelNumberDotted (iModel)) + ";\n  " + script1;
this.thisStateModel = iModel;
} else {
this.thisStateModel = -1;
}var stateScript =  new J.modelset.ModelCollection.StateScript (this.thisStateModel, script1, bsBonds, bsAtoms1, bsAtoms2, script2, postDefinitions);
if (stateScript.isValid ()) {
this.stateScripts.addLast (stateScript);
}return stateScript;
}, "~S,J.util.BS,J.util.BS,J.util.BS,~S,~B,~B");
$_M(c$, "calculateStructuresAllExcept", 
function (alreadyDefined, asDSSP, doReport, dsspIgnoreHydrogen, setStructure, includeAlpha) {
this.freezeModels ();
var ret = "";
var bsModels = J.util.BSUtil.copyInvert (alreadyDefined, this.modelCount);
if (setStructure) this.setDefaultStructure (bsModels);
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) {
ret += this.models[i].calculateStructures (asDSSP, doReport, dsspIgnoreHydrogen, setStructure, includeAlpha);
}
if (setStructure) {
this.setStructureIndexes ();
}return ret;
}, "J.util.BS,~B,~B,~B,~B,~B");
$_M(c$, "setDefaultStructure", 
function (bsModels) {
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) if (this.models[i].isBioModel && this.models[i].defaultStructure == null) this.models[i].defaultStructure = this.getProteinStructureState (this.models[i].bsAtoms, false, false, 0);

}, "J.util.BS");
$_M(c$, "setProteinType", 
function (bs, type) {
var monomerIndexCurrent = -1;
var iLast = -1;
var bsModels = this.getModelBitSet (bs, false);
this.setDefaultStructure (bsModels);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (iLast != i - 1) monomerIndexCurrent = -1;
monomerIndexCurrent = this.atoms[i].group.setProteinStructureType (type, monomerIndexCurrent);
var modelIndex = this.atoms[i].modelIndex;
this.proteinStructureTainted = this.models[modelIndex].structureTainted = true;
iLast = i = this.atoms[i].group.lastAtomIndex;
}
var lastStrucNo =  Clazz.newIntArray (this.modelCount, 0);
for (var i = 0; i < this.atomCount; ) {
var modelIndex = this.atoms[i].modelIndex;
if (!bsModels.get (modelIndex)) {
i = this.models[modelIndex].firstAtomIndex + this.models[modelIndex].atomCount;
continue;
}iLast = this.atoms[i].getStrucNo ();
if (iLast < 1000 && iLast > lastStrucNo[modelIndex]) lastStrucNo[modelIndex] = iLast;
i = this.atoms[i].group.lastAtomIndex + 1;
}
for (var i = 0; i < this.atomCount; ) {
var modelIndex = this.atoms[i].modelIndex;
if (!bsModels.get (modelIndex)) {
i = this.models[modelIndex].firstAtomIndex + this.models[modelIndex].atomCount;
continue;
}if (this.atoms[i].getStrucNo () > 1000) this.atoms[i].group.setStrucNo (++lastStrucNo[modelIndex]);
i = this.atoms[i].group.lastAtomIndex + 1;
}
}, "J.util.BS,J.constant.EnumStructure");
$_M(c$, "freezeModels", 
function () {
for (var iModel = this.modelCount; --iModel >= 0; ) this.models[iModel].freeze ();

});
$_M(c$, "getStructureList", 
function () {
return this.viewer.getStructureList ();
});
$_M(c$, "setStructureList", 
function (structureList) {
for (var iModel = this.modelCount; --iModel >= 0; ) this.models[iModel].setStructureList (structureList);

}, "java.util.Map");
$_M(c$, "setConformation", 
function (bsAtoms) {
var bsModels = this.getModelBitSet (bsAtoms, false);
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.models[i].setConformation (bsAtoms);

return bsAtoms;
}, "J.util.BS");
$_M(c$, "getConformation", 
function (modelIndex, conformationIndex, doSet) {
var bs =  new J.util.BS ();
for (var i = this.modelCount; --i >= 0; ) if (i == modelIndex || modelIndex < 0) {
var altLocs = this.getAltLocListInModel (i);
var nAltLocs = this.getAltLocCountInModel (i);
if (conformationIndex > 0 && conformationIndex >= nAltLocs) continue;
var bsConformation = this.viewer.getModelUndeletedAtomsBitSet (i);
if (conformationIndex >= 0) {
if (!this.models[i].getPdbConformation (bsConformation, conformationIndex)) for (var c = nAltLocs; --c >= 0; ) if (c != conformationIndex) bsConformation.andNot (this.getAtomBitsMDa (1048607, altLocs.substring (c, c + 1)));

}if (bsConformation.nextSetBit (0) >= 0) {
bs.or (bsConformation);
if (doSet) this.models[i].setConformation (bsConformation);
}}
return bs;
}, "~N,~N,~B");
$_M(c$, "getHeteroList", 
function (modelIndex) {
var htFull =  new java.util.Hashtable ();
var ok = false;
for (var i = this.modelCount; --i >= 0; ) if (modelIndex < 0 || i == modelIndex) {
var ht = this.getModelAuxiliaryInfoValue (i, "hetNames");
if (ht == null) continue;
ok = true;
for (var entry, $entry = ht.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var key = entry.getKey ();
htFull.put (key, entry.getValue ());
}
}
return (ok ? htFull : this.getModelSetAuxiliaryInfoValue ("hetNames"));
}, "~N");
$_M(c$, "getModelSetProperties", 
function () {
return this.modelSetProperties;
});
$_M(c$, "getModelSetAuxiliaryInfo", 
function () {
return this.modelSetAuxiliaryInfo;
});
$_M(c$, "getModelSetProperty", 
function (propertyName) {
return (this.modelSetProperties == null ? null : this.modelSetProperties.getProperty (propertyName));
}, "~S");
$_M(c$, "getModelSetAuxiliaryInfoValue", 
function (keyName) {
return (this.modelSetAuxiliaryInfo == null ? null : this.modelSetAuxiliaryInfo.get (keyName));
}, "~S");
$_M(c$, "getModelSetAuxiliaryInfoBoolean", 
function (keyName) {
var val = this.getModelSetAuxiliaryInfoValue (keyName);
return (Clazz.instanceOf (val, Boolean) && (val).booleanValue ());
}, "~S");
$_M(c$, "mergeTrajectories", 
function (isTrajectory) {
if (this.trajectorySteps == null) {
if (!isTrajectory) return 0;
this.trajectorySteps =  new J.util.JmolList ();
}for (var i = this.trajectorySteps.size (); i < this.modelCount; i++) this.trajectorySteps.addLast (null);

return this.modelCount;
}, "~B");
$_M(c$, "getTrajectoryIndex", 
function (modelIndex) {
return this.models[modelIndex].trajectoryBaseIndex;
}, "~N");
$_M(c$, "isTrajectory", 
function (modelIndex) {
return this.models[modelIndex].isTrajectory;
}, "~N");
$_M(c$, "isTrajectoryMeasurement", 
function (countPlusIndices) {
if (countPlusIndices == null) return false;
var count = countPlusIndices[0];
var atomIndex;
for (var i = 1; i <= count; i++) if ((atomIndex = countPlusIndices[i]) >= 0 && this.models[this.atoms[atomIndex].modelIndex].isTrajectory) return true;

return false;
}, "~A");
$_M(c$, "getModelBitSet", 
function (atomList, allTrajectories) {
var bs =  new J.util.BS ();
var modelIndex = 0;
var isAll = (atomList == null);
var i0 = (isAll ? 0 : atomList.nextSetBit (0));
for (var i = i0; i >= 0 && i < this.atomCount; i = (isAll ? i + 1 : atomList.nextSetBit (i + 1))) {
bs.set (modelIndex = this.atoms[i].modelIndex);
if (allTrajectories) {
var iBase = this.models[modelIndex].trajectoryBaseIndex;
for (var j = 0; j < this.modelCount; j++) if (this.models[j].trajectoryBaseIndex == iBase) bs.set (j);

}i = this.models[modelIndex].firstAtomIndex + this.models[modelIndex].atomCount - 1;
}
return bs;
}, "J.util.BS,~B");
$_M(c$, "getIterativeModels", 
function (allowJmolData) {
var bs =  new J.util.BS ();
for (var i = 0; i < this.modelCount; i++) {
if (!allowJmolData && this.isJmolDataFrameForModel (i)) continue;
if (this.models[i].trajectoryBaseIndex == i) bs.set (i);
}
return bs;
}, "~B");
$_M(c$, "isTrajectorySubFrame", 
function (i) {
return (this.models[i].isTrajectory && this.models[i].trajectoryBaseIndex != i);
}, "~N");
$_M(c$, "selectDisplayedTrajectories", 
function (bs) {
for (var i = 0; i < this.modelCount; i++) {
if (this.models[i].isTrajectory && this.atoms[this.models[i].firstAtomIndex].modelIndex != i) bs.clear (i);
}
return bs;
}, "J.util.BS");
$_M(c$, "fillAtomData", 
function (atomData, mode) {
if ((mode & 4) != 0) {
this.getMolecules ();
atomData.bsMolecules =  new Array (this.molecules.length);
atomData.atomMolecule =  Clazz.newIntArray (this.atomCount, 0);
var bs;
for (var i = 0; i < this.molecules.length; i++) {
bs = atomData.bsMolecules[i] = this.molecules[i].atomList;
for (var iAtom = bs.nextSetBit (0); iAtom >= 0; iAtom = bs.nextSetBit (iAtom + 1)) atomData.atomMolecule[iAtom] = i;

}
}if ((mode & 8) != 0) {
var nH =  Clazz.newIntArray (1, 0);
atomData.hAtomRadius = this.viewer.getVanderwaalsMar (1) / 1000;
atomData.hAtoms = this.calculateHydrogens (atomData.bsSelected, nH, false, true, null);
atomData.hydrogenAtomCount = nH[0];
return;
}if (atomData.modelIndex < 0) atomData.firstAtomIndex = (atomData.bsSelected == null ? 0 : Math.max (0, atomData.bsSelected.nextSetBit (0)));
 else atomData.firstAtomIndex = this.models[atomData.modelIndex].firstAtomIndex;
atomData.lastModelIndex = atomData.firstModelIndex = (this.atomCount == 0 ? 0 : this.atoms[atomData.firstAtomIndex].modelIndex);
atomData.modelName = this.getModelNumberDotted (atomData.firstModelIndex);
this.fillADa (atomData, mode);
}, "J.atomdata.AtomData,~N");
$_M(c$, "getModelNumberDotted", 
function (modelIndex) {
return (this.modelCount < 1 || modelIndex >= this.modelCount || modelIndex < 0 ? "" : J.util.Escape.escapeModelFileNumber (this.modelFileNumbers[modelIndex]));
}, "~N");
$_M(c$, "getModelNumber", 
function (modelIndex) {
if (modelIndex == 2147483647) modelIndex = this.modelCount - 1;
return this.modelNumbers[modelIndex];
}, "~N");
$_M(c$, "getModelFileNumber", 
function (modelIndex) {
return this.modelFileNumbers[modelIndex];
}, "~N");
$_M(c$, "getModelProperties", 
function (modelIndex) {
return this.models[modelIndex].properties;
}, "~N");
$_M(c$, "getModelProperty", 
function (modelIndex, property) {
var props = this.models[modelIndex].properties;
return props == null ? null : props.getProperty (property);
}, "~N,~S");
$_M(c$, "getModelAuxiliaryInfo", 
function (modelIndex) {
return (modelIndex < 0 ? null : this.models[modelIndex].auxiliaryInfo);
}, "~N");
$_M(c$, "setModelAuxiliaryInfo", 
function (modelIndex, key, value) {
this.models[modelIndex].auxiliaryInfo.put (key, value);
}, "~N,~O,~O");
$_M(c$, "getModelAuxiliaryInfoValue", 
function (modelIndex, key) {
if (modelIndex < 0) {
return null;
}return this.models[modelIndex].auxiliaryInfo.get (key);
}, "~N,~S");
$_M(c$, "getModelAuxiliaryInfoBoolean", 
function (modelIndex, keyName) {
var info = this.models[modelIndex].auxiliaryInfo;
return (info != null && info.containsKey (keyName) && (info.get (keyName)).booleanValue ());
}, "~N,~S");
$_M(c$, "getModelAuxiliaryInfoInt", 
function (modelIndex, keyName) {
var info = this.models[modelIndex].auxiliaryInfo;
if (info != null && info.containsKey (keyName)) {
return (info.get (keyName)).intValue ();
}return -2147483648;
}, "~N,~S");
$_M(c$, "getModelAtomProperty", 
function (atom, text) {
var data = this.getModelAuxiliaryInfoValue (atom.modelIndex, text);
if (!(Clazz.instanceOf (data, Array))) return "";
var sdata = data;
var iatom = atom.index - this.models[atom.modelIndex].firstAtomIndex;
return (iatom < sdata.length ? sdata[iatom].toString () : "");
}, "J.modelset.Atom,~S");
$_M(c$, "getInsertionCountInModel", 
function (modelIndex) {
return this.models[modelIndex].nInsertions;
}, "~N");
c$.modelFileNumberFromFloat = $_M(c$, "modelFileNumberFromFloat", 
function (fDotM) {
var file = Clazz.doubleToInt (Math.floor (fDotM));
var model = Clazz.doubleToInt (Math.floor ((fDotM - file + 0.00001) * 10000));
while (model != 0 && model % 10 == 0) model /= 10;

return file * 1000000 + model;
}, "~N");
$_M(c$, "getAltLocCountInModel", 
function (modelIndex) {
return this.models[modelIndex].nAltLocs;
}, "~N");
$_M(c$, "getChainCount", 
function (addWater) {
var chainCount = 0;
for (var i = this.modelCount; --i >= 0; ) chainCount += this.models[i].getChainCount (addWater);

return chainCount;
}, "~B");
$_M(c$, "getBioPolymerCount", 
function () {
var polymerCount = 0;
for (var i = this.modelCount; --i >= 0; ) if (!this.isTrajectorySubFrame (i)) polymerCount += this.models[i].getBioPolymerCount ();

return polymerCount;
});
$_M(c$, "getBioPolymerCountInModel", 
function (modelIndex) {
return (modelIndex < 0 ? this.getBioPolymerCount () : this.isTrajectorySubFrame (modelIndex) ? 0 : this.models[modelIndex].getBioPolymerCount ());
}, "~N");
$_M(c$, "getPolymerPointsAndVectors", 
function (bs, vList, isTraceAlpha, sheetSmoothing) {
for (var i = 0; i < this.modelCount; ++i) this.models[i].getPolymerPointsAndVectors (bs, vList, isTraceAlpha, sheetSmoothing);

}, "J.util.BS,J.util.JmolList,~B,~N");
$_M(c$, "recalculateLeadMidpointsAndWingVectors", 
function (modelIndex) {
if (modelIndex < 0) {
for (var i = 0; i < this.modelCount; i++) if (!this.isTrajectorySubFrame (i)) this.models[i].recalculateLeadMidpointsAndWingVectors ();

return;
}this.models[modelIndex].recalculateLeadMidpointsAndWingVectors ();
}, "~N");
$_M(c$, "getPolymerLeadMidPoints", 
function (iModel, iPolymer) {
return this.models[iModel].getPolymerLeadMidPoints (iPolymer);
}, "~N,~N");
$_M(c$, "getChainCountInModel", 
function (modelIndex, countWater) {
if (modelIndex < 0) return this.getChainCount (countWater);
return this.models[modelIndex].getChainCount (countWater);
}, "~N,~B");
$_M(c$, "getGroupCount", 
function () {
var groupCount = 0;
for (var i = this.modelCount; --i >= 0; ) groupCount += this.models[i].getGroupCount ();

return groupCount;
});
$_M(c$, "getGroupCountInModel", 
function (modelIndex) {
if (modelIndex < 0) return this.getGroupCount ();
return this.models[modelIndex].getGroupCount ();
}, "~N");
$_M(c$, "calcSelectedGroupsCount", 
function (bsSelected) {
for (var i = this.modelCount; --i >= 0; ) this.models[i].calcSelectedGroupsCount (bsSelected);

}, "J.util.BS");
$_M(c$, "calcSelectedMonomersCount", 
function (bsSelected) {
for (var i = this.modelCount; --i >= 0; ) this.models[i].calcSelectedMonomersCount (bsSelected);

}, "J.util.BS");
$_M(c$, "calcRasmolHydrogenBonds", 
function (bsA, bsB, vHBonds, nucleicOnly, nMax, dsspIgnoreHydrogens, bsHBonds) {
var isSame = (bsB == null || bsA.equals (bsB));
for (var i = this.modelCount; --i >= 0; ) if (this.models[i].isBioModel && this.models[i].trajectoryBaseIndex == i) {
if (vHBonds == null) {
this.models[i].clearRasmolHydrogenBonds (bsA);
if (!isSame) this.models[i].clearRasmolHydrogenBonds (bsB);
}this.models[i].getRasmolHydrogenBonds (bsA, bsB, vHBonds, nucleicOnly, nMax, dsspIgnoreHydrogens, bsHBonds);
}
}, "J.util.BS,J.util.BS,J.util.JmolList,~B,~N,~B,J.util.BS");
$_M(c$, "calculateStraightness", 
function () {
if (this.getHaveStraightness ()) return;
var ctype = 'S';
var qtype = this.viewer.getQuaternionFrame ();
var mStep = this.viewer.getInt (553648146);
for (var i = this.modelCount; --i >= 0; ) this.models[i].calculateStraightness (this.viewer, ctype, qtype, mStep);

this.setHaveStraightness (true);
});
$_M(c$, "getAtomGroupQuaternions", 
function (bsAtoms, nMax, qtype) {
var n = 0;
var v =  new J.util.JmolList ();
for (var i = bsAtoms.nextSetBit (0); i >= 0 && n < nMax; i = bsAtoms.nextSetBit (i + 1)) {
var g = this.atoms[i].group;
var q = g.getQuaternion (qtype);
if (q == null) {
if (g.seqcode == -2147483648) q = g.getQuaternionFrame (this.atoms);
if (q == null) continue;
}n++;
v.addLast (q);
i = g.lastAtomIndex;
}
return v.toArray ( new Array (v.size ()));
}, "J.util.BS,~N,~S");
$_M(c$, "getPdbAtomData", 
function (bs, sb) {
if (this.atomCount == 0 || bs.nextSetBit (0) < 0) return "";
if (sb == null) sb =  new J.io.OutputStringBuilder (null, false);
var iModel = this.atoms[bs.nextSetBit (0)].modelIndex;
var iModelLast = -1;
var isPQR = "PQR".equals (sb.type);
var occTemp = "%6.2Q%6.2b          ";
if (isPQR) {
occTemp = "%8.4P%7.4V       ";
var charge = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) charge += this.atoms[i].getPartialCharge ();

sb.append ("REMARK   1 PQR file generated by Jmol " + J.viewer.Viewer.getJmolVersion ()).append ("\nREMARK   1 " + "created " + ( new java.util.Date ())).append ("\nREMARK   1 Forcefield Used: unknown\nREMARK   1").append ("\nREMARK   5").append ("\nREMARK   6 Total charge on this protein: " + charge + " e\nREMARK   6\n");
}var lastAtomIndex = bs.length () - 1;
var showModels = (iModel != this.atoms[lastAtomIndex].modelIndex);
var sbCONECT = (showModels ? null :  new J.util.SB ());
var isMultipleBondPDB = this.models[iModel].isPdbWithMultipleBonds;
var tokens;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var a = this.atoms[i];
if (showModels && a.modelIndex != iModelLast) {
if (iModelLast != -1) sb.append ("ENDMDL\n");
iModelLast = a.modelIndex;
sb.append ("MODEL     " + (iModelLast + 1) + "\n");
}var sa = a.getAtomName ();
var leftJustify = (a.getElementSymbol ().length == 2 || sa.length >= 4 || Character.isDigit (sa.charAt (0)));
var isBiomodel = this.models[a.modelIndex].isBioModel;
var isHetero = a.isHetero ();
if (!isBiomodel) tokens = (leftJustify ? J.modelset.LabelToken.compile (this.viewer, "HETATM%5.-5i %-4.4a%1AUNK %1c   1%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null) : J.modelset.LabelToken.compile (this.viewer, "HETATM%5.-5i  %-3.3a%1AUNK %1c   1%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null));
 else if (isHetero) tokens = (leftJustify ? J.modelset.LabelToken.compile (this.viewer, "HETATM%5.-5i %-4.4a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null) : J.modelset.LabelToken.compile (this.viewer, "HETATM%5.-5i  %-3.3a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null));
 else tokens = (leftJustify ? J.modelset.LabelToken.compile (this.viewer, "ATOM  %5.-5i %-4.4a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null) : J.modelset.LabelToken.compile (this.viewer, "ATOM  %5.-5i  %-3.3a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null));
var XX = a.getElementSymbolIso (false).toUpperCase ();
sb.append (J.modelset.LabelToken.formatLabelAtomArray (this.viewer, a, tokens, '\0', null)).append (XX.length == 1 ? " " + XX : XX.substring (0, 2)).append ("  \n");
if (!showModels && (!isBiomodel || isHetero || isMultipleBondPDB)) {
var bonds = a.getBonds ();
if (bonds != null) for (var j = 0; j < bonds.length; j++) {
var iThis = a.getAtomNumber ();
var a2 = bonds[j].getOtherAtom (a);
if (!bs.get (a2.index)) continue;
var n = bonds[j].getCovalentOrder ();
if (n == 1 && isMultipleBondPDB && !isHetero) continue;
var iOther = a2.getAtomNumber ();
switch (n) {
case 2:
case 3:
if (iOther < iThis) continue;
case 1:
sbCONECT.append ("CONECT").append (J.util.TextFormat.formatStringI ("%5i", "i", iThis));
for (var k = 0; k < n; k++) sbCONECT.append (J.util.TextFormat.formatStringI ("%5i", "i", iOther));

sbCONECT.appendC ('\n');
break;
}
}
}}
if (showModels) sb.append ("ENDMDL\n");
 else sb.append (sbCONECT.toString ());
return sb.toString ();
}, "J.util.BS,J.io.OutputStringBuilder");
$_M(c$, "getPdbData", 
function (modelIndex, type, bsSelected, parameters, sb) {
if (this.isJmolDataFrameForModel (modelIndex)) modelIndex = this.getJmolDataSourceFrame (modelIndex);
if (modelIndex < 0) return "";
var isPDB = this.models[modelIndex].isBioModel;
if (parameters == null && !isPDB) return null;
var model = this.models[modelIndex];
if (sb == null) sb =  new J.io.OutputStringBuilder (null, false);
var pdbCONECT =  new J.util.SB ();
var isDraw = (type.indexOf ("draw") >= 0);
var bsAtoms = null;
var bsWritten =  new J.util.BS ();
var ctype = '\u0000';
var tokens = J.modelset.LabelToken.compile (this.viewer, "ATOM  %-6i%4a%1A%3n %1c%4R%1E   ", '\0', null);
if (parameters == null) {
ctype = (type.length > 11 && type.indexOf ("quaternion ") >= 0 ? type.charAt (11) : 'R');
model.getPdbData (this.viewer, type, ctype, isDraw, bsSelected, sb, tokens, pdbCONECT, bsWritten);
bsAtoms = this.viewer.getModelUndeletedAtomsBitSet (modelIndex);
} else {
bsAtoms = parameters[0];
var dataX = parameters[1];
var dataY = parameters[2];
var dataZ = parameters[3];
var haveZ = (dataZ != null);
var minXYZ = parameters[4];
var maxXYZ = parameters[5];
var factors = parameters[6];
var center = parameters[7];
sb.append ("REMARK   6 Jmol PDB-encoded data: ").append (type).append (";\n");
sb.append ("REMARK   6 Jmol data").append (" min = ").append (J.util.Escape.eP (minXYZ)).append (" max = ").append (J.util.Escape.eP (maxXYZ)).append (" unScaledXyz = xyz * ").append (J.util.Escape.eP (factors)).append (" + ").append (J.util.Escape.eP (center)).append (";\n");
var strExtra = "";
var atomLast = null;
for (var i = bsAtoms.nextSetBit (0), n = 0; i >= 0; i = bsAtoms.nextSetBit (i + 1), n++) {
var x = dataX[n];
var y = dataY[n];
var z = (haveZ ? dataZ[n] : 0);
if (Float.isNaN (x) || Float.isNaN (y) || Float.isNaN (z)) continue;
var a = this.atoms[i];
sb.append (J.modelset.LabelToken.formatLabelAtomArray (this.viewer, a, tokens, '\0', null));
if (isPDB) bsWritten.set (i);
sb.append (J.util.TextFormat.sprintf ("%-8.2f%-8.2f%-10.2f    %6.3f          %2s    %s\n", "ssF", [a.getElementSymbolIso (false).toUpperCase (), strExtra, [x, y, z, 0]]));
if (atomLast != null && atomLast.getPolymerIndexInModel () == a.getPolymerIndexInModel ()) pdbCONECT.append ("CONECT").append (J.util.TextFormat.formatStringI ("%5i", "i", atomLast.getAtomNumber ())).append (J.util.TextFormat.formatStringI ("%5i", "i", a.getAtomNumber ())).appendC ('\n');
atomLast = a;
}
}sb.append (pdbCONECT.toString ());
if (isDraw) return sb.toString ();
bsSelected.and (bsAtoms);
if (isPDB) sb.append ("\n\n" + this.getProteinStructureState (bsWritten, false, ctype == 'R', 1));
return sb.toString ();
}, "~N,~S,J.util.BS,~A,J.io.OutputStringBuilder");
$_M(c$, "isJmolDataFrameForModel", 
function (modelIndex) {
return (modelIndex >= 0 && modelIndex < this.modelCount && this.models[modelIndex].isJmolDataFrame);
}, "~N");
$_M(c$, "isJmolDataFrameForAtom", 
($fz = function (atom) {
return (this.models[atom.modelIndex].isJmolDataFrame);
}, $fz.isPrivate = true, $fz), "J.modelset.Atom");
$_M(c$, "setJmolDataFrame", 
function (type, modelIndex, modelDataIndex) {
var model = this.models[type == null ? this.models[modelDataIndex].dataSourceFrame : modelIndex];
if (type == null) {
type = this.models[modelDataIndex].jmolFrameType;
}if (modelIndex >= 0) {
if (model.dataFrames == null) {
model.dataFrames =  new java.util.Hashtable ();
}this.models[modelDataIndex].dataSourceFrame = modelIndex;
this.models[modelDataIndex].jmolFrameType = type;
model.dataFrames.put (type, Integer.$valueOf (modelDataIndex));
}if (type.startsWith ("quaternion") && type.indexOf ("deriv") < 0) {
type = type.substring (0, type.indexOf (" "));
model.dataFrames.put (type, Integer.$valueOf (modelDataIndex));
}}, "~S,~N,~N");
$_M(c$, "getJmolDataFrameIndex", 
function (modelIndex, type) {
if (this.models[modelIndex].dataFrames == null) {
return -1;
}var index = this.models[modelIndex].dataFrames.get (type);
return (index == null ? -1 : index.intValue ());
}, "~N,~S");
$_M(c$, "clearDataFrameReference", 
function (modelIndex) {
for (var i = 0; i < this.modelCount; i++) {
var df = this.models[i].dataFrames;
if (df == null) {
continue;
}var e = df.values ().iterator ();
while (e.hasNext ()) {
if ((e.next ()).intValue () == modelIndex) {
e.remove ();
}}
}
}, "~N");
$_M(c$, "getJmolFrameType", 
function (modelIndex) {
return (modelIndex >= 0 && modelIndex < this.modelCount ? this.models[modelIndex].jmolFrameType : "modelSet");
}, "~N");
$_M(c$, "getJmolDataSourceFrame", 
function (modelIndex) {
return (modelIndex >= 0 && modelIndex < this.modelCount ? this.models[modelIndex].dataSourceFrame : -1);
}, "~N");
$_M(c$, "saveModelOrientation", 
function (modelIndex, orientation) {
this.models[modelIndex].orientation = orientation;
}, "~N,J.viewer.StateManager.Orientation");
$_M(c$, "getModelOrientation", 
function (modelIndex) {
return this.models[modelIndex].orientation;
}, "~N");
$_M(c$, "getPDBHeader", 
function (modelIndex) {
return (this.models[modelIndex].isBioModel ? this.models[modelIndex].getFullPDBHeader () : this.getFileHeader (modelIndex));
}, "~N");
$_M(c$, "getFileHeader", 
function (modelIndex) {
if (modelIndex < 0) return "";
if (this.models[modelIndex].isBioModel) return this.models[modelIndex].getFullPDBHeader ();
var info = this.getModelAuxiliaryInfoValue (modelIndex, "fileHeader");
if (info == null) info = this.modelSetName;
if (info != null) return info;
return "no header information found";
}, "~N");
$_M(c$, "getAltLocIndexInModel", 
function (modelIndex, alternateLocationID) {
if (alternateLocationID == '\0') {
return 0;
}var altLocList = this.getAltLocListInModel (modelIndex);
if (altLocList.length == 0) {
return 0;
}return altLocList.indexOf (alternateLocationID) + 1;
}, "~N,~S");
$_M(c$, "getInsertionCodeIndexInModel", 
function (modelIndex, insertionCode) {
if (insertionCode == '\0') return 0;
var codeList = this.getInsertionListInModel (modelIndex);
if (codeList.length == 0) return 0;
return codeList.indexOf (insertionCode) + 1;
}, "~N,~S");
$_M(c$, "getAltLocListInModel", 
function (modelIndex) {
if (modelIndex < 0) return "";
var str = this.getModelAuxiliaryInfoValue (modelIndex, "altLocs");
return (str == null ? "" : str);
}, "~N");
$_M(c$, "getInsertionListInModel", 
($fz = function (modelIndex) {
var str = this.getModelAuxiliaryInfoValue (modelIndex, "insertionCodes");
return (str == null ? "" : str);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getModelSymmetryCount", 
function (modelIndex) {
var operations;
return (this.models[modelIndex].biosymmetryCount > 0 || this.unitCells == null || this.unitCells[modelIndex] == null || (operations = this.unitCells[modelIndex].getSymmetryOperations ()) == null ? this.models[modelIndex].biosymmetryCount : operations.length);
}, "~N");
$_M(c$, "getSymmetryOperation", 
function (modelIndex, spaceGroup, symOp, pt1, pt2, drawID, labelOnly) {
var sginfo = this.getSymTemp (true).getSpaceGroupInfo (this, modelIndex, spaceGroup, symOp, pt1, pt2, drawID);
if (sginfo == null) return "";
return this.symTemp.getSymmetryOperationInfo (sginfo, symOp, drawID, labelOnly);
}, "~N,~S,~N,J.util.P3,J.util.P3,~S,~B");
$_M(c$, "getModelCellRange", 
function (modelIndex) {
if (this.unitCells == null) return null;
return this.unitCells[modelIndex].getCellRange ();
}, "~N");
$_M(c$, "getLastVibrationVector", 
function (modelIndex, tok) {
if (this.vibrations != null) for (var i = this.atomCount; --i >= 0; ) if ((modelIndex < 0 || this.atoms[i].modelIndex == modelIndex) && this.vibrations[i] != null && this.vibrations[i].length () > 0 && (tok == 0 || (tok == 135270424) == (Clazz.instanceOf (this.vibrations[i], J.util.ModulationSet)))) return i;

return -1;
}, "~N,~N");
$_M(c$, "getElementsPresentBitSet", 
function (modelIndex) {
if (modelIndex >= 0) return this.elementsPresent[modelIndex];
var bs =  new J.util.BS ();
for (var i = 0; i < this.modelCount; i++) bs.or (this.elementsPresent[i]);

return bs;
}, "~N");
$_M(c$, "getSymmetryInfoAsStringForModel", 
($fz = function (modelIndex) {
var unitCell = this.getUnitCell (modelIndex);
return (unitCell == null ? "no symmetry information" : unitCell.getSymmetryInfoString ());
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getMoleculeIndex", 
function (atomIndex, inModel) {
if (this.moleculeCount == 0) this.getMolecules ();
for (var i = 0; i < this.moleculeCount; i++) {
if (this.molecules[i].atomList.get (atomIndex)) return (inModel ? this.molecules[i].indexInModel : i);
}
return 0;
}, "~N,~B");
$_M(c$, "getMoleculeBitSet", 
function (bs) {
if (this.moleculeCount == 0) this.getMolecules ();
var bsResult = J.util.BSUtil.copy (bs);
var bsInitial = J.util.BSUtil.copy (bs);
var i = 0;
var bsTemp =  new J.util.BS ();
while ((i = bsInitial.length () - 1) >= 0) {
bsTemp = this.getMoleculeBitSetForAtom (i);
if (bsTemp == null) {
bsInitial.clear (i);
bsResult.clear (i);
continue;
}bsInitial.andNot (bsTemp);
bsResult.or (bsTemp);
}
return bsResult;
}, "J.util.BS");
$_M(c$, "getMoleculeBitSetForAtom", 
function (atomIndex) {
if (this.moleculeCount == 0) this.getMolecules ();
for (var i = 0; i < this.moleculeCount; i++) if (this.molecules[i].atomList.get (atomIndex)) return this.molecules[i].atomList;

return null;
}, "~N");
$_M(c$, "getModelDipole", 
function (modelIndex) {
if (modelIndex < 0) return null;
var dipole = this.getModelAuxiliaryInfoValue (modelIndex, "dipole");
if (dipole == null) dipole = this.getModelAuxiliaryInfoValue (modelIndex, "DIPOLE_VEC");
return dipole;
}, "~N");
$_M(c$, "calculateMolecularDipole", 
function (modelIndex) {
if (this.partialCharges == null || modelIndex < 0) return null;
var nPos = 0;
var nNeg = 0;
var cPos = 0;
var cNeg = 0;
var pos =  new J.util.V3 ();
var neg =  new J.util.V3 ();
for (var i = 0; i < this.atomCount; i++) {
if (this.atoms[i].modelIndex != modelIndex) continue;
var c = this.partialCharges[i];
if (c < 0) {
nNeg++;
cNeg += c;
neg.scaleAdd2 (c, this.atoms[i], neg);
} else if (c > 0) {
nPos++;
cPos += c;
pos.scaleAdd2 (c, this.atoms[i], pos);
}}
if (nNeg == 0 || nPos == 0) return null;
pos.scale (1 / cPos);
neg.scale (1 / cNeg);
pos.sub (neg);
J.util.Logger.warn ("CalculateMolecularDipole: this is an approximate result -- needs checking");
pos.scale (cPos * 4.8);
return pos;
}, "~N");
$_M(c$, "getMoleculeCountInModel", 
function (modelIndex) {
var n = 0;
if (this.moleculeCount == 0) this.getMolecules ();
if (modelIndex < 0) return this.moleculeCount;
for (var i = 0; i < this.modelCount; i++) {
if (modelIndex == i) n += this.models[i].moleculeCount;
}
return n;
}, "~N");
$_M(c$, "calcSelectedMoleculesCount", 
function (bsSelected) {
if (this.moleculeCount == 0) this.getMolecules ();
this.selectedMolecules.xor (this.selectedMolecules);
this.selectedMoleculeCount = 0;
var bsTemp =  new J.util.BS ();
for (var i = 0; i < this.moleculeCount; i++) {
J.util.BSUtil.copy2 (bsSelected, bsTemp);
bsTemp.and (this.molecules[i].atomList);
if (bsTemp.length () > 0) {
this.selectedMolecules.set (i);
this.selectedMoleculeCount++;
}}
}, "J.util.BS");
$_M(c$, "setCentroid", 
function (bs, minmax) {
var bsDelete = this.getNotInCentroid (bs, minmax);
if (bsDelete != null && bsDelete.nextSetBit (0) >= 0) this.viewer.deleteAtoms (bsDelete, false);
}, "J.util.BS,~A");
$_M(c$, "getNotInCentroid", 
($fz = function (bs, minmax) {
var iAtom0 = bs.nextSetBit (0);
if (iAtom0 < 0) return null;
var uc = this.getUnitCell (this.atoms[iAtom0].modelIndex);
return (uc == null ? null : uc.notInCentroid (this, bs, minmax));
}, $fz.isPrivate = true, $fz), "J.util.BS,~A");
$_M(c$, "getMolecules", 
function () {
if (this.moleculeCount > 0) return this.molecules;
if (this.molecules == null) this.molecules =  new Array (4);
this.moleculeCount = 0;
var m = null;
var bsModelAtoms =  new Array (this.modelCount);
var biobranches = null;
for (var i = 0; i < this.modelCount; i++) {
bsModelAtoms[i] = this.viewer.getModelUndeletedAtomsBitSet (i);
m = this.models[i];
m.moleculeCount = 0;
biobranches = m.getBioBranches (biobranches);
}
this.molecules = J.util.JmolMolecule.getMolecules (this.atoms, bsModelAtoms, biobranches, null);
this.moleculeCount = this.molecules.length;
for (var i = this.moleculeCount; --i >= 0; ) {
m = this.models[this.molecules[i].modelIndex];
m.firstMoleculeIndex = i;
m.moleculeCount++;
}
return this.molecules;
});
$_M(c$, "initializeBspf", 
function () {
if (this.bspf != null && this.bspf.isInitialized ()) return;
if (this.showRebondTimes) J.util.Logger.startTimer ("build bspf");
var bspf =  new J.bspt.Bspf (3);
if (J.util.Logger.debugging) J.util.Logger.debug ("sequential bspt order");
var bsNew = J.util.BSUtil.newBitSet (this.modelCount);
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
if (!atom.isDeleted () && !this.isTrajectorySubFrame (atom.modelIndex)) {
bspf.addTuple (this.models[atom.modelIndex].trajectoryBaseIndex, atom);
bsNew.set (atom.modelIndex);
}}
if (this.showRebondTimes) {
J.util.Logger.checkTimer ("build bspf", false);
bspf.stats ();
}for (var i = bsNew.nextSetBit (0); i >= 0; i = bsNew.nextSetBit (i + 1)) bspf.validateModel (i, true);

bspf.validate (true);
this.bspf = bspf;
});
$_M(c$, "initializeBspt", 
function (modelIndex) {
this.initializeBspf ();
if (this.bspf.isInitializedIndex (modelIndex)) return;
this.bspf.initialize (modelIndex, this.atoms, this.viewer.getModelUndeletedAtomsBitSet (modelIndex));
}, "~N");
$_M(c$, "setIteratorForPoint", 
function (iterator, modelIndex, pt, distance) {
if (modelIndex < 0) {
iterator.setCenter (pt, distance);
return;
}this.initializeBspt (modelIndex);
iterator.setModel (this, modelIndex, this.models[modelIndex].firstAtomIndex, 2147483647, pt, distance, null);
}, "J.api.AtomIndexIterator,~N,J.util.P3,~N");
$_M(c$, "setIteratorForAtom", 
function (iterator, modelIndex, atomIndex, distance, rd) {
if (modelIndex < 0) modelIndex = this.atoms[atomIndex].modelIndex;
modelIndex = this.models[modelIndex].trajectoryBaseIndex;
this.initializeBspt (modelIndex);
iterator.setModel (this, modelIndex, this.models[modelIndex].firstAtomIndex, atomIndex, this.atoms[atomIndex], distance, rd);
}, "J.api.AtomIndexIterator,~N,~N,~N,J.atomdata.RadiusData");
$_M(c$, "getSelectedAtomIterator", 
function (bsSelected, isGreaterOnly, modelZeroBased, hemisphereOnly, isMultiModel) {
this.initializeBspf ();
var iter;
if (isMultiModel) {
var bsModels = this.getModelBitSet (bsSelected, false);
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.initializeBspt (i);

iter =  new J.modelset.AtomIteratorWithinModelSet (bsModels);
} else {
iter =  new J.modelset.AtomIteratorWithinModel ();
}iter.initialize (this.bspf, bsSelected, isGreaterOnly, modelZeroBased, hemisphereOnly, this.viewer.isParallel ());
return iter;
}, "J.util.BS,~B,~B,~B,~B");
Clazz.overrideMethod (c$, "getBondCountInModel", 
function (modelIndex) {
return (modelIndex < 0 ? this.bondCount : this.models[modelIndex].getBondCount ());
}, "~N");
$_M(c$, "calculateStruts", 
function (bs1, bs2) {
return this.calculateStrutsMC (bs1, bs2);
}, "J.util.BS,J.util.BS");
$_M(c$, "calculateStrutsMC", 
function (bs1, bs2) {
this.makeConnections2 (0, 3.4028235E38, 32768, 12291, bs1, bs2, null, false, false, 0);
var iAtom = bs1.nextSetBit (0);
if (iAtom < 0) return 0;
var model = this.models[this.atoms[iAtom].modelIndex];
return (model.isBioModel ? model.calculateStruts (this, bs1, bs2) : 0);
}, "J.util.BS,J.util.BS");
$_M(c$, "getAtomCountInModel", 
function (modelIndex) {
return (modelIndex < 0 ? this.atomCount : this.models[modelIndex].atomCount);
}, "~N");
$_M(c$, "getModelAtomBitSetIncludingDeletedBs", 
function (bsModels) {
var bs =  new J.util.BS ();
if (bsModels == null && this.bsAll == null) this.bsAll = J.util.BSUtil.setAll (this.atomCount);
if (bsModels == null) bs.or (this.bsAll);
 else for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) bs.or (this.getModelAtomBitSetIncludingDeleted (i, false));

return bs;
}, "J.util.BS");
$_M(c$, "getModelAtomBitSetIncludingDeleted", 
function (modelIndex, asCopy) {
var bs = (modelIndex < 0 ? this.bsAll : this.models[modelIndex].bsAtoms);
if (bs == null) bs = this.bsAll = J.util.BSUtil.setAll (this.atomCount);
return (asCopy ? J.util.BSUtil.copy (bs) : bs);
}, "~N,~B");
$_M(c$, "getAtomBitsMaybeDeleted", 
function (tokType, specInfo) {
var info;
var bs;
switch (tokType) {
default:
return this.getAtomBitsMDa (tokType, specInfo);
case 1678770178:
case 1048585:
return this.getAtomBitsMDb (tokType, specInfo);
case 1073741864:
return this.getBasePairBits (specInfo);
case 1679429641:
var boxInfo = this.getBoxInfo (specInfo, 1);
bs = this.getAtomsWithin (boxInfo.getBoundBoxCornerVector ().length () + 0.0001, boxInfo.getBoundBoxCenter (), null, -1);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) if (!boxInfo.isWithin (this.atoms[i])) bs.clear (i);

return bs;
case 1095761925:
bs =  new J.util.BS ();
info = specInfo;
this.ptTemp1.set (info[0] / 1000, info[1] / 1000, info[2] / 1000);
var isAbsolute = !this.viewer.getBoolean (603979848);
for (var i = this.atomCount; --i >= 0; ) if (this.isInLatticeCell (i, this.ptTemp1, this.ptTemp2, isAbsolute)) bs.set (i);

return bs;
case 1095761926:
bs = J.util.BSUtil.newBitSet2 (0, this.atomCount);
info = specInfo;
var minmax = [Clazz.doubleToInt (info[0] / 1000) - 1, Clazz.doubleToInt (info[1] / 1000) - 1, Clazz.doubleToInt (info[2] / 1000) - 1, Clazz.doubleToInt (info[0] / 1000), Clazz.doubleToInt (info[1] / 1000), Clazz.doubleToInt (info[2] / 1000), 0];
for (var i = this.modelCount; --i >= 0; ) {
var uc = this.getUnitCell (i);
if (uc == null) {
J.util.BSUtil.andNot (bs, this.models[i].bsAtoms);
continue;
}bs.andNot (uc.notInCentroid (this, this.models[i].bsAtoms, minmax));
}
return bs;
case 1095761934:
return this.getMoleculeBitSet (specInfo);
case 1087373320:
return this.getSequenceBits (specInfo, null);
case 1048615:
info = specInfo;
var seqcodeA = info[0];
var seqcodeB = info[1];
var chainID = info[2];
bs =  new J.util.BS ();
var caseSensitive = this.viewer.getBoolean (603979822);
if (chainID >= 0 && chainID < 256 && !caseSensitive) chainID = J.modelset.AtomCollection.chainToUpper (chainID);
for (var i = this.modelCount; --i >= 0; ) if (this.models[i].isBioModel) this.models[i].selectSeqcodeRange (seqcodeA, seqcodeB, chainID, bs, caseSensitive);

return bs;
case 3145772:
bs = J.util.BSUtil.newBitSet (this.atomCount);
var modelIndex = -1;
var nOps = 0;
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
var bsSym = atom.getAtomSymmetry ();
if (bsSym != null) {
if (atom.modelIndex != modelIndex) {
modelIndex = atom.modelIndex;
if (this.getModelCellRange (modelIndex) == null) continue;
nOps = this.getModelSymmetryCount (modelIndex);
}var n = 0;
for (var j = nOps; --j >= 0; ) if (bsSym.get (j)) if (++n > 1) {
bs.set (i);
break;
}
}}
return bs;
case 1089470478:
return J.util.BSUtil.copy (this.bsSymmetry == null ? this.bsSymmetry = J.util.BSUtil.newBitSet (this.atomCount) : this.bsSymmetry);
case 1614417948:
bs =  new J.util.BS ();
var unitcell = this.viewer.getCurrentUnitCell ();
if (unitcell == null) return bs;
this.ptTemp1.set (1, 1, 1);
for (var i = this.atomCount; --i >= 0; ) if (this.isInLatticeCell (i, this.ptTemp1, this.ptTemp2, false)) bs.set (i);

return bs;
}
}, "~N,~O");
$_M(c$, "isInLatticeCell", 
($fz = function (i, cell, ptTemp, isAbsolute) {
var iModel = this.atoms[i].modelIndex;
var uc = this.getUnitCell (iModel);
ptTemp.setT (this.atoms[i]);
return (uc != null && uc.checkUnitCell (uc, cell, ptTemp, isAbsolute));
}, $fz.isPrivate = true, $fz), "~N,J.util.P3,J.util.P3,~B");
$_M(c$, "getAtomsWithinRD", 
function (distance, bs, withinAllModels, rd) {
var bsResult =  new J.util.BS ();
var bsCheck = this.getIterativeModels (false);
bs = J.util.BSUtil.andNot (bs, this.viewer.getDeletedAtoms ());
var iter = this.getSelectedAtomIterator (null, false, false, false, false);
if (withinAllModels) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) for (var iModel = this.modelCount; --iModel >= 0; ) {
if (!bsCheck.get (iModel)) continue;
if (distance < 0) {
this.getAtomsWithin (distance, this.atoms[i].getFractionalUnitCoordPt (true), bsResult, -1);
continue;
}this.setIteratorForAtom (iter, iModel, i, distance, rd);
iter.addAtoms (bsResult);
}

} else {
bsResult.or (bs);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (distance < 0) {
this.getAtomsWithin (distance, this.atoms[i], bsResult, this.atoms[i].modelIndex);
continue;
}this.setIteratorForAtom (iter, -1, i, distance, rd);
iter.addAtoms (bsResult);
}
}iter.release ();
return bsResult;
}, "~N,J.util.BS,~B,J.atomdata.RadiusData");
$_M(c$, "getGroupsWithin", 
function (nResidues, bs) {
var bsCheck = this.getIterativeModels (false);
var bsResult =  new J.util.BS ();
for (var iModel = this.modelCount; --iModel >= 0; ) {
if (!bsCheck.get (iModel) || !this.models[iModel].isBioModel) continue;
this.models[iModel].getGroupsWithin (nResidues, bs, bsResult);
}
return bsResult;
}, "~N,J.util.BS");
$_M(c$, "getAtomsWithin", 
function (distance, coord, bsResult, modelIndex) {
if (bsResult == null) bsResult =  new J.util.BS ();
if (distance < 0) {
distance = -distance;
for (var i = this.atomCount; --i >= 0; ) {
var atom = this.atoms[i];
if (modelIndex >= 0 && this.atoms[i].modelIndex != modelIndex) continue;
if (!bsResult.get (i) && atom.getFractionalUnitDistance (coord, this.ptTemp1, this.ptTemp2) <= distance) bsResult.set (atom.index);
}
return bsResult;
}var bsCheck = this.getIterativeModels (true);
var iter = this.getSelectedAtomIterator (null, false, false, false, false);
for (var iModel = this.modelCount; --iModel >= 0; ) {
if (!bsCheck.get (iModel)) continue;
this.setIteratorForAtom (iter, -1, this.models[iModel].firstAtomIndex, -1, null);
iter.setCenter (coord, distance);
iter.addAtoms (bsResult);
}
iter.release ();
return bsResult;
}, "~N,J.util.P3,J.util.BS,~N");
$_M(c$, "getBasePairBits", 
($fz = function (specInfo) {
var bs =  new J.util.BS ();
if (specInfo.length % 2 != 0) return bs;
var bsA = null;
var bsB = null;
var vHBonds =  new J.util.JmolList ();
if (specInfo.length == 0) {
bsA = bsB = this.viewer.getModelUndeletedAtomsBitSet (-1);
this.calcRasmolHydrogenBonds (bsA, bsB, vHBonds, true, 1, false, null);
} else {
for (var i = 0; i < specInfo.length; ) {
bsA = this.getSequenceBits (specInfo.substring (i, ++i), null);
if (bsA.cardinality () == 0) continue;
bsB = this.getSequenceBits (specInfo.substring (i, ++i), null);
if (bsB.cardinality () == 0) continue;
this.calcRasmolHydrogenBonds (bsA, bsB, vHBonds, true, 1, false, null);
}
}var bsAtoms =  new J.util.BS ();
for (var i = vHBonds.size (); --i >= 0; ) {
var b = vHBonds.get (i);
bsAtoms.set (b.atom1.index);
bsAtoms.set (b.atom2.index);
}
return this.getAtomBitsMDb (1087373318, bsAtoms);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getSequenceBits", 
function (specInfo, bs) {
if (bs == null) bs = this.viewer.getModelUndeletedAtomsBitSet (-1);
var bsResult =  new J.util.BS ();
if (specInfo.length > 0) for (var i = 0; i < this.modelCount; ++i) if (this.models[i].isBioModel) this.models[i].getSequenceBits (specInfo, bs, bsResult);

return bsResult;
}, "~S,J.util.BS");
$_M(c$, "deleteBonds", 
function (bsBonds, isFullModel) {
if (!isFullModel) {
var bsA =  new J.util.BS ();
var bsB =  new J.util.BS ();
for (var i = bsBonds.nextSetBit (0); i >= 0; i = bsBonds.nextSetBit (i + 1)) {
var atom1 = this.bonds[i].atom1;
if (this.models[atom1.modelIndex].isModelKit) continue;
bsA.clearAll ();
bsB.clearAll ();
bsA.set (atom1.index);
bsB.set (this.bonds[i].getAtomIndex2 ());
this.addStateScript ("connect ", null, bsA, bsB, "delete", false, true);
}
}this.dBb (bsBonds, isFullModel);
}, "J.util.BS,~B");
$_M(c$, "makeConnections2", 
function (minDistance, maxDistance, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy) {
if (bsBonds == null) bsBonds =  new J.util.BS ();
var matchAny = (order == 65535);
var matchNull = (order == 131071);
if (matchNull) order = 1;
var matchHbond = J.modelset.Bond.isOrderH (order);
var identifyOnly = false;
var modifyOnly = false;
var createOnly = false;
var autoAromatize = false;
var minDistanceSquared = minDistance * minDistance;
var maxDistanceSquared = maxDistance * maxDistance;
switch (connectOperation) {
case 12291:
return this.deleteConnections (minDistance, maxDistance, order, bsA, bsB, isBonds, matchNull, minDistanceSquared, maxDistanceSquared);
case 603979874:
case 1073741852:
if (order != 515) return this.autoBond (bsA, bsB, bsBonds, isBonds, matchHbond, connectOperation == 603979874);
modifyOnly = true;
autoAromatize = true;
break;
case 1087373321:
identifyOnly = true;
break;
case 1073742025:
modifyOnly = true;
break;
case 1073741904:
createOnly = true;
break;
}
this.defaultCovalentMad = this.viewer.getMadBond ();
var minDistanceIsFractionRadius = (minDistance < 0);
var maxDistanceIsFractionRadius = (maxDistance < 0);
if (minDistanceIsFractionRadius) minDistance = -minDistance;
if (maxDistanceIsFractionRadius) maxDistance = -maxDistance;
var mad = this.getDefaultMadFromOrder (order);
var nNew = 0;
var nModified = 0;
var bondAB = null;
var m = (isBonds ? 1 : this.atomCount);
var atomA = null;
var atomB = null;
var dAB = 0;
var dABcalc = 0;
var newOrder = (order | 131072);
for (var iA = bsA.nextSetBit (0); iA >= 0; iA = bsA.nextSetBit (iA + 1)) {
if (isBonds) {
bondAB = this.bonds[iA];
atomA = bondAB.atom1;
atomB = bondAB.atom2;
} else {
atomA = this.atoms[iA];
if (atomA.isDeleted ()) continue;
}for (var iB = (isBonds ? m : bsB.nextSetBit (0)); iB >= 0; iB = (isBonds ? iB - 1 : bsB.nextSetBit (iB + 1))) {
if (!isBonds) {
if (iB == iA) continue;
atomB = this.atoms[iB];
if (atomA.modelIndex != atomB.modelIndex || atomB.isDeleted ()) continue;
if (atomA.altloc != atomB.altloc && atomA.altloc != '\0' && atomB.altloc != '\0') continue;
bondAB = atomA.getBond (atomB);
}if (bondAB == null && (identifyOnly || modifyOnly) || bondAB != null && createOnly) continue;
var distanceSquared = atomA.distanceSquared (atomB);
if (minDistanceIsFractionRadius || maxDistanceIsFractionRadius) {
dAB = atomA.distance (atomB);
dABcalc = atomA.getBondingRadiusFloat () + atomB.getBondingRadiusFloat ();
}if ((minDistanceIsFractionRadius ? dAB < dABcalc * minDistance : distanceSquared < minDistanceSquared) || (maxDistanceIsFractionRadius ? dAB > dABcalc * maxDistance : distanceSquared > maxDistanceSquared)) continue;
if (bondAB != null) {
if (!identifyOnly && !matchAny) {
bondAB.setOrder (order);
this.bsAromatic.clear (bondAB.index);
}if (!identifyOnly || matchAny || order == bondAB.order || newOrder == bondAB.order || matchHbond && bondAB.isHydrogen ()) {
bsBonds.set (bondAB.index);
nModified++;
}} else {
bsBonds.set (this.bondAtoms (atomA, atomB, order, mad, bsBonds, energy, addGroup, true).index);
nNew++;
}}
}
if (autoAromatize) this.assignAromaticBondsBs (true, bsBonds);
if (!identifyOnly) this.shapeManager.setShapeSizeBs (1, -2147483648, null, bsBonds);
return [nNew, nModified];
}, "~N,~N,~N,~N,J.util.BS,J.util.BS,J.util.BS,~B,~B,~N");
$_M(c$, "autoBondBs4", 
function (bsA, bsB, bsExclude, bsBonds, mad, preJmol11_9_24) {
if (preJmol11_9_24) return this.autoBond_Pre_11_9_24 (bsA, bsB, bsExclude, bsBonds, mad);
if (this.atomCount == 0) return 0;
if (mad == 0) mad = 1;
if (this.maxBondingRadius == 1.4E-45) this.findMaxRadii ();
var bondTolerance = this.viewer.getFloat (570425348);
var minBondDistance = this.viewer.getFloat (570425364);
var minBondDistance2 = minBondDistance * minBondDistance;
var nNew = 0;
if (this.showRebondTimes) J.util.Logger.startTimer ("autobond");
var lastModelIndex = -1;
var isAll = (bsA == null);
var bsCheck;
var i0;
if (isAll) {
i0 = 0;
bsCheck = null;
} else {
if (bsA.equals (bsB)) {
bsCheck = bsA;
} else {
bsCheck = J.util.BSUtil.copy (bsA);
bsCheck.or (bsB);
}i0 = bsCheck.nextSetBit (0);
}var iter = this.getSelectedAtomIterator (null, false, false, true, false);
for (var i = i0; i >= 0 && i < this.atomCount; i = (isAll ? i + 1 : bsCheck.nextSetBit (i + 1))) {
var isAtomInSetA = (isAll || bsA.get (i));
var isAtomInSetB = (isAll || bsB.get (i));
var atom = this.atoms[i];
if (atom.isDeleted ()) continue;
var modelIndex = atom.modelIndex;
if (modelIndex != lastModelIndex) {
lastModelIndex = modelIndex;
if (this.isJmolDataFrameForModel (modelIndex)) {
i = this.models[modelIndex].firstAtomIndex + this.models[modelIndex].atomCount - 1;
continue;
}}var myBondingRadius = atom.getBondingRadiusFloat ();
if (myBondingRadius == 0) continue;
var isFirstExcluded = (bsExclude != null && bsExclude.get (i));
var searchRadius = myBondingRadius + this.maxBondingRadius + bondTolerance;
this.setIteratorForAtom (iter, -1, i, searchRadius, null);
while (iter.hasNext ()) {
var atomNear = this.atoms[iter.next ()];
if (atomNear.isDeleted ()) continue;
var atomIndexNear = atomNear.index;
var isNearInSetA = (isAll || bsA.get (atomIndexNear));
var isNearInSetB = (isAll || bsB.get (atomIndexNear));
if (!isNearInSetA && !isNearInSetB || !(isAtomInSetA && isNearInSetB || isAtomInSetB && isNearInSetA) || isFirstExcluded && bsExclude.get (atomIndexNear)) continue;
var order = J.modelset.BondCollection.getBondOrderFull (myBondingRadius, atomNear.getBondingRadiusFloat (), iter.foundDistance2 (), minBondDistance2, bondTolerance);
if (order > 0 && this.checkValencesAndBond (atom, atomNear, order, mad, bsBonds)) nNew++;
}
iter.release ();
}
if (this.showRebondTimes) J.util.Logger.checkTimer ("autoBond", false);
return nNew;
}, "J.util.BS,J.util.BS,J.util.BS,J.util.BS,~N,~B");
$_M(c$, "autoBond_Pre_11_9_24", 
($fz = function (bsA, bsB, bsExclude, bsBonds, mad) {
if (this.atomCount == 0) return 0;
if (mad == 0) mad = 1;
if (this.maxBondingRadius == 1.4E-45) this.findMaxRadii ();
var bondTolerance = this.viewer.getFloat (570425348);
var minBondDistance = this.viewer.getFloat (570425364);
var minBondDistance2 = minBondDistance * minBondDistance;
var nNew = 0;
this.initializeBspf ();
var lastModelIndex = -1;
for (var i = this.atomCount; --i >= 0; ) {
var isAtomInSetA = (bsA == null || bsA.get (i));
var isAtomInSetB = (bsB == null || bsB.get (i));
if (!isAtomInSetA && !isAtomInSetB) continue;
var atom = this.atoms[i];
if (atom.isDeleted ()) continue;
var modelIndex = atom.modelIndex;
if (modelIndex != lastModelIndex) {
lastModelIndex = modelIndex;
if (this.isJmolDataFrameForModel (modelIndex)) {
for (; --i >= 0; ) if (this.atoms[i].modelIndex != modelIndex) break;

i++;
continue;
}}var myBondingRadius = atom.getBondingRadiusFloat ();
if (myBondingRadius == 0) continue;
var searchRadius = myBondingRadius + this.maxBondingRadius + bondTolerance;
this.initializeBspt (modelIndex);
var iter = this.bspf.getCubeIterator (modelIndex);
iter.initialize (atom, searchRadius, true);
while (iter.hasMoreElements ()) {
var atomNear = iter.nextElement ();
if (atomNear === atom || atomNear.isDeleted ()) continue;
var atomIndexNear = atomNear.index;
var isNearInSetA = (bsA == null || bsA.get (atomIndexNear));
var isNearInSetB = (bsB == null || bsB.get (atomIndexNear));
if (!isNearInSetA && !isNearInSetB || bsExclude != null && bsExclude.get (atomIndexNear) && bsExclude.get (i)) continue;
if (!(isAtomInSetA && isNearInSetB || isAtomInSetB && isNearInSetA)) continue;
var order = J.modelset.BondCollection.getBondOrderFull (myBondingRadius, atomNear.getBondingRadiusFloat (), iter.foundDistance2 (), minBondDistance2, bondTolerance);
if (order > 0) {
if (this.checkValencesAndBond (atom, atomNear, order, mad, bsBonds)) nNew++;
}}
iter.release ();
}
return nNew;
}, $fz.isPrivate = true, $fz), "J.util.BS,J.util.BS,J.util.BS,J.util.BS,~N");
$_M(c$, "autoBond", 
($fz = function (bsA, bsB, bsBonds, isBonds, matchHbond, legacyAutoBond) {
if (isBonds) {
var bs = bsA;
bsA =  new J.util.BS ();
bsB =  new J.util.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
bsA.set (this.bonds[i].atom1.index);
bsB.set (this.bonds[i].atom2.index);
}
}return [matchHbond ? this.autoHbond (bsA, bsB, false) : this.autoBondBs4 (bsA, bsB, null, bsBonds, this.viewer.getMadBond (), legacyAutoBond), 0];
}, $fz.isPrivate = true, $fz), "J.util.BS,J.util.BS,J.util.BS,~B,~B,~B");
$_M(c$, "autoHbond", 
function (bsA, bsB, onlyIfHaveCalculated) {
if (onlyIfHaveCalculated) {
var bsModels = this.getModelBitSet (bsA, false);
for (var i = bsModels.nextSetBit (0); i >= 0 && onlyIfHaveCalculated; i = bsModels.nextSetBit (i + 1)) onlyIfHaveCalculated = !this.models[i].hasRasmolHBonds;

if (onlyIfHaveCalculated) return 0;
}var haveHAtoms = false;
for (var i = bsA.nextSetBit (0); i >= 0; i = bsA.nextSetBit (i + 1)) if (this.atoms[i].getElementNumber () == 1) {
haveHAtoms = true;
break;
}
var bsHBonds =  new J.util.BS ();
var useRasMol = this.viewer.getBoolean (603979853);
if (bsB == null || useRasMol && !haveHAtoms) {
J.util.Logger.info ((bsB == null ? "DSSP " : "RasMol") + " pseudo-hbond calculation");
this.calcRasmolHydrogenBonds (bsA, bsB, null, false, 2147483647, false, bsHBonds);
return -J.util.BSUtil.cardinalityOf (bsHBonds);
}J.util.Logger.info (haveHAtoms ? "Standard Hbond calculation" : "Jmol pseudo-hbond calculation");
var bsCO = null;
if (!haveHAtoms) {
bsCO =  new J.util.BS ();
for (var i = bsA.nextSetBit (0); i >= 0; i = bsA.nextSetBit (i + 1)) {
var atomID = this.atoms[i].atomID;
switch (atomID) {
case 64:
case 4:
case 14:
case 15:
case 16:
case 17:
bsCO.set (i);
break;
}
}
}var maxXYDistance = this.viewer.getFloat (570425361);
var minAttachedAngle = (this.viewer.getFloat (570425360) * 3.141592653589793 / 180);
var hbondMax2 = maxXYDistance * maxXYDistance;
var hbondMin2 = J.modelset.ModelCollection.hbondMin * J.modelset.ModelCollection.hbondMin;
var hxbondMin2 = 1;
var hxbondMax2 = (maxXYDistance > J.modelset.ModelCollection.hbondMin ? hbondMin2 : hbondMax2);
var hxbondMax = (maxXYDistance > J.modelset.ModelCollection.hbondMin ? J.modelset.ModelCollection.hbondMin : maxXYDistance);
var nNew = 0;
var d2 = 0;
var v1 =  new J.util.V3 ();
var v2 =  new J.util.V3 ();
if (this.showRebondTimes && J.util.Logger.debugging) J.util.Logger.startTimer ("hbond");
var C = null;
var D = null;
var iter = this.getSelectedAtomIterator (bsB, false, false, false, false);
for (var i = bsA.nextSetBit (0); i >= 0; i = bsA.nextSetBit (i + 1)) {
var atom = this.atoms[i];
var elementNumber = atom.getElementNumber ();
var isH = (elementNumber == 1);
if (!isH && (haveHAtoms || elementNumber != 7 && elementNumber != 8) || isH && !haveHAtoms) continue;
var min2;
var max2;
var dmax;
var firstIsCO;
if (isH) {
var b = atom.bonds;
if (b == null) continue;
var isOK = false;
for (var j = 0; j < b.length && !isOK; j++) {
var a2 = b[j].getOtherAtom (atom);
var element = a2.getElementNumber ();
isOK = (element == 7 || element == 8);
}
if (!isOK) continue;
dmax = hxbondMax;
min2 = hxbondMin2;
max2 = hxbondMax2;
firstIsCO = false;
} else {
dmax = maxXYDistance;
min2 = hbondMin2;
max2 = hbondMax2;
firstIsCO = bsCO.get (i);
}this.setIteratorForAtom (iter, -1, atom.index, dmax, null);
while (iter.hasNext ()) {
var atomNear = this.atoms[iter.next ()];
var elementNumberNear = atomNear.getElementNumber ();
if (atomNear === atom || !isH && elementNumberNear != 7 && elementNumberNear != 8 || isH && elementNumberNear == 1 || (d2 = iter.foundDistance2 ()) < min2 || d2 > max2 || firstIsCO && bsCO.get (atomNear.index) || atom.isBonded (atomNear)) {
continue;
}if (minAttachedAngle > 0) {
v1.sub2 (atom, atomNear);
if ((D = J.modelset.ModelCollection.checkMinAttachedAngle (atom, minAttachedAngle, v1, v2, haveHAtoms)) == null) continue;
v1.scale (-1);
if ((C = J.modelset.ModelCollection.checkMinAttachedAngle (atomNear, minAttachedAngle, v1, v2, haveHAtoms)) == null) continue;
}var energy = 0;
var bo;
if (isH && !Float.isNaN (C.x) && !Float.isNaN (D.x)) {
bo = 4096;
energy = J.modelset.HBond.getEnergy (Math.sqrt (d2), C.distance (atom), C.distance (D), atomNear.distance (D)) / 1000;
} else {
bo = 2048;
}bsHBonds.set (this.addHBond (atom, atomNear, bo, energy));
nNew++;
}
}
iter.release ();
this.shapeManager.setShapeSizeBs (1, -2147483648, null, bsHBonds);
if (this.showRebondTimes) J.util.Logger.checkTimer ("hbond", false);
return (haveHAtoms ? nNew : -nNew);
}, "J.util.BS,J.util.BS,~B");
c$.checkMinAttachedAngle = $_M(c$, "checkMinAttachedAngle", 
($fz = function (atom1, minAngle, v1, v2, haveHAtoms) {
var bonds = atom1.bonds;
if (bonds == null || bonds.length == 0) return J.util.P3.new3 (NaN, 0, 0);
var X = null;
var dMin = 3.4028235E38;
for (var i = bonds.length; --i >= 0; ) if (bonds[i].isCovalent ()) {
var atomA = bonds[i].getOtherAtom (atom1);
if (!haveHAtoms && atomA.getElementNumber () == 1) continue;
v2.sub2 (atom1, atomA);
var d = v2.angle (v1);
if (d < minAngle) return null;
if (d < dMin) {
X = atomA;
dMin = d;
}}
return X;
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,~N,J.util.V3,J.util.V3,~B");
$_M(c$, "setStructureIndexes", 
function () {
var id;
var idnew = 0;
var lastid = -1;
var imodel = -1;
var lastmodel = -1;
for (var i = 0; i < this.atomCount; i++) {
if ((imodel = this.atoms[i].modelIndex) != lastmodel) {
idnew = 0;
lastmodel = imodel;
lastid = -1;
}if ((id = this.atoms[i].getStrucNo ()) != lastid && id != 0) {
this.atoms[i].getGroup ().setStrucNo (++idnew);
lastid = idnew;
}}
});
$_M(c$, "getProteinStructureState", 
function (bsAtoms, taintedOnly, needPhiPsi, mode) {
if (!this.isPDB) return "";
for (var i = 0; i < this.modelCount; i++) if (this.models[i].isBioModel) return this.models[i].getProteinStructureState (bsAtoms, taintedOnly, needPhiPsi, mode);

return "";
}, "J.util.BS,~B,~B,~N");
$_M(c$, "getModelInfoAsString", 
function () {
var sb =  new J.util.SB ().append ("<models count=\"");
sb.appendI (this.modelCount).append ("\" modelSetHasVibrationVectors=\"").append (this.modelSetHasVibrationVectors () + "\">\n<properties>");
if (this.modelSetProperties != null) {
var e = this.modelSetProperties.propertyNames ();
while (e.hasMoreElements ()) {
var propertyName = e.nextElement ();
sb.append ("\n <property name=\"").append (propertyName).append ("\" value=").append (J.util.Escape.eS (this.modelSetProperties.getProperty (propertyName))).append (" />");
}
sb.append ("\n</properties>");
}for (var i = 0; i < this.modelCount; ++i) {
sb.append ("\n<model index=\"").appendI (i).append ("\" n=\"").append (this.getModelNumberDotted (i)).append ("\" id=").append (J.util.Escape.eS ("" + this.getModelAuxiliaryInfoValue (i, "modelID")));
var ib = this.viewer.getJDXBaseModelIndex (i);
if (ib != i) sb.append (" baseModelId=").append (J.util.Escape.eS (this.getModelAuxiliaryInfoValue (ib, "jdxModelID")));
sb.append (" name=").append (J.util.Escape.eS (this.getModelName (i))).append (" title=").append (J.util.Escape.eS (this.getModelTitle (i))).append (" hasVibrationVectors=\"").appendB (this.viewer.modelHasVibrationVectors (i)).append ("\" />");
}
sb.append ("\n</models>");
return sb.toString ();
});
$_M(c$, "getSymmetryInfoAsString", 
function () {
var sb =  new J.util.SB ().append ("Symmetry Information:");
for (var i = 0; i < this.modelCount; ++i) sb.append ("\nmodel #").append (this.getModelNumberDotted (i)).append ("; name=").append (this.getModelName (i)).append ("\n").append (this.getSymmetryInfoAsStringForModel (i));

return sb.toString ();
});
$_M(c$, "getAtomsConnected", 
function (min, max, intType, bs) {
var bsResult =  new J.util.BS ();
var nBonded =  Clazz.newIntArray (this.atomCount, 0);
var i;
var ishbond = (intType == 30720);
var isall = (intType == 65535);
for (var ibond = 0; ibond < this.bondCount; ibond++) {
var bond = this.bonds[ibond];
if (isall || bond.is (intType) || ishbond && bond.isHydrogen ()) {
if (bs.get (bond.atom1.index)) {
nBonded[i = bond.atom2.index]++;
bsResult.set (i);
}if (bs.get (bond.atom2.index)) {
nBonded[i = bond.atom1.index]++;
bsResult.set (i);
}}}
var nonbonded = (min == 0);
for (i = this.atomCount; --i >= 0; ) {
var n = nBonded[i];
if (n < min || n > max) bsResult.clear (i);
 else if (nonbonded && n == 0) bsResult.set (i);
}
return bsResult;
}, "~N,~N,~N,J.util.BS");
$_M(c$, "getUnitCellInfoText", 
function () {
var modelIndex = this.viewer.getCurrentModelIndex ();
if (modelIndex < 0) return "no single current model";
var c = this.getUnitCell (modelIndex);
return (c == null ? "not applicable" : c.getUnitCellInfo ());
});
$_M(c$, "getSymTemp", 
function (forceNew) {
if (this.symTemp == null || forceNew) this.symTemp = J.api.Interface.getOptionInterface ("symmetry.Symmetry");
return this.symTemp;
}, "~B");
$_M(c$, "createModels", 
function (n) {
var newModelCount = this.modelCount + n;
var newModels = J.util.ArrayUtil.arrayCopyObject (this.models, newModelCount);
this.validateBspf (false);
this.modelNumbers = J.util.ArrayUtil.arrayCopyI (this.modelNumbers, newModelCount);
this.modelFileNumbers = J.util.ArrayUtil.arrayCopyI (this.modelFileNumbers, newModelCount);
this.modelNumbersForAtomLabel = J.util.ArrayUtil.arrayCopyS (this.modelNumbersForAtomLabel, newModelCount);
this.modelNames = J.util.ArrayUtil.arrayCopyS (this.modelNames, newModelCount);
this.frameTitles = J.util.ArrayUtil.arrayCopyS (this.frameTitles, newModelCount);
var f = Clazz.doubleToInt (this.getModelFileNumber (this.modelCount - 1) / 1000000) + 1;
for (var i = this.modelCount, pt = 0; i < newModelCount; i++) {
this.modelNumbers[i] = i + this.modelCount;
this.modelFileNumbers[i] = f * 1000000 + (++pt);
this.modelNumbersForAtomLabel[i] = this.modelNames[i] = f + "." + pt;
}
this.thisStateModel = -1;
var group3Lists = this.getModelSetAuxiliaryInfoValue ("group3Lists");
if (group3Lists != null) {
var group3Counts = this.getModelSetAuxiliaryInfoValue ("group3Counts");
group3Lists = J.util.ArrayUtil.arrayCopyS (group3Lists, newModelCount);
group3Counts = J.util.ArrayUtil.arrayCopyII (group3Counts, newModelCount);
this.modelSetAuxiliaryInfo.put ("group3Lists", group3Lists);
this.modelSetAuxiliaryInfo.put ("group3Counts", group3Counts);
}this.unitCells = J.util.ArrayUtil.arrayCopyObject (this.unitCells, newModelCount);
for (var i = this.modelCount; i < newModelCount; i++) {
newModels[i] =  new J.modelset.Model (this, i, -1, null, null, null);
newModels[i].loadState = " model create #" + i + ";";
}
this.models = newModels;
this.modelCount = newModelCount;
}, "~N");
$_M(c$, "deleteModel", 
function (modelIndex, firstAtomIndex, nAtoms, bsAtoms, bsBonds) {
if (modelIndex < 0) {
this.validateBspf (false);
this.bsAll = null;
this.resetMolecules ();
this.isBbcageDefault = false;
this.calcBoundBoxDimensions (null, 1);
return;
}this.modelNumbers = J.util.ArrayUtil.deleteElements (this.modelNumbers, modelIndex, 1);
this.modelFileNumbers = J.util.ArrayUtil.deleteElements (this.modelFileNumbers, modelIndex, 1);
this.modelNumbersForAtomLabel = J.util.ArrayUtil.deleteElements (this.modelNumbersForAtomLabel, modelIndex, 1);
this.modelNames = J.util.ArrayUtil.deleteElements (this.modelNames, modelIndex, 1);
this.frameTitles = J.util.ArrayUtil.deleteElements (this.frameTitles, modelIndex, 1);
this.thisStateModel = -1;
var group3Lists = this.getModelSetAuxiliaryInfoValue ("group3Lists");
var group3Counts = this.getModelSetAuxiliaryInfoValue ("group3Counts");
var ptm = modelIndex + 1;
if (group3Lists != null && group3Lists[ptm] != null) {
for (var i = Clazz.doubleToInt (group3Lists[ptm].length / 6); --i >= 0; ) if (group3Counts[ptm][i] > 0) {
group3Counts[0][i] -= group3Counts[ptm][i];
if (group3Counts[0][i] == 0) group3Lists[0] = group3Lists[0].substring (0, i * 6) + ",[" + group3Lists[0].substring (i * 6 + 2);
}
}if (group3Lists != null) {
this.modelSetAuxiliaryInfo.put ("group3Lists", J.util.ArrayUtil.deleteElements (group3Lists, modelIndex, 1));
this.modelSetAuxiliaryInfo.put ("group3Counts", J.util.ArrayUtil.deleteElements (group3Counts, modelIndex, 1));
}if (this.unitCells != null) {
this.unitCells = J.util.ArrayUtil.deleteElements (this.unitCells, modelIndex, 1);
}for (var i = this.stateScripts.size (); --i >= 0; ) {
if (!this.stateScripts.get (i).deleteAtoms (modelIndex, bsBonds, bsAtoms)) {
this.stateScripts.remove (i);
}}
this.deleteModelAtoms (firstAtomIndex, nAtoms, bsAtoms);
this.viewer.deleteModelAtoms (firstAtomIndex, nAtoms, bsAtoms);
}, "~N,~N,~N,J.util.BS,J.util.BS");
$_M(c$, "getMoInfo", 
function (modelIndex) {
var sb =  new J.util.SB ();
for (var m = 0; m < this.modelCount; m++) {
if (modelIndex >= 0 && m != modelIndex) {
continue;
}var moData = this.viewer.getModelAuxiliaryInfoValue (m, "moData");
if (moData == null) {
continue;
}var mos = (moData.get ("mos"));
var nOrb = (mos == null ? 0 : mos.size ());
if (nOrb == 0) {
continue;
}for (var i = nOrb; --i >= 0; ) {
var mo = mos.get (i);
var type = mo.get ("type");
if (type == null) {
type = "";
}var units = mo.get ("energyUnits");
if (units == null) {
units = "";
}var occ = mo.get ("occupancy");
if (occ != null) {
type = "occupancy " + occ.floatValue () + " " + type;
}var sym = mo.get ("symmetry");
if (sym != null) {
type += sym;
}var energy = "" + mo.get ("energy");
if (Float.isNaN (J.util.Parser.parseFloatStr (energy))) sb.append (J.util.TextFormat.sprintf ("model %-2s;  mo %-2i # %s\n", "sis", [this.getModelNumberDotted (m), Integer.$valueOf (i + 1), type]));
 else sb.append (J.util.TextFormat.sprintf ("model %-2s;  mo %-2i # energy %-8.3f %s %s\n", "sifss", [this.getModelNumberDotted (m), Integer.$valueOf (i + 1), mo.get ("energy"), units, type]));
}
}
return sb.toString ();
}, "~N");
$_M(c$, "assignAtom", 
function (atomIndex, type, autoBond) {
if (type == null) type = "C";
var atom = this.atoms[atomIndex];
var bs =  new J.util.BS ();
var wasH = (atom.getElementNumber () == 1);
var atomicNumber = J.util.Elements.elementNumberFromSymbol (type, true);
var isDelete = false;
if (atomicNumber > 0) {
this.setElement (atom, atomicNumber);
this.viewer.setShapeSizeRD (0, this.viewer.getDefaultRadiusData (), J.util.BSUtil.newAndSetBit (atomIndex));
this.setAtomName (atomIndex, type + atom.getAtomNumber ());
if (!this.models[atom.modelIndex].isModelKit) this.taintAtom (atomIndex, 0);
} else if (type.equals ("Pl")) {
atom.setFormalCharge (atom.getFormalCharge () + 1);
} else if (type.equals ("Mi")) {
atom.setFormalCharge (atom.getFormalCharge () - 1);
} else if (type.equals ("X")) {
isDelete = true;
} else if (!type.equals (".")) {
return;
}this.removeUnnecessaryBonds (atom, isDelete);
var dx = 0;
if (atom.getCovalentBondCount () == 1) if (wasH) {
dx = 1.50;
} else if (!wasH && atomicNumber == 1) {
dx = 1.0;
}if (dx != 0) {
var v = J.util.V3.newVsub (atom, this.atoms[atom.getBondedAtomIndex (0)]);
var d = v.length ();
v.normalize ();
v.scale (dx - d);
this.setAtomCoordRelative (atomIndex, v.x, v.y, v.z);
}var bsA = J.util.BSUtil.newAndSetBit (atomIndex);
if (atomicNumber != 1 && autoBond) {
this.validateBspf (false);
bs = this.getAtomsWithinRD (1.0, bsA, false, null);
bs.andNot (bsA);
if (bs.nextSetBit (0) >= 0) this.viewer.deleteAtoms (bs, false);
bs = this.viewer.getModelUndeletedAtomsBitSet (atom.modelIndex);
bs.andNot (this.getAtomBitsMDa (1613758476, null));
this.makeConnections2 (0.1, 1.8, 1, 1073741904, bsA, bs, null, false, false, 0);
}this.viewer.addHydrogens (bsA, false, true);
}, "~N,~S,~B");
$_M(c$, "deleteAtoms", 
function (bs) {
if (bs == null) return;
var bsBonds =  new J.util.BS ();
for (var i = bs.nextSetBit (0); i >= 0 && i < this.atomCount; i = bs.nextSetBit (i + 1)) this.atoms[i].deleteBonds (bsBonds);

for (var i = 0; i < this.modelCount; i++) {
this.models[i].bsAtomsDeleted.or (bs);
this.models[i].bsAtomsDeleted.and (this.models[i].bsAtoms);
}
this.deleteBonds (bsBonds, false);
}, "J.util.BS");
$_M(c$, "getModelCml", 
function (bs, atomsMax, addBonds) {
var sb =  new J.util.SB ();
var nAtoms = J.util.BSUtil.cardinalityOf (bs);
if (nAtoms == 0) return "";
J.io.XmlUtil.openTag (sb, "molecule");
J.io.XmlUtil.openTag (sb, "atomArray");
var bsAtoms =  new J.util.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (--atomsMax < 0) break;
var atom = this.atoms[i];
var name = atom.getAtomName ();
J.util.TextFormat.simpleReplace (name, "\"", "''");
bsAtoms.set (atom.index);
J.io.XmlUtil.appendTag (sb, "atom/", ["id", "a" + (atom.index + 1), "title", atom.getAtomName (), "elementType", atom.getElementSymbol (), "x3", "" + atom.x, "y3", "" + atom.y, "z3", "" + atom.z]);
}
J.io.XmlUtil.closeTag (sb, "atomArray");
if (addBonds) {
J.io.XmlUtil.openTag (sb, "bondArray");
for (var i = 0; i < this.bondCount; i++) {
var bond = this.bonds[i];
var a1 = bond.atom1;
var a2 = bond.atom2;
if (!bsAtoms.get (a1.index) || !bsAtoms.get (a2.index)) continue;
var order = J.util.JmolEdge.getCmlBondOrder (bond.order);
if (order == null) continue;
J.io.XmlUtil.appendTag (sb, "bond/", ["atomRefs2", "a" + (bond.atom1.index + 1) + " a" + (bond.atom2.index + 1), "order", order]);
}
J.io.XmlUtil.closeTag (sb, "bondArray");
}J.io.XmlUtil.closeTag (sb, "molecule");
return sb.toString ();
}, "J.util.BS,~N,~B");
$_M(c$, "adjustAtomArrays", 
function (map, i0, atomCount) {
this.atomCount = atomCount;
for (var i = i0; i < atomCount; i++) {
this.atoms[i] = this.atoms[map[i]];
this.atoms[i].index = i;
var m = this.models[this.atoms[i].modelIndex];
if (m.firstAtomIndex == map[i]) m.firstAtomIndex = i;
m.bsAtoms.set (i);
}
if (this.vibrations != null) for (var i = i0; i < atomCount; i++) this.vibrations[i] = this.vibrations[map[i]];

if (this.occupancies != null) for (var i = i0; i < atomCount; i++) this.occupancies[i] = this.occupancies[map[i]];

if (this.bfactor100s != null) for (var i = i0; i < atomCount; i++) this.bfactor100s[i] = this.bfactor100s[map[i]];

if (this.partialCharges != null) for (var i = i0; i < atomCount; i++) this.partialCharges[i] = this.partialCharges[map[i]];

if (this.atomTensorList != null) {
for (var i = i0; i < atomCount; i++) {
var list = this.atomTensorList[i] = this.atomTensorList[map[i]];
for (var j = list.length; --j >= 0; ) {
var t = list[j];
if (t != null) t.atomIndex1 = map[t.atomIndex1];
}
}
}if (this.atomNames != null) for (var i = i0; i < atomCount; i++) this.atomNames[i] = this.atomNames[map[i]];

if (this.atomTypes != null) for (var i = i0; i < atomCount; i++) this.atomTypes[i] = this.atomTypes[map[i]];

if (this.atomSerials != null) for (var i = i0; i < atomCount; i++) this.atomSerials[i] = this.atomSerials[map[i]];

}, "~A,~N,~N");
$_M(c$, "growAtomArrays", 
function (newLength) {
this.atoms = J.util.ArrayUtil.arrayCopyObject (this.atoms, newLength);
if (this.vibrations != null) this.vibrations = J.util.ArrayUtil.arrayCopyObject (this.vibrations, newLength);
if (this.occupancies != null) this.occupancies = J.util.ArrayUtil.arrayCopyByte (this.occupancies, newLength);
if (this.bfactor100s != null) this.bfactor100s = J.util.ArrayUtil.arrayCopyShort (this.bfactor100s, newLength);
if (this.partialCharges != null) this.partialCharges = J.util.ArrayUtil.arrayCopyF (this.partialCharges, newLength);
if (this.atomTensorList != null) this.atomTensorList = J.util.ArrayUtil.arrayCopyObject (this.atomTensorList, newLength);
if (this.atomNames != null) this.atomNames = J.util.ArrayUtil.arrayCopyS (this.atomNames, newLength);
if (this.atomTypes != null) this.atomTypes = J.util.ArrayUtil.arrayCopyS (this.atomTypes, newLength);
if (this.atomSerials != null) this.atomSerials = J.util.ArrayUtil.arrayCopyI (this.atomSerials, newLength);
}, "~N");
$_M(c$, "addAtom", 
function (modelIndex, group, atomicAndIsotopeNumber, atomName, atomSerial, atomSite, xyz, radius, vib, formalCharge, partialCharge, occupancy, bfactor, tensors, isHetero, specialAtomID, atomSymmetry) {
var atom =  new J.modelset.Atom (modelIndex, this.atomCount, xyz, radius, atomSymmetry, atomSite, atomicAndIsotopeNumber, formalCharge, isHetero);
this.models[modelIndex].atomCount++;
this.models[modelIndex].bsAtoms.set (this.atomCount);
if (J.util.Elements.isElement (atomicAndIsotopeNumber, 1)) this.models[modelIndex].hydrogenCount++;
if (this.atomCount >= this.atoms.length) this.growAtomArrays (this.atomCount + 100);
this.atoms[this.atomCount] = atom;
this.setBFactor (this.atomCount, bfactor);
this.setOccupancy (this.atomCount, occupancy);
this.setPartialCharge (this.atomCount, partialCharge);
if (tensors != null) this.setAtomTensors (this.atomCount, tensors);
atom.group = group;
atom.colixAtom = this.viewer.getColixAtomPalette (atom, J.constant.EnumPalette.CPK.id);
if (atomName != null) {
var i;
if ((i = atomName.indexOf ('\0')) >= 0) {
if (this.atomTypes == null) this.atomTypes =  new Array (this.atoms.length);
this.atomTypes[this.atomCount] = atomName.substring (i + 1);
atomName = atomName.substring (0, i);
}atom.atomID = specialAtomID;
if (specialAtomID == 0) {
if (this.atomNames == null) this.atomNames =  new Array (this.atoms.length);
this.atomNames[this.atomCount] = atomName.intern ();
}}if (atomSerial != -2147483648) {
if (this.atomSerials == null) this.atomSerials =  Clazz.newIntArray (this.atoms.length, 0);
this.atomSerials[this.atomCount] = atomSerial;
}if (vib != null) this.setVibrationVector (this.atomCount, vib);
this.atomCount++;
return atom;
}, "~N,J.modelset.Group,~N,~S,~N,~N,J.util.P3,~N,J.util.V3,~N,~N,~N,~N,J.util.JmolList,~B,~N,J.util.BS");
$_M(c$, "getInlineData", 
function (modelIndex) {
var data = null;
if (modelIndex >= 0) data = this.models[modelIndex].loadScript;
 else for (modelIndex = this.modelCount; --modelIndex >= 0; ) if ((data = this.models[modelIndex].loadScript).length () > 0) break;

var pt = data.lastIndexOf ("data \"");
if (pt < 0) return null;
pt = data.indexOf2 ("\"", pt + 7);
var pt2 = data.lastIndexOf ("end \"");
if (pt2 < pt || pt < 0) return null;
return data.substring2 (pt + 2, pt2);
}, "~N");
$_M(c$, "isAtomPDB", 
function (i) {
return i >= 0 && this.models[this.atoms[i].modelIndex].isBioModel;
}, "~N");
$_M(c$, "isAtomAssignable", 
function (i) {
return i >= 0 && this.atoms[i].modelIndex == this.modelCount - 1;
}, "~N");
$_M(c$, "getGroupAtom", 
function (atom, offset, name) {
var g = atom.group;
var monomerIndex = g.getMonomerIndex ();
if (monomerIndex < 0) return -1;
var groups = g.getGroups ();
var ipt = monomerIndex + offset;
if (ipt >= 0 && ipt < groups.length) {
var m = groups[ipt];
if (offset == 1 && !m.isConnectedPrevious ()) return -1;
if ("0".equals (name)) return m.leadAtomIndex;
for (var i = m.firstAtomIndex; i <= m.lastAtomIndex; i++) if (name == null || name.equalsIgnoreCase (this.atoms[i].getAtomName ())) return i;

}return -1;
}, "J.modelset.Atom,~N,~S");
$_M(c$, "haveModelKit", 
function () {
for (var i = 0; i < this.modelCount; i++) if (this.models[i].isModelKit) return true;

return false;
});
$_M(c$, "getModelKitStateBitset", 
function (bs, bsDeleted) {
var bs1 = J.util.BSUtil.copy (bsDeleted);
for (var i = 0; i < this.modelCount; i++) if (!this.models[i].isModelKit) bs1.andNot (this.models[i].bsAtoms);

return J.util.BSUtil.deleteBits (bs, bs1);
}, "J.util.BS,J.util.BS");
$_M(c$, "setAtomNamesAndNumbers", 
function (iFirst, baseAtomIndex, mergeSet) {
if (baseAtomIndex < 0) iFirst = this.models[this.atoms[iFirst].modelIndex].firstAtomIndex;
if (this.atomSerials == null) this.atomSerials =  Clazz.newIntArray (this.atomCount, 0);
if (this.atomNames == null) this.atomNames =  new Array (this.atomCount);
var isZeroBased = this.isXYZ && this.viewer.getBoolean (603979978);
var lastModelIndex = 2147483647;
var atomNo = 1;
for (var i = iFirst; i < this.atomCount; ++i) {
var atom = this.atoms[i];
if (atom.modelIndex != lastModelIndex) {
lastModelIndex = atom.modelIndex;
atomNo = (isZeroBased ? 0 : 1);
}if (i >= -baseAtomIndex) {
if (this.atomSerials[i] == 0 || baseAtomIndex < 0) this.atomSerials[i] = (i < baseAtomIndex ? mergeSet.atomSerials[i] : atomNo);
if (this.atomNames[i] == null || baseAtomIndex < 0) this.atomNames[i] = (atom.getElementSymbol () + this.atomSerials[i]).intern ();
}if (!this.models[lastModelIndex].isModelKit || atom.getElementNumber () > 0 && !atom.isDeleted ()) atomNo++;
}
}, "~N,~N,J.modelset.AtomCollection");
$_M(c$, "setUnitCellOffset", 
function (modelIndex, pt, ijk) {
for (var i = modelIndex; i < this.modelCount; i++) {
if (i < 0 || modelIndex >= 0 && i != modelIndex && this.models[i].trajectoryBaseIndex != modelIndex) continue;
var unitCell = this.getUnitCell (i);
if (unitCell == null) continue;
if (pt == null) unitCell.setOffset (ijk);
 else unitCell.setOffsetPt (pt);
}
}, "~N,J.util.P3,~N");
$_M(c$, "connect", 
function (connections) {
this.resetMolecules ();
var bsDelete =  new J.util.BS ();
for (var i = 0; i < connections.length; i++) {
var f = connections[i];
if (f == null || f.length < 2) continue;
var index1 = Clazz.floatToInt (f[0]);
var addGroup = (index1 < 0);
if (addGroup) index1 = -1 - index1;
var index2 = Clazz.floatToInt (f[1]);
if (index2 < 0 || index1 >= this.atomCount || index2 >= this.atomCount) continue;
var order = (f.length > 2 ? Clazz.floatToInt (f[2]) : 1);
if (order < 0) order &= 0xFFFF;
var mad = (f.length > 3 ? Clazz.floatToShort (1000 * connections[i][3]) : this.getDefaultMadFromOrder (order));
if (order == 0 || mad == 0 && order != 32768 && !J.modelset.Bond.isOrderH (order)) {
var b = this.atoms[index1].getBond (this.atoms[index2]);
if (b != null) bsDelete.set (b.index);
continue;
}var energy = (f.length > 4 ? f[4] : 0);
this.bondAtoms (this.atoms[index1], this.atoms[index2], order, mad, null, energy, addGroup, true);
}
if (bsDelete.nextSetBit (0) >= 0) this.deleteBonds (bsDelete, false);
}, "~A");
$_M(c$, "allowSpecAtom", 
function () {
return this.modelCount != 1 || this.models[0].isBioModel;
});
$_M(c$, "setFrameDelayMs", 
function (millis, bsModels) {
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.models[this.models[i].trajectoryBaseIndex].frameDelay = millis;

}, "~N,J.util.BS");
$_M(c$, "getFrameDelayMs", 
function (i) {
return (i < this.models.length && i >= 0 ? this.models[this.models[i].trajectoryBaseIndex].frameDelay : 0);
}, "~N");
$_M(c$, "getModelIndexFromId", 
function (id) {
var haveFile = (id.indexOf ("#") >= 0);
var isBaseModel = id.toLowerCase ().endsWith (".basemodel");
if (isBaseModel) id = id.substring (0, id.length - 10);
var errCode = -1;
var fname = null;
for (var i = 0; i < this.modelCount; i++) {
var mid = this.getModelAuxiliaryInfoValue (i, "modelID");
var mnum = (id.startsWith ("~") ? "~" + this.getModelNumberDotted (i) : null);
if (mnum == null && mid == null && (mid = this.getModelTitle (i)) == null) continue;
if (haveFile) {
fname = this.getModelFileName (i) + "#";
mid = fname + mid;
}if (id.equalsIgnoreCase (mid) || id.equalsIgnoreCase (mnum)) return (isBaseModel ? this.viewer.getJDXBaseModelIndex (i) : i);
if (fname != null && id.startsWith (fname)) errCode = -2;
}
return (fname == null && !haveFile ? -2 : errCode);
}, "~S");
$_M(c$, "getAuxiliaryInfo", 
function (bsModels) {
var info = this.modelSetAuxiliaryInfo;
if (info == null) return null;
var models =  new J.util.JmolList ();
for (var i = 0; i < this.modelCount; ++i) {
if (bsModels != null && !bsModels.get (i)) {
continue;
}var modelinfo = this.getModelAuxiliaryInfo (i);
models.addLast (modelinfo);
}
info.put ("models", models);
return info;
}, "J.util.BS");
$_M(c$, "getDihedralMap", 
function (alist) {
var list =  new J.util.JmolList ();
var n = alist.length;
var ai = null;
var aj = null;
var ak = null;
var al = null;
for (var i = n - 1; --i >= 0; ) for (var j = n; --j > i; ) {
ai = this.atoms[alist[i]];
aj = this.atoms[alist[j]];
if (ai.isBonded (aj)) {
for (var k = n; --k >= 0; ) if (k != i && k != j && (ak = this.atoms[alist[k]]).isBonded (ai)) for (var l = n; --l >= 0; ) if (l != i && l != j && l != k && (al = this.atoms[alist[l]]).isBonded (aj)) {
var a =  Clazz.newIntArray (4, 0);
a[0] = ak.index;
a[1] = ai.index;
a[2] = aj.index;
a[3] = al.index;
list.addLast (a);
}

}}

n = list.size ();
var ilist = J.util.ArrayUtil.newInt2 (n);
for (var i = n; --i >= 0; ) ilist[n - i - 1] = list.get (i);

return ilist;
}, "~A");
$_M(c$, "setModulation", 
function (bs, isOn, t) {
if (this.bsModulated == null) this.bsModulated =  new J.util.BS ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var v = this.getVibration (i, false);
if (!(Clazz.instanceOf (v, J.util.ModulationSet))) continue;
var ms = v;
var a = this.atoms[i];
var wasEnabled = ms.enabled;
switch (ms.setModT (isOn, t)) {
case 0:
continue;
case 1:
case 2:
a.sub (ms);
break;
case 3:
this.getUnitCell (a.modelIndex).toCartesian (ms, true);
if (wasEnabled) a.add (ms.prevSetting);
ms.setModT (true, 2147483647);
a.sub (ms);
break;
case 4:
ms.setModT (true, 2147483647);
if (!wasEnabled) a.sub (ms);
break;
}
this.bsModulated.setBitTo (i, ms.enabled);
}
}, "J.util.BS,~B,~N");
$_M(c$, "getBoundBoxOrientation", 
function (type, bsAtoms) {
var j0 = bsAtoms.nextSetBit (0);
if (j0 < 0) return "{0 0 0 1}";
var n = (this.vOrientations == null ? 0 : this.vOrientations.length);
if (n == 0) {
var av =  new Array (3375);
n = 0;
var p4 =  new J.util.P4 ();
for (var i = -7; i <= 7; i++) for (var j = -7; j <= 7; j++) for (var k = 0; k <= 14; k++, n++) if ((av[n] = J.util.V3.new3 (i / 7, j / 7, k / 14)).length () > 1) --n;



this.vOrientations =  new Array (n);
for (var i = n; --i >= 0; ) {
var cos = Math.sqrt (1 - av[i].lengthSquared ());
if (Float.isNaN (cos)) cos = 0;
p4.set (av[i].x, av[i].y, av[i].z, cos);
this.vOrientations[i] = J.util.Quaternion.newP4 (p4);
}
}var pt =  new J.util.P3 ();
var vMin = 3.4028235E38;
var q;
var qBest = null;
var bBest = null;
var v;
for (var i = 0; i < n; i++) {
q = this.vOrientations[i];
var b =  new J.util.BoxInfo ();
b.setMargin (0);
for (var j = j0; j >= 0; j = bsAtoms.nextSetBit (j + 1)) b.addBoundBoxPoint (q.transformP2 (this.atoms[j], pt));

switch (type) {
default:
case 1313866247:
case 1073741863:
v = (b.bbCorner1.x - b.bbCorner0.x) * (b.bbCorner1.y - b.bbCorner0.y) * (b.bbCorner1.z - b.bbCorner0.z);
break;
case 1112541205:
v = b.bbCorner1.x - b.bbCorner0.x;
break;
case 1112541206:
v = b.bbCorner1.y - b.bbCorner0.y;
break;
case 1112541207:
v = b.bbCorner1.z - b.bbCorner0.z;
break;
}
if (v < vMin) {
qBest = q;
bBest = b;
vMin = v;
}}
if (type != 1313866247 && type != 1073741863) return qBest.toString ();
q = J.util.Quaternion.newQ (qBest);
var dx = bBest.bbCorner1.x - bBest.bbCorner0.x;
var dy = bBest.bbCorner1.y - bBest.bbCorner0.y;
var dz = bBest.bbCorner1.z - bBest.bbCorner0.z;
if (dx < dy) {
pt.set (0, 0, 1);
q = J.util.Quaternion.newVA (pt, 90).mulQ (q);
var f = dx;
dx = dy;
dy = f;
}if (dy < dz) {
if (dz > dx) {
pt.set (0, 1, 0);
q = J.util.Quaternion.newVA (pt, 90).mulQ (q);
var f = dx;
dx = dz;
dz = f;
}pt.set (1, 0, 0);
q = J.util.Quaternion.newVA (pt, 90).mulQ (q);
var f = dy;
dy = dz;
dz = f;
}return (type == 1313866247 ? vMin + "\t{" + dx + " " + dy + " " + dz + "}" : q.getTheta () == 0 ? "{0 0 0 1}" : q.toString ());
}, "~N,J.util.BS");
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
this.modelIndex = 0;
this.bsBonds = null;
this.bsAtoms1 = null;
this.bsAtoms2 = null;
this.script1 = null;
this.script2 = null;
this.inDefinedStateBlock = false;
Clazz.instantialize (this, arguments);
}, J.modelset.ModelCollection, "StateScript");
Clazz.makeConstructor (c$, 
function (a, b, c, d, e, f, g) {
this.modelIndex = a;
this.script1 = b;
this.bsBonds = J.util.BSUtil.copy (c);
this.bsAtoms1 = J.util.BSUtil.copy (d);
this.bsAtoms2 = J.util.BSUtil.copy (e);
this.script2 = f;
this.inDefinedStateBlock = g;
}, "~N,~S,J.util.BS,J.util.BS,J.util.BS,~S,~B");
$_M(c$, "isValid", 
function () {
return this.script1 != null && this.script1.length > 0 && (this.bsBonds == null || this.bsBonds.nextSetBit (0) >= 0) && (this.bsAtoms1 == null || this.bsAtoms1.nextSetBit (0) >= 0) && (this.bsAtoms2 == null || this.bsAtoms2.nextSetBit (0) >= 0);
});
Clazz.overrideMethod (c$, "toString", 
function () {
if (!this.isValid ()) return "";
var a = J.util.SB.newS (this.script1);
if (this.bsBonds != null) a.append (" ").append (J.util.Escape.eBond (this.bsBonds));
if (this.bsAtoms1 != null) a.append (" ").append (J.util.Escape.eBS (this.bsAtoms1));
if (this.bsAtoms2 != null) a.append (" ").append (J.util.Escape.eBS (this.bsAtoms2));
if (this.script2 != null) a.append (" ").append (this.script2);
var b = a.toString ();
if (!b.endsWith (";")) b += ";";
return b;
});
$_M(c$, "isConnect", 
function () {
return (this.script1.indexOf ("connect") >= 0);
});
$_M(c$, "deleteAtoms", 
function (a, b, c) {
if (a == this.modelIndex) return false;
if (a > this.modelIndex) {
return true;
}J.util.BSUtil.deleteBits (this.bsBonds, b);
J.util.BSUtil.deleteBits (this.bsAtoms1, c);
J.util.BSUtil.deleteBits (this.bsAtoms2, c);
return this.isValid ();
}, "~N,J.util.BS,J.util.BS");
$_M(c$, "setModelIndex", 
function (a) {
this.modelIndex = a;
}, "~N");
c$ = Clazz.p0p ();
Clazz.defineStatics (c$,
"hbondMin", 2.5);
});
