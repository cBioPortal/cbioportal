/**
 * jQuery VGrid v0.1.11 - variable grid layout plugin
 *
 * Terms of Use - jQuery VGrid
 * under the MIT (http://www.opensource.org/licenses/mit-license.php) License.
 *
 * Copyright 2009-2013 xlune.com All rights reserved.
 * (http://blog.xlune.com/2009/09/jqueryvgrid.html)
 */
;(function(i){function a(v){var s=v.data("_vgchild");var r=v.width();var y=[[0,r,0]];var w=0,u,t,x;s.each(function(z){u=i(this);if(u.css("display")==="none"){return true;
}t=q(u);x=f(y,t[0]);y=m(y,x,t);w=Math.max(w,x[1]+t[1]);u.data("_vgleft",x[0]);u.data("_vgtop",x[1]);});v.data("_vgwrapheight",w);g(v);}function f(v,u){var w=v.concat().sort(k);
var r=w[w.length-1][2];for(var t=0,s=w.length;t<s;t++){if(w[t][2]>=r){break;}if(w[t][1]-w[t][0]>=u){return[w[t][0],w[t][2]];}}return[0,r];}function m(v,r,u){var w=v.concat().sort(k);
var x=[r[0],r[0]+u[0],r[1]+u[1]];for(var t=0,s=w.length;t<s;t++){if(x[0]<=w[t][0]&&w[t][1]<=x[1]){delete w[t];}else{w[t]=b(w[t],x);}}return d(w,x);}function k(s,r){if(!s||!r){return 0;
}return((s[2]===r[2]&&s[0]>r[0])||s[2]>r[2])?1:-1;}function c(s,r){if(!s||!r){return 0;}return(s[0]>r[0])?1:-1;}function d(v,r){var w=v.concat([r]).sort(c);
var s=[];for(var u=0,t=w.length;u<t;u++){if(!w[u]){continue;}if(s.length>0&&s[s.length-1][1]===w[u][0]&&s[s.length-1][2]===w[u][2]){s[s.length-1][1]=w[u][1];
}else{s.push(w[u]);}}return s;}function b(s,r){if(s[0]>=r[0]&&s[0]<r[1]||s[1]>=r[0]&&s[1]<r[1]){if(s[0]>=r[0]&&s[0]<r[1]){s[0]=r[1];}else{s[1]=r[0];}}return s;
}function q(t){var r=t.width();var s=t.height();r+=h(t.css("margin-left"))+h(t.css("padding-left"))+h(t.css("border-left-width"))+h(t.css("margin-right"))+h(t.css("padding-right"))+h(t.css("border-right-width"));
s+=h(t.css("margin-top"))+h(t.css("padding-top"))+h(t.css("border-top-width"))+h(t.css("margin-bottom"))+h(t.css("padding-bottom"))+h(t.css("border-bottom-width"));
return[r,s];}function h(s){var r=parseInt(s,10);if(isNaN(r)){return 0;}return r;}function e(s,r){r=typeof(r)==="number"&&isFinite(r)?r:0;s=typeof(s)==="number"&&isFinite(s)?s:r;
return s;}function g(s){var r=s;var u=e(r.data("_vgopt").time,500);var t=r.data("_vgchild").length*(r.data("_vgopt").delay||0)+u;r.stop(false,true);if(r.height()<r.data("_vgwrapheight")){if(!i.support.noCloneEvent){r.height(r.data("_vgwrapheight"));
}else{r.animate({height:r.data("_vgwrapheight")+"px"},u,"easeOutQuart");}}else{clearTimeout(r.data("_vgwraptimeout"));r.data("_vgwraptimeout",setTimeout(function(){if(!i.support.noCloneEvent){r.height(r.data("_vgwrapheight"));
}else{r.animate({height:r.data("_vgwrapheight")+"px"},u,"easeOutQuart");}},t));}}function p(s){var r;s.each(function(t){r=i(this);r.css("left",r.data("_vgleft")+"px");
r.css("top",r.data("_vgtop")+"px");});}function o(x,w,r,u){var y=i(x).parent();var z=false;var s=x.length;var t,v,A;for(t=0;t<s;t++){v=i(x[t]);A=v.position();
if(A.left!=v.data("_vgleft")||A.top!=v.data("_vgtop")){z=true;}}if(z){if(typeof(y.data("_vgopt").onStart)==="function"){y.data("_vgopt").onStart();}x.each(function(C){var B=i(this);
var D={duration:r,easing:w};if(x.size()-1===C){D.complete=y.data("_vgopt").onFinish||null;}clearTimeout(B.data("_vgtimeout"));B.data("_vgtimeout",setTimeout(function(){B.animate({left:B.data("_vgleft")+"px",top:B.data("_vgtop")+"px"},D);
},C*u));});}}function j(r){r.each(function(t){var s=i(this);clearTimeout(s.data("_vgtimeout"));s.data("_vgtimeout",setTimeout(function(){a(s);o(s.data("_vgchild"),s.data("_vgopt").easing||"linear",e(s.data("_vgopt").time,500),e(s.data("_vgopt").delay,0));
},e(s.data("_vgopt").wait,500)));});}function n(r,u){var t=i("<span />").text(" ").attr("id","_vgridspan").hide().appendTo("body");t.data("size",t.css("font-size"));
t.data("timer",setInterval(function(){if(t.css("font-size")!=t.data("size")){t.data("size",t.css("font-size"));u(r);}},1000));}function l(r,t){if(!r.data("vgrid-image-event-added")){r.data("vgrid-image-event-added",1);
r.on("vgrid-added",function(){r.find("img").each(function(){var v=i(this);if(!v.data("vgrid-image-handler")){v.data("vgrid-image-handler",1);v.on("load",function(){t(r);
});}});});}r.trigger("vgrid-added");var s=r.append;var u=r.prepend;r.append=function(){s.apply(r,arguments);r.trigger("vgrid-added");};r.prepend=function(){u.apply(r,arguments);
r.trigger("vgrid-added");};}i.fn.extend({vgrid:function(t){var r=i(this);var s=t||{};if(s.easeing){s.easing=s.easeing;}r.each(function(){var u=i(this);
u.data("_vgopt",s);u.data("_vgchild",u.find("> *"));u.data("_vgdefchild",u.data("_vgchild"));u.css({position:"relative",width:"auto"});u.data("_vgchild").css("position","absolute");
a(u);p(u.data("_vgchild"));if(u.data("_vgopt").fadeIn){var v=(typeof(u.data("_vgopt").fadeIn)==="object")?u.data("_vgopt").fadeIn:{time:u.data("_vgopt").fadeIn};
u.data("_vgchild").each(function(x){var w=i(this);if(w.css("display")==="none"){return true;}w.stop(false,true).css({opacity:0});setTimeout(function(){w.stop(false,true).fadeTo(v.time||250,1);
},x*(v.delay||0));});}i(window).resize(function(w){j(u);});if(s.useLoadImageEvent){l(u,j);}if(s.useFontSizeListener){n(u,j);}});return r;},vgrefresh:function(v,u,s,t){var r=i(this);
r.each(function(){var x=i(this);var w=x.data("_vgopt")||{};if(x.data("_vgchild")){x.data("_vgchild",x.find("> *"));x.data("_vgchild").css("position","absolute");
a(x);u=e(u,e(x.data("_vgopt").time,500));s=e(s,e(x.data("_vgopt").delay,0));o(x.data("_vgchild"),v||x.data("_vgopt").easing||"linear",u,s);if(typeof(t)==="function"){setTimeout(t,x.data("_vgchild").length*s+u);
}}if(w.useLoadImageEvent){l(x,j);}});return r;},vgsort:function(t,v,u,s){var r=i(this);r.each(function(){var w=i(this);if(w.data("_vgchild")){w.data("_vgchild",w.data("_vgchild").sort(t));
w.data("_vgchild").each(function(x){i(this).appendTo(w);});a(w);o(w.data("_vgchild"),v||w.data("_vgopt").easing||"linear",e(u,e(w.data("_vgopt").time,500)),e(s,e(w.data("_vgopt").delay,0)));
}});return r;}});})(jQuery);