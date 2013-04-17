Clazz.declarePackage ("J.export.image");
Clazz.load (null, "J.export.image.GenericCRCEncoder", ["java.util.zip.CRC32", "J.util.ArrayUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.startPos = 0;
this.bytePos = 0;
this.crc = null;
this.pngBytes = null;
this.dataLen = 0;
this.int2 = null;
this.int4 = null;
Clazz.instantialize (this, arguments);
}, J["export"].image, "GenericCRCEncoder");
Clazz.prepareFields (c$, function () {
this.int2 =  Clazz.newByteArray (2, 0);
this.int4 =  Clazz.newByteArray (4, 0);
});
Clazz.makeConstructor (c$, 
function () {
this.pngBytes =  Clazz.newByteArray (250, 0);
this.crc =  new java.util.zip.CRC32 ();
});
$_M(c$, "setData", 
function (b, pt) {
this.pngBytes = b;
this.dataLen = b.length;
this.startPos = this.bytePos = pt;
}, "~A,~N");
$_M(c$, "getBytes", 
function () {
return (this.dataLen == this.pngBytes.length ? this.pngBytes : J.util.ArrayUtil.arrayCopyByte (this.pngBytes, this.dataLen));
});
$_M(c$, "writeCRC", 
function () {
this.crc.reset ();
this.crc.update (this.pngBytes, this.startPos, this.bytePos - this.startPos);
this.writeInt4 (this.crc.getValue ());
});
$_M(c$, "writeInt2", 
function (n) {
this.int2[0] = ((n >> 8) & 0xff);
this.int2[1] = (n & 0xff);
this.writeBytes (this.int2);
}, "~N");
$_M(c$, "writeInt4", 
function (n) {
J["export"].image.GenericCRCEncoder.getInt4 (n, this.int4);
this.writeBytes (this.int4);
}, "~N");
c$.getInt4 = $_M(c$, "getInt4", 
function (n, int4) {
int4[0] = ((n >> 24) & 0xff);
int4[1] = ((n >> 16) & 0xff);
int4[2] = ((n >> 8) & 0xff);
int4[3] = (n & 0xff);
}, "~N,~A");
$_M(c$, "writeByte", 
function (b) {
var temp = [b];
this.writeBytes (temp);
}, "~N");
$_M(c$, "writeString", 
function (s) {
this.writeBytes (s.getBytes ());
}, "~S");
$_M(c$, "writeBytes", 
function (data) {
var newPos = this.bytePos + data.length;
this.dataLen = Math.max (this.dataLen, newPos);
if (newPos > this.pngBytes.length) this.pngBytes = J.util.ArrayUtil.arrayCopyByte (this.pngBytes, newPos + 16);
System.arraycopy (data, 0, this.pngBytes, this.bytePos, data.length);
this.bytePos = newPos;
}, "~A");
});
