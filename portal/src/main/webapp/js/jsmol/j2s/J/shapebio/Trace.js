Clazz.declarePackage ("J.shapebio");
Clazz.load (["J.shapebio.BioShapeCollection"], "J.shapebio.Trace", null, function () {
c$ = Clazz.declareType (J.shapebio, "Trace", J.shapebio.BioShapeCollection);
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shapebio.Trace, "initShape", []);
this.madOn = 600;
this.madHelixSheet = 1500;
this.madTurnRandom = 500;
this.madDnaRna = 1500;
});
});
