$_L(["java.io.InputStream"],"java.io.StringBufferInputStream",["java.lang.ArrayIndexOutOfBoundsException","$.NullPointerException"],function(){
c$=$_C(function(){
this.buffer=null;
this.count=0;
this.pos=0;
$_Z(this,arguments);
},java.io,"StringBufferInputStream",java.io.InputStream);
$_K(c$,
function(str){
$_R(this,java.io.StringBufferInputStream,[]);
if(str!=null){
this.buffer=str;
this.count=str.length;
}else{
throw new NullPointerException();
}},"~S");
$_V(c$,"available",
function(){
return this.count-this.pos;
});
$_M(c$,"read",
function(){
return this.pos<this.count?(this.buffer.charAt(this.pos++)).charCodeAt(0)&0xFF:-1;
});
$_M(c$,"read",
function(b,offset,length){
if(this.pos>=this.count){
return-1;
}if(b!=null){
if(0<=offset&&offset<=b.length&&0<=length&&length<=b.length-offset){
if(length==0){
return 0;
}var copylen=this.count-this.pos<length?this.count-this.pos:length;
for(var i=0;i<copylen;i++){
b[offset+i]=(this.buffer.charAt(this.pos+i)).charCodeAt(0);
}
this.pos+=copylen;
return copylen;
}throw new ArrayIndexOutOfBoundsException();
}throw new NullPointerException(("K0047"));
},"~A,~N,~N");
$_V(c$,"reset",
function(){
this.pos=0;
});
$_V(c$,"skip",
function(n){
if(n<=0){
return 0;
}var numskipped;
if(this.count-this.pos<n){
numskipped=this.count-this.pos;
this.pos=this.count;
}else{
numskipped=n;
this.pos+=n;
}return numskipped;
},"~N");
});
