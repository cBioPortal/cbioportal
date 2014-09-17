Clazz.declarePackage ("JS");
Clazz.load (["J.api.SymmetryInterface"], "JS.Symmetry", ["java.lang.Float", "java.util.Hashtable", "JU.BS", "$.Lst", "$.M3", "$.M4", "$.P3", "$.SB", "$.V3", "JM.Atom", "JS.PointGroup", "$.SpaceGroup", "$.SymmetryInfo", "$.SymmetryOperation", "$.UnitCell", "JU.Escape", "$.Logger", "$.SimpleUnitCell"], function () {
c$ = Clazz.decorateAsClass (function () {
this.pointGroup = null;
this.spaceGroup = null;
this.symmetryInfo = null;
this.unitCell = null;
this.$isBio = false;
Clazz.instantialize (this, arguments);
}, JS, "Symmetry", null, J.api.SymmetryInterface);
Clazz.defineMethod (c$, "isBio", 
function () {
return this.$isBio;
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "setPointGroup", 
function (siLast, atomset, bsAtoms, haveVibration, distanceTolerance, linearTolerance) {
this.pointGroup = JS.PointGroup.getPointGroup (siLast == null ? null : (siLast).pointGroup, atomset, bsAtoms, haveVibration, distanceTolerance, linearTolerance);
return this;
}, "J.api.SymmetryInterface,~A,JU.BS,~B,~N,~N");
Clazz.overrideMethod (c$, "getPointGroupName", 
function () {
return this.pointGroup.getName ();
});
Clazz.overrideMethod (c$, "getPointGroupInfo", 
function (modelIndex, asDraw, asInfo, type, index, scale) {
if (!asDraw && !asInfo && this.pointGroup.textInfo != null) return this.pointGroup.textInfo;
 else if (asDraw && this.pointGroup.isDrawType (type, index, scale)) return this.pointGroup.drawInfo;
 else if (asInfo && this.pointGroup.info != null) return this.pointGroup.info;
return this.pointGroup.getInfo (modelIndex, asDraw, asInfo, type, index, scale);
}, "~N,~B,~B,~S,~N,~N");
Clazz.defineMethod (c$, "setSpaceGroup", 
function (doNormalize) {
if (this.spaceGroup == null) this.spaceGroup = (JS.SpaceGroup.getNull (true)).set (doNormalize);
}, "~B");
Clazz.defineMethod (c$, "addSpaceGroupOperation", 
function (xyz, opId) {
return this.spaceGroup.addSymmetry (xyz, opId, false);
}, "~S,~N");
Clazz.defineMethod (c$, "addBioMoleculeOperation", 
function (mat, isReverse) {
this.$isBio = this.spaceGroup.isBio = true;
return this.spaceGroup.addSymmetry ((isReverse ? "!" : "") + "[[bio" + mat, 0, false);
}, "JU.M4,~B");
Clazz.overrideMethod (c$, "setLattice", 
function (latt) {
this.spaceGroup.setLatticeParam (latt);
}, "~N");
Clazz.defineMethod (c$, "getSpaceGroup", 
function () {
return this.spaceGroup;
});
Clazz.overrideMethod (c$, "setSpaceGroupFrom", 
function (symmetry) {
this.spaceGroup = symmetry.getSpaceGroup ();
}, "J.api.SymmetryInterface");
Clazz.overrideMethod (c$, "createSpaceGroup", 
function (desiredSpaceGroupIndex, name, object) {
this.spaceGroup = JS.SpaceGroup.createSpaceGroup (desiredSpaceGroupIndex, name, object);
if (this.spaceGroup != null && JU.Logger.debugging) JU.Logger.debug ("using generated space group " + this.spaceGroup.dumpInfo (null));
return this.spaceGroup != null;
}, "~N,~S,~O");
Clazz.overrideMethod (c$, "getSpaceGroupInfoStr", 
function (name, cellInfo) {
return JS.SpaceGroup.getInfo (name, cellInfo);
}, "~S,J.api.SymmetryInterface");
Clazz.overrideMethod (c$, "getLatticeDesignation", 
function () {
return this.spaceGroup.getLatticeDesignation ();
});
Clazz.overrideMethod (c$, "setFinalOperations", 
function (name, atoms, iAtomFirst, noSymmetryCount, doNormalize, filterSymop) {
if (name != null && (name.startsWith ("bio") || name.indexOf (" *(") >= 0)) this.spaceGroup.name = name;
if (filterSymop != null) {
var lst =  new JU.Lst ();
lst.addLast (this.spaceGroup.operations[0]);
for (var i = 1; i < this.spaceGroup.operationCount; i++) if (filterSymop.contains (" " + (i + 1) + " ")) lst.addLast (this.spaceGroup.operations[i]);

this.spaceGroup = JS.SpaceGroup.createSpaceGroup (-1, name + " *(" + filterSymop.trim () + ")", lst);
}this.spaceGroup.setFinalOperations (atoms, iAtomFirst, noSymmetryCount, doNormalize);
}, "~S,~A,~N,~N,~B,~S");
Clazz.defineMethod (c$, "getSpaceGroupOperation", 
function (i) {
return (i >= this.spaceGroup.operations.length ? null : this.spaceGroup.finalOperations == null ? this.spaceGroup.operations[i] : this.spaceGroup.finalOperations[i]);
}, "~N");
Clazz.overrideMethod (c$, "getSpaceGroupXyz", 
function (i, doNormalize) {
return this.spaceGroup.getXyz (i, doNormalize);
}, "~N,~B");
Clazz.defineMethod (c$, "newSpaceGroupPoint", 
function (i, atom1, atom2, transX, transY, transZ) {
if (this.spaceGroup.finalOperations == null) {
if (!this.spaceGroup.operations[i].isFinalized) this.spaceGroup.operations[i].doFinalize ();
this.spaceGroup.operations[i].newPoint (atom1, atom2, transX, transY, transZ);
return;
}this.spaceGroup.finalOperations[i].newPoint (atom1, atom2, transX, transY, transZ);
}, "~N,JU.P3,JU.P3,~N,~N,~N");
Clazz.overrideMethod (c$, "rotateAxes", 
function (iop, axes, ptTemp, mTemp) {
return (iop == 0 ? axes : this.spaceGroup.finalOperations[iop].rotateAxes (axes, this.unitCell, ptTemp, mTemp));
}, "~N,~A,JU.P3,JU.M3");
Clazz.overrideMethod (c$, "getSpaceGroupOperationCode", 
function (iOp) {
return this.spaceGroup.operations[iOp].subsystemCode;
}, "~N");
Clazz.overrideMethod (c$, "setTimeReversal", 
function (op, val) {
this.spaceGroup.operations[op].setTimeReversal (val);
}, "~N,~N");
Clazz.overrideMethod (c$, "getSpinOp", 
function (op) {
return this.spaceGroup.operations[op].getSpinOp ();
}, "~N");
Clazz.overrideMethod (c$, "addLatticeVectors", 
function (lattvecs) {
return this.spaceGroup.addLatticeVectors (lattvecs);
}, "JU.Lst");
Clazz.overrideMethod (c$, "getLatticeOp", 
function () {
return this.spaceGroup.latticeOp;
});
Clazz.overrideMethod (c$, "getOperationRsVs", 
function (iop) {
return (this.spaceGroup.finalOperations == null ? this.spaceGroup.operations : this.spaceGroup.finalOperations)[iop].rsvs;
}, "~N");
Clazz.overrideMethod (c$, "getSiteMultiplicity", 
function (pt) {
return this.spaceGroup.getSiteMultiplicity (pt, this.unitCell);
}, "JU.P3");
Clazz.overrideMethod (c$, "addOp", 
function (code, rs, vs, sigma) {
this.spaceGroup.isSSG = true;
var s = JS.SymmetryOperation.getXYZFromRsVs (rs, vs, false);
var i = this.spaceGroup.addSymmetry (s, -1, true);
this.spaceGroup.operations[i].setSigma (code, sigma);
return s;
}, "~S,JU.Matrix,JU.Matrix,JU.Matrix");
Clazz.overrideMethod (c$, "getMatrixFromString", 
function (xyz, rotTransMatrix, allowScaling, modDim) {
return JS.SymmetryOperation.getMatrixFromString (null, xyz, rotTransMatrix, allowScaling);
}, "~S,~A,~B,~N");
Clazz.defineMethod (c$, "getSpaceGroupName", 
function () {
return (this.symmetryInfo != null ? this.symmetryInfo.sgName : this.spaceGroup != null ? this.spaceGroup.getName () : this.unitCell != null && this.unitCell.name.length > 0 ? "cell=" + this.unitCell.name : "");
});
Clazz.overrideMethod (c$, "getSpaceGroupOperationCount", 
function () {
return (this.symmetryInfo != null ? this.symmetryInfo.symmetryOperations.length : this.spaceGroup != null && this.spaceGroup.finalOperations != null ? this.spaceGroup.finalOperations.length : 0);
});
Clazz.overrideMethod (c$, "getCoordinatesAreFractional", 
function () {
return this.symmetryInfo == null || this.symmetryInfo.coordinatesAreFractional;
});
Clazz.overrideMethod (c$, "getCellRange", 
function () {
return this.symmetryInfo.cellRange;
});
Clazz.overrideMethod (c$, "getSymmetryInfoStr", 
function () {
return this.symmetryInfo.infoStr;
});
Clazz.defineMethod (c$, "getSymmetryOperations", 
function () {
return this.symmetryInfo == null ? this.spaceGroup.finalOperations : this.symmetryInfo.symmetryOperations;
});
Clazz.overrideMethod (c$, "isPeriodic", 
function () {
return (this.symmetryInfo == null ? false : this.symmetryInfo.isPeriodic ());
});
Clazz.overrideMethod (c$, "setSymmetryInfo", 
function (modelIndex, modelAuxiliaryInfo, notionalCell) {
this.symmetryInfo =  new JS.SymmetryInfo ();
var notionalUnitcell = this.symmetryInfo.setSymmetryInfo (modelAuxiliaryInfo, notionalCell);
if (notionalUnitcell == null) return;
this.setUnitCell (notionalUnitcell, modelAuxiliaryInfo.containsKey ("jmolData"));
this.unitCell.moreInfo = modelAuxiliaryInfo.get ("moreUnitCellInfo");
modelAuxiliaryInfo.put ("infoUnitCell", this.getUnitCellAsArray (false));
this.setOffsetPt (modelAuxiliaryInfo.get ("unitCellOffset"));
var matUnitCellOrientation = modelAuxiliaryInfo.get ("matUnitCellOrientation");
if (matUnitCellOrientation != null) this.initializeOrientation (matUnitCellOrientation);
if (JU.Logger.debugging) JU.Logger.debug ("symmetryInfos[" + modelIndex + "]:\n" + this.unitCell.dumpInfo (true));
}, "~N,java.util.Map,~A");
Clazz.overrideMethod (c$, "haveUnitCell", 
function () {
return (this.unitCell != null);
});
Clazz.overrideMethod (c$, "checkUnitCell", 
function (uc, cell, ptTemp, isAbsolute) {
uc.toFractional (ptTemp, isAbsolute);
var slop = 0.02;
return (ptTemp.x >= cell.x - 1 - slop && ptTemp.x <= cell.x + slop && ptTemp.y >= cell.y - 1 - slop && ptTemp.y <= cell.y + slop && ptTemp.z >= cell.z - 1 - slop && ptTemp.z <= cell.z + slop);
}, "J.api.SymmetryInterface,JU.P3,JU.P3,~B");
Clazz.defineMethod (c$, "setUnitCell", 
function (notionalUnitCell, setRelative) {
this.unitCell = JS.UnitCell.newA (notionalUnitCell, setRelative);
}, "~A,~B");
Clazz.overrideMethod (c$, "unitCellEquals", 
function (uc2) {
return ((uc2)).unitCell.isSameAs (this.unitCell);
}, "J.api.SymmetryInterface");
Clazz.overrideMethod (c$, "getUnitCellState", 
function () {
return (this.unitCell == null ? "" : this.unitCell.getState ());
});
Clazz.overrideMethod (c$, "getMoreInfo", 
function () {
return this.unitCell.moreInfo;
});
Clazz.defineMethod (c$, "getUnitsymmetryInfo", 
function () {
return this.unitCell.dumpInfo (false);
});
Clazz.overrideMethod (c$, "initializeOrientation", 
function (mat) {
this.unitCell.initOrientation (mat);
}, "JU.M3");
Clazz.overrideMethod (c$, "unitize", 
function (ptFrac) {
this.unitCell.unitize (ptFrac);
}, "JU.P3");
Clazz.overrideMethod (c$, "toUnitCell", 
function (pt, offset) {
this.unitCell.toUnitCell (pt, offset);
}, "JU.P3,JU.P3");
Clazz.defineMethod (c$, "toCartesian", 
function (fpt, ignoreOffset) {
if (!this.$isBio) this.unitCell.toCartesian (fpt, ignoreOffset);
}, "JU.T3,~B");
Clazz.overrideMethod (c$, "toSupercell", 
function (fpt) {
return this.unitCell.toSupercell (fpt);
}, "JU.P3");
Clazz.defineMethod (c$, "toFractional", 
function (pt, isAbsolute) {
if (!this.$isBio) this.unitCell.toFractional (pt, isAbsolute);
}, "JU.T3,~B");
Clazz.defineMethod (c$, "getNotionalUnitCell", 
function () {
return this.unitCell.getNotionalUnitCell ();
});
Clazz.overrideMethod (c$, "getUnitCellAsArray", 
function (vectorsOnly) {
return this.unitCell.getUnitCellAsArray (vectorsOnly);
}, "~B");
Clazz.overrideMethod (c$, "getTensor", 
function (parBorU) {
if (parBorU == null) return null;
if (this.unitCell == null) this.unitCell = JS.UnitCell.newA ([1, 1, 1, 90, 90, 90], true);
return this.unitCell.getTensor (parBorU);
}, "~A");
Clazz.overrideMethod (c$, "getUnitCellVertices", 
function () {
return this.unitCell.getVertices ();
});
Clazz.overrideMethod (c$, "getCartesianOffset", 
function () {
return this.unitCell.getCartesianOffset ();
});
Clazz.overrideMethod (c$, "getFractionalOffset", 
function () {
return this.unitCell.getFractionalOffset ();
});
Clazz.overrideMethod (c$, "setOffsetPt", 
function (pt) {
this.unitCell.setOffset (pt);
}, "JU.T3");
Clazz.overrideMethod (c$, "setOffset", 
function (nnn) {
var pt =  new JU.P3 ();
JU.SimpleUnitCell.ijkToPoint3f (nnn, pt, 0);
this.unitCell.setOffset (pt);
}, "~N");
Clazz.overrideMethod (c$, "getUnitCellMultiplier", 
function () {
return this.unitCell.getUnitCellMultiplier ();
});
Clazz.overrideMethod (c$, "getCanonicalCopy", 
function (scale, withOffset) {
return this.unitCell.getCanonicalCopy (scale, withOffset);
}, "~N,~B");
Clazz.overrideMethod (c$, "getUnitCellInfoType", 
function (infoType) {
return this.unitCell.getInfo (infoType);
}, "~N");
Clazz.overrideMethod (c$, "getUnitCellInfo", 
function () {
return this.unitCell.dumpInfo (false);
});
Clazz.overrideMethod (c$, "isSlab", 
function () {
return this.unitCell.isSlab ();
});
Clazz.overrideMethod (c$, "isPolymer", 
function () {
return this.unitCell.isPolymer ();
});
Clazz.overrideMethod (c$, "setMinMaxLatticeParameters", 
function (minXYZ, maxXYZ) {
this.unitCell.setMinMaxLatticeParameters (minXYZ, maxXYZ);
}, "JU.P3i,JU.P3i");
Clazz.overrideMethod (c$, "checkDistance", 
function (f1, f2, distance, dx, iRange, jRange, kRange, ptOffset) {
return this.unitCell.checkDistance (f1, f2, distance, dx, iRange, jRange, kRange, ptOffset);
}, "JU.P3,JU.P3,~N,~N,~N,~N,~N,JU.P3");
Clazz.overrideMethod (c$, "getUnitCellVectors", 
function () {
return this.unitCell.getUnitCellVectors ();
});
Clazz.overrideMethod (c$, "getUnitCell", 
function (points, setRelative, name) {
this.unitCell = JS.UnitCell.newP (points, setRelative);
if (name != null) this.unitCell.name = name;
return this;
}, "~A,~B,~S");
Clazz.overrideMethod (c$, "isSupercell", 
function () {
return this.unitCell.isSupercell ();
});
Clazz.overrideMethod (c$, "notInCentroid", 
function (modelSet, bsAtoms, minmax) {
try {
var bsDelete =  new JU.BS ();
var iAtom0 = bsAtoms.nextSetBit (0);
var molecules = modelSet.getMolecules ();
var moleculeCount = molecules.length;
var atoms = modelSet.at;
var isOneMolecule = (molecules[moleculeCount - 1].firstAtomIndex == modelSet.am[atoms[iAtom0].mi].firstAtomIndex);
var center =  new JU.P3 ();
var centroidPacked = (minmax[6] == 1);
nextMol : for (var i = moleculeCount; --i >= 0 && bsAtoms.get (molecules[i].firstAtomIndex); ) {
var bs = molecules[i].atomList;
center.set (0, 0, 0);
var n = 0;
for (var j = bs.nextSetBit (0); j >= 0; j = bs.nextSetBit (j + 1)) {
if (isOneMolecule || centroidPacked) {
center.setT (atoms[j]);
if (this.isNotCentroid (center, 1, minmax, centroidPacked)) {
if (isOneMolecule) bsDelete.set (j);
} else if (!isOneMolecule) {
continue nextMol;
}} else {
center.add (atoms[j]);
n++;
}}
if (centroidPacked || n > 0 && this.isNotCentroid (center, n, minmax, false)) bsDelete.or (bs);
}
return bsDelete;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
}, "JM.ModelSet,JU.BS,~A");
Clazz.defineMethod (c$, "isNotCentroid", 
 function (center, n, minmax, centroidPacked) {
center.scale (1 / n);
this.toFractional (center, false);
if (centroidPacked) return (center.x + 0.000005 <= minmax[0] || center.x - 0.000005 > minmax[3] || center.y + 0.000005 <= minmax[1] || center.y - 0.000005 > minmax[4] || center.z + 0.000005 <= minmax[2] || center.z - 0.000005 > minmax[5]);
return (center.x + 0.000005 <= minmax[0] || center.x + 0.00005 > minmax[3] || center.y + 0.000005 <= minmax[1] || center.y + 0.00005 > minmax[4] || center.z + 0.000005 <= minmax[2] || center.z + 0.00005 > minmax[5]);
}, "JU.P3,~N,~A,~B");
Clazz.overrideMethod (c$, "getSpaceGroupInfo", 
function (modelSet, modelIndex, sgName, symOp, pt1, pt2, drawID, type) {
var strOperations = null;
var info = null;
var cellInfo = null;
var infolist = null;
var isStandard = true;
if (sgName == null) {
if (modelIndex <= 0) modelIndex = (Clazz.instanceOf (pt1, JM.Atom) ? (pt1).mi : modelSet.vwr.am.cmi);
var isBio = false;
if (modelIndex < 0) strOperations = "no single current model";
 else if (!(isBio = (cellInfo = modelSet.am[modelIndex].biosymmetry) != null) && (cellInfo = modelSet.getUnitCell (modelIndex)) == null) strOperations = "not applicable";
if (strOperations != null) {
info =  new java.util.Hashtable ();
info.put ("spaceGroupInfo", strOperations);
info.put ("symmetryInfo", "");
} else if (pt1 == null && drawID == null && symOp != 0) {
info = modelSet.getInfo (modelIndex, "spaceGroupInfo");
}if (info != null) return info;
info =  new java.util.Hashtable ();
if (pt1 == null && drawID == null && symOp == 0) modelSet.setInfo (modelIndex, "spaceGroupInfo", info);
sgName = cellInfo.getSpaceGroupName ();
var ops = cellInfo.getSymmetryOperations ();
var sg = (isBio ? (cellInfo).spaceGroup : null);
var jf = "";
if (ops == null) {
strOperations = "\n no symmetry operations";
} else {
isStandard = !isBio;
if (isBio) this.spaceGroup = (JS.SpaceGroup.getNull (false)).set (false);
 else this.setSpaceGroup (false);
strOperations = "\n" + ops.length + " symmetry operations:";
infolist =  new Array (ops.length);
var centering = null;
for (var i = 0; i < ops.length; i++) {
var op = (ops[i]);
var xyz = op.xyz;
var iop = (isBio ? this.addBioMoleculeOperation (sg.finalOperations[i], false) : this.addSpaceGroupOperation ("=" + xyz, i + 1));
if (iop < 0) continue;
op = this.getSpaceGroupOperation (i);
if (op.timeReversal != 0 || op.modDim > 0) isStandard = false;
centering = op.setCentering (centering, false);
jf += ";" + xyz;
infolist[i] = (symOp > 0 && symOp - 1 != iop ? null : op.getDescription (modelSet, cellInfo, pt1, pt2, drawID));
if (infolist[i] != null) strOperations += "\n" + (i + 1) + "\t" + infolist[i][0] + "\t" + infolist[i][2];
}
}jf = jf.substring (jf.indexOf (";") + 1);
if (sgName.indexOf ("[--]") >= 0) sgName = jf;
} else {
info =  new java.util.Hashtable ();
}info.put ("spaceGroupName", sgName);
if (infolist != null) {
info.put ("operations", infolist);
info.put ("symmetryInfo", strOperations);
}var data;
if (isStandard) {
data = this.getSpaceGroupInfoStr (sgName, cellInfo);
if (data == null || data.equals ("?")) data = "could not identify space group from name: " + sgName + "\nformat: show spacegroup \"2\" or \"P 2c\" " + "or \"C m m m\" or \"x, y, z;-x ,-y, -z\"";
} else {
data = sgName;
}info.put ("spaceGroupInfo", data);
return info;
}, "JM.ModelSet,~N,~S,~N,JU.P3,JU.P3,~S,~S");
Clazz.overrideMethod (c$, "getSymmetryInfoString", 
function (modelSet, modelIndex, symOp, pt1, pt2, drawID, type) {
var sginfo = this.getSpaceGroupInfo (modelSet, modelIndex, null, symOp, pt1, pt2, drawID, type);
if (sginfo == null) return "";
var labelOnly = "label".equals (type);
var prettyMat = "fmatrix".equals (type);
var infolist = sginfo.get ("operations");
if (infolist == null) return "";
var sb =  new JU.SB ();
symOp--;
for (var i = 0; i < infolist.length; i++) {
if (infolist[i] == null || symOp >= 0 && symOp != i) continue;
if (drawID != null) return infolist[i][3];
if (sb.length () > 0) sb.appendC ('\n');
if (prettyMat) {
sb.append (JS.SymmetryOperation.cleanMatrix (infolist[i][10])).append ("\t");
} else if (!labelOnly) {
if (symOp < 0) sb.appendI (i + 1).append ("\t");
sb.append (infolist[i][0]).append ("\t");
}sb.append (infolist[i][2]);
}
if (sb.length () == 0 && drawID != null) sb.append ("draw " + drawID + "* delete");
return sb.toString ();
}, "JM.ModelSet,~N,~N,JU.P3,JU.P3,~S,~S");
Clazz.overrideMethod (c$, "getSymmetryInfo", 
function (modelSet, iModel, iAtom, uc, xyz, op, pt, pt2, id, type) {
if (pt2 != null) return this.getSymmetryInfoString (modelSet, iModel, op, pt, pt2, (id == null ? "sym" : id), type == 1826248716 ? "label" : null);
var isBio = uc.isBio ();
var sym = uc;
var iop = op;
var centering = null;
if (xyz == null) {
var ops = sym.getSymmetryOperations ();
if (ops == null || op == 0 || Math.abs (op) > ops.length) {
return (type == 135176 ? "draw ID sym_* delete" : "");
}if (op > 0) {
xyz = ops[iop = op - 1].xyz;
} else {
xyz = ops[iop = -1 - op].xyz;
}centering = ops[iop].centering;
} else {
iop = op = 0;
}var symTemp = modelSet.getSymTemp (true);
symTemp.setSpaceGroup (false);
var i = (isBio ? symTemp.addBioMoleculeOperation (sym.spaceGroup.finalOperations[iop], op < 0) : symTemp.addSpaceGroupOperation ((op < 0 ? "!" : "=") + xyz, Math.abs (op)));
if (i < 0) return "";
var opTemp = symTemp.getSpaceGroupOperation (i);
if (!isBio) opTemp.centering = centering;
var info;
if (pt != null || iAtom >= 0) pt = JU.P3.newP (pt == null ? modelSet.at[iAtom] : pt);
if (type == 135266320) {
if (isBio) return "";
symTemp.setUnitCell (uc.getNotionalUnitCell (), false);
uc.toFractional (pt, false);
if (Float.isNaN (pt.x)) return "";
var sympt =  new JU.P3 ();
symTemp.newSpaceGroupPoint (i, pt, sympt, 0, 0, 0);
symTemp.toCartesian (sympt, false);
return sympt;
}info = opTemp.getDescription (modelSet, uc, pt, pt2, (id == null ? "sym" : id));
var ang = (info[9]).intValue ();
switch (type) {
case 135266306:
return info;
case 1073742001:
var sinfo = [info[0], info[1], info[2], JU.Escape.eP (info[4]), JU.Escape.eP (info[5]), JU.Escape.eP (info[6]), JU.Escape.eP (info[7]), JU.Escape.eP (info[8]), "" + info[9], "" + JU.Escape.e (info[10])];
return sinfo;
case 1073741982:
return info[0];
default:
case 1826248716:
return info[2];
case 135176:
return info[3];
case 1073742178:
return info[5];
case 12289:
return info[6];
case 135266320:
return info[7];
case 1073741854:
case 135266319:
return ((ang == 0) == (type == 135266319) ? info[8] : null);
case 135266305:
return info[9];
case 12:
return info[10];
}
}, "JM.ModelSet,~N,~N,J.api.SymmetryInterface,~S,~N,JU.P3,JU.P3,~S,~N");
Clazz.overrideMethod (c$, "fcoord", 
function (p) {
return JS.SymmetryOperation.fcoord (p);
}, "JU.T3");
Clazz.overrideMethod (c$, "getV0abc", 
function (def) {
if (this.unitCell == null) return null;
var m;
var isRev = false;
if (Clazz.instanceOf (def, String)) {
var sdef = def;
if (sdef.indexOf (";") < 0) sdef += ";0,0,0";
isRev = sdef.startsWith ("!");
if (isRev) sdef = sdef.substring (1);
var symTemp =  new JS.Symmetry ();
symTemp.setSpaceGroup (false);
var i = symTemp.addSpaceGroupOperation ("=" + sdef, 0);
if (i < 0) return null;
m = symTemp.getSpaceGroupOperation (i);
(m).doFinalize ();
} else {
m = (Clazz.instanceOf (def, JU.M3) ? JU.M4.newMV (def,  new JU.P3 ()) : def);
}var pts =  new Array (4);
var pt =  new JU.P3 ();
var m3 =  new JU.M3 ();
m.getRotationScale (m3);
m.getTranslation (pt);
if (isRev) {
m3.invert ();
m3.transpose ();
m3.rotate (pt);
pt.scale (-1);
} else {
m3.transpose ();
}this.unitCell.toCartesian (pt, false);
pts[0] = JU.V3.newV (pt);
pts[1] = JU.V3.new3 (1, 0, 0);
pts[2] = JU.V3.new3 (0, 1, 0);
pts[3] = JU.V3.new3 (0, 0, 1);
for (var i = 1; i < 4; i++) {
m3.rotate (pts[i]);
this.unitCell.toCartesian (pts[i], true);
}
return pts;
}, "~O");
Clazz.overrideMethod (c$, "getQuaternionRotation", 
function (abc) {
return (this.unitCell == null ? null : this.unitCell.getQuaternionRotation (abc));
}, "~S");
});
