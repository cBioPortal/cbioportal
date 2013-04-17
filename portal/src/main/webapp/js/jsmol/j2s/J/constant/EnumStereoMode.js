Clazz.declarePackage ("J.constant");
Clazz.load (["java.lang.Enum"], "J.constant.EnumStereoMode", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.$$name = null;
this.$isBiColor = false;
Clazz.instantialize (this, arguments);
}, J.constant, "EnumStereoMode", Enum);
Clazz.makeConstructor (c$, 
($fz = function (name, isBiColor) {
this.$$name = name;
this.$isBiColor = isBiColor;
}, $fz.isPrivate = true, $fz), "~S,~B");
$_M(c$, "getName", 
function () {
return this.$$name;
});
$_M(c$, "isBiColor", 
function () {
return this.$isBiColor;
});
c$.getStereoMode = $_M(c$, "getStereoMode", 
function (id) {
for (var item, $item = 0, $$item = J.constant.EnumStereoMode.values (); $item < $$item.length && ((item = $$item[$item]) || true); $item++) if (item.$$name.equalsIgnoreCase (id)) return item;

return null;
}, "~S");
c$.DEFAULT_STEREO_DEGREES = -5;
Clazz.defineEnumConstant (c$, "NONE", 0, ["OFF", false]);
Clazz.defineEnumConstant (c$, "DOUBLE", 1, ["", false]);
Clazz.defineEnumConstant (c$, "REDCYAN", 2, ["REDCYAN", true]);
Clazz.defineEnumConstant (c$, "REDBLUE", 3, ["REDBLUE", true]);
Clazz.defineEnumConstant (c$, "REDGREEN", 4, ["REDGREEN", true]);
Clazz.defineEnumConstant (c$, "CUSTOM", 5, ["", true]);
});
