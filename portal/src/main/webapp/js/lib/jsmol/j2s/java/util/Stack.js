$_L(["java.util.Vector"],"java.util.Stack",["java.util.EmptyStackException"],function(){
c$=$_T(java.util,"Stack",java.util.Vector);
$_M(c$,"empty",
function(){
return this.elementCount==0;
});
$_M(c$,"peek",
function(){
try{
return this.elementData[this.elementCount-1];
}catch(e){
if($_O(e,IndexOutOfBoundsException)){
throw new java.util.EmptyStackException();
}else{
throw e;
}
}
});
$_M(c$,"pop",
function(){
try{
var index=this.elementCount-1;
var obj=this.elementData[index];
this.removeElementAt(index);
return obj;
}catch(e){
if($_O(e,IndexOutOfBoundsException)){
throw new java.util.EmptyStackException();
}else{
throw e;
}
}
});
$_M(c$,"push",
function(object){
this.addElement(object);
return object;
},"~O");
$_M(c$,"search",
function(o){
var index=this.lastIndexOf(o);
if(index>=0)return(this.elementCount-index);
return-1;
},"~O");
});
