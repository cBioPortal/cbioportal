Clazz.declarePackage ("J.modelset");
Clazz.load (["J.modelset.ModelCollection", "J.util.Matrix3f", "$.Matrix4f", "$.V3"], "J.modelset.ModelSet", ["java.lang.Boolean", "java.util.Hashtable", "J.api.Interface", "J.atomdata.RadiusData", "J.util.AxisAngle4f", "$.BS", "$.BSUtil", "$.JmolList", "$.JmolMolecule", "$.Measure", "$.P3", "$.Quaternion", "$.SB", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.selectionHaloEnabled = false;
this.echoShapeActive = false;
this.modelSetTypeName = null;
this.closest = null;
this.pointGroup = null;
this.matTemp = null;
this.matInv = null;
this.mat4 = null;
this.mat4t = null;
this.vTemp = null;
Clazz.instantialize (this, arguments);
}, J.modelset, "ModelSet", J.modelset.ModelCollection);
Clazz.prepareFields (c$, function () {
this.closest =  new Array (1);
this.matTemp =  new J.util.Matrix3f ();
this.matInv =  new J.util.Matrix3f ();
this.mat4 =  new J.util.Matrix4f ();
this.mat4t =  new J.util.Matrix4f ();
this.vTemp =  new J.util.V3 ();
});
Clazz.makeConstructor (c$, 
function (viewer, name) {
Clazz.superConstructor (this, J.modelset.ModelSet, []);
this.viewer = viewer;
this.modelSetName = name;
}, "J.viewer.Viewer,~S");
$_M(c$, "releaseModelSet", 
function () {
this.models = null;
this.closest[0] = null;
Clazz.superCall (this, J.modelset.ModelSet, "releaseModelSet", []);
});
$_M(c$, "setSelectionHaloEnabled", 
function (selectionHaloEnabled) {
this.selectionHaloEnabled = selectionHaloEnabled;
}, "~B");
$_M(c$, "getSelectionHaloEnabled", 
function () {
return this.selectionHaloEnabled;
});
$_M(c$, "getEchoStateActive", 
function () {
return this.echoShapeActive;
});
$_M(c$, "setEchoStateActive", 
function (TF) {
this.echoShapeActive = TF;
}, "~B");
$_M(c$, "getModelSetTypeName", 
function () {
return this.modelSetTypeName;
});
$_M(c$, "getModelNumberIndex", 
function (modelNumber, useModelNumber, doSetTrajectory) {
if (useModelNumber) {
for (var i = 0; i < this.modelCount; i++) if (this.modelNumbers[i] == modelNumber || modelNumber < 1000000 && this.modelNumbers[i] == 1000000 + modelNumber) return i;

return -1;
}for (var i = 0; i < this.modelCount; i++) if (this.modelFileNumbers[i] == modelNumber) {
if (doSetTrajectory && this.isTrajectory (i)) this.setTrajectory (i);
return i;
}
return -1;
}, "~N,~B,~B");
$_M(c$, "getBitSetTrajectories", 
function () {
if (this.trajectorySteps == null) return null;
var bsModels =  new J.util.BS ();
for (var i = this.modelCount; --i >= 0; ) {
var t = this.models[i].getSelectedTrajectory ();
if (t >= 0) {
bsModels.set (t);
i = this.models[i].trajectoryBaseIndex;
}}
return bsModels;
});
$_M(c$, "setTrajectoryBs", 
function (bsModels) {
for (var i = 0; i < this.modelCount; i++) if (bsModels.get (i)) this.setTrajectory (i);

}, "J.util.BS");
$_M(c$, "setTrajectory", 
function (modelIndex) {
if (modelIndex < 0 || !this.isTrajectory (modelIndex)) return;
if (this.atoms[this.models[modelIndex].firstAtomIndex].modelIndex == modelIndex) return;
var baseModelIndex = this.models[modelIndex].trajectoryBaseIndex;
this.models[baseModelIndex].setSelectedTrajectory (modelIndex);
this.setAtomPositions (baseModelIndex, modelIndex, this.trajectorySteps.get (modelIndex), null, 0, (this.vibrationSteps == null ? null : this.vibrationSteps.get (modelIndex)), true);
var m = this.viewer.getCurrentModelIndex ();
if (m >= 0 && m != modelIndex && this.models[m].fileIndex == this.models[modelIndex].fileIndex) this.viewer.setCurrentModelIndexClear (modelIndex, false);
}, "~N");
$_M(c$, "morphTrajectories", 
function (m1, m2, f) {
if (m1 < 0 || m2 < 0 || !this.isTrajectory (m1) || !this.isTrajectory (m2)) return;
if (f == 0) {
this.setTrajectory (m1);
return;
}if (f == 1) {
this.setTrajectory (m2);
return;
}var baseModelIndex = this.models[m1].trajectoryBaseIndex;
this.models[baseModelIndex].setSelectedTrajectory (m1);
this.setAtomPositions (baseModelIndex, m1, this.trajectorySteps.get (m1), this.trajectorySteps.get (m2), f, (this.vibrationSteps == null ? null : this.vibrationSteps.get (m1)), true);
var m = this.viewer.getCurrentModelIndex ();
if (m >= 0 && m != m1 && this.models[m].fileIndex == this.models[m1].fileIndex) this.viewer.setCurrentModelIndexClear (m1, false);
}, "~N,~N,~N");
$_M(c$, "setAtomPositions", 
($fz = function (baseModelIndex, modelIndex, t1, t2, f, vibs, isFractional) {
var bs =  new J.util.BS ();
var vib =  new J.util.V3 ();
var iFirst = this.models[baseModelIndex].firstAtomIndex;
var iMax = iFirst + this.getAtomCountInModel (baseModelIndex);
if (f == 0) {
for (var pt = 0, i = iFirst; i < iMax && pt < t1.length; i++, pt++) {
this.atoms[i].modelIndex = modelIndex;
if (t1[pt] == null) continue;
if (isFractional) this.atoms[i].setFractionalCoordTo (t1[pt], true);
 else this.atoms[i].setT (t1[pt]);
if (this.vibrationSteps != null) {
if (vibs != null && vibs[pt] != null) vib = vibs[pt];
this.setVibrationVector (i, vib);
}bs.set (i);
}
} else {
var p =  new J.util.P3 ();
var n = Math.min (t1.length, t2.length);
for (var pt = 0, i = iFirst; i < iMax && pt < n; i++, pt++) {
this.atoms[i].modelIndex = modelIndex;
if (t1[pt] == null || t2[pt] == null) continue;
p.sub2 (t2[pt], t1[pt]);
p.scaleAdd2 (f, p, t1[pt]);
if (isFractional) this.atoms[i].setFractionalCoordTo (p, true);
 else this.atoms[i].setT (p);
bs.set (i);
}
}this.initializeBspf ();
this.validateBspfForModel (baseModelIndex, false);
this.recalculateLeadMidpointsAndWingVectors (baseModelIndex);
this.shapeManager.refreshShapeTrajectories (baseModelIndex, bs, null);
if (this.models[baseModelIndex].hasRasmolHBonds) {
this.models[baseModelIndex].clearRasmolHydrogenBonds (null);
this.models[baseModelIndex].getRasmolHydrogenBonds (bs, bs, null, false, 2147483647, false, null);
}}, $fz.isPrivate = true, $fz), "~N,~N,~A,~A,~N,~A,~B");
$_M(c$, "getFrameOffsets", 
function (bsAtoms) {
if (bsAtoms == null) return null;
var offsets =  new Array (this.modelCount);
for (var i = 0; i < this.modelCount; i++) offsets[i] =  new J.util.P3 ();

var lastModel = 0;
var n = 0;
var offset = offsets[0];
var asTrajectory = (this.trajectorySteps != null && this.trajectorySteps.size () == this.modelCount);
var m1 = (asTrajectory ? this.modelCount : 1);
for (var m = 0; m < m1; m++) {
if (asTrajectory) this.setTrajectory (m);
for (var i = 0; i <= this.atomCount; i++) {
if (i == this.atomCount || this.atoms[i].modelIndex != lastModel) {
if (n > 0) {
offset.scale (-1.0 / n);
if (lastModel != 0) offset.sub (offsets[0]);
n = 0;
}if (i == this.atomCount) break;
lastModel = this.atoms[i].modelIndex;
offset = offsets[lastModel];
}if (!bsAtoms.get (i)) continue;
offset.add (this.atoms[i]);
n++;
}
}
offsets[0].set (0, 0, 0);
return offsets;
}, "J.util.BS");
$_M(c$, "getAtomBits", 
function (tokType, specInfo) {
switch (tokType) {
default:
return J.util.BSUtil.andNot (this.getAtomBitsMaybeDeleted (tokType, specInfo), this.viewer.getDeletedAtoms ());
case 1048610:
var modelNumber = (specInfo).intValue ();
var modelIndex = this.getModelNumberIndex (modelNumber, true, true);
return (modelIndex < 0 && modelNumber > 0 ?  new J.util.BS () : this.viewer.getModelUndeletedAtomsBitSet (modelIndex));
}
}, "~N,~O");
$_M(c$, "getAtomLabel", 
function (i) {
return this.viewer.getShapePropertyIndex (5, "label", i);
}, "~N");
$_M(c$, "findNearestAtomIndex", 
function (x, y, bsNot, min) {
if (this.atomCount == 0) return -1;
this.closest[0] = null;
if (this.g3d.isAntialiased ()) {
x <<= 1;
y <<= 1;
}this.findNearest2 (x, y, this.closest, bsNot, min);
this.shapeManager.findNearestShapeAtomIndex (x, y, this.closest, bsNot);
var closestIndex = (this.closest[0] == null ? -1 : this.closest[0].index);
this.closest[0] = null;
return closestIndex;
}, "~N,~N,J.util.BS,~N");
$_M(c$, "calculateStructures", 
function (bsAtoms, asDSSP, dsspIgnoreHydrogen, setStructure) {
var bsAllAtoms =  new J.util.BS ();
var bsModelsExcluded = J.util.BSUtil.copyInvert (this.modelsOf (bsAtoms, bsAllAtoms), this.modelCount);
if (!setStructure) return this.calculateStructuresAllExcept (bsModelsExcluded, asDSSP, true, dsspIgnoreHydrogen, false, false);
for (var i = 0; i < this.modelCount; i++) if (!bsModelsExcluded.get (i)) this.models[i].clearBioPolymers ();

this.calculatePolymers (null, 0, 0, bsModelsExcluded);
var ret = this.calculateStructuresAllExcept (bsModelsExcluded, asDSSP, true, dsspIgnoreHydrogen, true, false);
this.viewer.resetBioshapes (bsAllAtoms);
this.setStructureIndexes ();
return ret;
}, "J.util.BS,~B,~B,~B");
$_M(c$, "calculatePointGroup", 
function (bsAtoms) {
return this.calculatePointGroupForFirstModel (bsAtoms, false, false, false, null, 0, 0);
}, "J.util.BS");
$_M(c$, "getPointGroupInfo", 
function (bsAtoms) {
return this.calculatePointGroupForFirstModel (bsAtoms, false, false, true, null, 0, 0);
}, "J.util.BS");
$_M(c$, "getPointGroupAsString", 
function (bsAtoms, asDraw, type, index, scale) {
return this.calculatePointGroupForFirstModel (bsAtoms, true, asDraw, false, type, index, scale);
}, "J.util.BS,~B,~S,~N,~N");
$_M(c$, "calculatePointGroupForFirstModel", 
($fz = function (bsAtoms, doAll, asDraw, asInfo, type, index, scale) {
var modelIndex = this.viewer.getCurrentModelIndex ();
var iAtom = (bsAtoms == null ? -1 : bsAtoms.nextSetBit (0));
if (modelIndex < 0 && iAtom >= 0) modelIndex = this.atoms[iAtom].getModelIndex ();
if (modelIndex < 0) {
modelIndex = this.viewer.getVisibleFramesBitSet ().nextSetBit (0);
bsAtoms = null;
}var bs = this.viewer.getModelUndeletedAtomsBitSet (modelIndex);
if (bsAtoms != null) bs.and (bsAtoms);
iAtom = bs.nextSetBit (0);
if (iAtom < 0) {
bs = this.viewer.getModelUndeletedAtomsBitSet (modelIndex);
iAtom = bs.nextSetBit (0);
}var obj = this.viewer.getShapePropertyIndex (18, "mad", iAtom);
var haveVibration = (obj != null && (obj).intValue () != 0 || this.viewer.isVibrationOn ());
var symmetry = J.api.Interface.getOptionInterface ("symmetry.Symmetry");
this.pointGroup = symmetry.setPointGroup (this.pointGroup, this.atoms, bs, haveVibration, this.viewer.getFloat (570425382), this.viewer.getFloat (570425384));
if (!doAll && !asInfo) return this.pointGroup.getPointGroupName ();
var ret = this.pointGroup.getPointGroupInfo (modelIndex, asDraw, asInfo, type, index, scale);
if (asInfo) return ret;
return (this.modelCount > 1 ? "frame " + this.getModelNumberDotted (modelIndex) + "; " : "") + ret;
}, $fz.isPrivate = true, $fz), "J.util.BS,~B,~B,~B,~S,~N,~N");
$_M(c$, "modelsOf", 
($fz = function (bsAtoms, bsAllAtoms) {
var bsModels = J.util.BSUtil.newBitSet (this.modelCount);
var isAll = (bsAtoms == null);
var i0 = (isAll ? this.atomCount - 1 : bsAtoms.nextSetBit (0));
for (var i = i0; i >= 0; i = (isAll ? i - 1 : bsAtoms.nextSetBit (i + 1))) {
var modelIndex = this.models[this.atoms[i].modelIndex].trajectoryBaseIndex;
if (this.isJmolDataFrameForModel (modelIndex)) continue;
bsModels.set (modelIndex);
bsAllAtoms.set (i);
}
return bsModels;
}, $fz.isPrivate = true, $fz), "J.util.BS,J.util.BS");
$_M(c$, "getDefaultStructure", 
function (bsAtoms, bsAllAtoms) {
var bsModels = this.modelsOf (bsAtoms, bsAllAtoms);
var ret =  new J.util.SB ();
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) if (this.models[i].isBioModel && this.models[i].defaultStructure != null) ret.append (this.models[i].defaultStructure);

return ret.toString ();
}, "J.util.BS,J.util.BS");
$_M(c$, "makeConnections", 
function (minDistance, maxDistance, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy) {
if (connectOperation == 1073741852 && order != 2048) {
var stateScript = "connect ";
if (minDistance != 0.1) stateScript += minDistance + " ";
if (maxDistance != 1.0E8) stateScript += maxDistance + " ";
this.addStateScript (stateScript, (isBonds ? bsA : null), (isBonds ? null : bsA), (isBonds ? null : bsB), " auto", false, true);
}this.moleculeCount = 0;
return this.makeConnections2 (minDistance, maxDistance, order, connectOperation, bsA, bsB, bsBonds, isBonds, addGroup, energy);
}, "~N,~N,~N,~N,J.util.BS,J.util.BS,J.util.BS,~B,~B,~N");
$_M(c$, "setPdbConectBonding", 
function (baseAtomIndex, baseModelIndex, bsExclude) {
var mad = this.viewer.getMadBond ();
for (var i = baseModelIndex; i < this.modelCount; i++) {
var vConnect = this.getModelAuxiliaryInfoValue (i, "PDB_CONECT_bonds");
if (vConnect == null) continue;
var nConnect = vConnect.size ();
this.setModelAuxiliaryInfo (i, "initialBondCount", Integer.$valueOf (nConnect));
var atomInfo = this.getModelAuxiliaryInfoValue (i, "PDB_CONECT_firstAtom_count_max");
var firstAtom = atomInfo[0] + baseAtomIndex;
var atomMax = firstAtom + atomInfo[1];
var max = atomInfo[2];
var serialMap =  Clazz.newIntArray (max + 1, 0);
var iSerial;
for (var iAtom = firstAtom; iAtom < atomMax; iAtom++) if ((iSerial = this.atomSerials[iAtom]) > 0) serialMap[iSerial] = iAtom + 1;

for (var iConnect = 0; iConnect < nConnect; iConnect++) {
var pair = vConnect.get (iConnect);
var sourceSerial = pair[0];
var targetSerial = pair[1];
var order = pair[2];
if (sourceSerial < 0 || targetSerial < 0 || sourceSerial > max || targetSerial > max) continue;
var sourceIndex = serialMap[sourceSerial] - 1;
var targetIndex = serialMap[targetSerial] - 1;
if (sourceIndex < 0 || targetIndex < 0) continue;
if (bsExclude != null) {
if (this.atoms[sourceIndex].isHetero ()) bsExclude.set (sourceIndex);
if (this.atoms[targetIndex].isHetero ()) bsExclude.set (targetIndex);
}this.checkValencesAndBond (this.atoms[sourceIndex], this.atoms[targetIndex], order, (order == 2048 ? 1 : mad), null);
}
}
}, "~N,~N,J.util.BS");
$_M(c$, "deleteAllBonds", 
function () {
this.moleculeCount = 0;
for (var i = this.stateScripts.size (); --i >= 0; ) {
if (this.stateScripts.get (i).isConnect ()) {
this.stateScripts.remove (i);
}}
this.deleteAllBonds2 ();
});
$_M(c$, "includeAllRelatedFrames", 
($fz = function (bsModels) {
var j;
for (var i = 0; i < this.modelCount; i++) {
if (bsModels.get (i)) {
if (this.isTrajectory (i) && !bsModels.get (j = this.models[i].trajectoryBaseIndex)) {
bsModels.set (j);
this.includeAllRelatedFrames (bsModels);
return;
}continue;
}if (this.isTrajectory (i) && bsModels.get (this.models[i].trajectoryBaseIndex) || this.isJmolDataFrameForModel (i) && bsModels.get (this.models[i].dataSourceFrame)) bsModels.set (i);
}
}, $fz.isPrivate = true, $fz), "J.util.BS");
$_M(c$, "deleteModels", 
function (bsAtoms) {
this.moleculeCount = 0;
var bsModels = this.getModelBitSet (bsAtoms, false);
this.includeAllRelatedFrames (bsModels);
var nAtomsDeleted = 0;
var nModelsDeleted = J.util.BSUtil.cardinalityOf (bsModels);
if (nModelsDeleted == 0) return null;
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.clearDataFrameReference (i);

var bsDeleted;
if (nModelsDeleted == this.modelCount) {
bsDeleted = this.getModelAtomBitSetIncludingDeleted (-1, true);
this.viewer.zap (true, false, false);
return bsDeleted;
}this.validateBspf (false);
var newModels =  new Array (this.modelCount - nModelsDeleted);
var oldModels = this.models;
bsDeleted =  new J.util.BS ();
for (var i = 0, mpt = 0; i < this.modelCount; i++) if (bsModels.get (i)) {
this.getAtomCountInModel (i);
bsDeleted.or (this.getModelAtomBitSetIncludingDeleted (i, false));
} else {
this.models[i].modelIndex = mpt;
newModels[mpt++] = this.models[i];
}
this.models = newModels;
var oldModelCount = this.modelCount;
var bsBonds = this.getBondsForSelectedAtoms (bsDeleted, true);
this.deleteBonds (bsBonds, true);
for (var i = 0, mpt = 0; i < oldModelCount; i++) {
if (!bsModels.get (i)) {
mpt++;
continue;
}var nAtoms = oldModels[i].atomCount;
if (nAtoms == 0) continue;
nAtomsDeleted += nAtoms;
var bs = oldModels[i].bsAtoms;
var firstAtomIndex = oldModels[i].firstAtomIndex;
J.util.BSUtil.deleteBits (this.bsSymmetry, bs);
this.deleteModel (mpt, firstAtomIndex, nAtoms, bs, bsBonds);
for (var j = oldModelCount; --j > i; ) oldModels[j].fixIndices (mpt, nAtoms, bs);

this.viewer.deleteShapeAtoms ([newModels, this.atoms, [mpt, firstAtomIndex, nAtoms]], bs);
this.modelCount--;
}
this.deleteModel (-1, 0, 0, null, null);
return bsDeleted;
}, "J.util.BS");
$_M(c$, "setAtomProperty", 
function (bs, tok, iValue, fValue, sValue, values, list) {
switch (tok) {
case 1115297793:
case 1113200642:
case 1113200647:
case 1113200649:
case 1113200650:
case 1650071565:
case 1113200654:
if (fValue > 4.0) fValue = 4.0;
if (values != null) {
var newValues =  Clazz.newFloatArray (this.atomCount, 0);
for (var i = bs.nextSetBit (0), ii = 0; i >= 0; i = bs.nextSetBit (i + 1)) newValues[i] = values[ii++];

values = newValues;
}case 1113200646:
case 1113200652:
var rd = null;
var mar = 0;
if (values == null) {
if (fValue > 16) fValue = 16;
if (fValue < 0) fValue = 0;
mar = Clazz.doubleToInt (Math.floor (fValue * 2000));
} else {
rd =  new J.atomdata.RadiusData (values, 0, null, null);
}this.shapeManager.setShapeSizeBs (J.viewer.JC.shapeTokenIndex (tok), mar, rd, bs);
return;
}
this.setAPm (bs, tok, iValue, fValue, sValue, values, list);
}, "J.util.BS,~N,~N,~N,~S,~A,~A");
$_M(c$, "getFileData", 
function (modelIndex) {
if (modelIndex < 0) return "";
var fileData = this.getModelAuxiliaryInfoValue (modelIndex, "fileData");
if (fileData != null) return fileData;
if (!this.getModelAuxiliaryInfoBoolean (modelIndex, "isCIF")) return this.getPDBHeader (modelIndex);
fileData = this.viewer.getCifData (modelIndex);
this.setModelAuxiliaryInfo (modelIndex, "fileData", fileData);
return fileData;
}, "~N");
Clazz.overrideMethod (c$, "calculateStruts", 
function (bs1, bs2) {
this.viewer.setModelVisibility ();
return this.calculateStrutsMC (bs1, bs2);
}, "J.util.BS,J.util.BS");
$_M(c$, "addHydrogens", 
function (vConnections, pts) {
var modelIndex = this.modelCount - 1;
var bs =  new J.util.BS ();
if (this.isTrajectory (modelIndex) || this.models[modelIndex].getGroupCount () > 1) {
return bs;
}this.growAtomArrays (this.atomCount + pts.length);
var rd = this.viewer.getDefaultRadiusData ();
var mad = this.getDefaultMadFromOrder (1);
for (var i = 0, n = this.models[modelIndex].atomCount + 1; i < vConnections.size (); i++, n++) {
var atom1 = vConnections.get (i);
var atom2 = this.addAtom (modelIndex, atom1.group, 1, "H" + n, n, n, pts[i], NaN, null, 0, 0, 100, NaN, null, false, 0, null);
atom2.setMadAtom (this.viewer, rd);
bs.set (atom2.index);
this.bondAtoms (atom1, atom2, 1, mad, null, 0, false, false);
}
this.shapeManager.loadDefaultShapes (this);
return bs;
}, "J.util.JmolList,~A");
$_M(c$, "setAtomCoordsRelative", 
function (offset, bs) {
this.setAtomsCoordRelative (bs, offset.x, offset.y, offset.z);
this.mat4.setIdentity ();
this.vTemp.setT (offset);
this.mat4.setTranslation (this.vTemp);
this.recalculatePositionDependentQuantities (bs, this.mat4);
}, "J.util.Tuple3f,J.util.BS");
$_M(c$, "setAtomCoords", 
function (bs, tokType, xyzValues) {
this.setAtomCoord2 (bs, tokType, xyzValues);
switch (tokType) {
case 1112541202:
case 1112541203:
case 1112541204:
case 1146095631:
break;
default:
this.recalculatePositionDependentQuantities (bs, null);
}
}, "J.util.BS,~N,~O");
$_M(c$, "invertSelected", 
function (pt, plane, iAtom, invAtoms, bs) {
if (pt != null) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var x = (pt.x - this.atoms[i].x) * 2;
var y = (pt.y - this.atoms[i].y) * 2;
var z = (pt.z - this.atoms[i].z) * 2;
this.setAtomCoordRelative (i, x, y, z);
}
return;
}if (plane != null) {
var norm = J.util.V3.new3 (plane.x, plane.y, plane.z);
norm.normalize ();
var d = Math.sqrt (plane.x * plane.x + plane.y * plane.y + plane.z * plane.z);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var twoD = -J.util.Measure.distanceToPlaneD (plane, d, this.atoms[i]) * 2;
var x = norm.x * twoD;
var y = norm.y * twoD;
var z = norm.z * twoD;
this.setAtomCoordRelative (i, x, y, z);
}
return;
}if (iAtom >= 0) {
var thisAtom = this.atoms[iAtom];
var bonds = thisAtom.bonds;
if (bonds == null) return;
var bsAtoms =  new J.util.BS ();
var vNot =  new J.util.JmolList ();
var bsModel = this.viewer.getModelUndeletedAtomsBitSet (thisAtom.modelIndex);
for (var i = 0; i < bonds.length; i++) {
var a = bonds[i].getOtherAtom (thisAtom);
if (invAtoms.get (a.index)) {
bsAtoms.or (J.util.JmolMolecule.getBranchBitSet (this.atoms, a.index, bsModel, null, iAtom, true, true));
} else {
vNot.addLast (a);
}}
if (vNot.size () == 0) return;
pt = J.util.Measure.getCenterAndPoints (vNot)[0];
var v = J.util.V3.newVsub (thisAtom, pt);
var q = J.util.Quaternion.newVA (v, 180);
this.moveAtoms (null, q.getMatrix (), null, bsAtoms, thisAtom, true);
}}, "J.util.P3,J.util.P4,~N,J.util.BS,J.util.BS");
$_M(c$, "setDihedrals", 
function (dihedralList, bsBranches, f) {
var n = Clazz.doubleToInt (dihedralList.length / 6);
if (f > 1) f = 1;
for (var j = 0, pt = 0; j < n; j++, pt += 6) {
var bs = bsBranches[j];
if (bs == null || bs.isEmpty ()) continue;
var a1 = this.atoms[Clazz.floatToInt (dihedralList[pt + 1])];
var v = J.util.V3.newVsub (this.atoms[Clazz.floatToInt (dihedralList[pt + 2])], a1);
var angle = (dihedralList[pt + 5] - dihedralList[pt + 4]) * f;
var aa = J.util.AxisAngle4f.newVA (v, (-angle / 57.29577951308232));
this.matTemp.setAA (aa);
this.ptTemp.setT (a1);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
this.atoms[i].sub (this.ptTemp);
this.matTemp.transform (this.atoms[i]);
this.atoms[i].add (this.ptTemp);
this.taintAtom (i, 2);
}
}
}, "~A,~A,~N");
$_M(c$, "moveAtoms", 
function (mNew, matrixRotate, translation, bs, center, isInternal) {
if (mNew == null) {
this.matTemp.setM (matrixRotate);
} else {
this.matInv.setM (matrixRotate);
this.matInv.invert ();
this.ptTemp.set (0, 0, 0);
this.matTemp.mul2 (mNew, matrixRotate);
this.matTemp.mul2 (this.matInv, this.matTemp);
}if (isInternal) {
this.vTemp.setT (center);
this.mat4.setIdentity ();
this.mat4.setTranslation (this.vTemp);
this.mat4t.setM3 (this.matTemp);
this.mat4.mulM4 (this.mat4t);
this.mat4t.setIdentity ();
this.vTemp.scale (-1);
this.mat4t.setTranslation (this.vTemp);
this.mat4.mulM4 (this.mat4t);
} else {
this.mat4.setM3 (this.matTemp);
}for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (isInternal) {
this.mat4.transform (this.atoms[i]);
} else {
this.ptTemp.add (this.atoms[i]);
this.mat4.transform (this.atoms[i]);
this.ptTemp.sub (this.atoms[i]);
}this.taintAtom (i, 2);
}
if (!isInternal) {
this.ptTemp.scale (1 / bs.cardinality ());
if (translation == null) translation =  new J.util.V3 ();
translation.add (this.ptTemp);
}if (translation != null) {
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) this.atoms[i].add (translation);

this.mat4t.setIdentity ();
this.mat4t.setTranslation (translation);
this.mat4.mul2 (this.mat4t, this.mat4);
}this.recalculatePositionDependentQuantities (bs, this.mat4);
}, "J.util.Matrix3f,J.util.Matrix3f,J.util.V3,J.util.BS,J.util.P3,~B");
$_M(c$, "recalculatePositionDependentQuantities", 
function (bs, mat) {
if (this.getHaveStraightness ()) this.calculateStraightness ();
this.recalculateLeadMidpointsAndWingVectors (-1);
var bsModels = this.getModelBitSet (bs, false);
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) this.shapeManager.refreshShapeTrajectories (i, bs, mat);

}, "J.util.BS,J.util.Matrix4f");
$_M(c$, "getBsBranches", 
function (dihedralList) {
var n = Clazz.doubleToInt (dihedralList.length / 6);
var bsBranches =  new Array (n);
var map =  new java.util.Hashtable ();
for (var i = 0, pt = 0; i < n; i++, pt += 6) {
var dv = dihedralList[pt + 5] - dihedralList[pt + 4];
if (Math.abs (dv) < 1) continue;
var i0 = Clazz.floatToInt (dihedralList[pt + 1]);
var i1 = Clazz.floatToInt (dihedralList[pt + 2]);
var s = "" + i0 + "_" + i1;
if (map.containsKey (s)) continue;
map.put (s, Boolean.TRUE);
var bs = this.viewer.getBranchBitSet (i1, i0, true);
var bonds = this.atoms[i0].bonds;
var a0 = this.atoms[i0];
for (var j = 0; j < bonds.length; j++) {
var b = bonds[j];
if (!b.isCovalent ()) continue;
var i2 = b.getOtherAtom (a0).index;
if (i2 == i1) continue;
if (bs.get (i2)) {
bs = null;
break;
}}
bsBranches[i] = bs;
}
return bsBranches;
}, "~A");
});
