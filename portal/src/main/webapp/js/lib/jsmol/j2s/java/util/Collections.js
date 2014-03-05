$_L(["java.util.AbstractList","$.AbstractMap","$.AbstractSet","$.Collection","$.Iterator","$.List","$.ListIterator","$.Map","$.RandomAccess","$.Set","$.SortedMap","$.SortedSet","java.lang.NullPointerException","$.UnsupportedOperationException","java.lang.reflect.Array"],"java.util.Collections",["java.lang.ArrayIndexOutOfBoundsException","$.ClassCastException","$.IllegalArgumentException","$.IndexOutOfBoundsException","java.util.ArrayList","$.Arrays","$.Enumeration","java.util.Map.Entry","java.util.NoSuchElementException","$.Random"],function(){
c$=$_T(java.util,"Collections");
c$.binarySearch=$_M(c$,"binarySearch",
function(list,object){
if(list==null){
throw new NullPointerException();
}if(list.isEmpty()){
return-1;
}var key=object;
if(!($_O(list,java.util.RandomAccess))){
var it=list.listIterator();
while(it.hasNext()){
var result;
if((result=key.compareTo(it.next()))<=0){
if(result==0){
return it.previousIndex();
}return-it.previousIndex()-1;
}}
return-list.size()-1;
}var low=0;
var mid=list.size();
var high=mid-1;
var result=-1;
while(low<=high){
mid=(low+high)>>1;
if((result=key.compareTo(list.get(mid)))>0){
low=mid+1;
}else if(result==0){
return mid;
}else{
high=mid-1;
}}
return-mid-(result<0?1:2);
},"java.util.List,~O");
c$.binarySearch=$_M(c$,"binarySearch",
function(list,object,comparator){
if(comparator==null){
return java.util.Collections.binarySearch(list,object);
}if(!($_O(list,java.util.RandomAccess))){
var it=list.listIterator();
while(it.hasNext()){
var result;
if((result=comparator.compare(object,it.next()))<=0){
if(result==0){
return it.previousIndex();
}return-it.previousIndex()-1;
}}
return-list.size()-1;
}var low=0;
var mid=list.size();
var high=mid-1;
var result=-1;
while(low<=high){
mid=(low+high)>>1;
if((result=comparator.compare(object,list.get(mid)))>0){
low=mid+1;
}else if(result==0){
return mid;
}else{
high=mid-1;
}}
return-mid-(result<0?1:2);
},"java.util.List,~O,java.util.Comparator");
c$.copy=$_M(c$,"copy",
function(destination,source){
if(destination.size()<source.size()){
throw new ArrayIndexOutOfBoundsException();
}var srcIt=source.iterator();
var destIt=destination.listIterator();
while(srcIt.hasNext()){
try{
destIt.next();
}catch(e){
if($_O(e,java.util.NoSuchElementException)){
throw new ArrayIndexOutOfBoundsException();
}else{
throw e;
}
}
destIt.set(srcIt.next());
}
},"java.util.List,java.util.List");
c$.enumeration=$_M(c$,"enumeration",
function(collection){
var c=collection;

if (!$_D("java.util.Collections$1"))
	java.util.Collections.$Collections$1$(c);

var x = $_N(java.util.Collections$1,this,null);

return x;
},"java.util.Collection");

c$.fill=$_M(c$,"fill",
function(list,object){
var it=list.listIterator();
while(it.hasNext()){
it.next();
it.set(object);
}
},"java.util.List,~O");
c$.max=$_M(c$,"max",
function(collection){
var it=collection.iterator();
var max=it.next();
while(it.hasNext()){
var next=it.next();
if(max.compareTo(next)<0){
max=next;
}}
return max;
},"java.util.Collection");
c$.max=$_M(c$,"max",
function(collection,comparator){
var it=collection.iterator();
var max=it.next();
while(it.hasNext()){
var next=it.next();
if(comparator.compare(max,next)<0){
max=next;
}}
return max;
},"java.util.Collection,java.util.Comparator");
c$.min=$_M(c$,"min",
function(collection){
var it=collection.iterator();
var min=it.next();
while(it.hasNext()){
var next=it.next();
if(min.compareTo(next)>0){
min=next;
}}
return min;
},"java.util.Collection");
c$.min=$_M(c$,"min",
function(collection,comparator){
var it=collection.iterator();
var min=it.next();
while(it.hasNext()){
var next=it.next();
if(comparator.compare(min,next)>0){
min=next;
}}
return min;
},"java.util.Collection,java.util.Comparator");
c$.nCopies=$_M(c$,"nCopies",
function(length,object){
return new java.util.Collections.CopiesList(length,object);
},"~N,~O");
c$.reverse=$_M(c$,"reverse",
function(list){
var size=list.size();
var front=list.listIterator();
var back=list.listIterator(size);
for(var i=0;i<Math.floor(size/2);i++){
var frontNext=front.next();
var backPrev=back.previous();
front.set(backPrev);
back.set(frontNext);
}
},"java.util.List");
c$.reverseOrder=$_M(c$,"reverseOrder",
function(){
return new java.util.Collections.ReverseComparator();
});
c$.reverseOrder=$_M(c$,"reverseOrder",
function(c){
if(c==null){
return java.util.Collections.reverseOrder();
}return new java.util.Collections.ReverseComparatorWithComparator(c);
},"java.util.Comparator");
c$.shuffle=$_M(c$,"shuffle",
function(list){
java.util.Collections.shuffle(list,new java.util.Random());
},"java.util.List");
c$.shuffle=$_M(c$,"shuffle",
function(list,random){
if(!($_O(list,java.util.RandomAccess))){
var array=list.toArray();
for(var i=array.length-1;i>0;i--){
var index=random.nextInt()%(i+1);
if(index<0){
index=-index;
}var temp=array[i];
array[i]=array[index];
array[index]=temp;
}
var i=0;
var it=list.listIterator();
while(it.hasNext()){
it.next();
it.set(array[i++]);
}
}else{
var rawList=list;
for(var i=rawList.size()-1;i>0;i--){
var index=random.nextInt()%(i+1);
if(index<0){
index=-index;
}rawList.set(index,rawList.set(i,rawList.get(index)));
}
}},"java.util.List,java.util.Random");
c$.singleton=$_M(c$,"singleton",
function(object){
return new java.util.Collections.SingletonSet(object);
},"~O");
c$.singletonList=$_M(c$,"singletonList",
function(object){
return new java.util.Collections.SingletonList(object);
},"~O");
c$.singletonMap=$_M(c$,"singletonMap",
function(key,value){
return new java.util.Collections.SingletonMap(key,value);
},"~O,~O");
c$.sort=$_M(c$,"sort",
function(list){
var array=list.toArray();
java.util.Arrays.sort(array);
var i=0;
var it=list.listIterator();
while(it.hasNext()){
it.next();
it.set(array[i++]);
}
},"java.util.List");
c$.sort=$_M(c$,"sort",
function(list,comparator){
var array=list.toArray(new Array(list.size()));
java.util.Arrays.sort(array,comparator);
var i=0;
var it=list.listIterator();
while(it.hasNext()){
it.next();
it.set(array[i++]);
}
},"java.util.List,java.util.Comparator");
c$.swap=$_M(c$,"swap",
function(list,index1,index2){
if(list==null){
throw new NullPointerException();
}if(index1==index2){
return;
}var rawList=list;
rawList.set(index2,rawList.set(index1,rawList.get(index2)));
},"java.util.List,~N,~N");
c$.replaceAll=$_M(c$,"replaceAll",
function(list,obj,obj2){
var index;
var found=false;
while((index=list.indexOf(obj))>-1){
found=true;
list.set(index,obj2);
}
return found;
},"java.util.List,~O,~O");
c$.rotate=$_M(c$,"rotate",
function(lst,dist){
var list=lst;
var size=list.size();
if(size==0){
return;
}var normdist;
if(dist>0){
normdist=dist%size;
}else{
normdist=size-((dist%size)*(-1));
}if(normdist==0||normdist==size){
return;
}if($_O(list,java.util.RandomAccess)){
var temp=list.get(0);
var index=0;
var beginIndex=0;
for(var i=0;i<size;i++){
index=(index+normdist)%size;
temp=list.set(index,temp);
if(index==beginIndex){
index=++beginIndex;
temp=list.get(beginIndex);
}}
}else{
var divideIndex=(size-normdist)%size;
var sublist1=list.subList(0,divideIndex);
var sublist2=list.subList(divideIndex,size);
java.util.Collections.reverse(sublist1);
java.util.Collections.reverse(sublist2);
java.util.Collections.reverse(list);
}},"java.util.List,~N");
c$.indexOfSubList=$_M(c$,"indexOfSubList",
function(list,sublist){
var size=list.size();
var sublistSize=sublist.size();
if(sublistSize>size){
return-1;
}if(sublistSize==0){
return 0;
}var firstObj=sublist.get(0);
var index=list.indexOf(firstObj);
if(index==-1){
return-1;
}while(index<size&&(size-index>=sublistSize)){
var listIt=list.listIterator(index);
if((firstObj==null)?listIt.next()==null:firstObj.equals(listIt.next())){
var sublistIt=sublist.listIterator(1);
var difFound=false;
while(sublistIt.hasNext()){
var element=sublistIt.next();
if(!listIt.hasNext()){
return-1;
}if((element==null)?listIt.next()!=null:!element.equals(listIt.next())){
difFound=true;
break;
}}
if(!difFound){
return index;
}}index++;
}
return-1;
},"java.util.List,java.util.List");
c$.lastIndexOfSubList=$_M(c$,"lastIndexOfSubList",
function(list,sublist){
var sublistSize=sublist.size();
var size=list.size();
if(sublistSize>size){
return-1;
}if(sublistSize==0){
return size;
}var lastObj=sublist.get(sublistSize-1);
var index=list.lastIndexOf(lastObj);
while((index>-1)&&(index+1>=sublistSize)){
var listIt=list.listIterator(index+1);
if((lastObj==null)?listIt.previous()==null:lastObj.equals(listIt.previous())){
var sublistIt=sublist.listIterator(sublistSize-1);
var difFound=false;
while(sublistIt.hasPrevious()){
var element=sublistIt.previous();
if(!listIt.hasPrevious()){
return-1;
}if((element==null)?listIt.previous()!=null:!element.equals(listIt.previous())){
difFound=true;
break;
}}
if(!difFound){
return listIt.nextIndex();
}}index--;
}
return-1;
},"java.util.List,java.util.List");
c$.list=$_M(c$,"list",
function(enumeration){
var list=new java.util.ArrayList();
while(enumeration.hasMoreElements()){
list.add(enumeration.nextElement());
}
return list;
},"java.util.Enumeration");
c$.synchronizedCollection=$_M(c$,"synchronizedCollection",
function(collection){
if(collection==null){
throw new NullPointerException();
}return new java.util.Collections.SynchronizedCollection(collection);
},"java.util.Collection");
c$.synchronizedList=$_M(c$,"synchronizedList",
function(list){
if(list==null){
throw new NullPointerException();
}if($_O(list,java.util.RandomAccess)){
return new java.util.Collections.SynchronizedRandomAccessList(list);
}return new java.util.Collections.SynchronizedList(list);
},"java.util.List");
c$.synchronizedMap=$_M(c$,"synchronizedMap",
function(map){
if(map==null){
throw new NullPointerException();
}return new java.util.Collections.SynchronizedMap(map);
},"java.util.Map");
c$.synchronizedSet=$_M(c$,"synchronizedSet",
function(set){
if(set==null){
throw new NullPointerException();
}return new java.util.Collections.SynchronizedSet(set);
},"java.util.Set");
c$.synchronizedSortedMap=$_M(c$,"synchronizedSortedMap",
function(map){
if(map==null){
throw new NullPointerException();
}return new java.util.Collections.SynchronizedSortedMap(map);
},"java.util.SortedMap");
c$.synchronizedSortedSet=$_M(c$,"synchronizedSortedSet",
function(set){
if(set==null){
throw new NullPointerException();
}return new java.util.Collections.SynchronizedSortedSet(set);
},"java.util.SortedSet");
c$.unmodifiableCollection=$_M(c$,"unmodifiableCollection",
function(collection){
if(collection==null){
throw new NullPointerException();
}return new java.util.Collections.UnmodifiableCollection(collection);
},"java.util.Collection");
c$.unmodifiableList=$_M(c$,"unmodifiableList",
function(list){
if(list==null){
throw new NullPointerException();
}if($_O(list,java.util.RandomAccess)){
return new java.util.Collections.UnmodifiableRandomAccessList(list);
}return new java.util.Collections.UnmodifiableList(list);
},"java.util.List");
c$.unmodifiableMap=$_M(c$,"unmodifiableMap",
function(map){
if(map==null){
throw new NullPointerException();
}return new java.util.Collections.UnmodifiableMap(map);
},"java.util.Map");
c$.unmodifiableSet=$_M(c$,"unmodifiableSet",
function(set){
if(set==null){
throw new NullPointerException();
}return new java.util.Collections.UnmodifiableSet(set);
},"java.util.Set");
c$.unmodifiableSortedMap=$_M(c$,"unmodifiableSortedMap",
function(map){
if(map==null){
throw new NullPointerException();
}return new java.util.Collections.UnmodifiableSortedMap(map);
},"java.util.SortedMap");
c$.unmodifiableSortedSet=$_M(c$,"unmodifiableSortedSet",
function(set){
if(set==null){
throw new NullPointerException();
}return new java.util.Collections.UnmodifiableSortedSet(set);
},"java.util.SortedSet");
c$.frequency=$_M(c$,"frequency",
function(c,o){
if(c==null){
throw new NullPointerException();
}if(c.isEmpty()){
return 0;
}
var result=0;
var itr=c.iterator();
while(itr.hasNext()){
var e=itr.next();
if(o==null?e==null:o.equals(e)){
result++;
}}
return result;
},"java.util.Collection,~O");

c$.emptyList=$_M(c$,"emptyList",
function(){
return java.util.Collections.EMPTY_LIST;
});
c$.emptySet=$_M(c$,"emptySet",
function(){
return java.util.Collections.EMPTY_SET;
});
c$.emptyMap=$_M(c$,"emptyMap",
function(){
return java.util.Collections.EMPTY_MAP;
});
c$.checkedCollection=$_M(c$,"checkedCollection",
function(c,type){
return new java.util.Collections.CheckedCollection(c,type);
},"java.util.Collection,Class");
c$.checkedMap=$_M(c$,"checkedMap",
function(m,keyType,valueType){
return new java.util.Collections.CheckedMap(m,keyType,valueType);
},"java.util.Map,Class,Class");
c$.checkedList=$_M(c$,"checkedList",
function(list,type){
if($_O(list,java.util.RandomAccess)){
return new java.util.Collections.CheckedRandomAccessList(list,type);
}return new java.util.Collections.CheckedList(list,type);
},"java.util.List,Class");
c$.checkedSet=$_M(c$,"checkedSet",
function(s,type){
return new java.util.Collections.CheckedSet(s,type);
},"java.util.Set,Class");
c$.checkedSortedMap=$_M(c$,"checkedSortedMap",
function(m,keyType,valueType){
return new java.util.Collections.CheckedSortedMap(m,keyType,valueType);
},"java.util.SortedMap,Class,Class");
c$.checkedSortedSet=$_M(c$,"checkedSortedSet",
function(s,type){
return new java.util.Collections.CheckedSortedSet(s,type);
},"java.util.SortedSet,Class");
c$.addAll=$_M(c$,"addAll",
function(c,a){
var modified=false;
for(var i=0;i<a.length;i++){
modified=new Boolean(modified|c.add(a[i])).valueOf();
}
return modified;
},"java.util.Collection,~A");
c$.disjoint=$_M(c$,"disjoint",
function(c1,c2){
if(($_O(c1,java.util.Set))&&!($_O(c2,java.util.Set))||(c2.size())>c1.size()){
var tmp=c1;
c1=c2;
c2=tmp;
}var it=c1.iterator();
while(it.hasNext()){
if(c2.contains(it.next())){
return false;
}}
return true;
},"java.util.Collection,java.util.Collection");
c$.checkType=$_M(c$,"checkType",
function(obj,type){
if(!type.isInstance(obj)){
throw new ClassCastException("Attempt to insert "+obj.getClass()+" element into collection with element type "+type);
}return obj;
},"~O,Class");

c$.$Collections$1$=function(c){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.it=null;
$_Z(this,arguments);
},java.util,"Collections$1",null,java.util.Enumeration);

$_Y(c$,function(){
this.it=c.iterator();
});

$_M(c$,"hasMoreElements",
function(){
return this.it.hasNext();
});
$_M(c$,"nextElement",
function(){
return this.it.next();
});
c$=$_P();
};

$_H();
c$=$_C(function(){
this.n=0;
this.element=null;
$_Z(this,arguments);
},java.util.Collections,"CopiesList",java.util.AbstractList,java.io.Serializable);
$_K(c$,
function(a,b){
$_R(this,java.util.Collections.CopiesList,[]);
if(a<0){
throw new IllegalArgumentException();
}this.n=a;
this.element=b;
},"~N,~O");
$_V(c$,"contains",
function(a){
return this.element==null?a==null:this.element.equals(a);
},"~O");
$_V(c$,"size",
function(){
return this.n;
});
$_V(c$,"get",
function(a){
if(0<=a&&a<this.n){
return this.element;
}throw new IndexOutOfBoundsException();
},"~N");
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"EmptyList",java.util.AbstractList,java.io.Serializable);
$_V(c$,"contains",
function(a){
return false;
},"~O");
$_V(c$,"size",
function(){
return 0;
});
$_V(c$,"get",
function(a){
throw new IndexOutOfBoundsException();
},"~N");
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"EmptySet",java.util.AbstractSet,java.io.Serializable);
$_V(c$,"contains",
function(a){
return false;
},"~O");
$_V(c$,"size",
function(){
return 0;
});
$_V(c$,"iterator",
function(){
return(($_D("java.util.Collections$EmptySet$1")?0:java.util.Collections.EmptySet.$Collections$EmptySet$1$()),$_N(java.util.Collections$EmptySet$1,this,null));
});
c$.$Collections$EmptySet$1$=function(){
$_H();
c$=$_W(java.util,"Collections$EmptySet$1",null,java.util.Iterator);
$_V(c$,"hasNext",
function(){
return false;
});
$_V(c$,"next",
function(){
throw new java.util.NoSuchElementException();
});
$_V(c$,"remove",
function(){
throw new UnsupportedOperationException();
});
c$=$_P();
};
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"EmptyMap",java.util.AbstractMap,java.io.Serializable);
$_V(c$,"containsKey",
function(a){
return false;
},"~O");
$_V(c$,"containsValue",
function(a){
return false;
},"~O");
$_V(c$,"entrySet",
function(){
return java.util.Collections.EMPTY_SET;
});
$_V(c$,"get",
function(a){
return null;
},"~O");
$_V(c$,"keySet",
function(){
return java.util.Collections.EMPTY_SET;
});
$_V(c$,"values",
function(){
return java.util.Collections.EMPTY_LIST;
});
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"ReverseComparator",null,[java.util.Comparator,java.io.Serializable]);
$_V(c$,"compare",
function(a,b){
var c=b;
return c.compareTo(a);
},"~O,~O");
c$=$_P();
$_H();
c$=$_C(function(){
this.comparator=null;
$_Z(this,arguments);
},java.util.Collections,"ReverseComparatorWithComparator",null,[java.util.Comparator,java.io.Serializable]);
$_K(c$,
function(a){
this.comparator=a;
},"java.util.Comparator");
$_M(c$,"compare",
function(a,b){
return this.comparator.compare(b,a);
},"~O,~O");
c$=$_P();
$_H();
c$=$_C(function(){
this.element=null;
$_Z(this,arguments);
},java.util.Collections,"SingletonSet",java.util.AbstractSet,java.io.Serializable);
$_K(c$,
function(a){
$_R(this,java.util.Collections.SingletonSet,[]);
this.element=a;
},"~O");
$_V(c$,"contains",
function(a){
return this.element==null?a==null:this.element.equals(a);
},"~O");
$_V(c$,"size",
function(){
return 1;
});
$_V(c$,"iterator",
function(){
return(($_D("java.util.Collections$SingletonSet$1")?0:java.util.Collections.SingletonSet.$Collections$SingletonSet$1$()),$_N(java.util.Collections$SingletonSet$1,this,null));
});
c$.$Collections$SingletonSet$1$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.$hasNext=true;
$_Z(this,arguments);
},java.util,"Collections$SingletonSet$1",null,java.util.Iterator);
$_V(c$,"hasNext",
function(){
return this.$hasNext;
});
$_V(c$,"next",
function(){
if(this.$hasNext){
this.$hasNext=false;
return this.b$["java.util.Collections.SingletonSet"].element;
}throw new java.util.NoSuchElementException();
});
$_V(c$,"remove",
function(){
throw new UnsupportedOperationException();
});
c$=$_P();
};
c$=$_P();
$_H();
c$=$_C(function(){
this.element=null;
$_Z(this,arguments);
},java.util.Collections,"SingletonList",java.util.AbstractList,java.io.Serializable);
$_K(c$,
function(a){
$_R(this,java.util.Collections.SingletonList,[]);
this.element=a;
},"~O");
$_V(c$,"contains",
function(a){
return this.element==null?a==null:this.element.equals(a);
},"~O");
$_V(c$,"get",
function(a){
if(a==0){
return this.element;
}throw new IndexOutOfBoundsException();
},"~N");
$_V(c$,"size",
function(){
return 1;
});
c$=$_P();
$_H();
c$=$_C(function(){
this.k=null;
this.v=null;
$_Z(this,arguments);
},java.util.Collections,"SingletonMap",java.util.AbstractMap,java.io.Serializable);
$_K(c$,
function(a,b){
$_R(this,java.util.Collections.SingletonMap,[]);
this.k=a;
this.v=b;
},"~O,~O");
$_V(c$,"containsKey",
function(a){
return this.k==null?a==null:this.k.equals(a);
},"~O");
$_V(c$,"containsValue",
function(a){
return this.v==null?a==null:this.v.equals(a);
},"~O");
$_V(c$,"get",
function(a){
if(this.containsKey(a)){
return this.v;
}return null;
},"~O");
$_V(c$,"size",
function(){
return 1;
});
$_V(c$,"entrySet",
function(){
return(($_D("java.util.Collections$SingletonMap$1")?0:java.util.Collections.SingletonMap.$Collections$SingletonMap$1$()),$_N(java.util.Collections$SingletonMap$1,this,null));
});
c$.$Collections$SingletonMap$1$=function(){
$_H();
c$=$_W(java.util,"Collections$SingletonMap$1",java.util.AbstractSet);
$_V(c$,"contains",
function(a){
if($_O(a,java.util.Map.Entry)){
var b=a;
return this.b$["java.util.Collections.SingletonMap"].containsKey(b.getKey())&&this.b$["java.util.Collections.SingletonMap"].containsValue(b.getValue());
}return false;
},"~O");
$_V(c$,"size",
function(){
return 1;
});
$_V(c$,"iterator",
function(){
return(($_D("java.util.Collections$SingletonMap$1$1")?0:java.util.Collections.$Collections$SingletonMap$1$1$()),$_N(java.util.Collections$SingletonMap$1$1,this,null));
});
c$=$_P();
};
c$.$Collections$SingletonMap$1$1$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.$hasNext=true;
$_Z(this,arguments);
},java.util,"Collections$SingletonMap$1$1",null,java.util.Iterator);
$_V(c$,"hasNext",
function(){
return this.$hasNext;
});
$_V(c$,"next",
function(){
if(this.$hasNext){
this.$hasNext=false;
return(($_D("java.util.Collections$SingletonMap$1$1$1")?0:java.util.Collections.$Collections$SingletonMap$1$1$1$()),$_N(java.util.Collections$SingletonMap$1$1$1,this,null));
}throw new java.util.NoSuchElementException();
});
$_V(c$,"remove",
function(){
throw new UnsupportedOperationException();
});
c$=$_P();
};
c$.$Collections$SingletonMap$1$1$1$=function(){
$_H();
c$=$_W(java.util,"Collections$SingletonMap$1$1$1",null,java.util.Map.Entry);
$_V(c$,"equals",
function(a){
return this.b$["java.util.Collections$SingletonMap$1"].contains(a);
},"~O");
$_V(c$,"getKey",
function(){
return this.b$["java.util.Collections.SingletonMap"].k;
});
$_V(c$,"getValue",
function(){
return this.b$["java.util.Collections.SingletonMap"].v;
});
$_V(c$,"hashCode",
function(){
return(this.b$["java.util.Collections.SingletonMap"].k==null?0:this.b$["java.util.Collections.SingletonMap"].k.hashCode())^(this.b$["java.util.Collections.SingletonMap"].v==null?0:this.b$["java.util.Collections.SingletonMap"].v.hashCode());
});
$_V(c$,"setValue",
function(a){
throw new UnsupportedOperationException();
},"~O");
c$=$_P();
};
c$=$_P();
$_H();
c$=$_C(function(){
this.c=null;
this.mutex=null;
$_Z(this,arguments);
},java.util.Collections,"SynchronizedCollection",null,[java.util.Collection,java.io.Serializable]);
$_K(c$,
function(a){
this.c=a;
this.mutex=this;
},"java.util.Collection");
$_K(c$,
function(a,b){
this.c=a;
this.mutex=b;
},"java.util.Collection,~O");
$_M(c$,"add",
function(a){
{
return this.c.add(a);
}},"~O");
$_M(c$,"addAll",
function(a){
{
return this.c.addAll(a);
}},"java.util.Collection");
$_M(c$,"clear",
function(){
{
this.c.clear();
}});
$_M(c$,"contains",
function(a){
{
return this.c.contains(a);
}},"~O");
$_M(c$,"containsAll",
function(a){
{
return this.c.containsAll(a);
}},"java.util.Collection");
$_M(c$,"isEmpty",
function(){
{
return this.c.isEmpty();
}});
$_M(c$,"iterator",
function(){
{
return this.c.iterator();
}});
$_M(c$,"remove",
function(a){
{
return this.c.remove(a);
}},"~O");
$_M(c$,"removeAll",
function(a){
{
return this.c.removeAll(a);
}},"java.util.Collection");
$_M(c$,"retainAll",
function(a){
{
return this.c.retainAll(a);
}},"java.util.Collection");
$_M(c$,"size",
function(){
{
return this.c.size();
}});
$_M(c$,"toArray",
function(){
{
return this.c.toArray();
}});
$_M(c$,"toString",
function(){
{
return this.c.toString();
}});
$_M(c$,"toArray",
function(a){
{
return this.c.toArray(a);
}},"~A");
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"SynchronizedRandomAccessList",java.util.Collections.SynchronizedList,java.util.RandomAccess);
$_V(c$,"subList",
function(a,b){
{
return new java.util.Collections.SynchronizedRandomAccessList(this.list.subList(a,b),this.mutex);
}},"~N,~N");
c$=$_P();
$_H();
c$=$_C(function(){
this.list=null;
$_Z(this,arguments);
},java.util.Collections,"SynchronizedList",java.util.Collections.SynchronizedCollection,java.util.List);
$_K(c$,
function(a){
$_R(this,java.util.Collections.SynchronizedList,[a]);
this.list=a;
},"java.util.List");
$_K(c$,
function(a,b){
$_R(this,java.util.Collections.SynchronizedList,[a,b]);
this.list=a;
},"java.util.List,~O");
$_M(c$,"add",
function(a,b){
{
this.list.add(a,b);
}},"~N,~O");
$_M(c$,"addAll",
function(a,b){
{
return this.list.addAll(a,b);
}},"~N,java.util.Collection");
$_V(c$,"equals",
function(a){
{
return this.list.equals(a);
}},"~O");
$_M(c$,"get",
function(a){
{
return this.list.get(a);
}},"~N");
$_V(c$,"hashCode",
function(){
{
return this.list.hashCode();
}});
$_M(c$,"indexOf",
function(a){
{
return this.list.indexOf(a);
}},"~O");
$_M(c$,"lastIndexOf",
function(a){
{
return this.list.lastIndexOf(a);
}},"~O");
$_M(c$,"listIterator",
function(){
{
return this.list.listIterator();
}});
$_M(c$,"listIterator",
function(a){
{
return this.list.listIterator(a);
}},"~N");
$_M(c$,"remove",
function(a){
{
return this.list.remove(a);
}},"~N");
$_M(c$,"set",
function(a,b){
{
return this.list.set(a,b);
}},"~N,~O");
$_M(c$,"subList",
function(a,b){
{
return new java.util.Collections.SynchronizedList(this.list.subList(a,b),this.mutex);
}},"~N,~N");
c$=$_P();
$_H();
c$=$_C(function(){
this.m=null;
this.mutex=null;
$_Z(this,arguments);
},java.util.Collections,"SynchronizedMap",null,[java.util.Map,java.io.Serializable]);
$_K(c$,
function(a){
this.m=a;
this.mutex=this;
},"java.util.Map");
$_K(c$,
function(a,b){
this.m=a;
this.mutex=b;
},"java.util.Map,~O");
$_M(c$,"clear",
function(){
{
this.m.clear();
}});
$_M(c$,"containsKey",
function(a){
{
return this.m.containsKey(a);
}},"~O");
$_M(c$,"containsValue",
function(a){
{
return this.m.containsValue(a);
}},"~O");
$_M(c$,"entrySet",
function(){
{
return new java.util.Collections.SynchronizedSet(this.m.entrySet(),this.mutex);
}});
$_V(c$,"equals",
function(a){
{
return this.m.equals(a);
}},"~O");
$_M(c$,"get",
function(a){
{
return this.m.get(a);
}},"~O");
$_V(c$,"hashCode",
function(){
{
return this.m.hashCode();
}});
$_M(c$,"isEmpty",
function(){
{
return this.m.isEmpty();
}});
$_M(c$,"keySet",
function(){
{
return new java.util.Collections.SynchronizedSet(this.m.keySet(),this.mutex);
}});
$_M(c$,"put",
function(a,b){
{
return this.m.put(a,b);
}},"~O,~O");
$_M(c$,"putAll",
function(a){
{
this.m.putAll(a);
}},"java.util.Map");
$_M(c$,"remove",
function(a){
{
return this.m.remove(a);
}},"~O");
$_M(c$,"size",
function(){
{
return this.m.size();
}});
$_M(c$,"values",
function(){
{
return new java.util.Collections.SynchronizedCollection(this.m.values(),this.mutex);
}});
$_M(c$,"toString",
function(){
{
return this.m.toString();
}});
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"SynchronizedSet",java.util.Collections.SynchronizedCollection,java.util.Set);
$_V(c$,"equals",
function(a){
{
return this.c.equals(a);
}},"~O");
$_V(c$,"hashCode",
function(){
{
return this.c.hashCode();
}});
c$=$_P();
$_H();
c$=$_C(function(){
this.sm=null;
$_Z(this,arguments);
},java.util.Collections,"SynchronizedSortedMap",java.util.Collections.SynchronizedMap,java.util.SortedMap);
$_K(c$,
function(a){
$_R(this,java.util.Collections.SynchronizedSortedMap,[a]);
this.sm=a;
},"java.util.SortedMap");
$_K(c$,
function(a,b){
$_R(this,java.util.Collections.SynchronizedSortedMap,[a,b]);
this.sm=a;
},"java.util.SortedMap,~O");
$_M(c$,"comparator",
function(){
{
return this.sm.comparator();
}});
$_M(c$,"firstKey",
function(){
{
return this.sm.firstKey();
}});
$_M(c$,"headMap",
function(a){
{
return new java.util.Collections.SynchronizedSortedMap(this.sm.headMap(a),this.mutex);
}},"~O");
$_M(c$,"lastKey",
function(){
{
return this.sm.lastKey();
}});
$_M(c$,"subMap",
function(a,b){
{
return new java.util.Collections.SynchronizedSortedMap(this.sm.subMap(a,b),this.mutex);
}},"~O,~O");
$_M(c$,"tailMap",
function(a){
{
return new java.util.Collections.SynchronizedSortedMap(this.sm.tailMap(a),this.mutex);
}},"~O");
c$=$_P();
$_H();
c$=$_C(function(){
this.ss=null;
$_Z(this,arguments);
},java.util.Collections,"SynchronizedSortedSet",java.util.Collections.SynchronizedSet,java.util.SortedSet);
$_K(c$,
function(a){
$_R(this,java.util.Collections.SynchronizedSortedSet,[a]);
this.ss=a;
},"java.util.SortedSet");
$_K(c$,
function(a,b){
$_R(this,java.util.Collections.SynchronizedSortedSet,[a,b]);
this.ss=a;
},"java.util.SortedSet,~O");
$_M(c$,"comparator",
function(){
{
return this.ss.comparator();
}});
$_M(c$,"first",
function(){
{
return this.ss.first();
}});
$_M(c$,"headSet",
function(a){
{
return new java.util.Collections.SynchronizedSortedSet(this.ss.headSet(a),this.mutex);
}},"~O");
$_M(c$,"last",
function(){
{
return this.ss.last();
}});
$_M(c$,"subSet",
function(a,b){
{
return new java.util.Collections.SynchronizedSortedSet(this.ss.subSet(a,b),this.mutex);
}},"~O,~O");
$_M(c$,"tailSet",
function(a){
{
return new java.util.Collections.SynchronizedSortedSet(this.ss.tailSet(a),this.mutex);
}},"~O");
c$=$_P();
$_H();
c$=$_C(function(){
this.c=null;
$_Z(this,arguments);
},java.util.Collections,"UnmodifiableCollection",null,[java.util.Collection,java.io.Serializable]);
$_K(c$,
function(a){
this.c=a;
},"java.util.Collection");
$_V(c$,"add",
function(a){
throw new UnsupportedOperationException();
},"~O");
$_V(c$,"addAll",
function(a){
throw new UnsupportedOperationException();
},"java.util.Collection");
$_V(c$,"clear",
function(){
throw new UnsupportedOperationException();
});
$_M(c$,"contains",
function(a){
return this.c.contains(a);
},"~O");
$_M(c$,"containsAll",
function(a){
return this.c.containsAll(a);
},"java.util.Collection");
$_M(c$,"isEmpty",
function(){
return this.c.isEmpty();
});
$_M(c$,"iterator",
function(){
return(($_D("java.util.Collections$UnmodifiableCollection$1")?0:java.util.Collections.UnmodifiableCollection.$Collections$UnmodifiableCollection$1$()),$_N(java.util.Collections$UnmodifiableCollection$1,this,null));
});
$_V(c$,"remove",
function(a){
throw new UnsupportedOperationException();
},"~O");
$_V(c$,"removeAll",
function(a){
throw new UnsupportedOperationException();
},"java.util.Collection");
$_V(c$,"retainAll",
function(a){
throw new UnsupportedOperationException();
},"java.util.Collection");
$_M(c$,"size",
function(){
return this.c.size();
});
$_M(c$,"toArray",
function(){
return this.c.toArray();
});
$_M(c$,"toArray",
function(a){
return this.c.toArray(a);
},"~A");
$_M(c$,"toString",
function(){
return this.c.toString();
});
c$.$Collections$UnmodifiableCollection$1$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.iterator=null;
$_Z(this,arguments);
},java.util,"Collections$UnmodifiableCollection$1",null,java.util.Iterator);
$_Y(c$,function(){
this.iterator=this.b$["java.util.Collections.UnmodifiableCollection"].c.iterator();
});
$_M(c$,"hasNext",
function(){
return this.iterator.hasNext();
});
$_M(c$,"next",
function(){
return this.iterator.next();
});
$_V(c$,"remove",
function(){
throw new UnsupportedOperationException();
});
c$=$_P();
};
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"UnmodifiableRandomAccessList",java.util.Collections.UnmodifiableList,java.util.RandomAccess);
$_V(c$,"subList",
function(a,b){
return new java.util.Collections.UnmodifiableRandomAccessList(this.list.subList(a,b));
},"~N,~N");
c$=$_P();
$_H();
c$=$_C(function(){
this.list=null;
$_Z(this,arguments);
},java.util.Collections,"UnmodifiableList",java.util.Collections.UnmodifiableCollection,java.util.List);
$_K(c$,
function(a){
$_R(this,java.util.Collections.UnmodifiableList,[a]);
this.list=a;
},"java.util.List");
$_M(c$,"add",
function(a,b){
throw new UnsupportedOperationException();
},"~N,~O");
$_M(c$,"addAll",
function(a,b){
throw new UnsupportedOperationException();
},"~N,java.util.Collection");
$_V(c$,"equals",
function(a){
return this.list.equals(a);
},"~O");
$_M(c$,"get",
function(a){
return this.list.get(a);
},"~N");
$_V(c$,"hashcode",
function(){
return this.list.hashCode();
});
$_M(c$,"indexOf",
function(a){
return this.list.indexOf(a);
},"~O");
$_M(c$,"lastIndexOf",
function(a){
return this.list.lastIndexOf(a);
},"~O");
$_M(c$,"listIterator",
function(){
return this.listIterator(0);
});
$_M(c$,"listIterator",
function(a){
return(($_D("java.util.Collections$UnmodifiableList$1")?0:java.util.Collections.UnmodifiableList.$Collections$UnmodifiableList$1$()),$_N(java.util.Collections$UnmodifiableList$1,this,null));
},"~N");
$_M(c$,"remove",
function(a){
throw new UnsupportedOperationException();
},"~N");
$_V(c$,"set",
function(a,b){
throw new UnsupportedOperationException();
},"~N,~O");
$_M(c$,"subList",
function(a,b){
return new java.util.Collections.UnmodifiableList(this.list.subList(a,b));
},"~N,~N");
c$.$Collections$UnmodifiableList$1$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.iterator=null;
$_Z(this,arguments);
},java.util,"Collections$UnmodifiableList$1",null,java.util.ListIterator);
$_Y(c$,function(){
this.iterator=this.b$["java.util.Collections.UnmodifiableList"].list.listIterator(location);
});
$_V(c$,"add",
function(b){
throw new UnsupportedOperationException();
},"~O");
$_M(c$,"hasNext",
function(){
return this.iterator.hasNext();
});
$_M(c$,"hasPrevious",
function(){
return this.iterator.hasPrevious();
});
$_M(c$,"next",
function(){
return this.iterator.next();
});
$_M(c$,"nextIndex",
function(){
return this.iterator.nextIndex();
});
$_M(c$,"previous",
function(){
return this.iterator.previous();
});
$_M(c$,"previousIndex",
function(){
return this.iterator.previousIndex();
});
$_V(c$,"remove",
function(){
throw new UnsupportedOperationException();
});
$_V(c$,"set",
function(b){
throw new UnsupportedOperationException();
},"~O");
c$=$_P();
};
c$=$_P();
$_H();
c$=$_C(function(){
this.m=null;
$_Z(this,arguments);
},java.util.Collections,"UnmodifiableMap",null,[java.util.Map,java.io.Serializable]);
$_K(c$,
function(a){
this.m=a;
},"java.util.Map");
$_V(c$,"clear",
function(){
throw new UnsupportedOperationException();
});
$_M(c$,"containsKey",
function(a){
return this.m.containsKey(a);
},"~O");
$_M(c$,"containsValue",
function(a){
return this.m.containsValue(a);
},"~O");
$_M(c$,"entrySet",
function(){
return new java.util.Collections.UnmodifiableMap.UnmodifiableEntrySet(this.m.entrySet());
});
$_V(c$,"equals",
function(a){
return this.m.equals(a);
},"~O");
$_M(c$,"get",
function(a){
return this.m.get(a);
},"~O");
$_V(c$,"hashcode",
function(){
return this.m.hashCode();
});
$_M(c$,"isEmpty",
function(){
return this.m.isEmpty();
});
$_M(c$,"keySet",
function(){
return new java.util.Collections.UnmodifiableSet(this.m.keySet());
});
$_V(c$,"put",
function(a,b){
throw new UnsupportedOperationException();
},"~O,~O");
$_V(c$,"putAll",
function(a){
throw new UnsupportedOperationException();
},"java.util.Map");
$_V(c$,"remove",
function(a){
throw new UnsupportedOperationException();
},"~O");
$_M(c$,"size",
function(){
return this.m.size();
});
$_M(c$,"values",
function(){
return new java.util.Collections.UnmodifiableCollection(this.m.values());
});
$_M(c$,"toString",
function(){
return this.m.toString();
});
$_H();
c$=$_T(java.util.Collections.UnmodifiableMap,"UnmodifiableEntrySet",java.util.Collections.UnmodifiableSet);
$_V(c$,"iterator",
function(){
return(($_D("java.util.Collections$UnmodifiableMap$UnmodifiableEntrySet$1")?0:java.util.Collections.UnmodifiableMap.UnmodifiableEntrySet.$Collections$UnmodifiableMap$UnmodifiableEntrySet$1$()),$_N(java.util.Collections$UnmodifiableMap$UnmodifiableEntrySet$1,this,null));
});
$_M(c$,"toArray",
function(){
var a=this.c.size();
var b=new Array(a);
var c=this.iterator();
for(var d=a;--d>=0;){
b[d]=c.next();
}
return b;
});
$_M(c$,"toArray",
function(a){
var b=this.c.size();
var c=0;
var d=this.iterator();
if(b>a.length){
var e=a.getClass().getComponentType();
a=java.lang.reflect.Array.newInstance(e,b);
}while(c<b){
a[c++]=d.next();
}
if(c<a.length){
a[c]=null;
}return a;
},"~A");
c$.$Collections$UnmodifiableMap$UnmodifiableEntrySet$1$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.iterator=null;
$_Z(this,arguments);
},java.util,"Collections$UnmodifiableMap$UnmodifiableEntrySet$1",null,java.util.Iterator);
$_Y(c$,function(){
this.iterator=this.b$["java.util.Collections.UnmodifiableMap.UnmodifiableEntrySet"].c.iterator();
});
$_M(c$,"hasNext",
function(){
return this.iterator.hasNext();
});
$_M(c$,"next",
function(){
return new java.util.Collections.UnmodifiableMap.UnmodifiableEntrySet.UnmodifiableMapEntry(this.iterator.next());
});
$_V(c$,"remove",
function(){
throw new UnsupportedOperationException();
});
c$=$_P();
};
$_H();
c$=$_C(function(){
this.mapEntry=null;
$_Z(this,arguments);
},java.util.Collections.UnmodifiableMap.UnmodifiableEntrySet,"UnmodifiableMapEntry",null,java.util.Map.Entry);
$_K(c$,
function(a){
this.mapEntry=a;
},"java.util.Map.Entry");
$_V(c$,"equals",
function(a){
return this.mapEntry.equals(a);
},"~O");
$_M(c$,"getKey",
function(){
return this.mapEntry.getKey();
});
$_M(c$,"getValue",
function(){
return this.mapEntry.getValue();
});
$_V(c$,"hashcode",
function(){
return this.mapEntry.hashCode();
});
$_V(c$,"setValue",
function(a){
throw new UnsupportedOperationException();
},"~O");
$_M(c$,"toString",
function(){
return this.mapEntry.toString();
});
c$=$_P();
c$=$_P();
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"UnmodifiableSet",java.util.Collections.UnmodifiableCollection,java.util.Set);
$_V(c$,"equals",
function(a){
return this.c.equals(a);
},"~O");
$_V(c$,"hashCode",
function(){
return this.c.hashCode();
});
c$=$_P();
$_H();
c$=$_C(function(){
this.sm=null;
$_Z(this,arguments);
},java.util.Collections,"UnmodifiableSortedMap",java.util.Collections.UnmodifiableMap,java.util.SortedMap);
$_K(c$,
function(a){
$_R(this,java.util.Collections.UnmodifiableSortedMap,[a]);
this.sm=a;
},"java.util.SortedMap");
$_M(c$,"comparator",
function(){
return this.sm.comparator();
});
$_M(c$,"firstKey",
function(){
return this.sm.firstKey();
});
$_M(c$,"headMap",
function(a){
return new java.util.Collections.UnmodifiableSortedMap(this.sm.headMap(a));
},"~O");
$_M(c$,"lastKey",
function(){
return this.sm.lastKey();
});
$_M(c$,"subMap",
function(a,b){
return new java.util.Collections.UnmodifiableSortedMap(this.sm.subMap(a,b));
},"~O,~O");
$_M(c$,"tailMap",
function(a){
return new java.util.Collections.UnmodifiableSortedMap(this.sm.tailMap(a));
},"~O");
c$=$_P();
$_H();
c$=$_C(function(){
this.ss=null;
$_Z(this,arguments);
},java.util.Collections,"UnmodifiableSortedSet",java.util.Collections.UnmodifiableSet,java.util.SortedSet);
$_K(c$,
function(a){
$_R(this,java.util.Collections.UnmodifiableSortedSet,[a]);
this.ss=a;
},"java.util.SortedSet");
$_M(c$,"comparator",
function(){
return this.ss.comparator();
});
$_M(c$,"first",
function(){
return this.ss.first();
});
$_M(c$,"headSet",
function(a){
return new java.util.Collections.UnmodifiableSortedSet(this.ss.headSet(a));
},"~O");
$_M(c$,"last",
function(){
return this.ss.last();
});
$_M(c$,"subSet",
function(a,b){
return new java.util.Collections.UnmodifiableSortedSet(this.ss.subSet(a,b));
},"~O,~O");
$_M(c$,"tailSet",
function(a){
return new java.util.Collections.UnmodifiableSortedSet(this.ss.tailSet(a));
},"~O");
c$=$_P();
$_H();
c$=$_C(function(){
this.c=null;
this.type=null;
$_Z(this,arguments);
},java.util.Collections,"CheckedCollection",null,[java.util.Collection,java.io.Serializable]);
$_K(c$,
function(a,b){
if(a==null||b==null){
throw new NullPointerException();
}this.c=a;
this.type=b;
},"java.util.Collection,Class");
$_M(c$,"size",
function(){
return this.c.size();
});
$_M(c$,"isEmpty",
function(){
return this.c.isEmpty();
});
$_M(c$,"contains",
function(a){
return this.c.contains(a);
},"~O");
$_M(c$,"iterator",
function(){
var a=this.c.iterator();
if($_O(a,java.util.ListIterator)){
a=new java.util.Collections.CheckedListIterator(a,this.type);
}return a;
});
$_M(c$,"toArray",
function(){
return this.c.toArray();
});
$_M(c$,"toArray",
function(a){
return this.c.toArray(a);
},"~A");
$_M(c$,"add",
function(a){
return this.c.add(java.util.Collections.checkType(a,this.type));
},"~O");
$_M(c$,"remove",
function(a){
return this.c.remove(a);
},"~O");
$_M(c$,"containsAll",
function(a){
return this.c.containsAll(a);
},"java.util.Collection");
$_V(c$,"addAll",
function(a){
var b=a.size();
if(b==0){
return false;
}var c=new Array(b);
var d=a.iterator();
for(var e=0;e<b;e++){
c[e]=java.util.Collections.checkType(d.next(),this.type);
}
var f=false;
for(var g=0;g<b;g++){
f=new Boolean(f|this.c.add(c[g])).valueOf();
}
return f;
},"java.util.Collection");
$_M(c$,"removeAll",
function(a){
return this.c.removeAll(a);
},"java.util.Collection");
$_M(c$,"retainAll",
function(a){
return this.c.retainAll(a);
},"java.util.Collection");
$_M(c$,"clear",
function(){
this.c.clear();
});
$_M(c$,"toString",
function(){
return this.c.toString();
});
c$=$_P();
$_H();
c$=$_C(function(){
this.i=null;
this.type=null;
$_Z(this,arguments);
},java.util.Collections,"CheckedListIterator",null,java.util.ListIterator);
$_K(c$,
function(a,b){
this.i=a;
this.type=b;
},"java.util.ListIterator,Class");
$_M(c$,"hasNext",
function(){
return this.i.hasNext();
});
$_M(c$,"next",
function(){
return this.i.next();
});
$_M(c$,"remove",
function(){
this.i.remove();
});
$_M(c$,"hasPrevious",
function(){
return this.i.hasPrevious();
});
$_M(c$,"previous",
function(){
return this.i.previous();
});
$_M(c$,"nextIndex",
function(){
return this.i.nextIndex();
});
$_M(c$,"previousIndex",
function(){
return this.i.previousIndex();
});
$_M(c$,"set",
function(a){
this.i.set(java.util.Collections.checkType(a,this.type));
},"~O");
$_M(c$,"add",
function(a){
this.i.add(java.util.Collections.checkType(a,this.type));
},"~O");
c$=$_P();
$_H();
c$=$_C(function(){
this.l=null;
$_Z(this,arguments);
},java.util.Collections,"CheckedList",java.util.Collections.CheckedCollection,java.util.List);
$_K(c$,
function(a,b){
$_R(this,java.util.Collections.CheckedList,[a,b]);
this.l=a;
},"java.util.List,Class");
$_M(c$,"addAll",
function(a,b){
var c=b.size();
if(c==0){
return false;
}var d=new Array(c);
var e=b.iterator();
for(var f=0;f<c;f++){
d[f]=java.util.Collections.checkType(e.next(),this.type);
}
return this.l.addAll(a,java.util.Arrays.asList(d));
},"~N,java.util.Collection");
$_M(c$,"get",
function(a){
return this.l.get(a);
},"~N");
$_M(c$,"set",
function(a,b){
return this.l.set(a,java.util.Collections.checkType(b,this.type));
},"~N,~O");
$_M(c$,"add",
function(a,b){
this.l.add(a,java.util.Collections.checkType(b,this.type));
},"~N,~O");
$_M(c$,"remove",
function(a){
return this.l.remove(a);
},"~N");
$_M(c$,"indexOf",
function(a){
return this.l.indexOf(a);
},"~O");
$_M(c$,"lastIndexOf",
function(a){
return this.l.lastIndexOf(a);
},"~O");
$_M(c$,"listIterator",
function(){
return new java.util.Collections.CheckedListIterator(this.l.listIterator(),this.type);
});
$_M(c$,"listIterator",
function(a){
return new java.util.Collections.CheckedListIterator(this.l.listIterator(a),this.type);
},"~N");
$_M(c$,"subList",
function(a,b){
return java.util.Collections.checkedList(this.l.subList(a,b),this.type);
},"~N,~N");
$_V(c$,"equals",
function(a){
return this.l.equals(a);
},"~O");
$_V(c$,"hashcode",
function(){
return this.l.hashCode();
});
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"CheckedRandomAccessList",java.util.Collections.CheckedList,java.util.RandomAccess);
c$=$_P();
$_H();
c$=$_T(java.util.Collections,"CheckedSet",java.util.Collections.CheckedCollection,java.util.Set);
$_V(c$,"equals",
function(a){
return this.c.equals(a);
},"~O");
$_V(c$,"hashCode",
function(){
return this.c.hashCode();
});
c$=$_P();
$_H();
c$=$_C(function(){
this.m=null;
this.keyType=null;
this.valueType=null;
$_Z(this,arguments);
},java.util.Collections,"CheckedMap",null,[java.util.Map,java.io.Serializable]);
$_K(c$,
($fz=function(a,b,c){
if(a==null||b==null||c==null){
throw new NullPointerException();
}this.m=a;
this.keyType=b;
this.valueType=c;
},$fz.isPrivate=true,$fz),"java.util.Map,Class,Class");
$_M(c$,"size",
function(){
return this.m.size();
});
$_M(c$,"isEmpty",
function(){
return this.m.isEmpty();
});
$_M(c$,"containsKey",
function(a){
return this.m.containsKey(a);
},"~O");
$_M(c$,"containsValue",
function(a){
return this.m.containsValue(a);
},"~O");
$_M(c$,"get",
function(a){
return this.m.get(a);
},"~O");
$_M(c$,"put",
function(a,b){
return this.m.put(java.util.Collections.checkType(a,this.keyType),java.util.Collections.checkType(b,this.valueType));
},"~O,~O");
$_M(c$,"remove",
function(a){
return this.m.remove(a);
},"~O");
$_V(c$,"putAll",
function(a){
var b=a.size();
if(b==0){
return;
}var c=new Array(b);
var d=a.entrySet().iterator();
for(var e=0;e<b;e++){
var f=d.next();
java.util.Collections.checkType(f.getKey(),this.keyType);
java.util.Collections.checkType(f.getValue(),this.valueType);
c[e]=f;
}
for(var f=0;f<b;f++){
this.m.put(c[f].getKey(),c[f].getValue());
}
},"java.util.Map");
$_M(c$,"clear",
function(){
this.m.clear();
});
$_M(c$,"keySet",
function(){
return this.m.keySet();
});
$_M(c$,"values",
function(){
return this.m.values();
});
$_M(c$,"entrySet",
function(){
return new java.util.Collections.CheckedMap.CheckedEntrySet(this.m.entrySet(),this.valueType);
});
$_V(c$,"equals",
function(a){
return this.m.equals(a);
},"~O");
$_V(c$,"hashcode",
function(){
return this.m.hashCode();
});
$_M(c$,"toString",
function(){
return this.m.toString();
});
$_H();
c$=$_C(function(){
this.e=null;
this.valueType=null;
$_Z(this,arguments);
},java.util.Collections.CheckedMap,"CheckedEntry",null,java.util.Map.Entry);
$_K(c$,
function(a,b){
if(a==null){
throw new NullPointerException();
}this.e=a;
this.valueType=b;
},"java.util.Map.Entry,Class");
$_M(c$,"getKey",
function(){
return this.e.getKey();
});
$_M(c$,"getValue",
function(){
return this.e.getValue();
});
$_M(c$,"setValue",
function(a){
return this.e.setValue(java.util.Collections.checkType(a,this.valueType));
},"~O");
$_V(c$,"equals",
function(a){
return this.e.equals(a);
},"~O");
$_V(c$,"hashcode",
function(){
return this.e.hashCode();
});
c$=$_P();
$_H();
c$=$_C(function(){
this.s=null;
this.valueType=null;
$_Z(this,arguments);
},java.util.Collections.CheckedMap,"CheckedEntrySet",null,java.util.Set);
$_K(c$,
function(a,b){
this.s=a;
this.valueType=b;
},"java.util.Set,Class");
$_M(c$,"iterator",
function(){
return new java.util.Collections.CheckedMap.CheckedEntrySet.CheckedEntryIterator(this.s.iterator(),this.valueType);
});
$_M(c$,"toArray",
function(){
var a=this.size();
var b=new Array(a);
var c=this.iterator();
for(var d=0;d<a;d++){
b[d]=c.next();
}
return b;
});
$_M(c$,"toArray",
function(a){
var b=this.size();
if(a.length<b){
var c=a.getClass().getComponentType();
a=java.lang.reflect.Array.newInstance(c,b);
}var c=this.iterator();
for(var d=0;d<b;d++){
a[d]=c.next();
}
if(b<a.length){
a[b]=null;
}return a;
},"~A");
$_M(c$,"retainAll",
function(a){
return this.s.retainAll(a);
},"java.util.Collection");
$_M(c$,"removeAll",
function(a){
return this.s.removeAll(a);
},"java.util.Collection");
$_M(c$,"containsAll",
function(a){
return this.s.containsAll(a);
},"java.util.Collection");
$_V(c$,"addAll",
function(a){
throw new UnsupportedOperationException();
},"java.util.Collection");
$_M(c$,"remove",
function(a){
return this.s.remove(a);
},"~O");
$_M(c$,"contains",
function(a){
return this.s.contains(a);
},"~O");
$_V(c$,"add",
function(a){
throw new UnsupportedOperationException();
},"java.util.Map.Entry");
$_M(c$,"isEmpty",
function(){
return this.s.isEmpty();
});
$_M(c$,"clear",
function(){
this.s.clear();
});
$_M(c$,"size",
function(){
return this.s.size();
});
$_V(c$,"hashcode",
function(){
return this.s.hashCode();
});
$_V(c$,"equals",
function(a){
return this.s.equals(a);
},"~O");
$_H();
c$=$_C(function(){
this.i=null;
this.valueType=null;
$_Z(this,arguments);
},java.util.Collections.CheckedMap.CheckedEntrySet,"CheckedEntryIterator",null,java.util.Iterator);
$_K(c$,
function(a,b){
this.i=a;
this.valueType=b;
},"java.util.Iterator,Class");
$_M(c$,"hasNext",
function(){
return this.i.hasNext();
});
$_M(c$,"remove",
function(){
this.i.remove();
});
$_M(c$,"next",
function(){
return new java.util.Collections.CheckedMap.CheckedEntry(this.i.next(),this.valueType);
});
c$=$_P();
c$=$_P();
c$=$_P();
$_H();
c$=$_C(function(){
this.ss=null;
$_Z(this,arguments);
},java.util.Collections,"CheckedSortedSet",java.util.Collections.CheckedSet,java.util.SortedSet);
$_K(c$,
function(a,b){
$_R(this,java.util.Collections.CheckedSortedSet,[a,b]);
this.ss=a;
},"java.util.SortedSet,Class");
$_M(c$,"comparator",
function(){
return this.ss.comparator();
});
$_M(c$,"subSet",
function(a,b){
return new java.util.Collections.CheckedSortedSet(this.ss.subSet(a,b),this.type);
},"~O,~O");
$_M(c$,"headSet",
function(a){
return new java.util.Collections.CheckedSortedSet(this.ss.headSet(a),this.type);
},"~O");
$_M(c$,"tailSet",
function(a){
return new java.util.Collections.CheckedSortedSet(this.ss.tailSet(a),this.type);
},"~O");
$_M(c$,"first",
function(){
return this.ss.first();
});
$_M(c$,"last",
function(){
return this.ss.last();
});
c$=$_P();
$_H();
c$=$_C(function(){
this.sm=null;
$_Z(this,arguments);
},java.util.Collections,"CheckedSortedMap",java.util.Collections.CheckedMap,java.util.SortedMap);
$_K(c$,
function(a,b,c){
$_R(this,java.util.Collections.CheckedSortedMap,[a,b,c]);
this.sm=a;
},"java.util.SortedMap,Class,Class");
$_M(c$,"comparator",
function(){
return this.sm.comparator();
});
$_M(c$,"subMap",
function(a,b){
return new java.util.Collections.CheckedSortedMap(this.sm.subMap(a,b),this.keyType,this.valueType);
},"~O,~O");
$_M(c$,"headMap",
function(a){
return new java.util.Collections.CheckedSortedMap(this.sm.headMap(a),this.keyType,this.valueType);
},"~O");
$_M(c$,"tailMap",
function(a){
return new java.util.Collections.CheckedSortedMap(this.sm.tailMap(a),this.keyType,this.valueType);
},"~O");
$_M(c$,"firstKey",
function(){
return this.sm.firstKey();
});
$_M(c$,"lastKey",
function(){
return this.sm.lastKey();
});
c$=$_P();
c$.EMPTY_LIST=c$.prototype.EMPTY_LIST=new java.util.Collections.EmptyList();
c$.EMPTY_SET=c$.prototype.EMPTY_SET=new java.util.Collections.EmptySet();
c$.EMPTY_MAP=c$.prototype.EMPTY_MAP=new java.util.Collections.EmptyMap();
});
