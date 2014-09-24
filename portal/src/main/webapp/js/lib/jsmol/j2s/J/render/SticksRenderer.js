Clazz.declarePackage ("J.render");
Clazz.load (["J.render.FontLineShapeRenderer", "JU.BS", "$.P3", "$.V3"], "J.render.SticksRenderer", ["java.lang.Float", "J.c.PAL", "JM.Bond", "JU.C", "$.Edge"], function () {
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
this.a = null;
this.b = null;
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
this.bondOrder = 0;
this.wireframeOnly = false;
this.isAntialiased = false;
this.slabbing = false;
this.slabByAtom = false;
this.x = null;
this.y = null;
this.z = null;
this.p1 = null;
this.p2 = null;
this.bsForPass2 = null;
this.isPass2 = false;
this.xAxis1 = 0;
this.yAxis1 = 0;
this.xAxis2 = 0;
this.yAxis2 = 0;
this.dxStep = 0;
this.dyStep = 0;
Clazz.instantialize (this, arguments);
}, J.render, "SticksRenderer", J.render.FontLineShapeRenderer);
Clazz.prepareFields (c$, function () {
this.x =  new JU.V3 ();
this.y =  new JU.V3 ();
this.z =  new JU.V3 ();
this.p1 =  new JU.P3 ();
this.p2 =  new JU.P3 ();
this.bsForPass2 = JU.BS.newN (64);
});
Clazz.overrideMethod (c$, "render", 
function () {
var bonds = this.ms.bo;
if (bonds == null) return false;
this.isPass2 = this.g3d.isPass2 ();
if (!this.isPass2) this.bsForPass2.clearAll ();
this.slabbing = this.tm.slabEnabled;
this.slabByAtom = this.vwr.getBoolean (603979939);
this.endcaps = 3;
this.dashDots = (this.vwr.getBoolean (603979889) ? J.render.FontLineShapeRenderer.sixdots : J.render.FontLineShapeRenderer.dashes);
this.isCartesianExport = (this.exportType == 1);
this.getMultipleBondSettings (false);
this.wireframeOnly = !this.vwr.checkMotionRendering (1678770178);
this.ssbondsBackbone = this.vwr.getBoolean (603979952);
this.hbondsBackbone = this.vwr.getBoolean (603979852);
this.bondsBackbone =  new Boolean (this.hbondsBackbone | this.ssbondsBackbone).valueOf ();
this.hbondsSolid = this.vwr.getBoolean (603979854);
this.isAntialiased = this.g3d.isAntialiased ();
var needTranslucent = false;
if (!this.isExport && this.isPass2) for (var i = this.bsForPass2.nextSetBit (0); i >= 0; i = this.bsForPass2.nextSetBit (i + 1)) {
this.bond = bonds[i];
this.renderBond ();
}
 else for (var i = this.ms.bondCount; --i >= 0; ) {
this.bond = bonds[i];
if ((this.bond.getShapeVisibilityFlags () & this.myVisibilityFlag) != 0 && this.renderBond ()) {
needTranslucent = true;
this.bsForPass2.set (i);
}}
return needTranslucent;
});
Clazz.defineMethod (c$, "getMultipleBondSettings", 
 function (isPymol) {
this.multipleBondSpacing = (isPymol ? 0.15 : this.vwr.getFloat (570425370));
this.multipleBondRadiusFactor = (isPymol ? 0.4 : this.vwr.getFloat (570425369));
if (this.multipleBondSpacing == 0 && this.isCartesianExport) this.multipleBondSpacing = 0.2;
this.modeMultipleBond = this.vwr.g.modeMultipleBond;
this.showMultipleBonds = (this.multipleBondSpacing != 0 && this.modeMultipleBond != 0 && this.vwr.getBoolean (603979928));
}, "~B");
Clazz.defineMethod (c$, "renderBond", 
 function () {
var atomA0;
var atomB0;
this.a = atomA0 = this.bond.getAtom1 ();
this.b = atomB0 = this.bond.getAtom2 ();
var order = this.bond.order & -131073;
if (this.bondsBackbone) {
if (this.ssbondsBackbone && (order & 256) != 0) {
this.a = this.a.getGroup ().getLeadAtomOr (this.a);
this.b = this.b.getGroup ().getLeadAtomOr (this.b);
} else if (this.hbondsBackbone && JM.Bond.isOrderH (order)) {
this.a = this.a.getGroup ().getLeadAtomOr (this.a);
this.b = this.b.getGroup ().getLeadAtomOr (this.b);
}}if (!this.isPass2 && (!this.a.isVisible (9) || !this.b.isVisible (9) || !this.g3d.isInDisplayRange (this.a.sX, this.a.sY) || !this.g3d.isInDisplayRange (this.b.sX, this.b.sY))) return false;
if (this.slabbing) {
if (this.g3d.isClippedZ (this.a.sZ) && this.g3d.isClippedZ (this.b.sZ)) return false;
if (this.slabByAtom && (this.g3d.isClippedZ (this.a.sZ) || this.g3d.isClippedZ (this.b.sZ))) return false;
}this.zA = this.a.sZ;
this.zB = this.b.sZ;
if (this.zA == 1 || this.zB == 1) return false;
this.colixA = atomA0.getColix ();
this.colixB = atomB0.getColix ();
if (((this.colix = this.bond.colix) & -30721) == 2) {
this.colix = (this.colix & 30720);
this.colixA = JU.C.getColixInherited ((this.colix | this.vwr.getColixAtomPalette (atomA0, J.c.PAL.CPK.id)), this.colixA);
this.colixB = JU.C.getColixInherited ((this.colix | this.vwr.getColixAtomPalette (atomB0, J.c.PAL.CPK.id)), this.colixB);
} else {
this.colixA = JU.C.getColixInherited (this.colix, this.colixA);
this.colixB = JU.C.getColixInherited (this.colix, this.colixB);
}var needTranslucent = false;
if (!this.isExport && !this.isPass2) {
var doA = !JU.C.isColixTranslucent (this.colixA);
var doB = !JU.C.isColixTranslucent (this.colixB);
if (!doA || !doB) {
if (!doA && !doB && !needTranslucent) {
this.g3d.setC (!doA ? this.colixA : this.colixB);
return true;
}needTranslucent = true;
}}this.bondOrder = order & -131073;
if ((this.bondOrder & 224) == 0) {
if ((this.bondOrder & 256) != 0) this.bondOrder &= -257;
if ((this.bondOrder & 1023) != 0) {
if (!this.showMultipleBonds || (this.modeMultipleBond == 2 && this.mad > 500) || (this.bondOrder & 98304) == 65536) {
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
this.bondOrder = JU.Edge.getPartialBondOrder (order);
mask = JU.Edge.getPartialBondDotted (order);
} else if (JM.Bond.isOrderH (this.bondOrder)) {
this.bondOrder = 1;
if (!this.hbondsSolid) mask = -1;
} else if (this.bondOrder == 32768) {
this.bondOrder = 1;
} else if ((this.bondOrder & 98304) == 98304) {
this.getMultipleBondSettings (true);
this.bondOrder &= 3;
mask = -2;
}}
this.xA = this.a.sX;
this.yA = this.a.sY;
this.xB = this.b.sX;
this.yB = this.b.sY;
this.mad = this.bond.mad;
if (this.multipleBondRadiusFactor > 0 && this.bondOrder > 1) this.mad *= this.multipleBondRadiusFactor;
this.dx = this.xB - this.xA;
this.dy = this.yB - this.yA;
this.width = Clazz.floatToInt (this.vwr.tm.scaleToScreen (Clazz.doubleToInt ((this.zA + this.zB) / 2), this.mad));
if (this.wireframeOnly && this.width > 0) this.width = 1;
if (!this.isCartesianExport) {
this.asLineOnly = (this.width <= 1);
if (this.asLineOnly && (this.isAntialiased)) {
this.width = 3;
this.asLineOnly = false;
}}switch (mask) {
case -2:
this.drawBond (0);
this.getMultipleBondSettings (false);
break;
case -1:
this.drawDashed (this.xA, this.yA, this.zA, this.xB, this.yB, this.zB, J.render.FontLineShapeRenderer.hDashes);
break;
default:
this.drawBond (mask);
break;
}
return needTranslucent;
});
Clazz.defineMethod (c$, "drawBond", 
 function (dottedMask) {
if (this.isCartesianExport && this.bondOrder == 1) {
this.g3d.drawBond (this.a, this.b, this.colixA, this.colixB, this.endcaps, this.mad, -1);
return;
}var isEndOn = (this.dx == 0 && this.dy == 0);
if (isEndOn && this.asLineOnly) return;
var doFixedSpacing = (this.bondOrder > 1 && this.multipleBondSpacing > 0);
var isPiBonded = doFixedSpacing && (this.vwr.getHybridizationAndAxes (this.a.i, this.z, this.x, "pz") != null || this.vwr.getHybridizationAndAxes (this.b.i, this.z, this.x, "pz") != null) && !Float.isNaN (this.x.x);
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
this.x.sub2 (this.b, this.a);
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
this.p1.sub2 (this.a, this.x);
this.p2.sub2 (this.b, this.x);
while (true) {
if (this.isCartesianExport && !isDashed) {
this.g3d.drawBond (this.p1, this.p2, this.colixA, this.colixB, this.endcaps, this.mad, -2);
} else {
this.tm.transformPtScr (this.p1, this.s1);
this.tm.transformPtScr (this.p2, this.s2);
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
}, "~N");
Clazz.defineMethod (c$, "resetAxisCoordinates", 
 function () {
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
});
Clazz.defineMethod (c$, "stepAxisCoordinates", 
 function () {
this.xAxis1 += this.dxStep;
this.yAxis1 += this.dyStep;
this.xAxis2 += this.dxStep;
this.yAxis2 += this.dyStep;
});
Clazz.defineMethod (c$, "getAromaticDottedBondMask", 
 function () {
var atomC = this.b.findAromaticNeighbor (this.a.i);
if (atomC == null) return 1;
var dxAC = atomC.sX - this.xA;
var dyAC = atomC.sY - this.yA;
return ((this.dx * dyAC - this.dy * dxAC) < 0 ? 2 : 1);
});
});
