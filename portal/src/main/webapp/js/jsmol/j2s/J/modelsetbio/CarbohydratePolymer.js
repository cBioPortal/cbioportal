Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.BioPolymer"], "J.modelsetbio.CarbohydratePolymer", null, function () {
c$ = Clazz.declareType (J.modelsetbio, "CarbohydratePolymer", J.modelsetbio.BioPolymer);
Clazz.makeConstructor (c$, 
function (monomers) {
Clazz.superConstructor (this, J.modelsetbio.CarbohydratePolymer, [monomers]);
this.type = 3;
}, "~A");
});
