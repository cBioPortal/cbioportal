
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                       VIEWS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Main view holding the tabs (one for each of the genes)
 */
var GeneratedPageMainView = Backbone.View.extend({

    initialize : function (options) {
        this.dmPresenter = options.dmPresenter;
    },

    render: function(){
        console.log(new Date() + ": START GeneratedPageMainView render()");
        $(this.el).html(this.dmPresenter.getHTMLPage());
    }
}); // end of GeneratedPageMainView

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                       CONTROLLERS / PRESENTERS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * 'Presenter' layer to expose the DataManager API (i.e. the cbioportal-datamanager.js),
 * transforming its data to a format that is ready to use by the views.
 */
function DataManagerPresenter(dmInitCallBack, sourceURL){
    var self = this;
    var htmlPage;

    function createHTMLPage(sourceURL){
        var mdPage;
        $.ajax({
            type: "GET",
            url: "api/getmarkdownpage.json",
            data: {sourceURL: sourceURL},
            dataType: "json"
        })
        .done(function(result){
            console.log(new Date() + ': successfully retrieved the markdownpage!');
            mdPage = result.response;
        })
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log(new Date() + ": Request Failed: " + err );
            mdPage = new Date() + "\nThere was a problem retrieving the page located at: "+sourceURL+"\n"+err;
        })
        .always(function(){
            htmlPage = markdown2html(mdPage);
            dmInitCallBack(self);
        });
    }

    function markdown2html(markdownPage){
        var converter = new showdown.Converter(),
            text      = markdownPage,
            html      = converter.makeHtml(text);
        return html;
    }

    this.getHTMLPage = function(){
        return htmlPage;
    }

    createHTMLPage(sourceURL);
}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            MAIN CLASS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


function GeneratePage(sourceURL, targetDiv)
{
    //var _pageMainView = null;
    // maybe store by url and if already exists render that one?

    this.init = function()
    {
        console.log(new Date() + ": init called");

        //Initialize presenter, which triggers the asynchronous services to get the
        //data and calls the callback function once the data is received:
        //var dmPresenter = new DataManagerPresenter();
        //dmPresenter.retrieveMarkdown(sourceURL);
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
    //this.getView = function() {return _cancerSummaryMainView;};
}
