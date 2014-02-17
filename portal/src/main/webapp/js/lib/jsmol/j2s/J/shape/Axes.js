Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.FontLineShape", "J.util.P3", "$.V3"], "J.shape.Axes", ["java.lang.Boolean", "J.constant.EnumAxesMode", "J.util.Escape", "$.SB", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.axisXY = null;
this.scale = 0;
this.fixedOrigin = null;
this.originPoint = null;
this.axisPoints = null;
this.labels = null;
this.ptTemp = null;
this.corner = null;
Clazz.instantialize (this, arguments);
}, J.shape, "Axes", J.shape.FontLineShape);
Clazz.prepareFields (c$, function () {
this.axisXY =  new J.util.P3 ();
this.originPoint =  new J.util.P3 ();
this.axisPoints =  new Array (6);
{
for (var i = 6; --i >= 0; ) this.axisPoints[i] =  new J.util.P3 ();

}this.ptTemp =  new J.util.P3 ();
this.corner =  new J.util.V3 ();
});
$_M(c$, "getOriginPoint", 
function (isDataFrame) {
return (isDataFrame ? J.shape.Axes.pt0 : this.originPoint);
}, "~B");
$_M(c$, "getAxisPoint", 
function (i, isDataFrame) {
if (!isDataFrame && this.axisXY.z == 0) return this.axisPoints[i];
this.ptTemp.setT (this.axisPoints[i]);
this.ptTemp.sub (this.originPoint);
this.ptTemp.scale (0.5);
return this.ptTemp;
}, "~N,~B");
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if ("position" === propertyName) {
this.axisXY = value;
return;
}if ("origin" === propertyName) {
if (value == null) {
this.fixedOrigin = null;
} else {
if (this.fixedOrigin == null) this.fixedOrigin =  new J.util.P3 ();
this.fixedOrigin.setT (value);
}this.initShape ();
return;
}if ("labels" === propertyName) {
this.labels = value;
return;
}if ("labelsOn" === propertyName) {
this.labels = null;
return;
}if ("labelsOff" === propertyName) {
this.labels = ["", "", ""];
return;
}this.setPropFLS (propertyName, value);
}, "~S,~O,J.util.BS");
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shape.Axes, "initShape", []);
this.myType = "axes";
this.font3d = this.gdata.getFont3D (14);
var axesMode = this.viewer.getAxesMode ();
if (this.fixedOrigin == null) this.originPoint.set (0, 0, 0);
 else this.originPoint.setT (this.fixedOrigin);
if (axesMode === J.constant.EnumAxesMode.UNITCELL && this.modelSet.unitCells != null) {
var unitcell = this.viewer.getCurrentUnitCell ();
if (unitcell != null) {
var vectors = unitcell.getUnitCellVertices ();
var offset = unitcell.getCartesianOffset ();
if (this.fixedOrigin == null) {
this.originPoint.setT (offset);
} else {
offset = this.fixedOrigin;
}this.scale = this.viewer.getFloat (570425346) / 2;
this.axisPoints[0].scaleAdd2 (this.scale, vectors[4], offset);
this.axisPoints[1].scaleAdd2 (this.scale, vectors[2], offset);
this.axisPoints[2].scaleAdd2 (this.scale, vectors[1], offset);
return;
}} else if (axesMode === J.constant.EnumAxesMode.BOUNDBOX) {
if (this.fixedOrigin == null) this.originPoint.setT (this.viewer.getBoundBoxCenter ());
}this.setScale (this.viewer.getFloat (570425346) / 2);
});
Clazz.overrideMethod (c$, "getProperty", 
function (property, index) {
if (property === "axisPoints") return this.axisPoints;
if (property === "origin") return this.fixedOrigin;
if (property === "axesTypeXY") return (this.axisXY.z == 0 ? Boolean.FALSE : Boolean.TRUE);
return null;
}, "~S,~N");
$_M(c$, "setScale", 
function (scale) {
this.scale = scale;
this.corner.setT (this.viewer.getBoundBoxCornerVector ());
for (var i = 6; --i >= 0; ) {
var axisPoint = this.axisPoints[i];
axisPoint.setT (J.viewer.JC.unitAxisVectors[i]);
if (this.corner.x < 1.5) this.corner.x = 1.5;
if (this.corner.y < 1.5) this.corner.y = 1.5;
if (this.corner.z < 1.5) this.corner.z = 1.5;
if (this.axisXY.z == 0) {
axisPoint.x *= this.corner.x * scale;
axisPoint.y *= this.corner.y * scale;
axisPoint.z *= this.corner.z * scale;
}axisPoint.add (this.originPoint);
}
}, "~N");
$_M(c$, "getShapeState", 
function () {
var sb =  new J.util.SB ();
sb.append ("  axes scale ").appendF (this.viewer.getFloat (570425346)).append (";\n");
if (this.fixedOrigin != null) sb.append ("  axes center ").append (J.util.Escape.eP (this.fixedOrigin)).append (";\n");
if (this.axisXY.z != 0) sb.append ("  axes position [").appendI (Clazz.floatToInt (this.axisXY.x)).append (" ").appendI (Clazz.floatToInt (this.axisXY.y)).append (" ").append (this.axisXY.z < 0 ? " %" : "").append ("];\n");
if (this.labels != null) {
sb.append ("  axes labels ");
for (var i = 0; i < this.labels.length; i++) if (this.labels[i] != null) sb.append (J.util.Escape.eS (this.labels[i])).append (" ");

sb.append (";\n");
}return Clazz.superCall (this, J.shape.Axes, "getShapeState", []) + sb;
});
c$.pt0 = c$.prototype.pt0 =  new J.util.P3 ();
Clazz.defineStatics (c$,
"MIN_AXIS_LEN", 1.5);
});
