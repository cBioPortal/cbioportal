Clazz.declarePackage ("J.c");
Clazz.load (["java.lang.Enum"], "J.c.QS", ["JU.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.tag = null;
this.tag2 = null;
this.id = 0;
this.idSpherical = 0;
Clazz.instantialize (this, arguments);
}, J.c, "QS", Enum);
Clazz.makeConstructor (c$, 
 function (tag, tag2, id, idSpherical) {
this.tag = tag;
this.tag2 = tag2;
this.id = id;
this.idSpherical = idSpherical;
}, "~S,~S,~N,~N");
c$.getNewDfCoefMap = Clazz.defineMethod (c$, "getNewDfCoefMap", 
function () {
return [ Clazz.newIntArray (1, 0),  Clazz.newIntArray (3, 0),  Clazz.newIntArray (4, 0),  Clazz.newIntArray (5, 0),  Clazz.newIntArray (6, 0),  Clazz.newIntArray (7, 0),  Clazz.newIntArray (10, 0),  Clazz.newIntArray (9, 0),  Clazz.newIntArray (15, 0),  Clazz.newIntArray (11, 0),  Clazz.newIntArray (21, 0),  Clazz.newIntArray (13, 0),  Clazz.newIntArray (28, 0)];
});
c$.getQuantumShellTagID = Clazz.defineMethod (c$, "getQuantumShellTagID", 
function (tag) {
if (tag.equals ("L")) return J.c.QS.SP.id;
var item = J.c.QS.getQuantumShell (tag);
return (item == null ? -1 : item.id);
}, "~S");
c$.getQuantumShell = Clazz.defineMethod (c$, "getQuantumShell", 
 function (tag) {
for (var item, $item = 0, $$item = J.c.QS.values (); $item < $$item.length && ((item = $$item[$item]) || true); $item++) if (item.tag.equals (tag) || item.tag2.equals (tag)) return item;

return null;
}, "~S");
c$.getQuantumShellTagIDSpherical = Clazz.defineMethod (c$, "getQuantumShellTagIDSpherical", 
function (tag) {
if (tag.equals ("L")) return J.c.QS.SP.idSpherical;
var item = J.c.QS.getQuantumShell (tag);
return (item == null ? -1 : item.idSpherical);
}, "~S");
c$.getItem = Clazz.defineMethod (c$, "getItem", 
function (id) {
switch (id) {
case 0:
return J.c.QS.S;
case 1:
return J.c.QS.P;
case 2:
return J.c.QS.SP;
case 3:
return J.c.QS.D_SPHERICAL;
case 4:
return J.c.QS.D_CARTESIAN;
case 5:
return J.c.QS.F_SPHERICAL;
case 6:
return J.c.QS.F_CARTESIAN;
case 7:
return J.c.QS.G_SPHERICAL;
case 8:
return J.c.QS.G_CARTESIAN;
case 9:
return J.c.QS.H_SPHERICAL;
case 10:
return J.c.QS.H_CARTESIAN;
case 11:
return J.c.QS.I_SPHERICAL;
case 12:
return J.c.QS.I_CARTESIAN;
}
return null;
}, "~N");
c$.getQuantumShellTag = Clazz.defineMethod (c$, "getQuantumShellTag", 
function (id) {
for (var item, $item = 0, $$item = J.c.QS.values (); $item < $$item.length && ((item = $$item[$item]) || true); $item++) if (item.id == id) return item.tag;

return "" + id;
}, "~N");
c$.getMOString = Clazz.defineMethod (c$, "getMOString", 
function (lc) {
var sb =  new JU.SB ();
if (lc.length == 2) return "" + Clazz.floatToInt (lc[0] < 0 ? -lc[1] : lc[1]);
sb.appendC ('[');
for (var i = 0; i < lc.length; i += 2) {
if (i > 0) sb.append (", ");
sb.appendF (lc[i]).append (" ").appendI (Clazz.floatToInt (lc[i + 1]));
}
sb.appendC (']');
return sb.toString ();
}, "~A");
c$.SUPPORTED_BASIS_FUNCTIONS = "SPLDF";
Clazz.defineEnumConstant (c$, "S", 0, ["S", "S", 0, 0]);
Clazz.defineEnumConstant (c$, "P", 1, ["P", "X", 1, 1]);
Clazz.defineEnumConstant (c$, "SP", 2, ["SP", "SP", 2, 2]);
Clazz.defineEnumConstant (c$, "D_SPHERICAL", 3, ["5D", "5D", 3, 3]);
Clazz.defineEnumConstant (c$, "D_CARTESIAN", 4, ["D", "XX", 4, 3]);
Clazz.defineEnumConstant (c$, "F_SPHERICAL", 5, ["7F", "7F", 5, 5]);
Clazz.defineEnumConstant (c$, "F_CARTESIAN", 6, ["F", "XXX", 6, 5]);
Clazz.defineEnumConstant (c$, "G_SPHERICAL", 7, ["9G", "9G", 7, 7]);
Clazz.defineEnumConstant (c$, "G_CARTESIAN", 8, ["G", "XXXX", 8, 7]);
Clazz.defineEnumConstant (c$, "H_SPHERICAL", 9, ["11H", "11H", 9, 9]);
Clazz.defineEnumConstant (c$, "H_CARTESIAN", 10, ["H", "XXXXX", 10, 9]);
Clazz.defineEnumConstant (c$, "I_SPHERICAL", 11, ["13I", "13I", 11, 11]);
Clazz.defineEnumConstant (c$, "I_CARTESIAN", 12, ["I", "XXXXXX", 12, 11]);
});
