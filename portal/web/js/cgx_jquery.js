$(document).ready(function(){
       
    setUpPopEye();
    setUpTabs();
    setUpTestimonials();
    
});


/*
 * Set up results tabs
 */
function setUpTabs() {
     // generate tabs for results page; set cookies to preserve
    // state of tabs when user navigates away from page and back
    $('#tabs').tabs({cookie:{expires:1, name:"results-tab"}});
    $('#tabs').show();
}

/*
 * Determine position and behavior of testimonials
 * based on current page
 */
function setUpTestimonials() {
    // If we are on 'view all' page (what_people_are_saying.jsp),
    // hide testimonial section in right column
    if ($('#all_testimonials').length > 0){
        $('#rotating_testimonials').hide();
    } else {
        // If we are not on 'view all' page, testimonials appear
        // in right column. Append link to 'view all' to each
        // blockquote. This is done to ensure link falls
        // directly beneath each quote, as quote lengths differ
        $('#testimonials blockquote').append("<br><a href=\"what_people_are_saying.jsp\">View All</a>");
        //rotateTestimonials();
	displayRandomTestimonial();
    }
}

/*
 * Rotate testimonials in right column
 */
function rotateTestimonials() {
    // use quovolver plugin to rotate testimonials

    // set animation speed to 400; quovolver default is 500
    var animationSpeed = 400;
    // set duration of each testimonial; quovolver default is 6000
    var duration = 8000;
    $('#testimonials > blockquote').quovolver(animationSpeed, duration);
}

function displayRandomTestimonial() {
    $('#testimonials blockquote').hide()
    
    var numTestimonials = $('#testimonials > blockquote').length;
    var ran = Math.floor(Math.random()*numTestimonials) + 1;
    $('#testimonials blockquote:nth-child(' + ran + ')').show();

}

/*
 * Set up preview images on home page
 */

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
        if(!$("#custom_case_set_dialog").dialog("isOpen") &&
        		!hovering && maxNextClicks >0)
        {
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
        if (data.results == null){
            $('#bitly').append("An unknown error occurred. Unable to shorten your URL.");
        }  else {
            $('#bitly').append("<br><strong><a href='" + data.results[fullURL].shortUrl+"'>" +
                    data.results[fullURL].shortUrl + "</a></strong><br>");
        }
    });


}
