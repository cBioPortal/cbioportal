/*
Copyright (C) 2015 Kevin Perard V0.1.3

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

(function(root, factory) {
  // CommonJS support
  if (typeof exports === 'object') {
    module.exports = factory();
  }
  // AMD
  else if (typeof define === 'function' && define.amd) {
    define(['jquery'], factory);
  }
  // Browser globals
  else {
    factory(root.jQuery);
  }
}(this, function($) {
  'use strict';

  var defer = function defer(fn) {
    if (window.requestAnimationFrame) {
      window.requestAnimationFrame(fn);
    }
    else {
      setTimeout(fn, 0);
    }
  };

  var wrapMaxItems = function wrapMaxItems(content, limit, total) {
    return ('<div class="alert maxitems">' + content + '</div>')
      .replace('{limit}', limit)
      .replace('{total}', total);
  };

  // **********************************
  // Templates
  // **********************************
  var template = '\
    <button class="dropdown-checkbox-toggle" data-toggle="dropdown" href="#">Dropdown trigger </button>\
    <div class="dropdown-checkbox-content">\
      <div class="dropdown-checkbox-header">\
        <input class="checkbox-all" type="checkbox"><input type="text" placeholder="Search" class="search"/>\
      </div>\
      <ul class="dropdown-checkbox-menu"></ul>\
    </div>';

  var templateOption = '<li><div class="layout"><input type="checkbox"/><label></label></div></li>';
  var templateNoResult = '<li><div class="layout"><label>No results.</label></div></li>';
  var templateNbSelected = ' <span class="dropdown-checkbox-nbselected"></span>';
  var templateMaxResults = 'Showing {limit} of {total} items<br>Use search to filter your results.';

  // **********************************
  // Constructor
  // **********************************
  var DropdownCheckbox = function(element, options) {
    $(element).html(template);
    $(element).addClass('dropdown-checkbox dropdown');

    this.$element = $(element).find('.dropdown-checkbox-toggle');
    this.$parent = $(element);
    this.$list = this.$parent.find('ul');

    this.data = [];
    this.hasChanges = false;
    this.showNbSelected = false;
    this.maxItems = false;
    this.alternate = false;

    // Set options if exist
    if (typeof options === 'object') {
      this.$element.text(options.title);
      this.$element.addClass(options.btnClass);
      this.autosearch = options.autosearch;
      this.data = options.data || [];
      this._sort = options.sort || this._sort;
      this.sortOptions = options.sortOptions;
      this.hideHeader = options.hideHeader || options.hideHeader === undefined ? true : false;
      this.templateButton = options.templateButton;
      this.showNbSelected = options.showNbSelected || false;
      this.maxItems = options.maxItems || false;
      this._query = options.query || this._query;
      this._queryMethod = options.httpMethod || 'GET';
      this._queryParse = options.queryParse || this._queryParse;
      this._queryError = options.queryError || function() {};
      this._queryUrl = options.queryUrl;
      this.templateMaxResults = options.templateMaxResults || templateMaxResults;
      this.alternate = !!options.alternate;
    }

    if (this.showNbSelected) this.$element.append(templateNbSelected);

    if (this.templateButton) {
      this.$element.remove();
      this.$parent.prepend(this.templateButton);
      this.$element = this.$parent.find('.dropdown-checkbox-toggle');
    }

    // Add toggle for dropdown
    this.$element.attr('data-toggle', 'dropdown');

    // Hide searchbox if needs
    if (this.hideHeader) this.$parent.find('.dropdown-checkbox-header').remove();

    // Prevent clicks on content
    this.$parent.find('.dropdown-checkbox-content').on('click.dropdown-checkbox.data-api', function(e) {
      e.stopPropagation();
    });

    // Open panel when the link is clicked
    this.$element.on('click.dropdown-checkbox.data-api', $.proxy(function() {
      // Remember current state
      var isOpened = this.$parent.hasClass('open');

      // Close all dropdown (bootstrap include)
      $('.dropdown').removeClass('open');

      // Reset last state
      if (isOpened) this.$parent.addClass('open');

      // Switch to next state
      this.$parent.toggleClass('open');

      // Notify changes on close
      if (this.hasChanges) this.$parent.trigger('change:dropdown-checkbox');

      this.hasChanges = false;
      return false;
    }, this));

    // Check or uncheck all checkbox
    this.$parent.find('.checkbox-all').on('change.dropdown-checkbox.data-api', $.proxy(function(event) {
      this.onChangeCheckboxAll(event);
      this._showNbSelected();
    }, this));

    // Events on document
    // - Close panel when click out
    // - Catch keyup events in search box
    // - Catch click on checkbox
    $(document).on('click.dropdown-checkbox.data-api', $.proxy(function() {
      this.$parent.removeClass('open');

      // Notify changes on close
      if (this.hasChanges) this.$parent.trigger('change:dropdown-checkbox');
      this.hasChanges = false;
    }, this));

    this.$parent.find('.dropdown-checkbox-header').on('keyup.dropdown-checkbox.data-api', $.proxy(DropdownCheckbox.prototype.onKeyup, this));
    this.$parent.find('ul').delegate('li input[type=checkbox]', 'change.dropdown-checkbox.data-api', $.proxy(function(event) {
      this.onChangeCheckbox(event);
      this._showNbSelected();
    }, this));

    this._reset(this.data);
    this._showNbSelected();
    this._refreshCheckboxAll();
  };

  DropdownCheckbox.prototype = {
    constructor: DropdownCheckbox,

    // ----------------------------------
    // Methods to override
    // ----------------------------------
    _sort: function(elements) {
      return elements;
    },

    _query: function(type, url, success, error) {
      return $.ajax({
        type: type,
        url: url + '?q=' + this.word,
        dataType: 'json',
        cache: false,
        contentType: 'application/json',
        success: $.proxy(success, this),
        error: error
      });
    },

    _querySuccess: function(data) {
      var results = this._queryParse(data);
      if (results.length  > 0) return this._reset(results);
      return this.$list.html(templateNoResult);
    },

    _queryParse: function(data) {
      return data;
    },

    // ----------------------------------
    // Internal methods
    // ----------------------------------

    /**
     * Take an array of ids and remove them from the data.
     * @param  {Array<Number>} ids [description]
     * @chained
     */
    _removeElements: function(ids) {
      this._isValidArray(ids);
      var tmp = [],
        toAdd = true;
      for (var i = 0; i < this.data.length; i++) {
        for (var j = 0; j < ids.length; j++) {
          if (ids[j] === parseInt(this.data[i].id, 10)) toAdd = false;
        }
        if (toAdd) tmp.push(this.data[i]);
        toAdd = true;
      }
      this.data = tmp;
      return this;
    },

    /**
     * Returns a list of checked or unchecked data items
     * @param  {Boolean} isChecked True to returned checked items, false for unchecked
     * @param  {Boolean} isAll     True to return all items.
     * @return {Array<Object>}
     */
    _getCheckbox: function(isChecked, isAll) {
      var results = [];

      for (var i = 0; i < this.data.length; i++) {
        if (isChecked === this.data[i].isChecked || isAll) {
          results.push(this.data[i]);
        }
      }
      return results;
    },

    /**
     * Validates that arr is an array, or throws.
     * @param  {Any}  arr Hopefully an array
     */
    _isValidArray: function(arr) {
      if (!$.isArray(arr)) throw '[DropdownCheckbox] Requires array.';
    },

    /**
     * Case-insensitive search of data with a label attribute matching word
     * @param  {String} word  The word to look for
     * @param  {Array<Object>} data  The array of data items
     * @return {Array<Object>} The data items matching the search
     */
    _findMatch: function(word, data) {
      var results = [];
      for (var i = 0; i < data.length; i++) {
        if (data[i].label.toLowerCase().search(word.toLowerCase()) !== -1) {
          results.push(data[i]);
        }
      }
      return results;
    },

    _findById: function(id) {
      for (var i = 0; i < this.data.length; i++) {
        if (id === this.data[i].id) {
          return this.data[i];
        }
      }
      return null;
    },

    /**
     * Sets the "isChecked" flag of a data item that we find by id
     * @param {Boolean} isChecked True or false, to check or uncheck
     * @param {Number}  id Data item id
     */
    _setCheckbox: function(isChecked, id) {
      var item = this._findById(id);
      if (item) {
          item.isChecked = isChecked;
      }
    },

    /**
     * Given data item, returns true if the item has an attribute isChecked set to truthy.
     * @param  {Object}  item Data item
     * @return {Boolean}
     */
    _isDataItemChecked: function(item) {
      return !!item.isChecked;
    },

    /**
     * Returns true when some data items are checked
     * @return {Boolean}
     */
    _anyChecked: function() {
      return this.data.some(this._isDataItemChecked);
    },

    /**
     * Returns true when all data items are checked
     * @return {Boolean}
     */
    _allChecked: function() {
      return this.data.every(this._isDataItemChecked);
    },

    /**
     * Guess what this does
     * @return {Boolean}
     */
    _noneChecked: function() {
      return !this._anyChecked();
    },

    /**
     * Returns how many items are checked in the data
     * @return {Number}
     */
    _checkedLength: function() {
      return this.data.filter(this._isDataItemChecked).length;
    },

    /**
     * Refreshes the state of the "Check all" checkbox, based on the data array
     */
    _refreshCheckboxAll: function() {
      var state = this._anyChecked();

      if (this.alternate) {
        // If all the items are checked, or none are checked, show the checkbox
        if (this._allChecked() || this._noneChecked()) {
          state = true;
        }
        else {
          // Otherwise, don't show the checkbox if any data is checked.
          state = !this._anyChecked();
        }
      }

      this.$element.parents('.dropdown-checkbox').find('.checkbox-all').prop('checked', state);
    },

    /**
     * Removes the search criteria and re-renders the widget using the current data
     * @chained
     */
    _resetSearch: function() {
      this.$parent.find('.search').val('');
      this._reset(this.data);
      return this;
    },

    /**
     * Creates a new list item element representing one data item.
     * @param  {Object} item Data item
     * @return {Element} The new list item element
     */
    _createListItem: function(item) {
      var id = item.id,
          label = item.label,
          isChecked = item.isChecked,
          uuid = new Date().getTime() * Math.random(),
          allChecked = this._allChecked();

      var node = this.listItemPrototype.cloneNode(true);
      var container = node.firstChild;

      $(node).data('id', id);
      container.firstChild.id = uuid;
      container.firstChild.checked = this.alternate && allChecked ? false : isChecked;
      container.lastChild.textContent = label;
      container.lastChild.setAttribute('for', uuid);
      return node;
    },

    /**
     * Adds a single list item to the dropdown, using item. Item is presumed
     * to be in the data array already.
     * @param  {Object} item Data item to add. Should be in this.data already.
     * @chained
     */
    _appendOne: function(item) {
      this.$list.append(this._createListItem(item));
      return this;
    },


    /**
     * Refreshes the state of the "check all", and the state of all the checkboxes
     * @return {[type]} [description]
     */
    _refresh: function() {

    },

    /**
     * Appends one or many list elements to the drop down list for the given data items.
     * @param  {Array<Object>, Object} data Data element(s) to add
     * @chained
     */
    _append: function(data) {
      // Create a list element we can clone
      if (!this.listItemPrototype) this.listItemPrototype = $(templateOption)[0];

      if (!$.isArray(data)) data = [data];

      var len = this.maxItems ? Math.min(this.maxItems, data.length) : data.length;
      var remainder = data.length - this.maxItems;
      var maxItems = this.maxItems;
      var batchsize = 100;
      var templateMaxResults = this.templateMaxResults;
      var createListItem = this._createListItem.bind(this);
      var $list = this.$list;
      var i;
      var $container = this.$parent.find('.dropdown-checkbox-content');

      data = this._sort(data, this.sortOptions);

      (function appendBatch(index) {
        var fragment = document.createDocumentFragment();
        for (i = index; i < Math.min(index + batchsize, len); i++) {
          fragment.appendChild(createListItem(data[i]));
        }
        $list[0].appendChild(fragment);
        if (i < len) {
          defer(appendBatch.bind(null, i));
        }
        else {
          if (remainder > 0 && maxItems) {
            $container.append(wrapMaxItems(templateMaxResults, maxItems, data.length));
          }
        }
      })(0);

      this._showNbSelected();
      return this;
    },

    /**
     * Resets and re-renders the widget using the given data array.
     * @param  {Array<Object>} items Data items
     * @chained
     */
    _reset: function(items) {
      // In bizarro world mode, if not of the items are checked, they all are
      if (this.alternate && this._noneChecked()) {
        this.data.forEach(function(item) {
          item.isChecked = true;
        });
      }

      this._isValidArray(items);
      this.$parent.find('.maxitems').remove();
      this.$list.empty();
      this._append(this._sort(items));
      this._refreshCheckboxAll();
      return this;
    },

    /**
     * Updates the "number of selected items" element
     * @chained
     */
    _showNbSelected: function() {
      if (!this.showNbSelected) return;

      this.$element.find('.dropdown-checkbox-nbselected').html('(' + this._checkedLength() + ')');
      return this;
    },

    // ----------------------------------
    // Event methods
    // ----------------------------------

    /**
     * Handles keyUp events triggered by the search box
     * @param  {Event} event
     */
    onKeyup: function(event) {
      var keyCode = event.keyCode,
        word = this.word = $(event.target).val();

      if (word.length < 1 && keyCode === 8) {
        this.$parent.find('.checkbox-all').show();
        return this._reset(this.data);
      }

      if (keyCode === 27) {
        this.$parent.find('.checkbox-all').show();
        return this._resetSearch();
      }

      // In alternate bizarro world, we have to hide the checkbox
      if (this.alternate) {
        this.$parent.find('.checkbox-all').hide();
      }

      if (this.autosearch || keyCode === 13) {
        if (this._queryUrl) {
          this._query(this._queryMethod,
                      this._queryUrl,
                      this._querySuccess,
                      this._queryError);
        } else {
          var results = this._findMatch(word, this.data);
          if (results.length  > 0) return this._reset(results);
          return this.$list.html(templateNoResult);
        }
      }
    },

    /**
     * Handles a click on "select all" checkbox.
     * @param  {Event} event
     */
    onChangeCheckboxAll: function(event) {
      var isChecked = $(event.target).is(':checked');

      // In alternate mode, when the "check all" check box is checked, none
      // of the items should show as selected, but, in the data, they should
      // all be selected.

      var alternate = this.alternate;
      var $elements = this.$parent.find('ul li');
      var self = this;

      var checkboxState = isChecked;
      var modelState = isChecked;

      if (alternate) {
        if (isChecked) {
          checkboxState = false;
          modelState = true;
        }
        else {
          checkboxState = false;
          modelState = false;
        }
      }

      $elements.each(function() {
        $(this).find('input[type=checkbox]').prop('checked', checkboxState);
        self._setCheckbox(modelState, $(this).data('id'));
      });

      // Make sure we select all the items, not just the visible ones.
      if (alternate && isChecked) {
        this.data.forEach(function(item){
          item.isChecked = true;
        });
      }

      this.$parent.trigger('checked:all', isChecked);
      isChecked ? this.$parent.trigger('check:all') : this.$parent.trigger('uncheck:all');

      // In alternate mode, select the first one if you've created yourself a nice empty list.
      if (alternate && modelState === false) {
        // Reset all the data to unchecked
        this.data.forEach(function(item){
          item.isChecked = false;
        });
        // Select the first one.
        $elements.first().each(function(){
          $(this).find('input[type=checkbox]').prop('checked', true);
          self._setCheckbox(true, $(this).data('id'));
        });
      }

      // Notify changes
      this.hasChanges = true;
    },

    /**
     * Handles a click on a single checkbox.
     * @param {Event} event
     */
    onChangeCheckbox: function(event) {
      var checked = $(event.target).prop('checked');
      var id = $(event.target).parent().parent().data('id');

      // If we're in alternate mode, and all the items are checked, we want to
      // uncheck them and single check this one.
      if (this.alternate && this._allChecked() && checked) {
        this.data.forEach(function(item) {
          item.isChecked = false;
        });

        this._setCheckbox(true, id);
      }
      // If you're in shit mode and you've unchecked everything except one
      // that you're about to uncheck, you will not believe what happens next!
      else if (this.alternate && this._checkedLength() === 1 && !checked) {
        this.data.forEach(function(item) {
          item.isChecked = true;
        });

        this._setCheckbox(true, id);
      }
      else {
        this._setCheckbox(checked, id);
      }

      this._refreshCheckboxAll();
      this.$parent.trigger('checked', checked);
      checked ? this.$parent.trigger('check:checkbox') : this.$parent.trigger('uncheck:checkbox');

      // Notify changes
      this.hasChanges = true;
    },

    // ----------------------------------
    // External methods
    // ----------------------------------
    checked: function() {
      return this._getCheckbox(true);
    },

    unchecked: function() {
      return this._getCheckbox(false);
    },

    items: function() {
      return this._getCheckbox(undefined, true);
    },

    append: function(elements) {
      if (!$.isArray(elements)) {
        this.data.push(elements);
      } else {
        for (var i = 0; i < elements.length; i++)
          this.data.push(elements[i]);
      }

      elements = this._sort(elements);

      this._append(elements);

      // Notify changes
      this.hasChanges = true;
    },

    remove: function(ids) {
      if (!$.isArray(ids)) ids = [ids];
      this._isValidArray(ids);
      this._removeElements(ids);
      this._reset(this.data);

      // Notify changes
      this.hasChanges = true;
    },

    reset: function(elements) {
      if (!$.isArray(elements)) {
        this.data = [elements];
      } else {
        this.data = elements;
      }

      this._reset(elements);

      // Notify changes
      this.hasChanges = true;
    }
  };


  $.fn.dropdownCheckbox = function(option, more) {
    var $this = $(this),
      data = $this.data('dropdownCheckbox'),
      options = typeof option == 'object' && option;

    if (!data) $this.data('dropdownCheckbox', (data = new DropdownCheckbox(this, options)));
    if (typeof option == 'string') return data[option](more);
    return this;
  };

  $.fn.dropdownCheckbox.Constructor = DropdownCheckbox;

}));
