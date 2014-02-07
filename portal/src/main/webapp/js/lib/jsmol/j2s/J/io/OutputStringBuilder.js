Clazz.declarePackage ("J.io");
Clazz.load (null, "J.io.OutputStringBuilder", ["J.util.ArrayUtil", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.type = null;
this.sb = null;
this.bw = null;
this.nBytes = 0;
this.bos = null;
this.buf = null;
Clazz.instantialize (this, arguments);
}, J.io, "OutputStringBuilder");
Clazz.makeConstructor (c$, 
function (bos, asBytes) {
if (bos != null) {
if (asBytes) {
this.bos = bos;
} else {
{
this.bw = bos;
}}} else if (asBytes) {
this.buf =  Clazz.newByteArray (8092, 0);
} else {
this.sb =  new J.util.SB ();
}}, "java.io.BufferedOutputStream,~B");
$_M(c$, "append", 
function (s) {
try {
if (this.bw != null) {
this.bw.write (s);
} else if (this.bos != null) {
var buf = s.getBytes ();
this.bos.write (buf, 0, buf.length);
return this;
} else {
this.sb.append (s);
}this.nBytes += s.length;
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
return this;
}, "~S");
$_M(c$, "write", 
function (buf, offset, len) {
if (this.bos == null) {
if (this.buf.length < this.nBytes + len) this.buf = J.util.ArrayUtil.ensureLengthByte (this.buf, this.nBytes * 2 + len);
System.arraycopy (buf, offset, this.buf, this.nBytes, len);
} else {
this.bos.write (buf, offset, len);
}this.nBytes += buf.length;
}, "~A,~N,~N");
$_M(c$, "length", 
function () {
return this.nBytes;
});
$_M(c$, "getBytes", 
function () {
return (this.buf != null ? this.buf : this.sb != null ? this.sb.toBytes (0, -1) : null);
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
