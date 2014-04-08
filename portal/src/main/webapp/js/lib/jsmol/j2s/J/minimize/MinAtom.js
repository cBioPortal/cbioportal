Clazz.declarePackage ("J.minimize");
Clazz.load (["J.util.BS", "$.JmolList"], "J.minimize.MinAtom", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.index = 0;
this.sType = null;
this.atom = null;
this.ffAtomType = null;
this.ffType = 0;
this.vdwKey = null;
this.coord = null;
this.force = null;
this.bonds = null;
this.nBonds = 0;
this.hCount = 0;
this.partialCharge = 0;
this.bsVdw = null;
this.bs14 = null;
this.bondedAtoms = null;
Clazz.instantialize (this, arguments);
}, J.minimize, "MinAtom");
Clazz.prepareFields (c$, function () {
this.coord =  Clazz.newDoubleArray (3, 0);
this.force =  Clazz.newDoubleArray (3, 0);
this.bonds =  new J.util.JmolList ();
this.bsVdw =  new J.util.BS ();
this.bs14 =  new J.util.BS ();
});
Clazz.overrideMethod (c$, "toString", 
function () {
return "#" + this.index + " " + this.sType;
});
Clazz.makeConstructor (c$, 
function (index, atom, coord, atomCount) {
this.index = index;
this.atom = atom;
this.coord = coord;
this.bsVdw.setBits (index + 1, atomCount);
this.bsVdw.clear (index);
this.hCount = atom.getCovalentHydrogenCount ();
}, "~N,J.modelset.Atom,~A,~N");
$_M(c$, "set", 
function () {
this.coord[0] = this.atom.x;
this.coord[1] = this.atom.y;
this.coord[2] = this.atom.z;
});
$_M(c$, "getBondTo", 
function (iAtom) {
this.getBondedAtomIndexes ();
for (var i = 0; i < this.nBonds; i++) if (this.bondedAtoms[i] == iAtom) return this.bonds.get (i);

return null;
}, "~N");
$_M(c$, "getBondedAtomIndexes", 
function () {
if (this.bondedAtoms == null) {
this.bondedAtoms =  Clazz.newIntArray (this.nBonds, 0);
for (var i = this.nBonds; --i >= 0; ) this.bondedAtoms[i] = this.bonds.get (i).getOtherAtom (this.index);

}return this.bondedAtoms;
});
$_M(c$, "getIdentity", 
function () {
return this.atom.getInfo ();
});
$_M(c$, "addBond", 
function (bond, i) {
this.bonds.addLast (bond);
this.nBonds++;
this.bsVdw.clear (i);
}, "J.minimize.MinBond,~N");
$_M(c$, "getBondIndex", 
function (j) {
return this.bonds.get (j).index;
}, "~N");
c$.isLinear = $_M(c$, "isLinear", 
function (minAtom) {
switch (minAtom.ffType) {
case 4:
case 53:
case 61:
return true;
}
return false;
}, "J.minimize.MinAtom");
});
