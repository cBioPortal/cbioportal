Clazz.declarePackage ("J.io2");
c$ = Clazz.decorateAsClass (function () {
this.fm = null;
this.viewer = null;
this.aDOMNode = null;
this.atomSetCollection = null;
this.htParams = null;
Clazz.instantialize (this, arguments);
}, J.io2, "DOMReader");
Clazz.prepareFields (c$, function () {
this.aDOMNode =  new Array (1);
});
Clazz.makeConstructor (c$, 
function () {
});
$_M(c$, "set", 
function (fileManager, viewer, DOMNode, htParams) {
this.fm = fileManager;
this.viewer = viewer;
this.aDOMNode[0] = DOMNode;
this.htParams = htParams;
}, "J.viewer.FileManager,J.viewer.Viewer,~O,java.util.Map");
$_M(c$, "run", 
function () {
var info = this.viewer.apiPlatform.getJsObjectInfo (this.aDOMNode, null, null);
if (info != null) this.htParams.put ("nameSpaceInfo", info);
this.atomSetCollection = this.viewer.getModelAdapter ().getAtomSetCollectionFromDOM (this.aDOMNode, this.htParams);
if (Clazz.instanceOf (this.atomSetCollection, String)) return;
this.viewer.zap (false, true, false);
this.fm.fullPathName = this.fm.fileName = this.fm.nameAsGiven = "JSNode";
});
