module.exports = function(array, target_key, keyFn, return_closest_if_not_found) {
    var upper_excl = array.length;
    var lower_incl = 0;
    var middle;
    while (lower_incl < upper_excl) {
	middle = Math.floor((upper_excl + lower_incl) / 2);
	var middle_key = keyFn(array[middle]);
	if (middle_key === target_key) {
	    return middle;
	} else if (target_key > middle_key) {
	    lower_incl = middle + 1;
	} else if (target_key < middle_key) {
	    upper_excl = middle;
	}
    }
    if (return_closest_if_not_found) {
	return lower_incl-1;
    } else {
	return null;
    }
}