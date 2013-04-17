Clazz.declarePackage ("J.minimize");
Clazz.load (["J.minimize.MinObject"], "J.minimize.MinAngle", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.sbType = 0;
this.sbKey = null;
this.ka = 0;
this.theta0 = NaN;
Clazz.instantialize (this, arguments);
}, J.minimize, "MinAngle", J.minimize.MinObject);
Clazz.makeConstructor (c$, 
function (data) {
Clazz.superConstructor (this, J.minimize.MinAngle, []);
this.data = data;
}, "~A");
});
