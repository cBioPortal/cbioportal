Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.FontLineShape"], "J.shape.Uccage", null, function () {
c$ = Clazz.declareType (J.shape, "Uccage", J.shape.FontLineShape);
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
this.setPropFLS (propertyName, value);
}, "~S,~O,JU.BS");
Clazz.defineMethod (c$, "getShapeState", 
function () {
if (!this.ms.haveUnitCells) return "";
var s = Clazz.superCall (this, J.shape.Uccage, "getShapeState", []);
var iAtom = this.vwr.am.cai;
if (iAtom >= 0) s += "  unitcell ({" + iAtom + "});\n";
var uc = this.vwr.getCurrentUnitCell ();
if (uc != null) s += uc.getUnitCellState ();
return s;
});
Clazz.defineMethod (c$, "initShape", 
function () {
Clazz.superCall (this, J.shape.Uccage, "initShape", []);
this.font3d = this.gdata.getFont3D (14);
this.myType = "unitcell";
});
});
