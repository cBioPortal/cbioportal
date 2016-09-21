
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
        this.servicePresenter = options.servicePresenter;
        this.baseURL = options.baseURL;
        this.markdownDocumentation = options.markdownDocumentation;
        this.hash='';
        this.render();
    },
    events : {
        "click a" : "linkClickedHandler"
    },

    linkClickedHandler: function(event){
        // prevent link from opening
        event.preventDefault();
        var target = event.currentTarget.getAttribute("href");
        this.hash = event.currentTarget.hash;

        // if target starts with http, open a new tab and navigate to the target
        if(target.startsWith("http")){
            window.open(target);
        }
        // if target starts with a hash, stay on the current page and scroll to the hash
        else if(target.startsWith("#")){
            this.scrollToHash();
        }
        else{
            // if the target is internal, check whether the target has a hash; if so, remove it
            if(this.hash.length>0) target = target.substring(0, target.indexOf('#'));
            // if our base url is a wiki or github page, add md to our target to fetch markdown
            if(this.markdownDocumentation==='true') target+=".md";
            if(this.baseURL.length==0) target="content/"+target;
            // fetch the target
            this.servicePresenter.fetchSourcePage(target);
        }
    },

    scrollToHash: function(){
        if (this.hash.length > 0){
            // if our base url is a wiki page, the page has been translated to markdown and
            // the library replaces all non-word characters with nothing.
            // do the same with our hash to be able to scroll to it
            // this is a workaround for https://github.com/showdownjs/github-extension/issues/5
            if(this.markdownDocumentation==='true'){
                this.hash = this.hash.replace(/[^\w]/g, '').toLowerCase();
            }
            // else remove the #
            else{
                this.hash = this.hash.substring(1, this.hash.length);
            }
            // find the element to scroll to and scroll to it
            var scrollToElement = document.getElementById(this.hash);
            scrollToElement.scrollIntoView();
        }
        else{
            window.scrollTo(0,0);
        }
    },

    // retrieve the page and scroll to the hash
    render: function(){
        console.log(new Date() + ": GeneratedPageMainView render()");
        $(this.el).html(this.servicePresenter.getHTMLPage());
        // give the image a max-width
        $("img").css("max-width", "800px")
        this.scrollToHash();
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
function ServicePresenter(baseURL, markdownDocumentation){
    // default HTML page is the loader
    var htmlPage="<img id='loader_img' src='images/ajax-loader.gif' alt='loading...'>";
    var serviceCallBack;

    function getErrorHtml(sourceURL){
        return new Date() + "<br>There was a problem retrieving the page located at: "+sourceURL+"<br>";
    }

    // return whether the baseURL starts with http
    function isExternalContent(){
        return baseURL.startsWith("http");
    }

    function getFullURL(sourceURL){
        if(isExternalContent()){
            return baseURL+sourceURL;
        }
        return sourceURL;
    }

    function replacer(match, p1){
        // check whether the captured group starts with http
        // if it does, return as-is, otherwise attach the url to the baseURL
        if(p1.match(/^http\.*/)){
            return 'src=\"'+p1+'\"';
        }
        return 'src=\"'+baseURL+p1+'\"';
    }

    // replace image tags to full source reference instead of relative
    function replaceImageTags(){
        htmlPage = htmlPage.replace(/src\s*=\s*"(.*)"/g, replacer);
    }

    // fetch an external page
    function fetchExternalPage(sourceURL){
        $.ajax({
            type: "GET",
            url: "api-legacy/getexternalpage.json",
            data: {sourceURL: getFullURL(sourceURL)},
            dataType: "json"
        })
        .done(function(result){
            console.log(new Date() + ': successfully retrieved the markdownpage!');
            // the resultPage is stored in result.response
            var resultPage = result.response;
            // check whether it's a markdown page. If so, convert it; otherwise use the results as the htmlPage
            if(markdownDocumentation==='true') {
                htmlPage = markdown2html(resultPage);
            }
            else {
                htmlPage = resultPage;
            }
            replaceImageTags();
        })
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            htmlPage = getErrorHtml()+err;
            console.log(new Date() + ": Request Failed for external page: " + err );
        })
        .always(function(){
            serviceCallBack();
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
            if(markdownDocumentation==='true') {
                htmlPage = markdown2html(resultPage);
            }
            else {
                htmlPage = resultPage;
            }
        })
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            htmlPage = getErrorHtml()+err;
            console.log(new Date() + ": Request Failed for local page: " + err );
        })
        .always(function(){
            serviceCallBack();
        });
    }

    // convert markdown to html using the showdown library
    function markdown2html(markdownPage){
        var converter = new showdown.Converter({extensions: ['github']}),
            text      = markdownPage,
            html      = converter.makeHtml(text);
        return html;
    }

    // returns the html string
    this.getHTMLPage = function(){
        return htmlPage;
    }

    // determines what to do with the sourceURL
    this.fetchSourcePage = function (sourceURL, callBack){
        if(callBack!=undefined){
            serviceCallBack = callBack;
        }
        if(!isExternalContent()) {
            fetchInternalPage(sourceURL);
        }
        else{
            fetchExternalPage(sourceURL);
        }
    }
}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            MAIN CLASS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


function GeneratePage(baseURL, sourceURL, markdownDocumentation, targetDiv){
    var generatedPageView;

    this.init = function(){
        console.log(new Date() + ": init called!");

        // create a new presenter, with a call-back function
        var servicePresenter = new ServicePresenter(baseURL, markdownDocumentation);

        // create the view
        generatedPageView = new GeneratedPageMainView({
            servicePresenter: servicePresenter,
            baseURL: baseURL,
            markdownDocumentation: markdownDocumentation,
            el: targetDiv
        });

        //Initialize presenter, which triggers the asynchronous services to get the
        //data and calls the callback function once the data is received
        _.bindAll(generatedPageView, 'render');
        servicePresenter.fetchSourcePage(sourceURL, generatedPageView.render);
    };
}
