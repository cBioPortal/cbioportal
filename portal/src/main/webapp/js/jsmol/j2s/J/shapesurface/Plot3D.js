Clazz.declarePackage ("J.shapesurface");
Clazz.load (["J.shapesurface.Pmesh"], "J.shapesurface.Plot3D", null, function () {
c$ = Clazz.declareType (J.shapesurface, "Plot3D", J.shapesurface.Pmesh);
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shapesurface.Plot3D, "initShape", []);
this.myType = "plot3d";
});
});
