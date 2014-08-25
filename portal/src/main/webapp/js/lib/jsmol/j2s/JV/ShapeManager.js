Clazz.declarePackage ("JV");
Clazz.load (["JU.BS"], "JV.ShapeManager", ["java.lang.Boolean", "JU.P3", "J.api.Interface", "J.c.PAL", "$.VDW", "JM.Atom", "JU.BSUtil", "JV.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.gdata = null;
this.ms = null;
this.shapes = null;
this.vwr = null;
this.bsRenderableAtoms = null;
this.navigationCrossHairMinMax = null;
Clazz.instantialize (this, arguments);
}, JV, "ShapeManager");
Clazz.prepareFields (c$, function () {
this.bsRenderableAtoms =  new JU.BS ();
this.navigationCrossHairMinMax =  Clazz.newIntArray (4, 0);
});
Clazz.makeConstructor (c$, 
function (vwr) {
this.vwr = vwr;
this.gdata = vwr.gdata;
}, "JV.Viewer");
Clazz.defineMethod (c$, "findNearestShapeAtomIndex", 
function (x, y, closest, bsNot) {
if (this.shapes != null) for (var i = 0; i < this.shapes.length && closest[0] == null; ++i) if (this.shapes[i] != null) this.shapes[i].findNearestAtomIndex (x, y, closest, bsNot);

}, "~N,~N,~A,JU.BS");
Clazz.defineMethod (c$, "getShapes", 
function () {
return this.shapes;
});
Clazz.defineMethod (c$, "getShapePropertyIndex", 
function (shapeID, propertyName, index) {
if (this.shapes == null || this.shapes[shapeID] == null) return null;
this.vwr.setShapeErrorState (shapeID, "get " + propertyName);
var result = this.shapes[shapeID].getProperty (propertyName, index);
this.vwr.setShapeErrorState (-1, null);
return result;
}, "~N,~S,~N");
Clazz.defineMethod (c$, "getShapePropertyData", 
function (shapeID, propertyName, data) {
if (this.shapes == null || this.shapes[shapeID] == null) return false;
this.vwr.setShapeErrorState (shapeID, "get " + propertyName);
var result = this.shapes[shapeID].getPropertyData (propertyName, data);
this.vwr.setShapeErrorState (-1, null);
return result;
}, "~N,~S,~A");
Clazz.defineMethod (c$, "getShapeIdFromObjectName", 
function (objectName) {
if (this.shapes != null) for (var i = 16; i < 29; ++i) if (this.shapes[i] != null && this.shapes[i].getIndexFromName (objectName) >= 0) return i;

return -1;
}, "~S");
Clazz.defineMethod (c$, "loadDefaultShapes", 
function (newModelSet) {
this.ms = newModelSet;
if (this.shapes != null) for (var i = 0; i < this.shapes.length; ++i) if (this.shapes[i] != null) this.shapes[i].setModelSet (newModelSet);

this.loadShape (0);
this.loadShape (1);
}, "JM.ModelSet");
Clazz.defineMethod (c$, "loadShape", 
function (shapeID) {
if (this.shapes == null) return null;
if (this.shapes[shapeID] != null) return this.shapes[shapeID];
if (shapeID == 2 || shapeID == 3 || shapeID == 4) return null;
var className = JV.JC.getShapeClassName (shapeID, false);
var shape;
if ((shape = J.api.Interface.getInterface (className)) == null) return null;
this.vwr.setShapeErrorState (shapeID, "allocate");
shape.initializeShape (this.vwr, this.gdata, this.ms, shapeID);
this.vwr.setShapeErrorState (-1, null);
return this.shapes[shapeID] = shape;
}, "~N");
Clazz.defineMethod (c$, "refreshShapeTrajectories", 
function (baseModel, bs, mat) {
var Imodel = Integer.$valueOf (baseModel);
var bsModelAtoms = this.vwr.getModelUndeletedAtomsBitSet (baseModel);
for (var i = 0; i < 36; i++) if (this.shapes[i] != null) this.setShapePropertyBs (i, "refreshTrajectories", [Imodel, bs, mat], bsModelAtoms);

}, "~N,JU.BS,JU.M4");
Clazz.defineMethod (c$, "releaseShape", 
function (shapeID) {
if (this.shapes != null) this.shapes[shapeID] = null;
}, "~N");
Clazz.defineMethod (c$, "resetShapes", 
function () {
if (!this.vwr.noGraphicsAllowed ()) this.shapes =  new Array (36);
});
Clazz.defineMethod (c$, "setShapeSizeBs", 
function (shapeID, size, rd, bsSelected) {
if (this.shapes == null) return;
if (bsSelected == null && (shapeID != 1 || size != 2147483647)) bsSelected = this.vwr.bsA ();
if (rd != null && rd.value != 0 && rd.vdwType === J.c.VDW.TEMP) this.ms.getBfactor100Lo ();
this.vwr.setShapeErrorState (shapeID, "set size");
if (rd == null ? size != 0 : rd.value != 0) this.loadShape (shapeID);
if (this.shapes[shapeID] != null) {
this.shapes[shapeID].setShapeSizeRD (size, rd, bsSelected);
}this.vwr.setShapeErrorState (-1, null);
}, "~N,~N,J.atomdata.RadiusData,JU.BS");
Clazz.defineMethod (c$, "setLabel", 
function (strLabel, bsSelection) {
if (strLabel == null) {
if (this.shapes[5] == null) return;
} else {
this.loadShape (5);
this.setShapeSizeBs (5, 0, null, bsSelection);
}this.setShapePropertyBs (5, "label", strLabel, bsSelection);
}, "~O,JU.BS");
Clazz.defineMethod (c$, "setShapePropertyBs", 
function (shapeID, propertyName, value, bsSelected) {
if (this.shapes == null || this.shapes[shapeID] == null) return;
if (bsSelected == null) bsSelected = this.vwr.bsA ();
this.vwr.setShapeErrorState (shapeID, "set " + propertyName);
this.shapes[shapeID].setProperty (propertyName.intern (), value, bsSelected);
this.vwr.setShapeErrorState (-1, null);
}, "~N,~S,~O,JU.BS");
Clazz.defineMethod (c$, "checkFrankclicked", 
function (x, y) {
var frankShape = this.shapes[35];
return (frankShape != null && frankShape.wasClicked (x, y));
}, "~N,~N");
Clazz.defineMethod (c$, "checkObjectClicked", 
function (x, y, modifiers, bsVisible, drawPicking) {
var shape;
var map = null;
if (modifiers != 0 && this.vwr.getBondPicking () && (map = this.shapes[1].checkObjectClicked (x, y, modifiers, bsVisible, false)) != null) return map;
for (var i = 0; i < JV.ShapeManager.clickableMax; i++) if ((shape = this.shapes[JV.ShapeManager.hoverable[i]]) != null && (map = shape.checkObjectClicked (x, y, modifiers, bsVisible, drawPicking)) != null) return map;

return null;
}, "~N,~N,~N,JU.BS,~B");
Clazz.defineMethod (c$, "checkObjectDragged", 
function (prevX, prevY, x, y, modifiers, bsVisible, iShape) {
var found = false;
var n = (iShape > 0 ? iShape + 1 : 36);
for (var i = iShape; !found && i < n; ++i) if (this.shapes[i] != null) found = this.shapes[i].checkObjectDragged (prevX, prevY, x, y, modifiers, bsVisible);

return found;
}, "~N,~N,~N,~N,~N,JU.BS,~N");
Clazz.defineMethod (c$, "checkObjectHovered", 
function (x, y, bsVisible, checkBonds) {
var shape = this.shapes[1];
if (checkBonds && shape != null && shape.checkObjectHovered (x, y, bsVisible)) return true;
for (var i = 0; i < JV.ShapeManager.hoverable.length; i++) {
shape = this.shapes[JV.ShapeManager.hoverable[i]];
if (shape != null && shape.checkObjectHovered (x, y, bsVisible)) return true;
}
return false;
}, "~N,~N,JU.BS,~B");
Clazz.defineMethod (c$, "deleteShapeAtoms", 
function (value, bs) {
if (this.shapes != null) for (var j = 0; j < 36; j++) if (this.shapes[j] != null) this.setShapePropertyBs (j, "deleteModelAtoms", value, bs);

}, "~A,JU.BS");
Clazz.defineMethod (c$, "deleteVdwDependentShapes", 
function (bs) {
if (bs == null) bs = this.vwr.bsA ();
if (this.shapes[24] != null) this.shapes[24].setProperty ("deleteVdw", null, bs);
if (this.shapes[25] != null) this.shapes[25].setProperty ("deleteVdw", null, bs);
}, "JU.BS");
Clazz.defineMethod (c$, "getAtomShapeValue", 
function (tok, group, atomIndex) {
var iShape = JV.JC.shapeTokenIndex (tok);
if (iShape < 0 || this.shapes[iShape] == null) return 0;
var mad = this.shapes[iShape].getSize (atomIndex);
if (mad == 0) {
if ((group.shapeVisibilityFlags & this.shapes[iShape].vf) == 0) return 0;
mad = this.shapes[iShape].getSizeG (group);
}return mad / 2000;
}, "~N,JM.Group,~N");
Clazz.defineMethod (c$, "getObjectMap", 
function (map, withDollar) {
if (this.shapes == null) return;
var bDollar = Boolean.$valueOf (withDollar);
for (var i = 16; i < 29; ++i) this.getShapePropertyData (i, "getNames", [map, bDollar]);

}, "java.util.Map,~B");
Clazz.defineMethod (c$, "getProperty", 
function (paramInfo) {
if (paramInfo.equals ("getShapes")) return this.shapes;
return null;
}, "~O");
Clazz.defineMethod (c$, "getRenderableBitSet", 
function () {
return this.bsRenderableAtoms;
});
Clazz.defineMethod (c$, "getShape", 
function (i) {
return (this.shapes == null ? null : this.shapes[i]);
}, "~N");
Clazz.defineMethod (c$, "mergeShapes", 
function (newShapes) {
if (newShapes == null) return;
if (this.shapes == null) this.shapes = newShapes;
 else for (var i = 0; i < newShapes.length; ++i) if (newShapes[i] != null) {
if (this.shapes[i] == null) this.loadShape (i);
this.shapes[i].merge (newShapes[i]);
}
}, "~A");
Clazz.defineMethod (c$, "resetBioshapes", 
function (bsAllAtoms) {
if (this.shapes == null) return;
for (var i = 0; i < this.shapes.length; ++i) if (this.shapes[i] != null && this.shapes[i].isBioShape) {
this.shapes[i].setModelSet (this.ms);
this.shapes[i].setShapeSizeRD (0, null, bsAllAtoms);
this.shapes[i].setProperty ("color", J.c.PAL.NONE, bsAllAtoms);
}
}, "JU.BS");
Clazz.defineMethod (c$, "setAtomLabel", 
function (strLabel, i) {
if (this.shapes == null) return;
this.loadShape (5);
this.shapes[5].setProperty ("label:" + strLabel, Integer.$valueOf (i), null);
}, "~S,~N");
Clazz.defineMethod (c$, "setModelVisibility", 
function () {
var shapes = this.shapes;
if (shapes == null || shapes[0] == null) return;
var bs = this.vwr.getVisibleFramesBitSet ();
for (var i = 8; i < 32; i++) if (shapes[i] != null) shapes[i].setVisibilityFlags (bs);

var showHydrogens = this.vwr.getBoolean (603979922);
var bsDeleted = this.vwr.getDeletedAtoms ();
var atoms = this.ms.at;
for (var i = this.ms.ac; --i >= 0; ) {
var atom = atoms[i];
atom.shapeVisibilityFlags &= -64;
if (bsDeleted != null && bsDeleted.get (i) || !showHydrogens && atom.getElementNumber () == 1) continue;
var modelIndex = atom.getModelIndex ();
if (bs.get (modelIndex)) {
var f = 1;
if (!this.ms.isAtomHidden (i)) {
f |= 8;
if (atom.madAtom != 0) f |= 16;
atom.setShapeVisibility (f, true);
}}}
for (var i = 0; i < 36; ++i) {
var shape = shapes[i];
if (shape != null) shape.setModelClickability ();
}
});
Clazz.defineMethod (c$, "finalizeAtoms", 
function (bsAtoms, ptOffset) {
var vwr = this.vwr;
var tm = vwr.tm;
var bs = this.bsRenderableAtoms;
var gdata = this.gdata;
if (bsAtoms != null) {
var ptCenter = this.ms.getAtomSetCenter (bsAtoms);
var pt =  new JU.P3 ();
tm.transformPt3f (ptCenter, pt);
pt.add (ptOffset);
tm.unTransformPoint (pt, pt);
pt.sub (ptCenter);
vwr.setAtomCoordsRelative (pt, bsAtoms);
ptOffset.set (0, 0, 0);
}this.ms.getRenderable (bs);
var vibrationVectors = this.ms.vibrations;
var vibs = (vibrationVectors != null && tm.vibrationOn);
var atoms = this.ms.at;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var atom = atoms[i];
var screen = (vibs && atom.hasVibration () ? tm.transformPtVib (atom, vibrationVectors[i]) : tm.transformPt (atom));
atom.sX = screen.x;
atom.sY = screen.y;
atom.sZ = screen.z;
var d = Math.abs (atom.madAtom);
if (d == JM.Atom.MAD_GLOBAL) d = Clazz.floatToInt (vwr.getFloat (1141899265) * 2000);
atom.sD = Clazz.floatToShort (vwr.tm.scaleToScreen (screen.z, d));
}
if (tm.slabEnabled) {
var slabByMolecule = vwr.getBoolean (603979940);
var slabByAtom = vwr.getBoolean (603979939);
var minZ = gdata.getSlab ();
var maxZ = gdata.getDepth ();
if (slabByMolecule) {
var molecules = this.ms.getMolecules ();
var moleculeCount = this.ms.getMoleculeCountInModel (-1);
for (var i = 0; i < moleculeCount; i++) {
var m = molecules[i];
var j = 0;
var pt = m.firstAtomIndex;
if (!bs.get (pt)) continue;
for (; j < m.ac; j++, pt++) if (gdata.isClippedZ (atoms[pt].sZ - (atoms[pt].sD >> 1))) break;

if (j != m.ac) {
pt = m.firstAtomIndex;
for (var k = 0; k < m.ac; k++) {
bs.clear (pt);
atoms[pt++].sZ = 0;
}
}}
}for (var i = bs.nextSetBit (0); i >= 0; i = this.bsRenderableAtoms.nextSetBit (i + 1)) {
var atom = atoms[i];
if (gdata.isClippedZ (atom.sZ - (slabByAtom ? atoms[i].sD >> 1 : 0))) {
atom.setClickable (0);
var r = Clazz.doubleToInt ((slabByAtom ? -1 : 1) * atom.sD / 2);
if (atom.sZ + r < minZ || atom.sZ - r > maxZ || !gdata.isInDisplayRange (atom.sX, atom.sY)) {
bs.clear (i);
}}}
}if (this.ms.getAtomCount () == 0 || !vwr.getShowNavigationPoint ()) return null;
var minX = 2147483647;
var maxX = -2147483648;
var minY = 2147483647;
var maxY = -2147483648;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var atom = atoms[i];
if (atom.sX < minX) minX = atom.sX;
if (atom.sX > maxX) maxX = atom.sX;
if (atom.sY < minY) minY = atom.sY;
if (atom.sY > maxY) maxY = atom.sY;
}
this.navigationCrossHairMinMax[0] = minX;
this.navigationCrossHairMinMax[1] = maxX;
this.navigationCrossHairMinMax[2] = minY;
this.navigationCrossHairMinMax[3] = maxY;
return this.navigationCrossHairMinMax;
}, "JU.BS,JU.P3");
Clazz.defineMethod (c$, "setModelSet", 
function (modelSet) {
this.ms = this.vwr.ms = modelSet;
}, "JM.ModelSet");
Clazz.defineMethod (c$, "checkInheritedShapes", 
function () {
if (this.shapes[24] == null) return;
this.setShapePropertyBs (24, "remapInherited", null, null);
});
Clazz.defineMethod (c$, "restrictSelected", 
function (isBond, doInvert) {
var bsSelected = this.vwr.getSelectedAtomsNoSubset ();
if (doInvert) {
this.vwr.invertSelection ();
var bsSubset = this.vwr.getSelectionSubset ();
if (bsSubset != null) {
bsSelected = this.vwr.getSelectedAtomsNoSubset ();
bsSelected.and (bsSubset);
this.vwr.select (bsSelected, false, 0, true);
JU.BSUtil.invertInPlace (bsSelected, this.vwr.getAtomCount ());
bsSelected.and (bsSubset);
}}JU.BSUtil.andNot (bsSelected, this.vwr.getDeletedAtoms ());
var bondmode = this.vwr.getBoolean (603979812);
if (!isBond) this.vwr.setBooleanProperty ("bondModeOr", true);
this.setShapeSizeBs (1, 0, null, null);
this.setShapePropertyBs (1, "type", Integer.$valueOf (32768), null);
this.setShapeSizeBs (1, 0, null, null);
this.setShapePropertyBs (1, "type", Integer.$valueOf (1023), null);
var bs = this.vwr.bsA ();
for (var iShape = 21; --iShape >= 0; ) if (iShape != 6 && this.getShape (iShape) != null) this.setShapeSizeBs (iShape, 0, null, bs);

if (this.getShape (21) != null) this.setShapePropertyBs (21, "delete", bs, null);
this.setLabel (null, bs);
if (!isBond) this.vwr.setBooleanProperty ("bondModeOr", bondmode);
this.vwr.select (bsSelected, false, 0, true);
}, "~B,~B");
Clazz.defineStatics (c$,
"hoverable", [30, 25, 24, 22, 35]);
c$.clickableMax = c$.prototype.clickableMax = JV.ShapeManager.hoverable.length - 1;
});
