
//------------------------------------------------------------------------------
//- preamble -------------------------------------------------------------------
//------------------------------------------------------------------------------

// for the benefit of jslint, declare global variables from outside this script
/*global $, $R, $w, $break, Class, console, Element, Hash, Event, document,
  window, G_vmlCanvasManager, Template, Tip */

// spoof a console, if necessary, so that we can run in IE (<8) without having
// to entirely disable debug messages
if ( ! window.console ) {
  window.console     = {};
  window.console.log = function() {};
}  

//------------------------------------------------------------------------------
//- class ----------------------------------------------------------------------
//------------------------------------------------------------------------------

// A javascript library for drawing Pfam-style domain graphics in an HTML5
// canvas.
//
// jt6 20090803 WTSI
//
// $Id: domain_graphics.js 6299 2011-05-04 12:09:52Z jt6 $
//
// Copyright (c) 2009: Genome Research Ltd.
// 
// Authors: Rob Finn (rdf@sanger.ac.uk), John Tate (jt6@sanger.ac.uk)
// 
// This is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details.
//
// You should have received a copy of the GNU General Public License along with
// this program. If not, see <http://www.gnu.org/licenses/>.

var PfamGraphic = Class.create( {
  /**
   * @lends PfamGraphic#
   * @author John Tate
   * @author Rob Finn
   */

  /**
   * A hash for keeping track of the canvas(es) that we use to draw the graphics.
   * For each new <code>&lt;canvas&gt;</code> we store its parent element and
   * its list of areas, keyed on the ID of the canvas element itself.
   * 
   * @private
   */
  _canvases: new Hash(),

  /**
   * A boolean for keeping track of whether the "middle click listener" has
   * been added to the window.
   * 
   * @private
   */
  _middleClickListenerAdded: false,

  //----------------------------------------------------------------------------
  //- constructor --------------------------------------------------------------
  //----------------------------------------------------------------------------
  /**
   * @class 
   * A javascript library for drawing Pfam-style domain graphics in an HTML5
   * &lt;canvas&gt; element. 
   * </p>
   * 
   * <h2>Synopsis</h2>
   *
   * <code><pre>
   *   // build the description of the graphic
   *   var sequence = {
   *     length: 400,
   *     regions: [ { startStyle: "curved",
   *                  endStyle:   "curved",
   *                  start:      40,
   *                  end:        220,
   *                  aliStart:   60,
   *                  aliEnd:     200,
   *                  colour:     "#ff8800",
   *                  text:       "first" },
   *                { startStyle: "jagged",
   *                  endStyle:   "jagged",
   *                  start:      300,
   *                  end:        380,
   *                  aliStart:   315,
   *                  aliEnd:     350,
   *                  colour:     "#ff0000",
   *                  text:       "second" } ]
   *   };
   *  
   *   // get a handle on the element in the page (usually a div) that will 
   *   // hold the canvas 
   *   var parentEl = document.getElementById("canvasContainer");
   *   
   *   // get a PfamGraphic object and hand it the element and sequence object
   *   var pg = new PfamGraphic( parentEl, sequence );
   *  
   *   // create a canvas within the specified element and draw the graphic on 
   *   // that canvas
   *   pg.render();</pre>
   * </code>
   *
   * <h2>Usage</h2>
   * 
   * <p>
   * One of the main visualisations in the <a href="http://pfam.sanger.ac.uk/">
   *   Pfam website</a> is the Pfam domain graphic. Domain graphics provide
   * simple representations of protein sequences, showing the locations of 
   * various sequence elements, from Pfam-A families to predicted active sites 
   * and disulphide bridges. Given a description of the sequence, this library 
   * draws Pfam graphics within a browser window.
   * </p>
   * 
   * <h2>Inputs</h2>
   *
   * <p>
   * The library needs a minimum of two inputs. Unless specified in the options,
   * the library will create a new &lt;canvas&gt; element and use that for 
   * drawing. The parent can be given either as a string, with the ID of the 
   * element in question, or as a reference to the DOM element. The canvas 
   * element will be attached to the page as the child of the supplied parent 
   * element. 
   * </p>
   * <p>
   * The graphic itself is described by a javascript data structure (a JSON 
   * object) that specifies all necessary parameters, from the length of the 
   * sequence to the colours of the domains and other markup.
   * </p>
   * 
   * <h3>Sequence object</h3>
   *
   * <p>The sequence object must have the following structure:</p>
   *
   * <pre>
   *   var structure = {
   *     length: 100,     // required: positive integer
   *     regions: [],     // optional: javascript array
   *     motifs:  [],     // optional: javascript array
   *     markups: [],     // optional: javascript array
   *     pileups: [],     // optional: javascript array
   *     histograms: [],     // optional: javascript array
   *     options: {}      // optional: javascript object
   *   };
   * </pre>
   *
   * <p>
   * There are five main sections, <code>regions</code>, <code>motifs</code> 
   * <code>markups</code>, <code>pileups</code>, and <code>histograms</code>.
   * Each can be empty or can be omitted entirely.  The five distinct types of
   * entity are:
   * </p>
   * 
   * <dl>
   *   <dt>region</dt>
   *   <dd>typically a Pfam-A region, represented as a &quot;lozenge&quot;
   *     with various types of vertical edge</dd>
   *   <dt>motif</dt>
   *   <dd>a sequence feature such as a Pfam-B region, drawn as a simple
   *     rectangle</dd>
   *   <dt>markup</dt>
   *   <dd>a sequence annotation, such as a disulphide bridge, drawn as
   *     a line joining two points on the sequence, or an active site 
   *     residue, drawn as a &quot;lollipop&quot;</dd>
  *   <dt>pileups</dt>
   *   <dd></dd>
  *   <dt>histograms</dt>
   *   <dd></dd>
   * </dl>
   * 
   * <h2>The canvas</h2>
   *
   * <p>The &lt;canvas&gt; element that is used to draw the graphic can 
   * either be supplied or generated by the PfamGraphic object. The parent 
   * element must be accessible in the DOM before calling 
   * <code>render()</code>.</p>
   * 
   * <h2>Dependencies</h2>
   *
   * <dl>
   *   <dt>Prototype</dt>
   *   <dd>This library is built on the <a href="http://prototypejs.org/">
   *     prototype</a> javascript library. Make sure that the source of the page 
   *     loads prototype before this library.</dd>
   *  
   *   <dt>Canvas-text</dt>
   *   <dd>The labels on domains are rendered using standard canvas text
   *     drawing calls, but these are only available as a native API in the most
   *     recent browsers (FF3.5, Safari 3, maybe others). Load the 
   *     <a href="http://code.google.com/p/canvas-text/">canvas-text</a>
   *     library before this file, in order to provide text functionality in 
   *     other browsers.</dd>
   *   
   *   <dt>Excanvas</dt>
   *   <dd>Graphics are rendered into an HTML5 &lt;canvas&gt; element. This 
   *     should work in most modern browsers, though Internet Explorer requires 
   *     the <a href="http://code.google.com/p/explorercanvas/">excanvas</a> 
   *     library too.</dd>
   * </dl>
   * 
   * <p>
   *   You can optionally include the 
   *   <a href="http://www.nickstakenburg.com/projects/prototip2/">prototip2</a>
   *   library and add a <code>metadata</code> item to each region in the 
   *   sequence object, to have tooltips added to the graphical elements.
   * </p>
   *  
   * <p>
   *
   * @description Builds a new PfamGraphic object. The parent element and sequence
   *   can be specified as arguments, or they can be set using setters. Both
   *   <strong>must</strong> be set before calling <code>render()</code>.
   * @constructs
   * @param {String|Element} [parent] the parent element
   * @param {Object} [sequence] the object describing the graphic
   */
  initialize: function( parent, sequence ) {
  
    // defaults for the image parameters
    this._imageParams = {

      // lollipop head dimensions
      headSizeCircle:  3,
      headSizeSquare:  6,
      headSizeDiamond: 4,
      headSizeArrow:   3,
      headSizePointer: 3,
      headSizeLine:    3,
     
      // padding (in pixels) for the ends of the graphic, so that lollipop heads 
      // are not truncated when the lollipop is close to one end of the 
      // sequence. This padding is added to both sides of the sequence when 
      // calculating the width of the canvas
      sequenceEndPadding: 2,
      
      // sets the X and Y offsets for drawing the graphic. By default, the 
      // graphic is drawing as close to the origin of the canvas as possible. 
      // Setting the offsets shifts the drawing origin for the graphics, 
      // meaning that it can be positioned away from the canvas origin, if 
      // required. Note that the drawing offsets are applied before the scale 
      // factors applied, so they are raw pixel values
      xOffset: 0,
      yOffset: 0,
      
      // parameters for adjusting the edges of domains
      defaultMarkupHeight:         20, // default height for a lollipop
      lollipopToLollipopIncrement: 7,  // step up each lollipop X pixels from the previous one
      bridgeToBridgeIncrement:     2,  // step up each bridge X pixels from the last bridge
      bridgeToLollipopIncrement:   5,  // step up each bridge X pixels from the last lollipop
      largeJaggedSteps:            6,  // number of steps on jagged edge (must be an even integer)

      // TODO make this configurable
      //font:   "sans",
      fontSize: "0.8em",

      // general image parameters
      regionHeight:    20,       // the height of a region
      motifHeight:     14,       // the height of a motif
      motifOpacity:    0.6,      // the opacity of a motif
      labelPadding:    3,        // padding for the text label on a region
      residueWidth:    0.5,      // number of pixels per residue
      xscale:          1.0,      // X-dimension scale factor
      yscale:          1.0,      // Y-dimension scale factor
      envOpacity:      0.6,      // opacity of the envelope regions
      highlightWeight: 1,        // line width of region highlight
      highlightColour: "#000000" // colour of region highlight line
    };

    // general options, specified as part of the "sequence"
    this._options = {
      baseUrl:   "",     // a URL to be prepended to hrefs when domains, motifs, etc are clicked
      imageMap:  true,   // add the image map ?
      labels:    true,   // add the text labels to regions ?
      tips:      true,   // add tooltips ? Requires prototip2
      tipStyle:  "pfam", // style for tooltips
      newCanvas: true    // generate a new canvas for each new sequence that's to
                         // be rendered
    };

    // specification of various allowed values in the input
    this._markupSpec = {
      valignValues:       $w( "top bottom" ),
      linesStyleValues:   $w( "mixed bold dashed" ),
      lollipopHeadValues: $w( "diamond circle square arrow pointer line" ),
      regionEndValues:    $w( "curved straight jagged arrow" )
    };

    // store the heights of the various drawing elements (only used in the 
    // context of a single rendering)
    this._heights = {};

    // somewhere to put <area> definitions for the domains and markups
    this._areasHash = new Hash();
    
    // somewhere to cache the calculated steps for jagged edges 
    this._cache = {};

    // check for a sequence object and a parent node
    if ( parent !== undefined ) {
      this.setParent( parent );
    }

    if ( sequence !== undefined ) {
      this.setSequence( sequence );
    }
    
    this._saveLevel = 0;
  },

  //----------------------------------------------------------------------------
  //- methods ------------------------------------------------------------------
  //----------------------------------------------------------------------------

  //----------------------------------------------------------------------------
  //- setters and getters ------------------------------------------------------
  //----------------------------------------------------------------------------
  /**
   * Sets the parent node for the <code>&lt;canvas&gt;</code> element that we 
   * create.
   *
   * @param {String|Element} parent the parent element, i.e. that element that 
   *   will contain the canvas. The element can be identified either by a string,
   *   giving the ID of the parent element, or as an reference to
   *   the parent element directly
   * @throws {PfamGraphicException} if a valid parent node is not specified
   * @returns {PfamGraphic} reference to this object
   */
  setParent: function( parent ) {
    this._parent = $(parent);

    if ( this._parent === undefined || this._parent === null ) {
      this._throw( "couldn't find the parent node" );
    }

    // console.log( "PfamGraphic.setParent: parent node ID: |%s|", 
    //   this._parent.identify() );

    return this;
  },

  //----------------------------------
  /**
   * Returns the parent node for the <code>&lt;canvas&gt;</code> element.
   *
   * @returns {Element} the parent element of the domain graphic
   */
  getParent: function() {
    return this._parent;
  },

  //----------------------------------------------------------------------------
  /**
   * Sets the <code>&lt;canvas&gt;</code> element for this graphic. 
   *
   * @param {String|Element} canvas the canvas element that should be used 
   *   for drawing the graphic. The canvas can be identified either by a string,
   *   giving the ID of the canvas element, or as an reference to
   *   the canvas element directly
   * @throws {PfamGraphicException} if a valid canvas is is not specified
   * @returns {PfamGraphic} reference to this object
   */
  setCanvas: function( canvas ) {
    this._canvas = $(canvas);

    if ( this._canvas === undefined || this._canvas === null ) {
      this._throw( "couldn't find the canvas node" );
    }

    // exit if canvas is not supported
    if ( this._canvas.getContext === undefined ) {
      this._throw( "canvas is not supported" );
    }

    this._context = this._canvas.getContext( "2d" );

    if ( this._context === undefined ) {
      this._throw( "couldn't create a 2d context from canvas" );
    }

    // we need to tie the areas to the canvases, so if we change canvas, we
    // also need to reset the areas list
    this._areasList = [];

    return this;
  },

  //----------------------------------
  /**
   * Returns the <code>&lt;canvas&gt;</code> element.
   *
   * @returns {Element} the canvas element that contains the domain graphic
   */
  getCanvas: function() {
    return this._canvas;
  },

  //----------------------------------------------------------------------------
  /**
   * Sets the image parameters.
   *
   * @param {Object} userImageParams user params
   * @returns {PfamGraphic} reference to this object
   */
  setImageParams: function( userImageParams ) {
    this._imageParams = Object.extend( this._imageParams, userImageParams );
    this._applyImageParams = true;
    return this;
  },

  //----------------------------------
  /**
   * Returns the image parameters.
   *
   * @returns {Object} the current image parameters
   */
  getImageParams: function() {
    return this._imageParams;
  },

  //----------------------------------------------------------------------------
  /**
   * Sets the flag that determines whether each new sequence will be rendered
   * into a new canvas.
   *
   * @param {boolean} newCanvas flag determining whether a new canvas should
   *   be used for each rendering
   * @returns {PfamGraphic} reference to this object
   */
  setNewCanvas: function( newCanvas ) {
    this._options.newCanvas = newCanvas;
    return this;
  },

  //----------------------------------
  /**
   * Returns the "new canvas" boolean flag.
   *
   * @see PfamGraphic#setNewCanvas
   * @returns {Object} the current value of the "new canvas" flag
   */
  getNewCanvas: function() {
    return this._options.newCanvas;
  },

  //----------------------------------------------------------------------------
  /**
   * Sets the base URL for all clicks on this graphic. The base URL is prepended
   * to what is presumed to be a relative URL on a region, motif, etc, so make
   * an absolute URL, so it must be a valid URL fragment.
   *
   * @param {String} url new value for the base URL
   * @returns {PfamGraphic} reference to this object
   */
  setBaseUrl: function( baseUrl ) {
    this._options.baseUrl = baseUrl;
    return this;
  },

  //----------------------------------
  /**
   * Returns the base URL.
   *
   * @returns {String} the current value of the base URL
   */
  getBaseUrl: function() {
    return this._options.baseUrl;
  },

  //----------------------------------------------------------------------------
  /**
   * Accepts the "sequence" object, containing all of the data needed to draw
   * the graphic into a new canvas.
   *
   * @param {Object} sequence data structure describing the graphic
   * @throws {PfamGraphicException} if sequence object is not valid
   * @returns {PfamGraphic} reference to this object
   */
  setSequence: function( sequence ) {

    // validate the sequence object

    // first, it has to be an object...
    if ( typeof sequence !== "object" ) {
      this._throw( "must supply a valid sequence object" );
    }

    // check that the sequence value makes sense
    if ( sequence.length === undefined ) {
      this._throw( "must specify a sequence length" );
    }

    if ( isNaN( sequence.length ) ) {
      this._throw( "sequence length must be a valid number" );
    }

    if ( parseInt( sequence.length, 10 ) <= 0 ) {
      this._throw( "sequence length must be a positive integer" );
    }

    //----------------------------------

    // check the "regions", "markups" and "motifs" sections of the sequence
    if ( sequence.regions !== undefined ) {
      if ( typeof sequence.regions !== "object" ) {
        this._throw( "'regions' must be a valid object" );
      }
    } else {

      // add an empty "regions" object, to keep later code happy
      sequence.regions = [];
    }

    if ( sequence.markups !== undefined ) {
      if ( typeof sequence.markups !== "object" ) {
        this._throw( "'markups' must be a valid object" );
      }
    } else {
      sequence.markups = [];
    }

    if ( sequence.motifs !== undefined ) {
      if ( typeof sequence.motifs !== "object" ) {
        this._throw( "'motifs' must be a valid object" );
      }
    } else {
      sequence.motifs = [];
    }

    if ( sequence.pileups !== undefined ) {
      if ( typeof sequence.pileups !== "object" ) {
        this._throw( "'pileups' must be a valid object" );
      }
    } else {
      sequence.pileups = [];
    }

    if ( sequence.histograms !== undefined ) {
      if ( typeof sequence.histograms !== "object" ) {
        this._throw( "'histograms' must be a valid object" );
      }
    } else {
      sequence.histograms = [];
    }

    //----------------------------------

    // see if any options were defined
    if ( sequence.options !== undefined ) {
      if ( typeof sequence.options !== "object" ) {
        this._throw( "'options' must be a valid object" );
      }
      this._options = Object.extend( this._options, sequence.options );
      // console.log( "PfamGraphic.setSequence: merged options: ", this._options );
    }

    //----------------------------------

    // see if any image parameters were defined
    if ( sequence.imageParams !== undefined ) {
      if ( typeof sequence.imageParams !== "object" ) {
        this._throw( "'imageParams' must be a valid object" );
      }
      this.setImageParams( sequence.imageParams );
      // console.log( "PfamGraphic.setSequence: merged imageParams: ", this._imageParams );
    }

    // everything passes. Stash it
    this._sequence = sequence;

    //----------------------------------

    // go through the sequence object and run anything that should be an integer
    // through "parseInt"
    this._walkSequence();

    // get the scale out of the image parameters
    // console.log( "PfamGraphic.setSequence: residue width, xscale / yscale: %d, %d / %d", 
    //   this._imageParams.residueWidth, this._imageParams.xscale, this._imageParams.yscale );
    
    // scale the length of the sequence and the weight of the domain image
    this._imageWidth = this._sequence.length * this._imageParams.residueWidth + 2;
    this._regionHeight = this._imageParams.regionHeight;

    // console.log( "PfamGraphic.setSequence: image width, domain height: %d, %d", 
    //   this._imageWidth, this._regionHeight );

    // set the height of the sequence line and the length of one "step" across
    // that line. The line is draw as a light band, a darker band and another
    // lighter one, to give the effect of a reflection

    // set the sequence line to one sixth the height of the domains
    this._seqHeight = Math.round( this._regionHeight / 6 );

    // we want 5 steps across the sequence line
    this._seqStep   = Math.round( this._seqHeight / 5 );    

    // console.log( "PfamGraphic.setSequence: seqHeight / seqStep: %d / %d", 
    //   this._seqHeight, this._seqStep );

    //----------------------------------

    // build the markups (lollipops, etc.). This will calculate the heights
    // of the various markup elements, without actually drawing them
    this._buildMarkups();

    // find the maximum of three values, in order to determine the top
    // extent (and then bottom extent) for the graphical elements:
    //   o maximum top extend for lollipops
    //   o maximum top extend for bridges
    //   o half the domain height

    // todo: include histograms & pileups in height calculation

    this._canvasHeight = [ this._heights.lollipops.upMax,
                           this._heights.bridges.upMax,
                           ( this._regionHeight / 2 + 1 ) ].max() +
                         [ this._heights.lollipops.downMax,
                           this._heights.bridges.downMax,
                           ( this._regionHeight / 2 + 1 ) ].max() + 1;
                         // that single pixel is just a fudge factor...

    // finally, scale the height by the specified factor
    this._canvasHeight *= this._imageParams.yscale;
    
    // if a highlight region is given, adjust the canvas height so that we
    // have room to add the highlight at the bottom. This is, currently, not
    // subject to the scaling factor, though that might need to change...
    if ( this._sequence.highlight !== undefined ) {
      this._canvasHeight += ( 5 + Math.ceil( this._imageParams.highlightWeight / 2 ) );
      // console.log( "PfamGraphic.render: got a highlight; canvasHeight now %d",
      //   this._canvasHeight );
    }

    // canvas width is just calculated from the length of the sequence (which 
    // is set in the "setSequence" method). We also add some padding at either 
    // end of the sequence, to allow for lollipop heads that might fall on the 
    // end-most residues
    this._canvasWidth = this._imageWidth + 1 + this._imageParams.sequenceEndPadding * 2;
    // console.log( "PfamGraphic.render: canvasWidth: %d", this._canvasWidth );
    
    // multiply the width by the specified horizontal scale factor
    this._canvasWidth *= this._imageParams.xscale;

    // set the baseline, relative to which the various elements will
    // actually be drawn
    this._baseline = [ this._heights.lollipops.upMax,
                       this._heights.bridges.upMax,
                       this._imageParams.regionHeight / 2 ].max() + 1;
                       // that single pixel is just a fudge factor...

    return this;
  },
  
  //----------------------------------
  /**
   * Returns the sequence object.
   *
   * @returns {Object} the sequence object
   */
  getSequence: function() {
    return this._sequence;
  },

  //----------------------------------------------------------------------------
  /**
   * Returns the calculated width and height of the domain graphic. This is
   * calculated as the dimensions of the canvas element that would be required 
   * to draw the whole graphic, taking into account image parameter settings 
   * such as <code>seqEndPadding</code>, but not the canvas offset parameter.
   * Values are accessible as elements of an array 
   * (<code>[ width, height ]</code>) or as values in an object
   * (<code>{ width: 100, height: 100 }</code>). If called before the sequence
   * object has been supplied, this method returns <code>null</code>.
   * <br />
   * <strong>Note</strong> that these value are calculated when a new sequence
   * object is added (e.g. via <code>setSequence()</code>). Because their values
   * are affected by the image parameters, you should adjust the image 
   * parameters, if necessary, <em>before</em> retrieving the dimensions.
   *
   * @returns {Object} array with two elements; first is width, second is 
   *   height
   */
  // TODO need to flag the image params as clean/dirty, so that we can 
  // recalculate the dimensions/mid-point of the graphic when requested,
  // rather than only when a new sequence is given
  getDimensions: function() {
    if ( this._canvasWidth  === undefined ||
         this._canvasHeight === undefined ) { 
      return null;
    }
    
    // console.log( "PfamGraphic.getDimensions: canvas (w, h): (%d, %d)",
    //   this._canvasWidth, this._canvasHeight );
    // console.log( "PfamGraphic.getDimensions: sequence end padding: %d",
    //   this._imageParams.sequenceEndPadding );
    // console.log( "PfamGraphic.getDimensions: xscale, yscale: %d x %d",
    //   this._imageParams.xscale, this._imageParams.yscale  );
    
    var dim = [ this._canvasWidth, this._canvasHeight ];
    dim.width  = this._canvasWidth;
    dim.height = this._canvasHeight;

    return dim;
  },

  //----------------------------------------------------------------------------
  /**
   * Returns the mid-point of the sequence line, i.e. the Y-coordinate of the 
   * middle of the domain graphic.
   * <br />
   * <strong>Note</strong> that this value is calculated when a new sequence
   * object is added (e.g. via <code>setSequence()</code>). Because its value
   * is affected by the image parameters, you should adjust the image 
   * parameters, if necessary, <em>before</em> retrieving this value.
   */
  getBaseline: function() {
    if ( this._baseline === undefined ) {
      return null;
    }
    
    return this._baseline;
  },

  //----------------------------------------------------------------------------
  /**
   * Returns a data structure that stores what is effectively &lt;area&gt; data 
   * for each of the regions on the sequence. Returns a list containing two
   * different versions of the same data: the first is a simple list of
   * the &lt;area&gt; objects, the second is a hash of the areas, keyed on the
   * domain/motif name.
   *
   * @returns {Object} array with two elements; first is an array of areas, 
   *   second is a hash
   */
  getAreas: function() {
    return [ this._areasList, this._areasHash ];
  },

  //----------------------------------------------------------------------------
  //- public methods -----------------------------------------------------------
  //----------------------------------------------------------------------------
  /** 
   * Coordinates the construction and drawing of the whole graphic.
   *
   * @param {String|Element} [parent] parent element for domain canvas
   * @param {Object} [sequence] sequence object
   * @throws {PfamGraphicException} if sequence or parent is not set either here
   *   or previously
   * @returns {PfamGraphic} reference to this object
   */
  render: function( parent, sequence ) {

    if ( sequence !== undefined ) {
      this.setSequence( sequence );
    }

    if ( parent !== undefined ) {
      this.setParent( parent );
    }

    // by this point, we need to have both a sequence object and a parent
    // node (to which we'll append the <canvas>)
    if ( this._sequence === undefined ) {
      this._throw( "sequence was not supplied" );
    }

    // we're going to try to build a canvas, so we need to have a parent node
    if ( this._options.newCanvas && 
         this._parent === undefined ) {
      this._throw( "parent node was not supplied" );
    }

    // if we don't yet have a <canvas>, build the one with the calculated
    // dimensions, adding the sequence end padding too
    if ( ( ! this._canvas ) || this._options.newCanvas ) {
      // console.log( "PfamGraphic.render: building canvas with width x height, baseline: %d x %d, %d",
      //   this._canvasWidth, this._canvasHeight, this._baseline );
      this._buildCanvas( this._canvasWidth, this._canvasHeight );
    }

    // draw everything
    this._draw();

    // we need to keep track of the parent element and the areas for each
    // canvas individually, so that we can correctly interpret mouseover
    // events and add tooltips properly. Store them in a class variable so 
    // that this (*should*) work even if there are multiple PfamGraphic
    // objects in operation
    this._canvases.set( this._canvas.identify(),
                        { "parentEl":    this._parent,
                          "areas":       this._areasList } );

    // add mouse event listeners
    this._addListeners();

    return this;
  }, // end of "render"

  //----------------------------------------------------------------------------
  //- private methods ----------------------------------------------------------
  //----------------------------------------------------------------------------

  /**
   * Throws a "PfamGraphicException" with the specified message. The exception 
   * object has a "toString" method attached.
   *
   * @param {String} message the error message to embed in the exception
   * @throws {PfamGraphicException} specified exception
   */
  _throw: function( message ) {
    throw { name: "PfamGraphicException",
            message: message,
            toString: function() { return this.message; } };
  },  
  
  //----------------------------------------------------------------------------
  /**
   * Walks the sequence object and runs <code>parseInt()</code> on the various
   * values that must be integers rather than strings.
   * 
   * @private
   */
  _walkSequence: function() {
    var s = this._sequence;
    s.length = this._parseInt( s.length );
    
    [ s.motifs, s.regions, s.markups ].each( function( j ) {
      $w( 'start end aliStart aliEnd' ).each( function( i ) {
        i = this._parseInt( i );
      }.bind( this ) );
    }.bind( this ) );

  },

  //----------------------------------------------------------------------------
  /**
   * Passes the supplied value through the native javascript 
   * <code>parseInt</code> function and returns the resulting value. If the 
   * input couldn't be parsed as an integer (so "parseInt" returns "NaN"), the
   * raw, unparsed value is returned instead.
   * 
   * @private
   * @param {String} value value to be parsed as an integer
   * @returns {int|String} parsed value (integer if possible, raw value 
   *   otherwise)
   */
  _parseInt: function( value ) {
    if ( value === undefined ) {
      return;
    }
    var num = parseInt( value, 10 );
    return ( num !== "NaN" ) ? num : value;
  },

  //----------------------------------------------------------------------------
  /**
   * Constructs a new <code>&lt;canvas&gt;</code> element for this graphic.
   *
   * @private
   * @param {int} width width of the required canvas 
   * @param {int} height height of the required canvas 
   */
  _buildCanvas: function( width, height ) {

    // for some reason, creating the <canvas> like this doesn't work:
    // 
    //   var canvas = new Element( "canvas", { width: width,
    //                                         height: height } );
    // 
    // instead, we need to use the plain old document.createElement and
    // then let prototype extend it:
    var canvas = document.createElement( "canvas" );
    Element.extend( canvas );

    // have to set the dimensions explicitly too
    canvas.width = width;
    canvas.height = height;

    // add the new <canvas> to the parent and generate an identifier for it
    this._parent.appendChild( canvas );
    canvas.identify();

    // make sure it gets initialised in bloody IE...
    // N.B. this "undefined" really needs the quotation marks !
    if ( typeof G_vmlCanvasManager !== "undefined" ) {
      canvas = G_vmlCanvasManager.initElement( canvas );
    }

    // since bloody IE (up to and including IE7) seems incapable of correctly
    // putting a scrollbar *below* the canvas content, we have to flag divs 
    // that have horizontal scrollbars, so that we can fix the problem by 
    // adding a padding value to the bottom of the div. Shouldn't this all be 
    // behind us by now ?
    //
    // walk up from the canvas parent until we get to a div and see how wide it
    // is. If it's narrower than the canvas, we need to add this extra class
    // to the parent, so that scrolling works everywhere
    var wrapperDiv = this._parent.up("div");
    if ( wrapperDiv && width > wrapperDiv.scrollWidth ) {
      this._parent.addClassName( "canvasScroller" );
    }

    // and stash it on the object
    this.setCanvas( canvas );
  },

  //----------------------------------------------------------------------------
  /**
   * Adds the mouse event listeners that will take care of interpretting 
   * mouseover events on the canvases, e.g. if the area has an href, we change
   * the cursor. If the tooltip library is loaded, this method also adds tooltips.
   *
   * @private
   */
  _addListeners: function() {

    // console.log( "PfamGraphic._addListeners: got %d areas", this._areasList.size() );
    // this._areasList.each( function( a ) {
    //   console.log( "PfamGraphic._addListeners: %d - %d", a.start, a.end ); 
    // } );

    // should we add tips ?
    var addTips = ( window.Prototip && this._options.tips );

    // are we inside or outside of an area ?
    this._inside = null;

    // add a listener for mouse movements over the canvas
    // console.log( "PfamGraphic._addListeners: adding listener to |%s|",
    //   this._canvas.identify() );
    this._canvas.observe( "mousemove", function( e ) {

      // find out where the event originated
      var activeCanvas = e.findElement("canvas");

      // retrieve the parent element and areas list for this particular canvas
      var canvasSettings = this._canvases.get( activeCanvas.identify() );
      var parentEl  = canvasSettings.parentEl;
      var areasList = canvasSettings.areas;

      // the offset coordinates of the canvas itself
      var offset = activeCanvas.cumulativeOffset();
      var cx = offset[0];
      var cy = offset[1];

      // take into account a scrolling offset
      // var sx = activeCanvas.getOffsetParent().scrollLeft;
      // var sy = activeCanvas.getOffsetParent().scrollTop;
      var sx = parentEl.scrollLeft;
      var sy = parentEl.scrollTop;
      // console.log( "PfamGraphic._addListeners: parentEl: %s, scrollTop: %d",
      //   parentEl.identify(), parentEl.scrollTop );

      // the area coordinates are all stored relative to the top-left corner
      // of the drawing area of the graphic, before the sequence end padding
      // value has been added, and before the X- and Y-scale factors have been 
      // applied. This means that the coordinates of the mouse event need to be
      // adjusted accordingly. 
      
      // TODO It might prove sensible to adjust the coordinates of the areas 
      // before they're stored, so that we don't have to do any calculations
      // for a mouse event.

      // the X-position of the event is adjusted to take into account the 
      // offset of the graphic due to the sequence end padding value and
      // the drawing offsets
      var ip = this._imageParams;
      var x = e.pointerX() - cx + sx - ip.sequenceEndPadding;
          y = e.pointerY() - cy + sy;
          activeArea = null;
      x /= this._imageParams.xscale;
      y /= this._imageParams.yscale;

      // see if we're in an area
      areasList.each( function( area ) {
        if ( x >= area.coords[0] && x <= area.coords[2] &&
             y >= area.coords[1] && y <= area.coords[3] ) {
          activeArea = area;
          // console.log( "PfamGraphic._addListeners: mouseover an area: ", area );
          throw $break;
        }
      } );

      if ( activeArea ) {
        // console.log( "PfamGraphic._addListeners: in an active area: ", activeArea );

        if ( this._inside !== activeArea ) {
          // console.log( "PfamGraphic._addListeners: in a NEW active area" );

          // we're in a new area
          this._inside = activeArea;

          if ( addTips && activeArea.tip ) {
            var opts =  { title: activeArea.tip.title,
                          stem: "topLeft" };
            if ( this._options.tipStyle ) {
              opts.style = this._options.tipStyle;
            }
            var t1 = new Tip(
              parentEl,
              activeArea.tip.body,
              opts
            );
          }

          // change the pointer if there's a link on this area
          if ( activeArea.href ) {
            var url;
            if ( this._options.baseUrl && ! this._options.baseUrl.empty() ) {
              url = this._options.baseUrl + activeArea.href;
            } else {
              url = activeArea.href;
            }

            activeCanvas.setStyle( { cursor: "pointer" } );
            window.status = url;
          }
        }

      } else {

        // console.log( "PfamGraphic._addListeners: not in an area" );

        // we aren't inside an area...
        if ( this._inside ) {

          activeCanvas.setStyle( { cursor: "default" } );

          this._inside = null;
          window.status = "";

          if ( parentEl.prototip ) {
            parentEl.prototip.remove();
          }
        }

      }

    }.bind( this ) );

    //----------------------------------

    // watch for clicks on areas with URLs
    this._canvas.observe( "click", function( e ) {
      this._handleClick( e );
    }.bind( this ) );

    if ( ! this._middleClickListenerAdded ) {
      Event.observe( window, "click", function( e ) {
        // we only want to handle middle clicks from the window
        if ( e.isMiddleClick() ) {
          this._handleClick( e );
        }
      }.bind( this ) );
      this._middleClickListenerAdded = true;
    }

  }, // end of "_addListeners"

  //----------------------------------------------------------------------------
  /**
   * Handles mouse click events. We first check if the click was made on a
   * canvas and, if not, we're done. If the click did originate on a canvas, we
   * walk through the areas that we have on the canvas and test whether the 
   * click falls within an area, before redirecting (if it was a left click) or
   * opening a new window (if it was a middle click).
   *
   * @private
   * @param {Object} e click <code>Event</code> object
   */
  _handleClick: function( e ) {
    // console.log( "PfamGraphic._handleClick: got a click on the canvas" );

    var clickedElement = e.findElement("canvas");
    var canvasId       = clickedElement.identify();
    var activeCanvas   = this._canvases.get( canvasId );

    // we're only interested in clicks on the canvas(es)
    if ( activeCanvas === undefined ) {
      return;
    }

    // we want to handle this event; don't let is go anywhere else, otherwise,
    // for a middle click at least, the browser will take over before we get
    // to do anything sensible
    e.stop();
    var areasList = activeCanvas.areas;

    // the offset coordinates of the canvas itself
    var offset = clickedElement.cumulativeOffset(),
        cx = offset[0],
        cy = offset[1]; 

    // get the location of the click and work out if it's inside any of 
    // the areas
    var x = e.pointerX() - cx,
        y = e.pointerY() - cy,
        activeArea = null;

    areasList.reverse().each( function( area ) {
      if ( x > area.coords[0] && x < area.coords[2] &&
           y > area.coords[1] && y < area.coords[3] ) {
        activeArea = area;
        throw $break;
      }  
    } );
    
    if ( activeArea && activeArea.href ) {
      var url;
      if ( this._options.baseUrl && ! this._options.baseUrl.empty() ) {
        url = this._options.baseUrl + activeArea.href;
      } else {
        url = activeArea.href;
      }

      if ( e.isMiddleClick() ) {
        window.open( url );
      } else {
        window.location = url;
      }
    }

  }, // end of "_handleClick"

  //----------------------------------------------------------------------------
  /**
   * Builds an in-memory representation of the markups (lollipops and bridges),
   * but doesn't actually draw them. The data structure built here contains
   * enough information to decide on the size of the <code>&lt;canvas&gt;</code>
   * element and, once that's built, we can render the elements into it.
   *
   * @private
   * @throws {PfamGraphicException} if there is an error in the markup description
   */
  _buildMarkups: function() {

    var heights = { lollipops: { up:   [],
                                 down: [],
                                 markups: [],
                                 downMax: 0,
                                 upMax: 0 },
                    bridges:   { up:   [],
                                 down: [],
                                 markups: [],
                                 downMax: 0,
                                 upMax: 0 } },
        bridgeMarkups   = [],
        ip              = this._imageParams,
        ms              = this._markupSpec;

    // console.log( "PfamGraphic._buildMarkups: assessing lollipops" );

    var orderedMarkups = [];
    this._sequence.markups.each( function( markup ) {

      var start = Math.floor( markup.start );
      if ( start === "NaN" ) {
        this._throw( "markup start position is not a number: '" + 
                     markup.start + "'" );
      }

      if ( orderedMarkups[markup.start] === undefined ) {
        orderedMarkups[markup.start] = [];
      }

      orderedMarkups[markup.start].push( markup );
    }.bind(this) );
    
    // flatten to get rid of nested arrays and then strip out slots with 
    // "undefined" as a value
    orderedMarkups = orderedMarkups.flatten().compact();
    
    // get the width of a residue. We need this when assessing whether lollipops
    // are overlapping
    var residueWidth = this._imageParams.residueWidth;

    // walk the markups, in order of start position, and build a map showing where
    // the lollipops are found
    orderedMarkups.each( function( markup ) {

      var start = Math.floor( markup.start );
      if ( start === "NaN" ) {
        this._throw( "markup start position is not a number: '" + 
               markup.start + "'" );
      }

      if ( markup.end === undefined ) {
        heights.lollipops.markups.push( markup ); // store as a lollipop
      } else {
        bridgeMarkups.push( markup );   // store as a bridge
        return; // equivalent to "next markup"
      }

      if ( markup.v_align !== undefined &&
           ! ms.valignValues.include( markup.v_align ) ) {
        this._throw( "markup 'v_align' value is not valid: '" + 
                     markup.v_align + "'" );
      }

      if ( markup.headStyle !== undefined &&
           ! ms.lollipopHeadValues.include( markup.headStyle ) ) {
        this._throw( "markup 'headStyle' value is not valid: '" + 
                     markup.headStyle + "'" );
      }

      // see if we're drawing on the top or the bottom
      var up = ( markup.v_align === undefined || markup.v_align === "top" );
      // console.log( "PfamGraphic._buildMarkups: up: %s", up );

      var h = up ? heights.lollipops.up : heights.lollipops.down;

      // check for an overlap with another lollipop (which was added previously)
      if ( h[ start - ( 1 / residueWidth ) ] !== undefined ||
           h[ start                        ] !== undefined ||
           h[ start + ( 1 / residueWidth ) ] !== undefined ) {

        var firstLollipopHeight = h.slice( start - ( 1 / residueWidth ), 
                                           start + ( 1 / residueWidth ) ).max();

        h[ start ] = firstLollipopHeight + ip.lollipopToLollipopIncrement;

        // console.log( "PfamGraphic._buildMarkups: duplicate markup at position %d, setting height to %d",
        //   start, h[start] );

      } else { 

        // console.log( "PfamGraphic._buildMarkups: no duplicate markup at position %d; using default height",
        //   start );
        h[start] = ip.defaultMarkupHeight;

      }

      var headSize = ip["headSize" + markup.headStyle.capitalize()];
      // console.log( "PfamGraphic._buildMarkups: head size for '%s': %d",
      //   markup.headStyle, headSize );

      if ( up ) {
        // maximum extent above the sequence line
        heights.lollipops.upMax = Math.max( h[start] + headSize,
                                            heights.lollipops.upMax );
      } else {
        // maximum extent below the sequence line
        heights.lollipops.downMax = Math.max( h[start] + headSize,
                                              heights.lollipops.downMax );
      }

      // console.log( "PfamGraphic._buildMarkup: max heights for lollipops: up/down: %d / %d",
      //   heights.lollipops.upMax, heights.lollipops.downMax );

    }.bind(this) );

    bridgeMarkups.each( function( bridgeMarkup ) {

      // the hash that stores the bridge parameters
      var bridge = { markup: bridgeMarkup };

      // we need to keep track of the markup for a bridge, but also its
      // calculated height and direction
      heights.bridges.markups.push( bridge );

      var start = Math.floor( bridgeMarkup.start );
      if ( start === "NaN" ) {
        this._throw( "bridge start position is not a number: '" + bridgeMarkup.start + "'" );
      }

      var end = Math.floor( bridgeMarkup.end );
      if ( end === "NaN" ) {
        this._throw( "bridge end position is not a number: '" + bridgeMarkup.end + "'" );
      }

      // console.log( "PfamGraphic._buildMarkups: checking bridge from %d to %d (+/- 1)",
      //   start, end );

      // see if we're drawing the current bridge on the top or the bottom
      bridge.up = ( bridgeMarkup.v_align === undefined || bridgeMarkup.v_align === "top" );
      var hl = bridge.up ? heights.lollipops.up : heights.lollipops.down,
          hb = bridge.up ? heights.bridges.up   : heights.bridges.down;

      // console.log( "PfamGraphic._buildMarkups: checking for overlapping bridges" );

      // find the maximum height of overlapping bridges
      var maxBridgeHeight = hb.slice( start, end ).flatten().max();
      var bridgeHeight = ip.defaultMarkupHeight;

      // set the height of the current bridge either to the default height or 
      // to the height of the previous highest bridge plus an increment
      if ( maxBridgeHeight === undefined ) {

        // console.log( "PfamGraphic._buildMarkups: no overlapping bridge; setting to default height (%d)",
        //   bridgeHeight );

      } else { 

        if ( hb.slice( start, end ).flatten().include( bridgeHeight ) ) {

          bridgeHeight = maxBridgeHeight + ip.bridgeToBridgeIncrement;

          // console.log( "PfamGraphic._buildMarkups: found overlapping bridge; setting bridge height to %d", 
          //   bridgeHeight );

        // } else { 
        //   console.log( "PfamGraphic._buildMarkups: no bridge at this height; leaving height at %d",
        //     bridgeHeight );
        }

      }

      // console.log( "PfamGraphic._buildMarkups: checking for overlapping lollipops" );

      // find the maximum height of overlapping lollipops (add a buffer to take into 
      // account the width of lollipop heads)
      var maxLollipopHeight = hl.slice( start - 4, end + 4 ).max();

      if ( maxLollipopHeight !== undefined ) {

        // console.log( "PfamGraphic._buildMarkups: found an overlapping lollipop, height %d", 
        //   maxLollipopHeight );

        if ( ( maxLollipopHeight + ip.bridgeToLollipopIncrement ) >= bridgeHeight ) {

          // bridgeHeight += ip.bridgeToLollipopIncrement;
          bridgeHeight = maxLollipopHeight + ip.bridgeToLollipopIncrement;

          // console.log( "PfamGraphic._buildMarkups: found an overlapping lollipop; bridge height reset to %d", 
          //   bridgeHeight );

        }

      // } else {

      //   console.log( "PfamGraphic._buildMarkups: no overlapping lollipop; bridge height unchanged, at %d", 
      //     bridgeHeight );
        
      }

      // check again for a bridge at this height. Increment the current bridge 
      // height if so
      // console.log( "PfamGraphic._buildMarkups: checking again for a bridge at this height (%d)",
      //   bridgeHeight );
      while ( hb.slice( start, end ).flatten().include( bridgeHeight ) ) {

        bridgeHeight += ip.bridgeToBridgeIncrement;
        
        // console.log( "PfamGraphic._buildMarkups: found overlapping bridge; setting bridge height to %d", 
        //   bridgeHeight );

      }

      // console.log( "PfamGraphic._buildMarkups: final bridge: range %d - %d, height %d",
      //   start, end, bridgeHeight );

      // store the calculated height on the bridge object
      bridge.height = bridgeHeight;

      // and set the height on the map
      $R( start, end ).each( function( pos ) { 
        if ( hb[pos] === undefined ) {
          hb[pos] = [];
        }
        hb[pos].push( bridgeHeight );
      } );

      if ( bridge.up ) {
        heights.bridges.upMax = Math.max( bridgeHeight, heights.bridges.upMax ) + 2;
      } else {
        heights.bridges.downMax = Math.max( bridgeHeight, heights.bridges.downMax ) + 2;
      }

      // console.log( "PfamGraphic._buildMarkup: max heights for bridges: %d / %d",
      //   heights.bridges.upMax, heights.bridges.downMax );

    }.bind(this) );

    // finally, push the data structure onto the object, to make it globally
    // accessible
    this._heights = heights;

  }, // end of "_buildMarkups"

  //----------------------------------------------------------------------------
  /**
   * Draws all of the graphical components.
   *
   * @private
   */
  _draw: function() {
    
    this._context.save();
        
    // start by translating everything by the sequence end padding value, and
    // applying the separate X- and Y-dimension scale factors to the context, so 
    // that we don't have to worry about it all the time when actually drawing

    if ( this._applyImageParams ) {

      var ip = this._imageParams; // just a shortcut...
      
      // console.log( "PfamGraphic._draw: setting drawing offsets: (%d, %d)",
      //   ip.xOffset, ip.yOffset );
      this._context.translate( ip.xOffset, ip.yOffset );
  
      // console.log( "PfamGraphic._draw: applying scale factors: %d x %d",
      //   ip.xscale, ip.yscale );
      this._context.scale( ip.xscale, ip.yscale );
  
      // console.log( "PfamGraphic._draw: applying sequence end padding: (%d, %d)",
      //   ip.sequenceEndPadding, 0 );
      this._context.translate( ip.sequenceEndPadding, 0 );
      
      this._applyImageParams = false;
    }
    
    // draw the sequence
    var seqArea = this._drawSequence();

    // draw the briges
    this._heights.bridges.markups.each( function( bridge ) {
      if ( bridge.display !== undefined &&
           bridge.display !== null &&
           ! bridge.display ) {
        return;
      }
      this._drawBridge( bridge );
    }.bind( this ) );

    // draw the lollipops after the bridges, so that the heads appear on top of
    // any overlapping bridges and draw them in reserve order, so that, where two
    // lollipops fall close to each other, the left-most lollipop is drawn above
    // the other
    this._heights.lollipops.markups.reverse().each( function( lollipop ) {
      if ( lollipop.display !== undefined &&
           lollipop.display !== null &&
           ! lollipop.display ) {
        return;
      }
      this._drawLollipop( lollipop );
    }.bind( this ) );

    // draw the regions
    this._sequence.regions.each( function( region ) {
      if ( region.display !== undefined && 
           region.display !== null && 
           ! region.display ) {
        return;
      }
      this._drawRegion( region );
    }.bind( this ) );

    // draw the motifs
    this._sequence.motifs.each( function( motif ) {
      if ( motif.display !== undefined && 
           motif.display !== null && 
           ! motif.display ) {
        return;
      }
      this._drawMotif( motif );
    }.bind( this ) );

    // draw the pileups
    this._sequence.pileups.each( function( pileup ) {
      this._drawPileup( pileup );
    }.bind( this ) );

    // draw the histogram scale
    var maxCount = this._sequence.histograms.max( function( histogram ) {
      return histogram.count;
    } );

    if ( maxCount ) {
      this._drawHistogramScale(maxCount);
    }

    // draw the histograms
    this._sequence.histograms.each( function( histogram ) {
      this._drawHistograms( histograms );
    }.bind( this ) );

    // draw the sequence scale
    // todo: create an option and new method to draw the sequence scale here

    // add the <area> details for the sequence last
    // this._areasList.push( seqArea );
    // Not adding this at the moment, though it could be pressed into service
    // if required...
  
    if ( this._sequence.highlight !== undefined &&
         parseInt( this._sequence.highlight.start, 10 ) && 
         parseInt( this._sequence.highlight.end, 10 ) ) {
      this._drawHighlight();
    }

    // restore the canvas state to what it was before we started drawing. This
    // effectively removes the offset due to the sequence end padding, and
    // resets the X and Y scale factors
    this._context.restore();
  },

  //----------------------------------------------------------------------------
  /**
   * Draws the basic ribbon, representing the sequence. Returns data for an
   * <code>&lt;area&gt;</code> tag, so that we can add it to the list of areas 
   * at the appropriate end of that list.
   *
   * @private
   * @returns {Object} data structure describing the <code>&lt;area&gt;</code>
   *   for the sequence line
   */
  _drawSequence: function() {

    // calculate "offsets" for the top and bottom of the sequence line and 
    // draw the rectangles (which make up the line) relative to that. Store the
    // two value in the object, so that we can use them when drawing markups too
    this._topOffset = Math.floor( this._baseline - ( this._seqHeight / 2 ) );
    this._botOffset = Math.floor( this._baseline + ( this._seqHeight / 2 ) + 1 );

    // console.log( "PfamGraphic._drawSequence: baseline: %d", this._baseline );
    // console.log( "PfamGraphic._drawSequence: calculated top offset as %d", this._topOffset );
    // console.log( "PfamGraphic._drawSequence: calculated bottom offset as %d", this._botOffset );

    // store the canvas state before we start drawing
    this._context.save();

    var gradient = this._context.createLinearGradient( 1, this._topOffset,
                                                       1, this._botOffset );

    gradient.addColorStop( 0,   "#999999" );
    gradient.addColorStop( 0.5, "#eeeeee" );
    gradient.addColorStop( 0.7, "#cccccc" );
    gradient.addColorStop( 1,   "#999999" );

    this._context.fillStyle = gradient;
    this._context.fillRect( 1,                    this._topOffset + 0.5,
                            this._imageWidth - 1, ( this._seqHeight / 2 ) * 3 );

    // todo:  ... or add a sequence scale here?

    // we're done drawing, so restore the canvas state
    this._context.restore();

    // need X- and Y-offsets
    var xo = this._imageParams.xOffset,
        yo = this._imageParams.yOffset;

    // add an area
    return { label:  "sequence", // TODO make this more informative...
             text:   "sequence",
             coords: [ xo,                    yo + this._topOffset, 
                       xo + this._imageWidth, yo + this._topOffset + this._seqStep * 5 ] };
  },

  //----------------------------------------------------------------------------
  /**
   * Draws an individual lollipop.
   *
   * @private
   * @param {Object} markup object containing description of the lollipop
   */
  _drawLollipop: function( markup ) {
    // console.log( "PfamGraphic._drawLollipop: start" );

    var start = markup.start,

        up = markup.v_align === undefined || markup.v_align === "top", 

        x1 = Math.floor( start * this._imageParams.residueWidth ) + 1.5,
        y1,
        y2;

    if ( up ) {
      // console.log( "PfamGraphic._drawLollipop: drawing lollipop on top at %d", start );
      y1 = Math.round( this._topOffset + 1 ) - 0.5;
      y2 = Math.floor( y1 - this._heights.lollipops.up[start] + ( this._baseline - this._topOffset ) + 1 );
    } else { 
      // console.log( "PfamGraphic._drawLollipop: drawing lollipop on bottom at %d", start );
      y1 = Math.round( this._botOffset + 1 ) - 0.5;
      y2 = Math.ceil( y1 + this._heights.lollipops.down[start] - ( this._botOffset - this._baseline ) - 1 );
    }

    // console.log( "PfamGraphic._drawLollipop: (x1, y1), (x1, y2): (%d, %d), (%d, %d)",
    //              x1, y1, x1, y2 );

    // store the canvas state before we start drawing
    this._context.save();
    
    this._context.beginPath();
    this._context.moveTo( x1, y1 );
    this._context.lineTo( x1, y2 );

    this._context.strokeStyle = markup.lineColour || "#000000";  
    this._context.stroke();
    this._context.closePath();
    
    // we're done drawing, so restore the canvas state
    this._context.restore();

    //----------------------------------
    
    // need X- and Y-offsets
    var xo = this._imageParams.xOffset,
        yo = this._imageParams.yOffset;

    // add an <area> for the stick (the head drawing function will add a separate 
    // <area> for the head)
    var ys = [ y1, y2 ].sort(function( a, b ) { return a - b; } );
    var area = { start:    start,
                 type:     "lollipop",
                 coords:   [ xo + Math.floor( x1 ) - 1, yo + ys[0] - 1, 
                             xo + Math.floor( x1 ) + 1, yo + ys[1] + 1 ] };
    this._areasList.push( area );

    // console.log( "PfamGraphic._drawLollipop: area coords: (%d, %d), (%d, %d)", 
    //   Math.floor( x1 ),     ys[0] - 1, 
    //   Math.floor( x1 ) + 2, ys[1] + 1 );

    // if there's a URL on the region, add it to the area
    if ( markup.href !== undefined ) {
      area.href = markup.href;
    }
    
    //----------------------------------
    
    // add tooltip data, if possible
    var tip = {};

    if ( markup.metadata ) {
      var md = markup.metadata;
    
        tip.title = ( md.type || "Annotation" ).capitalize();
        tip.body = 
          '<div class="tipContent">' +
          '  <dl>' +
          '    <dt>Description:</dt>' +
          '    <dd>' + md.description + '</dd>' +
          '    <dt>Position:</dt>' +
          '    <dd>' + md.start + '</dd>' +
          '    <dt>Source:</dt>' +
          '    <dd>' + ( md.database || '<span class="na">n/a</span>' ) + '</dd>' + 
          '  </dl>' +
          '</div>';
    }
    area.tip = tip;

    //----------------------------------
    
    // add the head
    if ( markup.headStyle ) {
      this._drawLollipopHead( x1, y1, y2, start, up, markup.headStyle, 
                              markup.colour, markup.lineColour, area.tip, 
                              markup.metadata );
    }

    // console.log( "PfamGraphic._drawLollipop: end" );
  },
 
  //----------------------------------------------------------------------------
  /**
   * Draws the head of a lollipop.
   *
   * @private
   * @param {int} x x-coordinate of lollipop (canvas coords)
   * @param {int} y1 y-coordinate of lollipop (canvas coords, end close to sequence)
   * @param {int} y2 y-coordinate of lollipop (canvas coords, end distant from sequence)
   * @param {int} start position of lollipop (residue number)
   * @param {boolean} up flag determining whether lollipop is drawn above or
   *   below the sequence line
   * @param {String} colour HTML color for lollipop head
   * @param {String} colour HTML color for lollipop stem
   * @param {Object} tip object with information for the tooltip
   * @param {Object} metadata object with metadata about the lollipop
   */
  _drawLollipopHead: function( x, y1, y2, start, up, style, colour, lineColour, tip, metadata ) {
    // console.log( "PfamGraphic._drawLollipopHead: starting to draw head |%s|", style );

    // need X- and Y-offsets
    var xo = this._imageParams.xOffset,
        yo = this._imageParams.yOffset,
        r,
        d;

    // store the canvas state before we start drawing
    this._context.save();

    switch ( style ) {

      case "circle":
        r = this._imageParams.headSizeCircle;
        // console.log( "PfamGraphic._drawLollipopHead: drawing circle" );
        this._context.beginPath();
        this._context.arc( x, y2, r, 0, (Math.PI * 2), "true" );
        this._context.fillStyle = colour || "red";
        this._context.fill();
        this._areasList.push( { tip:      tip,
                                type:     "lollipop-head",
                                shape:    "circle",
                                colour:   colour || "red",
                                start:    start,
                                coords:   [ xo + x - r, yo + y2 - r, 
                                            xo + x + r, yo + y2 + r ] } );
        break;

      case "square":
        d = this._imageParams.headSizeSquare / 2;
        // console.log( "PfamGraphic._drawLollipopHead: drawing square, edge |%d|, centred at %d x %d",
        //   this._imageParams.headSize.square, x, y2 );
        this._context.beginPath();
        this._context.moveTo( (x - d), (y2 - d) );
        this._context.lineTo( (x - d), (y2 + d) );
        this._context.lineTo( x + d, y2 + d );
        this._context.lineTo( x + d, y2 - d );
        this._context.lineTo( x - d, y2 - d );
        this._context.closePath();
        this._context.fillStyle = colour || "rgb(100, 200, 9)";
        this._context.fill();
        this._areasList.push( { tip:      tip,
                                type:     "lollipop-head",
                                start:    start,
                                colour:   colour || "rgb(100, 200, 9)",
                                coords:   [ xo + x - d, yo + y2 - d, 
                                            xo + x + d, yo + y2 + d ] } );
        break;

      case "diamond":
        d = this._imageParams.headSizeDiamond;
        // console.log( "PfamGraphic._drawLollipopHead: drawing diamond, extent |%d|, centred %d x %d",
        //   d, x, y2 );
        this._context.beginPath();
        this._context.moveTo( x - d, y2 );
        this._context.lineTo( x,     y2 + d );
        this._context.lineTo( x + d, y2 );
        this._context.lineTo( x,     y2 - d );
        this._context.lineTo( x - d, y2 );
        this._context.closePath();
        this._context.fillStyle = colour || "rgb(100, 200, 9)";
        this._context.fill();
        this._areasList.push( { tip:      tip,
                                ty2pe:     "lollipop-head",
                                shape:    "poly",
                                start:    start,
                                colour:   colour || "rgb(100, 200, 9)",
                                coords:   [ xo + x - d, yo + y2 - d, 
                                            xo + x + d, yo + y2 + d ] } );
        break;

      case "line":
        d = this._imageParams.headSizeLine;
        // console.log( "PfamGraphic._drawLollipopHead: drawing line, length |%d|, centred %d x %d", 
        //   d, x, y2 );
        this._context.beginPath();
        this._context.moveTo( x, y2 - d );
        this._context.lineTo( x, y2 + d );
        this._context.closePath();
        this._context.strokeStyle = colour || "rgb(50, 40, 255)";
        this._context.stroke();
        this._areasList.push( { tip:      tip,
                                type:     "lollipop-head",
                                start:    start,
                                colour:   colour || "rgb(50, 40, 255)",
                                coords:   [ xo + x - 1, yo + y2 - d - 1,
                                            xo + x + 1, yo + y2 + d + 1 ] } );
        break;

      case "arrow":
        d = this._imageParams.headSizeArrow;
        // console.log( "PfamGraphic._drawLollipopHead: drawing arrow, extent |%d|, centred %d x %d", 
        //   d, x, y2 );

        var coords;
        if ( up ) {
          this._context.beginPath();
          this._context.moveTo( x,     y2  );
          this._context.lineTo( x,     y2 - d );
          this._context.strokeStyle = lineColour || "#000000";  
          this._context.stroke();
          this._context.beginPath();
          this._context.moveTo( x - d, y2 + d * 0.5 );
          this._context.lineTo( x,     y2 - d );
          this._context.lineTo( x + d, y2 + d * 0.5 );
          coords = [ xo + x - d, yo + y2, 
                     xo + x + d, yo + y2 + d * 0.5 ];
        } else { 
          this._context.beginPath();
          this._context.moveTo( x,     y2  );
          this._context.lineTo( x,     y2 + d );
          this._context.strokeStyle = lineColour || "#000000";  
          this._context.stroke();
          this._context.beginPath();
          this._context.moveTo( x - d, y2 - d * 0.5 );
          this._context.lineTo( x,     y2 + d );
          this._context.lineTo( x + d, y2 - d * 1.5 );
          coords = [ xo + x - d, yo + y2 - d * 1.5, 
                     xo + x + d, yo + y2 - d ];
        }
        this._context.strokeStyle = colour || "rgb(50, 40, 255)";
        this._context.stroke();
        this._areasList.push( { tip:      tip,
                                type:     "lollipop-head",
                                colour:   colour || "rgb(50, 40, 255)",
                                start:    start,
                                shape:    "poly",
                                coords:   coords } );
        break;

      case "pointer":
        d = this._imageParams.headSizePointer;
        // console.log( "PfamGraphic._drawLollipopHead: drawing pointer, extent |%d|, centred %d x %d", 
        //   d, x, y2 );
        this._context.beginPath();

        var coords;
        if ( up ) {
          this._context.moveTo( x - d, y1 - d * 1.5 );
          this._context.lineTo( x,     y1     );
          this._context.lineTo( x + d, y1 - d * 1.5 );
          coords = [ xo + x - d, yo + y1, 
                     xo + x + d, yo + y1 - d ];
        } else { 
          this._context.moveTo( x - d, y1 + d * 1.5 );
          this._context.lineTo( x,     y1     );
          this._context.lineTo( x + d, y1 + d * 1.5 );
          coords = [ xo + x - d, yo + y1 + d, 
                     xo + x + d, yo + y1 ];
        }
        this._context.strokeStyle = colour || "rgb(50, 40, 255)";
        this._context.stroke();
        this._areasList.push( { tip:      tip,
                                type:     "lollipop-head",
                                colour:   colour || "rgb(50, 40, 255)",
                                start:    start,
                                shape:    "poly",
                                coords:   coords } );
        break;
    }

    // we're done drawing, so restore the canvas state
    this._context.restore();

    // console.log( "PfamGraphic._drawLollipopHead: done" );
  },

  //----------------------------------------------------------------------------
  /**
   * Draws a bridge.
   *
   * @private
   * @param {Object} bridge object with description of the bridge
   */
  _drawBridge: function( bridge ) {
    // console.log( "PfamGraphic._drawBridge: start" );

    // store the canvas state before we start drawing
    this._context.save();
  
    var start  = bridge.markup.start,
        end    = bridge.markup.end,
        height = bridge.height,
        up     = bridge.up,

        colour = "#000000",
        
        x1 = Math.floor( start * this._imageParams.residueWidth ) + 1.5,
        x2 = Math.floor( end   * this._imageParams.residueWidth ) + 1.5,
        y1 = Math.round( up ? this._topOffset : this._botOffset ) + 0.5,
        y2,
        label,

        xo = this._imageParams.xOffset, // need X- and Y-offsets
        yo = this._imageParams.yOffset;


    if ( up ) {
      // console.log( "PfamGraphic._drawBridge: drawing bridge on top at position %d", start );
      y2 = Math.ceil( this._baseline - height ) - 0.5;
    } else {
      // console.log( "PfamGraphic._drawBridge: drawing bridge on bottom at position %d", start );
      y2 = Math.floor( this._baseline + height ) + 0.5;
    }

    // console.log( "PfamGraphic._drawBridge: (x1, y1), (x2, y2): (%d, %d), (%d, %d)",
    //              x1, y1, x2, y2 );

    this._context.beginPath();
    this._context.moveTo( x1, y1 );
    this._context.lineTo( x1, y2 );
    this._context.lineTo( x2, y2 );
    this._context.lineTo( x2, y1 );

    this._context.strokeStyle = bridge.markup.colour;  
    this._context.stroke();
    this._context.closePath();

    // we're done drawing, so restore the canvas state
    this._context.restore();

    //----------------------------------

    // add tooltip data, if possible
    var tip = {};

    if ( bridge.markup.metadata ) {
      var md = bridge.markup.metadata;
    
        tip.title = ( md.type || "Bridge" ).capitalize();
        tip.body =
          '<div class="tipContent">' +
          '  <dl>' +
          '    <dt>Coordinates:</dt>' +
          '    <dd>' + md.start + '-' + md.end + '</dd>' +
          '    <dt>Source:</dt>' +
          '    <dd>' + ( md.database || '<span class="na">n/a</span>' ) + '</dd>' +
          '  </dl>' +
          '</div>';
    }

    //----------------------------------

    // add <area> tags for each of the legs and the horizontal
    var ys = [ y1, y2 ].sort(function( a, b ) { return a - b; } );
    this._areasList.push( { start:  start,
                            type:   "bridge-start",
                            colour: colour,
                            end:    end,
                            tip:    tip,
                            coords: [ xo + x1 - 1, yo + ys[0] - 1, 
                                      xo + x1 + 1, yo + ys[1] + 1 ] } );
    this._areasList.push( { start:  start,
                            type:   "bridge-horizontal",
                            colour: colour,
                            end:    end,
                            tip:    tip,
                            coords: [ xo + x1 - 1, yo + ys[0], 
                                      xo + x2 + 1, yo + ys[0] + 2 ] } );
    this._areasList.push( { start:  start,
                            type:   "bridge-end",
                            colour: colour,
                            end:    end,
                            tip:    tip,
                            coords: [ xo + x2 - 1, yo + ys[0] - 1, 
                                      xo + x2 + 1, yo + ys[1] + 1 ] } );

    // console.log( "PfamGraphic._drawBridge: end" );
  },

/*      {
         "lineColour" : "#ff0000",
         "colour" : "#aaaaaa",
         "end" : "228",
         "display" : true,
         "v_align" : "top",
         "type" : "disulphide",
         "metadata" : {
            "database" : "UniProt",
            "type" : "disulphide",
            "start" : "189",
            "end" : "228"
         },
         "start" : "189"
      },
*/
  //----------------------------------------------------------------------------
  /** 
   * Draws a region (most commonly a domain).
   * 
   * @private
   * @param {Object} region object with description of the region
   * @throws {PfamGraphicException} if the start or end styles are not valid
   */
  _drawRegion: function( region ) {
    // console.log( "PfamGraphic._drawRegion: drawing region..." );

    if ( ! this._markupSpec.regionEndValues.include( region.startStyle ) ) {
      this._throw( "region start style is not valid: '" + region.startStyle + "'" );
    }

    if ( ! this._markupSpec.regionEndValues.include( region.endStyle ) ) {
      this._throw( "region end style is not valid: '" + region.endStyle + "'" );
    }

    //----------------------------------

    // calculate dimensions for the inner shape
    var height = Math.floor( this._regionHeight ) - 2,
        radius = Math.round( height / 2 ),
        arrow  = radius,
        width  = ( region.end - region.start + 1 ) * this._imageParams.residueWidth,

        x = Math.max( 1, Math.floor( region.start * this._imageParams.residueWidth ) + 1 ),
        y = Math.floor( this._baseline - radius ) + 0.5,

        regionParams = {
          x: x, 
          y: y, 
          w: width, 
          h: height,
          r: radius,
          a: arrow,
          s: region.startStyle,
          e: region.endStyle
        };

    // console.log( "PfamGraphic._drawRegion: inner: (x, y), h, w: (%d, %d), %d, %d",
    //   x, y, height, width );

    //----------------------------------

    // the inner-most shell is filled, with a colour gradient running from white 
    // to dark to light colour as y increases. First draw the shell, then fill it

    // fill the path with a gradient
    var gradient = this._context.createLinearGradient( x, y, x, y + height );

     gradient.addColorStop( 0, "#ffffff" );
     gradient.addColorStop( 0.5, region.colour );
     gradient.addColorStop( 0.7, region.colour );
     gradient.addColorStop( 1, "#ffffff" ); // TODO make this a bit darker

    this._context.save();

    this._context.beginPath();

    this._buildRegionPath( regionParams );

    // fill the region
    this._context.fillStyle = gradient;
    this._context.fill();

    // add the envelope, if required. This call used to return the list of 
    // areas for the envelope regions, and might again one day...
    var areas;
    if ( region.aliStart !== undefined && region.aliEnd !== undefined ) { 
      areas = this._drawEnvelope( region, radius, height );
    }

    // outline the region
    this._context.strokeStyle = region.colour;
    this._context.stroke();

    this._context.closePath();

    //----------------------------------

    // this is an almight hack. There is a bug in firefox (upto and including 4.0b6)
    // which causes a context.strokeText() call to stroke not only the required
    // text but also the last path that was stroked. In our case, that means that 
    // when adding text to a region, the outline of the region also gets coloured
    // white. See bugzilla: https://bugzilla.mozilla.org/show_bug.cgi?id=478445
    //
    // One workaround for this bug is to allow the canvas.text.js library to 
    // over-ride the built-in firefox canvas text routines, but that hits a problem
    // with the canvas.text.js library...
    //
    // Adding this extra tiny path before trying to add text means that the 
    // strokeText call affects this sacrificial path rather than the outline of 
    // the region. 

    this._context.beginPath();
    this._context.moveTo( 0, this._topOffset );
    this._context.lineTo( 0, this._botOffset );
    this._context.strokeStyle = "white";
    this._context.stroke();
    this._context.closePath();

    //----------------------------------

    // add the text label
    if ( this._options.labels ) {
      this._drawText( x, this._baseline, width, region.text );
    }

    // we're done drawing, so restore the canvas state
    this._context.restore();

    //----------------------------------

    // build the area data

    // need X- and Y-offsets
    var xo = this._imageParams.xOffset,
        yo = this._imageParams.yOffset;

    var area = { text:     region.text,
                 type:     "region",
                 start:    region.start,
                 end:      region.end,
                 colour:   region.colour,
                 aliStart: region.aliStart,
                 aliEnd:   region.aliEnd,
                 coords:   [ xo + x,             yo + y, 
                             xo + x + width + 1, yo + y + height ] };
    this._areasList.push( area );
    this._areasHash.set( "region_" + region.text + "_" + region.start + "_" + region.end, area); 
    
    // if there's a URL on the region, add it to the area'
    if ( region.href !== undefined ) {
      area.href = region.href;
    }
    
    // add the information that we need to build a tooltip onto the area
    this._buildTip( region, area );

    // console.log( "PfamGraphic._drawRegion: done" );
  }, // end of "_drawRegion"

  //----------------------------------------------------------------------------
  /**
   * Builds the two fields ("title" and "body") that are required on an area, in
   * order for a tooltip to be constructed. A single property, "tip", is added
   * to the area that's passed in. Should work for both regions and motifs.
   *
   * @private
   * @param {Object} item description of the item (region or motif) that the
   *   tip is associated with
   * @param {Object} area description of the &lt;area&gt; tag for this tip
   */
  _buildTip: function( item, area ) {

    // if there's some metadata, we can build the tooltip
    if ( item.metadata === undefined ) {
      // console.log( "PfamGraphic._buildTip: no meta data; not building tooltip data" );
      return; 
    }

    var md = item.metadata;
    
    var tipTitle;
    if ( md.accession !== undefined && md.identifier !== undefined ) {
      tipTitle = md.identifier + " (" + md.accession.toUpperCase() + ")";
    } else if ( md.identifier !== undefined ) {
      tipTitle = md.identifier;
    } else if ( md.accession !== undefined ) {
      tipTitle = md.accession.toUpperCase();
    } else {
      tipTitle = md.type;
    }

    var coords = '<span class="inactive">n/a</span>';
    if ( md.start !== undefined && md.end !== undefined ) {
      coords = md.start + " - " + md.end;
      if ( md.aliStart !== undefined && md.aliEnd !== undefined ) {
        coords = coords.concat( " (alignment region " + md.aliStart + " - " + md.aliEnd + ")" );
      }
    }

    var tipBody = 
      '<div class="tipContent">' +
      '  <dl>' +
      '    <dt>Description:</dt>' +
      '    <dd>' + ( md.description || '<span class="inactive">n/a</span>' ) + '</dd>' +
      '    <dt>Coordinates:</dt>' +
      '    <dd>' + coords + '</dd>' +
      '    <dt>Source:</dt>' +
      '    <dd>' + ( md.database || '<span class="na">n/a</span>' ) + '</dd>' +
      '  </dl>' +
      '</div>';
    // console.log( "PfamGraphic._buildTip: tip title:    ", tipTitle );
    // console.log( "PfamGraphic._buildTip: tip contents: ", tipBody );

    area.tip = { title: tipTitle,
                 body:  tipBody };
  },

  //----------------------------------------------------------------------------
  /**
   * Draws a motif.
   * 
   * @param {Object} motif description of the motif
   * @throws {PfamGraphicException} if the colour is not valid
   */
  _drawMotif: function( motif ) {
    // console.log( "PfamGraphic._drawMotif: motif: ", motif );

    motif.start = parseInt( motif.start, 10 );
    motif.end   = parseInt( motif.end,   10 );
    
    // work out the dimensions
    var height = this._imageParams.motifHeight,
        width  = Math.floor( ( motif.end - motif.start + 1 ) * this._imageParams.residueWidth ) + 1,
        x = Math.max( 1, Math.floor( ( motif.start + 1 ) * this._imageParams.residueWidth ) ),
        y = Math.floor( this._baseline - Math.round( height / 2 ) );

    // console.log( "PfamGraphic._drawMotif: (x, y), h, w: (%d, %d), %d, %d",
    //   x, y, height, width );

    //----------------------------------

    // save the current state of the canvas
    this._context.save();

    // use the same slot for either a single or multiple colours
    var motifColour;
    
    // decide what we're drawing, based on the number of colours we're given
    if ( motif.colour instanceof Array ) {

      // Pfam-B

      // first, make sure we have a sensible number of colours to play with...
      if ( motif.colour.length !== 3 ) {
        this._throw( "motifs must have either one or three colours" );
      }

      // convert the colours from hex strings into "rgba()" values
      colour = [];
  
      var getRGBColour = this._getRGBColour.bind( this ),
          ip           = this._imageParams;
  
      motif.colour.each( function( c ) {
        var rgbColour = getRGBColour( c );
        colour.push( { rgb:  "rgb("  + rgbColour.join(",") + ")",
                       rgba: "rgba(" + rgbColour.join(",") + "," + ip.motifOpacity + ")" } );
      } );
  
      // draw the three stripes
      var step   = Math.round( height / 3 );
      for ( var i = 0; i < 3; i = i + 1 ) {
        this._context.fillStyle = colour[i].rgb;
        this._context.fillRect( x, y + ( step * i ), width, step );
      }
      
    } else {

      // regular "motif"

      // convert the colour from a hex string into an "rgba()" value
      colour = this._getRGBColour( motif.colour );
      var rgb  = "rgb(" + colour.join(",") + ")";
      var rgba = "rgba(" + colour.join(",") + "," + this._imageParams.motifOpacity + ")";
  
      // draw the rectangle
      this._context.fillStyle = rgba;
      this._context.fillRect( x, y, width, parseInt( height, 10 ) + 1 );
  
    }

    // restore the canvas state
    this._context.restore();

    //----------------------------------

    // add the area
    var label;
    if ( motif.metadata            !== undefined &&
         motif.metadata.identifier !== undefined ) {
      label = motif.metadata.identifier;
    } else if ( motif.text !== undefined ) {
      label = motif.text;
    } else {
      label = "motif, " + motif.start + " - " + motif.end;
    }
      
    // need X- and Y-offsets
    var xo = this._imageParams.xOffset,
        yo = this._imageParams.yOffset;

    var area = { text:   label,
                 type:   "motif",
                 start:  motif.aliStart || motif.start,
                 end:    motif.aliEnd   || motif.end,
                 colour: colour,
                 coords: [ xo + x,         yo + y,  
                           xo + x + width, yo + y + height ] };
    this._areasList.push( area );
    this._areasHash.set( "motif_" + label + "_" + motif.start + "_" + motif.end, area );

    // if there's a URL on the region, add it to the area'
    if ( motif.href !== undefined ) {
      area.href = motif.href;
    }
    
    // add the information that we need to build a tooltip onto the area
    this._buildTip( motif, area );

    // console.log( "PfamGraphic._drawMotif: done" );
  }, // end of "_drawMotif"

  //----------------------------------------------------------------------------
  /**
   * Builds the path for constructing regions. The path can be used either as
   * an outline for filling or stroking.
   *
   * @private
   * @param {Object} params details of the region (x, y, radius, height, etc )
   */
  _buildRegionPath: function( params ) {

    // console.log( "PfamGraphic._buildRegionPath: drawing left end" );
    switch ( params.s ) {
      case "curved":
        this._context.moveTo( params.x + params.r, params.y );
        this._drawLeftRounded( params.x, params.y, params.r, params.h );
        break;
      case "jagged":
        this._context.moveTo( params.x, params.y );
        this._drawJagged( params.x, params.y, params.h, true );
        break;
      case "straight":
        this._context.moveTo( params.x, params.y );
        this._context.lineTo( params.x, params.y + params.h );
        break;
      case "arrow":
        this._context.moveTo( params.x + params.a, params.y );
        this._drawLeftArrow( params.x, params.y, params.a, params.h );
        break;
    }

    // bottom line and right hand edge 
    // console.log( "PfamGraphic._buildRegionPath: drawing bottom line and right end" );
    switch ( params.e ) {
      case "curved":
        this._context.lineTo( params.x + params.w - params.r, params.y + params.h );
        this._drawRightRounded( params.x, params.y, params.r, params.h, params.w );
        break;
      case "jagged":
        this._context.lineTo( params.x + params.w, params.y + params.h );
        this._drawJagged( params.x + params.w, params.y + params.h, params.h, false );
        break;
      case "straight":
        this._context.lineTo( params.x + params.w, params.y + params.h );
        this._context.lineTo( params.x + params.w, params.y );
        break;
      case "arrow":
        this._context.lineTo( params.x + params.w - params.a, params.y + params.h );
        this._drawRightArrow( params.x + params.w - params.a, params.y + params.h, params.a, params.h );
        break;
    }

    // top horizontal line
    // console.log( "PfamGraphic._buildRegionPath: drawing top line" );
    if ( params.s === "curved" || 
         params.s === "arrow" ) {
      this._context.lineTo( params.x + params.r, params.y );
    } else {
      this._context.lineTo( params.x, params.y );
    }

  }, // end of "_buildRegionPath"

  //----------------------------------------------------------------------------
  /**
   * Draws semi-transparent overlays to represent the envelope regions around
   * the core alignment.
   *
   * @private
   * @param {Object} region description of the region
   * @param {int} radius radius of the region end
   * @param {int} height height of the region
   * @throws {PfamGraphicException} if the start/end coordinates are not valid
   */  
  _drawEnvelope: function( region, radius, height ) {
    // console.log( "PfamGraphic._drawEnvelope: adding envelope overlay" );

    // TODO handle the case where there's an aliStart but no aliEnd given

    // make sure the endpoints are sensible
    if ( parseInt( region.start, 10 ) > parseInt( region.aliStart, 10 ) ) {
      this._throw( "regions must have start <= aliStart (" + region.start + " is > " + region.aliStart + ")" );
    }

    if ( parseInt( region.end, 10 ) < parseInt( region.aliEnd, 10 ) ) {
      this._throw( "regions must have end >= aliEnd (" + region.end + " is < " + region.aliEnd + ")" );
    }

    //----------------------------------

    var y  = this._baseline - radius,
        xs = this._imageParams.residueWidth,
        l,
        r;
        
    if ( region.aliStart && 
         region.aliStart > region.start ) {
      l = { x: Math.floor( region.start * xs ),
            y: Math.floor( y - 1 ) + 1,
            w: Math.floor( region.aliStart * xs ) - Math.floor( region.start * xs ) + 1,
            h: height + 1 };
    }
              
    if ( region.aliEnd && 
         region.aliEnd < region.end ) {
      r = { x: Math.floor( region.aliEnd * xs ) + 1,
            y: Math.floor( y - 1 ) + 1,
            w: Math.floor( region.end * xs ) - Math.floor( region.aliEnd * xs ),
            h: height + 1 };
    }

    this._context.save();

    // clip the envelope regions to the existing canvas content, so that we 
    // restrict the shading to, for example, the true edges of the arrow head
    this._context.globalCompositeOperation = "source-atop";
    // N.B. not implemented in excanvas and therefore IE

    // the intended fillStyle
    var fillStyle = "rgba(255,255,255," + this._imageParams.envOpacity + ")";
    this._context.fillStyle = fillStyle;

    if ( l !== undefined ) {    
      // console.log( "PfamGraphic._drawEnvelope: left region: (x, y, w, h): (%d, %d, %d, %d)",
      //   l.x, l.y, l.w, l.h );
      this._context.fillRect( l.x, l.y, l.w, l.h );
    }

    if ( r !== undefined ) {
      // console.log( "PfamGraphic._drawEnvelope: left region: (x, y, w, h): (%d, %d, %d, %d)",
      //   r.x, r.y, r.w, r.h );
      this._context.fillRect( r.x, r.y, r.w, r.h );
    }

    this._context.restore();

    // console.log( "PfamGraphic._drawEnvelope: added envelope(s)" );

    //----------------------------------

    // add two <area> tags for the envelope; one at the start, one at the end

    // add the areas
/*    var startArea, endArea;

    if ( l !== undefined ) {
      // console.log( "PfamGraphic._drawEnvelope: got a left region; adding area" );

      startArea = { text:   region.text,
                    start:  region.start,
                    end:    region.aliStart,
                    coords: [ l.x, l.y, l.x + l.w, l.y + l.h ] };
      
      if ( region.href !== undefined ) {
        startArea.href = region.href;
      }
    }
    
    if ( r !== undefined ) {
      // console.log( "PfamGraphic._drawEnvelope: got a right region; adding area" );

      endArea = { text:   region.text,
                  start:  region.aliEnd,
                  end:    region.end,
                  coords: [ r.x, r.y, r.x + r.w, r.y + r.h ] };

      if ( region.href !== undefined ) {
        endArea.href = region.href;
      }
    }
    
    return [ startArea, endArea ];
*/  
  }, // end of "_drawEnvelope"
  
  //----------------------------------------------------------------------------
  /**
   * Adds the text label to the domain, if it will fit nicely inside the shape.
   *
   * @private
   * @param {int} x x-coordinate of the region (canvas coords)
   * @param {int} midpoint the midpoint of the region (canvas coords)
   * @param {int} regionWidth width of the region (canvas coords)
   * @param {String} text text to be added as the label
   */
  _drawText: function( x, midpoint, regionWidth, text ) {

    this._context.save();

    // set up the font
    // this._context.font         = "bold " + this._imageParams.fontSize + " 'optimer'";
    this._context.font         = "bold 12px 'optimer'";
    this._context.textAlign    = "center";
    this._context.textBaseline = "middle";

    // calculate the width of the text to be rendered
    var metrics = this._context.measureText( text );

    // console.log( "PfamGraphic._drawText: textBaseline: %d", this._context.textBaseline );

    // pad the text a little and then compare that to the width of the region,
    // so that we can assess whether it's going to fit inside the region when
    // rendered
    /* var paddedTextWidth = metrics.width + 2 * this._imageParams.labelPadding; */
    var paddedTextWidth  = metrics.width  + Math.round( this._regionHeight / 3 );
    var paddedTextHeight = metrics.height;

    // console.log( "PfamGraphic._drawText: padded text width:  %d, region width:  %d",
    //   paddedTextWidth, regionWidth );
    // console.log( "PfamGraphic._drawText: padded text height: %d, region height: %d",
    //   paddedTextHeight, this._regionHeight );

    if ( paddedTextWidth > regionWidth ) {
      // console.log( "PfamGraphic._drawText: text is wider than region; not adding" );
      this._context.restore();
      return;
    }
    if ( paddedTextHeight > this._regionHeight ) {
      // console.log( "PfamGraphic._drawText: text is taller than region; not adding" );
      this._context.restore();
      return;
    }

    var textX = x + ( regionWidth / 2 );
    // console.log( "PfamGraphic._drawText: region X, midpoint: (%d, %d); textX: %d", 
    //   x, midpoint, textX );

    // stroke the outline in white...
    this._context.lineWidth   = 2;
    this._context.strokeStyle = "#eeeeee";
    this._context.strokeText( text, textX, midpoint );

    // ... and then fill in black
    this._context.fillStyle = "#000000";
    this._context.fillText( text, textX, midpoint );

    this._context.restore();
  },

  //----------------------------------------------------------------------------
  /**
   * Draws the left-hand end of region with a curved end.
   *
   * @private
   * @param {int} x x-coordinate of the region (canvas coords)
   * @param {int} y y-coordinate of the region (canvas coords)
   * @param {int} radius radius of the region
   * @param {int} height height of the region
   */
  _drawLeftRounded: function( x, y, radius, height ) {
    this._context.quadraticCurveTo( x, y, x, y + radius );
    this._context.quadraticCurveTo( x, y + height, x + radius, y + height );
  },

  //----------------------------------------------------------------------------
  /**
   * Draws the right-hand end of region with a curved end.
   *
   * @private
   * @param {int} x x-coordinate of the region (canvas coords)
   * @param {int} y y-coordinate of the region (canvas coords)
   * @param {int} radius radius of the region
   * @param {int} height height of the region
   * @param {int} width width of the region
   */
  _drawRightRounded: function( x, y, radius, height, width ) {
    this._context.quadraticCurveTo( x + width, y + height, x + width, y + radius );
    this._context.quadraticCurveTo( x + width, y , x + width - radius, y );
  },

  //----------------------------------------------------------------------------
  /**
   * Generates a jagged end, either left or right, depending on the value of 
   * the "left" argument (either true or false).
   *
   * @private
   * @param {int} x x-coordinate of the region (canvas coords)
   * @param {int} y y-coordinate of the region (canvas coords)
   * @param {boolean} left whether this is the left edge of the region
   */
  _drawJagged: function( x, y, height, left ) {

    // make sure we have an even number of steps
    var steps = parseInt( this._imageParams.largeJaggedSteps, 10 );
    steps += steps % 2;

    // get the list of Y-coords
    var yShifts = this._getSteps( height, steps );

    // the step size, in pixels. This is used when stepping on X
    var step = height / steps;

    // console.log( "PfamGraphic._drawJagged: (x, y), height, steps, left: (%d, %d), %d, %d, %s",
    //   x, y, height, steps, left );

    for ( var i = 0; i < yShifts.length; i = i + 1 ) {
      // odd; outer vertices
      if ( i % 2 !== 0 ) {
        if ( left ) {
          this._context.lineTo( x, y + yShifts[i] );
        } else {
          this._context.lineTo( x, y - yShifts[i] );
        }
      }
      // even; inner vertices
      else {
        if ( left ) {
          this._context.lineTo( x + step, y + yShifts[i] );
        } else {
          this._context.lineTo( x - step, y - yShifts[i] );
        }
      }
    }

    // close the path
    if ( left ) {
      this._context.lineTo( x, y + height );
    } else {
      this._context.lineTo( x, y - height );
    }
  },

  //----------------------------------------------------------------------------
  /**
   * Generates a list of Y-axis coordinates for a jagged end. The list will 
   * be cached for this combination of height and number of steps.
   *
   * @private
   * @param {int} height the height of the domain (canvas coords)
   * @param {int} steps number of steps in the jagged edge
   * @returns {Array} list of Y-coordinates for vertices on the edge
   */
  _getSteps: function( height, steps ) {

    var cacheKey = "shifts_" + height + "_" + steps;
    var list = this._cache[cacheKey];

    if ( list === undefined ) {

      // the "period" of the step, in pixels
      var step = height / steps;

      // walk out from the mid-line and add Y-axis coords to an array
      var yShifts = [];
      for ( var i = 0; i < ( steps / 2 ); i = i + 1 ) {
        yShifts.push( ( height / 2 ) - ( i * step ) );
        yShifts.push( ( height / 2 ) + ( i * step ) );
      }

      // uniquify and (numerically) sort the list of Y-coords
      list = yShifts.uniq().sort( function (a, b) { return a - b; } );

      // cache the list for later
      this._cache[cacheKey] = list;
    }

    return list;
  },

  //----------------------------------------------------------------------------
  /**
   * Draws the left-hand end of a region as an arrow head.
   *
   * @private
   * @param {int} x x-coordinate of the region (canvas coords)
   * @param {int} y y-coordinate of the region (canvas coords)
   */
  _drawLeftArrow: function( x, y, arrow, height ) {
    this._context.lineTo( x, y + arrow );
    this._context.lineTo( x + arrow, y + height );
  },

  //----------------------------------------------------------------------------
  /**
   * Draws the right-hand end of a region as an arrow head.
   *
   * @private
   * @param {int} x x-coordinate of the region (canvas coords)
   * @param {int} y y-coordinate of the region (canvas coords)
   * @param {int} arrow the width of the arrow
   * @param {int} height height of the region
   */
  _drawRightArrow: function( x, y, arrow, height ) {
    this._context.lineTo( x + arrow, y - height + arrow );
    this._context.lineTo( x, y - height );
  },

  //----------------------------------------------------------------------------
  /**
   * Draws the region highlight
   *
   * @private
   */
  _drawHighlight: function() {

    // need X- and Y-offsets
    var xo = this._imageParams.xOffset,
        yo = this._imageParams.yOffset,

        lineThicknessOffset = Math.round( this._imageParams.highlightWeight / 2 ),

        left   = Math.floor( xo + this._sequence.highlight.start * this._imageParams.residueWidth ),
        right  = Math.ceil(  yo + this._sequence.highlight.end   * this._imageParams.residueWidth ),
        top    = this._canvasHeight - 4 - lineThicknessOffset,
        bottom = this._canvasHeight - 2;

    if ( this._imageParams.highlightWeight % 2 ) {
      // console.log( "PfamGraphic._drawHighlight: line width is an odd number of pixels (%d); adjusting offsets",
      //   this._imageParams.highlightWeight );
      left   -= 0.5;
      right  -= 0.5;
      bottom -= 0.5;
    }

    // console.log( "PfamGraphic._drawHighlight: moveTo( x, y ): ( %d, %d )", left, top );
    // console.log( "PfamGraphic._drawHighlight: lineTo( x, y ): ( %d, %d )", left, bottom );
    // console.log( "PfamGraphic._drawHighlight: lineTo( x, y ): ( %d, %d )", right, bottom );
    // console.log( "PfamGraphic._drawHighlight: lineTo( x, y ): ( %d, %d )", right, top );

    this._context.save();

    this._context.beginPath();
    this._context.strokeStyle = this._imageParams.highlightColour;
    this._context.lineWidth   = this._imageParams.highlightWeight;
    this._context.moveTo( left, top );
    this._context.lineTo( left, bottom );
    this._context.lineTo( right, bottom );
    this._context.lineTo( right, top );
    this._context.stroke();

    this._context.restore();

    //----------------------------------
    
    // add an <area> and associated tooltip
    var area = { start:  this._sequence.highlight.start,
                 end:    this._sequence.highlight.end,
                 coords: [ left,  top,
                           right, bottom ],
                 tip:    { title: this._sequence.highlight.text || "Highlighted region",
                           body:   '<div class="tipContent">' +
                                   '  <dl>' +
                                   '    <dt>Start:</dt>' +
                                   '    <dd>' + this._sequence.highlight.start + '</dd>' +
                                   '    <dt>End:</dt>' +
                                   '    <dd>' + this._sequence.highlight.end + '</dd>' +
                                   '  </dl>' +
                                   '</div>' } };
    this._areasList.push( area );

    // console.log( "PfamGraphic._drawHighlight: area coords: (%d, %d), (%d, %d)", 
    //   left, top, right, bottom );
  },

      _drawPileup: function ( pileup ) {
          // todo
      },

      _drawHistogramScale: function ( maxCount ) {
          // todo
      },

      _drawHistogram: function ( histogram ) {
          // todo
      },

  //----------------------------------------------------------------------------
  /**
   * Converts a hex string (eg "#07874f") into an RGB triplet (eg [ 7, 135, 79 ]).
   * RGB values are in the range 0 - 255. Returns an array containing the RGB values,
   * which are also available as { r: RED, g: GREEN, b: BLUE }. Also handles three
   * digit hex strings, e.g. "#FC0". Does not distinguish between "#FC0" and "#fc0".
   *
   * (these two complementary methods were taken originally from
   * "http://www.linuxtopia.org/online_books/javascript_guides/javascript_faq/rgbtohex.htm")
   *
   * @private
   * @param {String} hexString HTML colour value
   * @returns {Object} RGB colour values
   * @throws {PfamGraphicException} if the colour is not valid
   */
  _getRGBColour: function( hexString ) {

    var longHexMatches  = /^#?([A-F0-9]{6})$/i.exec( hexString ),
        shortHexMatches = /^#?([A-F0-9]{3})$/i.exec( hexString ),
        h, r, g, b, rgb;

    if ( longHexMatches === null && shortHexMatches === null ) {
      this._throw( "not a valid hex colour ('" + hexString + "')" );
    }

    if ( longHexMatches !== null ) {
      h = longHexMatches[1];
      r = parseInt( h.substring( 0, 2 ), 16 );
      g = parseInt( h.substring( 2, 4 ), 16 );
      b = parseInt( h.substring( 4, 6 ), 16 );
    } else if ( shortHexMatches !== null ) {
      h = shortHexMatches[1];
      r = parseInt( "" + h.substring( 0, 1 ) + h.substring( 0, 1 ), 16 );
      g = parseInt( "" + h.substring( 1, 2 ) + h.substring( 1, 2 ), 16 );
      b = parseInt( "" + h.substring( 2, 3 ) + h.substring( 2, 3 ), 16 );
    }

    rgb = [ r, g, b ];
    rgb.r = r;
    rgb.g = g;
    rgb.b = b;

    return rgb;
  },

  //----------------------------------------------------------------------------
  /**
   * Converts an RGB triplet (eg 7, 135, 79) into a hex colour (eg #07874f").
   * The RGB values must be in the range 0 - 255, but they can be given as
   * a hash (e.g. { r: 7, g: 135, b: 79 }), an array (e.g. [7, 135, 79]) or as
   * individual values (e.g. 7, 135, 79). The hex value is returned with a leading
   * hash (#). RGB values are silently clamped, individually, to the range 0-255
   * and are rounded to the nearest integer value.
   *
   * @private
   * @param {int} red red value 
   * @param {int} green greenvalue 
   * @param {int} blue blue value 
   * @returns {String} RGB colour string
   * @throws {PfamGraphicException} if the colour is not valid
   */
  _getHexColour: function( red, green, blue ) {

    var r, g, b;

    if ( red.shift ) {
      // console.log( "PfamGraphic._getHexColour: got an array" );
      r = red[0];
      g = red[1];
      b = red[2];
    } else if ( red.r !== undefined &&
                red.g !== undefined &&
                red.b !== undefined ) {
      // console.log( "PfamGraphic._getHexColour: got a hash" );
      r = red.r;
      g = red.g;
      b = red.b;
    } else {
      // console.log( "PfamGraphic._getHexColour: looking for individual values" );
      r = red;
      g = green;
      b = blue;
    }

    var rgbColour = [ r, g, b ].collect( function( x ) {
      if ( x === undefined ) {
        this._throw( "need all three RGB colour values" );
      }
      if ( isNaN( x ) ) {
        this._throw( "failed to get a valid RGB colour triplet" );
      }
      x = parseInt( x, 10 );
      x = Math.max( 0, x );
      x = Math.min( x, 255 );
      return x;
    } );

    var hex = rgbColour.collect( function( x ) {
      return "0123456789abcdef".charAt( ( x - x % 16 ) / 16 ) +
             "0123456789abcdef".charAt( x % 16 );
    } ).join( "" );

    // console.log( "PfamGraphic._getHexColour: converted (%d, %d, %d) to '%s'",
    //   r, g, b, hex );

    return "#" + hex;
  }

  //----------------------------------------------------------------------------

} );


