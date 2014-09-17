Clazz.declarePackage ("JS");
c$ = Clazz.decorateAsClass (function () {
this.search = null;
this.nPoints = 0;
this.type = 0;
this.index = 0;
this.isNot = false;
this.indices = null;
this.min = 0;
this.max = 0;
this.points = null;
Clazz.instantialize (this, arguments);
}, JS, "SmilesMeasure");
Clazz.prepareFields (c$, function () {
this.indices =  Clazz.newIntArray (4, 0);
this.points =  new Array (4);
});
Clazz.makeConstructor (c$, 
function (search, index, type, min, max, isNot) {
this.search = search;
this.type = Math.min (4, Math.max (type, 2));
this.index = index;
this.min = Math.min (min, max);
this.max = Math.max (min, max);
this.isNot = isNot;
}, "JS.SmilesSearch,~N,~N,~N,~N,~B");
Clazz.overrideMethod (c$, "toString", 
function () {
var s = "(." + "__dat".charAt (this.type) + this.index + ":" + this.min + "," + this.max + ") for";
for (var i = 0; i < this.type; i++) s += " " + (i >= this.nPoints ? "?" : "" + this.indices[i]);

return s;
});
Clazz.defineMethod (c$, "addPoint", 
function (index) {
if (this.nPoints == this.type) return false;
if (this.nPoints == 0) for (var i = 1; i < this.type; i++) this.indices[i] = index + i;

this.indices[this.nPoints++] = index;
return true;
}, "~N");
Clazz.defineMethod (c$, "check", 
function () {
for (var i = 0; i < this.type; i++) {
var iAtom = this.search.patternAtoms[this.indices[i]].getMatchingAtom ();
this.points[i] = this.search.jmolAtoms[iAtom];
}
var d = 0;
switch (this.type) {
case 2:
d = this.points[0].distance (this.points[1]);
break;
case 3:
this.search.v.vA.sub2 (this.points[0], this.points[1]);
this.search.v.vB.sub2 (this.points[2], this.points[1]);
d = this.search.v.vA.angle (this.search.v.vB) / 0.017453292;
break;
case 4:
JS.SmilesMeasure.setTorsionData (this.points[0], this.points[1], this.points[2], this.points[3], this.search.v, true);
d = this.search.v.vTemp1.angle (this.search.v.vTemp2) / 0.017453292 * (this.search.v.vNorm1.dot (this.search.v.vNorm2) < 0 ? 1 : -1);
break;
}
return ((d < this.min || d > this.max) == this.isNot);
});
c$.setTorsionData = Clazz.defineMethod (c$, "setTorsionData", 
function (pt1a, pt1, pt2, pt2a, v, isAll) {
v.vTemp1.sub2 (pt1a, pt1);
v.vTemp2.sub2 (pt2a, pt2);
if (!isAll) return;
v.vNorm1.sub2 (pt1, pt2);
v.vNorm1.normalize ();
v.vTemp1.cross (v.vTemp1, v.vNorm1);
v.vTemp1.normalize ();
v.vTemp2.cross (v.vTemp2, v.vNorm1);
v.vTemp2.normalize ();
v.vNorm2.cross (v.vTemp1, v.vTemp2);
}, "JU.P3,JU.P3,JU.P3,JU.P3,JS.VTemp,~B");
Clazz.defineStatics (c$,
"TYPES", "__dat",
"radiansPerDegree", (0.017453292519943295));
