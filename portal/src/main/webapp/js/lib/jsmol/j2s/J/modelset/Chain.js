Clazz.declarePackage ("J.modelset");
c$ = Clazz.decorateAsClass (function () {
this.model = null;
this.chainID = 0;
this.isDna = false;
this.isRna = false;
this.groupCount = 0;
this.groups = null;
this.selectedGroupCount = 0;
Clazz.instantialize (this, arguments);
}, J.modelset, "Chain");
Clazz.prepareFields (c$, function () {
this.groups =  new Array (16);
});
$_M(c$, "getAtom", 
function (index) {
return this.model.modelSet.atoms[index];
}, "~N");
Clazz.makeConstructor (c$, 
function (model, chainID) {
this.model = model;
this.chainID = chainID;
}, "J.modelset.Model,~N");
$_M(c$, "getGroup", 
function (groupIndex) {
return this.groups[groupIndex];
}, "~N");
$_M(c$, "getGroupCount", 
function () {
return this.groupCount;
});
$_M(c$, "calcSelectedGroupsCount", 
function (bsSelected) {
this.selectedGroupCount = 0;
for (var i = 0; i < this.groupCount; i++) this.groups[i].selectedIndex = (this.groups[i].isSelected (bsSelected) ? this.selectedGroupCount++ : -1);

}, "J.util.BS");
$_M(c$, "selectSeqcodeRange", 
function (index0, seqcodeA, seqcodeB, bs) {
var seqcode;
var indexA;
var indexB;
var minDiff;
var isInexact = false;
for (indexA = index0; indexA < this.groupCount && this.groups[indexA].seqcode != seqcodeA; indexA++) {
}
if (indexA == this.groupCount) {
if (index0 > 0) return -1;
isInexact = true;
minDiff = 2147483647;
for (var i = this.groupCount; --i >= 0; ) if ((seqcode = this.groups[i].seqcode) > seqcodeA && (seqcode - seqcodeA) < minDiff) {
indexA = i;
minDiff = seqcode - seqcodeA;
}
if (minDiff == 2147483647) return -1;
}if (seqcodeB == 2147483647) {
indexB = this.groupCount - 1;
isInexact = true;
} else {
for (indexB = indexA; indexB < this.groupCount && this.groups[indexB].seqcode != seqcodeB; indexB++) {
}
if (indexB == this.groupCount) {
if (index0 > 0) return -1;
isInexact = true;
minDiff = 2147483647;
for (var i = indexA; i < this.groupCount; i++) if ((seqcode = this.groups[i].seqcode) < seqcodeB && (seqcodeB - seqcode) < minDiff) {
indexB = i;
minDiff = seqcodeB - seqcode;
}
if (minDiff == 2147483647) return -1;
}}for (var i = indexA; i <= indexB; ++i) this.groups[i].selectAtoms (bs);

return (isInexact ? -1 : indexB + 1);
}, "~N,~N,~N,J.util.BS");
$_M(c$, "fixIndices", 
function (atomsDeleted, bsDeleted) {
for (var i = 0; i < this.groupCount; i++) this.groups[i].fixIndices (atomsDeleted, bsDeleted);

}, "~N,J.util.BS");
$_M(c$, "setAtomBitSet", 
function (bs) {
for (var i = 0; i < this.groupCount; i++) this.groups[i].selectAtoms (bs);

}, "J.util.BS");
$_M(c$, "getIDStr", 
function () {
return (this.chainID == 0 ? "" : this.chainID < 256 ? "" + String.fromCharCode (this.chainID) : this.model.modelSet.viewer.chainMap.get (Integer.$valueOf (this.chainID)));
});
