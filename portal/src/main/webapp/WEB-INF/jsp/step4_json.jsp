<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
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
    <div style='padding-top:10px;'>
        <button id="toggle_mutsig_dialog" onclick="promptMutsigTable(); return false;" style="font-size: 1em;">Select From Recurrently Mutated Genes (MutSig)</button>
        <button id="toggle_gistic_dialog_button" onclick="Gistic.UI.open_dialog(); return false;" style="font-size: 1em; display: none;">Select Genes from Recurrent CNAs (Gistic)</button>
    </div>

    <P/>
    <script type="text/javascript">
        $(document).ready(function() {
            GeneSymbolValidator.initialize();
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

<textarea rows='5' cols='80' id='gene_list' placeholder="Enter HUGO Gene Symbols or Gene Aliases" required
name='<%= QueryBuilder.GENE_LIST %>'><%
    if (localGeneList != null && localGeneList.length() > 0) {
        out.print(org.mskcc.cbio.portal.oncoPrintSpecLanguage.Utilities.appendSemis(localGeneList));
    }
%></textarea>

<p id="genestatus"></p>

    
   <p id="example_gene_set"><span style="font-size:80%">Select from Example Gene Sets:<br>
    <select id="select_gene_set" name="<%= QueryBuilder.GENE_SET_CHOICE %>"></select></span></p>

</div>
<script type='text/javascript'>
$('#toggle_gistic_dialog_button').button();
</script>
