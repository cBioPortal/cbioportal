$_L(["java.util.AbstractSet","$.Set"],"java.util.HashSet",["java.util.HashMap"],function(){
c$=$_C(function(){
this.backingMap=null;
$_Z(this,arguments);
},java.util,"HashSet",java.util.AbstractSet,[java.util.Set,Cloneable,java.io.Serializable]);
$_K(c$,
function(){
this.construct(new java.util.HashMap());
});
$_K(c$,
function(capacity){
this.construct(new java.util.HashMap(capacity));
},"~N");
$_K(c$,
function(capacity,loadFactor){
this.construct(new java.util.HashMap(capacity,loadFactor));
},"~N,~N");
$_K(c$,
function(collection){
this.construct(new java.util.HashMap(collection.size()<6?11:collection.size()*2));
for(var e,$e=collection.iterator();$e.hasNext()&&((e=$e.next())||true);){
this.add(e);
}
},"java.util.Collection");
$_K(c$,
function(backingMap){
$_R(this,java.util.HashSet,[]);
this.backingMap=backingMap;
},"java.util.HashMap");
$_V(c$,"add",
function(object){
return this.backingMap.put(object,this)==null;
},"~O");
$_V(c$,"clear",
function(){
this.backingMap.clear();
});
$_M(c$,"clone",
function(){
try{
var clone=$_U(this,java.util.HashSet,"clone",[]);
clone.backingMap=this.backingMap.clone();
return clone;
}catch(e){
if($_O(e,CloneNotSupportedException)){
return null;
}else{
throw e;
}
}
});
$_V(c$,"contains",
function(object){
return this.backingMap.containsKey(object);
},"~O");
$_V(c$,"isEmpty",
function(){
return this.backingMap.isEmpty();
});
$_M(c$,"iterator",
function(){
return this.backingMap.keySet().iterator();
});
$_V(c$,"remove",
function(object){
return this.backingMap.remove(object)!=null;
},"~O");
$_V(c$,"size",
function(){
return this.backingMap.size();
});
$_M(c$,"createBackingMap",
function(capacity,loadFactor){
return new java.util.HashMap(capacity,loadFactor);
},"~N,~N");
});
