Clazz.declarePackage ("J.adapter.readers.xtal");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.xtal.AbinitReader", ["J.util.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.cellLattice = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xtal, "AbinitReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.setSpaceGroupName ("P1");
this.doApplySymmetry = true;
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.contains ("Real(R)+Recip(G)")) {
this.readIntiallattice ();
}return true;
});
$_M(c$, "readIntiallattice", 
($fz = function () {
this.cellLattice =  Clazz.newFloatArray (9, 0);
var data = "";
var counter = 0;
while (this.readLine () != null && this.line.indexOf ("Unit cell volume") < 0) {
data += this.line;
data = J.util.TextFormat.simpleReplace (data, "=", "= ");
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (data);
this.cellLattice[counter++] = this.parseFloatStr (tokens[1]) * 0.5291772;
this.cellLattice[counter++] = this.parseFloatStr (tokens[2]) * 0.5291772;
this.cellLattice[counter++] = this.parseFloatStr (tokens[3]) * 0.5291772;
counter++;
}
this.setSymmetry ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "setSymmetry", 
($fz = function () {
this.applySymmetryAndSetTrajectory ();
this.setSpaceGroupName ("P1");
this.setFractionalCoordinates (false);
}, $fz.isPrivate = true, $fz));
});
