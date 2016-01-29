(function (root, factory) {

  if (typeof define === 'function' && define.amd) {
    define(['react', 'jquery'], factory);
  } else if (typeof module === 'object' && typeof module.exports === 'object') {
    module.exports = factory(require('react'), require('jquery'));
  } else {
    root.Chosen = factory(root.React, root.jQuery);
  }

}(this, function (React, $) {

  var Chosen = React.createClass({
    displayName: 'Chosen',

    componentDidUpdate: function() {
      // chosen doesn't refresh the options by itself, babysit it
      $(this.refs.select).trigger('chosen:updated');
    },

    handleChange: function(a, b, c) {
      // force the update makes it so that we reset chosen to whatever
      // controlled value the parent dictated
      this.forceUpdate();
      this.props.onChange && this.props.onChange(a, b, c);
    },

    componentDidMount: function() {
      var props = this.props;
      var select = $(this.refs.select);
      $(select)
        .chosen({
          allow_single_deselect: props.allowSingleDeselect,
          disable_search: props.disableSearch,
          disable_search_threshold: props.disableSearchThreshold,
          enable_split_word_search: props.enableSplitWordSearch,
          inherit_select_classes: props.inheritSelectClasses,
          max_selected_options: props.maxSelectedOptions,
          no_results_text: props.noResultsText,
          placeholder_text_multiple: props.placeholderTextMultiple,
          placeholder_text_single: props.placeholderTextSingle,
          search_contains: props.searchContains,
          single_backstroke_delete: props.singleBackstrokeDelete,
          width: props.width,
          display_disabled_options: props.displayDisabledOptions,
          display_selected_options: props.displaySelectedOptions
        })
        .on('chosen:maxselected', this.props.onMaxSelected)
        .change(this.handleChange);
    },

    componentWillUnmount: function() {
      $(this.refs.select).off('chosen:maxselected change');
    },

    render: function() {
      var selectProps = $.extend({}, this.props, {ref: "select"});
      return React.createElement("div", null,
        React.createElement("select", selectProps, this.props.children)
      );
    }
  });

  return Chosen;

}));
