Clazz.declarePackage ("J.symmetry");
Clazz.load (null, "J.symmetry.HallTranslation", ["J.util.P3i"], function () {
c$ = Clazz.decorateAsClass (function () {
this.translationCode = '\0';
this.rotationOrder = 0;
this.rotationShift12ths = 0;
this.vectorShift12ths = null;
Clazz.instantialize (this, arguments);
}, J.symmetry, "HallTranslation");
Clazz.makeConstructor (c$, 
function (translationCode, params) {
this.translationCode = translationCode;
if (params != null) {
if (params.z >= 0) {
this.vectorShift12ths = params;
return;
}this.rotationOrder = params.x;
this.rotationShift12ths = params.y;
}this.vectorShift12ths =  new J.util.P3i ();
}, "~S,J.util.P3i");
c$.getHallLatticeEquivalent = $_M(c$, "getHallLatticeEquivalent", 
function (latticeParameter) {
var latticeCode = J.symmetry.HallTranslation.getLatticeCode (latticeParameter);
var isCentrosymmetric = (latticeParameter > 0);
return (isCentrosymmetric ? "-" : "") + latticeCode + " 1";
}, "~N");
c$.getLatticeIndex = $_M(c$, "getLatticeIndex", 
function (latt) {
for (var i = 1, ipt = 3; i <= J.symmetry.HallTranslation.nLatticeTypes; i++, ipt += 3) if (J.symmetry.HallTranslation.latticeTranslationData[ipt].charAt (0) == latt) return i;

return 0;
}, "~S");
c$.getLatticeCode = $_M(c$, "getLatticeCode", 
function (latt) {
if (latt < 0) latt = -latt;
return (latt == 0 ? '\0' : latt > J.symmetry.HallTranslation.nLatticeTypes ? J.symmetry.HallTranslation.getLatticeCode (J.symmetry.HallTranslation.getLatticeIndex (String.fromCharCode (latt))) : J.symmetry.HallTranslation.latticeTranslationData[latt * 3].charAt (0));
}, "~N");
c$.getLatticeDesignation = $_M(c$, "getLatticeDesignation", 
function (latt) {
var isCentrosymmetric = (latt > 0);
var str = (isCentrosymmetric ? "-" : "");
if (latt < 0) latt = -latt;
if (latt == 0 || latt > J.symmetry.HallTranslation.nLatticeTypes) return "";
return str + J.symmetry.HallTranslation.getLatticeCode (latt) + ": " + (isCentrosymmetric ? "centrosymmetric " : "") + J.symmetry.HallTranslation.latticeTranslationData[latt * 3 + 1];
}, "~N");
c$.getLatticeDesignation2 = $_M(c$, "getLatticeDesignation2", 
function (latticeCode, isCentrosymmetric) {
var latt = J.symmetry.HallTranslation.getLatticeIndex (latticeCode);
if (!isCentrosymmetric) latt = -latt;
return J.symmetry.HallTranslation.getLatticeDesignation (latt);
}, "~S,~B");
c$.getLatticeExtension = $_M(c$, "getLatticeExtension", 
function (latt, isCentrosymmetric) {
for (var i = 1, ipt = 3; i <= J.symmetry.HallTranslation.nLatticeTypes; i++, ipt += 3) if (J.symmetry.HallTranslation.latticeTranslationData[ipt].charAt (0) == latt) return J.symmetry.HallTranslation.latticeTranslationData[ipt + 2] + (isCentrosymmetric ? " -1" : "");

return "";
}, "~S,~B");
c$.getHallTerms = $_M(c$, "getHallTerms", 
($fz = function () {
return (J.symmetry.HallTranslation.hallTranslationTerms == null ? ($t$ = J.symmetry.HallTranslation.hallTranslationTerms = [ new J.symmetry.HallTranslation ('a', J.util.P3i.new3 (6, 0, 0)),  new J.symmetry.HallTranslation ('b', J.util.P3i.new3 (0, 6, 0)),  new J.symmetry.HallTranslation ('c', J.util.P3i.new3 (0, 0, 6)),  new J.symmetry.HallTranslation ('n', J.util.P3i.new3 (6, 6, 6)),  new J.symmetry.HallTranslation ('u', J.util.P3i.new3 (3, 0, 0)),  new J.symmetry.HallTranslation ('v', J.util.P3i.new3 (0, 3, 0)),  new J.symmetry.HallTranslation ('w', J.util.P3i.new3 (0, 0, 3)),  new J.symmetry.HallTranslation ('d', J.util.P3i.new3 (3, 3, 3)),  new J.symmetry.HallTranslation ('1', J.util.P3i.new3 (2, 6, -1)),  new J.symmetry.HallTranslation ('1', J.util.P3i.new3 (3, 4, -1)),  new J.symmetry.HallTranslation ('2', J.util.P3i.new3 (3, 8, -1)),  new J.symmetry.HallTranslation ('1', J.util.P3i.new3 (4, 3, -1)),  new J.symmetry.HallTranslation ('3', J.util.P3i.new3 (4, 9, -1)),  new J.symmetry.HallTranslation ('1', J.util.P3i.new3 (6, 2, -1)),  new J.symmetry.HallTranslation ('2', J.util.P3i.new3 (6, 4, -1)),  new J.symmetry.HallTranslation ('4', J.util.P3i.new3 (6, 8, -1)),  new J.symmetry.HallTranslation ('5', J.util.P3i.new3 (6, 10, -1)),  new J.symmetry.HallTranslation ('r', J.util.P3i.new3 (4, 8, 8)),  new J.symmetry.HallTranslation ('s', J.util.P3i.new3 (8, 8, 4)),  new J.symmetry.HallTranslation ('t', J.util.P3i.new3 (8, 4, 8))], J.symmetry.HallTranslation.prototype.hallTranslationTerms = J.symmetry.HallTranslation.hallTranslationTerms, $t$) : J.symmetry.HallTranslation.hallTranslationTerms);
}, $fz.isPrivate = true, $fz));
c$.getHallTranslation = $_M(c$, "getHallTranslation", 
function (translationCode, order) {
var ht = null;
for (var i = J.symmetry.HallTranslation.getHallTerms ().length; --i >= 0; ) {
var h = J.symmetry.HallTranslation.hallTranslationTerms[i];
if (h.translationCode == translationCode) {
if (h.rotationOrder == 0 || h.rotationOrder == order) {
ht =  new J.symmetry.HallTranslation (translationCode, null);
ht.translationCode = translationCode;
ht.rotationShift12ths = h.rotationShift12ths;
ht.vectorShift12ths = h.vectorShift12ths;
return ht;
}}}
return ht;
}, "~S,~N");
Clazz.defineStatics (c$,
"latticeTranslationData", ["\0", "unknown", "", "P", "primitive", "", "I", "body-centered", " 1n", "R", "rhombohedral", " 1r 1r", "F", "face-centered", " 1ab 1bc 1ac", "A", "A-centered", " 1bc", "B", "B-centered", " 1ac", "C", "C-centered", " 1ab", "S", "rhombohedral(S)", " 1s 1s", "T", "rhombohedral(T)", " 1t 1t"]);
c$.nLatticeTypes = c$.prototype.nLatticeTypes = Clazz.doubleToInt (J.symmetry.HallTranslation.latticeTranslationData.length / 3) - 1;
Clazz.defineStatics (c$,
"hallTranslationTerms", null);
});
