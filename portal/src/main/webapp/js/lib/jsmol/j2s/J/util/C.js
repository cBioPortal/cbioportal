Clazz.declarePackage ("J.util");
Clazz.load (["J.util.Int2IntHash"], "J.util.C", ["java.lang.Float", "J.constant.EnumPalette", "J.util.ArrayUtil", "$.ColorUtil", "$.Escape", "$.Logger", "$.Parser", "$.SB"], function () {
c$ = Clazz.declareType (J.util, "C");
Clazz.makeConstructor (c$, 
function () {
});
c$.getColix = $_M(c$, "getColix", 
function (argb) {
if (argb == 0) return 0;
var translucentFlag = 0;
if ((argb & 0xFF000000) != (-16777216)) {
translucentFlag = J.util.C.getTranslucentFlag ((argb >> 24) & 0xFF);
argb |= 0xFF000000;
}var c = J.util.C.colixHash.get (argb);
if ((c & 3) == 3) translucentFlag = 0;
return ((c > 0 ? c : J.util.C.allocateColix (argb)) | translucentFlag);
}, "~N");
c$.allocateColix = $_M(c$, "allocateColix", 
function (argb) {
for (var i = J.util.C.colixMax; --i >= 4; ) if ((argb & 0xFFFFFF) == (J.util.C.argbs[i] & 0xFFFFFF)) return i;

if (J.util.C.colixMax == J.util.C.argbs.length) {
var oldSize = J.util.C.colixMax;
var newSize = oldSize * 2;
if (newSize > 2048) newSize = 2048;
($t$ = J.util.C.argbs = J.util.ArrayUtil.arrayCopyI (J.util.C.argbs, newSize), J.util.C.prototype.argbs = J.util.C.argbs, $t$);
if (J.util.C.argbsGreyscale != null) ($t$ = J.util.C.argbsGreyscale = J.util.ArrayUtil.arrayCopyI (J.util.C.argbsGreyscale, newSize), J.util.C.prototype.argbsGreyscale = J.util.C.argbsGreyscale, $t$);
}J.util.C.argbs[J.util.C.colixMax] = argb;
if (J.util.C.argbsGreyscale != null) J.util.C.argbsGreyscale[J.util.C.colixMax] = J.util.ColorUtil.calcGreyscaleRgbFromRgb (argb);
J.util.C.colixHash.put (argb, J.util.C.colixMax);
return (J.util.C.colixMax < 2047 ? ($t$ = J.util.C.colixMax ++, J.util.C.prototype.colixMax = J.util.C.colixMax, $t$) : J.util.C.colixMax);
}, "~N");
c$.setLastGrey = $_M(c$, "setLastGrey", 
function (argb) {
J.util.C.calcArgbsGreyscale ();
J.util.C.argbsGreyscale[2047] = J.util.ColorUtil.calcGreyscaleRgbFromRgb (argb);
}, "~N");
c$.calcArgbsGreyscale = $_M(c$, "calcArgbsGreyscale", 
function () {
if (J.util.C.argbsGreyscale != null) return;
var a =  Clazz.newIntArray (J.util.C.argbs.length, 0);
for (var i = J.util.C.argbs.length; --i >= 4; ) a[i] = J.util.ColorUtil.calcGreyscaleRgbFromRgb (J.util.C.argbs[i]);

($t$ = J.util.C.argbsGreyscale = a, J.util.C.prototype.argbsGreyscale = J.util.C.argbsGreyscale, $t$);
});
c$.getArgbGreyscale = $_M(c$, "getArgbGreyscale", 
function (colix) {
if (J.util.C.argbsGreyscale == null) J.util.C.calcArgbsGreyscale ();
return J.util.C.argbsGreyscale[colix & -30721];
}, "~N");
c$.getColixO = $_M(c$, "getColixO", 
function (obj) {
if (obj == null) return 0;
if (Clazz.instanceOf (obj, J.constant.EnumPalette)) return ((obj) === J.constant.EnumPalette.NONE ? 0 : 2);
if (Clazz.instanceOf (obj, Integer)) return J.util.C.getColix ((obj).intValue ());
if (Clazz.instanceOf (obj, String)) return J.util.C.getColixS (obj);
if (Clazz.instanceOf (obj, Byte)) return ((obj).byteValue () == 0 ? 0 : 2);
if (J.util.Logger.debugging) {
J.util.Logger.debug ("?? getColix(" + obj + ")");
}return 22;
}, "~O");
c$.getTranslucentFlag = $_M(c$, "getTranslucentFlag", 
($fz = function (translucentLevel) {
if (translucentLevel == 0) return 0;
if (translucentLevel < 0) return 30720;
if (Float.isNaN (translucentLevel) || translucentLevel >= 255 || translucentLevel == 1.0) return 16384;
var iLevel = Clazz.doubleToInt (Math.floor (translucentLevel < 1 ? translucentLevel * 256 : translucentLevel <= 9 ? (Clazz.doubleToInt (Math.floor (translucentLevel - 1))) << 5 : translucentLevel < 15 ? 256 : translucentLevel));
return (((iLevel >> 5) & 0xF) << 11);
}, $fz.isPrivate = true, $fz), "~N");
c$.isColixLastAvailable = $_M(c$, "isColixLastAvailable", 
function (colix) {
return (colix > 0 && (colix & 2047) == 2047);
}, "~N");
c$.getArgb = $_M(c$, "getArgb", 
function (colix) {
return J.util.C.argbs[colix & -30721];
}, "~N");
c$.isColixColorInherited = $_M(c$, "isColixColorInherited", 
function (colix) {
switch (colix) {
case 0:
case 1:
return true;
default:
return (colix & -30721) == 1;
}
}, "~N");
c$.getColixInherited = $_M(c$, "getColixInherited", 
function (myColix, parentColix) {
switch (myColix) {
case 0:
return parentColix;
case 1:
return (parentColix & -30721);
default:
return ((myColix & -30721) == 1 ? (parentColix & -30721 | myColix & 30720) : myColix);
}
}, "~N,~N");
c$.isColixTranslucent = $_M(c$, "isColixTranslucent", 
function (colix) {
return ((colix & 30720) != 0);
}, "~N");
c$.getChangeableColixIndex = $_M(c$, "getChangeableColixIndex", 
function (colix) {
return (colix >= 0 ? -1 : (colix & 2047));
}, "~N");
c$.getColixTranslucent3 = $_M(c$, "getColixTranslucent3", 
function (colix, isTranslucent, translucentLevel) {
colix &= -30721;
if (colix == 0) colix = 1;
return (isTranslucent ? (colix | J.util.C.getTranslucentFlag (translucentLevel)) : colix);
}, "~N,~B,~N");
c$.copyColixTranslucency = $_M(c$, "copyColixTranslucency", 
function (colixFrom, colixTo) {
return J.util.C.getColixTranslucent3 (colixTo, J.util.C.isColixTranslucent (colixFrom), J.util.C.getColixTranslucencyLevel (colixFrom));
}, "~N,~N");
c$.getColixTranslucencyFractional = $_M(c$, "getColixTranslucencyFractional", 
function (colix) {
var translevel = J.util.C.getColixTranslucencyLevel (colix);
return (translevel == -1 ? 0.5 : translevel == 0 ? 0 : translevel == 255 ? 1 : translevel / 256);
}, "~N");
c$.getColixTranslucencyLevel = $_M(c$, "getColixTranslucencyLevel", 
function (colix) {
var logAlpha = (colix >> 11) & 0xF;
switch (logAlpha) {
case 0:
return 0;
case 1:
case 2:
case 3:
case 4:
case 5:
case 6:
case 7:
return logAlpha << 5;
case 15:
return -1;
default:
return 255;
}
}, "~N");
c$.getColixS = $_M(c$, "getColixS", 
function (colorName) {
var argb = J.util.ColorUtil.getArgbFromString (colorName);
if (argb != 0) return J.util.C.getColix (argb);
if ("none".equalsIgnoreCase (colorName)) return 0;
if ("opaque".equalsIgnoreCase (colorName)) return 1;
return 2;
}, "~S");
c$.getColixArray = $_M(c$, "getColixArray", 
function (colorNames) {
if (colorNames == null || colorNames.length == 0) return null;
var colors = J.util.Parser.getTokens (colorNames);
var colixes =  Clazz.newShortArray (colors.length, 0);
for (var j = 0; j < colors.length; j++) {
colixes[j] = J.util.C.getColix (J.util.ColorUtil.getArgbFromString (colors[j]));
if (colixes[j] == 0) return null;
}
return colixes;
}, "~S");
c$.getHexCode = $_M(c$, "getHexCode", 
function (colix) {
return J.util.Escape.escapeColor (J.util.C.getArgb (colix));
}, "~N");
c$.getHexCodes = $_M(c$, "getHexCodes", 
function (colixes) {
if (colixes == null) return null;
var s =  new J.util.SB ();
for (var i = 0; i < colixes.length; i++) s.append (i == 0 ? "" : " ").append (J.util.C.getHexCode (colixes[i]));

return s.toString ();
}, "~A");
c$.getColixTranslucent = $_M(c$, "getColixTranslucent", 
function (argb) {
var a = (argb >> 24) & 0xFF;
return (a == 0xFF ? J.util.C.getColix (argb) : J.util.C.getColixTranslucent3 (J.util.C.getColix (argb), true, a / 255));
}, "~N");
Clazz.defineStatics (c$,
"INHERIT_ALL", 0,
"INHERIT_COLOR", 1,
"USE_PALETTE", 2,
"RAW_RGB", 3,
"SPECIAL_COLIX_MAX", 4,
"colixMax", 4,
"argbs",  Clazz.newIntArray (128, 0),
"argbsGreyscale", null);
c$.colixHash = c$.prototype.colixHash =  new J.util.Int2IntHash (256);
Clazz.defineStatics (c$,
"RAW_RGB_INT", 3,
"UNMASK_CHANGEABLE_TRANSLUCENT", 0x07FF,
"CHANGEABLE_MASK", 0x8000,
"LAST_AVAILABLE_COLIX", 2047,
"TRANSLUCENT_SHIFT", 11,
"ALPHA_SHIFT", 13,
"TRANSLUCENT_MASK", 30720,
"TRANSLUCENT_SCREENED", 30720,
"TRANSPARENT", 16384,
"OPAQUE_MASK", -30721,
"BLACK", 4,
"ORANGE", 5,
"PINK", 6,
"BLUE", 7,
"WHITE", 8,
"CYAN", 9,
"RED", 10,
"GREEN", 11,
"GRAY", 12,
"SILVER", 13,
"LIME", 14,
"MAROON", 15,
"NAVY", 16,
"OLIVE", 17,
"PURPLE", 18,
"TEAL", 19,
"MAGENTA", 20,
"YELLOW", 21,
"HOTPINK", 22,
"GOLD", 23,
"predefinedArgbs", [0xFF000000, 0xFFFFA500, 0xFFFFC0CB, 0xFF0000FF, 0xFFFFFFFF, 0xFF00FFFF, 0xFFFF0000, 0xFF008000, 0xFF808080, 0xFFC0C0C0, 0xFF00FF00, 0xFF800000, 0xFF000080, 0xFF808000, 0xFF800080, 0xFF008080, 0xFFFF00FF, 0xFFFFFF00, 0xFFFF69B4, 0xFFFFD700]);
{
for (var i = 0; i < J.util.C.predefinedArgbs.length; ++i) J.util.C.getColix (J.util.C.predefinedArgbs[i]);

}});
