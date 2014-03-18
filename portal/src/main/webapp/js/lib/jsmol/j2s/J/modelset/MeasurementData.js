Clazz.declarePackage ("J.modelset");
Clazz.load (["J.api.JmolMeasurementClient"], "J.modelset.MeasurementData", ["java.lang.Float", "J.modelset.Measurement", "J.util.BSUtil", "$.JmolList"], function () {
c$ = Clazz.decorateAsClass (function () {
this.client = null;
this.measurementStrings = null;
this.measurements = null;
this.points = null;
this.mustBeConnected = false;
this.mustNotBeConnected = false;
this.tickInfo = null;
this.tokAction = 1060866;
this.radiusData = null;
this.strFormat = null;
this.note = null;
this.isAll = false;
this.colix = 0;
this.intramolecular = null;
this.mad = 0;
this.thisID = null;
this.text = null;
this.atoms = null;
this.units = null;
this.minArray = null;
this.modelSet = null;
this.viewer = null;
this.iFirstAtom = 0;
this.justOneModel = true;
this.htMin = null;
Clazz.instantialize (this, arguments);
}, J.modelset, "MeasurementData", null, J.api.JmolMeasurementClient);
Clazz.makeConstructor (c$, 
function (id, viewer, points) {
this.viewer = viewer;
this.points = points;
this.thisID = id;
}, "~S,J.viewer.Viewer,J.util.JmolList");
$_M(c$, "setModelSet", 
function (m) {
this.modelSet = m;
return this;
}, "J.modelset.ModelSet");
$_M(c$, "set", 
function (tokAction, htMin, radiusData, strFormat, units, tickInfo, mustBeConnected, mustNotBeConnected, intramolecular, isAll, mad, colix, text) {
this.modelSet = this.viewer.getModelSet ();
this.tokAction = tokAction;
if (this.points.size () >= 2 && Clazz.instanceOf (this.points.get (0), J.util.BS) && Clazz.instanceOf (this.points.get (1), J.util.BS)) {
this.justOneModel = J.util.BSUtil.haveCommon (this.viewer.getModelBitSet (this.points.get (0), false), this.viewer.getModelBitSet (this.points.get (1), false));
}this.htMin = htMin;
this.radiusData = radiusData;
this.strFormat = strFormat;
this.units = units;
this.tickInfo = tickInfo;
this.mustBeConnected = mustBeConnected;
this.mustNotBeConnected = mustNotBeConnected;
this.intramolecular = intramolecular;
this.isAll = isAll;
this.mad = mad;
this.colix = colix;
this.text = text;
return this;
}, "~N,java.util.Map,J.atomdata.RadiusData,~S,~S,J.modelset.TickInfo,~B,~B,Boolean,~B,~N,~N,J.modelset.Text");
$_M(c$, "processNextMeasure", 
function (m) {
var value = m.getMeasurement ();
if (this.htMin != null && !m.isMin (this.htMin) || this.radiusData != null && !m.isInRange (this.radiusData, value)) return;
if (this.measurementStrings == null && this.measurements == null) {
var f = this.minArray[this.iFirstAtom];
m.value = value;
value = m.fixValue (this.units, false);
this.minArray[this.iFirstAtom] = (1 / f == -Infinity ? value : Math.min (f, value));
return;
}if (this.measurementStrings != null) this.measurementStrings.addLast (m.getStringUsing (this.viewer, this.strFormat, this.units));
 else this.measurements.addLast (Float.$valueOf (m.getMeasurement ()));
}, "J.modelset.Measurement");
$_M(c$, "getMeasurements", 
function (asArray, asMinArray) {
if (asMinArray) {
this.minArray =  Clazz.newFloatArray ((this.points.get (0)).cardinality (), 0);
for (var i = 0; i < this.minArray.length; i++) this.minArray[i] = -0.0;

this.define (null, this.modelSet);
return this.minArray;
}if (asArray) {
this.measurements =  new J.util.JmolList ();
this.define (null, this.modelSet);
return this.measurements;
}this.measurementStrings =  new J.util.JmolList ();
this.define (null, this.modelSet);
return this.measurementStrings;
}, "~B,~B");
$_M(c$, "define", 
function (client, modelSet) {
this.client = (client == null ? this : client);
this.atoms = modelSet.atoms;
var nPoints = this.points.size ();
if (nPoints < 2) return;
var modelIndex = -1;
var pts =  new Array (4);
var indices =  Clazz.newIntArray (5, 0);
var m =  new J.modelset.Measurement ().setPoints (modelSet, indices, pts, null);
m.setCount (nPoints);
var ptLastAtom = -1;
for (var i = 0; i < nPoints; i++) {
var obj = this.points.get (i);
if (Clazz.instanceOf (obj, J.util.BS)) {
var bs = obj;
var nAtoms = bs.cardinality ();
if (nAtoms == 0) return;
if (nAtoms > 1) modelIndex = 0;
ptLastAtom = i;
if (i == 0) this.iFirstAtom = 0;
indices[i + 1] = bs.nextSetBit (0);
} else {
pts[i] = obj;
indices[i + 1] = -2 - i;
}}
this.nextMeasure (0, ptLastAtom, m, modelIndex);
}, "J.api.JmolMeasurementClient,J.modelset.ModelSet");
$_M(c$, "nextMeasure", 
($fz = function (thispt, ptLastAtom, m, thisModel) {
if (thispt > ptLastAtom) {
if (m.isValid () && (!this.mustBeConnected || m.isConnected (this.atoms, thispt)) && (!this.mustNotBeConnected || !m.isConnected (this.atoms, thispt)) && (this.intramolecular == null || m.isIntramolecular (this.atoms, thispt) == this.intramolecular.booleanValue ())) this.client.processNextMeasure (m);
return;
}var bs = this.points.get (thispt);
var indices = m.getCountPlusIndices ();
var thisAtomIndex = (thispt == 0 ? 2147483647 : indices[thispt]);
if (thisAtomIndex < 0) {
this.nextMeasure (thispt + 1, ptLastAtom, m, thisModel);
return;
}var haveNext = false;
for (var i = bs.nextSetBit (0), pt = 0; i >= 0; i = bs.nextSetBit (i + 1), pt++) {
if (i == thisAtomIndex) continue;
var modelIndex = this.atoms[i].getModelIndex ();
if (thisModel >= 0 && this.justOneModel) {
if (thispt == 0) thisModel = modelIndex;
 else if (thisModel != modelIndex) continue;
}indices[thispt + 1] = i;
if (thispt == 0) this.iFirstAtom = pt;
haveNext = true;
this.nextMeasure (thispt + 1, ptLastAtom, m, thisModel);
}
if (!haveNext) this.nextMeasure (thispt + 1, ptLastAtom, m, thisModel);
}, $fz.isPrivate = true, $fz), "~N,~N,J.modelset.Measurement,~N");
});
