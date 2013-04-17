Clazz.declarePackage ("J.io2");
Clazz.load (null, "J.io2.ZipData", ["J.io.JmolBinary", "J.io2.ZipUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.isEnabled = true;
this.buf = null;
this.pt = 0;
this.nBytes = 0;
Clazz.instantialize (this, arguments);
}, J.io2, "ZipData");
Clazz.makeConstructor (c$, 
function (nBytes) {
this.nBytes = nBytes;
}, "~N");
$_M(c$, "addBytes", 
function (byteBuf, nSectorBytes, nBytesRemaining) {
if (this.pt == 0) {
if (!J.io.JmolBinary.isGzipB (byteBuf)) {
this.isEnabled = false;
return -1;
}this.buf =  Clazz.newByteArray (nBytesRemaining, 0);
}var nToAdd = Math.min (nSectorBytes, nBytesRemaining);
System.arraycopy (byteBuf, 0, this.buf, this.pt, nToAdd);
this.pt += nToAdd;
return nBytesRemaining - nToAdd;
}, "~A,~N,~N");
$_M(c$, "addTo", 
function (data) {
data.append (J.io2.ZipUtil.staticGetGzippedBytesAsString (this.buf));
}, "J.util.SB");
});
