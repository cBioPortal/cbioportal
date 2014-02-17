Clazz.declarePackage ("J.constant");
Clazz.load (["java.lang.Enum"], "J.constant.EnumCallback", ["J.util.SB"], function () {
c$ = Clazz.declareType (J.constant, "EnumCallback", Enum);
c$.getCallback = $_M(c$, "getCallback", 
function (name) {
name = name.toUpperCase ();
name = name.substring (0, Math.max (name.indexOf ("CALLBACK"), 0));
for (var item, $item = 0, $$item = J.constant.EnumCallback.values (); $item < $$item.length && ((item = $$item[$item]) || true); $item++) if (item.name ().equalsIgnoreCase (name)) return item;

return null;
}, "~S");
c$.getNameList = $_M(c$, "getNameList", 
function () {
if (J.constant.EnumCallback.nameList == null) {
var names =  new J.util.SB ();
for (var item, $item = 0, $$item = J.constant.EnumCallback.values (); $item < $$item.length && ((item = $$item[$item]) || true); $item++) names.append (item.name ().toLowerCase ()).append ("Callback;");

($t$ = J.constant.EnumCallback.nameList = names.toString (), J.constant.EnumCallback.prototype.nameList = J.constant.EnumCallback.nameList, $t$);
}return J.constant.EnumCallback.nameList;
});
c$.nameList = null;
Clazz.defineEnumConstant (c$, "ANIMFRAME", 0, []);
Clazz.defineEnumConstant (c$, "APPLETREADY", 1, []);
Clazz.defineEnumConstant (c$, "ATOMMOVED", 2, []);
Clazz.defineEnumConstant (c$, "CLICK", 3, []);
Clazz.defineEnumConstant (c$, "ECHO", 4, []);
Clazz.defineEnumConstant (c$, "ERROR", 5, []);
Clazz.defineEnumConstant (c$, "EVAL", 6, []);
Clazz.defineEnumConstant (c$, "HOVER", 7, []);
Clazz.defineEnumConstant (c$, "LOADSTRUCT", 8, []);
Clazz.defineEnumConstant (c$, "MEASURE", 9, []);
Clazz.defineEnumConstant (c$, "MESSAGE", 10, []);
Clazz.defineEnumConstant (c$, "MINIMIZATION", 11, []);
Clazz.defineEnumConstant (c$, "PICK", 12, []);
Clazz.defineEnumConstant (c$, "RESIZE", 13, []);
Clazz.defineEnumConstant (c$, "SCRIPT", 14, []);
Clazz.defineEnumConstant (c$, "SYNC", 15, []);
Clazz.defineEnumConstant (c$, "STRUCTUREMODIFIED", 16, []);
});
