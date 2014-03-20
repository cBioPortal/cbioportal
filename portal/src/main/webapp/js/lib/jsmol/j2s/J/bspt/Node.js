Clazz.declarePackage ("J.bspt");
Clazz.load (["J.bspt.Element"], "J.bspt.Node", ["java.lang.NullPointerException", "J.bspt.Leaf"], function () {
c$ = Clazz.decorateAsClass (function () {
this.dim = 0;
this.minLeft = 0;
this.maxLeft = 0;
this.eleLeft = null;
this.minRight = 0;
this.maxRight = 0;
this.eleRight = null;
Clazz.instantialize (this, arguments);
}, J.bspt, "Node", J.bspt.Element);
Clazz.makeConstructor (c$, 
function (bspt, level, leafLeft) {
Clazz.superConstructor (this, J.bspt.Node, []);
this.bspt = bspt;
if (level == bspt.treeDepth) {
bspt.treeDepth = level + 1;
}if (leafLeft.count != 2) throw  new NullPointerException ();
this.dim = level % bspt.dimMax;
leafLeft.sort (this.dim);
var leafRight =  new J.bspt.Leaf (bspt, leafLeft, 1);
this.minLeft = J.bspt.Node.getDimensionValue (leafLeft.tuples[0], this.dim);
this.maxLeft = J.bspt.Node.getDimensionValue (leafLeft.tuples[leafLeft.count - 1], this.dim);
this.minRight = J.bspt.Node.getDimensionValue (leafRight.tuples[0], this.dim);
this.maxRight = J.bspt.Node.getDimensionValue (leafRight.tuples[leafRight.count - 1], this.dim);
this.eleLeft = leafLeft;
this.eleRight = leafRight;
this.count = 2;
}, "J.bspt.Bspt,~N,J.bspt.Leaf");
$_M(c$, "addTuple", 
function (level, tuple) {
var dimValue = J.bspt.Node.getDimensionValue (tuple, this.dim);
++this.count;
var addLeft;
if (dimValue < this.maxLeft) {
addLeft = true;
} else if (dimValue > this.minRight) {
addLeft = false;
} else if (dimValue == this.maxLeft) {
if (dimValue == this.minRight) {
if (this.eleLeft.count < this.eleRight.count) addLeft = true;
 else addLeft = false;
} else {
addLeft = true;
}} else if (dimValue == this.minRight) {
addLeft = false;
} else {
if (this.eleLeft.count < this.eleRight.count) addLeft = true;
 else addLeft = false;
}if (addLeft) {
if (dimValue < this.minLeft) this.minLeft = dimValue;
 else if (dimValue > this.maxLeft) this.maxLeft = dimValue;
this.eleLeft = this.eleLeft.addTuple (level + 1, tuple);
} else {
if (dimValue < this.minRight) this.minRight = dimValue;
 else if (dimValue > this.maxRight) this.maxRight = dimValue;
this.eleRight = this.eleRight.addTuple (level + 1, tuple);
}return this;
}, "~N,J.util.P3");
$_M(c$, "dump", 
function (level, sb) {
sb.append ("\nnode LEFT" + level);
this.eleLeft.dump (level + 1, sb);
for (var i = 0; i < level; ++i) sb.append ("->");

sb.append (" RIGHT" + level);
this.eleRight.dump (level + 1, sb);
}, "~N,J.util.SB");
$_M(c$, "toString", 
function () {
return this.eleLeft.toString () + this.dim + ":" + "\n" + this.eleRight.toString ();
});
c$.getDimensionValue = $_M(c$, "getDimensionValue", 
function (pt, dim) {
return (dim == 0 ? pt.x : dim == 1 ? pt.y : pt.z);
}, "J.util.P3,~N");
});
