$_L(["java.util.AbstractCollection","$.Iterator","$.List","$.ListIterator","$.RandomAccess","$.NoSuchElementException"],"java.util.AbstractList",["java.lang.IllegalArgumentException","$.IllegalStateException","$.IndexOutOfBoundsException","$.UnsupportedOperationException","java.util.ConcurrentModificationException"],function(){
c$=$_C(function(){
this.modCount=0;
if(!$_D("java.util.AbstractList.SimpleListIterator")){
java.util.AbstractList.$AbstractList$SimpleListIterator$();
}
if(!$_D("java.util.AbstractList.FullListIterator")){
java.util.AbstractList.$AbstractList$FullListIterator$();
}
$_Z(this,arguments);
},java.util,"AbstractList",java.util.AbstractCollection,java.util.List);
$_M(c$,"add",
function(location,object){
throw new UnsupportedOperationException();
},"~N,~O");
$_M(c$,"add",
function(object){
this.add(this.size(),object);
return true;
},"~O");
$_M(c$,"addAll",
function(location,collection){
var it=collection.iterator();
while(it.hasNext()){
this.add(location++,it.next());
}
return!collection.isEmpty();
},"~N,java.util.Collection");
$_V(c$,"clear",
function(){
this.removeRange(0,this.size());
});
$_V(c$,"equals",
function(object){
if(this===object){
return true;
}if($_O(object,java.util.List)){
var list=object;
if(list.size()!=this.size()){
return false;
}var it1=this.iterator();
var it2=list.iterator();
while(it1.hasNext()){
var e1=it1.next();
var e2=it2.next();
if(!(e1==null?e2==null:e1.equals(e2))){
return false;
}}
return true;
}return false;
},"~O");
$_V(c$,"hashCode",
function(){
var result=1;
var it=this.iterator();
while(it.hasNext()){
var object=it.next();
result=(31*result)+(object==null?0:object.hashCode());
}
return result;
});
$_V(c$,"indexOf",
function(object){
var it=this.listIterator();
if(object!=null){
while(it.hasNext()){
if(object.equals(it.next())){
return it.previousIndex();
}}
}else{
while(it.hasNext()){
if(it.next()==null){
return it.previousIndex();
}}
}return-1;
},"~O");
$_V(c$,"iterator",
function(){
return $_N(java.util.AbstractList.SimpleListIterator,this,null);
});
$_V(c$,"lastIndexOf",
function(object){
var it=this.listIterator(this.size());
if(object!=null){
while(it.hasPrevious()){
if(object.equals(it.previous())){
return it.nextIndex();
}}
}else{
while(it.hasPrevious()){
if(it.previous()==null){
return it.nextIndex();
}}
}return-1;
},"~O");
$_M(c$,"listIterator",
function(){
return this.listIterator(0);
});
$_M(c$,"listIterator",
function(location){
return $_N(java.util.AbstractList.FullListIterator,this,null,location);
},"~N");
$_M(c$,"remove",
function(location){
throw new UnsupportedOperationException();
},"~N");
$_M(c$,"removeRange",
function(start,end){
var it=this.listIterator(start);
for(var i=start;i<end;i++){
it.next();
it.remove();
}
},"~N,~N");
$_V(c$,"set",
function(location,object){
throw new UnsupportedOperationException();
},"~N,~O");
$_V(c$,"subList",
function(start,end){
if(0<=start&&end<=this.size()){
if(start<=end){
if($_O(this,java.util.RandomAccess)){
return new java.util.AbstractList.SubAbstractListRandomAccess(this,start,end);
}return new java.util.AbstractList.SubAbstractList(this,start,end);
}throw new IllegalArgumentException();
}throw new IndexOutOfBoundsException();
},"~N,~N");
c$.$AbstractList$SimpleListIterator$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.pos=-1;
this.expectedModCount=0;
this.lastPosition=-1;
$_Z(this,arguments);
},java.util.AbstractList,"SimpleListIterator",null,java.util.Iterator);
$_K(c$,
function(){
this.expectedModCount=this.b$["java.util.AbstractList"].modCount;
});
$_V(c$,"hasNext",
function(){
return this.pos+1<this.b$["java.util.AbstractList"].size();
});
$_V(c$,"next",
function(){
if(this.expectedModCount==this.b$["java.util.AbstractList"].modCount){
try{
var a=this.b$["java.util.AbstractList"].get(this.pos+1);
this.lastPosition=++this.pos;
return a;
}catch(e){
if($_O(e,IndexOutOfBoundsException)){
throw new java.util.NoSuchElementException();
}else{
throw e;
}
}
}throw new java.util.ConcurrentModificationException();
});
$_V(c$,"remove",
function(){
if(this.expectedModCount==this.b$["java.util.AbstractList"].modCount){
try{
this.b$["java.util.AbstractList"].remove(this.lastPosition);
}catch(e){
if($_O(e,IndexOutOfBoundsException)){
throw new IllegalStateException();
}else{
throw e;
}
}
if(this.b$["java.util.AbstractList"].modCount!=this.expectedModCount){
this.expectedModCount++;
}if(this.pos==this.lastPosition){
this.pos--;
}this.lastPosition=-1;
}else{
throw new java.util.ConcurrentModificationException();
}});
c$=$_P();
};
c$.$AbstractList$FullListIterator$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
$_Z(this,arguments);
},java.util.AbstractList,"FullListIterator",java.util.AbstractList.SimpleListIterator,java.util.ListIterator,$_N(java.util.AbstractList.SimpleListIterator,this,null,$_G));
$_K(c$,
function(a){
$_R(this,java.util.AbstractList.FullListIterator);
if(0<=a&&a<=this.b$["java.util.AbstractList"].size()){
this.pos=a-1;
}else{
throw new IndexOutOfBoundsException();
}},"~N");
$_V(c$,"add",
function(a){
if(this.expectedModCount==this.b$["java.util.AbstractList"].modCount){
try{
this.b$["java.util.AbstractList"].add(this.pos+1,a);
}catch(e){
if($_O(e,IndexOutOfBoundsException)){
throw new java.util.NoSuchElementException();
}else{
throw e;
}
}
this.pos++;
this.lastPosition=-1;
if(this.b$["java.util.AbstractList"].modCount!=this.expectedModCount){
this.expectedModCount++;
}}else{
throw new java.util.ConcurrentModificationException();
}},"~O");
$_V(c$,"hasPrevious",
function(){
return this.pos>=0;
});
$_V(c$,"nextIndex",
function(){
return this.pos+1;
});
$_V(c$,"previous",
function(){
if(this.expectedModCount==this.b$["java.util.AbstractList"].modCount){
try{
var a=this.b$["java.util.AbstractList"].get(this.pos);
this.lastPosition=this.pos;
this.pos--;
return a;
}catch(e){
if($_O(e,IndexOutOfBoundsException)){
throw new java.util.NoSuchElementException();
}else{
throw e;
}
}
}throw new java.util.ConcurrentModificationException();
});
$_V(c$,"previousIndex",
function(){
return this.pos;
});
$_V(c$,"set",
function(a){
if(this.expectedModCount==this.b$["java.util.AbstractList"].modCount){
try{
this.b$["java.util.AbstractList"].set(this.lastPosition,a);
}catch(e){
if($_O(e,IndexOutOfBoundsException)){
throw new IllegalStateException();
}else{
throw e;
}
}
}else{
throw new java.util.ConcurrentModificationException();
}},"~O");
c$=$_P();
};
$_H();
c$=$_T(java.util.AbstractList,"SubAbstractListRandomAccess",java.util.AbstractList.SubAbstractList,java.util.RandomAccess);
c$=$_P();
$_H();
c$=$_C(function(){
this.fullList=null;
this.offset=0;
this.$size=0;
$_Z(this,arguments);
},java.util.AbstractList,"SubAbstractList",java.util.AbstractList);
$_K(c$,
function(a,b,c){
$_R(this,java.util.AbstractList.SubAbstractList);
this.fullList=a;
this.modCount=this.fullList.modCount;
this.offset=b;
this.$size=c-b;
},"java.util.AbstractList,~N,~N");
$_M(c$,"add",
function(a,b){
if(this.modCount==this.fullList.modCount){
if(0<=a&&a<=this.$size){
this.fullList.add(a+this.offset,b);
this.$size++;
this.modCount=this.fullList.modCount;
}else{
throw new IndexOutOfBoundsException();
}}else{
throw new java.util.ConcurrentModificationException();
}},"~N,~O");
$_M(c$,"addAll",
function(a,b){
if(this.modCount==this.fullList.modCount){
if(0<=a&&a<=this.$size){
var c=this.fullList.addAll(a+this.offset,b);
if(c){
this.$size+=b.size();
this.modCount=this.fullList.modCount;
}return c;
}throw new IndexOutOfBoundsException();
}throw new java.util.ConcurrentModificationException();
},"~N,java.util.Collection");
$_M(c$,"addAll",
function(a){
if(this.modCount==this.fullList.modCount){
var b=this.fullList.addAll(this.offset+this.$size,a);
if(b){
this.$size+=a.size();
this.modCount=this.fullList.modCount;
}return b;
}throw new java.util.ConcurrentModificationException();
},"java.util.Collection");
$_M(c$,"get",
function(a){
if(this.modCount==this.fullList.modCount){
if(0<=a&&a<this.$size){
return this.fullList.get(a+this.offset);
}throw new IndexOutOfBoundsException();
}throw new java.util.ConcurrentModificationException();
},"~N");
$_V(c$,"iterator",
function(){
return this.listIterator(0);
});
$_M(c$,"listIterator",
function(a){
if(this.modCount==this.fullList.modCount){
if(0<=a&&a<=this.$size){
return new java.util.AbstractList.SubAbstractList.SubAbstractListIterator(this.fullList.listIterator(a+this.offset),this,this.offset,this.$size);
}throw new IndexOutOfBoundsException();
}throw new java.util.ConcurrentModificationException();
},"~N");
$_M(c$,"remove",
function(a){
if(this.modCount==this.fullList.modCount){
if(0<=a&&a<this.$size){
var b=this.fullList.remove(a+this.offset);
this.$size--;
this.modCount=this.fullList.modCount;
return b;
}throw new IndexOutOfBoundsException();
}throw new java.util.ConcurrentModificationException();
},"~N");
$_M(c$,"removeRange",
function(a,b){
if(a!=b){
if(this.modCount==this.fullList.modCount){
this.fullList.removeRange(a+this.offset,b+this.offset);
this.$size-=b-a;
this.modCount=this.fullList.modCount;
}else{
throw new java.util.ConcurrentModificationException();
}}},"~N,~N");
$_M(c$,"set",
function(a,b){
if(this.modCount==this.fullList.modCount){
if(0<=a&&a<this.$size){
return this.fullList.set(a+this.offset,b);
}throw new IndexOutOfBoundsException();
}throw new java.util.ConcurrentModificationException();
},"~N,~O");
$_V(c$,"size",
function(){
return this.$size;
});
$_M(c$,"sizeChanged",
function(a){
if(a){
this.$size++;
}else{
this.$size--;
}this.modCount=this.fullList.modCount;
},"~B");
$_H();
c$=$_C(function(){
this.subList=null;
this.iterator=null;
this.start=0;
this.end=0;
$_Z(this,arguments);
},java.util.AbstractList.SubAbstractList,"SubAbstractListIterator",null,java.util.ListIterator);
$_K(c$,
function(a,b,c,d){
this.iterator=a;
this.subList=b;
this.start=c;
this.end=this.start+d;
},"java.util.ListIterator,java.util.AbstractList.SubAbstractList,~N,~N");
$_M(c$,"add",
function(a){
this.iterator.add(a);
this.subList.sizeChanged(true);
this.end++;
},"~O");
$_V(c$,"hasNext",
function(){
return this.iterator.nextIndex()<this.end;
});
$_V(c$,"hasPrevious",
function(){
return this.iterator.previousIndex()>=this.start;
});
$_M(c$,"next",
function(){
if(this.iterator.nextIndex()<this.end){
return this.iterator.next();
}throw new java.util.NoSuchElementException();
});
$_M(c$,"nextIndex",
function(){
return this.iterator.nextIndex()-this.start;
});
$_M(c$,"previous",
function(){
if(this.iterator.previousIndex()>=this.start){
return this.iterator.previous();
}throw new java.util.NoSuchElementException();
});
$_M(c$,"previousIndex",
function(){
var a=this.iterator.previousIndex();
if(a>=this.start){
return a-this.start;
}return-1;
});
$_M(c$,"remove",
function(){
this.iterator.remove();
this.subList.sizeChanged(false);
this.end--;
});
$_M(c$,"set",
function(a){
this.iterator.set(a);
},"~O");
c$=$_P();
c$=$_P();
});
