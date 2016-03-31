var modeHandler = {
  mode: "selection-mode",
  selectedNodeType: "macromolecule",
  selectedEdgeType: "consumption",
  elementsHTMLNameToName: {
    //nodes
    "macromolecule": "macromolecule",
    "simple-chemical": "simple chemical",
    "complex": "complex",
    "process": "process",
    "omitted-process": "omitted process",
    "uncertain-process": "uncertain process",
    "association": "association",
    "dissociation": "dissociation",
    "phenotype": "phenotype",
    "compartment": "compartment",
    "unspecified-entity": "unspecified entity",
    "nucleic-acid-feature": "nucleic acid feature",
    "source-and-sink": "source and sink",
    "perturbing-agent": "perturbing agent",
    "tag": "tag",
    "and": "and",
    "or": "or",
    "not": "not",
    //edges
    "consumption": "consumption",
    "production": "production",
    "modulation": "modulation",
    "stimulation": "stimulation",
    "catalysis": "catalysis",
    "inhibition": "inhibition",
    "necessary-stimulation": "necessary stimulation",
    "logic-arc": "logic arc",
    "equivalence-arc": "equivalence arc"
  },
  initilize: function () {
    $('#select-icon').addClass('selectedType');
    $("#first-sbgn-select-node-item").addClass("selectedDDItem");
    $("#first-sbgn-select-edge-item").addClass("selectedDDItem");
    this.setSelectedMenuItem("selection-mode");
  },
  setAddNodeMode: function () {
    if (modeHandler.mode != "add-node-mode") {
      $('#node-list').addClass('selectedType');

      cy.elements().unselect();

      modeHandler.setSelectedMenuItem("add-node-mode", modeHandler.selectedNodeType);
      modeHandler.mode = "add-node-mode";

      $('#select-icon').removeClass('selectedType');

      $('#edge-list').removeClass('selectedType');

      modeHandler.autoEnableMenuItems(false);

      cy.autolock(true);
      cy.autounselectify(true);

      cy.edgehandles('drawoff');
    }
  },
  setAddEdgeMode: function () {
    if (modeHandler.mode != "add-edge-mode") {
      $('#edge-list').addClass('selectedType');
      
      cy.elements().unselect();
      
      modeHandler.setSelectedMenuItem("add-edge-mode", modeHandler.selectedEdgeType);
      modeHandler.mode = "add-edge-mode";

      $('#select-icon').removeClass('selectedType');
      $('#node-list').removeClass('selectedType');

      modeHandler.autoEnableMenuItems(false);

      cy.autolock(true);
      cy.autounselectify(true);

      cy.edgehandles('drawon');
    }
  },
  setSelectionMode: function () {
    if (modeHandler.mode != "selection-mode") {
      $('#select-icon').addClass('selectedType');
      modeHandler.setSelectedMenuItem("selection-mode");

      $('#edge-list').removeClass('selectedType');
      $('#node-list').removeClass('selectedType');

      modeHandler.autoEnableMenuItems(true);

      modeHandler.mode = "selection-mode";
      cy.autolock(false);
      cy.autounselectify(false);

      cy.edgehandles('drawoff');
    }
  },
  autoEnableMenuItems: function (enable) {
    if (enable) {
      $("#expand-selected").parent("li").removeClass("disabled");
      $("#collapse-selected").parent("li").removeClass("disabled");
      $("#expand-all").parent("li").removeClass("disabled");
      $("#collapse-all").parent("li").removeClass("disabled");
      $("#perform-layout").parent("li").removeClass("disabled");
      $("#delete-selected-simple").parent("li").removeClass("disabled");
      $("#delete-selected-smart").parent("li").removeClass("disabled");
      $("#hide-selected").parent("li").removeClass("disabled");
      $("#show-selected").parent("li").removeClass("disabled");
      $("#show-all").parent("li").removeClass("disabled");
      $("#make-compound-complex").parent("li").removeClass("disabled");
      $("#make-compound-compartment").parent("li").removeClass("disabled");
      $("#neighbors-of-selected").parent("li").removeClass("disabled");
      $("#processes-of-selected").parent("li").removeClass("disabled");
      $("#remove-highlights").parent("li").removeClass("disabled");
    }
    else{
      $("#expand-selected").parent("li").addClass("disabled");
      $("#collapse-selected").parent("li").addClass("disabled");
      $("#expand-all").parent("li").addClass("disabled");
      $("#collapse-all").parent("li").addClass("disabled");
      $("#perform-layout").parent("li").addClass("disabled");
      $("#delete-selected-simple").parent("li").addClass("disabled");
      $("#delete-selected-smart").parent("li").addClass("disabled");
      $("#hide-selected").parent("li").addClass("disabled");
      $("#show-selected").parent("li").addClass("disabled");
      $("#show-all").parent("li").addClass("disabled");
      $("#make-compound-complex").parent("li").addClass("disabled");
      $("#make-compound-compartment").parent("li").addClass("disabled");
      $("#neighbors-of-selected").parent("li").addClass("disabled");
      $("#processes-of-selected").parent("li").addClass("disabled");
      $("#remove-highlights").parent("li").addClass("disabled");
    }
  },
  setSelectedIndexOfSelector: function (mode, value) {
    if(mode == "add-node-mode"){
      $(".selectedType").removeClass("selectedType");
      $("#node-list").addClass("selectedType");
      $("#node-list li").removeClass("selectedDDItem");;
      var ele = $("#node-list [value=" + value + "]");
      var text = $(ele).parent('a').text();
      var src = $(ele).attr('src');
      $("#node-list-set-mode-btn").attr("title", "Create a new " + text);
//      $('#sbgn-selected-node-text').text(text);
      $('#sbgn-selected-node-img').attr('src', src);
      $(ele).parent('a').parent('li').addClass("selectedDDItem");
    }
    else if(mode == "add-edge-mode"){
      $(".selectedType").removeClass("selectedType");
      $("#edge-list").addClass("selectedType");
      $("#edge-list li").removeClass("selectedDDItem");
      var ele = $("#edge-list [value=" + value + "]");
      var text = $(ele).parent('a').text();
      var src = $(ele).attr('src');
      $("#edge-list-set-mode-btn").attr("title", "Create a new " + text);
//      $('#sbgn-selected-edge-text').text(text);
      $('#sbgn-selected-edge-img').attr('src', src);
      $(ele).parent('a').parent('li').addClass("selectedDDItem");
    }
  },
  
  setSelectedMenuItem: function (mode, name) {
    $(".selectedMenuItem").removeClass("selectedMenuItem");

    if (mode == "selection-mode") {
      $('#select-edit').addClass('selectedMenuItem');
    }
    else if (mode == "add-node-mode") {
      $('#add-node-menu-option').addClass('selectedMenuItem');
      var menuItem = $("#add-node-submenu [name=" + name + "]");
      menuItem.addClass("selectedMenuItem");
      if (menuItem.hasClass("process-type")) {
        $('#process-menu-option').addClass("selectedMenuItem");
      }
      if (menuItem.hasClass("logical-operator-type")) {
        $('#logical-operator-menu-option').addClass("selectedMenuItem");
      }
    }
    else if (mode == "add-edge-mode") {
      $('#add-edge-menu-option').addClass('selectedMenuItem');
      var menuItem = $("#add-edge-submenu [name=" + name + "]");
      menuItem.addClass("selectedMenuItem");
    }
  }
};