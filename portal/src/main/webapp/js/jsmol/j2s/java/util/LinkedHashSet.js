$_L(["java.util.HashSet","$.Set"],"java.util.LinkedHashSet",["java.util.LinkedHashMap"],function(){
c$=$_T(java.util,"LinkedHashSet",java.util.HashSet,[java.util.Set,Cloneable,java.io.Serializable]);
$_K(c$,
function(){
$_R(this,java.util.LinkedHashSet,[new java.util.LinkedHashMap()]);
});
$_K(c$,
function(capacity){
$_R(this,java.util.LinkedHashSet,[new java.util.LinkedHashMap(capacity)]);
},"~N");
$_K(c$,
function(capacity,loadFactor){
$_R(this,java.util.LinkedHashSet,[new java.util.LinkedHashMap(capacity,loadFactor)]);
},"~N,~N");
$_K(c$,
function(collection){
$_R(this,java.util.LinkedHashSet,[new java.util.LinkedHashMap(collection.size()<6?11:collection.size()*2)]);
for(var e,$e=collection.iterator();$e.hasNext()&&((e=$e.next())||true);){
this.add(e);
}
},"java.util.Collection");
$_V(c$,"createBackingMap",
function(capacity,loadFactor){
return new java.util.LinkedHashMap(capacity,loadFactor);
},"~N,~N");
});
