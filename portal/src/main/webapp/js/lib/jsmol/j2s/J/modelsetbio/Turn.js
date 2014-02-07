Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.ProteinStructure"], "J.modelsetbio.Turn", ["J.constant.EnumStructure"], function () {
c$ = Clazz.declareType (J.modelsetbio, "Turn", J.modelsetbio.ProteinStructure);
Clazz.makeConstructor (c$, 
function (apolymer, monomerIndex, monomerCount) {
Clazz.superConstructor (this, J.modelsetbio.Turn, []);
this.setupPS (apolymer, J.constant.EnumStructure.TURN, monomerIndex, monomerCount);
this.subtype = J.constant.EnumStructure.TURN;
}, "J.modelsetbio.AlphaPolymer,~N,~N");
});
