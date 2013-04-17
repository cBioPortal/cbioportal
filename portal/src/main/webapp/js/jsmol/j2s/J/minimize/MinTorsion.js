Clazz.declarePackage ("J.minimize");
Clazz.load (["J.minimize.MinObject"], "J.minimize.MinTorsion", null, function () {
c$ = Clazz.declareType (J.minimize, "MinTorsion", J.minimize.MinObject);
Clazz.makeConstructor (c$, 
function (data) {
Clazz.superConstructor (this, J.minimize.MinTorsion, []);
this.data = data;
}, "~A");
});
