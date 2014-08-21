// modified by Bob Hanson 3/21/2014 6:44:21 AM  to reduce this.b$[....] phrases to simply this.b$H

Clazz.load(["java.util.Dictionary","$.Enumeration","$.Iterator","$.Map","$.MapEntry","$.NoSuchElementException"],"java.util.Hashtable",["java.lang.IllegalArgumentException","$.IllegalStateException","$.NullPointerException","$.StringBuilder","java.util.AbstractCollection","$.AbstractSet","$.Arrays","$.Collections","$.ConcurrentModificationException","java.util.MapEntry.Type"],function(){
c$=Clazz.decorateAsClass(function(){
this.elementCount=0;
this.elementData=null;
this.loadFactor=0;
this.threshold=0;
this.firstSlot=0;
this.lastSlot=-1;
this.modCount=0;
if(!Clazz.isClassDefined("java.util.Hashtable.HashIterator")){
java.util.Hashtable.$Hashtable$HashIterator$();

}
if(!Clazz.isClassDefined("java.util.Hashtable.HashEnumerator")){
java.util.Hashtable.$Hashtable$HashEnumerator$();
}
Clazz.instantialize(this,arguments);
},java.util,"Hashtable",java.util.Dictionary,[java.util.Map,Cloneable,java.io.Serializable]);
c$.newEntry=Clazz.defineMethod(c$,"newEntry",
($fz=function(key,value,hash){
return new java.util.Hashtable.Entry(key,value);
},$fz.isPrivate=true,$fz),"~O,~O,~N");

Clazz.overrideConstructor(c$,
function(){
this.elementCount=0;
this.elementData=this.newElementArray(11);
this.firstSlot=this.elementData.length;
this.loadFactor=0.75;
this.computeMaxSize();
});

Clazz.defineMethod(c$,"newElementArray",
($fz=function(size){
return new Array(size);
},$fz.isPrivate=true,$fz),"~N");
Clazz.overrideMethod(c$,"clear",
function(){
this.elementCount=0;
for (var i = this.elementData.length; --i >= 0;)
	  this.elementData[i] = null;
this.modCount++;
});
Clazz.defineMethod(c$,"clone",
function(){
try{
var hashtable=Clazz.superCall(this,java.util.Hashtable,"clone",[]);
hashtable.elementData=this.elementData.clone();
var entry;
for(var i=this.elementData.length;--i>=0;){
if((entry=this.elementData[i])!=null){
hashtable.elementData[i]=entry.clone();
}}
return hashtable;
}catch(e){
if(Clazz.instanceOf(e,CloneNotSupportedException)){
return null;
}else{
throw e;
}
}
});
Clazz.defineMethod(c$,"computeMaxSize",
($fz=function(){
this.threshold=Math.round((this.elementData.length*this.loadFactor));
},$fz.isPrivate=true,$fz));
Clazz.defineMethod(c$,"contains",
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
Clazz.overrideMethod(c$,"containsKey",
function(key){
return this.getEntry(key)!=null;
},"~O");
Clazz.overrideMethod(c$,"containsValue",
function(value){
return this.contains(value);
},"~O");
Clazz.overrideMethod(c$,"elements",
function(){
if(this.elementCount==0){
return java.util.Hashtable.EMPTY_ENUMERATION;
}return Clazz.innerTypeInstance(java.util.Hashtable.HashEnumerator,this,null,false);
});
Clazz.overrideMethod(c$,"entrySet",
function(){
var b
var a = new java.util.Collections.SynchronizedSet(((Clazz.isClassDefined("java.util.Hashtable$2")?0:java.util.Hashtable.$Hashtable$2$()),b=Clazz.innerTypeInstance(java.util.Hashtable$2,this,null)),this);
b && (b.b$H = b.b$["java.util.Hashtable"]);
return a;
});
Clazz.overrideMethod(c$,"equals",
function(object){
if(this===object){
return true;
}if(Clazz.instanceOf(object,java.util.Map)){
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
Clazz.overrideMethod(c$,"get",
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
Clazz.defineMethod(c$,"getEntry",
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
Clazz.overrideMethod(c$,"hashCode",
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
Clazz.overrideMethod(c$,"isEmpty",
function(){
return this.elementCount==0;
});
Clazz.overrideMethod(c$,"keys",
function(){
if(this.elementCount==0){
return java.util.Hashtable.EMPTY_ENUMERATION;
}return Clazz.innerTypeInstance(java.util.Hashtable.HashEnumerator,this,null,true);
});
Clazz.overrideMethod(c$,"keySet",
function(){
var b
var a = new java.util.Collections.SynchronizedSet(((Clazz.isClassDefined("java.util.Hashtable$3")?0:java.util.Hashtable.$Hashtable$3$()),(b=Clazz.innerTypeInstance(java.util.Hashtable$3,this,null))),this);
b && (b.b$H = b.b$["java.util.Hashtable"]);
return a;
});
Clazz.overrideMethod(c$,"put",
function(key,value){
if(key!=null&&value!=null){
	// BH added ability to use a non-Java key for HTML elements, for example.
	if(!key.hashCode) {
		  var hc = Math.floor(Math.random()*10000000);
		  key.hashCode = function(){return hc};
		  key.equals = function(a){return this.hashCode() == a.hashCode()};
	}
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
Clazz.overrideMethod(c$,"putAll",
function(map){
for(var entry,$entry=map.entrySet().iterator();$entry.hasNext()&&((entry=$entry.next())||true);){
this.put(entry.getKey(),entry.getValue());
}
},"java.util.Map");

Clazz.defineMethod(c$,"rehash",
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
Clazz.overrideMethod(c$,"remove",
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
Clazz.overrideMethod(c$,"size",
function(){
return this.elementCount;
});
Clazz.overrideMethod(c$,"toString",
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
Clazz.overrideMethod(c$,"values",
function(){
var b
var a = new java.util.Collections.SynchronizedCollection(((Clazz.isClassDefined("java.util.Hashtable$4")?0:java.util.Hashtable.$Hashtable$4$()),(b=Clazz.innerTypeInstance(java.util.Hashtable$4,this,null))),this);
b && (b.b$H = b.b$["java.util.Hashtable"]);
return a;
});
c$.$Hashtable$HashIterator$=function(){
Clazz.pu$h(self.c$);
c$=Clazz.decorateAsClass(function(){
Clazz.prepareCallback(this,arguments);
this.position=0;
this.expectedModCount=0;
this.type=null;
this.lastEntry=null;
this.lastPosition=0;
this.canRemove=false;
Clazz.instantialize(this,arguments);
},java.util.Hashtable,"HashIterator",null,java.util.Iterator);
Clazz.makeConstructor(c$,
function(a){
this.type=a;
this.b$H = this.b$["java.util.Hashtable"];
this.position=this.b$H.lastSlot;
this.expectedModCount=this.b$H.modCount;
},"java.util.MapEntry.Type");
Clazz.overrideMethod(c$,"hasNext",
function(){
if(this.lastEntry!=null&&this.lastEntry.next!=null){
return true;
}while(this.position>=this.b$H.firstSlot){
if(this.b$H.elementData[this.position]==null){
this.position--;
}else{
return true;
}}
return false;
});
Clazz.overrideMethod(c$,"next",
function(){
if(this.expectedModCount==this.b$H.modCount){
if(this.lastEntry!=null){
this.lastEntry=this.lastEntry.next;
}if(this.lastEntry==null){
while(this.position>=this.b$H.firstSlot&&(this.lastEntry=this.b$H.elementData[this.position])==null){
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
Clazz.overrideMethod(c$,"remove",
function(){
if(this.expectedModCount==this.b$H.modCount){
if(this.canRemove){
this.canRemove=false;
{
var a=false;
var b=this.b$H.elementData[this.lastPosition];
if(b===this.lastEntry){
this.b$H.elementData[this.lastPosition]=b.next;
a=true;
}else{
while(b!=null&&b.next!==this.lastEntry){
b=b.next;
}
if(b!=null){
b.next=this.lastEntry.next;
a=true;
}}if(a){
this.b$H.modCount++;
this.b$H.elementCount--;
this.expectedModCount++;
return;
}}}else{
throw new IllegalStateException();
}}throw new java.util.ConcurrentModificationException();
});
c$=Clazz.p0p();
};
c$.$Hashtable$HashEnumerator$=function(){
Clazz.pu$h(self.c$);
c$=Clazz.decorateAsClass(function(){
Clazz.prepareCallback(this,arguments);
this.key=false;
this.start=0;
this.entry=null;
Clazz.instantialize(this,arguments);
},java.util.Hashtable,"HashEnumerator",null,java.util.Enumeration);
Clazz.makeConstructor(c$,
function(a){
this.key=a;
this.b$H = this.b$["java.util.Hashtable"];
this.start=this.b$H.lastSlot+1;
},"~B");
Clazz.overrideMethod(c$,"hasMoreElements",
function(){
if(this.entry!=null){
return true;
}
while(--this.start>=this.b$H.firstSlot){
if(this.b$H.elementData[this.start]!=null){
this.entry=this.b$H.elementData[this.start];
return true;
}}
return false;
});
Clazz.overrideMethod(c$,"nextElement",
function(){
if(this.hasMoreElements()){
var a=this.key?this.entry.key:this.entry.value;
this.entry=this.entry.next;
return a;
}throw new java.util.NoSuchElementException();
});
c$=Clazz.p0p();
};
c$.$Hashtable$2$=function(){
// private class EntrySet extends AbstractSet<Map.Entry<K,V>>  
Clazz.pu$h(self.c$);
c$=Clazz.declareAnonymous(java.util,"Hashtable$2",java.util.AbstractSet);
Clazz.overrideMethod(c$,"size",
function(){
return this.b$H.elementCount;
});
Clazz.overrideMethod(c$,"clear",
function(){
this.b$H.clear();
});
Clazz.overrideMethod(c$,"remove",
function(object){
if(this.contains(object)){
this.b$H.remove((object).getKey());
return true;
}return false;
},"~O");
Clazz.defineMethod(c$,"contains",
function(object){
var entry=this.b$H.getEntry((object).getKey());
return object.equals(entry);
},"~O");
Clazz.defineMethod(c$,"iterator",
function(){
return Clazz.innerTypeInstance(java.util.Hashtable.HashIterator,this,null,((Clazz.isClassDefined("java.util.Hashtable$2$1")?0:java.util.Hashtable.$Hashtable$2$1$()),Clazz.innerTypeInstance(java.util.Hashtable$2$1,this,null)));
});
c$=Clazz.p0p();
};
c$.$Hashtable$2$1$=function(){
// EntrySet.HashIterator
Clazz.pu$h(self.c$);
c$=Clazz.declareAnonymous(java.util,"Hashtable$2$1",null,java.util.MapEntry.Type);
Clazz.overrideMethod(c$,"get",
function(entry){
return entry;
},"java.util.MapEntry");
c$=Clazz.p0p();
};
c$.$Hashtable$3$=function(){
// private class KeySet extends AbstractSet<K>
Clazz.pu$h(self.c$);
c$=Clazz.declareAnonymous(java.util,"Hashtable$3",java.util.AbstractSet);
Clazz.overrideMethod(c$,"contains",
function(object){
return this.b$H.containsKey(object);
},"~O");
Clazz.overrideMethod(c$,"size",
function(){
return this.b$H.elementCount;
});
Clazz.overrideMethod(c$,"clear",
function(){
this.b$H.clear();
});
Clazz.overrideMethod(c$,"remove",
function(key){
if(this.b$H.containsKey(key)){
this.b$H.remove(key);
return true;
}return false;
},"~O");
Clazz.overrideMethod(c$,"iterator",
function(){
return Clazz.innerTypeInstance(java.util.Hashtable.HashIterator,this,null,((Clazz.isClassDefined("java.util.Hashtable$3$1")?0:java.util.Hashtable.$Hashtable$3$1$()),Clazz.innerTypeInstance(java.util.Hashtable$3$1,this,null)));
});
c$=Clazz.p0p();
};
c$.$Hashtable$3$1$=function(){
// KeySet.HashIterator
Clazz.pu$h(self.c$);
c$=Clazz.declareAnonymous(java.util,"Hashtable$3$1",null,java.util.MapEntry.Type);
Clazz.overrideMethod(c$,"get",
function(entry){
return entry.key;
},"java.util.MapEntry");
c$=Clazz.p0p();
};
c$.$Hashtable$4$=function(){
// private class ValueCollection extends AbstractCollection<V> 
Clazz.pu$h(self.c$);
c$=Clazz.declareAnonymous(java.util,"Hashtable$4",java.util.AbstractCollection);
Clazz.overrideMethod(c$,"contains",
function(object){
return this.b$H.contains(object);
},"~O");
Clazz.overrideMethod(c$,"size",
function(){
return this.b$H.elementCount;
});
Clazz.overrideMethod(c$,"clear",
function(){
this.b$H.clear();
});
Clazz.overrideMethod(c$,"iterator",
function(){
return Clazz.innerTypeInstance(java.util.Hashtable.HashIterator,this,null,((Clazz.isClassDefined("java.util.Hashtable$4$1")?0:java.util.Hashtable.$Hashtable$4$1$()),Clazz.innerTypeInstance(java.util.Hashtable$4$1,this,null)));
});
c$=Clazz.p0p();
};
c$.$Hashtable$4$1$=function(){
// ValueCollection.HashIterator
Clazz.pu$h(self.c$);
c$=Clazz.declareAnonymous(java.util,"Hashtable$4$1",null,java.util.MapEntry.Type);
Clazz.overrideMethod(c$,"get",
function(entry){
return entry.value;
},"java.util.MapEntry");
c$=Clazz.p0p();
};
c$.$Hashtable$1$=function(){
// private class Enumerator<T> implements Enumeration<T>, Iterator<T>
Clazz.pu$h(self.c$);
c$=Clazz.declareAnonymous(java.util,"Hashtable$1",null,java.util.Enumeration);
Clazz.overrideMethod(c$,"hasMoreElements",
function(){
return false;
});
Clazz.overrideMethod(c$,"nextElement",
function(){
throw new java.util.NoSuchElementException();
});
c$=Clazz.p0p();
};
Clazz.pu$h(self.c$);
c$=Clazz.decorateAsClass(function(){
this.next=null;
this.hashcode=0;
Clazz.instantialize(this,arguments);
},java.util.Hashtable,"Entry",java.util.MapEntry);
Clazz.overrideConstructor(c$,
function(a,b){
	// _k for @j2sOverride
this.key = a;
this.value = b;

//Clazz.superConstructor(this,java.util.Hashtable.Entry,[a,b]);
this.hashcode=a.hashCode();
});
Clazz.defineMethod(c$,"clone",
function(){
var a=Clazz.superCall(this,java.util.Hashtable.Entry,"clone",[]);
if(this.next!=null){
a.next=this.next.clone();

}
return a;
});
Clazz.overrideMethod(c$,"setValue",
function(a){
if(a==null){
throw new NullPointerException();
}var b=this.value;
this.value=a;
return b;
},"~O");
Clazz.defineMethod(c$,"getKeyHash",
function(){
return this.key.hashCode();
});
Clazz.defineMethod(c$,"equalsKey",
function(a,b){
return this.hashcode==a.hashCode()&&this.key.equals(a);
},"~O,~N");
Clazz.overrideMethod(c$,"toString",
function(){
return this.key+"="+this.value;
});
c$=Clazz.p0p();
c$.EMPTY_ENUMERATION=c$.prototype.EMPTY_ENUMERATION=((Clazz.isClassDefined("java.util.Hashtable$1")?0:java.util.Hashtable.$Hashtable$1$()),Clazz.innerTypeInstance(java.util.Hashtable$1,this,null));
});