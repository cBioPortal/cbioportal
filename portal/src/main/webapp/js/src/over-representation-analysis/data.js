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


var orData = function() {
    
    var data = {}, retrieved = false;

    return {
        init: function(_param) {
            $.ajax({
                url: "oranalysis.do",
                method: "POST",
                data: _param
            })  
            .done(function(result) {
                result = result.substring(0, result.length - 2); //remove the quote sign
                var _arr = result.substring(1, result.length).split("|");
                $.each(_arr, function(index, str) {
                    var _tmp = str.split(":");
                    data[_tmp[0]] = _tmp[1];
                });
                retrieved = true;
            })
            .fail(function( jqXHR, textStatus ) {
                alert( "Request failed: " + textStatus );
            }); 
        },
        get: function(callback_func, _div_id, _table_div, _table_id, _table_title) { 
            var tmp = setInterval(function () { timer(); }, 1000);
            function timer() {
                if (retrieved) {
                    clearInterval(tmp);
                    callback_func(data, _div_id, _table_div, _table_id, _table_title);
                }
            }
        }
    };

};
