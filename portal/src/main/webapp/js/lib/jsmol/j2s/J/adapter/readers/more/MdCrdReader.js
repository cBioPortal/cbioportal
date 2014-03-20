Clazz.declarePackage ("J.adapter.readers.more");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.more.MdCrdReader", ["java.lang.Float", "J.util.Logger", "$.P3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.ptFloat = 0;
this.lenLine = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.more, "MdCrdReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.initializeTrajectoryFile ();
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
this.readCoordinates ();
J.util.Logger.info ("Total number of trajectory steps=" + this.trajectorySteps.size ());
this.continuing = false;
return false;
});
$_M(c$, "readCoordinates", 
($fz = function () {
this.line = null;
var atomCount = (this.bsFilter == null ? this.templateAtomCount : (this.htParams.get ("filteredAtomCount")).intValue ());
var isPeriodic = this.htParams.containsKey ("isPeriodic");
var floatCount = this.templateAtomCount * 3 + (isPeriodic ? 3 : 0);
while (true) if (this.doGetModel (++this.modelNumber, null)) {
var trajectoryStep =  new Array (atomCount);
if (!this.getTrajectoryStep (trajectoryStep, isPeriodic)) return;
this.trajectorySteps.addLast (trajectoryStep);
if (this.isLastModel (this.modelNumber)) return;
} else {
if (!this.skipFloats (floatCount)) return;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "getFloat", 
($fz = function () {
while (this.line == null || this.ptFloat >= this.lenLine) {
if (this.readLine () == null) return NaN;
this.ptFloat = 0;
this.lenLine = this.line.length;
}
this.ptFloat += 8;
return this.parseFloatRange (this.line, this.ptFloat - 8, this.ptFloat);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getPoint", 
($fz = function () {
var x = this.getFloat ();
var y = this.getFloat ();
var z = this.getFloat ();
return (Float.isNaN (z) ? null : J.util.P3.new3 (x, y, z));
}, $fz.isPrivate = true, $fz));
$_M(c$, "getTrajectoryStep", 
($fz = function (trajectoryStep, isPeriodic) {
var atomCount = trajectoryStep.length;
var n = -1;
for (var i = 0; i < this.templateAtomCount; i++) {
var pt = this.getPoint ();
if (pt == null) return false;
if (this.bsFilter == null || this.bsFilter.get (i)) {
if (++n == atomCount) return false;
trajectoryStep[n] = pt;
}}
if (isPeriodic) this.getPoint ();
return (this.line != null);
}, $fz.isPrivate = true, $fz), "~A,~B");
$_M(c$, "skipFloats", 
($fz = function (n) {
var i = 0;
while (i < n && this.readLine () != null) i += this.getTokens ().length;

return (this.line != null);
}, $fz.isPrivate = true, $fz), "~N");
});
