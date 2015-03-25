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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


function DataTableWrapper() {
    this.dataTable = null;
}
DataTableWrapper.prototype = {
    setDataMatrixAndFixTypes: function(matrix) {
        var converter = new MatrixDataTypeConverter();
        converter.convertTypes(matrix);
        this.dataTable = google.visualization.arrayToDataTable(converter.dataMatrix);
        for (var i=matrix[0].length-1; i>=0; i--) {
            if (converter.isColumnNA(i)) {
                this.dataTable.removeColumn(i);
            }
        }
    },
    setDataMap: function(map,headers) {
        var arr = [headers];
        for (var key in map) {
            arr.push([key,map[key]]);
        }
        this.dataTable = google.visualization.arrayToDataTable(arr);
    },
    getColumnData: function(col) {
        var data = [];
        var rows = this.dataTable.getNumberOfRows();
        for (var i=0; i<rows; i++) {
            data.push(this.dataTable.getValue(i,col));
        }
        return data;
    }
};

function MatrixDataTypeConverter() {
    this.dataMatrix = null;
    this.colTypes = null;
}
MatrixDataTypeConverter.prototype = {
    convertTypes: function (dataMatrix) {
        this.dataMatrix = dataMatrix;
        this.colTypes = this.determineColumnTypes(this.dataMatrix);
        var headers = this.dataMatrix[0];
        for (var c=0; c<headers.length; c++) {
            for (var r=1; r<this.dataMatrix.length; r++) {
                if (this.isValueNA(this.dataMatrix[r][c])) {
                    this.dataMatrix[r][c] = null;
                } else if (this.colTypes[c]=='number') {
                    this.dataMatrix[r][c] = parseFloat(this.dataMatrix[r][c]);
                } else if (this.colTypes[c]=='boolean') {
                    var dl = this.dataMatrix[r][c].toLowerCase();
                    this.dataMatrix[r][c] = dl=='true' || dl=='y';
                }
            }
        }
    },
    determineColumnTypes: function (dataMatrix) {
        var rows = dataMatrix.length;
        var headers = dataMatrix[0];
        var types = [];
        var cols = headers.length;
        for (var c=0; c<cols; c++) {
            var type = headers[c].toLowerCase().match(/(_cluster$)|(_id$)/) ? 'string' : null;
            for (var r=1; r<rows; r++) {
                if (type=='string') break;
                var d = dataMatrix[r][c];
                if (this.isValueNA(d)) continue;
                if (type==null)
                    type = this.getType(d);
                else if (type!=this.getType(d))
                    type = 'string';
            }
            types.push(type);
        }
        return types;
    },
    getType: function(str) {
        if (this.isNum(str))
            return 'number';
        if (this.isBool(str))
            return 'boolean';
        return 'string';
    },
    isNum: function (str) {
        this.regex = new RegExp('^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$');
        return this.regex.test(str);
    },
    isBool: function (str) {
        return false;
        //this.regex = new RegExp('^(true)|(false)|(y)|(n)$');
        //return this.regex.test(str.toLowerCase());
    },
    isColumnNA: function(col) {
        return this.colTypes[col] == null;
    },
    isValueNA: function(value) {
        return value==null || (typeof value)==undefined || value.length==0 || value==='N/A' || value==="NA";
    }
};
