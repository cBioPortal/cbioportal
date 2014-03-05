$_J("java.util.regex");
$_L(["java.util.regex.MatchResult"],"java.util.regex.Matcher",["java.lang.IllegalArgumentException","$.IndexOutOfBoundsException","$.NullPointerException","$.StringBuffer"],function(){
c$=$_C(function(){
this.pat=null;
this.string=null;
this.leftBound=-1;
this.rightBound=-1;
this.appendPos=0;
this.replacement=null;
this.processedRepl=null;
this.replacementParts=null;
this.results=null;
$_Z(this,arguments);
},java.util.regex,"Matcher",null,java.util.regex.MatchResult);
$_M(c$,"appendReplacement",
function(sb,replacement){
this.processedRepl=this.processReplacement(replacement);
sb.append(this.string.subSequence(this.appendPos,this.start()));
sb.append(this.processedRepl);
this.appendPos=this.end();
return this;
},"StringBuffer,~S");
$_M(c$,"processReplacement",
($fz=function(replacement){
if(this.replacement!=null&&this.replacement.equals(replacement)){
if(this.replacementParts==null){
return this.processedRepl;
}else{
var sb=new StringBuffer();
for(var i=0;i<this.replacementParts.length;i++){
sb.append(this.replacementParts[i]);
}
return sb.toString();
}}else{
this.replacement=replacement;
var repl=replacement.toCharArray();
var res=new StringBuffer();
this.replacementParts=null;
var index=0;
var replacementPos=0;
var nextBackSlashed=false;
while(index<repl.length){
if((repl[index]).charCodeAt(0)==('\\').charCodeAt(0)&&!nextBackSlashed){
nextBackSlashed=true;
index++;
}if(nextBackSlashed){
res.append(repl[index]);
nextBackSlashed=false;
}else{
if((repl[index]).charCodeAt(0)==('$').charCodeAt(0)){
if(this.replacementParts==null){
this.replacementParts=new Array(0);
}try{
var gr=Integer.parseInt(String.instantialize(repl,++index,1));
if(replacementPos!=res.length()){
this.replacementParts[this.replacementParts.length]=res.subSequence(replacementPos,res.length());
replacementPos=res.length();
}this.replacementParts[this.replacementParts.length]=(($_D("java.util.regex.Matcher$1")?0:java.util.regex.Matcher.$Matcher$1$()),$_N(java.util.regex.Matcher$1,this,null));
var group=this.group(gr);
replacementPos+=group.length;
res.append(group);
}catch(e$$){
if($_O(e$$,IndexOutOfBoundsException)){
var iob=e$$;
{
throw iob;
}
}else if($_O(e$$,Exception)){
var e=e$$;
{
throw new IllegalArgumentException("Illegal regular expression format");
}
}else{
throw e$$;
}
}
}else{
res.append(repl[index]);
}}index++;
}
if(this.replacementParts!=null&&replacementPos!=res.length()){
this.replacementParts[this.replacementParts.length]=res.subSequence(replacementPos,res.length());
}return res.toString();
}},$fz.isPrivate=true,$fz),"~S");
$_M(c$,"reset",
function(newSequence){
if(newSequence==null){
throw new NullPointerException("Empty new sequence!");
}this.string=newSequence;
return this.reset();
},"CharSequence");
$_M(c$,"reset",
function(){
this.leftBound=0;
this.rightBound=this.string.length();
this.appendPos=0;
this.replacement=null;
{
var flags=""+(this.pat.regexp.ignoreCase?"i":"")
+(this.pat.regexp.global?"g":"")
+(this.pat.regexp.multiline?"m":"");
this.pat.regexp=new RegExp(this.pat.regexp.source,flags);
}return this;
});
$_M(c$,"region",
function(leftBound,rightBound){
if(leftBound>rightBound||leftBound<0||rightBound<0||leftBound>this.string.length()||rightBound>this.string.length()){
throw new IndexOutOfBoundsException(leftBound+" is out of bound of "+rightBound);
}this.leftBound=leftBound;
this.rightBound=rightBound;
this.results=null;
this.appendPos=0;
this.replacement=null;
return this;
},"~N,~N");
$_M(c$,"appendTail",
function(sb){
return sb.append(this.string.subSequence(this.appendPos,this.string.length()));
},"StringBuffer");
$_M(c$,"replaceFirst",
function(replacement){
this.reset();
if(this.find()){
var sb=new StringBuffer();
this.appendReplacement(sb,replacement);
return this.appendTail(sb).toString();
}return this.string.toString();
},"~S");
$_M(c$,"replaceAll",
function(replacement){
var sb=new StringBuffer();
this.reset();
while(this.find()){
this.appendReplacement(sb,replacement);
}
return this.appendTail(sb).toString();
},"~S");
$_M(c$,"pattern",
function(){
return this.pat;
});
$_M(c$,"group",
function(groupIndex){
if(this.results==null||groupIndex<0||groupIndex>this.results.length){
return null;
}return this.results[groupIndex];
},"~N");
$_M(c$,"group",
function(){
return this.group(0);
});
$_M(c$,"find",
function(startIndex){
var stringLength=this.string.length();
if(startIndex<0||startIndex>stringLength)throw new IndexOutOfBoundsException("Out of bound "+startIndex);
startIndex=this.findAt(startIndex);
return false;
},"~N");
$_M(c$,"findAt",
($fz=function(startIndex){
return-1;
},$fz.isPrivate=true,$fz),"~N");
$_M(c$,"find",
function(){
{
this.results=this.pat.regexp.exec(this.string.subSequence(this.leftBound,this.rightBound));
}return(this.results!=null);
});
$_M(c$,"start",
function(groupIndex){
var beginningIndex=0;
{
beginningIndex=this.pat.regexp.lastIndex;
}beginningIndex-=this.results[0].length;
return beginningIndex;
},"~N");
$_M(c$,"end",
function(groupIndex){
{
return this.pat.regexp.lastIndex;
}return-1;
},"~N");
$_M(c$,"matches",
function(){
return this.find();
});
c$.quoteReplacement=$_M(c$,"quoteReplacement",
function(string){
if(string.indexOf('\\') < 0 && string.indexOf ('$')<0)return string;
var res=new StringBuffer(string.length*2);
var ch;
var len=string.length;
for(var i=0;i<len;i++){
switch(ch=string.charAt(i)){
case'$':
res.append('\\');
res.append('$');
break;
case'\\':
res.append('\\');
res.append('\\');
break;
default:
res.append(ch);
}
}
return res.toString();
},"~S");
$_M(c$,"lookingAt",
function(){
return false;
});
$_M(c$,"start",
function(){
return this.start(0);
});
$_V(c$,"groupCount",
function(){
return this.results==null?0:this.results.length;
});
$_M(c$,"end",
function(){
return this.end(0);
});
$_M(c$,"toMatchResult",
function(){
return this;
});
$_M(c$,"useAnchoringBounds",
function(value){
return this;
},"~B");
$_M(c$,"hasAnchoringBounds",
function(){
return false;
});
$_M(c$,"useTransparentBounds",
function(value){
return this;
},"~B");
$_M(c$,"hasTransparentBounds",
function(){
return false;
});
$_M(c$,"regionStart",
function(){
return this.leftBound;
});
$_M(c$,"regionEnd",
function(){
return this.rightBound;
});
$_M(c$,"requireEnd",
function(){
return false;
});
$_M(c$,"hitEnd",
function(){
return false;
});
$_M(c$,"usePattern",
function(pat){
if(pat==null){
throw new IllegalArgumentException("Empty pattern!");
}this.pat=pat;
this.results=null;
return this;
},"java.util.regex.Pattern");
$_K(c$,
function(pat,cs){
this.pat=pat;
this.string=cs;
this.leftBound=0;
this.rightBound=this.string.toString().length;
},"java.util.regex.Pattern,CharSequence");
c$.$Matcher$1$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.grN=0;
$_Z(this,arguments);
},java.util.regex,"Matcher$1");
$_Y(c$,function(){
this.grN=gr;
});
$_V(c$,"toString",
function(){
return this.b$["java.util.regex.Matcher"].group(this.grN);
});
c$=$_P();
};
$_S(c$,
"MODE_FIND",1,
"MODE_MATCH",2);
});
