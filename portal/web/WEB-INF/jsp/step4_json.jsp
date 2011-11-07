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
    <span style="float:right">
        <a href="onco_query_lang_desc.jsp" onclick="return popitup('onco_query_lang_desc.jsp')">Advanced:  Onco Query Language (OQL)</a>
    </span>

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
            $("#genestatus").html("<img src='images/ajax-loader2.gif'> <small>Validating gene symbols...</small>");
            $("#main_submit").attr("disabled", "disabled");

            var genes = [];
            var content = $("#gene_list").val();

            if( content.search(":") > -1 ) {
                var lines = content.split("\n");
                for(var i=0; i < lines.length; i++) {
                    var values = lines[i].split(":");
                    genes.push(values[0].trim());
                }
            } else {
                var values = content.split(" ");
                for(var i=0; i < values.length; i++) {
                    var values2 = values[i].split("\n");
                    for(var j=0; j < values2.length; j++) {
                        genes.push(values2[j].trim());
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
                          var stateList = $("<ul>").addClass("ui-widget icon-collection");
                          var allValid = true;

                          for(var i=0; i < genes.length; i++) {
                              var found = false;
                              var multiple = false;
                              var symbols = [];

                              if(genes[i] == "")
                                  continue;

                              for(var j=0; j < symbolResults.length; j++) {
                                  var aResult = symbolResults[j];
                                  if( aResult.name.toUpperCase() == genes[i].toUpperCase() ) {
                                      if( aResult.symbols.length == 1 ) {
                                          found = true;
                                          multiple = false;
                                      } else if( aResult.symbols.length > 1 ) {
                                          found = true;
                                          multiple = true;
                                          symbols = aResult.symbols;
                                      }
                                      break;
                                  }
                              }

                              if(found && !multiple)
                                  continue;
                              else
                                allValid = false;

                              var state = $("<li>").addClass("ui-state-default ui-corner-all");
                              if(!found) {
                                    state.click(function(){
                                          $(this).toggleClass('ui-state-active');
                                          geneName = $(this).attr("name");
                                          $("#gene_list").val($("#gene_list").val().replace(" " + geneName, ""));
                                          $("#gene_list").val($("#gene_list").val().replace(geneName + " ", ""));
                                          $("#gene_list").val($("#gene_list").val().replace(geneName, ""));
                                          validateGenes();
                                      });
                              }

                              var stateSpan = $("<span>")
                                  .addClass("ui-icon " + (found ? "ui-icon-help": "ui-icon-circle-close"));
                              var stateText = $("<span>").addClass("text");

                              if(multiple) {
                                  stateText.html("<b>" + genes[i] + "</b>");
                                  stateText.html(stateText.html() + ": ");

                                  for(var k=0; k < symbols.length; k++) {
                                    var altLink = $("<a>").attr("name", symbols[k]);
                                    altLink.html(symbols[k]);
                                    altLink.appendTo(stateText);
                                    altLink.click(function() {
                                       alert("OK" + genes[i] + "/" + symbols[k]);
                                       $("#gene_list").val($("#gene_list").val().replace(" " + genes[i], symbols[k]));
                                       $("#gene_list").val($("#gene_list").val().replace(genes[i] + " ", symbols[k]));
                                       $("#gene_list").val($("#gene_list").val().replace(genes[i], symbols[k]));
                                       validateGenes();
                                    });
                                    stateText.html(stateText.html() + " ");
                                  }

                              } else {
                                  stateText.html(genes[i]);
                              }

                              stateSpan.appendTo(state);
                              stateText.insertAfter(stateSpan);
                              state.attr("title",
                                (found
                                       ? "Ambiguous gene symbol. Click on one of the alternate symbols to replace it."
                                       : "Could not find gene symbol. Click to remove it from the gene list."
                                )
                              );
                              state.attr("name", genes[i]);
                              if(found) {
                                  state.addClass("ui-state-active");
                              }
                              state.appendTo(stateList);
                          }

                          stateList.appendTo("#genestatus");

                          $('.ui-state-default').hover(
                              function(){ $(this).addClass('ui-state-hover'); },
                              function(){ $(this).removeClass('ui-state-hover'); }
                          );

                          $('.ui-state-default').tipTip();

                          if( allValid ) {
                                $("#main_submit").removeAttr("disabled").removeAttr("title")
                                    .attr("title", "Click to submit.").tipTip();

                                var validState = $("<li>").addClass("ui-state-default ui-corner-all");
                                var validSpan = $("<span>")
                                  .addClass("ui-icon ui-icon-circle-check");
                                var validText = $("<span>").addClass("text");
                                validText.html("All gene symbols are valid");

                                validSpan.appendTo(validState);
                                validText.insertAfter(validSpan);
                                validState.addClass("ui-state-active");
                                validState.appendTo(stateList);
                          } else {
                                $("#main_submit")
                                    .attr("title", "Gene symbols are not valid. Please edit the gene list.")
                                    .tipTip();
                          }

                          $("<br>").appendTo(stateList);
                  },
                  'json'
            );
        }

        $(document).ready(function() {
		    $("#gene_list").data("timerid", null).bind('keyup', function(e) {
                clearTimeout(jQuery(this).data('timerid'));
                jQuery(this).data('timerid', setTimeout(validateGenes, 1000));
    		});

            $("#select_gene_set").combobox();
        });

        (function($) {
		$.widget("ui.combobox", {
			_create: function() {
				var self = this;
				var select = this.element.hide();
                var defaultText = select.children("option:first").text();
				var input = $("<input size='60'>")
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
				.addClass("ui-corner-right ui-button-icon")
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
				});

			}
		});

	})(jQuery);

    </script>

    <style>
        ul {margin: 0; padding: 0;}
		li.ui-state-default {
		    margin: 2px;
		    position: relative;
		    padding: 4px 0;
		    cursor: pointer;
		    float: left;
		    list-style: none;
		}
		span.ui-icon {float: left; margin: 0 4px;}
		span.text {float: left; padding-right: 5px;}
		.ui-button-icon-only .ui-button-text { padding: 0.35em; }
		.ui-autocomplete-input { padding: 0.48em 0 0.47em 0.45em; }
		.ui-menu-item { font-size: 0.6em; }
		#genestatus { clear: both; }
		#example_gene_set { clear: both; }
	</style>


<textarea rows='5' cols='80' id='gene_list' placeholder="Enter HUGO Gene Symbols" required name='<%= QueryBuilder.GENE_LIST %>'><%
    if (localGeneList != null && localGeneList.length() > 0) {
        out.println(org.mskcc.portal.oncoPrintSpecLanguage.Utilities.appendSemis(localGeneList));
    }
%>
</textarea>

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