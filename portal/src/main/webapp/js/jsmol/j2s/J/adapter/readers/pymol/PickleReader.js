Clazz.declarePackage ("J.adapter.readers.pymol");
Clazz.load (["J.util.JmolList"], "J.adapter.readers.pymol.PickleReader", ["java.lang.Double", "java.util.Hashtable", "J.util.P3", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.binaryDoc = null;
this.list = null;
this.marks = null;
this.build = null;
this.logging = false;
this.viewer = null;
this.id = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.pymol, "PickleReader");
Clazz.prepareFields (c$, function () {
this.list =  new J.util.JmolList ();
this.marks =  new J.util.JmolList ();
this.build =  new J.util.JmolList ();
});
Clazz.makeConstructor (c$, 
function (doc, viewer) {
this.binaryDoc = doc;
this.logging = (viewer.getLogFile ().length > 0);
if (this.logging) this.viewer = viewer;
}, "J.api.JmolDocument,J.viewer.Viewer");
$_M(c$, "log", 
($fz = function (s) {
this.viewer.log (s + "\0");
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getMap", 
function () {
var s;
var module;
var name;
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
(this.peek ()).addAll (l);
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
break;
case 114:
i = this.binaryDoc.readIntLE ();
break;
case 104:
i = this.binaryDoc.readByte ();
break;
case 106:
i = this.binaryDoc.readIntLE ();
this.push ("LONG_BINGET" + (++this.id));
break;
case 85:
i = this.binaryDoc.readByte () & 0xff;
a =  Clazz.newByteArray (i, 0);
this.binaryDoc.readByteArray (a, 0, i);
s =  String.instantialize (a, "UTF-8");
this.push (s);
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
this.push ( new J.util.JmolList ());
break;
case 99:
module = this.readString ();
name = this.readString ();
this.push (["global", module, name]);
break;
case 98:
o = this.pop ();
this.build.addLast (o);
break;
case 40:
i = this.list.size ();
if (this.logging) this.log ("\n " + Integer.toHexString (this.binaryDoc.getPosition ()) + " [");
this.marks.addLast (Integer.$valueOf (i));
break;
case 78:
this.push (null);
break;
case 111:
this.push (this.getObjects (this.getMark ()));
break;
case 115:
o = this.pop ();
if (!(Clazz.instanceOf (this.peek (), String))) System.out.println (this.peek () + " is not a string");
s = this.pop ();
(this.peek ()).put (s, o);
break;
case 117:
mark = this.getMark ();
l = this.getObjects (mark);
o = this.peek ();
if (Clazz.instanceOf (o, J.util.JmolList)) {
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
l = this.getObjects (this.getMark ());
var jl =  new J.util.JmolList ();
for (i = 0; i < l.size (); i++) {
var pt = J.util.P3.new3 ((l.get (i++)).floatValue (), (l.get (i++)).floatValue (), (l.get (i)).floatValue ());
jl.addLast (pt);
}
this.push (jl);
break;
default:
System.out.println ("PyMOL reader error: " + b + " " + this.binaryDoc.getPosition ());
}
}
if (this.logging) this.log ("");
map = this.list.remove (0);
if (map.size () == 0) for (i = this.list.size (); --i >= 0; ) {
o = this.list.get (i--);
s = this.list.get (i);
map.put (s, o);
}
return map;
});
$_M(c$, "getObjects", 
($fz = function (mark) {
var n = this.list.size () - mark;
var args =  new J.util.JmolList ();
for (var j = 0; j < n; j++) args.addLast (null);

for (var j = n, i = this.list.size (); --i >= mark; ) args.set (--j, this.list.remove (i));

return args;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "readString", 
($fz = function () {
var sb =  new J.util.SB ();
while (true) {
var b = this.binaryDoc.readByte ();
if (b == 0xA) break;
sb.appendC (String.fromCharCode (b));
}
return sb.toString ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "getMark", 
($fz = function () {
return this.marks.remove (this.marks.size () - 1).intValue ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "push", 
($fz = function (o) {
if (this.logging && (Clazz.instanceOf (o, String) || Clazz.instanceOf (o, Double) || Clazz.instanceOf (o, Integer))) this.log ((Clazz.instanceOf (o, String) ? "'" + o + "'" : o) + ", ");
this.list.addLast (o);
}, $fz.isPrivate = true, $fz), "~O");
$_M(c$, "peek", 
($fz = function () {
return this.list.get (this.list.size () - 1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "pop", 
($fz = function () {
return this.list.remove (this.list.size () - 1);
}, $fz.isPrivate = true, $fz));
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
"TUPLE", 116);
});
