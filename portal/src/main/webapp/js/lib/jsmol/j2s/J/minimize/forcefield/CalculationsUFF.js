Clazz.declarePackage ("J.minimize.forcefield");
Clazz.load (["J.minimize.forcefield.Calculation", "$.Calculations"], "J.minimize.forcefield.CalculationsUFF", ["J.minimize.Util", "J.util.JmolList"], function () {
c$ = Clazz.decorateAsClass (function () {
this.ffParams = null;
this.parA = null;
this.parB = null;
this.parC = null;
this.bondCalc = null;
this.angleCalc = null;
this.torsionCalc = null;
this.oopCalc = null;
this.vdwCalc = null;
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsUFF.DistanceCalc")) {
J.minimize.forcefield.CalculationsUFF.$CalculationsUFF$DistanceCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsUFF.AngleCalc")) {
J.minimize.forcefield.CalculationsUFF.$CalculationsUFF$AngleCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsUFF.TorsionCalc")) {
J.minimize.forcefield.CalculationsUFF.$CalculationsUFF$TorsionCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsUFF.OOPCalc")) {
J.minimize.forcefield.CalculationsUFF.$CalculationsUFF$OOPCalc$ ();
}
if (!Clazz.isClassDefined ("J.minimize.forcefield.CalculationsUFF.VDWCalc")) {
J.minimize.forcefield.CalculationsUFF.$CalculationsUFF$VDWCalc$ ();
}
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield, "CalculationsUFF", J.minimize.forcefield.Calculations);
Clazz.makeConstructor (c$, 
function (ff, ffParams, minAtoms, minBonds, minAngles, minTorsions, minPositions, constraints) {
Clazz.superConstructor (this, J.minimize.forcefield.CalculationsUFF, [ff, minAtoms, minBonds, minAngles, minTorsions, minPositions, constraints]);
this.ffParams = ffParams;
this.bondCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.DistanceCalc, this, null);
this.angleCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.AngleCalc, this, null);
this.torsionCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.TorsionCalc, this, null);
this.oopCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.OOPCalc, this, null);
this.vdwCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.VDWCalc, this, null);
}, "J.minimize.forcefield.ForceField,java.util.Map,~A,~A,~A,~A,~A,J.util.JmolList");
Clazz.overrideMethod (c$, "getUnits", 
function () {
return "kJ";
});
Clazz.overrideMethod (c$, "setupCalculations", 
function () {
var calc;
var distanceCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.DistanceCalc, this, null);
calc = this.calculations[0] =  new J.util.JmolList ();
for (var i = 0; i < this.bondCount; i++) {
var bond = this.minBonds[i];
var bondOrder = bond.order;
if (bond.isAromatic) bondOrder = 1.5;
if (bond.isAmide) bondOrder = 1.41;
distanceCalc.setData (calc, bond.data[0], bond.data[1], bondOrder);
}
calc = this.calculations[1] =  new J.util.JmolList ();
var angleCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.AngleCalc, this, null);
for (var i = this.minAngles.length; --i >= 0; ) angleCalc.setData (calc, this.minAngles[i].data);

calc = this.calculations[3] =  new J.util.JmolList ();
var torsionCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.TorsionCalc, this, null);
for (var i = this.minTorsions.length; --i >= 0; ) torsionCalc.setData (calc, this.minTorsions[i].data);

calc = this.calculations[4] =  new J.util.JmolList ();
var oopCalc = Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.OOPCalc, this, null);
var elemNo;
for (var i = 0; i < this.atomCount; i++) {
var a = this.minAtoms[i];
if (a.nBonds == 3 && J.minimize.forcefield.CalculationsUFF.isInvertible (elemNo = a.atom.getElementNumber ())) oopCalc.setData (calc, i, elemNo);
}
this.pairSearch (this.calculations[5] =  new J.util.JmolList (), Clazz.innerTypeInstance (J.minimize.forcefield.CalculationsUFF.VDWCalc, this, null), null, null);
return true;
});
c$.isInvertible = $_M(c$, "isInvertible", 
($fz = function (n) {
switch (n) {
case 6:
case 7:
case 8:
case 15:
case 33:
case 51:
case 83:
return true;
default:
return false;
}
}, $fz.isPrivate = true, $fz), "~N");
c$.calculateR0 = $_M(c$, "calculateR0", 
function (ri, rj, chiI, chiJ, bondorder) {
var rbo = -0.1332 * (ri + rj) * Math.log (bondorder);
var dchi = Math.sqrt (chiI) - Math.sqrt (chiJ);
var ren = ri * rj * dchi * dchi / (chiI * ri + chiJ * rj);
return (ri + rj + rbo - ren);
}, "~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "compute", 
function (iType, dataIn) {
switch (iType) {
case 0:
return this.bondCalc.compute (dataIn);
case 1:
return this.angleCalc.compute (dataIn);
case 3:
return this.torsionCalc.compute (dataIn);
case 4:
return this.oopCalc.compute (dataIn);
case 5:
return this.vdwCalc.compute (dataIn);
}
return 0.0;
}, "~N,~A");
$_M(c$, "getParameter", 
function (a) {
return this.ffParams.get (a);
}, "~O");
Clazz.overrideMethod (c$, "getDebugHeader", 
function (iType) {
switch (iType) {
case -1:
return "Universal Force Field -- Rappe, A. K., et. al.; J. Am. Chem. Soc. (1992) 114(25) p. 10024-10035\n";
default:
return this.getDebugHeader2 (iType);
}
}, "~N");
c$.$CalculationsUFF$DistanceCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.r0 = 0;
this.kb = 0;
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsUFF, "DistanceCalc", J.minimize.forcefield.Calculation);
$_M(c$, "setData", 
function (a, b, c, d) {
this.b$["J.minimize.forcefield.CalculationsUFF"].parA = this.b$["J.minimize.forcefield.CalculationsUFF"].getParameter (this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[b].sType);
this.b$["J.minimize.forcefield.CalculationsUFF"].parB = this.b$["J.minimize.forcefield.CalculationsUFF"].getParameter (this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[c].sType);
this.r0 = J.minimize.forcefield.CalculationsUFF.calculateR0 (this.b$["J.minimize.forcefield.CalculationsUFF"].parA.dVal[0], this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[0], this.b$["J.minimize.forcefield.CalculationsUFF"].parA.dVal[8], this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[8], d);
this.kb = 1390.2842991599998 * this.b$["J.minimize.forcefield.CalculationsUFF"].parA.dVal[5] * this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[5] / (this.r0 * this.r0 * this.r0);
a.addLast ([[b, c], [this.r0, this.kb, d]]);
}, "J.util.JmolList,~N,~N,~N");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
this.r0 = this.dData[0];
this.kb = this.dData[1];
this.b$["J.minimize.forcefield.CalculationsUFF"].setPairVariables (this);
this.delta = this.rab - this.r0;
this.energy = this.kb * this.delta * this.delta;
if (this.b$["J.minimize.forcefield.CalculationsUFF"].gradients) {
this.dE = 2.0 * this.kb * this.delta;
this.b$["J.minimize.forcefield.CalculationsUFF"].addForces (this, 2);
}if (this.b$["J.minimize.forcefield.CalculationsUFF"].logging) this.b$["J.minimize.forcefield.CalculationsUFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsUFF"].getDebugLine (0, this));
return this.energy;
}, "~A");
c$ = Clazz.p0p ();
};
c$.$CalculationsUFF$AngleCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsUFF, "AngleCalc", J.minimize.forcefield.Calculation);
$_M(c$, "setData", 
function (a, b) {
this.a = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.ia = b[0]];
this.b = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.ib = b[1]];
this.c = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.ic = b[2]];
var c = (this.a.sType === "H_" && this.c.sType === "H_" ? 10 : 1);
this.b$["J.minimize.forcefield.CalculationsUFF"].parA = this.b$["J.minimize.forcefield.CalculationsUFF"].getParameter (this.a.sType);
this.b$["J.minimize.forcefield.CalculationsUFF"].parB = this.b$["J.minimize.forcefield.CalculationsUFF"].getParameter (this.b.sType);
this.b$["J.minimize.forcefield.CalculationsUFF"].parC = this.b$["J.minimize.forcefield.CalculationsUFF"].getParameter (this.c.sType);
var d = this.b$["J.minimize.forcefield.CalculationsUFF"].parB.iVal[0];
var e = this.b$["J.minimize.forcefield.CalculationsUFF"].parA.dVal[5];
var f = this.b$["J.minimize.forcefield.CalculationsUFF"].parC.dVal[5];
var g = this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[1];
var h = Math.cos (g);
var i = Math.sin (g);
var j;
var k;
var l;
switch (d) {
case 1:
case 2:
case 4:
case 6:
j = k = l = 0;
break;
default:
l = 1.0 / (4.0 * i * i);
k = -4.0 * l * h;
j = l * (2.0 * h * h + 1.0);
}
var m = this.a.getBondTo (this.ib);
var n = m.order;
if (m.isAromatic) n = 1.5;
if (m.isAmide) n = 1.41;
this.rab = J.minimize.forcefield.CalculationsUFF.calculateR0 (this.b$["J.minimize.forcefield.CalculationsUFF"].parA.dVal[0], this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[0], this.b$["J.minimize.forcefield.CalculationsUFF"].parA.dVal[8], this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[8], n);
m = this.c.getBondTo (this.ib);
n = m.order;
if (m.isAromatic) n = 1.5;
if (m.isAmide) n = 1.41;
var o = J.minimize.forcefield.CalculationsUFF.calculateR0 (this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[0], this.b$["J.minimize.forcefield.CalculationsUFF"].parC.dVal[0], this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[8], this.b$["J.minimize.forcefield.CalculationsUFF"].parC.dVal[8], n);
var p = Math.sqrt (this.rab * this.rab + o * o - 2.0 * this.rab * o * h);
var q = (2696.8016159999997) * (e * f / (Math.pow (p, 5.0))) * (3.0 * this.rab * o * (1.0 - h * h) - p * p * h);
a.addLast ([[this.ia, this.ib, this.ic, d], [q, g * 57.29577951308232, j - l, k, 2 * l, c * q]]);
}, "J.util.JmolList,~A");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
var b = this.iData[3];
var c = (this.b$["J.minimize.forcefield.CalculationsUFF"].isPreliminary ? this.dData[5] : this.dData[0]);
var d = this.dData[2];
var e = this.dData[3];
var f = this.dData[4];
this.b$["J.minimize.forcefield.CalculationsUFF"].setAngleVariables (this);
if ((b == 4 || b == 6) && (this.theta > 2.35619 || this.theta < 0.785398)) b = 1;
var g = Math.cos (this.theta);
var h = Math.sin (this.theta);
switch (b) {
case 0:
case 1:
this.energy = c * (1.0 + g) * (1.0 + g) / 4.0;
break;
case 2:
this.energy = c * (1.0 + (4.0 * g) * (1.0 + g)) / 9.0;
break;
case 4:
case 6:
this.energy = c * g * g;
break;
default:
this.energy = c * (d + e * g + f * g * g);
}
if (this.b$["J.minimize.forcefield.CalculationsUFF"].gradients) {
switch (b) {
case 0:
case 1:
this.dE = -0.5 * c * h * (1 + g);
break;
case 2:
this.dE = -4.0 * h * c * (1.0 - 2.0 * g) / 9.0;
break;
case 4:
case 6:
this.dE = -c * h * g;
break;
default:
this.dE = -c * (e * h - 2.0 * f * g * h);
}
this.b$["J.minimize.forcefield.CalculationsUFF"].addForces (this, 3);
}if (this.b$["J.minimize.forcefield.CalculationsUFF"].logging) this.b$["J.minimize.forcefield.CalculationsUFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsUFF"].getDebugLine (1, this));
return this.energy;
}, "~A");
c$ = Clazz.p0p ();
};
c$.$CalculationsUFF$TorsionCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsUFF, "TorsionCalc", J.minimize.forcefield.Calculation);
$_M(c$, "setData", 
function (a, b) {
var c = -1;
var d = 0;
var e = 0;
this.a = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.ia = b[0]];
this.b = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.ib = b[1]];
this.c = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.ic = b[2]];
this.d = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.id = b[3]];
var f = this.c.getBondTo (this.ib);
var g = f.order;
if (f.isAromatic) g = 1.5;
if (f.isAmide) g = 1.41;
this.b$["J.minimize.forcefield.CalculationsUFF"].parB = this.b$["J.minimize.forcefield.CalculationsUFF"].getParameter (this.b.sType);
this.b$["J.minimize.forcefield.CalculationsUFF"].parC = this.b$["J.minimize.forcefield.CalculationsUFF"].getParameter (this.c.sType);
switch (this.b$["J.minimize.forcefield.CalculationsUFF"].parB.iVal[0] * this.b$["J.minimize.forcefield.CalculationsUFF"].parC.iVal[0]) {
case 9:
d = 3;
var h = this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[6];
var i = this.b$["J.minimize.forcefield.CalculationsUFF"].parC.dVal[6];
var j = 0;
switch (this.b.atom.getElementNumber ()) {
case 8:
j = 2.0;
break;
case 16:
case 34:
case 52:
case 84:
j = 6.8;
}
if (j != 0) switch (this.c.atom.getElementNumber ()) {
case 8:
h = j;
i = 2.0;
d = 2;
break;
case 16:
case 34:
case 52:
case 84:
h = j;
i = 6.8;
d = 2;
}
e = 0.5 * 4.1868 * Math.sqrt (h * i);
break;
case 4:
c = 1;
d = 2;
e = 0.5 * 4.1868 * 5.0 * Math.sqrt (this.b$["J.minimize.forcefield.CalculationsUFF"].parB.dVal[7] * this.b$["J.minimize.forcefield.CalculationsUFF"].parC.dVal[7]) * (1.0 + 4.18 * Math.log (g));
break;
case 6:
c = 1;
d = 6;
var k = (this.b$["J.minimize.forcefield.CalculationsUFF"].parC.iVal[0] == 3);
switch ((k ? this.c : this.b).atom.getElementNumber ()) {
case 8:
case 16:
case 34:
case 52:
case 84:
switch ((k ? this.b : this.c).atom.getElementNumber ()) {
case 8:
case 16:
case 34:
case 52:
case 84:
break;
default:
d = 2;
c = -1;
}
break;
}
e = 2.0934;
}
if (J.minimize.Util.isNearZero (e)) return;
a.addLast ([[this.ia, this.ib, this.ic, this.id, d], [e, c]]);
}, "J.util.JmolList,~A");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
var b = this.iData[4];
var c = this.dData[0];
var d = this.dData[1];
this.b$["J.minimize.forcefield.CalculationsUFF"].setTorsionVariables (this);
this.energy = c * (1.0 - d * Math.cos (this.theta * b));
if (this.b$["J.minimize.forcefield.CalculationsUFF"].gradients) {
this.dE = c * b * d * Math.sin (b * this.theta);
this.b$["J.minimize.forcefield.CalculationsUFF"].addForces (this, 4);
}if (this.b$["J.minimize.forcefield.CalculationsUFF"].logging) this.b$["J.minimize.forcefield.CalculationsUFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsUFF"].getDebugLine (3, this));
return this.energy;
}, "~A");
c$ = Clazz.p0p ();
};
c$.$CalculationsUFF$OOPCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsUFF, "OOPCalc", J.minimize.forcefield.Calculation);
$_M(c$, "setData", 
function (a, b, c) {
this.b = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[b];
var d = this.b.getBondedAtomIndexes ();
this.a = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.ia = d[0]];
this.c = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.ic = d[1]];
this.d = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[this.id = d[2]];
var e = 1.0;
var f = -1.0;
var g = 0.0;
var h = 25.1208;
switch (c) {
case 6:
if (this.b.sType === "C_2" && this.b.hCount > 1 || this.b.sType === "C_2+" || this.a.sType === "O_2" || this.c.sType === "O_2" || this.d.sType === "O_2") {
h += 184.2192;
break;
}break;
case 7:
case 8:
break;
default:
h = 92.1096;
var i = 0.017453292519943295;
switch (c) {
case 15:
i *= 84.4339;
break;
case 33:
i *= 86.9735;
break;
case 51:
i *= 87.7047;
break;
case 83:
i *= 90.0;
break;
}
var j = Math.cos (i);
e = j * j;
f = -2.0 * j;
g = 1.0;
}
h /= 3.0;
a.addLast ([[this.ia, b, this.ic, this.id], [h, e, f, g, h * 10]]);
a.addLast ([[this.ic, b, this.id, this.ia], [h, e, f, g, h * 10]]);
a.addLast ([[this.id, b, this.ia, this.ic], [h, e, f, g, h * 10]]);
}, "J.util.JmolList,~N,~N");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
var b = (this.b$["J.minimize.forcefield.CalculationsUFF"].isPreliminary ? this.dData[4] : this.dData[0]);
var c = this.dData[1];
var d = this.dData[2];
var e = this.dData[3];
this.b$["J.minimize.forcefield.CalculationsUFF"].setOopVariables (this, true);
var f = Math.cos (this.theta);
this.energy = b * (c + d * f + e * f * f);
if (this.b$["J.minimize.forcefield.CalculationsUFF"].gradients) {
this.dE = b * (d * Math.sin (this.theta) + e * 2.0 * Math.sin (this.theta) * f);
this.b$["J.minimize.forcefield.CalculationsUFF"].addForces (this, 4);
}if (this.b$["J.minimize.forcefield.CalculationsUFF"].logging) this.b$["J.minimize.forcefield.CalculationsUFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsUFF"].getDebugLine (4, this));
return this.energy;
}, "~A");
c$ = Clazz.p0p ();
};
c$.$CalculationsUFF$VDWCalc$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
Clazz.instantialize (this, arguments);
}, J.minimize.forcefield.CalculationsUFF, "VDWCalc", J.minimize.forcefield.Calculations.PairCalc, null, Clazz.innerTypeInstance (J.minimize.forcefield.Calculations.PairCalc, this, null, Clazz.inheritArgs));
Clazz.overrideMethod (c$, "setData", 
function (a, b, c) {
this.a = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[b];
this.b = this.b$["J.minimize.forcefield.CalculationsUFF"].minAtoms[c];
var d = this.b$["J.minimize.forcefield.CalculationsUFF"].getParameter (this.a.sType);
var e = this.b$["J.minimize.forcefield.CalculationsUFF"].getParameter (this.b.sType);
var f = d.dVal[2];
var g = d.dVal[3];
var h = e.dVal[2];
var i = e.dVal[3];
var j = 4.1868 * Math.sqrt (g * i);
var k = Math.sqrt (f * h);
a.addLast ([[b, c], [k, j]]);
}, "J.util.JmolList,~N,~N");
Clazz.overrideMethod (c$, "compute", 
function (a) {
this.getPointers (a);
var b = this.dData[0];
var c = this.dData[1];
this.b$["J.minimize.forcefield.CalculationsUFF"].setPairVariables (this);
var d = b / this.rab;
var e = d * d * d;
e *= e;
this.energy = c * e * (e - 2.0);
if (this.b$["J.minimize.forcefield.CalculationsUFF"].gradients) {
this.dE = c * 12.0 * (1.0 - e) * e * d / b;
this.b$["J.minimize.forcefield.CalculationsUFF"].addForces (this, 2);
}if (this.b$["J.minimize.forcefield.CalculationsUFF"].logging) this.b$["J.minimize.forcefield.CalculationsUFF"].appendLogData (this.b$["J.minimize.forcefield.CalculationsUFF"].getDebugLine (5, this));
return this.energy;
}, "~A");
c$ = Clazz.p0p ();
};
Clazz.defineStatics (c$,
"PAR_R", 0,
"PAR_THETA", 1,
"PAR_X", 2,
"PAR_D", 3,
"PAR_ZETA", 4,
"PAR_Z", 5,
"PAR_V", 6,
"PAR_U", 7,
"PAR_XI", 8,
"PAR_HARD", 9,
"PAR_RADIUS", 10,
"KCAL332", 1390.2842991599998,
"KCAL644", 2696.8016159999997,
"KCAL6", 25.1208,
"KCAL22", 92.1096,
"KCAL44", 184.2192);
});
