Clazz.declarePackage ("J.util");
Clazz.load (["J.util.Tuple3i"], "J.util.P3i", null, function () {
c$ = Clazz.declareType (J.util, "P3i", J.util.Tuple3i);
c$.new3 = $_M(c$, "new3", 
function (x, y, z) {
var pt =  new J.util.P3i ();
pt.x = x;
pt.y = y;
pt.z = z;
return pt;
}, "~N,~N,~N");
});
