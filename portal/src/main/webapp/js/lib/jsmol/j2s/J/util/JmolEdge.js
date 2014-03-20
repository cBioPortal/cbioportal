Clazz.declarePackage ("J.util");
Clazz.load (["java.lang.Enum"], "J.util.JmolEdge", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.index = -1;
this.order = 0;
Clazz.instantialize (this, arguments);
}, J.util, "JmolEdge");
c$.getArgbHbondType = $_M(c$, "getArgbHbondType", 
function (order) {
var argbIndex = ((order & 30720) >> 11);
return J.util.JmolEdge.argbsHbondType[argbIndex];
}, "~N");
c$.getBondOrderNumberFromOrder = $_M(c$, "getBondOrderNumberFromOrder", 
function (order) {
order &= -131073;
if (order == 131071 || order == 65535) return "0";
if (J.modelset.Bond.isOrderH (order) || (order & 256) != 0) return J.util.JmolEdge.EnumBondOrder.SINGLE.number;
if ((order & 224) != 0) return (order >> 5) + "." + (order & 0x1F);
return J.util.JmolEdge.EnumBondOrder.getNumberFromCode (order);
}, "~N");
c$.getCmlBondOrder = $_M(c$, "getCmlBondOrder", 
function (order) {
var sname = J.util.JmolEdge.getBondOrderNameFromOrder (order);
switch (sname.charAt (0)) {
case 's':
case 'd':
case 't':
return "" + sname.toUpperCase ().charAt (0);
case 'a':
if (sname.indexOf ("Double") >= 0) return "D";
 else if (sname.indexOf ("Single") >= 0) return "S";
return "aromatic";
case 'p':
if (sname.indexOf (" ") >= 0) return sname.substring (sname.indexOf (" ") + 1);
return "partial12";
}
return null;
}, "~N");
c$.getBondOrderNameFromOrder = $_M(c$, "getBondOrderNameFromOrder", 
function (order) {
order &= -131073;
switch (order) {
case 65535:
case 131071:
return "";
case 32768:
return J.util.JmolEdge.EnumBondOrder.STRUT.$$name;
case 1:
return J.util.JmolEdge.EnumBondOrder.SINGLE.$$name;
case 2:
return J.util.JmolEdge.EnumBondOrder.DOUBLE.$$name;
}
if ((order & 224) != 0) return "partial " + J.util.JmolEdge.getBondOrderNumberFromOrder (order);
if (J.modelset.Bond.isOrderH (order)) return J.util.JmolEdge.EnumBondOrder.H_REGULAR.$$name;
if ((order & 256) != 0) return J.util.JmolEdge.EnumBondOrder.SINGLE.$$name;
return J.util.JmolEdge.EnumBondOrder.getNameFromCode (order);
}, "~N");
c$.getPartialBondDotted = $_M(c$, "getPartialBondDotted", 
function (order) {
return (order & 0x1F);
}, "~N");
c$.getPartialBondOrder = $_M(c$, "getPartialBondOrder", 
function (order) {
return ((order & -131073) >> 5);
}, "~N");
c$.getCovalentBondOrder = $_M(c$, "getCovalentBondOrder", 
function (order) {
if ((order & 1023) == 0) return 0;
order &= -131073;
if ((order & 224) != 0) return J.util.JmolEdge.getPartialBondOrder (order);
if ((order & 256) != 0) order &= -257;
if ((order & 0xF8) != 0) order = 1;
return order & 7;
}, "~N");
c$.getBondOrderFromFloat = $_M(c$, "getBondOrderFromFloat", 
function (fOrder) {
switch (Clazz.floatToInt (fOrder * 10)) {
case 10:
return 1;
case 5:
case -10:
return 33;
case 15:
return 515;
case -15:
return 66;
case 20:
return 2;
case 25:
return 97;
case -25:
return 100;
case 30:
return 3;
case 40:
return 4;
}
return 131071;
}, "~N");
c$.getBondOrderFromString = $_M(c$, "getBondOrderFromString", 
function (name) {
return J.util.JmolEdge.EnumBondOrder.getCodeFromName (name);
}, "~S");
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
this.code = 0;
this.number = null;
this.$$name = null;
Clazz.instantialize (this, arguments);
}, J.util.JmolEdge, "EnumBondOrder", Enum);
Clazz.makeConstructor (c$, 
($fz = function (a, b, c) {
this.code = a;
this.number = b;
this.$$name = c;
}, $fz.isPrivate = true, $fz), "~N,~S,~S");
c$.getCodeFromName = $_M(c$, "getCodeFromName", 
function (a) {
for (var item, $item = 0, $$item = J.util.JmolEdge.EnumBondOrder.values (); $item < $$item.length && ((item = $$item[$item]) || true); $item++) if (item.$$name.equalsIgnoreCase (a)) return item.code;

return 131071;
}, "~S");
c$.getNameFromCode = $_M(c$, "getNameFromCode", 
function (a) {
for (var item, $item = 0, $$item = J.util.JmolEdge.EnumBondOrder.values (); $item < $$item.length && ((item = $$item[$item]) || true); $item++) if (item.code == a) return item.$$name;

return "?";
}, "~N");
c$.getNumberFromCode = $_M(c$, "getNumberFromCode", 
function (a) {
for (var item, $item = 0, $$item = J.util.JmolEdge.EnumBondOrder.values (); $item < $$item.length && ((item = $$item[$item]) || true); $item++) if (item.code == a) return item.number;

return "?";
}, "~N");
Clazz.defineEnumConstant (c$, "SINGLE", 0, [1, "1", "single"]);
Clazz.defineEnumConstant (c$, "DOUBLE", 1, [2, "2", "double"]);
Clazz.defineEnumConstant (c$, "TRIPLE", 2, [3, "3", "triple"]);
Clazz.defineEnumConstant (c$, "QUADRUPLE", 3, [4, "4", "quadruple"]);
Clazz.defineEnumConstant (c$, "AROMATIC", 4, [515, "1.5", "aromatic"]);
Clazz.defineEnumConstant (c$, "STRUT", 5, [32768, "1", "struts"]);
Clazz.defineEnumConstant (c$, "H_REGULAR", 6, [2048, "1", "hbond"]);
Clazz.defineEnumConstant (c$, "PARTIAL01", 7, [33, "0.5", "partial"]);
Clazz.defineEnumConstant (c$, "PARTIAL12", 8, [66, "1.5", "partialDouble"]);
Clazz.defineEnumConstant (c$, "PARTIAL23", 9, [97, "2.5", "partialTriple"]);
Clazz.defineEnumConstant (c$, "PARTIAL32", 10, [100, "2.5", "partialTriple2"]);
Clazz.defineEnumConstant (c$, "AROMATIC_SINGLE", 11, [513, "1", "aromaticSingle"]);
Clazz.defineEnumConstant (c$, "AROMATIC_DOUBLE", 12, [514, "2", "aromaticDouble"]);
Clazz.defineEnumConstant (c$, "UNSPECIFIED", 13, [17, "1", "unspecified"]);
c$ = Clazz.p0p ();
Clazz.defineStatics (c$,
"BOND_STEREO_MASK", 0x400,
"BOND_STEREO_NEAR", 0x401,
"BOND_STEREO_FAR", 0x411,
"BOND_AROMATIC_MASK", 0x200,
"BOND_AROMATIC_SINGLE", 0x201,
"BOND_AROMATIC_DOUBLE", 0x202,
"BOND_AROMATIC", 0x203,
"BOND_SULFUR_MASK", 0x100,
"BOND_PARTIAL_MASK", 0xE0,
"BOND_PARTIAL01", 0x21,
"BOND_PARTIAL12", 0x42,
"BOND_PARTIAL23", 0x61,
"BOND_PARTIAL32", 0x64,
"BOND_COVALENT_MASK", 0x3FF,
"BOND_COVALENT_SINGLE", 1,
"BOND_COVALENT_DOUBLE", 2,
"BOND_COVALENT_TRIPLE", 3,
"BOND_COVALENT_QUADRUPLE", 4,
"BOND_ORDER_UNSPECIFIED", 0x11,
"BOND_ORDER_ANY", 0x0FFFF,
"BOND_ORDER_NULL", 0x1FFFF,
"BOND_STRUT", 0x08000,
"BOND_AS_SINGLE", 0x10000,
"BOND_NEW", 0x20000,
"BOND_HBOND_SHIFT", 11,
"BOND_HYDROGEN_MASK", 30720,
"BOND_H_REGULAR", 2048,
"BOND_H_CALC_MASK", 28672,
"BOND_H_CALC", 4096,
"BOND_H_PLUS_2", 6144,
"BOND_H_PLUS_3", 8192,
"BOND_H_PLUS_4", 10240,
"BOND_H_PLUS_5", 12288,
"BOND_H_MINUS_3", 14336,
"BOND_H_MINUS_4", 16384,
"BOND_H_NUCLEOTIDE", 18432,
"argbsHbondType", [0xFFFF69B4, 0xFFFFFF00, 0xFFFFFF00, 0xFFFFFFFF, 0xFFFF00FF, 0xFFFF0000, 0xFFFFA500, 0xFF00FFFF, 0xFF00FF00, 0xFFFF8080],
"FLAG_AROMATIC_DOUBLE", 16,
"FLAG_AROMATIC_DEFINED", 8,
"FLAG_AROMATIC_STRICT", 4,
"FLAG_IGNORE_STEREOCHEMISTRY", 2,
"FLAG_NO_AROMATIC", 1);
});
