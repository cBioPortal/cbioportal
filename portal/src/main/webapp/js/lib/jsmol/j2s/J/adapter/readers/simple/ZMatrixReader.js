Clazz.declarePackage ("J.adapter.readers.simple");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader", "java.util.Hashtable", "JU.Lst", "$.P3", "$.P4", "$.V3"], "J.adapter.readers.simple.ZMatrixReader", ["java.lang.Character", "$.Exception", "$.Float", "JU.Measure", "$.Quat", "J.adapter.smarter.Atom", "$.Bond", "J.api.JmolAdapter", "JU.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.ac = 0;
this.vAtoms = null;
this.atomMap = null;
this.tokens = null;
this.isJmolZformat = false;
this.lineBuffer = null;
this.symbolicMap = null;
this.isMopac = false;
this.isHeader = true;
this.pt0 = null;
this.v1 = null;
this.v2 = null;
this.plane1 = null;
this.plane2 = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.simple, "ZMatrixReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.prepareFields (c$, function () {
this.vAtoms =  new JU.Lst ();
this.atomMap =  new java.util.Hashtable ();
this.lineBuffer =  new JU.Lst ();
this.symbolicMap =  new java.util.Hashtable ();
this.pt0 =  new JU.P3 ();
this.v1 =  new JU.V3 ();
this.v2 =  new JU.V3 ();
this.plane1 =  new JU.P4 ();
this.plane2 =  new JU.P4 ();
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
this.cleanLine ();
if (this.line.length <= 2) this.isHeader = false;
if (this.line.startsWith ("#") || this.isMopac && this.isHeader) {
if (this.line.startsWith ("#ZMATRIX")) this.isJmolZformat = this.line.toUpperCase ().indexOf ("GAUSSIAN") < 0 && !(this.isMopac = (this.line.toUpperCase ().indexOf ("MOPAC") >= 0));
this.checkCurrentLineForScript ();
return true;
}if (this.line.indexOf ("#") >= 0) this.line = this.line.substring (0, this.line.indexOf ("#"));
if (this.line.indexOf (":") >= 0) return true;
this.tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line);
if (this.tokens.length == 2) {
this.getSymbolic ();
return true;
}this.lineBuffer.addLast (this.tokens);
return true;
});
Clazz.defineMethod (c$, "cleanLine", 
 function () {
this.line = this.line.$replace (',', ' ');
var pt1;
var pt2;
while ((pt1 = this.line.indexOf ('(')) >= 0 && (pt2 = this.line.indexOf ('(', pt1)) >= 0) this.line = this.line.substring (0, pt1) + " " + this.line.substring (pt2 + 1);

this.line = this.line.trim ();
});
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
var firstLine = 0;
for (var i = firstLine; i < this.lineBuffer.size (); i++) if ((this.tokens = this.lineBuffer.get (i)).length > 0) this.getAtom ();

this.finalizeReaderASCR ();
});
Clazz.defineMethod (c$, "getSymbolic", 
 function () {
if (this.symbolicMap.containsKey (this.tokens[0])) return;
var f = this.parseFloatStr (this.tokens[1]);
this.symbolicMap.put (this.tokens[0], Float.$valueOf (f));
JU.Logger.info ("symbolic " + this.tokens[0] + " = " + f);
});
Clazz.defineMethod (c$, "getAtom", 
 function () {
var f;
var atom =  new J.adapter.smarter.Atom ();
var element = this.tokens[0];
var i = element.length;
while (--i >= 0 && Character.isDigit (element.charAt (i))) {
}
if (++i == 0) throw  new Exception ("Bad Z-matrix atom name");
if (i == element.length) {
atom.atomName = element + (this.ac + 1);
} else {
atom.atomName = element;
element = element.substring (0, i);
}if (this.isMopac && i != this.tokens[0].length) element = this.tokens[0].substring (i) + element;
this.setElementAndIsotope (atom, element);
var ia = this.getAtomIndex (1);
var bondOrder = 0;
switch (this.tokens.length) {
case 8:
case 6:
bondOrder = Clazz.floatToInt (this.getValue (this.tokens.length - 1));
case 5:
if (this.tokens.length == 5 && this.tokens[1].equals ("0")) {
atom.set (this.getValue (2), this.getValue (3), this.getValue (4));
bondOrder = 0;
break;
}case 7:
var ib;
var ic;
if (this.tokens.length < 7 && this.ac != 2 || (ib = this.getAtomIndex (3)) < 0 || (ic = (this.tokens.length < 7 ? -2 : this.getAtomIndex (5))) == -1) {
atom = null;
} else {
var d = this.getValue (2);
var theta1 = this.getValue (4);
var theta2 = (this.tokens.length < 7 ? 3.4028235E38 : this.getValue (6));
if (this.tokens.length == 8 && !this.isJmolZformat && !this.isMopac && bondOrder == 1) d = -Math.abs (d);
atom = this.setAtom (atom, ia, ib, ic, d, theta1, theta2);
}break;
case 4:
if (this.getAtomIndex (1) < 0) {
atom.set (this.getValue (1), this.getValue (2), this.getValue (3));
break;
}bondOrder = Clazz.floatToInt (this.getValue (3));
case 3:
f = this.getValue (2);
if (this.ac != 1 || (ia = this.getAtomIndex (1)) != 0) {
atom = null;
} else {
atom.set (f, 0, 0);
}break;
case 1:
if (this.ac != 0) atom = null;
 else atom.set (0, 0, 0);
break;
default:
atom = null;
}
if (atom == null) throw  new Exception ("bad Z-Matrix line");
this.vAtoms.addLast (atom);
this.atomMap.put (atom.atomName, Integer.$valueOf (this.ac));
this.ac++;
if (element.startsWith ("X") && J.api.JmolAdapter.getElementNumber (element) < 1) {
JU.Logger.info ("#dummy atom ignored: atom " + this.ac + " - " + atom.atomName);
} else {
this.asc.addAtom (atom);
this.setAtomCoord (atom);
JU.Logger.info (atom.atomName + " " + atom.x + " " + atom.y + " " + atom.z);
if (this.isJmolZformat && bondOrder > 0) this.asc.addBond ( new J.adapter.smarter.Bond (atom.index, this.vAtoms.get (ia).index, bondOrder));
}});
Clazz.defineMethod (c$, "getSymbolic", 
 function (key) {
var isNeg = key.startsWith ("-");
var F = this.symbolicMap.get (isNeg ? key.substring (1) : key);
if (F == null) return NaN;
var f = F.floatValue ();
return (isNeg ? -f : f);
}, "~S");
Clazz.defineMethod (c$, "getValue", 
 function (i) {
var f = this.getSymbolic (this.tokens[i]);
if (Float.isNaN (f)) f = this.parseFloatStr (this.tokens[i]);
if (Float.isNaN (f)) throw  new Exception ("Bad Z-matrix value: " + this.tokens[i]);
return f;
}, "~N");
Clazz.defineMethod (c$, "getAtomIndex", 
 function (i) {
var name;
if (i >= this.tokens.length || (name = this.tokens[i]).indexOf (".") >= 0 || !Character.isLetterOrDigit (name.charAt (0))) return -1;
var ia = this.parseIntStr (name);
if (ia <= 0 || name.length != ("" + ia).length) {
var I = this.atomMap.get (name);
if (I == null) {
for (i = this.vAtoms.size (); --i >= 0; ) {
var atom = this.vAtoms.get (i);
if (atom.atomName.startsWith (name) && atom.atomName.length > name.length && Character.isDigit (atom.atomName.charAt (name.length))) {
I = this.atomMap.get (atom.atomName);
break;
}}
}if (I == null) ia = -1;
 else ia = I.intValue ();
} else {
ia--;
}return ia;
}, "~N");
Clazz.defineMethod (c$, "setAtom", 
function (atom, ia, ib, ic, d, theta1, theta2) {
if (Float.isNaN (theta1) || Float.isNaN (theta2)) return null;
this.pt0.setT (this.vAtoms.get (ia));
this.v1.sub2 (this.vAtoms.get (ib), this.pt0);
this.v1.normalize ();
if (theta2 == 3.4028235E38) {
this.v2.set (0, 0, 1);
(JU.Quat.newVA (this.v2, theta1)).transformP2 (this.v1, this.v2);
} else if (d >= 0) {
this.v2.sub2 (this.vAtoms.get (ic), this.pt0);
this.v2.cross (this.v1, this.v2);
(JU.Quat.newVA (this.v2, theta1)).transformP2 (this.v1, this.v2);
(JU.Quat.newVA (this.v1, -theta2)).transformP2 (this.v2, this.v2);
} else {
JU.Measure.getPlaneThroughPoint (this.setAtom (atom, ia, ib, ic, -d, theta1, 0), this.v1, this.plane1);
JU.Measure.getPlaneThroughPoint (this.setAtom (atom, ia, ic, ib, -d, theta2, 0), this.v1, this.plane2);
var list = JU.Measure.getIntersectionPP (this.plane1, this.plane2);
if (list.size () == 0) return null;
this.pt0.setT (list.get (0));
d = Math.sqrt (d * d - this.pt0.distanceSquared (this.vAtoms.get (ia))) * Math.signum (theta1) * Math.signum (theta2);
this.v2.setT (list.get (1));
}atom.scaleAdd2 (d, this.v2, this.pt0);
return atom;
}, "J.adapter.smarter.Atom,~N,~N,~N,~N,~N,~N");
});
