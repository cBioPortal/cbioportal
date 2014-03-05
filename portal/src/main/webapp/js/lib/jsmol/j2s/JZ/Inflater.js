Clazz.declarePackage ("JZ");
Clazz.load (["JZ.ZStream"], "JZ.Inflater", ["JZ.Inflate"], function () {
c$ = Clazz.declareType (JZ, "Inflater", JZ.ZStream);
$_M(c$, "init", 
function (w, nowrap) {
this.setAdler32 ();
if (w == 0) w = 15;
this.istate =  new JZ.Inflate (this);
this.istate.inflateInit (nowrap ? -w : w);
return this;
}, "~N,~B");
Clazz.overrideMethod (c$, "inflate", 
function (f) {
if (this.istate == null) return -2;
var ret = this.istate.inflate (f);
return ret;
}, "~N");
Clazz.overrideMethod (c$, "end", 
function () {
if (this.istate == null) return -2;
var ret = this.istate.inflateEnd ();
return ret;
});
$_M(c$, "sync", 
function () {
if (this.istate == null) return -2;
return this.istate.inflateSync ();
});
$_M(c$, "syncPoint", 
function () {
if (this.istate == null) return -2;
return this.istate.inflateSyncPoint ();
});
$_M(c$, "setDictionary", 
function (dictionary, dictLength) {
if (this.istate == null) return -2;
return this.istate.inflateSetDictionary (dictionary, dictLength);
}, "~A,~N");
Clazz.overrideMethod (c$, "finished", 
function () {
return this.istate.mode == 12;
});
$_M(c$, "reset", 
function () {
this.avail_in = 0;
if (this.istate != null) this.istate.reset ();
});
Clazz.defineStatics (c$,
"MAX_WBITS", 15,
"DEF_WBITS", 15,
"$Z_STREAM_ERROR", -2);
});
