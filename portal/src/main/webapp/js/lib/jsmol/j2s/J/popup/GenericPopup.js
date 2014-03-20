Clazz.declarePackage ("J.popup");
Clazz.load (["J.api.JmolPopupInterface", "J.popup.JmolAbstractMenu", "java.util.Hashtable", "$.Properties", "J.util.JmolList"], "J.popup.GenericPopup", ["java.lang.Boolean", "$.Long", "java.util.StringTokenizer", "J.i18n.GT", "J.popup.MainPopupResourceBundle", "J.util.Elements", "$.Escape", "$.Logger", "$.Parser", "$.SB", "$.TextFormat", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.htCheckbox = null;
this.menuText = null;
this.buttonGroup = null;
this.currentMenuItemId = null;
this.strMenuStructure = null;
this.updateMode = 0;
this.menuName = null;
this.frankPopup = null;
this.popupMenu = null;
this.thisPopup = null;
this.nFrankList = 0;
this.itemMax = 25;
this.titleWidthMax = 20;
this.thisx = 0;
this.thisy = 0;
this.nullModelSetName = null;
this.modelSetName = null;
this.modelSetFileName = null;
this.modelSetRoot = null;
this.currentFrankId = null;
this.configurationSelected = "";
this.altlocs = null;
this.frankList = null;
this.modelSetInfo = null;
this.modelInfo = null;
this.htMenus = null;
this.NotPDB = null;
this.PDBOnly = null;
this.FileUnitOnly = null;
this.FileMolOnly = null;
this.UnitcellOnly = null;
this.SingleModelOnly = null;
this.FramesOnly = null;
this.VibrationOnly = null;
this.SymmetryOnly = null;
this.SignedOnly = null;
this.AppletOnly = null;
this.ChargesOnly = null;
this.TemperatureOnly = null;
this.allowSignedFeatures = false;
this.isJS = false;
this.fileHasUnitCell = false;
this.haveBFactors = false;
this.haveCharges = false;
this.isApplet = false;
this.isLastFrame = false;
this.isMultiConfiguration = false;
this.isMultiFrame = false;
this.isPDB = false;
this.isSigned = false;
this.isSymmetry = false;
this.isUnitCell = false;
this.isVibration = false;
this.isZapped = false;
this.modelIndex = 0;
this.modelCount = 0;
this.atomCount = 0;
this.aboutComputedMenuBaseCount = 0;
this.group3List = null;
this.group3Counts = null;
this.cnmrPeaks = null;
this.hnmrPeaks = null;
Clazz.instantialize (this, arguments);
}, J.popup, "GenericPopup", null, [J.api.JmolPopupInterface, J.popup.JmolAbstractMenu]);
Clazz.prepareFields (c$, function () {
this.htCheckbox =  new java.util.Hashtable ();
this.menuText =  new java.util.Properties ();
this.frankList =  new Array (10);
this.htMenus =  new java.util.Hashtable ();
this.NotPDB =  new J.util.JmolList ();
this.PDBOnly =  new J.util.JmolList ();
this.FileUnitOnly =  new J.util.JmolList ();
this.FileMolOnly =  new J.util.JmolList ();
this.UnitcellOnly =  new J.util.JmolList ();
this.SingleModelOnly =  new J.util.JmolList ();
this.FramesOnly =  new J.util.JmolList ();
this.VibrationOnly =  new J.util.JmolList ();
this.SymmetryOnly =  new J.util.JmolList ();
this.SignedOnly =  new J.util.JmolList ();
this.AppletOnly =  new J.util.JmolList ();
this.ChargesOnly =  new J.util.JmolList ();
this.TemperatureOnly =  new J.util.JmolList ();
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "jpiDispose", 
function () {
this.menuClearListeners (this.popupMenu);
this.menuClearListeners (this.frankPopup);
this.popupMenu = this.frankPopup = this.thisPopup = null;
});
Clazz.overrideMethod (c$, "jpiGetMenuAsObject", 
function () {
return this.popupMenu;
});
Clazz.overrideMethod (c$, "jpiGetMenuAsString", 
function (title) {
this.updateForShow ();
var pt = title.indexOf ("|");
if (pt >= 0) {
var type = title.substring (pt);
title = title.substring (0, pt);
if (type.indexOf ("current") >= 0) {
var sb =  new J.util.SB ();
var menu = this.htMenus.get (this.menuName);
this.menuGetAsText (sb, 0, menu, "PopupMenu");
return sb.toString ();
}}return ( new J.popup.MainPopupResourceBundle (this.strMenuStructure, null)).getMenuAsText (title);
}, "~S");
Clazz.overrideMethod (c$, "jpiShow", 
function (x, y) {
if (!this.viewer.haveDisplay) return;
this.show (x, y, false);
if (x < 0) {
this.getViewerData ();
this.setFrankMenu (this.currentMenuItemId);
this.thisx = -x - 50;
if (this.nFrankList > 1) {
this.thisy = y - this.nFrankList * 20;
this.menuShowPopup (this.frankPopup, this.thisx, this.thisy);
return;
}}this.restorePopupMenu ();
this.menuShowPopup (this.popupMenu, this.thisx, this.thisy);
}, "~N,~N");
Clazz.overrideMethod (c$, "jpiUpdateComputedMenus", 
function () {
if (this.updateMode == -1) return;
this.updateMode = 0;
this.getViewerData ();
this.updateSelectMenu ();
this.updateFileMenu ();
this.updateElementsComputedMenu (this.viewer.getElementsPresentBitSet (this.modelIndex));
this.updateHeteroComputedMenu (this.viewer.getHeteroList (this.modelIndex));
this.updateSurfMoComputedMenu (this.modelInfo.get ("moData"));
this.updateFileTypeDependentMenus ();
this.updatePDBComputedMenus ();
this.updateMode = 1;
this.updateConfigurationComputedMenu ();
this.updateSYMMETRYComputedMenus ();
this.updateFRAMESbyModelComputedMenu ();
this.updateModelSetComputedMenu ();
this.updateLanguageSubmenu ();
this.updateAboutSubmenu ();
});
$_M(c$, "getEntryIcon", 
function (ret) {
var entry = ret[0];
if (!entry.startsWith ("<")) return null;
var pt = entry.indexOf (">");
ret[0] = entry.substring (pt + 1);
var fileName = entry.substring (1, pt);
return this.getImageIcon (fileName);
}, "~A");
$_M(c$, "getImageIcon", 
function (fileName) {
return null;
}, "~S");
$_M(c$, "checkMenuFocus", 
function (name, cmd, isFocus) {
if (name.indexOf ("Focus") < 0) return;
if (isFocus) {
this.viewer.script ("selectionHalos ON;" + cmd);
} else {
this.viewer.script ("selectionHalos OFF");
}}, "~S,~S,~B");
$_M(c$, "checkBoxStateChanged", 
function (source) {
this.restorePopupMenu ();
this.menuSetCheckBoxValue (source);
var id = this.menuGetId (source);
if (id != null) {
this.currentMenuItemId = id;
}}, "~O");
c$.addItemText = $_M(c$, "addItemText", 
function (sb, type, level, name, label, script, flags) {
sb.appendC (type).appendI (level).appendC ('\t').append (name);
if (label == null) {
sb.append (".\n");
return;
}sb.append ("\t").append (label).append ("\t").append (script == null || script.length == 0 ? "-" : script).append ("\t").append (flags).append ("\n");
}, "J.util.SB,~S,~N,~S,~S,~S,~S");
$_M(c$, "fixScript", 
function (id, script) {
var pt;
if (script === "" || id.endsWith ("Checkbox")) return script;
if (script.indexOf ("SELECT") == 0) {
return "select thisModel and (" + script.substring (6) + ")";
}if ((pt = id.lastIndexOf ("[")) >= 0) {
id = id.substring (pt + 1);
if ((pt = id.indexOf ("]")) >= 0) id = id.substring (0, pt);
id = id.$replace ('_', ' ');
if (script.indexOf ("[]") < 0) script = "[] " + script;
return J.util.TextFormat.simpleReplace (script, "[]", id);
} else if (script.indexOf ("?FILEROOT?") >= 0) {
script = J.util.TextFormat.simpleReplace (script, "FILEROOT?", this.modelSetRoot);
} else if (script.indexOf ("?FILE?") >= 0) {
script = J.util.TextFormat.simpleReplace (script, "FILE?", this.modelSetFileName);
} else if (script.indexOf ("?PdbId?") >= 0) {
script = J.util.TextFormat.simpleReplace (script, "PdbId?", "=xxxx");
}return script;
}, "~S,~S");
$_M(c$, "initialize", 
function (viewer, bundle, title) {
this.viewer = viewer;
this.menuName = title;
this.popupMenu = this.menuCreatePopup (title);
this.thisPopup = this.popupMenu;
this.menuSetListeners ();
this.htMenus.put (title, this.popupMenu);
this.isJS = viewer.isJS;
this.allowSignedFeatures = (!viewer.isApplet () || viewer.getBooleanProperty ("_signedApplet"));
this.addMenuItems ("", title, this.popupMenu, bundle);
try {
this.jpiUpdateComputedMenus ();
} catch (e) {
if (Clazz.exceptionOf (e, NullPointerException)) {
} else {
throw e;
}
}
}, "J.viewer.Viewer,J.popup.PopupResource,~S");
$_M(c$, "restorePopupMenu", 
function () {
this.thisPopup = this.popupMenu;
if (this.nFrankList < 2) return;
for (var i = this.nFrankList; --i > 0; ) {
var f = this.frankList[i];
{
f[1].parent = f[0];
}}
this.nFrankList = 1;
});
$_M(c$, "setCheckBoxValue", 
function (item, what, TF) {
this.checkForCheckBoxScript (item, what, TF);
if (what.indexOf ("#CONFIG") >= 0) {
this.configurationSelected = what;
this.updateConfigurationComputedMenu ();
this.updateModelSetComputedMenu ();
return;
}}, "~O,~S,~B");
c$.checkBoolean = $_M(c$, "checkBoolean", 
($fz = function (info, key) {
return (info != null && info.get (key) === Boolean.TRUE);
}, $fz.isPrivate = true, $fz), "java.util.Map,~S");
$_M(c$, "getViewerData", 
($fz = function () {
this.isApplet = this.viewer.isApplet ();
this.isSigned = (this.viewer.getBooleanProperty ("_signedApplet"));
this.modelSetName = this.viewer.getModelSetName ();
this.modelSetFileName = this.viewer.getModelSetFileName ();
var i = this.modelSetFileName.lastIndexOf (".");
this.isZapped = ("zapped".equals (this.modelSetName));
if (this.isZapped || "string".equals (this.modelSetFileName) || "files".equals (this.modelSetFileName) || "string[]".equals (this.modelSetFileName)) this.modelSetFileName = "";
this.modelSetRoot = this.modelSetFileName.substring (0, i < 0 ? this.modelSetFileName.length : i);
if (this.modelSetRoot.length == 0) this.modelSetRoot = "Jmol";
this.modelIndex = this.viewer.getDisplayModelIndex ();
this.modelCount = this.viewer.getModelCount ();
this.atomCount = this.viewer.getAtomCountInModel (this.modelIndex);
this.modelSetInfo = this.viewer.getModelSetAuxiliaryInfo ();
this.modelInfo = this.viewer.getModelAuxiliaryInfo (this.modelIndex);
if (this.modelInfo == null) this.modelInfo =  new java.util.Hashtable ();
this.isPDB = J.popup.GenericPopup.checkBoolean (this.modelSetInfo, "isPDB");
this.isMultiFrame = (this.modelCount > 1);
this.isSymmetry = J.popup.GenericPopup.checkBoolean (this.modelInfo, "hasSymmetry");
this.isUnitCell = this.modelInfo.containsKey ("notionalUnitcell");
this.fileHasUnitCell = (this.isPDB && this.isUnitCell || J.popup.GenericPopup.checkBoolean (this.modelInfo, "fileHasUnitCell"));
this.isLastFrame = (this.modelIndex == this.modelCount - 1);
this.altlocs = this.viewer.getAltLocListInModel (this.modelIndex);
this.isMultiConfiguration = (this.altlocs.length > 0);
this.isVibration = (this.viewer.modelHasVibrationVectors (this.modelIndex));
this.haveCharges = (this.viewer.havePartialCharges ());
this.haveBFactors = (this.viewer.getBooleanProperty ("haveBFactors"));
this.cnmrPeaks = this.modelInfo.get ("jdxAtomSelect_13CNMR");
this.hnmrPeaks = this.modelInfo.get ("jdxAtomSelect_1HNMR");
}, $fz.isPrivate = true, $fz));
$_M(c$, "updateFileTypeDependentMenus", 
($fz = function () {
for (var i = 0; i < this.NotPDB.size (); i++) this.menuEnable (this.NotPDB.get (i), !this.isPDB);

for (var i = 0; i < this.PDBOnly.size (); i++) this.menuEnable (this.PDBOnly.get (i), this.isPDB);

for (var i = 0; i < this.UnitcellOnly.size (); i++) this.menuEnable (this.UnitcellOnly.get (i), this.isUnitCell);

for (var i = 0; i < this.FileUnitOnly.size (); i++) this.menuEnable (this.FileUnitOnly.get (i), this.isUnitCell || this.fileHasUnitCell);

for (var i = 0; i < this.FileMolOnly.size (); i++) this.menuEnable (this.FileMolOnly.get (i), this.isUnitCell || this.fileHasUnitCell);

for (var i = 0; i < this.SingleModelOnly.size (); i++) this.menuEnable (this.SingleModelOnly.get (i), this.isLastFrame);

for (var i = 0; i < this.FramesOnly.size (); i++) this.menuEnable (this.FramesOnly.get (i), this.isMultiFrame);

for (var i = 0; i < this.VibrationOnly.size (); i++) this.menuEnable (this.VibrationOnly.get (i), this.isVibration);

for (var i = 0; i < this.SymmetryOnly.size (); i++) this.menuEnable (this.SymmetryOnly.get (i), this.isSymmetry && this.isUnitCell);

for (var i = 0; i < this.SignedOnly.size (); i++) this.menuEnable (this.SignedOnly.get (i), this.isSigned || !this.isApplet);

for (var i = 0; i < this.AppletOnly.size (); i++) this.menuEnable (this.AppletOnly.get (i), this.isApplet);

for (var i = 0; i < this.ChargesOnly.size (); i++) this.menuEnable (this.ChargesOnly.get (i), this.haveCharges);

for (var i = 0; i < this.TemperatureOnly.size (); i++) this.menuEnable (this.TemperatureOnly.get (i), this.haveBFactors);

}, $fz.isPrivate = true, $fz));
$_M(c$, "addMenuItems", 
($fz = function (parentId, key, menu, popupResourceBundle) {
var id = parentId + "." + key;
var value = popupResourceBundle.getStructure (key);
if (J.util.Logger.debugging) J.util.Logger.debug (id + " --- " + value);
if (value == null) {
this.menuCreateItem (menu, "#" + key, "", "");
return;
}var st =  new java.util.StringTokenizer (value);
var item;
while (value.indexOf ("@") >= 0) {
var s = "";
while (st.hasMoreTokens ()) s += " " + ((item = st.nextToken ()).startsWith ("@") ? popupResourceBundle.getStructure (item) : item);

value = s.substring (1);
st =  new java.util.StringTokenizer (value);
}
while (st.hasMoreTokens ()) {
item = st.nextToken ();
if (!this.checkKey (item)) continue;
var label = popupResourceBundle.getWord (item);
var newMenu = null;
var script = "";
var isCB = false;
if (label.equals ("null")) {
continue;
} else if (item.indexOf ("Menu") >= 0) {
if (item.indexOf ("more") < 0) this.buttonGroup = null;
var subMenu = this.menuNewSubMenu (label, id + "." + item);
this.menuAddSubMenu (menu, subMenu);
this.htMenus.put (item, subMenu);
if (item.indexOf ("Computed") < 0) this.addMenuItems (id, item, subMenu, popupResourceBundle);
this.checkSpecialMenu (item, subMenu, label);
newMenu = subMenu;
} else if ("-".equals (item)) {
this.menuAddSeparator (menu);
continue;
} else if (item.endsWith ("Checkbox") || (isCB = (item.endsWith ("CB") || item.endsWith ("RD")))) {
script = popupResourceBundle.getStructure (item);
var basename = item.substring (0, item.length - (!isCB ? 8 : 2));
var isRadio = (isCB && item.endsWith ("RD"));
if (script == null || script.length == 0 && !isRadio) script = "set " + basename + " T/F";
newMenu = this.menuCreateCheckboxItem (menu, label, basename + ":" + script, id + "." + item, false, isRadio);
this.rememberCheckbox (basename, newMenu);
if (isRadio) this.menuAddButtonGroup (newMenu);
} else {
script = popupResourceBundle.getStructure (item);
if (script == null) script = item;
if (!this.isJS && item.startsWith ("JS")) continue;
newMenu = this.menuCreateItem (menu, label, script, id + "." + item);
}if (!this.allowSignedFeatures && item.startsWith ("SIGNED")) this.menuEnable (newMenu, false);
if (item.indexOf ("VARIABLE") >= 0) this.htMenus.put (item, newMenu);
if (item.indexOf ("!PDB") >= 0) {
this.NotPDB.addLast (newMenu);
} else if (item.indexOf ("PDB") >= 0) {
this.PDBOnly.addLast (newMenu);
}if (item.indexOf ("URL") >= 0) {
this.AppletOnly.addLast (newMenu);
} else if (item.indexOf ("CHARGE") >= 0) {
this.ChargesOnly.addLast (newMenu);
} else if (item.indexOf ("BFACTORS") >= 0) {
this.TemperatureOnly.addLast (newMenu);
} else if (item.indexOf ("UNITCELL") >= 0) {
this.UnitcellOnly.addLast (newMenu);
} else if (item.indexOf ("FILEUNIT") >= 0) {
this.FileUnitOnly.addLast (newMenu);
} else if (item.indexOf ("FILEMOL") >= 0) {
this.FileMolOnly.addLast (newMenu);
}if (item.indexOf ("!FRAMES") >= 0) {
this.SingleModelOnly.addLast (newMenu);
} else if (item.indexOf ("FRAMES") >= 0) {
this.FramesOnly.addLast (newMenu);
}if (item.indexOf ("VIBRATION") >= 0) {
this.VibrationOnly.addLast (newMenu);
} else if (item.indexOf ("SYMMETRY") >= 0) {
this.SymmetryOnly.addLast (newMenu);
}if (item.startsWith ("SIGNED")) this.SignedOnly.addLast (newMenu);
if (false) {
var str = item.endsWith ("Menu") ? "----" : id + "." + item + "\t" + label + "\t" + this.fixScript (id + "." + item, script);
str = "addMenuItem('\t" + str + "\t')";
J.util.Logger.info (str);
}}
}, $fz.isPrivate = true, $fz), "~S,~S,~O,J.popup.PopupResource");
$_M(c$, "checkKey", 
($fz = function (key) {
{
return (key.indexOf("JAVA") < 0 && !(key.indexOf("NOGL") &&
this.viewer.isWebGL));
}}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "rememberCheckbox", 
($fz = function (key, checkboxMenuItem) {
this.htCheckbox.put (key + "::" + this.htCheckbox.size (), checkboxMenuItem);
}, $fz.isPrivate = true, $fz), "~S,~O");
$_M(c$, "checkForCheckBoxScript", 
($fz = function (item, what, TF) {
if (what.indexOf ("##") < 0) {
var pt = what.indexOf (":");
if (pt < 0) {
J.util.Logger.error ("check box " + item + " IS " + what);
return;
}var basename = what.substring (0, pt);
if (this.viewer.getBooleanProperty (basename) == TF) return;
if (basename.endsWith ("P!")) {
if (basename.indexOf ("??") >= 0) {
what = this.menuSetCheckBoxOption (item, basename, what);
} else {
if (!TF) return;
what = "set picking " + basename.substring (0, basename.length - 2);
}} else {
what = what.substring (pt + 1);
if ((pt = what.indexOf ("|")) >= 0) what = (TF ? what.substring (0, pt) : what.substring (pt + 1)).trim ();
what = J.util.TextFormat.simpleReplace (what, "T/F", (TF ? " TRUE" : " FALSE"));
}}this.viewer.evalStringQuiet (what);
}, $fz.isPrivate = true, $fz), "~O,~S,~B");
Clazz.overrideMethod (c$, "checkMenuClick", 
function (source, script) {
this.checkMenuClickGP (source, script);
}, "~O,~S");
$_M(c$, "checkMenuClickGP", 
function (source, script) {
this.restorePopupMenu ();
if (script == null || script.length == 0) return;
if (script.equals ("MAIN")) {
this.show (this.thisx, this.thisy, true);
return;
}var id = this.menuGetId (source);
if (id != null) {
script = this.fixScript (id, script);
this.currentMenuItemId = id;
}this.viewer.evalStringQuiet (script);
}, "~O,~S");
$_M(c$, "addMenuItem", 
($fz = function (menuItem, entry) {
return this.menuCreateItem (menuItem, entry, "", null);
}, $fz.isPrivate = true, $fz), "~O,~S");
$_M(c$, "checkSpecialMenu", 
($fz = function (item, subMenu, word) {
if ("aboutComputedMenu".equals (item)) {
this.aboutComputedMenuBaseCount = this.menuGetItemCount (subMenu);
} else if ("modelSetMenu".equals (item)) {
this.nullModelSetName = word;
this.menuEnable (subMenu, false);
}}, $fz.isPrivate = true, $fz), "~S,~O,~S");
$_M(c$, "updateFileMenu", 
($fz = function () {
var menu = this.htMenus.get ("fileMenu");
if (menu == null) return;
var text = this.getMenuText ("writeFileTextVARIABLE");
menu = this.htMenus.get ("writeFileTextVARIABLE");
if (this.modelSetFileName.equals ("zapped") || this.modelSetFileName.equals ("")) {
this.menuSetLabel (menu, J.i18n.GT._ ("No atoms loaded"));
this.menuEnableItem (menu, false);
} else {
this.menuSetLabel (menu, J.i18n.GT._ (text, this.modelSetFileName));
this.menuEnableItem (menu, true);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "getMenuText", 
($fz = function (key) {
var str = this.menuText.getProperty (key);
return (str == null ? key : str);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "updateSelectMenu", 
($fz = function () {
var menu = this.htMenus.get ("selectMenuText");
if (menu == null) return;
this.menuEnable (menu, this.atomCount != 0);
this.menuSetLabel (menu, J.i18n.GT._ (this.getMenuText ("selectMenuText"), this.viewer.getSelectionCount ()));
}, $fz.isPrivate = true, $fz));
$_M(c$, "updateElementsComputedMenu", 
($fz = function (elementsPresentBitSet) {
var menu = this.htMenus.get ("elementsComputedMenu");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
this.menuEnable (menu, false);
if (elementsPresentBitSet == null) return;
for (var i = elementsPresentBitSet.nextSetBit (0); i >= 0; i = elementsPresentBitSet.nextSetBit (i + 1)) {
var elementName = J.util.Elements.elementNameFromNumber (i);
var elementSymbol = J.util.Elements.elementSymbolFromNumber (i);
var entryName = elementSymbol + " - " + elementName;
this.menuCreateItem (menu, entryName, "SELECT " + elementName, null);
}
for (var i = 4; i < J.util.Elements.altElementMax; ++i) {
var n = J.util.Elements.elementNumberMax + i;
if (elementsPresentBitSet.get (n)) {
n = J.util.Elements.altElementNumberFromIndex (i);
var elementName = J.util.Elements.elementNameFromNumber (n);
var elementSymbol = J.util.Elements.elementSymbolFromNumber (n);
var entryName = elementSymbol + " - " + elementName;
this.menuCreateItem (menu, entryName, "SELECT " + elementName, null);
}}
this.menuEnable (menu, true);
}, $fz.isPrivate = true, $fz), "J.util.BS");
$_M(c$, "updateSpectraMenu", 
($fz = function () {
var menuh = this.htMenus.get ("hnmrMenu");
var menuc = this.htMenus.get ("cnmrMenu");
if (menuh != null) this.menuRemoveAll (menuh, 0);
if (menuc != null) this.menuRemoveAll (menuc, 0);
var menu = this.htMenus.get ("spectraMenu");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
var isOK =  new Boolean (this.setSpectraMenu (menuh, this.hnmrPeaks) | this.setSpectraMenu (menuc, this.cnmrPeaks)).valueOf ();
if (isOK) {
if (menuh != null) this.menuAddSubMenu (menu, menuh);
if (menuc != null) this.menuAddSubMenu (menu, menuc);
}this.menuEnable (menu, isOK);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setSpectraMenu", 
($fz = function (menu, peaks) {
if (menu == null) return false;
this.menuEnable (menu, false);
var n = (peaks == null ? 0 : peaks.size ());
if (n == 0) return false;
for (var i = 0; i < n; i++) {
var peak = peaks.get (i);
var title = J.util.Parser.getQuotedAttribute (peak, "title");
var atoms = J.util.Parser.getQuotedAttribute (peak, "atoms");
if (atoms != null) this.menuCreateItem (menu, title, "select visible & (@" + J.util.TextFormat.simpleReplace (atoms, ",", " or @") + ")", "Focus" + i);
}
this.menuEnable (menu, true);
return true;
}, $fz.isPrivate = true, $fz), "~O,J.util.JmolList");
$_M(c$, "updateHeteroComputedMenu", 
($fz = function (htHetero) {
var menu = this.htMenus.get ("PDBheteroComputedMenu");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
this.menuEnable (menu, false);
if (htHetero == null) return;
var n = 0;
for (var hetero, $hetero = htHetero.entrySet ().iterator (); $hetero.hasNext () && ((hetero = $hetero.next ()) || true);) {
var heteroCode = hetero.getKey ();
var heteroName = hetero.getValue ();
if (heteroName.length > 20) heteroName = heteroName.substring (0, 20) + "...";
var entryName = heteroCode + " - " + heteroName;
this.menuCreateItem (menu, entryName, "SELECT [" + heteroCode + "]", null);
n++;
}
this.menuEnable (menu, (n > 0));
}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "updateSurfMoComputedMenu", 
($fz = function (moData) {
var menu = this.htMenus.get ("surfMoComputedMenuText");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
var mos = (moData == null ? null : (moData.get ("mos")));
var nOrb = (mos == null ? 0 : mos.size ());
var text = this.getMenuText ("surfMoComputedMenuText");
if (nOrb == 0) {
this.menuSetLabel (menu, J.i18n.GT._ (text, ""));
this.menuEnable (menu, false);
return;
}this.menuSetLabel (menu, J.i18n.GT._ (text, nOrb));
this.menuEnable (menu, true);
var subMenu = menu;
var nmod = (nOrb % this.itemMax);
if (nmod == 0) nmod = this.itemMax;
var pt = (nOrb > this.itemMax ? 0 : -2147483648);
for (var i = nOrb; --i >= 0; ) {
if (pt >= 0 && (pt++ % nmod) == 0) {
if (pt == nmod + 1) nmod = this.itemMax;
var id = "mo" + pt + "Menu";
subMenu = this.menuNewSubMenu (Math.max (i + 2 - nmod, 1) + "..." + (i + 1), this.menuGetId (menu) + "." + id);
this.menuAddSubMenu (menu, subMenu);
this.htMenus.put (id, subMenu);
pt = 1;
}var mo = mos.get (i);
var entryName = "#" + (i + 1) + " " + (mo.containsKey ("type") ? mo.get ("type") + " " : "") + (mo.containsKey ("symmetry") ? mo.get ("symmetry") + " " : "") + (mo.containsKey ("energy") ? mo.get ("energy") : "");
var script = "mo " + (i + 1);
this.menuCreateItem (subMenu, entryName, script, null);
}
}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "updateSceneComputedMenu", 
($fz = function () {
var menu = this.htMenus.get ("sceneComputedMenu");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
this.menuEnable (menu, false);
var scenes = this.viewer.getSceneList ();
if (scenes == null) return;
for (var i = 0; i < scenes.length; i++) this.menuCreateItem (menu, scenes[i], "restore scene " + J.util.Escape.eS (scenes[i]) + " 1.0", null);

this.menuEnable (menu, true);
}, $fz.isPrivate = true, $fz));
$_M(c$, "updatePDBComputedMenus", 
($fz = function () {
var menu = this.htMenus.get ("PDBaaResiduesComputedMenu");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
this.menuEnable (menu, false);
var menu1 = this.htMenus.get ("PDBnucleicResiduesComputedMenu");
if (menu1 == null) return;
this.menuRemoveAll (menu1, 0);
this.menuEnable (menu1, false);
var menu2 = this.htMenus.get ("PDBcarboResiduesComputedMenu");
if (menu2 == null) return;
this.menuRemoveAll (menu2, 0);
this.menuEnable (menu2, false);
if (this.modelSetInfo == null) return;
var n = (this.modelIndex < 0 ? 0 : this.modelIndex + 1);
var lists = (this.modelSetInfo.get ("group3Lists"));
this.group3List = (lists == null ? null : lists[n]);
this.group3Counts = (lists == null ? null : (this.modelSetInfo.get ("group3Counts"))[n]);
if (this.group3List == null) return;
var nItems = 0;
for (var i = 1; i < 24; ++i) nItems += this.updateGroup3List (menu, J.viewer.JC.predefinedGroup3Names[i]);

nItems += this.augmentGroup3List (menu, "p>", true);
this.menuEnable (menu, (nItems > 0));
this.menuEnable (this.htMenus.get ("PDBproteinMenu"), (nItems > 0));
nItems = this.augmentGroup3List (menu1, "n>", false);
this.menuEnable (menu1, nItems > 0);
this.menuEnable (this.htMenus.get ("PDBnucleicMenu"), (nItems > 0));
nItems = this.augmentGroup3List (menu2, "c>", false);
this.menuEnable (menu2, nItems > 0);
this.menuEnable (this.htMenus.get ("PDBcarboMenu"), (nItems > 0));
}, $fz.isPrivate = true, $fz));
$_M(c$, "updateGroup3List", 
($fz = function (menu, name) {
var nItems = 0;
var n = this.group3Counts[Clazz.doubleToInt (this.group3List.indexOf (name) / 6)];
var script = null;
if (n > 0) {
script = "SELECT " + name;
name += "  (" + n + ")";
nItems++;
}var item = this.menuCreateItem (menu, name, script, this.menuGetId (menu) + "." + name);
if (n == 0) this.menuEnableItem (item, false);
return nItems;
}, $fz.isPrivate = true, $fz), "~O,~S");
$_M(c$, "augmentGroup3List", 
($fz = function (menu, type, addSeparator) {
var pt = 138;
var nItems = 0;
while (true) {
pt = this.group3List.indexOf (type, pt);
if (pt < 0) break;
if (nItems++ == 0 && addSeparator) this.menuAddSeparator (menu);
var n = this.group3Counts[Clazz.doubleToInt (pt / 6)];
var heteroCode = this.group3List.substring (pt + 2, pt + 5);
var name = heteroCode + "  (" + n + ")";
this.menuCreateItem (menu, name, "SELECT [" + heteroCode + "]", this.menuGetId (menu) + "." + name);
pt++;
}
return nItems;
}, $fz.isPrivate = true, $fz), "~O,~S,~B");
$_M(c$, "updateSYMMETRYComputedMenus", 
($fz = function () {
this.updateSYMMETRYSelectComputedMenu ();
this.updateSYMMETRYShowComputedMenu ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "updateSYMMETRYShowComputedMenu", 
($fz = function () {
var menu = this.htMenus.get ("SYMMETRYShowComputedMenu");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
this.menuEnable (menu, false);
if (!this.isSymmetry || this.modelIndex < 0) return;
var info = this.viewer.getProperty ("DATA_API", "spaceGroupInfo", null);
if (info == null) return;
var infolist = info.get ("operations");
if (infolist == null) return;
var name = info.get ("spaceGroupName");
this.menuSetLabel (menu, name == null ? J.i18n.GT._ ("Space Group") : name);
var subMenu = menu;
var nmod = this.itemMax;
var pt = (infolist.length > this.itemMax ? 0 : -2147483648);
for (var i = 0; i < infolist.length; i++) {
if (pt >= 0 && (pt++ % nmod) == 0) {
var id = "drawsymop" + pt + "Menu";
subMenu = this.menuNewSubMenu ((i + 1) + "..." + Math.min (i + this.itemMax, infolist.length), this.menuGetId (menu) + "." + id);
this.menuAddSubMenu (menu, subMenu);
this.htMenus.put (id, subMenu);
pt = 1;
}var sym = infolist[i][1];
if (sym.indexOf ("x1") < 0) sym = infolist[i][0];
var entryName = (i + 1) + " " + infolist[i][2] + " (" + sym + ")";
this.menuEnableItem (this.menuCreateItem (subMenu, entryName, "draw SYMOP " + (i + 1), null), true);
}
this.menuEnable (menu, true);
}, $fz.isPrivate = true, $fz));
$_M(c$, "updateSYMMETRYSelectComputedMenu", 
($fz = function () {
var menu = this.htMenus.get ("SYMMETRYSelectComputedMenu");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
this.menuEnable (menu, false);
if (!this.isSymmetry || this.modelIndex < 0) return;
var list = this.modelInfo.get ("symmetryOperations");
if (list == null) return;
var cellRange = this.modelInfo.get ("unitCellRange");
var haveUnitCellRange = (cellRange != null);
var subMenu = menu;
var nmod = this.itemMax;
var pt = (list.length > this.itemMax ? 0 : -2147483648);
for (var i = 0; i < list.length; i++) {
if (pt >= 0 && (pt++ % nmod) == 0) {
var id = "symop" + pt + "Menu";
subMenu = this.menuNewSubMenu ((i + 1) + "..." + Math.min (i + this.itemMax, list.length), this.menuGetId (menu) + "." + id);
this.menuAddSubMenu (menu, subMenu);
this.htMenus.put (id, subMenu);
pt = 1;
}var entryName = "symop=" + (i + 1) + " # " + list[i];
this.menuEnableItem (this.menuCreateItem (subMenu, entryName, "SELECT symop=" + (i + 1), null), haveUnitCellRange);
}
this.menuEnable (menu, true);
}, $fz.isPrivate = true, $fz));
$_M(c$, "updateFRAMESbyModelComputedMenu", 
($fz = function () {
var menu = this.htMenus.get ("FRAMESbyModelComputedMenu");
if (menu == null) return;
this.menuEnable (menu, (this.modelCount > 0));
this.menuSetLabel (menu, (this.modelIndex < 0 ? J.i18n.GT._ (this.getMenuText ("allModelsText"), this.modelCount) : this.getModelLabel ()));
this.menuRemoveAll (menu, 0);
if (this.modelCount < 1) return;
if (this.modelCount > 1) this.menuCreateCheckboxItem (menu, J.i18n.GT._ ("All"), "frame 0 ##", null, (this.modelIndex < 0), false);
var subMenu = menu;
var nmod = this.itemMax;
var pt = (this.modelCount > this.itemMax ? 0 : -2147483648);
for (var i = 0; i < this.modelCount; i++) {
if (pt >= 0 && (pt++ % nmod) == 0) {
var id = "model" + pt + "Menu";
subMenu = this.menuNewSubMenu ((i + 1) + "..." + Math.min (i + this.itemMax, this.modelCount), this.menuGetId (menu) + "." + id);
this.menuAddSubMenu (menu, subMenu);
this.htMenus.put (id, subMenu);
pt = 1;
}var script = "" + this.viewer.getModelNumberDotted (i);
var entryName = this.viewer.getModelName (i);
var spectrumTypes = this.viewer.getModelAuxiliaryInfoValue (i, "spectrumTypes");
if (spectrumTypes != null && entryName.startsWith (spectrumTypes)) spectrumTypes = null;
if (!entryName.equals (script)) {
var ipt = entryName.indexOf (";PATH");
if (ipt >= 0) entryName = entryName.substring (0, ipt);
if (entryName.indexOf ("Model[") == 0 && (ipt = entryName.indexOf ("]:")) >= 0) entryName = entryName.substring (ipt + 2);
entryName = script + ": " + entryName;
}if (entryName.length > 60) entryName = entryName.substring (0, 55) + "...";
if (spectrumTypes != null) entryName += " (" + spectrumTypes + ")";
this.menuCreateCheckboxItem (subMenu, entryName, "model " + script + " ##", null, (this.modelIndex == i), false);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "updateConfigurationComputedMenu", 
($fz = function () {
var menu = this.htMenus.get ("configurationComputedMenu");
if (menu == null) return;
this.menuEnable (menu, this.isMultiConfiguration);
if (!this.isMultiConfiguration) return;
var nAltLocs = this.altlocs.length;
this.menuSetLabel (menu, J.i18n.GT._ (this.getMenuText ("configurationMenuText"), nAltLocs));
this.menuRemoveAll (menu, 0);
var script = "hide none ##CONFIG";
this.menuCreateCheckboxItem (menu, J.i18n.GT._ ("All"), script, null, (this.updateMode == 1 && this.configurationSelected.equals (script)), false);
for (var i = 0; i < nAltLocs; i++) {
script = "configuration " + (i + 1) + "; hide thisModel and not selected ##CONFIG";
var entryName = "" + (i + 1) + " -- \"" + this.altlocs.charAt (i) + "\"";
this.menuCreateCheckboxItem (menu, entryName, script, null, (this.updateMode == 1 && this.configurationSelected.equals (script)), false);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "updateModelSetComputedMenu", 
($fz = function () {
var menu = this.htMenus.get ("modelSetMenu");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
this.menuSetLabel (menu, this.nullModelSetName);
this.menuEnable (menu, false);
this.menuEnable (this.htMenus.get ("surfaceMenu"), !this.isZapped);
this.menuEnable (this.htMenus.get ("measureMenu"), !this.isZapped);
this.menuEnable (this.htMenus.get ("pickingMenu"), !this.isZapped);
this.menuEnable (this.htMenus.get ("computationMenu"), !this.isZapped);
if (this.modelSetName == null || this.isZapped) return;
if (this.isMultiFrame) {
this.modelSetName = J.i18n.GT._ (this.getMenuText ("modelSetCollectionText"), this.modelCount);
if (this.modelSetName.length > this.titleWidthMax) this.modelSetName = this.modelSetName.substring (0, this.titleWidthMax) + "...";
} else if (this.viewer.getBooleanProperty ("hideNameInPopup")) {
this.modelSetName = this.getMenuText ("hiddenModelSetText");
} else if (this.modelSetName.length > this.titleWidthMax) {
this.modelSetName = this.modelSetName.substring (0, this.titleWidthMax) + "...";
}this.menuSetLabel (menu, this.modelSetName);
this.menuEnable (menu, true);
this.menuEnable (this.htMenus.get ("computationMenu"), this.atomCount <= 100);
this.addMenuItem (menu, J.i18n.GT._ (this.getMenuText ("atomsText"), this.atomCount));
this.addMenuItem (menu, J.i18n.GT._ (this.getMenuText ("bondsText"), this.viewer.getBondCountInModel (this.modelIndex)));
if (this.isPDB) {
this.menuAddSeparator (menu);
this.addMenuItem (menu, J.i18n.GT._ (this.getMenuText ("groupsText"), this.viewer.getGroupCountInModel (this.modelIndex)));
this.addMenuItem (menu, J.i18n.GT._ (this.getMenuText ("chainsText"), this.viewer.getChainCountInModel (this.modelIndex)));
this.addMenuItem (menu, J.i18n.GT._ (this.getMenuText ("polymersText"), this.viewer.getPolymerCountInModel (this.modelIndex)));
var submenu = this.htMenus.get ("BiomoleculesMenu");
if (submenu == null) {
submenu = this.menuNewSubMenu (J.i18n.GT._ (this.getMenuText ("biomoleculesMenuText")), this.menuGetId (menu) + ".biomolecules");
this.menuAddSubMenu (menu, submenu);
}this.menuRemoveAll (submenu, 0);
this.menuEnable (submenu, false);
var biomolecules;
if (this.modelIndex >= 0 && (biomolecules = this.viewer.getModelAuxiliaryInfoValue (this.modelIndex, "biomolecules")) != null) {
this.menuEnable (submenu, true);
var nBiomolecules = biomolecules.size ();
for (var i = 0; i < nBiomolecules; i++) {
var script = (this.isMultiFrame ? "" : "save orientation;load \"\" FILTER \"biomolecule " + (i + 1) + "\";restore orientation;");
var nAtoms = (biomolecules.get (i).get ("atomCount")).intValue ();
var entryName = J.i18n.GT._ (this.getMenuText (this.isMultiFrame ? "biomoleculeText" : "loadBiomoleculeText"), [Integer.$valueOf (i + 1), Integer.$valueOf (nAtoms)]);
this.menuCreateItem (submenu, entryName, script, null);
}
}}if (this.isApplet && !this.viewer.getBooleanProperty ("hideNameInPopup")) {
this.menuAddSeparator (menu);
this.menuCreateItem (menu, J.i18n.GT._ (this.getMenuText ("viewMenuText"), this.modelSetFileName), "show url", null);
}}, $fz.isPrivate = true, $fz));
$_M(c$, "getModelLabel", 
($fz = function () {
return J.i18n.GT._ (this.getMenuText ("modelMenuText"), (this.modelIndex + 1) + "/" + this.modelCount);
}, $fz.isPrivate = true, $fz));
$_M(c$, "updateAboutSubmenu", 
($fz = function () {
var menu = this.htMenus.get ("aboutComputedMenu");
if (menu == null) return;
this.menuRemoveAll (menu, this.aboutComputedMenuBaseCount);
var subMenu = this.menuNewSubMenu ("About molecule", "modelSetMenu");
this.menuAddSubMenu (menu, subMenu);
this.htMenus.put ("modelSetMenu", subMenu);
this.updateModelSetComputedMenu ();
subMenu = this.menuNewSubMenu ("Jmol " + J.viewer.JC.version + (this.isSigned ? " (signed)" : ""), "aboutJmolMenu");
this.menuAddSubMenu (menu, subMenu);
this.htMenus.put ("aboutJmolMenu", subMenu);
this.addMenuItem (subMenu, J.viewer.JC.date);
this.menuCreateItem (subMenu, "http://www.jmol.org", "show url \"http://www.jmol.org\"", null);
this.menuCreateItem (subMenu, J.i18n.GT._ ("Mouse Manual"), "show url \"http://wiki.jmol.org/index.php/Mouse_Manual\"", null);
this.menuCreateItem (subMenu, J.i18n.GT._ ("Translations"), "show url \"http://wiki.jmol.org/index.php/Internationalisation\"", null);
subMenu = this.menuNewSubMenu (J.i18n.GT._ ("System"), "systemMenu");
this.menuAddSubMenu (menu, subMenu);
this.htMenus.put ("systemMenu", subMenu);
this.addMenuItem (subMenu, this.viewer.getOperatingSystemName ());
this.menuAddSeparator (subMenu);
this.addMenuItem (subMenu, J.i18n.GT._ ("Java version:"));
this.addMenuItem (subMenu, this.viewer.getJavaVendor ());
this.addMenuItem (subMenu, this.viewer.getJavaVersion ());
var runtime = null;
{
}if (runtime != null) {
var availableProcessors = runtime.availableProcessors ();
if (availableProcessors > 0) this.addMenuItem (subMenu, (availableProcessors == 1) ? J.i18n.GT._ ("1 processor") : J.i18n.GT._ ("{0} processors", availableProcessors));
 else this.addMenuItem (subMenu, J.i18n.GT._ ("unknown processor count"));
this.addMenuItem (subMenu, J.i18n.GT._ ("Java memory usage:"));
var mbTotal = this.convertToMegabytes (runtime.totalMemory ());
var mbFree = this.convertToMegabytes (runtime.freeMemory ());
var mbMax = this.convertToMegabytes (runtime.maxMemory ());
this.addMenuItem (subMenu, J.i18n.GT._ ("{0} MB total", [ new Long (mbTotal)]));
this.addMenuItem (subMenu, J.i18n.GT._ ("{0} MB free", [ new Long (mbFree)]));
if (mbMax > 0) this.addMenuItem (subMenu, J.i18n.GT._ ("{0} MB maximum", [ new Long (mbMax)]));
 else this.addMenuItem (subMenu, J.i18n.GT._ ("unknown maximum"));
}}, $fz.isPrivate = true, $fz));
$_M(c$, "updateLanguageSubmenu", 
($fz = function () {
var menu = this.htMenus.get ("languageComputedMenu");
if (menu == null) return;
this.menuRemoveAll (menu, 0);
var language = J.i18n.GT.getLanguage ();
var id = this.menuGetId (menu);
var languages = J.i18n.GT.getLanguageList (null);
for (var i = 0, p = 0; i < languages.length; i++) {
if (language.equals (languages[i].code)) languages[i].display = true;
if (languages[i].display) {
var code = languages[i].code;
var name = languages[i].language;
var nativeName = languages[i].nativeLanguage;
var menuLabel = code + " - " + J.i18n.GT._ (name);
if ((nativeName != null) && (!nativeName.equals (J.i18n.GT._ (name)))) {
menuLabel += " - " + nativeName;
}if (p++ > 0 && (p % 4 == 1)) this.menuAddSeparator (menu);
this.menuCreateCheckboxItem (menu, menuLabel, "language = \"" + code + "\" ##" + name, id + "." + code, language.equals (code), false);
}}
}, $fz.isPrivate = true, $fz));
$_M(c$, "convertToMegabytes", 
($fz = function (num) {
if (num <= 9223372036854251519) num += 524288;
return Clazz.doubleToInt (num / (1048576));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "updateForShow", 
($fz = function () {
if (this.updateMode == -1) return;
this.getViewerData ();
this.updateMode = 2;
this.updateSelectMenu ();
this.updateSpectraMenu ();
this.updateFRAMESbyModelComputedMenu ();
this.updateSceneComputedMenu ();
this.updateModelSetComputedMenu ();
this.updateAboutSubmenu ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "setFrankMenu", 
($fz = function (id) {
if (this.currentFrankId != null && this.currentFrankId === id && this.nFrankList > 0) return;
if (this.frankPopup == null) this.frankPopup = this.menuCreatePopup ("Frank");
this.thisPopup = this.frankPopup;
this.menuRemoveAll (this.frankPopup, 0);
if (id == null) return;
this.currentFrankId = id;
this.nFrankList = 0;
this.frankList[this.nFrankList++] = [null, null, null];
this.menuCreateItem (this.frankPopup, this.getMenuText ("mainMenuText"), "MAIN", "");
for (var i = id.indexOf (".", 2) + 1; ; ) {
var iNew = id.indexOf (".", i);
if (iNew < 0) break;
var strMenu = id.substring (i, iNew);
var menu = this.htMenus.get (strMenu);
this.frankList[this.nFrankList++] = [this.menuGetParent (menu), menu, Integer.$valueOf (this.menuGetPosition (menu))];
this.menuAddSubMenu (this.frankPopup, menu);
i = iNew + 1;
}
this.thisPopup = this.popupMenu;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "show", 
($fz = function (x, y, doPopup) {
this.thisx = x;
this.thisy = y;
this.updateForShow ();
for (var entry, $entry = this.htCheckbox.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var key = entry.getKey ();
var item = entry.getValue ();
var basename = key.substring (0, key.indexOf (":"));
var b = this.viewer.getBooleanProperty (basename);
this.menuSetCheckBoxState (item, b);
}
if (doPopup) this.menuShowPopup (this.popupMenu, this.thisx, this.thisy);
}, $fz.isPrivate = true, $fz), "~N,~N,~B");
Clazz.defineStatics (c$,
"dumpList", false,
"UPDATE_NEVER", -1,
"UPDATE_ALL", 0,
"UPDATE_CONFIG", 1,
"UPDATE_SHOW", 2,
"MENUITEM_HEIGHT", 20);
});
