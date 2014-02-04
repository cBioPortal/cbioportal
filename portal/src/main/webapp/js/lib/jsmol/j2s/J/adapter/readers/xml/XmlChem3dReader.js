Clazz.declarePackage ("J.adapter.readers.xml");
Clazz.load (["J.adapter.readers.xml.XmlReader", "J.util.JmolList"], "J.adapter.readers.xml.XmlChem3dReader", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "J.adapter.smarter.Atom", "J.api.Interface", "J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.orbitals = null;
this.moData = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xml, "XmlChem3dReader", J.adapter.readers.xml.XmlReader);
Clazz.prepareFields (c$, function () {
this.orbitals =  new J.util.JmolList ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.adapter.readers.xml.XmlChem3dReader, []);
});
Clazz.overrideMethod (c$, "getDOMAttributes", 
function () {
return ["id", "symbol", "cartCoords", "bondAtom1", "bondAtom2", "bondOrder", "gridDatXDim", "gridDatYDim", "gridDatZDim", "gridDatXSize", "gridDatYSize", "gridDatZSize", "gridDatOrigin", "gridDatData", "calcPartialCharges", "calcAtoms"];
});
Clazz.overrideMethod (c$, "processXml", 
function (parent, saxReader) {
this.PX (parent, saxReader);
this.finalizeMOData (this.moData);
}, "J.adapter.readers.xml.XmlReader,~O");
Clazz.overrideMethod (c$, "processStartElement", 
function (localName) {
var tokens;
if ("model".equals (localName)) {
this.atomSetCollection.newAtomSet ();
return;
}if ("atom".equals (localName)) {
this.atom =  new J.adapter.smarter.Atom ();
this.atom.atomName = this.atts.get ("id");
this.atom.elementSymbol = this.atts.get ("symbol");
if (this.atts.containsKey ("cartCoords")) {
var xyz = this.atts.get ("cartCoords");
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (xyz);
this.atom.set (this.parseFloatStr (tokens[0]), this.parseFloatStr (tokens[1]), this.parseFloatStr (tokens[2]));
}return;
}if ("bond".equals (localName)) {
var atom1 = this.atts.get ("bondAtom1");
var atom2 = this.atts.get ("bondAtom2");
var order = 1;
if (this.atts.containsKey ("bondOrder")) order = this.parseIntStr (this.atts.get ("bondOrder"));
this.atomSetCollection.addNewBondFromNames (atom1, atom2, order);
return;
}if ("electronicStructureCalculation".equalsIgnoreCase (localName)) {
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.atts.get ("calcPartialCharges"));
var tokens2 = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.atts.get ("calcAtoms"));
for (var i = this.parseIntStr (tokens[0]); --i >= 0; ) this.atomSetCollection.mapPartialCharge (tokens2[i + 1], this.parseFloatStr (tokens[i + 1]));

}if ("gridData".equalsIgnoreCase (localName)) {
var nPointsX = this.parseIntStr (this.atts.get ("gridDatXDim"));
var nPointsY = this.parseIntStr (this.atts.get ("gridDatYDim"));
var nPointsZ = this.parseIntStr (this.atts.get ("gridDatZDim"));
var xStep = this.parseFloatStr (this.atts.get ("gridDatXSize")) / (nPointsX);
var yStep = this.parseFloatStr (this.atts.get ("gridDatYSize")) / (nPointsY);
var zStep = this.parseFloatStr (this.atts.get ("gridDatZSize")) / (nPointsZ);
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.atts.get ("gridDatOrigin"));
var ox = this.parseFloatStr (tokens[0]);
var oy = this.parseFloatStr (tokens[1]);
var oz = this.parseFloatStr (tokens[2]);
tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.atts.get ("gridDatData"));
var pt = 1;
var voxelData =  Clazz.newFloatArray (nPointsX, nPointsY, nPointsZ, 0);
var sum = 0;
for (var z = 0; z < nPointsZ; z++) for (var y = 0; y < nPointsY; y++) for (var x = 0; x < nPointsX; x++) {
var f = this.parseFloatStr (tokens[pt++]);
voxelData[x][y][z] = f;
sum += f * f;
}


sum = (1 / Math.sqrt (sum));
for (var z = 0; z < nPointsZ; z++) for (var y = 0; y < nPointsY; y++) for (var x = 0; x < nPointsX; x++) {
voxelData[x][y][z] *= sum;
}


var vd = J.api.Interface.getOptionInterface ("jvxl.data.VolumeData");
vd.setVoxelCounts (nPointsX, nPointsY, nPointsZ);
vd.setVolumetricVector (0, xStep, 0, 0);
vd.setVolumetricVector (1, 0, yStep, 0);
vd.setVolumetricVector (2, 0, 0, zStep);
vd.setVolumetricOrigin (ox, oy, oz);
vd.setVoxelDataAsArray (voxelData);
if (this.moData == null) {
this.moData =  new java.util.Hashtable ();
this.moData.put ("defaultCutoff", Float.$valueOf (0.01));
this.moData.put ("haveVolumeData", Boolean.TRUE);
this.moData.put ("calculationType", "Chem3D");
this.orbitals =  new J.util.JmolList ();
this.moData.put ("mos", this.orbitals);
}var mo =  new java.util.Hashtable ();
mo.put ("volumeData", vd);
this.orbitals.addLast (mo);
J.util.Logger.info ("Chem3D molecular orbital data displayable using ISOSURFACE MO " + this.orbitals.size ());
return;
}}, "~S");
Clazz.overrideMethod (c$, "processEndElement", 
function (localName) {
if ("atom".equals (localName)) {
if (this.atom.elementSymbol != null && !Float.isNaN (this.atom.z)) {
this.parent.setAtomCoord (this.atom);
this.atomSetCollection.addAtomWithMappedName (this.atom);
}this.atom = null;
return;
}this.keepChars = false;
this.chars = null;
}, "~S");
});
