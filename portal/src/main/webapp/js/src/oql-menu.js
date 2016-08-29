/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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
 * This file generates and controls the OQL keyword helper menu for the
 * gene editor box.
 *
 * The OQL menu is generated using the model in this file, which is passed to
 * Handlebars for templating. Events to remove and show the menu are bound to
 * the generated menu.
 */


var OqlMenu = (function($){
	var $TA, $ME, lineHeight, gene_list;
	var inState = false;	// whether menu should be relaunched on space
	
	// Generates OQL menu based on model and inserts into page
	var initialize = function(){
		gene_list = document.getElementById('gene_list');
		rangy.init();
		
		console.log('oql init');
		rangy.init();
		generateList();
		$TA = $('#gene_list');
		$ME = $('#oql-menu');
		var fontSize = $('#gene_list').css('font-size');
		lineHeight =  Math.floor(parseInt(fontSize.replace('px', '')) * 1.5);
		bindEvents();
	};
	
	var CHAR = {
		BACKSPACE: 8,
		COLON: 58,
		DOWN: 40,
		ENTER: 13,
		ESCAPE: 27,
		LEFT: 37,
		LINEFEED: '&#10;',
		RIGHT: 39,
		SPACE: 32,
		TAB: 9,
		UP: 38
	};
	
	var insert = function(text) {
		// var savedSel = rangy.getSelection().saveCharacterRanges(gene_list);
		console.log('inserting:'+text);
		var sel = rangy.getSelection();
		var range = sel.rangeCount ? sel.getRangeAt(0) : null;
		if (range) {
			var el = document.createElement("x");
			el.appendChild(document.createTextNode(text));
			range.insertNode(el);
			range.setStartAfter(el);
			rangy.getSelection().setSingleRange(range);
		}
		// var sel, range;
		// if (window.getSelection) {
		// 	sel = window.getSelection();
		// 	if (sel.getRangeAt && sel.rangeCount) {
		// 		range = sel.getRangeAt(0);
		// 		range.deleteContents();
		// 		range.insertNode(document.createTextNode(text));
		// 		range.collapse(true);
		// 	}
		// } else if (document.selection && document.selection.createRange) {
		// 	document.selection.createRange().text = text;
		// }
		inState = true;
	};
	
	var caretIndex = function(text) {
		// var sel, range;
		if (window.getSelection){
			sel = window.getSelection();
			if (sel.getRangeAt && sel.rangeCount){
				return sel.getRangeAt(0).startOffset;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	};
	
	var showMenu = function() {
		console.log('showing menu');
		
		// var rawtext = rangy.innerText(gene_list);
		// var savedsel = rangy.getSelection().saveCharacterRanges(gene_list);
		// var firsthalf = rawtext.slice(0, savedsel[0].characterRange.start);
		// var second half = rawtext.slice(savedsel[0].characterRange.start);
		
		var offset = $TA.caret('offset');
		delete offset.height;
		offset.top += lineHeight;
		$ME.css(offset);
		$ME.css('display', 'inline-block');
	};
	
	var removeMenu = function() {
		console.log('removing menu');
		$ME.css('display', 'none');
	};

	
	var bindEvents = function() {
		
		// jQuery-UI menu setup
		$( function(){
		    $ME.menu({
				// bind action on selecting an item
			    select: function(event, ui) {
					if (ui.item.hasClass('oql-menu-close')){
						removeMenu();
					}
				    else if (ui.item.attr('data-val') !== '') {
					    removeMenu();
					    insert(' ' + ui.item.attr('data-val'));
						GeneSymbolValidator.validateGenes();
					    showMenu();
				    }
			    }
		    });
		});
		
		// Show menu on colon
		$TA.on('keypress', function(event){
			if($('#oql_menu_checkbox').prop('checked') === false){
				return;
			}
			
			if (event.which === CHAR.COLON){
				console.log('colon press');
				showMenu();
			} else if (event.which === CHAR.SPACE && inState){
				console.log('space press instate');
				showMenu();
			}
		});
		
		// Remove menu on backspace/escape
		$TA.on('keyup', function(event){
			// when menu is displayed
			if ($ME.css('display') !== 'none') {
				switch(event.which){
					case CHAR.BACKSPACE:
						inState = true;
						removeMenu();
						break;
					case CHAR.ESCAPE:
						inState = false;
						removeMenu();
						break;
					case CHAR.SPACE:
					case CHAR.TAB:
					case CHAR.UP:
					case CHAR.DOWN:
					case CHAR.LEFT:
					case CHAR.RIGHT:
						removeMenu();
						showMenu();
						break;
					case CHAR.ENTER:
						inState = false;
						removeMenu();
						break;
					// default:
					// 	break;
				}
			}
			
			// when menu is hidden
			else {
				switch(event.which) {
					case CHAR.COLON:
						showMenu();
						break;
					case CHAR.TAB:
					case CHAR.SPACE:
						if (inState){
							showMenu();
						}
						break;
					case CHAR.UP:
					case CHAR.DOWN:
					case CHAR.LEFT:
					case CHAR.RIGHT:
					case CHAR.ENTER:
						inState = false;
						break;
				}
			}
		});
		
		// Remove menu when textarea out of focus
		$TA.focusout(function() {
			removeMenu();
			inState = false;
		});
		
		// Remove/Disable menu when disabled is checked
		$('#oql_menu_checkbox').change(function() {
			removeMenu();
			inState = false;
		});
		
	};
	
	// OQL Menu Model
	var generateList = function() {
		// 1st level of menu
		var items = [
			{val:'', name: 'CNA', desc:'Copy Number Alterations'},
			{val:'', name: 'MUT', desc:'Mutations'},
			{val:'', name: 'EXP', desc:'mRNA Expression'},
			{val:'', name: 'PROT', desc:'Protein/phosphoprotein level (RPPA)'}
		];

		// 2nd level CNA
		items[0].items = [
			{val:'CNA', name: 'Default', desc:'AMP and HOMDEL'},
			{val:'AMP', name: 'AMP', desc:'Amplified'},
			{val:'HOMDEL', name: 'HOMDEL', desc:'Deep Deletion'},
			{val:'GAIN', name: 'GAIN', desc:'Gained'},
			{val:'HETLOSS', name: 'HETLOSS', desc:'Shallow Deletion'}
		];

		// 2nd level MUT
		items[1].items = [
			{val:'MUT', name: 'Default', desc:'All somatic nonsyn. mutations'},
			{val:'', name: 'Type', desc:''}
		];

		// 3rd level MUT>Type
		items[1].items[1].items = [
			{val:'MUT = MISSENSE', name: 'MISSENSE', desc:''},
			{val:'MUT = NONSENSE', name: 'NONSENSE', desc:''},
			{val:'MUT = NONSTART', name: 'NONSTART', desc:''},
			{val:'MUT = NONSTOP', name: 'NONSTOP', desc:''},
			{val:'MUT = FRAMESHIFT', name: 'FRAMESHIFT', desc:''},
			{val:'MUT = INFRAME', name: 'INFRAME', desc:''},
			{val:'MUT = SPLIC', name: 'SPLIC', desc:''},
			{val:'MUT = TRUNC', name: 'TRUNC', desc:''}
		];

		// 2nd level EXP
		items[2].items = [
			{val:'EXP', name: 'Default', desc:'At least 2 SD from mean'},
			{val:'EXP < -x', name: '< -x', desc:'Less than x SD below mean'},
			{val:'EXP <= -x', name: '<= -x', desc:'Less than or equal to x SD below mean'},
			{val:'EXP > x', name: '> x', desc:'More than x SD above mean'},
			{val:'EXP >= x', name: '>= x', desc:'More than or equal to x SD above mean'}
		];

		// 2nd level PROT
		items[3].items = [
			{val:'PROT', name: 'Default', desc:'At least 2 SD from mean'},
			{val:'PROT < -x', name: '< -x', desc:'Less than x SD below mean'},
			{val:'PROT <= -x', name: '<= -x', desc:'Less than or equal to x SD below mean'},
			{val:'PROT > x', name: '> x', desc:'More than x SD above mean'},
			{val:'PROT >= x', name: '>= x', desc:'More than or equal to x SD above mean'}
		];

		// Handlerbars.js
		var main = Handlebars.compile($('#oql-placeholder-template').html());
		Handlebars.registerPartial('oql-menu-template', $('#oql-menu-template').html());
		$('#oql-menu-placeholder').html(main({items: items}));
	};
	
	return {
		initialize: initialize
	};
})(jQuery);
