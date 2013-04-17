Clazz.declarePackage ("J.bspt");
Clazz.load (null, "J.bspt.Bspf", ["J.bspt.Bspt", "J.util.ArrayUtil", "$.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.dimMax = 0;
this.bspts = null;
this.isValid = false;
this.bsptsValid = null;
this.cubeIterators = null;
Clazz.instantialize (this, arguments);
}, J.bspt, "Bspf");
$_M(c$, "validate", 
function (isValid) {
this.isValid = isValid;
}, "~B");
$_M(c$, "validateModel", 
function (i, isValid) {
this.bsptsValid[i] = isValid;
}, "~N,~B");
$_M(c$, "isInitialized", 
function () {
return this.isValid;
});
$_M(c$, "isInitializedIndex", 
function (bsptIndex) {
return this.bspts.length > bsptIndex && this.bspts[bsptIndex] != null && this.bsptsValid[bsptIndex];
}, "~N");
Clazz.makeConstructor (c$, 
function (dimMax) {
this.dimMax = dimMax;
this.bspts =  new Array (0);
this.bsptsValid =  Clazz.newBooleanArray (0, false);
this.cubeIterators =  new Array (0);
}, "~N");
$_M(c$, "getBsptCount", 
function () {
return this.bspts.length;
});
$_M(c$, "addTuple", 
function (bsptIndex, tuple) {
if (bsptIndex >= this.bspts.length) {
this.bspts = J.util.ArrayUtil.arrayCopyObject (this.bspts, bsptIndex + 1);
this.bsptsValid = J.util.ArrayUtil.arrayCopyBool (this.bsptsValid, bsptIndex + 1);
}var bspt = this.bspts[bsptIndex];
if (bspt == null) {
bspt = this.bspts[bsptIndex] =  new J.bspt.Bspt (this.dimMax, bsptIndex);
}bspt.addTuple (tuple);
}, "~N,J.util.P3");
$_M(c$, "stats", 
function () {
for (var i = 0; i < this.bspts.length; ++i) if (this.bspts[i] != null) this.bspts[i].stats ();

});
$_M(c$, "dump", 
function () {
for (var i = 0; i < this.bspts.length; ++i) {
J.util.Logger.info (">>>>\nDumping bspt #" + i + "\n>>>>");
this.bspts[i].dump ();
}
J.util.Logger.info ("<<<<");
});
$_M(c$, "getCubeIterator", 
function (bsptIndex) {
if (bsptIndex < 0) return this.getNewCubeIterator (-1 - bsptIndex);
if (bsptIndex >= this.cubeIterators.length) this.cubeIterators = J.util.ArrayUtil.arrayCopyObject (this.cubeIterators, bsptIndex + 1);
if (this.cubeIterators[bsptIndex] == null && this.bspts[bsptIndex] != null) this.cubeIterators[bsptIndex] = this.getNewCubeIterator (bsptIndex);
this.cubeIterators[bsptIndex].set (this.bspts[bsptIndex]);
return this.cubeIterators[bsptIndex];
}, "~N");
$_M(c$, "getNewCubeIterator", 
function (bsptIndex) {
return this.bspts[bsptIndex].allocateCubeIterator ();
}, "~N");
$_M(c$, "initialize", 
function (modelIndex, atoms, modelAtomBitSet) {
if (this.bspts[modelIndex] != null) this.bspts[modelIndex].reset ();
for (var i = modelAtomBitSet.nextSetBit (0); i >= 0; i = modelAtomBitSet.nextSetBit (i + 1)) this.addTuple (modelIndex, atoms[i]);

this.bsptsValid[modelIndex] = true;
}, "~N,~A,J.util.BS");
});
