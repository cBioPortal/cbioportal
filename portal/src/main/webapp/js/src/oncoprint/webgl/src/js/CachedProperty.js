var CachedProperty = (function() {
    function CachedProperty(init_val, updateFn) {
	this.value = init_val;
	this.updateFn = updateFn;
	this.bound_properties = [];
    }
    CachedProperty.prototype.update = function() {
	this.value = this.updateFn.apply(null, arguments);
	for (var i=0; i<this.bound_properties.length; i++) {
	    this.bound_properties[i].update();
	}
    }
    CachedProperty.prototype.get = function() {
	return this.value;
    }
    CachedProperty.prototype.updateAndGet = function() {
	this.update();
	return this.get();
    }
    CachedProperty.prototype.addBoundProperty = function(cached_property) {
	this.bound_properties.push(cached_property);
    };
    return CachedProperty;
})();

module.exports = CachedProperty;