// FIRST PASS: no optimization
var OncoprintSVGCellView = (function () {
    function OncoprintSVGCellView($svg) {
	this.$svg = $svg;
	this.track_shapes = {};
    }
    OncoprintSVGCellView.prototype.removeTrack = function (model, track_id) {
	// TODO: what parameters
	// TODO: implementation
    }
    OncoprintSVGCellView.prototype.moveTrack = function () {
	// TODO: what parameters
	// TODO: implementation
    }

    var renderTrack = function (cell_view, model, track_id) {
	cell_view.track_shapes[track_id] = cell_view.track_shapes[track_id] || [];
	var track_shapes = cell_view.track_shapes[track_id];
	while (track_shapes.length > 0) {
	    var elt = track_shapes.pop();
	    elt.parentNode.removeChild(elt);
	}

	var y = cell_view.getTrackTop(model, track_id) + model.getTrackPadding(track_id);
	// Now y is the top of the cells
	var cell_width = model.getCellWidth();
	var cell_padding = model.getCellPadding();
	var cell_height = model.getTrackHeight(track_id);

	var shape_list_list = model.getRuleSet(track_id).apply(
		model.getTrackData(track_id),
		cell_width,
		cell_height);
	for (var i = 0; i < shape_list_list.length; i++) {
	    var x = i * (cell_width + cell_padding);
	    var shape_list = shape_list_list[i];
	    for (var j = 0; j < shape_list.length; j++) {
		track_shapes.push(cell_view.renderShape(shape_list[j], x, y));
	    }
	}
    }

    var renderTracks = function (cell_view, model) {
	var tracks = model.getTracks();
	for (var i = 0; i < tracks.length; i++) {
	    renderTrack(cell_view, model, tracks[i]);
	}
    }

    OncoprintSVGCellView.prototype.addTracks = function (model, track_ids) {
	for (var i = 0; i < track_ids.length; i++) {
	    renderTrack(this, model, track_ids[i]);
	}
    }

    OncoprintSVGCellView.prototype.renderShape = function (shape, x, y) {
	var tag;
	if (shape.type === 'rectangle') {
	    tag = this.renderRectangle(shape, x, y);
	} else if (shape.type === 'triangle') {
	    tag = this.renderTriangle(shape, x, y);
	} else if (shape.type === 'ellipse') {
	    tag = this.renderEllipse(shape, x, y);
	} else if (shape.type === 'line') {
	    tag = this.renderLine(shape, x, y);
	}
	return tag;
    }

    var makeSVGTag = function (tag, attrs) {
	var el = document.createElementNS('http://www.w3.org/2000/svg', tag);
	for (var k in attrs) {
	    if (attrs.hasOwnProperty(k)) {
		el.setAttribute(k, attrs[k]);
	    }
	}
	return el;
    }
    OncoprintSVGCellView.prototype.renderRectangle = function (rectangle, x, y) {
	var new_rect = makeSVGTag('rect', {
	    'x': x + parseFloat(rectangle.x),
	    'y': y + parseFloat(rectangle.y),
	    'width': rectangle.width,
	    'height': rectangle.height,
	    'stroke': rectangle.stroke,
	    'stroke-width': rectangle['stroke-width'],
	    'fill': rectangle.fill
	});
	this.$svg[0].appendChild(new_rect);
	return new_rect;
    }

    OncoprintSVGCellView.prototype.renderTriangle = function (rectangle, x, y,
	    cell_width, cell_height) {
	// TODO: implement
    }

    OncoprintSVGCellView.prototype.renderEllipse = function (rectangle, x, y,
	    cell_width, cell_height) {
	// TODO: implement
    }

    OncoprintSVGCellView.prototype.renderLine = function (rectangle, x, y,
	    cell_width, cell_height) {
	// TODO: implement
    }

    OncoprintSVGCellView.prototype.setCellPadding = function () {
	// TODO: what parameters
	// TODO: implementation
    }


    OncoprintSVGCellView.prototype.setZoom = function (model, z) {
	renderTracks(this, model);
    }
    OncoprintSVGCellView.prototype.setOrder = function () {
	// TODO: what parameters
	// TODO: implementation
    }

    OncoprintSVGCellView.prototype.setTrackData = function (model, track_id) {
	renderTrack(this, model, track_id);
    }

    OncoprintSVGCellView.prototype.setRuleSet = function (model, track_id) {
	renderTrack(this, model, track_id);
    }

    return OncoprintSVGCellView;
})();

module.exports = OncoprintSVGCellView;