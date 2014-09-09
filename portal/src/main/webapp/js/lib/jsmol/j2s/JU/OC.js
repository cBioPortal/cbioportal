Clazz.declarePackage ("JU");
Clazz.load (["java.io.OutputStream"], "JU.OC", ["java.io.BufferedWriter", "$.ByteArrayOutputStream", "$.FileOutputStream", "$.OutputStreamWriter", "JU.Base64", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.bytePoster = null;
this.fileName = null;
this.bw = null;
this.isLocalFile = false;
this.byteCount = 0;
this.isCanceled = false;
this.closed = false;
this.os = null;
this.sb = null;
this.type = null;
this.$isBase64 = false;
this.os0 = null;
this.bytes = null;
Clazz.instantialize (this, arguments);
}, JU, "OC", java.io.OutputStream);
Clazz.defineMethod (c$, "setParams", 
function (bytePoster, fileName, asWriter, os) {
this.bytePoster = bytePoster;
this.fileName = fileName;
this.$isBase64 = ";base64,".equals (fileName);
if (this.$isBase64) {
fileName = null;
this.os0 = os;
os = null;
}this.os = os;
this.isLocalFile = (fileName != null && !(fileName.startsWith ("http://") || fileName.startsWith ("https://")));
if (asWriter && !this.$isBase64 && os != null) this.bw =  new java.io.BufferedWriter ( new java.io.OutputStreamWriter (os));
return this;
}, "javajs.api.BytePoster,~S,~B,java.io.OutputStream");
Clazz.defineMethod (c$, "setBytes", 
function (b) {
this.bytes = b;
return this;
}, "~A");
Clazz.defineMethod (c$, "getFileName", 
function () {
return this.fileName;
});
Clazz.defineMethod (c$, "getName", 
function () {
return (this.fileName == null ? null : this.fileName.substring (this.fileName.lastIndexOf ("/") + 1));
});
Clazz.defineMethod (c$, "getByteCount", 
function () {
return this.byteCount;
});
Clazz.defineMethod (c$, "setType", 
function (type) {
this.type = type;
}, "~S");
Clazz.defineMethod (c$, "getType", 
function () {
return this.type;
});
Clazz.defineMethod (c$, "append", 
function (s) {
try {
if (this.bw != null) {
this.bw.write (s);
} else if (this.os == null) {
if (this.sb == null) this.sb =  new JU.SB ();
this.sb.append (s);
} else {
var b = s.getBytes ();
this.os.write (b, 0, b.length);
this.byteCount += b.length;
return this;
}} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
this.byteCount += s.length;
return this;
}, "~S");
Clazz.defineMethod (c$, "reset", 
function () {
this.sb = null;
try {
if (Clazz.instanceOf (this.os, java.io.FileOutputStream)) {
this.os.close ();
this.os =  new java.io.FileOutputStream (this.fileName);
} else {
this.os =  new java.io.ByteArrayOutputStream ();
}if (this.bw != null) {
this.bw.close ();
this.bw =  new java.io.BufferedWriter ( new java.io.OutputStreamWriter (this.os));
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println (e.toString ());
} else {
throw e;
}
}
this.byteCount = 0;
});
Clazz.overrideMethod (c$, "write", 
function (buf, i, len) {
if (this.os == null) this.os =  new java.io.ByteArrayOutputStream ();
{
this.os.write(buf, i, len);
}this.byteCount += len;
}, "~A,~N,~N");
Clazz.overrideMethod (c$, "writeByteAsInt", 
function (b) {
if (this.os == null) this.os =  new java.io.ByteArrayOutputStream ();
{
this.os.writeByteAsInt(b);
}this.byteCount++;
}, "~N");
Clazz.defineMethod (c$, "cancel", 
function () {
this.isCanceled = true;
this.closeChannel ();
});
Clazz.defineMethod (c$, "closeChannel", 
function () {
if (this.closed) return null;
try {
if (this.bw != null) {
this.bw.flush ();
this.bw.close ();
} else if (this.os != null) {
this.os.flush ();
this.os.close ();
}if (this.os0 != null && this.isCanceled) {
this.os0.flush ();
this.os0.close ();
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
if (this.isCanceled) {
this.closed = true;
return null;
}if (this.fileName == null) {
if (this.$isBase64) {
var s = this.getBase64 ();
if (this.os0 != null) {
this.os = this.os0;
this.append (s);
}this.sb =  new JU.SB ();
this.sb.append (s);
this.$isBase64 = false;
return this.closeChannel ();
}return (this.sb == null ? null : this.sb.toString ());
}this.closed = true;
{
var data = (this.sb == null ? this.toByteArray() :
this.sb.toString()); if (typeof this.fileName == "function") {
this.fileName(data); } else { Jmol._doAjax(this.fileName,
null, data); }
}return null;
});
Clazz.defineMethod (c$, "isBase64", 
function () {
return this.$isBase64;
});
Clazz.defineMethod (c$, "getBase64", 
function () {
return JU.Base64.getBase64 (this.toByteArray ()).toString ();
});
Clazz.defineMethod (c$, "toByteArray", 
function () {
return (this.bytes != null ? this.bytes : Clazz.instanceOf (this.os, java.io.ByteArrayOutputStream) ? (this.os).toByteArray () : null);
});
Clazz.defineMethod (c$, "close", 
function () {
this.closeChannel ();
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
if (this.sb != null) return this.closeChannel ();
return this.byteCount + " bytes";
});
Clazz.defineMethod (c$, "postByteArray", 
 function () {
var bytes = (this.sb == null ? this.toByteArray () : this.sb.toString ().getBytes ());
return this.bytePoster.postByteArray (this.fileName, bytes);
});
});
