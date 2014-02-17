Clazz.declarePackage ("J.adapter.readers.molxyz");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.molxyz.XyzReader", ["java.lang.Float", "J.util.Logger"], function () {
c$ = Clazz.declareType (J.adapter.readers.molxyz, "XyzReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "checkLine", 
function () {
var modelAtomCount = this.parseIntStr (this.line);
if (modelAtomCount == -2147483648) {
this.continuing = false;
return false;
}this.vibrationNumber = ++this.modelNumber;
if (this.desiredVibrationNumber <= 0 ? this.doGetModel (this.modelNumber, null) : this.doGetVibration (this.vibrationNumber)) {
this.readLine ();
this.checkCurrentLineForScript ();
this.atomSetCollection.newAtomSet ();
var name = this.line;
this.readAtoms (modelAtomCount);
this.applySymmetryAndSetTrajectory ();
this.atomSetCollection.setAtomSetName (name);
if (this.isLastModel (this.modelNumber)) {
this.continuing = false;
return false;
}} else {
this.skipAtomSet (modelAtomCount);
}this.discardLinesUntilNonBlank ();
return false;
});
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
this.isTrajectory = false;
this.finalizeReaderASCR ();
});
$_M(c$, "skipAtomSet", 
($fz = function (modelAtomCount) {
this.readLine ();
for (var i = modelAtomCount; --i >= 0; ) this.readLine ();

}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "readAtoms", 
($fz = function (modelAtomCount) {
for (var i = 0; i < modelAtomCount; ++i) {
this.readLine ();
var tokens = this.getTokens ();
if (tokens.length < 4) {
J.util.Logger.warn ("line cannot be read for XYZ atom data: " + this.line);
continue;
}var atom = this.atomSetCollection.addNewAtom ();
this.setElementAndIsotope (atom, tokens[0]);
atom.x = this.parseFloatStr (tokens[1]);
atom.y = this.parseFloatStr (tokens[2]);
atom.z = this.parseFloatStr (tokens[3]);
if (Float.isNaN (atom.x) || Float.isNaN (atom.y) || Float.isNaN (atom.z)) {
J.util.Logger.warn ("line cannot be read for XYZ atom data: " + this.line);
atom.set (0, 0, 0);
}var vpt = 4;
this.setAtomCoord (atom);
switch (tokens.length) {
case 4:
continue;
case 5:
case 6:
case 8:
case 9:
if (tokens[4].indexOf (".") >= 0) {
atom.partialCharge = this.parseFloatStr (tokens[4]);
} else {
var charge = this.parseIntStr (tokens[4]);
if (charge != -2147483648) atom.formalCharge = charge;
}switch (tokens.length) {
case 5:
continue;
case 6:
atom.radius = this.parseFloatStr (tokens[5]);
continue;
case 9:
atom.atomSerial = this.parseIntStr (tokens[8]);
}
vpt++;
default:
var vx = this.parseFloatStr (tokens[vpt++]);
var vy = this.parseFloatStr (tokens[vpt++]);
var vz = this.parseFloatStr (tokens[vpt++]);
if (Float.isNaN (vx) || Float.isNaN (vy) || Float.isNaN (vz)) continue;
this.atomSetCollection.addVibrationVector (atom.index, vx, vy, vz);
}
}
}, $fz.isPrivate = true, $fz), "~N");
});
