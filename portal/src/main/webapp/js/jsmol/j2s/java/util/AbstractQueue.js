$_L(["java.util.AbstractCollection","$.Queue"],"java.util.AbstractQueue",["java.lang.IllegalArgumentException","$.IllegalStateException","$.NullPointerException","java.util.NoSuchElementException"],function(){
c$=$_T(java.util,"AbstractQueue",java.util.AbstractCollection,java.util.Queue);
$_V(c$,"add",
function(o){
if(null==o){
throw new NullPointerException();
}if(this.offer(o)){
return true;
}throw new IllegalStateException();
},"~O");
$_V(c$,"addAll",
function(c){
if(null==c){
throw new NullPointerException();
}if(this===c){
throw new IllegalArgumentException();
}return $_U(this,java.util.AbstractQueue,"addAll",[c]);
},"java.util.Collection");
$_M(c$,"remove",
function(){
var o=this.poll();
if(null==o){
throw new java.util.NoSuchElementException();
}return o;
});
$_V(c$,"element",
function(){
var o=this.peek();
if(null==o){
throw new java.util.NoSuchElementException();
}return o;
});
$_V(c$,"clear",
function(){
var o;
do{
o=this.poll();
}while(null!=o);
});
});
