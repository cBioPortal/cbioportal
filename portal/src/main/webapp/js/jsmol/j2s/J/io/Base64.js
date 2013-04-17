Clazz.declarePackage ("J.io");
Clazz.load (null, "J.io.Base64", ["J.util.SB"], function () {
c$ = Clazz.declareType (J.io, "Base64");
c$.write = $_M(c$, "write", 
function (bytes, os) {
var sb = J.io.Base64.getBase64 (bytes);
var len = sb.length ();
var b =  Clazz.newByteArray (1, 0);
for (var i = 0; i < len; i++) {
b[0] = (sb.charAt (i)).charCodeAt (0);
os.write (b, 0, 1);
}
}, "~A,java.io.OutputStream");
c$.getBytes64 = $_M(c$, "getBytes64", 
function (bytes) {
return J.io.Base64.getBase64 (bytes).toBytes (0, -1);
}, "~A");
c$.getBase64 = $_M(c$, "getBase64", 
function (bytes) {
var nBytes = bytes.length;
var sout =  new J.util.SB ();
if (nBytes == 0) return sout;
for (var i = 0, nPad = 0; i < nBytes && nPad == 0; ) {
if (i % 75 == 0 && i != 0) sout.append ("\r\n");
nPad = (i + 2 == nBytes ? 1 : i + 1 == nBytes ? 2 : 0);
var outbytes = ((bytes[i++] << 16) & 0xFF0000) | ((nPad == 2 ? 0 : bytes[i++] << 8) & 0x00FF00) | ((nPad >= 1 ? 0 : bytes[i++]) & 0x0000FF);
sout.appendC (J.io.Base64.base64.charAt ((outbytes >> 18) & 0x3F));
sout.appendC (J.io.Base64.base64.charAt ((outbytes >> 12) & 0x3F));
sout.appendC (nPad == 2 ? '=' : J.io.Base64.base64.charAt ((outbytes >> 6) & 0x3F));
sout.appendC (nPad >= 1 ? '=' : J.io.Base64.base64.charAt (outbytes & 0x3F));
}
return sout;
}, "~A");
c$.decodeBase64 = $_M(c$, "decodeBase64", 
function (strBase64) {
var nBytes = 0;
var ch;
var chars64 = strBase64.toCharArray ();
var len64 = chars64.length;
if (len64 == 0) return  Clazz.newByteArray (0, 0);
for (var i = len64; --i >= 0; ) nBytes += ((ch = (chars64[i]).charCodeAt (0) & 0x7F) == 65 || J.io.Base64.decode64[ch] > 0 ? 3 : 0);

nBytes = nBytes >> 2;
var bytes =  Clazz.newByteArray (nBytes, 0);
var offset = 18;
for (var i = 0, pt = 0, b = 0; i < len64; i++) {
if (J.io.Base64.decode64[ch = (chars64[i]).charCodeAt (0) & 0x7F] > 0 || ch == 65 || ch == 61) {
b |= J.io.Base64.decode64[ch] << offset;
offset -= 6;
if (offset < 0) {
bytes[pt++] = ((b & 0xFF0000) >> 16);
if (pt < nBytes) bytes[pt++] = ((b & 0xFF00) >> 8);
if (pt < nBytes) bytes[pt++] = (b & 0xFF);
offset = 18;
b = 0;
}}}
return bytes;
}, "~S");
Clazz.defineStatics (c$,
"base64", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
"decode64", [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62, 0, 62, 0, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0, 0, 0, 0, 63, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 0, 0, 0, 0, 0]);
});
