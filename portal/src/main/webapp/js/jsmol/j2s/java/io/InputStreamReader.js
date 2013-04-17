Clazz.load (["java.io.Reader"], "java.io.InputStreamReader", ["java.lang.NullPointerException"], function () {
c$ = Clazz.decorateAsClass (function () {
this.$in = null;
this.isOpen = true;
this.charsetName = null;
this.isUTF8 = false;
this.bytearr = null;
this.pos = 0;
Clazz.instantialize (this, arguments);
}, java.io, "InputStreamReader", java.io.Reader);
Clazz.makeConstructor (c$, 
function ($in, charsetName) {
Clazz.superConstructor (this, java.io.InputStreamReader, [$in]);
this.$in = $in;
this.charsetName = charsetName;
if (!(this.isUTF8 = "UTF-8".equals (charsetName)) && !"ISO-8859-1".equals (charsetName)) throw  new NullPointerException ("charsetName");
}, "java.io.InputStream,~S");
$_M(c$, "getEncoding", 
function () {
return this.charsetName;
});
Clazz.overrideMethod (c$, "read", 
function (cbuf, offset, length) {
if (this.bytearr == null || this.bytearr.length < length) this.bytearr =  Clazz.newByteArray (length, 0);
var c;
var char2;
var char3;
var count = 0;
var chararr_count = 0;
var len = this.$in.read (this.bytearr, this.pos, length - this.pos);
if (len < 0) return -1;
this.pos = 0;
while (count < len) {
c = this.bytearr[count] & 0xff;
if (this.isUTF8) switch (c >> 4) {
case 0xC:
case 0xD:
if (count > len - 2) continue;
count += 2;
char2 = this.bytearr[count - 1];
if ((char2 & 0xC0) != 0x80) {
count -= 2;
break;
}cbuf[chararr_count++] = String.fromCharCode (((c & 0x1F) << 6) | (char2 & 0x3F));
continue;
case 0xE:
if (count > len - 3) continue;
count += 3;
char2 = this.bytearr[count - 2];
char3 = this.bytearr[count - 1];
if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
count -= 3;
break;
}cbuf[chararr_count++] = String.fromCharCode (((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
continue;
}
count++;
cbuf[chararr_count++] = String.fromCharCode (c);
}
this.pos = len - count;
for (var i = 0; i < this.pos; i++) {
this.bytearr[i] = this.bytearr[count++];
}
return len - this.pos;
}, "~A,~N,~N");
Clazz.overrideMethod (c$, "ready", 
function () {
return this.isOpen;
});
Clazz.overrideMethod (c$, "close", 
function () {
this.$in.close ();
this.isOpen = false;
});
});
