$_L(["java.lang.AbstractStringBuilder","$.Appendable"],"java.lang.StringBuffer",["java.lang.Character","$.Double","$.Float","$.Long"],function(){
c$=$_T(java.lang,"StringBuffer",AbstractStringBuilder,[Appendable,java.io.Serializable,CharSequence]);
$_K(c$,
function(cs){
if(cs==null){
throw new NullPointerException();
}
$_R(this,StringBuffer,[cs.toString()]);
},"CharSequence");
$_M(c$,"append",
function(b){
return this.append(b?"true":"false");
},"~B");
$_M(c$,"append",
function(ch){
this.append0(ch);
return this;
},"~N");
$_M(c$,"append",
function(d){
return this.append(Double.toString(d));
},"~N");
$_M(c$,"append",
function(obj){
if(obj==null){
this.appendNull();
}else{
this.append0(obj.toString());
}return this;
},"~O");
$_M(c$,"append",
function(string){
this.append0(string);
return this;
},"~S");
$_M(c$,"append",
function(sb){
if(sb==null){
this.appendNull();
}else{
{
this.append0(sb.getValue(),0,sb.length());
}}return this;
},"StringBuffer");
$_M(c$,"append",
function(chars){
this.append0(chars);
return this;
},"~A");
$_M(c$,"append",
function(chars,start,length){
this.append0(chars,start,length);
return this;
},"~A,~N,~N");
$_M(c$,"append",
function(s){
if(s==null){
this.appendNull();
}else{
this.append0(s.toString());
}return this;
},"CharSequence");
$_M(c$,"append",
function(s,start,end){
this.append0(s,start,end);
return this;
},"CharSequence,~N,~N");
$_M(c$,"appendCodePoint",
function(codePoint){
return this.append(Character.toChars(codePoint));
},"~N");
$_M(c$,"$delete",
function(start,end){
this.delete0(start,end);
return this;
},"~N,~N");
$_M(c$,"deleteCharAt",
function(location){
this.deleteCharAt0(location);
return this;
},"~N");
$_M(c$,"insert",
function(index,ch){
this.insert0(index,ch);
return this;
},"~N,~N");
$_M(c$,"insert",
function(index,b){
return this.insert(index,b?"true":"false");
},"~N,~B");
$_M(c$,"insert",
function(index,i){
return this.insert(index,Integer.toString(i));
},"~N,~N");
$_M(c$,"insert",
function(index,l){
return this.insert(index,Long.toString(l));
},"~N,~N");
$_M(c$,"insert",
function(index,d){
return this.insert(index,Double.toString(d));
},"~N,~N");
$_M(c$,"insert",
function(index,f){
return this.insert(index,Float.toString(f));
},"~N,~N");
$_M(c$,"insert",
function(index,obj){
return this.insert(index,obj==null?"null":obj.toString());
},"~N,~O");
$_M(c$,"insert",
function(index,string){
this.insert0(index,string);
return this;
},"~N,~S");
$_M(c$,"insert",
function(index,chars){
this.insert0(index,chars);
return this;
},"~N,~A");
$_M(c$,"insert",
function(index,chars,start,length){
this.insert0(index,chars,start,length);
return this;
},"~N,~A,~N,~N");
$_M(c$,"insert",
function(index,s){
this.insert0(index,s==null?"null":s.toString());
return this;
},"~N,CharSequence");
$_M(c$,"insert",
function(index,s,start,end){
this.insert0(index,s,start,end);
return this;
},"~N,CharSequence,~N,~N");
$_M(c$,"replace",
function(start,end,string){
this.replace0(start,end,string);
return this;
},"~N,~N,~S");
$_M(c$,"reverse",
function(){
this.reverse0();
return this;
});
$_V(c$,"subSequence",
function(start,end){
return $_U(this,StringBuffer,"substring",[start,end]);
},"~N,~N");
});
