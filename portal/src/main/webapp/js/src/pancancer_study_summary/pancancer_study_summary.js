

/**
 * Some constants used in event handling below.
 */
var GeneDetailsEvents = (function(){
  return {
      // events for creating and selecting tabs   
     GENE_TAB_SELECTED: "geneDetailsGeneTabSelected",
     GENE_TABS_CREATED: "geneDetailsGeneTabsCreated",
     // events for the buttons template
     SHOW_HIDE_CUSTOMIZE_HISTOGRAM_CLICKED: "showHideCustomizeHistogramClicked",
     DOWNLOAD_PDF_CLICKED: "DOWNLOAD_PDF_CLICKED", 
     DOWNLOAD_SVG_CLICKED: "DOWNLOAD_SVG_CLICKED",
  };
})();



///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                       VIEWS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Main view holding the tabs (one for each of the genes)
 */
var CancerSummaryMainView = Backbone.View.extend({
  el: "#pancancer_study_summary",

  initialize : function (options) {
     this.options = options || {};
     this.dispatcher = options.dispatcher;
     this.dmPresenter = options.dmPresenter;

  },

  render: function() {
	 console.log(new Date() + ": START CancerSummaryMainView render()"); 
     var self = this;
     var content = self.generateContent();

     // create the variables for the main template
     var variables = {
        listContent: content.listContent,
        mainContent: content.mainContent
     };

     // load the template using underscore
     var templateFn = PanCancerTemplateCache.getTemplateFn("cancertypesummary_main_template");
     var template = templateFn(variables);

     // load the compiled HTML into the Backbone "el"
     self.$el.html(template);
     //initialize the view:
     self.initDefaultView();

     //apply the tabs format:
     self.format();
     console.log(new Date() + ": END CancerSummaryMainView render()"); 
  },

  /**
   * Formats the contents of the view after the initial rendering.
   */
  format: function(){
    var self = this;

    var mainContent = self.$el.find(".gene-tabs-container");
    //apply the tabs() function so that the items are 'formed' into tabs (jquery-ui):
    mainContent.tabs();
    //select first tab:
    mainContent.tabs("option", "active", 0);
  },

  /**
   * Generates the content structure by creating div elements for each
   * gene.
   *
   * @return {Object} content backbone with div elements for each gene
   */
  generateContent: function() {
     var self = this;
     var mainContent = "";
     var listContent = "";

     // retrieve the gene list for the tabs
     var geneList = self.dmPresenter.getGeneList();

     // create a div for for each gene
     _.each(geneList, function(gene, idx) {
 
            // get the template for the main content and apply it
        var templateFn = PanCancerTemplateCache.getTemplateFn("gene_details_main_content_template");
        mainContent += templateFn(
           {//geneId: cbio.util.safeProperty(gene)}
            geneId: gene} //TODO is the gene unique? Or is it an ambiguous alias that can be shared by different genes? 
        );

        // get the template for the tab ear (tab title) and apply it
        templateFn = PanCancerTemplateCache.getTemplateFn("gene_tab_ear_template");
        listContent += templateFn(
           {geneSymbol: gene,
           //geneId: cbio.util.safeProperty(gene)});
           geneId: gene}
        );
     });

     return {mainContent: mainContent, listContent: listContent};
  },

  /**
   * Initializes the gene view for the current gene data.
   */
  initDefaultView: function() {
     var self = this;

     var contentSelector = self.$el.find(".gene-tabs-container");
     console.log(contentSelector);

     // bind the tabscreate event (jquery-ui event, fires when the tabs() function is called)
     // now we can init the view for the first gene
     contentSelector.bind('tabscreate', function(event, ui) {

        // when created tab is the active tab; fetch its text
        var gene = ui.tab.text().trim();

        // trigger corresponding event in the Controller
        self.dispatcher.trigger(GeneDetailsEvents.GENE_TABS_CREATED, gene);
     });

     // bind the tabsactivate event (jquery-ui event, fires upon selecting the corresponding tab) 
     contentSelector.bind('tabsactivate', function(event, ui) {
        // when activated, newTab is the active tab; fetch its text
        var gene = ui.newTab.text().trim();
        // trigger corresponding event in the Controller
            self.dispatcher.trigger(GeneDetailsEvents.GENE_TAB_SELECTED, gene);
         });
   }
}); // end of CancerSummaryMainView


/** 
 * View containing the buttons to customize the histogram or download the 
 * histogram as SVG/PDF.
 */
var ButtonsView = Backbone.View.extend({
  //el: provided bij the GeneDetailsView

  initialize: function(options){
     var templateFn = PanCancerTemplateCache.getTemplateFn("buttons_template");
     this.template = templateFn();
     this.gene = options.gene;
     this.dispatcher = options.dispatcher;
  },

  // add events
  events:{
     "click .histogram-customize": "showHideCustomizeHistogram",
     "click .diagram-to-pdf": "downloadHistogramPdf",
     "click .diagram-to-svg": "downloadHistogramSvg"
  },

  render: function(){
     console.log("ButtonsView Render");
     $(this.el).html(this.template);

  },

  // When the histogram-customize button is clicked, notify the dispatcher        
  showHideCustomizeHistogram: function(){
     console.log("clicked for "+this.gene);
     this.dispatcher.trigger(GeneDetailsEvents.SHOW_HIDE_CUSTOMIZE_HISTOGRAM_CLICKED+this.gene);
  },
  
  downloadHistogramPdf: function(){
      console.log("clicked for "+this.gene);
      this.dispatcher.trigger(GeneDetailsEvents.DOWNLOAD_PDF_CLICKED+this.gene);
  },
   
  downloadHistogramSvg: function(){
      console.log("clicked for "+this.gene);
          this.dispatcher.trigger(GeneDetailsEvents.DOWNLOAD_SVG_CLICKED+this.gene);
  } 
      
}); // End ButtonsView


/**
 * View containing the options and parameters to customize the histogram
 * with extra filtering and sorting options. 
 */
var CustomizeHistogramView = Backbone.View.extend({
  //el: provided bij the GeneDetailsView

  initialize: function(options){
     this.dispatcher = options.dispatcher;
     this.gene = options.gene;
     var templateFn = PanCancerTemplateCache.getTemplateFn("customize_histogram_template");
     this.template = templateFn({geneId: this.gene});

     // subscribe to the showHideCustomizeHistogram event and give "this" is the context
     this.dispatcher.bind(GeneDetailsEvents.SHOW_HIDE_CUSTOMIZE_HISTOGRAM_CLICKED+this.gene, this.showHideCustomizeHistogram, this);
     
     this.dmPresenter = options.dmPresenter;
  },
  
  // add events
  events:{
     "click .close-customize a": "showHideCustomizeHistogram"
  },
  
  render: function(){
     console.log("CustomizeHistogram Render");
     // add the generated template
     $(this.el).html(this.template);
     
     // add all the elements
     this.addcancerTypeSelect();
     this.addSortByYAxisSelect();
     this.addSortByXAxisSelect();
     this.addNrAlteredSamplesSlider();
     this.addNrTotalSamplesSlider();
     this.addShowGenomicAlterationTypesCheckbox();
     this.addcancerTypeDetailedView();
   
  },

  // add the Cancer Type Select
  addcancerTypeSelect: function(){
	 var self = this;
     // cancer types:
     var selOptions = self.dmPresenter.getCancerTypeList();
     // add "all" entry, if more than 1 cancer type is found:
     if (selOptions.length > 1)
    	 selOptions.splice(0,0, "All");

     //event handler for when the Cancer Type Select is changed
     var changeCallBack = function(){
         var fields = {};
         var cancerType = $(this).val();
         fields["cancerType"] = cancerType;
         fields["cancerTypeDetailed"] = self.dmPresenter.getCancerTypeDetailedList(cancerType);
         //also reset minAlteredSamples and minTotalSamples (for the sliders):
         var max = self.dmPresenter.getMaxAlteredSamplesForCancerTypeAndGene(cancerType, self.gene, self.model.get("dataTypeYAxis"));
         fields["minAlteredSamples"] = self.dmPresenter.getMinAlteredSamples(self.model.get("dataTypeYAxis"), max);
         fields["minTotalSamples"] = 0;
    	 self.model.set(fields);
     }
     // create the dropdown and add it
     $("#customize-cancertype-dropdown-"+this.gene).append(fnCreateSelect(
    		 "Cancer Type: ", selOptions, changeCallBack));
  },

  // add the "Sort by" select box for the y-axis:
  addSortByYAxisSelect: function(){
	 var self = this;
     // static options: 
     var selOptions = ["Alteration Frequency", "Absolute Counts"];  
     
     // handle the event for when the Sort By Y-Axis Select is changed
     var changeCallBack = function(){
        console.log("sortByYAxisSelect changed to "+event.currentTarget.value);
        var fields = {};
        fields["dataTypeYAxis"] = $(this).val();
        //also reset minAlteredSamples:
        var max = self.dmPresenter.getMaxAlteredSamplesForCancerTypeAndGene(self.model.get("cancerType"), self.gene, fields["dataTypeYAxis"]);
        fields["minAlteredSamples"] = self.dmPresenter.getMinAlteredSamples(fields["dataTypeYAxis"], max);
        self.model.set(fields);        
     }
     // create the dropdown and add it
     $("#customize-data-type-y-axis-"+this.gene).append(fnCreateSelect(
    		 "Y-Axis value: ", selOptions, changeCallBack));
  },

  // add the "Sort by" select box for the x-axis:
  addSortByXAxisSelect: function(){
	 var self = this;
     // this will be provided at some point, but for now let's set it manually
     var selOptions = ["Y-Axis Values", "Cancer Types"];
     
     //handle the event for when the Sort By Y-Axis Select is changed
     var changeCallBack = function(){
        //console.log("sortByXAxisSelect changed to "+event.currentTarget.value);
        self.model.set("sortXAxis", $(this).val());
     }         
     // create the dropdown and add it
     $("#customize-sort-by-x-axis-"+this.gene).append(fnCreateSelect(
    		 "Sort X-Axis by: ", selOptions, changeCallBack));
  },

  // add the slider for minimum number of altered samples
  addNrAlteredSamplesSlider: function(){
     new MinAlteredSamplesSliderView({
    	 gene:this.gene, 
    	 el:"#customize-min-nr-altered-samples-slider-"+this.gene, 
    	 dispatcher:this.dispatcher, 
    	 model:this.model,
    	 dmPresenter:this.dmPresenter});
  },
    // add the slider for total number of altered samples
  addNrTotalSamplesSlider: function(){
      new MinTotalSamplesSliderView({
         gene:this.gene,
         el:"#customize-total-nr-altered-samples-slider-"+this.gene,
         dispatcher:this.dispatcher,
         model:this.model,
         dmPresenter:this.dmPresenter});
  },

  // add checkbox for genomic alteration types
  addShowGenomicAlterationTypesCheckbox: function(){
	 var self = this;    	 
	 // handle the event for when the Show genomic alteration types checkbkox is changed
     var changeCallBack = function(){
        //console.log("showGenomicAlterationTypesChanged changed to  "+$(event.currentTarget).is(':checked'));
        self.model.set("showGenomicAlterationTypes", $(this).is(':checked'));
     }
     //create checkbox and add it
     $("#customize-show-genomic-alteration-types-"+this.gene).append(
    	$("<input/>", {type: 'checkbox', checked: true}).change(changeCallBack));
     //checkbox label:
     $("#customize-show-genomic-alteration-types-"+this.gene).append( 	
    	$("<label>Show Genomic Alteration Types</label>"));
  },

  // add view for the specific cancer types selection
  addcancerTypeDetailedView: function(){
     new SpecificCancerTypesView({gene:this.gene, 
    	 el:"#specific-cancertypes-area-"+this.gene, 
    	 dispatcher:this.dispatcher, 
    	 model:this.model,
    	 dmPresenter:this.dmPresenter});
  },
  // incoming event - change the visibility of the customize histogram part
  showHideCustomizeHistogram: function(){
     console.log("ShowHide Order Received! "+this.gene);
     $("#customize-histogram-"+this.gene).toggle();
  } 

}); // End CustomizeHistogramView

//utility function to create a select/drop-down box: 
var fnCreateSelect = function(title, aData, callBack) {
	var div = $('<div/>');
	div.html(title);
	var sel = $('<select />');	
	sel.appendTo(div);
	
	for(var i = 0; i < aData.length; i++) {
	    $('<option />', {value: aData[i], text: aData[i]}).appendTo(sel);
	}
	sel.change(callBack);
	return div;
}

/**
 * View for the "Min nr of altered Samples" parameter, presented in the form of a slider. 
 * It is used as part of CustomizeHistogramView above.
 */
var MinAlteredSamplesSliderView = Backbone.View.extend({
      
  initialize: function(options){
     this.dispatcher = options.dispatcher;
     this.gene = options.gene;
     this.dmPresenter = options.dmPresenter;
     this.render();
     // call render when the model is changed
     this.model.on("change", this.updateRender, this);
  },

  events: {
     'slidechange .diagram-general-slider': 'handleSliderChange',
     'keyup .diagram-general-slider-value': 'handleSetThreshold'
  }, 

  //function for model.onchange above, it will check whether the slider max threshold needs
  //to be udpated:
  updateRender: function(){
	  var renderNeeded = this.model.hasChanged("cancerType") || this.model.hasChanged("dataTypeYAxis");
	  if (renderNeeded)
		  this.render();
  },
  
  render: function(){
	 //add % after the values or not:
	 var suffix = "";
	 this.max = this.dmPresenter.getMaxAlteredSamplesForCancerTypeAndGene(this.model.get("cancerType"), this.gene, this.model.get("dataTypeYAxis"));

     var text = "Min. # altered samples ";
     var init=1;

	 if (this.model.get("dataTypeYAxis") == "Alteration Frequency") {
		 suffix = "%";
		 //in %, with 1 decimal:
		 this.max = Math.round(parseFloat(this.max) * 1000)/10;
         text = "Min. % altered samples ";
         init = this.dmPresenter.getMinAlteredSamples(this.model.get("dataTypeYAxis"), this.max);
	 }

     // initialise general template with initial value of 1
     var templateFn = PanCancerTemplateCache.getTemplateFn("general_slider_template");
     this.template = templateFn({min:0, init:init, max:this.max, suffix: suffix, text:text});

     // add the template
     $(this.el).html(this.template);

     // create the jQuery ui slider with initial value of 1
     var sampleSlider = this.$el.find(".diagram-general-slider");
     sampleSlider.slider({ 
        value: init,
        min: 0, 
        max: this.max 
     });
    this.$el.find(".diagram-general-slider-value").attr("id", "input1"+this.gene);
  },

  // handle change to the slider        
  handleSliderChange: function(e, ui) {
     // update text 
     $("#input1"+this.gene).val(ui.value);
     // and notify the histogram via model change:
     this.model.set("minAlteredSamples", parseInt(ui.value));
  },
  
  handleSetThreshold: function(e){
      if(e.keyCode == 13)
          {
            var inputValue = $("#input1"+this.gene).val();
            var slider = this.$el.find(".diagram-general-slider"); 
            slider.slider({value: inputValue});
            this.model.set("minAlteredSamples", parseInt(inputValue));
          }
  }

}); // end MinAlteredSamplesSliderView


// min number of total samples
var MinTotalSamplesSliderView = Backbone.View.extend({

    initialize: function(options){
        this.dispatcher = options.dispatcher;
        this.gene = options.gene;
        this.dmPresenter = options.dmPresenter;
        this.render();
        // call render when the model is changed
        this.model.on("change", this.updateRender, this);
    },

    events: {
        'slidechange .diagram-general-slider': 'handleSliderChange',
        'keyup .diagram-general-slider-value': 'handleSetThreshold'
    },

    //function for model.onchange above, it will check whether the slider max threshold needs
    //to be updated:
    updateRender: function(){
        var renderNeeded = this.model.hasChanged("cancerType");
        if (renderNeeded)
            this.render();
    },

    render: function(){
        // find the maximum number of samples for the cancertype
        this.max = this.dmPresenter.getMaxSamplesForCancerType(this.model.get("cancerType"));

        var templateFn = PanCancerTemplateCache.getTemplateFn("general_slider_template");
        //this.template = templateFn({min:0, max:this.max});
        this.template = templateFn({min:0, init:0, max:this.max, suffix: "", text:"Min. # total samples "});

        // add the template
        $(this.el).html(this.template);

        // create the jQuery ui slider
        var sampleSlider = this.$el.find(".diagram-general-slider");
        sampleSlider.slider({
            value: 0,
            min: 0,
            max: this.max
        });
        
       this.$el.find(".diagram-general-slider-value").attr("id", "input2"+this.gene);
         
    },

    // handle change to the slider
    handleSliderChange: function(e, ui) {
        // update text
        $("#input2"+this.gene).val(ui.value);
        // and notify the histogram via model change:
        this.model.set("minTotalSamples", ui.value);
    },
    
    handleSetThreshold: function(e){
      if(e.keyCode == 13)
          {
            var inputValue = $("#input2"+this.gene).val();
            var slider = this.$el.find(".diagram-general-slider"); 
            slider.slider({value: inputValue});
            this.model.set("minTotalSamples", parseInt(inputValue));
          }
    }

}); // end MinTotalSamplesSliderView


/**
 * View containing the Histogram based on the data related to the 
 * gene of the selected tab. 
 */ 
var GeneHistogramView = Backbone.View.extend({
  //el: will be provided as it depends on the gene where we'll want to put the information
  //model: contains the settings required to generated the histogram and is provided   

  //initialize function is called when a new instance of GeneHistogramView is 
  //created. E.g. of valid constructor call:
	//	new GeneHistogramView({
	//        gene:gene, 
	//        el:"#histogram-"+gene, 
	//        model: histogramSettings, 
	//        dispatcher: dispatcher, 
	//        dmPresenter: dmPresenter
	//     })
  initialize: function(options){
     this.gene = options.gene;
     this.dispatcher = options.dispatcher;
     this.dmPresenter = options.dmPresenter;
     this.pancancerStudyHistogram = new PancancerStudySummaryHistogram();
     
     // subscribe to the DOWNLOAD_PDF/SVG_CLICKED event and give "this" is the context
     this.dispatcher.bind(GeneDetailsEvents.DOWNLOAD_PDF_CLICKED+this.gene, this.downloadPdf, this);
     this.dispatcher.bind(GeneDetailsEvents.DOWNLOAD_SVG_CLICKED+this.gene, this.downloadSvg, this);
     
     // call render when the model is changed
     this.model.on("change", this.render, this); 
  },

  render: function(){

     this.pancancerStudyHistogram.render(this.el, this.model, this.dmPresenter, this.gene);
  },
  
  downloadPdf: function() {
	  console.log("downloadPdf Received! "+this.gene);

	  var downloadOptions = {
        filename: "pancancerhistogram"+this.gene+".pdf",
        contentType: "application/pdf",
        servletName: "svgtopdf.do"
	  };

	  cbio.download.initDownload(
        $("#" + this.el.id + " svg")[0], downloadOptions);
  },
  
  downloadSvg: function() {
	  console.log("downloadSvg Received! "+this.gene);

	  cbio.download.initDownload(
	            $("#" + this.el.id + " svg")[0], {filename:  "pancancerhistogram"+this.gene+".svg"});
  }
      
}); // end of GeneHistogramView


/**
 * View containing the toggle link to display the checkboxes for (de)selecting the 
 * individual cancer types, and containing the checkboxes themselves. 
 */
var SpecificCancerTypesView = Backbone.View.extend({
  initialize: function(options){
     this.gene = options.gene;
     this.dmPresenter = options.dmPresenter;
     this.dispatcher = options.dispatcher;
     this.addEventListener();
     this.render();
     // call render again when the model is changed
     this.model.on("change", this.updateRender, this);
  },

  //function for model.onchange above, it will check whether the view needs
  //to be udpated:
  updateRender: function(){
	  var cancerTypeChanged = this.model.hasChanged("cancerType");
	  if (cancerTypeChanged)
		  this.render();
  },
  
  addEventListener: function(){
     var self=this;
     // when the Show/Hide Specific Cancer Types is clicked, show or hide the section and change the icon
     $("#show-hide-cancertypes-toggle-"+this.gene).click(function() {
          $("#show-hide-cancertypes-"+self.gene+" .triangle").toggle();
          $("#specific-cancertypes-area-"+self.gene).slideToggle();
     });
     // add listener for the Cancer Type changed
     // when this happens, we need to generate the items in the 

  },

  render: function() {
	  var self = this;
	  var templateFn = PanCancerTemplateCache.getTemplateFn("specific_cancertypes_area_template");
	  this.template = templateFn({geneId: self.gene});
      // add the template
      $(this.el).html(this.template);
	  
      // add click handlers:
      $("#cc-select-all-"+self.gene).click(function(e) {
    	  e.preventDefault();//prevents # link click to scroll the page
          $("#specific-cancertypes-area-"+self.gene +" input").each(function(idx, el) {
              $(el).prop("checked", true);
          });
          //reset model field:
          self.model.set("cancerTypeDetailed", self.dmPresenter.getCancerTypeDetailedList(self.model.get("cancerType")));
      });

      $("#cc-select-none-"+self.gene).click(function(e) {
    	  e.preventDefault();
          $("#specific-cancertypes-area-"+self.gene +" input").each(function(idx, el) {
              $(el).prop("checked", false);
          });
          //reset model field to none:
          self.model.set("cancerTypeDetailed", []);
      });
      
	  var listOfOptions = [];
	  if (this.model.get("cancerType") == "All")
		  listOfOptions = this.dmPresenter.getCancerTypeList(); 
	  else
		  listOfOptions = this.dmPresenter.getCancerTypeDetailedList(this.model.get("cancerType"));
	  
	  for (var i = 0; i < listOfOptions.length; i++){
		  this.addCancerTypeCheckbox(listOfOptions[i]);
	  }

  },
  
  // add checkbox for genomic alteration types
  addCancerTypeCheckbox: function(cancerType){
	 var self = this;    	 
	 // handle the event for when the Show genomic alteration types checkbkox is changed
     var changeCallBack = function(){
    	 var cancerTypeDetailedList = self.model.get("cancerTypeDetailed");
    	 //update model, which triggers a redraw of the histogram:
    	 var checked = $(this).is(":checked");
    	 if (checked) {
    		 cancerTypeDetailedList.push($(this).attr("cancerType"));
    	 }
    	 else {
        	 var indexOfType = cancerTypeDetailedList.indexOf($(this).attr("cancerType"));
        	 cancerTypeDetailedList.splice(indexOfType, 1);
    	 }
    	 //unset model value,  otherwise model.onchange is not triggered, since it is an array object:
    	 self.model.unset("cancerTypeDetailed", {silent: true});
    	 self.model.set("cancerTypeDetailed", cancerTypeDetailedList);
     }
     //create checkbox and add it
     var checkBox = $("<input/>", {type: 'checkbox', checked: true}).change(changeCallBack);
     checkBox.attr("cancerType", cancerType);
     $(this.el).append(checkBox);
     //checkbox label:
     $(this.el).append( 	
    	$("<label/>").text(cancerType));
  }
});


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                               MODELS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


/** 
 * Main model containing the settings that can be changed by the 
 * fields in CustomizeHistogramView. Other views can subscribe to changes
 * in this model to render again based on the new model field values.  
 */
var HistogramSettings = Backbone.Model.extend({
  defaults: {
     myType: "HistogramSettings",
     cancerType: "All",
     cancerTypeDetailed: "All",
     sortXAxis: "Y-Axis Values",
     dataTypeYAxis: "Alteration Frequency",
     minAlteredSamples: 1,
     minTotalSamples: 0,
     showGenomicAlterationTypes: true
  },
  initialize: function(options) {
	  //initialize cancerType to something different than "All" if only 1 cancer_type is there:
	  if (options.dmPresenter.getCancerTypeList().length == 1) {
		  var cancerType = options.dmPresenter.getCancerTypeList()[0];
		  this.set("cancerType", cancerType);
		  this.set("cancerTypeDetailed", options.dmPresenter.getCancerTypeDetailedList(cancerType));
	  }
	  else
		  this.set("cancerTypeDetailed", options.dmPresenter.getCancerTypeList()); 
	  
	  //initialize minAlteredSamples:
	  var max = options.dmPresenter.getMaxAlteredSamplesForCancerTypeAndGene(this.get("cancerType"), options.gene, this.get("dataTypeYAxis"));
	  this.set("minAlteredSamples", options.dmPresenter.getMinAlteredSamples(this.get("dataTypeYAxis"), max));
	  
      console.log("HistogramSettings model Created");
  }
});



///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                       CONTROLLERS / PRESENTERS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


/** 
 * Controller which glues it all together
 */
function GeneDetailsController(cancerSummaryMainView, dispatcher, dmPresenter){
   // variable for keeping track of whether the tab has been initiliazed to prevent creating new content every time we switch to a different tab
   var geneTabGenerated = {};

   // initialize the controller
   function init(){
      dispatcher.on(GeneDetailsEvents.GENE_TABS_CREATED, geneTabCreatedHandler); // subscribe to the GENE_TAB_CREATED event
      dispatcher.on(GeneDetailsEvents.GENE_TAB_SELECTED, geneTabSelectedHandler); // subscribe to the GENE_TAB_CREATED event
   }

   // handle the content of the first tab when the tabs are created
   function geneTabCreatedHandler(gene){
      //console.log("geneTabCreated! "+gene);
      geneTabGenerated[gene] = createTabContent(gene);
   }

   // handle the content of the tab when user switches to a different tab
   function geneTabSelectedHandler(gene){
	  //if tab has not yet been created/rendered before, do it now:
      if(geneTabGenerated[gene]==null){
         //console.log("geneTabSelected! "+gene);
         geneTabGenerated[gene] = createTabContent(gene);
      }
   }

   // create the content of a tab, triggered when tab 
   function createTabContent(gene){
      var histogramSettings = new HistogramSettings({dmPresenter: dmPresenter, gene: gene});

      // create a ButtonsView, providing the gene, the dispatcher and the el
      var buttonsView = new ButtonsView({
         gene:gene, 
         el:"#button-container-"+gene, 
         dispatcher:dispatcher
      });
      // create a CustomizeHistogramView, providing the gene, the dispatcher and the el
      var customizeHistogramView = new CustomizeHistogramView({
         gene:gene, 
         el:"#customize-histogram-"+gene, 
         dispatcher:dispatcher,
         model: histogramSettings, 
         dmPresenter:dmPresenter
      });
      // create a GeneHistogramView, providing the gene, the dispatcher, the el and a model
      // the model contains the settings for the Histogram
      // the CustomizeHistogramView notifies the GeneHistogramView when a setting is changed
      // which triggers a change to the settings model, which triggers a render call, thereby updating the histogram
      var geneHistogramView = new GeneHistogramView({
         gene:gene, 
         el:"#histogram-"+gene, 
         model: histogramSettings, 
         dispatcher: dispatcher, 
         dmPresenter: dmPresenter
      });
      buttonsView.render();
      customizeHistogramView.render();
      geneHistogramView.render();
      return true;
   }

   // when GeneDetailsController is called, initialise
   init();
}

/**
 * 'Presenter' layer to expose the DataManager API (i.e. the cbioportal-datamanager.js), 
 * transforming its data to a format that is ready to use by the views.
 */
function DataManagerPresenter(dmInitCallBack)
{
	var self = this;
	var callbackA_done = new $.Deferred();
    var callbackB_done = new $.Deferred();
	
	//Initialize: run initial ws requests and data parsing. 
	//This sequence of calls gets: 
	//  - all sample clinical atttribute values for attributes CANCER_TYPE and CANCER_TYPE_DETAILED
	//  - all genomic event data, for all queried genes, according to selected profiles and OQL criteria.
	console.log(new Date() + ": CALL to getSampleClinicalData()");
	self.cancerTypeList = [];  //each entry contains cancerTypeDetailed[] and sample_ids[], each cancerTypeDetailed entry also contains sample_ids[]
	window.QuerySession.getSampleClinicalData(["CANCER_TYPE","CANCER_TYPE_DETAILED"])
	.then(
		function (sampleClinicalData){
			//parse the data to the correct internal format. Here we can assume that the samples are only the ones 
			//that comply with the query form parameters (e.g. the sample set ):
			console.log(new Date() + ": started processing sample clinical atttributes (cancer types)");
			
			var sampleIdAndCancerTypeIdx = [];
			for (var i = 0; i < sampleClinicalData.length; i++)
			{
				if (sampleClinicalData[i].attr_id == "CANCER_TYPE")
				{
					//track cancer types and sample ids:
					if (!self.cancerTypeList[sampleClinicalData[i].attr_val])
						self.cancerTypeList[sampleClinicalData[i].attr_val] = {cancerTypeDetailed: [], sampleIds: []};
					var cancerType = self.cancerTypeList[sampleClinicalData[i].attr_val];
					//a sample contains only one cancer_type, so refer to it:
					sampleIdAndCancerTypeIdx[sampleClinicalData[i].sample] = cancerType;
					cancerType.sampleIds.push(sampleClinicalData[i].sample);
				}
			}
			for (var i = 0; i < sampleClinicalData.length; i++)
			{
				if (sampleClinicalData[i].attr_id == "CANCER_TYPE_DETAILED")
				{
					//track cancer type detailed per cancer type:
					var cancerType = sampleIdAndCancerTypeIdx[sampleClinicalData[i].sample];
                    if (!cbio.util.checkNullOrUndefined(cancerType) && "cancerTypeDetailed" in cancerType) {
                        if (!cancerType.cancerTypeDetailed[sampleClinicalData[i].attr_val])
                            cancerType.cancerTypeDetailed[sampleClinicalData[i].attr_val] = {sampleIds: []};
                        cancerType.cancerTypeDetailed[sampleClinicalData[i].attr_val].sampleIds.push(sampleClinicalData[i].sample);
                    }
				}
			}
			console.log(new Date() + ": finished processing getSampleClinicalData()");
			//signal "done":
			callbackB_done.resolve();
		},
		function(err){
			// handle error, if any
			alert(" error found");
		});	
    
	console.log(new Date() + ": CALL to getOncoprintSampleGenomicEventData()");
	//keep track of samples and their respective alteration events 
	self.sampleList = {}; //each entry contains alterationEvents[] 
	window.QuerySession.getGeneAggregatedOncoprintSampleGenomicEventData()
	.then(
		function (genomicEventData){
			
			console.log(new Date() + ": started processing getOncoprintSampleGenomicEventData() data");
			
			    for (var i = 0; i < genomicEventData.length; i++) {
				//init alteration events, if not yet done
				if (!self.sampleList[genomicEventData[i].sample])
				    self.sampleList[genomicEventData[i].sample] = {alterationEvents: []};
				self.sampleList[genomicEventData[i].sample].alterationEvents.push(genomicEventData[i]);

			    }
			console.log(new Date() + ": finished processing getOncoprintSampleGenomicEventData() data");
			
			//signal "done":
			callbackA_done.resolve();
		},
		function(err){
			// handle error, if any
			alert(" error found");//TODO - check how the error will come in and how we should present it. Logged in https://github.com/cBioPortal/cbioportal/issues/264
		});
	
	//when both calls above are done processing, then we want to continue with dmInitCallBack:
	$.when(callbackA_done, callbackB_done).then(function () {
		console.log(new Date() + ": getOncoprintSampleGenomicEventData() and getSampleClinicalData() DONE, continuing with dmInitCallBack...");
		dmInitCallBack(self);
	});


	/**
	 * Returns the alteration events found for the sample list
	 * based on cancerType, cancerTypeDetailed. The result is an object in the format
	 * like below:
	 	{
	        "all": 136,
	        "mutation": 50,
	        "cnaUp": 70,
	        "cnaDown": 16,
	        "cnaLoss": 0,
	        "cnaGain": 0,
	        "multiple": 0
		}
	 *  
	 */
	this.getAlterationEvents = function(cancerType, cancerTypeDetailed, geneId) {
		//get the sample list based on cancerType, cancerTypeDetailed
		var sampleIds;
		if (cancerTypeDetailed === null)
			sampleIds = self.cancerTypeList[cancerType].sampleIds;
		else
			sampleIds = self.cancerTypeList[cancerType].cancerTypeDetailed[cancerTypeDetailed].sampleIds;
		
		//return the alteration event counts for the sample list and geneId  
		return this._getAlterationEventCounts(sampleIds, geneId);
	}

	
	this._getAlterationEventCounts = function(sampleIds, geneId) {
		//returns object in format like below: 
		//		{
		//	        "all": 136,
		//	        "mutation": 50,
		//	        "cnaUp": 70,
		//	        "cnaDown": 16,
		//	        "cnaLoss": 0,
		//	        "cnaGain": 0,
		//	        "multiple": 0
		//		}
		var mutation = cnaUp = cnaDown = cnaLoss = cnaGain = multiple = 0; 
		
		//count / mapping logic: 
		for (var i = 0; i < sampleIds.length; i++) {
			var sampleItem = this.sampleList[sampleIds[i]];
			var alterationEvents = sampleItem.alterationEvents;
			var alterationEventFound = false;
			for (var j = 0; j < alterationEvents.length; j++) {
				var alterations = alterationEvents[j];
				//only count for given gene: 
				if (alterations.gene.toUpperCase() === geneId.toUpperCase()) {
					//validation (not expected): 
					if (alterationEventFound)
						throw "prog error: only one alterations group item expected for a given sample/gene combination"; 
					alterationEventFound = true;
					//if both:
					if (typeof alterations.disp_cna !== "undefined" && typeof alterations.disp_mut !== "undefined") {
						multiple++;
					}
					//cna counts:
					else if (typeof alterations.disp_cna !== "undefined") {
						cnaUp += (alterations.disp_cna === "amp" ? 1 : 0);
						cnaDown += (alterations.disp_cna === "homdel" ? 1 : 0);
						cnaLoss += (alterations.disp_cna === "hetloss" ? 1 : 0);
						cnaGain += (alterations.disp_cna === "gain" ? 1 : 0);
						//From cbioportal-datamanager.js:
						//{"-2":"HOMODELETED","-1":"HEMIZYGOUSLYDELETED","0":undefined,"1":"GAINED","2":"AMPLIFIED"};
					}
					//mutation counts:
					else if (typeof alterations.disp_mut !== "undefined") {
						mutation++;
					}
				}
			}
		}
		
		return {
			all: mutation + cnaUp + cnaDown + cnaLoss + cnaGain + multiple, 
			mutation: mutation, 
			cnaUp: cnaUp,
			cnaDown: cnaDown,
			cnaLoss: cnaLoss,
			cnaGain: cnaGain,
			multiple: multiple
		}
		
	}
	
	
	/** 
	 * Returns the total number of samples for the given cancer type.
	 * If cancerTypeDetailed is given, it returns the total for this sub type instead.
	 */
	this.getTotalNrSamplesPerCancerType = function(cancerType, cancerTypeDetailed) {
		if (cancerTypeDetailed == null)
			return self.cancerTypeList[cancerType].sampleIds.length;
		else
			return self.cancerTypeList[cancerType].cancerTypeDetailed[cancerTypeDetailed].sampleIds.length;
	}	
	
	/**
	 * Returns the number of samples in the current case set.
	 */
	this.getCaseSetLength = function() {
		//alternative: Object.keys(this.sampleList).length  ...but could fail in older browsers....
		var count = 0;
		for (var k in this.sampleList) if (this.sampleList.hasOwnProperty(k)) count++;
		return count;
	}
	
	
	/**
	 * Returns the CANCER_TYPE list as an array such as ["breast", "lung"]
	 */
	this.getCancerTypeList = function() {
		
		var result = [];
		//return in form such as : ["breast", "lung"];
		for (var item in self.cancerTypeList) {
		    if (self.cancerTypeList.hasOwnProperty(item)) {
		        result.push(item);
		    }
		}		
		return result.sort();
	}
	
	/** 
	 * Returns the CANCER_TYPE_DETAILED list for the given cancerType. 
	 * Return format is an array such as ["CancerTypeDetailed1", "CancerTypeDetailed2"].
	 * If the cancerType == "All" , then it just returns the list of main cancer types
	 * (i.e. the value of this.getCancerTypeList() ) 
	 */
	this.getCancerTypeDetailedList = function(cancerType) {
		var result = [];
		
		if (cancerType == "All")
			return this.getCancerTypeList();
		else {
			var cancerTypeDetailedList = self.cancerTypeList[cancerType].cancerTypeDetailed;
			//return in form such as : ["s1", "s2"];
			for (var item in cancerTypeDetailedList) {
			    if (cancerTypeDetailedList.hasOwnProperty(item)) {
			        result.push(item);
			    }
			}		
			return result.sort();
		}
	}
	
	/** 
	 * Returns information on: the max number or frequency (%) of altered samples
	 * for the given cancerType and gene.
	 * If cancerType == "All", it will iterate over the main cancer types. 
	 * 
	 * If cancerType == a specific main cancer type, it will iterate over its sub cancer types. 
	 * 
	 * @param cancerType: "All" or one of the main cancer types
	 * @param geneId : gene id for which current tab was rendered
	 * @param dataTypeYAxis: set to "Alteration Frequency" to return max in frequency %
	 * 
	 * @return : max as number of samples or frequency % (depending on the value of dataTypeYAxis)
	 */
	this.getMaxAlteredSamplesForCancerTypeAndGene = function(cancerType, geneId, dataTypeYAxis) {
		//TODO : result of this function could be cached if performance becomes a problem
		console.log("Calculate getMaxAlteredSamplesForCancerTypeAndGene...");
		if (cancerType == "All") {
			//check max:
			var max = 0;
			var cancerTypes = this.getCancerTypeList();
			for (var i = 0; i < cancerTypes.length; i++) {
				var denominator = 1;
				if (dataTypeYAxis == "Alteration Frequency")
					denominator = this.getTotalNrSamplesPerCancerType(cancerTypes[i], null);
				//this method call is repeated (also called to build histogram JSON data)...TODO - performance improvement could be gained here...tests will indicate if necessary
				var value = this.getAlterationEvents(cancerTypes[i], null, geneId).all / denominator;
				if (value > max)
					max = value;
			}
			return max;
		}
		else {
			var max = 0;
			var cancerTypes = this.getCancerTypeDetailedList(cancerType);
			for (var i = 0; i < cancerTypes.length; i++) {
				var denominator = 1;
				if (dataTypeYAxis == "Alteration Frequency")
					denominator = this.getTotalNrSamplesPerCancerType(cancerType, cancerTypes[i]);
				var value = this.getAlterationEvents(cancerType, cancerTypes[i], geneId).all / denominator;
				if (value > max)
					max = value;
			}
			return max;
		}

	}

    // maybe already stored somewhere?
    this.getMaxSamplesForCancerType = function(cancerType){
        var nrSamples= 0, max=0;
        if (cancerType == "All") {
            var cancerTypes = this.getCancerTypeList();
            for (var i = 0; i < cancerTypes.length; i++) {
                nrSamples = this.getTotalNrSamplesPerCancerType(cancerTypes[i], null);
                if(nrSamples>max) max=nrSamples;
            }
        }
        else {
            var cancerTypes = this.getCancerTypeDetailedList(cancerType);
            for (var i = 0; i < cancerTypes.length; i++) {
                nrSamples = this.getTotalNrSamplesPerCancerType(cancerType, cancerTypes[i]);
                if(nrSamples>max) max=nrSamples;
            }
        }
        return max;
    }

	/**
	 * Returns the gene list chosen by user in query form.
	 */
	this.getGeneList = function(){
		return window.QuerySession.getQueryGenes();
	}
	
	/**
	 * Returns the value to be set as minimum altered samples. Depends on given max,
	 * if max is < 1 (can happen when dataTypeYAxis == "Alteration Frequency" in some rare cases) then
	 * then this function returns 0. This is to avoid the scenario where no histogram is
	 * showed at all in this case.
	 */
	this.getMinAlteredSamples = function(dataTypeYAxis, max) {
	     var defaultMinAlteredSamples=1;

		 if (dataTypeYAxis == "Alteration Frequency") {
			 //in %, with 1 decimal:
			 max = Math.round(parseFloat(this.max) * 1000)/10;

	         // in the rare cases where the maximum alteration frequency is smaller than 1%
	         // set the defaultMinAlteredSamples to 0
	         if(max<=defaultMinAlteredSamples) {
	        	 console.log("Special case (max<=1) for 'Min. % altered samples'...");
	        	 defaultMinAlteredSamples = 0;  
	         }
		 }
         return defaultMinAlteredSamples;
	}
	
}



///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            MAIN CLASS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
function PancancerStudySummary()
{
   var _cancerSummaryMainView = null;

   this.init = function()
   {
	  console.log(new Date() + ": init called");
      //Initialize presenter, which triggers the asynchronous services to get the 
	  //data and calls the callback function once the data is received:
      new DataManagerPresenter(dmInitCallBack);
   };
   
   //continues init:
   var dmInitCallBack = function(dmPresenter)
   {
	  console.log(new Date() + ": histogram data fetched and processed. Rendering of pancancer summary view");
      // create event dispacther
      var dispatcher = _.extend({}, Backbone.Events);

      var cancerSummaryMainView = new CancerSummaryMainView({dispatcher:dispatcher, dmPresenter:dmPresenter});
      _cancerSummaryMainView = cancerSummaryMainView;

      // init main controller...
      var controller = new GeneDetailsController(
         cancerSummaryMainView,
         dispatcher, 
         dmPresenter
      );

      // ...and let the fun begin!
      cancerSummaryMainView.render();
   }

   this.getView = function() {return _cancerSummaryMainView;};
}




///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//          Utils
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////



/**
* Utility class to precompile & cache backbone templates.
* The idea is that precompiled templates should increase rendering speed. 
*/
var PanCancerTemplateCache = (function () {
  var _cache = {};

  /**
   * Compiles the template for the given template id
   * by using underscore template function.
   *
   * @param templateId    html id of the template content
   * @returns function    compiled template function
   */
  function compileTemplate(templateId)
  {
     return _.template($("#" + templateId).html());
  }

  /**
   * Gets the template function corresponding to the given template id.
   *
   * @param templateId    html id of the template content
   * @returns function    template function
   */
  function getTemplateFn(templateId)
  {
     // try to use the cached value first
     var templateFn = _cache[templateId];

     // compile if not compiled yet
     if (templateFn == null)
     {
        templateFn = compileTemplate(templateId);
        _cache[templateId] = templateFn;
     }
     return templateFn;
   }

   return { getTemplateFn: getTemplateFn };
})();
