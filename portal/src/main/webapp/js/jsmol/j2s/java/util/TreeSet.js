$_L(["java.util.AbstractSet","$.SortedSet","$.TreeMap"],"java.util.TreeSet",["java.lang.IllegalArgumentException"],function(){
c$=$_C(function(){
this.backingMap=null;
$_Z(this,arguments);
},java.util,"TreeSet",java.util.AbstractSet,[java.util.SortedSet,Cloneable,java.io.Serializable]);
$_K(c$,
($fz=function(map){
$_R(this,java.util.TreeSet,[]);
this.backingMap=map;
},$fz.isPrivate=true,$fz),"java.util.SortedMap");
$_K(c$,
function(){
$_R(this,java.util.TreeSet,[]);
this.backingMap=new java.util.TreeMap();
});
$_K(c$,
function(collection){
this.construct();
this.addAll(collection);
},"java.util.Collection");
$_K(c$,
function(comparator){
$_R(this,java.util.TreeSet,[]);
this.backingMap=new java.util.TreeMap(comparator);
},"java.util.Comparator");
$_K(c$,
function(set){
this.construct(set.comparator());
var it=set.iterator();
while(it.hasNext()){
this.add(it.next());
}
},"java.util.SortedSet");
$_V(c$,"add",
function(object){
return this.backingMap.put(object,object)==null;
},"~O");
$_V(c$,"clear",
function(){
this.backingMap.clear();
});
$_M(c$,"clone",
function(){
try{
var clone=$_U(this,java.util.TreeSet,"clone",[]);
if($_O(this.backingMap,java.util.TreeMap)){
clone.backingMap=(this.backingMap).clone();
}else{
clone.backingMap=new java.util.TreeMap(this.backingMap);
}return clone;
}catch(e){
if($_O(e,CloneNotSupportedException)){
return null;
}else{
throw e;
}
}
});
$_M(c$,"comparator",
function(){
return this.backingMap.comparator();
});
$_V(c$,"contains",
function(object){
return this.backingMap.containsKey(object);
},"~O");
$_V(c$,"first",
function(){
return this.backingMap.firstKey();
});
$_V(c$,"headSet",
function(end){
var c=this.backingMap.comparator();
if(c==null){
(end).compareTo(end);
}else{
c.compare(end,end);
}return new java.util.TreeSet(this.backingMap.headMap(end));
},"~O");
$_V(c$,"isEmpty",
function(){
return this.backingMap.isEmpty();
});
$_M(c$,"iterator",
function(){
return this.backingMap.keySet().iterator();
});
$_V(c$,"last",
function(){
return this.backingMap.lastKey();
});
$_V(c$,"remove",
function(object){
return this.backingMap.remove(object)!=null;
},"~O");
$_V(c$,"size",
function(){
return this.backingMap.size();
});
$_V(c$,"subSet",
function(start,end){
var c=this.backingMap.comparator();
if(c==null){
if((start).compareTo(end)<=0){
return new java.util.TreeSet(this.backingMap.subMap(start,end));
}}else{
if(c.compare(start,end)<=0){
return new java.util.TreeSet(this.backingMap.subMap(start,end));
}}throw new IllegalArgumentException();
},"~O,~O");
$_V(c$,"tailSet",
function(start){
var c=this.backingMap.comparator();
if(c==null){
(start).compareTo(start);
}else{
c.compare(start,start);
}return new java.util.TreeSet(this.backingMap.tailMap(start));
},"~O");
});
