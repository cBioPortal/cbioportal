/*
 * UI Tabs Paging extension - v1.1 (Legacy - jQuery 1.3 to 1.7, jQuery UI 1.7 or 1.8)
 * 
 * Copyright (c) 2012, http://seyfertdesign.com/jquery/ui-tabs-paging.html
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Depends:
 *   jquery.ui.core.js
 *   jquery.ui.widget.js (when using jQuery UI 1.8)
 *   jquery.ui.tabs.js
 */

(function($) {

//  overridden ui.tabs functions
var uiTabsFuncs = { 
	select: $.ui.tabs.prototype.select, 
	add: $.ui.tabs.prototype.add, 
	remove: $.ui.tabs.prototype.remove
};
	
$.extend($.ui.tabs.prototype, {
	paging: function(options) {
		var opts = {
			tabsPerPage: 0,       // Max number of tabs to display at one time.  0 automatically sizing.
			nextButton: '&#187;', // Text displayed for next button.
			prevButton: '&#171;', // Text displayed for previous button.
			follow: false,        // When clicking next button, automatically select first.  When clicking previous button automatically select last.
			cycle: false,         // When at end of list, next button returns to first page.  When at beginning of list previous button goes to end of list.
			selectOnAdd: false,   // When new tab is added, select it automatically
			followOnSelect: false // When tab is selected with javascript, automatically go move to that tab group.
		};
		
		opts = $.extend(opts, options);

		var self = this, initialized = false, currentPage, 
			buttonWidth, containerWidth, allTabsWidth, tabWidths, 
			maxPageWidth, pages, resizeTimer = null, 
			windowHeight, windowWidth;
		
		// initialize paging
		function init() {
			destroy();
			
			windowHeight = $(window).height();
			windowWidth = $(window).width();
			
			allTabsWidth = 0, currentPage = 0, maxPageWidth = 0, buttonWidth = 0,
				pages = new Array(), tabWidths = new Array(), selectedTabWidths = new Array();
			
			containerWidth = self.element.width();
			
			// loops through LIs, get width of each tab when selected and unselected.
			var maxDiff = 0;  // the max difference between a selected and unselected tab
			self.lis.each(function(i) {			
				if (i == self.options.selected) {
					selectedTabWidths[i] = $(this).outerWidth({ margin: true });
					tabWidths[i] = self.lis.eq(i).removeClass('ui-tabs-selected').outerWidth({ margin: true });
					self.lis.eq(i).addClass('ui-tabs-selected');
					maxDiff = Math.min(maxDiff, Math.abs(selectedTabWidths[i] - tabWidths[i]));
					allTabsWidth += tabWidths[i];
				} else {
					tabWidths[i] = $(this).outerWidth({ margin: true });
					selectedTabWidths[i] = self.lis.eq(i).addClass('ui-tabs-selected').outerWidth({ margin: true });
					self.lis.eq(i).removeClass('ui-tabs-selected');
					maxDiff = Math.max(maxDiff, Math.abs(selectedTabWidths[i] - tabWidths[i]));
					allTabsWidth += tabWidths[i];
				}
			});
			
			// fix padding issues with buttons
			// TODO determine a better way to handle this
			allTabsWidth += maxDiff + ($.browser.msie?4:0) + 9;  

			// if the width of all tables is greater than the container's width, calculate the pages
			if (allTabsWidth > containerWidth) {
				// create next button			
				li = $('<li></li>')
					.addClass('ui-state-default ui-tabs-paging-next')
					.append($('<a href="#"></a>')
							.click(function() { page('next'); return false; })
							.html(opts.nextButton));
				
				self.lis.eq(self.length()-1).after(li);
				buttonWidth = li.outerWidth({ margin: true });
				
				// create prev button
				li = $('<li></li>')
					.addClass('ui-state-default ui-tabs-paging-prev')
					.append($('<a href="#"></a>')
							.click(function() { page('prev'); return false; })
							.html(opts.prevButton));
				self.lis.eq(0).before(li);
				buttonWidth += li.outerWidth({ margin: true });
				
				// TODO determine fix for padding issues to next button
				buttonWidth += 19; 
								
				var pageIndex = 0, pageWidth = 0, maxTabPadding = 0;
				
				// start calculating pageWidths
				for (var i = 0; i < tabWidths.length; i++) {
					// if first tab of page or selected tab's padding larger than the current max, set the maxTabPadding
					if (pageWidth == 0 || selectedTabWidths[i] - tabWidths[i] > maxTabPadding)
						maxTabPadding = (selectedTabWidths[i] - tabWidths[i]);
					
					// if first tab of page, initialize pages variable for page 
					if (pages[pageIndex] == null) {
						pages[pageIndex] = { start: i };
					
					} else if ((i > 0 && (i % opts.tabsPerPage) == 0) || (tabWidths[i] + pageWidth + buttonWidth + 12) > containerWidth) {
						if ((pageWidth + maxTabPadding) > maxPageWidth)	
							maxPageWidth = (pageWidth + maxTabPadding);
						pageIndex++;
						pages[pageIndex] = { start: i };			
						pageWidth = 0;
					}
					pages[pageIndex].end = i+1;
					pageWidth += tabWidths[i];
					if (i == self.options.selected) currentPage = pageIndex;
				}
				if ((pageWidth + maxTabPadding) > maxPageWidth)	
					maxPageWidth = (pageWidth + maxTabPadding);				

			    // hide all tabs then show tabs for current page
				self.lis.hide().slice(pages[currentPage].start, pages[currentPage].end).show();
				if (currentPage == (pages.length - 1) && !opts.cycle) 
					disableButton('next');			
				if (currentPage == 0 && !opts.cycle) 
					disableButton('prev');
				
				// calculate the right padding for the next button
				buttonPadding = containerWidth - maxPageWidth - buttonWidth;
				if (buttonPadding > 0) 
					$('.ui-tabs-paging-next', self.element).css({ paddingRight: buttonPadding + 'px' });
			} else {
				destroy();
			}
			
			$(window).bind('resize', handleResize);
			
			initialized = true;
		}
		
		// handles paging forward and backward
		function page(direction) {
			currentPage = currentPage + (direction == 'prev'?-1:1);
			
			if ((direction == 'prev' && currentPage < 0 && opts.cycle) ||
				(direction == 'next' && currentPage >= pages.length && !opts.cycle))
				currentPage = pages.length - 1;
			else if ((direction == 'prev' && currentPage < 0) || 
					 (direction == 'next' && currentPage >= pages.length && opts.cycle))
				currentPage = 0;
			
			var start = pages[currentPage].start;
			var end = pages[currentPage].end;
			self.lis.hide().slice(start, end).show();
			
			if (direction == 'prev') {
				enableButton('next');
				if (opts.follow && (self.options.selected < start || self.options.selected > (end-1))) self.select(end-1);
				if (!opts.cycle && start <= 0) disableButton('prev');
			} else {
				enableButton('prev');
				if (opts.follow && (self.options.selected < start || self.options.selected > (end-1))) self.select(start);
				if (!opts.cycle && end >= self.length()) disableButton('next');
			}
		}
		
		// change styling of next/prev buttons when disabled
		function disableButton(direction) {
			$('.ui-tabs-paging-'+direction, self.element).addClass('ui-tabs-paging-disabled');
		}
		
		function enableButton(direction) {
			$('.ui-tabs-paging-'+direction, self.element).removeClass('ui-tabs-paging-disabled');
		}
		
		// special function defined to handle IE resize issues
		function handleResize() {
			if (resizeTimer) clearTimeout(resizeTimer);
			
			if (windowHeight != $(window).height() || windowWidth != $(window).width()) 
			{
				resizeTimer = setTimeout(init, 100);
			}
		}
		
		// remove all paging related changes and events
		function destroy() {
			// remove buttons
			$('.ui-tabs-paging-next', self.element).remove();
			$('.ui-tabs-paging-prev', self.element).remove();
			
			// show all tabs
			self.lis.show();
			
			initialized = false;
			
			$(window).unbind('resize', handleResize);
		}
		
		
		
		// ------------- OVERRIDDEN PUBLIC FUNCTIONS -------------
		// temporarily remove paging buttons before adding a tab
		self.add = function(url, label, index) {
			if (initialized)
			{
				destroy();

				uiTabsFuncs.add.apply(this, [url, label, index]);

				if (opts.selectOnAdd) {
					if (index == undefined) index = this.lis.length-1;
					this.select(index);
				}
				// re-initialize paging buttons
				init();

				return this;
			}
			
			return uiTabsFuncs.add.apply(this, [url, label, index]);
		}
		
		// temporarily remove paging buttons before removing a tab
		self.remove = function(index) {
			if (initialized)
			{
				destroy();
				uiTabsFuncs.remove.apply(this, [index]);
				init();

				return this;
			}
			
			return uiTabsFuncs.remove.apply(this, [index]);
		}
		
		// if "followOnSelect" is true, then move page when selection changes
		self.select = function(index) {
			uiTabsFuncs.select.apply(this, [index]);

			// if paging is not initialized or it is not configured to 
			// change pages when a new tab is selected, then do nothing
			if (!initialized || !opts.followOnSelect)
				return this;

			// find the new page based on index of the tab selected
			for (var i in pages) {
				var start = pages[i].start;
				var end = pages[i].end;
				if (index >= start && index < end) {
					// if the the tab selected is not within the currentPage of tabs, then change pages
					if (i != currentPage) {
						this.lis.hide().slice(start, end).show();

						currentPage = parseInt(i);
						if (currentPage == 0) {
							enableButton('next');
							if (!opts.cycle && start <= 0) disableButton('prev');
						} else {
							enableButton('prev');
							if (!opts.cycle && end >= this.length()) disableButton('next');
						}
					}
					break;
				}
			}
			
			return this;
		}


		// ------------- PUBLIC FUNCTIONS -------------
		$.extend($.ui.tabs.prototype, {
			// public function for removing paging
			pagingDestroy: function() {
				destroy();
				return this;
			},

			// public function to handle resizes that are not on the window
			pagingResize: function() {
				init();
				return this;
			}
		});
		
		// initialize on startup!
		init();
	}
});


})(jQuery);

