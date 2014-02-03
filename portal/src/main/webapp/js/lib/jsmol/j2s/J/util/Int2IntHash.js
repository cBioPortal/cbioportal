Clazz.declarePackage ("J.util");
c$ = Clazz.decorateAsClass (function () {
this.entryCount = 0;
this.entries = null;
Clazz.instantialize (this, arguments);
}, J.util, "Int2IntHash");
Clazz.makeConstructor (c$, 
function (initialCapacity) {
this.entries =  new Array (initialCapacity);
}, "~N");
$_M(c$, "get", 
function (key) {
var entries = this.entries;
var hash = (key & 0x7FFFFFFF) % entries.length;
for (var e = entries[hash]; e != null; e = e.next) if (e.key == key) return e.value;

return -2147483648;
}, "~N");
$_M(c$, "put", 
function (key, value) {
var entries = this.entries;
var hash = (key & 0x7FFFFFFF) % entries.length;
for (var e = entries[hash]; e != null; e = e.next) if (e.key == key) {
e.value = value;
return;
}
if (this.entryCount > entries.length) this.rehash ();
entries = this.entries;
hash = (key & 0x7FFFFFFF) % entries.length;
entries[hash] =  new J.util.Int2IntHash.Entry (key, value, entries[hash]);
++this.entryCount;
}, "~N,~N");
$_M(c$, "rehash", 
($fz = function () {
var oldEntries = this.entries;
var oldSize = oldEntries.length;
var newSize = oldSize * 2 + 1;
var newEntries =  new Array (newSize);
for (var i = oldSize; --i >= 0; ) {
for (var e = oldEntries[i]; e != null; ) {
var t = e;
e = e.next;
var hash = (t.key & 0x7FFFFFFF) % newSize;
t.next = newEntries[hash];
newEntries[hash] = t;
}
}
this.entries = newEntries;
}, $fz.isPrivate = true, $fz));
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
this.key = 0;
this.value = 0;
this.next = null;
Clazz.instantialize (this, arguments);
}, J.util.Int2IntHash, "Entry");
Clazz.makeConstructor (c$, 
function (a, b, c) {
this.key = a;
this.value = b;
this.next = c;
}, "~N,~N,J.util.Int2IntHash.Entry");
c$ = Clazz.p0p ();
