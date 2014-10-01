Clazz.declarePackage ("J.shapebio");
Clazz.load (["J.shapebio.Strands"], "J.shapebio.MeshRibbon", null, function () {
c$ = Clazz.declareType (J.shapebio, "MeshRibbon", J.shapebio.Strands);
Clazz.defineMethod (c$, "initShape", 
function () {
Clazz.superCall (this, J.shapebio.MeshRibbon, "initShape", []);
this.isMesh = true;
});
});
