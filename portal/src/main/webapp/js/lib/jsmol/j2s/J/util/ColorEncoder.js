Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.ColorEncoder", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "J.constant.EnumPalette", "J.util.ArrayUtil", "$.C", "$.ColorUtil", "$.Escape", "$.JmolList", "$.Logger", "$.TextFormat", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.paletteBW = null;
this.paletteWB = null;
this.argbsCpk = null;
this.argbsRoygb = null;
this.argbsRwb = null;
this.argbsShapely = null;
this.argbsAmino = null;
this.ihalf = 0;
this.schemes = null;
this.currentPalette = 0;
this.currentSegmentCount = 1;
this.isTranslucent = false;
this.lo = 0;
this.hi = 0;
this.isReversed = false;
this.userScale = null;
this.thisScale = null;
this.thisName = "scheme";
this.isColorIndex = false;
this.propertyColorEncoder = null;
Clazz.instantialize (this, arguments);
}, J.util, "ColorEncoder");
Clazz.prepareFields (c$, function () {
this.userScale = [-8355712];
this.thisScale = [-8355712];
});
Clazz.makeConstructor (c$, 
function (propertyColorEncoder) {
if (propertyColorEncoder == null) {
this.schemes =  new java.util.Hashtable ();
this.argbsCpk = J.constant.EnumPalette.argbsCpk;
this.argbsRoygb = J.viewer.JC.argbsRoygbScale;
this.argbsRwb = J.viewer.JC.argbsRwbScale;
this.argbsShapely = J.viewer.JC.argbsShapely;
this.argbsAmino = J.viewer.JC.argbsAmino;
this.ihalf = Clazz.doubleToInt (J.viewer.JC.argbsRoygbScale.length / 3);
this.propertyColorEncoder = this;
} else {
this.propertyColorEncoder = propertyColorEncoder;
this.schemes = propertyColorEncoder.schemes;
}}, "J.util.ColorEncoder");
c$.getSchemeIndex = $_M(c$, "getSchemeIndex", 
($fz = function (colorScheme) {
for (var i = 0; i < J.util.ColorEncoder.colorSchemes.length; i++) if (J.util.ColorEncoder.colorSchemes[i].equalsIgnoreCase (colorScheme)) return (i >= 15 ? i - 15 : i < 12 ? i : -i);

return -1;
}, $fz.isPrivate = true, $fz), "~S");
c$.fixName = $_M(c$, "fixName", 
($fz = function (name) {
if (name.equalsIgnoreCase ("byelement")) return "byelement_jmol";
var ipt = J.util.ColorEncoder.getSchemeIndex (name);
return (ipt >= 0 ? J.util.ColorEncoder.colorSchemes[ipt] : name.toLowerCase ());
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "makeColorScheme", 
($fz = function (name, scale, isOverloaded) {
name = J.util.ColorEncoder.fixName (name);
if (scale == null) {
this.schemes.remove (name);
var iScheme = this.createColorScheme (name, false, isOverloaded);
if (isOverloaded) switch (iScheme) {
case 10:
this.paletteBW = this.getPaletteBW ();
break;
case 11:
this.paletteWB = this.getPaletteWB ();
break;
case 0:
case 1:
this.argbsRoygb = J.viewer.JC.argbsRoygbScale;
break;
case 6:
case 7:
this.argbsRwb = J.viewer.JC.argbsRwbScale;
break;
case 2:
this.argbsCpk = J.constant.EnumPalette.argbsCpk;
break;
case 3:
J.util.ColorEncoder.getRasmolScale ();
break;
case 5:
this.argbsAmino = J.viewer.JC.argbsAmino;
break;
case 4:
this.argbsShapely = J.viewer.JC.argbsShapely;
break;
}
return (iScheme == 2147483647 ? 0 : iScheme);
}this.schemes.put (name, scale);
this.setThisScheme (name, scale);
var iScheme = this.createColorScheme (name, false, isOverloaded);
if (isOverloaded) switch (iScheme) {
case 10:
this.paletteBW = this.thisScale;
break;
case 11:
this.paletteWB = this.thisScale;
break;
case 0:
case 1:
this.argbsRoygb = this.thisScale;
this.ihalf = Clazz.doubleToInt (this.argbsRoygb.length / 3);
break;
case 6:
case 7:
this.argbsRwb = this.thisScale;
break;
case 2:
this.argbsCpk = this.thisScale;
break;
case 3:
break;
case 5:
this.argbsAmino = this.thisScale;
break;
case 4:
this.argbsShapely = this.thisScale;
break;
}
return -1;
}, $fz.isPrivate = true, $fz), "~S,~A,~B");
$_M(c$, "createColorScheme", 
function (colorScheme, defaultToRoygb, isOverloaded) {
colorScheme = colorScheme.toLowerCase ();
if (colorScheme.equals ("inherit")) return 14;
var pt = Math.max (colorScheme.indexOf ("="), colorScheme.indexOf ("["));
if (pt >= 0) {
var name = J.util.TextFormat.replaceAllCharacters (colorScheme.substring (0, pt), " =", "");
if (name.length > 0) isOverloaded = true;
var n = 0;
if (colorScheme.length > pt + 1 && !colorScheme.contains ("[")) {
colorScheme = "[" + colorScheme.substring (pt + 1).trim () + "]";
colorScheme = J.util.TextFormat.simpleReplace (colorScheme.$replace ('\n', ' '), "  ", " ");
colorScheme = J.util.TextFormat.simpleReplace (colorScheme, ", ", ",").$replace (' ', ',');
colorScheme = J.util.TextFormat.simpleReplace (colorScheme, ",", "][");
}pt = -1;
while ((pt = colorScheme.indexOf ("[", pt + 1)) >= 0) n++;

if (n == 0) return this.makeColorScheme (name, null, isOverloaded);
var scale =  Clazz.newIntArray (n, 0);
n = 0;
while ((pt = colorScheme.indexOf ("[", pt + 1)) >= 0) {
var pt2 = colorScheme.indexOf ("]", pt);
if (pt2 < 0) pt2 = colorScheme.length - 1;
var c = J.util.ColorUtil.getArgbFromString (colorScheme.substring (pt, pt2 + 1));
if (c == 0) c = J.util.ColorUtil.getArgbFromString (colorScheme.substring (pt + 1, pt2).trim ());
if (c == 0) {
J.util.Logger.error ("error in color value: " + colorScheme.substring (pt, pt2 + 1));
return 0;
}scale[n++] = c;
}
if (name.equals ("user")) {
this.setUserScale (scale);
return -12;
}return this.makeColorScheme (name, scale, isOverloaded);
}colorScheme = J.util.ColorEncoder.fixName (colorScheme);
var ipt = J.util.ColorEncoder.getSchemeIndex (colorScheme);
if (this.schemes.containsKey (colorScheme)) {
this.setThisScheme (colorScheme, this.schemes.get (colorScheme));
return ipt;
}return (ipt != -1 ? ipt : defaultToRoygb ? 0 : 2147483647);
}, "~S,~B,~B");
$_M(c$, "setUserScale", 
function (scale) {
this.propertyColorEncoder.userScale = scale;
this.makeColorScheme ("user", scale, false);
}, "~A");
$_M(c$, "getColorSchemeArray", 
function (palette) {
var b;
switch (palette) {
case -1:
return this.thisScale;
case 0:
return this.propertyColorEncoder.argbsRoygb;
case 1:
return J.util.ArrayUtil.arrayCopyRangeRevI (this.propertyColorEncoder.argbsRoygb, 0, -1);
case 8:
return J.util.ArrayUtil.arrayCopyRangeI (this.propertyColorEncoder.argbsRoygb, 0, this.propertyColorEncoder.ihalf);
case 9:
var a = J.util.ArrayUtil.arrayCopyRangeI (this.propertyColorEncoder.argbsRoygb, this.propertyColorEncoder.argbsRoygb.length - 2 * this.propertyColorEncoder.ihalf, -1);
b =  Clazz.newIntArray (this.propertyColorEncoder.ihalf, 0);
for (var i = b.length, j = a.length; --i >= 0 && --j >= 0; ) b[i] = a[j--];

return b;
case 10:
return this.getPaletteBW ();
case 11:
return this.getPaletteWB ();
case 6:
return this.propertyColorEncoder.argbsRwb;
case 7:
return J.util.ArrayUtil.arrayCopyRangeRevI (this.propertyColorEncoder.argbsRwb, 0, -1);
case 2:
return this.propertyColorEncoder.argbsCpk;
case 3:
return J.util.ColorEncoder.getRasmolScale ();
case 4:
return this.propertyColorEncoder.argbsShapely;
case 5:
return this.propertyColorEncoder.argbsAmino;
case -12:
return this.propertyColorEncoder.userScale;
case -13:
return J.util.ArrayUtil.arrayCopyRangeRevI (this.propertyColorEncoder.userScale, 0, -1);
default:
return null;
}
}, "~N");
$_M(c$, "getColorIndexFromPalette", 
function (val, lo, hi, palette, isTranslucent) {
var colix = J.util.C.getColix (this.getArgbFromPalette (val, lo, hi, palette));
if (isTranslucent) {
var f = (hi - val) / (hi - lo);
if (f > 1) f = 1;
 else if (f < 0.125) f = 0.125;
colix = J.util.C.getColixTranslucent3 (colix, true, f);
}return colix;
}, "~N,~N,~N,~N,~B");
$_M(c$, "getPaletteColorCount", 
function (palette) {
switch (palette) {
case -1:
return this.thisScale.length;
case 10:
case 11:
this.getPaletteBW ();
return this.propertyColorEncoder.paletteBW.length;
case 0:
case 1:
return this.propertyColorEncoder.argbsRoygb.length;
case 8:
case 9:
return this.propertyColorEncoder.ihalf;
case 6:
case 7:
return this.propertyColorEncoder.argbsRwb.length;
case -12:
case -13:
return this.propertyColorEncoder.userScale.length;
case 2:
return this.argbsCpk.length;
case 3:
return J.util.ColorEncoder.getRasmolScale ().length;
case 4:
return this.propertyColorEncoder.argbsShapely.length;
case 5:
return this.propertyColorEncoder.argbsAmino.length;
default:
return 0;
}
}, "~N");
$_M(c$, "getArgbFromPalette", 
function (val, lo, hi, palette) {
if (Float.isNaN (val)) return -8355712;
var n = this.getPaletteColorCount (palette);
switch (palette) {
case -1:
if (this.isColorIndex) {
lo = 0;
hi = this.thisScale.length;
}return this.thisScale[J.util.ColorEncoder.quantize (val, lo, hi, n)];
case 10:
return this.getPaletteBW ()[J.util.ColorEncoder.quantize (val, lo, hi, n)];
case 11:
return this.getPaletteWB ()[J.util.ColorEncoder.quantize (val, lo, hi, n)];
case 0:
return this.propertyColorEncoder.argbsRoygb[J.util.ColorEncoder.quantize (val, lo, hi, n)];
case 1:
return this.propertyColorEncoder.argbsRoygb[J.util.ColorEncoder.quantize (-val, -hi, -lo, n)];
case 8:
return this.propertyColorEncoder.argbsRoygb[J.util.ColorEncoder.quantize (val, lo, hi, n)];
case 9:
return this.propertyColorEncoder.argbsRoygb[this.propertyColorEncoder.ihalf + J.util.ColorEncoder.quantize (val, lo, hi, n) * 2];
case 6:
return this.propertyColorEncoder.argbsRwb[J.util.ColorEncoder.quantize (val, lo, hi, n)];
case 7:
return this.propertyColorEncoder.argbsRwb[J.util.ColorEncoder.quantize (-val, -hi, -lo, n)];
case -12:
return (this.propertyColorEncoder.userScale.length == 0 ? -8355712 : this.propertyColorEncoder.userScale[J.util.ColorEncoder.quantize (val, lo, hi, n)]);
case -13:
return (this.propertyColorEncoder.userScale.length == 0 ? -8355712 : this.propertyColorEncoder.userScale[J.util.ColorEncoder.quantize (-val, -hi, -lo, n)]);
case 2:
return this.propertyColorEncoder.argbsCpk[J.util.ColorEncoder.colorIndex (val, n)];
case 3:
return J.util.ColorEncoder.getRasmolScale ()[J.util.ColorEncoder.colorIndex (val, n)];
case 4:
return this.propertyColorEncoder.argbsShapely[J.util.ColorEncoder.colorIndex (val, n)];
case 5:
return this.propertyColorEncoder.argbsAmino[J.util.ColorEncoder.colorIndex (val, n)];
default:
return -8355712;
}
}, "~N,~N,~N,~N");
$_M(c$, "setThisScheme", 
($fz = function (name, scale) {
this.thisName = name;
this.thisScale = scale;
if (name.equals ("user")) this.userScale = scale;
this.isColorIndex = (name.indexOf ("byelement") == 0 || name.indexOf ("byresidue") == 0);
}, $fz.isPrivate = true, $fz), "~S,~A");
$_M(c$, "getArgb", 
function (val) {
return (this.isReversed ? this.getArgbFromPalette (-val, -this.hi, -this.lo, this.currentPalette) : this.getArgbFromPalette (val, this.lo, this.hi, this.currentPalette));
}, "~N");
$_M(c$, "getColorIndex", 
function (val) {
return (this.isReversed ? this.getColorIndexFromPalette (-val, -this.hi, -this.lo, this.currentPalette, this.isTranslucent) : this.getColorIndexFromPalette (val, this.lo, this.hi, this.currentPalette, this.isTranslucent));
}, "~N");
$_M(c$, "getColorKey", 
function () {
var info =  new java.util.Hashtable ();
var segmentCount = this.getPaletteColorCount (this.currentPalette);
var colors =  new J.util.JmolList ();
var values =  Clazz.newFloatArray (segmentCount + 1, 0);
var quantum = (this.hi - this.lo) / segmentCount;
var f = quantum * (this.isReversed ? -0.5 : 0.5);
for (var i = 0; i < segmentCount; i++) {
values[i] = (this.isReversed ? this.hi - i * quantum : this.lo + i * quantum);
colors.addLast (J.util.ColorUtil.colorPointFromInt2 (this.getArgb (values[i] + f)));
}
values[segmentCount] = (this.isReversed ? this.lo : this.hi);
info.put ("values", values);
info.put ("colors", colors);
info.put ("min", Float.$valueOf (this.lo));
info.put ("max", Float.$valueOf (this.hi));
info.put ("reversed", Boolean.$valueOf (this.isReversed));
info.put ("name", this.getCurrentColorSchemeName ());
return info;
});
$_M(c$, "setColorScheme", 
function (colorScheme, isTranslucent) {
this.isTranslucent = isTranslucent;
if (colorScheme != null) this.currentPalette = this.createColorScheme (colorScheme, true, false);
}, "~S,~B");
$_M(c$, "setRange", 
function (lo, hi, isReversed) {
if (hi == 3.4028235E38) {
lo = 1;
hi = this.getPaletteColorCount (this.currentPalette) + 1;
}this.lo = Math.min (lo, hi);
this.hi = Math.max (lo, hi);
this.isReversed = isReversed;
}, "~N,~N,~B");
$_M(c$, "getCurrentColorSchemeName", 
function () {
return this.getColorSchemeName (this.currentPalette);
});
$_M(c$, "getColorSchemeName", 
function (i) {
var absi = Math.abs (i);
return (i == -1 ? this.thisName : absi < J.util.ColorEncoder.colorSchemes.length && absi >= 0 ? J.util.ColorEncoder.colorSchemes[absi] : null);
}, "~N");
c$.getColorSchemeList = $_M(c$, "getColorSchemeList", 
function (scheme) {
if (scheme == null) return "";
var colors = "";
for (var i = 0; i < scheme.length; i++) colors += (i == 0 ? "" : " ") + J.util.Escape.escapeColor (scheme[i]);

return colors;
}, "~A");
c$.getRasmolScale = $_M(c$, "getRasmolScale", 
function () {
if (J.util.ColorEncoder.rasmolScale != null) return J.util.ColorEncoder.rasmolScale;
($t$ = J.util.ColorEncoder.rasmolScale =  Clazz.newIntArray (J.constant.EnumPalette.argbsCpk.length, 0), J.util.ColorEncoder.prototype.rasmolScale = J.util.ColorEncoder.rasmolScale, $t$);
var argb = J.constant.EnumPalette.argbsCpkRasmol[0] | 0xFF000000;
for (var i = J.util.ColorEncoder.rasmolScale.length; --i >= 0; ) J.util.ColorEncoder.rasmolScale[i] = argb;

for (var i = J.constant.EnumPalette.argbsCpkRasmol.length; --i >= 0; ) {
argb = J.constant.EnumPalette.argbsCpkRasmol[i];
J.util.ColorEncoder.rasmolScale[argb >> 24] = argb | 0xFF000000;
}
return J.util.ColorEncoder.rasmolScale;
});
$_M(c$, "getPaletteWB", 
($fz = function () {
if (this.propertyColorEncoder.paletteWB != null) return this.propertyColorEncoder.paletteWB;
var b =  Clazz.newIntArray (J.viewer.JC.argbsRoygbScale.length, 0);
for (var i = 0; i < b.length; i++) {
var xff = (1 / b.length * (b.length - i));
b[i] = J.util.ColorUtil.colorTriadToInt (xff, xff, xff);
}
return this.propertyColorEncoder.paletteWB = b;
}, $fz.isPrivate = true, $fz));
c$.getPaletteAtoB = $_M(c$, "getPaletteAtoB", 
function (color1, color2, n) {
if (n < 2) n = J.viewer.JC.argbsRoygbScale.length;
var b =  Clazz.newIntArray (n, 0);
var red1 = (((color1 & 0xFF0000) >> 16) & 0xFF) / 255;
var green1 = (((color1 & 0xFF00) >> 8) & 0xFF) / 255;
var blue1 = (color1 & 0xFF) / 255;
var red2 = (((color2 & 0xFF0000) >> 16) & 0xFF) / 255;
var green2 = (((color2 & 0xFF00) >> 8) & 0xFF) / 255;
var blue2 = (color2 & 0xFF) / 255;
var dr = (red2 - red1) / (n - 1);
var dg = (green2 - green1) / (n - 1);
var db = (blue2 - blue1) / (n - 1);
for (var i = 0; i < n; i++) b[i] = J.util.ColorUtil.colorTriadToInt (red1 + dr * i, green1 + dg * i, blue1 + db * i);

return b;
}, "~N,~N,~N");
$_M(c$, "getPaletteBW", 
($fz = function () {
if (this.propertyColorEncoder.paletteBW != null) return this.propertyColorEncoder.paletteBW;
var b =  Clazz.newIntArray (J.viewer.JC.argbsRoygbScale.length, 0);
for (var i = 0; i < b.length; i++) {
var xff = (1 / b.length * i);
b[i] = J.util.ColorUtil.colorTriadToInt (xff, xff, xff);
}
return this.propertyColorEncoder.paletteBW = b;
}, $fz.isPrivate = true, $fz));
$_M(c$, "quantize", 
function (x, isLowEnd) {
var n = this.getPaletteColorCount (this.currentPalette);
x = ((Clazz.floatToInt (x * n)) + (isLowEnd ? 0 : 1)) / n;
return (x <= 0 ? this.lo : x >= 1 ? this.hi : this.lo + (this.hi - this.lo) * x);
}, "~N,~B");
c$.quantize = $_M(c$, "quantize", 
function (val, lo, hi, segmentCount) {
var range = hi - lo;
if (range <= 0 || Float.isNaN (val)) return Clazz.doubleToInt (segmentCount / 2);
var t = val - lo;
if (t <= 0) return 0;
var quanta = range / segmentCount;
var q = Clazz.floatToInt (t / quanta + 0.0001);
if (q >= segmentCount) q = segmentCount - 1;
return q;
}, "~N,~N,~N,~N");
c$.colorIndex = $_M(c$, "colorIndex", 
($fz = function (q, segmentCount) {
return Clazz.doubleToInt (Math.floor ( new Boolean (q <= 0 | q >= segmentCount).valueOf () ? 0 : q));
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "getColorScheme", 
function () {
return (this.isTranslucent ? "translucent " : "") + (this.currentPalette < 0 ? J.util.ColorEncoder.getColorSchemeList (this.getColorSchemeArray (this.currentPalette)) : this.getColorSchemeName (this.currentPalette));
});
c$.RGBtoHSL = $_M(c$, "RGBtoHSL", 
function (r, g, b, ret) {
r /= 255;
g /= 255;
b /= 255;
if (r > 1) r = 1;
if (g > 1) g = 1;
if (b > 1) b = 1;
var min = Math.min (r, Math.min (g, b));
var max = Math.max (r, Math.max (g, b));
var h = 0;
if (max == min) h = 0;
 else if (max == r) h = ((60 * (g - b) / (max - min)) + 360) % 360;
 else if (max == g) h = (60 * (b - r) / (max - min)) + 120;
 else if (max == b) h = (60 * (r - g) / (max - min)) + 240;
var l = (max + min) / 2;
var s = 0;
if (max == min) s = 0;
 else if (l <= .5) s = (max - min) / (max + min);
 else s = (max - min) / (2 - max - min);
ret[0] = h / 360;
ret[1] = s;
ret[2] = l;
}, "~N,~N,~N,~A");
Clazz.defineStatics (c$,
"GRAY", 0xFF808080,
"BYELEMENT_PREFIX", "byelement",
"BYRESIDUE_PREFIX", "byresidue");
c$.BYELEMENT_JMOL = c$.prototype.BYELEMENT_JMOL = "byelement_jmol";
c$.BYELEMENT_RASMOL = c$.prototype.BYELEMENT_RASMOL = "byelement_rasmol";
c$.BYRESIDUE_SHAPELY = c$.prototype.BYRESIDUE_SHAPELY = "byresidue_shapely";
c$.BYRESIDUE_AMINO = c$.prototype.BYRESIDUE_AMINO = "byresidue_amino";
Clazz.defineStatics (c$,
"CUSTOM", -1,
"ROYGB", 0,
"BGYOR", 1,
"JMOL", 2,
"RASMOL", 3,
"SHAPELY", 4,
"AMINO", 5,
"RWB", 6,
"BWR", 7,
"LOW", 8,
"HIGH", 9,
"BW", 10,
"WB", 11,
"USER", -12,
"RESU", -13,
"INHERIT", 14,
"ALT", 15);
c$.colorSchemes = c$.prototype.colorSchemes = ["roygb", "bgyor", "byelement_jmol", "byelement_rasmol", "byresidue_shapely", "byresidue_amino", "rwb", "bwr", "low", "high", "bw", "wb", "user", "resu", "inherit", "rgb", "bgr", "jmol", "rasmol", "byresidue"];
Clazz.defineStatics (c$,
"rasmolScale", null);
});
