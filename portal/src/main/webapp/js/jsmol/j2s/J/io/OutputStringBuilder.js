Clazz.declarePackage ("J.io");
Clazz.load (null, "J.io.OutputStringBuilder", ["java.io.BufferedWriter", "$.OutputStreamWriter", "J.util.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.type = null;
this.sb = null;
this.bw = null;
this.nBytes = 0;
Clazz.instantialize (this, arguments);
}, J.io, "OutputStringBuilder");
Clazz.makeConstructor (c$, 
function (os) {
if (os == null) {
this.sb =  new J.util.SB ();
} else {
var osw =  new java.io.OutputStreamWriter (os);
this.bw =  new java.io.BufferedWriter (osw, 8192);
}}, "java.io.BufferedOutputStream");
$_M(c$, "append", 
function (s) {
if (this.bw == null) {
this.sb.append (s);
} else {
this.nBytes += s.length;
try {
this.bw.write (s);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
}return this;
}, "~S");
$_M(c$, "length", 
function () {
return (this.bw == null ? this.sb.length () : this.nBytes);
});
Clazz.overrideMethod (c$, "toString", 
function () {
if (this.bw != null) try {
this.bw.flush ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
return (this.bw == null ? this.sb.toString () : this.nBytes + " bytes");
});
});
