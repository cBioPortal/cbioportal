var Promise = (function() {
    function Promise() {
	this.callbacks = [];
	this.resolved = false;
	this.resolve_arg = null;
    }
    Promise.prototype.then = function(callback) {
	if (this.resolved) {
	    callback(this.resolve_arg);
	} else {
	    this.callbacks.push(callback);
	}
	return this;
    }
    Promise.prototype.resolve = function(arg) {
	this.resolve_arg = arg;
	while (this.callbacks.length > 0) {
	    var next_callback = this.callbacks.shift();
	    next_callback(this.resolve_arg);
	}
    }
})();

module.exports = Promise;