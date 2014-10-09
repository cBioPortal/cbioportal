/*
 * DataTables 1.10 `two_button` pagination control compatibility file
 */

jQuery.extend( jQuery.fn.DataTable.ext.classes, {
	"sPagePrevEnabled": "paginate_enabled_previous",
	"sPagePrevDisabled": "paginate_disabled_previous",
	"sPageNextEnabled": "paginate_enabled_next",
	"sPageNextDisabled": "paginate_disabled_next"
} );

jQuery.extend( jQuery.fn.DataTable.ext.oJUIClasses, {
	"sPagePrevEnabled": "fg-button ui-button ui-state-default ui-corner-left",
	"sPagePrevDisabled": "fg-button ui-button ui-state-default ui-corner-left ui-state-disabled",
	"sPageNextEnabled": "fg-button ui-button ui-state-default ui-corner-right",
	"sPageNextDisabled": "fg-button ui-button ui-state-default ui-corner-right ui-state-disabled",
	"sPageJUINext": "ui-icon ui-icon-circle-arrow-e",
	"sPageJUIPrev": "ui-icon ui-icon-circle-arrow-w"
} );

jQuery.extend( jQuery.fn.DataTable.ext.pager, {
	"two_button": {
		"fnInit": function ( oSettings, nPaging, fnCallbackDraw )
		{
			var oLang = oSettings.oLanguage.oPaginate;
			var oClasses = oSettings.oClasses;
			var fnClickHandler = function ( e ) {
				if ( oSettings.oApi._fnPageChange( oSettings, e.data.action ) )
				{
					fnCallbackDraw( oSettings );
				}
			};

			var sAppend = (!oSettings.bJUI) ?
				'<a class="'+oSettings.oClasses.sPagePrevDisabled+'" tabindex="'+oSettings.iTabIndex+'" role="button">'+oLang.sPrevious+'</a>'+
				'<a class="'+oSettings.oClasses.sPageNextDisabled+'" tabindex="'+oSettings.iTabIndex+'" role="button">'+oLang.sNext+'</a>'
				:
				'<a class="'+oSettings.oClasses.sPagePrevDisabled+'" tabindex="'+oSettings.iTabIndex+'" role="button"><span class="'+oSettings.oClasses.sPageJUIPrev+'"></span></a>'+
				'<a class="'+oSettings.oClasses.sPageNextDisabled+'" tabindex="'+oSettings.iTabIndex+'" role="button"><span class="'+oSettings.oClasses.sPageJUINext+'"></span></a>';
			$(nPaging).append( sAppend );

			var els = $('a', nPaging);
			var nPrevious = els[0],
				nNext = els[1];

			oSettings.oApi._fnBindAction( nPrevious, {action: "previous"}, fnClickHandler );
			oSettings.oApi._fnBindAction( nNext,     {action: "next"},     fnClickHandler );

			/* ID the first elements only */
			if ( !oSettings.aanFeatures.p )
			{
				nPaging.id = oSettings.sTableId+'_paginate';
				nPrevious.id = oSettings.sTableId+'_previous';
				nNext.id = oSettings.sTableId+'_next';

				nPrevious.setAttribute('aria-controls', oSettings.sTableId);
				nNext.setAttribute('aria-controls', oSettings.sTableId);
			}
		},

		"fnUpdate": function ( oSettings, fnCallbackDraw )
		{
			if ( !oSettings.aanFeatures.p )
			{
				return;
			}

			var oClasses = oSettings.oClasses;
			var an = oSettings.aanFeatures.p;
			var nNode;

			/* Loop over each instance of the pager */
			for ( var i=0, iLen=an.length ; i<iLen ; i++ )
			{
				nNode = an[i].firstChild;
				if ( nNode )
				{
					/* Previous page */
					nNode.className = ( oSettings._iDisplayStart === 0 ) ?
						oClasses.sPagePrevDisabled : oClasses.sPagePrevEnabled;

					/* Next page */
					nNode = nNode.nextSibling;
					nNode.className = ( oSettings.fnDisplayEnd() == oSettings.fnRecordsDisplay() ) ?
						oClasses.sPageNextDisabled : oClasses.sPageNextEnabled;
				}
			}
		}
	}
} );

jQuery.fn.dataTable.defaults.pagingType = 'two_button';

