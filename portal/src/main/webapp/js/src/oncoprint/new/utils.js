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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
window.oncoprint_utils = (function() {
	var exports = {};

	exports.sign = function(number) {
		return number?((number<0)?-1:1):0
	};

	exports.invert_array = function invert_array(arr) {
		return arr.reduce(function(curr, next, index) {
			curr[next] = index;
			return curr;
		}, {});
	};

	exports.cssClassToSelector = function(classes) {
		return "."+classes.split(" ").join(".");
	};
	exports.mouseY = function(evt) {
		return exports.ifndef(evt.offsetY, evt.originalEvent && evt.originalEvent.layerY);
	};
	exports.mouseX = function(evt) {
		return exports.ifndef(evt.offsetX, evt.originalEvent && evt.originalEvent.layerX);
	};
	exports.ifndef = function(val, replacement) {
		return (typeof val === 'undefined') ? replacement : val;
	};
	exports.extends = function(child_class, parent_class) {
		child_class.prototype = Object.create(parent_class.prototype);
		child_class.prototype.constructor = child_class;
	};

	exports.makeIdCounter = function() {
		var counter = 0;
		return function() {
			counter += 1;
			return counter;
		};
	};

	exports.clamp = function(t, a, b) {
		return Math.max(Math.min(b,t), a);
	};

	exports.makeD3SVGElement = function(tag) {
		return d3.select(document.createElementNS('http://www.w3.org/2000/svg', tag));
	};

	exports.appendD3SVGElement = function(elt, target, svg) {
		return target.select(function() {
			return this.appendChild(elt.node().cloneNode(true));
		});
	};

	exports.spaceSVGElementsHorizontally = function(group, padding) {
		var x = 0;
		var elts = exports.d3SelectChildren(group, '*').each(function() {
			if (this.tagName === 'defs') {
				// don't adjust spacing for a defs element
				return;
			}
			var transform = d3.select(this).attr('transform');
			var y = transform && transform.indexOf("translate") > -1 && parseFloat(transform.split(",")[1], 10);
			y = y || 0;
			d3.select(this).attr('transform', exports.translate(x, y));
			x += this.getBBox().width;
			x += padding;
		});
		return group;
	};

	exports.textWidth = function(string, font) {
		var obj = $('<div>'+string+'</div>')
				.css({position: 'absolute', float: 'left',
					'white-space':'nowrap', visibility: 'hidden',
					font: font})
				.appendTo($('body'));
		var width = obj.width();
		obj.remove();
		return width;
	};

	exports.d3SelectChildren = function(parent, selector) {
		return parent.selectAll(selector).filter(function() {
			return this.parentNode === parent.node();
		});
	};

	exports.warn = function(str, context) {
		console.log("Oncoprint error in "+context+": "+str);
	};

	exports.stableSort = function(arr, cmp) {
		// cmp returns something in [-1,0,1]

		var zipped = [];
		_.each(arr, function(val, ind) {
			zipped.push([val, ind]);
		});
		var stable_cmp = function(a,b) {
			var res = cmp(a[0], b[0]);
			if (res === 0) {
				if (a[1] < b[1]) {
					res = -1;
				} else if (a[1] > b[1]) {
					res = 1;
				}
			}
			return res;
		};
		zipped.sort(stable_cmp);
		return _.map(zipped, function(x) { return x[0];});
	};

	exports.lin_interp = function(t, a, b) {
		if (a[0] === '#') {
			var r = [parseInt(a.substring(1,3), 16), parseInt(b.substring(1,3), 16)];
			var g = [parseInt(a.substring(3,5), 16), parseInt(b.substring(3,5), 16)];
			var b = [parseInt(a.substring(5,7), 16), parseInt(b.substring(5,7), 16)];
			var R = Math.round(r[0]*(1-t) + r[1]*t).toString(16);
			var G = Math.round(g[0]*(1-t) + g[1]*t).toString(16);
			var B = Math.round(b[0]*(1-t) + b[1]*t).toString(16);

			R = R.length < 2 ? '0'+R : R;
			G = G.length < 2 ? '0'+G : G;
			B = B.length < 2 ? '0'+B : B;

			return '#' + R + G + B;
		} else if (isNaN(a) && a.indexOf('%') > -1) {
			var A = parseFloat(a, 10);
			var B = parseFloat(b, 10);
			return (A*(1-t) + B*t)+'%';
		} else {
			return a*(1-t) + b*t;
		}
	};

	exports.translate = function(x,y) {
		return "translate(" + x + "," + y + ")";
	};

	exports.assert = function(bool, msg) {
		if (!bool) {
			throw msg;
		}
	}
	return exports;
})();
