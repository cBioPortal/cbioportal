Clazz.declarePackage ("J.constant");
Clazz.load (["java.lang.Enum"], "J.constant.EnumFileStatus", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.code = 0;
Clazz.instantialize (this, arguments);
}, J.constant, "EnumFileStatus", Enum);
$_M(c$, "getCode", 
function () {
return this.code;
});
Clazz.makeConstructor (c$, 
($fz = function (code) {
this.code = code;
}, $fz.isPrivate = true, $fz), "~N");
Clazz.defineEnumConstant (c$, "DELETED", 0, [5]);
Clazz.defineEnumConstant (c$, "CREATED", 1, [3]);
Clazz.defineEnumConstant (c$, "CREATING_MODELSET", 2, [2]);
Clazz.defineEnumConstant (c$, "ZAPPED", 3, [0]);
Clazz.defineEnumConstant (c$, "NOT_LOADED", 4, [-1]);
});
