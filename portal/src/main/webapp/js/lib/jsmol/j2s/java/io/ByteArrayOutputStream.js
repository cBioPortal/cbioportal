Clazz.load (["java.io.OutputStream"], "java.io.ByteArrayOutputStream", ["java.lang.IllegalArgumentException", "$.IndexOutOfBoundsException", "$.OutOfMemoryError", "J.util.ArrayUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.buf = null;
this.count = 0;
Clazz.instantialize (this, arguments);
}, java.io, "ByteArrayOutputStream", java.io.OutputStream);
Clazz.makeConstructor (c$, 
function () {
this.construct (32);
});
Clazz.makeConstructor (c$, 
function (size) {
Clazz.superConstructor (this, java.io.ByteArrayOutputStream, []);
if (size < 0) {
throw  new IllegalArgumentException ("Negative initial size: " + size);
}this.buf =  Clazz.newByteArray (size, 0);
}, "~N");
$_M(c$, "ensureCapacity", 
($fz = function (minCapacity) {
if (minCapacity - this.buf.length > 0) this.grow (minCapacity);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "grow", 
($fz = function (minCapacity) {
var oldCapacity = this.buf.length;
var newCapacity = oldCapacity << 1;
if (newCapacity - minCapacity < 0) newCapacity = minCapacity;
if (newCapacity < 0) {
if (minCapacity < 0) throw  new OutOfMemoryError ();
newCapacity = 2147483647;
}this.buf = J.util.ArrayUtil.arrayCopyByte (this.buf, newCapacity);
}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "writeByteAsInt", 
function (b) {
this.ensureCapacity (this.count + 1);
this.buf[this.count] = b;
this.count += 1;
}, "~N");
$_M(c$, "write", 
function (b, off, len) {
if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) - b.length > 0)) {
throw  new IndexOutOfBoundsException ();
}this.ensureCapacity (this.count + len);
System.arraycopy (b, off, this.buf, this.count, len);
this.count += len;
}, "~A,~N,~N");
$_M(c$, "writeTo", 
function (out) {
out.write (this.buf, 0, this.count);
}, "java.io.OutputStream");
$_M(c$, "reset", 
function () {
this.count = 0;
});
$_M(c$, "toByteArray", 
function () {
return (this.count == this.buf.length ? this.buf : J.util.ArrayUtil.arrayCopyByte (this.buf, this.count));
});
$_M(c$, "size", 
function () {
return this.count;
});
Clazz.overrideMethod (c$, "toString", 
function () {
return  String.instantialize (this.buf, 0, this.count);
});
Clazz.overrideMethod (c$, "close", 
function () {
});
});
