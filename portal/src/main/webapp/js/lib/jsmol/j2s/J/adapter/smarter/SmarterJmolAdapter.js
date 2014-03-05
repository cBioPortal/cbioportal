Clazz.declarePackage ("J.adapter.smarter");
Clazz.load (["J.api.JmolAdapter"], "J.adapter.smarter.SmarterJmolAdapter", ["J.adapter.smarter.AtomIterator", "$.AtomSetCollection", "$.BondIterator", "$.Resolver", "$.StructureIterator", "J.util.Logger"], function () {
c$ = Clazz.declareType (J.adapter.smarter, "SmarterJmolAdapter", J.api.JmolAdapter);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.adapter.smarter.SmarterJmolAdapter, ["SmarterJmolAdapter"]);
});
Clazz.overrideMethod (c$, "getFileTypeName", 
function (atomSetCollectionOrReader) {
if (Clazz.instanceOf (atomSetCollectionOrReader, J.adapter.smarter.AtomSetCollection)) return (atomSetCollectionOrReader).getFileTypeName ();
if (Clazz.instanceOf (atomSetCollectionOrReader, java.io.BufferedReader)) return J.adapter.smarter.Resolver.getFileType (atomSetCollectionOrReader);
return null;
}, "~O");
Clazz.overrideMethod (c$, "getAtomSetCollectionReader", 
function (name, type, bufferedReader, htParams) {
return J.adapter.smarter.SmarterJmolAdapter.staticGetAtomSetCollectionReader (name, type, bufferedReader, htParams);
}, "~S,~S,~O,java.util.Map");
c$.staticGetAtomSetCollectionReader = $_M(c$, "staticGetAtomSetCollectionReader", 
function (name, type, bufferedReader, htParams) {
try {
var ret = J.adapter.smarter.Resolver.getAtomCollectionReader (name, type, bufferedReader, htParams, -1);
if (Clazz.instanceOf (ret, String)) {
try {
J.adapter.smarter.SmarterJmolAdapter.close (bufferedReader);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
} else {
(ret).setup (name, htParams, bufferedReader);
}return ret;
} catch (e) {
try {
J.adapter.smarter.SmarterJmolAdapter.close (bufferedReader);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
} else {
throw ex;
}
}
bufferedReader = null;
J.util.Logger.error ("" + e);
return "" + e;
}
}, "~S,~S,~O,java.util.Map");
$_M(c$, "getAtomSetCollectionFromReader", 
function (fname, reader, htParams) {
var ret = J.adapter.smarter.Resolver.getAtomCollectionReader (fname, null, reader, htParams, -1);
if (Clazz.instanceOf (ret, J.adapter.smarter.AtomSetCollectionReader)) {
(ret).setup (fname, htParams, reader);
return (ret).readData ();
}return "" + ret;
}, "~S,~O,java.util.Map");
Clazz.overrideMethod (c$, "getAtomSetCollection", 
function (atomSetCollectionReader) {
return J.adapter.smarter.SmarterJmolAdapter.staticGetAtomSetCollection (atomSetCollectionReader);
}, "~O");
c$.staticGetAtomSetCollection = $_M(c$, "staticGetAtomSetCollection", 
function (a) {
var br = null;
try {
br = a.reader;
var ret = a.readData ();
if (!(Clazz.instanceOf (ret, J.adapter.smarter.AtomSetCollection))) return ret;
var atomSetCollection = ret;
if (atomSetCollection.errorMessage != null) return atomSetCollection.errorMessage;
return atomSetCollection;
} catch (e) {
try {
System.out.println (e.toString ());
} catch (ee) {
if (Clazz.exceptionOf (ee, Exception)) {
J.util.Logger.error (e.toString ());
} else {
throw ee;
}
}
try {
br.close ();
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
} else {
throw ex;
}
}
br = null;
J.util.Logger.error ("" + e);
return "" + e;
}
}, "J.adapter.smarter.AtomSetCollectionReader");
Clazz.overrideMethod (c$, "getAtomSetCollectionReaders", 
function (filesReader, names, types, htParams, getReadersOnly) {
var size = names.length;
var readers = (getReadersOnly ?  new Array (size) : null);
var atomsets = (getReadersOnly ? null :  new Array (size));
var r = null;
var viewer = htParams.get ("viewer");
for (var i = 0; i < size; i++) {
try {
if (r != null) htParams.put ("viewer", viewer);
var reader = filesReader.getBufferedReaderOrBinaryDocument (i, false);
if (!(Clazz.instanceOf (reader, java.io.BufferedReader) || Clazz.instanceOf (reader, J.api.JmolDocument))) return reader;
var ret = J.adapter.smarter.Resolver.getAtomCollectionReader (names[i], (types == null ? null : types[i]), reader, htParams, i);
if (!(Clazz.instanceOf (ret, J.adapter.smarter.AtomSetCollectionReader))) return ret;
r = ret;
r.setup (null, null, null);
if (r.isBinary) {
r.setup (names[i], htParams, filesReader.getBufferedReaderOrBinaryDocument (i, true));
} else {
r.setup (names[i], htParams, reader);
}if (getReadersOnly) {
readers[i] = r;
} else {
ret = r.readData ();
if (!(Clazz.instanceOf (ret, J.adapter.smarter.AtomSetCollection))) return ret;
atomsets[i] = ret;
if (atomsets[i].errorMessage != null) return atomsets[i].errorMessage;
}} catch (e) {
J.util.Logger.error ("" + e);
if (!viewer.isJS) e.printStackTrace ();
return "" + e;
}
}
if (getReadersOnly) return readers;
return this.getAtomSetCollectionFromSet (readers, atomsets, htParams);
}, "J.api.JmolFilesReaderInterface,~A,~A,java.util.Map,~B");
Clazz.overrideMethod (c$, "getAtomSetCollectionFromSet", 
function (readerSet, atomsets, htParams) {
var readers = readerSet;
var asc = (atomsets == null ?  new Array (readers.length) : atomsets);
if (atomsets == null) {
for (var i = 0; i < readers.length; i++) {
try {
var ret = readers[i].readData ();
if (!(Clazz.instanceOf (ret, J.adapter.smarter.AtomSetCollection))) return ret;
asc[i] = ret;
if (asc[i].errorMessage != null) return asc[i].errorMessage;
} catch (e) {
J.util.Logger.error ("" + e);
return "" + e;
}
}
}var result;
if (htParams.containsKey ("trajectorySteps")) {
result = asc[0];
try {
result.finalizeTrajectoryAs (htParams.get ("trajectorySteps"), htParams.get ("vibrationSteps"));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
if (result.errorMessage == null) result.errorMessage = "" + e;
} else {
throw e;
}
}
} else if (asc[0].isTrajectory) {
result = asc[0];
for (var i = 1; i < asc.length; i++) asc[0].mergeTrajectories (asc[i]);

} else {
result =  new J.adapter.smarter.AtomSetCollection ("Array", null, asc, null);
}return (result.errorMessage == null ? result : result.errorMessage);
}, "~O,~O,java.util.Map");
Clazz.overrideMethod (c$, "getAtomSetCollectionFromDOM", 
function (DOMNode, htParams) {
try {
var ret = J.adapter.smarter.Resolver.DOMResolve (DOMNode, htParams);
if (!(Clazz.instanceOf (ret, J.adapter.smarter.AtomSetCollectionReader))) return ret;
var a = ret;
a.setup ("DOM node", htParams, null);
ret = a.readDataObject (DOMNode);
if (!(Clazz.instanceOf (ret, J.adapter.smarter.AtomSetCollection))) return ret;
var asc = ret;
if (asc.errorMessage != null) return asc.errorMessage;
return asc;
} catch (e) {
J.util.Logger.error ("" + e);
return "" + e;
}
}, "~O,java.util.Map");
Clazz.overrideMethod (c$, "finish", 
function (atomSetCollection) {
(atomSetCollection).finish ();
}, "~O");
Clazz.overrideMethod (c$, "getAtomSetCollectionName", 
function (atomSetCollection) {
return (atomSetCollection).getCollectionName ();
}, "~O");
Clazz.overrideMethod (c$, "getAtomSetCollectionAuxiliaryInfo", 
function (atomSetCollection) {
return (atomSetCollection).getAtomSetCollectionAuxiliaryInfoMap ();
}, "~O");
Clazz.overrideMethod (c$, "getAtomSetCount", 
function (atomSetCollection) {
return (atomSetCollection).getAtomSetCount ();
}, "~O");
Clazz.overrideMethod (c$, "getAtomSetNumber", 
function (atomSetCollection, atomSetIndex) {
return (atomSetCollection).getAtomSetNumber (atomSetIndex);
}, "~O,~N");
Clazz.overrideMethod (c$, "getAtomSetName", 
function (atomSetCollection, atomSetIndex) {
return (atomSetCollection).getAtomSetName (atomSetIndex);
}, "~O,~N");
Clazz.overrideMethod (c$, "getAtomSetAuxiliaryInfo", 
function (atomSetCollection, atomSetIndex) {
return (atomSetCollection).getAtomSetAuxiliaryInfo (atomSetIndex);
}, "~O,~N");
Clazz.overrideMethod (c$, "getHydrogenAtomCount", 
function (atomSetCollection) {
return (atomSetCollection).getHydrogenAtomCount ();
}, "~O");
Clazz.overrideMethod (c$, "getBondList", 
function (atomSetCollection) {
return (atomSetCollection).getBondList ();
}, "~O");
Clazz.overrideMethod (c$, "getAtomCount", 
function (atomSetCollection) {
var a = atomSetCollection;
return (a.bsAtoms == null ? a.getAtomCount () : a.bsAtoms.cardinality ());
}, "~O");
Clazz.overrideMethod (c$, "coordinatesAreFractional", 
function (atomSetCollection) {
return (atomSetCollection).coordinatesAreFractional;
}, "~O");
Clazz.overrideMethod (c$, "getNotionalUnitcell", 
function (atomSetCollection) {
return (atomSetCollection).notionalUnitCell;
}, "~O");
Clazz.overrideMethod (c$, "getPdbScaleMatrix", 
function (atomSetCollection) {
var a = (atomSetCollection).notionalUnitCell;
if (a.length < 22) return null;
var b =  Clazz.newFloatArray (16, 0);
for (var i = 0; i < 16; i++) b[i] = a[6 + i];

return b;
}, "~O");
Clazz.overrideMethod (c$, "getPdbScaleTranslate", 
function (atomSetCollection) {
var a = (atomSetCollection).notionalUnitCell;
if (a.length < 22) return null;
var b =  Clazz.newFloatArray (3, 0);
b[0] = a[9];
b[1] = a[13];
b[2] = a[17];
return b;
}, "~O");
Clazz.overrideMethod (c$, "getAtomIterator", 
function (atomSetCollection) {
return  new J.adapter.smarter.AtomIterator (atomSetCollection);
}, "~O");
Clazz.overrideMethod (c$, "getBondIterator", 
function (atomSetCollection) {
return  new J.adapter.smarter.BondIterator (atomSetCollection);
}, "~O");
Clazz.overrideMethod (c$, "getStructureIterator", 
function (atomSetCollection) {
return (atomSetCollection).getStructureCount () == 0 ? null :  new J.adapter.smarter.StructureIterator (atomSetCollection);
}, "~O");
c$.close = $_M(c$, "close", 
function (bufferedReader) {
if (Clazz.instanceOf (bufferedReader, java.io.BufferedReader)) (bufferedReader).close ();
 else (bufferedReader).close ();
}, "~O");
Clazz.defineStatics (c$,
"PATH_KEY", ".PATH");
c$.PATH_SEPARATOR = c$.prototype.PATH_SEPARATOR = System.getProperty ("path.separator", "/");
});
