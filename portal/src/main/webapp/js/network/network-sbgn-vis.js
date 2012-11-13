/**
 * Constructor for the network (sbgn) visualization class.
 *
 * @param divId     target div id for this visualization.
 * @constructor
 */
function NetworkSbgnVis(divId)
{
    // call the parent constructor
    NetworkVis.call(this, divId);
}

// this simulates NetworkSbgnVis extends NetworkVis (inheritance)
NetworkSbgnVis.prototype = new NetworkVis("");

// update constructor
NetworkSbgnVis.prototype.constructor = NetworkSbgnVis;

// TODO override necessary methods (filters, inspectors, initializers, etc.) to have a proper UI.