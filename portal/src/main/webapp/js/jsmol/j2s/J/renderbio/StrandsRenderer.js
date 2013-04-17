Clazz.declarePackage ("J.renderbio");
Clazz.load (["J.renderbio.BioShapeRenderer"], "J.renderbio.StrandsRenderer", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.strandCount = 0;
this.strandSeparation = 0;
this.baseOffset = 0;
Clazz.instantialize (this, arguments);
}, J.renderbio, "StrandsRenderer", J.renderbio.BioShapeRenderer);
Clazz.overrideMethod (c$, "renderBioShape", 
function (bioShape) {
if (!this.setStrandCount ()) return;
this.render1 ();
}, "J.shapebio.BioShape");
$_M(c$, "setStrandCount", 
function () {
if (this.wingVectors == null) return false;
this.strandCount = this.viewer.getStrandCount ((this.shape).shapeID);
this.strandSeparation = (this.strandCount <= 1) ? 0 : 1 / (this.strandCount - 1);
this.baseOffset = ((this.strandCount & 1) == 0 ? this.strandSeparation / 2 : this.strandSeparation);
return true;
});
$_M(c$, "render1", 
function () {
var screens;
for (var i = this.strandCount >> 1; --i >= 0; ) {
var f = (i * this.strandSeparation) + this.baseOffset;
screens = this.calcScreens (f);
this.render1Strand (screens);
this.viewer.freeTempScreens (screens);
screens = this.calcScreens (-f);
this.render1Strand (screens);
this.viewer.freeTempScreens (screens);
}
if (this.strandCount % 2 == 1) {
screens = this.calcScreens (0);
this.render1Strand (screens);
this.viewer.freeTempScreens (screens);
}});
$_M(c$, "render1Strand", 
($fz = function (screens) {
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) this.renderHermiteCylinder (screens, i);

}, $fz.isPrivate = true, $fz), "~A");
});
