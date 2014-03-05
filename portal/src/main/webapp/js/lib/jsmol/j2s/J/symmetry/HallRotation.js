Clazz.declarePackage ("J.symmetry");
Clazz.load (["J.util.Matrix4f"], "J.symmetry.HallRotation", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.rotCode = null;
this.seitzMatrix = null;
this.seitzMatrixInv = null;
Clazz.instantialize (this, arguments);
}, J.symmetry, "HallRotation");
Clazz.prepareFields (c$, function () {
this.seitzMatrix =  new J.util.Matrix4f ();
this.seitzMatrixInv =  new J.util.Matrix4f ();
});
Clazz.makeConstructor (c$, 
($fz = function (code, matrixData) {
this.rotCode = code;
var data =  Clazz.newFloatArray (16, 0);
var dataInv =  Clazz.newFloatArray (16, 0);
data[15] = dataInv[15] = 1;
for (var i = 0, ipt = 0; ipt < 11; i++) {
var value = 0;
switch (matrixData.charAt (i)) {
case ' ':
ipt++;
continue;
case '+':
case '1':
value = 1;
break;
case '-':
value = -1;
break;
}
data[ipt] = value;
dataInv[ipt] = -value;
ipt++;
}
this.seitzMatrix.setA (data, 0);
this.seitzMatrixInv.setA (dataInv, 0);
}, $fz.isPrivate = true, $fz), "~S,~S");
c$.lookup = $_M(c$, "lookup", 
function (code) {
for (var i = J.symmetry.HallRotation.getHallTerms ().length; --i >= 0; ) if (J.symmetry.HallRotation.hallRotationTerms[i].rotCode.equals (code)) return J.symmetry.HallRotation.hallRotationTerms[i];

return null;
}, "~S");
c$.getHallTerms = $_M(c$, "getHallTerms", 
($fz = function () {
return (J.symmetry.HallRotation.hallRotationTerms == null ? ($t$ = J.symmetry.HallRotation.hallRotationTerms = [ new J.symmetry.HallRotation ("1_", "+00 0+0 00+"),  new J.symmetry.HallRotation ("2x", "+00 0-0 00-"),  new J.symmetry.HallRotation ("2y", "-00 0+0 00-"),  new J.symmetry.HallRotation ("2z", "-00 0-0 00+"),  new J.symmetry.HallRotation ("2\'", "0-0 -00 00-"),  new J.symmetry.HallRotation ("2\"", "0+0 +00 00-"),  new J.symmetry.HallRotation ("2x\'", "-00 00- 0-0"),  new J.symmetry.HallRotation ("2x\"", "-00 00+ 0+0"),  new J.symmetry.HallRotation ("2y\'", "00- 0-0 -00"),  new J.symmetry.HallRotation ("2y\"", "00+ 0-0 +00"),  new J.symmetry.HallRotation ("2z\'", "0-0 -00 00-"),  new J.symmetry.HallRotation ("2z\"", "0+0 +00 00-"),  new J.symmetry.HallRotation ("3x", "+00 00- 0+-"),  new J.symmetry.HallRotation ("3y", "-0+ 0+0 -00"),  new J.symmetry.HallRotation ("3z", "0-0 +-0 00+"),  new J.symmetry.HallRotation ("3*", "00+ +00 0+0"),  new J.symmetry.HallRotation ("4x", "+00 00- 0+0"),  new J.symmetry.HallRotation ("4y", "00+ 0+0 -00"),  new J.symmetry.HallRotation ("4z", "0-0 +00 00+"),  new J.symmetry.HallRotation ("6x", "+00 0+- 0+0"),  new J.symmetry.HallRotation ("6y", "00+ 0+0 -0+"),  new J.symmetry.HallRotation ("6z", "+-0 +00 00+")], J.symmetry.HallRotation.prototype.hallRotationTerms = J.symmetry.HallRotation.hallRotationTerms, $t$) : J.symmetry.HallRotation.hallRotationTerms);
}, $fz.isPrivate = true, $fz));
Clazz.defineStatics (c$,
"hallRotationTerms", null);
});
