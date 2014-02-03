Clazz.declarePackage ("J.viewer");
Clazz.load (["J.api.JmolStateCreator", "java.util.Hashtable"], "J.viewer.StateCreator", ["java.lang.Boolean", "$.Float", "java.util.Arrays", "$.Date", "J.constant.EnumAxesMode", "$.EnumPalette", "$.EnumStereoMode", "$.EnumStructure", "$.EnumVdw", "J.i18n.GT", "J.io.Base64", "$.JmolBinary", "$.OutputStringBuilder", "J.modelset.AtomCollection", "$.Bond", "$.Object2d", "J.shape.Shape", "J.util.BSUtil", "$.C", "$.ColorEncoder", "$.Escape", "$.JmolEdge", "$.JmolFont", "$.JmolList", "$.Logger", "$.P3", "$.Parser", "$.SB", "$.TextFormat", "$.V3", "J.viewer.DataManager", "$.FileManager", "$.JC", "$.StateManager", "$.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.privateKey = 0;
this.temp = null;
this.temp2 = null;
this.temp3 = null;
this.undoWorking = false;
this.logFileName = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "StateCreator", null, J.api.JmolStateCreator);
Clazz.prepareFields (c$, function () {
this.temp =  new java.util.Hashtable ();
this.temp2 =  new java.util.Hashtable ();
this.temp3 =  new java.util.Hashtable ();
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "setViewer", 
function (viewer, privateKey) {
this.viewer = viewer;
this.privateKey = privateKey;
}, "J.viewer.Viewer,~N");
Clazz.overrideMethod (c$, "getWrappedState", 
function (fileName, scripts, isImage, asJmolZip, width, height) {
if (isImage && !this.viewer.global.imageState && !asJmolZip || !this.viewer.global.preserveState) return "";
var s = this.viewer.getStateInfo3 (null, width, height);
if (asJmolZip) {
if (fileName != null) this.viewer.fileManager.clearPngjCache (fileName);
return J.io.JmolBinary.createZipSet (this.privateKey, this.viewer.fileManager, this.viewer, null, s, scripts, true);
}try {
s = J.viewer.JC.embedScript (J.viewer.FileManager.setScriptFileReferences (s, ".", null, null));
} catch (e) {
J.util.Logger.error ("state could not be saved: " + e.toString ());
s = "Jmol " + J.viewer.Viewer.getJmolVersion ();
}
return s;
}, "~S,~A,~B,~B,~N,~N");
Clazz.overrideMethod (c$, "getStateScript", 
function (type, width, height) {
var isAll = (type == null || type.equalsIgnoreCase ("all"));
var s =  new J.util.SB ();
var sfunc = (isAll ?  new J.util.SB ().append ("function _setState() {\n") : null);
if (isAll) s.append ("# Jmol state version " + J.viewer.Viewer.getJmolVersion () + ";\n");
if (this.viewer.isApplet () && isAll) {
J.viewer.StateCreator.appendCmd (s, "# fullName = " + J.util.Escape.eS (this.viewer.fullName));
J.viewer.StateCreator.appendCmd (s, "# documentBase = " + J.util.Escape.eS (this.viewer.appletDocumentBase));
J.viewer.StateCreator.appendCmd (s, "# codeBase = " + J.util.Escape.eS (this.viewer.appletCodeBase));
s.append ("\n");
}var global = this.viewer.global;
if (isAll || type.equalsIgnoreCase ("windowState")) s.append (this.getWindowState (sfunc, width, height));
if (isAll || type.equalsIgnoreCase ("fileState")) s.append (this.getFileState (sfunc));
if (isAll || type.equalsIgnoreCase ("definedState")) s.append (this.getDefinedState (sfunc, true));
if (isAll || type.equalsIgnoreCase ("variableState")) s.append (this.getVariableState (global, sfunc));
if (isAll || type.equalsIgnoreCase ("dataState")) this.getDataState (this.viewer.dataManager, s, sfunc, this.getAtomicPropertyState (-1, null));
if (isAll || type.equalsIgnoreCase ("modelState")) s.append (this.getModelState (sfunc, true, this.viewer.getBooleanProperty ("saveProteinStructureState")));
if (isAll || type.equalsIgnoreCase ("colorState")) s.append (this.getColorState (this.viewer.colorManager, sfunc));
if (isAll || type.equalsIgnoreCase ("frameState")) s.append (this.getAnimState (this.viewer.animationManager, sfunc));
if (isAll || type.equalsIgnoreCase ("perspectiveState")) s.append (this.getViewState (this.viewer.transformManager, sfunc));
if (isAll || type.equalsIgnoreCase ("selectionState")) s.append (this.getSelectionState (this.viewer.selectionManager, sfunc));
if (sfunc != null) {
J.viewer.StateCreator.appendCmd (sfunc, "set refreshing true");
J.viewer.StateCreator.appendCmd (sfunc, "set antialiasDisplay " + global.antialiasDisplay);
J.viewer.StateCreator.appendCmd (sfunc, "set antialiasTranslucent " + global.antialiasTranslucent);
J.viewer.StateCreator.appendCmd (sfunc, "set antialiasImages " + global.antialiasImages);
if (this.viewer.getSpinOn ()) J.viewer.StateCreator.appendCmd (sfunc, "spin on");
sfunc.append ("}\n\n_setState;\n");
}if (isAll) s.appendSB (sfunc);
return s.toString ();
}, "~S,~N,~N");
$_M(c$, "getDefinedState", 
($fz = function (sfunc, isAll) {
var ms = this.viewer.modelSet;
var len = ms.stateScripts.size ();
if (len == 0) return "";
var haveDefs = false;
var commands =  new J.util.SB ();
var cmd;
for (var i = 0; i < len; i++) {
var ss = ms.stateScripts.get (i);
if (ss.inDefinedStateBlock && (cmd = ss.toString ()).length > 0) {
commands.append ("  ").append (cmd).append ("\n");
haveDefs = true;
}}
if (!haveDefs) return "";
cmd = "";
if (isAll && sfunc != null) {
sfunc.append ("  _setDefinedState;\n");
cmd = "function _setDefinedState() {\n\n";
}if (sfunc != null) commands.append ("\n}\n\n");
return cmd + commands.toString ();
}, $fz.isPrivate = true, $fz), "J.util.SB,~B");
Clazz.overrideMethod (c$, "getModelState", 
function (sfunc, isAll, withProteinStructure) {
var commands =  new J.util.SB ();
if (isAll && sfunc != null) {
sfunc.append ("  _setModelState;\n");
commands.append ("function _setModelState() {\n");
}var cmd;
var ms = this.viewer.modelSet;
var bonds = ms.bonds;
var models = ms.models;
var modelCount = ms.modelCount;
if (isAll) {
var len = ms.stateScripts.size ();
for (var i = 0; i < len; i++) {
var ss = ms.stateScripts.get (i);
if (!ss.inDefinedStateBlock && (cmd = ss.toString ()).length > 0) {
commands.append ("  ").append (cmd).append ("\n");
}}
var sb =  new J.util.SB ();
for (var i = 0; i < ms.bondCount; i++) if (!models[bonds[i].atom1.modelIndex].isModelKit) if (bonds[i].isHydrogen () || (bonds[i].order & 131072) != 0) {
var bond = bonds[i];
var index = bond.atom1.index;
if (bond.atom1.getGroup ().isAdded (index)) index = -1 - index;
sb.appendI (index).appendC ('\t').appendI (bond.atom2.index).appendC ('\t').appendI (bond.order & -131073).appendC ('\t').appendF (bond.mad / 1000).appendC ('\t').appendF (bond.getEnergy ()).appendC ('\t').append (J.util.JmolEdge.getBondOrderNameFromOrder (bond.order)).append (";\n");
}
if (sb.length () > 0) commands.append ("data \"connect_atoms\"\n").appendSB (sb).append ("end \"connect_atoms\";\n");
commands.append ("\n");
}if (ms.haveHiddenBonds) {
var bs =  new J.modelset.Bond.BondSet ();
for (var i = ms.bondCount; --i >= 0; ) if (bonds[i].mad != 0 && (bonds[i].shapeVisibilityFlags & J.modelset.Bond.myVisibilityFlag) == 0) bs.set (i);

if (bs.isEmpty ()) ms.haveHiddenBonds = false;
 else commands.append ("  hide ").append (J.util.Escape.eBond (bs)).append (";\n");
}this.viewer.setModelVisibility ();
if (withProteinStructure) commands.append (ms.getProteinStructureState (null, isAll, false, 0));
this.getShapeState (commands, isAll, 2147483647);
if (isAll) {
var needOrientations = false;
for (var i = 0; i < modelCount; i++) if (models[i].isJmolDataFrame) {
needOrientations = true;
break;
}
for (var i = 0; i < modelCount; i++) {
var fcmd = "  frame " + ms.getModelNumberDotted (i);
var s = ms.getModelAuxiliaryInfoValue (i, "modelID");
if (s != null && !s.equals (ms.getModelAuxiliaryInfoValue (i, "modelID0"))) commands.append (fcmd).append ("; frame ID ").append (J.util.Escape.eS (s)).append (";\n");
var t = ms.frameTitles[i];
if (t != null && t.length > 0) commands.append (fcmd).append ("; frame title ").append (J.util.Escape.eS (t)).append (";\n");
if (needOrientations && models[i].orientation != null && !ms.isTrajectorySubFrame (i)) commands.append (fcmd).append ("; ").append (models[i].orientation.getMoveToText (false)).append (";\n");
if (models[i].frameDelay != 0 && !ms.isTrajectorySubFrame (i)) commands.append (fcmd).append ("; frame delay ").appendF (models[i].frameDelay / 1000).append (";\n");
if (models[i].simpleCage != null) {
commands.append (fcmd).append ("; unitcell ").append (J.util.Escape.eAP (models[i].simpleCage.getUnitCellVectors ())).append (";\n");
this.getShapeState (commands, isAll, 33);
}}
if (ms.unitCells != null) {
var haveModulation = false;
for (var i = 0; i < modelCount; i++) {
var symmetry = ms.getUnitCell (i);
if (symmetry == null) continue;
commands.append ("  frame ").append (ms.getModelNumberDotted (i));
var pt = symmetry.getFractionalOffset ();
if (pt != null) commands.append ("; set unitcell ").append (J.util.Escape.eP (pt));
pt = symmetry.getUnitCellMultiplier ();
if (pt != null) commands.append ("; set unitcell ").append (J.util.Escape.eP (pt));
commands.append (";\n");
haveModulation = new Boolean (haveModulation | (this.viewer.modelGetLastVibrationIndex (i, 135270424) >= 0)).valueOf ();
}
this.getShapeState (commands, isAll, 33);
if (haveModulation) {
var temp =  new java.util.Hashtable ();
var ivib;
for (var i = modelCount; --i >= 0; ) {
if ((ivib = this.viewer.modelGetLastVibrationIndex (i, 135270424)) >= 0) for (var j = models[i].firstAtomIndex; j <= ivib; j++) {
var mset = this.viewer.getVibration (j);
if (mset != null && mset.enabled) {
J.util.BSUtil.setMapBitSet (temp, j, j, mset.getState ());
}}
}
var s = this.getCommands (temp, null, "select");
commands.append (s);
}}commands.append ("  set fontScaling " + this.viewer.getBoolean (603979845) + ";\n");
if (this.viewer.getBoolean (603979883)) commands.append ("  set modelKitMode true;\n");
}if (sfunc != null) commands.append ("\n}\n\n");
return commands.toString ();
}, "J.util.SB,~B,~B");
$_M(c$, "getShapeState", 
($fz = function (commands, isAll, iShape) {
var shapes = this.viewer.shapeManager.shapes;
if (shapes == null) return;
var cmd;
var shape;
var i;
var imax;
if (iShape == 2147483647) {
i = 0;
imax = 36;
} else {
imax = (i = iShape) + 1;
}for (; i < imax; ++i) if ((shape = shapes[i]) != null && (isAll || J.viewer.JC.isShapeSecondary (i)) && (cmd = shape.getShapeState ()) != null && cmd.length > 1) commands.append (cmd);

commands.append ("  select *;\n");
}, $fz.isPrivate = true, $fz), "J.util.SB,~B,~N");
$_M(c$, "getWindowState", 
($fz = function (sfunc, width, height) {
var global = this.viewer.global;
var str =  new J.util.SB ();
if (sfunc != null) {
sfunc.append ("  initialize;\n  set refreshing false;\n  _setWindowState;\n");
str.append ("\nfunction _setWindowState() {\n");
}if (width != 0) str.append ("# preferredWidthHeight ").appendI (width).append (" ").appendI (height).append (";\n");
str.append ("# width ").appendI (width == 0 ? this.viewer.getScreenWidth () : width).append (";\n# height ").appendI (height == 0 ? this.viewer.getScreenHeight () : height).append (";\n");
J.viewer.StateCreator.appendCmd (str, "stateVersion = " + global.getParameter ("_version"));
J.viewer.StateCreator.appendCmd (str, "background " + J.util.Escape.escapeColor (global.objColors[0]));
for (var i = 1; i < 8; i++) if (global.objColors[i] != 0) J.viewer.StateCreator.appendCmd (str, J.viewer.StateManager.getObjectNameFromId (i) + "Color = \"" + J.util.Escape.escapeColor (global.objColors[i]) + '"');

if (global.backgroundImageFileName != null) J.viewer.StateCreator.appendCmd (str, "background IMAGE /*file*/" + J.util.Escape.eS (global.backgroundImageFileName));
str.append (this.getSpecularState ());
J.viewer.StateCreator.appendCmd (str, "statusReporting  = " + global.statusReporting);
if (sfunc != null) str.append ("}\n\n");
return str.toString ();
}, $fz.isPrivate = true, $fz), "J.util.SB,~N,~N");
Clazz.overrideMethod (c$, "getSpecularState", 
function () {
var str =  new J.util.SB ();
var g = this.viewer.gdata;
J.viewer.StateCreator.appendCmd (str, "set ambientPercent " + g.getAmbientPercent ());
J.viewer.StateCreator.appendCmd (str, "set diffusePercent " + g.getDiffusePercent ());
J.viewer.StateCreator.appendCmd (str, "set specular " + g.getSpecular ());
J.viewer.StateCreator.appendCmd (str, "set specularPercent " + g.getSpecularPercent ());
J.viewer.StateCreator.appendCmd (str, "set specularPower " + g.getSpecularPower ());
J.viewer.StateCreator.appendCmd (str, "set celShading " + g.getCel ());
var se = g.getSpecularExponent ();
var pe = g.getPhongExponent ();
if (Math.pow (2, se) == pe) J.viewer.StateCreator.appendCmd (str, "set specularExponent " + se);
 else J.viewer.StateCreator.appendCmd (str, "set phongExponent " + pe);
J.viewer.StateCreator.appendCmd (str, "set zShadePower " + this.viewer.global.zShadePower);
return str.toString ();
});
$_M(c$, "getFileState", 
($fz = function (sfunc) {
var commands =  new J.util.SB ();
if (sfunc != null) {
sfunc.append ("  _setFileState;\n");
commands.append ("function _setFileState() {\n\n");
}if (commands.indexOf ("append") < 0 && this.viewer.getModelSetFileName ().equals ("zapped")) commands.append ("  zap;\n");
this.appendLoadStates (commands);
if (sfunc != null) commands.append ("\n}\n\n");
return commands.toString ();
}, $fz.isPrivate = true, $fz), "J.util.SB");
$_M(c$, "getDataState", 
($fz = function (dm, state, sfunc, atomProps) {
if (dm.dataValues == null) return;
var sb =  new J.util.SB ();
var haveData = false;
if (atomProps.length > 0) {
haveData = true;
sb.append (atomProps);
}for (var name, $name = dm.dataValues.keySet ().iterator (); $name.hasNext () && ((name = $name.next ()) || true);) {
if (name.indexOf ("property_") == 0) {
var obj = dm.dataValues.get (name);
if (obj.length > 4 && obj[4] === Boolean.FALSE) continue;
haveData = true;
var data = obj[1];
if (data != null && (obj[3]).intValue () == 1) {
this.getAtomicPropertyStateBuffer (sb, 14, obj[2], name, data);
sb.append ("\n");
} else {
sb.append ("\n").append (J.util.Escape.encapsulateData (name, data, 0));
}} else if (name.indexOf ("data2d") == 0) {
var obj = dm.dataValues.get (name);
var data = obj[1];
if (data != null && (obj[3]).intValue () == 2) {
haveData = true;
sb.append ("\n").append (J.util.Escape.encapsulateData (name, data, 2));
}} else if (name.indexOf ("data3d") == 0) {
var obj = dm.dataValues.get (name);
var data = obj[1];
if (data != null && (obj[3]).intValue () == 3) {
haveData = true;
sb.append ("\n").append (J.util.Escape.encapsulateData (name, data, 3));
}}}
if (dm.userVdws != null) {
var info = dm.getDefaultVdwNameOrData (0, J.constant.EnumVdw.USER, dm.bsUserVdws);
if (info.length > 0) {
haveData = true;
sb.append (info);
}}if (this.viewer.nmrCalculation != null) haveData = new Boolean (haveData | this.viewer.getNMRCalculation ().getState (sb)).valueOf ();
if (!haveData) return;
if (sfunc != null) state.append ("function _setDataState() {\n");
state.appendSB (sb);
if (sfunc != null) {
sfunc.append ("  _setDataState;\n");
state.append ("}\n\n");
}}, $fz.isPrivate = true, $fz), "J.viewer.DataManager,J.util.SB,J.util.SB,~S");
$_M(c$, "getColorState", 
($fz = function (cm, sfunc) {
var s =  new J.util.SB ();
var n = this.getCEState (cm.propertyColorEncoder, s);
if (n > 0 && sfunc != null) sfunc.append ("\n  _setColorState\n");
return (n > 0 && sfunc != null ? "function _setColorState() {\n" + s.append ("}\n\n").toString () : s.toString ());
}, $fz.isPrivate = true, $fz), "J.viewer.ColorManager,J.util.SB");
$_M(c$, "getCEState", 
($fz = function (p, s) {
var n = 0;
for (var entry, $entry = p.schemes.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var name = entry.getKey ();
if ( new Boolean (name.length > 0 & n++ >= 0).valueOf ()) s.append ("color \"" + name + "=" + J.util.ColorEncoder.getColorSchemeList (entry.getValue ()) + "\";\n");
}
return n;
}, $fz.isPrivate = true, $fz), "J.util.ColorEncoder,J.util.SB");
$_M(c$, "getAnimState", 
($fz = function (am, sfunc) {
var modelCount = this.viewer.getModelCount ();
if (modelCount < 2) return "";
var commands =  new J.util.SB ();
if (sfunc != null) {
sfunc.append ("  _setFrameState;\n");
commands.append ("function _setFrameState() {\n");
}commands.append ("# frame state;\n");
commands.append ("# modelCount ").appendI (modelCount).append (";\n# first ").append (this.viewer.getModelNumberDotted (0)).append (";\n# last ").append (this.viewer.getModelNumberDotted (modelCount - 1)).append (";\n");
if (am.backgroundModelIndex >= 0) J.viewer.StateCreator.appendCmd (commands, "set backgroundModel " + this.viewer.getModelNumberDotted (am.backgroundModelIndex));
var bs = this.viewer.getFrameOffsets ();
if (bs != null) J.viewer.StateCreator.appendCmd (commands, "frame align " + J.util.Escape.eBS (bs));
J.viewer.StateCreator.appendCmd (commands, "frame RANGE " + am.getModelSpecial (-1) + " " + am.getModelSpecial (1));
J.viewer.StateCreator.appendCmd (commands, "animation DIRECTION " + (am.animationDirection == 1 ? "+1" : "-1"));
J.viewer.StateCreator.appendCmd (commands, "animation FPS " + am.animationFps);
J.viewer.StateCreator.appendCmd (commands, "animation MODE " + am.animationReplayMode.name () + " " + am.firstFrameDelay + " " + am.lastFrameDelay);
if (am.morphCount > 0) J.viewer.StateCreator.appendCmd (commands, "animation MORPH " + am.morphCount);
var frames = am.getAnimationFrames ();
var showModel = true;
if (frames != null) {
J.viewer.StateCreator.appendCmd (commands, "anim frames " + J.util.Escape.eAI (frames));
var i = am.getCurrentFrameIndex ();
J.viewer.StateCreator.appendCmd (commands, "frame " + (i + 1));
showModel = (am.getCurrentModelIndex () != am.modelIndexForFrame (i));
}if (showModel) J.viewer.StateCreator.appendCmd (commands, "model " + am.getModelSpecial (0));
J.viewer.StateCreator.appendCmd (commands, "animation " + (!am.animationOn ? "OFF" : am.currentDirection == 1 ? "PLAY" : "PLAYREV"));
if (am.animationOn && am.animationPaused) J.viewer.StateCreator.appendCmd (commands, "animation PAUSE");
if (sfunc != null) commands.append ("}\n\n");
return commands.toString ();
}, $fz.isPrivate = true, $fz), "J.viewer.AnimationManager,J.util.SB");
$_M(c$, "getVariableState", 
($fz = function (global, sfunc) {
var list =  new Array (global.htBooleanParameterFlags.size () + global.htNonbooleanParameterValues.size ());
var commands =  new J.util.SB ();
var isState = (sfunc != null);
if (isState) {
sfunc.append ("  _setVariableState;\n");
commands.append ("function _setVariableState() {\n\n");
}var n = 0;
for (var key, $key = global.htBooleanParameterFlags.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) if (J.viewer.StateManager.doReportProperty (key)) list[n++] = "set " + key + " " + global.htBooleanParameterFlags.get (key);

for (var key, $key = global.htNonbooleanParameterValues.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) if (J.viewer.StateManager.doReportProperty (key)) {
var value = global.htNonbooleanParameterValues.get (key);
if (key.charAt (0) == '=') {
key = key.substring (1);
} else {
if (key.indexOf ("default") == 0) key = " set " + key;
 else key = "set " + key;
value = J.util.Escape.e (value);
}list[n++] = key + " " + value;
}
switch (global.axesMode) {
case J.constant.EnumAxesMode.UNITCELL:
list[n++] = "set axes unitcell";
break;
case J.constant.EnumAxesMode.BOUNDBOX:
list[n++] = "set axes window";
break;
default:
list[n++] = "set axes molecular";
}
java.util.Arrays.sort (list, 0, n);
for (var i = 0; i < n; i++) if (list[i] != null) J.viewer.StateCreator.appendCmd (commands, list[i]);

var s = J.viewer.StateManager.getVariableList (global.htUserVariables, 0, false, true);
if (s.length > 0) {
commands.append ("\n#user-defined atom sets; \n");
commands.append (s);
}if (this.viewer.shapeManager.getShape (5) != null) commands.append (this.getDefaultLabelState (this.viewer.shapeManager.shapes[5]));
if (global.haveSetStructureList) {
var slist = global.structureList;
commands.append ("struture HELIX set " + J.util.Escape.eAF (slist.get (J.constant.EnumStructure.HELIX)));
commands.append ("struture SHEET set " + J.util.Escape.eAF (slist.get (J.constant.EnumStructure.SHEET)));
commands.append ("struture TURN set " + J.util.Escape.eAF (slist.get (J.constant.EnumStructure.TURN)));
}if (sfunc != null) commands.append ("\n}\n\n");
return commands.toString ();
}, $fz.isPrivate = true, $fz), "J.viewer.StateManager.GlobalSettings,J.util.SB");
$_M(c$, "getDefaultLabelState", 
($fz = function (l) {
var s =  new J.util.SB ().append ("\n# label defaults;\n");
J.viewer.StateCreator.appendCmd (s, "select none");
J.viewer.StateCreator.appendCmd (s, J.shape.Shape.getColorCommand ("label", l.defaultPaletteID, l.defaultColix, l.translucentAllowed));
J.viewer.StateCreator.appendCmd (s, "background label " + J.shape.Shape.encodeColor (l.defaultBgcolix));
J.viewer.StateCreator.appendCmd (s, "set labelOffset " + J.modelset.Object2d.getXOffset (l.defaultOffset) + " " + (-J.modelset.Object2d.getYOffset (l.defaultOffset)));
var align = J.modelset.Object2d.getAlignmentName (l.defaultAlignment);
J.viewer.StateCreator.appendCmd (s, "set labelAlignment " + (align.length < 5 ? "left" : align));
var pointer = J.modelset.Object2d.getPointer (l.defaultPointer);
J.viewer.StateCreator.appendCmd (s, "set labelPointer " + (pointer.length == 0 ? "off" : pointer));
if ((l.defaultZPos & 32) != 0) J.viewer.StateCreator.appendCmd (s, "set labelFront");
 else if ((l.defaultZPos & 16) != 0) J.viewer.StateCreator.appendCmd (s, "set labelGroup");
J.viewer.StateCreator.appendCmd (s, J.shape.Shape.getFontCommand ("label", J.util.JmolFont.getFont3D (l.defaultFontId)));
return s.toString ();
}, $fz.isPrivate = true, $fz), "J.shape.Labels");
$_M(c$, "getSelectionState", 
($fz = function (sm, sfunc) {
var commands =  new J.util.SB ();
if (sfunc != null) {
sfunc.append ("  _setSelectionState;\n");
commands.append ("function _setSelectionState() {\n");
}J.viewer.StateCreator.appendCmd (commands, this.getTrajectoryState ());
var temp =  new java.util.Hashtable ();
var cmd = null;
J.viewer.StateCreator.addBs (commands, "hide ", sm.bsHidden);
J.viewer.StateCreator.addBs (commands, "subset ", sm.bsSubset);
J.viewer.StateCreator.addBs (commands, "delete ", sm.bsDeleted);
J.viewer.StateCreator.addBs (commands, "fix ", sm.bsFixed);
temp.put ("-", sm.bsSelection);
cmd = this.getCommands (temp, null, "select");
if (cmd == null) J.viewer.StateCreator.appendCmd (commands, "select none");
 else commands.append (cmd);
J.viewer.StateCreator.appendCmd (commands, "set hideNotSelected " + sm.hideNotSelected);
commands.append (this.viewer.getShapeProperty (1, "selectionState"));
if (this.viewer.getSelectionHaloEnabled (false)) J.viewer.StateCreator.appendCmd (commands, "SelectionHalos ON");
if (sfunc != null) commands.append ("}\n\n");
return commands.toString ();
}, $fz.isPrivate = true, $fz), "J.viewer.SelectionManager,J.util.SB");
Clazz.overrideMethod (c$, "getTrajectoryState", 
function () {
var s = "";
var m = this.viewer.modelSet;
if (m.trajectorySteps == null) return "";
for (var i = m.modelCount; --i >= 0; ) {
var t = m.models[i].getSelectedTrajectory ();
if (t >= 0) {
s = " or " + m.getModelNumberDotted (t) + s;
i = m.models[i].trajectoryBaseIndex;
}}
if (s.length > 0) s = "set trajectory {" + s.substring (4) + "}";
return s;
});
$_M(c$, "getViewState", 
($fz = function (tm, sfunc) {
var commands =  new J.util.SB ();
var moveToText = tm.getMoveToText (0, false);
if (sfunc != null) {
sfunc.append ("  _setPerspectiveState;\n");
commands.append ("function _setPerspectiveState() {\n");
}J.viewer.StateCreator.appendCmd (commands, "set perspectiveModel " + tm.perspectiveModel);
J.viewer.StateCreator.appendCmd (commands, "set scaleAngstromsPerInch " + tm.scale3DAngstromsPerInch);
J.viewer.StateCreator.appendCmd (commands, "set perspectiveDepth " + tm.perspectiveDepth);
J.viewer.StateCreator.appendCmd (commands, "set visualRange " + tm.visualRange);
if (!tm.isWindowCentered ()) J.viewer.StateCreator.appendCmd (commands, "set windowCentered false");
J.viewer.StateCreator.appendCmd (commands, "set cameraDepth " + tm.cameraDepth);
var navigating = (tm.mode == 1);
if (navigating) J.viewer.StateCreator.appendCmd (commands, "set navigationMode true");
J.viewer.StateCreator.appendCmd (commands, this.viewer.getBoundBoxCommand (false));
J.viewer.StateCreator.appendCmd (commands, "center " + J.util.Escape.eP (tm.fixedRotationCenter));
commands.append (this.viewer.getOrientationText (1073742035, null));
J.viewer.StateCreator.appendCmd (commands, moveToText);
if (tm.stereoMode !== J.constant.EnumStereoMode.NONE) J.viewer.StateCreator.appendCmd (commands, "stereo " + (tm.stereoColors == null ? tm.stereoMode.getName () : J.util.Escape.escapeColor (tm.stereoColors[0]) + " " + J.util.Escape.escapeColor (tm.stereoColors[1])) + " " + tm.stereoDegrees);
if (!navigating && !tm.zoomEnabled) J.viewer.StateCreator.appendCmd (commands, "zoom off");
commands.append ("  slab ").appendI (tm.slabPercentSetting).append (";depth ").appendI (tm.depthPercentSetting).append (tm.slabEnabled && !navigating ? ";slab on" : "").append (";\n");
commands.append ("  set slabRange ").appendF (tm.slabRange).append (";\n");
if (tm.zShadeEnabled) commands.append ("  set zShade;\n");
try {
if (tm.zSlabPoint != null) commands.append ("  set zSlab ").append (J.util.Escape.eP (tm.zSlabPoint)).append (";\n");
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
if (tm.slabPlane != null) commands.append ("  slab plane ").append (J.util.Escape.eP4 (tm.slabPlane)).append (";\n");
if (tm.depthPlane != null) commands.append ("  depth plane ").append (J.util.Escape.eP4 (tm.depthPlane)).append (";\n");
commands.append (this.getSpinState (true)).append ("\n");
if (this.viewer.modelSetHasVibrationVectors () && tm.vibrationOn) J.viewer.StateCreator.appendCmd (commands, "set vibrationPeriod " + tm.vibrationPeriod + ";vibration on");
if (navigating) {
commands.append (tm.getNavigationState ());
if (tm.depthPlane != null || tm.slabPlane != null) commands.append ("  slab on;\n");
}if (sfunc != null) commands.append ("}\n\n");
return commands.toString ();
}, $fz.isPrivate = true, $fz), "J.viewer.TransformManager,J.util.SB");
Clazz.overrideMethod (c$, "getSpinState", 
function (isAll) {
var tm = this.viewer.transformManager;
var s = "  set spinX " + Clazz.floatToInt (tm.spinX) + "; set spinY " + Clazz.floatToInt (tm.spinY) + "; set spinZ " + Clazz.floatToInt (tm.spinZ) + "; set spinFps " + Clazz.floatToInt (tm.spinFps) + ";";
if (!Float.isNaN (tm.navFps)) s += "  set navX " + Clazz.floatToInt (tm.navX) + "; set navY " + Clazz.floatToInt (tm.navY) + "; set navZ " + Clazz.floatToInt (tm.navZ) + "; set navFps " + Clazz.floatToInt (tm.navFps) + ";";
if (tm.navOn) s += " navigation on;";
if (!tm.spinOn) return s;
var prefix = (tm.isSpinSelected ? "\n  select " + J.util.Escape.eBS (this.viewer.getSelectionSet (false)) + ";\n  rotateSelected" : "\n ");
if (tm.isSpinInternal) {
var pt = J.util.P3.newP (tm.internalRotationCenter);
pt.sub (tm.rotationAxis);
s += prefix + " spin " + tm.rotationRate + " " + J.util.Escape.eP (tm.internalRotationCenter) + " " + J.util.Escape.eP (pt);
} else if (tm.isSpinFixed) {
s += prefix + " spin axisangle " + J.util.Escape.eP (tm.rotationAxis) + " " + tm.rotationRate;
} else {
s += " spin on";
}return s + ";";
}, "~B");
Clazz.overrideMethod (c$, "getInfo", 
function (manager) {
if (Clazz.instanceOf (manager, J.viewer.AnimationManager)) return this.getAnimationInfo (manager);
return null;
}, "~O");
$_M(c$, "getAnimationInfo", 
($fz = function (am) {
var info =  new java.util.Hashtable ();
info.put ("firstModelIndex", Integer.$valueOf (am.firstFrameIndex));
info.put ("lastModelIndex", Integer.$valueOf (am.lastFrameIndex));
info.put ("animationDirection", Integer.$valueOf (am.animationDirection));
info.put ("currentDirection", Integer.$valueOf (am.currentDirection));
info.put ("displayModelIndex", Integer.$valueOf (am.currentModelIndex));
if (am.animationFrames != null) {
info.put ("isMovie", Boolean.TRUE);
info.put ("frames", J.util.Escape.eAI (am.animationFrames));
info.put ("currentAnimationFrame", Integer.$valueOf (am.currentAnimationFrame));
}info.put ("displayModelNumber", this.viewer.getModelNumberDotted (am.currentModelIndex));
info.put ("displayModelName", (am.currentModelIndex >= 0 ? this.viewer.getModelName (am.currentModelIndex) : ""));
info.put ("animationFps", Integer.$valueOf (am.animationFps));
info.put ("animationReplayMode", am.animationReplayMode.name ());
info.put ("firstFrameDelay", Float.$valueOf (am.firstFrameDelay));
info.put ("lastFrameDelay", Float.$valueOf (am.lastFrameDelay));
info.put ("animationOn", Boolean.$valueOf (am.animationOn));
info.put ("animationPaused", Boolean.$valueOf (am.animationPaused));
return info;
}, $fz.isPrivate = true, $fz), "J.viewer.AnimationManager");
Clazz.overrideMethod (c$, "getCommands", 
function (htDefine, htMore, selectCmd) {
var s =  new J.util.SB ();
var setPrev = J.viewer.StateCreator.getCommands2 (htDefine, s, null, selectCmd);
if (htMore != null) J.viewer.StateCreator.getCommands2 (htMore, s, setPrev, "select");
return s.toString ();
}, "java.util.Map,java.util.Map,~S");
c$.getCommands2 = $_M(c$, "getCommands2", 
($fz = function (ht, s, setPrev, selectCmd) {
if (ht == null) return "";
for (var entry, $entry = ht.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var key = entry.getKey ();
var set = J.util.Escape.eBS (entry.getValue ());
if (set.length < 5) continue;
set = selectCmd + " " + set;
if (!set.equals (setPrev)) J.viewer.StateCreator.appendCmd (s, set);
setPrev = set;
if (key.indexOf ("-") != 0) J.viewer.StateCreator.appendCmd (s, key);
}
return setPrev;
}, $fz.isPrivate = true, $fz), "java.util.Map,J.util.SB,~S,~S");
c$.appendCmd = $_M(c$, "appendCmd", 
($fz = function (s, cmd) {
if (cmd.length == 0) return;
s.append ("  ").append (cmd).append (";\n");
}, $fz.isPrivate = true, $fz), "J.util.SB,~S");
c$.addBs = $_M(c$, "addBs", 
($fz = function (sb, key, bs) {
if (bs == null || bs.length () == 0) return;
J.viewer.StateCreator.appendCmd (sb, key + J.util.Escape.eBS (bs));
}, $fz.isPrivate = true, $fz), "J.util.SB,~S,J.util.BS");
Clazz.overrideMethod (c$, "getFontState", 
function (myType, font3d) {
var objId = J.viewer.StateManager.getObjectIdFromName (myType.equalsIgnoreCase ("axes") ? "axis" : myType);
if (objId < 0) return "";
var mad = this.viewer.getObjectMad (objId);
var s =  new J.util.SB ().append ("\n");
J.viewer.StateCreator.appendCmd (s, myType + (mad == 0 ? " off" : mad == 1 ? " on" : mad == -1 ? " dotted" : mad < 20 ? " " + mad : " " + (mad / 2000)));
if (s.length () < 3) return "";
var fcmd = J.shape.Shape.getFontCommand (myType, font3d);
if (fcmd.length > 0) fcmd = "  " + fcmd + ";\n";
return (s + fcmd);
}, "~S,J.util.JmolFont");
Clazz.overrideMethod (c$, "getFontLineShapeState", 
function (s, myType, tickInfos) {
var isOff = (s.indexOf (" off") >= 0);
var sb =  new J.util.SB ();
sb.append (s);
for (var i = 0; i < 4; i++) if (tickInfos[i] != null) this.appendTickInfo (myType, sb, tickInfos[i]);

if (isOff) sb.append ("  " + myType + " off;\n");
return sb.toString ();
}, "~S,~S,~A");
$_M(c$, "appendTickInfo", 
($fz = function (myType, sb, t) {
sb.append ("  ");
sb.append (myType);
J.viewer.StateCreator.addTickInfo (sb, t, false);
sb.append (";\n");
}, $fz.isPrivate = true, $fz), "~S,J.util.SB,J.modelset.TickInfo");
c$.addTickInfo = $_M(c$, "addTickInfo", 
($fz = function (sb, tickInfo, addFirst) {
sb.append (" ticks ").append (tickInfo.type).append (" ").append (J.util.Escape.eP (tickInfo.ticks));
var isUnitCell = (tickInfo.scale != null && Float.isNaN (tickInfo.scale.x));
if (isUnitCell) sb.append (" UNITCELL");
if (tickInfo.tickLabelFormats != null) sb.append (" format ").append (J.util.Escape.eAS (tickInfo.tickLabelFormats, false));
if (!isUnitCell && tickInfo.scale != null) sb.append (" scale ").append (J.util.Escape.eP (tickInfo.scale));
if (addFirst && !Float.isNaN (tickInfo.first) && tickInfo.first != 0) sb.append (" first ").appendF (tickInfo.first);
if (tickInfo.reference != null) sb.append (" point ").append (J.util.Escape.eP (tickInfo.reference));
}, $fz.isPrivate = true, $fz), "J.util.SB,J.modelset.TickInfo,~B");
Clazz.overrideMethod (c$, "getShapeSetState", 
function (as, shape, monomerCount, monomers, bsSizeDefault, temp, temp2) {
var type = J.viewer.JC.shapeClassBases[shape.shapeID];
for (var i = 0; i < monomerCount; i++) {
var atomIndex1 = monomers[i].firstAtomIndex;
var atomIndex2 = monomers[i].lastAtomIndex;
if (as.bsSizeSet != null && (as.bsSizeSet.get (i) || as.bsColixSet != null && as.bsColixSet.get (i))) {
if (bsSizeDefault.get (i)) J.util.BSUtil.setMapBitSet (temp, atomIndex1, atomIndex2, type + (as.bsSizeSet.get (i) ? " on" : " off"));
 else J.util.BSUtil.setMapBitSet (temp, atomIndex1, atomIndex2, type + " " + (as.mads[i] / 2000));
}if (as.bsColixSet != null && as.bsColixSet.get (i)) J.util.BSUtil.setMapBitSet (temp2, atomIndex1, atomIndex2, J.shape.Shape.getColorCommand (type, as.paletteIDs[i], as.colixes[i], shape.translucentAllowed));
}
}, "J.shape.AtomShape,J.shape.Shape,~N,~A,J.util.BS,java.util.Map,java.util.Map");
Clazz.overrideMethod (c$, "getMeasurementState", 
function (shape, mList, measurementCount, font3d, ti) {
var commands =  new J.util.SB ();
J.viewer.StateCreator.appendCmd (commands, "measures delete");
for (var i = 0; i < measurementCount; i++) {
var m = mList.get (i);
var count = m.getCount ();
var sb =  new J.util.SB ().append ("measure");
if (m.thisID != null) sb.append (" ID ").append (J.util.Escape.eS (m.thisID));
if (m.mad != 0) sb.append (" radius ").appendF (m.thisID == null || m.mad > 0 ? m.mad / 2000 : 0);
if (m.colix != 0) sb.append (" color ").append (J.util.Escape.escapeColor (J.util.C.getArgb (m.colix)));
if (m.text != null) {
sb.append (" font ").append (m.text.font.getInfo ());
if (m.text.pymolOffset != null) sb.append (" offset ").append (J.util.Escape.eAF (m.text.pymolOffset));
}var tickInfo = m.tickInfo;
if (tickInfo != null) J.viewer.StateCreator.addTickInfo (sb, tickInfo, true);
for (var j = 1; j <= count; j++) sb.append (" ").append (m.getLabel (j, true, true));

sb.append ("; # " + shape.getInfoAsString (i));
J.viewer.StateCreator.appendCmd (commands, sb.toString ());
}
J.viewer.StateCreator.appendCmd (commands, "select *; set measures " + this.viewer.getMeasureDistanceUnits ());
J.viewer.StateCreator.appendCmd (commands, J.shape.Shape.getFontCommand ("measures", font3d));
var nHidden = 0;
var temp =  new java.util.Hashtable ();
var bs = J.util.BSUtil.newBitSet (measurementCount);
for (var i = 0; i < measurementCount; i++) {
var m = mList.get (i);
if (m.isHidden) {
nHidden++;
bs.set (i);
}if (shape.bsColixSet != null && shape.bsColixSet.get (i)) J.util.BSUtil.setMapBitSet (temp, i, i, J.shape.Shape.getColorCommandUnk ("measure", m.colix, shape.translucentAllowed));
if (m.getStrFormat () != null) J.util.BSUtil.setMapBitSet (temp, i, i, "measure " + J.util.Escape.eS (m.getStrFormat ()));
}
if (nHidden > 0) if (nHidden == measurementCount) J.viewer.StateCreator.appendCmd (commands, "measures off; # lines and numbers off");
 else for (var i = 0; i < measurementCount; i++) if (bs.get (i)) J.util.BSUtil.setMapBitSet (temp, i, i, "measure off");

if (ti != null) {
commands.append (" measure ");
J.viewer.StateCreator.addTickInfo (commands, ti, true);
commands.append (";\n");
}if (shape.mad >= 0) commands.append (" set measurements " + (shape.mad / 2000)).append (";\n");
var s = this.getCommands (temp, null, "select measures");
if (s != null && s.length != 0) {
commands.append (s);
J.viewer.StateCreator.appendCmd (commands, "select measures ({null})");
}return commands.toString ();
}, "J.shape.Measures,J.util.JmolList,~N,J.util.JmolFont,J.modelset.TickInfo");
Clazz.overrideMethod (c$, "getBondState", 
function (shape, bsOrderSet, reportAll) {
this.clearTemp ();
var modelSet = this.viewer.modelSet;
var haveTainted = false;
var bonds = modelSet.bonds;
var bondCount = modelSet.bondCount;
var r;
if (reportAll || shape.bsSizeSet != null) {
var i0 = (reportAll ? bondCount - 1 : shape.bsSizeSet.nextSetBit (0));
for (var i = i0; i >= 0; i = (reportAll ? i - 1 : shape.bsSizeSet.nextSetBit (i + 1))) J.util.BSUtil.setMapBitSet (this.temp, i, i, "wireframe " + ((r = bonds[i].mad) == 1 ? "on" : "" + (r / 2000)));

}if (reportAll || bsOrderSet != null) {
var i0 = (reportAll ? bondCount - 1 : bsOrderSet.nextSetBit (0));
for (var i = i0; i >= 0; i = (reportAll ? i - 1 : bsOrderSet.nextSetBit (i + 1))) {
var bond = bonds[i];
if (reportAll || (bond.order & 131072) == 0) J.util.BSUtil.setMapBitSet (this.temp, i, i, "bondOrder " + J.util.JmolEdge.getBondOrderNameFromOrder (bond.order));
}
}if (shape.bsColixSet != null) for (var i = shape.bsColixSet.nextSetBit (0); i >= 0; i = shape.bsColixSet.nextSetBit (i + 1)) {
var colix = bonds[i].colix;
if ((colix & -30721) == 2) J.util.BSUtil.setMapBitSet (this.temp, i, i, J.shape.Shape.getColorCommand ("bonds", J.constant.EnumPalette.CPK.id, colix, shape.translucentAllowed));
 else J.util.BSUtil.setMapBitSet (this.temp, i, i, J.shape.Shape.getColorCommandUnk ("bonds", colix, shape.translucentAllowed));
}
var s = this.getCommands (this.temp, null, "select BONDS") + "\n" + (haveTainted ? this.getCommands (this.temp2, null, "select BONDS") + "\n" : "");
this.clearTemp ();
return s;
}, "J.shape.Shape,J.util.BS,~B");
$_M(c$, "clearTemp", 
($fz = function () {
this.temp.clear ();
this.temp2.clear ();
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "getAtomShapeSetState", 
function (shape, bioShapes) {
this.clearTemp ();
for (var i = bioShapes.length; --i >= 0; ) {
var bs = bioShapes[i];
if (bs.monomerCount > 0) {
if (!bs.isActive || bs.bsSizeSet == null && bs.bsColixSet == null) continue;
this.viewer.getShapeSetState (bs, shape, bs.monomerCount, bs.getMonomers (), bs.bsSizeDefault, this.temp, this.temp2);
}}
var s = "\n" + this.getCommands (this.temp, this.temp2, shape.shapeID == 9 ? "Backbone" : "select");
this.clearTemp ();
return s;
}, "J.shape.Shape,~A");
$_M(c$, "getShapeState", 
function (shape) {
var s;
switch (shape.shapeID) {
case 30:
var es = shape;
var sb =  new J.util.SB ();
sb.append ("\n  set echo off;\n");
for (var t, $t = es.objects.values ().iterator (); $t.hasNext () && ((t = $t.next ()) || true);) {
sb.append (this.getTextState (t));
if (t.hidden) sb.append ("  set echo ID ").append (J.util.Escape.eS (t.target)).append (" hidden;\n");
}
s = sb.toString ();
break;
case 8:
var hs = shape;
s = this.getAtomShapeState (hs) + (hs.colixSelection == 2 ? "" : hs.colixSelection == 0 ? "  color SelectionHalos NONE;\n" : J.shape.Shape.getColorCommandUnk ("selectionHalos", hs.colixSelection, hs.translucentAllowed) + ";\n");
if (hs.bsHighlight != null) s += "  set highlight " + J.util.Escape.eBS (hs.bsHighlight) + "; " + J.shape.Shape.getColorCommandUnk ("highlight", hs.colixHighlight, hs.translucentAllowed) + ";\n";
break;
case 34:
this.clearTemp ();
var h = shape;
if (h.atomFormats != null) for (var i = this.viewer.getAtomCount (); --i >= 0; ) if (h.atomFormats[i] != null) J.util.BSUtil.setMapBitSet (this.temp, i, i, "set hoverLabel " + J.util.Escape.eS (h.atomFormats[i]));

s = "\n  hover " + J.util.Escape.eS ((h.labelFormat == null ? "" : h.labelFormat)) + ";\n" + this.getCommands (this.temp, null, "select");
this.clearTemp ();
break;
case 5:
this.clearTemp ();
var l = shape;
for (var i = l.bsSizeSet.nextSetBit (0); i >= 0; i = l.bsSizeSet.nextSetBit (i + 1)) {
var t = l.getLabel (i);
var cmd = null;
if (t != null) {
cmd = "label " + J.util.Escape.eS (t.textUnformatted);
if (t.pymolOffset != null) cmd += ";set labelOffset " + J.util.Escape.eAF (t.pymolOffset);
}if (cmd == null) cmd = "label " + J.util.Escape.eS (l.formats[i]);
J.util.BSUtil.setMapBitSet (this.temp, i, i, cmd);
if (l.bsColixSet != null && l.bsColixSet.get (i)) J.util.BSUtil.setMapBitSet (this.temp2, i, i, J.shape.Shape.getColorCommand ("label", l.paletteIDs[i], l.colixes[i], l.translucentAllowed));
if (l.bsBgColixSet != null && l.bsBgColixSet.get (i)) J.util.BSUtil.setMapBitSet (this.temp2, i, i, "background label " + J.shape.Shape.encodeColor (l.bgcolixes[i]));
var text = l.getLabel (i);
var sppm = (text != null ? text.getScalePixelsPerMicron () : 0);
if (sppm > 0) J.util.BSUtil.setMapBitSet (this.temp2, i, i, "set labelScaleReference " + (10000 / sppm));
if (l.offsets != null && l.offsets.length > i) {
var offsetFull = l.offsets[i];
J.util.BSUtil.setMapBitSet (this.temp2, i, i, "set " + ((offsetFull & 128) == 128 ? "labelOffsetExact " : "labelOffset ") + J.modelset.Object2d.getXOffset (offsetFull >> 8) + " " + (-J.modelset.Object2d.getYOffset (offsetFull >> 8)));
var align = J.modelset.Object2d.getAlignmentName (offsetFull >> 2);
var pointer = J.modelset.Object2d.getPointer (offsetFull);
if (pointer.length > 0) J.util.BSUtil.setMapBitSet (this.temp2, i, i, "set labelPointer " + pointer);
if ((offsetFull & 32) != 0) J.util.BSUtil.setMapBitSet (this.temp2, i, i, "set labelFront");
 else if ((offsetFull & 16) != 0) J.util.BSUtil.setMapBitSet (this.temp2, i, i, "set labelGroup");
if (align.length > 0) J.util.BSUtil.setMapBitSet (this.temp3, i, i, "set labelAlignment " + align);
}if (l.mads != null && l.mads[i] < 0) J.util.BSUtil.setMapBitSet (this.temp2, i, i, "set toggleLabel");
if (l.bsFontSet != null && l.bsFontSet.get (i)) J.util.BSUtil.setMapBitSet (this.temp2, i, i, J.shape.Shape.getFontCommand ("label", J.util.JmolFont.getFont3D (l.fids[i])));
}
s = this.getCommands (this.temp, this.temp2, "select") + this.getCommands (null, this.temp3, "select");
this.temp3.clear ();
this.clearTemp ();
break;
case 0:
this.clearTemp ();
var atomCount = this.viewer.getAtomCount ();
var atoms = this.viewer.modelSet.atoms;
var balls = shape;
var colixes = balls.colixes;
var pids = balls.paletteIDs;
var r = 0;
for (var i = 0; i < atomCount; i++) {
if (shape.bsSizeSet != null && shape.bsSizeSet.get (i)) {
if ((r = atoms[i].madAtom) < 0) J.util.BSUtil.setMapBitSet (this.temp, i, i, "Spacefill on");
 else J.util.BSUtil.setMapBitSet (this.temp, i, i, "Spacefill " + (r / 2000));
}if (shape.bsColixSet != null && shape.bsColixSet.get (i)) {
var pid = atoms[i].getPaletteID ();
if (pid != J.constant.EnumPalette.CPK.id || atoms[i].isTranslucent ()) J.util.BSUtil.setMapBitSet (this.temp, i, i, J.shape.Shape.getColorCommand ("atoms", pid, atoms[i].getColix (), shape.translucentAllowed));
if (colixes != null && i < colixes.length) J.util.BSUtil.setMapBitSet (this.temp2, i, i, J.shape.Shape.getColorCommand ("balls", pids[i], colixes[i], shape.translucentAllowed));
}}
s = this.getCommands (this.temp, this.temp2, "select");
this.clearTemp ();
break;
default:
s = "";
}
return s;
}, "J.shape.Shape");
$_M(c$, "getTextState", 
($fz = function (t) {
var s =  new J.util.SB ();
var text = t.getText ();
if (text == null || t.isLabelOrHover || t.target.equals ("error")) return "";
var isImage = (t.image != null);
var strOff = null;
var echoCmd = "set echo ID " + J.util.Escape.eS (t.target);
switch (t.valign) {
case 0:
if (t.movableXPercent == 2147483647 || t.movableYPercent == 2147483647) {
strOff = (t.movableXPercent == 2147483647 ? t.movableX + " " : t.movableXPercent + "% ") + (t.movableYPercent == 2147483647 ? t.movableY + "" : t.movableYPercent + "%");
} else {
strOff = "[" + t.movableXPercent + " " + t.movableYPercent + "%]";
}case 4:
if (strOff == null) strOff = J.util.Escape.eP (t.xyz);
s.append ("  ").append (echoCmd).append (" ").append (strOff);
if (t.align != 1) s.append (";  ").append (echoCmd).append (" ").append (J.modelset.Object2d.hAlignNames[t.align]);
break;
default:
s.append ("  set echo ").append (J.modelset.Object2d.vAlignNames[t.valign]).append (" ").append (J.modelset.Object2d.hAlignNames[t.align]);
}
if (t.valign == 0 && t.movableZPercent != 2147483647) s.append (";  ").append (echoCmd).append (" depth ").appendI (t.movableZPercent);
if (isImage) s.append ("; ").append (echoCmd).append (" IMAGE /*file*/");
 else s.append ("; echo ");
s.append (J.util.Escape.eS (text));
s.append (";\n");
if (isImage && t.imageScale != 1) s.append ("  ").append (echoCmd).append (" scale ").appendF (t.imageScale).append (";\n");
if (t.script != null) s.append ("  ").append (echoCmd).append (" script ").append (J.util.Escape.eS (t.script)).append (";\n");
if (t.modelIndex >= 0) s.append ("  ").append (echoCmd).append (" model ").append (this.viewer.getModelNumberDotted (t.modelIndex)).append (";\n");
if (t.pointerPt != null) {
s.append ("  ").append (echoCmd).append (" point ").append (Clazz.instanceOf (t.pointerPt, J.modelset.Atom) ? "({" + (t.pointerPt).index + "})" : J.util.Escape.eP (t.pointerPt)).append (";\n");
}s.append ("  " + J.shape.Shape.getFontCommand ("echo", t.font));
if (t.scalePixelsPerMicron > 0) s.append (" " + (10000 / t.scalePixelsPerMicron));
s.append ("; color echo");
if (J.util.C.isColixTranslucent (t.colix)) s.append (" translucent " + J.util.C.getColixTranslucencyFractional (t.colix));
s.append (" ").append (J.util.C.getHexCode (t.colix));
if (t.bgcolix != 0) {
s.append ("; color echo background");
if (J.util.C.isColixTranslucent (t.bgcolix)) s.append (" translucent " + J.util.C.getColixTranslucencyFractional (t.bgcolix));
s.append (" ").append (J.util.C.getHexCode (t.bgcolix));
}s.append (";\n");
return s.toString ();
}, $fz.isPrivate = true, $fz), "J.modelset.Text");
Clazz.overrideMethod (c$, "getLoadState", 
function (htParams) {
var g = this.viewer.global;
var str =  new J.util.SB ();
J.viewer.StateCreator.appendCmd (str, "set allowEmbeddedScripts false");
if (g.allowEmbeddedScripts) g.setB ("allowEmbeddedScripts", true);
J.viewer.StateCreator.appendCmd (str, "set appendNew " + g.appendNew);
J.viewer.StateCreator.appendCmd (str, "set appletProxy " + J.util.Escape.eS (g.appletProxy));
J.viewer.StateCreator.appendCmd (str, "set applySymmetryToBonds " + g.applySymmetryToBonds);
if (g.atomTypes.length > 0) J.viewer.StateCreator.appendCmd (str, "set atomTypes " + J.util.Escape.eS (g.atomTypes));
J.viewer.StateCreator.appendCmd (str, "set autoBond " + g.autoBond);
if (g.axesOrientationRasmol) J.viewer.StateCreator.appendCmd (str, "set axesOrientationRasmol true");
J.viewer.StateCreator.appendCmd (str, "set bondRadiusMilliAngstroms " + g.bondRadiusMilliAngstroms);
J.viewer.StateCreator.appendCmd (str, "set bondTolerance " + g.bondTolerance);
J.viewer.StateCreator.appendCmd (str, "set defaultLattice " + J.util.Escape.eP (g.ptDefaultLattice));
J.viewer.StateCreator.appendCmd (str, "set defaultLoadFilter " + J.util.Escape.eS (g.defaultLoadFilter));
J.viewer.StateCreator.appendCmd (str, "set defaultLoadScript \"\"");
if (g.defaultLoadScript.length > 0) g.setS ("defaultLoadScript", g.defaultLoadScript);
J.viewer.StateCreator.appendCmd (str, "set defaultStructureDssp " + g.defaultStructureDSSP);
var sMode = this.viewer.getDefaultVdwTypeNameOrData (-2147483648, null);
J.viewer.StateCreator.appendCmd (str, "set defaultVDW " + sMode);
if (sMode.equals ("User")) J.viewer.StateCreator.appendCmd (str, this.viewer.getDefaultVdwTypeNameOrData (2147483647, null));
J.viewer.StateCreator.appendCmd (str, "set forceAutoBond " + g.forceAutoBond);
J.viewer.StateCreator.appendCmd (str, "#set defaultDirectory " + J.util.Escape.eS (g.defaultDirectory));
J.viewer.StateCreator.appendCmd (str, "#set loadFormat " + J.util.Escape.eS (g.loadFormat));
J.viewer.StateCreator.appendCmd (str, "#set loadLigandFormat " + J.util.Escape.eS (g.loadLigandFormat));
J.viewer.StateCreator.appendCmd (str, "#set smilesUrlFormat " + J.util.Escape.eS (g.smilesUrlFormat));
J.viewer.StateCreator.appendCmd (str, "#set nihResolverFormat " + J.util.Escape.eS (g.nihResolverFormat));
J.viewer.StateCreator.appendCmd (str, "#set pubChemFormat " + J.util.Escape.eS (g.pubChemFormat));
J.viewer.StateCreator.appendCmd (str, "#set edsUrlFormat " + J.util.Escape.eS (g.edsUrlFormat));
J.viewer.StateCreator.appendCmd (str, "#set edsUrlCutoff " + J.util.Escape.eS (g.edsUrlCutoff));
J.viewer.StateCreator.appendCmd (str, "set legacyAutoBonding " + g.legacyAutoBonding);
J.viewer.StateCreator.appendCmd (str, "set legacyHAddition " + g.legacyHAddition);
J.viewer.StateCreator.appendCmd (str, "set minBondDistance " + g.minBondDistance);
J.viewer.StateCreator.appendCmd (str, "set minimizationCriterion  " + g.minimizationCriterion);
J.viewer.StateCreator.appendCmd (str, "set minimizationSteps  " + g.minimizationSteps);
J.viewer.StateCreator.appendCmd (str, "set pdbAddHydrogens " + (htParams != null && htParams.get ("pdbNoHydrogens") !== Boolean.TRUE ? g.pdbAddHydrogens : false));
J.viewer.StateCreator.appendCmd (str, "set pdbGetHeader " + g.pdbGetHeader);
J.viewer.StateCreator.appendCmd (str, "set pdbSequential " + g.pdbSequential);
J.viewer.StateCreator.appendCmd (str, "set percentVdwAtom " + g.percentVdwAtom);
J.viewer.StateCreator.appendCmd (str, "set smallMoleculeMaxAtoms " + g.smallMoleculeMaxAtoms);
J.viewer.StateCreator.appendCmd (str, "set smartAromatic " + g.smartAromatic);
if (g.zeroBasedXyzRasmol) J.viewer.StateCreator.appendCmd (str, "set zeroBasedXyzRasmol true");
return str.toString ();
}, "java.util.Map");
Clazz.overrideMethod (c$, "getAllSettings", 
function (prefix) {
var g = this.viewer.global;
var commands =  new J.util.SB ();
var list =  new Array (g.htBooleanParameterFlags.size () + g.htNonbooleanParameterValues.size () + g.htUserVariables.size ());
var n = 0;
var _prefix = "_" + prefix;
for (var key, $key = g.htBooleanParameterFlags.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) {
if (prefix == null || key.indexOf (prefix) == 0 || key.indexOf (_prefix) == 0) list[n++] = (key.indexOf ("_") == 0 ? key + " = " : "set " + key + " ") + g.htBooleanParameterFlags.get (key);
}
for (var key, $key = g.htNonbooleanParameterValues.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) {
if (key.charAt (0) != '@' && (prefix == null || key.indexOf (prefix) == 0 || key.indexOf (_prefix) == 0)) {
var value = g.htNonbooleanParameterValues.get (key);
if (Clazz.instanceOf (value, String)) value = J.viewer.StateCreator.chop (J.util.Escape.eS (value));
list[n++] = (key.indexOf ("_") == 0 ? key + " = " : "set " + key + " ") + value;
}}
for (var key, $key = g.htUserVariables.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) {
if (prefix == null || key.indexOf (prefix) == 0) {
var value = g.htUserVariables.get (key);
var s = value.asString ();
list[n++] = key + " " + (key.startsWith ("@") ? "" : "= ") + (value.tok == 4 ? J.viewer.StateCreator.chop (J.util.Escape.eS (s)) : s);
}}
java.util.Arrays.sort (list, 0, n);
for (var i = 0; i < n; i++) if (list[i] != null) J.viewer.StateCreator.appendCmd (commands, list[i]);

commands.append ("\n");
return commands.toString ();
}, "~S");
c$.chop = $_M(c$, "chop", 
($fz = function (s) {
var len = s.length;
if (len < 512) return s;
var sb =  new J.util.SB ();
var sep = "\"\\\n    + \"";
var pt = 0;
for (var i = 72; i < len; pt = i, i += 72) {
while (s.charAt (i - 1) == '\\') i++;

sb.append ((pt == 0 ? "" : sep)).append (s.substring (pt, i));
}
sb.append (sep).append (s.substring (pt, len));
return sb.toString ();
}, $fz.isPrivate = true, $fz), "~S");
Clazz.overrideMethod (c$, "getAtomShapeState", 
function (shape) {
this.clearTemp ();
var type = J.viewer.JC.shapeClassBases[shape.shapeID];
if (shape.bsSizeSet != null) for (var i = shape.bsSizeSet.nextSetBit (0); i >= 0; i = shape.bsSizeSet.nextSetBit (i + 1)) J.util.BSUtil.setMapBitSet (this.temp, i, i, type + (shape.mads[i] < 0 ? " on" : " " + shape.mads[i] / 2000));

if (shape.bsColixSet != null) for (var i = shape.bsColixSet.nextSetBit (0); i >= 0; i = shape.bsColixSet.nextSetBit (i + 1)) J.util.BSUtil.setMapBitSet (this.temp2, i, i, J.shape.Shape.getColorCommand (type, shape.paletteIDs[i], shape.colixes[i], shape.translucentAllowed));

var s = this.getCommands (this.temp, this.temp2, "select");
this.clearTemp ();
return s;
}, "J.shape.AtomShape");
Clazz.overrideMethod (c$, "getFunctionCalls", 
function (selectedFunction) {
if (selectedFunction == null) selectedFunction = "";
var s =  new J.util.SB ();
var pt = selectedFunction.indexOf ("*");
var isGeneric = (pt >= 0);
var isStatic = (selectedFunction.indexOf ("static_") == 0);
var namesOnly = (selectedFunction.equalsIgnoreCase ("names") || selectedFunction.equalsIgnoreCase ("static_names"));
if (namesOnly) selectedFunction = "";
if (isGeneric) selectedFunction = selectedFunction.substring (0, pt);
selectedFunction = selectedFunction.toLowerCase ();
var ht = (isStatic ? J.viewer.Viewer.staticFunctions : this.viewer.localFunctions);
var names =  new Array (ht.size ());
var n = 0;
for (var name, $name = ht.keySet ().iterator (); $name.hasNext () && ((name = $name.next ()) || true);) if (selectedFunction.length == 0 && !name.startsWith ("_") || name.equalsIgnoreCase (selectedFunction) || isGeneric && name.toLowerCase ().indexOf (selectedFunction) == 0) names[n++] = name;

java.util.Arrays.sort (names, 0, n);
for (var i = 0; i < n; i++) {
var f = ht.get (names[i]);
s.append (namesOnly ? f.getSignature () : f.toString ());
s.appendC ('\n');
}
return s.toString ();
}, "~S");
c$.isTainted = $_M(c$, "isTainted", 
($fz = function (tainted, atomIndex, type) {
return (tainted != null && tainted[type] != null && tainted[type].get (atomIndex));
}, $fz.isPrivate = true, $fz), "~A,~N,~N");
Clazz.overrideMethod (c$, "getAtomicPropertyState", 
function (taintWhat, bsSelected) {
if (!this.viewer.global.preserveState) return "";
var bs;
var commands =  new J.util.SB ();
for (var type = 0; type < 14; type++) if (taintWhat < 0 || type == taintWhat) if ((bs = (bsSelected != null ? bsSelected : this.viewer.getTaintedAtoms (type))) != null) this.getAtomicPropertyStateBuffer (commands, type, bs, null, null);

return commands.toString ();
}, "~N,J.util.BS");
Clazz.overrideMethod (c$, "getAtomicPropertyStateBuffer", 
function (commands, type, bs, label, fData) {
if (!this.viewer.global.preserveState) return;
var s =  new J.util.SB ();
var dataLabel = (label == null ? J.modelset.AtomCollection.userSettableValues[type] : label) + " set";
var n = 0;
var isDefault = (type == 2);
var atoms = this.viewer.modelSet.atoms;
var tainted = this.viewer.modelSet.tainted;
if (bs != null) for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
s.appendI (i + 1).append (" ").append (atoms[i].getElementSymbol ()).append (" ").append (atoms[i].getInfo ().$replace (' ', '_')).append (" ");
switch (type) {
case 14:
if (i < fData.length) s.appendF (fData[i]);
break;
case 13:
s.appendI (atoms[i].getAtomNumber ());
break;
case 0:
s.append (atoms[i].getAtomName ());
break;
case 1:
s.append (atoms[i].getAtomType ());
break;
case 2:
if (J.viewer.StateCreator.isTainted (tainted, i, 2)) isDefault = false;
s.appendF (atoms[i].x).append (" ").appendF (atoms[i].y).append (" ").appendF (atoms[i].z);
break;
case 12:
var v = atoms[i].getVibrationVector ();
if (v == null) v =  new J.util.V3 ();
s.appendF (v.x).append (" ").appendF (v.y).append (" ").appendF (v.z);
break;
case 3:
s.appendI (atoms[i].getAtomicAndIsotopeNumber ());
break;
case 4:
s.appendI (atoms[i].getFormalCharge ());
break;
case 6:
s.appendF (atoms[i].getBondingRadiusFloat ());
break;
case 7:
s.appendI (atoms[i].getOccupancy100 ());
break;
case 8:
s.appendF (atoms[i].getPartialCharge ());
break;
case 9:
s.appendF (atoms[i].getBfactor100 () / 100);
break;
case 10:
s.appendI (atoms[i].getValence ());
break;
case 11:
s.appendF (atoms[i].getVanderwaalsRadiusFloat (this.viewer, J.constant.EnumVdw.AUTO));
break;
}
s.append (" ;\n");
++n;
}
if (n == 0) return;
if (isDefault) dataLabel += "(default)";
commands.append ("\n  DATA \"" + dataLabel + "\"\n").appendI (n).append (" ;\nJmol Property Data Format 1 -- Jmol ").append (J.viewer.Viewer.getJmolVersion ()).append (";\n");
commands.appendSB (s);
commands.append ("  end \"" + dataLabel + "\";\n");
}, "J.util.SB,~N,J.util.BS,~S,~A");
Clazz.overrideMethod (c$, "undoMoveAction", 
function (action, n) {
switch (action) {
case 4165:
case 4139:
switch (n) {
case -2:
this.viewer.undoClear ();
break;
case -1:
(action == 4165 ? this.viewer.actionStates : this.viewer.actionStatesRedo).clear ();
break;
case 0:
n = 2147483647;
default:
if (n > 100) n = (action == 4165 ? this.viewer.actionStates : this.viewer.actionStatesRedo).size ();
for (var i = 0; i < n; i++) this.undoMoveActionClear (0, action, true);

}
break;
}
}, "~N,~N");
Clazz.overrideMethod (c$, "undoMoveActionClear", 
function (taintedAtom, type, clearRedo) {
if (!this.viewer.global.preserveState) return;
var modelIndex = (taintedAtom >= 0 ? this.viewer.modelSet.atoms[taintedAtom].modelIndex : this.viewer.modelSet.modelCount - 1);
switch (type) {
case 4139:
case 4165:
this.viewer.stopMinimization ();
var s = "";
var list1;
var list2;
switch (type) {
default:
case 4165:
list1 = this.viewer.actionStates;
list2 = this.viewer.actionStatesRedo;
break;
case 4139:
list1 = this.viewer.actionStatesRedo;
list2 = this.viewer.actionStates;
if (this.viewer.actionStatesRedo.size () == 1) return;
break;
}
if (list1.size () == 0 || this.undoWorking) return;
this.undoWorking = true;
list2.add (0, list1.remove (0));
s = this.viewer.actionStatesRedo.get (0);
if (type == 4165 && list2.size () == 1) {
var pt = [1];
type = J.util.Parser.parseIntNext (s, pt);
taintedAtom = J.util.Parser.parseIntNext (s, pt);
this.undoMoveActionClear (taintedAtom, type, false);
}if (this.viewer.modelSet.models[modelIndex].isModelkit () || s.indexOf ("zap ") < 0) {
if (J.util.Logger.debugging) this.viewer.log (s);
this.viewer.evalStringQuiet (s);
} else {
this.viewer.actionStates.clear ();
}break;
default:
if (this.undoWorking && clearRedo) return;
this.undoWorking = true;
var bs;
var sb =  new J.util.SB ();
sb.append ("#" + type + " " + taintedAtom + " " + ( new java.util.Date ()) + "\n");
if (taintedAtom >= 0) {
bs = this.viewer.getModelUndeletedAtomsBitSet (modelIndex);
this.viewer.modelSet.taintAtoms (bs, type);
sb.append (this.getAtomicPropertyState (-1, null));
} else {
bs = this.viewer.getModelUndeletedAtomsBitSet (modelIndex);
sb.append ("zap ");
sb.append (J.util.Escape.eBS (bs)).append (";");
J.viewer.DataManager.getInlineData (sb, this.viewer.getModelExtract (bs, false, true, "MOL"), true, null);
sb.append ("set refreshing false;").append (this.viewer.actionManager.getPickingState ()).append (this.viewer.transformManager.getMoveToText (0, false)).append ("set refreshing true;");
}if (clearRedo) {
this.viewer.actionStates.add (0, sb.toString ());
this.viewer.actionStatesRedo.clear ();
} else {
this.viewer.actionStatesRedo.add (1, sb.toString ());
}if (this.viewer.actionStates.size () == 100) {
this.viewer.actionStates.remove (99);
}}
this.undoWorking = !clearRedo;
}, "~N,~N,~B");
$_M(c$, "appendLoadStates", 
function (cmds) {
var ligandModelSet = this.viewer.ligandModelSet;
if (ligandModelSet != null) {
for (var key, $key = ligandModelSet.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) {
var data = this.viewer.ligandModels.get (key + "_data");
if (data != null) cmds.append ("  ").append (J.util.Escape.encapsulateData ("ligand_" + key, data.trim () + "\n", 0));
data = this.viewer.ligandModels.get (key + "_file");
if (data != null) cmds.append ("  ").append (J.util.Escape.encapsulateData ("file_" + key, data.trim () + "\n", 0));
}
}var commands =  new J.util.SB ();
var ms = this.viewer.modelSet;
var models = ms.models;
var modelCount = ms.modelCount;
for (var i = 0; i < modelCount; i++) {
if (ms.isJmolDataFrameForModel (i) || ms.isTrajectorySubFrame (i)) continue;
var m = models[i];
var pt = commands.indexOf (m.loadState);
if (pt < 0 || pt != commands.lastIndexOf (m.loadState)) commands.append (models[i].loadState);
if (models[i].isModelKit) {
var bs = ms.getModelAtomBitSetIncludingDeleted (i, false);
if (ms.tainted != null) {
if (ms.tainted[2] != null) ms.tainted[2].andNot (bs);
if (ms.tainted[3] != null) ms.tainted[3].andNot (bs);
}m.loadScript =  new J.util.SB ();
J.viewer.Viewer.getInlineData (commands, this.viewer.getModelExtract (bs, false, true, "MOL"), i > 0);
} else {
commands.appendSB (m.loadScript);
}}
var s = commands.toString ();
var i = s.indexOf ("load /*data*/");
var j = s.indexOf ("load /*file*/");
if (j >= 0 && j < i) i = j;
if ((j = s.indexOf ("load \"@")) >= 0 && j < i) i = j;
if (i >= 0) s = s.substring (0, i) + "zap;" + s.substring (i);
cmds.append (s);
}, "J.util.SB");
$_M(c$, "createSceneSet", 
($fz = function (sceneFile, type, width, height) {
var script0 = this.viewer.getFileAsString (sceneFile);
if (script0 == null) return "no such file: " + sceneFile;
sceneFile = J.util.TextFormat.simpleReplace (sceneFile, ".spt", "");
var fileRoot = sceneFile;
var fileExt = type.toLowerCase ();
var scenes = J.util.TextFormat.splitChars (script0, "pause scene ");
var htScenes =  new java.util.Hashtable ();
var list =  new J.util.JmolList ();
var script = J.io.JmolBinary.getSceneScript (scenes, htScenes, list);
if (J.util.Logger.debugging) J.util.Logger.debug (script);
script0 = J.util.TextFormat.simpleReplace (script0, "pause scene", "delay " + this.viewer.animationManager.lastFrameDelay + " # scene");
var str = [script0, script, null];
this.viewer.saveState ("_scene0");
var nFiles = 0;
if (scenes[0] !== "") this.viewer.zap (true, true, false);
var iSceneLast = -1;
for (var i = 0; i < scenes.length - 1; i++) {
try {
var iScene = list.get (i).intValue ();
if (iScene > iSceneLast) this.viewer.showString ("Creating Scene " + iScene, false);
this.viewer.eval.runScript (scenes[i]);
if (iScene <= iSceneLast) continue;
iSceneLast = iScene;
str[2] = "all";
var fileName = fileRoot + "_scene_" + iScene + ".all." + fileExt;
var msg = this.createImagePathCheck (fileName, "PNGJ", null, null, str, -1, width, height, null, false);
str[0] = null;
str[2] = "min";
fileName = fileRoot + "_scene_" + iScene + ".min." + fileExt;
msg += "\n" + this.createImagePathCheck (fileName, "PNGJ", null, null, str, -1, Math.min (width, 200), Math.min (height, 200), null, false);
this.viewer.showString (msg, false);
nFiles += 2;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return "script error " + e.toString ();
} else {
throw e;
}
}
}
try {
this.viewer.eval.runScript (this.viewer.getSavedState ("_scene0"));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return "OK " + nFiles + " files created";
}, $fz.isPrivate = true, $fz), "~S,~S,~N,~N");
Clazz.overrideMethod (c$, "createImageSet", 
function (fileName, type, text, bytes, scripts, quality, width, height, bsFrames, nVibes, fullPath) {
if (bsFrames == null && nVibes == 0) return this.createImagePathCheck (fileName, type, text, bytes, scripts, quality, width, height, fullPath, true);
var info = "";
var n = 0;
fileName = this.getOutputFileNameFromDialog (fileName, quality);
if (fullPath != null) fullPath[0] = fileName;
if (fileName == null) return null;
var ptDot = fileName.indexOf (".");
if (ptDot < 0) ptDot = fileName.length;
var froot = fileName.substring (0, ptDot);
var fext = fileName.substring (ptDot);
var sb =  new J.util.SB ();
if (bsFrames == null) {
this.viewer.transformManager.vibrationOn = true;
sb =  new J.util.SB ();
for (var i = 0; i < nVibes; i++) {
for (var j = 0; j < 20; j++) {
this.viewer.transformManager.setVibrationT (j / 20 + 0.2501);
if (!this.writeFrame (++n, froot, fext, fullPath, type, quality, width, height, sb)) return "ERROR WRITING FILE SET: \n" + info;
}
}
this.viewer.setVibrationOff ();
} else {
for (var i = bsFrames.nextSetBit (0); i >= 0; i = bsFrames.nextSetBit (i + 1)) {
this.viewer.setCurrentModelIndex (i);
if (!this.writeFrame (++n, froot, fext, fullPath, type, quality, width, height, sb)) return "ERROR WRITING FILE SET: \n" + info;
}
}if (info.length == 0) info = "OK\n";
return info + "\n" + n + " files created";
}, "~S,~S,~S,~A,~A,~N,~N,~N,J.util.BS,~N,~A");
$_M(c$, "writeFrame", 
($fz = function (n, froot, fext, fullPath, type, quality, width, height, sb) {
var fileName = "0000" + n;
fileName = froot + fileName.substring (fileName.length - 4) + fext;
if (fullPath != null) fullPath[0] = fileName;
var msg = this.createImagePathCheck (fileName, type, null, null, null, quality, width, height, null, false);
this.viewer.scriptEcho (msg);
sb.append (msg).append ("\n");
return msg.startsWith ("OK");
}, $fz.isPrivate = true, $fz), "~N,~S,~S,~A,~S,~N,~N,~N,J.util.SB");
Clazz.overrideMethod (c$, "createImage", 
function (fileName, type, text, bytes, scripts, quality, width, height) {
return this.createImagePathCheck (fileName, type, text, bytes, scripts, quality, width, height, null, true);
}, "~S,~S,~S,~A,~A,~N,~N,~N");
$_M(c$, "createImagePathCheck", 
($fz = function (fileName, type, text, bytes, scripts, quality, width, height, fullPath, doCheck) {
var ret = null;
var isClip = (fileName == null);
var localName = null;
if (!isClip) {
if (doCheck) fileName = this.getOutputFileNameFromDialog (fileName, quality);
if (fileName == null) return null;
if (J.viewer.FileManager.isLocal (fileName)) localName = fileName;
if (fullPath != null) fullPath[0] = fileName;
}if (!isClip && fullPath != null && (fileName = fullPath[0]) == null) return null;
var saveWidth = this.viewer.dimScreen.width;
var saveHeight = this.viewer.dimScreen.height;
this.viewer.creatingImage = true;
if (quality != -2147483648) {
this.viewer.mustRender = true;
this.viewer.resizeImage (width, height, true, false, false);
this.viewer.setModelVisibility ();
}try {
if (isClip) {
ret = this.viewer.clipImage (text);
} else {
if (type.equals ("JMOL")) type = "ZIPALL";
if (type.equals ("ZIP") || type.equals ("ZIPALL")) {
if (scripts != null && type.equals ("ZIP")) type = "ZIPALL";
ret = J.io.JmolBinary.createZipSet (this.privateKey, this.viewer.fileManager, this.viewer, localName, text, scripts, type.equals ("ZIPALL"));
} else if (type.equals ("SCENE")) {
ret = (this.viewer.isJS ? "ERROR: Not Available" : this.createSceneSet (fileName, text, width, height));
} else {
ret = this.viewer.statusManager.createImage (fileName, type, text, bytes, quality);
if (ret == null) {
var c = this.viewer.getImageCreator ();
ret = c.createImage (localName, type, text, bytes, scripts, quality);
if (Clazz.instanceOf (ret, String)) this.viewer.statusManager.createImage (ret, type, null, null, quality);
}}if (Clazz.instanceOf (ret, Array)) ret = "OK " + J.io.JmolBinary.postByteArray (this.viewer.fileManager, fileName, ret);
}} catch (er) {
J.util.Logger.error (this.viewer.setErrorMessage ((ret = "ERROR creating image??: " + er), null));
}
this.viewer.creatingImage = false;
if (quality != -2147483648) {
this.viewer.resizeImage (saveWidth, saveHeight, true, false, true);
}return ret;
}, $fz.isPrivate = true, $fz), "~S,~S,~S,~A,~A,~N,~N,~N,~A,~B");
Clazz.overrideMethod (c$, "syncScript", 
function (script, applet, port) {
var sm = this.viewer.statusManager;
if ("GET_GRAPHICS".equalsIgnoreCase (script)) {
sm.setSyncDriver (5);
sm.syncSend (script, applet, 0);
this.viewer.setBooleanProperty ("_syncMouse", false);
this.viewer.setBooleanProperty ("_syncScript", false);
return;
}if ("=".equals (applet)) {
applet = "~";
sm.setSyncDriver (2);
}var disableSend = "~".equals (applet);
if (port > 0 || !disableSend && !".".equals (applet)) {
sm.syncSend (script, applet, port);
if (!"*".equals (applet) || script.startsWith ("{")) return;
}if (script.equalsIgnoreCase ("on") || script.equalsIgnoreCase ("true")) {
sm.setSyncDriver (1);
return;
}if (script.equalsIgnoreCase ("off") || script.equalsIgnoreCase ("false")) {
sm.setSyncDriver (0);
return;
}if (script.equalsIgnoreCase ("slave")) {
sm.setSyncDriver (2);
return;
}var syncMode = sm.getSyncMode ();
if (syncMode == 0) return;
if (syncMode != 1) disableSend = false;
if (J.util.Logger.debugging) J.util.Logger.debug (this.viewer.htmlName + " syncing with script: " + script);
if (disableSend) sm.setSyncDriver (3);
if (script.indexOf ("Mouse: ") != 0) {
if (script.startsWith ("Peaks: [")) {
var list = J.util.Escape.unescapeStringArray (script.substring (7));
var peaks =  new J.util.JmolList ();
for (var i = 0; i < list.length; i++) peaks.addLast (list[i]);

this.viewer.getModelSet ().setModelAuxiliaryInfo (this.viewer.getCurrentModelIndex (), "jdxAtomSelect_1HNMR", peaks);
return;
}if (script.startsWith ("Select: ")) {
var filename = J.util.Parser.getQuotedAttribute (script, "file");
if (filename.startsWith (J.viewer.StateCreator.SIMULATION_PROTOCOL + "MOL=")) filename = null;
var modelID = J.util.Parser.getQuotedAttribute (script, "model");
var baseModel = J.util.Parser.getQuotedAttribute (script, "baseModel");
var atoms = J.util.Parser.getQuotedAttribute (script, "atoms");
var select = J.util.Parser.getQuotedAttribute (script, "select");
var script2 = J.util.Parser.getQuotedAttribute (script, "script");
var isNIH = (modelID != null && modelID.startsWith ("$"));
if (isNIH) filename = this.viewer.setLoadFormat (modelID, '$', false);
var id = (modelID == null ? null : (filename == null ? "" : filename + "#") + modelID);
if ("".equals (baseModel)) id += ".baseModel";
var modelIndex = (id == null ? -3 : this.viewer.getModelIndexFromId (id));
if (modelIndex == -2) return;
script = (modelIndex == -1 && filename != null ? script = "load " + J.util.Escape.eS (filename) : "");
script = J.util.TextFormat.simpleReplace (script, J.viewer.StateCreator.SIMULATION_PROTOCOL, "");
if (id != null) script += ";model " + J.util.Escape.eS (id);
if (atoms != null) script += ";select visible & (@" + J.util.TextFormat.simpleReplace (atoms, ",", " or @") + ")";
 else if (select != null) script += ";select visible & (" + select + ")";
if (script2 != null) script += ";" + script2;
} else if (script.toLowerCase ().startsWith ("jspecview")) {
if (!disableSend) sm.syncSend (this.viewer.fullName + "JSpecView" + script.substring (9), ">", 0);
return;
}this.viewer.evalStringQuietSync (script, true, false);
return;
}this.mouseScript (script);
if (disableSend) this.viewer.setSyncDriver (4);
}, "~S,~S,~N");
Clazz.overrideMethod (c$, "mouseScript", 
function (script) {
var tokens = J.util.Parser.getTokens (script);
var key = tokens[1];
try {
key = (key.toLowerCase () + "...............").substring (0, 15);
switch (("zoombyfactor...zoomby.........rotatezby......rotatexyby.....translatexyby..rotatemolecule.spinxyby.......rotatearcball..").indexOf (key)) {
case 0:
switch (tokens.length) {
case 3:
this.viewer.zoomByFactor (J.util.Parser.parseFloatStr (tokens[2]), 2147483647, 2147483647);
return;
case 5:
this.viewer.zoomByFactor (J.util.Parser.parseFloatStr (tokens[2]), J.util.Parser.parseInt (tokens[3]), J.util.Parser.parseInt (tokens[4]));
return;
}
break;
case 15:
switch (tokens.length) {
case 3:
this.viewer.zoomBy (J.util.Parser.parseInt (tokens[2]));
return;
}
break;
case 30:
switch (tokens.length) {
case 3:
this.viewer.rotateZBy (J.util.Parser.parseInt (tokens[2]), 2147483647, 2147483647);
return;
case 5:
this.viewer.rotateZBy (J.util.Parser.parseInt (tokens[2]), J.util.Parser.parseInt (tokens[3]), J.util.Parser.parseInt (tokens[4]));
}
break;
case 45:
this.viewer.rotateXYBy (J.util.Parser.parseFloatStr (tokens[2]), J.util.Parser.parseFloatStr (tokens[3]));
return;
case 60:
this.viewer.translateXYBy (J.util.Parser.parseInt (tokens[2]), J.util.Parser.parseInt (tokens[3]));
return;
case 75:
this.viewer.spinXYBy (J.util.Parser.parseInt (tokens[2]), J.util.Parser.parseInt (tokens[3]), J.util.Parser.parseFloatStr (tokens[4]));
return;
case 90:
this.viewer.rotateArcBall (J.util.Parser.parseInt (tokens[2]), J.util.Parser.parseInt (tokens[3]), J.util.Parser.parseFloatStr (tokens[4]));
return;
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
this.viewer.showString ("error reading SYNC command: " + script, false);
}, "~S");
Clazz.overrideMethod (c$, "generateOutputForExport", 
function (type, fileName, width, height) {
var fName = null;
if (fileName != null) {
fileName[0] = this.getOutputFileNameFromDialog (fileName[0], -2147483648);
if (fileName[0] == null) return null;
fName = fileName[0];
}this.viewer.mustRender = true;
var saveWidth = this.viewer.dimScreen.width;
var saveHeight = this.viewer.dimScreen.height;
this.viewer.resizeImage (width, height, true, true, false);
this.viewer.setModelVisibility ();
var data = this.viewer.repaintManager.renderExport (type, this.viewer.gdata, this.viewer.modelSet, fName);
this.viewer.resizeImage (saveWidth, saveHeight, true, true, true);
return data;
}, "~S,~A,~N,~N");
$_M(c$, "getOutputFileNameFromDialog", 
($fz = function (fileName, quality) {
if (fileName == null || this.viewer.$isKiosk) return null;
var useDialog = (fileName.indexOf ("?") == 0);
if (useDialog) fileName = fileName.substring (1);
useDialog = new Boolean (useDialog | (this.viewer.isApplet () && (fileName.indexOf ("http:") < 0))).valueOf ();
fileName = J.viewer.FileManager.getLocalPathForWritingFile (this.viewer, fileName);
if (useDialog) fileName = this.viewer.dialogAsk (quality == -2147483648 ? "Save" : "Save Image", fileName);
return fileName;
}, $fz.isPrivate = true, $fz), "~S,~N");
Clazz.overrideMethod (c$, "getImageAsWithComment", 
function (type, quality, width, height, fileName, scripts, os, comment) {
var saveWidth = this.viewer.dimScreen.width;
var saveHeight = this.viewer.dimScreen.height;
this.viewer.mustRender = true;
this.viewer.resizeImage (width, height, true, false, false);
this.viewer.setModelVisibility ();
this.viewer.creatingImage = true;
var c = null;
var bytes = null;
type = type.toLowerCase ();
if (!J.util.Parser.isOneOf (type, ";jpg;jpeg;jpg64;jpeg64;")) try {
c = this.viewer.getImageCreator ();
} catch (er) {
if (Clazz.exceptionOf (er, Error)) {
} else {
throw er;
}
}
if (c == null) {
try {
bytes = this.viewer.apiPlatform.getJpgImage (this.viewer, quality, comment);
if (type.equals ("jpg64") || type.equals ("jpeg64")) bytes = (bytes == null ? "" : J.io.Base64.getBase64 (bytes).toString ());
} catch (er) {
if (Clazz.exceptionOf (er, Error)) {
this.viewer.releaseScreenImage ();
this.viewer.handleError (er, false);
this.viewer.setErrorMessage ("Error creating image: " + er, null);
bytes = this.viewer.getErrorMessage ();
} else {
throw er;
}
}
} else {
try {
bytes = c.getImageBytes (type, quality, fileName, scripts, null, os);
} catch (e$$) {
if (Clazz.exceptionOf (e$$, java.io.IOException)) {
var e = e$$;
{
bytes = e;
this.viewer.setErrorMessage ("Error creating image: " + e, null);
}
} else if (Clazz.exceptionOf (e$$, Error)) {
var er = e$$;
{
this.viewer.handleError (er, false);
this.viewer.setErrorMessage ("Error creating image: " + er, null);
bytes = this.viewer.getErrorMessage ();
}
} else {
throw e$$;
}
}
}this.viewer.creatingImage = false;
this.viewer.resizeImage (saveWidth, saveHeight, true, false, true);
return bytes;
}, "~S,~N,~N,~N,~S,~A,java.io.OutputStream,~S");
Clazz.overrideMethod (c$, "writeFileData", 
function (fileName, type, modelIndex, parameters) {
var fullPath =  new Array (1);
var os = this.getOutputStream (fileName, fullPath);
if (os == null) return "";
fileName = fullPath[0];
var pathName = (type.equals ("FILE") ? this.viewer.getFullPathName () : null);
var getCurrentFile = (pathName != null && (pathName.equals ("string") || pathName.indexOf ("[]") >= 0 || pathName.equals ("JSNode")));
var asBytes = (pathName != null && !getCurrentFile);
if (asBytes) {
pathName = this.viewer.getModelSetPathName ();
if (pathName == null) return null;
}var bos;
{
bos = os;
}var osb =  new J.io.OutputStringBuilder (bos, asBytes);
osb.type = type;
var msg = (type.equals ("PDB") || type.equals ("PQR") ? this.viewer.getPdbAtomData (null, osb) : type.startsWith ("PLOT") ? this.viewer.modelSet.getPdbData (modelIndex, type.substring (5), this.viewer.getSelectionSet (false), parameters, osb) : getCurrentFile ? osb.append (this.viewer.getCurrentFileAsString ()).toString () : this.viewer.getFileAsBytes (pathName, osb));
if (msg != null) msg = "OK " + msg + " " + fileName;
try {
os.flush ();
os.close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
return msg;
}, "~S,~S,~N,~A");
Clazz.overrideMethod (c$, "getOutputStream", 
function (fileName, fullPath) {
if (!this.viewer.isRestricted (J.viewer.Viewer.ACCESS.ALL)) return null;
fileName = this.getOutputFileNameFromDialog (fileName, -2147483648);
if (fileName == null) return null;
if (fullPath != null) fullPath[0] = fileName;
var localName = (J.viewer.FileManager.isLocal (fileName) ? fileName : null);
try {
return this.viewer.openOutputChannel (this.privateKey, localName, false);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.info (e.toString ());
return null;
} else {
throw e;
}
}
}, "~S,~A");
Clazz.overrideMethod (c$, "openFileAsync", 
function (fileName, pdbCartoons) {
fileName = fileName.trim ();
var allowScript = (!fileName.startsWith ("\t"));
if (!allowScript) fileName = fileName.substring (1);
fileName = fileName.$replace ('\\', '/');
var isCached = fileName.startsWith ("cache://");
if (this.viewer.isApplet () && fileName.indexOf ("://") < 0) fileName = "file://" + (fileName.startsWith ("/") ? "" : "/") + fileName;
if (fileName.endsWith (".pse")) {
this.viewer.evalString ((isCached ? "" : "zap;") + "load SYNC " + J.util.Escape.eS (fileName) + " filter 'DORESIZE'");
return;
}var cmd = null;
if (fileName.endsWith ("jvxl")) {
cmd = "isosurface ";
} else if (!fileName.endsWith (".spt")) {
var type = this.viewer.fileManager.getFileTypeName (fileName);
if (type == null) {
type = J.io.JmolBinary.determineSurfaceTypeIs (this.viewer.getBufferedInputStream (fileName));
if (type != null) {
this.viewer.evalString ("if (_filetype == 'Pdb') { isosurface sigma 1.0 within 2.0 {*} " + J.util.Escape.eS (fileName) + " mesh nofill }; else; { isosurface " + J.util.Escape.eS (fileName) + "}");
return;
}} else if (type.equals ("Jmol")) {
cmd = "script ";
} else if (type.equals ("Cube")) {
cmd = "isosurface sign red blue ";
} else if (!type.equals ("spt")) {
cmd = this.viewer.global.defaultDropScript;
cmd = J.util.TextFormat.simpleReplace (cmd, "%FILE", fileName);
cmd = J.util.TextFormat.simpleReplace (cmd, "%ALLOWCARTOONS", "" + pdbCartoons);
if (cmd.toLowerCase ().startsWith ("zap") && isCached) cmd = cmd.substring (3);
this.viewer.evalString (cmd);
return;
}}if (allowScript && this.viewer.scriptEditorVisible && cmd == null) this.showEditor ([fileName, this.viewer.getFileAsString (fileName)]);
 else this.viewer.evalString ((cmd == null ? "script " : cmd) + J.util.Escape.eS (fileName));
}, "~S,~B");
Clazz.overrideMethod (c$, "showEditor", 
function (file_text) {
if (file_text == null) file_text = [null, null];
if (file_text[1] == null) file_text[1] = "<no data>";
var filename = file_text[0];
var msg = file_text[1];
var scriptEditor = this.viewer.getProperty ("DATA_API", "getScriptEditor", Boolean.TRUE);
if (scriptEditor == null) return;
if (msg != null) {
scriptEditor.setFilename (filename);
scriptEditor.output (J.io.JmolBinary.getEmbeddedScript (msg));
}scriptEditor.setVisible (true);
}, "~A");
Clazz.overrideMethod (c$, "getLogFileName", 
function () {
return (this.logFileName == null ? "" : this.logFileName);
});
Clazz.overrideMethod (c$, "setLogFile", 
function (value) {
var path = null;
var logFilePath = this.viewer.getLogFilePath ();
if (logFilePath == null || value.indexOf ("\\") >= 0 || value.indexOf ("/") >= 0) {
value = null;
} else if (value.length > 0) {
if (!value.startsWith ("JmolLog_")) value = "JmolLog_" + value;
path = this.viewer.getAbsolutePath (this.privateKey, logFilePath + value);
}if (path == null) value = null;
 else J.util.Logger.info (J.i18n.GT._ ("Setting log file to {0}", path));
if (value == null || !this.viewer.isRestricted (J.viewer.Viewer.ACCESS.ALL)) {
J.util.Logger.info (J.i18n.GT._ ("Cannot set log file path."));
value = null;
} else {
this.logFileName = path;
this.viewer.global.setS ("_logFile", this.viewer.isApplet () ? value : path);
}return value;
}, "~S");
Clazz.overrideMethod (c$, "logToFile", 
function (data) {
try {
var doClear = (data.equals ("$CLEAR$"));
if (data.indexOf ("$NOW$") >= 0) data = J.util.TextFormat.simpleReplace (data, "$NOW$", ( new java.util.Date ()).toString ());
if (this.logFileName == null) {
System.out.println (data);
return;
}var out = this.viewer.openLogFile (this.privateKey, this.logFileName, !doClear);
if (!doClear) {
var ptEnd = data.indexOf ('\0');
if (ptEnd >= 0) data = data.substring (0, ptEnd);
out.write (data);
if (ptEnd < 0) out.write ("\n");
}out.close ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
if (J.util.Logger.debugging) J.util.Logger.debug ("cannot log " + data);
} else {
throw e;
}
}
}, "~S");
Clazz.overrideMethod (c$, "getAtomDefs", 
function (names) {
var sb =  new J.util.SB ();
for (var e, $e = names.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
if (Clazz.instanceOf (e.getValue (), J.util.BS)) sb.append ("{" + e.getKey () + "} <" + (e.getValue ()).cardinality () + " atoms>\n");
}
return sb.append ("\n").toString ();
}, "java.util.Map");
Clazz.defineStatics (c$,
"SIMULATION_PROTOCOL", "http://SIMULATION/",
"MAX_ACTION_UNDO", 100);
});
