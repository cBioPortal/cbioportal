$_L(["java.util.AbstractCollection","$.AbstractMap","$.AbstractSet","$.Iterator","$.MapEntry","$.Set","$.SortedMap"],"java.util.TreeMap",["java.lang.IllegalArgumentException","$.IllegalStateException","java.util.ConcurrentModificationException","$.NoSuchElementException"],function(){
c$=$_C(function(){
this.$size=0;
this.root=null;
this.$comparator=null;
this.modCount=0;
this.$entrySet=null;
$_Z(this,arguments);
},java.util,"TreeMap",java.util.AbstractMap,[java.util.SortedMap,Cloneable,java.io.Serializable]);
c$.toComparable=$_M(c$,"toComparable",
($fz=function(obj){
return obj;
},$fz.isPrivate=true,$fz),"~O");
$_K(c$,
function(comparator){
$_R(this,java.util.TreeMap,[]);
this.$comparator=comparator;
},"java.util.Comparator");
$_K(c$,
function(map){
this.construct();
this.putAll(map);
},"java.util.Map");
$_K(c$,
function(map){
this.construct(map.comparator());
var it=map.entrySet().iterator();
if(it.hasNext()){
var entry=it.next();
var last=new java.util.TreeMap.Entry(entry.getKey(),entry.getValue());
this.root=last;
this.$size=1;
while(it.hasNext()){
entry=it.next();
var x=new java.util.TreeMap.Entry(entry.getKey(),entry.getValue());
x.parent=last;
last.right=x;
this.$size++;
this.balance(x);
last=x;
}
}},"java.util.SortedMap");
$_M(c$,"balance",
function(x){
var y;
x.color=true;
while(x!==this.root&&x.parent.color){
if(x.parent===x.parent.parent.left){
y=x.parent.parent.right;
if(y!=null&&y.color){
x.parent.color=false;
y.color=false;
x.parent.parent.color=true;
x=x.parent.parent;
}else{
if(x===x.parent.right){
x=x.parent;
this.leftRotate(x);
}x.parent.color=false;
x.parent.parent.color=true;
this.rightRotate(x.parent.parent);
}}else{
y=x.parent.parent.left;
if(y!=null&&y.color){
x.parent.color=false;
y.color=false;
x.parent.parent.color=true;
x=x.parent.parent;
}else{
if(x===x.parent.left){
x=x.parent;
this.rightRotate(x);
}x.parent.color=false;
x.parent.parent.color=true;
this.leftRotate(x.parent.parent);
}}}
this.root.color=false;
},"java.util.TreeMap.Entry");
$_V(c$,"clear",
function(){
this.root=null;
this.$size=0;
this.modCount++;
});
$_M(c$,"clone",
function(){
try{
var clone=$_U(this,java.util.TreeMap,"clone",[]);
clone.$entrySet=null;
if(this.root!=null){
clone.root=this.root.clone(null);
}return clone;
}catch(e){
if($_O(e,CloneNotSupportedException)){
return null;
}else{
throw e;
}
}
});
$_V(c$,"comparator",
function(){
return this.$comparator;
});
$_V(c$,"containsKey",
function(key){
return this.find(key)!=null;
},"~O");
$_M(c$,"containsValue",
function(value){
if(this.root!=null){
return this.containsValue(this.root,value);
}return false;
},"~O");
$_M(c$,"containsValue",
($fz=function(node,value){
if(value==null?node.value==null:value.equals(node.value)){
return true;
}if(node.left!=null){
if(this.containsValue(node.left,value)){
return true;
}}if(node.right!=null){
if(this.containsValue(node.right,value)){
return true;
}}return false;
},$fz.isPrivate=true,$fz),"java.util.TreeMap.Entry,~O");
$_V(c$,"entrySet",
function(){
if(this.$entrySet==null){
this.$entrySet=(($_D("java.util.TreeMap$1")?0:java.util.TreeMap.$TreeMap$1$()),$_N(java.util.TreeMap$1,this,null));
}return this.$entrySet;
});
$_M(c$,"find",
($fz=function(keyObj){
var result;
var key=keyObj;
var object=null;
if(this.$comparator==null){
object=java.util.TreeMap.toComparable(key);
}var x=this.root;
while(x!=null){
result=object!=null?object.compareTo(x.key):this.$comparator.compare(key,x.key);
if(result==0){
return x;
}x=result<0?x.left:x.right;
}
return null;
},$fz.isPrivate=true,$fz),"~O");
$_M(c$,"findAfter",
function(keyObj){
var key=keyObj;
var result;
var object=null;
if(this.$comparator==null){
object=java.util.TreeMap.toComparable(key);
}var x=this.root;
var last=null;
while(x!=null){
result=object!=null?object.compareTo(x.key):this.$comparator.compare(key,x.key);
if(result==0){
return x;
}if(result<0){
last=x;
x=x.left;
}else{
x=x.right;
}}
return last;
},"~O");
$_M(c$,"findBefore",
function(key){
var result;
var object=null;
if(this.$comparator==null){
object=java.util.TreeMap.toComparable(key);
}var x=this.root;
var last=null;
while(x!=null){
result=object!=null?object.compareTo(x.key):this.$comparator.compare(key,x.key);
if(result<=0){
x=x.left;
}else{
last=x;
x=x.right;
}}
return last;
},"~O");
$_V(c$,"firstKey",
function(){
if(this.root!=null){
return java.util.TreeMap.minimum(this.root).key;
}throw new java.util.NoSuchElementException();
});
$_M(c$,"fixup",
($fz=function(x){
var w;
while(x!==this.root&&!x.color){
if(x===x.parent.left){
w=x.parent.right;
if(w==null){
x=x.parent;
continue;}if(w.color){
w.color=false;
x.parent.color=true;
this.leftRotate(x.parent);
w=x.parent.right;
if(w==null){
x=x.parent;
continue;}}if((w.left==null||!w.left.color)&&(w.right==null||!w.right.color)){
w.color=true;
x=x.parent;
}else{
if(w.right==null||!w.right.color){
w.left.color=false;
w.color=true;
this.rightRotate(w);
w=x.parent.right;
}w.color=x.parent.color;
x.parent.color=false;
w.right.color=false;
this.leftRotate(x.parent);
x=this.root;
}}else{
w=x.parent.left;
if(w==null){
x=x.parent;
continue;}if(w.color){
w.color=false;
x.parent.color=true;
this.rightRotate(x.parent);
w=x.parent.left;
if(w==null){
x=x.parent;
continue;}}if((w.left==null||!w.left.color)&&(w.right==null||!w.right.color)){
w.color=true;
x=x.parent;
}else{
if(w.left==null||!w.left.color){
w.right.color=false;
w.color=true;
this.leftRotate(w);
w=x.parent.left;
}w.color=x.parent.color;
x.parent.color=false;
w.left.color=false;
this.rightRotate(x.parent);
x=this.root;
}}}
x.color=false;
},$fz.isPrivate=true,$fz),"java.util.TreeMap.Entry");
$_V(c$,"get",
function(key){
var node=this.find(key);
if(node!=null){
return node.value;
}return null;
},"~O");
$_V(c$,"headMap",
function(endKey){
if(this.$comparator==null){
java.util.TreeMap.toComparable(endKey).compareTo(endKey);
}else{
this.$comparator.compare(endKey,endKey);
}return new java.util.TreeMap.SubMap(this,endKey);
},"~O");
$_V(c$,"keySet",
function(){
if(this.$keySet==null){
this.$keySet=(($_D("java.util.TreeMap$2")?0:java.util.TreeMap.$TreeMap$2$()),$_N(java.util.TreeMap$2,this,null));
}return this.$keySet;
});
$_V(c$,"lastKey",
function(){
if(this.root!=null){
return java.util.TreeMap.maximum(this.root).key;
}throw new java.util.NoSuchElementException();
});
$_M(c$,"leftRotate",
($fz=function(x){
var y=x.right;
x.right=y.left;
if(y.left!=null){
y.left.parent=x;
}y.parent=x.parent;
if(x.parent==null){
this.root=y;
}else{
if(x===x.parent.left){
x.parent.left=y;
}else{
x.parent.right=y;
}}y.left=x;
x.parent=y;
},$fz.isPrivate=true,$fz),"java.util.TreeMap.Entry");
c$.maximum=$_M(c$,"maximum",
function(x){
while(x.right!=null){
x=x.right;
}
return x;
},"java.util.TreeMap.Entry");
c$.minimum=$_M(c$,"minimum",
function(x){
while(x.left!=null){
x=x.left;
}
return x;
},"java.util.TreeMap.Entry");
c$.predecessor=$_M(c$,"predecessor",
function(x){
if(x.left!=null){
return java.util.TreeMap.maximum(x.left);
}var y=x.parent;
while(y!=null&&x===y.left){
x=y;
y=y.parent;
}
return y;
},"java.util.TreeMap.Entry");
$_V(c$,"put",
function(key,value){
var entry=this.rbInsert(key);
var result=entry.value;
entry.value=value;
return result;
},"~O,~O");
$_M(c$,"rbDelete",
function(z){
var y=z.left==null||z.right==null?z:java.util.TreeMap.successor(z);
var x=y.left!=null?y.left:y.right;
if(x!=null){
x.parent=y.parent;
}if(y.parent==null){
this.root=x;
}else if(y===y.parent.left){
y.parent.left=x;
}else{
y.parent.right=x;
}this.modCount++;
if(y!==z){
z.key=y.key;
z.value=y.value;
}if(!y.color&&this.root!=null){
if(x==null){
this.fixup(y.parent);
}else{
this.fixup(x);
}}this.$size--;
},"java.util.TreeMap.Entry");
$_M(c$,"rbInsert",
($fz=function(object){
var result=0;
var y=null;
if(this.$size!=0){
var key=null;
if(this.$comparator==null){
key=java.util.TreeMap.toComparable(object);
}var x=this.root;
while(x!=null){
y=x;
result=key!=null?key.compareTo(x.key):this.$comparator.compare(object,x.key);
if(result==0){
return x;
}x=result<0?x.left:x.right;
}
}this.$size++;
this.modCount++;
var z=new java.util.TreeMap.Entry(object);
if(y==null){
return this.root=z;
}z.parent=y;
if(result<0){
y.left=z;
}else{
y.right=z;
}this.balance(z);
return z;
},$fz.isPrivate=true,$fz),"~O");
$_V(c$,"remove",
function(key){
var node=this.find(key);
if(node==null){
return null;
}var result=node.value;
this.rbDelete(node);
return result;
},"~O");
$_M(c$,"rightRotate",
($fz=function(x){
var y=x.left;
x.left=y.right;
if(y.right!=null){
y.right.parent=x;
}y.parent=x.parent;
if(x.parent==null){
this.root=y;
}else{
if(x===x.parent.right){
x.parent.right=y;
}else{
x.parent.left=y;
}}y.right=x;
x.parent=y;
},$fz.isPrivate=true,$fz),"java.util.TreeMap.Entry");
$_V(c$,"size",
function(){
return this.$size;
});
$_V(c$,"subMap",
function(startKey,endKey){
if(this.$comparator==null){
if(java.util.TreeMap.toComparable(startKey).compareTo(endKey)<=0){
return new java.util.TreeMap.SubMap(startKey,this,endKey);
}}else{
if(this.$comparator.compare(startKey,endKey)<=0){
return new java.util.TreeMap.SubMap(startKey,this,endKey);
}}throw new IllegalArgumentException();
},"~O,~O");
c$.successor=$_M(c$,"successor",
function(x){
if(x.right!=null){
return java.util.TreeMap.minimum(x.right);
}var y=x.parent;
while(y!=null&&x===y.right){
x=y;
y=y.parent;
}
return y;
},"java.util.TreeMap.Entry");
$_V(c$,"tailMap",
function(startKey){
if(this.$comparator==null){
java.util.TreeMap.toComparable(startKey).compareTo(startKey);
}else{
this.$comparator.compare(startKey,startKey);
}return new java.util.TreeMap.SubMap(startKey,this);
},"~O");
$_V(c$,"values",
function(){
if(this.valuesCollection==null){
this.valuesCollection=(($_D("java.util.TreeMap$3")?0:java.util.TreeMap.$TreeMap$3$()),$_N(java.util.TreeMap$3,this,null));
}return this.valuesCollection;
});
c$.$TreeMap$1$=function(){
$_H();
c$=$_W(java.util,"TreeMap$1",java.util.AbstractSet);
$_V(c$,"size",
function(){
return this.b$["java.util.TreeMap"].$size;
});
$_V(c$,"clear",
function(){
this.b$["java.util.TreeMap"].clear();
});
$_V(c$,"contains",
function(object){
if($_O(object,java.util.Map.Entry)){
var entry=object;
var v1=this.b$["java.util.TreeMap"].get(entry.getKey());
var v2=entry.getValue();
return v1==null?v2==null:v1.equals(v2);
}return false;
},"~O");
$_M(c$,"iterator",
function(){
return new java.util.TreeMap.UnboundedEntryIterator(this.b$["java.util.TreeMap"]);
});
c$=$_P();
};
c$.$TreeMap$2$=function(){
$_H();
c$=$_W(java.util,"TreeMap$2",java.util.AbstractSet);
$_V(c$,"contains",
function(object){
return this.b$["java.util.TreeMap"].containsKey(object);
},"~O");
$_V(c$,"size",
function(){
return this.b$["java.util.TreeMap"].$size;
});
$_V(c$,"clear",
function(){
this.b$["java.util.TreeMap"].clear();
});
$_V(c$,"iterator",
function(){
return new java.util.TreeMap.UnboundedKeyIterator(this.b$["java.util.TreeMap"]);
});
c$=$_P();
};
c$.$TreeMap$3$=function(){
$_H();
c$=$_W(java.util,"TreeMap$3",java.util.AbstractCollection);
$_V(c$,"contains",
function(object){
return this.b$["java.util.TreeMap"].containsValue(object);
},"~O");
$_V(c$,"size",
function(){
return this.b$["java.util.TreeMap"].$size;
});
$_V(c$,"clear",
function(){
this.b$["java.util.TreeMap"].clear();
});
$_V(c$,"iterator",
function(){
return new java.util.TreeMap.UnboundedValueIterator(this.b$["java.util.TreeMap"]);
});
c$=$_P();
};
$_H();
c$=$_C(function(){
this.parent=null;
this.left=null;
this.right=null;
this.color=false;
$_Z(this,arguments);
},java.util.TreeMap,"Entry",java.util.MapEntry);
$_M(c$,"clone",
function(a){
var b=$_U(this,java.util.TreeMap.Entry,"clone",[]);
b.parent=a;
if(this.left!=null){
b.left=this.left.clone(b);
}if(this.right!=null){
b.right=this.right.clone(b);
}return b;
},"java.util.TreeMap.Entry");
c$=$_P();
$_H();
c$=$_C(function(){
this.backingMap=null;
this.expectedModCount=0;
this.node=null;
this.lastNode=null;
$_Z(this,arguments);
},java.util.TreeMap,"AbstractMapIterator");
$_K(c$,
function(a,b){
this.backingMap=a;
this.expectedModCount=a.modCount;
this.node=b;
},"java.util.TreeMap,java.util.TreeMap.Entry");
$_M(c$,"hasNext",
function(){
return this.node!=null;
});
$_M(c$,"remove",
function(){
if(this.expectedModCount==this.backingMap.modCount){
if(this.lastNode!=null){
this.backingMap.rbDelete(this.lastNode);
this.lastNode=null;
this.expectedModCount++;
}else{
throw new IllegalStateException();
}}else{
throw new java.util.ConcurrentModificationException();
}});
$_M(c$,"makeNext",
function(){
if(this.expectedModCount!=this.backingMap.modCount){
throw new java.util.ConcurrentModificationException();
}else if(this.node==null){
throw new java.util.NoSuchElementException();
}this.lastNode=this.node;
this.node=java.util.TreeMap.successor(this.node);
});
c$=$_P();
$_H();
c$=$_T(java.util.TreeMap,"UnboundedEntryIterator",java.util.TreeMap.AbstractMapIterator,java.util.Iterator);
$_K(c$,
function(a){
$_R(this,java.util.TreeMap.UnboundedEntryIterator,[a,a.root==null?null:java.util.TreeMap.minimum(a.root)]);
},"java.util.TreeMap");
$_V(c$,"next",
function(){
this.makeNext();
return this.lastNode;
});
c$=$_P();
$_H();
c$=$_T(java.util.TreeMap,"UnboundedKeyIterator",java.util.TreeMap.AbstractMapIterator,java.util.Iterator);
$_K(c$,
function(a){
$_R(this,java.util.TreeMap.UnboundedKeyIterator,[a,a.root==null?null:java.util.TreeMap.minimum(a.root)]);
},"java.util.TreeMap");
$_V(c$,"next",
function(){
this.makeNext();
return this.lastNode.key;
});
c$=$_P();
$_H();
c$=$_T(java.util.TreeMap,"UnboundedValueIterator",java.util.TreeMap.AbstractMapIterator,java.util.Iterator);
$_K(c$,
function(a){
$_R(this,java.util.TreeMap.UnboundedValueIterator,[a,a.root==null?null:java.util.TreeMap.minimum(a.root)]);
},"java.util.TreeMap");
$_V(c$,"next",
function(){
this.makeNext();
return this.lastNode.value;
});
c$=$_P();
$_H();
c$=$_C(function(){
this.endKey=null;
this.cmp=null;
$_Z(this,arguments);
},java.util.TreeMap,"ComparatorBoundedIterator",java.util.TreeMap.AbstractMapIterator);
$_K(c$,
function(a,b,c){
$_R(this,java.util.TreeMap.ComparatorBoundedIterator,[a,b]);
this.endKey=c;
this.cmp=a.comparator();
},"java.util.TreeMap,java.util.TreeMap.Entry,~O");
$_M(c$,"cleanNext",
function(){
if(this.node!=null&&this.cmp.compare(this.endKey,this.node.key)<=0){
this.node=null;
}});
$_V(c$,"hasNext",
function(){
return(this.node!=null&&this.endKey!=null)&&(this.cmp.compare(this.node.key,this.endKey)<0);
});
c$=$_P();
$_H();
c$=$_T(java.util.TreeMap,"ComparatorBoundedEntryIterator",java.util.TreeMap.ComparatorBoundedIterator,java.util.Iterator);
$_V(c$,"next",
function(){
this.makeNext();
this.cleanNext();
return this.lastNode;
});
c$=$_P();
$_H();
c$=$_T(java.util.TreeMap,"ComparatorBoundedKeyIterator",java.util.TreeMap.ComparatorBoundedIterator,java.util.Iterator);
$_V(c$,"next",
function(){
this.makeNext();
this.cleanNext();
return this.lastNode.key;
});
c$=$_P();
$_H();
c$=$_T(java.util.TreeMap,"ComparatorBoundedValueIterator",java.util.TreeMap.ComparatorBoundedIterator,java.util.Iterator);
$_V(c$,"next",
function(){
this.makeNext();
this.cleanNext();
return this.lastNode.value;
});
c$=$_P();
$_H();
c$=$_C(function(){
this.endKey=null;
$_Z(this,arguments);
},java.util.TreeMap,"ComparableBoundedIterator",java.util.TreeMap.AbstractMapIterator);
$_K(c$,
function(a,b,c){
$_R(this,java.util.TreeMap.ComparableBoundedIterator,[a,b]);
this.endKey=c;
},"java.util.TreeMap,java.util.TreeMap.Entry,Comparable");
$_M(c$,"cleanNext",
function(){
if((this.node!=null)&&(this.endKey.compareTo(this.node.key)<=0)){
this.node=null;
}});
$_V(c$,"hasNext",
function(){
return(this.node!=null)&&(this.endKey.compareTo(this.node.key)>0);
});
c$=$_P();
$_H();
c$=$_T(java.util.TreeMap,"ComparableBoundedEntryIterator",java.util.TreeMap.ComparableBoundedIterator,java.util.Iterator);
$_V(c$,"next",
function(){
this.makeNext();
this.cleanNext();
return this.lastNode;
});
c$=$_P();
$_H();
c$=$_T(java.util.TreeMap,"ComparableBoundedKeyIterator",java.util.TreeMap.ComparableBoundedIterator,java.util.Iterator);
$_V(c$,"next",
function(){
this.makeNext();
this.cleanNext();
return this.lastNode.key;
});
c$=$_P();
$_H();
c$=$_T(java.util.TreeMap,"ComparableBoundedValueIterator",java.util.TreeMap.ComparableBoundedIterator,java.util.Iterator);
$_V(c$,"next",
function(){
this.makeNext();
this.cleanNext();
return this.lastNode.value;
});
c$=$_P();
$_H();
c$=$_C(function(){
this.backingMap=null;
this.hasStart=false;
this.hasEnd=false;
this.startKey=null;
this.endKey=null;
this.$entrySet=null;
$_Z(this,arguments);
},java.util.TreeMap,"SubMap",java.util.AbstractMap,[java.util.SortedMap,java.io.Serializable]);
$_K(c$,
function(a,b){
$_R(this,java.util.TreeMap.SubMap,[]);
this.backingMap=b;
this.hasStart=true;
this.startKey=a;
},"~O,java.util.TreeMap");
$_K(c$,
function(a,b,c){
$_R(this,java.util.TreeMap.SubMap,[]);
this.backingMap=b;
this.hasStart=this.hasEnd=true;
this.startKey=a;
this.endKey=c;
},"~O,java.util.TreeMap,~O");
$_K(c$,
function(a,b){
$_R(this,java.util.TreeMap.SubMap,[]);
this.backingMap=a;
this.hasEnd=true;
this.endKey=b;
},"java.util.TreeMap,~O");
$_V(c$,"comparator",
function(){
return this.backingMap.comparator();
});
$_V(c$,"containsKey",
function(a){
if(this.isInRange(a)){
return this.backingMap.containsKey(a);
}return false;
},"~O");
$_V(c$,"entrySet",
function(){
if(this.$entrySet==null){
this.$entrySet=new java.util.TreeMap.SubMapEntrySet(this);
}return this.$entrySet;
});
$_V(c$,"firstKey",
function(){
var a=this.firstEntry();
if(a!=null){
return a.key;
}throw new java.util.NoSuchElementException();
});
$_M(c$,"firstEntry",
function(){
if(!this.hasStart){
var a=this.backingMap.root;
return(a==null)?null:java.util.TreeMap.minimum(this.backingMap.root);
}var a=this.backingMap.findAfter(this.startKey);
if(a!=null&&this.checkUpperBound(a.key)){
return a;
}return null;
});
$_V(c$,"get",
function(a){
if(this.isInRange(a)){
return this.backingMap.get(a);
}return null;
},"~O");
$_V(c$,"headMap",
function(a){
this.checkRange(a);
if(this.hasStart){
return new java.util.TreeMap.SubMap(this.startKey,this.backingMap,a);
}return new java.util.TreeMap.SubMap(this.backingMap,a);
},"~O");
$_V(c$,"isEmpty",
function(){
if(this.hasStart){
var a=this.backingMap.findAfter(this.startKey);
return a==null||!this.checkUpperBound(a.key);
}return this.backingMap.findBefore(this.endKey)==null;
});
$_V(c$,"keySet",
function(){
if(this.$keySet==null){
this.$keySet=new java.util.TreeMap.SubMapKeySet(this);
}return this.$keySet;
});
$_V(c$,"lastKey",
function(){
if(!this.hasEnd){
return this.backingMap.lastKey();
}var a=this.backingMap.findBefore(this.endKey);
if(a!=null&&this.checkLowerBound(a.key)){
return a.key;
}throw new java.util.NoSuchElementException();
});
$_V(c$,"put",
function(a,b){
if(this.isInRange(a)){
return this.backingMap.put(a,b);
}throw new IllegalArgumentException();
},"~O,~O");
$_V(c$,"remove",
function(a){
if(this.isInRange(a)){
return this.backingMap.remove(a);
}return null;
},"~O");
$_V(c$,"subMap",
function(a,b){
this.checkRange(a);
this.checkRange(b);
var c=this.backingMap.comparator();
if(c==null){
if(java.util.TreeMap.toComparable(a).compareTo(b)<=0){
return new java.util.TreeMap.SubMap(a,this.backingMap,b);
}}else{
if(c.compare(a,b)<=0){
return new java.util.TreeMap.SubMap(a,this.backingMap,b);
}}throw new IllegalArgumentException();
},"~O,~O");
$_V(c$,"tailMap",
function(a){
this.checkRange(a);
if(this.hasEnd){
return new java.util.TreeMap.SubMap(a,this.backingMap,this.endKey);
}return new java.util.TreeMap.SubMap(a,this.backingMap);
},"~O");
$_V(c$,"values",
function(){
if(this.valuesCollection==null){
this.valuesCollection=new java.util.TreeMap.SubMapValuesCollection(this);
}return this.valuesCollection;
});
c$=$_P();
$_H();
c$=$_C(function(){
this.subMap=null;
$_Z(this,arguments);
},java.util.TreeMap,"SubMapEntrySet",java.util.AbstractSet,java.util.Set);
$_K(c$,
function(a){
$_R(this,java.util.TreeMap.SubMapEntrySet,[]);
this.subMap=a;
},"java.util.TreeMap.SubMap");
$_V(c$,"isEmpty",
function(){
return this.subMap.isEmpty();
});
$_V(c$,"iterator",
function(){
var a=this.subMap.firstEntry();
if(this.subMap.hasEnd){
var b=this.subMap.comparator();
if(b==null){
return new java.util.TreeMap.ComparableBoundedEntryIterator(this.subMap.backingMap,a,java.util.TreeMap.toComparable(this.subMap.endKey));
}return new java.util.TreeMap.ComparatorBoundedEntryIterator(this.subMap.backingMap,a,this.subMap.endKey);
}return new java.util.TreeMap.UnboundedEntryIterator(this.subMap.backingMap,a);
});
$_V(c$,"size",
function(){
var a=0;
var b=this.iterator();
while(b.hasNext()){
a++;
b.next();
}
return a;
});
$_V(c$,"contains",
function(a){
if($_O(a,java.util.Map.Entry)){
var b=a;
var c=b.getKey();
if(this.subMap.isInRange(c)){
var d=this.subMap.get(c);
var e=b.getValue();
return d==null?e==null:d.equals(e);
}}return false;
},"~O");
c$=$_P();
$_H();
c$=$_C(function(){
this.subMap=null;
$_Z(this,arguments);
},java.util.TreeMap,"SubMapKeySet",java.util.AbstractSet,java.util.Set);
$_K(c$,
function(a){
$_R(this,java.util.TreeMap.SubMapKeySet,[]);
this.subMap=a;
},"java.util.TreeMap.SubMap");
$_V(c$,"contains",
function(a){
return this.subMap.containsKey(a);
},"~O");
$_V(c$,"isEmpty",
function(){
return this.subMap.isEmpty();
});
$_V(c$,"size",
function(){
var a=0;
var b=this.iterator();
while(b.hasNext()){
a++;
b.next();
}
return a;
});
$_V(c$,"iterator",
function(){
var a=this.subMap.firstEntry();
if(this.subMap.hasEnd){
var b=this.subMap.comparator();
if(b==null){
return new java.util.TreeMap.ComparableBoundedKeyIterator(this.subMap.backingMap,a,java.util.TreeMap.toComparable(this.subMap.endKey));
}return new java.util.TreeMap.ComparatorBoundedKeyIterator(this.subMap.backingMap,a,this.subMap.endKey);
}return new java.util.TreeMap.UnboundedKeyIterator(this.subMap.backingMap,a);
});
c$=$_P();
$_H();
c$=$_C(function(){
this.subMap=null;
$_Z(this,arguments);
},java.util.TreeMap,"SubMapValuesCollection",java.util.AbstractCollection);
$_K(c$,
function(a){
$_R(this,java.util.TreeMap.SubMapValuesCollection,[]);
this.subMap=a;
},"java.util.TreeMap.SubMap");
$_V(c$,"isEmpty",
function(){
return this.subMap.isEmpty();
});
$_V(c$,"iterator",
function(){
var a=this.subMap.firstEntry();
if(this.subMap.hasEnd){
var b=this.subMap.comparator();
if(b==null){
return new java.util.TreeMap.ComparableBoundedValueIterator(this.subMap.backingMap,a,java.util.TreeMap.toComparable(this.subMap.endKey));
}return new java.util.TreeMap.ComparatorBoundedValueIterator(this.subMap.backingMap,a,this.subMap.endKey);
}return new java.util.TreeMap.UnboundedValueIterator(this.subMap.backingMap,a);
});
$_V(c$,"size",
function(){
var a=0;
for(var b=this.iterator();b.hasNext();){
b.next();
a++;
}
return a;
});
c$=$_P();
});
