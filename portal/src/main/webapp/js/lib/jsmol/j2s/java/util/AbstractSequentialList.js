$_L(["java.util.AbstractList"],"java.util.AbstractSequentialList",["java.lang.IndexOutOfBoundsException"],function(){
c$=$_T(java.util,"AbstractSequentialList",java.util.AbstractList);
$_M(c$,"add",
function(location,object){
this.listIterator(location).add(object);
},"~N,~O");
$_M(c$,"addAll",
function(location,collection){
var it=this.listIterator(location);
var colIt=collection.iterator();
var next=it.nextIndex();
while(colIt.hasNext()){
it.add(colIt.next());
it.previous();
}
return next!=it.nextIndex();
},"~N,java.util.Collection");
$_V(c$,"get",
function(location){
try{
return this.listIterator(location).next();
}catch(e){
if($_O(e,java.util.NoSuchElementException)){
throw new IndexOutOfBoundsException();
}else{
throw e;
}
}
},"~N");
$_V(c$,"iterator",
function(){
return this.listIterator(0);
});
$_M(c$,"remove",
function(location){
try{
var it=this.listIterator(location);
var result=it.next();
it.remove();
return result;
}catch(e){
if($_O(e,java.util.NoSuchElementException)){
throw new IndexOutOfBoundsException();
}else{
throw e;
}
}
},"~N");
$_V(c$,"set",
function(location,object){
var it=this.listIterator(location);
var result=it.next();
it.set(object);
return result;
},"~N,~O");
});
