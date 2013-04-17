Clazz.declarePackage ("J.jvxl.readers");
Clazz.load (["J.jvxl.readers.IsoMepReader"], "J.jvxl.readers.IsoMlpReader", null, function () {
c$ = Clazz.declareType (J.jvxl.readers, "IsoMlpReader", J.jvxl.readers.IsoMepReader);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.jvxl.readers.IsoMlpReader, []);
});
$_M(c$, "init", 
function (sg) {
Clazz.superCall (this, J.jvxl.readers.IsoMlpReader, "init", [sg]);
this.type = "Mlp";
}, "J.jvxl.readers.SurfaceGenerator");
});
