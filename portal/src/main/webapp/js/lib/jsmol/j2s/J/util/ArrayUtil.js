Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.ArrayUtil", ["java.util.Arrays", "J.util.JmolList"], function () {
c$ = Clazz.declareType (J.util, "ArrayUtil");
c$.ensureLength = $_M(c$, "ensureLength", 
function (array, minimumLength) {
if (array != null && J.util.ArrayUtil.getLength (array) >= minimumLength) return array;
return J.util.ArrayUtil.arrayCopyObject (array, minimumLength);
}, "~O,~N");
c$.ensureLengthS = $_M(c$, "ensureLengthS", 
function (array, minimumLength) {
if (array != null && array.length >= minimumLength) return array;
return J.util.ArrayUtil.arrayCopyS (array, minimumLength);
}, "~A,~N");
c$.ensureLengthA = $_M(c$, "ensureLengthA", 
function (array, minimumLength) {
if (array != null && array.length >= minimumLength) return array;
return J.util.ArrayUtil.arrayCopyF (array, minimumLength);
}, "~A,~N");
c$.ensureLengthI = $_M(c$, "ensureLengthI", 
function (array, minimumLength) {
if (array != null && array.length >= minimumLength) return array;
return J.util.ArrayUtil.arrayCopyI (array, minimumLength);
}, "~A,~N");
c$.ensureLengthShort = $_M(c$, "ensureLengthShort", 
function (array, minimumLength) {
if (array != null && array.length >= minimumLength) return array;
return J.util.ArrayUtil.arrayCopyShort (array, minimumLength);
}, "~A,~N");
c$.ensureLengthByte = $_M(c$, "ensureLengthByte", 
function (array, minimumLength) {
if (array != null && array.length >= minimumLength) return array;
return J.util.ArrayUtil.arrayCopyByte (array, minimumLength);
}, "~A,~N");
c$.doubleLength = $_M(c$, "doubleLength", 
function (array) {
return J.util.ArrayUtil.arrayCopyObject (array, (array == null ? 16 : 2 * J.util.ArrayUtil.getLength (array)));
}, "~O");
c$.doubleLengthS = $_M(c$, "doubleLengthS", 
function (array) {
return J.util.ArrayUtil.arrayCopyS (array, (array == null ? 16 : 2 * array.length));
}, "~A");
c$.doubleLengthF = $_M(c$, "doubleLengthF", 
function (array) {
return J.util.ArrayUtil.arrayCopyF (array, (array == null ? 16 : 2 * array.length));
}, "~A");
c$.doubleLengthI = $_M(c$, "doubleLengthI", 
function (array) {
return J.util.ArrayUtil.arrayCopyI (array, (array == null ? 16 : 2 * array.length));
}, "~A");
c$.doubleLengthShort = $_M(c$, "doubleLengthShort", 
function (array) {
return J.util.ArrayUtil.arrayCopyShort (array, (array == null ? 16 : 2 * array.length));
}, "~A");
c$.doubleLengthByte = $_M(c$, "doubleLengthByte", 
function (array) {
return J.util.ArrayUtil.arrayCopyByte (array, (array == null ? 16 : 2 * array.length));
}, "~A");
c$.doubleLengthBool = $_M(c$, "doubleLengthBool", 
function (array) {
return J.util.ArrayUtil.arrayCopyBool (array, (array == null ? 16 : 2 * array.length));
}, "~A");
c$.deleteElements = $_M(c$, "deleteElements", 
function (array, firstElement, nElements) {
if (nElements == 0 || array == null) return array;
var oldLength = J.util.ArrayUtil.getLength (array);
if (firstElement >= oldLength) return array;
var n = oldLength - (firstElement + nElements);
if (n < 0) n = 0;
var t = J.util.ArrayUtil.newInstanceO (array, firstElement + n);
if (firstElement > 0) System.arraycopy (array, 0, t, 0, firstElement);
if (n > 0) System.arraycopy (array, firstElement + nElements, t, firstElement, n);
return t;
}, "~O,~N,~N");
c$.arrayCopyObject = $_M(c$, "arrayCopyObject", 
function (array, newLength) {
if (array == null) {
return null;
}var oldLength = J.util.ArrayUtil.getLength (array);
if (newLength == oldLength) return array;
var t = J.util.ArrayUtil.newInstanceO (array, newLength);
System.arraycopy (array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
return t;
}, "~O,~N");
c$.newInstanceO = $_M(c$, "newInstanceO", 
($fz = function (array, n) {
{
return new Array(n);
}}, $fz.isPrivate = true, $fz), "~O,~N");
c$.getLength = $_M(c$, "getLength", 
($fz = function (array) {
{
return array.length
}}, $fz.isPrivate = true, $fz), "~O");
c$.arrayCopyS = $_M(c$, "arrayCopyS", 
function (array, newLength) {
if (newLength < 0) newLength = array.length;
var t =  new Array (newLength);
if (array != null) {
var oldLength = array.length;
System.arraycopy (array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
}return t;
}, "~A,~N");
c$.arrayCopyII = $_M(c$, "arrayCopyII", 
function (array, newLength) {
var t = J.util.ArrayUtil.newInt2 (newLength);
if (array != null) {
var oldLength = array.length;
System.arraycopy (array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
}return t;
}, "~A,~N");
c$.arrayCopyPt = $_M(c$, "arrayCopyPt", 
function (array, newLength) {
if (newLength < 0) newLength = array.length;
var t =  new Array (newLength);
if (array != null) {
var oldLength = array.length;
System.arraycopy (array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
}return t;
}, "~A,~N");
c$.arrayCopyF = $_M(c$, "arrayCopyF", 
function (array, newLength) {
if (newLength < 0) newLength = array.length;
var t =  Clazz.newFloatArray (newLength, 0);
if (array != null) {
var oldLength = array.length;
System.arraycopy (array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
}return t;
}, "~A,~N");
c$.arrayCopyI = $_M(c$, "arrayCopyI", 
function (array, newLength) {
if (newLength < 0) newLength = array.length;
var t =  Clazz.newIntArray (newLength, 0);
if (array != null) {
var oldLength = array.length;
System.arraycopy (array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
}return t;
}, "~A,~N");
c$.arrayCopyRangeI = $_M(c$, "arrayCopyRangeI", 
function (array, i0, n) {
if (array == null) return null;
var oldLength = array.length;
if (n == -1) n = oldLength;
if (n == -2) n = Clazz.doubleToInt (oldLength / 2);
n = n - i0;
var t =  Clazz.newIntArray (n, 0);
System.arraycopy (array, i0, t, 0, n);
return t;
}, "~A,~N,~N");
c$.arrayCopyRangeRevI = $_M(c$, "arrayCopyRangeRevI", 
function (array, i0, n) {
if (array == null) return null;
var t = J.util.ArrayUtil.arrayCopyRangeI (array, i0, n);
if (n < 0) n = array.length;
for (var i = Clazz.doubleToInt (n / 2); --i >= 0; ) J.util.ArrayUtil.swapInt (t, i, n - 1 - i);

return t;
}, "~A,~N,~N");
c$.arrayCopyShort = $_M(c$, "arrayCopyShort", 
function (array, newLength) {
if (newLength < 0) newLength = array.length;
var t =  Clazz.newShortArray (newLength, 0);
if (array != null) {
var oldLength = array.length;
System.arraycopy (array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
}return t;
}, "~A,~N");
c$.arrayCopyByte = $_M(c$, "arrayCopyByte", 
function (array, newLength) {
if (newLength < 0) newLength = array.length;
var t =  Clazz.newByteArray (newLength, 0);
if (array != null) {
var oldLength = array.length;
System.arraycopy (array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
}return t;
}, "~A,~N");
c$.arrayCopyBool = $_M(c$, "arrayCopyBool", 
function (array, newLength) {
if (newLength < 0) newLength = array.length;
var t =  Clazz.newBooleanArray (newLength, false);
if (array != null) {
var oldLength = array.length;
System.arraycopy (array, 0, t, 0, oldLength < newLength ? oldLength : newLength);
}return t;
}, "~A,~N");
c$.swapInt = $_M(c$, "swapInt", 
function (array, indexA, indexB) {
var t = array[indexA];
array[indexA] = array[indexB];
array[indexB] = t;
}, "~A,~N,~N");
c$.dumpArray = $_M(c$, "dumpArray", 
function (msg, A, x1, x2, y1, y2) {
var s = "dumpArray: " + msg + "\n";
for (var x = x1; x <= x2; x++) s += "\t*" + x + "*";

for (var y = y2; y >= y1; y--) {
s += "\n*" + y + "*";
for (var x = x1; x <= x2; x++) s += "\t" + (x < A.length && y < A[x].length ? A[x][y] : NaN);

}
return s;
}, "~S,~A,~N,~N,~N,~N");
c$.dumpIntArray = $_M(c$, "dumpIntArray", 
function (A, n) {
var str = "";
for (var i = 0; i < n; i++) str += " " + A[i];

return str;
}, "~A,~N");
c$.sortedItem = $_M(c$, "sortedItem", 
function (v, n) {
if (v.size () == 0) return null;
if (v.size () == 1) return v.get (0);
var keys = v.toArray ( new Array (v.size ()));
java.util.Arrays.sort (keys);
return keys[n % keys.length];
}, "J.util.JmolList,~N");
c$.createArrayOfArrayList = $_M(c$, "createArrayOfArrayList", 
function (size) {
return  new Array (size);
}, "~N");
c$.createArrayOfHashtable = $_M(c$, "createArrayOfHashtable", 
function (size) {
return  new Array (size);
}, "~N");
c$.swap = $_M(c$, "swap", 
function (o, i, j) {
var oi = o[i];
o[i] = o[j];
o[j] = oi;
}, "~A,~N,~N");
c$.newFloat2 = $_M(c$, "newFloat2", 
function (n) {
{
return Clazz.newArray(n, null);
}}, "~N");
c$.newInt2 = $_M(c$, "newInt2", 
function (n) {
{
return Clazz.newArray(n, null);
}}, "~N");
c$.newInt3 = $_M(c$, "newInt3", 
function (nx, ny) {
{
return Clazz.newArray(nx, null);
}}, "~N,~N");
c$.newFloat3 = $_M(c$, "newFloat3", 
function (nx, ny) {
{
return Clazz.newArray(nx, null);
}}, "~N,~N");
c$.newInt4 = $_M(c$, "newInt4", 
function (n) {
{
return Clazz.newArray(n, null);
}}, "~N");
c$.newShort2 = $_M(c$, "newShort2", 
function (n) {
{
return Clazz.newArray(n, null);
}}, "~N");
c$.newByte2 = $_M(c$, "newByte2", 
function (n) {
{
return Clazz.newArray(n, null);
}}, "~N");
c$.newDouble2 = $_M(c$, "newDouble2", 
function (n) {
{
return Clazz.newArray(n, null);
}}, "~N");
c$.removeMapKeys = $_M(c$, "removeMapKeys", 
function (map, root) {
var list =  new J.util.JmolList ();
for (var key, $key = map.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) if (key.startsWith (root)) list.addLast (key);

for (var i = list.size (); --i >= 0; ) map.remove (list.get (i));

return list.size ();
}, "java.util.Map,~S");
});
