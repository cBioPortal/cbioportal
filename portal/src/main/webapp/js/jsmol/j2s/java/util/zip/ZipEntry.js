Clazz.declarePackage ("java.util.zip");
Clazz.load (["java.util.zip.ZipConstants"], "java.util.zip.ZipEntry", ["java.lang.IllegalArgumentException", "$.InternalError", "$.NullPointerException", "java.util.Date"], function () {
c$ = Clazz.decorateAsClass (function () {
this.offset = 0;
this.name = null;
this.time = -1;
this.crc = -1;
this.size = -1;
this.csize = -1;
this.method = -1;
this.flag = 0;
this.extra = null;
this.comment = null;
Clazz.instantialize (this, arguments);
}, java.util.zip, "ZipEntry", null, [java.util.zip.ZipConstants, Cloneable]);
Clazz.makeConstructor (c$, 
function (name) {
if (name == null) {
throw  new NullPointerException ();
}if (name.length > 0xFFFF) {
throw  new IllegalArgumentException ("entry name too long");
}this.name = name;
}, "~S");
$_M(c$, "getName", 
function () {
return this.name;
});
$_M(c$, "setTime", 
function (time) {
this.time = java.util.zip.ZipEntry.javaToDosTime (time);
}, "~N");
$_M(c$, "getTime", 
function () {
return this.time != -1 ? java.util.zip.ZipEntry.dosToJavaTime (this.time) : -1;
});
$_M(c$, "setSize", 
function (size) {
if (size < 0) {
throw  new IllegalArgumentException ("invalid entry size");
}this.size = size;
}, "~N");
$_M(c$, "getSize", 
function () {
return this.size;
});
$_M(c$, "getCompressedSize", 
function () {
return this.csize;
});
$_M(c$, "setCompressedSize", 
function (csize) {
this.csize = csize;
}, "~N");
$_M(c$, "setCrc", 
function (crc) {
if (crc < 0 || crc > 0xFFFFFFFF) {
throw  new IllegalArgumentException ("invalid entry crc-32");
}this.crc = crc;
}, "~N");
$_M(c$, "getCrc", 
function () {
return this.crc;
});
$_M(c$, "setMethod", 
function (method) {
if (method != 0 && method != 8) {
throw  new IllegalArgumentException ("invalid compression method");
}this.method = method;
}, "~N");
$_M(c$, "getMethod", 
function () {
return this.method;
});
$_M(c$, "setExtra", 
function (extra) {
if (extra != null && extra.length > 0xFFFF) {
throw  new IllegalArgumentException ("invalid extra field length");
}this.extra = extra;
}, "~A");
$_M(c$, "getExtra", 
function () {
return this.extra;
});
$_M(c$, "setComment", 
function (comment) {
this.comment = comment;
}, "~S");
$_M(c$, "getComment", 
function () {
return this.comment;
});
$_M(c$, "isDirectory", 
function () {
return this.name.endsWith ("/");
});
Clazz.overrideMethod (c$, "toString", 
function () {
return this.getName ();
});
c$.dosToJavaTime = $_M(c$, "dosToJavaTime", 
($fz = function (dtime) {
var d =  new java.util.Date ((((dtime >> 25) & 0x7f) + 80), (((dtime >> 21) & 0x0f) - 1), ((dtime >> 16) & 0x1f), ((dtime >> 11) & 0x1f), ((dtime >> 5) & 0x3f), ((dtime << 1) & 0x3e));
return d.getTime ();
}, $fz.isPrivate = true, $fz), "~N");
c$.javaToDosTime = $_M(c$, "javaToDosTime", 
($fz = function (time) {
var d =  new java.util.Date (time);
var year = d.getYear () + 1900;
if (year < 1980) {
return 2162688;
}return (year - 1980) << 25 | (d.getMonth () + 1) << 21 | d.getDate () << 16 | d.getHours () << 11 | d.getMinutes () << 5 | d.getSeconds () >> 1;
}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "hashCode", 
function () {
return this.name.hashCode ();
});
$_M(c$, "clone", 
function () {
try {
var e = Clazz.superCall (this, java.util.zip.ZipEntry, "clone", []);
if (this.extra != null) {
e.extra =  Clazz.newByteArray (this.extra.length, 0);
System.arraycopy (this.extra, 0, e.extra, 0, this.extra.length);
}return e;
} catch (e) {
if (Clazz.exceptionOf (e, CloneNotSupportedException)) {
throw  new InternalError ();
} else {
throw e;
}
}
});
Clazz.defineStatics (c$,
"STORED", 0,
"DEFLATED", 8);
});
