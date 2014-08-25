Clazz.declarePackage ("J.shapebio");
Clazz.load (["J.shapebio.BioShapeCollection"], "J.shapebio.Rockets", null, function () {
c$ = Clazz.declareType (J.shapebio, "Rockets", J.shapebio.BioShapeCollection);
Clazz.defineMethod (c$, "initShape", 
function () {
Clazz.superCall (this, J.shapebio.Rockets, "initShape", []);
this.madTurnRandom = 500;
});
});
