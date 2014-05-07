Clazz.declarePackage ("J.viewer");
Clazz.load (["J.util.BS"], "J.viewer.ShapeManager", ["java.lang.Boolean", "java.util.Hashtable", "J.constant.EnumPalette", "$.EnumVdw", "J.util.BSUtil", "$.Logger", "$.P3", "$.SB", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.gdata = null;
this.modelSet = null;
this.shapes = null;
this.viewer = null;
this.bsRenderableAtoms = null;
this.navigationCrossHairMinMax = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "ShapeManager");
Clazz.prepareFields (c$, function () {
this.bsRenderableAtoms =  new J.util.BS ();
this.navigationCrossHairMinMax =  Clazz.newIntArray (4, 0);
});
Clazz.makeConstructor (c$, 
function (viewer) {
this.viewer = viewer;
this.gdata = viewer.getGraphicsData ();
}, "J.viewer.Viewer");
$_M(c$, "findNearestShapeAtomIndex", 
function (x, y, closest, bsNot) {
if (this.shapes != null) for (var i = 0; i < this.shapes.length && closest[0] == null; ++i) if (this.shapes[i] != null) this.shapes[i].findNearestAtomIndex (x, y, closest, bsNot);

}, "~N,~N,~A,J.util.BS");
$_M(c$, "getShapes", 
function () {
return this.shapes;
});
$_M(c$, "getShapePropertyIndex", 
function (shapeID, propertyName, index) {
if (this.shapes == null || this.shapes[shapeID] == null) return null;
this.viewer.setShapeErrorState (shapeID, "get " + propertyName);
var result = this.shapes[shapeID].getProperty (propertyName, index);
this.viewer.setShapeErrorState (-1, null);
return result;
}, "~N,~S,~N");
$_M(c$, "getShapePropertyData", 
function (shapeID, propertyName, data) {
if (this.shapes == null || this.shapes[shapeID] == null) return false;
this.viewer.setShapeErrorState (shapeID, "get " + propertyName);
var result = this.shapes[shapeID].getPropertyData (propertyName, data);
this.viewer.setShapeErrorState (-1, null);
return result;
}, "~N,~S,~A");
$_M(c$, "getShapeIdFromObjectName", 
function (objectName) {
if (this.shapes != null) for (var i = 16; i < 29; ++i) if (this.shapes[i] != null && this.shapes[i].getIndexFromName (objectName) >= 0) return i;

return -1;
}, "~S");
$_M(c$, "loadDefaultShapes", 
function (newModelSet) {
this.modelSet = newModelSet;
if (this.shapes != null) for (var i = 0; i < this.shapes.length; ++i) if (this.shapes[i] != null) this.shapes[i].setModelSet (newModelSet);

this.loadShape (0);
this.loadShape (1);
this.loadShape (6);
this.loadShape (32);
this.loadShape (33);
}, "J.modelset.ModelSet");
$_M(c$, "loadShape", 
function (shapeID) {
if (this.shapes == null) return null;
if (this.shapes[shapeID] != null) return this.shapes[shapeID];
if (shapeID == 2 || shapeID == 3 || shapeID == 4) return null;
var className = J.viewer.JC.getShapeClassName (shapeID, false);
try {
var shapeClass = Class.forName (className);
var shape = shapeClass.newInstance ();
this.viewer.setShapeErrorState (shapeID, "allocate");
shape.initializeShape (this.viewer, this.gdata, this.modelSet, shapeID);
this.viewer.setShapeErrorState (-1, null);
return this.shapes[shapeID] = shape;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.errorEx ("Could not instantiate shape:" + className, e);
return null;
} else {
throw e;
}
}
}, "~N");
$_M(c$, "refreshShapeTrajectories", 
function (baseModel, bs, mat) {
var Imodel = Integer.$valueOf (baseModel);
var bsModelAtoms = this.viewer.getModelUndeletedAtomsBitSet (baseModel);
for (var i = 0; i < 36; i++) if (this.shapes[i] != null) this.setShapePropertyBs (i, "refreshTrajectories", [Imodel, bs, mat], bsModelAtoms);

}, "~N,J.util.BS,J.util.Matrix4f");
$_M(c$, "releaseShape", 
function (shapeID) {
if (this.shapes != null) this.shapes[shapeID] = null;
}, "~N");
$_M(c$, "resetShapes", 
function () {
if (!this.viewer.noGraphicsAllowed ()) this.shapes =  new Array (36);
});
$_M(c$, "setShapeSizeBs", 
function (shapeID, size, rd, bsSelected) {
if (this.shapes == null) return;
if (bsSelected == null && (shapeID != 1 || size != 2147483647)) bsSelected = this.viewer.getSelectionSet (false);
if (rd != null && rd.value != 0 && rd.vdwType === J.constant.EnumVdw.TEMP) this.modelSet.getBfactor100Lo ();
this.viewer.setShapeErrorState (shapeID, "set size");
if (rd == null ? size != 0 : rd.value != 0) this.loadShape (shapeID);
if (this.shapes[shapeID] != null) {
this.shapes[shapeID].setShapeSizeRD (size, rd, bsSelected);
}this.viewer.setShapeErrorState (-1, null);
}, "~N,~N,J.atomdata.RadiusData,J.util.BS");
$_M(c$, "setLabel", 
function (strLabel, bsSelection) {
if (strLabel == null) {
if (this.shapes[5] == null) return;
} else {
this.loadShape (5);
this.setShapeSizeBs (5, 0, null, bsSelection);
}this.setShapePropertyBs (5, "label", strLabel, bsSelection);
}, "~S,J.util.BS");
$_M(c$, "setShapePropertyBs", 
function (shapeID, propertyName, value, bsSelected) {
if (this.shapes == null || this.shapes[shapeID] == null) return;
if (bsSelected == null) bsSelected = this.viewer.getSelectionSet (false);
this.viewer.setShapeErrorState (shapeID, "set " + propertyName);
this.shapes[shapeID].setProperty (propertyName.intern (), value, bsSelected);
this.viewer.setShapeErrorState (-1, null);
}, "~N,~S,~O,J.util.BS");
$_M(c$, "checkFrankclicked", 
function (x, y) {
var frankShape = this.shapes[35];
return (frankShape != null && frankShape.wasClicked (x, y));
}, "~N,~N");
$_M(c$, "checkObjectClicked", 
function (x, y, modifiers, bsVisible, drawPicking) {
var shape;
var map = null;
if (modifiers != 0 && this.viewer.getBondPicking () && (map = this.shapes[1].checkObjectClicked (x, y, modifiers, bsVisible, drawPicking)) != null) return map;
for (var i = 0; i < J.viewer.ShapeManager.clickableMax; i++) if ((shape = this.shapes[J.viewer.ShapeManager.hoverable[i]]) != null && (map = shape.checkObjectClicked (x, y, modifiers, bsVisible, drawPicking)) != null) return map;

return null;
}, "~N,~N,~N,J.util.BS,~B");
$_M(c$, "checkObjectDragged", 
function (prevX, prevY, x, y, modifiers, bsVisible, iShape) {
var found = false;
var n = (iShape > 0 ? iShape + 1 : 36);
for (var i = iShape; !found && i < n; ++i) if (this.shapes[i] != null) found = this.shapes[i].checkObjectDragged (prevX, prevY, x, y, modifiers, bsVisible);

return found;
}, "~N,~N,~N,~N,~N,J.util.BS,~N");
$_M(c$, "checkObjectHovered", 
function (x, y, bsVisible, checkBonds) {
var shape = this.shapes[1];
if (checkBonds && shape != null && shape.checkObjectHovered (x, y, bsVisible)) return true;
for (var i = 0; i < J.viewer.ShapeManager.hoverable.length; i++) {
shape = this.shapes[J.viewer.ShapeManager.hoverable[i]];
if (shape != null && shape.checkObjectHovered (x, y, bsVisible)) return true;
}
return false;
}, "~N,~N,J.util.BS,~B");
$_M(c$, "deleteShapeAtoms", 
function (value, bs) {
if (this.shapes != null) for (var j = 0; j < 36; j++) if (this.shapes[j] != null) this.setShapePropertyBs (j, "deleteModelAtoms", value, bs);

}, "~A,J.util.BS");
$_M(c$, "deleteVdwDependentShapes", 
function (bs) {
if (bs == null) bs = this.viewer.getSelectionSet (false);
if (this.shapes[24] != null) this.shapes[24].setProperty ("deleteVdw", null, bs);
if (this.shapes[25] != null) this.shapes[25].setProperty ("deleteVdw", null, bs);
}, "J.util.BS");
$_M(c$, "getAtomShapeValue", 
function (tok, group, atomIndex) {
var iShape = J.viewer.JC.shapeTokenIndex (tok);
if (iShape < 0 || this.shapes[iShape] == null) return 0;
var mad = this.shapes[iShape].getSize (atomIndex);
if (mad == 0) {
if ((group.shapeVisibilityFlags & this.shapes[iShape].myVisibilityFlag) == 0) return 0;
mad = this.shapes[iShape].getSizeG (group);
}return mad / 2000;
}, "~N,J.modelset.Group,~N");
$_M(c$, "getObjectMap", 
function (map, withDollar) {
if (this.shapes == null) return;
var bDollar = Boolean.$valueOf (withDollar);
for (var i = 16; i < 29; ++i) this.getShapePropertyData (i, "getNames", [map, bDollar]);

}, "java.util.Map,~B");
$_M(c$, "getProperty", 
function (paramInfo) {
if (paramInfo.equals ("getShapes")) return this.shapes;
return null;
}, "~O");
$_M(c$, "getRenderableBitSet", 
function () {
return this.bsRenderableAtoms;
});
$_M(c$, "getShape", 
function (i) {
return (this.shapes == null ? null : this.shapes[i]);
}, "~N");
$_M(c$, "getShapeInfo", 
function () {
var info =  new java.util.Hashtable ();
var commands =  new J.util.SB ();
if (this.shapes != null) for (var i = 0; i < 36; ++i) {
var shape = this.shapes[i];
if (shape != null) {
var shapeType = J.viewer.JC.shapeClassBases[i];
var shapeDetail = shape.getShapeDetail ();
if (shapeDetail != null) info.put (shapeType, shapeDetail);
}}
if (commands.length () > 0) info.put ("shapeCommands", commands.toString ());
return info;
});
$_M(c$, "mergeShapes", 
function (newShapes) {
if (newShapes == null) return;
if (this.shapes == null) this.shapes = newShapes;
 else for (var i = 0; i < newShapes.length; ++i) if (newShapes[i] != null) {
if (this.shapes[i] == null) this.loadShape (i);
this.shapes[i].merge (newShapes[i]);
}
}, "~A");
$_M(c$, "resetBioshapes", 
function (bsAllAtoms) {
if (this.shapes == null) return;
for (var i = 0; i < this.shapes.length; ++i) if (this.shapes[i] != null && this.shapes[i].isBioShape) {
this.shapes[i].setModelSet (this.modelSet);
this.shapes[i].setShapeSizeRD (0, null, bsAllAtoms);
this.shapes[i].setProperty ("color", J.constant.EnumPalette.NONE, bsAllAtoms);
}
}, "J.util.BS");
$_M(c$, "setAtomLabel", 
function (strLabel, i) {
if (this.shapes == null) return;
this.loadShape (5);
this.shapes[5].setProperty ("label:" + strLabel, Integer.$valueOf (i), null);
}, "~S,~N");
$_M(c$, "setModelVisibility", 
function () {
if (this.shapes == null || this.shapes[0] == null) return;
var bs = this.viewer.getVisibleFramesBitSet ();
for (var i = 1; i < 36; i++) if (this.shapes[i] != null) this.shapes[i].setVisibilityFlags (bs);

this.shapes[0].setVisibilityFlags (bs);
for (var i = 0; i < 36; ++i) {
var shape = this.shapes[i];
if (shape != null) shape.setModelClickability ();
}
});
$_M(c$, "finalizeAtoms", 
function (bsAtoms, ptOffset) {
if (bsAtoms != null) {
var ptCenter = this.viewer.getAtomSetCenter (bsAtoms);
var pt =  new J.util.P3 ();
this.viewer.transformPt3f (ptCenter, pt);
pt.add (ptOffset);
this.viewer.unTransformPoint (pt, pt);
pt.sub (ptCenter);
this.viewer.setAtomCoordsRelative (pt, bsAtoms);
ptOffset.set (0, 0, 0);
}this.bsRenderableAtoms.clearAll ();
var atoms = this.modelSet.atoms;
for (var i = this.modelSet.getAtomCount (); --i >= 0; ) {
var atom = atoms[i];
if ((atom.getShapeVisibilityFlags () & 1) == 0) continue;
this.bsRenderableAtoms.set (i);
}
}, "J.util.BS,J.util.P3");
$_M(c$, "transformAtoms", 
function () {
var vibrationVectors = this.modelSet.vibrations;
var atoms = this.modelSet.atoms;
for (var i = this.bsRenderableAtoms.nextSetBit (0); i >= 0; i = this.bsRenderableAtoms.nextSetBit (i + 1)) {
var atom = atoms[i];
var screen = (vibrationVectors != null && atom.hasVibration () ? this.viewer.transformPtVib (atom, vibrationVectors[i]) : this.viewer.transformPt (atom));
atom.screenX = screen.x;
atom.screenY = screen.y;
atom.screenZ = screen.z;
atom.screenDiameter = Clazz.floatToShort (this.viewer.scaleToScreen (screen.z, Math.abs (atom.madAtom)));
}
if (this.viewer.getSlabEnabled ()) {
var slabByMolecule = this.viewer.getBoolean (603979940);
var slabByAtom = this.viewer.getBoolean (603979938);
var minZ = this.gdata.getSlab ();
var maxZ = this.gdata.getDepth ();
if (slabByMolecule) {
var molecules = this.modelSet.getMolecules ();
var moleculeCount = this.modelSet.getMoleculeCountInModel (-1);
for (var i = 0; i < moleculeCount; i++) {
var m = molecules[i];
var j = 0;
var pt = m.firstAtomIndex;
if (!this.bsRenderableAtoms.get (pt)) continue;
for (; j < m.atomCount; j++, pt++) if (this.gdata.isClippedZ (atoms[pt].screenZ - (atoms[pt].screenDiameter >> 1))) break;

if (j != m.atomCount) {
pt = m.firstAtomIndex;
for (var k = 0; k < m.atomCount; k++) {
this.bsRenderableAtoms.clear (pt);
atoms[pt++].screenZ = 0;
}
}}
}for (var i = this.bsRenderableAtoms.nextSetBit (0); i >= 0; i = this.bsRenderableAtoms.nextSetBit (i + 1)) {
var atom = atoms[i];
if (this.gdata.isClippedZ (atom.screenZ - (slabByAtom ? atoms[i].screenDiameter >> 1 : 0))) {
atom.setClickable (0);
var r = Clazz.doubleToInt ((slabByAtom ? -1 : 1) * atom.screenDiameter / 2);
if (atom.screenZ + r < minZ || atom.screenZ - r > maxZ || !this.gdata.isInDisplayRange (atom.screenX, atom.screenY)) {
this.bsRenderableAtoms.clear (i);
}}}
}if (this.modelSet.getAtomCount () == 0 || !this.viewer.getShowNavigationPoint ()) return null;
var minX = 2147483647;
var maxX = -2147483648;
var minY = 2147483647;
var maxY = -2147483648;
for (var i = this.bsRenderableAtoms.nextSetBit (0); i >= 0; i = this.bsRenderableAtoms.nextSetBit (i + 1)) {
var atom = atoms[i];
if (atom.screenX < minX) minX = atom.screenX;
if (atom.screenX > maxX) maxX = atom.screenX;
if (atom.screenY < minY) minY = atom.screenY;
if (atom.screenY > maxY) maxY = atom.screenY;
}
this.navigationCrossHairMinMax[0] = minX;
this.navigationCrossHairMinMax[1] = maxX;
this.navigationCrossHairMinMax[2] = minY;
this.navigationCrossHairMinMax[3] = maxY;
return this.navigationCrossHairMinMax;
});
$_M(c$, "setModelSet", 
function (modelSet) {
this.modelSet = this.viewer.modelSet = modelSet;
}, "J.modelset.ModelSet");
$_M(c$, "checkInheritedShapes", 
function () {
if (this.shapes[24] == null) return;
this.setShapePropertyBs (24, "remapInherited", null, null);
});
$_M(c$, "restrictSelected", 
function (isBond, doInvert) {
var bsSelected = J.util.BSUtil.copy (this.viewer.getSelectionSet (true));
if (doInvert) {
this.viewer.invertSelection ();
var bsSubset = this.viewer.getSelectionSubset ();
if (bsSubset != null) {
bsSelected = J.util.BSUtil.copy (this.viewer.getSelectionSet (true));
bsSelected.and (bsSubset);
this.viewer.select (bsSelected, false, 0, true);
J.util.BSUtil.invertInPlace (bsSelected, this.viewer.getAtomCount ());
bsSelected.and (bsSubset);
}}J.util.BSUtil.andNot (bsSelected, this.viewer.getDeletedAtoms ());
var bondmode = this.viewer.getBoolean (603979812);
if (!isBond) this.viewer.setBooleanProperty ("bondModeOr", true);
this.setShapeSizeBs (1, 0, null, null);
this.setShapePropertyBs (1, "type", Integer.$valueOf (32768), null);
this.setShapeSizeBs (1, 0, null, null);
this.setShapePropertyBs (1, "type", Integer.$valueOf (1023), null);
var bs = this.viewer.getSelectionSet (false);
for (var iShape = 21; --iShape >= 0; ) if (iShape != 6 && this.getShape (iShape) != null) this.setShapeSizeBs (iShape, 0, null, bs);

if (this.getShape (21) != null) this.setShapePropertyBs (21, "delete", bs, null);
this.setLabel (null, bs);
if (!isBond) this.viewer.setBooleanProperty ("bondModeOr", bondmode);
this.viewer.select (bsSelected, false, 0, true);
}, "~B,~B");
Clazz.defineStatics (c$,
"hoverable", [30, 25, 24, 22, 35]);
c$.clickableMax = c$.prototype.clickableMax = J.viewer.ShapeManager.hoverable.length - 1;
});
