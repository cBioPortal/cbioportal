$(document).ready(function(){
    
    //generate tabs for results page
    $('#tabs').tabs();
    $('#tabs').show();

	/*
	set a sessionStorage item so that when user leaves page and clicks back,
    the last-visited tab will be active
    */
    var tabs = $('#tabs').tabs();
    /*var strTabToShow = window.sessionStorage.getItem("tabToReturnTo");
    var tabToShow = parseInt(strTabToShow);
    tabs.tabs('option', 'selected', tabToShow);*/

    /*$('#tabs ul li a').click(function(){
        var indexOfSelected = tabs.tabs('option', 'selected');
        window.sessionStorage.setItem("tabToReturnTo", indexOfSelected);
    });*/


    //check Mutations and Copy Number Data by default
    $('input[value*="gistic"]').attr('checked', true);
    $('input[value*="mutation"]').attr('checked', true);



/**********  THIS HAS BEEN REPLACED IN dynamicQuery.js ********/
/***********                   CAN BE DELETED                       *********/
    //only show threshold options if gene expression profile is checked
    /*$('#threshold').hide();
    $('#mRNAcheckbox' ).click(function(){
        var checkboxSelected = $('#mRNAcheckbox input[type=checkbox]:checked').val();
        var radio = $('#mRNAcheckbox input[type=radio]').val();
        var radioSelected = $('#mRNAcheckbox input[type=radio]:checked').val();

        if (radioSelected != null && radioSelected.length > 0 ) {
            if (!radioSelected.contains("outlier")){
                $('#threshold').slideDown();
            } else {
                $('#threshold').slideUp();
            }
        } else if (checkboxSelected && !radio){
            $('#threshold').slideDown();
        } else {
            $('#threshold').slideUp();
        }
    });      */
/*****************************************************************/

    var hovering=false;
    var maxNextClicks = $('#ppy2 li').length * 2;
    
    $(".ppy").hover(
        function() {
            hovering = true;
            $('.ppy-caption').show();
        },
        function() {
            hovering = false;
        }
    );

    setInterval(function(){
        if(!hovering && maxNextClicks >0){
            maxNextClicks--;
            $('a.ppy-next').click();
            $('.ppy-caption').hide();
        }
    }, 3000);

    /*$('#tabs ul li:eq(3) a').click(function(){
        $('#clinical img').hide();
        $('#clinical img').each(function(){
            $(this).load(function(){
                $('#clinical img').fadeIn('normal', hideClinicalLoader());
            });
        });

        function hideClinicalLoader() {
            //hide loader image and remove load div
            $('#load').delay(500).fadeOut('normal').$('#clinical div#load').remove();
        }

    });*/

});

