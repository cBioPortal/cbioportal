Clazz.declarePackage ("J.constant");
Clazz.load (["java.lang.Enum"], "J.constant.EnumAxesMode", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.code = 0;
Clazz.instantialize (this, arguments);
}, J.constant, "EnumAxesMode", Enum);
Clazz.makeConstructor (c$, 
($fz = function (code) {
this.code = code;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getCode", 
function () {
return this.code;
});
c$.getAxesMode = $_M(c$, "getAxesMode", 
function (code) {
for (var mode, $mode = 0, $$mode = J.constant.EnumAxesMode.values (); $mode < $$mode.length && ((mode = $$mode[$mode]) || true); $mode++) {
if (mode.getCode () == code) {
return mode;
}}
return null;
}, "~N");
Clazz.defineEnumConstant (c$, "BOUNDBOX", 0, [0]);
Clazz.defineEnumConstant (c$, "MOLECULAR", 1, [1]);
Clazz.defineEnumConstant (c$, "UNITCELL", 2, [2]);
});
