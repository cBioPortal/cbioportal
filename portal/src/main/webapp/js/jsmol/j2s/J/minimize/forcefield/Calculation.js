Clazz.declarePackage ("J.minimize.forcefield");
c$ = Clazz.decorateAsClass (function () {
this.key = null;
this.dE = 0;
this.a = null;
this.b = null;
this.c = null;
this.d = null;
this.ia = 0;
this.ib = 0;
this.ic = 0;
this.id = 0;
this.iData = null;
this.dData = null;
this.delta = 0;
this.rab = 0;
this.theta = 0;
this.energy = 0;
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield, "Calculation");
$_M(c$, "getEnergy", 
function () {
return this.energy;
});
$_M(c$, "getPointers", 
function (dataIn) {
this.dData = dataIn[1];
this.iData = dataIn[0];
switch (this.iData.length) {
default:
this.id = this.iData[3];
case 3:
this.ic = this.iData[2];
case 2:
this.ib = this.iData[1];
case 1:
this.ia = this.iData[0];
case 0:
break;
}
}, "~A");
