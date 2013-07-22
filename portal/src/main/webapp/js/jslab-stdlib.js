// **************************************************************************
// Copyright 2007 - 2008 The JSLab Team, Tavs Dokkedahl and Allan Jacobs
// Contact: http://www.jslab.dk/contact.php
//
// This file is part of the JSLab Standard Library (JSL) Program.
//
// JSL is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or
// any later version.
//
// JSL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
// ***************************************************************************
// File created 2009-01-28 10:09:31
// 
// functions modified and renamed by Selcuk Onur Sumer to convert from 
// prototype to plain versions

// Compute the intersection of n arrays
function intersectNArrays(firstArray) {
    if (!arguments.length)
      return [];
    var a1 = firstArray;
    var a = a2 = null;
    var n = 1;
    while(n < arguments.length) {
      a = [];
      a2 = arguments[n];
      var l = a1.length;
      var l2 = a2.length;
      for(var i=0; i<l; i++) {
        for(var j=0; j<l2; j++) {
          if (a1[i] == a2[j])
            a.push(a1[i]);
        }
      }
      a1 = a;
      n++;
    }
    return uniqueElementsOfArray(a);
  };

// Return new array with duplicate values removed
function uniqueElementsOfArray(collection) {
    var a = [];
    var l = collection.length;
    for(var i=0; i<l; i++) {
      for(var j=i+1; j<l; j++) {
        // If this[i] is found later in the array
        if (collection[i] == collection[j])
          j = ++i;
      }
      a.push(collection[i]);
    }
    return a;
  };
