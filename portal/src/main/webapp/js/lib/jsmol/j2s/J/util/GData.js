Clazz.declarePackage ("J.util");
Clazz.load (["J.api.JmolGraphicsInterface"], "J.util.GData", ["J.util.ArrayUtil", "$.C", "$.JmolFont", "$.Shader"], function () {
c$ = Clazz.decorateAsClass (function () {
this.apiPlatform = null;
this.translucentCoverOnly = false;
this.windowWidth = 0;
this.windowHeight = 0;
this.displayMinX = 0;
this.displayMaxX = 0;
this.displayMinY = 0;
this.displayMaxY = 0;
this.antialiasThisFrame = false;
this.antialiasEnabled = false;
this.inGreyscaleMode = false;
this.changeableColixMap = null;
this.backgroundImage = null;
this.newWindowWidth = 0;
this.newWindowHeight = 0;
this.newAntialiasing = false;
this.bgcolor = 0;
this.xLast = 0;
this.yLast = 0;
this.slab = 0;
this.depth = 0;
this.width = 0;
this.height = 0;
this.zSlab = 0;
this.zDepth = 0;
this.zShadePower = 3;
this.colixCurrent = 0;
this.argbCurrent = 0;
this.bufferSize = 0;
this.shader = null;
this.zShadeR = 0;
this.zShadeG = 0;
this.zShadeB = 0;
this.graphicsForMetrics = null;
this.$isPass2 = false;
Clazz.instantialize (this, arguments);
}, J.util, "GData", null, J.api.JmolGraphicsInterface);
Clazz.prepareFields (c$, function () {
this.changeableColixMap =  Clazz.newShortArray (16, 0);
});
$_M(c$, "setTranslucentCoverOnly", 
function (TF) {
this.translucentCoverOnly = TF;
}, "~B");
$_M(c$, "getTranslucentCoverOnly", 
function () {
return this.translucentCoverOnly;
});
Clazz.makeConstructor (c$, 
function () {
this.shader =  new J.util.Shader ();
});
$_M(c$, "initialize", 
function (apiPlatform) {
this.apiPlatform = apiPlatform;
}, "J.api.ApiPlatform");
Clazz.overrideMethod (c$, "setDepth", 
function (depthValue) {
this.depth = depthValue < 0 ? 0 : depthValue;
}, "~N");
Clazz.overrideMethod (c$, "setSlab", 
function (slabValue) {
this.slab = slabValue < 0 ? 0 : slabValue;
}, "~N");
$_M(c$, "setZShade", 
function (zShade, zSlab, zDepth, zPower) {
if (zShade) this.setZShade2 (zSlab, zDepth, zPower);
}, "~B,~N,~N,~N");
$_M(c$, "setZShade2", 
function (zSlab, zDepth, zPower) {
this.zShadeR = this.bgcolor & 0xFF;
this.zShadeG = (this.bgcolor & 0xFF00) >> 8;
this.zShadeB = (this.bgcolor & 0xFF0000) >> 16;
this.zSlab = zSlab < 0 ? 0 : zSlab;
this.zDepth = zDepth < 0 ? 0 : zDepth;
this.zShadePower = zPower;
}, "~N,~N,~N");
Clazz.overrideMethod (c$, "getRenderWidth", 
function () {
return this.width;
});
Clazz.overrideMethod (c$, "getRenderHeight", 
function () {
return this.height;
});
Clazz.overrideMethod (c$, "getSlab", 
function () {
return this.slab;
});
Clazz.overrideMethod (c$, "getDepth", 
function () {
return this.depth;
});
$_M(c$, "isDisplayAntialiased", 
function () {
return this.antialiasEnabled;
});
Clazz.overrideMethod (c$, "isAntialiased", 
function () {
return this.antialiasThisFrame;
});
$_M(c$, "getChangeableColix", 
function (id, argb) {
if (id >= this.changeableColixMap.length) this.changeableColixMap = J.util.ArrayUtil.arrayCopyShort (this.changeableColixMap, id + 16);
if (this.changeableColixMap[id] == 0) this.changeableColixMap[id] = J.util.C.getColix (argb);
return (id | -32768);
}, "~N,~N");
$_M(c$, "changeColixArgb", 
function (id, argb) {
if (id < this.changeableColixMap.length && this.changeableColixMap[id] != 0) this.changeableColixMap[id] = J.util.C.getColix (argb);
}, "~N,~N");
$_M(c$, "getBgColixes", 
function (bgcolixes) {
return bgcolixes;
}, "~A");
Clazz.overrideMethod (c$, "getColorArgbOrGray", 
function (colix) {
if (colix < 0) colix = this.changeableColixMap[colix & 2047];
return (this.inGreyscaleMode ? J.util.C.getArgbGreyscale (colix) : J.util.C.getArgb (colix));
}, "~N");
$_M(c$, "getShades", 
function (colix) {
if (colix < 0) colix = this.changeableColixMap[colix & 2047];
return (this.inGreyscaleMode ? this.shader.getShadesG (colix) : this.shader.getShades (colix));
}, "~N");
$_M(c$, "setGreyscaleMode", 
function (greyscaleMode) {
this.inGreyscaleMode = greyscaleMode;
}, "~B");
$_M(c$, "getSpecularPower", 
function () {
return this.shader.specularPower;
});
$_M(c$, "setSpecularPower", 
function (val) {
if (val < 0) {
this.setSpecularExponent (-val);
return;
}if (this.shader.specularPower == val) return;
this.shader.specularPower = val;
this.shader.intenseFraction = val / 100;
this.shader.flushCaches ();
}, "~N");
$_M(c$, "getSpecularPercent", 
function () {
return this.shader.specularPercent;
});
$_M(c$, "setSpecularPercent", 
function (val) {
if (this.shader.specularPercent == val) return;
this.shader.specularPercent = val;
this.shader.specularFactor = val / 100;
this.shader.flushCaches ();
}, "~N");
$_M(c$, "getSpecularExponent", 
function () {
return this.shader.specularExponent;
});
$_M(c$, "setSpecularExponent", 
function (val) {
if (this.shader.specularExponent == val) return;
this.shader.specularExponent = val;
this.shader.phongExponent = Clazz.doubleToInt (Math.pow (2, val));
this.shader.usePhongExponent = false;
this.shader.flushCaches ();
}, "~N");
$_M(c$, "getPhongExponent", 
function () {
return this.shader.phongExponent;
});
$_M(c$, "setPhongExponent", 
function (val) {
if (this.shader.phongExponent == val && this.shader.usePhongExponent) return;
this.shader.phongExponent = val;
var x = (Math.log (val) / Math.log (2));
this.shader.usePhongExponent = (x != Clazz.floatToInt (x));
if (!this.shader.usePhongExponent) this.shader.specularExponent = Clazz.floatToInt (x);
this.shader.flushCaches ();
}, "~N");
$_M(c$, "getDiffusePercent", 
function () {
return this.shader.diffusePercent;
});
$_M(c$, "setDiffusePercent", 
function (val) {
if (this.shader.diffusePercent == val) return;
this.shader.diffusePercent = val;
this.shader.diffuseFactor = val / 100;
this.shader.flushCaches ();
}, "~N");
$_M(c$, "getAmbientPercent", 
function () {
return this.shader.ambientPercent;
});
$_M(c$, "setAmbientPercent", 
function (val) {
if (this.shader.ambientPercent == val) return;
this.shader.ambientPercent = val;
this.shader.ambientFraction = val / 100;
this.shader.flushCaches ();
}, "~N");
$_M(c$, "getSpecular", 
function () {
return this.shader.specularOn;
});
$_M(c$, "setSpecular", 
function (val) {
if (this.shader.specularOn == val) return;
this.shader.specularOn = val;
this.shader.flushCaches ();
}, "~B");
$_M(c$, "setCel", 
function (val) {
this.shader.setCel (val, this.bgcolor);
}, "~B");
$_M(c$, "getCel", 
function () {
return this.shader.getCelOn ();
});
$_M(c$, "getLightSource", 
function () {
return this.shader.lightSource;
});
$_M(c$, "isClipped3", 
function (x, y, z) {
return (x < 0 || x >= this.width || y < 0 || y >= this.height || z < this.slab || z > this.depth);
}, "~N,~N,~N");
$_M(c$, "isClipped", 
function (x, y) {
return (x < 0 || x >= this.width || y < 0 || y >= this.height);
}, "~N,~N");
Clazz.overrideMethod (c$, "isInDisplayRange", 
function (x, y) {
return (x >= this.displayMinX && x < this.displayMaxX && y >= this.displayMinY && y < this.displayMaxY);
}, "~N,~N");
Clazz.overrideMethod (c$, "isClippedXY", 
function (diameter, x, y) {
var r = (diameter + 1) >> 1;
return (x < -r || x >= this.width + r || y < -r || y >= this.height + r);
}, "~N,~N,~N");
Clazz.overrideMethod (c$, "isClippedZ", 
function (z) {
return (z != -2147483648 && (z < this.slab || z > this.depth));
}, "~N");
$_M(c$, "clipCode3", 
function (x, y, z) {
var code = 0;
if (x < 0) code |= 8;
 else if (x >= this.width) code |= 4;
if (y < 0) code |= 2;
 else if (y >= this.height) code |= 1;
if (z < this.slab) code |= 32;
 else if (z > this.depth) code |= 16;
return code;
}, "~N,~N,~N");
$_M(c$, "clipCode", 
function (z) {
var code = 0;
if (z < this.slab) code |= 32;
 else if (z > this.depth) code |= 16;
return code;
}, "~N");
$_M(c$, "getFont3D", 
function (fontSize) {
return J.util.JmolFont.createFont3D (0, 0, fontSize, fontSize, this.apiPlatform, this.graphicsForMetrics);
}, "~N");
$_M(c$, "getFont3DFS", 
function (fontFace, fontSize) {
return J.util.JmolFont.createFont3D (J.util.JmolFont.getFontFaceID (fontFace), 0, fontSize, fontSize, this.apiPlatform, this.graphicsForMetrics);
}, "~S,~N");
$_M(c$, "getFontFidFS", 
function (fontFace, fontSize) {
return this.getFont3DFSS (fontFace, "Bold", fontSize).fid;
}, "~S,~N");
$_M(c$, "getFont3DFSS", 
function (fontFace, fontStyle, fontSize) {
var iStyle = J.util.JmolFont.getFontStyleID (fontStyle);
if (iStyle < 0) iStyle = 0;
return J.util.JmolFont.createFont3D (J.util.JmolFont.getFontFaceID (fontFace), iStyle, fontSize, fontSize, this.apiPlatform, this.graphicsForMetrics);
}, "~S,~S,~N");
Clazz.overrideMethod (c$, "getFont3DScaled", 
function (font, scale) {
var newScale = font.fontSizeNominal * scale;
return (newScale == font.fontSize ? font : J.util.JmolFont.createFont3D (font.idFontFace, font.idFontStyle, newScale, font.fontSizeNominal, this.apiPlatform, this.graphicsForMetrics));
}, "J.util.JmolFont,~N");
Clazz.overrideMethod (c$, "getFontFid", 
function (fontSize) {
return this.getFont3D (fontSize).fid;
}, "~N");
c$.getFontStyleID = $_M(c$, "getFontStyleID", 
function (fontStyle) {
return J.util.JmolFont.getFontStyleID (fontStyle);
}, "~S");
$_M(c$, "setBackgroundTransparent", 
function (TF) {
}, "~B");
$_M(c$, "setBackgroundArgb", 
function (argb) {
this.bgcolor = argb;
this.setCel (this.getCel ());
}, "~N");
$_M(c$, "setBackgroundImage", 
function (image) {
this.backgroundImage = image;
}, "~O");
$_M(c$, "setWindowParameters", 
function (width, height, antialias) {
this.setWinParams (width, height, antialias);
}, "~N,~N,~B");
$_M(c$, "setWinParams", 
function (width, height, antialias) {
this.newWindowWidth = width;
this.newWindowHeight = height;
this.newAntialiasing = antialias;
}, "~N,~N,~B");
$_M(c$, "setNewWindowParametersForExport", 
function () {
this.windowWidth = this.newWindowWidth;
this.windowHeight = this.newWindowHeight;
this.setWidthHeight (false);
});
$_M(c$, "setWidthHeight", 
function (isAntialiased) {
this.width = this.windowWidth;
this.height = this.windowHeight;
if (isAntialiased) {
this.width <<= 1;
this.height <<= 1;
}this.xLast = this.width - 1;
this.yLast = this.height - 1;
this.displayMinX = -(this.width >> 1);
this.displayMaxX = this.width - this.displayMinX;
this.displayMinY = -(this.height >> 1);
this.displayMaxY = this.height - this.displayMinY;
this.bufferSize = this.width * this.height;
}, "~B");
$_M(c$, "beginRendering", 
function (stereoRotationMatrix, translucentMode, isImageWrite, renderLow) {
}, "J.util.Matrix3f,~B,~B,~B");
$_M(c$, "endRendering", 
function () {
});
$_M(c$, "snapshotAnaglyphChannelBytes", 
function () {
});
$_M(c$, "getScreenImage", 
function (isImageWrite) {
return null;
}, "~B");
$_M(c$, "releaseScreenImage", 
function () {
});
$_M(c$, "applyAnaglygh", 
function (stereoMode, stereoColors) {
}, "J.constant.EnumStereoMode,~A");
$_M(c$, "setPass2", 
function (antialias) {
return false;
}, "~B");
$_M(c$, "destroy", 
function () {
});
$_M(c$, "clearFontCache", 
function () {
});
$_M(c$, "plotImage", 
function (x, y, z, image, jmolRenderer, bgcolix, width, height) {
}, "~N,~N,~N,java.awt.Image,J.api.JmolRendererInterface,~N,~N,~N");
$_M(c$, "plotText", 
function (x, y, z, colorArgbOrGray, bgColor, text, font3d, jmolRenderer) {
}, "~N,~N,~N,~N,~N,~S,J.util.JmolFont,J.api.JmolRendererInterface");
$_M(c$, "renderBackground", 
function (jmolRenderer) {
}, "J.api.JmolRendererInterface");
$_M(c$, "getFont3DCurrent", 
function () {
return null;
});
$_M(c$, "setFont", 
function (font3d) {
}, "J.util.JmolFont");
$_M(c$, "setFontFid", 
function (fid) {
}, "~N");
$_M(c$, "setColor", 
function (color) {
this.argbCurrent = color;
}, "~N");
$_M(c$, "isPass2", 
function () {
return this.$isPass2;
});
$_M(c$, "setColix", 
function (colix) {
return true;
}, "~N");
$_M(c$, "isDirectedTowardsCamera", 
function (normix) {
return true;
}, "~N");
$_M(c$, "getTransformedVertexVectors", 
function () {
return null;
});
$_M(c$, "setNoisySurfaceShade", 
function (pointA, pointB, pointC) {
}, "J.util.P3i,J.util.P3i,J.util.P3i");
c$.roundInt = $_M(c$, "roundInt", 
function (a) {
{
return a;
}}, "~N");
$_M(c$, "clear", 
function () {
});
Clazz.overrideMethod (c$, "renderAllStrings", 
function (jmolRenderer) {
}, "~O");
c$.getScreenOctant = $_M(c$, "getScreenOctant", 
function (pt) {
var i = 0;
if (pt.x < 0) i |= 1;
if (pt.y < 0) i |= 2;
if (pt.z < 0) i |= 4;
return i;
}, "J.util.P3");
Clazz.defineStatics (c$,
"ENDCAPS_NONE", 0,
"ENDCAPS_OPEN", 1,
"ENDCAPS_FLAT", 2,
"ENDCAPS_SPHERICAL", 3,
"ENDCAPS_OPENEND", 4,
"EXPORT_RAYTRACER", 2,
"EXPORT_CARTESIAN", 1,
"EXPORT_NOT", 0,
"yGT", 1,
"yLT", 2,
"xGT", 4,
"xLT", 8,
"zGT", 16,
"zLT", 32);
});
