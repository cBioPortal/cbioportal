Clazz.load (["java.io.Closeable"], "java.io.Reader", ["java.io.IOException", "java.lang.IllegalArgumentException", "$.NullPointerException"], function () {
c$ = Clazz.decorateAsClass (function () {
this.lock = null;
this.skipBuffer = null;
Clazz.instantialize (this, arguments);
}, java.io, "Reader", null, java.io.Closeable);
Clazz.makeConstructor (c$, 
function (lock) {
if (lock == null) {
throw  new NullPointerException ();
}this.lock = lock;
}, "~O");
$_M(c$, "skip", 
function (n) {
if (n < 0) throw  new IllegalArgumentException ("skip value is negative");
var nn = Math.min (n, 8192);
{
if ((this.skipBuffer == null) || (this.skipBuffer.length < nn)) this.skipBuffer =  Clazz.newCharArray (nn, '\0');
var r = n;
while (r > 0) {
var nc = this.read (this.skipBuffer, 0, Math.min (r, nn));
if (nc == -1) break;
r -= nc;
}
return n - r;
}}, "~N");
$_M(c$, "ready", 
function () {
return false;
});
$_M(c$, "markSupported", 
function () {
return false;
});
$_M(c$, "mark", 
function (readAheadLimit) {
throw  new java.io.IOException ("mark() not supported");
}, "~N");
$_M(c$, "reset", 
function () {
throw  new java.io.IOException ("reset() not supported");
});
Clazz.defineStatics (c$,
"maxSkipBufferSize", 8192);
});
