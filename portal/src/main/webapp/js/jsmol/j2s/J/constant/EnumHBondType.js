Clazz.declarePackage ("J.constant");
Clazz.load (["java.lang.Enum"], "J.constant.EnumHBondType", null, function () {
c$ = Clazz.declareType (J.constant, "EnumHBondType", Enum);
c$.getType = $_M(c$, "getType", 
function (atom) {
var group = atom.getGroup ();
var groupID = group.getGroupID ();
var considerHydrogens = !atom.isHetero ();
switch (atom.getElementNumber ()) {
default:
return J.constant.EnumHBondType.NOT;
case 1:
if (atom.getCovalentBondCount () == 0) return J.constant.EnumHBondType.DONOR;
var bonds = atom.getBonds ();
if (bonds == null) return J.constant.EnumHBondType.NOT;
switch (bonds[0].getOtherAtom (atom).getElementNumber ()) {
case 7:
case 8:
case 16:
return J.constant.EnumHBondType.DONOR;
}
return J.constant.EnumHBondType.NOT;
case 7:
if (atom === group.getNitrogenAtom ()) return J.constant.EnumHBondType.DONOR;
if (groupID == 9) return J.constant.EnumHBondType.UNKNOWN;
if (atom.getCovalentHydrogenCount () > 0) return J.constant.EnumHBondType.DONOR;
if (considerHydrogens) return J.constant.EnumHBondType.ACCEPTOR;
switch (groupID) {
case 2:
case 3:
case 12:
case 6:
case 19:
return J.constant.EnumHBondType.DONOR;
}
return J.constant.EnumHBondType.UNKNOWN;
case 8:
if (atom === group.getCarbonylOxygenAtom () || atom.getFormalCharge () == -1) return J.constant.EnumHBondType.ACCEPTOR;
if (atom.getCovalentBondCount () == 0 || atom.getCovalentHydrogenCount () > 0) return J.constant.EnumHBondType.UNKNOWN;
if (considerHydrogens) return J.constant.EnumHBondType.ACCEPTOR;
switch (groupID) {
case 4:
case 7:
return J.constant.EnumHBondType.ACCEPTOR;
}
return J.constant.EnumHBondType.UNKNOWN;
}
}, "J.modelset.Atom");
c$.isPossibleHBond = $_M(c$, "isPossibleHBond", 
function (typeA, typeB) {
return (typeA === J.constant.EnumHBondType.NOT || typeB === J.constant.EnumHBondType.NOT ? false : typeA === J.constant.EnumHBondType.UNKNOWN || typeA !== typeB);
}, "J.constant.EnumHBondType,J.constant.EnumHBondType");
Clazz.defineEnumConstant (c$, "NOT", 0, []);
Clazz.defineEnumConstant (c$, "ACCEPTOR", 1, []);
Clazz.defineEnumConstant (c$, "DONOR", 2, []);
Clazz.defineEnumConstant (c$, "UNKNOWN", 3, []);
});
