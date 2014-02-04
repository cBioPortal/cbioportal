Clazz.declarePackage ("J.renderbio");
Clazz.load (["J.renderbio.StrandsRenderer"], "J.renderbio.MeshRibbonRenderer", null, function () {
c$ = Clazz.declareType (J.renderbio, "MeshRibbonRenderer", J.renderbio.StrandsRenderer);
Clazz.overrideMethod (c$, "renderBioShape", 
function (bioShape) {
if (this.wireframeOnly) this.renderStrands ();
 else this.renderMeshRibbon ();
}, "J.shapebio.BioShape");
$_M(c$, "renderMeshRibbon", 
function () {
if (!this.setStrandCount ()) return;
var offset = ((this.strandCount >> 1) * this.strandSeparation) + this.baseStrandOffset;
this.render2Strand (false, offset, offset);
this.renderStrands ();
});
$_M(c$, "render2Strand", 
function (doFill, offsetTop, offsetBottom) {
this.getScreenControlPoints ();
this.ribbonTopScreens = this.calcScreens (offsetTop);
this.ribbonBottomScreens = this.calcScreens (-offsetBottom);
for (var i = this.bsVisible.nextSetBit (0); i >= 0; i = this.bsVisible.nextSetBit (i + 1)) this.renderHermiteRibbon (doFill, i, false);

this.viewer.freeTempScreens (this.ribbonTopScreens);
this.viewer.freeTempScreens (this.ribbonBottomScreens);
}, "~B,~N,~N");
});
