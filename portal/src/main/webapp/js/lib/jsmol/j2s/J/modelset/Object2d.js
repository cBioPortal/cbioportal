Clazz.declarePackage ("J.modelset");
Clazz.load (null, "J.modelset.Object2d", ["java.lang.Float", "J.util.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.isLabelOrHover = false;
this.gdata = null;
this.xyz = null;
this.target = null;
this.script = null;
this.colix = 0;
this.bgcolix = 0;
this.pointer = 0;
this.align = 0;
this.valign = 0;
this.atomX = 0;
this.atomY = 0;
this.atomZ = 2147483647;
this.movableX = 0;
this.movableY = 0;
this.movableZ = 0;
this.movableXPercent = 2147483647;
this.movableYPercent = 2147483647;
this.movableZPercent = 2147483647;
this.offsetX = 0;
this.offsetY = 0;
this.z = 1;
this.zSlab = -2147483648;
this.pymolOffset = null;
this.windowWidth = 0;
this.windowHeight = 0;
this.adjustForWindow = false;
this.boxWidth = 0;
this.boxHeight = 0;
this.boxX = 0;
this.boxY = 0;
this.modelIndex = -1;
this.visible = true;
this.hidden = false;
this.boxXY = null;
this.scalePixelsPerMicron = 0;
Clazz.instantialize (this, arguments);
}, J.modelset, "Object2d");
Clazz.prepareFields (c$, function () {
this.boxXY =  Clazz.newFloatArray (5, 0);
});
$_M(c$, "getScalePixelsPerMicron", 
function () {
return this.scalePixelsPerMicron;
});
$_M(c$, "setScalePixelsPerMicron", 
function (scalePixelsPerMicron) {
this.scalePixelsPerMicron = scalePixelsPerMicron;
}, "~N");
$_M(c$, "setModel", 
function (modelIndex) {
this.modelIndex = modelIndex;
}, "~N");
$_M(c$, "setVisibility", 
function (TF) {
this.visible = TF;
}, "~B");
$_M(c$, "setXYZ", 
function (xyz, doAdjust) {
this.xyz = xyz;
if (xyz == null) this.zSlab = -2147483648;
if (doAdjust) {
this.valign = (xyz == null ? 0 : 4);
this.setAdjustForWindow (xyz == null);
}}, "J.util.P3,~B");
$_M(c$, "setAdjustForWindow", 
function (TF) {
this.adjustForWindow = TF;
}, "~B");
$_M(c$, "setColix", 
function (colix) {
this.colix = colix;
}, "~N");
$_M(c$, "setColixO", 
function (value) {
this.colix = J.util.C.getColixO (value);
}, "~O");
$_M(c$, "setTranslucent", 
function (level, isBackground) {
if (isBackground) {
if (this.bgcolix != 0) this.bgcolix = J.util.C.getColixTranslucent3 (this.bgcolix, !Float.isNaN (level), level);
} else {
this.colix = J.util.C.getColixTranslucent3 (this.colix, !Float.isNaN (level), level);
}}, "~N,~B");
$_M(c$, "setBgColix", 
function (colix) {
this.bgcolix = colix;
}, "~N");
$_M(c$, "setBgColixO", 
function (value) {
this.bgcolix = (value == null ? 0 : J.util.C.getColixO (value));
}, "~O");
$_M(c$, "setMovableX", 
($fz = function (x) {
this.valign = (this.valign == 4 ? 4 : 0);
this.movableX = x;
this.movableXPercent = 2147483647;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setMovableY", 
($fz = function (y) {
this.valign = (this.valign == 4 ? 4 : 0);
this.movableY = y;
this.movableYPercent = 2147483647;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setMovableXPercent", 
function (x) {
this.valign = (this.valign == 4 ? 4 : 0);
this.movableX = 2147483647;
this.movableXPercent = x;
}, "~N");
$_M(c$, "setMovableYPercent", 
function (y) {
this.valign = (this.valign == 4 ? 4 : 0);
this.movableY = 2147483647;
this.movableYPercent = y;
}, "~N");
$_M(c$, "setMovableZPercent", 
function (z) {
if (this.valign != 4) this.valign = 0;
this.movableZ = 2147483647;
this.movableZPercent = z;
}, "~N");
$_M(c$, "setZs", 
function (z, zSlab) {
this.z = z;
this.zSlab = zSlab;
}, "~N,~N");
$_M(c$, "setXYZs", 
function (x, y, z, zSlab) {
this.setMovableX (x);
this.setMovableY (y);
this.setZs (z, zSlab);
}, "~N,~N,~N,~N");
$_M(c$, "setScript", 
function (script) {
this.script = (script == null || script.length == 0 ? null : script);
}, "~S");
$_M(c$, "getScript", 
function () {
return this.script;
});
$_M(c$, "setOffset", 
function (offset) {
this.offsetX = J.modelset.Object2d.getXOffset (offset);
this.offsetY = J.modelset.Object2d.getYOffset (offset);
this.pymolOffset = null;
this.valign = 0;
}, "~N");
c$.getXOffset = $_M(c$, "getXOffset", 
function (offset) {
switch (offset) {
case 0:
return 4;
case 32767:
return 0;
default:
return ((offset << 48) >> 56);
}
}, "~N");
c$.getYOffset = $_M(c$, "getYOffset", 
function (offset) {
switch (offset) {
case 0:
return -4;
case 32767:
return 0;
default:
return -((offset << 56) >> 56);
}
}, "~N");
$_M(c$, "setAlignmentLCR", 
function (align) {
if ("left".equals (align)) return this.setAlignment (1);
if ("center".equals (align)) return this.setAlignment (2);
if ("right".equals (align)) return this.setAlignment (3);
return false;
}, "~S");
$_M(c$, "setAlignment", 
function (align) {
if (this.align != align) {
this.align = align;
this.recalc ();
}return true;
}, "~N");
c$.getAlignmentName = $_M(c$, "getAlignmentName", 
function (align) {
return J.modelset.Object2d.hAlignNames[align & 3];
}, "~N");
$_M(c$, "setPointer", 
function (pointer) {
this.pointer = pointer;
}, "~N");
c$.getPointer = $_M(c$, "getPointer", 
function (pointer) {
return ((pointer & 1) == 0 ? "" : (pointer & 2) > 0 ? "background" : "on");
}, "~N");
$_M(c$, "setBoxOffsetsInWindow", 
function (margin, vMargin, vTop) {
var bw = this.boxWidth + margin;
var x = this.boxX;
if (x + bw > this.windowWidth) x = this.windowWidth - bw;
if (x < margin) x = margin;
this.boxX = x;
var bh = this.boxHeight;
var y = vTop;
if (y + bh > this.windowHeight) y = this.windowHeight - bh;
if (y < vMargin) y = vMargin;
this.boxY = y;
}, "~N,~N,~N");
$_M(c$, "setWindow", 
function (width, height, scalePixelsPerMicron) {
this.windowWidth = width;
this.windowHeight = height;
if (this.pymolOffset == null && this.scalePixelsPerMicron < 0 && scalePixelsPerMicron != 0) this.scalePixelsPerMicron = scalePixelsPerMicron;
}, "~N,~N,~N");
$_M(c$, "checkObjectClicked", 
function (isAntialiased, x, y, bsVisible) {
if (this.hidden || this.script == null || this.modelIndex >= 0 && !bsVisible.get (this.modelIndex)) return false;
if (isAntialiased) {
x <<= 1;
y <<= 1;
}return (x >= this.boxX && x <= this.boxX + this.boxWidth && y >= this.boxY && y <= this.boxY + this.boxHeight);
}, "~B,~N,~N,J.util.BS");
c$.setProperty = $_M(c$, "setProperty", 
function (propertyName, value, currentObject) {
if ("script" === propertyName) {
if (currentObject != null) currentObject.setScript (value);
return true;
}if ("xpos" === propertyName) {
if (currentObject != null) currentObject.setMovableX ((value).intValue ());
return true;
}if ("ypos" === propertyName) {
if (currentObject != null) currentObject.setMovableY ((value).intValue ());
return true;
}if ("%xpos" === propertyName) {
if (currentObject != null) currentObject.setMovableXPercent ((value).intValue ());
return true;
}if ("%ypos" === propertyName) {
if (currentObject != null) currentObject.setMovableYPercent ((value).intValue ());
return true;
}if ("%zpos" === propertyName) {
if (currentObject != null) currentObject.setMovableZPercent ((value).intValue ());
return true;
}if ("xypos" === propertyName) {
if (currentObject == null) return true;
var pt = value;
currentObject.setXYZ (null, true);
if (pt.z == 3.4028235E38) {
currentObject.setMovableX (Clazz.floatToInt (pt.x));
currentObject.setMovableY (Clazz.floatToInt (pt.y));
} else {
currentObject.setMovableXPercent (Clazz.floatToInt (pt.x));
currentObject.setMovableYPercent (Clazz.floatToInt (pt.y));
}return true;
}if ("xyz" === propertyName) {
if (currentObject != null) {
currentObject.setXYZ (value, true);
}return true;
}return false;
}, "~S,~O,J.modelset.Object2d");
c$.getOffset = $_M(c$, "getOffset", 
function (xOffset, yOffset) {
xOffset = Math.min (Math.max (xOffset, -127), 127);
yOffset = Math.min (Math.max (yOffset, -127), 127);
return ((xOffset & 0xFF) << 8) | (yOffset & 0xFF);
}, "~N,~N");
Clazz.defineStatics (c$,
"POINTER_NONE", 0,
"POINTER_ON", 1,
"POINTER_BACKGROUND", 2,
"hAlignNames", ["", "left", "center", "right", ""],
"ALIGN_NONE", 0,
"ALIGN_LEFT", 1,
"ALIGN_CENTER", 2,
"ALIGN_RIGHT", 3,
"vAlignNames", ["xy", "top", "bottom", "middle"],
"VALIGN_XY", 0,
"VALIGN_TOP", 1,
"VALIGN_BOTTOM", 2,
"VALIGN_MIDDLE", 3,
"VALIGN_XYZ", 4);
});
