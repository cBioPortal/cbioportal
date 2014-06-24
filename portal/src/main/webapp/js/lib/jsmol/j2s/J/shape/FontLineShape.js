Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.FontShape"], "J.shape.FontLineShape", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.tickInfos = null;
this.mad = 0;
Clazz.instantialize (this, arguments);
}, J.shape, "FontLineShape", J.shape.FontShape);
Clazz.prepareFields (c$, function () {
this.tickInfos =  new Array (4);
});
$_M(c$, "setPropFLS", 
function (propertyName, value) {
if ("tickInfo" === propertyName) {
var t = value;
if (t.ticks == null) {
if (t.type.equals (" ")) this.tickInfos[0] = this.tickInfos[1] = this.tickInfos[2] = this.tickInfos[3] = null;
 else this.tickInfos["xyz".indexOf (t.type) + 1] = null;
return;
}this.tickInfos["xyz".indexOf (t.type) + 1] = t;
return;
}this.setPropFS (propertyName, value);
}, "~S,~O");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
var s = this.viewer.getFontState (this.myType, this.font3d);
return (this.tickInfos == null ? s : this.viewer.getFontLineShapeState (s, this.myType, this.tickInfos));
});
});
