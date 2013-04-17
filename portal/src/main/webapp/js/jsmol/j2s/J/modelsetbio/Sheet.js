Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.ProteinStructure"], "J.modelsetbio.Sheet", ["J.constant.EnumStructure", "J.util.Measure", "$.P3", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.alphaPolymer = null;
this.widthUnitVector = null;
this.heightUnitVector = null;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "Sheet", J.modelsetbio.ProteinStructure);
Clazz.makeConstructor (c$, 
function (alphaPolymer, monomerIndex, monomerCount, subtype) {
Clazz.superConstructor (this, J.modelsetbio.Sheet, []);
this.setupPS (alphaPolymer, J.constant.EnumStructure.SHEET, monomerIndex, monomerCount);
this.alphaPolymer = alphaPolymer;
this.subtype = subtype;
}, "J.modelsetbio.AlphaPolymer,~N,~N,J.constant.EnumStructure");
Clazz.overrideMethod (c$, "calcAxis", 
function () {
if (this.axisA != null) return;
if (this.monomerCount == 2) {
this.axisA = this.alphaPolymer.getLeadPoint (this.monomerIndexFirst);
this.axisB = this.alphaPolymer.getLeadPoint (this.monomerIndexFirst + 1);
} else {
this.axisA =  new J.util.P3 ();
this.alphaPolymer.getLeadMidPoint (this.monomerIndexFirst + 1, this.axisA);
this.axisB =  new J.util.P3 ();
this.alphaPolymer.getLeadMidPoint (this.monomerIndexFirst + this.monomerCount - 1, this.axisB);
}this.axisUnitVector =  new J.util.V3 ();
this.axisUnitVector.sub2 (this.axisB, this.axisA);
this.axisUnitVector.normalize ();
var tempA =  new J.util.P3 ();
this.alphaPolymer.getLeadMidPoint (this.monomerIndexFirst, tempA);
if (this.lowerNeighborIsHelixOrSheet ()) {
} else {
J.util.Measure.projectOntoAxis (tempA, this.axisA, this.axisUnitVector, this.vectorProjection);
}var tempB =  new J.util.P3 ();
this.alphaPolymer.getLeadMidPoint (this.monomerIndexFirst + this.monomerCount, tempB);
if (this.upperNeighborIsHelixOrSheet ()) {
} else {
J.util.Measure.projectOntoAxis (tempB, this.axisA, this.axisUnitVector, this.vectorProjection);
}this.axisA = tempA;
this.axisB = tempB;
});
$_M(c$, "calcSheetUnitVectors", 
function () {
if (!(Clazz.instanceOf (this.alphaPolymer, J.modelsetbio.AminoPolymer))) return;
if (this.widthUnitVector == null) {
var vectorCO =  new J.util.V3 ();
var vectorCOSum =  new J.util.V3 ();
var amino = this.alphaPolymer.monomers[this.monomerIndexFirst];
vectorCOSum.sub2 (amino.getCarbonylOxygenAtom (), amino.getCarbonylCarbonAtom ());
for (var i = this.monomerCount; --i > this.monomerIndexFirst; ) {
amino = this.alphaPolymer.monomers[i];
vectorCO.sub2 (amino.getCarbonylOxygenAtom (), amino.getCarbonylCarbonAtom ());
if (vectorCOSum.angle (vectorCO) < 1.5707964) vectorCOSum.add (vectorCO);
 else vectorCOSum.sub (vectorCO);
}
this.heightUnitVector = vectorCO;
this.heightUnitVector.cross (this.axisUnitVector, vectorCOSum);
this.heightUnitVector.normalize ();
this.widthUnitVector = vectorCOSum;
this.widthUnitVector.cross (this.axisUnitVector, this.heightUnitVector);
}});
$_M(c$, "getWidthUnitVector", 
function () {
if (this.widthUnitVector == null) this.calcSheetUnitVectors ();
return this.widthUnitVector;
});
$_M(c$, "getHeightUnitVector", 
function () {
if (this.heightUnitVector == null) this.calcSheetUnitVectors ();
return this.heightUnitVector;
});
});
