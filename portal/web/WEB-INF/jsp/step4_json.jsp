<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%
    String step4ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP4_ERROR_MSG);
%>

<div class="query_step_section">
    <span class="step_header">Enter Gene Set:</span>

    <script language="javascript" type="text/javascript">

    function popitup(url) {
        newwindow=window.open(url,'OncoSpecLangInstructions','height=1000,width=1000,left=400,top=0,scrollbars=yes');
        if (window.focus) {newwindow.focus()}
        return false;
    }
    </script>
    <span style="font-size:120%; color:black">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="onco_query_lang_desc.jsp" onclick="return popitup('onco_query_lang_desc.jsp')">Advanced:  Onco Query Language (OQL)</a></span>
    <script type="text/javascript" src="js/mutsig.js"></script>
    <span id="MutSig_view" style="word-wrap:break-word;float:right"> <a href="" id="toggle_mutsig">View Significantly Mutated Genes</a></span>

<%
// Output step 4 form validation error
if (step4ErrorMsg != null) {
    out.println("<div class='ui-state-error ui-corner-all'>"
            + "<span class='ui-icon ui-icon-alert' style='float: left; margin-right: .3em;'></span>"
            + "<strong>" + step4ErrorMsg + "</strong>");
}
%>

    <P/>
    <script type="text/javascript">
        function validateGenes() {
            $("#gene_list").val($("#gene_list").val().replace("  ", " "));
            $("#genestatus").html("<img src='images/ajax-loader2.gif'> <small>Validating gene symbols...</small>");
            $("#main_submit").attr("disabled", "disabled");

            var genes = [];
            var content = $("#gene_list").val();

            if( content.toUpperCase().search("DATATYPES") > -1 ) {
                var endOfStatement = content.search(";");
                if( endOfStatement < 0 ) {
                    endOfStatement = content.search("\n");
                }

                content = content.substring(endOfStatement+1);
            }

            if( content.search(":") > -1 ) {
                var lines = content.split("\n");
                for(var i=0; i < lines.length; i++) {
                    var tokens = lines[i].split(";");
                    for(var j=0; j < tokens.length; j++) {
                        var values = tokens[j].split(":");
                        genes.push($.trim(values[0]));
                    }
                }
            } else {
                var values = content.split(" ");
                for(var i=0; i < values.length; i++) {
                    var values2 = values[i].split("\n");
                    for(var j=0; j < values2.length; j++) {
                        genes.push($.trim(values2[j]));
                    }
                }
            }

            var genesStr = "";
            for(var i=0; i < genes.length; i++) {
                if(genes[i] == "")
                    continue;

                genesStr += genes[i] + " ";
            }

            $.get(
                  'CheckGeneSymbol.json',
                  { 'genes': genesStr },
                  function(symbolResults) {
                          $("#genestatus").html("");
                          var stateList = $("<ul>").addClass("ui-widget icon-collection validation-list");
                          var allValid = true;

                          for(var i=0; i < genes.length; i++) {
                              var found = false;
                              var multiple = false;
                              var foundSynonym = false;
                              var symbols = [];

                              if(genes[i] == "")
                                  continue;

                              for(var j=0; j < symbolResults.length; j++) {
                                  var aResult = symbolResults[j];
                                  if( aResult.name.toUpperCase() == genes[i].toUpperCase() ) {
                                      if( aResult.symbols.length == 1 ) {
                                          found = true;
                                          multiple = false;
                                          foundSynonym = aResult.symbols[0].toUpperCase() != aResult.name.toUpperCase();
                                      } else if( aResult.symbols.length > 1 ) {
                                          found = true;
                                          multiple = true;
                                          symbols = aResult.symbols;
                                      }
                                      break;
                                  }
                              }

                              if(found && !foundSynonym && !multiple)
                                  continue;
                              else
                                allValid = false;

                              if(multiple) {
                                   var state = $("<li>").addClass("ui-state-default ui-corner-all");
                                   var stateSpan = $("<span>").addClass("ui-icon ui-icon-help");
                                   var stateText = $("<span>").addClass("text");

                                   stateText.html(genes[i] + ": ");
                                   var nameSelect = $("<select>").addClass("geneSelectBox").attr("name", genes[i]);
                                   $("<option>").attr("value", "")
                                        .html("select a symbol")
                                        .appendTo(nameSelect);
                                   for(var k=0; k < symbols.length; k++) {
                                        var aSymbol = symbols[k];
                                        var anOption = $("<option>").attr("value", aSymbol).html(aSymbol);
                                        anOption.appendTo(nameSelect);
                                   }
                                   nameSelect.appendTo(stateText);
                                   nameSelect.change(function() {
                                         var trueSymbol = $(this).attr('value');
                                         var geneName = $(this).attr("name");
                                         $("#gene_list").val($("#gene_list").val().replace(geneName, trueSymbol));
                                         setTimeout(validateGenes, 500);
                                   });

                                   stateSpan.appendTo(state);
                                   stateText.insertAfter(stateSpan);
                                   state.attr("title",
                                         "Ambiguous gene symbol. Click on one of the alternatives to replace it."
                                   );
                                   state.attr("name", genes[i]);
                                   state.appendTo(stateList);
                              } else if( foundSynonym ) {
                                     var state = $("<li>").addClass("ui-state-default ui-corner-all");
                                     var trueSymbol = aResult.symbols[0];

                                     state.click(function(){
                                          $(this).toggleClass('ui-state-active');
                                          var names = $(this).attr("name").split(":");
                                          var geneName = names[0];
                                          var symbol = names[1];
                                          $("#gene_list").val($("#gene_list").val().replace(geneName, symbol));
                                          setTimeout(validateGenes, 500);
                                     });

                                     var stateSpan = $("<span>").addClass("ui-icon ui-icon-help");
                                     var stateText = $("<span>").addClass("text");
                                     stateText.html("<b>" + genes[i] + "</b>: " + trueSymbol);
                                     stateSpan.appendTo(state);
                                     stateText.insertAfter(stateSpan);
                                     state.attr("title",
                                            "'" + genes[i] + "' is a synonym for '" + trueSymbol + "'. "
                                                + "Click here to replace it with the official symbol."
                                     );
                                     state.attr("name", genes[i] + ":" + trueSymbol);
                                     state.appendTo(stateList);
                              } else {
                                     var state = $("<li>").addClass("ui-state-default ui-corner-all");
                                     state.click(function(){
                                          $(this).toggleClass('ui-state-active');
                                          geneName = $(this).attr("name");
                                          $("#gene_list").val($("#gene_list").val().replace(" " + geneName, ""));
                                          $("#gene_list").val($("#gene_list").val().replace(geneName + " ", ""));
                                          $("#gene_list").val($("#gene_list").val().replace(geneName + "\n", ""));
                                          $("#gene_list").val($("#gene_list").val().replace(geneName, ""));
                                          setTimeout(validateGenes, 500);
                                     });
                                     var stateSpan = $("<span>").addClass("ui-icon ui-icon-circle-close");
                                     var stateText = $("<span>").addClass("text");
                                     stateText.html(genes[i]);
                                     stateSpan.appendTo(state);
                                     stateText.insertAfter(stateSpan);
                                     state.attr("title",
                                        "Could not find gene symbol. Click to remove it from the gene list."
                                     );
                                     state.attr("name", genes[i]);
                                     state.appendTo(stateList);
                              }
                          }

                          stateList.appendTo("#genestatus");

                          $('.ui-state-default').hover(
                              function(){ $(this).addClass('ui-state-hover'); },
                              function(){ $(this).removeClass('ui-state-hover'); }
                          );

                          $('.ui-state-default').tipTip();

                          if( allValid ) {
                                $("#main_submit").removeAttr("disabled").removeAttr("title")

                                if( symbolResults.length > 0
                                    && !(symbolResults[0].name == "" && symbolResults[0].symbols.length == 0) ) {

                                    var validState = $("<li>").addClass("ui-state-default ui-corner-all");
                                    var validSpan = $("<span>")
                                      .addClass("ui-icon ui-icon-circle-check");
                                    var validText = $("<span>").addClass("text");
                                    validText.html("All gene symbols are valid.");

                                    validSpan.appendTo(validState);
                                    validText.insertAfter(validSpan);
                                    validState.addClass("ui-state-active");
                                    validState.appendTo(stateList);
                                    validState.attr("title", "You can now submit the list").tipTip();
                                    $("<br>").appendTo(stateList);
                                    $("<br>").appendTo(stateList);
                                }

                          } else {
                                var invalidState = $("<li>").addClass("ui-state-default ui-corner-all");
                                var invalidSpan = $("<span>")
                                  .addClass("ui-icon ui-icon-notice");
                                var invalidText = $("<span>").addClass("text");
                                invalidText.html("<b>Invalid gene symbols.</b>");

                                invalidState.attr("title", "Please edit the gene symbols").tipTip();

                                invalidSpan.appendTo(invalidState);
                                invalidText.insertAfter(invalidSpan);
                                invalidState.addClass("ui-state-active");
                                invalidState.prependTo(stateList);

                                /*
                                $("#main_submit")
                                    .attr("title", "Gene symbols are not valid. Please edit the gene list.")
                                    .tipTip();
                                */

                                $("<br>").appendTo(stateList);
                                $("<br>").appendTo(stateList);
                          }
                  },
                  'json'
            );
        }

        $(document).ready(function() {
		    $("#gene_list").data("timerid", null).bind('keyup', function(e) {
                clearTimeout(jQuery(this).data('timerid'));
                jQuery(this).data('timerid', setTimeout(validateGenes, 500));
    		});

            $("#select_gene_set").combobox();
        });

        (function($) {
		$.widget("ui.combobox", {
			_create: function() {
				var self = this;
				var select = this.element.hide();
                var defaultText = select.children("option:first").text();
				var input = $("<input size='80'>")
					.insertAfter(select)
					.autocomplete({
						source: function(request, response) {
							var matcher = new RegExp(request.term, "i");
							response(select.children("option").map(function() {
								var text = $(this).text();
								if (!request.term || matcher.test(text))
									return {
										id: $(this).val(),
										value: text
									};
							}));
						},
						delay: 0,
						select: function(e, ui) {
							if (!ui.item) {
								// remove invalid value, as it didn't match anything
								$(this).val("");
								return;
							}
							$(this).focus();
							select.val(ui.item.id);
							self._trigger("selected", null, {
								item: select.find("[value='" + ui.item.id + "']")
							});

							geneSetSelected();
							validateGenes();
						},
						minLength: 0
					})
					.addClass("ui-widget ui-widget-content ui-corner-left");

				$("<button>&nbsp;</button>")
				.insertAfter(input)
				.button({
					icons: {
						primary: "ui-icon-triangle-1-s"
					},
					text: false
				}).removeClass("ui-corner-all")
				.addClass("ui-corner-right ui-button-icon geneset-button")
				.position({
					my: "left center",
					at: "right center",
					of: input,
					offset: "-1 0"
				}).css("top", "")
				.css("left", 0)
				.click(function() {
					// close if already visible
					if (input.autocomplete("widget").is(":visible")) {
						input.autocomplete("close");
						return false;
					}
					// pass empty string as value to search for, displaying all results
					input.autocomplete("search", "");
					input.focus();

					return false;
				}).attr("title", "Click to see the example gene sets")
				.tipTip();

			}
		});

	})(jQuery);

    </script>

    <style>
        #genestatus ul.validation-list {margin: 0; padding: 0;}
		#genestatus * li.ui-state-default {
		    margin: 2px;
		    position: relative;
		    padding: 4px 0;
		    cursor: pointer;
		    float: left;
		    list-style: none;
		    height: 20px;
		}
		#genestatus * span.ui-icon {float: left; margin: 0 4px;}
		#genestatus * span.text {float: left; padding-right: 5px;}
		.geneset-button .ui-button-text { padding: 0.35em; }
		.ui-autocomplete-input { padding: 0.48em 0 0.47em 0.45em; }
		ul.ui-autocomplete li.ui-menu-item { font-size: 0.6em; text-align: left; }
		#genestatus { clear: both; }
		#example_gene_set { clear: both; }
	</style>

<div class="MutSig_table" style="overflow:auto; height:128px; width:100%;"></div>

<textarea rows='5' cols='80' id='gene_list' placeholder="Enter HUGO Gene Symbols or Gene Aliases" required
name='<%= QueryBuilder.GENE_LIST %>'><%
    if (localGeneList != null && localGeneList.length() > 0) {
        out.print(org.mskcc.portal.oncoPrintSpecLanguage.Utilities.appendSemis(localGeneList));
    }
%></textarea>

<p id="genestatus"></p>

<%
// Output step 4 form validation error
if (step4ErrorMsg != null) {
    out.println("</div>");
}
%>
    
   <p id="example_gene_set"><span style="font-size:80%">Or Select from Example Gene Sets:<br>
    <select id="select_gene_set" name="<%= QueryBuilder.GENE_SET_CHOICE %>"></select></span></p>
</div>
