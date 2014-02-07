Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.Shape"], "J.shape.MeshCollection", ["java.util.Hashtable", "J.script.T", "J.shape.Mesh", "J.util.ArrayUtil", "$.C", "$.Escape", "$.Logger", "$.P3", "$.SB", "$.TextFormat", "J.viewer.StateManager"], function () {
c$ = Clazz.decorateAsClass (function () {
this.meshCount = 0;
this.meshes = null;
this.currentMesh = null;
this.isFixed = false;
this.nUnnamed = 0;
this.colix = 0;
this.myType = null;
this.explicitID = false;
this.previousMeshID = null;
this.linkedMesh = null;
this.modelIndex = 0;
this.displayWithinDistance2 = 0;
this.isDisplayWithinNot = false;
this.displayWithinPoints = null;
this.bsDisplay = null;
this.title = null;
this.pickedMesh = null;
this.pickedModel = 0;
this.pickedVertex = 0;
this.pickedPt = null;
this.connections = null;
this.htObjects = null;
this.color = 0;
Clazz.instantialize (this, arguments);
}, J.shape, "MeshCollection", J.shape.Shape);
Clazz.prepareFields (c$, function () {
this.meshes =  new Array (4);
});
$_M(c$, "setMesh", 
($fz = function (thisID) {
this.linkedMesh = null;
if (thisID == null || J.util.TextFormat.isWild (thisID)) {
if (thisID != null) this.previousMeshID = thisID;
this.currentMesh = null;
return null;
}this.currentMesh = this.getMesh (thisID);
if (this.currentMesh == null) {
this.allocMesh (thisID, null);
} else if (thisID.equals ("+PREVIOUS_MESH+")) {
this.linkedMesh = this.currentMesh.linkedMesh;
}if (this.currentMesh.thisID == null) {
if (this.nUnnamed == 0 || this.getMesh (this.myType + this.nUnnamed) != null) this.nUnnamed++;
this.currentMesh.thisID = this.myType + this.nUnnamed;
if (this.htObjects != null) this.htObjects.put (this.currentMesh.thisID.toUpperCase (), this.currentMesh);
}this.previousMeshID = this.currentMesh.thisID;
return this.currentMesh;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "allocMesh", 
function (thisID, m) {
var index = this.meshCount++;
this.meshes = J.util.ArrayUtil.ensureLength (this.meshes, this.meshCount * 2);
this.currentMesh = this.meshes[index] = (m == null ?  new J.shape.Mesh ().mesh1 (thisID, this.colix, index) : m);
this.currentMesh.color = this.color;
this.currentMesh.index = index;
if (thisID != null && this.htObjects != null) this.htObjects.put (thisID.toUpperCase (), this.currentMesh);
this.previousMeshID = null;
}, "~S,J.shape.Mesh");
Clazz.overrideMethod (c$, "merge", 
function (shape) {
var mc = shape;
for (var i = 0; i < mc.meshCount; i++) {
if (mc.meshes[i] != null) {
var m = mc.meshes[i];
var m0 = this.getMesh (m.thisID);
if (m0 == null) {
this.allocMesh (m.thisID, m);
} else {
this.meshes[m0.index] = m;
m.index = m0.index;
}}}
this.previousMeshID = null;
this.currentMesh = null;
}, "J.shape.Shape");
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shape.MeshCollection, "initShape", []);
this.colix = 5;
this.color = 0xFFFFFFFF;
});
$_M(c$, "setPropMC", 
function (propertyName, value, bs) {
if ("init" === propertyName) {
this.title = null;
return;
}if ("link" === propertyName) {
if (this.meshCount >= 2 && this.currentMesh != null) this.currentMesh.linkedMesh = this.meshes[this.meshCount - 2];
return;
}if ("lattice" === propertyName) {
if (this.currentMesh != null) this.currentMesh.lattice = value;
return;
}if ("variables" === propertyName) {
if (this.currentMesh != null && this.currentMesh.scriptCommand != null && !this.currentMesh.scriptCommand.startsWith ("{")) this.currentMesh.scriptCommand = "{\n" + J.viewer.StateManager.getVariableList (value, 0, false, false) + "\n" + this.currentMesh.scriptCommand;
return;
}if ("thisID" === propertyName) {
var id = value;
this.setMesh (id);
this.checkExplicit (id);
return;
}if ("title" === propertyName) {
if (value == null) {
this.title = null;
} else if (Clazz.instanceOf (value, String)) {
var nLine = 1;
var lines = value;
for (var i = lines.length; --i >= 0; ) if (lines.charAt (i) == '|') nLine++;

this.title =  new Array (nLine);
nLine = 0;
var i0 = -1;
for (var i = 0; i < lines.length; i++) if (lines.charAt (i) == '|') {
this.title[nLine++] = lines.substring (i0 + 1, i);
i0 = i;
}
this.title[nLine] = lines.substring (i0 + 1);
} else {
this.title = value;
}return;
}if ("delete" === propertyName) {
this.deleteMesh ();
return;
}if ("reset" === propertyName) {
var thisID = value;
if (this.setMesh (thisID) == null) return;
this.setMesh (thisID);
return;
}if ("color" === propertyName) {
if (value == null) return;
this.colix = J.util.C.getColixO (value);
this.color = (value).intValue ();
if (this.currentMesh != null) {
this.currentMesh.color = this.color;
}this.setTokenProperty (1766856708, false, false);
return;
}if ("translucency" === propertyName) {
this.setTokenProperty (603979967, ((value).equals ("translucent")), false);
return;
}if ("hidden" === propertyName) {
value = Integer.$valueOf ((value).booleanValue () ? 1048588 : 1048589);
propertyName = "token";
}if ("token" === propertyName) {
var tok = (value).intValue ();
var tok2 = 0;
var test = true;
switch (tok) {
case 1610625028:
case 1048589:
case 1073741958:
case 1073741862:
case 1073741964:
case 1113198595:
case 1073741938:
case 1073742182:
case 1073741960:
break;
case 1048588:
test = false;
tok = 1048589;
break;
case 1073741898:
tok2 = 1073742018;
break;
case 1073742039:
test = false;
tok = 1073741898;
tok2 = 1073742018;
break;
case 1073742018:
tok2 = 1073741898;
break;
case 1073742052:
test = false;
tok = 1073742018;
tok2 = 1073741898;
break;
case 1073742042:
test = false;
tok = 1113198595;
break;
case 1073742046:
test = false;
tok = 1073741938;
break;
case 1073742060:
test = false;
tok = 1073742182;
break;
case 1073742058:
test = false;
tok = 1073741960;
break;
default:
J.util.Logger.error ("PROBLEM IN MESHCOLLECTION: token? " + J.script.T.nameOf (tok));
}
this.setTokenProperty (tok, test, false);
if (tok2 != 0) this.setTokenProperty (tok2, test, true);
return;
}this.setPropS (propertyName, value, bs);
}, "~S,~O,J.util.BS");
$_M(c$, "checkExplicit", 
function (id) {
if (this.explicitID) return;
this.explicitID = (id != null && !id.equals ("+PREVIOUS_MESH+"));
if (this.explicitID) this.previousMeshID = id;
}, "~S");
$_M(c$, "setTokenProperty", 
($fz = function (tokProp, bProp, testD) {
if (this.currentMesh == null) {
var key = (this.explicitID && this.previousMeshID != null && J.util.TextFormat.isWild (this.previousMeshID) ? this.previousMeshID.toUpperCase () : null);
if (key != null && key.length == 0) key = null;
for (var i = 0; i < this.meshCount; i++) if (key == null || J.util.TextFormat.isMatch (this.meshes[i].thisID.toUpperCase (), key, true, true)) this.setMeshTokenProperty (this.meshes[i], tokProp, bProp, testD);

} else {
this.setMeshTokenProperty (this.currentMesh, tokProp, bProp, testD);
if (this.linkedMesh != null) this.setMeshTokenProperty (this.linkedMesh, tokProp, bProp, testD);
}}, $fz.isPrivate = true, $fz), "~N,~B,~B");
$_M(c$, "setMeshTokenProperty", 
($fz = function (m, tokProp, bProp, testD) {
if (testD && (!m.havePlanarContours || m.drawTriangles == m.showContourLines)) return;
switch (tokProp) {
case 1610625028:
m.bsDisplay = this.bsDisplay;
if (this.bsDisplay == null && this.displayWithinPoints != null) m.setShowWithin (this.displayWithinPoints, this.displayWithinDistance2, this.isDisplayWithinNot);
return;
case 1048589:
m.visible = bProp;
return;
case 1766856708:
m.colix = this.colix;
return;
case 603979967:
m.setTranslucent (bProp, this.translucentLevel);
if (bProp && m.bsSlabGhost != null) m.resetSlab ();
if (m.bsTransPolygons != null) m.resetTransPolygons ();
return;
default:
m.setTokenProperty (tokProp, bProp);
}
}, $fz.isPrivate = true, $fz), "J.shape.Mesh,~N,~B,~B");
$_M(c$, "getPropDataMC", 
function (property, data) {
if (property === "getNames") {
var map = data[0];
var withDollar = (data[1]).booleanValue ();
for (var i = this.meshCount; --i >= 0; ) if (this.meshes[i] != null && this.meshes[i].vertexCount != 0) map.put ((withDollar ? "$" : "") + this.meshes[i].thisID, J.script.T.tokenOr);

return true;
}if (property === "getVertices") {
var m = this.getMesh (data[0]);
if (m == null) return false;
data[1] = m.vertices;
data[2] = m.getVisibleVertexBitSet ();
return true;
}if (property === "checkID") {
var key = (data[0]).toUpperCase ();
var isWild = J.util.TextFormat.isWild (key);
for (var i = this.meshCount; --i >= 0; ) {
var id = this.meshes[i].thisID;
if (id.equalsIgnoreCase (key) || isWild && J.util.TextFormat.isMatch (id.toUpperCase (), key, true, true)) {
data[1] = id;
return true;
}}
return false;
}if (property === "getCenter") {
var id = data[0];
var index = (data[1]).intValue ();
var m;
if ((m = this.getMesh (id)) == null || m.vertices == null) return false;
if (index == 2147483647) data[2] = J.util.P3.new3 (m.index + 1, this.meshCount, m.vertexCount);
 else data[2] = m.vertices[m.getVertexIndexFromNumber (index)];
return true;
}return false;
}, "~S,~A");
$_M(c$, "getPropMC", 
function (property) {
var m;
if (property === "count") {
var n = 0;
for (var i = 0; i < this.meshCount; i++) if ((m = this.meshes[i]) != null && m.vertexCount > 0) n++;

return Integer.$valueOf (n);
}if (property === "ID") return (this.currentMesh == null ? null : this.currentMesh.thisID);
if (property.startsWith ("list")) {
this.clean ();
var sb =  new J.util.SB ();
var k = 0;
var id = (property.equals ("list") ? null : property.substring (5));
for (var i = 0; i < this.meshCount; i++) {
m = this.meshes[i];
if (id != null && !id.equalsIgnoreCase (m.thisID)) continue;
sb.appendI ((++k)).append (" id:" + m.thisID).append ("; model:" + this.viewer.getModelNumberDotted (m.modelIndex)).append ("; vertices:" + m.vertexCount).append ("; polygons:" + m.polygonCount).append ("; visible:" + m.visible);
var range = this.getProperty ("dataRange", 0);
if (range != null) sb.append ("; dataRange:").append (J.util.Escape.eAF (range));
if (m.title != null) {
var s = "";
for (var j = 0; j < m.title.length; j++) s += (j == 0 ? "; title:" : " | ") + m.title[j];

if (s.length > 10000) s = s.substring (0, 10000) + "...";
sb.append (s);
}sb.appendC ('\n');
if (id != null) {
var info = this.getProperty ("jvxlFileInfo", 0);
if (info != null) sb.append (info).appendC ('\n');
}}
return sb.toString ();
}if (property === "vertices") return this.getVertices (this.currentMesh);
if (property === "getInfo") return (this.currentMesh == null ? null : this.currentMesh.getInfo (false));
if (property === "getData") return (this.currentMesh == null ? null : this.currentMesh.getInfo (true));
return null;
}, "~S");
$_M(c$, "getVertices", 
($fz = function (mesh) {
if (mesh == null) return null;
return mesh.vertices;
}, $fz.isPrivate = true, $fz), "J.shape.Mesh");
$_M(c$, "clean", 
function () {
for (var i = this.meshCount; --i >= 0; ) if (this.meshes[i] == null || this.meshes[i].vertexCount == 0) this.deleteMeshI (i);

});
$_M(c$, "deleteMesh", 
($fz = function () {
if (this.explicitID && this.currentMesh != null) this.deleteMeshI (this.currentMesh.index);
 else this.deleteMeshKey (this.explicitID && this.previousMeshID != null && J.util.TextFormat.isWild (this.previousMeshID) ? this.previousMeshID : null);
this.currentMesh = null;
}, $fz.isPrivate = true, $fz));
$_M(c$, "deleteMeshKey", 
function (key) {
if (key == null || key.length == 0) {
for (var i = this.meshCount; --i >= 0; ) this.meshes[i] = null;

this.meshCount = 0;
this.nUnnamed = 0;
if (this.htObjects != null) this.htObjects.clear ();
} else {
key = key.toLowerCase ();
for (var i = this.meshCount; --i >= 0; ) {
if (J.util.TextFormat.isMatch (this.meshes[i].thisID.toLowerCase (), key, true, true)) this.deleteMeshI (i);
}
}}, "~S");
$_M(c$, "deleteMeshI", 
function (i) {
if (this.htObjects != null) this.htObjects.remove (this.meshes[i].thisID.toUpperCase ());
for (var j = i + 1; j < this.meshCount; ++j) this.meshes[--this.meshes[j].index] = this.meshes[j];

this.meshes[--this.meshCount] = null;
}, "~N");
$_M(c$, "getMesh", 
function (thisID) {
var i = this.getIndexFromName (thisID);
return (i < 0 ? null : this.meshes[i]);
}, "~S");
Clazz.overrideMethod (c$, "getIndexFromName", 
function (thisID) {
if ("+PREVIOUS_MESH+".equals (thisID)) return (this.previousMeshID == null ? this.meshCount - 1 : this.getIndexFromName (this.previousMeshID));
if (J.util.TextFormat.isWild (thisID)) {
thisID = thisID.toLowerCase ();
for (var i = this.meshCount; --i >= 0; ) {
if (this.meshes[i] != null && J.util.TextFormat.isMatch (this.meshes[i].thisID, thisID, true, true)) return i;
}
} else {
if (this.htObjects != null) {
var m = this.htObjects.get (thisID.toUpperCase ());
return (m == null ? -1 : m.index);
}for (var i = this.meshCount; --i >= 0; ) {
if (this.meshes[i] != null && this.meshes[i].vertexCount != 0 && thisID.equalsIgnoreCase (this.meshes[i].thisID)) return i;
}
}return -1;
}, "~S");
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bs) {
var bsDeleted = this.viewer.getDeletedAtoms ();
for (var i = this.meshCount; --i >= 0; ) {
var mesh = this.meshes[i];
mesh.visibilityFlags = (mesh.visible && mesh.isValid && (mesh.modelIndex < 0 || bs.get (mesh.modelIndex) && (mesh.atomIndex < 0 || !this.modelSet.isAtomHidden (mesh.atomIndex) && !(bsDeleted != null && bsDeleted.get (mesh.atomIndex)))) ? this.myVisibilityFlag : 0);
}
}, "J.util.BS");
$_M(c$, "setStatusPicked", 
function (flag, v) {
this.viewer.setStatusAtomPicked (flag, "[\"" + this.myType + "\"," + J.util.Escape.eS (this.pickedMesh.thisID) + "," + +this.pickedModel + "," + this.pickedVertex + "," + v.x + "," + v.y + "," + v.z + "," + (this.pickedMesh.title == null ? "\"\"" : J.util.Escape.eS (this.pickedMesh.title[0])) + "]");
}, "~N,J.util.P3");
$_M(c$, "getPickedPoint", 
function (v, modelIndex) {
var map =  new java.util.Hashtable ();
if (v != null) {
map.put ("pt", v);
map.put ("modelIndex", Integer.$valueOf (modelIndex));
map.put ("model", this.viewer.getModelNumberDotted (modelIndex));
map.put ("id", this.pickedMesh.thisID);
map.put ("vertex", Integer.$valueOf (this.pickedVertex + 1));
map.put ("type", this.myType);
}return map;
}, "J.util.P3,~N");
Clazz.defineStatics (c$,
"PREVIOUS_MESH_ID", "+PREVIOUS_MESH+");
});
