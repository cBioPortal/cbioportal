$_J("java.util.regex");
$_L(null,"java.util.regex.Pattern",["java.lang.IllegalArgumentException","$.StringBuffer","java.util.regex.Matcher"],function(){
c$=$_C(function(){
this.$flags=0;
this.regexp=null;
$_Z(this,arguments);
},java.util.regex,"Pattern",null,java.io.Serializable);
$_M(c$,"matcher",
function(cs){
return new java.util.regex.Matcher(this,cs);
},"CharSequence");
$_M(c$,"split",
function(input,limit){
var res=new Array(0);
var mat=this.matcher(input);
var index=0;
var curPos=0;
if(input.length()==0){
return[""];
}else{
while(mat.find()&&(index+1<limit||limit<=0)){
res[res.length]=input.subSequence(curPos,mat.start()).toString();
curPos=mat.end();
index++;
}
res[res.length]=input.subSequence(curPos,input.length()).toString();
index++;
if(limit==0){
while(--index>=0&&res[index].toString().length==0){
res.length--;
}
}}return res;
},"CharSequence,~N");
$_M(c$,"split",
function(input){
return this.split(input,0);
},"CharSequence");
$_M(c$,"pattern",
function(){
{
return this.regexp.source;
}return null;
});
$_M(c$,"toString",
function(){
return this.pattern();
});
$_M(c$,"flags",
function(){
return this.$flags;
});
c$.compile=$_M(c$,"compile",
function(regex,flags){
if((flags!=0)&&((flags|239)!=239)){
throw new IllegalArgumentException("Illegal flags");
}var flagStr="g";
if((flags&8)!=0){
flagStr+="m";
}if((flags&2)!=0){
flagStr+="i";
}var pattern=new java.util.regex.Pattern();
{
pattern.regexp=new RegExp(regex,flagStr);
}return pattern;
},"~S,~N");
c$.compile=$_M(c$,"compile",
function(pattern){
return java.util.regex.Pattern.compile(pattern,0);
},"~S");
c$.matches=$_M(c$,"matches",
function(regex,input){
return java.util.regex.Pattern.compile(regex).matcher(input).matches();
},"~S,CharSequence");
c$.quote=$_M(c$,"quote",
function(s){
var sb=new StringBuffer().append("\\Q");
var apos=0;
var k;
while((k=s.indexOf("\\E",apos))>=0){
sb.append(s.substring(apos,k+2)).append("\\\\E\\Q");
apos=k+2;
}
return sb.append(s.substring(apos)).append("\\E").toString();
},"~S");
$_K(c$,
($fz=function(){
},$fz.isPrivate=true,$fz));
$_S(c$,
"UNIX_LINES",1,
"CASE_INSENSITIVE",2,
"COMMENTS",4,
"MULTILINE",8,
"LITERAL",16,
"DOTALL",32,
"UNICODE_CASE",64,
"CANON_EQ",128,
"flagsBitMask",239);
});
