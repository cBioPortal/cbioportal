Clazz.declarePackage ("J.shapebio");
Clazz.load (["J.shapebio.Rockets"], "J.shapebio.Cartoon", null, function () {
c$ = Clazz.declareType (J.shapebio, "Cartoon", J.shapebio.Rockets);
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shapebio.Cartoon, "initShape", []);
this.madDnaRna = 1000;
});
});
