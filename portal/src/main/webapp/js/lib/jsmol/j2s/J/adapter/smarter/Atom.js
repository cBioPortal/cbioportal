Clazz.declarePackage ("J.adapter.smarter");
Clazz.load (["JU.P3"], "J.adapter.smarter.Atom", ["java.lang.Float", "JU.AU", "$.Lst", "$.V3", "JU.Vibration"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atomSetIndex = 0;
this.index = 0;
this.bsSymmetry = null;
this.atomSite = 0;
this.elementSymbol = null;
this.elementNumber = -1;
this.atomName = null;
this.formalCharge = -2147483648;
this.partialCharge = NaN;
this.vib = null;
this.bfactor = NaN;
this.foccupancy = 1;
this.radius = NaN;
this.isHetero = false;
this.atomSerial = -2147483648;
this.chainID = 0;
this.altLoc = '\0';
this.group3 = null;
this.sequenceNumber = -2147483648;
this.insertionCode = '\0';
this.anisoBorU = null;
this.tensors = null;
this.ignoreSymmetry = false;
Clazz.instantialize (this, arguments);
}, J.adapter.smarter, "Atom", JU.P3, Cloneable);
Clazz.defineMethod (c$, "addTensor", 
function (tensor, type, reset) {
if (tensor == null) return null;
if (reset || this.tensors == null) this.tensors =  new JU.Lst ();
this.tensors.addLast (tensor);
if (type != null) tensor.setType (type);
return tensor;
}, "JU.Tensor,~S,~B");
Clazz.overrideConstructor (c$, 
function () {
this.set (NaN, NaN, NaN);
});
Clazz.defineMethod (c$, "getClone", 
function () {
var a = this.clone ();
if (this.vib != null) {
if (Clazz.instanceOf (this.vib, JU.Vibration)) {
a.vib = (this.vib).clone ();
} else {
a.vib = JU.V3.newV (a.vib);
}}if (this.anisoBorU != null) a.anisoBorU = JU.AU.arrayCopyF (this.anisoBorU, -1);
if (this.tensors != null) {
a.tensors =  new JU.Lst ();
for (var i = this.tensors.size (); --i >= 0; ) a.tensors.addLast ((this.tensors.get (i)).copyTensor ());

}return a;
});
Clazz.defineMethod (c$, "getElementSymbol", 
function () {
if (this.elementSymbol == null) if (this.atomName != null) {
var len = this.atomName.length;
var ichFirst = 0;
var chFirst = String.fromCharCode (0);
while (ichFirst < len && !J.adapter.smarter.Atom.isValidFirstSymbolChar (chFirst = this.atomName.charAt (ichFirst))) ++ichFirst;

switch (len - ichFirst) {
case 0:
break;
default:
var chSecond = this.atomName.charAt (ichFirst + 1);
if (J.adapter.smarter.Atom.isValidElementSymbolNoCaseSecondChar2 (chFirst, chSecond)) {
this.elementSymbol = "" + chFirst + chSecond;
break;
}case 1:
if (J.adapter.smarter.Atom.isValidElementSymbol (chFirst)) this.elementSymbol = "" + chFirst;
break;
}
}return this.elementSymbol;
});
c$.isValidElementSymbol = Clazz.defineMethod (c$, "isValidElementSymbol", 
function (ch) {
return ch >= 'A' && ch <= 'Z' && J.adapter.smarter.Atom.elementCharMasks[ch.charCodeAt (0) - 65] < 0;
}, "~S");
c$.isValidElementSymbol2 = Clazz.defineMethod (c$, "isValidElementSymbol2", 
function (chFirst, chSecond) {
return (chFirst >= 'A' && chFirst <= 'Z' && chSecond >= 'a' && chSecond <= 'z' && ((J.adapter.smarter.Atom.elementCharMasks[chFirst.charCodeAt (0) - 65] >> (chSecond.charCodeAt (0) - 97)) & 1) != 0);
}, "~S,~S");
c$.isValidElementSymbolNoCaseSecondChar2 = Clazz.defineMethod (c$, "isValidElementSymbolNoCaseSecondChar2", 
function (chFirst, chSecond) {
if (chSecond >= 'A' && chSecond <= 'Z') chSecond = String.fromCharCode (chSecond.charCodeAt (0) + 32);
if (chFirst < 'A' || chFirst > 'Z' || chSecond < 'a' || chSecond > 'z') return false;
return ((J.adapter.smarter.Atom.elementCharMasks[chFirst.charCodeAt (0) - 65] >> (chSecond.charCodeAt (0) - 97)) & 1) != 0;
}, "~S,~S");
c$.isValidFirstSymbolChar = Clazz.defineMethod (c$, "isValidFirstSymbolChar", 
function (ch) {
return ch >= 'A' && ch <= 'Z' && J.adapter.smarter.Atom.elementCharMasks[ch.charCodeAt (0) - 65] != 0;
}, "~S");
c$.isValidElementSymbolNoCaseSecondChar = Clazz.defineMethod (c$, "isValidElementSymbolNoCaseSecondChar", 
function (str) {
if (str == null) return false;
var length = str.length;
if (length == 0) return false;
var chFirst = str.charAt (0);
if (length == 1) return J.adapter.smarter.Atom.isValidElementSymbol (chFirst);
if (length > 2) return false;
var chSecond = str.charAt (1);
return J.adapter.smarter.Atom.isValidElementSymbolNoCaseSecondChar2 (chFirst, chSecond);
}, "~S");
Clazz.defineMethod (c$, "scaleVector", 
function (vibScale) {
if (this.vib == null || Float.isNaN (this.vib.z)) return;
this.vib.scale (vibScale);
}, "~N");
Clazz.defineStatics (c$,
"elementCharMasks", [1972292, -2147351151, -2146019271, -2130706430, 1441792, -2147348464, 25, -2147205008, -2147344384, 0, -2147352576, 1179905, 548936, -2147434213, -2147221504, -2145759221, 0, 1056947, -2147339946, -2147477097, -2147483648, -2147483648, -2147483648, 8388624, -2147483646, 139264]);
});
