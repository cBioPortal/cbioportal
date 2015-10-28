
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
        this.render();
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
function DataManagerPresenter(dmInitCallBack){
    var self = this;
    // default HTML page is the loader
    var htmlPage="<img id='loader_img' src='images/ajax-loader.gif' alt='loading...'>";

    function getErrorHtml(sourceURL){
        return new Date() + "<br>There was a problem retrieving the page located at: "+sourceURL+"<br>";
    }

    // returns whether sourceURL ends with md
    function isMarkdownPage(sourceURL){
        return sourceURL.match(/\.md$/);
    }

    // fetch an external page
    function fetchExternalPage(sourceURL){
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
            // check whether it's a markdown page. If so, convert it; otherwise use the results as the htmlPage
            if(isMarkdownPage(sourceURL))
                htmlPage = markdown2html(resultPage);
            else
                htmlPage = resultPage;
        })
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            htmlPage = getErrorHtml()+err;
            console.log(new Date() + ": Request Failed for external page: " + err );
        })
        .always(function(){
            dmInitCallBack(self);
        });
    }

    // fetch an internal page
    function fetchInternalPage(sourceURL){
        $.ajax({
            url : sourceURL
        })
        .done(function(resultPage){
            console.log(new Date() + ': successfully retrieved internal page!');
            // check whether it's a markdown page. If so, convert it; otherwise use the results as the htmlPage
            if(isMarkdownPage(sourceURL))
                htmlPage = markdown2html(resultPage);
            else
                htmlPage = resultPage;
        })
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            htmlPage = getErrorHtml()+err;
            console.log(new Date() + ": Request Failed for local page: " + err );
        })
        .always(function(){
            dmInitCallBack();
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
    this.init = function (sourceURL){
        // if the page does not start with http we're assuming it's an internal page
        if(!(sourceURL.lastIndexOf("http", 0) === 0))
            // in that case, fetch the internal page
            fetchInternalPage(sourceURL);
        // other url
        else
            fetchExternalPage(sourceURL);
    }

}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            MAIN CLASS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


function GeneratePage(sourceURL, targetDiv)
{
    var generatedPageView;

    this.init = function()
    {
        console.log(new Date() + ": init called");

        // create a new presenter, with a call-back function
        var dmPresenter = new DataManagerPresenter(dmInitCallBack);

        // create the view
        generatedPageView = new GeneratedPageMainView({
            dmPresenter:dmPresenter,
            sourceURL:sourceURL,
            el: targetDiv
        });

        //Initialize presenter, which triggers the asynchronous services to get the
        //data and calls the callback function once the data is received:
        dmPresenter.init(sourceURL);
    };

    //continues init:
    var dmInitCallBack = function(){
        console.log(new Date() + ": page data fetched and processed. Rendering of retrieved view");

        // render the htmlpage
        generatedPageView.render();
    }
}
