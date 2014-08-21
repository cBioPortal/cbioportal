Clazz.declarePackage ("J.api");
Clazz.load (null, "J.api.Interface", ["JU.Logger"], function () {
c$ = Clazz.declareType (J.api, "Interface");
c$.getInterface = Clazz.defineMethod (c$, "getInterface", 
function (name) {
try {
var x = Clazz._4Name (name);
return (x == null ? null : x.newInstance ());
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
JU.Logger.error ("Interface.java Error creating instance for " + name + ": \n" + e);
return null;
} else {
throw e;
}
}
}, "~S");
c$.getOption = Clazz.defineMethod (c$, "getOption", 
function (className) {
return J.api.Interface.getInterface ("J." + className);
}, "~S");
c$.getUtil = Clazz.defineMethod (c$, "getUtil", 
function (name) {
return J.api.Interface.getInterface ("JU." + name);
}, "~S");
c$.getSymmetry = Clazz.defineMethod (c$, "getSymmetry", 
function () {
return J.api.Interface.getInterface ("JS.Symmetry");
});
});
