/*
    Based on this article:
    http://www.html-advisor.com/javascript/hide-email-with-javascript-jquery/

    Example markup:
    ---------------

    <span class="mailme" title="Send me a letter!">me at mydomain dot com</span>

    Example code:
	-------------

	// Replaces all the matching elements with a <a href="mailto:..> tag.

	$('span.mailme').mailme();

*/

jQuery.fn.mailme = function(replace) {
    var at = / at /;
    var dot = / dot /g;
    replace = replace ? true : false;
    this.each( function() {
        var addr = jQuery(this).text().replace(at,"@").replace(dot,".");
        var title = jQuery(this).attr('title')
        if(replace) {
            $(this).html('<a href="mailto:'+addr+'" title="'+title+'">'+ addr +'</a>');
        }else{
            $(this)
                .after('<a href="mailto:'+addr+'" title="'+title+'">'+ addr +'</a>')
                .remove();
        }
    });
};