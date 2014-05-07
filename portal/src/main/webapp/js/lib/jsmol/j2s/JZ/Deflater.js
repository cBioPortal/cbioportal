Clazz.declarePackage ("JZ");
Clazz.load (["JZ.ZStream"], "JZ.Deflater", ["JZ.Deflate"], function () {
c$ = Clazz.decorateAsClass (function () {
this.$finished = false;
Clazz.instantialize (this, arguments);
}, JZ, "Deflater", JZ.ZStream);
$_M(c$, "init", 
function (level, bits, nowrap) {
if (bits == 0) bits = 15;
this.$finished = false;
this.setAdler32 ();
this.dstate =  new JZ.Deflate (this);
this.dstate.deflateInit2 (level, nowrap ? -bits : bits);
return this;
}, "~N,~N,~B");
Clazz.overrideMethod (c$, "deflate", 
function (flush) {
if (this.dstate == null) {
return -2;
}var ret = this.dstate.deflate (flush);
if (ret == 1) this.$finished = true;
return ret;
}, "~N");
Clazz.overrideMethod (c$, "end", 
function () {
this.$finished = true;
if (this.dstate == null) return -2;
var ret = this.dstate.deflateEnd ();
this.dstate = null;
this.free ();
return ret;
});
$_M(c$, "params", 
function (level, strategy) {
if (this.dstate == null) return -2;
return this.dstate.deflateParams (level, strategy);
}, "~N,~N");
$_M(c$, "setDictionary", 
function (dictionary, dictLength) {
if (this.dstate == null) return -2;
return this.dstate.deflateSetDictionary (dictionary, dictLength);
}, "~A,~N");
Clazz.overrideMethod (c$, "finished", 
function () {
return this.$finished;
});
$_M(c$, "finish", 
function () {
});
$_M(c$, "getBytesRead", 
function () {
return this.dstate.getBytesRead ();
});
$_M(c$, "getBytesWritten", 
function () {
return this.dstate.getBytesWritten ();
});
Clazz.defineStatics (c$,
"MAX_WBITS", 15,
"Z_STREAM_END", 1,
"$Z_STREAM_ERROR", -2);
});
