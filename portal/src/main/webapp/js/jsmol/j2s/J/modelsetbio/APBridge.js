Clazz.declarePackage ("J.modelsetbio");
Clazz.load (null, "J.modelsetbio.APBridge", ["java.lang.Boolean", "J.util.Escape"], function () {
c$ = Clazz.decorateAsClass (function () {
this.a = null;
this.b = null;
this.ladder = null;
this.isAntiparallel = false;
Clazz.instantialize (this, arguments);
}, J.modelsetbio, "APBridge");
Clazz.makeConstructor (c$, 
function (a, b, htLadders) {
this.a = a;
this.b = b;
this.ladder =  Clazz.newIntArray (2, 2, 0);
this.ladder[0][0] = this.ladder[0][1] = Math.min (a.index, b.index);
this.ladder[1][0] = this.ladder[1][1] = Math.max (a.index, b.index);
this.addLadder (htLadders);
}, "J.modelset.Atom,J.modelset.Atom,java.util.Map");
$_M(c$, "addLadder", 
($fz = function (htLadders) {
htLadders.put (this.ladder, (this.isAntiparallel ? Boolean.TRUE : Boolean.FALSE));
}, $fz.isPrivate = true, $fz), "java.util.Map");
Clazz.overrideMethod (c$, "toString", 
function () {
return (this.isAntiparallel ? "a " : "p ") + this.a + " - " + this.b + "\t" + J.util.Escape.e (this.ladder);
});
$_M(c$, "addBridge", 
function (bridge, htLadders) {
if (bridge == null || bridge.isAntiparallel != this.isAntiparallel || !this.canAdd (bridge) || !bridge.canAdd (this)) return false;
this.extendLadder (bridge.ladder[0][0], bridge.ladder[1][0]);
this.extendLadder (bridge.ladder[0][1], bridge.ladder[1][1]);
bridge.ladder = this.ladder;
if (bridge.ladder !== this.ladder) {
htLadders.remove (bridge.ladder);
this.addLadder (htLadders);
}return true;
}, "J.modelsetbio.APBridge,java.util.Map");
$_M(c$, "canAdd", 
($fz = function (bridge) {
var index1 = bridge.a.index;
var index2 = bridge.b.index;
return (this.isAntiparallel ? (index1 >= this.ladder[0][1] && index2 <= this.ladder[1][0] || index1 <= this.ladder[0][0] && index2 >= this.ladder[1][1]) : (index1 <= this.ladder[0][0] && index2 <= this.ladder[1][0] || index1 >= this.ladder[0][1] && index2 >= this.ladder[1][1]));
}, $fz.isPrivate = true, $fz), "J.modelsetbio.APBridge");
$_M(c$, "extendLadder", 
($fz = function (index1, index2) {
if (this.ladder[0][0] > index1) this.ladder[0][0] = index1;
if (this.ladder[0][1] < index1) this.ladder[0][1] = index1;
if (this.ladder[1][0] > index2) this.ladder[1][0] = index2;
if (this.ladder[1][1] < index2) this.ladder[1][1] = index2;
}, $fz.isPrivate = true, $fz), "~N,~N");
});
