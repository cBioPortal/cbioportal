Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.BS", ["java.lang.IndexOutOfBoundsException", "$.NegativeArraySizeException", "J.util.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.words = null;
this.wordsInUse = 0;
this.sizeIsSticky = false;
Clazz.instantialize (this, arguments);
}, J.util, "BS", null, Cloneable);
c$.wordIndex = $_M(c$, "wordIndex", 
($fz = function (bitIndex) {
return bitIndex >> 5;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "recalculateWordsInUse", 
($fz = function () {
var i;
for (i = this.wordsInUse - 1; i >= 0; i--) if (this.words[i] != 0) break;

this.wordsInUse = i + 1;
}, $fz.isPrivate = true, $fz));
Clazz.makeConstructor (c$, 
function () {
this.initWords (32);
this.sizeIsSticky = false;
});
c$.newN = $_M(c$, "newN", 
function (nbits) {
var bs =  new J.util.BS ();
bs.init (nbits);
return bs;
}, "~N");
$_M(c$, "init", 
($fz = function (nbits) {
if (nbits < 0) throw  new NegativeArraySizeException ("nbits < 0: " + nbits);
this.initWords (nbits);
this.sizeIsSticky = true;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "initWords", 
($fz = function (nbits) {
this.words =  Clazz.newIntArray (J.util.BS.wordIndex (nbits - 1) + 1, 0);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "ensureCapacity", 
($fz = function (wordsRequired) {
if (this.words.length < wordsRequired) {
var request = Math.max (2 * this.words.length, wordsRequired);
this.setLength (request);
this.sizeIsSticky = false;
}}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "expandTo", 
($fz = function (wordIndex) {
var wordsRequired = wordIndex + 1;
if (this.wordsInUse < wordsRequired) {
this.ensureCapacity (wordsRequired);
this.wordsInUse = wordsRequired;
}}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "set", 
function (bitIndex) {
if (bitIndex < 0) throw  new IndexOutOfBoundsException ("bitIndex < 0: " + bitIndex);
var wordIndex = J.util.BS.wordIndex (bitIndex);
this.expandTo (wordIndex);
this.words[wordIndex] |= (1 << bitIndex);
}, "~N");
$_M(c$, "setBitTo", 
function (bitIndex, value) {
if (value) this.set (bitIndex);
 else this.clear (bitIndex);
}, "~N,~B");
$_M(c$, "setBits", 
function (fromIndex, toIndex) {
if (fromIndex == toIndex) return;
var startWordIndex = J.util.BS.wordIndex (fromIndex);
var endWordIndex = J.util.BS.wordIndex (toIndex - 1);
this.expandTo (endWordIndex);
var firstWordMask = -1 << fromIndex;
var lastWordMask = -1 >>> -toIndex;
if (startWordIndex == endWordIndex) {
this.words[startWordIndex] |= (firstWordMask & lastWordMask);
} else {
this.words[startWordIndex] |= firstWordMask;
for (var i = startWordIndex + 1; i < endWordIndex; i++) this.words[i] = -1;

this.words[endWordIndex] |= lastWordMask;
}}, "~N,~N");
$_M(c$, "clear", 
function (bitIndex) {
if (bitIndex < 0) throw  new IndexOutOfBoundsException ("bitIndex < 0: " + bitIndex);
var wordIndex = J.util.BS.wordIndex (bitIndex);
if (wordIndex >= this.wordsInUse) return;
this.words[wordIndex] &= ~(1 << bitIndex);
this.recalculateWordsInUse ();
}, "~N");
$_M(c$, "clearBits", 
function (fromIndex, toIndex) {
if (fromIndex == toIndex) return;
var startWordIndex = J.util.BS.wordIndex (fromIndex);
if (startWordIndex >= this.wordsInUse) return;
var endWordIndex = J.util.BS.wordIndex (toIndex - 1);
if (endWordIndex >= this.wordsInUse) {
toIndex = this.length ();
endWordIndex = this.wordsInUse - 1;
}var firstWordMask = -1 << fromIndex;
var lastWordMask = -1 >>> -toIndex;
if (startWordIndex == endWordIndex) {
this.words[startWordIndex] &= ~(firstWordMask & lastWordMask);
} else {
this.words[startWordIndex] &= ~firstWordMask;
for (var i = startWordIndex + 1; i < endWordIndex; i++) this.words[i] = 0;

this.words[endWordIndex] &= ~lastWordMask;
}this.recalculateWordsInUse ();
}, "~N,~N");
$_M(c$, "clearAll", 
function () {
while (this.wordsInUse > 0) this.words[--this.wordsInUse] = 0;

});
$_M(c$, "get", 
function (bitIndex) {
if (bitIndex < 0) throw  new IndexOutOfBoundsException ("bitIndex < 0: " + bitIndex);
var wordIndex = J.util.BS.wordIndex (bitIndex);
return (wordIndex < this.wordsInUse) && ((this.words[wordIndex] & (1 << bitIndex)) != 0);
}, "~N");
$_M(c$, "nextSetBit", 
function (fromIndex) {
if (fromIndex < 0) throw  new IndexOutOfBoundsException ("fromIndex < 0: " + fromIndex);
var u = J.util.BS.wordIndex (fromIndex);
if (u >= this.wordsInUse) return -1;
var word = this.words[u] & (-1 << fromIndex);
while (true) {
if (word != 0) return (u * 32) + Integer.numberOfTrailingZeros (word);
if (++u == this.wordsInUse) return -1;
word = this.words[u];
}
}, "~N");
$_M(c$, "nextClearBit", 
function (fromIndex) {
if (fromIndex < 0) throw  new IndexOutOfBoundsException ("fromIndex < 0: " + fromIndex);
var u = J.util.BS.wordIndex (fromIndex);
if (u >= this.wordsInUse) return fromIndex;
var word = ~this.words[u] & (-1 << fromIndex);
while (true) {
if (word != 0) return (u * 32) + Integer.numberOfTrailingZeros (word);
if (++u == this.wordsInUse) return this.wordsInUse * 32;
word = ~this.words[u];
}
}, "~N");
$_M(c$, "length", 
function () {
if (this.wordsInUse == 0) return 0;
return 32 * (this.wordsInUse - 1) + (32 - Integer.numberOfLeadingZeros (this.words[this.wordsInUse - 1]));
});
$_M(c$, "isEmpty", 
function () {
return this.wordsInUse == 0;
});
$_M(c$, "intersects", 
function (set) {
for (var i = Math.min (this.wordsInUse, set.wordsInUse) - 1; i >= 0; i--) if ((this.words[i] & set.words[i]) != 0) return true;

return false;
}, "J.util.BS");
$_M(c$, "cardinality", 
function () {
var sum = 0;
for (var i = 0; i < this.wordsInUse; i++) sum += Integer.bitCount (this.words[i]);

return sum;
});
$_M(c$, "and", 
function (set) {
if (this === set) return;
while (this.wordsInUse > set.wordsInUse) this.words[--this.wordsInUse] = 0;

for (var i = 0; i < this.wordsInUse; i++) this.words[i] &= set.words[i];

this.recalculateWordsInUse ();
}, "J.util.BS");
$_M(c$, "or", 
function (set) {
if (this === set) return;
var wordsInCommon = Math.min (this.wordsInUse, set.wordsInUse);
if (this.wordsInUse < set.wordsInUse) {
this.ensureCapacity (set.wordsInUse);
this.wordsInUse = set.wordsInUse;
}for (var i = 0; i < wordsInCommon; i++) this.words[i] |= set.words[i];

if (wordsInCommon < set.wordsInUse) System.arraycopy (set.words, wordsInCommon, this.words, wordsInCommon, this.wordsInUse - wordsInCommon);
}, "J.util.BS");
$_M(c$, "xor", 
function (set) {
var wordsInCommon = Math.min (this.wordsInUse, set.wordsInUse);
if (this.wordsInUse < set.wordsInUse) {
this.ensureCapacity (set.wordsInUse);
this.wordsInUse = set.wordsInUse;
}for (var i = 0; i < wordsInCommon; i++) this.words[i] ^= set.words[i];

if (wordsInCommon < set.wordsInUse) System.arraycopy (set.words, wordsInCommon, this.words, wordsInCommon, set.wordsInUse - wordsInCommon);
this.recalculateWordsInUse ();
}, "J.util.BS");
$_M(c$, "andNot", 
function (set) {
for (var i = Math.min (this.wordsInUse, set.wordsInUse) - 1; i >= 0; i--) this.words[i] &= ~set.words[i];

this.recalculateWordsInUse ();
}, "J.util.BS");
Clazz.overrideMethod (c$, "hashCode", 
function () {
var h = 1234;
for (var i = this.wordsInUse; --i >= 0; ) h ^= this.words[i] * (i + 1);

return ((h >> 32) ^ h);
});
$_M(c$, "size", 
function () {
return this.words.length * 32;
});
Clazz.overrideMethod (c$, "equals", 
function (obj) {
if (!(Clazz.instanceOf (obj, J.util.BS))) return false;
if (this === obj) return true;
var set = obj;
if (this.wordsInUse != set.wordsInUse) return false;
for (var i = 0; i < this.wordsInUse; i++) if (this.words[i] != set.words[i]) return false;

return true;
}, "~O");
Clazz.overrideMethod (c$, "clone", 
function () {
if (!this.sizeIsSticky && this.wordsInUse != this.words.length) this.setLength (this.wordsInUse);
return J.util.BS.copy (this);
});
$_M(c$, "setLength", 
($fz = function (n) {
var a =  Clazz.newIntArray (n, 0);
System.arraycopy (this.words, 0, a, 0, Math.min (this.wordsInUse, n));
this.words = a;
}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "toString", 
function () {
var numBits = (this.wordsInUse > 128) ? this.cardinality () : this.wordsInUse * 32;
var b = J.util.SB.newN (6 * numBits + 2);
b.appendC ('{');
var i = this.nextSetBit (0);
if (i != -1) {
b.appendI (i);
for (i = this.nextSetBit (i + 1); i >= 0; i = this.nextSetBit (i + 1)) {
var endOfRun = this.nextClearBit (i);
do {
b.append (", ").appendI (i);
} while (++i < endOfRun);
}
}b.appendC ('}');
return b.toString ();
});
c$.copy = $_M(c$, "copy", 
function (bitsetToCopy) {
var bs = null;
{
bs = Clazz.clone(bitsetToCopy);
}var wordCount = bitsetToCopy.wordsInUse;
if (wordCount == 0) {
bs.words = J.util.BS.emptyBitmap;
} else {
bs.words =  Clazz.newIntArray (wordCount, 0);
System.arraycopy (bitsetToCopy.words, 0, bs.words, 0, wordCount);
}return bs;
}, "J.util.BS");
$_M(c$, "cardinalityN", 
function (max) {
var n = this.cardinality ();
for (var i = this.length (); --i >= max; ) if (this.get (i)) n--;

return n;
}, "~N");
Clazz.defineStatics (c$,
"ADDRESS_BITS_PER_WORD", 5,
"BITS_PER_WORD", 32,
"WORD_MASK", 0xffffffff,
"emptyBitmap",  Clazz.newIntArray (0, 0));
});
