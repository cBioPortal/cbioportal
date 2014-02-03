Clazz.declarePackage ("J.adapter.readers.simple");
Clazz.load (["J.adapter.readers.simple.ZMatrixReader"], "J.adapter.readers.simple.MopacArchiveReader", ["java.lang.Float", "J.adapter.smarter.Atom", "J.api.JmolAdapter", "J.util.P3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.energyWithUnits = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.simple, "MopacArchiveReader", J.adapter.readers.simple.ZMatrixReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.atomSetCollection.newAtomSet ();
if (!this.checkFilterKey ("NOCENTER")) this.doCentralize = true;
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
if (this.line.indexOf ("=") == 34) return this.getValue ();
if (this.line.indexOf ("FINAL GEOMETRY OBTAINED") >= 0) return this.readCoordinates ();
return true;
});
$_M(c$, "getValue", 
($fz = function () {
var $private = Clazz.checkPrivateMethod (arguments);
if ($private != null) {
return $private.apply (this, arguments);
}
if (this.line.substring (0, 10).trim ().length != 0) return true;
var key = this.line.substring (0, 34).trim ().$replace (' ', '_');
var value = this.line.substring (35).trim ();
this.atomSetCollection.setAtomSetAuxiliaryInfo (key, value);
if (this.line.indexOf ("TOTAL ENERGY") >= 0) {
var tokens = this.getTokens ();
this.energyWithUnits = " (" + tokens[3] + " " + tokens[4] + ")";
this.atomSetCollection.setAtomSetEnergy (tokens[3], this.parseFloatStr (tokens[3]));
}return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readCoordinates", 
($fz = function () {
this.readLine ();
this.line = this.readLine ().trim ();
this.atomSetCollection.setAtomSetName (this.line + (this.energyWithUnits == null ? "" : this.energyWithUnits));
this.readLine ();
var atom = null;
var sym = null;
this.setFractionalCoordinates (false);
while (this.readLine () != null && this.line.length >= 50) {
this.vAtoms.addLast (atom =  new J.adapter.smarter.Atom ());
atom.x = this.parseFloatRange (this.line, 5, 18);
atom.y = this.parseFloatRange (this.line, 21, 34);
atom.z = this.parseFloatRange (this.line, 37, 50);
if (this.line.length > 58 && this.line.charAt (58) != ' ') {
switch (this.atomCount) {
case 0:
break;
case 1:
atom.sub (this.vAtoms.get (0));
break;
case 2:
this.setAtom (atom, 0, 1, 0, atom.x, atom.y, 3.4028235E38);
break;
default:
this.setAtom (atom, this.parseIntRange (this.line, 54, 59) - 1, this.parseIntRange (this.line, 60, 65) - 1, this.parseIntRange (this.line, 66, 71) - 1, atom.x, atom.y, atom.z);
}
}sym = this.line.substring (1, 3).trim ();
atom.elementSymbol = sym;
if (!sym.equals ("Tv")) {
this.atomCount++;
if (this.line.length >= 84) atom.partialCharge = this.parseFloatRange (this.line, 76, 84);
if (J.api.JmolAdapter.getElementNumber (sym) != 0) this.atomSetCollection.addAtom (atom);
this.setAtomCoord (atom);
}}
if (sym.equals ("Tv")) {
this.setSpaceGroupName ("P1");
var nTv = this.vAtoms.size () - this.atomCount;
for (var i = nTv; i < 3; i++) this.vAtoms.addLast ( new J.adapter.smarter.Atom ());

var xyz =  Clazz.newFloatArray (9, 0);
for (var i = 0; i < 3; i++) {
var j = i * 3;
atom = this.vAtoms.get (this.atomCount + i);
if (!Float.isNaN (atom.x)) {
xyz[j] = atom.x;
xyz[j + 1] = atom.y;
xyz[j + 2] = atom.z;
}this.addPrimitiveLatticeVector (i, xyz, j);
}
for (var i = this.atomCount; --i >= 0; ) this.setAtomCoord (this.vAtoms.get (i));

var ptMax = J.util.P3.new3 (-3.4028235E38, -3.4028235E38, -3.4028235E38);
var ptMin = J.util.P3.new3 (3.4028235E38, 3.4028235E38, 3.4028235E38);
if (this.doCentralize) {
for (var i = this.atomCount; --i >= 0; ) {
atom = this.vAtoms.get (i);
ptMax.x = Math.max (ptMax.x, atom.x);
ptMax.y = Math.max (ptMax.y, atom.y);
ptMax.z = Math.max (ptMax.z, atom.z);
ptMin.x = Math.min (ptMin.x, atom.x);
ptMin.y = Math.min (ptMin.y, atom.y);
ptMin.z = Math.min (ptMin.z, atom.z);
}
var ptCenter =  new J.util.P3 ();
switch (nTv) {
case 3:
ptCenter.x = 0.5;
case 2:
ptCenter.y = 0.5;
case 1:
ptCenter.z = 0.5;
}
ptCenter.scaleAdd2 (-0.5, ptMin, ptCenter);
ptCenter.scaleAdd2 (-0.5, ptMax, ptCenter);
for (var i = this.atomCount; --i >= 0; ) this.vAtoms.get (i).add (ptCenter);

}this.doCentralize = false;
}return true;
}, $fz.isPrivate = true, $fz));
});
