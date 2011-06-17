/**
 * Created by IntelliJ IDEA.
 * User: byrne
 * Date: Nov 30, 2010
 * Time: 2:26:52 PM
 * To change this template use File | Settings | File Templates.
 */
$(document).ready(function() {
    var pathname = window.location.pathname;
    var start = pathname.lastIndexOf("/")+1;
    var filename = pathname.substring(start);

    $('#results').hide();
    $('td.navigation li:first').addClass('selected');
    $('td.navigation li').each(function(index) {
        var currentPage = $(this).find('a').attr('href');
        if (currentPage == filename) {
            $('td.navigation li').removeClass('selected');
            $(this).addClass('selected');
            return false;
        }
    });

    if ($('#results_container').length > 0){
        $('td.navigation li').removeClass('selected');
        $('#results').addClass('selected').show();
        return false;
    }
      return false;
});