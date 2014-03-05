Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.Monomer"], "J.modelsetbio.CarbohydrateMonomer", ["J.constant.EnumStructure"], function () {
c$ = Clazz.declareType (J.modelsetbio, "CarbohydrateMonomer", J.modelsetbio.Monomer);
Clazz.overrideConstructor (c$, 
($fz = function () {
}, $fz.isPrivate = true, $fz));
c$.validateAndAllocate = $_M(c$, "validateAndAllocate", 
function (chain, group3, seqcode, firstIndex, lastIndex) {
return  new J.modelsetbio.CarbohydrateMonomer ().set2 (chain, group3, seqcode, firstIndex, lastIndex, J.modelsetbio.CarbohydrateMonomer.alphaOffsets);
}, "J.modelset.Chain,~S,~N,~N,~N");
Clazz.overrideMethod (c$, "isCarbohydrate", 
function () {
return true;
});
Clazz.overrideMethod (c$, "getProteinStructureType", 
function () {
return J.constant.EnumStructure.CARBOHYDRATE;
});
Clazz.overrideMethod (c$, "isConnectedAfter", 
function (possiblyPreviousMonomer) {
if (possiblyPreviousMonomer == null) return true;
for (var i = this.firstAtomIndex; i <= this.lastAtomIndex; i++) for (var j = possiblyPreviousMonomer.firstAtomIndex; j <= possiblyPreviousMonomer.lastAtomIndex; j++) {
var a = this.chain.getAtom (i);
var b = this.chain.getAtom (j);
if (a.getElementNumber () + b.getElementNumber () == 14 && a.distanceSquared (b) < 3.24) return true;
}

return false;
}, "J.modelsetbio.Monomer");
Clazz.overrideMethod (c$, "findNearestAtomIndex", 
function (x, y, closest, madBegin, madEnd) {
var competitor = closest[0];
var anomericO = this.getLeadAtom ();
var marBegin = (Clazz.doubleToInt (madBegin / 2));
if (marBegin < 1200) marBegin = 1200;
if (anomericO.screenZ == 0) return;
var radiusBegin = Clazz.floatToInt (this.scaleToScreen (anomericO.screenZ, marBegin));
if (radiusBegin < 4) radiusBegin = 4;
if (this.isCursorOnTopOf (anomericO, x, y, radiusBegin, competitor)) closest[0] = anomericO;
}, "~N,~N,~A,~N,~N");
Clazz.overrideMethod (c$, "isConnectedPrevious", 
function () {
if (this.monomerIndex <= 0) return false;
for (var i = this.firstAtomIndex; i <= this.lastAtomIndex; i++) {
if (this.getCrossLink (i, null)) return true;
}
return false;
});
Clazz.defineStatics (c$,
"alphaOffsets", [0]);
});
