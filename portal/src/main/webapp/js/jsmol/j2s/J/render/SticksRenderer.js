Clazz.declarePackage ("J.render");
Clazz.load (["J.render.ShapeRenderer", "J.util.BS", "$.P3", "$.P3i", "$.V3"], "J.render.SticksRenderer", ["java.lang.Float", "J.constant.EnumPalette", "J.modelset.Bond", "J.util.C", "$.JmolEdge"], function () {
c$ = Clazz.decorateAsClass (function () {
this.showMultipleBonds = false;
this.multipleBondSpacing = 0;
this.multipleBondRadiusFactor = 0;
this.modeMultipleBond = 0;
this.isCartesianExport = false;
this.endcaps = 0;
this.ssbondsBackbone = false;
this.hbondsBackbone = false;
this.bondsBackbone = false;
this.hbondsSolid = false;
this.atomA = null;
this.atomB = null;
this.bond = null;
this.xA = 0;
this.yA = 0;
this.zA = 0;
this.xB = 0;
this.yB = 0;
this.zB = 0;
this.dx = 0;
this.dy = 0;
this.mag2d = 0;
this.colixA = 0;
this.colixB = 0;
this.width = 0;
this.lineBond = false;
this.bondOrder = 0;
this.renderWireframe = false;
this.isAntialiased = false;
this.slabbing = false;
this.slabByAtom = false;
this.dashDots = null;
this.x = null;
this.y = null;
this.z = null;
this.p1 = null;
this.p2 = null;
this.s1 = null;
this.s2 = null;
this.bsForPass2 = null;
this.isPass2 = false;
this.atomA0 = null;
this.atomB0 = null;
this.xAxis1 = 0;
this.yAxis1 = 0;
this.xAxis2 = 0;
this.yAxis2 = 0;
this.dxStep = 0;
this.dyStep = 0;
Clazz.instantialize (this, arguments);
}, J.render, "SticksRenderer", J.render.ShapeRenderer);
Clazz.prepareFields (c$, function () {
this.x =  new J.util.V3 ();
this.y =  new J.util.V3 ();
this.z =  new J.util.V3 ();
this.p1 =  new J.util.P3 ();
this.p2 =  new J.util.P3 ();
this.s1 =  new J.util.P3i ();
this.s2 =  new J.util.P3i ();
this.bsForPass2 = J.util.BS.newN (64);
});
Clazz.overrideMethod (c$, "render", 
function () {
this.isPass2 = this.g3d.isPass2 ();
if (!this.isPass2) this.bsForPass2.clearAll ();
this.slabbing = this.viewer.getSlabEnabled ();
this.slabByAtom = this.viewer.getSlabByAtom ();
this.endcaps = 3;
this.dashDots = (this.viewer.getPartialDots () ? J.render.SticksRenderer.sixdots : J.render.SticksRenderer.dashes);
this.multipleBondSpacing = this.viewer.getMultipleBondSpacing ();
this.isCartesianExport = (this.exportType == 1);
if (this.multipleBondSpacing == 0 && this.isCartesianExport) this.multipleBondSpacing = 0.2;
this.multipleBondRadiusFactor = this.viewer.getMultipleBondRadiusFactor ();
this.showMultipleBonds = this.multipleBondSpacing != 0 && this.viewer.getShowMultipleBonds ();
this.modeMultipleBond = this.viewer.getModeMultipleBond ();
this.renderWireframe = this.viewer.getInMotion () && this.viewer.getWireframeRotation ();
this.ssbondsBackbone = this.viewer.getSsbondsBackbone ();
this.hbondsBackbone = this.viewer.getHbondsBackbone ();
this.bondsBackbone =  new Boolean (this.hbondsBackbone | this.ssbondsBackbone).valueOf ();
this.hbondsSolid = this.viewer.getHbondsSolid ();
this.isAntialiased = this.g3d.isAntialiased ();
var bonds = this.modelSet.bonds;
var needTranslucent = false;
if (this.isPass2) for (var i = this.bsForPass2.nextSetBit (0); i >= 0; i = this.bsForPass2.nextSetBit (i + 1)) {
this.bond = bonds[i];
this.renderBond ();
}
 else for (var i = this.modelSet.bondCount; --i >= 0; ) {
this.bond = bonds[i];
if ((this.bond.getShapeVisibilityFlags () & this.myVisibilityFlag) != 0 && this.renderBond ()) {
needTranslucent = true;
this.bsForPass2.set (i);
}}
return needTranslucent;
});
$_M(c$, "renderBond", 
($fz = function () {
this.atomA = this.atomA0 = this.bond.getAtom1 ();
this.atomB = this.atomB0 = this.bond.getAtom2 ();
var order = this.bond.order & -131073;
if (this.bondsBackbone) {
if (this.ssbondsBackbone && (order & 256) != 0) {
this.atomA = this.atomA.getGroup ().getLeadAtomOr (this.atomA);
this.atomB = this.atomB.getGroup ().getLeadAtomOr (this.atomB);
} else if (this.hbondsBackbone && J.modelset.Bond.isOrderH (order)) {
this.atomA = this.atomA.getGroup ().getLeadAtomOr (this.atomA);
this.atomB = this.atomB.getGroup ().getLeadAtomOr (this.atomB);
}}if (!this.isPass2 && (!this.atomA.isInFrame () || !this.atomB.isInFrame () || !this.g3d.isInDisplayRange (this.atomA.screenX, this.atomA.screenY) || !this.g3d.isInDisplayRange (this.atomB.screenX, this.atomB.screenY) || this.modelSet.isAtomHidden (this.atomA.getIndex ()) || this.modelSet.isAtomHidden (this.atomB.getIndex ()))) return false;
if (this.slabbing) {
if (this.g3d.isClippedZ (this.atomA.screenZ) && this.g3d.isClippedZ (this.atomB.screenZ)) return false;
if (this.slabByAtom && (this.g3d.isClippedZ (this.atomA.screenZ) || this.g3d.isClippedZ (this.atomB.screenZ))) return false;
}this.zA = this.atomA.screenZ;
this.zB = this.atomB.screenZ;
if (this.zA == 1 || this.zB == 1) return false;
this.colixA = this.atomA0.getColix ();
this.colixB = this.atomB0.getColix ();
if (((this.colix = this.bond.colix) & -30721) == 2) {
this.colix = (this.colix & 30720);
this.colixA = J.util.C.getColixInherited ((this.colix | this.viewer.getColixAtomPalette (this.atomA0, J.constant.EnumPalette.CPK.id)), this.colixA);
this.colixB = J.util.C.getColixInherited ((this.colix | this.viewer.getColixAtomPalette (this.atomB0, J.constant.EnumPalette.CPK.id)), this.colixB);
} else {
this.colixA = J.util.C.getColixInherited (this.colix, this.colixA);
this.colixB = J.util.C.getColixInherited (this.colix, this.colixB);
}var needTranslucent = false;
if (!this.isExport && !this.isPass2) {
var doA = !J.util.C.isColixTranslucent (this.colixA);
var doB = !J.util.C.isColixTranslucent (this.colixB);
if (!doA || !doB) {
if (!doA && !doB && !needTranslucent) {
this.g3d.setColix (!doA ? this.colixA : this.colixB);
return true;
}needTranslucent = true;
}}this.bondOrder = order & -131073;
if ((this.bondOrder & 224) == 0) {
if ((this.bondOrder & 256) != 0) this.bondOrder &= -257;
if ((this.bondOrder & 1023) != 0) {
if (!this.showMultipleBonds || this.modeMultipleBond == 0 || (this.modeMultipleBond == 2 && this.mad > 500)) {
this.bondOrder = 1;
}}}var mask = 0;
switch (this.bondOrder) {
case 1:
case 2:
case 3:
case 4:
break;
case 17:
case 513:
this.bondOrder = 1;
mask = (order == 513 ? 0 : 1);
break;
case 515:
case 514:
this.bondOrder = 2;
mask = (order == 515 ? this.getAromaticDottedBondMask () : 0);
break;
default:
if ((this.bondOrder & 224) != 0) {
this.bondOrder = J.util.JmolEdge.getPartialBondOrder (order);
mask = J.util.JmolEdge.getPartialBondDotted (order);
} else if (J.modelset.Bond.isOrderH (this.bondOrder)) {
this.bondOrder = 1;
if (!this.hbondsSolid) mask = -1;
} else if (this.bondOrder == 32768) {
this.bondOrder = 1;
}}
this.xA = this.atomA.screenX;
this.yA = this.atomA.screenY;
this.xB = this.atomB.screenX;
this.yB = this.atomB.screenY;
this.mad = this.bond.mad;
if (this.multipleBondRadiusFactor > 0 && this.bondOrder > 1) this.mad *= this.multipleBondRadiusFactor;
this.dx = this.xB - this.xA;
this.dy = this.yB - this.yA;
this.width = this.viewer.scaleToScreen (Clazz.doubleToInt ((this.zA + this.zB) / 2), this.mad);
if (this.renderWireframe && this.width > 0) this.width = 1;
if (!this.isCartesianExport) {
this.lineBond = (this.width <= 1);
if (this.lineBond && (this.isAntialiased)) {
this.width = 3;
this.lineBond = false;
}}switch (mask) {
case -1:
this.drawDashed (this.xA, this.yA, this.zA, this.xB, this.yB, this.zB, J.render.SticksRenderer.hDashes);
break;
default:
this.drawBond (mask);
break;
}
return needTranslucent;
}, $fz.isPrivate = true, $fz));
$_M(c$, "drawBond", 
($fz = function (dottedMask) {
if (this.isCartesianExport && this.bondOrder == 1) {
this.g3d.drawBond (this.atomA, this.atomB, this.colixA, this.colixB, this.endcaps, this.mad, -1);
return;
}var isEndOn = (this.dx == 0 && this.dy == 0);
if (isEndOn && this.lineBond) return;
var doFixedSpacing = (this.bondOrder > 1 && this.multipleBondSpacing > 0);
var isPiBonded = doFixedSpacing && (this.viewer.getHybridizationAndAxes (this.atomA.index, this.z, this.x, "pz") != null || this.viewer.getHybridizationAndAxes (this.atomB.index, this.z, this.x, "pz") != null) && !Float.isNaN (this.x.x);
if (isEndOn && !doFixedSpacing) {
var space = Clazz.doubleToInt (this.width / 8) + 3;
var step = this.width + space;
var y = this.yA - Clazz.doubleToInt ((this.bondOrder - 1) * step / 2);
do {
this.fillCylinder (this.colixA, this.colixA, this.endcaps, this.width, this.xA, y, this.zA, this.xA, y, this.zA);
y += step;
} while (--this.bondOrder > 0);
return;
}var isDashed = (dottedMask & 1) != 0;
if (this.bondOrder == 1) {
if (isDashed) this.drawDashed (this.xA, this.yA, this.zA, this.xB, this.yB, this.zB, this.dashDots);
 else this.fillCylinder (this.colixA, this.colixB, this.endcaps, this.width, this.xA, this.yA, this.zA, this.xB, this.yB, this.zB);
return;
}if (doFixedSpacing) {
if (!isPiBonded) this.z.set (3.141592653589793, 2.718281828459045, (8.539734222673566));
this.x.sub2 (this.atomB, this.atomA);
this.y.cross (this.x, this.z);
this.y.normalize ();
if (Float.isNaN (this.y.x)) {
this.z.set (3.141592653589793, 2.718281828459045, (8.539734222673566));
this.y.cross (this.x, this.z);
this.y.cross (this.y, this.x);
this.y.normalize ();
}this.y.scale (this.multipleBondSpacing);
this.x.setT (this.y);
this.x.scale ((this.bondOrder - 1) / 2);
this.p1.sub2 (this.atomA, this.x);
this.p2.sub2 (this.atomB, this.x);
while (true) {
if (this.isCartesianExport && !isDashed) {
this.g3d.drawBond (this.p1, this.p2, this.colixA, this.colixB, this.endcaps, this.mad, -2);
} else {
this.viewer.transformPtScr (this.p1, this.s1);
this.viewer.transformPtScr (this.p2, this.s2);
if (isDashed) this.drawDashed (this.s1.x, this.s1.y, this.s1.z, this.s2.x, this.s2.y, this.s2.z, this.dashDots);
 else this.fillCylinder (this.colixA, this.colixB, this.endcaps, this.width, this.s1.x, this.s1.y, this.s1.z, this.s2.x, this.s2.y, this.s2.z);
dottedMask >>= 1;
isDashed = (dottedMask & 1) != 0;
}if (--this.bondOrder <= 0) break;
this.p1.add (this.y);
this.p2.add (this.y);
this.stepAxisCoordinates ();
}
return;
}var dxB = this.dx * this.dx;
var dyB = this.dy * this.dy;
this.mag2d = Math.round (Math.sqrt (dxB + dyB));
this.resetAxisCoordinates ();
while (true) {
if ((dottedMask & 1) != 0) this.drawDashed (this.xAxis1, this.yAxis1, this.zA, this.xAxis2, this.yAxis2, this.zB, this.dashDots);
 else this.fillCylinder (this.colixA, this.colixB, this.endcaps, this.width, this.xAxis1, this.yAxis1, this.zA, this.xAxis2, this.yAxis2, this.zB);
dottedMask >>= 1;
if (--this.bondOrder <= 0) break;
this.stepAxisCoordinates ();
}
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "resetAxisCoordinates", 
($fz = function () {
var space = this.mag2d >> 3;
if (this.multipleBondSpacing != -1 && this.multipleBondSpacing < 0) space *= -this.multipleBondSpacing;
var step = this.width + space;
this.dxStep = Clazz.doubleToInt (step * this.dy / this.mag2d);
this.dyStep = Clazz.doubleToInt (step * -this.dx / this.mag2d);
this.xAxis1 = this.xA;
this.yAxis1 = this.yA;
this.xAxis2 = this.xB;
this.yAxis2 = this.yB;
var f = (this.bondOrder - 1);
this.xAxis1 -= Clazz.doubleToInt (this.dxStep * f / 2);
this.yAxis1 -= Clazz.doubleToInt (this.dyStep * f / 2);
this.xAxis2 -= Clazz.doubleToInt (this.dxStep * f / 2);
this.yAxis2 -= Clazz.doubleToInt (this.dyStep * f / 2);
}, $fz.isPrivate = true, $fz));
$_M(c$, "stepAxisCoordinates", 
($fz = function () {
this.xAxis1 += this.dxStep;
this.yAxis1 += this.dyStep;
this.xAxis2 += this.dxStep;
this.yAxis2 += this.dyStep;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getAromaticDottedBondMask", 
($fz = function () {
var atomC = this.atomB.findAromaticNeighbor (this.atomA.getIndex ());
if (atomC == null) return 1;
var dxAC = atomC.screenX - this.xA;
var dyAC = atomC.screenY - this.yA;
return ((this.dx * dyAC - this.dy * dxAC) < 0 ? 2 : 1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "drawDashed", 
($fz = function (xA, yA, zA, xB, yB, zB, array) {
var dx = xB - xA;
var dy = yB - yA;
var dz = zB - zA;
var isDots = (array === J.render.SticksRenderer.sixdots);
if (isDots) {
if (this.mad * 4 > 1500) array = J.render.SticksRenderer.twodots;
 else if (this.mad * 6 > 1500) array = J.render.SticksRenderer.fourdots;
}var f = array[0];
var ptS = array[1];
var ptE = array[2];
var colixS = this.colixA;
var colixE = (ptE == 0 ? this.colixB : this.colixA);
for (var pt = 3; pt < array.length; pt++) {
var i = array[pt];
var xS = Clazz.doubleToInt (Math.floor (xA + dx * i / f));
var yS = Clazz.doubleToInt (Math.floor (yA + dy * i / f));
var zS = Clazz.doubleToInt (Math.floor (zA + dz * i / f));
if (isDots) {
this.s1.set (xS, yS, zS);
if (pt == ptS) this.g3d.setColix (this.colixA);
 else if (pt == ptE) this.g3d.setColix (this.colixB);
this.g3d.fillSphereI (this.width, this.s1);
continue;
}if (pt == ptS) colixS = this.colixB;
i = array[++pt];
if (pt == ptE) colixE = this.colixB;
var xE = Clazz.doubleToInt (Math.floor (xA + dx * i / f));
var yE = Clazz.doubleToInt (Math.floor (yA + dy * i / f));
var zE = Clazz.doubleToInt (Math.floor (zA + dz * i / f));
this.fillCylinder (colixS, colixE, 2, this.width, xS, yS, zS, xE, yE, zE);
}
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N,~N,~A");
$_M(c$, "fillCylinder", 
($fz = function (colixA, colixB, endcaps, diameter, xA, yA, zA, xB, yB, zB) {
if (this.lineBond) this.g3d.drawLine (colixA, colixB, xA, yA, zA, xB, yB, zB);
 else this.g3d.fillCylinderXYZ (colixA, colixB, endcaps, (!this.isExport || this.mad == 1 ? diameter : this.mad), xA, yA, zA, xB, yB, zB);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.defineStatics (c$,
"dashes", [12, 0, 0, 2, 5, 7, 10],
"hDashes", [10, 7, 6, 1, 3, 4, 6, 7, 9],
"sixdots", [12, 3, 6, 1, 3, 5, 7, 9, 11],
"fourdots", [13, 3, 5, 2, 5, 8, 11],
"twodots", [12, 3, 4, 3, 9]);
});
