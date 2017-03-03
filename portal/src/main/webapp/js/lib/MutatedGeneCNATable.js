'use strict';

window.EnhancedFixedDataTableSpecial = (function() {
// Data button component
  var FileGrabber = React.createClass({displayName: "FileGrabber",
    // Saves table content to a text file
    saveFile: function() {
      var formatData = this.props.content();

      var blob = new Blob([formatData], {type: 'text/plain'});
      var fileName = this.props.downloadFileName ? this.props.downloadFileName : "data.txt";

      var downloadLink = document.createElement("a");
      downloadLink.download = fileName;
      downloadLink.innerHTML = "Download File";
      if (window.webkitURL) {
        // Chrome allows the link to be clicked
        // without actually adding it to the DOM.
        downloadLink.href = window.webkitURL.createObjectURL(blob);
      }
      else {
        // Firefox requires the link to be added to the DOM
        // before it can be clicked.
        downloadLink.href = window.URL.createObjectURL(blob);
        downloadLink.onclick = function(event) {
          document.body.removeChild(event.target);
        };
        downloadLink.style.display = "none";
        document.body.appendChild(downloadLink);
      }

      downloadLink.click();
    },

    render: function() {
      return (
        React.createElement("button", {className: "btn btn-default", onClick: this.saveFile},
          "DATA")
      );
    }
  });

// Copy button component
  var ClipboardGrabber = React.createClass({displayName: "ClipboardGrabber",
    notify: function(opts) {
      // Default settings for Copied.
      var _message = 'Copied.';
      var _type = 'success';

      if (_.isObject(opts)) {
        if (!_.isUndefined(opts.message)) {
          _message = opts.message;
        }
        if (opts.type) {
          _type = opts.type;
        }
      }
      $.notify({
        message: _message
      }, {
        type: _type,
        animate: {
          enter: 'animated fadeInDown',
          exit: 'animated fadeOutUp'
        },
        delay: 1000
      });
    },

    componentDidMount: function() {
      var client = new ZeroClipboard($("#copy-button"));
      var self = this;
      client.on("ready", function(readyEvent) {
        client.on("copy", function(event) {
          event.clipboardData.setData('text/plain', self.props.content());
        });
        client.on("aftercopy", function(event) {
          self.notify();
        });
        client.on("error", function(event) {
          // Error happened, disable Copy button notify the user.
          ZeroClipboard.destroy();
          self.notify({
            message: 'Copy button is not availble at this moment.',
            type: 'danger'
          });
          self.setState({show: false});
        });
      });
    },

    getInitialState: function() {
      var _show = true;
      var _content = this.props.content();

      // The current not official limitation is 1,000,000
      // https://github.com/zeroclipboard/zeroclipboard/issues/529
      if (!_.isString(_content) || _content.length > 1000000) {
        _show = false;
      }

      return {
        show: _show,
        formatData: ''
      };
    },

    render: function() {
      return (
        React.createElement("div", null,
          this.state.show ?
            React.createElement("button", {className: "btn btn-default", id: "copy-button"},
              "COPY") : ''
        )
      );
    }
  });

// Container of FileGrabber and ClipboardGrabber
  var DataGrabber = React.createClass({displayName: "DataGrabber",
    // Prepares table content data for download or copy button
    prepareContent: function() {
      var content = [], cols = $.extend(true, [], this.props.cols), rows = this.props.rows;

      // List fixed columns first
      cols = cols.sort(function(x, y) {
        return (x.fixed === y.fixed) ? 0 : x.fixed ? -1 : 1;
      });

      _.each(cols, function(e) {
        content.push((e.displayName || 'Unknown'), '\t');
      });
      content.pop();

      _.each(rows, function(row) {
        content.push('\r\n');
        _.each(cols, function(col) {
          content.push(row[col.name], '\t');
        });
        content.pop();
      });
      return content.join('');
    },

    render: function() {
      var getData = this.props.getData;
      if (getData === "NONE") {
        return React.createElement("div", null);
      }

      var content = this.prepareContent;

      return (
        React.createElement("div", null,
          React.createElement("div", {className: "EFDT-download-btn EFDT-top-btn"},

            getData != "COPY" ? React.createElement(FileGrabber, {content: content,
                downloadFileName: this.props.downloadFileName}) :
              React.createElement("div", null)

          ),
          React.createElement("div", {className: "EFDT-download-btn EFDT-top-btn"},

            getData != "DOWNLOAD" ? React.createElement(ClipboardGrabber, {content: content}) :
              React.createElement("div", null)

          )
        )
      );
    }
  });

// Wrapper of qTip for string
// Generates qTip when string length is larger than 20
  var QtipWrapper = React.createClass({displayName: "QtipWrapper",
    render: function() {
      var label = this.props.label, qtipFlag = false;
      var shortLabel = this.props.shortLabel;
      var className = this.props.className || '';
      var field = this.props.field;
      var color = this.props.color || '';
      var tableType = this.props.tableType || '';

      if (label && shortLabel && label.toString().length > shortLabel.toString().length) {
        qtipFlag = true;
      }
      return (
        React.createElement("span", {className: className + (qtipFlag ? " hasQtip " : '') +
          ((field === 'alttype' && ['mutatedGene', 'cna'].indexOf(tableType) !== -1) ? (label === 'AMP' ? ' alt-type-red' : ' alt-type-blue') : ''),
            "data-qtip": label},

          (field === 'name' && tableType === 'pieLabel') ? (
              React.createElement("svg", {width: "15", height: "10"},
                React.createElement("g", null,
                  React.createElement("rect", {height: "10", width: "10", fill: color})
                )
              )
            ) : '',

          shortLabel
        )
      );
    }
  });

// Column show/hide component
  var ColumnHider = React.createClass({displayName: "ColumnHider",
    tableCols: [],// For the checklist

    // Updates column show/hide settings
    hideColumns: function(list) {
      var cols = this.props.cols, filters = this.props.filters;
      for (var i = 0; i < list.length; i++) {
        cols[i].show = list[i].isChecked;
        if (this.props.hideFilter) {
          filters[cols[i].name].hide = !cols[i].show;
        }
      }
      this.props.updateCols(cols, filters);
    },

    // Prepares tableCols
    componentWillMount: function() {
      var cols = this.props.cols;
      var colsL = cols.length;
      for (var i = 0; i < colsL; i++) {
        this.tableCols.push({
          id: cols[i].name,
          label: cols[i].displayName,
          isChecked: cols[i].show
        });
      }
    },

    componentDidMount: function() {
      var hideColumns = this.hideColumns;

      // Dropdown checklist
      $('#hide_column_checklist')
        .dropdownCheckbox({
          data: this.tableCols,
          autosearch: true,
          title: "Show / Hide Columns",
          hideHeader: false,
          showNbSelected: true
        })
        // Handles dropdown checklist event
        .on("change", function() {
          var list = ($("#hide_column_checklist").dropdownCheckbox("items"));
          hideColumns(list);
        });
      // add title attributes to unlabeled inputs generated by
      // .dropdownCheckbox() for accessibility, until
      // https://github.com/Nelrohd/bootstrap-dropdown-checkbox/issues/33 is
      // fixed upstream
      $('#hide_column_checklist input[type="checkbox"].checkbox-all')
        .attr('title', 'Select all');
      $('#hide_column_checklist input[type="text"].search')
        .attr('title', 'Search');
    },

    render: function() {
      return (
        React.createElement("div", {id: "hide_column_checklist", className: "EFDT-top-btn"})
      );
    }
  });

// Choose fixed columns component
  var PinColumns = React.createClass({displayName: "PinColumns",
    tableCols: [],// For the checklist

    // Updates fixed column settings
    pinColumns: function(list) {
      var cols = this.props.cols;
      for (var i = 0; i < list.length; i++) {
        cols[i].fixed = list[i].isChecked;
      }
      this.props.updateCols(cols, this.props.filters);
    },

    // Prepares tableCols
    componentWillMount: function() {
      var cols = this.props.cols;
      var colsL = cols.length;
      for (var i = 0; i < colsL; i++) {
        this.tableCols.push({
          id: cols[i].name,
          label: cols[i].displayName,
          isChecked: cols[i].fixed
        });
      }
    },

    componentDidMount: function() {
      var pinColumns = this.pinColumns;

      // Dropdown checklist
      $("#pin_column_checklist")
        .dropdownCheckbox({
          data: this.tableCols,
          autosearch: true,
          title: "Choose Fixed Columns",
          hideHeader: false,
          showNbSelected: true
        })
        // Handles dropdown checklist event
        .on("change", function() {
          var list = ($("#pin_column_checklist").dropdownCheckbox("items"));
          pinColumns(list);
        });
      // add title attributes to unlabeled inputs generated by
      // .dropdownCheckbox() for accessibility, until
      // https://github.com/Nelrohd/bootstrap-dropdown-checkbox/issues/33 is
      // fixed upstream
      $('#pin_column_checklist input[type="checkbox"].checkbox-all')
        .attr('title', 'Select all');
      $('#pin_column_checklist input[type="text"].search')
        .attr('title', 'Search');
    },

    render: function() {
      return (
        React.createElement("div", {id: "pin_column_checklist", className: "EFDT-top-btn"})
      );
    }
  });

// Column scroller component
  var ColumnScroller = React.createClass({displayName: "ColumnScroller",
    // Scrolls to user selected column
    scrollToColumn: function(e) {
      var name = e.target.value, cols = this.props.cols, index, colsL = cols.length;
      for (var i = 0; i < colsL; i++) {
        if (name === cols[i].name) {
          index = i;
          break;
        }
      }
      this.props.updateGoToColumn(index);
    },

    render: function() {
      return (
        React.createElement(Chosen, {"data-placeholder": "Column Scroller",
            onChange: this.scrollToColumn},

          this.props.cols.map(function(col) {
            return (
              React.createElement("option", {title: col.displayName, value: col.name},
                React.createElement(QtipWrapper, {label: col.displayName})
              )
            );
          })

        )
      );
    }
  });

// Filter component
  var Filter = React.createClass({displayName: "Filter",
    getInitialState: function() {
      return {key: ''};
    },
    handleChange: function(event) {
      this.setState({key: event.target.value});
      this.props.onFilterKeywordChange(event);
    },
    componentWillUpdate: function() {
      if (this.props.type === 'STRING') {
        if (!_.isUndefined(this.props.filter) && this.props.filter.key !== this.state.key && this.props.filter.key === '' && this.props.filter.reset) {
          this.state.key = '';
          this.props.filter.reset = false;
        }
      }
    },
    render: function() {
      if (this.props.type === "NUMBER" || this.props.type === "PERCENTAGE") {
        // explicitly set anchors without href for the handles, as
        // jQuery UI 1.10 otherwise adds href="#" which may confuse
        // assistive technologies
        return (
          React.createElement("div", {className: "EFDT-header-filters"},
            React.createElement("span", {id: "range-" + this.props.name}),

            React.createElement("div", {className: "rangeSlider", "data-max": this.props.max,
                "data-min": this.props.min, "data-column": this.props.name,
                "data-type": this.props.type},
              React.createElement("a", {className: "ui-slider-handle", tabIndex: "0"}),
              React.createElement("a", {className: "ui-slider-handle", tabIndex: "0"})
            )
          )
        );
      } else {
        return (
          React.createElement("div", {className: "EFDT-header-filters"},
            React.createElement("input", {className: "form-control",
              placeholder: this.props.hasOwnProperty('placeholder') ? this.props.placeholder : "Search...",
              "data-column": this.props.name,
              value: this.state.key,
              onChange: this.handleChange,
              title: "Input a keyword"})
          )
        );
      }
    }
  });

// Table prefix component
// Contains components above the main part of table
  var TablePrefix = React.createClass({displayName: "TablePrefix",
    render: function() {
      return (
        React.createElement("div", null,
          React.createElement("div", null,

            this.props.hider ?
              React.createElement("div", {className: "EFDT-show-hide"},
                React.createElement(ColumnHider, {cols: this.props.cols,
                  filters: this.props.filters,
                  hideFilter: this.props.hideFilter,
                  updateCols: this.props.updateCols})
              ) :
              "",


            this.props.fixedChoose ?
              React.createElement("div", {className: "EFDT-fixed-choose"},
                React.createElement(PinColumns, {cols: this.props.cols,
                  filters: this.props.filters,
                  updateCols: this.props.updateCols})
              ) :
              "",

            React.createElement("div", {className: "EFDT-download"},
              React.createElement(DataGrabber, {cols: this.props.cols, rows: this.props.rows,
                downloadFileName: this.props.downloadFileName,
                getData: this.props.getData})
            ),

            this.props.resultInfo ?
              React.createElement("div", {className: "EFDT-result-info"},
                React.createElement("span", {className: "EFDT-result-info-content"},
                  "Showing ", this.props.filteredRowsSize, " samples",

                  this.props.filteredRowsSize !== this.props.rowsSize ?
                    React.createElement("span", null, ' (filtered from ' + this.props.rowsSize + ') ',
                      React.createElement("span", {className: "EFDT-header-filters-reset",
                        onClick: this.props.onResetFilters}, "Reset")
                    )
                    : ''

                )
              ) :
              ""

          ),
          React.createElement("div", null
          )
        )
      );
    }
  });

// Wrapper for the header rendering
  var HeaderWrapper = React.createClass({displayName: "HeaderWrapper",
    render: function() {
      var columnData = this.props.columnData;
      var shortLabel = this.props.shortLabel;
      return (
        React.createElement("div", {className: "EFDT-header"},
          React.createElement("span", {className: "EFDT-header-sort", href: "#",
              onClick: this.props.sortNSet.bind(null, this.props.cellDataKey)},
            React.createElement(QtipWrapper, {label: columnData.displayName,
              shortLabel: shortLabel,
              className: 'EFDT-header-sort-content'}),
            columnData.sortFlag ?
              React.createElement("div", {
                className: columnData.sortDirArrow + ' EFDT-header-sort-icon'})
              : ""
          )
        )
      );
    }
  });

  var CustomizeCell = React.createClass({displayName: "CustomizeCell",
    selectRow: function(rowIndex) {
      this.props.selectRow(rowIndex);
    },
    selectGene: function(rowIndex) {
      this.props.selectGene(rowIndex);
    },
    enterPieLabel: function(data) {
      this.props.pieLabelMouseEnterFunc(data);
    },
    leavePieLabel: function(data) {
      this.props.pieLabelMouseLeaveFunc(data);
    },
    render: function() {
      var Cell = FixedDataTable.Cell;
      var rowIndex = this.props.rowIndex, data = this.props.data, field = this.props.field, filterAll = this.props.filterAll;
      var flag = (data[rowIndex][field] && filterAll.length > 0) ?
        (data[rowIndex][field].toLowerCase().indexOf(filterAll.toLowerCase()) >= 0) : false;
      var shortLabels = this.props.shortLabels;
      var tableType = this.props.tableType;
      var confirmedRowsIndex = this.props.confirmedRowsIndex;
      return (
        React.createElement(Cell, {onFocus: this.onFocus, className: 'EFDT-cell EFDT-cell-full' +
          (this.props.selectedRowIndex.indexOf(data[rowIndex].index) != -1 ? ' row-selected' : ''),
            columnKey: field},
          React.createElement("span", {style: flag ? {backgroundColor: 'yellow'} : {},
              onClick: field === 'gene' ? this.selectGene.bind(this, data[rowIndex].index) : '',
              onMouseEnter: (tableType === 'pieLabel' && _.isFunction(this.props.pieLabelMouseEnterFunc) && field === 'name') ? this.enterPieLabel.bind(this, data[rowIndex].row) : '',
              onMouseLeave: (tableType === 'pieLabel' && _.isFunction(this.props.pieLabelMouseLeaveFunc) && field === 'name') ? this.leavePieLabel.bind(this, data[rowIndex].row) : '',
              "data-qtip": field === 'gene' ? ('Click ' + data[rowIndex].row[field] + ' to ' + ( this.props.selectedGeneRowIndex.indexOf(data[rowIndex].index) === -1 ? 'add to ' : ' remove from ' ) + 'your query') : '',
              className: (field === 'gene' ? 'gene hasQtip' : '') +
              ((field === 'gene' && this.props.selectedGeneRowIndex.indexOf(data[rowIndex].index) != -1) ? ' gene-selected' : '')},
            React.createElement(QtipWrapper, {label: data[rowIndex].row[field],
              shortLabel: shortLabels[data[rowIndex].index][field],
              field: field,
              tableType: tableType,
              color: data[rowIndex].row.color})
          ),

          field === 'gene' && data[rowIndex].row.qval ?
            (tableType === 'mutatedGene' ?
              React.createElement("img", {src: "images/mutsig.png", className: "hasQtip qval-icon",
                "data-qtip": '<b>MutSig</b><br/><i>Q-value</i>: ' + data[rowIndex].row.qval,
                alt: "MutSig"}) :
              React.createElement("img", {src: "images/gistic.png", className: "hasQtip qval-icon",
                "data-qtip": '<b>Gistic</b><br/><i>Q-value</i>: ' + data[rowIndex].row.qval,
                alt: "Gistic"})) : '',


          field === 'cases' ?
            React.createElement("input", {type: "checkbox", style: {float: 'right'},
              title: 'Select ' + data[rowIndex].row[field]
              + ' sample' + (Number(data[rowIndex].row[field]) > 1 ? 's' : '')
              + (tableType === 'mutatedGene' ? (' with ' + data[rowIndex].row.gene + ' mutation') :
                (tableType === 'cna' ? (' with ' + data[rowIndex].row.gene + ' ' + data[rowIndex].row.alttype) :
                  (tableType === 'pieLabel' ? (' in ' + data[rowIndex].row.name) : ''))),
              checked: this.props.selectedRowIndex.indexOf(data[rowIndex].index) != -1,
              disabled: this.props.confirmedRowsIndex.indexOf(data[rowIndex].index) !== -1,
              onChange: this.selectRow.bind(this, data[rowIndex].index)}) : ''

        )
      );
    }
  });

// Main part table component
// Uses FixedDataTable library
  var TableMainPart = React.createClass({displayName: "TableMainPart",
    // Creates Qtip
    createQtip: function() {
      $('.EFDT-table .hasQtip').one('mouseenter', function() {
        $(this).qtip({
          content: {text: $(this).attr('data-qtip')},
          hide: {fixed: true, delay: 100},
          show: {ready: true},
          style: {classes: 'qtip-light qtip-rounded qtip-shadow', tip: true},
          position: {my: 'center left', at: 'center right', viewport: $(window)}
        });
      });
    },

    // Creates Qtip after first rendering
    componentDidMount: function() {
      this.createQtip();
    },

    // Creates Qtip after update rendering
    componentDidUpdate: function() {
      this.createQtip();
    },

    // Creates Qtip after page scrolling
    onScrollEnd: function() {
      $(".qtip").remove();
      this.createQtip();
    },

    // Destroys Qtip before update rendering
    componentWillUpdate: function() {
      //console.log('number of elments which has "hasQtip" as class name: ', $('.hasQtip').size());
      //console.log('number of elments which has "hasQtip" as class name under class EFDT: ', $('.EFDT-table .hasQtip').size());

      $('.EFDT-table .hasQtip')
        .each(function() {
          $(this).qtip('destroy', true);
        });
    },

    getDefaultProps: function() {
      return {
        selectedRowIndex: []
      };
    },

    getInitialState: function() {
      return {
        selectedRowIndex: this.props.selectedRowIndex,
        selectedGeneRowIndex: this.props.selectedGeneRowIndex
      }
    },

    selectRow: function(rowIndex) {
      var selectedRowIndex = this.state.selectedRowIndex;
      var selected = false;
      var rows = this.props.rows;
      if ((_.intersection(selectedRowIndex, [rowIndex])).length > 0) {
        selectedRowIndex = _.without(selectedRowIndex, rowIndex);
      } else {
        selectedRowIndex.push(rowIndex);
        selected = true;
      }

      if (_.isFunction(this.props.rowClickFunc)) {
        var selectedData = selectedRowIndex.map(function(item) {
          return rows[item];
        });
        this.props.rowClickFunc(rows[rowIndex], selected, selectedData);
      }

      this.setState({
        selectedRowIndex: selectedRowIndex
      });
    },

    selectGene: function(rowIndex) {
      var selectedGeneRowIndex = this.state.selectedGeneRowIndex;
      var selected = false;
      var rows = this.props.rows;
      if ((_.intersection(selectedGeneRowIndex, [rowIndex])).length > 0) {
        selectedGeneRowIndex = _.without(selectedGeneRowIndex, rowIndex);
      } else {
        selectedGeneRowIndex.push(rowIndex);
        selected = true;
      }

      if (_.isFunction(this.props.geneClickFunc)) {
        this.props.geneClickFunc(rows[rowIndex], selected);
      }

      this.setState({
        selectedGeneRowIndex: selectedGeneRowIndex
      });
    },

    // If properties changed
    componentWillReceiveProps: function(newProps) {
      if (newProps.selectedRowIndex !== this.state.selectedRowIndex) {
        this.setState({
          selectedRowIndex: newProps.selectedRowIndex
        });
      }
      if (newProps.selectedGeneRowIndex !== this.state.selectedGeneRowIndex) {
        this.setState({
          selectedGeneRowIndex: newProps.selectedGeneRowIndex
        });
      }
    },

    // FixedDataTable render function
    render: function() {
      var Table = FixedDataTable.Table, Column = FixedDataTable.Column,
        ColumnGroup = FixedDataTable.ColumnGroup, props = this.props,
        rows = this.props.filteredRows, columnsWidth = this.props.columnsWidth,
        cellShortLabels = this.props.shortLabels.cell,
        headerShortLabels = this.props.shortLabels.header,
        confirmedRowsIndex = this.props.confirmedRowsIndex,
        selectedRowIndex = this.state.selectedRowIndex,
        selectedGeneRowIndex = this.state.selectedGeneRowIndex,
        self = this;

      return (
        React.createElement("div", null,
          React.createElement(Table, {
              rowHeight: props.rowHeight ? props.rowHeight : 30,
              rowGetter: this.rowGetter,
              onScrollEnd: this.onScrollEnd,
              rowsCount: props.filteredRows.length,
              width: props.tableWidth ? props.tableWidth : 1230,
              maxHeight: props.maxHeight ? props.maxHeight : 500,
              headerHeight: props.headerHeight ? props.headerHeight : 30,
              groupHeaderHeight: props.groupHeaderHeight ? props.groupHeaderHeight : 50,
              scrollToColumn: props.goToColumn,
              isColumnResizing: false,
              onColumnResizeEndCallback: props.onColumnResizeEndCallback
            },

            props.cols.map(function(col, index) {
              var column;
              var width = col.show ? (col.width ? col.width :
                  (columnsWidth[col.name] ? columnsWidth[col.name] : 200)) : 0;

              if (props.groupHeader) {
                column = React.createElement(ColumnGroup, {
                    header:
                      React.createElement(Filter, {type: props.filters[col.name].type,
                        name: col.name,
                        max: col.max, min: col.min,
                        filter: props.filters[col.name],
                        placeholder: "Filter column",
                        onFilterKeywordChange: props.onFilterKeywordChange,
                        title: "Filter column"}
                      ),

                    key: col.name,
                    fixed: col.fixed,
                    align: "center"
                  },
                  React.createElement(Column, {
                    header:
                      React.createElement(HeaderWrapper, {cellDataKey: col.name, columnData: {
                          displayName: col.displayName,
                          sortFlag: props.sortBy === col.name,
                          sortDirArrow: props.sortDirArrow,
                          filterAll: props.filterAll,
                          type: props.filters[col.name].type
                        },
                          sortNSet: props.sortNSet,
                          filter: props.filters[col.name],
                          shortLabel: headerShortLabels[col.name]}
                      ),

                    cell: React.createElement(CustomizeCell, {data: rows, field: col.name,
                      filterAll: props.filterAll,
                      shortLabels: cellShortLabels,
                      tableType: props.tableType,
                      selectRow: self.selectRow,
                      selectGene: self.selectGene,
                      selectedRowIndex: selectedRowIndex,
                      selectedGeneRowIndex: selectedGeneRowIndex,
                      pieLabelMouseEnterFunc: props.pieLabelMouseEnterFunc,
                      pieLabelMouseLeaveFunc: props.pieLabelMouseLeaveFunc}
                    ),
                    width: width,
                    fixed: col.fixed,
                    allowCellsRecycling: true,
                    isResizable: props.isResizable,
                    columnKey: col.name,
                    key: col.name}
                  )
                )
              } else {
                column = React.createElement(Column, {
                  header:
                    React.createElement(HeaderWrapper, {cellDataKey: col.name, columnData: {
                        displayName: col.displayName,
                        sortFlag: props.sortBy === col.name,
                        sortDirArrow: props.sortDirArrow,
                        filterAll: props.filterAll,
                        type: props.filters[col.name].type
                      },
                        sortNSet: props.sortNSet,
                        filter: props.filters[col.name],
                        shortLabel: headerShortLabels[col.name]}
                    ),

                  cell: React.createElement(CustomizeCell, {data: rows, field: col.name,
                    filterAll: props.filterAll,
                    shortLabels: cellShortLabels,
                    tableType: props.tableType,
                    selectRow: self.selectRow,
                    selectGene: self.selectGene,
                    selectedRowIndex: selectedRowIndex,
                    selectedGeneRowIndex: selectedGeneRowIndex,
                    confirmedRowsIndex: confirmedRowsIndex,
                    pieLabelMouseEnterFunc: props.pieLabelMouseEnterFunc,
                    pieLabelMouseLeaveFunc: props.pieLabelMouseLeaveFunc}
                  ),
                  width: width,
                  fixed: col.fixed,
                  allowCellsRecycling: true,
                  columnKey: col.name,
                  key: col.name,
                  isResizable: props.isResizable}
                )
              }
              return (
                column
              );
            })

          )
        )
      );
    }
  });

// Root component
  var Main = React.createClass({displayName: "Main",
    SortTypes: {
      ASC: 'ASC',
      DESC: 'DESC'
    },

    rows: null,

    getRulerWidth: function(str, measureMethod, fontSize) {
      var rulerWidth = 0;

      //TODO: what about 0
      if (!str) {
        return 0;
      }

      str = str.toString();
      switch (measureMethod) {
      case 'jquery':
        var ruler = $("#ruler");
        ruler.css('font-size', fontSize);
        ruler.text(str);
        rulerWidth = ruler.outerWidth();
        break;
      default:
        var upperCaseLength = str.replace(/[^A-Z]/g, "").length;
        var dataLength = str.length;
        rulerWidth = upperCaseLength * (fontSize - 4) + (dataLength - upperCaseLength) * (fontSize - 6) + 15;
        break;
      }
      return rulerWidth;
    },

    //TODO: need to find way shorten this time. One possible solution is to calculate the categories for each column, and only detect the width for these categories.
    getShortLabels: function(rows, cols, columnWidth, measureMethod) {
      var cellShortLabels = [];
      var headerShortLabels = {};
      var self = this;
      _.each(rows, function(row) {
        var rowWidthObj = {};
        _.each(row, function(content, attr) {
          var _label = content;
          var _labelShort = _label;
          if (_label) {
            _label = _label.toString();
            var _labelWidth = self.getRulerWidth(_label, measureMethod, 14);

            if (_labelWidth > columnWidth[attr]) {
              var end = Math.floor(_label.length * columnWidth[attr] / _labelWidth) - 3;
              _labelShort = _label.substring(0, end) + '...';
            } else {
              _labelShort = _label;
            }
          }
          rowWidthObj[attr] = _labelShort;
        });
        cellShortLabels.push(rowWidthObj);
      });

      _.each(cols, function(col) {
        if (!col.hasOwnProperty('show') || col.show) {
          var _label = col.displayName;
          var _shortLabel = '';

          if (_label) {
            _label = _label.toString();
            var _labelWidth = self.getRulerWidth(_label, measureMethod, 15);
            if (_labelWidth > columnWidth[col.name]) {
              var end = Math.floor((_label.length) * columnWidth[col.name] / _labelWidth) - 3;
              _shortLabel = _label.substring(0, end) + '...';
            } else {
              _shortLabel = _label;
            }
          }
          headerShortLabels[col.name] = _shortLabel;
        }
      });

      return {
        cell: cellShortLabels,
        header: headerShortLabels
      };
    },
    // Filters rows by selected column
    filterRowsBy: function(filterAll, filters) {
      var rows = this.rows.slice();
      var hasGroupHeader = this.props.groupHeader;
      var filterRowsStartIndex = [];
      var filteredRows = _.filter(rows, function(row, index) {
        var allFlag = false; // Current row contains the global keyword
        for (var col in filters) {
          if (!filters[col].hide) {
            if (filters[col].type === "STRING") {
              if (!row[col] && hasGroupHeader) {
                if (filters[col].key.length > 0) {
                  return false;
                }
              } else {
                if (hasGroupHeader && row[col].toLowerCase().indexOf(filters[col].key.toLowerCase()) < 0) {
                  return false;
                }
                if (row[col] && row[col].toLowerCase().indexOf(filterAll.toLowerCase()) >= 0) {
                  allFlag = true;
                }
              }
            } else if (filters[col].type === "NUMBER" || filters[col].type === 'PERCENTAGE') {
              var cell = filters[col].type === 'PERCENTAGE' ? Number(row[col].toString().replace('%', '')) : row[col];

              if (!isNaN(cell)) {
                if (hasGroupHeader) {
                  if (filters[col].min !== filters[col]._min && cell < filters[col].min) {
                    return false;
                  }
                  if (filters[col].max !== filters[col]._max && cell > filters[col].max) {
                    return false;
                  }
                }
                if (row[col] && row[col].toString().toLowerCase().indexOf(filterAll.toLowerCase()) >= 0) {
                  allFlag = true;
                }
              }
            }
          }
        }
        if (allFlag) {
          filterRowsStartIndex.push(index);
        }
        return allFlag;
      });

      filteredRows = filteredRows.map(function(item, index) {
        return {
          row: item,
          index: filterRowsStartIndex[index]
        }
      });
      return filteredRows;
    },

    // Sorts rows by selected column
    sortRowsBy: function(filters, filteredRows, sortBy, switchDir) {
      var type = filters[sortBy].type, sortDir = this.state.sortDir,
        SortTypes = this.SortTypes, confirmedRowsIndex = this.getSelectedRowIndex(this.state.confirmedRows);
      if (switchDir) {
        if (sortBy === this.state.sortBy) {
          sortDir = this.state.sortDir === SortTypes.ASC ? SortTypes.DESC : SortTypes.ASC;
        } else {
          sortDir = SortTypes.DESC;
        }
      }

      filteredRows.sort(function(a, b) {
        var sortVal = 0, aVal = a.row[sortBy], bVal = b.row[sortBy];

        if (confirmedRowsIndex.indexOf(a.index) !== -1 && confirmedRowsIndex.indexOf(b.index) === -1) {
          return -1;
        }

        if (confirmedRowsIndex.indexOf(a.index) === -1 && confirmedRowsIndex.indexOf(b.index) !== -1) {
          return 1;
        }

        if (sortBy === 'cytoband' && window.hasOwnProperty('StudyViewUtil')) {
          var _sortResult = window.StudyViewUtil.cytobanBaseSort(aVal, bVal);
          sortVal = sortDir === SortTypes.ASC ? -_sortResult : _sortResult;
        } else {

          if (type === "NUMBER") {
            aVal = (aVal && !isNaN(aVal)) ? Number(aVal) : aVal;
            bVal = (bVal && !isNaN(bVal)) ? Number(bVal) : bVal;
          }
          if (type === 'PERCENTAGE') {
            aVal = aVal ? Number(aVal.replace('%', '')) : aVal;
            bVal = bVal ? Number(bVal.replace('%', '')) : bVal;
          }
          if (typeof aVal != "undefined" && !isNaN(aVal) && typeof bVal != "undefined" && !isNaN(bVal)) {
            if (aVal > bVal) {
              sortVal = 1;
            }
            if (aVal < bVal) {
              sortVal = -1;
            }

            if (sortDir === SortTypes.ASC) {
              sortVal = sortVal * -1;
            }
          } else if (typeof aVal != "undefined" && typeof bVal != "undefined") {
            if (!isNaN(aVal)) {
              sortVal = -1;
            } else if (!isNaN(bVal)) {
              sortVal = 1;
            }
            else {
              if (aVal > bVal) {
                sortVal = 1;
              }
              if (aVal < bVal) {
                sortVal = -1;
              }

              if (sortDir === SortTypes.ASC) {
                sortVal = sortVal * -1;
              }
            }
          } else if (aVal) {
            sortVal = -1;
          }
          else {
            sortVal = 1;
          }
        }
        return -sortVal;
      });

      return {filteredRows: filteredRows, sortDir: sortDir};
    },

    // Sorts and sets state
    sortNSet: function(sortBy) {
      var result = this.sortRowsBy(this.state.filters, this.state.filteredRows, sortBy, true);
      this.setState({
        filteredRows: result.filteredRows,
        sortBy: sortBy,
        sortDir: result.sortDir
      });
    },

    // Filters, sorts and sets state
    filterSortNSet: function(filterAll, filters, sortBy) {
      var filteredRows = this.filterRowsBy(filterAll, filters);
      var result = this.sortRowsBy(filters, filteredRows, sortBy, false);
      this.setState({
        filteredRows: result.filteredRows,
        sortBy: sortBy,
        sortDir: result.sortDir,
        filterAll: filterAll,
        filters: filters
      });
    },

    // Operations when filter keyword changes
    onFilterKeywordChange: function(e) {
      ++this.state.filterTimer;

      //Disable event pooling in react, see https://goo.gl/1mq6qI
      e.persist();

      var self = this;
      var id = setTimeout(function() {
        var filterAll = self.state.filterAll, filters = self.state.filters;
        if (e.target.getAttribute("data-column") === "all") {
          filterAll = e.target.value;
        } else {
          filters[e.target.getAttribute("data-column")].key = e.target.value;
        }
        self.filterSortNSet(filterAll, filters, self.state.sortBy);
        --self.state.filterTimer;
      }, 500);

      if (this.state.filterTimer > 1) {
        clearTimeout(id);
        --self.state.filterTimer;
      }
    },

    // Operations when filter range changes
    onFilterRangeChange: function(column, min, max) {
      ++this.state.filterTimer;

      var self = this;
      var id = setTimeout(function() {
        var filters = self.state.filters;
        filters[column].min = min;
        filters[column].max = max;
        self.filterSortNSet(self.state.filterAll, filters, self.state.sortBy);
        --self.state.filterTimer;
      }, 500);

      if (this.state.filterTimer > 1) {
        clearTimeout(id);
        --self.state.filterTimer;
      }
    },

    // Operations when reset all filters
    onResetFilters: function() {
      var filters = this.state.filters;
      _.each(filters, function(filter) {
        if (!_.isUndefined(filter._key)) {
          filter.key = filter._key;
        }
        if (!_.isUndefined(filter._min)) {
          filter.min = filter._min;
        }
        if (!_.isUndefined(filter._max)) {
          filter.max = filter._max;
        }
        filter.reset = true;
      });
      if (this.props.groupHeader) {
        this.registerSliders();
      }
      this.filterSortNSet('', filters, this.state.sortBy);
    },

    updateCols: function(cols, filters) {
      var filteredRows = this.filterRowsBy(this.state.filterAll, filters);
      var result = this.sortRowsBy(filters, filteredRows, this.state.sortBy, false);
      this.setState({
        cols: cols,
        filteredRows: result.filteredRows,
        filters: filters
      });
      if (this.props.groupHeader) {
        this.registerSliders();
      }
    },

    updateGoToColumn: function(val) {
      this.setState({
        goToColumn: val
      });
    },

    registerSliders: function() {
      var onFilterRangeChange = this.onFilterRangeChange;
      $('.rangeSlider')
        .each(function() {
          var min = Math.floor(Number($(this).attr('data-min')) * 100) / 100, max = (Math.ceil(Number($(this).attr('data-max')) * 100)) / 100,
            column = $(this).attr('data-column'), diff = max - min, step = 1;
          var type = $(this).attr('data-type');

          if (diff < 0.01) {
            step = 0.001;
          } else if (diff < 0.1) {
            step = 0.01;
          } else if (diff < 2) {
            step = 0.1;
          }

          $(this).slider({
            range: true,
            min: min,
            max: max,
            step: step,
            values: [min, max],
            change: function(event, ui) {
              $("#range-" + column).text(ui.values[0] + " to " + ui.values[1]);
              onFilterRangeChange(column, ui.values[0], ui.values[1]);
            }
          });
          if (type === 'PERCENTAGE') {
            $("#range-" + column).text(min + "% to " + max + '%');
          } else {
            $("#range-" + column).text(min + " to " + max);
          }
        });
    },
    // Processes input data, and initializes table states
    getInitialState: function() {
      var state = this.parseInputData(this.props.input, this.props.uniqueId,
        this.props.selectedRows, this.props.groupHeader, this.props.columnSorting);

      state.confirmedRows = ['mutatedGene', 'cna'].indexOf(this.props.tableType) !== -1 ? state.selectedRows : [];
      state.filteredRows = null;
      state.filterAll = "";
      state.sortBy = this.props.sortBy ? this.props.sortBy.toLowerCase() : 'cases';
      state.goToColumn = null;
      state.filterTimer = 0;
      state.sortDir = this.props.sortDir || this.SortTypes.DESC;
      state.rowClickFunc = this.rowClickCallback;
      state.selectButtonClickCallback = this.selectButtonClickCallback;
      return state;
    },

    rowClickCallback: function(selectedRows, isSelected, allSelectedRows) {
      var uniqueId = this.state.uniqueId;
      this.setState({
        selectedRows: allSelectedRows.map(function(item) {
          return item[uniqueId];
        })
      });
      if (_.isFunction(this.props.rowClickFunc)) {
        this.props.rowClickFunc(selectedRows, isSelected, allSelectedRows);
      }
    },

    selectButtonClickCallback: function() {
      var selectedRows = this.state.selectedRows;
      this.setState({
        confirmedRows: selectedRows
      });
      if (_.isFunction(this.props.selectButtonClickCallback)) {
        this.props.selectButtonClickCallback();
      }
    },

    getSelectedRowIndex: function(selectedRows) {
      var selectedRowIndex = [];
      var uniqueId = this.state.uniqueId;
      _.each(this.rows, function(row, index) {
        if (selectedRows.indexOf(row[uniqueId]) !== -1) {
          selectedRowIndex.push(index);
        }
      })
      return selectedRowIndex;
    },

    getSelectedGeneRowIndex: function(selectedGene) {
      var selectedGeneRowIndex = [];
      _.each(this.rows, function(row, index) {
        if (selectedGene.indexOf(row.gene) !== -1) {
          selectedGeneRowIndex.push(index);
        }
      })
      return selectedGeneRowIndex;
    },

    parseInputData: function(input, uniqueId, selectedRows, groupHeader, columnSorting) {
      var cols = [], rows = [], rowsDict = {}, attributes = input.attributes,
        data = input.data, dataLength = data.length, col, cell, i, filters = {},
        uniqueId = uniqueId || 'id', newCol,
        selectedRows = selectedRows || [],
        measureMethod = (dataLength > 100000 || !this.props.autoColumnWidth) ? 'charNum' : 'jquery',
        autoColumnWidth = this.props.autoColumnWidth,
        columnMinWidth = this.props.groupHeader ? 130 : 50; //The minimum width to at least fit in number slider.

      var selectedRowIndex = [];
      var columnsWidth = {}, self = this;

      // Gets column info from input
      var colsDict = {};
      for (i = 0; i < attributes.length; i++) {
        col = attributes[i];
        col.attr_id = col.attr_id.toLowerCase();
        newCol = {
          displayName: col.display_name,
          name: col.attr_id,
          type: col.datatype,
          fixed: false,
          show: true
        };

        if (col.hasOwnProperty('column_width')) {
          newCol.width = col.column_width;
        }

        if (_.isBoolean(col.show)) {
          newCol.show = col.show;
        }

        if (_.isBoolean(col.fixed)) {
          newCol.fixed = col.fixed;
        }

        cols.push(newCol);
        colsDict[col.attr_id] = newCol;
        columnsWidth[col.attr_id] = 0;
      }

      // Gets data rows from input
      for (i = 0; i < dataLength; i++) {
        cell = data[i];
        cell.attr_id = cell.attr_id.toLowerCase();

        if (!colsDict.hasOwnProperty(cell.attr_id)) {
          continue;
        }

        if (!rowsDict[cell[uniqueId]]) {
          rowsDict[cell[uniqueId]] = {};
        }

        //Clean up the input data
        if (_.isUndefined(cell.attr_val)) {
          cell.attr_val = '';
        }

        if (colsDict[cell.attr_id].type === 'NUMBER') {
          rowsDict[cell[uniqueId]][cell.attr_id] = cell.attr_val !== '' ? Number(cell.attr_val) : NaN;
        } else if (colsDict[cell.attr_id].type === 'STRING') {
          rowsDict[cell[uniqueId]][cell.attr_id] = cell.attr_val.toString();
        } else if (colsDict[cell.attr_id].type === 'PERCENTAGE') {
          rowsDict[cell[uniqueId]][cell.attr_id] = cell.attr_val.toString();
        } else {
          rowsDict[cell[uniqueId]][cell.attr_id] = cell.attr_val;
        }

        if (autoColumnWidth) {
          var val = rowsDict[cell[uniqueId]][cell.attr_id];
          var rulerWidth = 0;
          if (val !== 0) {
            rulerWidth = val ? this.getRulerWidth(val, measureMethod, 14) : 0;
          }
          columnsWidth[cell.attr_id] = columnsWidth[cell.attr_id] < rulerWidth ? rulerWidth : columnsWidth[cell.attr_id];
        }
      }

      if (!autoColumnWidth) {
        _.each(cols, function(col, attr) {
          columnsWidth[col.name] = col.width ? col.width : 200;
        });
      } else {
        columnsWidth = _.object(_.map(columnsWidth, function(length, attr) {
          return [attr, length > self.props.columnMaxWidth ?
            self.props.columnMaxWidth :
            ( (length + 20) < columnMinWidth ?
              columnMinWidth : (length + 20))];
        }));
      }

      var index = 0;
      var _uniqueId = uniqueId.toLowerCase();
      _.each(rowsDict, function(item, i) {
        rowsDict[i][_uniqueId] = i;
        rows.push(rowsDict[i]);
        if (selectedRows.indexOf(i) !== -1) {
          selectedRowIndex.push(index);
        }
        ++index;
      });

      // Gets the range of number type features
      for (i = 0; i < cols.length; i++) {
        col = cols[i];
        var _filter = {
          type: col.type,
          hide: !col.show
        };

        if (col.type === "NUMBER" || col.type === "PERCENTAGE") {
          var min = Number.MAX_VALUE, max = -Number.MAX_VALUE;
          for (var j = 0; j < rows.length; j++) {
            cell = col.type === "PERCENTAGE" ? Number(rows[j][col.name].replace('%')) : rows[j][col.name];
            if (!isNaN(cell)) {
              max = cell > max ? cell : max;
              min = cell < min ? cell : min;
            }
          }
          if (max === -Number.MAX_VALUE || min === Number.MIN_VALUE) {
            _filter.key = '';
            _filter._key = '';
          } else {
            col.max = max;
            col.min = min;
            _filter.min = min;
            _filter.max = max;
            _filter._min = min;
            _filter._max = max;
          }
        } else {
          _filter.key = '';
          _filter._key = '';
        }
        filters[col.name] = _filter;
      }

      if (columnSorting) {
        cols = _.sortBy(cols, function(obj) {
          if (!_.isUndefined(obj.displayName)) {
            return obj.displayName;
          } else {
            return obj.name;
          }
        });
      }
      this.rows = rows;

      var shortLabels = this.getShortLabels(rows, cols, columnsWidth, measureMethod);

      return {
        cols: cols,
        rowsSize: rows.length,
        filters: filters,
        shortLabels: shortLabels,
        columnsWidth: columnsWidth,
        columnMinWidth: columnMinWidth,
        selectedRowIndex: selectedRowIndex,
        selectedRows: selectedRows,
        dataSize: dataLength,
        measureMethod: measureMethod,
        uniqueId: _uniqueId
      };
    },
    // If properties changed
    componentWillReceiveProps: function(newProps) {
      var state = this.parseInputData(newProps.input, newProps.uniqueId,
        newProps.selectedRows, newProps.groupHeader, newProps.columnSorting);
      state.confirmedRows = ['mutatedGene', 'cna'].indexOf(newProps.tableType) !== -1 ? state.selectedRows : [];
      state.filteredRows = null;
      state.filterAll = this.state.filterAll || '';
      if (newProps.sortBy && newProps.sortBy.toLowerCase() !== this.state.sortBy) {
        state.sortBy = newProps.sortBy.toLowerCase();
      } else {
        state.sortBy = this.state.sortBy || 'cases';
      }
      if (newProps.sortDir && newProps.sortDir.toLowerCase() !== this.state.sortDir) {
        state.sortDir = newProps.sortDir;
      } else {
        state.sortDir = this.state.sortDir || this.SortTypes.DESC;
      }
      state.goToColumn = null;
      state.filterTimer = 0;

      var filteredRows = this.filterRowsBy(state.filterAll, state.filters);
      var result = this.sortRowsBy(state.filters, filteredRows, state.sortBy, false);
      state.filteredRows = result.filteredRows;

      this.setState(state);
    },

    // Initializes filteredRows before first rendering
    componentWillMount: function() {
      var rows = this.rows.map(function(item, index) {
        return {
          row: item,
          index: index
        }
      });
      var result = this.sortRowsBy(this.state.filters, rows, this.state.sortBy, false);
      this.setState({
        filteredRows: result.filteredRows,
        sortBy: this.state.sortBy,
        sortDir: result.sortDir,
        filterAll: this.state.filterAll,
        filters: this.state.filters
      });
    },

    //Will be triggered if the column width has been changed
    onColumnResizeEndCallback: function(width, key) {
      var foundMatch = false;
      var cols = this.state.cols;

      _.each(cols, function(col, attr) {
        if (col.name === key) {
          col.width = width;
          foundMatch = true;
        }
      });
      if (foundMatch) {
        var columnsWidth = this.state.columnsWidth;
        columnsWidth[key] = width;
        var shortLabels = this.getShortLabels(this.rows, cols, columnsWidth, this.state.measureMethod);
        this.setState({
          columnsWidth: columnsWidth,
          shortLabels: shortLabels,
          cols: cols
        });
      }
    },

    // Activates range sliders after first rendering
    componentDidMount: function() {
      if (this.props.groupHeader) {
        this.registerSliders();
      }
    },

    // Expose the current sorting settings
    getCurrentSort: function() {
      return {
        sortBy: this.state.sortBy,
        sortDir: this.state.sortDir
      };
    },

    // Sets default properties
    getDefaultProps: function() {
      return {
        filter: "NONE",
        download: "NONE",
        showHide: false,
        hideFilter: true,
        scroller: false,
        resultInfo: true,
        groupHeader: true,
        downloadFileName: 'data.txt',
        autoColumnWidth: true,
        columnMaxWidth: 300,
        columnSorting: true,
        tableType: 'mutatedGene',
        selectedRows: [],
        selectedGene: [],
        sortBy: 'cases',
        sortDir: 'DESC',
        isResizable: false
      };
    },

    render: function() {
      var sortDirArrow = this.state.sortDir === this.SortTypes.DESC ? 'fa fa-sort-desc' : 'fa fa-sort-asc';
      var selectedGeneRowIndex = this.getSelectedGeneRowIndex(this.props.selectedGene);
      var selectedRowIndex = this.getSelectedRowIndex(this.state.selectedRows);
      var confirmedRowsIndex = this.getSelectedRowIndex(this.state.confirmedRows);
      return (
        React.createElement("div", {className: "EFDT-table"},
          React.createElement("div", {className: "EFDT-table-prefix"},
            React.createElement(TablePrefix, {cols: this.state.cols, rows: this.rows,
              onFilterKeywordChange: this.onFilterKeywordChange,
              onResetFilters: this.onResetFilters,
              filters: this.state.filters,
              updateCols: this.updateCols,
              updateGoToColumn: this.updateGoToColumn,
              scroller: this.props.scroller,
              filter: this.props.filter,
              hideFilter: this.props.hideFilter,
              getData: this.props.download,
              downloadFileName: this.props.downloadFileName,
              hider: this.props.showHide,
              fixedChoose: this.props.fixedChoose,
              resultInfo: this.props.resultInfo,
              rowsSize: this.state.rowsSize,
              filteredRowsSize: this.state.filteredRows.length}
            )
          ),
          React.createElement("div", {className: "EFDT-tableMain"},
            React.createElement(TableMainPart, {cols: this.state.cols,
              rows: this.rows,
              filteredRows: this.state.filteredRows,
              filters: this.state.filters,
              sortNSet: this.sortNSet,
              onFilterKeywordChange: this.onFilterKeywordChange,
              goToColumn: this.state.goToColumn,
              sortBy: this.state.sortBy,
              sortDirArrow: sortDirArrow,
              filterAll: this.state.filterAll,
              filter: this.props.filter,
              rowHeight: this.props.rowHeight,
              tableWidth: this.props.tableWidth,
              maxHeight: this.props.maxHeight,
              headerHeight: this.props.headerHeight,
              groupHeaderHeight: this.props.groupHeaderHeight,
              groupHeader: this.props.groupHeader,
              tableType: this.props.tableType,
              confirmedRowsIndex: confirmedRowsIndex,
              shortLabels: this.state.shortLabels,
              columnsWidth: this.state.columnsWidth,
              rowClickFunc: this.state.rowClickFunc,
              geneClickFunc: this.props.geneClickFunc,
              selectButtonClickCallback: this.state.selectButtonClickCallback,
              pieLabelMouseEnterFunc: this.props.pieLabelMouseEnterFunc,
              pieLabelMouseLeaveFunc: this.props.pieLabelMouseLeaveFunc,
              selectedRowIndex: selectedRowIndex,
              selectedGeneRowIndex: selectedGeneRowIndex,
              isResizable: this.props.isResizable,
              onColumnResizeEndCallback: this.onColumnResizeEndCallback}
            )
          ),
          React.createElement("div", {className: "EFDT-filter"},

            (this.props.filter === "ALL" || this.props.filter === "GLOBAL") ?
              React.createElement(Filter, {type: "STRING", name: "all",
                onFilterKeywordChange: this.onFilterKeywordChange}) :
              React.createElement("div", null)

          ),
          React.createElement("div", {className: "EFDT-finish-selection-button"},

            (['mutatedGene', 'cna'].indexOf(this.props.tableType) !== -1 && this.state.selectedRows.length > 0 && this.state.confirmedRows.length !== this.state.selectedRows.length ) ?
              React.createElement("button", {className: "btn btn-default btn-xs",
                onClick: this.state.selectButtonClickCallback}, "Select" + ' ' +
                "Samples") : ''

          )
        )
      );
    }
  });

  return Main;
})();
