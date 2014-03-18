Clazz.declarePackage ("J.jvxl.readers");
Clazz.load (["J.jvxl.readers.AtomDataReader"], "J.jvxl.readers.IsoMepReader", ["J.api.Interface"], function () {
c$ = Clazz.decorateAsClass (function () {
this.type = null;
Clazz.instantialize (this, arguments);
}, J.jvxl.readers, "IsoMepReader", J.jvxl.readers.AtomDataReader);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.jvxl.readers.IsoMepReader, []);
});
Clazz.overrideMethod (c$, "init", 
function (sg) {
this.initIMR (sg);
}, "J.jvxl.readers.SurfaceGenerator");
$_M(c$, "initIMR", 
function (sg) {
this.initADR (sg);
this.type = "Mep";
}, "J.jvxl.readers.SurfaceGenerator");
Clazz.overrideMethod (c$, "setup", 
function (isMapData) {
this.setup2 ();
this.doAddHydrogens = false;
this.getAtoms (this.params.bsSelected, this.doAddHydrogens, true, false, false, false, false, this.params.mep_marginAngstroms);
this.setHeader ("MEP", "");
this.setRanges (this.params.mep_ptsPerAngstrom, this.params.mep_gridMax, 0);
}, "~B");
Clazz.overrideMethod (c$, "generateCube", 
function () {
this.newVoxelDataCube ();
var m = J.api.Interface.getOptionInterface ("quantum." + this.type + "Calculation");
m.calculate (this.volumeData, this.bsMySelected, this.atomData.atomXyz, this.params.theProperty, this.params.mep_calcType);
});
});
