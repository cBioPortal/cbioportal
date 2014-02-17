Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.SlaterReader"], "J.adapter.readers.quantum.MopacSlaterReader", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.atomicNumbers = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum, "MopacSlaterReader", J.adapter.readers.quantum.SlaterReader);
$_M(c$, "createSphericalSlaterByType", 
function (iAtom, atomicNumber, type, zeta, coef) {
var pt = "S Px Py Pz  Dx2-y2Dxz Dz2 Dyz Dxy".indexOf (type);
switch (pt) {
case 0:
this.addSlater (iAtom, 0, 0, 0, J.adapter.readers.quantum.MopacSlaterReader.getNPQs (atomicNumber) - 1, zeta, coef);
return;
case 2:
case 5:
case 8:
this.addSlater (iAtom, pt == 2 ? 1 : 0, pt == 5 ? 1 : 0, pt == 8 ? 1 : 0, J.adapter.readers.quantum.MopacSlaterReader.getNPQp (atomicNumber) - 2, zeta, coef);
return;
}
pt = (pt >> 2) * 3 - 9;
this.addSlater (iAtom, J.adapter.readers.quantum.MopacSlaterReader.sphericalDValues[pt++], J.adapter.readers.quantum.MopacSlaterReader.sphericalDValues[pt++], J.adapter.readers.quantum.MopacSlaterReader.sphericalDValues[pt++], J.adapter.readers.quantum.MopacSlaterReader.getNPQd (atomicNumber) - 3, zeta, coef);
}, "~N,~N,~S,~N,~N");
$_M(c$, "scaleSlater", 
function (ex, ey, ez, er, zeta) {
if (ex >= 0 && ey >= 0) {
return Clazz.superCall (this, J.adapter.readers.quantum.MopacSlaterReader, "scaleSlater", [ex, ey, ez, er, zeta]);
}var el = Math.abs (ex + ey + ez);
if (el == 3) {
return 0;
}return J.adapter.readers.quantum.SlaterReader.getSlaterConstDSpherical (el + er + 1, Math.abs (zeta), ex, ey);
}, "~N,~N,~N,~N,~N");
c$.getNPQ = $_M(c$, "getNPQ", 
($fz = function (atomicNumber) {
return (atomicNumber < J.adapter.readers.quantum.MopacSlaterReader.principalQuantumNumber.length ? J.adapter.readers.quantum.MopacSlaterReader.principalQuantumNumber[atomicNumber] : 0);
}, $fz.isPrivate = true, $fz), "~N");
c$.getNPQs = $_M(c$, "getNPQs", 
($fz = function (atomicNumber) {
var n = J.adapter.readers.quantum.MopacSlaterReader.getNPQ (atomicNumber);
switch (atomicNumber) {
case 10:
case 18:
case 36:
case 54:
case 86:
return n + 1;
default:
return n;
}
}, $fz.isPrivate = true, $fz), "~N");
c$.getNPQp = $_M(c$, "getNPQp", 
($fz = function (atomicNumber) {
var n = J.adapter.readers.quantum.MopacSlaterReader.getNPQ (atomicNumber);
switch (atomicNumber) {
case 2:
return n + 1;
default:
return n;
}
}, $fz.isPrivate = true, $fz), "~N");
c$.getNPQd = $_M(c$, "getNPQd", 
($fz = function (atomicNumber) {
return (atomicNumber < J.adapter.readers.quantum.MopacSlaterReader.npqd.length ? J.adapter.readers.quantum.MopacSlaterReader.npqd[atomicNumber] : 0);
}, $fz.isPrivate = true, $fz), "~N");
Clazz.defineStatics (c$,
"MIN_COEF", 0.0001,
"sphericalDValues", [0, -2, 0, 1, 0, 1, -2, 0, 0, 0, 1, 1, 1, 1, 0],
"principalQuantumNumber", [0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6],
"npqd", [0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 7]);
});
