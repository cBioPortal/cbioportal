Clazz.declarePackage ("J.util");
Clazz.load (["java.util.Hashtable"], "J.util.ColorUtil", ["J.util.P3", "$.Parser", "$.TextFormat"], function () {
c$ = Clazz.declareType (J.util, "ColorUtil");
c$.getArgbFromString = $_M(c$, "getArgbFromString", 
function (strColor) {
var len = 0;
if (strColor == null || (len = strColor.length) == 0) return 0;
if (strColor.charAt (0) == '[' && strColor.charAt (len - 1) == ']') {
var check;
if (strColor.indexOf (",") >= 0) {
var tokens = J.util.TextFormat.splitChars (strColor.substring (1, strColor.length - 1), ",");
if (tokens.length != 3) return 0;
var red = J.util.Parser.parseFloatStr (tokens[0]);
var grn = J.util.Parser.parseFloatStr (tokens[1]);
var blu = J.util.Parser.parseFloatStr (tokens[2]);
return J.util.ColorUtil.colorTriadToInt (red, grn, blu);
}switch (len) {
case 9:
check = "x";
break;
case 10:
check = "0x";
break;
default:
return 0;
}
if (strColor.indexOf (check) != 1) return 0;
strColor = "#" + strColor.substring (len - 7, len - 1);
len = 7;
}if (len == 7 && strColor.charAt (0) == '#') {
try {
return J.util.Parser.parseIntRadix (strColor.substring (1, 7), 16) | 0xFF000000;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return 0;
} else {
throw e;
}
}
}var boxedArgb = J.util.ColorUtil.mapJavaScriptColors.get (strColor.toLowerCase ());
return (boxedArgb == null ? 0 : boxedArgb.intValue ());
}, "~S");
c$.colorTriadToInt = $_M(c$, "colorTriadToInt", 
function (x, y, z) {
if (x <= 1 && y <= 1 && z <= 1) {
if (x > 0) x = x * 256 - 1;
if (y > 0) y = y * 256 - 1;
if (z > 0) z = z * 256 - 1;
}return J.util.ColorUtil.rgb (Clazz.floatToInt (x), Clazz.floatToInt (y), Clazz.floatToInt (z));
}, "~N,~N,~N");
c$.rgb = $_M(c$, "rgb", 
function (red, grn, blu) {
return 0xFF000000 | (red << 16) | (grn << 8) | blu;
}, "~N,~N,~N");
c$.colorPointFromString = $_M(c$, "colorPointFromString", 
function (colorName, pt) {
return J.util.ColorUtil.colorPointFromInt (J.util.ColorUtil.getArgbFromString (colorName), pt);
}, "~S,J.util.P3");
c$.colorPointFromInt2 = $_M(c$, "colorPointFromInt2", 
function (color) {
return J.util.P3.new3 ((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
}, "~N");
c$.colorPtToInt = $_M(c$, "colorPtToInt", 
function (pt) {
return J.util.ColorUtil.colorTriadToInt (pt.x, pt.y, pt.z);
}, "J.util.Tuple3f");
c$.colorPointFromInt = $_M(c$, "colorPointFromInt", 
function (color, pt) {
pt.z = color & 0xFF;
pt.y = (color >> 8) & 0xFF;
pt.x = (color >> 16) & 0xFF;
return pt;
}, "~N,J.util.P3");
c$.calcGreyscaleRgbFromRgb = $_M(c$, "calcGreyscaleRgbFromRgb", 
function (rgb) {
var grey = (Clazz.doubleToInt (((2989 * ((rgb >> 16) & 0xFF)) + (5870 * ((rgb >> 8) & 0xFF)) + (1140 * (rgb & 0xFF)) + 5000) / 10000)) & 0xFFFFFF;
return J.util.ColorUtil.rgb (grey, grey, grey);
}, "~N");
c$.getBgContrast = $_M(c$, "getBgContrast", 
function (argb) {
return ((J.util.ColorUtil.calcGreyscaleRgbFromRgb (argb) & 0xFF) < 128 ? 8 : 4);
}, "~N");
Clazz.defineStatics (c$,
"colorNames", ["black", "pewhite", "pecyan", "pepurple", "pegreen", "peblue", "peviolet", "pebrown", "pepink", "peyellow", "pedarkgreen", "peorange", "pelightblue", "pedarkcyan", "pedarkgray", "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray", "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro", "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "honeydew", "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgreen", "lightgrey", "lightpink", "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray", "snow", "springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen", "bluetint", "greenblue", "greentint", "grey", "pinktint", "redorange", "yellowtint"],
"colorArgbs", [0xFF000000, 0xFFffffff, 0xFF00ffff, 0xFFd020ff, 0xFF00ff00, 0xFF6060ff, 0xFFff80c0, 0xFFa42028, 0xFFffd8d8, 0xFFffff00, 0xFF00c000, 0xFFffb000, 0xFFb0b0ff, 0xFF00a0a0, 0xFF606060, 0xFFF0F8FF, 0xFFFAEBD7, 0xFF00FFFF, 0xFF7FFFD4, 0xFFF0FFFF, 0xFFF5F5DC, 0xFFFFE4C4, 0xFFFFEBCD, 0xFF0000FF, 0xFF8A2BE2, 0xFFA52A2A, 0xFFDEB887, 0xFF5F9EA0, 0xFF7FFF00, 0xFFD2691E, 0xFFFF7F50, 0xFF6495ED, 0xFFFFF8DC, 0xFFDC143C, 0xFF00FFFF, 0xFF00008B, 0xFF008B8B, 0xFFB8860B, 0xFFA9A9A9, 0xFF006400, 0xFFBDB76B, 0xFF8B008B, 0xFF556B2F, 0xFFFF8C00, 0xFF9932CC, 0xFF8B0000, 0xFFE9967A, 0xFF8FBC8F, 0xFF483D8B, 0xFF2F4F4F, 0xFF00CED1, 0xFF9400D3, 0xFFFF1493, 0xFF00BFFF, 0xFF696969, 0xFF1E90FF, 0xFFB22222, 0xFFFFFAF0, 0xFF228B22, 0xFFFF00FF, 0xFFDCDCDC, 0xFFF8F8FF, 0xFFFFD700, 0xFFDAA520, 0xFF808080, 0xFF008000, 0xFFADFF2F, 0xFFF0FFF0, 0xFFFF69B4, 0xFFCD5C5C, 0xFF4B0082, 0xFFFFFFF0, 0xFFF0E68C, 0xFFE6E6FA, 0xFFFFF0F5, 0xFF7CFC00, 0xFFFFFACD, 0xFFADD8E6, 0xFFF08080, 0xFFE0FFFF, 0xFFFAFAD2, 0xFF90EE90, 0xFFD3D3D3, 0xFFFFB6C1, 0xFFFFA07A, 0xFF20B2AA, 0xFF87CEFA, 0xFF778899, 0xFFB0C4DE, 0xFFFFFFE0, 0xFF00FF00, 0xFF32CD32, 0xFFFAF0E6, 0xFFFF00FF, 0xFF800000, 0xFF66CDAA, 0xFF0000CD, 0xFFBA55D3, 0xFF9370DB, 0xFF3CB371, 0xFF7B68EE, 0xFF00FA9A, 0xFF48D1CC, 0xFFC71585, 0xFF191970, 0xFFF5FFFA, 0xFFFFE4E1, 0xFFFFE4B5, 0xFFFFDEAD, 0xFF000080, 0xFFFDF5E6, 0xFF808000, 0xFF6B8E23, 0xFFFFA500, 0xFFFF4500, 0xFFDA70D6, 0xFFEEE8AA, 0xFF98FB98, 0xFFAFEEEE, 0xFFDB7093, 0xFFFFEFD5, 0xFFFFDAB9, 0xFFCD853F, 0xFFFFC0CB, 0xFFDDA0DD, 0xFFB0E0E6, 0xFF800080, 0xFFFF0000, 0xFFBC8F8F, 0xFF4169E1, 0xFF8B4513, 0xFFFA8072, 0xFFF4A460, 0xFF2E8B57, 0xFFFFF5EE, 0xFFA0522D, 0xFFC0C0C0, 0xFF87CEEB, 0xFF6A5ACD, 0xFF708090, 0xFFFFFAFA, 0xFF00FF7F, 0xFF4682B4, 0xFFD2B48C, 0xFF008080, 0xFFD8BFD8, 0xFFFF6347, 0xFF40E0D0, 0xFFEE82EE, 0xFFF5DEB3, 0xFFFFFFFF, 0xFFF5F5F5, 0xFFFFFF00, 0xFF9ACD32, 0xFFAFD7FF, 0xFF2E8B57, 0xFF98FFB3, 0xFF808080, 0xFFFFABBB, 0xFFFF4500, 0xFFF6F675]);
c$.mapJavaScriptColors = c$.prototype.mapJavaScriptColors =  new java.util.Hashtable ();
{
for (var i = J.util.ColorUtil.colorNames.length; --i >= 0; ) J.util.ColorUtil.mapJavaScriptColors.put (J.util.ColorUtil.colorNames[i], Integer.$valueOf (J.util.ColorUtil.colorArgbs[i]));

}});
