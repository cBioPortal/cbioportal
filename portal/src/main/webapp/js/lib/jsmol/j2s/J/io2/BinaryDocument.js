Clazz.declarePackage ("J.io2");
Clazz.load (["J.api.JmolDocument"], "J.io2.BinaryDocument", ["java.io.DataInputStream", "java.lang.Double", "J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.stream = null;
this.isRandom = false;
this.isBigEndian = true;
this.t8 = null;
this.nBytes = 0;
this.os = null;
Clazz.instantialize (this, arguments);
}, J.io2, "BinaryDocument", null, J.api.JmolDocument);
Clazz.prepareFields (c$, function () {
this.t8 =  Clazz.newByteArray (8, 0);
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "close", 
function () {
if (this.stream != null) try {
this.stream.close ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
if (this.os != null) {
try {
this.os.flush ();
this.os.close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
}});
Clazz.overrideMethod (c$, "setStream", 
function (bis, isBigEndian) {
if (bis != null) this.stream =  new java.io.DataInputStream (bis);
this.isBigEndian = isBigEndian;
}, "java.io.BufferedInputStream,~B");
Clazz.overrideMethod (c$, "setStreamData", 
function (stream, isBigEndian) {
if (stream != null) this.stream = stream;
this.isBigEndian = isBigEndian;
}, "java.io.DataInputStream,~B");
$_M(c$, "setRandom", 
function (TF) {
this.isRandom = TF;
}, "~B");
Clazz.overrideMethod (c$, "readByte", 
function () {
this.nBytes++;
return this.ioReadByte ();
});
$_M(c$, "ioReadByte", 
($fz = function () {
var b = this.stream.readByte ();
if (this.os != null) {
{
this.os.writeByteAsInt(b);
}}return b;
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "readByteArray", 
function (b, off, len) {
var n = this.ioRead (b, off, len);
if (n > 0) this.nBytes += n;
var nBytesRead = n;
if (n > 0 && n < len) {
while (nBytesRead < len && n > 0) {
n = this.ioRead (b, nBytesRead, len - nBytesRead);
if (n > 0) {
this.nBytes += n;
nBytesRead += n;
}}
}return nBytesRead;
}, "~A,~N,~N");
$_M(c$, "ioRead", 
($fz = function (b, off, len) {
var n = this.stream.read (b, off, len);
if (n > 0 && this.os != null) this.writeBytes (b, off, n);
return n;
}, $fz.isPrivate = true, $fz), "~A,~N,~N");
$_M(c$, "writeBytes", 
function (b, off, n) {
this.os.write (b, off, n);
}, "~A,~N,~N");
Clazz.overrideMethod (c$, "readString", 
function (nChar) {
var temp =  Clazz.newByteArray (nChar, 0);
var n = this.readByteArray (temp, 0, nChar);
return  String.instantialize (temp, 0, n, "UTF-8");
}, "~N");
Clazz.overrideMethod (c$, "readShort", 
function () {
this.nBytes += 2;
return (this.isBigEndian ? this.ioReadShort () : ((this.ioReadByte () & 0xff) | (this.ioReadByte () & 0xff) << 8));
});
$_M(c$, "ioReadShort", 
($fz = function () {
var b = this.stream.readShort ();
if (this.os != null) this.writeShort (b);
return b;
}, $fz.isPrivate = true, $fz));
$_M(c$, "writeShort", 
function (i) {
{
this.os.writeByteAsInt(i >> 8);
this.os.writeByteAsInt(i);
}}, "~N");
Clazz.overrideMethod (c$, "readIntLE", 
function () {
this.nBytes += 4;
return this.readLEInt ();
});
Clazz.overrideMethod (c$, "readInt", 
function () {
this.nBytes += 4;
return (this.isBigEndian ? this.ioReadInt () : this.readLEInt ());
});
$_M(c$, "ioReadInt", 
($fz = function () {
var i = this.stream.readInt ();
if (this.os != null) this.writeInt (i);
return i;
}, $fz.isPrivate = true, $fz));
$_M(c$, "writeInt", 
function (i) {
{
this.os.writeByteAsInt(i >> 24);
this.os.writeByteAsInt(i >> 16);
this.os.writeByteAsInt(i >> 8);
this.os.writeByteAsInt(i);
}}, "~N");
Clazz.overrideMethod (c$, "swapBytesI", 
function (n) {
return (((n >> 24) & 0xff) | ((n >> 16) & 0xff) << 8 | ((n >> 8) & 0xff) << 16 | (n & 0xff) << 24);
}, "~N");
Clazz.overrideMethod (c$, "swapBytesS", 
function (n) {
return ((((n >> 8) & 0xff) | (n & 0xff) << 8));
}, "~N");
Clazz.overrideMethod (c$, "readUnsignedShort", 
function () {
this.nBytes += 2;
var a = (this.ioReadByte () & 0xff);
var b = (this.ioReadByte () & 0xff);
return (this.isBigEndian ? (a << 8) + b : (b << 8) + a);
});
Clazz.overrideMethod (c$, "readLong", 
function () {
this.nBytes += 8;
return (this.isBigEndian ? this.ioReadLong () : (((this.ioReadByte ()) & 0xff) | ((this.ioReadByte ()) & 0xff) << 8 | ((this.ioReadByte ()) & 0xff) << 16 | ((this.ioReadByte ()) & 0xff) << 24 | ((this.ioReadByte ()) & 0xff) << 32 | ((this.ioReadByte ()) & 0xff) << 40 | ((this.ioReadByte ()) & 0xff) << 48 | ((this.ioReadByte ()) & 0xff) << 54));
});
$_M(c$, "ioReadLong", 
($fz = function () {
var b = this.stream.readLong ();
if (this.os != null) this.writeLong (b);
return b;
}, $fz.isPrivate = true, $fz));
$_M(c$, "writeLong", 
function (b) {
this.writeInt (((b >> 32) & 0xFFFFFFFF));
this.writeInt ((b & 0xFFFFFFFF));
}, "~N");
Clazz.overrideMethod (c$, "readFloat", 
function () {
var x = this.readInt ();
{
if (x == 0) return 0;
var o = J.io2.BinaryDocument;
if (o.fracIEEE == null);
o.setFracIEEE();
var m = ((x & 0x7F800000) >> 23);
return ((x & 0x80000000) == 0 ? 1 : -1) * o.shiftIEEE((x & 0x7FFFFF) | 0x800000, m - 149);
}});
$_M(c$, "readLEInt", 
($fz = function () {
return ((this.ioReadByte () & 0xff) | (this.ioReadByte () & 0xff) << 8 | (this.ioReadByte () & 0xff) << 16 | (this.ioReadByte () & 0xff) << 24);
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "readDouble", 
function () {
{
this.readByteArray(this.t8, 0, 8);
return J.io2.BinaryDocument.bytesToDoubleToFloat(this.t8, 0, this.isBigEndian);
}});
c$.bytesToDoubleToFloat = $_M(c$, "bytesToDoubleToFloat", 
function (bytes, j, isBigEndian) {
{
if (J.io2.BinaryDocument.fracIEEE == null) J.io2.BinaryDocument.setFracIEEE ();
{
var o = J.io2.BinaryDocument;
var b1, b2, b3, b4, b5;
if (isBigEndian) {
b1 = bytes[j] & 0xFF;
b2 = bytes[j + 1] & 0xFF;
b3 = bytes[j + 2] & 0xFF;
b4 = bytes[j + 3] & 0xFF;
b5 = bytes[j + 4] & 0xFF;
} else {
b1 = bytes[j + 7] & 0xFF;
b2 = bytes[j + 6] & 0xFF;
b3 = bytes[j + 5] & 0xFF;
b4 = bytes[j + 4] & 0xFF;
b5 = bytes[j + 3] & 0xFF;
}
var s = ((b1 & 0x80) == 0 ? 1 : -1);
var e = (((b1 & 0x7F) << 4) | (b2 >> 4)) - 1026;
b2 = (b2 & 0xF) | 0x10;
return s * (o.shiftIEEE(b2, e) + o.shiftIEEE(b3, e - 8) + o.shiftIEEE(b4, e - 16)
+ o.shiftIEEE(b5, e - 24));
}}}, "~A,~N,~B");
c$.setFracIEEE = $_M(c$, "setFracIEEE", 
function () {
($t$ = J.io2.BinaryDocument.fracIEEE =  Clazz.newFloatArray (270, 0), J.io2.BinaryDocument.prototype.fracIEEE = J.io2.BinaryDocument.fracIEEE, $t$);
for (var i = 0; i < 270; i++) J.io2.BinaryDocument.fracIEEE[i] = Math.pow (2, i - 141);

});
c$.shiftIEEE = $_M(c$, "shiftIEEE", 
function (f, i) {
if (f == 0 || i < -140) return 0;
if (i > 128) return 3.4028235E38;
return f * J.io2.BinaryDocument.fracIEEE[i + 140];
}, "~N,~N");
$_M(c$, "ioReadDouble", 
($fz = function () {
var d = this.stream.readDouble ();
if (this.os != null) this.writeLong (Double.doubleToRawLongBits (d));
return d;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readLELong", 
($fz = function () {
return (((this.ioReadByte ()) & 0xff) | ((this.ioReadByte ()) & 0xff) << 8 | ((this.ioReadByte ()) & 0xff) << 16 | ((this.ioReadByte ()) & 0xff) << 24 | ((this.ioReadByte ()) & 0xff) << 32 | ((this.ioReadByte ()) & 0xff) << 40 | ((this.ioReadByte ()) & 0xff) << 48 | ((this.ioReadByte ()) & 0xff) << 56);
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "seek", 
function (offset) {
try {
if (offset == this.nBytes) return;
if (offset < this.nBytes) {
this.stream.reset ();
this.nBytes = 0;
} else {
offset -= this.nBytes;
}this.stream.skipBytes (offset);
this.nBytes += offset;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.errorEx (null, e);
} else {
throw e;
}
}
}, "~N");
Clazz.overrideMethod (c$, "getPosition", 
function () {
return this.nBytes;
});
Clazz.overrideMethod (c$, "setOutputStream", 
function (os, viewer, privateKey) {
if (viewer.checkPrivateKey (privateKey)) this.os = os;
}, "java.io.OutputStream,J.viewer.Viewer,~N");
Clazz.overrideMethod (c$, "getAllDataFiles", 
function (binaryFileList, firstFile) {
return null;
}, "~S,~S");
Clazz.overrideMethod (c$, "getAllDataMapped", 
function (replace, string, fileData) {
}, "~S,~S,java.util.Map");
Clazz.defineStatics (c$,
"fracIEEE", null);
});
