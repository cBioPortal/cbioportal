Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.Shape"], "J.shape.FontShape", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.myType = null;
this.font3d = null;
Clazz.instantialize (this, arguments);
}, J.shape, "FontShape", J.shape.Shape);
Clazz.overrideMethod (c$, "initShape", 
function () {
this.translucentAllowed = false;
});
Clazz.defineMethod (c$, "setPropFS", 
function (propertyName, value) {
if ("font" === propertyName) {
this.font3d = value;
return;
}}, "~S,~O");
});
