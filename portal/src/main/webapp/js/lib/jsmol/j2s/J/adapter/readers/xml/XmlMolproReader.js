Clazz.declarePackage ("J.adapter.readers.xml");
Clazz.load (["J.adapter.readers.xml.XmlCmlReader"], "J.adapter.readers.xml.XmlMolproReader", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.myAttributes = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xml, "XmlMolproReader", J.adapter.readers.xml.XmlCmlReader);
Clazz.prepareFields (c$, function () {
this.myAttributes = ["id", "length", "type", "x3", "y3", "z3", "elementType", "name", "groups", "cartesianLength", "primitives", "minL", "maxL", "angular", "contractions", "occupation", "energy", "symmetryID", "wavenumber", "units"];
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.adapter.readers.xml.XmlMolproReader, []);
});
Clazz.overrideMethod (c$, "getDOMAttributes", 
function () {
return this.myAttributes;
});
Clazz.overrideMethod (c$, "processStartElement", 
function (localName) {
if (!this.processing) return;
this.processStart2 (localName);
if (localName.equalsIgnoreCase ("normalCoordinate")) {
this.keepChars = false;
if (!this.parent.doGetVibration (++this.vibrationNumber)) return;
try {
this.asc.cloneLastAtomSet ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println ("" + e);
this.asc.errorMessage = "Error processing normalCoordinate: " + e.getMessage ();
this.vibrationNumber = 0;
return;
} else {
throw e;
}
}
if (this.atts.containsKey ("wavenumber")) {
var wavenumber = this.atts.get ("wavenumber");
var units = "cm^-1";
if (this.atts.containsKey ("units")) {
units = this.atts.get ("units");
if (units.startsWith ("inverseCent")) units = "cm^-1";
}this.asc.setAtomSetFrequency (null, null, wavenumber, units);
this.keepChars = true;
}return;
}if (localName.equals ("vibrations")) {
this.vibrationNumber = 0;
return;
}}, "~S");
Clazz.overrideMethod (c$, "processEndElement", 
function (localName) {
if (localName.equalsIgnoreCase ("normalCoordinate")) {
if (!this.keepChars) return;
var ac = this.asc.getLastAtomSetAtomCount ();
var baseAtomIndex = this.asc.getLastAtomSetAtomIndex ();
this.tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.chars);
for (var offset = this.tokens.length - ac * 3, i = 0; i < ac; i++) {
this.asc.addVibrationVector (i + baseAtomIndex, this.parseFloatStr (this.tokens[offset++]), this.parseFloatStr (this.tokens[offset++]), this.parseFloatStr (this.tokens[offset++]));
}
}this.processEnd2 (localName);
}, "~S");
});
