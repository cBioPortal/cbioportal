Clazz.declarePackage ("J.renderbio");
Clazz.load (["J.render.MeshRenderer", "JU.A4", "$.BS", "$.M3", "$.P3", "$.P3i", "$.V3"], "J.renderbio.BioShapeRenderer", ["J.c.STR", "JM.CarbohydratePolymer", "$.NucleicPolymer", "J.shape.Mesh", "JU.C", "$.GData", "$.Logger", "$.Normix"], function () {
c$ = Clazz.decorateAsClass (function () {
this.invalidateMesh = false;
this.invalidateSheets = false;
this.isHighRes = false;
this.isTraceAlpha = false;
this.ribbonBorder = false;
this.haveControlPointScreens = false;
this.aspectRatio = 0;
this.hermiteLevel = 0;
this.sheetSmoothing = 0;
this.cartoonsFancy = false;
this.meshes = null;
this.meshReady = null;
this.bsRenderMesh = null;
this.monomerCount = 0;
this.monomers = null;
this.isNucleic = false;
this.isCarbohydrate = false;
this.bsVisible = null;
this.ribbonTopScreens = null;
this.ribbonBottomScreens = null;
this.controlPoints = null;
this.controlPointScreens = null;
this.leadAtomIndices = null;
this.wingVectors = null;
this.mads = null;
this.colixes = null;
this.colixesBack = null;
this.structureTypes = null;
this.isPass2 = false;
this.wireframeOnly = false;
this.pointT = null;
this.iPrev = 0;
this.iNext = 0;
this.iNext2 = 0;
this.iNext3 = 0;
this.diameterBeg = 0;
this.diameterMid = 0;
this.diameterEnd = 0;
this.doCap0 = false;
this.doCap1 = false;
this.colixBack = 0;
this.reversed = null;
this.screenArrowTop = null;
this.screenArrowTopPrev = null;
this.screenArrowBot = null;
this.screenArrowBotPrev = null;
this.controlHermites = null;
this.wingHermites = null;
this.radiusHermites = null;
this.norm = null;
this.wing = null;
this.wing1 = null;
this.wingT = null;
this.aa = null;
this.pt = null;
this.pt1 = null;
this.ptPrev = null;
this.ptNext = null;
this.mat = null;
this.bsTemp = null;
this.norml = null;
Clazz.instantialize (this, arguments);
}, J.renderbio, "BioShapeRenderer", J.render.MeshRenderer);
Clazz.prepareFields (c$, function () {
this.bsVisible =  new JU.BS ();
this.pointT =  new JU.P3 ();
this.screenArrowTop =  new JU.P3i ();
this.screenArrowTopPrev =  new JU.P3i ();
this.screenArrowBot =  new JU.P3i ();
this.screenArrowBotPrev =  new JU.P3i ();
this.norm =  new JU.V3 ();
this.wing =  new JU.V3 ();
this.wing1 =  new JU.V3 ();
this.wingT =  new JU.V3 ();
this.aa =  new JU.A4 ();
this.pt =  new JU.P3 ();
this.pt1 =  new JU.P3 ();
this.ptPrev =  new JU.P3 ();
this.ptNext =  new JU.P3 ();
this.mat =  new JU.M3 ();
this.norml =  new JU.V3 ();
});
Clazz.overrideMethod (c$, "render", 
function () {
if (this.shape == null) return false;
this.setGlobals ();
this.renderShapes ();
return this.needTranslucent;
});
Clazz.defineMethod (c$, "setGlobals", 
 function () {
this.isPass2 = this.g3d.isPass2 ();
this.invalidateMesh = false;
this.needTranslucent = false;
this.g3d.addRenderer (553648147);
var TF = (!this.isExport && !this.vwr.checkMotionRendering (1113200642));
if (TF != this.wireframeOnly) this.invalidateMesh = true;
this.wireframeOnly = TF;
TF = (this.isExport || !this.wireframeOnly && this.vwr.getBoolean (603979864));
if (TF != this.isHighRes) this.invalidateMesh = true;
this.isHighRes = TF;
TF = !this.wireframeOnly && this.vwr.getBoolean (603979817);
if (this.cartoonsFancy != TF) {
this.invalidateMesh = true;
this.cartoonsFancy = TF;
}var val1 = this.vwr.getHermiteLevel ();
val1 = (val1 <= 0 ? -val1 : this.vwr.getInMotion (true) ? 0 : val1);
if (this.cartoonsFancy && !this.wireframeOnly) val1 = Math.max (val1, 3);
if (val1 != this.hermiteLevel) this.invalidateMesh = true;
this.hermiteLevel = Math.min (val1, 8);
var val = this.vwr.getInt (553648166);
val = Math.min (Math.max (0, val), 20);
if (this.cartoonsFancy && val >= 16) val = 4;
if (this.wireframeOnly || this.hermiteLevel == 0) val = 0;
if (val != this.aspectRatio && val != 0 && val1 != 0) this.invalidateMesh = true;
this.aspectRatio = val;
TF = this.vwr.getBoolean (603979966);
if (TF != this.isTraceAlpha) this.invalidateMesh = true;
this.isTraceAlpha = TF;
this.invalidateSheets = false;
var fval = this.vwr.getFloat (570425392);
if (fval != this.sheetSmoothing && this.isTraceAlpha) {
this.sheetSmoothing = fval;
this.invalidateMesh = true;
this.invalidateSheets = true;
}});
Clazz.defineMethod (c$, "renderShapes", 
 function () {
var mps = this.shape;
for (var c = mps.bioShapes.length; --c >= 0; ) {
var bioShape = mps.getBioShape (c);
if ((bioShape.modelVisibilityFlags & this.myVisibilityFlag) == 0) continue;
if (bioShape.monomerCount >= 2 && this.initializePolymer (bioShape)) {
this.bsRenderMesh.clearAll ();
this.renderBioShape (bioShape);
this.renderMeshes ();
this.freeTempArrays ();
}}
});
Clazz.defineMethod (c$, "setBioColix", 
function (colix) {
if (this.g3d.setC (colix)) return true;
this.needTranslucent = true;
return false;
}, "~N");
Clazz.defineMethod (c$, "freeTempArrays", 
 function () {
if (this.haveControlPointScreens) this.vwr.freeTempScreens (this.controlPointScreens);
this.vwr.freeTempEnum (this.structureTypes);
});
Clazz.defineMethod (c$, "initializePolymer", 
 function (bioShape) {
var bsDeleted = this.vwr.getDeletedAtoms ();
if (this.vwr.ms.isJmolDataFrameForModel (bioShape.modelIndex)) {
this.controlPoints = bioShape.bioPolymer.getControlPoints (true, 0, false);
} else {
this.controlPoints = bioShape.bioPolymer.getControlPoints (this.isTraceAlpha, this.sheetSmoothing, this.invalidateSheets);
}this.monomerCount = bioShape.monomerCount;
this.bsRenderMesh = JU.BS.newN (this.monomerCount);
this.monomers = bioShape.monomers;
this.reversed = bioShape.bioPolymer.reversed;
this.leadAtomIndices = bioShape.bioPolymer.getLeadAtomIndices ();
this.bsVisible.clearAll ();
var haveVisible = false;
if (this.invalidateMesh) bioShape.falsifyMesh ();
for (var i = this.monomerCount; --i >= 0; ) {
if ((this.monomers[i].shapeVisibilityFlags & this.myVisibilityFlag) == 0 || this.ms.isAtomHidden (this.leadAtomIndices[i]) || bsDeleted != null && bsDeleted.get (this.leadAtomIndices[i])) continue;
var lead = this.ms.at[this.leadAtomIndices[i]];
if (!this.g3d.isInDisplayRange (lead.sX, lead.sY)) continue;
this.bsVisible.set (i);
haveVisible = true;
}
if (!haveVisible) return false;
this.ribbonBorder = this.vwr.getBoolean (603979898);
this.isNucleic = Clazz.instanceOf (bioShape.bioPolymer, JM.NucleicPolymer);
this.isCarbohydrate = Clazz.instanceOf (bioShape.bioPolymer, JM.CarbohydratePolymer);
this.haveControlPointScreens = false;
this.wingVectors = bioShape.wingVectors;
this.meshReady = bioShape.meshReady;
this.meshes = bioShape.meshes;
this.mads = bioShape.mads;
this.colixes = bioShape.colixes;
this.colixesBack = bioShape.colixesBack;
this.setStructureTypes ();
return true;
}, "J.shapebio.BioShape");
Clazz.defineMethod (c$, "setStructureTypes", 
 function () {
this.structureTypes = this.vwr.allocTempEnum (this.monomerCount + 1);
for (var i = this.monomerCount; --i >= 0; ) {
this.structureTypes[i] = this.monomers[i].getProteinStructureType ();
if (this.structureTypes[i] === J.c.STR.TURN) this.structureTypes[i] = J.c.STR.NONE;
}
this.structureTypes[this.monomerCount] = this.structureTypes[this.monomerCount - 1];
});
Clazz.defineMethod (c$, "isHelix", 
function (i) {
return this.structureTypes[i] === J.c.STR.HELIX;
}, "~N");
Clazz.defineMethod (c$, "getScreenControlPoints", 
function () {
this.calcScreenControlPoints (this.controlPoints);
});
Clazz.defineMethod (c$, "calcScreenControlPoints", 
function (points) {
var count = this.monomerCount + 1;
this.controlPointScreens = this.vwr.allocTempScreens (count);
for (var i = count; --i >= 0; ) {
this.tm.transformPtScr (points[i], this.controlPointScreens[i]);
}
this.haveControlPointScreens = true;
}, "~A");
Clazz.defineMethod (c$, "calcScreens", 
function (offsetFraction) {
var count = this.controlPoints.length;
var screens = this.vwr.allocTempScreens (count);
if (offsetFraction == 0) {
for (var i = count; --i >= 0; ) this.tm.transformPtScr (this.controlPoints[i], screens[i]);

} else {
var offset_1000 = offsetFraction / 1000;
for (var i = count; --i >= 0; ) this.calc1Screen (this.controlPoints[i], this.wingVectors[i], (this.mads[i] == 0 && i > 0 ? this.mads[i - 1] : this.mads[i]), offset_1000, screens[i]);

}return screens;
}, "~N");
Clazz.defineMethod (c$, "calc1Screen", 
 function (center, vector, mad, offset_1000, screen) {
this.pointT.scaleAdd2 (mad * offset_1000, vector, center);
this.tm.transformPtScr (this.pointT, screen);
}, "JU.P3,JU.V3,~N,~N,JU.P3i");
Clazz.defineMethod (c$, "getLeadColix", 
function (i) {
return JU.C.getColixInherited (this.colixes[i], this.monomers[i].getLeadAtom ().getColix ());
}, "~N");
Clazz.defineMethod (c$, "getLeadColixBack", 
function (i) {
return (this.colixesBack == null || this.colixesBack.length <= i ? 0 : this.colixesBack[i]);
}, "~N");
Clazz.defineMethod (c$, "setNeighbors", 
 function (i) {
this.iPrev = Math.max (i - 1, 0);
this.iNext = Math.min (i + 1, this.monomerCount);
this.iNext2 = Math.min (i + 2, this.monomerCount);
this.iNext3 = Math.min (i + 3, this.monomerCount);
}, "~N");
Clazz.defineMethod (c$, "setMads", 
 function (i, thisTypeOnly) {
this.madMid = this.madBeg = this.madEnd = this.mads[i];
if (this.isTraceAlpha) {
if (!thisTypeOnly || this.structureTypes[i] === this.structureTypes[this.iNext]) {
this.madEnd = this.mads[this.iNext];
if (this.madEnd == 0) {
if (Clazz.instanceOf (this, J.renderbio.TraceRenderer)) {
this.madEnd = this.madBeg;
} else {
this.madEnd = this.madBeg;
}}this.madMid = ((this.madBeg + this.madEnd) >> 1);
}} else {
if (!thisTypeOnly || this.structureTypes[i] === this.structureTypes[this.iPrev]) this.madBeg = (((this.mads[this.iPrev] == 0 ? this.madMid : this.mads[this.iPrev]) + this.madMid) >> 1);
if (!thisTypeOnly || this.structureTypes[i] === this.structureTypes[this.iNext]) this.madEnd = (((this.mads[this.iNext] == 0 ? this.madMid : this.mads[this.iNext]) + this.madMid) >> 1);
}this.diameterBeg = Clazz.floatToInt (this.vwr.tm.scaleToScreen (this.controlPointScreens[i].z, this.madBeg));
this.diameterMid = Clazz.floatToInt (this.vwr.tm.scaleToScreen (this.monomers[i].getLeadAtom ().sZ, this.madMid));
this.diameterEnd = Clazz.floatToInt (this.vwr.tm.scaleToScreen (this.controlPointScreens[this.iNext].z, this.madEnd));
this.doCap0 = (i == this.iPrev || thisTypeOnly && this.structureTypes[i] !== this.structureTypes[this.iPrev]);
this.doCap1 = (this.iNext == this.iNext2 || thisTypeOnly && this.structureTypes[i] !== this.structureTypes[this.iNext]);
return ((this.aspectRatio > 0 && (this.exportType == 1 || this.checkDiameter (this.diameterBeg) || this.checkDiameter (this.diameterMid) || this.checkDiameter (this.diameterEnd))));
}, "~N,~B");
Clazz.defineMethod (c$, "checkDiameter", 
 function (d) {
return ( new Boolean ( new Boolean (this.isHighRes & d > 3).valueOf () || d >= 8).valueOf ());
}, "~N");
Clazz.defineMethod (c$, "renderHermiteCylinder", 
function (screens, i) {
this.colix = this.getLeadColix (i);
if (!this.setBioColix (this.colix)) return;
this.setNeighbors (i);
this.g3d.drawHermite4 (this.isNucleic ? 4 : 7, screens[this.iPrev], screens[i], screens[this.iNext], screens[this.iNext2]);
}, "~A,~N");
Clazz.defineMethod (c$, "renderHermiteConic", 
function (i, thisTypeOnly, tension) {
this.setNeighbors (i);
this.colix = this.getLeadColix (i);
if (!this.setBioColix (this.colix)) return;
if (this.setMads (i, thisTypeOnly) || this.isExport) {
try {
if ((this.meshes[i] == null || !this.meshReady[i]) && !this.createMesh (i, this.madBeg, this.madMid, this.madEnd, 1, tension)) return;
this.meshes[i].setColix (this.colix);
this.bsRenderMesh.set (i);
return;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
this.bsRenderMesh.clear (i);
this.meshes[i] = null;
JU.Logger.error ("render mesh error hermiteConic: " + e.toString ());
} else {
throw e;
}
}
}if (this.diameterBeg == 0 && this.diameterEnd == 0 || this.wireframeOnly) this.g3d.drawLineAB (this.controlPointScreens[i], this.controlPointScreens[this.iNext]);
 else {
this.g3d.fillHermite (this.isNucleic ? 4 : 7, this.diameterBeg, this.diameterMid, this.diameterEnd, this.controlPointScreens[this.iPrev], this.controlPointScreens[i], this.controlPointScreens[this.iNext], this.controlPointScreens[this.iNext2]);
}}, "~N,~B,~N");
Clazz.defineMethod (c$, "renderHermiteRibbon", 
function (doFill, i, thisTypeOnly) {
this.setNeighbors (i);
this.colix = this.getLeadColix (i);
if (!this.setBioColix (this.colix)) return;
this.colixBack = this.getLeadColixBack (i);
if (doFill && (this.aspectRatio != 0 || this.isExport)) {
if (this.setMads (i, thisTypeOnly) || this.isExport) {
try {
if ((this.meshes[i] == null || !this.meshReady[i]) && !this.createMesh (i, this.madBeg, this.madMid, this.madEnd, this.aspectRatio, this.isNucleic ? 4 : 7)) return;
this.meshes[i].setColix (this.colix);
this.meshes[i].setColixBack (this.colixBack);
this.bsRenderMesh.set (i);
return;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
this.bsRenderMesh.clear (i);
this.meshes[i] = null;
JU.Logger.error ("render mesh error hermiteRibbon: " + e.toString ());
} else {
throw e;
}
}
}}this.g3d.drawHermite7 (doFill, this.ribbonBorder, (this.reversed.get (i) ? -1 : 1) * (this.isNucleic ? 4 : 7), this.ribbonTopScreens[this.iPrev], this.ribbonTopScreens[i], this.ribbonTopScreens[this.iNext], this.ribbonTopScreens[this.iNext2], this.ribbonBottomScreens[this.iPrev], this.ribbonBottomScreens[i], this.ribbonBottomScreens[this.iNext], this.ribbonBottomScreens[this.iNext2], Clazz.floatToInt (this.aspectRatio), this.colixBack);
}, "~B,~N,~B");
Clazz.defineMethod (c$, "renderHermiteArrowHead", 
function (i) {
this.colix = this.getLeadColix (i);
if (!this.setBioColix (this.colix)) return;
this.colixBack = this.getLeadColixBack (i);
this.setNeighbors (i);
if (this.setMads (i, false) || this.isExport) {
try {
this.doCap0 = true;
this.doCap1 = false;
if ((this.meshes[i] == null || !this.meshReady[i]) && !this.createMesh (i, Clazz.doubleToInt (Math.floor (this.madBeg * 1.2)), Clazz.doubleToInt (Math.floor (this.madBeg * 0.6)), 0, (this.aspectRatio == 1 ? this.aspectRatio : this.aspectRatio / 2), 7)) return;
this.meshes[i].setColix (this.colix);
this.bsRenderMesh.set (i);
return;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
this.bsRenderMesh.clear (i);
this.meshes[i] = null;
JU.Logger.error ("render mesh error hermiteArrowHead: " + e.toString ());
} else {
throw e;
}
}
}this.calc1Screen (this.controlPoints[i], this.wingVectors[i], this.madBeg, .0007, this.screenArrowTop);
this.calc1Screen (this.controlPoints[i], this.wingVectors[i], this.madBeg, -7.0E-4, this.screenArrowBot);
this.calc1Screen (this.controlPoints[i], this.wingVectors[i], this.madBeg, 0.001, this.screenArrowTopPrev);
this.calc1Screen (this.controlPoints[i], this.wingVectors[i], this.madBeg, -0.001, this.screenArrowBotPrev);
this.g3d.drawHermite7 (true, this.ribbonBorder, this.isNucleic ? 4 : 7, this.screenArrowTopPrev, this.screenArrowTop, this.controlPointScreens[this.iNext], this.controlPointScreens[this.iNext2], this.screenArrowBotPrev, this.screenArrowBot, this.controlPointScreens[this.iNext], this.controlPointScreens[this.iNext2], Clazz.floatToInt (this.aspectRatio), this.colixBack);
if (this.ribbonBorder && this.aspectRatio == 0) {
this.g3d.fillCylinderXYZ (this.colix, this.colix, 3, (this.exportType == 1 ? 50 : 3), this.screenArrowTop.x, this.screenArrowTop.y, this.screenArrowTop.z, this.screenArrowBot.x, this.screenArrowBot.y, this.screenArrowBot.z);
}}, "~N");
Clazz.defineMethod (c$, "createMesh", 
 function (i, madBeg, madMid, madEnd, aspectRatio, tension) {
this.setNeighbors (i);
if (this.controlPoints[i].distance (this.controlPoints[this.iNext]) == 0) return false;
var isEccentric = (aspectRatio != 1 && this.wingVectors != null);
var isFlatMesh = (aspectRatio == 0);
var isElliptical = (this.cartoonsFancy || this.hermiteLevel >= 6);
var nHermites = (this.hermiteLevel + 1) * 2 + 1;
var nPer = (isFlatMesh ? 4 : (this.hermiteLevel + 1) * 4 - 2);
var angle = ((isFlatMesh ? 3.141592653589793 / (nPer - 1) : 6.283185307179586 / nPer));
var mesh = this.meshes[i] =  new J.shape.Mesh ().mesh1 ("mesh_" + this.shapeID + "_" + i, 0, i);
var variableRadius = (madBeg != madMid || madMid != madEnd);
if (this.controlHermites == null || this.controlHermites.length < nHermites + 1) {
this.controlHermites =  new Array (nHermites + 1);
}JU.GData.getHermiteList (tension, this.controlPoints[this.iPrev], this.controlPoints[i], this.controlPoints[this.iNext], this.controlPoints[this.iNext2], this.controlPoints[this.iNext3], this.controlHermites, 0, nHermites, true);
if (this.wingHermites == null || this.wingHermites.length < nHermites + 1) {
this.wingHermites =  new Array (nHermites + 1);
}this.wing.setT (this.wingVectors[this.iPrev]);
if (madEnd == 0) this.wing.scale (2.0);
JU.GData.getHermiteList (tension, this.wing, this.wingVectors[i], this.wingVectors[this.iNext], this.wingVectors[this.iNext2], this.wingVectors[this.iNext3], this.wingHermites, 0, nHermites, false);
var radius1 = madBeg / 2000;
var radius2 = madMid / 2000;
var radius3 = madEnd / 2000;
if (variableRadius) {
if (this.radiusHermites == null || this.radiusHermites.length < ((nHermites + 1) >> 1) + 1) {
this.radiusHermites =  new Array (((nHermites + 1) >> 1) + 1);
}this.ptPrev.set (radius1, radius1, 0);
this.pt.set (radius1, radius2, 0);
this.pt1.set (radius2, radius3, 0);
this.ptNext.set (radius3, radius3, 0);
JU.GData.getHermiteList (4, this.ptPrev, this.pt, this.pt1, this.ptNext, this.ptNext, this.radiusHermites, 0, (nHermites + 1) >> 1, true);
}var nPoints = 0;
var iMid = nHermites >> 1;
var kpt1 = Clazz.doubleToInt ((nPer + 2) / 4);
var kpt2 = Clazz.doubleToInt ((3 * nPer + 2) / 4);
var mode = (!isEccentric ? 0 : isFlatMesh ? 1 : isElliptical ? 2 : 3);
var useMat = (mode == 0 || mode == 3);
for (var p = 0; p < nHermites; p++) {
this.norm.sub2 (this.controlHermites[p + 1], this.controlHermites[p]);
var scale = (!variableRadius ? radius1 : p < iMid ? this.radiusHermites[p].x : this.radiusHermites[p - iMid].y);
this.wing.setT (this.wingHermites[p]);
this.wing1.setT (this.wing);
switch (mode) {
case 1:
break;
case 2:
this.wing1.cross (this.norm, this.wing);
this.wing1.normalize ();
this.wing1.scale (this.wing.length () / aspectRatio);
break;
case 3:
this.wing.scale (2 / aspectRatio);
this.wing1.sub (this.wing);
break;
case 0:
this.wing.cross (this.wing, this.norm);
this.wing.normalize ();
break;
}
this.wing.scale (scale);
this.wing1.scale (scale);
if (useMat) {
this.aa.setVA (this.norm, angle);
this.mat.setAA (this.aa);
}this.pt1.setT (this.controlHermites[p]);
var theta = (isFlatMesh ? 0 : angle);
for (var k = 0; k < nPer; k++, theta += angle) {
if (useMat && k > 0) this.mat.rotate (this.wing);
switch (mode) {
case 1:
this.wingT.setT (this.wing1);
this.wingT.scale (Math.cos (theta));
break;
case 2:
this.wingT.setT (this.wing1);
this.wingT.scale (Math.sin (theta));
this.wingT.scaleAdd2 (Math.cos (theta), this.wing, this.wingT);
break;
case 3:
this.wingT.setT (this.wing);
if (k == kpt1 || k == kpt2) this.wing1.scale (-1);
this.wingT.add (this.wing1);
break;
case 0:
this.wingT.setT (this.wing);
break;
}
this.pt.add2 (this.pt1, this.wingT);
mesh.addV (this.pt, true);
}
if (p > 0) {
var nLast = (isFlatMesh ? nPer - 1 : nPer);
for (var k = 0; k < nLast; k++) {
var a = nPoints - nPer + k;
var b = nPoints - nPer + ((k + 1) % nPer);
var c = nPoints + ((k + 1) % nPer);
var d = nPoints + k;
if (k < Clazz.doubleToInt (nLast / 2)) mesh.addQuad (a, b, c, d);
 else mesh.addQuad (b, c, d, a);
}
}nPoints += nPer;
}
if (!isFlatMesh) {
var nPointsPreCap = nPoints;
if (this.doCap0) {
for (var l = 0; l < nPer; l++) mesh.addV (mesh.vs[l], true);

nPoints += nPer;
for (var k = this.hermiteLevel * 2; --k >= 0; ) mesh.addQuad (nPoints - nPer + k + 2, nPoints - nPer + k + 1, nPoints - nPer + (nPer - k) % nPer, nPoints - k - 1);

}if (this.doCap1) {
for (var l = 0; l < nPer; l++) mesh.addV (mesh.vs[nPointsPreCap - nPer + l], true);

nPoints += nPer;
for (var k = this.hermiteLevel * 2; --k >= 0; ) mesh.addQuad (nPoints - k - 1, nPoints - nPer + (nPer - k) % nPer, nPoints - nPer + k + 1, nPoints - nPer + k + 2);

}}this.meshReady[i] = true;
this.adjustCartoonSeamNormals (i, nPer);
mesh.setVisibilityFlags (1);
return true;
}, "~N,~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "adjustCartoonSeamNormals", 
function (i, nPer) {
if (this.bsTemp == null) this.bsTemp = JU.Normix.newVertexBitSet ();
if (i == this.iNext - 1 && this.iNext < this.monomerCount && this.monomers[i].getStrucNo () == this.monomers[this.iNext].getStrucNo () && this.meshReady[i] && this.meshReady[this.iNext]) {
try {
var normals2 = this.meshes[this.iNext].getNormalsTemp ();
var normals = this.meshes[i].getNormalsTemp ();
var normixCount = normals.length;
if (this.doCap0) normixCount -= nPer;
for (var j = 1; j <= nPer; ++j) {
this.norml.add2 (normals[normixCount - j], normals2[nPer - j]);
this.norml.normalize ();
this.meshes[i].normalsTemp[normixCount - j].setT (this.norml);
this.meshes[this.iNext].normalsTemp[nPer - j].setT (this.norml);
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}}, "~N,~N");
Clazz.defineMethod (c$, "renderMeshes", 
 function () {
for (var i = this.bsRenderMesh.nextSetBit (0); i >= 0; i = this.bsRenderMesh.nextSetBit (i + 1)) {
if (this.meshes[i].normalsTemp != null) {
this.meshes[i].setNormixes (this.meshes[i].normalsTemp);
this.meshes[i].normalsTemp = null;
} else if (this.meshes[i].normixes == null) {
this.meshes[i].initialize (1073741958, null, null);
}this.renderMesh (this.meshes[i]);
}
});
Clazz.defineStatics (c$,
"ABSOLUTE_MIN_MESH_SIZE", 3,
"MIN_MESH_RENDER_SIZE", 8,
"MODE_TUBE", 0,
"MODE_FLAT", 1,
"MODE_ELLIPTICAL", 2,
"MODE_NONELLIPTICAL", 3);
});
