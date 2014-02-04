Clazz.declarePackage ("J.adapter.readers.xml");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.xml.XmlReader", ["java.util.Hashtable", "J.adapter.smarter.AtomSetCollection", "$.Resolver", "J.api.Interface", "J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atom = null;
this.domAttributes = null;
this.parent = null;
this.atts = null;
this.keepChars = false;
this.chars = null;
this.domObj = null;
this.attribs = null;
this.attArgs = null;
this.nullObj = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xml, "XmlReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.prepareFields (c$, function () {
this.domObj =  new Array (1);
this.nullObj =  new Array (0);
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.atts =  new java.util.Hashtable ();
this.setMyError (this.parseXML ());
this.continuing = false;
});
$_M(c$, "setMyError", 
($fz = function (err) {
if (err != null && (this.atomSetCollection == null || this.atomSetCollection.errorMessage == null)) {
this.atomSetCollection =  new J.adapter.smarter.AtomSetCollection ("xml", this, null, null);
this.atomSetCollection.errorMessage = err;
}}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "parseXML", 
($fz = function () {
var saxReader = null;
{
}return this.selectReaderAndGo (saxReader);
}, $fz.isPrivate = true, $fz));
$_M(c$, "selectReaderAndGo", 
($fz = function (saxReader) {
this.atomSetCollection =  new J.adapter.smarter.AtomSetCollection (this.readerName, this, null, null);
var className = null;
var thisReader = null;
try {
var pt = this.readerName.indexOf ("(");
var name = (pt < 0 ? this.readerName : this.readerName.substring (0, pt));
className = J.adapter.smarter.Resolver.getReaderClassBase (name);
var atomSetCollectionReaderClass = Class.forName (className);
thisReader = atomSetCollectionReaderClass.newInstance ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return "File reader was not found: " + className;
} else {
throw e;
}
}
try {
thisReader.processXml (this, saxReader);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return "Error reading XML: " + (this.parent.viewer.isJS ? e : e.getMessage ());
} else {
throw e;
}
}
return null;
}, $fz.isPrivate = true, $fz), "~O");
$_M(c$, "processXml", 
function (parent, saxReader) {
this.PX (parent, saxReader);
}, "J.adapter.readers.xml.XmlReader,~O");
$_M(c$, "PX", 
function (parent, saxReader) {
this.parent = parent;
this.atomSetCollection = parent.atomSetCollection;
this.reader = parent.reader;
this.atts = parent.atts;
if (saxReader == null) {
this.domAttributes = this.getDOMAttributes ();
this.attribs =  new Array (1);
this.attArgs =  new Array (1);
this.domObj =  new Array (1);
{
var s = this.reader.lock.lock;
if (Clazz.instanceOf (s, java.io.BufferedInputStream)) {
s = new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(s.$in.buf));
s = J.io.JmolBinary.StreamToString (s);
}
this.domObj[0] = parent.viewer.applet._createDomNode("xmlReader",s);
this.walkDOMTree();
parent.viewer.applet._createDomNode("xmlReader",null);
}} else {
var saxHandler = J.api.Interface.getOptionInterface ("adapter.readers.xml.XmlHandler");
saxHandler.parseXML (this, saxReader, this.reader);
}}, "J.adapter.readers.xml.XmlReader,~O");
Clazz.overrideMethod (c$, "applySymmetryAndSetTrajectory", 
function () {
try {
if (this.parent == null) this.applySymTrajASCR ();
 else this.parent.applySymmetryAndSetTrajectory ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println ((this.parent == null ? this : this.parent).viewer.isJS ? e : e.getMessage ());
J.util.Logger.error ("applySymmetry failed: " + e);
} else {
throw e;
}
}
});
Clazz.overrideMethod (c$, "processDOM", 
function (DOMNode) {
this.domObj = [DOMNode];
this.setMyError (this.selectReaderAndGo (null));
}, "~O");
$_M(c$, "getDOMAttributes", 
function () {
return ["id"];
});
$_M(c$, "processStartElement", 
function (localName) {
}, "~S");
$_M(c$, "setKeepChars", 
function (TF) {
this.keepChars = TF;
this.chars = null;
}, "~B");
$_M(c$, "processEndElement", 
function (localName) {
}, "~S");
$_M(c$, "walkDOMTree", 
($fz = function () {
var localName;
{
localName = this.jsObjectGetMember(this.domObj,
"nodeName").toLowerCase();
}if (localName.equals ("#text")) {
if (this.keepChars) this.chars = this.jsObjectGetMember (this.domObj, "data");
return;
}this.attribs[0] = this.jsObjectGetMember (this.domObj, "attributes");
this.getDOMAttributesA (this.attribs);
this.processStartElement (localName);
var haveChildren;
{
haveChildren = this.jsObjectCall(this.domObj, "hasChildNodes",
null);
}if (haveChildren) {
var nextNode = this.jsObjectGetMember (this.domObj, "firstChild");
while (nextNode != null) {
this.domObj[0] = nextNode;
this.walkDOMTree ();
this.domObj[0] = nextNode;
nextNode = this.jsObjectGetMember (this.domObj, "nextSibling");
}
}this.processEndElement (localName);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getDOMAttributesA", 
($fz = function (attributes) {
this.atts.clear ();
if (attributes == null) {
return;
}{
if (!this.jsObjectGetMember(attributes, "length")) return;
}var name;
for (var i = this.domAttributes.length; --i >= 0; ) {
this.attArgs[0] = name = this.domAttributes[i];
var att = this.jsObjectCall (attributes, "getNamedItem", this.attArgs);
if (att != null) {
this.attArgs[0] = att;
var attValue = this.jsObjectGetMember (this.attArgs, "value");
if (attValue != null) this.atts.put (name, attValue);
}}
}, $fz.isPrivate = true, $fz), "~A");
$_M(c$, "jsObjectCall", 
($fz = function (jsObject, method, args) {
return this.parent.viewer.getJsObjectInfo (jsObject, method, args == null ? this.nullObj : args);
}, $fz.isPrivate = true, $fz), "~A,~S,~A");
$_M(c$, "jsObjectGetMember", 
($fz = function (jsObject, name) {
return this.parent.viewer.getJsObjectInfo (jsObject, name, null);
}, $fz.isPrivate = true, $fz), "~A,~S");
});
