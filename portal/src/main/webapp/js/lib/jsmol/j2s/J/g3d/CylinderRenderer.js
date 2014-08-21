Clazz.declarePackage ("J.g3d");
Clazz.load (null, "J.g3d.CylinderRenderer", ["JU.AU"], function () {
c$ = Clazz.decorateAsClass (function () {
this.g3d = null;
this.line3d = null;
this.shader = null;
this.colixA = 0;
this.colixB = 0;
this.shadesA = null;
this.shadesB = null;
this.xA = 0;
this.yA = 0;
this.zA = 0;
this.dxB = 0;
this.dyB = 0;
this.dzB = 0;
this.xAf = 0;
this.yAf = 0;
this.zAf = 0;
this.dxBf = 0;
this.dyBf = 0;
this.dzBf = 0;
this.tEvenDiameter = false;
this.diameter = 0;
this.endcaps = 0;
this.tEndcapOpen = false;
this.xEndcap = 0;
this.yEndcap = 0;
this.zEndcap = 0;
this.argbEndcap = 0;
this.colixEndcap = 0;
this.endcapShadeIndex = 0;
this.radius = 0;
this.radius2 = 0;
this.cosTheta = 0;
this.cosPhi = 0;
this.sinPhi = 0;
this.clipped = false;
this.drawBackside = false;
this.xTip = 0;
this.yTip = 0;
this.zTip = 0;
this.rasterCount = 0;
this.tRaster = null;
this.txRaster = null;
this.tyRaster = null;
this.tzRaster = null;
this.xRaster = null;
this.yRaster = null;
this.zRaster = null;
this.fp8ShadeIndexUp = null;
this.yMin = 0;
this.yMax = 0;
this.xMin = 0;
this.xMax = 0;
this.zXMin = 0;
this.zXMax = 0;
Clazz.instantialize (this, arguments);
}, J.g3d, "CylinderRenderer");
Clazz.prepareFields (c$, function () {
this.tRaster =  Clazz.newFloatArray (32, 0);
this.txRaster =  Clazz.newFloatArray (32, 0);
this.tyRaster =  Clazz.newFloatArray (32, 0);
this.tzRaster =  Clazz.newFloatArray (32, 0);
this.xRaster =  Clazz.newIntArray (32, 0);
this.yRaster =  Clazz.newIntArray (32, 0);
this.zRaster =  Clazz.newIntArray (32, 0);
this.fp8ShadeIndexUp =  Clazz.newIntArray (32, 0);
});
Clazz.makeConstructor (c$, 
function (g3d) {
this.g3d = g3d;
this.line3d = g3d.line3d;
this.shader = g3d.shader;
}, "J.g3d.Graphics3D");
Clazz.defineMethod (c$, "render", 
function (colixA, colixB, isScreenedA, isScreenedB, endcaps, diameter, xa, ya, za, xb, yb, zb) {
if (diameter > this.g3d.getRenderHeight () * 3) return;
var r = Clazz.doubleToInt (diameter / 2) + 1;
var codeMinA = this.g3d.clipCode3 (xa - r, ya - r, za - r);
var codeMaxA = this.g3d.clipCode3 (xa + r, ya + r, za + r);
var codeMinB = this.g3d.clipCode3 (xb - r, yb - r, zb - r);
var codeMaxB = this.g3d.clipCode3 (xb + r, yb + r, zb + r);
this.clipped = ((codeMinA | codeMaxA | codeMinB | codeMaxB) != 0);
if ((codeMinA & codeMaxB & codeMaxA & codeMinB) != 0) return;
this.dxB = xb - xa;
this.dyB = yb - ya;
this.dzB = zb - za;
if (diameter <= 1) {
this.line3d.plotLineDelta (this.g3d.getColorArgbOrGray (colixA), isScreenedA, this.g3d.getColorArgbOrGray (colixB), isScreenedB, xa, ya, za, this.dxB, this.dyB, this.dzB, this.clipped);
return;
}this.drawBackside = (this.clipped || endcaps == 2 || endcaps == 0);
this.diameter = diameter;
this.xA = xa;
this.yA = ya;
this.zA = za;
this.endcaps = endcaps;
this.shadesA = this.g3d.getShades (this.colixA = colixA);
this.shadesB = this.g3d.getShades (this.colixB = colixB);
this.calcArgbEndcap (true, false);
this.generateBaseEllipse ();
if (endcaps == 2 || endcaps == 4) this.renderFlatEndcap (true);
this.g3d.setZMargin (5);
for (var i = this.rasterCount; --i >= 0; ) {
var fpz = this.fp8ShadeIndexUp[i] >> (8);
var fpzBack = fpz >> 1;
var x = this.xRaster[i];
var y = this.yRaster[i];
var z = this.zRaster[i];
if (this.tEndcapOpen && this.argbEndcap != 0) {
if (this.clipped) {
this.g3d.plotPixelClippedArgb (this.argbEndcap, this.xEndcap + x, this.yEndcap + y, this.zEndcap - z - 1);
this.g3d.plotPixelClippedArgb (this.argbEndcap, this.xEndcap - x, this.yEndcap - y, this.zEndcap + z - 1);
} else {
this.g3d.plotPixelUnclippedArgb (this.argbEndcap, this.xEndcap + x, this.yEndcap + y, this.zEndcap - z - 1);
this.g3d.plotPixelUnclippedArgb (this.argbEndcap, this.xEndcap - x, this.yEndcap - y, this.zEndcap + z - 1);
}}this.line3d.plotLineDeltaA (this.shadesA, isScreenedA, this.shadesB, isScreenedB, fpz, this.xA + x, this.yA + y, this.zA - z, this.dxB, this.dyB, this.dzB, this.clipped);
if (this.drawBackside) {
this.line3d.plotLineDelta (this.shadesA[fpzBack], isScreenedA, this.shadesB[fpzBack], isScreenedB, this.xA - x, this.yA - y, this.zA + z, this.dxB, this.dyB, this.dzB, this.clipped);
}}
this.g3d.setZMargin (0);
if (endcaps == 3) this.renderSphericalEndcaps ();
}, "~N,~N,~B,~B,~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "renderBits", 
function (colixA, colixB, isScreenedA, isScreenedB, endcaps, diameter, xa, ya, za, xb, yb, zb) {
if (diameter > this.g3d.getRenderHeight () * 3) return;
var r = Clazz.doubleToInt (diameter / 2) + 1;
var ixA = Math.round (xa);
var iyA = Math.round (ya);
var izA = Math.round (za);
var ixB = Math.round (xb);
var iyB = Math.round (yb);
var izB = Math.round (zb);
var codeMinA = this.g3d.clipCode3 (ixA - r, iyA - r, izA - r);
var codeMaxA = this.g3d.clipCode3 (ixA + r, iyA + r, izA + r);
var codeMinB = this.g3d.clipCode3 (ixB - r, iyB - r, izB - r);
var codeMaxB = this.g3d.clipCode3 (ixB + r, iyB + r, izB + r);
this.clipped = ((codeMinA | codeMaxA | codeMinB | codeMaxB) != 0);
if ((codeMinA & codeMaxB & codeMaxA & codeMinB) != 0) return;
this.dxBf = xb - xa;
this.dyBf = yb - ya;
this.dzBf = zb - za;
if (diameter == 0 || diameter == 1) {
this.line3d.plotLineDelta (this.g3d.getColorArgbOrGray (colixA), isScreenedA, this.g3d.getColorArgbOrGray (colixB), isScreenedB, Clazz.floatToInt (xa), Clazz.floatToInt (ya), Clazz.floatToInt (za), Clazz.floatToInt (this.dxBf), Clazz.floatToInt (this.dyBf), Clazz.floatToInt (this.dzBf), this.clipped);
return;
}if (diameter > 0) {
this.diameter = diameter;
this.xAf = xa;
this.yAf = ya;
this.zAf = za;
}this.drawBackside = (!isScreenedA && !isScreenedB && (this.clipped || endcaps == 2 || endcaps == 0));
this.xA = Clazz.floatToInt (this.xAf);
this.yA = Clazz.floatToInt (this.yAf);
this.zA = Clazz.floatToInt (this.zAf);
this.dxB = Clazz.floatToInt (this.dxBf);
this.dyB = Clazz.floatToInt (this.dyBf);
this.dzB = Clazz.floatToInt (this.dzBf);
this.shadesA = this.g3d.getShades (this.colixA = colixA);
this.shadesB = this.g3d.getShades (this.colixB = colixB);
this.endcaps = endcaps;
this.calcArgbEndcap (true, true);
if (diameter > 0) this.generateBaseEllipsePrecisely (false);
if (endcaps == 2) this.renderFlatEndcapPrecisely (true);
this.line3d.setLineBits (this.dxBf, this.dyBf);
this.g3d.setZMargin (5);
for (var i = this.rasterCount; --i >= 0; ) {
var fpz = this.fp8ShadeIndexUp[i] >> (8);
var fpzBack = fpz >> 1;
var x = this.xRaster[i];
var y = this.yRaster[i];
var z = this.zRaster[i];
if (this.tEndcapOpen && this.argbEndcap != 0) {
if (this.clipped) {
this.g3d.plotPixelClippedArgb (this.argbEndcap, this.xEndcap + x, this.yEndcap + y, this.zEndcap - z - 1);
this.g3d.plotPixelClippedArgb (this.argbEndcap, this.xEndcap - x, this.yEndcap - y, this.zEndcap + z - 1);
} else {
this.g3d.plotPixelUnclippedArgb (this.argbEndcap, this.xEndcap + x, this.yEndcap + y, this.zEndcap - z - 1);
this.g3d.plotPixelUnclippedArgb (this.argbEndcap, this.xEndcap - x, this.yEndcap - y, this.zEndcap + z - 1);
}}this.line3d.plotLineDeltaBits (this.shadesA, isScreenedA, this.shadesB, isScreenedB, fpz, this.xA + x, this.yA + y, this.zA - z, this.dxB, this.dyB, this.dzB, this.clipped);
if (this.drawBackside) {
this.line3d.plotLineDelta (this.shadesA[fpzBack], isScreenedA, this.shadesB[fpzBack], isScreenedB, this.xA - x, this.yA - y, this.zA + z, this.dxB, this.dyB, this.dzB, this.clipped);
}}
this.g3d.setZMargin (0);
if (endcaps == 3) this.renderSphericalEndcaps ();
this.xAf += this.dxBf;
this.yAf += this.dyBf;
this.zAf += this.dzBf;
}, "~N,~N,~B,~B,~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.defineMethod (c$, "renderCone", 
function (colix, isScreenedA, endcap, diameter, xa, ya, za, xtip, ytip, ztip, doFill, isBarb) {
if (diameter > this.g3d.getRenderHeight () * 3) return;
this.dxBf = (xtip) - (this.xAf = xa);
this.dyBf = (ytip) - (this.yAf = ya);
this.dzBf = (ztip) - (this.zAf = za);
this.xA = Clazz.doubleToInt (Math.floor (this.xAf));
this.yA = Clazz.doubleToInt (Math.floor (this.yAf));
this.zA = Clazz.doubleToInt (Math.floor (this.zAf));
this.dxB = Clazz.doubleToInt (Math.floor (this.dxBf));
this.dyB = Clazz.doubleToInt (Math.floor (this.dyBf));
this.dzB = Clazz.doubleToInt (Math.floor (this.dzBf));
this.xTip = xtip;
this.yTip = ytip;
this.zTip = ztip;
this.colixA = colix;
this.shadesA = this.g3d.getShades (colix);
var shadeIndexTip = this.shader.getShadeIndex (this.dxB, this.dyB, -this.dzB);
this.g3d.plotPixelClippedScreened (this.shadesA[shadeIndexTip], isScreenedA, Clazz.floatToInt (xtip), Clazz.floatToInt (ytip), Clazz.floatToInt (ztip));
this.diameter = diameter;
if (diameter <= 1) {
if (diameter == 1) this.line3d.plotLineDelta (this.colixA, isScreenedA, this.colixA, isScreenedA, this.xA, this.yA, this.zA, this.dxB, this.dyB, this.dzB, this.clipped);
return;
}this.endcaps = endcap;
this.calcArgbEndcap (false, true);
this.generateBaseEllipsePrecisely (isBarb);
if (!isBarb && this.endcaps == 2) this.renderFlatEndcapPrecisely (false);
this.g3d.setZMargin (5);
for (var i = this.rasterCount; --i >= 0; ) {
var x = this.txRaster[i];
var y = this.tyRaster[i];
var z = this.tzRaster[i];
var xUp = this.xAf + x;
var yUp = this.yAf + y;
var zUp = this.zAf - z;
var xDn = this.xAf - x;
var yDn = this.yAf - y;
var zDn = this.zAf + z;
var argb = this.shadesA[0];
if (this.tEndcapOpen && this.argbEndcap != 0) {
this.g3d.plotPixelClippedScreened (this.argbEndcap, isScreenedA, Clazz.floatToInt (xUp), Clazz.floatToInt (yUp), Clazz.floatToInt (zUp));
this.g3d.plotPixelClippedScreened (this.argbEndcap, isScreenedA, Clazz.floatToInt (xDn), Clazz.floatToInt (yDn), Clazz.floatToInt (zDn));
}var fpz = this.fp8ShadeIndexUp[i] >> (8);
if (argb != 0) {
this.line3d.plotLineDeltaA (this.shadesA, isScreenedA, this.shadesA, isScreenedA, fpz, Clazz.floatToInt (xUp), Clazz.floatToInt (yUp), Clazz.floatToInt (zUp), Clazz.doubleToInt (Math.ceil (this.xTip - xUp)), Clazz.doubleToInt (Math.ceil (this.yTip - yUp)), Clazz.doubleToInt (Math.ceil (this.zTip - zUp)), true);
if (doFill) {
this.line3d.plotLineDeltaA (this.shadesA, isScreenedA, this.shadesA, isScreenedA, fpz, Clazz.floatToInt (xUp), Clazz.floatToInt (yUp) + 1, Clazz.floatToInt (zUp), Clazz.doubleToInt (Math.ceil (this.xTip - xUp)), Clazz.doubleToInt (Math.ceil (this.yTip - yUp)) + 1, Clazz.doubleToInt (Math.ceil (this.zTip - zUp)), true);
this.line3d.plotLineDeltaA (this.shadesA, isScreenedA, this.shadesA, isScreenedA, fpz, Clazz.floatToInt (xUp) + 1, Clazz.floatToInt (yUp), Clazz.floatToInt (zUp), Clazz.doubleToInt (Math.ceil (this.xTip - xUp)) + 1, Clazz.doubleToInt (Math.ceil (this.yTip - yUp)), Clazz.doubleToInt (Math.ceil (this.zTip - zUp)), true);
}if (!isBarb && !(this.endcaps != 2 && this.dzB > 0)) {
this.line3d.plotLineDelta (argb, isScreenedA, argb, isScreenedA, Clazz.floatToInt (xDn), Clazz.floatToInt (yDn), Clazz.floatToInt (zDn), Clazz.doubleToInt (Math.ceil (this.xTip - xDn)), Clazz.doubleToInt (Math.ceil (this.yTip - yDn)), Clazz.doubleToInt (Math.ceil (this.zTip - zDn)), true);
}}}
this.g3d.setZMargin (0);
}, "~N,~B,~N,~N,~N,~N,~N,~N,~N,~N,~B,~B");
Clazz.defineMethod (c$, "generateBaseEllipse", 
 function () {
this.tEvenDiameter = (this.diameter & 1) == 0;
this.radius = this.diameter / 2.0;
this.radius2 = this.radius * this.radius;
var mag2d2 = this.dxB * this.dxB + this.dyB * this.dyB;
if (mag2d2 == 0) {
this.cosTheta = 1;
this.cosPhi = 1;
this.sinPhi = 0;
} else {
var mag2d = Math.sqrt (mag2d2);
var mag3d = Math.sqrt (mag2d2 + this.dzB * this.dzB);
this.cosTheta = this.dzB / mag3d;
this.cosPhi = this.dxB / mag2d;
this.sinPhi = this.dyB / mag2d;
}this.calcRotatedPoint (0, 0, false);
this.calcRotatedPoint (0.5, 1, false);
this.calcRotatedPoint (1, 2, false);
this.rasterCount = 3;
this.interpolate (0, 1);
this.interpolate (1, 2);
});
Clazz.defineMethod (c$, "generateBaseEllipsePrecisely", 
 function (isBarb) {
this.tEvenDiameter = (this.diameter & 1) == 0;
this.radius = this.diameter / 2.0;
this.radius2 = this.radius * this.radius;
var mag2d2 = this.dxBf * this.dxBf + this.dyBf * this.dyBf;
if (mag2d2 == 0) {
this.cosTheta = 1;
this.cosPhi = 1;
this.sinPhi = 0;
} else {
var mag2d = Math.sqrt (mag2d2);
var mag3d = Math.sqrt (mag2d2 + this.dzBf * this.dzBf);
this.cosTheta = this.dzBf / mag3d;
this.cosPhi = this.dxBf / mag2d;
this.sinPhi = this.dyBf / mag2d;
}if (isBarb) {
this.calcRotatedPoint (0, 0, true);
this.calcRotatedPoint (0.5, 1, true);
this.rasterCount = 2;
this.interpolatePrecisely (0, 1);
} else {
this.calcRotatedPoint (0, 0, true);
this.calcRotatedPoint (0.5, 1, true);
this.calcRotatedPoint (1, 2, true);
this.rasterCount = 3;
this.interpolatePrecisely (0, 1);
this.interpolatePrecisely (1, 2);
}for (var i = 0; i < this.rasterCount; i++) {
this.xRaster[i] = Clazz.doubleToInt (Math.floor (this.txRaster[i]));
this.yRaster[i] = Clazz.doubleToInt (Math.floor (this.tyRaster[i]));
this.zRaster[i] = Clazz.doubleToInt (Math.floor (this.tzRaster[i]));
}
}, "~B");
Clazz.defineMethod (c$, "calcRotatedPoint", 
 function (t, i, isPrecision) {
this.tRaster[i] = t;
var tPI = t * 3.141592653589793;
var xT = Math.sin (tPI) * this.cosTheta;
var yT = Math.cos (tPI);
var xR = this.radius * (xT * this.cosPhi - yT * this.sinPhi);
var yR = this.radius * (xT * this.sinPhi + yT * this.cosPhi);
var z2 = this.radius2 - (xR * xR + yR * yR);
var zR = (z2 > 0 ? Math.sqrt (z2) : 0);
if (isPrecision) {
this.txRaster[i] = xR;
this.tyRaster[i] = yR;
this.tzRaster[i] = zR;
} else if (this.tEvenDiameter) {
this.xRaster[i] = Clazz.doubleToInt (xR - 0.5);
this.yRaster[i] = Clazz.doubleToInt (yR - 0.5);
this.zRaster[i] = Clazz.doubleToInt (zR + 0.5);
} else {
this.xRaster[i] = Clazz.doubleToInt (xR);
this.yRaster[i] = Clazz.doubleToInt (yR);
this.zRaster[i] = Clazz.doubleToInt (zR + 0.5);
}this.fp8ShadeIndexUp[i] = this.shader.getShadeFp8 (xR, yR, zR);
}, "~N,~N,~B");
Clazz.defineMethod (c$, "interpolate", 
 function (iLower, iUpper) {
var dx = this.xRaster[iUpper] - this.xRaster[iLower];
if (dx < 0) dx = -dx;
var dy = this.yRaster[iUpper] - this.yRaster[iLower];
if (dy < 0) dy = -dy;
if ((dx + dy) <= 1) return;
var tLower = this.tRaster[iLower];
var tUpper = this.tRaster[iUpper];
var iMid = this.allocRaster (false);
for (var j = 4; --j >= 0; ) {
var tMid = (tLower + tUpper) / 2;
this.calcRotatedPoint (tMid, iMid, false);
if ((this.xRaster[iMid] == this.xRaster[iLower]) && (this.yRaster[iMid] == this.yRaster[iLower])) {
this.fp8ShadeIndexUp[iLower] = (this.fp8ShadeIndexUp[iLower] + this.fp8ShadeIndexUp[iMid]) >>> 1;
tLower = tMid;
} else if ((this.xRaster[iMid] == this.xRaster[iUpper]) && (this.yRaster[iMid] == this.yRaster[iUpper])) {
this.fp8ShadeIndexUp[iUpper] = (this.fp8ShadeIndexUp[iUpper] + this.fp8ShadeIndexUp[iMid]) >>> 1;
tUpper = tMid;
} else {
this.interpolate (iLower, iMid);
this.interpolate (iMid, iUpper);
return;
}}
this.xRaster[iMid] = this.xRaster[iLower];
this.yRaster[iMid] = this.yRaster[iUpper];
}, "~N,~N");
Clazz.defineMethod (c$, "interpolatePrecisely", 
 function (iLower, iUpper) {
var dx = Clazz.doubleToInt (Math.floor (this.txRaster[iUpper])) - Clazz.doubleToInt (Math.floor (this.txRaster[iLower]));
if (dx < 0) dx = -dx;
var dy = Clazz.doubleToInt (Math.floor (this.tyRaster[iUpper])) - Clazz.doubleToInt (Math.floor (this.tyRaster[iLower]));
if (dy < 0) dy = -dy;
if ((dx + dy) <= 1) return;
var tLower = this.tRaster[iLower];
var tUpper = this.tRaster[iUpper];
var iMid = this.allocRaster (true);
for (var j = 4; --j >= 0; ) {
var tMid = (tLower + tUpper) / 2;
this.calcRotatedPoint (tMid, iMid, true);
if ((Clazz.doubleToInt (Math.floor (this.txRaster[iMid])) == Clazz.doubleToInt (Math.floor (this.txRaster[iLower]))) && (Clazz.doubleToInt (Math.floor (this.tyRaster[iMid])) == Clazz.doubleToInt (Math.floor (this.tyRaster[iLower])))) {
this.fp8ShadeIndexUp[iLower] = (this.fp8ShadeIndexUp[iLower] + this.fp8ShadeIndexUp[iMid]) >>> 1;
tLower = tMid;
} else if ((Clazz.doubleToInt (Math.floor (this.txRaster[iMid])) == Clazz.doubleToInt (Math.floor (this.txRaster[iUpper]))) && (Clazz.doubleToInt (Math.floor (this.tyRaster[iMid])) == Clazz.doubleToInt (Math.floor (this.tyRaster[iUpper])))) {
this.fp8ShadeIndexUp[iUpper] = (this.fp8ShadeIndexUp[iUpper] + this.fp8ShadeIndexUp[iMid]) >>> 1;
tUpper = tMid;
} else {
this.interpolatePrecisely (iLower, iMid);
this.interpolatePrecisely (iMid, iUpper);
return;
}}
this.txRaster[iMid] = this.txRaster[iLower];
this.tyRaster[iMid] = this.tyRaster[iUpper];
}, "~N,~N");
Clazz.defineMethod (c$, "allocRaster", 
 function (isPrecision) {
while (this.rasterCount >= this.xRaster.length) {
this.xRaster = JU.AU.doubleLengthI (this.xRaster);
this.yRaster = JU.AU.doubleLengthI (this.yRaster);
this.zRaster = JU.AU.doubleLengthI (this.zRaster);
this.tRaster = JU.AU.doubleLengthF (this.tRaster);
}
while (this.rasterCount >= this.fp8ShadeIndexUp.length) this.fp8ShadeIndexUp = JU.AU.doubleLengthI (this.fp8ShadeIndexUp);

if (isPrecision) while (this.rasterCount >= this.txRaster.length) {
this.txRaster = JU.AU.doubleLengthF (this.txRaster);
this.tyRaster = JU.AU.doubleLengthF (this.tyRaster);
this.tzRaster = JU.AU.doubleLengthF (this.tzRaster);
}
return this.rasterCount++;
}, "~B");
Clazz.defineMethod (c$, "findMinMaxY", 
 function () {
this.yMin = this.yMax = this.yRaster[0];
for (var i = this.rasterCount; --i > 0; ) {
var y = this.yRaster[i];
if (y < this.yMin) this.yMin = y;
 else if (y > this.yMax) this.yMax = y;
 else {
y = -y;
if (y < this.yMin) this.yMin = y;
 else if (y > this.yMax) this.yMax = y;
}}
});
Clazz.defineMethod (c$, "findMinMaxX", 
 function (y) {
this.xMin = 2147483647;
this.xMax = -2147483648;
for (var i = this.rasterCount; --i >= 0; ) {
if (this.yRaster[i] == y) {
var x = this.xRaster[i];
if (x < this.xMin) {
this.xMin = x;
this.zXMin = this.zRaster[i];
}if (x > this.xMax) {
this.xMax = x;
this.zXMax = this.zRaster[i];
}}if (this.yRaster[i] == -y) {
var x = -this.xRaster[i];
if (x < this.xMin) {
this.xMin = x;
this.zXMin = -this.zRaster[i];
}if (x > this.xMax) {
this.xMax = x;
this.zXMax = -this.zRaster[i];
}}}
}, "~N");
Clazz.defineMethod (c$, "renderFlatEndcap", 
 function (tCylinder) {
if (this.dzB == 0 || !this.g3d.setC (this.colixEndcap)) return;
var xT = this.xA;
var yT = this.yA;
var zT = this.zA;
if (tCylinder && this.dzB < 0) {
if (this.endcaps == 4) return;
xT += this.dxB;
yT += this.dyB;
zT += this.dzB;
}this.findMinMaxY ();
for (var y = this.yMin; y <= this.yMax; ++y) {
this.findMinMaxX (y);
var count = this.xMax - this.xMin + 1;
this.g3d.setColorNoisy (this.endcapShadeIndex);
this.g3d.plotPixelsClippedRaster (count, xT + this.xMin, yT + y, zT - this.zXMin - 1, zT - this.zXMax - 1, null, null);
}
}, "~B");
Clazz.defineMethod (c$, "renderFlatEndcapPrecisely", 
 function (tCylinder) {
if (this.dzBf == 0 || !this.g3d.setC (this.colixEndcap)) return;
var xTf = this.xAf;
var yTf = this.yAf;
var zTf = this.zAf;
if (tCylinder && this.dzBf < 0) {
xTf += this.dxBf;
yTf += this.dyBf;
zTf += this.dzBf;
}var xT = Clazz.floatToInt (xTf);
var yT = Clazz.floatToInt (yTf);
var zT = Clazz.floatToInt (zTf);
this.findMinMaxY ();
for (var y = this.yMin; y <= this.yMax; ++y) {
this.findMinMaxX (y);
var count = this.xMax - this.xMin + 1;
this.g3d.setColorNoisy (this.endcapShadeIndex);
this.g3d.plotPixelsClippedRaster (count, xT + this.xMin, yT + y, zT - this.zXMin - 1, zT - this.zXMax - 1, null, null);
}
}, "~B");
Clazz.defineMethod (c$, "renderSphericalEndcaps", 
 function () {
if (this.colixA != 0 && this.g3d.setC (this.colixA)) this.g3d.fillSphereXYZ (this.diameter, this.xA, this.yA, this.zA + 1);
if (this.colixB != 0 && this.g3d.setC (this.colixB)) this.g3d.fillSphereXYZ (this.diameter, this.xA + this.dxB, this.yA + this.dyB, this.zA + this.dzB + 1);
});
Clazz.defineMethod (c$, "calcArgbEndcap", 
 function (tCylinder, isFloat) {
this.tEndcapOpen = false;
var dzf = (isFloat ? this.dzBf : this.dzB);
if (this.endcaps == 3 || dzf == 0) return;
this.xEndcap = this.xA;
this.yEndcap = this.yA;
this.zEndcap = this.zA;
var shadesEndcap;
var dxf = (isFloat ? this.dxBf : this.dxB);
var dyf = (isFloat ? this.dyBf : this.dyB);
if (dzf >= 0 || !tCylinder) {
this.endcapShadeIndex = this.shader.getShadeIndex (-dxf, -dyf, dzf);
this.colixEndcap = this.colixA;
shadesEndcap = this.shadesA;
} else {
this.endcapShadeIndex = this.shader.getShadeIndex (dxf, dyf, -dzf);
this.colixEndcap = this.colixB;
shadesEndcap = this.shadesB;
this.xEndcap += this.dxB;
this.yEndcap += this.dyB;
this.zEndcap += this.dzB;
}if (this.endcapShadeIndex > 56) this.endcapShadeIndex = 56;
this.argbEndcap = shadesEndcap[this.endcapShadeIndex];
this.tEndcapOpen = (this.endcaps == 1);
}, "~B,~B");
});
