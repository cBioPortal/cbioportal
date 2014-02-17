Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.JmolFont", ["J.util.ArrayUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.fid = 0;
this.fontFace = null;
this.fontStyle = null;
this.fontSizeNominal = 0;
this.idFontFace = 0;
this.idFontStyle = 0;
this.fontSize = 0;
this.font = null;
this.fontMetrics = null;
this.apiPlatform = null;
this.ascent = 0;
this.descent = 0;
this.isBold = false;
this.isItalic = false;
Clazz.instantialize (this, arguments);
}, J.util, "JmolFont");
Clazz.makeConstructor (c$, 
($fz = function (apiPlatform, fid, idFontFace, idFontStyle, fontSize, fontSizeNominal, graphics) {
this.apiPlatform = apiPlatform;
this.fid = fid;
this.fontFace = J.util.JmolFont.fontFaces[idFontFace];
this.fontStyle = J.util.JmolFont.fontStyles[idFontStyle];
this.idFontFace = idFontFace;
this.idFontStyle = idFontStyle;
this.fontSize = fontSize;
this.isBold = (idFontStyle & 1) == 1;
this.isItalic = (idFontStyle & 2) == 2;
this.fontSizeNominal = fontSizeNominal;
this.font = apiPlatform.newFont (J.util.JmolFont.fontFaces[idFontFace], this.isBold, this.isItalic, fontSize);
this.fontMetrics = apiPlatform.getFontMetrics (this, graphics);
this.descent = apiPlatform.getFontDescent (this.fontMetrics);
this.ascent = apiPlatform.getFontAscent (this.fontMetrics);
}, $fz.isPrivate = true, $fz), "J.api.ApiPlatform,~N,~N,~N,~N,~N,~O");
c$.getFont3D = $_M(c$, "getFont3D", 
function (fontID) {
return J.util.JmolFont.font3ds[fontID & 0xFF];
}, "~N");
c$.createFont3D = $_M(c$, "createFont3D", 
function (fontface, fontstyle, fontsize, fontsizeNominal, apiPlatform, graphicsForMetrics) {
if (fontsize > 0xFF) fontsize = 0xFF;
var fontsizeX16 = (Clazz.floatToInt (fontsize)) << 4;
var fontkey = ((fontface & 3) | ((fontstyle & 3) << 2) | (fontsizeX16 << 4));
for (var i = J.util.JmolFont.fontkeyCount; --i > 0; ) if (fontkey == J.util.JmolFont.fontkeys[i] && J.util.JmolFont.font3ds[i].fontSizeNominal == fontsizeNominal) return J.util.JmolFont.font3ds[i];

var fontIndexNext = ($t$ = J.util.JmolFont.fontkeyCount ++, J.util.JmolFont.prototype.fontkeyCount = J.util.JmolFont.fontkeyCount, $t$);
if (fontIndexNext == J.util.JmolFont.fontkeys.length) ($t$ = J.util.JmolFont.fontkeys = J.util.ArrayUtil.arrayCopyI (J.util.JmolFont.fontkeys, fontIndexNext + 8), J.util.JmolFont.prototype.fontkeys = J.util.JmolFont.fontkeys, $t$);
($t$ = J.util.JmolFont.font3ds = J.util.ArrayUtil.arrayCopyObject (J.util.JmolFont.font3ds, fontIndexNext + 8), J.util.JmolFont.prototype.font3ds = J.util.JmolFont.font3ds, $t$);
var font3d =  new J.util.JmolFont (apiPlatform, fontIndexNext, fontface, fontstyle, fontsize, fontsizeNominal, graphicsForMetrics);
J.util.JmolFont.font3ds[fontIndexNext] = font3d;
J.util.JmolFont.fontkeys[fontIndexNext] = fontkey;
return font3d;
}, "~N,~N,~N,~N,J.api.ApiPlatform,~O");
c$.getFontFaceID = $_M(c$, "getFontFaceID", 
function (fontface) {
return ("Monospaced".equalsIgnoreCase (fontface) ? 2 : "Serif".equalsIgnoreCase (fontface) ? 1 : 0);
}, "~S");
c$.getFontStyleID = $_M(c$, "getFontStyleID", 
function (fontstyle) {
for (var i = 4; --i >= 0; ) if (J.util.JmolFont.fontStyles[i].equalsIgnoreCase (fontstyle)) return i;

return -1;
}, "~S");
$_M(c$, "getAscent", 
function () {
return this.ascent;
});
$_M(c$, "getDescent", 
function () {
return this.descent;
});
$_M(c$, "getHeight", 
function () {
return this.getAscent () + this.getDescent ();
});
$_M(c$, "stringWidth", 
function (text) {
return this.apiPlatform.fontStringWidth (this, this.fontMetrics, text);
}, "~S");
$_M(c$, "getInfo", 
function () {
return this.fontSizeNominal + " " + this.fontFace + " " + this.fontStyle;
});
Clazz.defineStatics (c$,
"FONT_ALLOCATION_UNIT", 8,
"fontkeyCount", 1,
"fontkeys",  Clazz.newIntArray (8, 0));
c$.font3ds = c$.prototype.font3ds =  new Array (8);
Clazz.defineStatics (c$,
"FONT_FACE_SANS", 0,
"FONT_FACE_SERIF", 1,
"FONT_FACE_MONO", 2,
"fontFaces", ["SansSerif", "Serif", "Monospaced", ""],
"FONT_STYLE_PLAIN", 0,
"FONT_STYLE_BOLD", 1,
"FONT_STYLE_ITALIC", 2,
"FONT_STYLE_BOLDITALIC", 3,
"fontStyles", ["Plain", "Bold", "Italic", "BoldItalic"]);
});
