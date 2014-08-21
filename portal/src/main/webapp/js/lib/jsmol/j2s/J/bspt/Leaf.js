Clazz.declarePackage ("J.bspt");
Clazz.load (["J.bspt.Element"], "J.bspt.Leaf", ["J.bspt.Node", "JU.Escape"], function () {
c$ = Clazz.decorateAsClass (function () {
this.tuples = null;
Clazz.instantialize (this, arguments);
}, J.bspt, "Leaf", J.bspt.Element);
Clazz.makeConstructor (c$, 
function (bspt, leaf, countToKeep) {
Clazz.superConstructor (this, J.bspt.Leaf, []);
this.bspt = bspt;
this.count = 0;
this.tuples =  new Array (2);
if (leaf == null) return;
for (var i = countToKeep; i < 2; ++i) {
this.tuples[this.count++] = leaf.tuples[i];
leaf.tuples[i] = null;
}
leaf.count = countToKeep;
}, "J.bspt.Bspt,J.bspt.Leaf,~N");
Clazz.defineMethod (c$, "sort", 
function (dim) {
for (var i = this.count; --i > 0; ) {
var champion = this.tuples[i];
var championValue = J.bspt.Node.getDimensionValue (champion, dim);
for (var j = i; --j >= 0; ) {
var challenger = this.tuples[j];
var challengerValue = J.bspt.Node.getDimensionValue (challenger, dim);
if (challengerValue > championValue) {
this.tuples[i] = challenger;
this.tuples[j] = champion;
champion = challenger;
championValue = challengerValue;
}}
}
}, "~N");
Clazz.overrideMethod (c$, "addTuple", 
function (level, tuple) {
if (this.count < 2) {
this.tuples[this.count++] = tuple;
return this;
}var node =  new J.bspt.Node (this.bspt, level, this);
return node.addTuple (level, tuple);
}, "~N,JU.P3");
Clazz.overrideMethod (c$, "dump", 
function (level, sb) {
for (var i = 0; i < this.count; ++i) {
var t = this.tuples[i];
for (var j = 0; j < level; ++j) sb.append (".");

sb.append (JU.Escape.eP (t)).append ("Leaf ").appendI (i).append (": ").append ((t).getInfo ());
}
}, "~N,JU.SB");
Clazz.overrideMethod (c$, "toString", 
function () {
return "leaf:" + this.count + "\n";
});
});
