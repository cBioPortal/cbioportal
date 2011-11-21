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


function bitlyURL(fullURL){

    //testing - can not encode URI component when localhost is in URL
    //fullURL = fullURL.replace("localhost:8080/cgx","cbioportal.org/public-portal");
    //console.log("URL: "+fullURL);

    var defaults = {
        version: '3.0',
        login: 'cbioportal',
        apiKey: 'R_94aa4ca6019d2a1f4dfe99acf59c9275',
        history: '0',
        longURL: encodeURIComponent(fullURL)
    };

    //console.log("LONGURL: "+defaults.longURL);

    var qurl = "http://api.bit.ly/shorten?"
    +"version="+defaults.version
    +"&longUrl="+defaults.longURL
    +"&login="+defaults.login
    +"&apiKey="+defaults.apiKey
    +"&history="+defaults.history
    +"&format=json&callback=?";

    $.getJSON(qurl, function(data){

	console.log(data);

        $('#bitly').append(data.results[fullURL].shortUrl);

    });


}
