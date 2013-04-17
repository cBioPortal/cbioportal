Clazz.declarePackage ("J.renderbio");
Clazz.load (["J.renderbio.BioShapeRenderer"], "J.renderbio.TraceRenderer", null, function () {
c$ = Clazz.declareType (J.renderbio, "TraceRenderer", J.renderbio.BioShapeRenderer);
Clazz.overrideMethod (c$, "renderBioShape", 
function (bioShape) {
this.getScreenControlPoints ();
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) this.renderHermiteConic (i, false);

}, "J.shapebio.BioShape");
});
