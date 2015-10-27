
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                       VIEWS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Main view
 */
var GeneratedPageMainView = Backbone.View.extend({

    initialize : function (options) {
        this.dmPresenter = options.dmPresenter;
    },

    render: function(){
        console.log(new Date() + ": START GeneratedPageMainView render()");
        $(this.el).html(this.dmPresenter.getHTMLPage());
    }
});

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                       CONTROLLERS / PRESENTERS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * 'Presenter' layer
 * fetches the data from the webservice and transforms the data to a format that is ready to use by the views.
 */
function DataManagerPresenter(dmInitCallBack, sourceURL){
    var self = this;
    var htmlPage;
    var htmlErrorPage = new Date() + "\nThere was a problem retrieving the page located at: "+sourceURL+"\n";

    // fetch an external page
    function fetchExternalPage(sourceURL, isMarkdown){
        $.ajax({
            type: "GET",
            url: "api/getexternalpage.json",
            data: {sourceURL: sourceURL},
            dataType: "json"
        })
        .done(function(result){
            console.log(new Date() + ': successfully retrieved the markdownpage!');
            // the resultPage is stored in result.response
            var resultPage = result.response;
            if(isMarkdown) htmlPage = markdown2html(resultPage);
            else htmlPage = resultPage;
        })
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log(new Date() + ": Request Failed: " + err );
            htmlPage = htmlErrorPage+err;
        })
        .always(function(){
            dmInitCallBack(self);
        });
    }

    // fetch an internal page
    function fetchInternalPage(sourceURL, isMarkdown){
        $.ajax({
            url : sourceURL
        })
        .done(function(resultPage){
            console.log(new Date() + ': successfully retrieved local page!');
            if(isMarkdown) htmlPage = markdown2html(resultPage);
            else htmlPage = resultPage;
        })
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log(new Date() + ": Request Failed for local page: " + err );
            htmlPage = htmlErrorPage+err;
        })
        .always(function(){
            dmInitCallBack(self);
        });
    }

    // convert markdown to html
    function markdown2html(markdownPage){
        var converter = new showdown.Converter(),
            text      = markdownPage,
            html      = converter.makeHtml(text);
        return html;
    }

    // returns the html string
    this.getHTMLPage = function(){
        return htmlPage;
    }

    // determines what to do with the sourceURL
    function init(sourceURL){
        // local url, located in content
        if(!(sourceURL.lastIndexOf("http", 0) === 0)) {
            // markdown
            if(sourceURL.match(/\.md$/)) fetchInternalPage(sourceURL, true);
            // other
            else fetchInternalPage(sourceURL, false);
        }
        // other url
        else{
            // markdown
            if(sourceURL.match(/\.md$/)) fetchExternalPage(sourceURL, true);
            //other
            else fetchExternalPage(sourceURL, false);
        }
    }

    // call init with the source URL
    init(sourceURL);
}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            MAIN CLASS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


function GeneratePage(sourceURL, targetDiv)
{
    this.init = function()
    {
        console.log(new Date() + ": init called");

        // add the "loading" image
        $(targetDiv).html("<img id='loader_img' src='images/ajax-loader.gif' alt='loading...'>");

        //Initialize presenter, which triggers the asynchronous services to get the
        //data and calls the callback function once the data is received:
        new DataManagerPresenter(dmInitCallBack, sourceURL);
    };

    //continues init:
    var dmInitCallBack = function(dmPresenter){

        console.log(new Date() + ": page data fetched and processed. Rendering of retrieved view");

        var generatedPageView = new GeneratedPageMainView({
            dmPresenter:dmPresenter,
            sourceURL:sourceURL,
            el: targetDiv
        });

        // ...and let the fun begin!
        generatedPageView.render();
    }
}
