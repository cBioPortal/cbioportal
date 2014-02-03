Clazz.declarePackage ("J.util");
Clazz.load (["J.util.P3"], "J.util.Point3fi", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.index = 0;
this.screenX = 0;
this.screenY = 0;
this.screenZ = 0;
this.screenDiameter = -1;
this.modelIndex = -1;
Clazz.instantialize (this, arguments);
}, J.util, "Point3fi", J.util.P3);
c$.set2 = $_M(c$, "set2", 
function (p3f, p3i) {
p3f.x = p3i.x;
p3f.y = p3i.y;
p3f.z = p3i.z;
}, "J.util.P3,J.util.P3i");
});
