Clazz.load (["java.io.InputStream"], "java.io.FilterInputStream", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.$in = null;
Clazz.instantialize (this, arguments);
}, java.io, "FilterInputStream", java.io.InputStream);
Clazz.makeConstructor (c$, 
function ($in) {
Clazz.superConstructor (this, java.io.FilterInputStream, []);
this.$in = $in;
}, "java.io.InputStream");
$_M(c$, "readByteAsInt", 
function () {
return this.$in.readByteAsInt ();
});
$_M(c$, "read", 
function (b, off, len) {
return this.$in.read (b, off, len);
}, "~A,~N,~N");
$_M(c$, "skip", 
function (n) {
return this.$in.skip (n);
}, "~N");
$_M(c$, "available", 
function () {
return this.$in.available ();
});
$_M(c$, "close", 
function () {
this.$in.close ();
});
$_M(c$, "mark", 
function (readlimit) {
this.$in.mark (readlimit);
}, "~N");
$_M(c$, "reset", 
function () {
this.$in.reset ();
});
$_M(c$, "markSupported", 
function () {
return this.$in.markSupported ();
});
});
