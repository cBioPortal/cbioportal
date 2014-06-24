Clazz.declarePackage ("J.constant");
Clazz.load (["java.lang.Enum"], "J.constant.EnumStructure", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.id = 0;
this.color = 0;
Clazz.instantialize (this, arguments);
}, J.constant, "EnumStructure", Enum);
Clazz.makeConstructor (c$, 
($fz = function (id, color) {
this.id = id;
this.color = color;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "getId", 
function () {
return this.id;
});
$_M(c$, "getColor", 
function () {
return this.color;
});
c$.getProteinStructureType = $_M(c$, "getProteinStructureType", 
function (name) {
for (var item, $item = 0, $$item = J.constant.EnumStructure.values (); $item < $$item.length && ((item = $$item[$item]) || true); $item++) if (name.equalsIgnoreCase (item.name ())) return (item.isProtein () ? item : J.constant.EnumStructure.NOT);

return J.constant.EnumStructure.NOT;
}, "~S");
$_M(c$, "getBioStructureTypeName", 
function (isGeneric) {
return (this.id < 0 ? "" : isGeneric && this.isProtein () ? "protein" : this.name ());
}, "~B");
$_M(c$, "isProtein", 
($fz = function () {
return this.id >= 0 && this.id <= 3 || this.id >= 7;
}, $fz.isPrivate = true, $fz));
Clazz.defineEnumConstant (c$, "NOT", 0, [-1, 0xFF808080]);
Clazz.defineEnumConstant (c$, "NONE", 1, [0, 0xFFFFFFFF]);
Clazz.defineEnumConstant (c$, "TURN", 2, [1, 0xFF6080FF]);
Clazz.defineEnumConstant (c$, "SHEET", 3, [2, 0xFFFFC800]);
Clazz.defineEnumConstant (c$, "HELIX", 4, [3, 0xFFFF0080]);
Clazz.defineEnumConstant (c$, "DNA", 5, [4, 0xFFAE00FE]);
Clazz.defineEnumConstant (c$, "RNA", 6, [5, 0xFFFD0162]);
Clazz.defineEnumConstant (c$, "CARBOHYDRATE", 7, [6, 0xFFA6A6FA]);
Clazz.defineEnumConstant (c$, "HELIX310", 8, [7, 0xFFA00080]);
Clazz.defineEnumConstant (c$, "HELIXALPHA", 9, [8, 0xFFFF0080]);
Clazz.defineEnumConstant (c$, "HELIXPI", 10, [9, 0xFF600080]);
});
