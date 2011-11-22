$(document).ready(function(){
       
    setUpPopEye();
    setUpTabs();

});



function setUpTabs() {
     // generate tabs for results page; set cookies to preserve
    // state of tabs when user navigates away from page and back
    $('#tabs').tabs({cookie:{expires:1, name:"results-tab"}});
    $('#tabs').show();
}


function setUpPopEye(){

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
}

function clinical(){
    $('#tabs ul li:eq(3) a').click(function(){
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

    });
}


/*
This function accepts as arguments the URL to be shortened,
our bitly user name and bitly API key (user and key are
stored in properties file)
 */
function bitlyURL(fullURL, usr, key){

    /*testing - can not encode URI component when localhost is in URL.
              - uncomment following line when testing on localhost.
     */
    //fullURL = fullURL.replace("localhost:8080/cgx","cbioportal.org/public-portal");

    var defaults = {
        version: '3.0',
        login: usr,
        apiKey: key,
        history: '0',
        longURL: encodeURIComponent(fullURL)
    };

    //build call to web API
    var qurl = "http://api.bit.ly/shorten?"
    +"version="+defaults.version
    +"&longUrl="+defaults.longURL
    +"&login="+defaults.login
    +"&apiKey="+defaults.apiKey
    +"&history="+defaults.history
    +"&format=json&callback=?";

    // get JSON data, extract from it the new short URL and
    // append the short URL to div with id 'bitly'
    $.getJSON(qurl, function(data){
        $('#bitly').append("<br><strong>"+data.results[fullURL].shortUrl+"</strong><br>");
    });


}
