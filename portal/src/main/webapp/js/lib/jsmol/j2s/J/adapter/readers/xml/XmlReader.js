Clazz.declarePackage ("J.adapter.readers.xml");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.xml.XmlReader", ["java.io.BufferedInputStream", "java.util.Hashtable", "JU.Rdr", "J.adapter.smarter.AtomSetCollection", "$.Resolver", "J.api.Interface", "JU.Logger"], function () {
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
Clazz.defineMethod (c$, "setMyError", 
 function (err) {
if (err != null && (this.asc == null || this.asc.errorMessage == null)) {
this.asc =  new J.adapter.smarter.AtomSetCollection ("xml", this, null, null);
this.asc.errorMessage = err;
}}, "~S");
Clazz.defineMethod (c$, "parseXML", 
 function () {
var saxReader = null;
{
}return this.selectReaderAndGo (saxReader);
});
Clazz.defineMethod (c$, "selectReaderAndGo", 
 function (saxReader) {
this.asc =  new J.adapter.smarter.AtomSetCollection (this.readerName, this, null, null);
var className = null;
var thisReader = null;
var pt = this.readerName.indexOf ("(");
var name = (pt < 0 ? this.readerName : this.readerName.substring (0, pt));
className = J.adapter.smarter.Resolver.getReaderClassBase (name);
if ((thisReader = J.api.Interface.getInterface (className)) == null) return "File reader was not found: " + className;
try {
thisReader.processXml (this, saxReader);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return "Error reading XML: " + (this.parent.vwr.isJS ? e : e.getMessage ());
} else {
throw e;
}
}
return null;
}, "~O");
Clazz.defineMethod (c$, "processXml", 
function (parent, saxReader) {
this.PX (parent, saxReader);
}, "J.adapter.readers.xml.XmlReader,~O");
Clazz.defineMethod (c$, "PX", 
function (parent, saxReader) {
this.parent = parent;
this.asc = parent.asc;
this.reader = parent.reader;
this.atts = parent.atts;
if (saxReader == null) {
this.domAttributes = this.getDOMAttributes ();
this.attribs =  new Array (1);
this.attArgs =  new Array (1);
this.domObj =  new Array (1);
var o = "";
var data = null;
{
o = this.reader.lock.lock; if (o.$in) data = o.$in.buf;
}if (Clazz.instanceOf (o, java.io.BufferedInputStream)) o = JU.Rdr.StreamToUTF8String (JU.Rdr.getBIS (data));
{
this.domObj[0] =
parent.vwr.html5Applet._createDomNode("xmlReader",o);
this.walkDOMTree();
parent.vwr.html5Applet._createDomNode("xmlReader",null);
}} else {
var saxHandler = J.api.Interface.getOption ("adapter.readers.xml.XmlHandler");
saxHandler.parseXML (this, saxReader, this.reader);
}}, "J.adapter.readers.xml.XmlReader,~O");
Clazz.overrideMethod (c$, "applySymmetryAndSetTrajectory", 
function () {
try {
if (this.parent == null) this.applySymTrajASCR ();
 else this.parent.applySymmetryAndSetTrajectory ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println ((this.parent == null ? this : this.parent).vwr.isJS ? e : e.getMessage ());
JU.Logger.error ("applySymmetry failed: " + e);
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
Clazz.defineMethod (c$, "getDOMAttributes", 
function () {
return ["id"];
});
Clazz.defineMethod (c$, "processStartElement", 
function (localName) {
}, "~S");
Clazz.defineMethod (c$, "setKeepChars", 
function (TF) {
this.keepChars = TF;
this.chars = null;
}, "~B");
Clazz.defineMethod (c$, "processEndElement", 
function (localName) {
}, "~S");
Clazz.defineMethod (c$, "walkDOMTree", 
 function () {
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
});
Clazz.defineMethod (c$, "getDOMAttributesA", 
 function (attributes) {
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
}, "~A");
Clazz.defineMethod (c$, "jsObjectCall", 
 function (jsObject, method, args) {
return this.parent.vwr.getJsObjectInfo (jsObject, method, args == null ? this.nullObj : args);
}, "~A,~S,~A");
Clazz.defineMethod (c$, "jsObjectGetMember", 
 function (jsObject, name) {
return this.parent.vwr.getJsObjectInfo (jsObject, name, null);
}, "~A,~S");
});
