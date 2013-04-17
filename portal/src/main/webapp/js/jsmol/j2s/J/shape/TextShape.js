Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.Object2dShape"], "J.shape.TextShape", null, function () {
c$ = Clazz.declareType (J.shape, "TextShape", J.shape.Object2dShape);
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bsSelected) {
this.setPropTS (propertyName, value, bsSelected);
}, "~S,~O,J.util.BS");
$_M(c$, "setPropTS", 
function (propertyName, value, bsSelected) {
if ("text" === propertyName) {
var text = value;
if (this.currentObject == null) {
if (this.isAll) {
var e = this.objects.values ().iterator ();
while (e.hasNext ()) {
e.next ().setText (text);
}
}return;
}(this.currentObject).setText (text);
return;
}if ("font" === propertyName) {
this.currentFont = value;
if (this.currentObject == null) {
if (this.isAll) {
var e = this.objects.values ().iterator ();
while (e.hasNext ()) {
e.next ().setFont (this.currentFont, true);
}
}return;
}(this.currentObject).setFont (this.currentFont, true);
(this.currentObject).setFontScale (0);
return;
}this.setPropOS (propertyName, value, bsSelected);
}, "~S,~O,J.util.BS");
});
