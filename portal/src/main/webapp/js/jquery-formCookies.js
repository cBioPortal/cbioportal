/**
 * Created by IntelliJ IDEA.
 * User: byrne
 * Date: Aug 25, 2011
 * Time: 1:39:33 PM
 * To change this template use File | Settings | File Templates.
 */
// Seeded by a komodomedia article:
// http://www.komodomedia.com/blog/2008/07/using-jquery-to-save-form-details/

// Written by Alex Wreschnig
// Started 3/11/2009
// Generalized and turned into a plugin on 12/12/2009

jQuery.fn.saveToCookie = function(options){
  var options = jQuery.extend({
    prefix: "form-"
  }, options);

  this.each(function(){
    // if we have a checkbox
    if($(this).attr("type") == "checkbox") {
      // Get "checked" status and set name field
      var isChecked = $(this).attr('checked');
      var name = options['prefix'] + $(this).attr('id');

      //if this item has been cookied, restore it
      if($.cookie( name ) == "true") {
        $(this).attr( 'checked', $.cookie( name ) );
      }
      //assign a change function to the item to cookie it
      $(this).change(
        function(){
          $.cookie( name, $(this).attr('checked'), { path: '/', expires: 365 });
        }
      );
    }

    // if we have a radio button
    else if($(this).attr("type") == "radio") {
      /* We need to do something special for radio buttons.
         We'll identify radio buttons not only by name
         but by value. */
      var isSelected = $(this).attr('selected');
      var name = options['prefix'] + $(this).attr('id');
      var value = $(this).val();

      // Unlike the others, we have to test against the value here.
      // After we make sure the cookie exists.
      if($.cookie( name )) {
        if ($.cookie ( name ) == value) {
          $(this).attr( 'checked', 'checked' );
        }
      }
      // Assign a change function to the item to cookie it
      $(this).change(
        function(){
          $.cookie( name, $(this).val(), { path: '/', expires: 365 });
        }
      );
    }

    // otherwise it's probably a text input, a textarea element, or a select element.
    else {
      //if this item has been cookied, restore it
      var name = options['prefix'] + $(this).attr('id');

      if($.cookie( name )) {
        $(this).val( $.cookie(name) );
      }
      //assign a change function to the item to cookie it
      $(this).change(
        function() {
          $.cookie(name, $(this).val(), { path: '/', expires: 365 });
        }
      );
    }
  });
  return this;
}
