Clazz.declarePackage ("J.adapter.readers.pymol");
Clazz.load (null, "J.adapter.readers.pymol.JmolObject", ["java.lang.Float", "JU.P3", "$.PT", "$.SB", "J.adapter.readers.pymol.PyMOLScene", "JU.BSUtil", "$.Escape"], function () {
c$ = Clazz.decorateAsClass (function () {
this.id = 0;
this.bsAtoms = null;
this.info = null;
this.size = -1;
this.colors = null;
this.modelIndex = -2147483648;
this.jmolName = null;
this.argb = 0;
this.translucency = 0;
this.visible = true;
this.rd = null;
this.cacheID = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.pymol, "JmolObject");
Clazz.makeConstructor (c$, 
function (id, branchNameID, bsAtoms, info) {
this.id = id;
this.bsAtoms = bsAtoms;
this.info = info;
this.jmolName = branchNameID;
}, "~N,~S,JU.BS,~O");
Clazz.defineMethod (c$, "offset", 
function (modelOffset, atomOffset) {
if (modelOffset > 0) {
if (this.modelIndex != -2147483648) this.modelIndex += modelOffset;
switch (this.id) {
case 1610625028:
case 12294:
return;
case 4115:
var i = (this.info).intValue ();
if (i >= 0) this.info = Integer.$valueOf (modelOffset + i);
return;
case 1073742032:
var movie = this.info;
var frames = movie.get ("frames");
for (var j = frames.length; --j >= 0; ) frames[j] += modelOffset;

return;
}
}if (atomOffset <= 0) return;
if (this.id == 1060866) {
var map = (this.info).values ();
for (var o, $o = map.iterator (); $o.hasNext () && ((o = $o.next ()) || true);) JU.BSUtil.offset (o, 0, atomOffset);

return;
}if (this.bsAtoms != null) JU.BSUtil.offset (this.bsAtoms, 0, atomOffset);
if (this.colors != null) {
var colixes = this.colors[0];
var c =  Clazz.newShortArray (colixes.length + atomOffset, 0);
System.arraycopy (colixes, 0, c, atomOffset, colixes.length);
this.colors[0] = c;
}}, "~N,~N");
Clazz.defineMethod (c$, "finalizeObject", 
function (pymolScene, m, mepList, doCache) {
var sm = m.sm;
var color = "color";
var sID;
var sb = null;
if (this.bsAtoms != null) this.modelIndex = this.getModelIndex (m);
switch (this.id) {
case 3145770:
sm.vwr.displayAtoms (this.bsAtoms, false, false, 1276118017, true);
return;
case 12295:
var bs = sm.vwr.getModelUndeletedAtomsBitSet (this.argb);
JU.BSUtil.invertInPlace (bs, sm.vwr.getAtomCount ());
sm.vwr.select (bs, false, 0, true);
sm.restrictSelected (false, true);
return;
case 1610625028:
case 12294:
if (this.bsAtoms == null) {
if (this.info == null) {
sm.vwr.displayAtoms (null, true, false, 0, true);
}sm.vwr.setObjectProp (this.info, this.id);
} else {
sm.vwr.displayAtoms (this.bsAtoms, this.id == 1610625028, false, 1276118017, true);
}return;
case 1060866:
sm.vwr.defineAtomSets (this.info);
return;
case 1073742032:
sm.vwr.am.setMovie (this.info);
return;
case 4115:
var frame = (this.info).intValue ();
if (frame >= 0) {
sm.vwr.setCurrentModelIndex (frame);
} else {
sm.vwr.setAnimationRange (-1, -1);
sm.vwr.setCurrentModelIndex (-1);
}return;
case 1073742139:
sm.vwr.stm.saveScene (this.jmolName, this.info);
sm.vwr.stm.saveOrientation (this.jmolName, (this.info).get ("pymolView"));
return;
case 5:
sm.loadShape (this.id);
sm.setShapePropertyBs (this.id, "textLabels", this.info, this.bsAtoms);
return;
case 1678770178:
break;
case 659488:
case 1:
if (this.size != -1) {
sm.setShapeSizeBs (1, this.size, null, this.bsAtoms);
var bsBonds = (sm.getShapePropertyIndex (1, "sets", 0))[1];
pymolScene.setUniqueBonds (bsBonds, this.id == 1);
this.size = -1;
}this.id = 1;
break;
case 1141899265:
this.id = 0;
break;
case 0:
break;
case 10:
case 9:
sm.loadShape (this.id);
var bsCarb = m.getAtoms (3145764, null);
JU.BSUtil.andNot (this.bsAtoms, bsCarb);
break;
case 16:
sm.loadShape (this.id);
sm.setShapePropertyBs (this.id, "ignore", JU.BSUtil.copyInvert (this.bsAtoms, sm.vwr.getAtomCount ()), null);
break;
default:
if (!this.visible) return;
break;
}
switch (this.id) {
case 23:
sm.vwr.setCGO (this.info);
break;
case 16:
case 0:
case 7:
case 20:
case 11:
case 9:
case 10:
case 24:
if (Clazz.instanceOf (this.info, Array)) {
sm.loadShape (this.id);
sm.setShapePropertyBs (this.id, "params", this.info, this.bsAtoms);
}break;
case 6:
if (this.modelIndex < 0) return;
sm.loadShape (this.id);
var md = this.info;
md.setModelSet (m);
var points = md.points;
for (var i = points.size (); --i >= 0; ) (points.get (i)).mi = this.modelIndex;

sm.setShapePropertyBs (this.id, "measure", md, this.bsAtoms);
return;
case 135180:
sID = (this.bsAtoms == null ? this.info : this.jmolName);
if (sm.getShapeIdFromObjectName (sID) >= 0) {
sm.vwr.setObjectProp (sID, 1610625028);
return;
}sb =  new JU.SB ();
sb.append ("isosurface ID ").append (JU.PT.esc (sID));
if (this.modelIndex < 0) this.modelIndex = sm.vwr.am.cmi;
if (this.bsAtoms == null) {
sb.append (" model ").append (m.am[this.modelIndex].getModelNumberDotted ()).append (" color density sigma 1.0 ").append (JU.PT.esc (this.cacheID)).append (" ").append (JU.PT.esc (sID));
if (doCache) sb.append (";isosurface cache");
} else {
var lighting = (this.info)[0];
var only = (this.info)[1];
only = " only";
var bsCarve = (this.info)[2];
var carveDistance = ((this.info)[3]).floatValue ();
var resolution = "";
if (lighting == null) {
lighting = "mesh nofill";
resolution = " resolution 1.5";
}var haveMep = JU.PT.isOneOf (sID, mepList);
var model = m.am[this.modelIndex].getModelNumberDotted ();
var ignore = "";
var type = (this.size < 0 ? " sasurface " : " solvent ");
sb.append (" model ").append (model).append (resolution).append (" select ").append (JU.Escape.eBS (this.bsAtoms)).append (only).append (ignore).append (type).appendF (Math.abs (this.size / 1000));
if (!haveMep) {
if (this.argb == 0) sb.append (" map property color");
 else sb.append (";color isosurface ").append (JU.Escape.escapeColor (this.argb));
}sb.append (";isosurface frontOnly ").append (lighting);
if (this.translucency > 0) sb.append (";color isosurface translucent " + this.translucency);
if (bsCarve != null && !bsCarve.isEmpty ()) sb.append (";isosurface slab within " + carveDistance + " {" + model + " and " + JU.Escape.eBS (bsCarve) + "}");
if (doCache && !haveMep) sb.append (";isosurface cache");
}break;
case 1073742016:
var mep = this.info;
sID = mep.get (mep.size () - 2).toString ();
var mapID = mep.get (mep.size () - 1).toString ();
var min = J.adapter.readers.pymol.PyMOLScene.floatAt (J.adapter.readers.pymol.PyMOLScene.listAt (mep, 3), 0);
var max = J.adapter.readers.pymol.PyMOLScene.floatAt (J.adapter.readers.pymol.PyMOLScene.listAt (mep, 3), 2);
sb =  new JU.SB ();
sb.append (";isosurface ID ").append (JU.PT.esc (sID)).append (" map ").append (JU.PT.esc (this.cacheID)).append (" ").append (JU.PT.esc (mapID)).append (";color isosurface range " + min + " " + max + ";isosurface colorscheme rwb;set isosurfacekey true");
if (this.translucency > 0) sb.append (";color isosurface translucent " + this.translucency);
if (doCache) sb.append (";isosurface cache");
break;
case 1073742018:
this.modelIndex = sm.vwr.am.cmi;
var mesh = this.info;
sID = mesh.get (mesh.size () - 2).toString ();
sb =  new JU.SB ();
sb.append ("isosurface ID ").append (JU.PT.esc (sID)).append (" model ").append (m.am[this.modelIndex].getModelNumberDotted ()).append (" color ").append (JU.Escape.escapeColor (this.argb)).append ("  ").append (JU.PT.esc (this.cacheID)).append (" ").append (JU.PT.esc (sID)).append (" mesh nofill frontonly");
var within = J.adapter.readers.pymol.PyMOLScene.floatAt (J.adapter.readers.pymol.PyMOLScene.listAt (J.adapter.readers.pymol.PyMOLScene.listAt (mesh, 2), 0), 11);
var list = J.adapter.readers.pymol.PyMOLScene.listAt (J.adapter.readers.pymol.PyMOLScene.listAt (J.adapter.readers.pymol.PyMOLScene.listAt (mesh, 2), 0), 12);
if (within > 0) {
var pt =  new JU.P3 ();
sb.append (";isosurface slab within ").appendF (within).append (" [ ");
for (var j = list.size () - 3; j >= 0; j -= 3) {
J.adapter.readers.pymol.PyMOLScene.pointAt (list, j, pt);
sb.append (JU.Escape.eP (pt));
}
sb.append (" ]");
}if (doCache && !JU.PT.isOneOf (sID, mepList)) sb.append (";isosurface cache");
sb.append (";set meshScale ").appendI (Clazz.doubleToInt (this.size / 500));
break;
case 135271429:
sb = this.info;
break;
case 1113200654:
sm.loadShape (this.id = 10);
sm.setShapePropertyBs (this.id, "putty", this.info, this.bsAtoms);
break;
}
if (sb != null) {
sm.vwr.runScript (sb.toString ());
return;
}if (this.size != -1 || this.rd != null) sm.setShapeSizeBs (this.id, this.size, this.rd, this.bsAtoms);
if (this.argb != 0) sm.setShapePropertyBs (this.id, color, Integer.$valueOf (this.argb), this.bsAtoms);
if (this.translucency > 0) {
sm.setShapePropertyBs (this.id, "translucentLevel", Float.$valueOf (this.translucency), this.bsAtoms);
sm.setShapePropertyBs (this.id, "translucency", "translucent", this.bsAtoms);
} else if (this.colors != null) sm.setShapePropertyBs (this.id, "colors", this.colors, this.bsAtoms);
}, "J.adapter.readers.pymol.PyMOLScene,JM.ModelSet,~S,~B");
Clazz.defineMethod (c$, "getModelIndex", 
 function (m) {
if (this.bsAtoms == null) return -1;
var iAtom = this.bsAtoms.nextSetBit (0);
if (iAtom >= m.at.length) System.out.println ("PyMOL LOADING ERROR IN MERGE");
return (iAtom < 0 ? -1 : m.at[iAtom].mi);
}, "JM.ModelSet");
Clazz.defineMethod (c$, "setColors", 
function (colixes, translucency) {
this.colors = [colixes, Float.$valueOf (translucency)];
}, "~A,~N");
Clazz.defineMethod (c$, "setSize", 
function (size) {
this.size = Clazz.floatToInt (size * 1000);
}, "~N");
});
