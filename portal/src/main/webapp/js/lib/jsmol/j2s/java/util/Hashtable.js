$_L(["java.util.Dictionary","$.Enumeration","$.Iterator","$.Map","$.MapEntry","$.NoSuchElementException"],"java.util.Hashtable",["java.lang.IllegalArgumentException","$.IllegalStateException","$.NullPointerException","$.StringBuilder","java.util.AbstractCollection","$.AbstractSet","$.Arrays","$.Collections","$.ConcurrentModificationException","java.util.MapEntry.Type"],function(){
c$=$_C(function(){
this.elementCount=0;
this.elementData=null;
this.loadFactor=0;
this.threshold=0;
this.firstSlot=0;
this.lastSlot=-1;
this.modCount=0;
if(!$_D("java.util.Hashtable.HashIterator")){
java.util.Hashtable.$Hashtable$HashIterator$();
}
if(!$_D("java.util.Hashtable.HashEnumerator")){
java.util.Hashtable.$Hashtable$HashEnumerator$();
}
$_Z(this,arguments);
},java.util,"Hashtable",java.util.Dictionary,[java.util.Map,Cloneable,java.io.Serializable]);
c$.newEntry=$_M(c$,"newEntry",
($fz=function(key,value,hash){
return new java.util.Hashtable.Entry(key,value);
},$fz.isPrivate=true,$fz),"~O,~O,~N");

$_k(c$,
function(){
this.elementCount=0;
this.elementData=this.newElementArray(11);
this.firstSlot=this.elementData.length;
this.loadFactor=0.75;
this.computeMaxSize();
});

$_M(c$,"newElementArray",
($fz=function(size){
return new Array(size);
},$fz.isPrivate=true,$fz),"~N");
$_V(c$,"clear",
function(){
this.elementCount=0;
for (var i = this.elementData.length; --i >= 0;)
	  this.elementData[i] = null;
this.modCount++;
});
$_M(c$,"clone",
function(){
try{
var hashtable=$_U(this,java.util.Hashtable,"clone",[]);
hashtable.elementData=this.elementData.clone();
var entry;
for(var i=this.elementData.length;--i>=0;){
if((entry=this.elementData[i])!=null){
hashtable.elementData[i]=entry.clone();
}}
return hashtable;
}catch(e){
if($_O(e,CloneNotSupportedException)){
return null;
}else{
throw e;
}
}
});
$_M(c$,"computeMaxSize",
($fz=function(){
this.threshold=Math.round((this.elementData.length*this.loadFactor));
},$fz.isPrivate=true,$fz));
$_M(c$,"contains",
function(value){
if(value==null){
throw new NullPointerException();
}for(var i=this.elementData.length;--i>=0;){
var entry=this.elementData[i];
while(entry!=null){
if(value.equals(entry.value)){
return true;
}entry=entry.next;
}
}
return false;
},"~O");
$_V(c$,"containsKey",
function(key){
return this.getEntry(key)!=null;
},"~O");
$_V(c$,"containsValue",
function(value){
return this.contains(value);
},"~O");
$_V(c$,"elements",
function(){
if(this.elementCount==0){
return java.util.Hashtable.EMPTY_ENUMERATION;
}return $_N(java.util.Hashtable.HashEnumerator,this,null,false);
});
$_V(c$,"entrySet",
function(){
return new java.util.Collections.SynchronizedSet((($_D("java.util.Hashtable$2")?0:java.util.Hashtable.$Hashtable$2$()),$_N(java.util.Hashtable$2,this,null)),this);
});
$_V(c$,"equals",
function(object){
if(this===object){
return true;
}if($_O(object,java.util.Map)){
var map=object;
if(this.size()!=map.size()){
return false;
}var entries=this.entrySet();
for(var e,$e=map.entrySet().iterator();$e.hasNext()&&((e=$e.next())||true);){
if(!entries.contains(e)){
return false;
}}
return true;
}return false;
},"~O");
$_V(c$,"get",
function(key){
var hash=key.hashCode();
var index=(hash&0x7FFFFFFF)%this.elementData.length;
var entry=this.elementData[index];
while(entry!=null){
if(entry.equalsKey(key,hash)){
return entry.value;
}entry=entry.next;
}
return null;
},"~O");
$_M(c$,"getEntry",
function(key){
var hash=key.hashCode();
var index=(hash&0x7FFFFFFF)%this.elementData.length;
var entry=this.elementData[index];
while(entry!=null){
if(entry.equalsKey(key,hash)){
return entry;
}entry=entry.next;
}
return null;
},"~O");
$_V(c$,"hashCode",
function(){
var result=0;
var it=this.entrySet().iterator();
while(it.hasNext()){
var entry=it.next();
var key=entry.getKey();
var value=entry.getValue();
var hash=(key!==this?key.hashCode():0)^(value!==this?(value!=null?value.hashCode():0):0);
result+=hash;
}
return result;
});
$_V(c$,"isEmpty",
function(){
return this.elementCount==0;
});
$_V(c$,"keys",
function(){
if(this.elementCount==0){
return java.util.Hashtable.EMPTY_ENUMERATION;
}return $_N(java.util.Hashtable.HashEnumerator,this,null,true);
});
$_V(c$,"keySet",
function(){
return new java.util.Collections.SynchronizedSet((($_D("java.util.Hashtable$3")?0:java.util.Hashtable.$Hashtable$3$()),$_N(java.util.Hashtable$3,this,null)),this);
});
$_V(c$,"put",
function(key,value){
if(key!=null&&value!=null){
var hash=key.hashCode();
var index=(hash&0x7FFFFFFF)%this.elementData.length;
var entry=this.elementData[index];
while(entry!=null&&!entry.equalsKey(key,hash)){
entry=entry.next;
}
if(entry==null){
this.modCount++;
if(++this.elementCount>this.threshold){
this.rehash();
index=(hash&0x7FFFFFFF)%this.elementData.length;
}if(index<this.firstSlot){
this.firstSlot=index;
}if(index>this.lastSlot){
this.lastSlot=index;
}

entry=java.util.Hashtable.newEntry(key,value,hash);
entry.next=this.elementData[index];
this.elementData[index]=entry;
return null;
}var result=entry.value;
entry.value=value;
return result;
}throw new NullPointerException();
},"~O,~O");
$_V(c$,"putAll",
function(map){
for(var entry,$entry=map.entrySet().iterator();$entry.hasNext()&&((entry=$entry.next())||true);){
this.put(entry.getKey(),entry.getValue());
}
},"java.util.Map");

$_M(c$,"rehash",
function(){
var length=(this.elementData.length<<1)+1;
if(length==0){
length=1;
}var newFirst=length;
var newLast=-1;
var newData=this.newElementArray(length);
for(var i=this.lastSlot+1;--i>=this.firstSlot;){
var entry=this.elementData[i];
while(entry!=null){
var index=(entry.getKeyHash()&0x7FFFFFFF)%length;
if(index<newFirst){
newFirst=index;
}if(index>newLast){
newLast=index;
}var next=entry.next;
entry.next=newData[index];
newData[index]=entry;
entry=next;
}
}
this.firstSlot=newFirst;
this.lastSlot=newLast;
this.elementData=newData;
this.computeMaxSize();
});
$_V(c$,"remove",
function(key){
var hash=key.hashCode();
var index=(hash&0x7FFFFFFF)%this.elementData.length;
var last=null;
var entry=this.elementData[index];
while(entry!=null&&!entry.equalsKey(key,hash)){
last=entry;
entry=entry.next;
}
if(entry!=null){
this.modCount++;
if(last==null){
this.elementData[index]=entry.next;
}else{
last.next=entry.next;
}this.elementCount--;
var result=entry.value;
entry.value=null;
return result;
}return null;
},"~O");
$_V(c$,"size",
function(){
return this.elementCount;
});
$_V(c$,"toString",
function(){
if(this.isEmpty()){
return"{}";
}var buffer=new StringBuilder(this.size()*28);
buffer.append('{');
for(var i=this.lastSlot;i>=this.firstSlot;i--){
var entry=this.elementData[i];
while(entry!=null){
if(entry.key!==this){
buffer.append(entry.key);
}else{
buffer.append("(this Map)");
}buffer.append('=');
if(entry.value!==this){
buffer.append(entry.value);
}else{
buffer.append("(this Map)");
}buffer.append(", ");
entry=entry.next;
}
}
if(this.elementCount>0){
buffer.setLength(buffer.length()-2);
}buffer.append('}');
return buffer.toString();
});
$_V(c$,"values",
function(){
return new java.util.Collections.SynchronizedCollection((($_D("java.util.Hashtable$4")?0:java.util.Hashtable.$Hashtable$4$()),$_N(java.util.Hashtable$4,this,null)),this);
});
c$.$Hashtable$HashIterator$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.position=0;
this.expectedModCount=0;
this.type=null;
this.lastEntry=null;
this.lastPosition=0;
this.canRemove=false;
$_Z(this,arguments);
},java.util.Hashtable,"HashIterator",null,java.util.Iterator);
$_K(c$,
function(a){
this.type=a;
this.position=this.b$["java.util.Hashtable"].lastSlot;
this.expectedModCount=this.b$["java.util.Hashtable"].modCount;
},"java.util.MapEntry.Type");
$_V(c$,"hasNext",
function(){
if(this.lastEntry!=null&&this.lastEntry.next!=null){
return true;
}while(this.position>=this.b$["java.util.Hashtable"].firstSlot){
if(this.b$["java.util.Hashtable"].elementData[this.position]==null){
this.position--;
}else{
return true;
}}
return false;
});
$_V(c$,"next",
function(){
if(this.expectedModCount==this.b$["java.util.Hashtable"].modCount){
if(this.lastEntry!=null){
this.lastEntry=this.lastEntry.next;
}if(this.lastEntry==null){
while(this.position>=this.b$["java.util.Hashtable"].firstSlot&&(this.lastEntry=this.b$["java.util.Hashtable"].elementData[this.position])==null){
this.position--;
}
if(this.lastEntry!=null){
this.lastPosition=this.position;
this.position--;
}}if(this.lastEntry!=null){
this.canRemove=true;
return this.type.get(this.lastEntry);
}throw new java.util.NoSuchElementException();
}throw new java.util.ConcurrentModificationException();
});
$_V(c$,"remove",
function(){
if(this.expectedModCount==this.b$["java.util.Hashtable"].modCount){
if(this.canRemove){
this.canRemove=false;
{
var a=false;
var b=this.b$["java.util.Hashtable"].elementData[this.lastPosition];
if(b===this.lastEntry){
this.b$["java.util.Hashtable"].elementData[this.lastPosition]=b.next;
a=true;
}else{
while(b!=null&&b.next!==this.lastEntry){
b=b.next;
}
if(b!=null){
b.next=this.lastEntry.next;
a=true;
}}if(a){
this.b$["java.util.Hashtable"].modCount++;
this.b$["java.util.Hashtable"].elementCount--;
this.expectedModCount++;
return;
}}}else{
throw new IllegalStateException();
}}throw new java.util.ConcurrentModificationException();
});
c$=$_P();
};
c$.$Hashtable$HashEnumerator$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.key=false;
this.start=0;
this.entry=null;
$_Z(this,arguments);
},java.util.Hashtable,"HashEnumerator",null,java.util.Enumeration);
$_K(c$,
function(a){
this.key=a;
this.start=this.b$["java.util.Hashtable"].lastSlot+1;
},"~B");
$_V(c$,"hasMoreElements",
function(){
if(this.entry!=null){
return true;
}while(--this.start>=this.b$["java.util.Hashtable"].firstSlot){
if(this.b$["java.util.Hashtable"].elementData[this.start]!=null){
this.entry=this.b$["java.util.Hashtable"].elementData[this.start];
return true;
}}
return false;
});
$_V(c$,"nextElement",
function(){
if(this.hasMoreElements()){
var a=this.key?this.entry.key:this.entry.value;
this.entry=this.entry.next;
return a;
}throw new java.util.NoSuchElementException();
});
c$=$_P();
};
c$.$Hashtable$2$=function(){
$_H();
c$=$_W(java.util,"Hashtable$2",java.util.AbstractSet);
$_V(c$,"size",
function(){
return this.b$["java.util.Hashtable"].elementCount;
});
$_V(c$,"clear",
function(){
this.b$["java.util.Hashtable"].clear();
});
$_V(c$,"remove",
function(object){
if(this.contains(object)){
this.b$["java.util.Hashtable"].remove((object).getKey());
return true;
}return false;
},"~O");
$_M(c$,"contains",
function(object){
var entry=this.b$["java.util.Hashtable"].getEntry((object).getKey());
return object.equals(entry);
},"~O");
$_M(c$,"iterator",
function(){
return $_N(java.util.Hashtable.HashIterator,this,null,(($_D("java.util.Hashtable$2$1")?0:java.util.Hashtable.$Hashtable$2$1$()),$_N(java.util.Hashtable$2$1,this,null)));
});
c$=$_P();
};
c$.$Hashtable$2$1$=function(){
$_H();
c$=$_W(java.util,"Hashtable$2$1",null,java.util.MapEntry.Type);
$_V(c$,"get",
function(entry){
return entry;
},"java.util.MapEntry");
c$=$_P();
};
c$.$Hashtable$3$=function(){
$_H();
c$=$_W(java.util,"Hashtable$3",java.util.AbstractSet);
$_V(c$,"contains",
function(object){
return this.b$["java.util.Hashtable"].containsKey(object);
},"~O");
$_V(c$,"size",
function(){
return this.b$["java.util.Hashtable"].elementCount;
});
$_V(c$,"clear",
function(){
this.b$["java.util.Hashtable"].clear();
});
$_V(c$,"remove",
function(key){
if(this.b$["java.util.Hashtable"].containsKey(key)){
this.b$["java.util.Hashtable"].remove(key);
return true;
}return false;
},"~O");
$_V(c$,"iterator",
function(){
return $_N(java.util.Hashtable.HashIterator,this,null,(($_D("java.util.Hashtable$3$1")?0:java.util.Hashtable.$Hashtable$3$1$()),$_N(java.util.Hashtable$3$1,this,null)));
});
c$=$_P();
};
c$.$Hashtable$3$1$=function(){
$_H();
c$=$_W(java.util,"Hashtable$3$1",null,java.util.MapEntry.Type);
$_V(c$,"get",
function(entry){
return entry.key;
},"java.util.MapEntry");
c$=$_P();
};
c$.$Hashtable$4$=function(){
$_H();
c$=$_W(java.util,"Hashtable$4",java.util.AbstractCollection);
$_V(c$,"contains",
function(object){
return this.b$["java.util.Hashtable"].contains(object);
},"~O");
$_V(c$,"size",
function(){
return this.b$["java.util.Hashtable"].elementCount;
});
$_V(c$,"clear",
function(){
this.b$["java.util.Hashtable"].clear();
});
$_V(c$,"iterator",
function(){
return $_N(java.util.Hashtable.HashIterator,this,null,(($_D("java.util.Hashtable$4$1")?0:java.util.Hashtable.$Hashtable$4$1$()),$_N(java.util.Hashtable$4$1,this,null)));
});
c$=$_P();
};
c$.$Hashtable$4$1$=function(){
$_H();
c$=$_W(java.util,"Hashtable$4$1",null,java.util.MapEntry.Type);
$_V(c$,"get",
function(entry){
return entry.value;
},"java.util.MapEntry");
c$=$_P();
};
c$.$Hashtable$1$=function(){
$_H();
c$=$_W(java.util,"Hashtable$1",null,java.util.Enumeration);
$_V(c$,"hasMoreElements",
function(){
return false;
});
$_V(c$,"nextElement",
function(){
throw new java.util.NoSuchElementException();
});
c$=$_P();
};
$_H();
c$=$_C(function(){
this.next=null;
this.hashcode=0;
$_Z(this,arguments);
},java.util.Hashtable,"Entry",java.util.MapEntry);
$_k(c$,
function(a,b){
	// _k for @j2sOverride
this.key = a;
this.value = b;
//$_R(this,java.util.Hashtable.Entry,[a,b]);
this.hashcode=a.hashCode();
});
$_M(c$,"clone",
function(){
var a=$_U(this,java.util.Hashtable.Entry,"clone",[]);
if(this.next!=null){
a.next=this.next.clone();

}
return a;
});
$_V(c$,"setValue",
function(a){
if(a==null){
throw new NullPointerException();
}var b=this.value;
this.value=a;
return b;
},"~O");
$_M(c$,"getKeyHash",
function(){
return this.key.hashCode();
});
$_M(c$,"equalsKey",
function(a,b){
return this.hashcode==a.hashCode()&&this.key.equals(a);
},"~O,~N");
$_V(c$,"toString",
function(){
return this.key+"="+this.value;
});
c$=$_P();
c$.EMPTY_ENUMERATION=c$.prototype.EMPTY_ENUMERATION=(($_D("java.util.Hashtable$1")?0:java.util.Hashtable.$Hashtable$1$()),$_N(java.util.Hashtable$1,this,null));
});
