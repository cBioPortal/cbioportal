/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */
/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <h2ttp://www.gnu.org/licenses/>.
 */
 
 /*
  * This file handles gene validation for symbols entered into the oql editor box.
  * All parsed entered symbols are wrapped in individual spans.
  * For each invalid gene, a replace div and a replace state is generated using Handlebars templates.
  */
 
var GeneSymbolValidator = (function($) {
	var gene_list;
	var replaceListTemplate;
	var replaceDropdownTemplate;
	var stateTemplate;
	var spanTemplate;
	var lineHeight;

	var COLOR = {
		AMBIGUOUS: 'orange',
		CORRECT: 'green',
		INCORRECT: 'red',
	};

	var CHAR = {
		SPACE: 32,
		COLON_FF: 59,
		COLON_CH: 186,
		TAB: 9,
		SHIFT: 16,
		CTRL: 17,
		LEFT: 37,
		UP: 38,
		RIGHT: 39,
		DOWN: 40,
		isArrowKey: function(key) {
			return (key >= 37 && key <= 40);
		}
	};

	// max # of genes to show in a list before converting to dropdown
	var DROPDOWN_CUTOFF = 4;

	var initialize = function() {

		// Initialize rangy (to maintain caret position during inserts)
		gene_list = document.getElementById('gene_list');
		rangy.init();

		// Compile templates
		replaceListTemplate = Handlebars.compile($('#hb_replace_list').html());
		replaceDropdownTemplate = Handlebars.compile($('#hb_replace_dropdown').html());
		stateTemplate = Handlebars.compile($('#hb_state').html());
		spanTemplate = Handlebars.compile($('#hb_span').html());

		//Determine line height
		var fontSize = $('#gene_list').css('font-size');
		lineHeight = Math.floor(parseInt(fontSize.replace('px', '')) * 1.5);

		// Bind validation event
		$('#gene_list').data('timerid', null).on('keyup', function(e) {
			if (e.which === CHAR.SPACE || e.which === CHAR.COLON_FF || e.which === CHAR.COLON_CR) {
				var inner = $('#gene_list').html();
				var re = new RegExp('([: \\t]+)(</span>)', 'm');
				if (re.test(inner)) {
					inner = inner.replace(re, '$2$1');
					replaceAll(inner);
				}
			} else if (CHAR.isArrowKey(e.which) || e.which === CHAR.CTRL || e.which === CHAR.SHIFT) {
				return;
			}

			clearTimeout(jQuery(this).data('timerid'));
			jQuery(this).data('timerid', setTimeout(validateGenes, 500));
		});
		
		// Bind newline event
		$('#newline_checkbox').change(function() {
			if ($('#newline_checkbox').prop('checked')) {
				newlineSymbols();
				bindEvents();
			}
		});

		// Validate immediately if #gene_list not empty
		if ($('#gene_list').html().length > 0) {
			validateGenes();
		}
	};

	var getRawText = function() {
		return rangy.innerText(gene_list);
		//return $('#gene_list').text(); // alternative
	};

	// Get absolute position of element
	var cumulativeOffset = function(element) {
		var top = 0,
			left = 0;
		do {
			top += element.offsetTop || 0;
			left += element.offsetLeft || 0;
			element = element.offsetParent;
		} while (element);

		top += lineHeight;

		return {
			top: top,
			left: left
		};
	};

	// Replace gene symbol
	var replaceSymbol = function(symbol, replacement) {
		var savedSel = rangy.getSelection().saveCharacterRanges(gene_list);
		var innerHTML = $('#gene_list').html();
		var re = new RegExp('(^|[^A-Za-z0-9>])(' + symbol + ')([^A-Za-z0-9<]|$)', 'm');
		$('#gene_list').html(innerHTML.replace(re, '$1' + replacement + '$3'));
		
		// remove empty lines
		// if (replacement === ''){
		// 	validateGenes();
		// 	innerHTML = $('#gene_list').html();
		// 	var re_blankline = new RegExp('^\s*[\r\n]', 'mg');
		// 	$('#gene_list').html(innerHTML.replace(re_blankline, ''));
		// }
		
		rangy.getSelection().restoreCharacterRanges(gene_list, savedSel);
	};

	// Replace all text in #gene_list
	var replaceAll = function(newText) {
		var savedSel = rangy.getSelection().saveCharacterRanges(gene_list);
		$('#gene_list').html(newText);
		rangy.getSelection().restoreCharacterRanges(gene_list, savedSel);
	};

	// Newlining of gene symbols
	var newlineSymbols = function() {
		var savedSel = rangy.getSelection().saveCharacterRanges(gene_list);
		var re = new RegExp('(.*[ \t])(<span.*)', 'gm');
		var count = 0;
		var innerHTML = $('#gene_list').html();

		while (re.test(innerHTML)) {
			innerHTML = innerHTML.replace(re, '$1' + '\n' + '$2');
			count++;
		}
		$('#gene_list').html(innerHTML);
		// savedsel[0].characterRange.start += count;
		// savedsel[0].characterRange.end += count;
		rangy.getSelection().restoreCharacterRanges(gene_list, savedSel);
		rangy.getSelection().move('character', count);
	};
	
	// Event handlers
	var bindEvents = function() {

		var timer;
		$('.replace-span, .replace-state').mouseenter(function() {
			$('.replace-div').hide();
			showReplaceDiv($(this));
		});

		$('.replace-span, .replace-div, .replace-state').mouseleave(function() {
			hideReplaceDiv();
		});

		$('.replace-div').mouseenter(function() {
			clearTimeout(timer);
		});

		$('.replace-div-link, .replace-div-remove').click(function() {
			replaceDivClick($(this));
		});

		$('.replace-div-select').change(function() {
			replaceDivClick($(this));
		});

		$('.ui-state-default').hover(
			function() {
				$(this).addClass('ui-state-hover');
			},
			function() {
				$(this).removeClass('ui-state-hover');
			}
		);

		function showReplaceDiv($this) {
			var index = $this.attr('data-index');
			clearTimeout(timer);
			var offset = cumulativeOffset($this[0]);
			$('.replace-div' + '[data-index="' + index + '"]').css(offset);
			$('.replace-div' + '[data-index="' + index + '"]').show();
		}

		function hideReplaceDiv() {
			clearTimeout(timer);
			timer = setTimeout(function() {
				$('.replace-div').hide();
			}, 500);
		}

		function replaceDivClick($this) {
			var index = $this.attr('data-index');
			var replacement = $this.attr('val');

			if (undefined === replacement) {
				replacement = $this.val();
			}

			if (replacement === '') {
				$('.replace-span[data-index="' + index + '"]').remove();
			} else {
				$('.replace-span[data-index="' + index + '"]').html(replacement);
				$('.replace-span[data-index="' + index + '"]').off();
				$('.replace-span[data-index="' + index + '"]').removeClass().addClass('noreplace-span-valid');
			}

			$('.replace-div[data-index="' + index + '"]').remove();
			$('.replace-state[data-index="' + index + '"]').remove();
			if ($('.replace-state').length === 0) {
				validateGenes();
			}
		}

	};

	//
	var validateGenes = function() {
		console.log('validateGene');
		$('#main_submit').attr('disabled', 'disabled');

		var rawtext = getRawText();
		rawtext = rawtext.toUpperCase();


		if (rawtext.length === 0) {
			$('#state_placeholder').html('');
			return;
		}

		try {
			var non_datatypes_lines = oql_parser.parse(rawtext).filter(function(parsed_line) {
				return parsed_line.gene !== 'DATATYPES';
			});

			if (non_datatypes_lines.length === 0) {
				$('#state_placeholder').html('');
				return;
			}

			replaceAll(rawtext);

			$('#state_placeholder').html('<img src="images/ajax-loader2.gif"> <small>Validating gene symbols...</small>');
			var genesStr = non_datatypes_lines.map(function(parsed_line) {
				return parsed_line.gene;
			}).join(',');

			$.post(
				'CheckGeneSymbol.json', // testUrl is for PhantomJS testing
				{
					'genes': genesStr
				},
				function(symbolResults) {

					// If the # of genes is more than 100, show an error
					if (symbolResults.length > 100) {
						var stateContext = {
							icon: 'fa-exclamation-triangle',
							text: 'You have entered more than 100 genes.',
							class: 'ui-state-default ui-corner-all ui-state-active'
						};
						$('#state_placeholder').html(stateTemplate(stateContext));
						return;
					}

					var divList = '';
					var stateList = '';
					var allValid = true;

					for (var j = 0; j < symbolResults.length; j++) {
						var result = symbolResults[j];
						var gene = result.name.toUpperCase();

						// All genes valid
						if (result.hasOwnProperty('symbols') && (result.symbols.length > 0) && (result.symbols[0].toUpperCase() === gene)) {
							var spanContext = {
								index: j,
								symbol: gene
								class: 'noreplace-span-valid',
							};
							replaceSymbol(gene, spanTemplate(spanContext));
						}

						// Some genes invalid
						else {
							allValid = false;
							var divContext, spanContext, stateContext;

							// Synonym/Ambiguous symbol - show valid symbols
							if (result.hasOwnProperty('symbols') && result.symbols.length > 0) {
								divContext = {
									index: j,
									symbol: gene,
									replacement: result.symbols,
									color: COLOR.AMBIGUOUS
								};
								spanContext = {
									index: j,
									symbol: gene,
									class: 'replace-span replace-span-synonym'
								};
								stateContext = {
									index: j,
									icon: 'fa-question',
									class: 'ui-state-default ui-corner-all ui-validator replace-state'
								};
								if (result.symbols.length > 1) {
									stateContext.text = '<b>' + gene + '</b>: Ambiguous';
								} else {
									stateContext.text = '<b>' + gene + '</b>: Alias';
								}
							}

							// Incorrect symbol - show suggestions
							else {
								
								divContext = {
									index: j,
									symbol: gene,
									color: COLOR.INCORRECT
								};
								divContext.replacement = result.suggestions || [];
								divContext.replacement = divContext.replacement.slice(0, DROPDOWN_CUTOFF);

								spanContext = {
									index: j,
									symbol: gene,
									class: 'replace-span replace-span-wrong'
								};
								stateContext = {
									index: j,
									icon: 'fa-times',
									text: '<b>' + gene + '</b>: No match',
									class: 'ui-state-default ui-corner-all ui-validator replace-state'
								};
							}

							// Generate Span
							replaceSymbol(gene, spanTemplate(spanContext));

							// Generate Div
							// Decide if to display as list or as dropdown
							if (divContext.replacement.length > DROPDOWN_CUTOFF) {
								divContext.replacement.unshift('Select');
								divList += replaceDropdownTemplate(divContext);
							} else {
								divList += replaceListTemplate(divContext);
							}

							// Generate State
							stateList += stateTemplate(stateContext);
						}
					} // end of for

					// Make status indicator
					if (allValid) {
						$('#main_submit').removeAttr('disabled');
						if (symbolResults.length > 0 && !(symbolResults[0].name === '' && symbolResults[0].symbols.length === 0)) {
							var stateContext = {
								icon: 'fa-check-circle',
								text: 'All gene symbols valid',
								class: 'ui-state-default ui-corner-all ui-state-active'
							};
							stateList = stateTemplate(stateContext);
						}
					} else { // some symbol invalid
						var stateContext = {
							icon: 'fa-exclamation-triangle',
							text: 'Invalid gene symbols',
							class: 'ui-state-default ui-corner-all ui-state-active'
						};
						stateList = stateTemplate(stateContext) + stateList;
					}

					// Wrap States in <ul>
					var stateWrapper = $('<ul>').addClass('ui-widget icon-collection validation-list');
					stateWrapper.html(stateList);

					// Add elements to page
					$('#div_placeholder').html(divList);
					$('#state_placeholder').html(stateWrapper);

					if ($('#newline_checkbox').prop('checked')) {
						newlineSymbols();
					}

					// $('.ui-validator').tipTip();
					bindEvents();
				},
				'json'
			);
		} catch (e) {
			console.log(e);
			$('#state_placeholder').html('');
			$('<small>').appendTo($('#state_placeholder')).html('Cannot validate gene symbols because of invalid OQL. Please click "Submit" to see location of error.');
			$('#main_submit').removeAttr('disabled');
		}
	};

	return {
		initialize: initialize,
		validateGenes: validateGenes
	};

})(jQuery);

// module.exports.GeneSymbolValidator = GeneSymbolValidator;
