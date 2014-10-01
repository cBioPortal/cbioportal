Clazz.declarePackage ("J.c");
Clazz.load (["java.lang.Enum"], "J.c.AXES", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.code = 0;
Clazz.instantialize (this, arguments);
}, J.c, "AXES", Enum);
Clazz.makeConstructor (c$, 
 function (code) {
this.code = code;
}, "~N");
Clazz.defineMethod (c$, "getCode", 
function () {
return this.code;
});
c$.getAxesMode = Clazz.defineMethod (c$, "getAxesMode", 
function (code) {
for (var mode, $mode = 0, $$mode = J.c.AXES.values (); $mode < $$mode.length && ((mode = $$mode[$mode]) || true); $mode++) {
if (mode.getCode () == code) {
return mode;
}}
return null;
}, "~N");
Clazz.defineEnumConstant (c$, "BOUNDBOX", 0, [0]);
Clazz.defineEnumConstant (c$, "MOLECULAR", 1, [1]);
Clazz.defineEnumConstant (c$, "UNITCELL", 2, [2]);
});
