Clazz.declarePackage ("J.modelset");
Clazz.load (["java.lang.Short", "java.util.Hashtable", "J.viewer.JC"], "J.modelset.Group", ["java.lang.Float", "J.constant.EnumStructure", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.Logger", "$.P3", "$.Quaternion", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.groupIndex = 0;
this.chain = null;
this.firstAtomIndex = -1;
this.leadAtomIndex = -1;
this.lastAtomIndex = 0;
this.seqcode = 0;
this.groupID = 0;
this.$isProtein = false;
this.selectedIndex = 0;
this.shapeVisibilityFlags = 0;
this.phi = NaN;
this.psi = NaN;
this.omega = NaN;
this.straightness = NaN;
this.mu = NaN;
this.theta = NaN;
this.bsAdded = null;
Clazz.instantialize (this, arguments);
}, J.modelset, "Group");
$_M(c$, "getGroupIndex", 
function () {
return this.groupIndex;
});
$_M(c$, "setGroupIndex", 
function (groupIndex) {
this.groupIndex = groupIndex;
}, "~N");
Clazz.makeConstructor (c$, 
function () {
});
$_M(c$, "setGroup", 
function (chain, group3, seqcode, firstAtomIndex, lastAtomIndex) {
this.chain = chain;
this.seqcode = seqcode;
if (group3 == null) group3 = "";
this.groupID = J.modelset.Group.getGroupIdFor (group3);
this.$isProtein = (this.groupID >= 1 && this.groupID < 24);
this.firstAtomIndex = firstAtomIndex;
this.lastAtomIndex = lastAtomIndex;
return this;
}, "J.modelset.Chain,~S,~N,~N,~N");
$_M(c$, "calcBioParameters", 
function () {
return false;
});
$_M(c$, "haveParameters", 
function () {
return true;
});
$_M(c$, "setGroupParameter", 
function (tok, f) {
switch (tok) {
case 1112539145:
this.phi = f;
break;
case 1112539146:
this.psi = f;
break;
case 1112539144:
this.omega = f;
break;
case 1112539141:
this.mu = f;
break;
case 1112539152:
this.theta = f;
break;
case 1112539150:
this.straightness = f;
break;
}
}, "~N,~N");
$_M(c$, "getGroupParameter", 
function (tok) {
if (!this.haveParameters ()) this.calcBioParameters ();
switch (tok) {
case 1112539144:
return this.omega;
case 1112539145:
return this.phi;
case 1112539146:
return this.psi;
case 1112539141:
return this.mu;
case 1112539152:
return this.theta;
case 1112539150:
return this.straightness;
}
return NaN;
}, "~N");
$_M(c$, "setModelSet", 
function (modelSet) {
this.chain.model.modelSet = modelSet;
}, "J.modelset.ModelSet");
$_M(c$, "setShapeVisibility", 
function (visFlag, isVisible) {
if (isVisible) {
this.shapeVisibilityFlags |= visFlag;
} else {
this.shapeVisibilityFlags &= ~visFlag;
}}, "~N,~B");
$_M(c$, "getGroup3", 
function () {
return J.modelset.Group.group3Names[this.groupID];
});
c$.getGroup3For = $_M(c$, "getGroup3For", 
function (groupID) {
return J.modelset.Group.group3Names[groupID];
}, "~N");
$_M(c$, "getGroup1", 
function () {
if (this.groupID >= J.viewer.JC.predefinedGroup1Names.length) return '?';
return J.viewer.JC.predefinedGroup1Names[this.groupID];
});
$_M(c$, "getGroupID", 
function () {
return this.groupID;
});
$_M(c$, "getChainID", 
function () {
return this.chain.chainID;
});
$_M(c$, "getBioPolymerLength", 
function () {
return 0;
});
$_M(c$, "getMonomerIndex", 
function () {
return -1;
});
$_M(c$, "getGroups", 
function () {
return null;
});
$_M(c$, "getStructure", 
function () {
return null;
});
$_M(c$, "getStrucNo", 
function () {
return 0;
});
$_M(c$, "getProteinStructureType", 
function () {
return J.constant.EnumStructure.NOT;
});
$_M(c$, "getProteinStructureSubType", 
function () {
return this.getProteinStructureType ();
});
$_M(c$, "setProteinStructureType", 
function (type, monomerIndexCurrent) {
return -1;
}, "J.constant.EnumStructure,~N");
$_M(c$, "isProtein", 
function () {
return this.$isProtein;
});
$_M(c$, "isNucleic", 
function () {
return (this.groupID >= 24 && this.groupID < 42);
});
$_M(c$, "isDna", 
function () {
return false;
});
$_M(c$, "isRna", 
function () {
return false;
});
$_M(c$, "isPurine", 
function () {
return false;
});
$_M(c$, "isPyrimidine", 
function () {
return false;
});
$_M(c$, "isCarbohydrate", 
function () {
return false;
});
c$.addGroup3Name = $_M(c$, "addGroup3Name", 
function (group3) {
if (J.modelset.Group.group3NameCount == J.modelset.Group.group3Names.length) ($t$ = J.modelset.Group.group3Names = J.util.ArrayUtil.doubleLengthS (J.modelset.Group.group3Names), J.modelset.Group.prototype.group3Names = J.modelset.Group.group3Names, $t$);
var groupID = ($t$ = J.modelset.Group.group3NameCount ++, J.modelset.Group.prototype.group3NameCount = J.modelset.Group.group3NameCount, $t$);
J.modelset.Group.group3Names[groupID] = group3;
J.modelset.Group.htGroup.put (group3, Short.$valueOf (groupID));
return groupID;
}, "~S");
c$.getGroupIdFor = $_M(c$, "getGroupIdFor", 
function (group3) {
if (group3 == null) return -1;
var groupID = J.modelset.Group.lookupGroupID (group3);
return (groupID != -1) ? groupID : J.modelset.Group.addGroup3Name (group3);
}, "~S");
c$.lookupGroupID = $_M(c$, "lookupGroupID", 
function (group3) {
if (group3 != null) {
var boxedGroupID = J.modelset.Group.htGroup.get (group3);
if (boxedGroupID != null) return boxedGroupID.shortValue ();
}return -1;
}, "~S");
$_M(c$, "getResno", 
function () {
return (this.seqcode == -2147483648 ? 0 : this.seqcode >> 8);
});
c$.getSeqNumberFor = $_M(c$, "getSeqNumberFor", 
function (seqcode) {
return (J.modelset.Group.haveSequenceNumber (seqcode) ? seqcode >> 8 : 2147483647);
}, "~N");
c$.haveSequenceNumber = $_M(c$, "haveSequenceNumber", 
function (seqcode) {
return ((seqcode & 128) != 0);
}, "~N");
$_M(c$, "getSeqcodeString", 
function () {
return J.modelset.Group.getSeqcodeStringFor (this.seqcode);
});
c$.getSeqcodeFor = $_M(c$, "getSeqcodeFor", 
function (seqNo, insCode) {
if (seqNo == -2147483648) return seqNo;
if (!((insCode >= 'A' && insCode <= 'Z') || (insCode >= 'a' && insCode <= 'z') || (insCode >= '0' && insCode <= '9') || insCode == '?' || insCode == '*')) {
if (insCode != ' ' && insCode != '\0') J.util.Logger.warn ("unrecognized insertionCode:" + insCode);
insCode = '\0';
}return ((seqNo == 2147483647 ? 0 : (seqNo << 8) | 128)) + insCode.charCodeAt (0);
}, "~N,~S");
c$.getSeqcodeStringFor = $_M(c$, "getSeqcodeStringFor", 
function (seqcode) {
if (seqcode == -2147483648) return null;
return (seqcode & 127) == 0 ? "" + (seqcode >> 8) : "" + (seqcode >> 8) + '^' + String.fromCharCode (seqcode & 127);
}, "~N");
$_M(c$, "getInsertionCode", 
function () {
if (this.seqcode == -2147483648) return '\0';
return String.fromCharCode (this.seqcode & 127);
});
c$.getInsertionCodeFor = $_M(c$, "getInsertionCodeFor", 
function (seqcode) {
return (seqcode & 127);
}, "~N");
c$.getInsertionCodeChar = $_M(c$, "getInsertionCodeChar", 
function (seqcode) {
return (seqcode == -2147483648 ? '\0' : String.fromCharCode (seqcode & 127));
}, "~N");
$_M(c$, "isAdded", 
function (atomIndex) {
return this.bsAdded != null && this.bsAdded.get (atomIndex);
}, "~N");
$_M(c$, "addAtoms", 
function (atomIndex) {
if (this.bsAdded == null) this.bsAdded =  new J.util.BS ();
this.bsAdded.set (atomIndex);
}, "~N");
$_M(c$, "selectAtoms", 
function (bs) {
bs.setBits (this.firstAtomIndex, this.lastAtomIndex + 1);
if (this.bsAdded != null) bs.or (this.bsAdded);
return this.lastAtomIndex;
}, "J.util.BS");
$_M(c$, "isSelected", 
function (bs) {
var pt = bs.nextSetBit (this.firstAtomIndex);
return (pt >= 0 && pt <= this.lastAtomIndex || this.bsAdded != null && this.bsAdded.intersects (bs));
}, "J.util.BS");
$_M(c$, "isHetero", 
function () {
return this.chain.getAtom (this.firstAtomIndex).isHetero ();
});
Clazz.overrideMethod (c$, "toString", 
function () {
return "[" + this.getGroup3 () + "-" + this.getSeqcodeString () + "]";
});
$_M(c$, "scaleToScreen", 
function (Z, mar) {
return this.chain.model.modelSet.viewer.scaleToScreen (Z, mar);
}, "~N,~N");
$_M(c$, "isCursorOnTopOf", 
function (atom, x, y, radius, champ) {
return this.chain.model.modelSet.isCursorOnTopOf (atom, x, y, radius, champ);
}, "J.modelset.Atom,~N,~N,~N,J.modelset.Atom");
$_M(c$, "isAtomHidden", 
function (atomIndex) {
return this.chain.model.modelSet.isAtomHidden (atomIndex);
}, "~N");
$_M(c$, "getModel", 
function () {
return this.chain.model;
});
$_M(c$, "getModelIndex", 
function () {
return this.chain.model.modelIndex;
});
$_M(c$, "getSelectedMonomerCount", 
function () {
return 0;
});
$_M(c$, "getSelectedMonomerIndex", 
function () {
return -1;
});
$_M(c$, "getSelectedGroupIndex", 
function () {
return this.selectedIndex;
});
$_M(c$, "isLeadAtom", 
function (atomIndex) {
return false;
}, "~N");
$_M(c$, "getLeadAtomOr", 
function (atom) {
var a = this.getLeadAtom ();
return (a == null ? atom : a);
}, "J.modelset.Atom");
$_M(c$, "getLeadAtom", 
function () {
return null;
});
$_M(c$, "getQuaternion", 
function (qType) {
return null;
}, "~S");
$_M(c$, "getQuaternionFrame", 
function (atoms) {
if (this.lastAtomIndex - this.firstAtomIndex < 3) return null;
var pt = this.firstAtomIndex;
return J.util.Quaternion.getQuaternionFrame (atoms[pt], atoms[++pt], atoms[++pt]);
}, "~A");
$_M(c$, "setStrucNo", 
function (i) {
}, "~N");
$_M(c$, "getHelixData", 
function (tokType, qType, mStep) {
switch (tokType) {
case 135266320:
return  new J.util.P3 ();
case 1073741854:
case 1666189314:
return  new J.util.V3 ();
case 135266305:
return Float.$valueOf (NaN);
case 135266306:
case 1073742001:
return [];
}
return "";
}, "~N,~S,~N");
$_M(c$, "isWithinStructure", 
function (type) {
return false;
}, "J.constant.EnumStructure");
$_M(c$, "getProteinStructureTag", 
function () {
return null;
});
$_M(c$, "getStructureId", 
function () {
return "";
});
$_M(c$, "getBioPolymerIndexInModel", 
function () {
return -1;
});
$_M(c$, "isCrossLinked", 
function (g) {
return false;
}, "J.modelset.Group");
$_M(c$, "getCrossLinkLead", 
function (vReturn) {
return false;
}, "J.util.JmolList");
$_M(c$, "isConnectedPrevious", 
function () {
return false;
});
$_M(c$, "getNitrogenAtom", 
function () {
return null;
});
$_M(c$, "getCarbonylOxygenAtom", 
function () {
return null;
});
$_M(c$, "fixIndices", 
function (atomsDeleted, bsDeleted) {
this.firstAtomIndex -= atomsDeleted;
this.leadAtomIndex -= atomsDeleted;
this.lastAtomIndex -= atomsDeleted;
if (this.bsAdded != null) J.util.BSUtil.deleteBits (this.bsAdded, bsDeleted);
}, "~N,J.util.BS");
$_M(c$, "getGroupInfo", 
function (igroup) {
var infoGroup =  new java.util.Hashtable ();
infoGroup.put ("groupIndex", Integer.$valueOf (igroup));
infoGroup.put ("groupID", Short.$valueOf (this.groupID));
var s = this.getSeqcodeString ();
if (s != null) infoGroup.put ("seqCode", s);
infoGroup.put ("_apt1", Integer.$valueOf (this.firstAtomIndex));
infoGroup.put ("_apt2", Integer.$valueOf (this.lastAtomIndex));
if (this.bsAdded != null) infoGroup.put ("addedAtoms", this.bsAdded);
infoGroup.put ("atomInfo1", this.chain.model.modelSet.getAtomInfo (this.firstAtomIndex, null));
infoGroup.put ("atomInfo2", this.chain.model.modelSet.getAtomInfo (this.lastAtomIndex, null));
infoGroup.put ("visibilityFlags", Integer.$valueOf (this.shapeVisibilityFlags));
return infoGroup;
}, "~N");
$_M(c$, "getMinZ", 
function (atoms, minZ) {
minZ[0] = 2147483647;
for (var i = this.firstAtomIndex; i <= this.lastAtomIndex; i++) this.checkMinZ (atoms[i], minZ);

if (this.bsAdded != null) for (var i = this.bsAdded.nextSetBit (0); i >= 0; i = this.bsAdded.nextSetBit (i + 1)) this.checkMinZ (atoms[i], minZ);

}, "~A,~A");
$_M(c$, "checkMinZ", 
($fz = function (atom, minZ) {
var z = atom.screenZ - Clazz.doubleToInt (atom.screenDiameter / 2) - 2;
if (z < minZ[0]) minZ[0] = Math.max (1, z);
}, $fz.isPrivate = true, $fz), "J.modelset.Atom,~A");
Clazz.defineStatics (c$,
"SEQUENCE_NUMBER_FLAG", 0x80,
"INSERTION_CODE_MASK", 0x7F,
"SEQUENCE_NUMBER_SHIFT", 8);
c$.htGroup = c$.prototype.htGroup =  new java.util.Hashtable ();
c$.group3Names = c$.prototype.group3Names =  new Array (128);
Clazz.defineStatics (c$,
"group3NameCount", 0);
{
for (var i = 0; i < J.viewer.JC.predefinedGroup3Names.length; ++i) {
J.modelset.Group.addGroup3Name (J.viewer.JC.predefinedGroup3Names[i]);
}
}});
