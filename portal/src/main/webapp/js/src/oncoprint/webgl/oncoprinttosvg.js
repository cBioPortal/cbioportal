var shapeToSVG = require('./oncoprintshapetosvg.js');

var makeSVGElement = function (tag, attrs) {
    var el = document.createElementNS('http://www.w3.org/2000/svg', tag);
    for (var k in attrs) {
	if (attrs.hasOwnProperty(k)) {
	    el.setAttribute(k, attrs[k]);
	}
    }
    return el;
};

var cellsToSVG = function(model) {
    var tracks = model.getTracks();
    var id_to_index_map = model.getIdToIndexMap();
    var shape_lists = {};
    
    (function prepareShapeLists() {
	for (var i = 0; i < tracks.length; i++) {
	    var track_id = tracks[i];

	    var identified_shape_list_list = model.getIdentifiedShapeListList(track_id);
	    for (var j = 0; j < identified_shape_list_list.length; j++) {
		identified_shape_list_list[j].shape_list.sort(function (shapeA, shapeB) {
		    var diff = parseFloat(shapeA.z) - parseFloat(shapeB.z);
		    if (diff < 0) {
			return -1;
		    } else if (diff > 0) {
			return 1;
		    } else {
			return 0;
		    }
		});
	    }
	    shape_lists[track_id] = identified_shape_list_list;
	    shape_lists[track_id].sort(function (id_slA, id_slB) {
		var diff = id_to_index_map[id_slA.id] - id_to_index_map[id_slB.id];
		if (diff < 0) {
		    return -1;
		} else if (diff > 0) {
		    return 1;
		} else {
		    return 0;
		}
	    });
	}
    })();
    
    var root = makeSVGElement("svg");
    
};

module.exports = function() {
    
}