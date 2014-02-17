Clazz.declarePackage ("J.minimize");
Clazz.load (["J.minimize.MinObject"], "J.minimize.MinPosition", null, function () {
c$ = Clazz.declareType (J.minimize, "MinPosition", J.minimize.MinObject);
Clazz.makeConstructor (c$, 
function (data, ddata) {
Clazz.superConstructor (this, J.minimize.MinPosition, []);
this.data = data;
this.ddata = ddata;
}, "~A,~A");
});
