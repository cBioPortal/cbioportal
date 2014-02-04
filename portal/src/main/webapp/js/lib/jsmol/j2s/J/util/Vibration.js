Clazz.declarePackage ("J.util");
Clazz.load (["J.util.V3"], "J.util.Vibration", null, function () {
c$ = Clazz.declareType (J.util, "Vibration", J.util.V3);
$_M(c$, "setTempPoint", 
function (pt, t, scale) {
pt.scaleAdd2 ((Math.cos (t * 6.283185307179586) * scale), this, pt);
}, "J.util.P3,~N,~N");
Clazz.defineStatics (c$,
"twoPI", 6.283185307179586);
});
