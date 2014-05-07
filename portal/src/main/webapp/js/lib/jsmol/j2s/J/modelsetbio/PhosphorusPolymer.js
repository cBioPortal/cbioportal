Clazz.declarePackage ("J.modelsetbio");
Clazz.load (["J.modelsetbio.BioPolymer"], "J.modelsetbio.PhosphorusPolymer", null, function () {
c$ = Clazz.declareType (J.modelsetbio, "PhosphorusPolymer", J.modelsetbio.BioPolymer);
$_M(c$, "getPdbData", 
function (viewer, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten) {
J.modelsetbio.BioPolymer.getPdbData (viewer, this, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten);
}, "J.viewer.Viewer,~S,~S,~N,~N,J.util.BS,J.util.BS,~B,~B,~B,~A,J.io.OutputStringBuilder,J.util.SB,J.util.BS");
});
