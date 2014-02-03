$_L(["java.io.Closeable","$.InputStream"],"java.io.FileInputStream",["java.lang.IndexOutOfBoundsException","$.NullPointerException"],function(){
c$=$_C(function(){
this.fd=null;
this.innerFD=false;
$_Z(this,arguments);
},java.io,"FileInputStream",java.io.InputStream,java.io.Closeable);
$_K(c$,
function(file){
$_R(this,java.io.FileInputStream);
},"java.io.File");
$_K(c$,
function(fd){
$_R(this,java.io.FileInputStream);
if(fd==null){
throw new NullPointerException();
}},"java.io.FileDescriptor");
$_K(c$,
function(fileName){
this.construct(null==fileName?null:null);
},"~S");
$_V(c$,"available",
function(){
return 0;
});
$_V(c$,"close",
function(){
if(this.fd==null){
return;
}});
$_V(c$,"finalize",
function(){
this.close();
});
$_M(c$,"getFD",
function(){
return this.fd;
});
$_M(c$,"read",
function(){
var readed=$_A(1,0);
var result=this.read(readed,0,1);
return result==-1?-1:readed[0]&0xff;
});
$_M(c$,"read",
function(buffer){
return this.read(buffer,0,buffer.length);
},"~A");
$_M(c$,"read",
function(buffer,offset,count){
if(count>buffer.length-offset||count<0||offset<0){
throw new IndexOutOfBoundsException();
}if(0==count){
return 0;
}return 0;
},"~A,~N,~N");
$_V(c$,"skip",
function(count){
return 0;
},"~N");
});
