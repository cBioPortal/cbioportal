Clazz.declarePackage ("J.adapter.readers.pymol");
Clazz.load (["java.util.Hashtable", "JU.Lst"], "J.adapter.readers.pymol.PickleReader", ["java.lang.Double", "$.Long", "JU.SB", "JU.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vwr = null;
this.binaryDoc = null;
this.stack = null;
this.marks = null;
this.build = null;
this.memo = null;
this.logging = false;
this.id = 0;
this.markCount = 0;
this.filePt = 0;
this.emptyListPt = 0;
this.thisSection = null;
this.inMovie = false;
this.inNames = false;
this.lastMark = 0;
this.retrieveCount = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.pymol, "PickleReader");
Clazz.prepareFields (c$, function () {
this.stack =  new JU.Lst ();
this.marks =  new JU.Lst ();
this.build =  new JU.Lst ();
this.memo =  new java.util.Hashtable ();
});
Clazz.makeConstructor (c$, 
function (doc, vwr) {
this.binaryDoc = doc;
this.vwr = vwr;
}, "javajs.api.GenericBinaryDocument,JV.Viewer");
Clazz.defineMethod (c$, "log", 
 function (s) {
this.vwr.log (s + "\0");
}, "~S");
Clazz.defineMethod (c$, "getMap", 
function (logging) {
this.logging = logging;
var s;
var b;
var i;
var mark;
var d;
var o;
var a;
var map;
var l;
var going = true;
while (going) {
b = this.binaryDoc.readByte ();
switch (b) {
case 125:
this.push ( new java.util.Hashtable ());
break;
case 97:
o = this.pop ();
(this.peek ()).addLast (o);
break;
case 101:
l = this.getObjects (this.getMark ());
if (this.inNames && this.markCount == 2) {
var pt = this.binaryDoc.getPosition ();
var l2 =  new JU.Lst ();
l2.addLast (Integer.$valueOf (this.filePt));
l2.addLast (Integer.$valueOf (pt - this.filePt));
l.addLast (l2);
}(this.peek ()).addAll (l);
break;
case 71:
d = this.binaryDoc.readDouble ();
this.push (Double.$valueOf (d));
break;
case 74:
i = this.binaryDoc.readIntLE ();
this.push (Integer.$valueOf (i));
break;
case 75:
i = this.binaryDoc.readByte () & 0xff;
this.push (Integer.$valueOf (i));
break;
case 77:
i = (this.binaryDoc.readByte () & 0xff | ((this.binaryDoc.readByte () & 0xff) << 8)) & 0xffff;
this.push (Integer.$valueOf (i));
break;
case 113:
i = this.binaryDoc.readByte ();
this.putMemo (i, false);
break;
case 114:
i = this.binaryDoc.readIntLE ();
this.putMemo (i, true);
break;
case 104:
i = this.binaryDoc.readByte ();
o = this.getMemo (i);
this.push (o == null ? "BINGET" + (++this.id) : o);
break;
case 106:
i = this.binaryDoc.readIntLE ();
o = this.getMemo (i);
if (o == null) {
JU.Logger.error ("did not find memo item for " + i);
this.push ("LONG_BINGET" + (++this.id));
} else {
this.push (o);
}break;
case 85:
i = this.binaryDoc.readByte () & 0xff;
a =  Clazz.newByteArray (i, 0);
this.binaryDoc.readByteArray (a, 0, i);
s =  String.instantialize (a, "UTF-8");
if (this.inNames && this.markCount == 3 && this.lastMark == this.stack.size ()) {
this.filePt = this.emptyListPt;
}this.push (s);
break;
case 84:
i = this.binaryDoc.readIntLE ();
a =  Clazz.newByteArray (i, 0);
this.binaryDoc.readByteArray (a, 0, i);
s =  String.instantialize (a, "UTF-8");
this.push (s);
break;
case 87:
i = this.binaryDoc.readIntLE ();
a =  Clazz.newByteArray (i, 0);
this.binaryDoc.readByteArray (a, 0, i);
s =  String.instantialize (a, "UTF-8");
this.push (s);
break;
case 93:
this.emptyListPt = this.binaryDoc.getPosition () - 1;
this.push ( new JU.Lst ());
break;
case 99:
l =  new JU.Lst ();
l.addLast ("global");
l.addLast (this.readString ());
l.addLast (this.readString ());
this.push (l);
break;
case 98:
o = this.pop ();
this.build.addLast (o);
break;
case 40:
this.putMark (this.stack.size ());
break;
case 78:
this.push (null);
break;
case 111:
this.push (this.getObjects (this.getMark ()));
break;
case 115:
o = this.pop ();
if (!(Clazz.instanceOf (this.peek (), String))) JU.Logger.error (this.peek () + " is not a string");
s = this.pop ();
(this.peek ()).put (s, o);
break;
case 117:
mark = this.getMark ();
l = this.getObjects (mark);
o = this.peek ();
if (Clazz.instanceOf (o, JU.Lst)) {
for (i = 0; i < l.size (); i++) (o).addLast (l.get (i));

} else {
map = o;
for (i = l.size (); --i >= 0; ) {
o = l.get (i);
s = l.get (--i);
map.put (s, o);
}
}break;
case 46:
going = false;
break;
case 116:
this.push (this.getObjects (this.getMark ()));
break;
case 73:
s = this.readString ();
try {
this.push (Integer.$valueOf (Integer.parseInt (s)));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
var ll = Long.parseLong (s);
this.push (Integer.$valueOf ((ll & 0xFFFFFFFF)));
} else {
throw e;
}
}
break;
default:
JU.Logger.error ("Pickle reader error: " + b + " " + this.binaryDoc.getPosition ());
}
}
if (logging) this.log ("");
JU.Logger.info ("PyMOL Pickle reader cached " + this.memo.size () + " tokens; retrieved " + this.retrieveCount);
this.memo = null;
map = this.stack.remove (0);
if (map.size () == 0) for (i = this.stack.size (); --i >= 0; ) {
o = this.stack.get (i--);
s = this.stack.get (i);
map.put (s, o);
}
return map;
}, "~B");
Clazz.defineMethod (c$, "putMemo", 
 function (i, doCheck) {
var o = this.peek ();
if (Clazz.instanceOf (o, String)) {
if (doCheck && this.markCount >= 6 || this.markCount == 3 && this.inMovie) return;
this.memo.put (Integer.$valueOf (i), o);
}}, "~N,~B");
Clazz.defineMethod (c$, "getMemo", 
 function (i) {
var o = this.memo.get (Integer.$valueOf (i));
if (o == null) return o;
this.retrieveCount++;
return o;
}, "~N");
Clazz.defineMethod (c$, "getObjects", 
 function (mark) {
var n = this.stack.size () - mark;
var args =  new JU.Lst ();
for (var j = 0; j < n; j++) args.addLast (null);

for (var j = n, i = this.stack.size (); --i >= mark; ) args.set (--j, this.stack.remove (i));

return args;
}, "~N");
Clazz.defineMethod (c$, "readString", 
 function () {
var sb =  new JU.SB ();
while (true) {
var b = this.binaryDoc.readByte ();
if (b == 0xA) break;
sb.appendC (String.fromCharCode (b));
}
return sb.toString ();
});
Clazz.defineMethod (c$, "putMark", 
 function (i) {
if (this.logging) this.log ("\n " + Integer.toHexString (this.binaryDoc.getPosition ()) + " [");
this.marks.addLast (Integer.$valueOf (this.lastMark = i));
this.markCount++;
switch (this.markCount) {
case 2:
this.thisSection = this.stack.get (i - 2);
this.inMovie = "movie".equals (this.thisSection);
this.inNames = "names".equals (this.thisSection);
break;
default:
break;
}
}, "~N");
Clazz.defineMethod (c$, "getMark", 
 function () {
return this.marks.remove (--this.markCount).intValue ();
});
Clazz.defineMethod (c$, "push", 
 function (o) {
if (this.logging && (Clazz.instanceOf (o, String) || Clazz.instanceOf (o, Double) || Clazz.instanceOf (o, Integer))) this.log ((Clazz.instanceOf (o, String) ? "'" + o + "'" : o) + ", ");
this.stack.addLast (o);
}, "~O");
Clazz.defineMethod (c$, "peek", 
 function () {
return this.stack.get (this.stack.size () - 1);
});
Clazz.defineMethod (c$, "pop", 
 function () {
return this.stack.remove (this.stack.size () - 1);
});
Clazz.defineStatics (c$,
"APPEND", 97,
"APPENDS", 101,
"BINFLOAT", 71,
"BININT", 74,
"BININT1", 75,
"BININT2", 77,
"BINPUT", 113,
"BINSTRING", 84,
"BINUNICODE", 87,
"BUILD", 98,
"EMPTY_DICT", 125,
"EMPTY_LIST", 93,
"GLOBAL", 99,
"LONG_BINPUT", 114,
"MARK", 40,
"NONE", 78,
"OBJ", 111,
"SETITEM", 115,
"SETITEMS", 117,
"SHORT_BINSTRING", 85,
"STOP", 46,
"BINGET", 104,
"LONG_BINGET", 106,
"TUPLE", 116,
"INT", 73);
});
