Clazz.load (null, "java.io.InputStream", ["java.io.IOException", "java.lang.IndexOutOfBoundsException", "$.NullPointerException"], function () {
c$ = Clazz.declareType (java.io, "InputStream");
$_M(c$, "read", 
function (b, off, len) {
if (b == null) {
throw  new NullPointerException ();
} else if (off < 0 || len < 0 || len > b.length - off) {
throw  new IndexOutOfBoundsException ();
} else if (len == 0) {
return 0;
}var c = this.readByteAsInt ();
if (c == -1) {
return -1;
}b[off] = c;
var i = 1;
try {
for (; i < len; i++) {
c = this.readByteAsInt ();
if (c == -1) {
break;
}b[off + i] = c;
}
} catch (ee) {
if (Clazz.exceptionOf (ee, java.io.IOException)) {
} else {
throw ee;
}
}
return i;
}, "~A,~N,~N");
$_M(c$, "skip", 
function (n) {
var remaining = n;
var nr;
if (java.io.InputStream.skipBuffer == null) ($t$ = java.io.InputStream.skipBuffer =  Clazz.newByteArray (2048, 0), java.io.InputStream.prototype.skipBuffer = java.io.InputStream.skipBuffer, $t$);
var localSkipBuffer = java.io.InputStream.skipBuffer;
if (n <= 0) {
return 0;
}while (remaining > 0) {
nr = this.read (localSkipBuffer, 0, Math.min (2048, remaining));
if (nr < 0) {
break;
}remaining -= nr;
}
return n - remaining;
}, "~N");
$_M(c$, "available", 
function () {
return 0;
});
$_M(c$, "close", 
function () {
});
$_M(c$, "mark", 
function (readlimit) {
}, "~N");
$_M(c$, "reset", 
function () {
throw  new java.io.IOException ("mark/reset not supported");
});
$_M(c$, "markSupported", 
function () {
return false;
});
$_M(c$, "resetStream", 
function () {
});
Clazz.defineStatics (c$,
"SKIP_BUFFER_SIZE", 2048,
"skipBuffer", null);
});
