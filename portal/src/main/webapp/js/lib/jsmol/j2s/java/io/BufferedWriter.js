$_L(["java.io.Writer"],"java.io.BufferedWriter",["java.io.IOException","java.lang.IllegalArgumentException","$.IndexOutOfBoundsException","$.StringIndexOutOfBoundsException"],function(){
c$=$_C(function(){
this.out=null;
this.buf=null;
this.pos=0;
this.lineSeparator="\r\n";
$_Z(this,arguments);
},java.io,"BufferedWriter",java.io.Writer);
$_K(c$,
function(out){
$_R(this,java.io.BufferedWriter,[out]);
this.out=out;
this.buf=$_A(8192,'\0');
},"java.io.Writer");
$_K(c$,
function(out,size){
$_R(this,java.io.BufferedWriter,[out]);
if(size>0){
this.out=out;
this.buf=$_A(size,'\0');
}else{
throw new IllegalArgumentException(("K0058"));
}},"java.io.Writer,~N");
$_M(c$,"close",
function(){
{
if(this.isOpen()){
this.flush();
this.out.close();
this.buf=null;
this.out=null;
}}});
$_M(c$,"flush",
function(){
{
if(this.isOpen()){
if(this.pos>0){
this.out.write(this.buf,0,this.pos);
}this.pos=0;
this.out.flush();
}else{
throw new java.io.IOException(("K005d"));
}}});
$_M(c$,"isOpen",
($fz=function(){
return this.out!=null;
},$fz.isPrivate=true,$fz));
$_M(c$,"newLine",
function(){
this.write("\r\n",0,"\r\n".length);
});
$_M(c$,"write",
function(cbuf,offset,count){
{
if(!this.isOpen()){
throw new java.io.IOException(("K005d"));
}if(offset<0||offset>cbuf.length-count||count<0){
throw new IndexOutOfBoundsException();
}if(this.pos==0&&count>=this.buf.length){
this.out.write(cbuf,offset,count);
return;
}var available=this.buf.length-this.pos;
if(count<available){
available=count;
}if(available>0){
System.arraycopy(cbuf,offset,this.buf,this.pos,available);
this.pos+=available;
}if(this.pos==this.buf.length){
this.out.write(this.buf,0,this.buf.length);
this.pos=0;
if(count>available){
offset+=available;
available=count-available;
if(available>=this.buf.length){
this.out.write(cbuf,offset,available);
return;
}System.arraycopy(cbuf,offset,this.buf,this.pos,available);
this.pos+=available;
}}}},"~A,~N,~N");
$_M(c$,"write",
function(oneChar){
{
if(this.isOpen()){
if(this.pos>=this.buf.length){
this.out.write(this.buf,0,this.buf.length);
this.pos=0;
}this.buf[this.pos++]=String.fromCharCode(oneChar);
}else{
throw new java.io.IOException(("K005d"));
}}},"~N");
$_M(c$,"write",
function(str,offset,count){
{
if(!this.isOpen()){
throw new java.io.IOException(("K005d"));
}if(count<=0){
return;
}if(offset>str.length-count||offset<0){
throw new StringIndexOutOfBoundsException();
}if(this.pos==0&&count>=this.buf.length){
var chars=$_A(count,'\0');
str.getChars(offset,offset+count,chars,0);
this.out.write(chars,0,count);
return;
}var available=this.buf.length-this.pos;
if(count<available){
available=count;
}if(available>0){
str.getChars(offset,offset+available,this.buf,this.pos);
this.pos+=available;
}if(this.pos==this.buf.length){
this.out.write(this.buf,0,this.buf.length);
this.pos=0;
if(count>available){
offset+=available;
available=count-available;
if(available>=this.buf.length){
var chars=$_A(count,'\0');
str.getChars(offset,offset+available,chars,0);
this.out.write(chars,0,available);
return;
}str.getChars(offset,offset+available,this.buf,this.pos);
this.pos+=available;
}}}},"~S,~N,~N");
});
