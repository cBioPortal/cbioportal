Clazz.declarePackage ("J.g3d");
Clazz.load (["java.util.Hashtable"], "J.g3d.LineRenderer", ["java.lang.Float", "J.util.BSUtil", "$.Logger", "$.Shader"], function () {
c$ = Clazz.decorateAsClass (function () {
this.g3d = null;
this.shader = null;
this.lineBits = null;
this.slope = 0;
this.lineTypeX = false;
this.nBits = 0;
this.nCached = 0;
this.nFound = 0;
this.lineCache = null;
this.slopeKey = null;
this.x1t = 0;
this.y1t = 0;
this.z1t = 0;
this.x2t = 0;
this.y2t = 0;
this.z2t = 0;
this.cc1 = 0;
this.cc2 = 0;
Clazz.instantialize (this, arguments);
}, J.g3d, "LineRenderer");
Clazz.prepareFields (c$, function () {
this.lineCache =  new java.util.Hashtable ();
});
Clazz.makeConstructor (c$, 
function (g3d) {
this.g3d = g3d;
this.shader = g3d.shader;
}, "J.g3d.Graphics3D");
$_M(c$, "setLineBits", 
function (dx, dy) {
this.slope = (dx != 0 ? dy / dx : dy >= 0 ? 3.4028235E38 : -3.4028235E38);
this.lineTypeX = (this.slope <= 1 && this.slope >= -1);
this.nBits = (this.lineTypeX ? this.g3d.getRenderWidth () : this.g3d.getRenderHeight ());
if (this.getCachedLine ()) return;
this.lineBits = J.util.BSUtil.newBitSet (this.nBits);
dy = Math.abs (dy);
dx = Math.abs (dx);
if (dy > dx) {
var t = dx;
dx = dy;
dy = t;
}var twoDError = 0;
var twoDx = dx + dx;
var twoDy = dy + dy;
for (var i = 0; i < this.nBits; i++) {
twoDError += twoDy;
if (twoDError > dx) {
this.lineBits.set (i);
twoDError -= twoDx;
}}
this.lineCache.put (this.slopeKey, this.lineBits);
this.nCached++;
}, "~N,~N");
$_M(c$, "clearLineCache", 
function () {
this.lineCache.clear ();
this.nCached = 0;
});
$_M(c$, "plotLine", 
function (argbA, tScreenedA, argbB, tScreenedB, xA, yA, zA, xB, yB, zB, clipped) {
this.x1t = xA;
this.x2t = xB;
this.y1t = yA;
this.y2t = yB;
this.z1t = zA;
this.z2t = zB;
if (clipped) switch (this.getTrimmedLine ()) {
case 0:
clipped = false;
break;
case 2:
return;
}
this.plotLineClipped (argbA, tScreenedA, argbB, tScreenedB, xA, yA, zA, xB - xA, yB - yA, zB - zA, clipped, 0, 0);
}, "~N,~B,~N,~B,~N,~N,~N,~N,~N,~N,~B");
$_M(c$, "plotLineDelta", 
function (argbA, tScreenedA, argbB, tScreenedB, xA, yA, zA, dxBA, dyBA, dzBA, clipped) {
this.x1t = xA;
this.x2t = xA + dxBA;
this.y1t = yA;
this.y2t = yA + dyBA;
this.z1t = zA;
this.z2t = zA + dzBA;
if (clipped) switch (this.getTrimmedLine ()) {
case 2:
return;
case 0:
clipped = false;
break;
}
this.plotLineClipped (argbA, tScreenedA, argbB, tScreenedB, xA, yA, zA, dxBA, dyBA, dzBA, clipped, 0, 0);
}, "~N,~B,~N,~B,~N,~N,~N,~N,~N,~N,~B");
$_M(c$, "plotLineDeltaA", 
function (shades1, tScreened1, shades2, tScreened2, shadeIndex, xA, yA, zA, dxBA, dyBA, dzBA, clipped) {
this.x1t = xA;
this.x2t = xA + dxBA;
this.y1t = yA;
this.y2t = yA + dyBA;
this.z1t = zA;
this.z2t = zA + dzBA;
if (clipped) switch (this.getTrimmedLine ()) {
case 2:
return;
case 0:
clipped = false;
}
this.plotLineClippedA (shades1, tScreened1, shades2, tScreened2, shadeIndex, xA, yA, zA, dxBA, dyBA, dzBA, clipped, 0, 0);
}, "~A,~B,~A,~B,~N,~N,~N,~N,~N,~N,~N,~B");
$_M(c$, "plotLineDeltaBits", 
function (shades1, tScreened1, shades2, tScreened2, shadeIndex, xA, yA, zA, dxBA, dyBA, dzBA, clipped) {
this.x1t = xA;
this.x2t = xA + dxBA;
this.y1t = yA;
this.y2t = yA + dyBA;
this.z1t = zA;
this.z2t = zA + dzBA;
if (clipped && this.getTrimmedLine () == 2) return;
this.plotLineClippedBits (shades1, tScreened1, shades2, tScreened2, shadeIndex, xA, yA, zA, dxBA, dyBA, dzBA, 0, 0);
}, "~A,~B,~A,~B,~N,~N,~N,~N,~N,~N,~N,~B");
$_M(c$, "plotDashedLine", 
function (argb, tScreened, run, rise, xA, yA, zA, xB, yB, zB, clipped) {
this.x1t = xA;
this.x2t = xB;
this.y1t = yA;
this.y2t = yB;
this.z1t = zA;
this.z2t = zB;
if (clipped) switch (this.getTrimmedLine ()) {
case 2:
return;
case 0:
clipped = false;
break;
}
this.plotLineClipped (argb, tScreened, argb, tScreened, xA, yA, zA, xB - xA, yB - yA, zB - zA, clipped, run, rise);
}, "~N,~B,~N,~N,~N,~N,~N,~N,~N,~N,~B");
$_M(c$, "getCachedLine", 
($fz = function () {
this.slopeKey = Float.$valueOf (this.slope);
if (!this.lineCache.containsKey (this.slopeKey)) return false;
this.lineBits = this.lineCache.get (this.slopeKey);
if (J.util.Logger.debugging) {
this.nFound++;
if (this.nFound == 1000000) J.util.Logger.debug ("nCached/nFound lines: " + this.nCached + " " + this.nFound);
}return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getTrimmedLine", 
($fz = function () {
this.cc1 = this.g3d.clipCode3 (this.x1t, this.y1t, this.z1t);
this.cc2 = this.g3d.clipCode3 (this.x2t, this.y2t, this.z2t);
if ((this.cc1 | this.cc2) == 0) return 0;
var xLast = this.g3d.xLast;
var yLast = this.g3d.yLast;
var slab = this.g3d.slab;
var depth = this.g3d.depth;
do {
if ((this.cc1 & this.cc2) != 0) return 2;
var dx = this.x2t - this.x1t;
var dy = this.y2t - this.y1t;
var dz = this.z2t - this.z1t;
if (this.cc1 != 0) {
if ((this.cc1 & 8) != 0) {
this.y1t += (-this.x1t * dy) / dx;
this.z1t += (-this.x1t * dz) / dx;
this.x1t = 0;
} else if ((this.cc1 & 4) != 0) {
this.y1t += ((xLast - this.x1t) * dy) / dx;
this.z1t += ((xLast - this.x1t) * dz) / dx;
this.x1t = xLast;
} else if ((this.cc1 & 2) != 0) {
this.x1t += (-this.y1t * dx) / dy;
this.z1t += (-this.y1t * dz) / dy;
this.y1t = 0;
} else if ((this.cc1 & 1) != 0) {
this.x1t += ((yLast - this.y1t) * dx) / dy;
this.z1t += ((yLast - this.y1t) * dz) / dy;
this.y1t = yLast;
} else if ((this.cc1 & 32) != 0) {
this.x1t += ((slab - this.z1t) * dx) / dz;
this.y1t += ((slab - this.z1t) * dy) / dz;
this.z1t = slab;
} else {
this.x1t += ((depth - this.z1t) * dx) / dz;
this.y1t += ((depth - this.z1t) * dy) / dz;
this.z1t = depth;
}this.cc1 = this.g3d.clipCode3 (this.x1t, this.y1t, this.z1t);
} else {
if ((this.cc2 & 8) != 0) {
this.y2t += (-this.x2t * dy) / dx;
this.z2t += (-this.x2t * dz) / dx;
this.x2t = 0;
} else if ((this.cc2 & 4) != 0) {
this.y2t += ((xLast - this.x2t) * dy) / dx;
this.z2t += ((xLast - this.x2t) * dz) / dx;
this.x2t = xLast;
} else if ((this.cc2 & 2) != 0) {
this.x2t += (-this.y2t * dx) / dy;
this.z2t += (-this.y2t * dz) / dy;
this.y2t = 0;
} else if ((this.cc2 & 1) != 0) {
this.x2t += ((yLast - this.y2t) * dx) / dy;
this.z2t += ((yLast - this.y2t) * dz) / dy;
this.y2t = yLast;
} else if ((this.cc2 & 32) != 0) {
this.x2t += ((slab - this.z2t) * dx) / dz;
this.y2t += ((slab - this.z2t) * dy) / dz;
this.z2t = slab;
} else {
this.x2t += ((depth - this.z2t) * dx) / dz;
this.y2t += ((depth - this.z2t) * dy) / dz;
this.z2t = depth;
}this.cc2 = this.g3d.clipCode3 (this.x2t, this.y2t, this.z2t);
}} while ((this.cc1 | this.cc2) != 0);
return 1;
}, $fz.isPrivate = true, $fz));
$_M(c$, "plotLineClipped", 
($fz = function (argb1, tScreened1, argb2, tScreened2, x, y, z, dx, dy, dz, clipped, run, rise) {
var zbuf = this.g3d.zbuf;
var width = this.g3d.getRenderWidth ();
var runIndex = 0;
if (run == 0) {
rise = 2147483647;
run = 1;
}var offset = y * width + x;
var offsetMax = this.g3d.bufferSize;
var flipflop = (((x ^ y) & 1) != 0);
var tScreened = tScreened1;
var argb = argb1;
if (argb != 0 && !clipped && offset >= 0 && offset < offsetMax && z < zbuf[offset] && (!tScreened || (flipflop = !flipflop))) {
this.g3d.addPixel (offset, z, argb);
}if (dx == 0 && dy == 0) {
return;
}var xIncrement = 1;
var yOffsetIncrement = width;
var x2 = x + dx;
var y2 = y + dy;
if (dx < 0) {
dx = -dx;
xIncrement = -1;
}if (dy < 0) {
dy = -dy;
yOffsetIncrement = -width;
}var twoDx = dx + dx;
var twoDy = dy + dy;
var zCurrentScaled = z << 10;
if (dy <= dx) {
var roundingFactor = dx - 1;
if (dz < 0) roundingFactor = -roundingFactor;
var zIncrementScaled = Clazz.doubleToInt (((dz << 10) + roundingFactor) / dx);
var twoDxAccumulatedYError = 0;
var n1 = Math.abs (x2 - this.x2t) - 1;
var n2 = Math.abs (x2 - this.x1t) - 1;
for (var n = dx - 1, nMid = Clazz.doubleToInt (n / 2); --n >= n1; ) {
if (n == nMid) {
tScreened = tScreened2;
argb = argb2;
if (argb == 0) return;
}offset += xIncrement;
zCurrentScaled += zIncrementScaled;
twoDxAccumulatedYError += twoDy;
if (twoDxAccumulatedYError > dx) {
offset += yOffsetIncrement;
twoDxAccumulatedYError -= twoDx;
flipflop = !flipflop;
}if (argb != 0 && n < n2 && offset >= 0 && offset < offsetMax && runIndex < rise && (!tScreened || (flipflop = !flipflop))) {
var zCurrent = zCurrentScaled >> 10;
if (zCurrent < zbuf[offset]) this.g3d.addPixel (offset, zCurrent, argb);
}runIndex = (runIndex + 1) % run;
}
} else {
var roundingFactor = dy - 1;
if (dz < 0) roundingFactor = -roundingFactor;
var zIncrementScaled = Clazz.doubleToInt (((dz << 10) + roundingFactor) / dy);
var twoDyAccumulatedXError = 0;
var n1 = Math.abs (y2 - this.y2t) - 1;
var n2 = Math.abs (y2 - this.y1t) - 1;
for (var n = dy - 1, nMid = Clazz.doubleToInt (n / 2); --n >= n1; ) {
if (n == nMid) {
tScreened = tScreened2;
argb = argb2;
if (argb == 0) return;
}offset += yOffsetIncrement;
zCurrentScaled += zIncrementScaled;
twoDyAccumulatedXError += twoDx;
if (twoDyAccumulatedXError > dy) {
offset += xIncrement;
twoDyAccumulatedXError -= twoDy;
flipflop = !flipflop;
}if (argb != 0 && n < n2 && offset >= 0 && offset < offsetMax && runIndex < rise && (!tScreened || (flipflop = !flipflop))) {
var zCurrent = zCurrentScaled >> 10;
if (zCurrent < zbuf[offset]) this.g3d.addPixel (offset, zCurrent, argb);
}runIndex = (runIndex + 1) % run;
}
}}, $fz.isPrivate = true, $fz), "~N,~B,~N,~B,~N,~N,~N,~N,~N,~N,~B,~N,~N");
$_M(c$, "plotLineClippedA", 
($fz = function (shades1, tScreened1, shades2, tScreened2, shadeIndex, x, y, z, dx, dy, dz, clipped, run, rise) {
var zbuf = this.g3d.zbuf;
var width = this.g3d.getRenderWidth ();
var runIndex = 0;
if (run == 0) {
rise = 2147483647;
run = 1;
}var offset = y * width + x;
var offsetMax = this.g3d.bufferSize;
var shadeIndexUp = (shadeIndex < J.util.Shader.shadeIndexLast ? shadeIndex + 1 : shadeIndex);
var shadeIndexDn = (shadeIndex > 0 ? shadeIndex - 1 : shadeIndex);
var argb1 = shades1[shadeIndex];
var argb1Up = shades1[shadeIndexUp];
var argb1Dn = shades1[shadeIndexDn];
var argb2 = shades2[shadeIndex];
var argb2Up = shades2[shadeIndexUp];
var argb2Dn = shades2[shadeIndexDn];
var argb = argb1;
var tScreened = tScreened1;
var flipflop = (((x ^ y) & 1) != 0);
if (argb != 0 && !clipped && offset >= 0 && offset < offsetMax && z < zbuf[offset] && (!tScreened || (flipflop = !flipflop))) this.g3d.addPixel (offset, z, argb);
if (dx == 0 && dy == 0) {
return;
}var xIncrement = 1;
var yOffsetIncrement = width;
var x2 = x + dx;
var y2 = y + dy;
if (dx < 0) {
dx = -dx;
xIncrement = -1;
}if (dy < 0) {
dy = -dy;
yOffsetIncrement = -width;
}var twoDx = dx + dx;
var twoDy = dy + dy;
var zCurrentScaled = z << 10;
var argbUp = argb1Up;
var argbDn = argb1Dn;
if (dy <= dx) {
var roundingFactor = dx - 1;
if (dz < 0) roundingFactor = -roundingFactor;
var zIncrementScaled = Clazz.doubleToInt (((dz << 10) + roundingFactor) / dx);
var twoDxAccumulatedYError = 0;
var n1 = Math.abs (x2 - this.x2t) - 1;
var n2 = Math.abs (x2 - this.x1t) - 1;
for (var n = dx - 1, nMid = Clazz.doubleToInt (n / 2); --n >= n1; ) {
if (n == nMid) {
argb = argb2;
if (argb == 0) return;
argbUp = argb2Up;
argbDn = argb2Dn;
tScreened = tScreened2;
if (tScreened && !tScreened1) {
var yT = Clazz.doubleToInt (offset / width);
var xT = offset % width;
flipflop = ((xT ^ yT) & 1) == 0;
}}offset += xIncrement;
zCurrentScaled += zIncrementScaled;
twoDxAccumulatedYError += twoDy;
if (twoDxAccumulatedYError > dx) {
offset += yOffsetIncrement;
twoDxAccumulatedYError -= twoDx;
flipflop = !flipflop;
}if (argb != 0 && n < n2 && offset >= 0 && offset < offsetMax && runIndex < rise && (!tScreened || (flipflop = !flipflop))) {
var zCurrent = zCurrentScaled >> 10;
if (zCurrent < zbuf[offset]) {
var rand8 = this.shader.nextRandom8Bit ();
this.g3d.addPixel (offset, zCurrent, rand8 < 85 ? argbDn : (rand8 > 170 ? argbUp : argb));
}}runIndex = (runIndex + 1) % run;
}
} else {
var roundingFactor = dy - 1;
if (dz < 0) roundingFactor = -roundingFactor;
var zIncrementScaled = Clazz.doubleToInt (((dz << 10) + roundingFactor) / dy);
var twoDyAccumulatedXError = 0;
var n1 = Math.abs (y2 - this.y2t) - 1;
var n2 = Math.abs (y2 - this.y1t) - 1;
for (var n = dy - 1, nMid = Clazz.doubleToInt (n / 2); --n >= n1; ) {
if (n == nMid) {
argb = argb2;
if (argb == 0) return;
argbUp = argb2Up;
argbDn = argb2Dn;
tScreened = tScreened2;
if (tScreened && !tScreened1) {
var yT = Clazz.doubleToInt (offset / width);
var xT = offset % width;
flipflop = ((xT ^ yT) & 1) == 0;
}}offset += yOffsetIncrement;
zCurrentScaled += zIncrementScaled;
twoDyAccumulatedXError += twoDx;
if (twoDyAccumulatedXError > dy) {
offset += xIncrement;
twoDyAccumulatedXError -= twoDy;
flipflop = !flipflop;
}if (argb != 0 && n < n2 && offset >= 0 && offset < offsetMax && runIndex < rise && (!tScreened || (flipflop = !flipflop))) {
var zCurrent = zCurrentScaled >> 10;
if (zCurrent < zbuf[offset]) {
var rand8 = this.g3d.shader.nextRandom8Bit ();
this.g3d.addPixel (offset, zCurrent, rand8 < 85 ? argbDn : (rand8 > 170 ? argbUp : argb));
}}runIndex = (runIndex + 1) % run;
}
}}, $fz.isPrivate = true, $fz), "~A,~B,~A,~B,~N,~N,~N,~N,~N,~N,~N,~B,~N,~N");
$_M(c$, "plotLineClippedBits", 
($fz = function (shades1, tScreened1, shades2, tScreened2, shadeIndex, x, y, z, dx, dy, dz, run, rise) {
var zbuf = this.g3d.zbuf;
var width = this.g3d.width;
var runIndex = 0;
if (run == 0) {
rise = 2147483647;
run = 1;
}var shadeIndexUp = (shadeIndex < J.util.Shader.shadeIndexLast ? shadeIndex + 1 : shadeIndex);
var shadeIndexDn = (shadeIndex > 0 ? shadeIndex - 1 : shadeIndex);
var argb1 = shades1[shadeIndex];
var argb1Up = shades1[shadeIndexUp];
var argb1Dn = shades1[shadeIndexDn];
var argb2 = shades2[shadeIndex];
var argb2Up = shades2[shadeIndexUp];
var argb2Dn = shades2[shadeIndexDn];
var tScreened = tScreened1;
var flipflop = (((x ^ y) & 1) != 0);
var offset = y * width + x;
var offsetMax = this.g3d.bufferSize;
var i0;
var iMid;
var i1;
var i2;
var iIncrement;
var xIncrement;
var yIncrement;
var zIncrement;
if (this.lineTypeX) {
i0 = x;
i1 = this.x1t;
i2 = this.x2t;
iMid = x + Clazz.doubleToInt (dx / 2);
iIncrement = (dx >= 0 ? 1 : -1);
xIncrement = iIncrement;
yIncrement = (dy >= 0 ? width : -width);
zIncrement = dz / Math.abs (dx);
} else {
i0 = y;
i1 = this.y1t;
i2 = this.y2t;
iMid = y + Clazz.doubleToInt (dy / 2);
iIncrement = (dy >= 0 ? 1 : -1);
xIncrement = (dy >= 0 ? width : -width);
yIncrement = (dx >= 0 ? 1 : -1);
zIncrement = dz / Math.abs (dy);
}var zFloat = z;
var argb = argb1;
var argbUp = argb1Up;
var argbDn = argb1Dn;
var isInWindow = false;
for (var i = i0, iBits = i0; ; i += iIncrement, iBits += iIncrement) {
if (i == i1) isInWindow = true;
if (i == iMid) {
argb = argb2;
if (argb == 0) return;
argbUp = argb2Up;
argbDn = argb2Dn;
tScreened = tScreened2;
if (tScreened && !tScreened1) {
var yT = Clazz.doubleToInt (offset / width);
var xT = offset % width;
flipflop = ((xT ^ yT) & 1) == 0;
}}if (argb != 0 && isInWindow && offset >= 0 && offset < offsetMax && runIndex < rise && (!tScreened || (flipflop = !flipflop))) {
if (zFloat < zbuf[offset]) {
var rand8 = this.shader.nextRandom8Bit ();
this.g3d.addPixel (offset, Clazz.floatToInt (zFloat), rand8 < 85 ? argbDn : (rand8 > 170 ? argbUp : argb));
}}if (i == i2) break;
runIndex = (runIndex + 1) % run;
offset += xIncrement;
while (iBits < 0) iBits += this.nBits;

if (this.lineBits.get (iBits % this.nBits)) offset += yIncrement;
zFloat += zIncrement;
}
}, $fz.isPrivate = true, $fz), "~A,~B,~A,~B,~N,~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.defineStatics (c$,
"VISIBILITY_UNCLIPPED", 0,
"VISIBILITY_CLIPPED", 1,
"VISIBILITY_OFFSCREEN", 2);
});
