Clazz.declarePackage ("JS");
Clazz.load (null, "JS.SymmetryInfo", ["JU.Lst", "$.PT", "JU.SimpleUnitCell"], function () {
c$ = Clazz.decorateAsClass (function () {
this.coordinatesAreFractional = false;
this.isMultiCell = false;
this.sgName = null;
this.symmetryOperations = null;
this.infoStr = null;
this.cellRange = null;
this.periodicOriginXyz = null;
this.centerings = null;
Clazz.instantialize (this, arguments);
}, JS, "SymmetryInfo");
Clazz.defineMethod (c$, "isPeriodic", 
function () {
return this.periodicOriginXyz != null;
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.defineMethod (c$, "setSymmetryInfo", 
function (info, notionalUnitCell) {
this.cellRange = info.get ("unitCellRange");
this.periodicOriginXyz = info.get ("periodicOriginXyz");
this.sgName = info.get ("spaceGroup");
if (this.sgName == null || this.sgName === "") this.sgName = "spacegroup unspecified";
var symmetryCount = info.containsKey ("symmetryCount") ? (info.get ("symmetryCount")).intValue () : 0;
this.symmetryOperations = info.remove ("symmetryOps");
this.infoStr = "Spacegroup: " + this.sgName;
if (this.symmetryOperations == null) {
this.infoStr += "\nNumber of symmetry operations: ?\nSymmetry Operations: unspecified\n";
} else {
this.centerings =  new JU.Lst ();
var c = "";
var s = "\nNumber of symmetry operations: " + (symmetryCount == 0 ? 1 : symmetryCount) + "\nSymmetry Operations:";
for (var i = 0; i < symmetryCount; i++) {
var op = this.symmetryOperations[i];
s += "\n" + op.xyz;
if (op.isCenteringOp) {
this.centerings.addLast (op.centering);
var oc = JU.PT.replaceAllCharacters (op.xyz, "xyz", "0");
c += " (" + JU.PT.rep (oc, "0+", "") + ")";
}}
if (c.length > 0) this.infoStr += "\nCentering: " + c;
this.infoStr += s;
}this.infoStr += "\n";
if (notionalUnitCell == null) notionalUnitCell = info.get ("notionalUnitcell");
if (!JU.SimpleUnitCell.isValid (notionalUnitCell)) return null;
this.coordinatesAreFractional = info.containsKey ("coordinatesAreFractional") ? (info.get ("coordinatesAreFractional")).booleanValue () : false;
this.isMultiCell = (this.coordinatesAreFractional && this.symmetryOperations != null);
return notionalUnitCell;
}, "java.util.Map,~A");
});
