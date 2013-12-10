Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.AtomShape", "java.util.Hashtable"], "J.shape.Labels", ["J.constant.EnumPalette", "J.modelset.LabelToken", "$.Object2d", "$.Text", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.C", "$.JmolFont"], function () {
c$ = Clazz.decorateAsClass (function () {
this.strings = null;
this.formats = null;
this.bgcolixes = null;
this.fids = null;
this.offsets = null;
this.atomLabels = null;
this.text = null;
this.labelBoxes = null;
this.bsFontSet = null;
this.bsBgColixSet = null;
this.defaultOffset = 0;
this.defaultAlignment = 0;
this.defaultZPos = 0;
this.defaultFontId = 0;
this.defaultColix = 0;
this.defaultBgcolix = 0;
this.defaultPaletteID = 0;
this.defaultPointer = 0;
this.zeroFontId = 0;
this.defaultsOnlyForNone = true;
this.setDefaults = false;
this.isScaled = false;
this.scalePixelsPerMicron = 0;
this.pickedAtom = -1;
this.pickedOffset = 0;
this.pickedX = 0;
this.pickedY = 0;
Clazz.instantialize (this, arguments);
}, J.shape, "Labels", J.shape.AtomShape);
Clazz.prepareFields (c$, function () {
this.atomLabels =  new java.util.Hashtable ();
});
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shape.Labels, "initShape", []);
this.defaultFontId = this.zeroFontId = this.gdata.getFont3DFSS ("SansSerif", "Plain", 13).fid;
this.defaultColix = 0;
this.defaultBgcolix = 0;
this.defaultOffset = J.shape.Labels.zeroOffset;
this.defaultZPos = 0;
this.translucentAllowed = false;
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bsSelected) {
this.isActive = true;
if ("setDefaults" === propertyName) {
this.setDefaults = (value).booleanValue ();
return;
}if ("color" === propertyName) {
var pid = J.constant.EnumPalette.pidOf (value);
var colix = J.util.C.getColixO (value);
if (!this.setDefaults) for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setLabelColix (i, colix, pid);

if (this.setDefaults || !this.defaultsOnlyForNone) {
this.defaultColix = colix;
this.defaultPaletteID = pid;
}return;
}if ("scalereference" === propertyName) {
if (this.strings == null) return;
var val = (value).floatValue ();
var scalePixelsPerMicron = (val == 0 ? 0 : 10000 / val);
for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) {
if (this.strings.length <= i) continue;
this.text = this.getLabel (i);
if (this.text == null) {
this.text = J.modelset.Text.newLabel (this.gdata, null, this.strings[i], 0, 0, 0, scalePixelsPerMicron, null);
this.putLabel (i, this.text);
} else {
this.text.setScalePixelsPerMicron (scalePixelsPerMicron);
}}
return;
}if ("label" === propertyName) {
this.setScaling ();
var strLabel = value;
var tokens = (strLabel == null || strLabel.length == 0 ? J.shape.Labels.nullToken : [null]);
for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setLabel (tokens, strLabel, i);

return;
}if ("labels" === propertyName) {
this.setScaling ();
var labels = value;
for (var i = bsSelected.nextSetBit (0), pt = 0; i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) {
var strLabel = labels.get (pt++);
var tokens = (strLabel == null || strLabel.length == 0 ? J.shape.Labels.nullToken : [null]);
this.setLabel (tokens, strLabel, i);
}
return;
}if ("clearBoxes" === propertyName) {
this.labelBoxes = null;
return;
}if ("translucency" === propertyName || "bgtranslucency" === propertyName) {
return;
}if ("bgcolor" === propertyName) {
this.isActive = true;
if (this.bsBgColixSet == null) this.bsBgColixSet =  new J.util.BS ();
var bgcolix = J.util.C.getColixO (value);
if (!this.setDefaults) for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setBgcolix (i, bgcolix);

if (this.setDefaults || !this.defaultsOnlyForNone) this.defaultBgcolix = bgcolix;
return;
}if (this.bsFontSet == null) this.bsFontSet =  new J.util.BS ();
if ("textLabels" === propertyName) {
this.setScaling ();
var labels = value;
for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setTextLabel (i, labels.get (Integer.$valueOf (i)));

return;
}if ("fontsize" === propertyName) {
var fontsize = (value).intValue ();
if (fontsize < 0) {
this.fids = null;
return;
}var fid = this.gdata.getFontFid (fontsize);
if (!this.setDefaults) for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setFont (i, fid);

if (this.setDefaults || !this.defaultsOnlyForNone) this.defaultFontId = fid;
return;
}if ("font" === propertyName) {
var fid = (value).fid;
if (!this.setDefaults) for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setFont (i, fid);

if (this.setDefaults || !this.defaultsOnlyForNone) this.defaultFontId = fid;
return;
}if ("offset" === propertyName || "offsetexact" === propertyName) {
if (!(Clazz.instanceOf (value, Integer))) {
if (!this.setDefaults) for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setPymolOffset (i, value);

return;
}var offset = (value).intValue ();
var isExact = (propertyName === "offsetexact");
if (offset == 0) offset = 32767;
 else if (offset == J.shape.Labels.zeroOffset) offset = 0;
if (!this.setDefaults) for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setOffsets (i, offset, isExact);

if (this.setDefaults || !this.defaultsOnlyForNone) this.defaultOffset = offset;
return;
}if ("align" === propertyName) {
var type = value;
var alignment = 1;
if (type.equalsIgnoreCase ("right")) alignment = 3;
 else if (type.equalsIgnoreCase ("center")) alignment = 2;
for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setAlignment (i, alignment);

if (this.setDefaults || !this.defaultsOnlyForNone) this.defaultAlignment = alignment;
return;
}if ("pointer" === propertyName) {
var pointer = (value).intValue ();
if (!this.setDefaults) for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setPointer (i, pointer);

if (this.setDefaults || !this.defaultsOnlyForNone) this.defaultPointer = pointer;
return;
}if ("front" === propertyName) {
var TF = (value).booleanValue ();
if (!this.setDefaults) for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setFront (i, TF);

if (this.setDefaults || !this.defaultsOnlyForNone) this.defaultZPos = (TF ? 32 : 0);
return;
}if ("group" === propertyName) {
var TF = (value).booleanValue ();
if (!this.setDefaults) for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) this.setGroup (i, TF);

if (this.setDefaults || !this.defaultsOnlyForNone) this.defaultZPos = (TF ? 16 : 0);
return;
}if ("display" === propertyName || "toggleLabel" === propertyName) {
var mode = ("toggleLabel" === propertyName ? 0 : (value).booleanValue () ? 1 : -1);
if (this.mads == null) this.mads =  Clazz.newShortArray (this.atomCount, 0);
var strLabelPDB = null;
var tokensPDB = null;
var strLabelUNK = null;
var tokensUNK = null;
var strLabel;
var tokens;
for (var i = bsSelected.nextSetBit (0); i >= 0 && i < this.atomCount; i = bsSelected.nextSetBit (i + 1)) {
var atom = this.atoms[i];
if (this.formats == null || i >= this.formats.length) this.formats = J.util.ArrayUtil.ensureLengthS (this.formats, i + 1);
if (this.strings != null && this.strings.length > i && this.strings[i] != null) {
this.mads[i] = (mode == 0 && this.mads[i] < 0 || mode == 1 ? 1 : -1);
} else {
if (this.bsSizeSet == null) this.bsSizeSet =  new J.util.BS ();
this.strings = J.util.ArrayUtil.ensureLengthS (this.strings, i + 1);
if (atom.getGroup3 (false).equals ("UNK")) {
if (strLabelUNK == null) {
strLabelUNK = this.viewer.getStandardLabelFormat (1);
tokensUNK = J.modelset.LabelToken.compile (this.viewer, strLabelUNK, '\0', null);
}strLabel = strLabelUNK;
tokens = tokensUNK;
} else {
if (strLabelPDB == null) {
strLabelPDB = this.viewer.getStandardLabelFormat (2);
tokensPDB = J.modelset.LabelToken.compile (this.viewer, strLabelPDB, '\0', null);
}strLabel = strLabelPDB;
tokens = tokensPDB;
}this.strings[i] = J.modelset.LabelToken.formatLabelAtomArray (this.viewer, atom, tokens, '\0', null);
this.formats[i] = strLabel;
this.bsSizeSet.set (i);
if ((this.bsBgColixSet == null || !this.bsBgColixSet.get (i)) && this.defaultBgcolix != 0) this.setBgcolix (i, this.defaultBgcolix);
this.mads[i] = (mode >= 0 ? 1 : -1);
}atom.setShapeVisibility (this.myVisibilityFlag, this.strings != null && i < this.strings.length && this.strings[i] != null && this.mads[i] >= 0);
}
return;
}if (propertyName.startsWith ("label:")) {
this.setScaling ();
this.setLabel ( new Array (1), propertyName.substring (6), (value).intValue ());
return;
}if (propertyName === "deleteModelAtoms") {
this.labelBoxes = null;
var firstAtomDeleted = ((value)[2])[1];
var nAtomsDeleted = ((value)[2])[2];
this.fids = J.util.ArrayUtil.deleteElements (this.fids, firstAtomDeleted, nAtomsDeleted);
this.bgcolixes = J.util.ArrayUtil.deleteElements (this.bgcolixes, firstAtomDeleted, nAtomsDeleted);
this.offsets = J.util.ArrayUtil.deleteElements (this.offsets, firstAtomDeleted, nAtomsDeleted);
this.formats = J.util.ArrayUtil.deleteElements (this.formats, firstAtomDeleted, nAtomsDeleted);
this.strings = J.util.ArrayUtil.deleteElements (this.strings, firstAtomDeleted, nAtomsDeleted);
J.util.BSUtil.deleteBits (this.bsFontSet, bsSelected);
J.util.BSUtil.deleteBits (this.bsBgColixSet, bsSelected);
}this.setPropAS (propertyName, value, bsSelected);
}, "~S,~O,J.util.BS");
$_M(c$, "setPymolOffset", 
($fz = function (i, value) {
var text = this.getLabel (i);
if (text == null) {
var fid = (this.bsFontSet != null && this.bsFontSet.get (i) ? this.fids[i] : -1);
if (fid < 0) this.setFont (i, fid = this.defaultFontId);
var font = J.util.JmolFont.getFont3D (fid);
var colix = this.getColix2 (i, this.atoms[i], false);
text = J.modelset.Text.newLabel (this.gdata, font, this.strings[i], colix, this.getColix2 (i, this.atoms[i], true), 0, this.scalePixelsPerMicron, value);
this.setTextLabel (i, text);
} else {
text.pymolOffset = value;
}}, $fz.isPrivate = true, $fz), "~N,~A");
$_M(c$, "setScaling", 
($fz = function () {
this.isActive = true;
if (this.bsSizeSet == null) this.bsSizeSet =  new J.util.BS ();
this.isScaled = this.viewer.getBoolean (603979845);
this.scalePixelsPerMicron = (this.isScaled ? this.viewer.getScalePixelsPerAngstrom (false) * 10000 : 0);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setTextLabel", 
($fz = function (i, t) {
if (t == null) return;
var label = t.getText ();
var atom = this.atoms[i];
this.addString (atom, i, label, label);
atom.setShapeVisibility (this.myVisibilityFlag, true);
if (t.colix >= 0) this.setLabelColix (i, t.colix, J.constant.EnumPalette.UNKNOWN.id);
this.setFont (i, t.font.fid);
this.putLabel (i, t);
}, $fz.isPrivate = true, $fz), "~N,J.modelset.Text");
$_M(c$, "setLabel", 
($fz = function (temp, strLabel, i) {
var atom = this.atoms[i];
var tokens = temp[0];
if (tokens == null) tokens = temp[0] = J.modelset.LabelToken.compile (this.viewer, strLabel, '\0', null);
var label = (tokens == null ? null : J.modelset.LabelToken.formatLabelAtomArray (this.viewer, atom, tokens, '\0', null));
this.addString (atom, i, label, strLabel);
this.text = this.getLabel (i);
if (this.isScaled) {
this.text = J.modelset.Text.newLabel (this.gdata, null, label, 0, 0, 0, this.scalePixelsPerMicron, null);
this.putLabel (i, this.text);
} else if (this.text != null && label != null) {
this.text.setText (label);
}if (this.defaultOffset != J.shape.Labels.zeroOffset) this.setOffsets (i, this.defaultOffset, false);
if (this.defaultAlignment != 1) this.setAlignment (i, this.defaultAlignment);
if ((this.defaultZPos & 32) != 0) this.setFront (i, true);
 else if ((this.defaultZPos & 16) != 0) this.setGroup (i, true);
if (this.defaultPointer != 0) this.setPointer (i, this.defaultPointer);
if (this.defaultColix != 0 || this.defaultPaletteID != 0) this.setLabelColix (i, this.defaultColix, this.defaultPaletteID);
if (this.defaultBgcolix != 0) this.setBgcolix (i, this.defaultBgcolix);
if (this.defaultFontId != this.zeroFontId) this.setFont (i, this.defaultFontId);
}, $fz.isPrivate = true, $fz), "~A,~S,~N");
$_M(c$, "addString", 
($fz = function (atom, i, label, strLabel) {
atom.setShapeVisibility (this.myVisibilityFlag, label != null);
if (this.strings == null || i >= this.strings.length) this.strings = J.util.ArrayUtil.ensureLengthS (this.strings, i + 1);
if (this.formats == null || i >= this.formats.length) this.formats = J.util.ArrayUtil.ensureLengthS (this.formats, i + 1);
this.strings[i] = label;
this.formats[i] = (strLabel != null && strLabel.indexOf ("%{") >= 0 ? label : strLabel);
this.bsSizeSet.setBitTo (i, (strLabel != null));
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,~N,~S,~S");
Clazz.overrideMethod (c$, "getProperty", 
function (property, index) {
if (property.equals ("offsets")) return this.offsets;
if (property.equals ("label")) return (this.strings != null && index < this.strings.length && this.strings[index] != null ? this.strings[index] : "");
return null;
}, "~S,~N");
$_M(c$, "putLabel", 
function (i, text) {
if (text == null) this.atomLabels.remove (Integer.$valueOf (i));
 else this.atomLabels.put (Integer.$valueOf (i), text);
}, "~N,J.modelset.Text");
$_M(c$, "getLabel", 
function (i) {
return this.atomLabels.get (Integer.$valueOf (i));
}, "~N");
$_M(c$, "putBox", 
function (i, boxXY) {
if (this.labelBoxes == null) this.labelBoxes =  new java.util.Hashtable ();
this.labelBoxes.put (Integer.$valueOf (i), boxXY);
}, "~N,~A");
$_M(c$, "getBox", 
function (i) {
if (this.labelBoxes == null) return null;
return this.labelBoxes.get (Integer.$valueOf (i));
}, "~N");
$_M(c$, "setLabelColix", 
($fz = function (i, colix, pid) {
this.setColixAndPalette (colix, pid, i);
if (this.colixes != null && ((this.text = this.getLabel (i)) != null)) this.text.setColix (this.colixes[i]);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "setBgcolix", 
($fz = function (i, bgcolix) {
if (this.bgcolixes == null || i >= this.bgcolixes.length) {
if (bgcolix == 0) return;
this.bgcolixes = J.util.ArrayUtil.ensureLengthShort (this.bgcolixes, i + 1);
}this.bgcolixes[i] = bgcolix;
this.bsBgColixSet.setBitTo (i, bgcolix != 0);
this.text = this.getLabel (i);
if (this.text != null) this.text.setBgColix (bgcolix);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setOffsets", 
($fz = function (i, offset, isExact) {
if (this.offsets == null || i >= this.offsets.length) {
if (offset == 0) return;
this.offsets = J.util.ArrayUtil.ensureLengthI (this.offsets, i + 1);
}this.offsets[i] = (this.offsets[i] & 255) | (offset << 8);
if (isExact) this.offsets[i] |= 128;
this.text = this.getLabel (i);
if (this.text != null) this.text.setOffset (offset);
}, $fz.isPrivate = true, $fz), "~N,~N,~B");
$_M(c$, "setAlignment", 
($fz = function (i, alignment) {
if (this.offsets == null || i >= this.offsets.length) {
if (alignment == 1) return;
this.offsets = J.util.ArrayUtil.ensureLengthI (this.offsets, i + 1);
}this.offsets[i] = (this.offsets[i] & -13) | (alignment << 2);
this.text = this.getLabel (i);
if (this.text != null) this.text.setAlignment (alignment);
}, $fz.isPrivate = true, $fz), "~N,~N");
c$.getAlignment = $_M(c$, "getAlignment", 
function (offsetFull) {
return (offsetFull & 12) >> 2;
}, "~N");
$_M(c$, "setPointer", 
($fz = function (i, pointer) {
if (this.offsets == null || i >= this.offsets.length) {
if (pointer == 0) return;
this.offsets = J.util.ArrayUtil.ensureLengthI (this.offsets, i + 1);
}this.offsets[i] = (this.offsets[i] & -4) + pointer;
this.text = this.getLabel (i);
if (this.text != null) this.text.setPointer (pointer);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setFront", 
($fz = function (i, TF) {
if (this.offsets == null || i >= this.offsets.length) {
if (!TF) return;
this.offsets = J.util.ArrayUtil.ensureLengthI (this.offsets, i + 1);
}this.offsets[i] = (this.offsets[i] & -49) + (TF ? 32 : 0);
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "setGroup", 
($fz = function (i, TF) {
if (this.offsets == null || i >= this.offsets.length) {
if (!TF) return;
this.offsets = J.util.ArrayUtil.ensureLengthI (this.offsets, i + 1);
}this.offsets[i] = (this.offsets[i] & -49) + (TF ? 16 : 0);
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "setFont", 
($fz = function (i, fid) {
if (this.fids == null || i >= this.fids.length) {
if (fid == this.zeroFontId) return;
this.fids = J.util.ArrayUtil.ensureLengthByte (this.fids, i + 1);
}this.fids[i] = fid;
this.bsFontSet.set (i);
this.text = this.getLabel (i);
if (this.text != null) {
this.text.setFontFromFid (fid);
}}, $fz.isPrivate = true, $fz), "~N,~N");
Clazz.overrideMethod (c$, "setModelClickability", 
function () {
if (this.strings == null) return;
for (var i = this.strings.length; --i >= 0; ) {
var label = this.strings[i];
if (label != null && this.modelSet.atoms.length > i && !this.modelSet.isAtomHidden (i)) this.modelSet.atoms[i].setClickable (this.myVisibilityFlag);
}
});
Clazz.overrideMethod (c$, "getShapeState", 
function () {
if (!this.isActive || this.bsSizeSet == null) return "";
return this.viewer.getShapeState (this);
});
Clazz.overrideMethod (c$, "checkObjectDragged", 
function (prevX, prevY, x, y, dragAction, bsVisible) {
if (this.viewer.getPickingMode () != 2 || this.labelBoxes == null) return false;
if (prevX == -2147483648) {
var iAtom = this.findNearestLabel (x, y);
if (iAtom >= 0) {
this.pickedAtom = iAtom;
this.pickedX = x;
this.pickedY = y;
this.pickedOffset = (this.offsets == null || this.pickedAtom >= this.offsets.length ? 0 : this.offsets[this.pickedAtom]) >> 8;
return true;
}return false;
}if (prevX == 2147483647) {
this.pickedAtom = -1;
return false;
}if (this.pickedAtom < 0) return false;
this.move2D (this.pickedAtom, x, y);
return true;
}, "~N,~N,~N,~N,~N,J.util.BS");
$_M(c$, "findNearestLabel", 
($fz = function (x, y) {
if (this.labelBoxes == null) return -1;
var dmin = 3.4028235E38;
var imin = -1;
var zmin = 3.4028235E38;
for (var entry, $entry = this.labelBoxes.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
if (!this.atoms[entry.getKey ().intValue ()].isVisible (this.myVisibilityFlag)) continue;
var boxXY = entry.getValue ();
var dx = x - boxXY[0];
var dy = y - boxXY[1];
if (dx <= 0 || dy <= 0 || dx >= boxXY[2] || dy >= boxXY[3] || boxXY[4] > zmin) continue;
zmin = boxXY[4];
var d = Math.min (Math.abs (dx - boxXY[2] / 2), Math.abs (dy - boxXY[3] / 2));
if (d <= dmin) {
dmin = d;
imin = entry.getKey ().intValue ();
}}
return imin;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "move2D", 
($fz = function (pickedAtom, x, y) {
var xOffset = J.modelset.Object2d.getXOffset (this.pickedOffset);
var yOffset = -J.modelset.Object2d.getYOffset (this.pickedOffset);
xOffset += x - this.pickedX;
yOffset += this.pickedY - y;
var offset = J.modelset.Object2d.getOffset (xOffset, yOffset);
if (offset == 0) offset = 32767;
 else if (offset == J.shape.Labels.zeroOffset) offset = 0;
this.setOffsets (pickedAtom, offset, true);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "getColix2", 
function (i, atom, isBg) {
var colix;
if (isBg) {
colix = (this.bgcolixes == null || i >= this.bgcolixes.length) ? 0 : this.bgcolixes[i];
} else {
colix = (this.colixes == null || i >= this.colixes.length) ? 0 : this.colixes[i];
colix = J.util.C.getColixInherited (colix, atom.getColix ());
if (J.util.C.isColixTranslucent (colix)) colix = J.util.C.getColixTranslucent3 (colix, false, 0);
}return colix;
}, "~N,J.modelset.Atom,~B");
Clazz.defineStatics (c$,
"zeroOffset", 1028);
c$.nullToken = c$.prototype.nullToken = [null];
Clazz.defineStatics (c$,
"POINTER_FLAGS", 0x03,
"ALIGN_FLAGS", 0x0C,
"ZPOS_FLAGS", 0x30,
"GROUP_FLAG", 0x10,
"FRONT_FLAG", 0x20,
"SCALE_FLAG", 0x40,
"EXACT_OFFSET_FLAG", 0x80,
"FLAGS", 0xFF,
"FLAG_OFFSET", 8);
});
