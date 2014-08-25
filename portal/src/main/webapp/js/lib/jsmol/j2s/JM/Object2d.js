Clazz.declarePackage ("JM");
Clazz.load (null, "JM.Object2d", ["java.lang.Float", "JU.C", "JV.JC"], function () {
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
}, JM, "Object2d");
Clazz.prepareFields (c$, function () {
this.boxXY =  Clazz.newFloatArray (5, 0);
});
Clazz.defineMethod (c$, "getScalePixelsPerMicron", 
function () {
return this.scalePixelsPerMicron;
});
Clazz.defineMethod (c$, "setScalePixelsPerMicron", 
function (scalePixelsPerMicron) {
this.scalePixelsPerMicron = scalePixelsPerMicron;
}, "~N");
Clazz.defineMethod (c$, "setModel", 
function (modelIndex) {
this.modelIndex = modelIndex;
}, "~N");
Clazz.defineMethod (c$, "setVisibility", 
function (TF) {
this.visible = TF;
}, "~B");
Clazz.defineMethod (c$, "setXYZ", 
function (xyz, doAdjust) {
this.xyz = xyz;
if (xyz == null) this.zSlab = -2147483648;
if (doAdjust) {
this.valign = (xyz == null ? 0 : 4);
this.setAdjustForWindow (xyz == null);
}}, "JU.P3,~B");
Clazz.defineMethod (c$, "setAdjustForWindow", 
function (TF) {
this.adjustForWindow = TF;
}, "~B");
Clazz.defineMethod (c$, "setColix", 
function (colix) {
this.colix = colix;
}, "~N");
Clazz.defineMethod (c$, "setColixO", 
function (value) {
this.colix = JU.C.getColixO (value);
}, "~O");
Clazz.defineMethod (c$, "setTranslucent", 
function (level, isBackground) {
if (isBackground) {
if (this.bgcolix != 0) this.bgcolix = JU.C.getColixTranslucent3 (this.bgcolix, !Float.isNaN (level), level);
} else {
this.colix = JU.C.getColixTranslucent3 (this.colix, !Float.isNaN (level), level);
}}, "~N,~B");
Clazz.defineMethod (c$, "setBgColix", 
function (colix) {
this.bgcolix = colix;
}, "~N");
Clazz.defineMethod (c$, "setBgColixO", 
function (value) {
this.bgcolix = (value == null ? 0 : JU.C.getColixO (value));
}, "~O");
Clazz.defineMethod (c$, "setMovableX", 
 function (x) {
this.valign = (this.valign == 4 ? 4 : 0);
this.movableX = x;
this.movableXPercent = 2147483647;
}, "~N");
Clazz.defineMethod (c$, "setMovableY", 
 function (y) {
this.valign = (this.valign == 4 ? 4 : 0);
this.movableY = y;
this.movableYPercent = 2147483647;
}, "~N");
Clazz.defineMethod (c$, "setMovableXPercent", 
function (x) {
this.valign = (this.valign == 4 ? 4 : 0);
this.movableX = 2147483647;
this.movableXPercent = x;
}, "~N");
Clazz.defineMethod (c$, "setMovableYPercent", 
function (y) {
this.valign = (this.valign == 4 ? 4 : 0);
this.movableY = 2147483647;
this.movableYPercent = y;
}, "~N");
Clazz.defineMethod (c$, "setMovableZPercent", 
function (z) {
if (this.valign != 4) this.valign = 0;
this.movableZ = 2147483647;
this.movableZPercent = z;
}, "~N");
Clazz.defineMethod (c$, "setZs", 
function (z, zSlab) {
this.z = z;
this.zSlab = zSlab;
}, "~N,~N");
Clazz.defineMethod (c$, "setXYZs", 
function (x, y, z, zSlab) {
this.setMovableX (x);
this.setMovableY (y);
this.setZs (z, zSlab);
}, "~N,~N,~N,~N");
Clazz.defineMethod (c$, "setScript", 
function (script) {
this.script = (script == null || script.length == 0 ? null : script);
}, "~S");
Clazz.defineMethod (c$, "getScript", 
function () {
return this.script;
});
Clazz.defineMethod (c$, "setOffset", 
function (offset) {
this.offsetX = JV.JC.getXOffset (offset);
this.offsetY = JV.JC.getYOffset (offset);
this.pymolOffset = null;
this.valign = 0;
}, "~N");
Clazz.defineMethod (c$, "setAlignmentLCR", 
function (align) {
if ("left".equals (align)) return this.setAlignment (1);
if ("center".equals (align)) return this.setAlignment (2);
if ("right".equals (align)) return this.setAlignment (3);
return false;
}, "~S");
Clazz.defineMethod (c$, "setAlignment", 
function (align) {
if (this.align != align) {
this.align = align;
this.recalc ();
}return true;
}, "~N");
Clazz.defineMethod (c$, "setPointer", 
function (pointer) {
this.pointer = pointer;
}, "~N");
Clazz.defineMethod (c$, "setBoxOffsetsInWindow", 
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
Clazz.defineMethod (c$, "setWindow", 
function (width, height, scalePixelsPerMicron) {
this.windowWidth = width;
this.windowHeight = height;
if (this.pymolOffset == null && this.scalePixelsPerMicron < 0 && scalePixelsPerMicron != 0) this.scalePixelsPerMicron = scalePixelsPerMicron;
}, "~N,~N,~N");
Clazz.defineMethod (c$, "checkObjectClicked", 
function (isAntialiased, x, y, bsVisible) {
if (this.hidden || this.script == null || this.modelIndex >= 0 && !bsVisible.get (this.modelIndex)) return false;
if (isAntialiased) {
x <<= 1;
y <<= 1;
}return (x >= this.boxX && x <= this.boxX + this.boxWidth && y >= this.boxY && y <= this.boxY + this.boxHeight);
}, "~B,~N,~N,JU.BS");
c$.setProperty = Clazz.defineMethod (c$, "setProperty", 
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
}, "~S,~O,JM.Object2d");
});
