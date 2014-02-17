Clazz.declarePackage ("J.viewer");
Clazz.load (["J.api.JmolJSpecView"], "J.viewer.JSpecView", ["java.util.Hashtable", "J.util.BS", "$.Logger", "$.Parser", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "JSpecView", null, J.api.JmolJSpecView);
Clazz.overrideMethod (c$, "setViewer", 
function (viewer) {
this.viewer = viewer;
}, "J.viewer.Viewer");
Clazz.overrideMethod (c$, "atomPicked", 
function (atomIndex) {
if (atomIndex < 0) return;
var peak = this.getPeakAtomRecord (atomIndex);
if (peak != null) this.sendJSpecView (peak + " src=\"JmolAtomSelect\"");
}, "~N");
$_M(c$, "getPeakAtomRecord", 
($fz = function (atomIndex) {
var atoms = this.viewer.modelSet.atoms;
var iModel = atoms[atomIndex].modelIndex;
var type = null;
switch (atoms[atomIndex].getElementNumber ()) {
case 1:
type = "1HNMR";
break;
case 6:
type = "13CNMR";
break;
default:
return null;
}
var peaks = this.viewer.getModelAuxiliaryInfoValue (iModel, "jdxAtomSelect_" + type);
if (peaks == null) return null;
this.viewer.modelSet.htPeaks =  new java.util.Hashtable ();
var htPeaks = this.viewer.modelSet.htPeaks;
for (var i = 0; i < peaks.size (); i++) {
var peak = peaks.get (i);
System.out.println ("Jmol JSpecView.java peak=" + peak);
var bsPeak = htPeaks.get (peak);
System.out.println ("Jmol JSpecView.java bspeak=" + bsPeak);
if (bsPeak == null) {
htPeaks.put (peak, bsPeak =  new J.util.BS ());
var satoms = J.util.Parser.getQuotedAttribute (peak, "atoms");
var select = J.util.Parser.getQuotedAttribute (peak, "select");
System.out.println ("Jmol JSpecView.java satoms select " + satoms + " " + select);
var script = "";
if (satoms != null) script += "visible & (atomno=" + J.util.TextFormat.simpleReplace (satoms, ",", " or atomno=") + ")";
 else if (select != null) script += "visible & (" + select + ")";
System.out.println ("Jmol JSpecView.java script : " + script);
bsPeak.or (this.viewer.getAtomBitSet (script));
}System.out.println ("Jmol JSpecView bsPeak now : " + bsPeak + " " + atomIndex);
if (bsPeak.get (atomIndex)) return peak;
}
return null;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "sendJSpecView", 
($fz = function (peak) {
var msg = J.util.Parser.getQuotedAttribute (peak, "title");
if (msg != null) this.viewer.scriptEcho (J.util.Logger.debugging ? peak : msg);
peak = this.viewer.fullName + "JSpecView: " + peak;
J.util.Logger.info ("Jmol>JSV " + peak);
this.viewer.statusManager.syncSend (peak, ">", 0);
}, $fz.isPrivate = true, $fz), "~S");
Clazz.overrideMethod (c$, "setModel", 
function (modelIndex) {
var syncMode = ("sync on".equals (this.viewer.modelSet.getModelSetAuxiliaryInfoValue ("jmolscript")) ? 1 : this.viewer.statusManager.getSyncMode ());
if (syncMode != 1) return;
var peak = this.viewer.getModelAuxiliaryInfoValue (modelIndex, "jdxModelSelect");
if (peak != null) this.sendJSpecView (peak);
}, "~N");
Clazz.overrideMethod (c$, "getBaseModelIndex", 
function (modelIndex) {
var baseModel = this.viewer.getModelAuxiliaryInfoValue (modelIndex, "jdxBaseModel");
if (baseModel != null) for (var i = this.viewer.getModelCount (); --i >= 0; ) if (baseModel.equals (this.viewer.getModelAuxiliaryInfoValue (i, "jdxModelID"))) return i;

return modelIndex;
}, "~N");
});
