Clazz.declarePackage ("JU");
Clazz.load (["JU.V3"], "JU.Vibration", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.modDim = -1;
Clazz.instantialize (this, arguments);
}, JU, "Vibration", JU.V3);
Clazz.defineMethod (c$, "setTempPoint", 
function (pt, t456, scale, modulationScale) {
if (this.modDim >= -1) pt.scaleAdd2 ((Math.cos (t456.x * 6.283185307179586) * scale), this, pt);
}, "JU.T3,JU.T3,~N,~N");
Clazz.defineMethod (c$, "getInfo", 
function (info) {
info.put ("vibVector", JU.V3.newV (this));
info.put ("vibType", (this.modDim == -3 ? "displacement" : this.modDim == -2 ? "spin" : this.modDim == -1 ? "vib" : "mod"));
}, "java.util.Map");
Clazz.defineMethod (c$, "getUnitCell", 
function () {
return null;
});
Clazz.overrideMethod (c$, "clone", 
function () {
var v =  new JU.Vibration ();
v.setT (this);
v.modDim = this.modDim;
return v;
});
Clazz.defineStatics (c$,
"twoPI", 6.283185307179586,
"TYPE_MODULATION", 0,
"TYPE_VIBRATION", -1,
"TYPE_SPIN", -2,
"TYPE_DISPLACEMENT", -3);
});
