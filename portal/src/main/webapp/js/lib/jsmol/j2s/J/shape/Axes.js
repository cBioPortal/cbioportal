Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.FontLineShape", "JU.P3", "$.V3"], "J.shape.Axes", ["java.lang.Boolean", "JU.PT", "$.SB", "J.c.AXES", "JU.Escape", "JV.JC"], function () {
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
this.axisXY =  new JU.P3 ();
this.originPoint =  new JU.P3 ();
this.axisPoints =  new Array (6);
{
for (var i = 6; --i >= 0; ) this.axisPoints[i] =  new JU.P3 ();

}this.ptTemp =  new JU.P3 ();
this.corner =  new JU.V3 ();
});
Clazz.defineMethod (c$, "getOriginPoint", 
function (isDataFrame) {
return (isDataFrame ? J.shape.Axes.pt0 : this.originPoint);
}, "~B");
Clazz.defineMethod (c$, "getAxisPoint", 
function (i, isDataFrame) {
if (!isDataFrame && this.axisXY.z == 0) return this.axisPoints[i];
this.ptTemp.sub2 (this.axisPoints[i], this.originPoint);
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
if (this.fixedOrigin == null) this.fixedOrigin =  new JU.P3 ();
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
}, "~S,~O,JU.BS");
Clazz.defineMethod (c$, "initShape", 
function () {
Clazz.superCall (this, J.shape.Axes, "initShape", []);
this.myType = "axes";
this.font3d = this.gdata.getFont3D (14);
var axesMode = this.vwr.g.axesMode;
if (this.fixedOrigin == null) this.originPoint.set (0, 0, 0);
 else this.originPoint.setT (this.fixedOrigin);
if (axesMode === J.c.AXES.UNITCELL && this.ms.unitCells != null) {
var unitcell = this.vwr.getCurrentUnitCell ();
if (unitcell != null) {
var vertices = unitcell.getUnitCellVertices ();
var offset = unitcell.getCartesianOffset ();
if (this.fixedOrigin == null) {
this.originPoint.add2 (offset, vertices[0]);
} else {
offset = this.fixedOrigin;
}this.scale = this.vwr.getFloat (570425346) / 2;
this.axisPoints[0].scaleAdd2 (this.scale, vertices[4], offset);
this.axisPoints[1].scaleAdd2 (this.scale, vertices[2], offset);
this.axisPoints[2].scaleAdd2 (this.scale, vertices[1], offset);
return;
}} else if (axesMode === J.c.AXES.BOUNDBOX) {
if (this.fixedOrigin == null) this.originPoint.setT (this.vwr.getBoundBoxCenter ());
}this.setScale (this.vwr.getFloat (570425346) / 2);
});
Clazz.overrideMethod (c$, "getProperty", 
function (property, index) {
if (property === "axisPoints") return this.axisPoints;
if (property === "origin") return this.fixedOrigin;
if (property === "axesTypeXY") return (this.axisXY.z == 0 ? Boolean.FALSE : Boolean.TRUE);
return null;
}, "~S,~N");
Clazz.defineMethod (c$, "setScale", 
function (scale) {
this.scale = scale;
this.corner.setT (this.vwr.getBoundBoxCornerVector ());
for (var i = 6; --i >= 0; ) {
var axisPoint = this.axisPoints[i];
axisPoint.setT (JV.JC.unitAxisVectors[i]);
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
Clazz.defineMethod (c$, "getShapeState", 
function () {
var sb =  new JU.SB ();
sb.append ("  axes scale ").appendF (this.vwr.getFloat (570425346)).append (";\n");
if (this.fixedOrigin != null) sb.append ("  axes center ").append (JU.Escape.eP (this.fixedOrigin)).append (";\n");
if (this.axisXY.z != 0) sb.append ("  axes position [").appendI (Clazz.floatToInt (this.axisXY.x)).append (" ").appendI (Clazz.floatToInt (this.axisXY.y)).append (" ").append (this.axisXY.z < 0 ? " %" : "").append ("];\n");
if (this.labels != null) {
sb.append ("  axes labels ");
for (var i = 0; i < this.labels.length; i++) if (this.labels[i] != null) sb.append (JU.PT.esc (this.labels[i])).append (" ");

sb.append (";\n");
}return Clazz.superCall (this, J.shape.Axes, "getShapeState", []) + sb;
});
c$.pt0 = c$.prototype.pt0 =  new JU.P3 ();
Clazz.defineStatics (c$,
"MIN_AXIS_LEN", 1.5);
});
