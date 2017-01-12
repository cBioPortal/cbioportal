/*!
 * Modernizr v2.8.3
 * www.modernizr.com
 *
 * Copyright (c) Faruk Ates, Paul Irish, Alex Sexton
 * Available under the BSD and MIT licenses: www.modernizr.com/license/
 */

/*
 * Modernizr tests which native CSS3 and HTML5 features are available in
 * the current UA and makes the results available to you in two ways:
 * as properties on a global Modernizr object, and as classes on the
 * <html> element. This information allows you to progressively enhance
 * your pages with a granular level of control over the experience.
 *
 * Modernizr has an optional (not included) conditional resource loader
 * called Modernizr.load(), based on Yepnope.js (yepnopejs.com).
 * To get a build that includes Modernizr.load(), as well as choosing
 * which tests to include, go to www.modernizr.com/download/
 *
 * Authors        Faruk Ates, Paul Irish, Alex Sexton
 * Contributors   Ryan Seddon, Ben Alman
 */

window.Modernizr = (function( window, document, undefined ) {

    var version = '2.8.3',

    Modernizr = {},

    /*>>cssclasses*/
    // option for enabling the HTML classes to be added
    enableClasses = true,
    /*>>cssclasses*/

    docElement = document.documentElement,

    /**
     * Create our "modernizr" element that we do most feature tests on.
     */
    mod = 'modernizr',
    modElem = document.createElement(mod),
    mStyle = modElem.style,

    /**
     * Create the input element for various Web Forms feature tests.
     */
    inputElem /*>>inputelem*/ = document.createElement('input') /*>>inputelem*/ ,

    /*>>smile*/
    smile = ':)',
    /*>>smile*/

    toString = {}.toString,

    // TODO :: make the prefixes more granular
    /*>>prefixes*/
    // List of property values to set for css tests. See ticket #21
    prefixes = ' -webkit- -moz- -o- -ms- '.split(' '),
    /*>>prefixes*/

    /*>>domprefixes*/
    // Following spec is to expose vendor-specific style properties as:
    //   elem.style.WebkitBorderRadius
    // and the following would be incorrect:
    //   elem.style.webkitBorderRadius

    // Webkit ghosts their properties in lowercase but Opera & Moz do not.
    // Microsoft uses a lowercase `ms` instead of the correct `Ms` in IE8+
    //   erik.eae.net/archives/2008/03/10/21.48.10/

    // More here: github.com/Modernizr/Modernizr/issues/issue/21
    omPrefixes = 'Webkit Moz O ms',

    cssomPrefixes = omPrefixes.split(' '),

    domPrefixes = omPrefixes.toLowerCase().split(' '),
    /*>>domprefixes*/

    /*>>ns*/
    ns = {'svg': 'http://www.w3.org/2000/svg'},
    /*>>ns*/

    tests = {},
    inputs = {},
    attrs = {},

    classes = [],

    slice = classes.slice,

    featureName, // used in testing loop


    /*>>teststyles*/
    // Inject element with style element and some CSS rules
    injectElementWithStyles = function( rule, callback, nodes, testnames ) {

      var style, ret, node, docOverflow,
          div = document.createElement('div'),
          // After page load injecting a fake body doesn't work so check if body exists
          body = document.body,
          // IE6 and 7 won't return offsetWidth or offsetHeight unless it's in the body element, so we fake it.
          fakeBody = body || document.createElement('body');

      if ( parseInt(nodes, 10) ) {
          // In order not to give false positives we create a node for each test
          // This also allows the method to scale for unspecified uses
          while ( nodes-- ) {
              node = document.createElement('div');
              node.id = testnames ? testnames[nodes] : mod + (nodes + 1);
              div.appendChild(node);
          }
      }

      // <style> elements in IE6-9 are considered 'NoScope' elements and therefore will be removed
      // when injected with innerHTML. To get around this you need to prepend the 'NoScope' element
      // with a 'scoped' element, in our case the soft-hyphen entity as it won't mess with our measurements.
      // msdn.microsoft.com/en-us/library/ms533897%28VS.85%29.aspx
      // Documents served as xml will throw if using &shy; so use xml friendly encoded version. See issue #277
      style = ['&#173;','<style id="s', mod, '">', rule, '</style>'].join('');
      div.id = mod;
      // IE6 will false positive on some tests due to the style element inside the test div somehow interfering offsetHeight, so insert it into body or fakebody.
      // Opera will act all quirky when injecting elements in documentElement when page is served as xml, needs fakebody too. #270
      (body ? div : fakeBody).innerHTML += style;
      fakeBody.appendChild(div);
      if ( !body ) {
          //avoid crashing IE8, if background image is used
          fakeBody.style.background = '';
          //Safari 5.13/5.1.4 OSX stops loading if ::-webkit-scrollbar is used and scrollbars are visible
          fakeBody.style.overflow = 'hidden';
          docOverflow = docElement.style.overflow;
          docElement.style.overflow = 'hidden';
          docElement.appendChild(fakeBody);
      }

      ret = callback(div, rule);
      // If this is done after page load we don't want to remove the body so check if body exists
      if ( !body ) {
          fakeBody.parentNode.removeChild(fakeBody);
          docElement.style.overflow = docOverflow;
      } else {
          div.parentNode.removeChild(div);
      }

      return !!ret;

    },
    /*>>teststyles*/

    /*>>mq*/
    // adapted from matchMedia polyfill
    // by Scott Jehl and Paul Irish
    // gist.github.com/786768
    testMediaQuery = function( mq ) {

      var matchMedia = window.matchMedia || window.msMatchMedia;
      if ( matchMedia ) {
        return matchMedia(mq) && matchMedia(mq).matches || false;
      }

      var bool;

      injectElementWithStyles('@media ' + mq + ' { #' + mod + ' { position: absolute; } }', function( node ) {
        bool = (window.getComputedStyle ?
                  getComputedStyle(node, null) :
                  node.currentStyle)['position'] == 'absolute';
      });

      return bool;

     },
     /*>>mq*/


    /*>>hasevent*/
    //
    // isEventSupported determines if a given element supports the given event
    // kangax.github.com/iseventsupported/
    //
    // The following results are known incorrects:
    //   Modernizr.hasEvent("webkitTransitionEnd", elem) // false negative
    //   Modernizr.hasEvent("textInput") // in Webkit. github.com/Modernizr/Modernizr/issues/333
    //   ...
    isEventSupported = (function() {

      var TAGNAMES = {
        'select': 'input', 'change': 'input',
        'submit': 'form', 'reset': 'form',
        'error': 'img', 'load': 'img', 'abort': 'img'
      };

      function isEventSupported( eventName, element ) {

        element = element || document.createElement(TAGNAMES[eventName] || 'div');
        eventName = 'on' + eventName;

        // When using `setAttribute`, IE skips "unload", WebKit skips "unload" and "resize", whereas `in` "catches" those
        var isSupported = eventName in element;

        if ( !isSupported ) {
          // If it has no `setAttribute` (i.e. doesn't implement Node interface), try generic element
          if ( !element.setAttribute ) {
            element = document.createElement('div');
          }
          if ( element.setAttribute && element.removeAttribute ) {
            element.setAttribute(eventName, '');
            isSupported = is(element[eventName], 'function');

            // If property was created, "remove it" (by setting value to `undefined`)
            if ( !is(element[eventName], 'undefined') ) {
              element[eventName] = undefined;
            }
            element.removeAttribute(eventName);
          }
        }

        element = null;
        return isSupported;
      }
      return isEventSupported;
    })(),
    /*>>hasevent*/

    // TODO :: Add flag for hasownprop ? didn't last time

    // hasOwnProperty shim by kangax needed for Safari 2.0 support
    _hasOwnProperty = ({}).hasOwnProperty, hasOwnProp;

    if ( !is(_hasOwnProperty, 'undefined') && !is(_hasOwnProperty.call, 'undefined') ) {
      hasOwnProp = function (object, property) {
        return _hasOwnProperty.call(object, property);
      };
    }
    else {
      hasOwnProp = function (object, property) { /* yes, this can give false positives/negatives, but most of the time we don't care about those */
        return ((property in object) && is(object.constructor.prototype[property], 'undefined'));
      };
    }

    // Adapted from ES5-shim https://github.com/kriskowal/es5-shim/blob/master/es5-shim.js
    // es5.github.com/#x15.3.4.5

    if (!Function.prototype.bind) {
      Function.prototype.bind = function bind(that) {

        var target = this;

        if (typeof target != "function") {
            throw new TypeError();
        }

        var args = slice.call(arguments, 1),
            bound = function () {

            if (this instanceof bound) {

              var F = function(){};
              F.prototype = target.prototype;
              var self = new F();

              var result = target.apply(
                  self,
                  args.concat(slice.call(arguments))
              );
              if (Object(result) === result) {
                  return result;
              }
              return self;

            } else {

              return target.apply(
                  that,
                  args.concat(slice.call(arguments))
              );

            }

        };

        return bound;
      };
    }

    /**
     * setCss applies given styles to the Modernizr DOM node.
     */
    function setCss( str ) {
        mStyle.cssText = str;
    }

    /**
     * setCssAll extrapolates all vendor-specific css strings.
     */
    function setCssAll( str1, str2 ) {
        return setCss(prefixes.join(str1 + ';') + ( str2 || '' ));
    }

    /**
     * is returns a boolean for if typeof obj is exactly type.
     */
    function is( obj, type ) {
        return typeof obj === type;
    }

    /**
     * contains returns a boolean for if substr is found within str.
     */
    function contains( str, substr ) {
        return !!~('' + str).indexOf(substr);
    }

    /*>>testprop*/

    // testProps is a generic CSS / DOM property test.

    // In testing support for a given CSS property, it's legit to test:
    //    `elem.style[styleName] !== undefined`
    // If the property is supported it will return an empty string,
    // if unsupported it will return undefined.

    // We'll take advantage of this quick test and skip setting a style
    // on our modernizr element, but instead just testing undefined vs
    // empty string.

    // Because the testing of the CSS property names (with "-", as
    // opposed to the camelCase DOM properties) is non-portable and
    // non-standard but works in WebKit and IE (but not Gecko or Opera),
    // we explicitly reject properties with dashes so that authors
    // developing in WebKit or IE first don't end up with
    // browser-specific content by accident.

    function testProps( props, prefixed ) {
        for ( var i in props ) {
            var prop = props[i];
            if ( !contains(prop, "-") && mStyle[prop] !== undefined ) {
                return prefixed == 'pfx' ? prop : true;
            }
        }
        return false;
    }
    /*>>testprop*/

    // TODO :: add testDOMProps
    /**
     * testDOMProps is a generic DOM property test; if a browser supports
     *   a certain property, it won't return undefined for it.
     */
    function testDOMProps( props, obj, elem ) {
        for ( var i in props ) {
            var item = obj[props[i]];
            if ( item !== undefined) {

                // return the property name as a string
                if (elem === false) return props[i];

                // let's bind a function
                if (is(item, 'function')){
                  // default to autobind unless override
                  return item.bind(elem || obj);
                }

                // return the unbound function or obj or value
                return item;
            }
        }
        return false;
    }

    /*>>testallprops*/
    /**
     * testPropsAll tests a list of DOM properties we want to check against.
     *   We specify literally ALL possible (known and/or likely) properties on
     *   the element including the non-vendor prefixed one, for forward-
     *   compatibility.
     */
    function testPropsAll( prop, prefixed, elem ) {

        var ucProp  = prop.charAt(0).toUpperCase() + prop.slice(1),
            props   = (prop + ' ' + cssomPrefixes.join(ucProp + ' ') + ucProp).split(' ');

        // did they call .prefixed('boxSizing') or are we just testing a prop?
        if(is(prefixed, "string") || is(prefixed, "undefined")) {
          return testProps(props, prefixed);

        // otherwise, they called .prefixed('requestAnimationFrame', window[, elem])
        } else {
          props = (prop + ' ' + (domPrefixes).join(ucProp + ' ') + ucProp).split(' ');
          return testDOMProps(props, prefixed, elem);
        }
    }
    /*>>testallprops*/


    /**
     * Tests
     * -----
     */

    // The *new* flexbox
    // dev.w3.org/csswg/css3-flexbox

    tests['flexbox'] = function() {
      return testPropsAll('flexWrap');
    };

    // The *old* flexbox
    // www.w3.org/TR/2009/WD-css3-flexbox-20090723/

    tests['flexboxlegacy'] = function() {
        return testPropsAll('boxDirection');
    };

    // On the S60 and BB Storm, getContext exists, but always returns undefined
    // so we actually have to call getContext() to verify
    // github.com/Modernizr/Modernizr/issues/issue/97/

    tests['canvas'] = function() {
        var elem = document.createElement('canvas');
        return !!(elem.getContext && elem.getContext('2d'));
    };

    tests['canvastext'] = function() {
        return !!(Modernizr['canvas'] && is(document.createElement('canvas').getContext('2d').fillText, 'function'));
    };

    // webk.it/70117 is tracking a legit WebGL feature detect proposal

    // We do a soft detect which may false positive in order to avoid
    // an expensive context creation: bugzil.la/732441

    tests['webgl'] = function() {
        return !!window.WebGLRenderingContext;
    };

    /*
     * The Modernizr.touch test only indicates if the browser supports
     *    touch events, which does not necessarily reflect a touchscreen
     *    device, as evidenced by tablets running Windows 7 or, alas,
     *    the Palm Pre / WebOS (touch) phones.
     *
     * Additionally, Chrome (desktop) used to lie about its support on this,
     *    but that has since been rectified: crbug.com/36415
     *
     * We also test for Firefox 4 Multitouch Support.
     *
     * For more info, see: modernizr.github.com/Modernizr/touch.html
     */

    tests['touch'] = function() {
        var bool;

        if(('ontouchstart' in window) || window.DocumentTouch && document instanceof DocumentTouch) {
          bool = true;
        } else {
          injectElementWithStyles(['@media (',prefixes.join('touch-enabled),('),mod,')','{#modernizr{top:9px;position:absolute}}'].join(''), function( node ) {
            bool = node.offsetTop === 9;
          });
        }

        return bool;
    };


    // geolocation is often considered a trivial feature detect...
    // Turns out, it's quite tricky to get right:
    //
    // Using !!navigator.geolocation does two things we don't want. It:
    //   1. Leaks memory in IE9: github.com/Modernizr/Modernizr/issues/513
    //   2. Disables page caching in WebKit: webk.it/43956
    //
    // Meanwhile, in Firefox < 8, an about:config setting could expose
    // a false positive that would throw an exception: bugzil.la/688158

    tests['geolocation'] = function() {
        return 'geolocation' in navigator;
    };


    tests['postmessage'] = function() {
      return !!window.postMessage;
    };


    // Chrome incognito mode used to throw an exception when using openDatabase
    // It doesn't anymore.
    tests['websqldatabase'] = function() {
      return !!window.openDatabase;
    };

    // Vendors had inconsistent prefixing with the experimental Indexed DB:
    // - Webkit's implementation is accessible through webkitIndexedDB
    // - Firefox shipped moz_indexedDB before FF4b9, but since then has been mozIndexedDB
    // For speed, we don't test the legacy (and beta-only) indexedDB
    tests['indexedDB'] = function() {
      return !!testPropsAll("indexedDB", window);
    };

    // documentMode logic from YUI to filter out IE8 Compat Mode
    //   which false positives.
    tests['hashchange'] = function() {
      return isEventSupported('hashchange', window) && (document.documentMode === undefined || document.documentMode > 7);
    };

    // Per 1.6:
    // This used to be Modernizr.historymanagement but the longer
    // name has been deprecated in favor of a shorter and property-matching one.
    // The old API is still available in 1.6, but as of 2.0 will throw a warning,
    // and in the first release thereafter disappear entirely.
    tests['history'] = function() {
      return !!(window.history && history.pushState);
    };

    tests['draganddrop'] = function() {
        var div = document.createElement('div');
        return ('draggable' in div) || ('ondragstart' in div && 'ondrop' in div);
    };

    // FF3.6 was EOL'ed on 4/24/12, but the ESR version of FF10
    // will be supported until FF19 (2/12/13), at which time, ESR becomes FF17.
    // FF10 still uses prefixes, so check for it until then.
    // for more ESR info, see: mozilla.org/en-US/firefox/organizations/faq/
    tests['websockets'] = function() {
        return 'WebSocket' in window || 'MozWebSocket' in window;
    };


    // css-tricks.com/rgba-browser-support/
    tests['rgba'] = function() {
        // Set an rgba() color and check the returned value

        setCss('background-color:rgba(150,255,150,.5)');

        return contains(mStyle.backgroundColor, 'rgba');
    };

    tests['hsla'] = function() {
        // Same as rgba(), in fact, browsers re-map hsla() to rgba() internally,
        //   except IE9 who retains it as hsla

        setCss('background-color:hsla(120,40%,100%,.5)');

        return contains(mStyle.backgroundColor, 'rgba') || contains(mStyle.backgroundColor, 'hsla');
    };

    tests['multiplebgs'] = function() {
        // Setting multiple images AND a color on the background shorthand property
        //  and then querying the style.background property value for the number of
        //  occurrences of "url(" is a reliable method for detecting ACTUAL support for this!

        setCss('background:url(https://),url(https://),red url(https://)');

        // If the UA supports multiple backgrounds, there should be three occurrences
        //   of the string "url(" in the return value for elemStyle.background

        return (/(url\s*\(.*?){3}/).test(mStyle.background);
    };



    // this will false positive in Opera Mini
    //   github.com/Modernizr/Modernizr/issues/396

    tests['backgroundsize'] = function() {
        return testPropsAll('backgroundSize');
    };

    tests['borderimage'] = function() {
        return testPropsAll('borderImage');
    };


    // Super comprehensive table about all the unique implementations of
    // border-radius: muddledramblings.com/table-of-css3-border-radius-compliance

    tests['borderradius'] = function() {
        return testPropsAll('borderRadius');
    };

    // WebOS unfortunately false positives on this test.
    tests['boxshadow'] = function() {
        return testPropsAll('boxShadow');
    };

    // FF3.0 will false positive on this test
    tests['textshadow'] = function() {
        return document.createElement('div').style.textShadow === '';
    };


    tests['opacity'] = function() {
        // Browsers that actually have CSS Opacity implemented have done so
        //  according to spec, which means their return values are within the
        //  range of [0.0,1.0] - including the leading zero.

        setCssAll('opacity:.55');

        // The non-literal . in this regex is intentional:
        //   German Chrome returns this value as 0,55
        // github.com/Modernizr/Modernizr/issues/#issue/59/comment/516632
        return (/^0.55$/).test(mStyle.opacity);
    };


    // Note, Android < 4 will pass this test, but can only animate
    //   a single property at a time
    //   goo.gl/v3V4Gp
    tests['cssanimations'] = function() {
        return testPropsAll('animationName');
    };


    tests['csscolumns'] = function() {
        return testPropsAll('columnCount');
    };


    tests['cssgradients'] = function() {
        /**
         * For CSS Gradients syntax, please see:
         * webkit.org/blog/175/introducing-css-gradients/
         * developer.mozilla.org/en/CSS/-moz-linear-gradient
         * developer.mozilla.org/en/CSS/-moz-radial-gradient
         * dev.w3.org/csswg/css3-images/#gradients-
         */

        var str1 = 'background-image:',
            str2 = 'gradient(linear,left top,right bottom,from(#9f9),to(white));',
            str3 = 'linear-gradient(left top,#9f9, white);';

        setCss(
             // legacy webkit syntax (FIXME: remove when syntax not in use anymore)
              (str1 + '-webkit- '.split(' ').join(str2 + str1) +
             // standard syntax             // trailing 'background-image:'
              prefixes.join(str3 + str1)).slice(0, -str1.length)
        );

        return contains(mStyle.backgroundImage, 'gradient');
    };


    tests['cssreflections'] = function() {
        return testPropsAll('boxReflect');
    };


    tests['csstransforms'] = function() {
        return !!testPropsAll('transform');
    };


    tests['csstransforms3d'] = function() {

        var ret = !!testPropsAll('perspective');

        // Webkit's 3D transforms are passed off to the browser's own graphics renderer.
        //   It works fine in Safari on Leopard and Snow Leopard, but not in Chrome in
        //   some conditions. As a result, Webkit typically recognizes the syntax but
        //   will sometimes throw a false positive, thus we must do a more thorough check:
        if ( ret && 'webkitPerspective' in docElement.style ) {

          // Webkit allows this media query to succeed only if the feature is enabled.
          // `@media (transform-3d),(-webkit-transform-3d){ ... }`
          injectElementWithStyles('@media (transform-3d),(-webkit-transform-3d){#modernizr{left:9px;position:absolute;height:3px;}}', function( node, rule ) {
            ret = node.offsetLeft === 9 && node.offsetHeight === 3;
          });
        }
        return ret;
    };


    tests['csstransitions'] = function() {
        return testPropsAll('transition');
    };


    /*>>fontface*/
    // @font-face detection routine by Diego Perini
    // javascript.nwbox.com/CSSSupport/

    // false positives:
    //   WebOS github.com/Modernizr/Modernizr/issues/342
    //   WP7   github.com/Modernizr/Modernizr/issues/538
    tests['fontface'] = function() {
        var bool;

        injectElementWithStyles('@font-face {font-family:"font";src:url("https://")}', function( node, rule ) {
          var style = document.getElementById('smodernizr'),
              sheet = style.sheet || style.styleSheet,
              cssText = sheet ? (sheet.cssRules && sheet.cssRules[0] ? sheet.cssRules[0].cssText : sheet.cssText || '') : '';

          bool = /src/i.test(cssText) && cssText.indexOf(rule.split(' ')[0]) === 0;
        });

        return bool;
    };
    /*>>fontface*/

    // CSS generated content detection
    tests['generatedcontent'] = function() {
        var bool;

        injectElementWithStyles(['#',mod,'{font:0/0 a}#',mod,':after{content:"',smile,'";visibility:hidden;font:3px/1 a}'].join(''), function( node ) {
          bool = node.offsetHeight >= 3;
        });

        return bool;
    };



    // These tests evaluate support of the video/audio elements, as well as
    // testing what types of content they support.
    //
    // We're using the Boolean constructor here, so that we can extend the value
    // e.g.  Modernizr.video     // true
    //       Modernizr.video.ogg // 'probably'
    //
    // Codec values from : github.com/NielsLeenheer/html5test/blob/9106a8/index.html#L845
    //                     thx to NielsLeenheer and zcorpan

    // Note: in some older browsers, "no" was a return value instead of empty string.
    //   It was live in FF3.5.0 and 3.5.1, but fixed in 3.5.2
    //   It was also live in Safari 4.0.0 - 4.0.4, but fixed in 4.0.5

    tests['video'] = function() {
        var elem = document.createElement('video'),
            bool = false;

        // IE9 Running on Windows Server SKU can cause an exception to be thrown, bug #224
        try {
            if ( bool = !!elem.canPlayType ) {
                bool      = new Boolean(bool);
                bool.ogg  = elem.canPlayType('video/ogg; codecs="theora"')      .replace(/^no$/,'');

                // Without QuickTime, this value will be `undefined`. github.com/Modernizr/Modernizr/issues/546
                bool.h264 = elem.canPlayType('video/mp4; codecs="avc1.42E01E"') .replace(/^no$/,'');

                bool.webm = elem.canPlayType('video/webm; codecs="vp8, vorbis"').replace(/^no$/,'');
            }

        } catch(e) { }

        return bool;
    };

    tests['audio'] = function() {
        var elem = document.createElement('audio'),
            bool = false;

        try {
            if ( bool = !!elem.canPlayType ) {
                bool      = new Boolean(bool);
                bool.ogg  = elem.canPlayType('audio/ogg; codecs="vorbis"').replace(/^no$/,'');
                bool.mp3  = elem.canPlayType('audio/mpeg;')               .replace(/^no$/,'');

                // Mimetypes accepted:
                //   developer.mozilla.org/En/Media_formats_supported_by_the_audio_and_video_elements
                //   bit.ly/iphoneoscodecs
                bool.wav  = elem.canPlayType('audio/wav; codecs="1"')     .replace(/^no$/,'');
                bool.m4a  = ( elem.canPlayType('audio/x-m4a;')            ||
                              elem.canPlayType('audio/aac;'))             .replace(/^no$/,'');
            }
        } catch(e) { }

        return bool;
    };


    // In FF4, if disabled, window.localStorage should === null.

    // Normally, we could not test that directly and need to do a
    //   `('localStorage' in window) && ` test first because otherwise Firefox will
    //   throw bugzil.la/365772 if cookies are disabled

    // Also in iOS5 Private Browsing mode, attempting to use localStorage.setItem
    // will throw the exception:
    //   QUOTA_EXCEEDED_ERRROR DOM Exception 22.
    // Peculiarly, getItem and removeItem calls do not throw.

    // Because we are forced to try/catch this, we'll go aggressive.

    // Just FWIW: IE8 Compat mode supports these features completely:
    //   www.quirksmode.org/dom/html5.html
    // But IE8 doesn't support either with local files

    tests['localstorage'] = function() {
        try {
            localStorage.setItem(mod, mod);
            localStorage.removeItem(mod);
            return true;
        } catch(e) {
            return false;
        }
    };

    tests['sessionstorage'] = function() {
        try {
            sessionStorage.setItem(mod, mod);
            sessionStorage.removeItem(mod);
            return true;
        } catch(e) {
            return false;
        }
    };


    tests['webworkers'] = function() {
        return !!window.Worker;
    };


    tests['applicationcache'] = function() {
        return !!window.applicationCache;
    };


    // Thanks to Erik Dahlstrom
    tests['svg'] = function() {
        return !!document.createElementNS && !!document.createElementNS(ns.svg, 'svg').createSVGRect;
    };

    // specifically for SVG inline in HTML, not within XHTML
    // test page: paulirish.com/demo/inline-svg
    tests['inlinesvg'] = function() {
      var div = document.createElement('div');
      div.innerHTML = '<svg/>';
      return (div.firstChild && div.firstChild.namespaceURI) == ns.svg;
    };

    // SVG SMIL animation
    tests['smil'] = function() {
        return !!document.createElementNS && /SVGAnimate/.test(toString.call(document.createElementNS(ns.svg, 'animate')));
    };

    // This test is only for clip paths in SVG proper, not clip paths on HTML content
    // demo: srufaculty.sru.edu/david.dailey/svg/newstuff/clipPath4.svg

    // However read the comments to dig into applying SVG clippaths to HTML content here:
    //   github.com/Modernizr/Modernizr/issues/213#issuecomment-1149491
    tests['svgclippaths'] = function() {
        return !!document.createElementNS && /SVGClipPath/.test(toString.call(document.createElementNS(ns.svg, 'clipPath')));
    };

    /*>>webforms*/
    // input features and input types go directly onto the ret object, bypassing the tests loop.
    // Hold this guy to execute in a moment.
    function webforms() {
        /*>>input*/
        // Run through HTML5's new input attributes to see if the UA understands any.
        // We're using f which is the <input> element created early on
        // Mike Taylr has created a comprehensive resource for testing these attributes
        //   when applied to all input types:
        //   miketaylr.com/code/input-type-attr.html
        // spec: www.whatwg.org/specs/web-apps/current-work/multipage/the-input-element.html#input-type-attr-summary

        // Only input placeholder is tested while textarea's placeholder is not.
        // Currently Safari 4 and Opera 11 have support only for the input placeholder
        // Both tests are available in feature-detects/forms-placeholder.js
        Modernizr['input'] = (function( props ) {
            for ( var i = 0, len = props.length; i < len; i++ ) {
                attrs[ props[i] ] = !!(props[i] in inputElem);
            }
            if (attrs.list){
              // safari false positive's on datalist: webk.it/74252
              // see also github.com/Modernizr/Modernizr/issues/146
              attrs.list = !!(document.createElement('datalist') && window.HTMLDataListElement);
            }
            return attrs;
        })('autocomplete autofocus list placeholder max min multiple pattern required step'.split(' '));
        /*>>input*/

        /*>>inputtypes*/
        // Run through HTML5's new input types to see if the UA understands any.
        //   This is put behind the tests runloop because it doesn't return a
        //   true/false like all the other tests; instead, it returns an object
        //   containing each input type with its corresponding true/false value

        // Big thanks to @miketaylr for the html5 forms expertise. miketaylr.com/
        Modernizr['inputtypes'] = (function(props) {

            for ( var i = 0, bool, inputElemType, defaultView, len = props.length; i < len; i++ ) {

                inputElem.setAttribute('type', inputElemType = props[i]);
                bool = inputElem.type !== 'text';

                // We first check to see if the type we give it sticks..
                // If the type does, we feed it a textual value, which shouldn't be valid.
                // If the value doesn't stick, we know there's input sanitization which infers a custom UI
                if ( bool ) {

                    inputElem.value         = smile;
                    inputElem.style.cssText = 'position:absolute;visibility:hidden;';

                    if ( /^range$/.test(inputElemType) && inputElem.style.WebkitAppearance !== undefined ) {

                      docElement.appendChild(inputElem);
                      defaultView = document.defaultView;

                      // Safari 2-4 allows the smiley as a value, despite making a slider
                      bool =  defaultView.getComputedStyle &&
                              defaultView.getComputedStyle(inputElem, null).WebkitAppearance !== 'textfield' &&
                              // Mobile android web browser has false positive, so must
                              // check the height to see if the widget is actually there.
                              (inputElem.offsetHeight !== 0);

                      docElement.removeChild(inputElem);

                    } else if ( /^(search|tel)$/.test(inputElemType) ){
                      // Spec doesn't define any special parsing or detectable UI
                      //   behaviors so we pass these through as true

                      // Interestingly, opera fails the earlier test, so it doesn't
                      //  even make it here.

                    } else if ( /^(url|email)$/.test(inputElemType) ) {
                      // Real url and email support comes with prebaked validation.
                      bool = inputElem.checkValidity && inputElem.checkValidity() === false;

                    } else {
                      // If the upgraded input compontent rejects the :) text, we got a winner
                      bool = inputElem.value != smile;
                    }
                }

                inputs[ props[i] ] = !!bool;
            }
            return inputs;
        })('search tel url email datetime date month week time datetime-local number range color'.split(' '));
        /*>>inputtypes*/
    }
    /*>>webforms*/


    // End of test definitions
    // -----------------------



    // Run through all tests and detect their support in the current UA.
    // todo: hypothetically we could be doing an array of tests and use a basic loop here.
    for ( var feature in tests ) {
        if ( hasOwnProp(tests, feature) ) {
            // run the test, throw the return value into the Modernizr,
            //   then based on that boolean, define an appropriate className
            //   and push it into an array of classes we'll join later.
            featureName  = feature.toLowerCase();
            Modernizr[featureName] = tests[feature]();

            classes.push((Modernizr[featureName] ? '' : 'no-') + featureName);
        }
    }

    /*>>webforms*/
    // input tests need to run.
    Modernizr.input || webforms();
    /*>>webforms*/


    /**
     * addTest allows the user to define their own feature tests
     * the result will be added onto the Modernizr object,
     * as well as an appropriate className set on the html element
     *
     * @param feature - String naming the feature
     * @param test - Function returning true if feature is supported, false if not
     */
     Modernizr.addTest = function ( feature, test ) {
       if ( typeof feature == 'object' ) {
         for ( var key in feature ) {
           if ( hasOwnProp( feature, key ) ) {
             Modernizr.addTest( key, feature[ key ] );
           }
         }
       } else {

         feature = feature.toLowerCase();

         if ( Modernizr[feature] !== undefined ) {
           // we're going to quit if you're trying to overwrite an existing test
           // if we were to allow it, we'd do this:
           //   var re = new RegExp("\\b(no-)?" + feature + "\\b");
           //   docElement.className = docElement.className.replace( re, '' );
           // but, no rly, stuff 'em.
           return Modernizr;
         }

         test = typeof test == 'function' ? test() : test;

         if (typeof enableClasses !== "undefined" && enableClasses) {
           docElement.className += ' ' + (test ? '' : 'no-') + feature;
         }
         Modernizr[feature] = test;

       }

       return Modernizr; // allow chaining.
     };


    // Reset modElem.cssText to nothing to reduce memory footprint.
    setCss('');
    modElem = inputElem = null;

    /*>>shiv*/
    /**
     * @preserve HTML5 Shiv prev3.7.1 | @afarkas @jdalton @jon_neal @rem | MIT/GPL2 Licensed
     */
    ;(function(window, document) {
        /*jshint evil:true */
        /** version */
        var version = '3.7.0';

        /** Preset options */
        var options = window.html5 || {};

        /** Used to skip problem elements */
        var reSkip = /^<|^(?:button|map|select|textarea|object|iframe|option|optgroup)$/i;

        /** Not all elements can be cloned in IE **/
        var saveClones = /^(?:a|b|code|div|fieldset|h1|h2|h3|h4|h5|h6|i|label|li|ol|p|q|span|strong|style|table|tbody|td|th|tr|ul)$/i;

        /** Detect whether the browser supports default html5 styles */
        var supportsHtml5Styles;

        /** Name of the expando, to work with multiple documents or to re-shiv one document */
        var expando = '_html5shiv';

        /** The id for the the documents expando */
        var expanID = 0;

        /** Cached data for each document */
        var expandoData = {};

        /** Detect whether the browser supports unknown elements */
        var supportsUnknownElements;

        (function() {
          try {
            var a = document.createElement('a');
            a.innerHTML = '<xyz></xyz>';
            //if the hidden property is implemented we can assume, that the browser supports basic HTML5 Styles
            supportsHtml5Styles = ('hidden' in a);

            supportsUnknownElements = a.childNodes.length == 1 || (function() {
              // assign a false positive if unable to shiv
              (document.createElement)('a');
              var frag = document.createDocumentFragment();
              return (
                typeof frag.cloneNode == 'undefined' ||
                typeof frag.createDocumentFragment == 'undefined' ||
                typeof frag.createElement == 'undefined'
              );
            }());
          } catch(e) {
            // assign a false positive if detection fails => unable to shiv
            supportsHtml5Styles = true;
            supportsUnknownElements = true;
          }

        }());

        /*--------------------------------------------------------------------------*/

        /**
         * Creates a style sheet with the given CSS text and adds it to the document.
         * @private
         * @param {Document} ownerDocument The document.
         * @param {String} cssText The CSS text.
         * @returns {StyleSheet} The style element.
         */
        function addStyleSheet(ownerDocument, cssText) {
          var p = ownerDocument.createElement('p'),
          parent = ownerDocument.getElementsByTagName('head')[0] || ownerDocument.documentElement;

          p.innerHTML = 'x<style>' + cssText + '</style>';
          return parent.insertBefore(p.lastChild, parent.firstChild);
        }

        /**
         * Returns the value of `html5.elements` as an array.
         * @private
         * @returns {Array} An array of shived element node names.
         */
        function getElements() {
          var elements = html5.elements;
          return typeof elements == 'string' ? elements.split(' ') : elements;
        }

        /**
         * Returns the data associated to the given document
         * @private
         * @param {Document} ownerDocument The document.
         * @returns {Object} An object of data.
         */
        function getExpandoData(ownerDocument) {
          var data = expandoData[ownerDocument[expando]];
          if (!data) {
            data = {};
            expanID++;
            ownerDocument[expando] = expanID;
            expandoData[expanID] = data;
          }
          return data;
        }

        /**
         * returns a shived element for the given nodeName and document
         * @memberOf html5
         * @param {String} nodeName name of the element
         * @param {Document} ownerDocument The context document.
         * @returns {Object} The shived element.
         */
        function createElement(nodeName, ownerDocument, data){
          if (!ownerDocument) {
            ownerDocument = document;
          }
          if(supportsUnknownElements){
            return ownerDocument.createElement(nodeName);
          }
          if (!data) {
            data = getExpandoData(ownerDocument);
          }
          var node;

          if (data.cache[nodeName]) {
            node = data.cache[nodeName].cloneNode();
          } else if (saveClones.test(nodeName)) {
            node = (data.cache[nodeName] = data.createElem(nodeName)).cloneNode();
          } else {
            node = data.createElem(nodeName);
          }

          // Avoid adding some elements to fragments in IE < 9 because
          // * Attributes like `name` or `type` cannot be set/changed once an element
          //   is inserted into a document/fragment
          // * Link elements with `src` attributes that are inaccessible, as with
          //   a 403 response, will cause the tab/window to crash
          // * Script elements appended to fragments will execute when their `src`
          //   or `text` property is set
          return node.canHaveChildren && !reSkip.test(nodeName) && !node.tagUrn ? data.frag.appendChild(node) : node;
        }

        /**
         * returns a shived DocumentFragment for the given document
         * @memberOf html5
         * @param {Document} ownerDocument The context document.
         * @returns {Object} The shived DocumentFragment.
         */
        function createDocumentFragment(ownerDocument, data){
          if (!ownerDocument) {
            ownerDocument = document;
          }
          if(supportsUnknownElements){
            return ownerDocument.createDocumentFragment();
          }
          data = data || getExpandoData(ownerDocument);
          var clone = data.frag.cloneNode(),
          i = 0,
          elems = getElements(),
          l = elems.length;
          for(;i<l;i++){
            clone.createElement(elems[i]);
          }
          return clone;
        }

        /**
         * Shivs the `createElement` and `createDocumentFragment` methods of the document.
         * @private
         * @param {Document|DocumentFragment} ownerDocument The document.
         * @param {Object} data of the document.
         */
        function shivMethods(ownerDocument, data) {
          if (!data.cache) {
            data.cache = {};
            data.createElem = ownerDocument.createElement;
            data.createFrag = ownerDocument.createDocumentFragment;
            data.frag = data.createFrag();
          }


          ownerDocument.createElement = function(nodeName) {
            //abort shiv
            if (!html5.shivMethods) {
              return data.createElem(nodeName);
            }
            return createElement(nodeName, ownerDocument, data);
          };

          ownerDocument.createDocumentFragment = Function('h,f', 'return function(){' +
                                                          'var n=f.cloneNode(),c=n.createElement;' +
                                                          'h.shivMethods&&(' +
                                                          // unroll the `createElement` calls
                                                          getElements().join().replace(/[\w\-]+/g, function(nodeName) {
            data.createElem(nodeName);
            data.frag.createElement(nodeName);
            return 'c("' + nodeName + '")';
          }) +
            ');return n}'
                                                         )(html5, data.frag);
        }

        /*--------------------------------------------------------------------------*/

        /**
         * Shivs the given document.
         * @memberOf html5
         * @param {Document} ownerDocument The document to shiv.
         * @returns {Document} The shived document.
         */
        function shivDocument(ownerDocument) {
          if (!ownerDocument) {
            ownerDocument = document;
          }
          var data = getExpandoData(ownerDocument);

          if (html5.shivCSS && !supportsHtml5Styles && !data.hasCSS) {
            data.hasCSS = !!addStyleSheet(ownerDocument,
                                          // corrects block display not defined in IE6/7/8/9
                                          'article,aside,dialog,figcaption,figure,footer,header,hgroup,main,nav,section{display:block}' +
                                            // adds styling not present in IE6/7/8/9
                                            'mark{background:#FF0;color:#000}' +
                                            // hides non-rendered elements
                                            'template{display:none}'
                                         );
          }
          if (!supportsUnknownElements) {
            shivMethods(ownerDocument, data);
          }
          return ownerDocument;
        }

        /*--------------------------------------------------------------------------*/

        /**
         * The `html5` object is exposed so that more elements can be shived and
         * existing shiving can be detected on iframes.
         * @type Object
         * @example
         *
         * // options can be changed before the script is included
         * html5 = { 'elements': 'mark section', 'shivCSS': false, 'shivMethods': false };
         */
        var html5 = {

          /**
           * An array or space separated string of node names of the elements to shiv.
           * @memberOf html5
           * @type Array|String
           */
          'elements': options.elements || 'abbr article aside audio bdi canvas data datalist details dialog figcaption figure footer header hgroup main mark meter nav output progress section summary template time video',

          /**
           * current version of html5shiv
           */
          'version': version,

          /**
           * A flag to indicate that the HTML5 style sheet should be inserted.
           * @memberOf html5
           * @type Boolean
           */
          'shivCSS': (options.shivCSS !== false),

          /**
           * Is equal to true if a browser supports creating unknown/HTML5 elements
           * @memberOf html5
           * @type boolean
           */
          'supportsUnknownElements': supportsUnknownElements,

          /**
           * A flag to indicate that the document's `createElement` and `createDocumentFragment`
           * methods should be overwritten.
           * @memberOf html5
           * @type Boolean
           */
          'shivMethods': (options.shivMethods !== false),

          /**
           * A string to describe the type of `html5` object ("default" or "default print").
           * @memberOf html5
           * @type String
           */
          'type': 'default',

          // shivs the document according to the specified `html5` object options
          'shivDocument': shivDocument,

          //creates a shived element
          createElement: createElement,

          //creates a shived documentFragment
          createDocumentFragment: createDocumentFragment
        };

        /*--------------------------------------------------------------------------*/

        // expose html5
        window.html5 = html5;

        // shiv the document
        shivDocument(document);

    }(this, document));
    /*>>shiv*/

    // Assign private properties to the return object with prefix
    Modernizr._version      = version;

    // expose these for the plugin API. Look in the source for how to join() them against your input
    /*>>prefixes*/
    Modernizr._prefixes     = prefixes;
    /*>>prefixes*/
    /*>>domprefixes*/
    Modernizr._domPrefixes  = domPrefixes;
    Modernizr._cssomPrefixes  = cssomPrefixes;
    /*>>domprefixes*/

    /*>>mq*/
    // Modernizr.mq tests a given media query, live against the current state of the window
    // A few important notes:
    //   * If a browser does not support media queries at all (eg. oldIE) the mq() will always return false
    //   * A max-width or orientation query will be evaluated against the current state, which may change later.
    //   * You must specify values. Eg. If you are testing support for the min-width media query use:
    //       Modernizr.mq('(min-width:0)')
    // usage:
    // Modernizr.mq('only screen and (max-width:768)')
    Modernizr.mq            = testMediaQuery;
    /*>>mq*/

    /*>>hasevent*/
    // Modernizr.hasEvent() detects support for a given event, with an optional element to test on
    // Modernizr.hasEvent('gesturestart', elem)
    Modernizr.hasEvent      = isEventSupported;
    /*>>hasevent*/

    /*>>testprop*/
    // Modernizr.testProp() investigates whether a given style property is recognized
    // Note that the property names must be provided in the camelCase variant.
    // Modernizr.testProp('pointerEvents')
    Modernizr.testProp      = function(prop){
        return testProps([prop]);
    };
    /*>>testprop*/

    /*>>testallprops*/
    // Modernizr.testAllProps() investigates whether a given style property,
    //   or any of its vendor-prefixed variants, is recognized
    // Note that the property names must be provided in the camelCase variant.
    // Modernizr.testAllProps('boxSizing')
    Modernizr.testAllProps  = testPropsAll;
    /*>>testallprops*/


    /*>>teststyles*/
    // Modernizr.testStyles() allows you to add custom styles to the document and test an element afterwards
    // Modernizr.testStyles('#modernizr { position:absolute }', function(elem, rule){ ... })
    Modernizr.testStyles    = injectElementWithStyles;
    /*>>teststyles*/


    /*>>prefixed*/
    // Modernizr.prefixed() returns the prefixed or nonprefixed property name variant of your input
    // Modernizr.prefixed('boxSizing') // 'MozBoxSizing'

    // Properties must be passed as dom-style camelcase, rather than `box-sizing` hypentated style.
    // Return values will also be the camelCase variant, if you need to translate that to hypenated style use:
    //
    //     str.replace(/([A-Z])/g, function(str,m1){ return '-' + m1.toLowerCase(); }).replace(/^ms-/,'-ms-');

    // If you're trying to ascertain which transition end event to bind to, you might do something like...
    //
    //     var transEndEventNames = {
    //       'WebkitTransition' : 'webkitTransitionEnd',
    //       'MozTransition'    : 'transitionend',
    //       'OTransition'      : 'oTransitionEnd',
    //       'msTransition'     : 'MSTransitionEnd',
    //       'transition'       : 'transitionend'
    //     },
    //     transEndEventName = transEndEventNames[ Modernizr.prefixed('transition') ];

    Modernizr.prefixed      = function(prop, obj, elem){
      if(!obj) {
        return testPropsAll(prop, 'pfx');
      } else {
        // Testing DOM property e.g. Modernizr.prefixed('requestAnimationFrame', window) // 'mozRequestAnimationFrame'
        return testPropsAll(prop, obj, elem);
      }
    };
    /*>>prefixed*/


    /*>>cssclasses*/
    // Remove "no-js" class from <html> element, if it exists:
    docElement.className = docElement.className.replace(/(^|\s)no-js(\s|$)/, '$1$2') +

                            // Add the new classes to the <html> element.
                            (enableClasses ? ' js ' + classes.join(' ') : '');
    /*>>cssclasses*/

    return Modernizr;

})(this, this.document);

//     Underscore.js 1.8.3
//     http://underscorejs.org
//     (c) 2009-2015 Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
//     Underscore may be freely distributed under the MIT license.

(function() {

  // Baseline setup
  // --------------

  // Establish the root object, `window` in the browser, or `exports` on the server.
  var root = this;

  // Save the previous value of the `_` variable.
  var previousUnderscore = root._;

  // Save bytes in the minified (but not gzipped) version:
  var ArrayProto = Array.prototype, ObjProto = Object.prototype, FuncProto = Function.prototype;

  // Create quick reference variables for speed access to core prototypes.
  var
    push             = ArrayProto.push,
    slice            = ArrayProto.slice,
    toString         = ObjProto.toString,
    hasOwnProperty   = ObjProto.hasOwnProperty;

  // All **ECMAScript 5** native function implementations that we hope to use
  // are declared here.
  var
    nativeIsArray      = Array.isArray,
    nativeKeys         = Object.keys,
    nativeBind         = FuncProto.bind,
    nativeCreate       = Object.create;

  // Naked function reference for surrogate-prototype-swapping.
  var Ctor = function(){};

  // Create a safe reference to the Underscore object for use below.
  var _ = function(obj) {
    if (obj instanceof _) return obj;
    if (!(this instanceof _)) return new _(obj);
    this._wrapped = obj;
  };

  // Export the Underscore object for **Node.js**, with
  // backwards-compatibility for the old `require()` API. If we're in
  // the browser, add `_` as a global object.
  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = _;
    }
    exports._ = _;
  } else {
    root._ = _;
  }

  // Current version.
  _.VERSION = '1.8.3';

  // Internal function that returns an efficient (for current engines) version
  // of the passed-in callback, to be repeatedly applied in other Underscore
  // functions.
  var optimizeCb = function(func, context, argCount) {
    if (context === void 0) return func;
    switch (argCount == null ? 3 : argCount) {
      case 1: return function(value) {
        return func.call(context, value);
      };
      case 2: return function(value, other) {
        return func.call(context, value, other);
      };
      case 3: return function(value, index, collection) {
        return func.call(context, value, index, collection);
      };
      case 4: return function(accumulator, value, index, collection) {
        return func.call(context, accumulator, value, index, collection);
      };
    }
    return function() {
      return func.apply(context, arguments);
    };
  };

  // A mostly-internal function to generate callbacks that can be applied
  // to each element in a collection, returning the desired result — either
  // identity, an arbitrary callback, a property matcher, or a property accessor.
  var cb = function(value, context, argCount) {
    if (value == null) return _.identity;
    if (_.isFunction(value)) return optimizeCb(value, context, argCount);
    if (_.isObject(value)) return _.matcher(value);
    return _.property(value);
  };
  _.iteratee = function(value, context) {
    return cb(value, context, Infinity);
  };

  // An internal function for creating assigner functions.
  var createAssigner = function(keysFunc, undefinedOnly) {
    return function(obj) {
      var length = arguments.length;
      if (length < 2 || obj == null) return obj;
      for (var index = 1; index < length; index++) {
        var source = arguments[index],
            keys = keysFunc(source),
            l = keys.length;
        for (var i = 0; i < l; i++) {
          var key = keys[i];
          if (!undefinedOnly || obj[key] === void 0) obj[key] = source[key];
        }
      }
      return obj;
    };
  };

  // An internal function for creating a new object that inherits from another.
  var baseCreate = function(prototype) {
    if (!_.isObject(prototype)) return {};
    if (nativeCreate) return nativeCreate(prototype);
    Ctor.prototype = prototype;
    var result = new Ctor;
    Ctor.prototype = null;
    return result;
  };

  var property = function(key) {
    return function(obj) {
      return obj == null ? void 0 : obj[key];
    };
  };

  // Helper for collection methods to determine whether a collection
  // should be iterated as an array or as an object
  // Related: http://people.mozilla.org/~jorendorff/es6-draft.html#sec-tolength
  // Avoids a very nasty iOS 8 JIT bug on ARM-64. #2094
  var MAX_ARRAY_INDEX = Math.pow(2, 53) - 1;
  var getLength = property('length');
  var isArrayLike = function(collection) {
    var length = getLength(collection);
    return typeof length == 'number' && length >= 0 && length <= MAX_ARRAY_INDEX;
  };

  // Collection Functions
  // --------------------

  // The cornerstone, an `each` implementation, aka `forEach`.
  // Handles raw objects in addition to array-likes. Treats all
  // sparse array-likes as if they were dense.
  _.each = _.forEach = function(obj, iteratee, context) {
    iteratee = optimizeCb(iteratee, context);
    var i, length;
    if (isArrayLike(obj)) {
      for (i = 0, length = obj.length; i < length; i++) {
        iteratee(obj[i], i, obj);
      }
    } else {
      var keys = _.keys(obj);
      for (i = 0, length = keys.length; i < length; i++) {
        iteratee(obj[keys[i]], keys[i], obj);
      }
    }
    return obj;
  };

  // Return the results of applying the iteratee to each element.
  _.map = _.collect = function(obj, iteratee, context) {
    iteratee = cb(iteratee, context);
    var keys = !isArrayLike(obj) && _.keys(obj),
        length = (keys || obj).length,
        results = Array(length);
    for (var index = 0; index < length; index++) {
      var currentKey = keys ? keys[index] : index;
      results[index] = iteratee(obj[currentKey], currentKey, obj);
    }
    return results;
  };

  // Create a reducing function iterating left or right.
  function createReduce(dir) {
    // Optimized iterator function as using arguments.length
    // in the main function will deoptimize the, see #1991.
    function iterator(obj, iteratee, memo, keys, index, length) {
      for (; index >= 0 && index < length; index += dir) {
        var currentKey = keys ? keys[index] : index;
        memo = iteratee(memo, obj[currentKey], currentKey, obj);
      }
      return memo;
    }

    return function(obj, iteratee, memo, context) {
      iteratee = optimizeCb(iteratee, context, 4);
      var keys = !isArrayLike(obj) && _.keys(obj),
          length = (keys || obj).length,
          index = dir > 0 ? 0 : length - 1;
      // Determine the initial value if none is provided.
      if (arguments.length < 3) {
        memo = obj[keys ? keys[index] : index];
        index += dir;
      }
      return iterator(obj, iteratee, memo, keys, index, length);
    };
  }

  // **Reduce** builds up a single result from a list of values, aka `inject`,
  // or `foldl`.
  _.reduce = _.foldl = _.inject = createReduce(1);

  // The right-associative version of reduce, also known as `foldr`.
  _.reduceRight = _.foldr = createReduce(-1);

  // Return the first value which passes a truth test. Aliased as `detect`.
  _.find = _.detect = function(obj, predicate, context) {
    var key;
    if (isArrayLike(obj)) {
      key = _.findIndex(obj, predicate, context);
    } else {
      key = _.findKey(obj, predicate, context);
    }
    if (key !== void 0 && key !== -1) return obj[key];
  };

  // Return all the elements that pass a truth test.
  // Aliased as `select`.
  _.filter = _.select = function(obj, predicate, context) {
    var results = [];
    predicate = cb(predicate, context);
    _.each(obj, function(value, index, list) {
      if (predicate(value, index, list)) results.push(value);
    });
    return results;
  };

  // Return all the elements for which a truth test fails.
  _.reject = function(obj, predicate, context) {
    return _.filter(obj, _.negate(cb(predicate)), context);
  };

  // Determine whether all of the elements match a truth test.
  // Aliased as `all`.
  _.every = _.all = function(obj, predicate, context) {
    predicate = cb(predicate, context);
    var keys = !isArrayLike(obj) && _.keys(obj),
        length = (keys || obj).length;
    for (var index = 0; index < length; index++) {
      var currentKey = keys ? keys[index] : index;
      if (!predicate(obj[currentKey], currentKey, obj)) return false;
    }
    return true;
  };

  // Determine if at least one element in the object matches a truth test.
  // Aliased as `any`.
  _.some = _.any = function(obj, predicate, context) {
    predicate = cb(predicate, context);
    var keys = !isArrayLike(obj) && _.keys(obj),
        length = (keys || obj).length;
    for (var index = 0; index < length; index++) {
      var currentKey = keys ? keys[index] : index;
      if (predicate(obj[currentKey], currentKey, obj)) return true;
    }
    return false;
  };

  // Determine if the array or object contains a given item (using `===`).
  // Aliased as `includes` and `include`.
  _.contains = _.includes = _.include = function(obj, item, fromIndex, guard) {
    if (!isArrayLike(obj)) obj = _.values(obj);
    if (typeof fromIndex != 'number' || guard) fromIndex = 0;
    return _.indexOf(obj, item, fromIndex) >= 0;
  };

  // Invoke a method (with arguments) on every item in a collection.
  _.invoke = function(obj, method) {
    var args = slice.call(arguments, 2);
    var isFunc = _.isFunction(method);
    return _.map(obj, function(value) {
      var func = isFunc ? method : value[method];
      return func == null ? func : func.apply(value, args);
    });
  };

  // Convenience version of a common use case of `map`: fetching a property.
  _.pluck = function(obj, key) {
    return _.map(obj, _.property(key));
  };

  // Convenience version of a common use case of `filter`: selecting only objects
  // containing specific `key:value` pairs.
  _.where = function(obj, attrs) {
    return _.filter(obj, _.matcher(attrs));
  };

  // Convenience version of a common use case of `find`: getting the first object
  // containing specific `key:value` pairs.
  _.findWhere = function(obj, attrs) {
    return _.find(obj, _.matcher(attrs));
  };

  // Return the maximum element (or element-based computation).
  _.max = function(obj, iteratee, context) {
    var result = -Infinity, lastComputed = -Infinity,
        value, computed;
    if (iteratee == null && obj != null) {
      obj = isArrayLike(obj) ? obj : _.values(obj);
      for (var i = 0, length = obj.length; i < length; i++) {
        value = obj[i];
        if (value > result) {
          result = value;
        }
      }
    } else {
      iteratee = cb(iteratee, context);
      _.each(obj, function(value, index, list) {
        computed = iteratee(value, index, list);
        if (computed > lastComputed || computed === -Infinity && result === -Infinity) {
          result = value;
          lastComputed = computed;
        }
      });
    }
    return result;
  };

  // Return the minimum element (or element-based computation).
  _.min = function(obj, iteratee, context) {
    var result = Infinity, lastComputed = Infinity,
        value, computed;
    if (iteratee == null && obj != null) {
      obj = isArrayLike(obj) ? obj : _.values(obj);
      for (var i = 0, length = obj.length; i < length; i++) {
        value = obj[i];
        if (value < result) {
          result = value;
        }
      }
    } else {
      iteratee = cb(iteratee, context);
      _.each(obj, function(value, index, list) {
        computed = iteratee(value, index, list);
        if (computed < lastComputed || computed === Infinity && result === Infinity) {
          result = value;
          lastComputed = computed;
        }
      });
    }
    return result;
  };

  // Shuffle a collection, using the modern version of the
  // [Fisher-Yates shuffle](http://en.wikipedia.org/wiki/Fisher–Yates_shuffle).
  _.shuffle = function(obj) {
    var set = isArrayLike(obj) ? obj : _.values(obj);
    var length = set.length;
    var shuffled = Array(length);
    for (var index = 0, rand; index < length; index++) {
      rand = _.random(0, index);
      if (rand !== index) shuffled[index] = shuffled[rand];
      shuffled[rand] = set[index];
    }
    return shuffled;
  };

  // Sample **n** random values from a collection.
  // If **n** is not specified, returns a single random element.
  // The internal `guard` argument allows it to work with `map`.
  _.sample = function(obj, n, guard) {
    if (n == null || guard) {
      if (!isArrayLike(obj)) obj = _.values(obj);
      return obj[_.random(obj.length - 1)];
    }
    return _.shuffle(obj).slice(0, Math.max(0, n));
  };

  // Sort the object's values by a criterion produced by an iteratee.
  _.sortBy = function(obj, iteratee, context) {
    iteratee = cb(iteratee, context);
    return _.pluck(_.map(obj, function(value, index, list) {
      return {
        value: value,
        index: index,
        criteria: iteratee(value, index, list)
      };
    }).sort(function(left, right) {
      var a = left.criteria;
      var b = right.criteria;
      if (a !== b) {
        if (a > b || a === void 0) return 1;
        if (a < b || b === void 0) return -1;
      }
      return left.index - right.index;
    }), 'value');
  };

  // An internal function used for aggregate "group by" operations.
  var group = function(behavior) {
    return function(obj, iteratee, context) {
      var result = {};
      iteratee = cb(iteratee, context);
      _.each(obj, function(value, index) {
        var key = iteratee(value, index, obj);
        behavior(result, value, key);
      });
      return result;
    };
  };

  // Groups the object's values by a criterion. Pass either a string attribute
  // to group by, or a function that returns the criterion.
  _.groupBy = group(function(result, value, key) {
    if (_.has(result, key)) result[key].push(value); else result[key] = [value];
  });

  // Indexes the object's values by a criterion, similar to `groupBy`, but for
  // when you know that your index values will be unique.
  _.indexBy = group(function(result, value, key) {
    result[key] = value;
  });

  // Counts instances of an object that group by a certain criterion. Pass
  // either a string attribute to count by, or a function that returns the
  // criterion.
  _.countBy = group(function(result, value, key) {
    if (_.has(result, key)) result[key]++; else result[key] = 1;
  });

  // Safely create a real, live array from anything iterable.
  _.toArray = function(obj) {
    if (!obj) return [];
    if (_.isArray(obj)) return slice.call(obj);
    if (isArrayLike(obj)) return _.map(obj, _.identity);
    return _.values(obj);
  };

  // Return the number of elements in an object.
  _.size = function(obj) {
    if (obj == null) return 0;
    return isArrayLike(obj) ? obj.length : _.keys(obj).length;
  };

  // Split a collection into two arrays: one whose elements all satisfy the given
  // predicate, and one whose elements all do not satisfy the predicate.
  _.partition = function(obj, predicate, context) {
    predicate = cb(predicate, context);
    var pass = [], fail = [];
    _.each(obj, function(value, key, obj) {
      (predicate(value, key, obj) ? pass : fail).push(value);
    });
    return [pass, fail];
  };

  // Array Functions
  // ---------------

  // Get the first element of an array. Passing **n** will return the first N
  // values in the array. Aliased as `head` and `take`. The **guard** check
  // allows it to work with `_.map`.
  _.first = _.head = _.take = function(array, n, guard) {
    if (array == null) return void 0;
    if (n == null || guard) return array[0];
    return _.initial(array, array.length - n);
  };

  // Returns everything but the last entry of the array. Especially useful on
  // the arguments object. Passing **n** will return all the values in
  // the array, excluding the last N.
  _.initial = function(array, n, guard) {
    return slice.call(array, 0, Math.max(0, array.length - (n == null || guard ? 1 : n)));
  };

  // Get the last element of an array. Passing **n** will return the last N
  // values in the array.
  _.last = function(array, n, guard) {
    if (array == null) return void 0;
    if (n == null || guard) return array[array.length - 1];
    return _.rest(array, Math.max(0, array.length - n));
  };

  // Returns everything but the first entry of the array. Aliased as `tail` and `drop`.
  // Especially useful on the arguments object. Passing an **n** will return
  // the rest N values in the array.
  _.rest = _.tail = _.drop = function(array, n, guard) {
    return slice.call(array, n == null || guard ? 1 : n);
  };

  // Trim out all falsy values from an array.
  _.compact = function(array) {
    return _.filter(array, _.identity);
  };

  // Internal implementation of a recursive `flatten` function.
  var flatten = function(input, shallow, strict, startIndex) {
    var output = [], idx = 0;
    for (var i = startIndex || 0, length = getLength(input); i < length; i++) {
      var value = input[i];
      if (isArrayLike(value) && (_.isArray(value) || _.isArguments(value))) {
        //flatten current level of array or arguments object
        if (!shallow) value = flatten(value, shallow, strict);
        var j = 0, len = value.length;
        output.length += len;
        while (j < len) {
          output[idx++] = value[j++];
        }
      } else if (!strict) {
        output[idx++] = value;
      }
    }
    return output;
  };

  // Flatten out an array, either recursively (by default), or just one level.
  _.flatten = function(array, shallow) {
    return flatten(array, shallow, false);
  };

  // Return a version of the array that does not contain the specified value(s).
  _.without = function(array) {
    return _.difference(array, slice.call(arguments, 1));
  };

  // Produce a duplicate-free version of the array. If the array has already
  // been sorted, you have the option of using a faster algorithm.
  // Aliased as `unique`.
  _.uniq = _.unique = function(array, isSorted, iteratee, context) {
    if (!_.isBoolean(isSorted)) {
      context = iteratee;
      iteratee = isSorted;
      isSorted = false;
    }
    if (iteratee != null) iteratee = cb(iteratee, context);
    var result = [];
    var seen = [];
    for (var i = 0, length = getLength(array); i < length; i++) {
      var value = array[i],
          computed = iteratee ? iteratee(value, i, array) : value;
      if (isSorted) {
        if (!i || seen !== computed) result.push(value);
        seen = computed;
      } else if (iteratee) {
        if (!_.contains(seen, computed)) {
          seen.push(computed);
          result.push(value);
        }
      } else if (!_.contains(result, value)) {
        result.push(value);
      }
    }
    return result;
  };

  // Produce an array that contains the union: each distinct element from all of
  // the passed-in arrays.
  _.union = function() {
    return _.uniq(flatten(arguments, true, true));
  };

  // Produce an array that contains every item shared between all the
  // passed-in arrays.
  _.intersection = function(array) {
    var result = [];
    var argsLength = arguments.length;
    for (var i = 0, length = getLength(array); i < length; i++) {
      var item = array[i];
      if (_.contains(result, item)) continue;
      for (var j = 1; j < argsLength; j++) {
        if (!_.contains(arguments[j], item)) break;
      }
      if (j === argsLength) result.push(item);
    }
    return result;
  };

  // Take the difference between one array and a number of other arrays.
  // Only the elements present in just the first array will remain.
  _.difference = function(array) {
    var rest = flatten(arguments, true, true, 1);
    return _.filter(array, function(value){
      return !_.contains(rest, value);
    });
  };

  // Zip together multiple lists into a single array -- elements that share
  // an index go together.
  _.zip = function() {
    return _.unzip(arguments);
  };

  // Complement of _.zip. Unzip accepts an array of arrays and groups
  // each array's elements on shared indices
  _.unzip = function(array) {
    var length = array && _.max(array, getLength).length || 0;
    var result = Array(length);

    for (var index = 0; index < length; index++) {
      result[index] = _.pluck(array, index);
    }
    return result;
  };

  // Converts lists into objects. Pass either a single array of `[key, value]`
  // pairs, or two parallel arrays of the same length -- one of keys, and one of
  // the corresponding values.
  _.object = function(list, values) {
    var result = {};
    for (var i = 0, length = getLength(list); i < length; i++) {
      if (values) {
        result[list[i]] = values[i];
      } else {
        result[list[i][0]] = list[i][1];
      }
    }
    return result;
  };

  // Generator function to create the findIndex and findLastIndex functions
  function createPredicateIndexFinder(dir) {
    return function(array, predicate, context) {
      predicate = cb(predicate, context);
      var length = getLength(array);
      var index = dir > 0 ? 0 : length - 1;
      for (; index >= 0 && index < length; index += dir) {
        if (predicate(array[index], index, array)) return index;
      }
      return -1;
    };
  }

  // Returns the first index on an array-like that passes a predicate test
  _.findIndex = createPredicateIndexFinder(1);
  _.findLastIndex = createPredicateIndexFinder(-1);

  // Use a comparator function to figure out the smallest index at which
  // an object should be inserted so as to maintain order. Uses binary search.
  _.sortedIndex = function(array, obj, iteratee, context) {
    iteratee = cb(iteratee, context, 1);
    var value = iteratee(obj);
    var low = 0, high = getLength(array);
    while (low < high) {
      var mid = Math.floor((low + high) / 2);
      if (iteratee(array[mid]) < value) low = mid + 1; else high = mid;
    }
    return low;
  };

  // Generator function to create the indexOf and lastIndexOf functions
  function createIndexFinder(dir, predicateFind, sortedIndex) {
    return function(array, item, idx) {
      var i = 0, length = getLength(array);
      if (typeof idx == 'number') {
        if (dir > 0) {
            i = idx >= 0 ? idx : Math.max(idx + length, i);
        } else {
            length = idx >= 0 ? Math.min(idx + 1, length) : idx + length + 1;
        }
      } else if (sortedIndex && idx && length) {
        idx = sortedIndex(array, item);
        return array[idx] === item ? idx : -1;
      }
      if (item !== item) {
        idx = predicateFind(slice.call(array, i, length), _.isNaN);
        return idx >= 0 ? idx + i : -1;
      }
      for (idx = dir > 0 ? i : length - 1; idx >= 0 && idx < length; idx += dir) {
        if (array[idx] === item) return idx;
      }
      return -1;
    };
  }

  // Return the position of the first occurrence of an item in an array,
  // or -1 if the item is not included in the array.
  // If the array is large and already in sort order, pass `true`
  // for **isSorted** to use binary search.
  _.indexOf = createIndexFinder(1, _.findIndex, _.sortedIndex);
  _.lastIndexOf = createIndexFinder(-1, _.findLastIndex);

  // Generate an integer Array containing an arithmetic progression. A port of
  // the native Python `range()` function. See
  // [the Python documentation](http://docs.python.org/library/functions.html#range).
  _.range = function(start, stop, step) {
    if (stop == null) {
      stop = start || 0;
      start = 0;
    }
    step = step || 1;

    var length = Math.max(Math.ceil((stop - start) / step), 0);
    var range = Array(length);

    for (var idx = 0; idx < length; idx++, start += step) {
      range[idx] = start;
    }

    return range;
  };

  // Function (ahem) Functions
  // ------------------

  // Determines whether to execute a function as a constructor
  // or a normal function with the provided arguments
  var executeBound = function(sourceFunc, boundFunc, context, callingContext, args) {
    if (!(callingContext instanceof boundFunc)) return sourceFunc.apply(context, args);
    var self = baseCreate(sourceFunc.prototype);
    var result = sourceFunc.apply(self, args);
    if (_.isObject(result)) return result;
    return self;
  };

  // Create a function bound to a given object (assigning `this`, and arguments,
  // optionally). Delegates to **ECMAScript 5**'s native `Function.bind` if
  // available.
  _.bind = function(func, context) {
    if (nativeBind && func.bind === nativeBind) return nativeBind.apply(func, slice.call(arguments, 1));
    if (!_.isFunction(func)) throw new TypeError('Bind must be called on a function');
    var args = slice.call(arguments, 2);
    var bound = function() {
      return executeBound(func, bound, context, this, args.concat(slice.call(arguments)));
    };
    return bound;
  };

  // Partially apply a function by creating a version that has had some of its
  // arguments pre-filled, without changing its dynamic `this` context. _ acts
  // as a placeholder, allowing any combination of arguments to be pre-filled.
  _.partial = function(func) {
    var boundArgs = slice.call(arguments, 1);
    var bound = function() {
      var position = 0, length = boundArgs.length;
      var args = Array(length);
      for (var i = 0; i < length; i++) {
        args[i] = boundArgs[i] === _ ? arguments[position++] : boundArgs[i];
      }
      while (position < arguments.length) args.push(arguments[position++]);
      return executeBound(func, bound, this, this, args);
    };
    return bound;
  };

  // Bind a number of an object's methods to that object. Remaining arguments
  // are the method names to be bound. Useful for ensuring that all callbacks
  // defined on an object belong to it.
  _.bindAll = function(obj) {
    var i, length = arguments.length, key;
    if (length <= 1) throw new Error('bindAll must be passed function names');
    for (i = 1; i < length; i++) {
      key = arguments[i];
      obj[key] = _.bind(obj[key], obj);
    }
    return obj;
  };

  // Memoize an expensive function by storing its results.
  _.memoize = function(func, hasher) {
    var memoize = function(key) {
      var cache = memoize.cache;
      var address = '' + (hasher ? hasher.apply(this, arguments) : key);
      if (!_.has(cache, address)) cache[address] = func.apply(this, arguments);
      return cache[address];
    };
    memoize.cache = {};
    return memoize;
  };

  // Delays a function for the given number of milliseconds, and then calls
  // it with the arguments supplied.
  _.delay = function(func, wait) {
    var args = slice.call(arguments, 2);
    return setTimeout(function(){
      return func.apply(null, args);
    }, wait);
  };

  // Defers a function, scheduling it to run after the current call stack has
  // cleared.
  _.defer = _.partial(_.delay, _, 1);

  // Returns a function, that, when invoked, will only be triggered at most once
  // during a given window of time. Normally, the throttled function will run
  // as much as it can, without ever going more than once per `wait` duration;
  // but if you'd like to disable the execution on the leading edge, pass
  // `{leading: false}`. To disable execution on the trailing edge, ditto.
  _.throttle = function(func, wait, options) {
    var context, args, result;
    var timeout = null;
    var previous = 0;
    if (!options) options = {};
    var later = function() {
      previous = options.leading === false ? 0 : _.now();
      timeout = null;
      result = func.apply(context, args);
      if (!timeout) context = args = null;
    };
    return function() {
      var now = _.now();
      if (!previous && options.leading === false) previous = now;
      var remaining = wait - (now - previous);
      context = this;
      args = arguments;
      if (remaining <= 0 || remaining > wait) {
        if (timeout) {
          clearTimeout(timeout);
          timeout = null;
        }
        previous = now;
        result = func.apply(context, args);
        if (!timeout) context = args = null;
      } else if (!timeout && options.trailing !== false) {
        timeout = setTimeout(later, remaining);
      }
      return result;
    };
  };

  // Returns a function, that, as long as it continues to be invoked, will not
  // be triggered. The function will be called after it stops being called for
  // N milliseconds. If `immediate` is passed, trigger the function on the
  // leading edge, instead of the trailing.
  _.debounce = function(func, wait, immediate) {
    var timeout, args, context, timestamp, result;

    var later = function() {
      var last = _.now() - timestamp;

      if (last < wait && last >= 0) {
        timeout = setTimeout(later, wait - last);
      } else {
        timeout = null;
        if (!immediate) {
          result = func.apply(context, args);
          if (!timeout) context = args = null;
        }
      }
    };

    return function() {
      context = this;
      args = arguments;
      timestamp = _.now();
      var callNow = immediate && !timeout;
      if (!timeout) timeout = setTimeout(later, wait);
      if (callNow) {
        result = func.apply(context, args);
        context = args = null;
      }

      return result;
    };
  };

  // Returns the first function passed as an argument to the second,
  // allowing you to adjust arguments, run code before and after, and
  // conditionally execute the original function.
  _.wrap = function(func, wrapper) {
    return _.partial(wrapper, func);
  };

  // Returns a negated version of the passed-in predicate.
  _.negate = function(predicate) {
    return function() {
      return !predicate.apply(this, arguments);
    };
  };

  // Returns a function that is the composition of a list of functions, each
  // consuming the return value of the function that follows.
  _.compose = function() {
    var args = arguments;
    var start = args.length - 1;
    return function() {
      var i = start;
      var result = args[start].apply(this, arguments);
      while (i--) result = args[i].call(this, result);
      return result;
    };
  };

  // Returns a function that will only be executed on and after the Nth call.
  _.after = function(times, func) {
    return function() {
      if (--times < 1) {
        return func.apply(this, arguments);
      }
    };
  };

  // Returns a function that will only be executed up to (but not including) the Nth call.
  _.before = function(times, func) {
    var memo;
    return function() {
      if (--times > 0) {
        memo = func.apply(this, arguments);
      }
      if (times <= 1) func = null;
      return memo;
    };
  };

  // Returns a function that will be executed at most one time, no matter how
  // often you call it. Useful for lazy initialization.
  _.once = _.partial(_.before, 2);

  // Object Functions
  // ----------------

  // Keys in IE < 9 that won't be iterated by `for key in ...` and thus missed.
  var hasEnumBug = !{toString: null}.propertyIsEnumerable('toString');
  var nonEnumerableProps = ['valueOf', 'isPrototypeOf', 'toString',
                      'propertyIsEnumerable', 'hasOwnProperty', 'toLocaleString'];

  function collectNonEnumProps(obj, keys) {
    var nonEnumIdx = nonEnumerableProps.length;
    var constructor = obj.constructor;
    var proto = (_.isFunction(constructor) && constructor.prototype) || ObjProto;

    // Constructor is a special case.
    var prop = 'constructor';
    if (_.has(obj, prop) && !_.contains(keys, prop)) keys.push(prop);

    while (nonEnumIdx--) {
      prop = nonEnumerableProps[nonEnumIdx];
      if (prop in obj && obj[prop] !== proto[prop] && !_.contains(keys, prop)) {
        keys.push(prop);
      }
    }
  }

  // Retrieve the names of an object's own properties.
  // Delegates to **ECMAScript 5**'s native `Object.keys`
  _.keys = function(obj) {
    if (!_.isObject(obj)) return [];
    if (nativeKeys) return nativeKeys(obj);
    var keys = [];
    for (var key in obj) if (_.has(obj, key)) keys.push(key);
    // Ahem, IE < 9.
    if (hasEnumBug) collectNonEnumProps(obj, keys);
    return keys;
  };

  // Retrieve all the property names of an object.
  _.allKeys = function(obj) {
    if (!_.isObject(obj)) return [];
    var keys = [];
    for (var key in obj) keys.push(key);
    // Ahem, IE < 9.
    if (hasEnumBug) collectNonEnumProps(obj, keys);
    return keys;
  };

  // Retrieve the values of an object's properties.
  _.values = function(obj) {
    var keys = _.keys(obj);
    var length = keys.length;
    var values = Array(length);
    for (var i = 0; i < length; i++) {
      values[i] = obj[keys[i]];
    }
    return values;
  };

  // Returns the results of applying the iteratee to each element of the object
  // In contrast to _.map it returns an object
  _.mapObject = function(obj, iteratee, context) {
    iteratee = cb(iteratee, context);
    var keys =  _.keys(obj),
          length = keys.length,
          results = {},
          currentKey;
      for (var index = 0; index < length; index++) {
        currentKey = keys[index];
        results[currentKey] = iteratee(obj[currentKey], currentKey, obj);
      }
      return results;
  };

  // Convert an object into a list of `[key, value]` pairs.
  _.pairs = function(obj) {
    var keys = _.keys(obj);
    var length = keys.length;
    var pairs = Array(length);
    for (var i = 0; i < length; i++) {
      pairs[i] = [keys[i], obj[keys[i]]];
    }
    return pairs;
  };

  // Invert the keys and values of an object. The values must be serializable.
  _.invert = function(obj) {
    var result = {};
    var keys = _.keys(obj);
    for (var i = 0, length = keys.length; i < length; i++) {
      result[obj[keys[i]]] = keys[i];
    }
    return result;
  };

  // Return a sorted list of the function names available on the object.
  // Aliased as `methods`
  _.functions = _.methods = function(obj) {
    var names = [];
    for (var key in obj) {
      if (_.isFunction(obj[key])) names.push(key);
    }
    return names.sort();
  };

  // Extend a given object with all the properties in passed-in object(s).
  _.extend = createAssigner(_.allKeys);

  // Assigns a given object with all the own properties in the passed-in object(s)
  // (https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Object/assign)
  _.extendOwn = _.assign = createAssigner(_.keys);

  // Returns the first key on an object that passes a predicate test
  _.findKey = function(obj, predicate, context) {
    predicate = cb(predicate, context);
    var keys = _.keys(obj), key;
    for (var i = 0, length = keys.length; i < length; i++) {
      key = keys[i];
      if (predicate(obj[key], key, obj)) return key;
    }
  };

  // Return a copy of the object only containing the whitelisted properties.
  _.pick = function(object, oiteratee, context) {
    var result = {}, obj = object, iteratee, keys;
    if (obj == null) return result;
    if (_.isFunction(oiteratee)) {
      keys = _.allKeys(obj);
      iteratee = optimizeCb(oiteratee, context);
    } else {
      keys = flatten(arguments, false, false, 1);
      iteratee = function(value, key, obj) { return key in obj; };
      obj = Object(obj);
    }
    for (var i = 0, length = keys.length; i < length; i++) {
      var key = keys[i];
      var value = obj[key];
      if (iteratee(value, key, obj)) result[key] = value;
    }
    return result;
  };

   // Return a copy of the object without the blacklisted properties.
  _.omit = function(obj, iteratee, context) {
    if (_.isFunction(iteratee)) {
      iteratee = _.negate(iteratee);
    } else {
      var keys = _.map(flatten(arguments, false, false, 1), String);
      iteratee = function(value, key) {
        return !_.contains(keys, key);
      };
    }
    return _.pick(obj, iteratee, context);
  };

  // Fill in a given object with default properties.
  _.defaults = createAssigner(_.allKeys, true);

  // Creates an object that inherits from the given prototype object.
  // If additional properties are provided then they will be added to the
  // created object.
  _.create = function(prototype, props) {
    var result = baseCreate(prototype);
    if (props) _.extendOwn(result, props);
    return result;
  };

  // Create a (shallow-cloned) duplicate of an object.
  _.clone = function(obj) {
    if (!_.isObject(obj)) return obj;
    return _.isArray(obj) ? obj.slice() : _.extend({}, obj);
  };

  // Invokes interceptor with the obj, and then returns obj.
  // The primary purpose of this method is to "tap into" a method chain, in
  // order to perform operations on intermediate results within the chain.
  _.tap = function(obj, interceptor) {
    interceptor(obj);
    return obj;
  };

  // Returns whether an object has a given set of `key:value` pairs.
  _.isMatch = function(object, attrs) {
    var keys = _.keys(attrs), length = keys.length;
    if (object == null) return !length;
    var obj = Object(object);
    for (var i = 0; i < length; i++) {
      var key = keys[i];
      if (attrs[key] !== obj[key] || !(key in obj)) return false;
    }
    return true;
  };


  // Internal recursive comparison function for `isEqual`.
  var eq = function(a, b, aStack, bStack) {
    // Identical objects are equal. `0 === -0`, but they aren't identical.
    // See the [Harmony `egal` proposal](http://wiki.ecmascript.org/doku.php?id=harmony:egal).
    if (a === b) return a !== 0 || 1 / a === 1 / b;
    // A strict comparison is necessary because `null == undefined`.
    if (a == null || b == null) return a === b;
    // Unwrap any wrapped objects.
    if (a instanceof _) a = a._wrapped;
    if (b instanceof _) b = b._wrapped;
    // Compare `[[Class]]` names.
    var className = toString.call(a);
    if (className !== toString.call(b)) return false;
    switch (className) {
      // Strings, numbers, regular expressions, dates, and booleans are compared by value.
      case '[object RegExp]':
      // RegExps are coerced to strings for comparison (Note: '' + /a/i === '/a/i')
      case '[object String]':
        // Primitives and their corresponding object wrappers are equivalent; thus, `"5"` is
        // equivalent to `new String("5")`.
        return '' + a === '' + b;
      case '[object Number]':
        // `NaN`s are equivalent, but non-reflexive.
        // Object(NaN) is equivalent to NaN
        if (+a !== +a) return +b !== +b;
        // An `egal` comparison is performed for other numeric values.
        return +a === 0 ? 1 / +a === 1 / b : +a === +b;
      case '[object Date]':
      case '[object Boolean]':
        // Coerce dates and booleans to numeric primitive values. Dates are compared by their
        // millisecond representations. Note that invalid dates with millisecond representations
        // of `NaN` are not equivalent.
        return +a === +b;
    }

    var areArrays = className === '[object Array]';
    if (!areArrays) {
      if (typeof a != 'object' || typeof b != 'object') return false;

      // Objects with different constructors are not equivalent, but `Object`s or `Array`s
      // from different frames are.
      var aCtor = a.constructor, bCtor = b.constructor;
      if (aCtor !== bCtor && !(_.isFunction(aCtor) && aCtor instanceof aCtor &&
                               _.isFunction(bCtor) && bCtor instanceof bCtor)
                          && ('constructor' in a && 'constructor' in b)) {
        return false;
      }
    }
    // Assume equality for cyclic structures. The algorithm for detecting cyclic
    // structures is adapted from ES 5.1 section 15.12.3, abstract operation `JO`.

    // Initializing stack of traversed objects.
    // It's done here since we only need them for objects and arrays comparison.
    aStack = aStack || [];
    bStack = bStack || [];
    var length = aStack.length;
    while (length--) {
      // Linear search. Performance is inversely proportional to the number of
      // unique nested structures.
      if (aStack[length] === a) return bStack[length] === b;
    }

    // Add the first object to the stack of traversed objects.
    aStack.push(a);
    bStack.push(b);

    // Recursively compare objects and arrays.
    if (areArrays) {
      // Compare array lengths to determine if a deep comparison is necessary.
      length = a.length;
      if (length !== b.length) return false;
      // Deep compare the contents, ignoring non-numeric properties.
      while (length--) {
        if (!eq(a[length], b[length], aStack, bStack)) return false;
      }
    } else {
      // Deep compare objects.
      var keys = _.keys(a), key;
      length = keys.length;
      // Ensure that both objects contain the same number of properties before comparing deep equality.
      if (_.keys(b).length !== length) return false;
      while (length--) {
        // Deep compare each member
        key = keys[length];
        if (!(_.has(b, key) && eq(a[key], b[key], aStack, bStack))) return false;
      }
    }
    // Remove the first object from the stack of traversed objects.
    aStack.pop();
    bStack.pop();
    return true;
  };

  // Perform a deep comparison to check if two objects are equal.
  _.isEqual = function(a, b) {
    return eq(a, b);
  };

  // Is a given array, string, or object empty?
  // An "empty" object has no enumerable own-properties.
  _.isEmpty = function(obj) {
    if (obj == null) return true;
    if (isArrayLike(obj) && (_.isArray(obj) || _.isString(obj) || _.isArguments(obj))) return obj.length === 0;
    return _.keys(obj).length === 0;
  };

  // Is a given value a DOM element?
  _.isElement = function(obj) {
    return !!(obj && obj.nodeType === 1);
  };

  // Is a given value an array?
  // Delegates to ECMA5's native Array.isArray
  _.isArray = nativeIsArray || function(obj) {
    return toString.call(obj) === '[object Array]';
  };

  // Is a given variable an object?
  _.isObject = function(obj) {
    var type = typeof obj;
    return type === 'function' || type === 'object' && !!obj;
  };

  // Add some isType methods: isArguments, isFunction, isString, isNumber, isDate, isRegExp, isError.
  _.each(['Arguments', 'Function', 'String', 'Number', 'Date', 'RegExp', 'Error'], function(name) {
    _['is' + name] = function(obj) {
      return toString.call(obj) === '[object ' + name + ']';
    };
  });

  // Define a fallback version of the method in browsers (ahem, IE < 9), where
  // there isn't any inspectable "Arguments" type.
  if (!_.isArguments(arguments)) {
    _.isArguments = function(obj) {
      return _.has(obj, 'callee');
    };
  }

  // Optimize `isFunction` if appropriate. Work around some typeof bugs in old v8,
  // IE 11 (#1621), and in Safari 8 (#1929).
  if (typeof /./ != 'function' && typeof Int8Array != 'object') {
    _.isFunction = function(obj) {
      return typeof obj == 'function' || false;
    };
  }

  // Is a given object a finite number?
  _.isFinite = function(obj) {
    return isFinite(obj) && !isNaN(parseFloat(obj));
  };

  // Is the given value `NaN`? (NaN is the only number which does not equal itself).
  _.isNaN = function(obj) {
    return _.isNumber(obj) && obj !== +obj;
  };

  // Is a given value a boolean?
  _.isBoolean = function(obj) {
    return obj === true || obj === false || toString.call(obj) === '[object Boolean]';
  };

  // Is a given value equal to null?
  _.isNull = function(obj) {
    return obj === null;
  };

  // Is a given variable undefined?
  _.isUndefined = function(obj) {
    return obj === void 0;
  };

  // Shortcut function for checking if an object has a given property directly
  // on itself (in other words, not on a prototype).
  _.has = function(obj, key) {
    return obj != null && hasOwnProperty.call(obj, key);
  };

  // Utility Functions
  // -----------------

  // Run Underscore.js in *noConflict* mode, returning the `_` variable to its
  // previous owner. Returns a reference to the Underscore object.
  _.noConflict = function() {
    root._ = previousUnderscore;
    return this;
  };

  // Keep the identity function around for default iteratees.
  _.identity = function(value) {
    return value;
  };

  // Predicate-generating functions. Often useful outside of Underscore.
  _.constant = function(value) {
    return function() {
      return value;
    };
  };

  _.noop = function(){};

  _.property = property;

  // Generates a function for a given object that returns a given property.
  _.propertyOf = function(obj) {
    return obj == null ? function(){} : function(key) {
      return obj[key];
    };
  };

  // Returns a predicate for checking whether an object has a given set of
  // `key:value` pairs.
  _.matcher = _.matches = function(attrs) {
    attrs = _.extendOwn({}, attrs);
    return function(obj) {
      return _.isMatch(obj, attrs);
    };
  };

  // Run a function **n** times.
  _.times = function(n, iteratee, context) {
    var accum = Array(Math.max(0, n));
    iteratee = optimizeCb(iteratee, context, 1);
    for (var i = 0; i < n; i++) accum[i] = iteratee(i);
    return accum;
  };

  // Return a random integer between min and max (inclusive).
  _.random = function(min, max) {
    if (max == null) {
      max = min;
      min = 0;
    }
    return min + Math.floor(Math.random() * (max - min + 1));
  };

  // A (possibly faster) way to get the current timestamp as an integer.
  _.now = Date.now || function() {
    return new Date().getTime();
  };

   // List of HTML entities for escaping.
  var escapeMap = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#x27;',
    '`': '&#x60;'
  };
  var unescapeMap = _.invert(escapeMap);

  // Functions for escaping and unescaping strings to/from HTML interpolation.
  var createEscaper = function(map) {
    var escaper = function(match) {
      return map[match];
    };
    // Regexes for identifying a key that needs to be escaped
    var source = '(?:' + _.keys(map).join('|') + ')';
    var testRegexp = RegExp(source);
    var replaceRegexp = RegExp(source, 'g');
    return function(string) {
      string = string == null ? '' : '' + string;
      return testRegexp.test(string) ? string.replace(replaceRegexp, escaper) : string;
    };
  };
  _.escape = createEscaper(escapeMap);
  _.unescape = createEscaper(unescapeMap);

  // If the value of the named `property` is a function then invoke it with the
  // `object` as context; otherwise, return it.
  _.result = function(object, property, fallback) {
    var value = object == null ? void 0 : object[property];
    if (value === void 0) {
      value = fallback;
    }
    return _.isFunction(value) ? value.call(object) : value;
  };

  // Generate a unique integer id (unique within the entire client session).
  // Useful for temporary DOM ids.
  var idCounter = 0;
  _.uniqueId = function(prefix) {
    var id = ++idCounter + '';
    return prefix ? prefix + id : id;
  };

  // By default, Underscore uses ERB-style template delimiters, change the
  // following template settings to use alternative delimiters.
  _.templateSettings = {
    evaluate    : /<%([\s\S]+?)%>/g,
    interpolate : /<%=([\s\S]+?)%>/g,
    escape      : /<%-([\s\S]+?)%>/g
  };

  // When customizing `templateSettings`, if you don't want to define an
  // interpolation, evaluation or escaping regex, we need one that is
  // guaranteed not to match.
  var noMatch = /(.)^/;

  // Certain characters need to be escaped so that they can be put into a
  // string literal.
  var escapes = {
    "'":      "'",
    '\\':     '\\',
    '\r':     'r',
    '\n':     'n',
    '\u2028': 'u2028',
    '\u2029': 'u2029'
  };

  var escaper = /\\|'|\r|\n|\u2028|\u2029/g;

  var escapeChar = function(match) {
    return '\\' + escapes[match];
  };

  // JavaScript micro-templating, similar to John Resig's implementation.
  // Underscore templating handles arbitrary delimiters, preserves whitespace,
  // and correctly escapes quotes within interpolated code.
  // NB: `oldSettings` only exists for backwards compatibility.
  _.template = function(text, settings, oldSettings) {
    if (!settings && oldSettings) settings = oldSettings;
    settings = _.defaults({}, settings, _.templateSettings);

    // Combine delimiters into one regular expression via alternation.
    var matcher = RegExp([
      (settings.escape || noMatch).source,
      (settings.interpolate || noMatch).source,
      (settings.evaluate || noMatch).source
    ].join('|') + '|$', 'g');

    // Compile the template source, escaping string literals appropriately.
    var index = 0;
    var source = "__p+='";
    text.replace(matcher, function(match, escape, interpolate, evaluate, offset) {
      source += text.slice(index, offset).replace(escaper, escapeChar);
      index = offset + match.length;

      if (escape) {
        source += "'+\n((__t=(" + escape + "))==null?'':_.escape(__t))+\n'";
      } else if (interpolate) {
        source += "'+\n((__t=(" + interpolate + "))==null?'':__t)+\n'";
      } else if (evaluate) {
        source += "';\n" + evaluate + "\n__p+='";
      }

      // Adobe VMs need the match returned to produce the correct offest.
      return match;
    });
    source += "';\n";

    // If a variable is not specified, place data values in local scope.
    if (!settings.variable) source = 'with(obj||{}){\n' + source + '}\n';

    source = "var __t,__p='',__j=Array.prototype.join," +
      "print=function(){__p+=__j.call(arguments,'');};\n" +
      source + 'return __p;\n';

    try {
      var render = new Function(settings.variable || 'obj', '_', source);
    } catch (e) {
      e.source = source;
      throw e;
    }

    var template = function(data) {
      return render.call(this, data, _);
    };

    // Provide the compiled source as a convenience for precompilation.
    var argument = settings.variable || 'obj';
    template.source = 'function(' + argument + '){\n' + source + '}';

    return template;
  };

  // Add a "chain" function. Start chaining a wrapped Underscore object.
  _.chain = function(obj) {
    var instance = _(obj);
    instance._chain = true;
    return instance;
  };

  // OOP
  // ---------------
  // If Underscore is called as a function, it returns a wrapped object that
  // can be used OO-style. This wrapper holds altered versions of all the
  // underscore functions. Wrapped objects may be chained.

  // Helper function to continue chaining intermediate results.
  var result = function(instance, obj) {
    return instance._chain ? _(obj).chain() : obj;
  };

  // Add your own custom functions to the Underscore object.
  _.mixin = function(obj) {
    _.each(_.functions(obj), function(name) {
      var func = _[name] = obj[name];
      _.prototype[name] = function() {
        var args = [this._wrapped];
        push.apply(args, arguments);
        return result(this, func.apply(_, args));
      };
    });
  };

  // Add all of the Underscore functions to the wrapper object.
  _.mixin(_);

  // Add all mutator Array functions to the wrapper.
  _.each(['pop', 'push', 'reverse', 'shift', 'sort', 'splice', 'unshift'], function(name) {
    var method = ArrayProto[name];
    _.prototype[name] = function() {
      var obj = this._wrapped;
      method.apply(obj, arguments);
      if ((name === 'shift' || name === 'splice') && obj.length === 0) delete obj[0];
      return result(this, obj);
    };
  });

  // Add all accessor Array functions to the wrapper.
  _.each(['concat', 'join', 'slice'], function(name) {
    var method = ArrayProto[name];
    _.prototype[name] = function() {
      return result(this, method.apply(this._wrapped, arguments));
    };
  });

  // Extracts the result from a wrapped and chained object.
  _.prototype.value = function() {
    return this._wrapped;
  };

  // Provide unwrapping proxy for some methods used in engine operations
  // such as arithmetic and JSON stringification.
  _.prototype.valueOf = _.prototype.toJSON = _.prototype.value;

  _.prototype.toString = function() {
    return '' + this._wrapped;
  };

  // AMD registration happens at the end for compatibility with AMD loaders
  // that may not enforce next-turn semantics on modules. Even though general
  // practice for AMD registration is to be anonymous, underscore registers
  // as a named module because, like jQuery, it is a base library that is
  // popular enough to be bundled in a third party lib, but not be part of
  // an AMD load request. Those cases could generate an error when an
  // anonymous define() is called outside of a loader request.
  if (typeof define === 'function' && define.amd) {
    define('underscore', [], function() {
      return _;
    });
  }
}.call(this));

/*!
 * Vue.js v1.0.26
 * (c) 2016 Evan You
 * Released under the MIT License.
 */
(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
  typeof define === 'function' && define.amd ? define(factory) :
  (global.Vue = factory());
}(this, function () { 'use strict';

  function set(obj, key, val) {
    if (hasOwn(obj, key)) {
      obj[key] = val;
      return;
    }
    if (obj._isVue) {
      set(obj._data, key, val);
      return;
    }
    var ob = obj.__ob__;
    if (!ob) {
      obj[key] = val;
      return;
    }
    ob.convert(key, val);
    ob.dep.notify();
    if (ob.vms) {
      var i = ob.vms.length;
      while (i--) {
        var vm = ob.vms[i];
        vm._proxy(key);
        vm._digest();
      }
    }
    return val;
  }

  /**
   * Delete a property and trigger change if necessary.
   *
   * @param {Object} obj
   * @param {String} key
   */

  function del(obj, key) {
    if (!hasOwn(obj, key)) {
      return;
    }
    delete obj[key];
    var ob = obj.__ob__;
    if (!ob) {
      if (obj._isVue) {
        delete obj._data[key];
        obj._digest();
      }
      return;
    }
    ob.dep.notify();
    if (ob.vms) {
      var i = ob.vms.length;
      while (i--) {
        var vm = ob.vms[i];
        vm._unproxy(key);
        vm._digest();
      }
    }
  }

  var hasOwnProperty = Object.prototype.hasOwnProperty;
  /**
   * Check whether the object has the property.
   *
   * @param {Object} obj
   * @param {String} key
   * @return {Boolean}
   */

  function hasOwn(obj, key) {
    return hasOwnProperty.call(obj, key);
  }

  /**
   * Check if an expression is a literal value.
   *
   * @param {String} exp
   * @return {Boolean}
   */

  var literalValueRE = /^\s?(true|false|-?[\d\.]+|'[^']*'|"[^"]*")\s?$/;

  function isLiteral(exp) {
    return literalValueRE.test(exp);
  }

  /**
   * Check if a string starts with $ or _
   *
   * @param {String} str
   * @return {Boolean}
   */

  function isReserved(str) {
    var c = (str + '').charCodeAt(0);
    return c === 0x24 || c === 0x5F;
  }

  /**
   * Guard text output, make sure undefined outputs
   * empty string
   *
   * @param {*} value
   * @return {String}
   */

  function _toString(value) {
    return value == null ? '' : value.toString();
  }

  /**
   * Check and convert possible numeric strings to numbers
   * before setting back to data
   *
   * @param {*} value
   * @return {*|Number}
   */

  function toNumber(value) {
    if (typeof value !== 'string') {
      return value;
    } else {
      var parsed = Number(value);
      return isNaN(parsed) ? value : parsed;
    }
  }

  /**
   * Convert string boolean literals into real booleans.
   *
   * @param {*} value
   * @return {*|Boolean}
   */

  function toBoolean(value) {
    return value === 'true' ? true : value === 'false' ? false : value;
  }

  /**
   * Strip quotes from a string
   *
   * @param {String} str
   * @return {String | false}
   */

  function stripQuotes(str) {
    var a = str.charCodeAt(0);
    var b = str.charCodeAt(str.length - 1);
    return a === b && (a === 0x22 || a === 0x27) ? str.slice(1, -1) : str;
  }

  /**
   * Camelize a hyphen-delmited string.
   *
   * @param {String} str
   * @return {String}
   */

  var camelizeRE = /-(\w)/g;

  function camelize(str) {
    return str.replace(camelizeRE, toUpper);
  }

  function toUpper(_, c) {
    return c ? c.toUpperCase() : '';
  }

  /**
   * Hyphenate a camelCase string.
   *
   * @param {String} str
   * @return {String}
   */

  var hyphenateRE = /([a-z\d])([A-Z])/g;

  function hyphenate(str) {
    return str.replace(hyphenateRE, '$1-$2').toLowerCase();
  }

  /**
   * Converts hyphen/underscore/slash delimitered names into
   * camelized classNames.
   *
   * e.g. my-component => MyComponent
   *      some_else    => SomeElse
   *      some/comp    => SomeComp
   *
   * @param {String} str
   * @return {String}
   */

  var classifyRE = /(?:^|[-_\/])(\w)/g;

  function classify(str) {
    return str.replace(classifyRE, toUpper);
  }

  /**
   * Simple bind, faster than native
   *
   * @param {Function} fn
   * @param {Object} ctx
   * @return {Function}
   */

  function bind(fn, ctx) {
    return function (a) {
      var l = arguments.length;
      return l ? l > 1 ? fn.apply(ctx, arguments) : fn.call(ctx, a) : fn.call(ctx);
    };
  }

  /**
   * Convert an Array-like object to a real Array.
   *
   * @param {Array-like} list
   * @param {Number} [start] - start index
   * @return {Array}
   */

  function toArray(list, start) {
    start = start || 0;
    var i = list.length - start;
    var ret = new Array(i);
    while (i--) {
      ret[i] = list[i + start];
    }
    return ret;
  }

  /**
   * Mix properties into target object.
   *
   * @param {Object} to
   * @param {Object} from
   */

  function extend(to, from) {
    var keys = Object.keys(from);
    var i = keys.length;
    while (i--) {
      to[keys[i]] = from[keys[i]];
    }
    return to;
  }

  /**
   * Quick object check - this is primarily used to tell
   * Objects from primitive values when we know the value
   * is a JSON-compliant type.
   *
   * @param {*} obj
   * @return {Boolean}
   */

  function isObject(obj) {
    return obj !== null && typeof obj === 'object';
  }

  /**
   * Strict object type check. Only returns true
   * for plain JavaScript objects.
   *
   * @param {*} obj
   * @return {Boolean}
   */

  var toString = Object.prototype.toString;
  var OBJECT_STRING = '[object Object]';

  function isPlainObject(obj) {
    return toString.call(obj) === OBJECT_STRING;
  }

  /**
   * Array type check.
   *
   * @param {*} obj
   * @return {Boolean}
   */

  var isArray = Array.isArray;

  /**
   * Define a property.
   *
   * @param {Object} obj
   * @param {String} key
   * @param {*} val
   * @param {Boolean} [enumerable]
   */

  function def(obj, key, val, enumerable) {
    Object.defineProperty(obj, key, {
      value: val,
      enumerable: !!enumerable,
      writable: true,
      configurable: true
    });
  }

  /**
   * Debounce a function so it only gets called after the
   * input stops arriving after the given wait period.
   *
   * @param {Function} func
   * @param {Number} wait
   * @return {Function} - the debounced function
   */

  function _debounce(func, wait) {
    var timeout, args, context, timestamp, result;
    var later = function later() {
      var last = Date.now() - timestamp;
      if (last < wait && last >= 0) {
        timeout = setTimeout(later, wait - last);
      } else {
        timeout = null;
        result = func.apply(context, args);
        if (!timeout) context = args = null;
      }
    };
    return function () {
      context = this;
      args = arguments;
      timestamp = Date.now();
      if (!timeout) {
        timeout = setTimeout(later, wait);
      }
      return result;
    };
  }

  /**
   * Manual indexOf because it's slightly faster than
   * native.
   *
   * @param {Array} arr
   * @param {*} obj
   */

  function indexOf(arr, obj) {
    var i = arr.length;
    while (i--) {
      if (arr[i] === obj) return i;
    }
    return -1;
  }

  /**
   * Make a cancellable version of an async callback.
   *
   * @param {Function} fn
   * @return {Function}
   */

  function cancellable(fn) {
    var cb = function cb() {
      if (!cb.cancelled) {
        return fn.apply(this, arguments);
      }
    };
    cb.cancel = function () {
      cb.cancelled = true;
    };
    return cb;
  }

  /**
   * Check if two values are loosely equal - that is,
   * if they are plain objects, do they have the same shape?
   *
   * @param {*} a
   * @param {*} b
   * @return {Boolean}
   */

  function looseEqual(a, b) {
    /* eslint-disable eqeqeq */
    return a == b || (isObject(a) && isObject(b) ? JSON.stringify(a) === JSON.stringify(b) : false);
    /* eslint-enable eqeqeq */
  }

  var hasProto = ('__proto__' in {});

  // Browser environment sniffing
  var inBrowser = typeof window !== 'undefined' && Object.prototype.toString.call(window) !== '[object Object]';

  // detect devtools
  var devtools = inBrowser && window.__VUE_DEVTOOLS_GLOBAL_HOOK__;

  // UA sniffing for working around browser-specific quirks
  var UA = inBrowser && window.navigator.userAgent.toLowerCase();
  var isIE = UA && UA.indexOf('trident') > 0;
  var isIE9 = UA && UA.indexOf('msie 9.0') > 0;
  var isAndroid = UA && UA.indexOf('android') > 0;
  var isIos = UA && /(iphone|ipad|ipod|ios)/i.test(UA);
  var iosVersionMatch = isIos && UA.match(/os ([\d_]+)/);
  var iosVersion = iosVersionMatch && iosVersionMatch[1].split('_');

  // detecting iOS UIWebView by indexedDB
  var hasMutationObserverBug = iosVersion && Number(iosVersion[0]) >= 9 && Number(iosVersion[1]) >= 3 && !window.indexedDB;

  var transitionProp = undefined;
  var transitionEndEvent = undefined;
  var animationProp = undefined;
  var animationEndEvent = undefined;

  // Transition property/event sniffing
  if (inBrowser && !isIE9) {
    var isWebkitTrans = window.ontransitionend === undefined && window.onwebkittransitionend !== undefined;
    var isWebkitAnim = window.onanimationend === undefined && window.onwebkitanimationend !== undefined;
    transitionProp = isWebkitTrans ? 'WebkitTransition' : 'transition';
    transitionEndEvent = isWebkitTrans ? 'webkitTransitionEnd' : 'transitionend';
    animationProp = isWebkitAnim ? 'WebkitAnimation' : 'animation';
    animationEndEvent = isWebkitAnim ? 'webkitAnimationEnd' : 'animationend';
  }

  /**
   * Defer a task to execute it asynchronously. Ideally this
   * should be executed as a microtask, so we leverage
   * MutationObserver if it's available, and fallback to
   * setTimeout(0).
   *
   * @param {Function} cb
   * @param {Object} ctx
   */

  var nextTick = (function () {
    var callbacks = [];
    var pending = false;
    var timerFunc;
    function nextTickHandler() {
      pending = false;
      var copies = callbacks.slice(0);
      callbacks = [];
      for (var i = 0; i < copies.length; i++) {
        copies[i]();
      }
    }

    /* istanbul ignore if */
    if (typeof MutationObserver !== 'undefined' && !hasMutationObserverBug) {
      var counter = 1;
      var observer = new MutationObserver(nextTickHandler);
      var textNode = document.createTextNode(counter);
      observer.observe(textNode, {
        characterData: true
      });
      timerFunc = function () {
        counter = (counter + 1) % 2;
        textNode.data = counter;
      };
    } else {
      // webpack attempts to inject a shim for setImmediate
      // if it is used as a global, so we have to work around that to
      // avoid bundling unnecessary code.
      var context = inBrowser ? window : typeof global !== 'undefined' ? global : {};
      timerFunc = context.setImmediate || setTimeout;
    }
    return function (cb, ctx) {
      var func = ctx ? function () {
        cb.call(ctx);
      } : cb;
      callbacks.push(func);
      if (pending) return;
      pending = true;
      timerFunc(nextTickHandler, 0);
    };
  })();

  var _Set = undefined;
  /* istanbul ignore if */
  if (typeof Set !== 'undefined' && Set.toString().match(/native code/)) {
    // use native Set when available.
    _Set = Set;
  } else {
    // a non-standard Set polyfill that only works with primitive keys.
    _Set = function () {
      this.set = Object.create(null);
    };
    _Set.prototype.has = function (key) {
      return this.set[key] !== undefined;
    };
    _Set.prototype.add = function (key) {
      this.set[key] = 1;
    };
    _Set.prototype.clear = function () {
      this.set = Object.create(null);
    };
  }

  function Cache(limit) {
    this.size = 0;
    this.limit = limit;
    this.head = this.tail = undefined;
    this._keymap = Object.create(null);
  }

  var p = Cache.prototype;

  /**
   * Put <value> into the cache associated with <key>.
   * Returns the entry which was removed to make room for
   * the new entry. Otherwise undefined is returned.
   * (i.e. if there was enough room already).
   *
   * @param {String} key
   * @param {*} value
   * @return {Entry|undefined}
   */

  p.put = function (key, value) {
    var removed;

    var entry = this.get(key, true);
    if (!entry) {
      if (this.size === this.limit) {
        removed = this.shift();
      }
      entry = {
        key: key
      };
      this._keymap[key] = entry;
      if (this.tail) {
        this.tail.newer = entry;
        entry.older = this.tail;
      } else {
        this.head = entry;
      }
      this.tail = entry;
      this.size++;
    }
    entry.value = value;

    return removed;
  };

  /**
   * Purge the least recently used (oldest) entry from the
   * cache. Returns the removed entry or undefined if the
   * cache was empty.
   */

  p.shift = function () {
    var entry = this.head;
    if (entry) {
      this.head = this.head.newer;
      this.head.older = undefined;
      entry.newer = entry.older = undefined;
      this._keymap[entry.key] = undefined;
      this.size--;
    }
    return entry;
  };

  /**
   * Get and register recent use of <key>. Returns the value
   * associated with <key> or undefined if not in cache.
   *
   * @param {String} key
   * @param {Boolean} returnEntry
   * @return {Entry|*}
   */

  p.get = function (key, returnEntry) {
    var entry = this._keymap[key];
    if (entry === undefined) return;
    if (entry === this.tail) {
      return returnEntry ? entry : entry.value;
    }
    // HEAD--------------TAIL
    //   <.older   .newer>
    //  <--- add direction --
    //   A  B  C  <D>  E
    if (entry.newer) {
      if (entry === this.head) {
        this.head = entry.newer;
      }
      entry.newer.older = entry.older; // C <-- E.
    }
    if (entry.older) {
      entry.older.newer = entry.newer; // C. --> E
    }
    entry.newer = undefined; // D --x
    entry.older = this.tail; // D. --> E
    if (this.tail) {
      this.tail.newer = entry; // E. <-- D
    }
    this.tail = entry;
    return returnEntry ? entry : entry.value;
  };

  var cache$1 = new Cache(1000);
  var filterTokenRE = /[^\s'"]+|'[^']*'|"[^"]*"/g;
  var reservedArgRE = /^in$|^-?\d+/;

  /**
   * Parser state
   */

  var str;
  var dir;
  var c;
  var prev;
  var i;
  var l;
  var lastFilterIndex;
  var inSingle;
  var inDouble;
  var curly;
  var square;
  var paren;
  /**
   * Push a filter to the current directive object
   */

  function pushFilter() {
    var exp = str.slice(lastFilterIndex, i).trim();
    var filter;
    if (exp) {
      filter = {};
      var tokens = exp.match(filterTokenRE);
      filter.name = tokens[0];
      if (tokens.length > 1) {
        filter.args = tokens.slice(1).map(processFilterArg);
      }
    }
    if (filter) {
      (dir.filters = dir.filters || []).push(filter);
    }
    lastFilterIndex = i + 1;
  }

  /**
   * Check if an argument is dynamic and strip quotes.
   *
   * @param {String} arg
   * @return {Object}
   */

  function processFilterArg(arg) {
    if (reservedArgRE.test(arg)) {
      return {
        value: toNumber(arg),
        dynamic: false
      };
    } else {
      var stripped = stripQuotes(arg);
      var dynamic = stripped === arg;
      return {
        value: dynamic ? arg : stripped,
        dynamic: dynamic
      };
    }
  }

  /**
   * Parse a directive value and extract the expression
   * and its filters into a descriptor.
   *
   * Example:
   *
   * "a + 1 | uppercase" will yield:
   * {
   *   expression: 'a + 1',
   *   filters: [
   *     { name: 'uppercase', args: null }
   *   ]
   * }
   *
   * @param {String} s
   * @return {Object}
   */

  function parseDirective(s) {
    var hit = cache$1.get(s);
    if (hit) {
      return hit;
    }

    // reset parser state
    str = s;
    inSingle = inDouble = false;
    curly = square = paren = 0;
    lastFilterIndex = 0;
    dir = {};

    for (i = 0, l = str.length; i < l; i++) {
      prev = c;
      c = str.charCodeAt(i);
      if (inSingle) {
        // check single quote
        if (c === 0x27 && prev !== 0x5C) inSingle = !inSingle;
      } else if (inDouble) {
        // check double quote
        if (c === 0x22 && prev !== 0x5C) inDouble = !inDouble;
      } else if (c === 0x7C && // pipe
      str.charCodeAt(i + 1) !== 0x7C && str.charCodeAt(i - 1) !== 0x7C) {
        if (dir.expression == null) {
          // first filter, end of expression
          lastFilterIndex = i + 1;
          dir.expression = str.slice(0, i).trim();
        } else {
          // already has filter
          pushFilter();
        }
      } else {
        switch (c) {
          case 0x22:
            inDouble = true;break; // "
          case 0x27:
            inSingle = true;break; // '
          case 0x28:
            paren++;break; // (
          case 0x29:
            paren--;break; // )
          case 0x5B:
            square++;break; // [
          case 0x5D:
            square--;break; // ]
          case 0x7B:
            curly++;break; // {
          case 0x7D:
            curly--;break; // }
        }
      }
    }

    if (dir.expression == null) {
      dir.expression = str.slice(0, i).trim();
    } else if (lastFilterIndex !== 0) {
      pushFilter();
    }

    cache$1.put(s, dir);
    return dir;
  }

var directive = Object.freeze({
    parseDirective: parseDirective
  });

  var regexEscapeRE = /[-.*+?^${}()|[\]\/\\]/g;
  var cache = undefined;
  var tagRE = undefined;
  var htmlRE = undefined;
  /**
   * Escape a string so it can be used in a RegExp
   * constructor.
   *
   * @param {String} str
   */

  function escapeRegex(str) {
    return str.replace(regexEscapeRE, '\\$&');
  }

  function compileRegex() {
    var open = escapeRegex(config.delimiters[0]);
    var close = escapeRegex(config.delimiters[1]);
    var unsafeOpen = escapeRegex(config.unsafeDelimiters[0]);
    var unsafeClose = escapeRegex(config.unsafeDelimiters[1]);
    tagRE = new RegExp(unsafeOpen + '((?:.|\\n)+?)' + unsafeClose + '|' + open + '((?:.|\\n)+?)' + close, 'g');
    htmlRE = new RegExp('^' + unsafeOpen + '((?:.|\\n)+?)' + unsafeClose + '$');
    // reset cache
    cache = new Cache(1000);
  }

  /**
   * Parse a template text string into an array of tokens.
   *
   * @param {String} text
   * @return {Array<Object> | null}
   *               - {String} type
   *               - {String} value
   *               - {Boolean} [html]
   *               - {Boolean} [oneTime]
   */

  function parseText(text) {
    if (!cache) {
      compileRegex();
    }
    var hit = cache.get(text);
    if (hit) {
      return hit;
    }
    if (!tagRE.test(text)) {
      return null;
    }
    var tokens = [];
    var lastIndex = tagRE.lastIndex = 0;
    var match, index, html, value, first, oneTime;
    /* eslint-disable no-cond-assign */
    while (match = tagRE.exec(text)) {
      /* eslint-enable no-cond-assign */
      index = match.index;
      // push text token
      if (index > lastIndex) {
        tokens.push({
          value: text.slice(lastIndex, index)
        });
      }
      // tag token
      html = htmlRE.test(match[0]);
      value = html ? match[1] : match[2];
      first = value.charCodeAt(0);
      oneTime = first === 42; // *
      value = oneTime ? value.slice(1) : value;
      tokens.push({
        tag: true,
        value: value.trim(),
        html: html,
        oneTime: oneTime
      });
      lastIndex = index + match[0].length;
    }
    if (lastIndex < text.length) {
      tokens.push({
        value: text.slice(lastIndex)
      });
    }
    cache.put(text, tokens);
    return tokens;
  }

  /**
   * Format a list of tokens into an expression.
   * e.g. tokens parsed from 'a {{b}} c' can be serialized
   * into one single expression as '"a " + b + " c"'.
   *
   * @param {Array} tokens
   * @param {Vue} [vm]
   * @return {String}
   */

  function tokensToExp(tokens, vm) {
    if (tokens.length > 1) {
      return tokens.map(function (token) {
        return formatToken(token, vm);
      }).join('+');
    } else {
      return formatToken(tokens[0], vm, true);
    }
  }

  /**
   * Format a single token.
   *
   * @param {Object} token
   * @param {Vue} [vm]
   * @param {Boolean} [single]
   * @return {String}
   */

  function formatToken(token, vm, single) {
    return token.tag ? token.oneTime && vm ? '"' + vm.$eval(token.value) + '"' : inlineFilters(token.value, single) : '"' + token.value + '"';
  }

  /**
   * For an attribute with multiple interpolation tags,
   * e.g. attr="some-{{thing | filter}}", in order to combine
   * the whole thing into a single watchable expression, we
   * have to inline those filters. This function does exactly
   * that. This is a bit hacky but it avoids heavy changes
   * to directive parser and watcher mechanism.
   *
   * @param {String} exp
   * @param {Boolean} single
   * @return {String}
   */

  var filterRE = /[^|]\|[^|]/;
  function inlineFilters(exp, single) {
    if (!filterRE.test(exp)) {
      return single ? exp : '(' + exp + ')';
    } else {
      var dir = parseDirective(exp);
      if (!dir.filters) {
        return '(' + exp + ')';
      } else {
        return 'this._applyFilters(' + dir.expression + // value
        ',null,' + // oldValue (null for read)
        JSON.stringify(dir.filters) + // filter descriptors
        ',false)'; // write?
      }
    }
  }

var text = Object.freeze({
    compileRegex: compileRegex,
    parseText: parseText,
    tokensToExp: tokensToExp
  });

  var delimiters = ['{{', '}}'];
  var unsafeDelimiters = ['{{{', '}}}'];

  var config = Object.defineProperties({

    /**
     * Whether to print debug messages.
     * Also enables stack trace for warnings.
     *
     * @type {Boolean}
     */

    debug: false,

    /**
     * Whether to suppress warnings.
     *
     * @type {Boolean}
     */

    silent: false,

    /**
     * Whether to use async rendering.
     */

    async: true,

    /**
     * Whether to warn against errors caught when evaluating
     * expressions.
     */

    warnExpressionErrors: true,

    /**
     * Whether to allow devtools inspection.
     * Disabled by default in production builds.
     */

    devtools: 'development' !== 'production',

    /**
     * Internal flag to indicate the delimiters have been
     * changed.
     *
     * @type {Boolean}
     */

    _delimitersChanged: true,

    /**
     * List of asset types that a component can own.
     *
     * @type {Array}
     */

    _assetTypes: ['component', 'directive', 'elementDirective', 'filter', 'transition', 'partial'],

    /**
     * prop binding modes
     */

    _propBindingModes: {
      ONE_WAY: 0,
      TWO_WAY: 1,
      ONE_TIME: 2
    },

    /**
     * Max circular updates allowed in a batcher flush cycle.
     */

    _maxUpdateCount: 100

  }, {
    delimiters: { /**
                   * Interpolation delimiters. Changing these would trigger
                   * the text parser to re-compile the regular expressions.
                   *
                   * @type {Array<String>}
                   */

      get: function get() {
        return delimiters;
      },
      set: function set(val) {
        delimiters = val;
        compileRegex();
      },
      configurable: true,
      enumerable: true
    },
    unsafeDelimiters: {
      get: function get() {
        return unsafeDelimiters;
      },
      set: function set(val) {
        unsafeDelimiters = val;
        compileRegex();
      },
      configurable: true,
      enumerable: true
    }
  });

  var warn = undefined;
  var formatComponentName = undefined;

  if ('development' !== 'production') {
    (function () {
      var hasConsole = typeof console !== 'undefined';

      warn = function (msg, vm) {
        if (hasConsole && !config.silent) {
          console.error('[Vue warn]: ' + msg + (vm ? formatComponentName(vm) : ''));
        }
      };

      formatComponentName = function (vm) {
        var name = vm._isVue ? vm.$options.name : vm.name;
        return name ? ' (found in component: <' + hyphenate(name) + '>)' : '';
      };
    })();
  }

  /**
   * Append with transition.
   *
   * @param {Element} el
   * @param {Element} target
   * @param {Vue} vm
   * @param {Function} [cb]
   */

  function appendWithTransition(el, target, vm, cb) {
    applyTransition(el, 1, function () {
      target.appendChild(el);
    }, vm, cb);
  }

  /**
   * InsertBefore with transition.
   *
   * @param {Element} el
   * @param {Element} target
   * @param {Vue} vm
   * @param {Function} [cb]
   */

  function beforeWithTransition(el, target, vm, cb) {
    applyTransition(el, 1, function () {
      before(el, target);
    }, vm, cb);
  }

  /**
   * Remove with transition.
   *
   * @param {Element} el
   * @param {Vue} vm
   * @param {Function} [cb]
   */

  function removeWithTransition(el, vm, cb) {
    applyTransition(el, -1, function () {
      remove(el);
    }, vm, cb);
  }

  /**
   * Apply transitions with an operation callback.
   *
   * @param {Element} el
   * @param {Number} direction
   *                  1: enter
   *                 -1: leave
   * @param {Function} op - the actual DOM operation
   * @param {Vue} vm
   * @param {Function} [cb]
   */

  function applyTransition(el, direction, op, vm, cb) {
    var transition = el.__v_trans;
    if (!transition ||
    // skip if there are no js hooks and CSS transition is
    // not supported
    !transition.hooks && !transitionEndEvent ||
    // skip transitions for initial compile
    !vm._isCompiled ||
    // if the vm is being manipulated by a parent directive
    // during the parent's compilation phase, skip the
    // animation.
    vm.$parent && !vm.$parent._isCompiled) {
      op();
      if (cb) cb();
      return;
    }
    var action = direction > 0 ? 'enter' : 'leave';
    transition[action](op, cb);
  }

var transition = Object.freeze({
    appendWithTransition: appendWithTransition,
    beforeWithTransition: beforeWithTransition,
    removeWithTransition: removeWithTransition,
    applyTransition: applyTransition
  });

  /**
   * Query an element selector if it's not an element already.
   *
   * @param {String|Element} el
   * @return {Element}
   */

  function query(el) {
    if (typeof el === 'string') {
      var selector = el;
      el = document.querySelector(el);
      if (!el) {
        'development' !== 'production' && warn('Cannot find element: ' + selector);
      }
    }
    return el;
  }

  /**
   * Check if a node is in the document.
   * Note: document.documentElement.contains should work here
   * but always returns false for comment nodes in phantomjs,
   * making unit tests difficult. This is fixed by doing the
   * contains() check on the node's parentNode instead of
   * the node itself.
   *
   * @param {Node} node
   * @return {Boolean}
   */

  function inDoc(node) {
    if (!node) return false;
    var doc = node.ownerDocument.documentElement;
    var parent = node.parentNode;
    return doc === node || doc === parent || !!(parent && parent.nodeType === 1 && doc.contains(parent));
  }

  /**
   * Get and remove an attribute from a node.
   *
   * @param {Node} node
   * @param {String} _attr
   */

  function getAttr(node, _attr) {
    var val = node.getAttribute(_attr);
    if (val !== null) {
      node.removeAttribute(_attr);
    }
    return val;
  }

  /**
   * Get an attribute with colon or v-bind: prefix.
   *
   * @param {Node} node
   * @param {String} name
   * @return {String|null}
   */

  function getBindAttr(node, name) {
    var val = getAttr(node, ':' + name);
    if (val === null) {
      val = getAttr(node, 'v-bind:' + name);
    }
    return val;
  }

  /**
   * Check the presence of a bind attribute.
   *
   * @param {Node} node
   * @param {String} name
   * @return {Boolean}
   */

  function hasBindAttr(node, name) {
    return node.hasAttribute(name) || node.hasAttribute(':' + name) || node.hasAttribute('v-bind:' + name);
  }

  /**
   * Insert el before target
   *
   * @param {Element} el
   * @param {Element} target
   */

  function before(el, target) {
    target.parentNode.insertBefore(el, target);
  }

  /**
   * Insert el after target
   *
   * @param {Element} el
   * @param {Element} target
   */

  function after(el, target) {
    if (target.nextSibling) {
      before(el, target.nextSibling);
    } else {
      target.parentNode.appendChild(el);
    }
  }

  /**
   * Remove el from DOM
   *
   * @param {Element} el
   */

  function remove(el) {
    el.parentNode.removeChild(el);
  }

  /**
   * Prepend el to target
   *
   * @param {Element} el
   * @param {Element} target
   */

  function prepend(el, target) {
    if (target.firstChild) {
      before(el, target.firstChild);
    } else {
      target.appendChild(el);
    }
  }

  /**
   * Replace target with el
   *
   * @param {Element} target
   * @param {Element} el
   */

  function replace(target, el) {
    var parent = target.parentNode;
    if (parent) {
      parent.replaceChild(el, target);
    }
  }

  /**
   * Add event listener shorthand.
   *
   * @param {Element} el
   * @param {String} event
   * @param {Function} cb
   * @param {Boolean} [useCapture]
   */

  function on(el, event, cb, useCapture) {
    el.addEventListener(event, cb, useCapture);
  }

  /**
   * Remove event listener shorthand.
   *
   * @param {Element} el
   * @param {String} event
   * @param {Function} cb
   */

  function off(el, event, cb) {
    el.removeEventListener(event, cb);
  }

  /**
   * For IE9 compat: when both class and :class are present
   * getAttribute('class') returns wrong value...
   *
   * @param {Element} el
   * @return {String}
   */

  function getClass(el) {
    var classname = el.className;
    if (typeof classname === 'object') {
      classname = classname.baseVal || '';
    }
    return classname;
  }

  /**
   * In IE9, setAttribute('class') will result in empty class
   * if the element also has the :class attribute; However in
   * PhantomJS, setting `className` does not work on SVG elements...
   * So we have to do a conditional check here.
   *
   * @param {Element} el
   * @param {String} cls
   */

  function setClass(el, cls) {
    /* istanbul ignore if */
    if (isIE9 && !/svg$/.test(el.namespaceURI)) {
      el.className = cls;
    } else {
      el.setAttribute('class', cls);
    }
  }

  /**
   * Add class with compatibility for IE & SVG
   *
   * @param {Element} el
   * @param {String} cls
   */

  function addClass(el, cls) {
    if (el.classList) {
      el.classList.add(cls);
    } else {
      var cur = ' ' + getClass(el) + ' ';
      if (cur.indexOf(' ' + cls + ' ') < 0) {
        setClass(el, (cur + cls).trim());
      }
    }
  }

  /**
   * Remove class with compatibility for IE & SVG
   *
   * @param {Element} el
   * @param {String} cls
   */

  function removeClass(el, cls) {
    if (el.classList) {
      el.classList.remove(cls);
    } else {
      var cur = ' ' + getClass(el) + ' ';
      var tar = ' ' + cls + ' ';
      while (cur.indexOf(tar) >= 0) {
        cur = cur.replace(tar, ' ');
      }
      setClass(el, cur.trim());
    }
    if (!el.className) {
      el.removeAttribute('class');
    }
  }

  /**
   * Extract raw content inside an element into a temporary
   * container div
   *
   * @param {Element} el
   * @param {Boolean} asFragment
   * @return {Element|DocumentFragment}
   */

  function extractContent(el, asFragment) {
    var child;
    var rawContent;
    /* istanbul ignore if */
    if (isTemplate(el) && isFragment(el.content)) {
      el = el.content;
    }
    if (el.hasChildNodes()) {
      trimNode(el);
      rawContent = asFragment ? document.createDocumentFragment() : document.createElement('div');
      /* eslint-disable no-cond-assign */
      while (child = el.firstChild) {
        /* eslint-enable no-cond-assign */
        rawContent.appendChild(child);
      }
    }
    return rawContent;
  }

  /**
   * Trim possible empty head/tail text and comment
   * nodes inside a parent.
   *
   * @param {Node} node
   */

  function trimNode(node) {
    var child;
    /* eslint-disable no-sequences */
    while ((child = node.firstChild, isTrimmable(child))) {
      node.removeChild(child);
    }
    while ((child = node.lastChild, isTrimmable(child))) {
      node.removeChild(child);
    }
    /* eslint-enable no-sequences */
  }

  function isTrimmable(node) {
    return node && (node.nodeType === 3 && !node.data.trim() || node.nodeType === 8);
  }

  /**
   * Check if an element is a template tag.
   * Note if the template appears inside an SVG its tagName
   * will be in lowercase.
   *
   * @param {Element} el
   */

  function isTemplate(el) {
    return el.tagName && el.tagName.toLowerCase() === 'template';
  }

  /**
   * Create an "anchor" for performing dom insertion/removals.
   * This is used in a number of scenarios:
   * - fragment instance
   * - v-html
   * - v-if
   * - v-for
   * - component
   *
   * @param {String} content
   * @param {Boolean} persist - IE trashes empty textNodes on
   *                            cloneNode(true), so in certain
   *                            cases the anchor needs to be
   *                            non-empty to be persisted in
   *                            templates.
   * @return {Comment|Text}
   */

  function createAnchor(content, persist) {
    var anchor = config.debug ? document.createComment(content) : document.createTextNode(persist ? ' ' : '');
    anchor.__v_anchor = true;
    return anchor;
  }

  /**
   * Find a component ref attribute that starts with $.
   *
   * @param {Element} node
   * @return {String|undefined}
   */

  var refRE = /^v-ref:/;

  function findRef(node) {
    if (node.hasAttributes()) {
      var attrs = node.attributes;
      for (var i = 0, l = attrs.length; i < l; i++) {
        var name = attrs[i].name;
        if (refRE.test(name)) {
          return camelize(name.replace(refRE, ''));
        }
      }
    }
  }

  /**
   * Map a function to a range of nodes .
   *
   * @param {Node} node
   * @param {Node} end
   * @param {Function} op
   */

  function mapNodeRange(node, end, op) {
    var next;
    while (node !== end) {
      next = node.nextSibling;
      op(node);
      node = next;
    }
    op(end);
  }

  /**
   * Remove a range of nodes with transition, store
   * the nodes in a fragment with correct ordering,
   * and call callback when done.
   *
   * @param {Node} start
   * @param {Node} end
   * @param {Vue} vm
   * @param {DocumentFragment} frag
   * @param {Function} cb
   */

  function removeNodeRange(start, end, vm, frag, cb) {
    var done = false;
    var removed = 0;
    var nodes = [];
    mapNodeRange(start, end, function (node) {
      if (node === end) done = true;
      nodes.push(node);
      removeWithTransition(node, vm, onRemoved);
    });
    function onRemoved() {
      removed++;
      if (done && removed >= nodes.length) {
        for (var i = 0; i < nodes.length; i++) {
          frag.appendChild(nodes[i]);
        }
        cb && cb();
      }
    }
  }

  /**
   * Check if a node is a DocumentFragment.
   *
   * @param {Node} node
   * @return {Boolean}
   */

  function isFragment(node) {
    return node && node.nodeType === 11;
  }

  /**
   * Get outerHTML of elements, taking care
   * of SVG elements in IE as well.
   *
   * @param {Element} el
   * @return {String}
   */

  function getOuterHTML(el) {
    if (el.outerHTML) {
      return el.outerHTML;
    } else {
      var container = document.createElement('div');
      container.appendChild(el.cloneNode(true));
      return container.innerHTML;
    }
  }

  var commonTagRE = /^(div|p|span|img|a|b|i|br|ul|ol|li|h1|h2|h3|h4|h5|h6|code|pre|table|th|td|tr|form|label|input|select|option|nav|article|section|header|footer)$/i;
  var reservedTagRE = /^(slot|partial|component)$/i;

  var isUnknownElement = undefined;
  if ('development' !== 'production') {
    isUnknownElement = function (el, tag) {
      if (tag.indexOf('-') > -1) {
        // http://stackoverflow.com/a/28210364/1070244
        return el.constructor === window.HTMLUnknownElement || el.constructor === window.HTMLElement;
      } else {
        return (/HTMLUnknownElement/.test(el.toString()) &&
          // Chrome returns unknown for several HTML5 elements.
          // https://code.google.com/p/chromium/issues/detail?id=540526
          // Firefox returns unknown for some "Interactive elements."
          !/^(data|time|rtc|rb|details|dialog|summary)$/.test(tag)
        );
      }
    };
  }

  /**
   * Check if an element is a component, if yes return its
   * component id.
   *
   * @param {Element} el
   * @param {Object} options
   * @return {Object|undefined}
   */

  function checkComponentAttr(el, options) {
    var tag = el.tagName.toLowerCase();
    var hasAttrs = el.hasAttributes();
    if (!commonTagRE.test(tag) && !reservedTagRE.test(tag)) {
      if (resolveAsset(options, 'components', tag)) {
        return { id: tag };
      } else {
        var is = hasAttrs && getIsBinding(el, options);
        if (is) {
          return is;
        } else if ('development' !== 'production') {
          var expectedTag = options._componentNameMap && options._componentNameMap[tag];
          if (expectedTag) {
            warn('Unknown custom element: <' + tag + '> - ' + 'did you mean <' + expectedTag + '>? ' + 'HTML is case-insensitive, remember to use kebab-case in templates.');
          } else if (isUnknownElement(el, tag)) {
            warn('Unknown custom element: <' + tag + '> - did you ' + 'register the component correctly? For recursive components, ' + 'make sure to provide the "name" option.');
          }
        }
      }
    } else if (hasAttrs) {
      return getIsBinding(el, options);
    }
  }

  /**
   * Get "is" binding from an element.
   *
   * @param {Element} el
   * @param {Object} options
   * @return {Object|undefined}
   */

  function getIsBinding(el, options) {
    // dynamic syntax
    var exp = el.getAttribute('is');
    if (exp != null) {
      if (resolveAsset(options, 'components', exp)) {
        el.removeAttribute('is');
        return { id: exp };
      }
    } else {
      exp = getBindAttr(el, 'is');
      if (exp != null) {
        return { id: exp, dynamic: true };
      }
    }
  }

  /**
   * Option overwriting strategies are functions that handle
   * how to merge a parent option value and a child option
   * value into the final value.
   *
   * All strategy functions follow the same signature:
   *
   * @param {*} parentVal
   * @param {*} childVal
   * @param {Vue} [vm]
   */

  var strats = config.optionMergeStrategies = Object.create(null);

  /**
   * Helper that recursively merges two data objects together.
   */

  function mergeData(to, from) {
    var key, toVal, fromVal;
    for (key in from) {
      toVal = to[key];
      fromVal = from[key];
      if (!hasOwn(to, key)) {
        set(to, key, fromVal);
      } else if (isObject(toVal) && isObject(fromVal)) {
        mergeData(toVal, fromVal);
      }
    }
    return to;
  }

  /**
   * Data
   */

  strats.data = function (parentVal, childVal, vm) {
    if (!vm) {
      // in a Vue.extend merge, both should be functions
      if (!childVal) {
        return parentVal;
      }
      if (typeof childVal !== 'function') {
        'development' !== 'production' && warn('The "data" option should be a function ' + 'that returns a per-instance value in component ' + 'definitions.', vm);
        return parentVal;
      }
      if (!parentVal) {
        return childVal;
      }
      // when parentVal & childVal are both present,
      // we need to return a function that returns the
      // merged result of both functions... no need to
      // check if parentVal is a function here because
      // it has to be a function to pass previous merges.
      return function mergedDataFn() {
        return mergeData(childVal.call(this), parentVal.call(this));
      };
    } else if (parentVal || childVal) {
      return function mergedInstanceDataFn() {
        // instance merge
        var instanceData = typeof childVal === 'function' ? childVal.call(vm) : childVal;
        var defaultData = typeof parentVal === 'function' ? parentVal.call(vm) : undefined;
        if (instanceData) {
          return mergeData(instanceData, defaultData);
        } else {
          return defaultData;
        }
      };
    }
  };

  /**
   * El
   */

  strats.el = function (parentVal, childVal, vm) {
    if (!vm && childVal && typeof childVal !== 'function') {
      'development' !== 'production' && warn('The "el" option should be a function ' + 'that returns a per-instance value in component ' + 'definitions.', vm);
      return;
    }
    var ret = childVal || parentVal;
    // invoke the element factory if this is instance merge
    return vm && typeof ret === 'function' ? ret.call(vm) : ret;
  };

  /**
   * Hooks and param attributes are merged as arrays.
   */

  strats.init = strats.created = strats.ready = strats.attached = strats.detached = strats.beforeCompile = strats.compiled = strats.beforeDestroy = strats.destroyed = strats.activate = function (parentVal, childVal) {
    return childVal ? parentVal ? parentVal.concat(childVal) : isArray(childVal) ? childVal : [childVal] : parentVal;
  };

  /**
   * Assets
   *
   * When a vm is present (instance creation), we need to do
   * a three-way merge between constructor options, instance
   * options and parent options.
   */

  function mergeAssets(parentVal, childVal) {
    var res = Object.create(parentVal || null);
    return childVal ? extend(res, guardArrayAssets(childVal)) : res;
  }

  config._assetTypes.forEach(function (type) {
    strats[type + 's'] = mergeAssets;
  });

  /**
   * Events & Watchers.
   *
   * Events & watchers hashes should not overwrite one
   * another, so we merge them as arrays.
   */

  strats.watch = strats.events = function (parentVal, childVal) {
    if (!childVal) return parentVal;
    if (!parentVal) return childVal;
    var ret = {};
    extend(ret, parentVal);
    for (var key in childVal) {
      var parent = ret[key];
      var child = childVal[key];
      if (parent && !isArray(parent)) {
        parent = [parent];
      }
      ret[key] = parent ? parent.concat(child) : [child];
    }
    return ret;
  };

  /**
   * Other object hashes.
   */

  strats.props = strats.methods = strats.computed = function (parentVal, childVal) {
    if (!childVal) return parentVal;
    if (!parentVal) return childVal;
    var ret = Object.create(null);
    extend(ret, parentVal);
    extend(ret, childVal);
    return ret;
  };

  /**
   * Default strategy.
   */

  var defaultStrat = function defaultStrat(parentVal, childVal) {
    return childVal === undefined ? parentVal : childVal;
  };

  /**
   * Make sure component options get converted to actual
   * constructors.
   *
   * @param {Object} options
   */

  function guardComponents(options) {
    if (options.components) {
      var components = options.components = guardArrayAssets(options.components);
      var ids = Object.keys(components);
      var def;
      if ('development' !== 'production') {
        var map = options._componentNameMap = {};
      }
      for (var i = 0, l = ids.length; i < l; i++) {
        var key = ids[i];
        if (commonTagRE.test(key) || reservedTagRE.test(key)) {
          'development' !== 'production' && warn('Do not use built-in or reserved HTML elements as component ' + 'id: ' + key);
          continue;
        }
        // record a all lowercase <-> kebab-case mapping for
        // possible custom element case error warning
        if ('development' !== 'production') {
          map[key.replace(/-/g, '').toLowerCase()] = hyphenate(key);
        }
        def = components[key];
        if (isPlainObject(def)) {
          components[key] = Vue.extend(def);
        }
      }
    }
  }

  /**
   * Ensure all props option syntax are normalized into the
   * Object-based format.
   *
   * @param {Object} options
   */

  function guardProps(options) {
    var props = options.props;
    var i, val;
    if (isArray(props)) {
      options.props = {};
      i = props.length;
      while (i--) {
        val = props[i];
        if (typeof val === 'string') {
          options.props[val] = null;
        } else if (val.name) {
          options.props[val.name] = val;
        }
      }
    } else if (isPlainObject(props)) {
      var keys = Object.keys(props);
      i = keys.length;
      while (i--) {
        val = props[keys[i]];
        if (typeof val === 'function') {
          props[keys[i]] = { type: val };
        }
      }
    }
  }

  /**
   * Guard an Array-format assets option and converted it
   * into the key-value Object format.
   *
   * @param {Object|Array} assets
   * @return {Object}
   */

  function guardArrayAssets(assets) {
    if (isArray(assets)) {
      var res = {};
      var i = assets.length;
      var asset;
      while (i--) {
        asset = assets[i];
        var id = typeof asset === 'function' ? asset.options && asset.options.name || asset.id : asset.name || asset.id;
        if (!id) {
          'development' !== 'production' && warn('Array-syntax assets must provide a "name" or "id" field.');
        } else {
          res[id] = asset;
        }
      }
      return res;
    }
    return assets;
  }

  /**
   * Merge two option objects into a new one.
   * Core utility used in both instantiation and inheritance.
   *
   * @param {Object} parent
   * @param {Object} child
   * @param {Vue} [vm] - if vm is present, indicates this is
   *                     an instantiation merge.
   */

  function mergeOptions(parent, child, vm) {
    guardComponents(child);
    guardProps(child);
    if ('development' !== 'production') {
      if (child.propsData && !vm) {
        warn('propsData can only be used as an instantiation option.');
      }
    }
    var options = {};
    var key;
    if (child['extends']) {
      parent = typeof child['extends'] === 'function' ? mergeOptions(parent, child['extends'].options, vm) : mergeOptions(parent, child['extends'], vm);
    }
    if (child.mixins) {
      for (var i = 0, l = child.mixins.length; i < l; i++) {
        var mixin = child.mixins[i];
        var mixinOptions = mixin.prototype instanceof Vue ? mixin.options : mixin;
        parent = mergeOptions(parent, mixinOptions, vm);
      }
    }
    for (key in parent) {
      mergeField(key);
    }
    for (key in child) {
      if (!hasOwn(parent, key)) {
        mergeField(key);
      }
    }
    function mergeField(key) {
      var strat = strats[key] || defaultStrat;
      options[key] = strat(parent[key], child[key], vm, key);
    }
    return options;
  }

  /**
   * Resolve an asset.
   * This function is used because child instances need access
   * to assets defined in its ancestor chain.
   *
   * @param {Object} options
   * @param {String} type
   * @param {String} id
   * @param {Boolean} warnMissing
   * @return {Object|Function}
   */

  function resolveAsset(options, type, id, warnMissing) {
    /* istanbul ignore if */
    if (typeof id !== 'string') {
      return;
    }
    var assets = options[type];
    var camelizedId;
    var res = assets[id] ||
    // camelCase ID
    assets[camelizedId = camelize(id)] ||
    // Pascal Case ID
    assets[camelizedId.charAt(0).toUpperCase() + camelizedId.slice(1)];
    if ('development' !== 'production' && warnMissing && !res) {
      warn('Failed to resolve ' + type.slice(0, -1) + ': ' + id, options);
    }
    return res;
  }

  var uid$1 = 0;

  /**
   * A dep is an observable that can have multiple
   * directives subscribing to it.
   *
   * @constructor
   */
  function Dep() {
    this.id = uid$1++;
    this.subs = [];
  }

  // the current target watcher being evaluated.
  // this is globally unique because there could be only one
  // watcher being evaluated at any time.
  Dep.target = null;

  /**
   * Add a directive subscriber.
   *
   * @param {Directive} sub
   */

  Dep.prototype.addSub = function (sub) {
    this.subs.push(sub);
  };

  /**
   * Remove a directive subscriber.
   *
   * @param {Directive} sub
   */

  Dep.prototype.removeSub = function (sub) {
    this.subs.$remove(sub);
  };

  /**
   * Add self as a dependency to the target watcher.
   */

  Dep.prototype.depend = function () {
    Dep.target.addDep(this);
  };

  /**
   * Notify all subscribers of a new value.
   */

  Dep.prototype.notify = function () {
    // stablize the subscriber list first
    var subs = toArray(this.subs);
    for (var i = 0, l = subs.length; i < l; i++) {
      subs[i].update();
    }
  };

  var arrayProto = Array.prototype;
  var arrayMethods = Object.create(arrayProto)

  /**
   * Intercept mutating methods and emit events
   */

  ;['push', 'pop', 'shift', 'unshift', 'splice', 'sort', 'reverse'].forEach(function (method) {
    // cache original method
    var original = arrayProto[method];
    def(arrayMethods, method, function mutator() {
      // avoid leaking arguments:
      // http://jsperf.com/closure-with-arguments
      var i = arguments.length;
      var args = new Array(i);
      while (i--) {
        args[i] = arguments[i];
      }
      var result = original.apply(this, args);
      var ob = this.__ob__;
      var inserted;
      switch (method) {
        case 'push':
          inserted = args;
          break;
        case 'unshift':
          inserted = args;
          break;
        case 'splice':
          inserted = args.slice(2);
          break;
      }
      if (inserted) ob.observeArray(inserted);
      // notify change
      ob.dep.notify();
      return result;
    });
  });

  /**
   * Swap the element at the given index with a new value
   * and emits corresponding event.
   *
   * @param {Number} index
   * @param {*} val
   * @return {*} - replaced element
   */

  def(arrayProto, '$set', function $set(index, val) {
    if (index >= this.length) {
      this.length = Number(index) + 1;
    }
    return this.splice(index, 1, val)[0];
  });

  /**
   * Convenience method to remove the element at given index or target element reference.
   *
   * @param {*} item
   */

  def(arrayProto, '$remove', function $remove(item) {
    /* istanbul ignore if */
    if (!this.length) return;
    var index = indexOf(this, item);
    if (index > -1) {
      return this.splice(index, 1);
    }
  });

  var arrayKeys = Object.getOwnPropertyNames(arrayMethods);

  /**
   * By default, when a reactive property is set, the new value is
   * also converted to become reactive. However in certain cases, e.g.
   * v-for scope alias and props, we don't want to force conversion
   * because the value may be a nested value under a frozen data structure.
   *
   * So whenever we want to set a reactive property without forcing
   * conversion on the new value, we wrap that call inside this function.
   */

  var shouldConvert = true;

  function withoutConversion(fn) {
    shouldConvert = false;
    fn();
    shouldConvert = true;
  }

  /**
   * Observer class that are attached to each observed
   * object. Once attached, the observer converts target
   * object's property keys into getter/setters that
   * collect dependencies and dispatches updates.
   *
   * @param {Array|Object} value
   * @constructor
   */

  function Observer(value) {
    this.value = value;
    this.dep = new Dep();
    def(value, '__ob__', this);
    if (isArray(value)) {
      var augment = hasProto ? protoAugment : copyAugment;
      augment(value, arrayMethods, arrayKeys);
      this.observeArray(value);
    } else {
      this.walk(value);
    }
  }

  // Instance methods

  /**
   * Walk through each property and convert them into
   * getter/setters. This method should only be called when
   * value type is Object.
   *
   * @param {Object} obj
   */

  Observer.prototype.walk = function (obj) {
    var keys = Object.keys(obj);
    for (var i = 0, l = keys.length; i < l; i++) {
      this.convert(keys[i], obj[keys[i]]);
    }
  };

  /**
   * Observe a list of Array items.
   *
   * @param {Array} items
   */

  Observer.prototype.observeArray = function (items) {
    for (var i = 0, l = items.length; i < l; i++) {
      observe(items[i]);
    }
  };

  /**
   * Convert a property into getter/setter so we can emit
   * the events when the property is accessed/changed.
   *
   * @param {String} key
   * @param {*} val
   */

  Observer.prototype.convert = function (key, val) {
    defineReactive(this.value, key, val);
  };

  /**
   * Add an owner vm, so that when $set/$delete mutations
   * happen we can notify owner vms to proxy the keys and
   * digest the watchers. This is only called when the object
   * is observed as an instance's root $data.
   *
   * @param {Vue} vm
   */

  Observer.prototype.addVm = function (vm) {
    (this.vms || (this.vms = [])).push(vm);
  };

  /**
   * Remove an owner vm. This is called when the object is
   * swapped out as an instance's $data object.
   *
   * @param {Vue} vm
   */

  Observer.prototype.removeVm = function (vm) {
    this.vms.$remove(vm);
  };

  // helpers

  /**
   * Augment an target Object or Array by intercepting
   * the prototype chain using __proto__
   *
   * @param {Object|Array} target
   * @param {Object} src
   */

  function protoAugment(target, src) {
    /* eslint-disable no-proto */
    target.__proto__ = src;
    /* eslint-enable no-proto */
  }

  /**
   * Augment an target Object or Array by defining
   * hidden properties.
   *
   * @param {Object|Array} target
   * @param {Object} proto
   */

  function copyAugment(target, src, keys) {
    for (var i = 0, l = keys.length; i < l; i++) {
      var key = keys[i];
      def(target, key, src[key]);
    }
  }

  /**
   * Attempt to create an observer instance for a value,
   * returns the new observer if successfully observed,
   * or the existing observer if the value already has one.
   *
   * @param {*} value
   * @param {Vue} [vm]
   * @return {Observer|undefined}
   * @static
   */

  function observe(value, vm) {
    if (!value || typeof value !== 'object') {
      return;
    }
    var ob;
    if (hasOwn(value, '__ob__') && value.__ob__ instanceof Observer) {
      ob = value.__ob__;
    } else if (shouldConvert && (isArray(value) || isPlainObject(value)) && Object.isExtensible(value) && !value._isVue) {
      ob = new Observer(value);
    }
    if (ob && vm) {
      ob.addVm(vm);
    }
    return ob;
  }

  /**
   * Define a reactive property on an Object.
   *
   * @param {Object} obj
   * @param {String} key
   * @param {*} val
   */

  function defineReactive(obj, key, val) {
    var dep = new Dep();

    var property = Object.getOwnPropertyDescriptor(obj, key);
    if (property && property.configurable === false) {
      return;
    }

    // cater for pre-defined getter/setters
    var getter = property && property.get;
    var setter = property && property.set;

    var childOb = observe(val);
    Object.defineProperty(obj, key, {
      enumerable: true,
      configurable: true,
      get: function reactiveGetter() {
        var value = getter ? getter.call(obj) : val;
        if (Dep.target) {
          dep.depend();
          if (childOb) {
            childOb.dep.depend();
          }
          if (isArray(value)) {
            for (var e, i = 0, l = value.length; i < l; i++) {
              e = value[i];
              e && e.__ob__ && e.__ob__.dep.depend();
            }
          }
        }
        return value;
      },
      set: function reactiveSetter(newVal) {
        var value = getter ? getter.call(obj) : val;
        if (newVal === value) {
          return;
        }
        if (setter) {
          setter.call(obj, newVal);
        } else {
          val = newVal;
        }
        childOb = observe(newVal);
        dep.notify();
      }
    });
  }



  var util = Object.freeze({
  	defineReactive: defineReactive,
  	set: set,
  	del: del,
  	hasOwn: hasOwn,
  	isLiteral: isLiteral,
  	isReserved: isReserved,
  	_toString: _toString,
  	toNumber: toNumber,
  	toBoolean: toBoolean,
  	stripQuotes: stripQuotes,
  	camelize: camelize,
  	hyphenate: hyphenate,
  	classify: classify,
  	bind: bind,
  	toArray: toArray,
  	extend: extend,
  	isObject: isObject,
  	isPlainObject: isPlainObject,
  	def: def,
  	debounce: _debounce,
  	indexOf: indexOf,
  	cancellable: cancellable,
  	looseEqual: looseEqual,
  	isArray: isArray,
  	hasProto: hasProto,
  	inBrowser: inBrowser,
  	devtools: devtools,
  	isIE: isIE,
  	isIE9: isIE9,
  	isAndroid: isAndroid,
  	isIos: isIos,
  	iosVersionMatch: iosVersionMatch,
  	iosVersion: iosVersion,
  	hasMutationObserverBug: hasMutationObserverBug,
  	get transitionProp () { return transitionProp; },
  	get transitionEndEvent () { return transitionEndEvent; },
  	get animationProp () { return animationProp; },
  	get animationEndEvent () { return animationEndEvent; },
  	nextTick: nextTick,
  	get _Set () { return _Set; },
  	query: query,
  	inDoc: inDoc,
  	getAttr: getAttr,
  	getBindAttr: getBindAttr,
  	hasBindAttr: hasBindAttr,
  	before: before,
  	after: after,
  	remove: remove,
  	prepend: prepend,
  	replace: replace,
  	on: on,
  	off: off,
  	setClass: setClass,
  	addClass: addClass,
  	removeClass: removeClass,
  	extractContent: extractContent,
  	trimNode: trimNode,
  	isTemplate: isTemplate,
  	createAnchor: createAnchor,
  	findRef: findRef,
  	mapNodeRange: mapNodeRange,
  	removeNodeRange: removeNodeRange,
  	isFragment: isFragment,
  	getOuterHTML: getOuterHTML,
  	mergeOptions: mergeOptions,
  	resolveAsset: resolveAsset,
  	checkComponentAttr: checkComponentAttr,
  	commonTagRE: commonTagRE,
  	reservedTagRE: reservedTagRE,
  	get warn () { return warn; }
  });

  var uid = 0;

  function initMixin (Vue) {
    /**
     * The main init sequence. This is called for every
     * instance, including ones that are created from extended
     * constructors.
     *
     * @param {Object} options - this options object should be
     *                           the result of merging class
     *                           options and the options passed
     *                           in to the constructor.
     */

    Vue.prototype._init = function (options) {
      options = options || {};

      this.$el = null;
      this.$parent = options.parent;
      this.$root = this.$parent ? this.$parent.$root : this;
      this.$children = [];
      this.$refs = {}; // child vm references
      this.$els = {}; // element references
      this._watchers = []; // all watchers as an array
      this._directives = []; // all directives

      // a uid
      this._uid = uid++;

      // a flag to avoid this being observed
      this._isVue = true;

      // events bookkeeping
      this._events = {}; // registered callbacks
      this._eventsCount = {}; // for $broadcast optimization

      // fragment instance properties
      this._isFragment = false;
      this._fragment = // @type {DocumentFragment}
      this._fragmentStart = // @type {Text|Comment}
      this._fragmentEnd = null; // @type {Text|Comment}

      // lifecycle state
      this._isCompiled = this._isDestroyed = this._isReady = this._isAttached = this._isBeingDestroyed = this._vForRemoving = false;
      this._unlinkFn = null;

      // context:
      // if this is a transcluded component, context
      // will be the common parent vm of this instance
      // and its host.
      this._context = options._context || this.$parent;

      // scope:
      // if this is inside an inline v-for, the scope
      // will be the intermediate scope created for this
      // repeat fragment. this is used for linking props
      // and container directives.
      this._scope = options._scope;

      // fragment:
      // if this instance is compiled inside a Fragment, it
      // needs to reigster itself as a child of that fragment
      // for attach/detach to work properly.
      this._frag = options._frag;
      if (this._frag) {
        this._frag.children.push(this);
      }

      // push self into parent / transclusion host
      if (this.$parent) {
        this.$parent.$children.push(this);
      }

      // merge options.
      options = this.$options = mergeOptions(this.constructor.options, options, this);

      // set ref
      this._updateRef();

      // initialize data as empty object.
      // it will be filled up in _initData().
      this._data = {};

      // call init hook
      this._callHook('init');

      // initialize data observation and scope inheritance.
      this._initState();

      // setup event system and option events.
      this._initEvents();

      // call created hook
      this._callHook('created');

      // if `el` option is passed, start compilation.
      if (options.el) {
        this.$mount(options.el);
      }
    };
  }

  var pathCache = new Cache(1000);

  // actions
  var APPEND = 0;
  var PUSH = 1;
  var INC_SUB_PATH_DEPTH = 2;
  var PUSH_SUB_PATH = 3;

  // states
  var BEFORE_PATH = 0;
  var IN_PATH = 1;
  var BEFORE_IDENT = 2;
  var IN_IDENT = 3;
  var IN_SUB_PATH = 4;
  var IN_SINGLE_QUOTE = 5;
  var IN_DOUBLE_QUOTE = 6;
  var AFTER_PATH = 7;
  var ERROR = 8;

  var pathStateMachine = [];

  pathStateMachine[BEFORE_PATH] = {
    'ws': [BEFORE_PATH],
    'ident': [IN_IDENT, APPEND],
    '[': [IN_SUB_PATH],
    'eof': [AFTER_PATH]
  };

  pathStateMachine[IN_PATH] = {
    'ws': [IN_PATH],
    '.': [BEFORE_IDENT],
    '[': [IN_SUB_PATH],
    'eof': [AFTER_PATH]
  };

  pathStateMachine[BEFORE_IDENT] = {
    'ws': [BEFORE_IDENT],
    'ident': [IN_IDENT, APPEND]
  };

  pathStateMachine[IN_IDENT] = {
    'ident': [IN_IDENT, APPEND],
    '0': [IN_IDENT, APPEND],
    'number': [IN_IDENT, APPEND],
    'ws': [IN_PATH, PUSH],
    '.': [BEFORE_IDENT, PUSH],
    '[': [IN_SUB_PATH, PUSH],
    'eof': [AFTER_PATH, PUSH]
  };

  pathStateMachine[IN_SUB_PATH] = {
    "'": [IN_SINGLE_QUOTE, APPEND],
    '"': [IN_DOUBLE_QUOTE, APPEND],
    '[': [IN_SUB_PATH, INC_SUB_PATH_DEPTH],
    ']': [IN_PATH, PUSH_SUB_PATH],
    'eof': ERROR,
    'else': [IN_SUB_PATH, APPEND]
  };

  pathStateMachine[IN_SINGLE_QUOTE] = {
    "'": [IN_SUB_PATH, APPEND],
    'eof': ERROR,
    'else': [IN_SINGLE_QUOTE, APPEND]
  };

  pathStateMachine[IN_DOUBLE_QUOTE] = {
    '"': [IN_SUB_PATH, APPEND],
    'eof': ERROR,
    'else': [IN_DOUBLE_QUOTE, APPEND]
  };

  /**
   * Determine the type of a character in a keypath.
   *
   * @param {Char} ch
   * @return {String} type
   */

  function getPathCharType(ch) {
    if (ch === undefined) {
      return 'eof';
    }

    var code = ch.charCodeAt(0);

    switch (code) {
      case 0x5B: // [
      case 0x5D: // ]
      case 0x2E: // .
      case 0x22: // "
      case 0x27: // '
      case 0x30:
        // 0
        return ch;

      case 0x5F: // _
      case 0x24:
        // $
        return 'ident';

      case 0x20: // Space
      case 0x09: // Tab
      case 0x0A: // Newline
      case 0x0D: // Return
      case 0xA0: // No-break space
      case 0xFEFF: // Byte Order Mark
      case 0x2028: // Line Separator
      case 0x2029:
        // Paragraph Separator
        return 'ws';
    }

    // a-z, A-Z
    if (code >= 0x61 && code <= 0x7A || code >= 0x41 && code <= 0x5A) {
      return 'ident';
    }

    // 1-9
    if (code >= 0x31 && code <= 0x39) {
      return 'number';
    }

    return 'else';
  }

  /**
   * Format a subPath, return its plain form if it is
   * a literal string or number. Otherwise prepend the
   * dynamic indicator (*).
   *
   * @param {String} path
   * @return {String}
   */

  function formatSubPath(path) {
    var trimmed = path.trim();
    // invalid leading 0
    if (path.charAt(0) === '0' && isNaN(path)) {
      return false;
    }
    return isLiteral(trimmed) ? stripQuotes(trimmed) : '*' + trimmed;
  }

  /**
   * Parse a string path into an array of segments
   *
   * @param {String} path
   * @return {Array|undefined}
   */

  function parse(path) {
    var keys = [];
    var index = -1;
    var mode = BEFORE_PATH;
    var subPathDepth = 0;
    var c, newChar, key, type, transition, action, typeMap;

    var actions = [];

    actions[PUSH] = function () {
      if (key !== undefined) {
        keys.push(key);
        key = undefined;
      }
    };

    actions[APPEND] = function () {
      if (key === undefined) {
        key = newChar;
      } else {
        key += newChar;
      }
    };

    actions[INC_SUB_PATH_DEPTH] = function () {
      actions[APPEND]();
      subPathDepth++;
    };

    actions[PUSH_SUB_PATH] = function () {
      if (subPathDepth > 0) {
        subPathDepth--;
        mode = IN_SUB_PATH;
        actions[APPEND]();
      } else {
        subPathDepth = 0;
        key = formatSubPath(key);
        if (key === false) {
          return false;
        } else {
          actions[PUSH]();
        }
      }
    };

    function maybeUnescapeQuote() {
      var nextChar = path[index + 1];
      if (mode === IN_SINGLE_QUOTE && nextChar === "'" || mode === IN_DOUBLE_QUOTE && nextChar === '"') {
        index++;
        newChar = '\\' + nextChar;
        actions[APPEND]();
        return true;
      }
    }

    while (mode != null) {
      index++;
      c = path[index];

      if (c === '\\' && maybeUnescapeQuote()) {
        continue;
      }

      type = getPathCharType(c);
      typeMap = pathStateMachine[mode];
      transition = typeMap[type] || typeMap['else'] || ERROR;

      if (transition === ERROR) {
        return; // parse error
      }

      mode = transition[0];
      action = actions[transition[1]];
      if (action) {
        newChar = transition[2];
        newChar = newChar === undefined ? c : newChar;
        if (action() === false) {
          return;
        }
      }

      if (mode === AFTER_PATH) {
        keys.raw = path;
        return keys;
      }
    }
  }

  /**
   * External parse that check for a cache hit first
   *
   * @param {String} path
   * @return {Array|undefined}
   */

  function parsePath(path) {
    var hit = pathCache.get(path);
    if (!hit) {
      hit = parse(path);
      if (hit) {
        pathCache.put(path, hit);
      }
    }
    return hit;
  }

  /**
   * Get from an object from a path string
   *
   * @param {Object} obj
   * @param {String} path
   */

  function getPath(obj, path) {
    return parseExpression(path).get(obj);
  }

  /**
   * Warn against setting non-existent root path on a vm.
   */

  var warnNonExistent;
  if ('development' !== 'production') {
    warnNonExistent = function (path, vm) {
      warn('You are setting a non-existent path "' + path.raw + '" ' + 'on a vm instance. Consider pre-initializing the property ' + 'with the "data" option for more reliable reactivity ' + 'and better performance.', vm);
    };
  }

  /**
   * Set on an object from a path
   *
   * @param {Object} obj
   * @param {String | Array} path
   * @param {*} val
   */

  function setPath(obj, path, val) {
    var original = obj;
    if (typeof path === 'string') {
      path = parse(path);
    }
    if (!path || !isObject(obj)) {
      return false;
    }
    var last, key;
    for (var i = 0, l = path.length; i < l; i++) {
      last = obj;
      key = path[i];
      if (key.charAt(0) === '*') {
        key = parseExpression(key.slice(1)).get.call(original, original);
      }
      if (i < l - 1) {
        obj = obj[key];
        if (!isObject(obj)) {
          obj = {};
          if ('development' !== 'production' && last._isVue) {
            warnNonExistent(path, last);
          }
          set(last, key, obj);
        }
      } else {
        if (isArray(obj)) {
          obj.$set(key, val);
        } else if (key in obj) {
          obj[key] = val;
        } else {
          if ('development' !== 'production' && obj._isVue) {
            warnNonExistent(path, obj);
          }
          set(obj, key, val);
        }
      }
    }
    return true;
  }

var path = Object.freeze({
    parsePath: parsePath,
    getPath: getPath,
    setPath: setPath
  });

  var expressionCache = new Cache(1000);

  var allowedKeywords = 'Math,Date,this,true,false,null,undefined,Infinity,NaN,' + 'isNaN,isFinite,decodeURI,decodeURIComponent,encodeURI,' + 'encodeURIComponent,parseInt,parseFloat';
  var allowedKeywordsRE = new RegExp('^(' + allowedKeywords.replace(/,/g, '\\b|') + '\\b)');

  // keywords that don't make sense inside expressions
  var improperKeywords = 'break,case,class,catch,const,continue,debugger,default,' + 'delete,do,else,export,extends,finally,for,function,if,' + 'import,in,instanceof,let,return,super,switch,throw,try,' + 'var,while,with,yield,enum,await,implements,package,' + 'protected,static,interface,private,public';
  var improperKeywordsRE = new RegExp('^(' + improperKeywords.replace(/,/g, '\\b|') + '\\b)');

  var wsRE = /\s/g;
  var newlineRE = /\n/g;
  var saveRE = /[\{,]\s*[\w\$_]+\s*:|('(?:[^'\\]|\\.)*'|"(?:[^"\\]|\\.)*"|`(?:[^`\\]|\\.)*\$\{|\}(?:[^`\\]|\\.)*`|`(?:[^`\\]|\\.)*`)|new |typeof |void /g;
  var restoreRE = /"(\d+)"/g;
  var pathTestRE = /^[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*|\['.*?'\]|\[".*?"\]|\[\d+\]|\[[A-Za-z_$][\w$]*\])*$/;
  var identRE = /[^\w$\.](?:[A-Za-z_$][\w$]*)/g;
  var literalValueRE$1 = /^(?:true|false|null|undefined|Infinity|NaN)$/;

  function noop() {}

  /**
   * Save / Rewrite / Restore
   *
   * When rewriting paths found in an expression, it is
   * possible for the same letter sequences to be found in
   * strings and Object literal property keys. Therefore we
   * remove and store these parts in a temporary array, and
   * restore them after the path rewrite.
   */

  var saved = [];

  /**
   * Save replacer
   *
   * The save regex can match two possible cases:
   * 1. An opening object literal
   * 2. A string
   * If matched as a plain string, we need to escape its
   * newlines, since the string needs to be preserved when
   * generating the function body.
   *
   * @param {String} str
   * @param {String} isString - str if matched as a string
   * @return {String} - placeholder with index
   */

  function save(str, isString) {
    var i = saved.length;
    saved[i] = isString ? str.replace(newlineRE, '\\n') : str;
    return '"' + i + '"';
  }

  /**
   * Path rewrite replacer
   *
   * @param {String} raw
   * @return {String}
   */

  function rewrite(raw) {
    var c = raw.charAt(0);
    var path = raw.slice(1);
    if (allowedKeywordsRE.test(path)) {
      return raw;
    } else {
      path = path.indexOf('"') > -1 ? path.replace(restoreRE, restore) : path;
      return c + 'scope.' + path;
    }
  }

  /**
   * Restore replacer
   *
   * @param {String} str
   * @param {String} i - matched save index
   * @return {String}
   */

  function restore(str, i) {
    return saved[i];
  }

  /**
   * Rewrite an expression, prefixing all path accessors with
   * `scope.` and generate getter/setter functions.
   *
   * @param {String} exp
   * @return {Function}
   */

  function compileGetter(exp) {
    if (improperKeywordsRE.test(exp)) {
      'development' !== 'production' && warn('Avoid using reserved keywords in expression: ' + exp);
    }
    // reset state
    saved.length = 0;
    // save strings and object literal keys
    var body = exp.replace(saveRE, save).replace(wsRE, '');
    // rewrite all paths
    // pad 1 space here because the regex matches 1 extra char
    body = (' ' + body).replace(identRE, rewrite).replace(restoreRE, restore);
    return makeGetterFn(body);
  }

  /**
   * Build a getter function. Requires eval.
   *
   * We isolate the try/catch so it doesn't affect the
   * optimization of the parse function when it is not called.
   *
   * @param {String} body
   * @return {Function|undefined}
   */

  function makeGetterFn(body) {
    try {
      /* eslint-disable no-new-func */
      return new Function('scope', 'return ' + body + ';');
      /* eslint-enable no-new-func */
    } catch (e) {
      if ('development' !== 'production') {
        /* istanbul ignore if */
        if (e.toString().match(/unsafe-eval|CSP/)) {
          warn('It seems you are using the default build of Vue.js in an environment ' + 'with Content Security Policy that prohibits unsafe-eval. ' + 'Use the CSP-compliant build instead: ' + 'http://vuejs.org/guide/installation.html#CSP-compliant-build');
        } else {
          warn('Invalid expression. ' + 'Generated function body: ' + body);
        }
      }
      return noop;
    }
  }

  /**
   * Compile a setter function for the expression.
   *
   * @param {String} exp
   * @return {Function|undefined}
   */

  function compileSetter(exp) {
    var path = parsePath(exp);
    if (path) {
      return function (scope, val) {
        setPath(scope, path, val);
      };
    } else {
      'development' !== 'production' && warn('Invalid setter expression: ' + exp);
    }
  }

  /**
   * Parse an expression into re-written getter/setters.
   *
   * @param {String} exp
   * @param {Boolean} needSet
   * @return {Function}
   */

  function parseExpression(exp, needSet) {
    exp = exp.trim();
    // try cache
    var hit = expressionCache.get(exp);
    if (hit) {
      if (needSet && !hit.set) {
        hit.set = compileSetter(hit.exp);
      }
      return hit;
    }
    var res = { exp: exp };
    res.get = isSimplePath(exp) && exp.indexOf('[') < 0
    // optimized super simple getter
    ? makeGetterFn('scope.' + exp)
    // dynamic getter
    : compileGetter(exp);
    if (needSet) {
      res.set = compileSetter(exp);
    }
    expressionCache.put(exp, res);
    return res;
  }

  /**
   * Check if an expression is a simple path.
   *
   * @param {String} exp
   * @return {Boolean}
   */

  function isSimplePath(exp) {
    return pathTestRE.test(exp) &&
    // don't treat literal values as paths
    !literalValueRE$1.test(exp) &&
    // Math constants e.g. Math.PI, Math.E etc.
    exp.slice(0, 5) !== 'Math.';
  }

var expression = Object.freeze({
    parseExpression: parseExpression,
    isSimplePath: isSimplePath
  });

  // we have two separate queues: one for directive updates
  // and one for user watcher registered via $watch().
  // we want to guarantee directive updates to be called
  // before user watchers so that when user watchers are
  // triggered, the DOM would have already been in updated
  // state.

  var queue = [];
  var userQueue = [];
  var has = {};
  var circular = {};
  var waiting = false;

  /**
   * Reset the batcher's state.
   */

  function resetBatcherState() {
    queue.length = 0;
    userQueue.length = 0;
    has = {};
    circular = {};
    waiting = false;
  }

  /**
   * Flush both queues and run the watchers.
   */

  function flushBatcherQueue() {
    var _again = true;

    _function: while (_again) {
      _again = false;

      runBatcherQueue(queue);
      runBatcherQueue(userQueue);
      // user watchers triggered more watchers,
      // keep flushing until it depletes
      if (queue.length) {
        _again = true;
        continue _function;
      }
      // dev tool hook
      /* istanbul ignore if */
      if (devtools && config.devtools) {
        devtools.emit('flush');
      }
      resetBatcherState();
    }
  }

  /**
   * Run the watchers in a single queue.
   *
   * @param {Array} queue
   */

  function runBatcherQueue(queue) {
    // do not cache length because more watchers might be pushed
    // as we run existing watchers
    for (var i = 0; i < queue.length; i++) {
      var watcher = queue[i];
      var id = watcher.id;
      has[id] = null;
      watcher.run();
      // in dev build, check and stop circular updates.
      if ('development' !== 'production' && has[id] != null) {
        circular[id] = (circular[id] || 0) + 1;
        if (circular[id] > config._maxUpdateCount) {
          warn('You may have an infinite update loop for watcher ' + 'with expression "' + watcher.expression + '"', watcher.vm);
          break;
        }
      }
    }
    queue.length = 0;
  }

  /**
   * Push a watcher into the watcher queue.
   * Jobs with duplicate IDs will be skipped unless it's
   * pushed when the queue is being flushed.
   *
   * @param {Watcher} watcher
   *   properties:
   *   - {Number} id
   *   - {Function} run
   */

  function pushWatcher(watcher) {
    var id = watcher.id;
    if (has[id] == null) {
      // push watcher into appropriate queue
      var q = watcher.user ? userQueue : queue;
      has[id] = q.length;
      q.push(watcher);
      // queue the flush
      if (!waiting) {
        waiting = true;
        nextTick(flushBatcherQueue);
      }
    }
  }

  var uid$2 = 0;

  /**
   * A watcher parses an expression, collects dependencies,
   * and fires callback when the expression value changes.
   * This is used for both the $watch() api and directives.
   *
   * @param {Vue} vm
   * @param {String|Function} expOrFn
   * @param {Function} cb
   * @param {Object} options
   *                 - {Array} filters
   *                 - {Boolean} twoWay
   *                 - {Boolean} deep
   *                 - {Boolean} user
   *                 - {Boolean} sync
   *                 - {Boolean} lazy
   *                 - {Function} [preProcess]
   *                 - {Function} [postProcess]
   * @constructor
   */
  function Watcher(vm, expOrFn, cb, options) {
    // mix in options
    if (options) {
      extend(this, options);
    }
    var isFn = typeof expOrFn === 'function';
    this.vm = vm;
    vm._watchers.push(this);
    this.expression = expOrFn;
    this.cb = cb;
    this.id = ++uid$2; // uid for batching
    this.active = true;
    this.dirty = this.lazy; // for lazy watchers
    this.deps = [];
    this.newDeps = [];
    this.depIds = new _Set();
    this.newDepIds = new _Set();
    this.prevError = null; // for async error stacks
    // parse expression for getter/setter
    if (isFn) {
      this.getter = expOrFn;
      this.setter = undefined;
    } else {
      var res = parseExpression(expOrFn, this.twoWay);
      this.getter = res.get;
      this.setter = res.set;
    }
    this.value = this.lazy ? undefined : this.get();
    // state for avoiding false triggers for deep and Array
    // watchers during vm._digest()
    this.queued = this.shallow = false;
  }

  /**
   * Evaluate the getter, and re-collect dependencies.
   */

  Watcher.prototype.get = function () {
    this.beforeGet();
    var scope = this.scope || this.vm;
    var value;
    try {
      value = this.getter.call(scope, scope);
    } catch (e) {
      if ('development' !== 'production' && config.warnExpressionErrors) {
        warn('Error when evaluating expression ' + '"' + this.expression + '": ' + e.toString(), this.vm);
      }
    }
    // "touch" every property so they are all tracked as
    // dependencies for deep watching
    if (this.deep) {
      traverse(value);
    }
    if (this.preProcess) {
      value = this.preProcess(value);
    }
    if (this.filters) {
      value = scope._applyFilters(value, null, this.filters, false);
    }
    if (this.postProcess) {
      value = this.postProcess(value);
    }
    this.afterGet();
    return value;
  };

  /**
   * Set the corresponding value with the setter.
   *
   * @param {*} value
   */

  Watcher.prototype.set = function (value) {
    var scope = this.scope || this.vm;
    if (this.filters) {
      value = scope._applyFilters(value, this.value, this.filters, true);
    }
    try {
      this.setter.call(scope, scope, value);
    } catch (e) {
      if ('development' !== 'production' && config.warnExpressionErrors) {
        warn('Error when evaluating setter ' + '"' + this.expression + '": ' + e.toString(), this.vm);
      }
    }
    // two-way sync for v-for alias
    var forContext = scope.$forContext;
    if (forContext && forContext.alias === this.expression) {
      if (forContext.filters) {
        'development' !== 'production' && warn('It seems you are using two-way binding on ' + 'a v-for alias (' + this.expression + '), and the ' + 'v-for has filters. This will not work properly. ' + 'Either remove the filters or use an array of ' + 'objects and bind to object properties instead.', this.vm);
        return;
      }
      forContext._withLock(function () {
        if (scope.$key) {
          // original is an object
          forContext.rawValue[scope.$key] = value;
        } else {
          forContext.rawValue.$set(scope.$index, value);
        }
      });
    }
  };

  /**
   * Prepare for dependency collection.
   */

  Watcher.prototype.beforeGet = function () {
    Dep.target = this;
  };

  /**
   * Add a dependency to this directive.
   *
   * @param {Dep} dep
   */

  Watcher.prototype.addDep = function (dep) {
    var id = dep.id;
    if (!this.newDepIds.has(id)) {
      this.newDepIds.add(id);
      this.newDeps.push(dep);
      if (!this.depIds.has(id)) {
        dep.addSub(this);
      }
    }
  };

  /**
   * Clean up for dependency collection.
   */

  Watcher.prototype.afterGet = function () {
    Dep.target = null;
    var i = this.deps.length;
    while (i--) {
      var dep = this.deps[i];
      if (!this.newDepIds.has(dep.id)) {
        dep.removeSub(this);
      }
    }
    var tmp = this.depIds;
    this.depIds = this.newDepIds;
    this.newDepIds = tmp;
    this.newDepIds.clear();
    tmp = this.deps;
    this.deps = this.newDeps;
    this.newDeps = tmp;
    this.newDeps.length = 0;
  };

  /**
   * Subscriber interface.
   * Will be called when a dependency changes.
   *
   * @param {Boolean} shallow
   */

  Watcher.prototype.update = function (shallow) {
    if (this.lazy) {
      this.dirty = true;
    } else if (this.sync || !config.async) {
      this.run();
    } else {
      // if queued, only overwrite shallow with non-shallow,
      // but not the other way around.
      this.shallow = this.queued ? shallow ? this.shallow : false : !!shallow;
      this.queued = true;
      // record before-push error stack in debug mode
      /* istanbul ignore if */
      if ('development' !== 'production' && config.debug) {
        this.prevError = new Error('[vue] async stack trace');
      }
      pushWatcher(this);
    }
  };

  /**
   * Batcher job interface.
   * Will be called by the batcher.
   */

  Watcher.prototype.run = function () {
    if (this.active) {
      var value = this.get();
      if (value !== this.value ||
      // Deep watchers and watchers on Object/Arrays should fire even
      // when the value is the same, because the value may
      // have mutated; but only do so if this is a
      // non-shallow update (caused by a vm digest).
      (isObject(value) || this.deep) && !this.shallow) {
        // set new value
        var oldValue = this.value;
        this.value = value;
        // in debug + async mode, when a watcher callbacks
        // throws, we also throw the saved before-push error
        // so the full cross-tick stack trace is available.
        var prevError = this.prevError;
        /* istanbul ignore if */
        if ('development' !== 'production' && config.debug && prevError) {
          this.prevError = null;
          try {
            this.cb.call(this.vm, value, oldValue);
          } catch (e) {
            nextTick(function () {
              throw prevError;
            }, 0);
            throw e;
          }
        } else {
          this.cb.call(this.vm, value, oldValue);
        }
      }
      this.queued = this.shallow = false;
    }
  };

  /**
   * Evaluate the value of the watcher.
   * This only gets called for lazy watchers.
   */

  Watcher.prototype.evaluate = function () {
    // avoid overwriting another watcher that is being
    // collected.
    var current = Dep.target;
    this.value = this.get();
    this.dirty = false;
    Dep.target = current;
  };

  /**
   * Depend on all deps collected by this watcher.
   */

  Watcher.prototype.depend = function () {
    var i = this.deps.length;
    while (i--) {
      this.deps[i].depend();
    }
  };

  /**
   * Remove self from all dependencies' subcriber list.
   */

  Watcher.prototype.teardown = function () {
    if (this.active) {
      // remove self from vm's watcher list
      // this is a somewhat expensive operation so we skip it
      // if the vm is being destroyed or is performing a v-for
      // re-render (the watcher list is then filtered by v-for).
      if (!this.vm._isBeingDestroyed && !this.vm._vForRemoving) {
        this.vm._watchers.$remove(this);
      }
      var i = this.deps.length;
      while (i--) {
        this.deps[i].removeSub(this);
      }
      this.active = false;
      this.vm = this.cb = this.value = null;
    }
  };

  /**
   * Recrusively traverse an object to evoke all converted
   * getters, so that every nested property inside the object
   * is collected as a "deep" dependency.
   *
   * @param {*} val
   */

  var seenObjects = new _Set();
  function traverse(val, seen) {
    var i = undefined,
        keys = undefined;
    if (!seen) {
      seen = seenObjects;
      seen.clear();
    }
    var isA = isArray(val);
    var isO = isObject(val);
    if ((isA || isO) && Object.isExtensible(val)) {
      if (val.__ob__) {
        var depId = val.__ob__.dep.id;
        if (seen.has(depId)) {
          return;
        } else {
          seen.add(depId);
        }
      }
      if (isA) {
        i = val.length;
        while (i--) traverse(val[i], seen);
      } else if (isO) {
        keys = Object.keys(val);
        i = keys.length;
        while (i--) traverse(val[keys[i]], seen);
      }
    }
  }

  var text$1 = {

    bind: function bind() {
      this.attr = this.el.nodeType === 3 ? 'data' : 'textContent';
    },

    update: function update(value) {
      this.el[this.attr] = _toString(value);
    }
  };

  var templateCache = new Cache(1000);
  var idSelectorCache = new Cache(1000);

  var map = {
    efault: [0, '', ''],
    legend: [1, '<fieldset>', '</fieldset>'],
    tr: [2, '<table><tbody>', '</tbody></table>'],
    col: [2, '<table><tbody></tbody><colgroup>', '</colgroup></table>']
  };

  map.td = map.th = [3, '<table><tbody><tr>', '</tr></tbody></table>'];

  map.option = map.optgroup = [1, '<select multiple="multiple">', '</select>'];

  map.thead = map.tbody = map.colgroup = map.caption = map.tfoot = [1, '<table>', '</table>'];

  map.g = map.defs = map.symbol = map.use = map.image = map.text = map.circle = map.ellipse = map.line = map.path = map.polygon = map.polyline = map.rect = [1, '<svg ' + 'xmlns="http://www.w3.org/2000/svg" ' + 'xmlns:xlink="http://www.w3.org/1999/xlink" ' + 'xmlns:ev="http://www.w3.org/2001/xml-events"' + 'version="1.1">', '</svg>'];

  /**
   * Check if a node is a supported template node with a
   * DocumentFragment content.
   *
   * @param {Node} node
   * @return {Boolean}
   */

  function isRealTemplate(node) {
    return isTemplate(node) && isFragment(node.content);
  }

  var tagRE$1 = /<([\w:-]+)/;
  var entityRE = /&#?\w+?;/;
  var commentRE = /<!--/;

  /**
   * Convert a string template to a DocumentFragment.
   * Determines correct wrapping by tag types. Wrapping
   * strategy found in jQuery & component/domify.
   *
   * @param {String} templateString
   * @param {Boolean} raw
   * @return {DocumentFragment}
   */

  function stringToFragment(templateString, raw) {
    // try a cache hit first
    var cacheKey = raw ? templateString : templateString.trim();
    var hit = templateCache.get(cacheKey);
    if (hit) {
      return hit;
    }

    var frag = document.createDocumentFragment();
    var tagMatch = templateString.match(tagRE$1);
    var entityMatch = entityRE.test(templateString);
    var commentMatch = commentRE.test(templateString);

    if (!tagMatch && !entityMatch && !commentMatch) {
      // text only, return a single text node.
      frag.appendChild(document.createTextNode(templateString));
    } else {
      var tag = tagMatch && tagMatch[1];
      var wrap = map[tag] || map.efault;
      var depth = wrap[0];
      var prefix = wrap[1];
      var suffix = wrap[2];
      var node = document.createElement('div');

      node.innerHTML = prefix + templateString + suffix;
      while (depth--) {
        node = node.lastChild;
      }

      var child;
      /* eslint-disable no-cond-assign */
      while (child = node.firstChild) {
        /* eslint-enable no-cond-assign */
        frag.appendChild(child);
      }
    }
    if (!raw) {
      trimNode(frag);
    }
    templateCache.put(cacheKey, frag);
    return frag;
  }

  /**
   * Convert a template node to a DocumentFragment.
   *
   * @param {Node} node
   * @return {DocumentFragment}
   */

  function nodeToFragment(node) {
    // if its a template tag and the browser supports it,
    // its content is already a document fragment. However, iOS Safari has
    // bug when using directly cloned template content with touch
    // events and can cause crashes when the nodes are removed from DOM, so we
    // have to treat template elements as string templates. (#2805)
    /* istanbul ignore if */
    if (isRealTemplate(node)) {
      return stringToFragment(node.innerHTML);
    }
    // script template
    if (node.tagName === 'SCRIPT') {
      return stringToFragment(node.textContent);
    }
    // normal node, clone it to avoid mutating the original
    var clonedNode = cloneNode(node);
    var frag = document.createDocumentFragment();
    var child;
    /* eslint-disable no-cond-assign */
    while (child = clonedNode.firstChild) {
      /* eslint-enable no-cond-assign */
      frag.appendChild(child);
    }
    trimNode(frag);
    return frag;
  }

  // Test for the presence of the Safari template cloning bug
  // https://bugs.webkit.org/showug.cgi?id=137755
  var hasBrokenTemplate = (function () {
    /* istanbul ignore else */
    if (inBrowser) {
      var a = document.createElement('div');
      a.innerHTML = '<template>1</template>';
      return !a.cloneNode(true).firstChild.innerHTML;
    } else {
      return false;
    }
  })();

  // Test for IE10/11 textarea placeholder clone bug
  var hasTextareaCloneBug = (function () {
    /* istanbul ignore else */
    if (inBrowser) {
      var t = document.createElement('textarea');
      t.placeholder = 't';
      return t.cloneNode(true).value === 't';
    } else {
      return false;
    }
  })();

  /**
   * 1. Deal with Safari cloning nested <template> bug by
   *    manually cloning all template instances.
   * 2. Deal with IE10/11 textarea placeholder bug by setting
   *    the correct value after cloning.
   *
   * @param {Element|DocumentFragment} node
   * @return {Element|DocumentFragment}
   */

  function cloneNode(node) {
    /* istanbul ignore if */
    if (!node.querySelectorAll) {
      return node.cloneNode();
    }
    var res = node.cloneNode(true);
    var i, original, cloned;
    /* istanbul ignore if */
    if (hasBrokenTemplate) {
      var tempClone = res;
      if (isRealTemplate(node)) {
        node = node.content;
        tempClone = res.content;
      }
      original = node.querySelectorAll('template');
      if (original.length) {
        cloned = tempClone.querySelectorAll('template');
        i = cloned.length;
        while (i--) {
          cloned[i].parentNode.replaceChild(cloneNode(original[i]), cloned[i]);
        }
      }
    }
    /* istanbul ignore if */
    if (hasTextareaCloneBug) {
      if (node.tagName === 'TEXTAREA') {
        res.value = node.value;
      } else {
        original = node.querySelectorAll('textarea');
        if (original.length) {
          cloned = res.querySelectorAll('textarea');
          i = cloned.length;
          while (i--) {
            cloned[i].value = original[i].value;
          }
        }
      }
    }
    return res;
  }

  /**
   * Process the template option and normalizes it into a
   * a DocumentFragment that can be used as a partial or a
   * instance template.
   *
   * @param {*} template
   *        Possible values include:
   *        - DocumentFragment object
   *        - Node object of type Template
   *        - id selector: '#some-template-id'
   *        - template string: '<div><span>{{msg}}</span></div>'
   * @param {Boolean} shouldClone
   * @param {Boolean} raw
   *        inline HTML interpolation. Do not check for id
   *        selector and keep whitespace in the string.
   * @return {DocumentFragment|undefined}
   */

  function parseTemplate(template, shouldClone, raw) {
    var node, frag;

    // if the template is already a document fragment,
    // do nothing
    if (isFragment(template)) {
      trimNode(template);
      return shouldClone ? cloneNode(template) : template;
    }

    if (typeof template === 'string') {
      // id selector
      if (!raw && template.charAt(0) === '#') {
        // id selector can be cached too
        frag = idSelectorCache.get(template);
        if (!frag) {
          node = document.getElementById(template.slice(1));
          if (node) {
            frag = nodeToFragment(node);
            // save selector to cache
            idSelectorCache.put(template, frag);
          }
        }
      } else {
        // normal string template
        frag = stringToFragment(template, raw);
      }
    } else if (template.nodeType) {
      // a direct node
      frag = nodeToFragment(template);
    }

    return frag && shouldClone ? cloneNode(frag) : frag;
  }

var template = Object.freeze({
    cloneNode: cloneNode,
    parseTemplate: parseTemplate
  });

  var html = {

    bind: function bind() {
      // a comment node means this is a binding for
      // {{{ inline unescaped html }}}
      if (this.el.nodeType === 8) {
        // hold nodes
        this.nodes = [];
        // replace the placeholder with proper anchor
        this.anchor = createAnchor('v-html');
        replace(this.el, this.anchor);
      }
    },

    update: function update(value) {
      value = _toString(value);
      if (this.nodes) {
        this.swap(value);
      } else {
        this.el.innerHTML = value;
      }
    },

    swap: function swap(value) {
      // remove old nodes
      var i = this.nodes.length;
      while (i--) {
        remove(this.nodes[i]);
      }
      // convert new value to a fragment
      // do not attempt to retrieve from id selector
      var frag = parseTemplate(value, true, true);
      // save a reference to these nodes so we can remove later
      this.nodes = toArray(frag.childNodes);
      before(frag, this.anchor);
    }
  };

  /**
   * Abstraction for a partially-compiled fragment.
   * Can optionally compile content with a child scope.
   *
   * @param {Function} linker
   * @param {Vue} vm
   * @param {DocumentFragment} frag
   * @param {Vue} [host]
   * @param {Object} [scope]
   * @param {Fragment} [parentFrag]
   */
  function Fragment(linker, vm, frag, host, scope, parentFrag) {
    this.children = [];
    this.childFrags = [];
    this.vm = vm;
    this.scope = scope;
    this.inserted = false;
    this.parentFrag = parentFrag;
    if (parentFrag) {
      parentFrag.childFrags.push(this);
    }
    this.unlink = linker(vm, frag, host, scope, this);
    var single = this.single = frag.childNodes.length === 1 &&
    // do not go single mode if the only node is an anchor
    !frag.childNodes[0].__v_anchor;
    if (single) {
      this.node = frag.childNodes[0];
      this.before = singleBefore;
      this.remove = singleRemove;
    } else {
      this.node = createAnchor('fragment-start');
      this.end = createAnchor('fragment-end');
      this.frag = frag;
      prepend(this.node, frag);
      frag.appendChild(this.end);
      this.before = multiBefore;
      this.remove = multiRemove;
    }
    this.node.__v_frag = this;
  }

  /**
   * Call attach/detach for all components contained within
   * this fragment. Also do so recursively for all child
   * fragments.
   *
   * @param {Function} hook
   */

  Fragment.prototype.callHook = function (hook) {
    var i, l;
    for (i = 0, l = this.childFrags.length; i < l; i++) {
      this.childFrags[i].callHook(hook);
    }
    for (i = 0, l = this.children.length; i < l; i++) {
      hook(this.children[i]);
    }
  };

  /**
   * Insert fragment before target, single node version
   *
   * @param {Node} target
   * @param {Boolean} withTransition
   */

  function singleBefore(target, withTransition) {
    this.inserted = true;
    var method = withTransition !== false ? beforeWithTransition : before;
    method(this.node, target, this.vm);
    if (inDoc(this.node)) {
      this.callHook(attach);
    }
  }

  /**
   * Remove fragment, single node version
   */

  function singleRemove() {
    this.inserted = false;
    var shouldCallRemove = inDoc(this.node);
    var self = this;
    this.beforeRemove();
    removeWithTransition(this.node, this.vm, function () {
      if (shouldCallRemove) {
        self.callHook(detach);
      }
      self.destroy();
    });
  }

  /**
   * Insert fragment before target, multi-nodes version
   *
   * @param {Node} target
   * @param {Boolean} withTransition
   */

  function multiBefore(target, withTransition) {
    this.inserted = true;
    var vm = this.vm;
    var method = withTransition !== false ? beforeWithTransition : before;
    mapNodeRange(this.node, this.end, function (node) {
      method(node, target, vm);
    });
    if (inDoc(this.node)) {
      this.callHook(attach);
    }
  }

  /**
   * Remove fragment, multi-nodes version
   */

  function multiRemove() {
    this.inserted = false;
    var self = this;
    var shouldCallRemove = inDoc(this.node);
    this.beforeRemove();
    removeNodeRange(this.node, this.end, this.vm, this.frag, function () {
      if (shouldCallRemove) {
        self.callHook(detach);
      }
      self.destroy();
    });
  }

  /**
   * Prepare the fragment for removal.
   */

  Fragment.prototype.beforeRemove = function () {
    var i, l;
    for (i = 0, l = this.childFrags.length; i < l; i++) {
      // call the same method recursively on child
      // fragments, depth-first
      this.childFrags[i].beforeRemove(false);
    }
    for (i = 0, l = this.children.length; i < l; i++) {
      // Call destroy for all contained instances,
      // with remove:false and defer:true.
      // Defer is necessary because we need to
      // keep the children to call detach hooks
      // on them.
      this.children[i].$destroy(false, true);
    }
    var dirs = this.unlink.dirs;
    for (i = 0, l = dirs.length; i < l; i++) {
      // disable the watchers on all the directives
      // so that the rendered content stays the same
      // during removal.
      dirs[i]._watcher && dirs[i]._watcher.teardown();
    }
  };

  /**
   * Destroy the fragment.
   */

  Fragment.prototype.destroy = function () {
    if (this.parentFrag) {
      this.parentFrag.childFrags.$remove(this);
    }
    this.node.__v_frag = null;
    this.unlink();
  };

  /**
   * Call attach hook for a Vue instance.
   *
   * @param {Vue} child
   */

  function attach(child) {
    if (!child._isAttached && inDoc(child.$el)) {
      child._callHook('attached');
    }
  }

  /**
   * Call detach hook for a Vue instance.
   *
   * @param {Vue} child
   */

  function detach(child) {
    if (child._isAttached && !inDoc(child.$el)) {
      child._callHook('detached');
    }
  }

  var linkerCache = new Cache(5000);

  /**
   * A factory that can be used to create instances of a
   * fragment. Caches the compiled linker if possible.
   *
   * @param {Vue} vm
   * @param {Element|String} el
   */
  function FragmentFactory(vm, el) {
    this.vm = vm;
    var template;
    var isString = typeof el === 'string';
    if (isString || isTemplate(el) && !el.hasAttribute('v-if')) {
      template = parseTemplate(el, true);
    } else {
      template = document.createDocumentFragment();
      template.appendChild(el);
    }
    this.template = template;
    // linker can be cached, but only for components
    var linker;
    var cid = vm.constructor.cid;
    if (cid > 0) {
      var cacheId = cid + (isString ? el : getOuterHTML(el));
      linker = linkerCache.get(cacheId);
      if (!linker) {
        linker = compile(template, vm.$options, true);
        linkerCache.put(cacheId, linker);
      }
    } else {
      linker = compile(template, vm.$options, true);
    }
    this.linker = linker;
  }

  /**
   * Create a fragment instance with given host and scope.
   *
   * @param {Vue} host
   * @param {Object} scope
   * @param {Fragment} parentFrag
   */

  FragmentFactory.prototype.create = function (host, scope, parentFrag) {
    var frag = cloneNode(this.template);
    return new Fragment(this.linker, this.vm, frag, host, scope, parentFrag);
  };

  var ON = 700;
  var MODEL = 800;
  var BIND = 850;
  var TRANSITION = 1100;
  var EL = 1500;
  var COMPONENT = 1500;
  var PARTIAL = 1750;
  var IF = 2100;
  var FOR = 2200;
  var SLOT = 2300;

  var uid$3 = 0;

  var vFor = {

    priority: FOR,
    terminal: true,

    params: ['track-by', 'stagger', 'enter-stagger', 'leave-stagger'],

    bind: function bind() {
      // support "item in/of items" syntax
      var inMatch = this.expression.match(/(.*) (?:in|of) (.*)/);
      if (inMatch) {
        var itMatch = inMatch[1].match(/\((.*),(.*)\)/);
        if (itMatch) {
          this.iterator = itMatch[1].trim();
          this.alias = itMatch[2].trim();
        } else {
          this.alias = inMatch[1].trim();
        }
        this.expression = inMatch[2];
      }

      if (!this.alias) {
        'development' !== 'production' && warn('Invalid v-for expression "' + this.descriptor.raw + '": ' + 'alias is required.', this.vm);
        return;
      }

      // uid as a cache identifier
      this.id = '__v-for__' + ++uid$3;

      // check if this is an option list,
      // so that we know if we need to update the <select>'s
      // v-model when the option list has changed.
      // because v-model has a lower priority than v-for,
      // the v-model is not bound here yet, so we have to
      // retrive it in the actual updateModel() function.
      var tag = this.el.tagName;
      this.isOption = (tag === 'OPTION' || tag === 'OPTGROUP') && this.el.parentNode.tagName === 'SELECT';

      // setup anchor nodes
      this.start = createAnchor('v-for-start');
      this.end = createAnchor('v-for-end');
      replace(this.el, this.end);
      before(this.start, this.end);

      // cache
      this.cache = Object.create(null);

      // fragment factory
      this.factory = new FragmentFactory(this.vm, this.el);
    },

    update: function update(data) {
      this.diff(data);
      this.updateRef();
      this.updateModel();
    },

    /**
     * Diff, based on new data and old data, determine the
     * minimum amount of DOM manipulations needed to make the
     * DOM reflect the new data Array.
     *
     * The algorithm diffs the new data Array by storing a
     * hidden reference to an owner vm instance on previously
     * seen data. This allows us to achieve O(n) which is
     * better than a levenshtein distance based algorithm,
     * which is O(m * n).
     *
     * @param {Array} data
     */

    diff: function diff(data) {
      // check if the Array was converted from an Object
      var item = data[0];
      var convertedFromObject = this.fromObject = isObject(item) && hasOwn(item, '$key') && hasOwn(item, '$value');

      var trackByKey = this.params.trackBy;
      var oldFrags = this.frags;
      var frags = this.frags = new Array(data.length);
      var alias = this.alias;
      var iterator = this.iterator;
      var start = this.start;
      var end = this.end;
      var inDocument = inDoc(start);
      var init = !oldFrags;
      var i, l, frag, key, value, primitive;

      // First pass, go through the new Array and fill up
      // the new frags array. If a piece of data has a cached
      // instance for it, we reuse it. Otherwise build a new
      // instance.
      for (i = 0, l = data.length; i < l; i++) {
        item = data[i];
        key = convertedFromObject ? item.$key : null;
        value = convertedFromObject ? item.$value : item;
        primitive = !isObject(value);
        frag = !init && this.getCachedFrag(value, i, key);
        if (frag) {
          // reusable fragment
          frag.reused = true;
          // update $index
          frag.scope.$index = i;
          // update $key
          if (key) {
            frag.scope.$key = key;
          }
          // update iterator
          if (iterator) {
            frag.scope[iterator] = key !== null ? key : i;
          }
          // update data for track-by, object repeat &
          // primitive values.
          if (trackByKey || convertedFromObject || primitive) {
            withoutConversion(function () {
              frag.scope[alias] = value;
            });
          }
        } else {
          // new isntance
          frag = this.create(value, alias, i, key);
          frag.fresh = !init;
        }
        frags[i] = frag;
        if (init) {
          frag.before(end);
        }
      }

      // we're done for the initial render.
      if (init) {
        return;
      }

      // Second pass, go through the old fragments and
      // destroy those who are not reused (and remove them
      // from cache)
      var removalIndex = 0;
      var totalRemoved = oldFrags.length - frags.length;
      // when removing a large number of fragments, watcher removal
      // turns out to be a perf bottleneck, so we batch the watcher
      // removals into a single filter call!
      this.vm._vForRemoving = true;
      for (i = 0, l = oldFrags.length; i < l; i++) {
        frag = oldFrags[i];
        if (!frag.reused) {
          this.deleteCachedFrag(frag);
          this.remove(frag, removalIndex++, totalRemoved, inDocument);
        }
      }
      this.vm._vForRemoving = false;
      if (removalIndex) {
        this.vm._watchers = this.vm._watchers.filter(function (w) {
          return w.active;
        });
      }

      // Final pass, move/insert new fragments into the
      // right place.
      var targetPrev, prevEl, currentPrev;
      var insertionIndex = 0;
      for (i = 0, l = frags.length; i < l; i++) {
        frag = frags[i];
        // this is the frag that we should be after
        targetPrev = frags[i - 1];
        prevEl = targetPrev ? targetPrev.staggerCb ? targetPrev.staggerAnchor : targetPrev.end || targetPrev.node : start;
        if (frag.reused && !frag.staggerCb) {
          currentPrev = findPrevFrag(frag, start, this.id);
          if (currentPrev !== targetPrev && (!currentPrev ||
          // optimization for moving a single item.
          // thanks to suggestions by @livoras in #1807
          findPrevFrag(currentPrev, start, this.id) !== targetPrev)) {
            this.move(frag, prevEl);
          }
        } else {
          // new instance, or still in stagger.
          // insert with updated stagger index.
          this.insert(frag, insertionIndex++, prevEl, inDocument);
        }
        frag.reused = frag.fresh = false;
      }
    },

    /**
     * Create a new fragment instance.
     *
     * @param {*} value
     * @param {String} alias
     * @param {Number} index
     * @param {String} [key]
     * @return {Fragment}
     */

    create: function create(value, alias, index, key) {
      var host = this._host;
      // create iteration scope
      var parentScope = this._scope || this.vm;
      var scope = Object.create(parentScope);
      // ref holder for the scope
      scope.$refs = Object.create(parentScope.$refs);
      scope.$els = Object.create(parentScope.$els);
      // make sure point $parent to parent scope
      scope.$parent = parentScope;
      // for two-way binding on alias
      scope.$forContext = this;
      // define scope properties
      // important: define the scope alias without forced conversion
      // so that frozen data structures remain non-reactive.
      withoutConversion(function () {
        defineReactive(scope, alias, value);
      });
      defineReactive(scope, '$index', index);
      if (key) {
        defineReactive(scope, '$key', key);
      } else if (scope.$key) {
        // avoid accidental fallback
        def(scope, '$key', null);
      }
      if (this.iterator) {
        defineReactive(scope, this.iterator, key !== null ? key : index);
      }
      var frag = this.factory.create(host, scope, this._frag);
      frag.forId = this.id;
      this.cacheFrag(value, frag, index, key);
      return frag;
    },

    /**
     * Update the v-ref on owner vm.
     */

    updateRef: function updateRef() {
      var ref = this.descriptor.ref;
      if (!ref) return;
      var hash = (this._scope || this.vm).$refs;
      var refs;
      if (!this.fromObject) {
        refs = this.frags.map(findVmFromFrag);
      } else {
        refs = {};
        this.frags.forEach(function (frag) {
          refs[frag.scope.$key] = findVmFromFrag(frag);
        });
      }
      hash[ref] = refs;
    },

    /**
     * For option lists, update the containing v-model on
     * parent <select>.
     */

    updateModel: function updateModel() {
      if (this.isOption) {
        var parent = this.start.parentNode;
        var model = parent && parent.__v_model;
        if (model) {
          model.forceUpdate();
        }
      }
    },

    /**
     * Insert a fragment. Handles staggering.
     *
     * @param {Fragment} frag
     * @param {Number} index
     * @param {Node} prevEl
     * @param {Boolean} inDocument
     */

    insert: function insert(frag, index, prevEl, inDocument) {
      if (frag.staggerCb) {
        frag.staggerCb.cancel();
        frag.staggerCb = null;
      }
      var staggerAmount = this.getStagger(frag, index, null, 'enter');
      if (inDocument && staggerAmount) {
        // create an anchor and insert it synchronously,
        // so that we can resolve the correct order without
        // worrying about some elements not inserted yet
        var anchor = frag.staggerAnchor;
        if (!anchor) {
          anchor = frag.staggerAnchor = createAnchor('stagger-anchor');
          anchor.__v_frag = frag;
        }
        after(anchor, prevEl);
        var op = frag.staggerCb = cancellable(function () {
          frag.staggerCb = null;
          frag.before(anchor);
          remove(anchor);
        });
        setTimeout(op, staggerAmount);
      } else {
        var target = prevEl.nextSibling;
        /* istanbul ignore if */
        if (!target) {
          // reset end anchor position in case the position was messed up
          // by an external drag-n-drop library.
          after(this.end, prevEl);
          target = this.end;
        }
        frag.before(target);
      }
    },

    /**
     * Remove a fragment. Handles staggering.
     *
     * @param {Fragment} frag
     * @param {Number} index
     * @param {Number} total
     * @param {Boolean} inDocument
     */

    remove: function remove(frag, index, total, inDocument) {
      if (frag.staggerCb) {
        frag.staggerCb.cancel();
        frag.staggerCb = null;
        // it's not possible for the same frag to be removed
        // twice, so if we have a pending stagger callback,
        // it means this frag is queued for enter but removed
        // before its transition started. Since it is already
        // destroyed, we can just leave it in detached state.
        return;
      }
      var staggerAmount = this.getStagger(frag, index, total, 'leave');
      if (inDocument && staggerAmount) {
        var op = frag.staggerCb = cancellable(function () {
          frag.staggerCb = null;
          frag.remove();
        });
        setTimeout(op, staggerAmount);
      } else {
        frag.remove();
      }
    },

    /**
     * Move a fragment to a new position.
     * Force no transition.
     *
     * @param {Fragment} frag
     * @param {Node} prevEl
     */

    move: function move(frag, prevEl) {
      // fix a common issue with Sortable:
      // if prevEl doesn't have nextSibling, this means it's
      // been dragged after the end anchor. Just re-position
      // the end anchor to the end of the container.
      /* istanbul ignore if */
      if (!prevEl.nextSibling) {
        this.end.parentNode.appendChild(this.end);
      }
      frag.before(prevEl.nextSibling, false);
    },

    /**
     * Cache a fragment using track-by or the object key.
     *
     * @param {*} value
     * @param {Fragment} frag
     * @param {Number} index
     * @param {String} [key]
     */

    cacheFrag: function cacheFrag(value, frag, index, key) {
      var trackByKey = this.params.trackBy;
      var cache = this.cache;
      var primitive = !isObject(value);
      var id;
      if (key || trackByKey || primitive) {
        id = getTrackByKey(index, key, value, trackByKey);
        if (!cache[id]) {
          cache[id] = frag;
        } else if (trackByKey !== '$index') {
          'development' !== 'production' && this.warnDuplicate(value);
        }
      } else {
        id = this.id;
        if (hasOwn(value, id)) {
          if (value[id] === null) {
            value[id] = frag;
          } else {
            'development' !== 'production' && this.warnDuplicate(value);
          }
        } else if (Object.isExtensible(value)) {
          def(value, id, frag);
        } else if ('development' !== 'production') {
          warn('Frozen v-for objects cannot be automatically tracked, make sure to ' + 'provide a track-by key.');
        }
      }
      frag.raw = value;
    },

    /**
     * Get a cached fragment from the value/index/key
     *
     * @param {*} value
     * @param {Number} index
     * @param {String} key
     * @return {Fragment}
     */

    getCachedFrag: function getCachedFrag(value, index, key) {
      var trackByKey = this.params.trackBy;
      var primitive = !isObject(value);
      var frag;
      if (key || trackByKey || primitive) {
        var id = getTrackByKey(index, key, value, trackByKey);
        frag = this.cache[id];
      } else {
        frag = value[this.id];
      }
      if (frag && (frag.reused || frag.fresh)) {
        'development' !== 'production' && this.warnDuplicate(value);
      }
      return frag;
    },

    /**
     * Delete a fragment from cache.
     *
     * @param {Fragment} frag
     */

    deleteCachedFrag: function deleteCachedFrag(frag) {
      var value = frag.raw;
      var trackByKey = this.params.trackBy;
      var scope = frag.scope;
      var index = scope.$index;
      // fix #948: avoid accidentally fall through to
      // a parent repeater which happens to have $key.
      var key = hasOwn(scope, '$key') && scope.$key;
      var primitive = !isObject(value);
      if (trackByKey || key || primitive) {
        var id = getTrackByKey(index, key, value, trackByKey);
        this.cache[id] = null;
      } else {
        value[this.id] = null;
        frag.raw = null;
      }
    },

    /**
     * Get the stagger amount for an insertion/removal.
     *
     * @param {Fragment} frag
     * @param {Number} index
     * @param {Number} total
     * @param {String} type
     */

    getStagger: function getStagger(frag, index, total, type) {
      type = type + 'Stagger';
      var trans = frag.node.__v_trans;
      var hooks = trans && trans.hooks;
      var hook = hooks && (hooks[type] || hooks.stagger);
      return hook ? hook.call(frag, index, total) : index * parseInt(this.params[type] || this.params.stagger, 10);
    },

    /**
     * Pre-process the value before piping it through the
     * filters. This is passed to and called by the watcher.
     */

    _preProcess: function _preProcess(value) {
      // regardless of type, store the un-filtered raw value.
      this.rawValue = value;
      return value;
    },

    /**
     * Post-process the value after it has been piped through
     * the filters. This is passed to and called by the watcher.
     *
     * It is necessary for this to be called during the
     * watcher's dependency collection phase because we want
     * the v-for to update when the source Object is mutated.
     */

    _postProcess: function _postProcess(value) {
      if (isArray(value)) {
        return value;
      } else if (isPlainObject(value)) {
        // convert plain object to array.
        var keys = Object.keys(value);
        var i = keys.length;
        var res = new Array(i);
        var key;
        while (i--) {
          key = keys[i];
          res[i] = {
            $key: key,
            $value: value[key]
          };
        }
        return res;
      } else {
        if (typeof value === 'number' && !isNaN(value)) {
          value = range(value);
        }
        return value || [];
      }
    },

    unbind: function unbind() {
      if (this.descriptor.ref) {
        (this._scope || this.vm).$refs[this.descriptor.ref] = null;
      }
      if (this.frags) {
        var i = this.frags.length;
        var frag;
        while (i--) {
          frag = this.frags[i];
          this.deleteCachedFrag(frag);
          frag.destroy();
        }
      }
    }
  };

  /**
   * Helper to find the previous element that is a fragment
   * anchor. This is necessary because a destroyed frag's
   * element could still be lingering in the DOM before its
   * leaving transition finishes, but its inserted flag
   * should have been set to false so we can skip them.
   *
   * If this is a block repeat, we want to make sure we only
   * return frag that is bound to this v-for. (see #929)
   *
   * @param {Fragment} frag
   * @param {Comment|Text} anchor
   * @param {String} id
   * @return {Fragment}
   */

  function findPrevFrag(frag, anchor, id) {
    var el = frag.node.previousSibling;
    /* istanbul ignore if */
    if (!el) return;
    frag = el.__v_frag;
    while ((!frag || frag.forId !== id || !frag.inserted) && el !== anchor) {
      el = el.previousSibling;
      /* istanbul ignore if */
      if (!el) return;
      frag = el.__v_frag;
    }
    return frag;
  }

  /**
   * Find a vm from a fragment.
   *
   * @param {Fragment} frag
   * @return {Vue|undefined}
   */

  function findVmFromFrag(frag) {
    var node = frag.node;
    // handle multi-node frag
    if (frag.end) {
      while (!node.__vue__ && node !== frag.end && node.nextSibling) {
        node = node.nextSibling;
      }
    }
    return node.__vue__;
  }

  /**
   * Create a range array from given number.
   *
   * @param {Number} n
   * @return {Array}
   */

  function range(n) {
    var i = -1;
    var ret = new Array(Math.floor(n));
    while (++i < n) {
      ret[i] = i;
    }
    return ret;
  }

  /**
   * Get the track by key for an item.
   *
   * @param {Number} index
   * @param {String} key
   * @param {*} value
   * @param {String} [trackByKey]
   */

  function getTrackByKey(index, key, value, trackByKey) {
    return trackByKey ? trackByKey === '$index' ? index : trackByKey.charAt(0).match(/\w/) ? getPath(value, trackByKey) : value[trackByKey] : key || value;
  }

  if ('development' !== 'production') {
    vFor.warnDuplicate = function (value) {
      warn('Duplicate value found in v-for="' + this.descriptor.raw + '": ' + JSON.stringify(value) + '. Use track-by="$index" if ' + 'you are expecting duplicate values.', this.vm);
    };
  }

  var vIf = {

    priority: IF,
    terminal: true,

    bind: function bind() {
      var el = this.el;
      if (!el.__vue__) {
        // check else block
        var next = el.nextElementSibling;
        if (next && getAttr(next, 'v-else') !== null) {
          remove(next);
          this.elseEl = next;
        }
        // check main block
        this.anchor = createAnchor('v-if');
        replace(el, this.anchor);
      } else {
        'development' !== 'production' && warn('v-if="' + this.expression + '" cannot be ' + 'used on an instance root element.', this.vm);
        this.invalid = true;
      }
    },

    update: function update(value) {
      if (this.invalid) return;
      if (value) {
        if (!this.frag) {
          this.insert();
        }
      } else {
        this.remove();
      }
    },

    insert: function insert() {
      if (this.elseFrag) {
        this.elseFrag.remove();
        this.elseFrag = null;
      }
      // lazy init factory
      if (!this.factory) {
        this.factory = new FragmentFactory(this.vm, this.el);
      }
      this.frag = this.factory.create(this._host, this._scope, this._frag);
      this.frag.before(this.anchor);
    },

    remove: function remove() {
      if (this.frag) {
        this.frag.remove();
        this.frag = null;
      }
      if (this.elseEl && !this.elseFrag) {
        if (!this.elseFactory) {
          this.elseFactory = new FragmentFactory(this.elseEl._context || this.vm, this.elseEl);
        }
        this.elseFrag = this.elseFactory.create(this._host, this._scope, this._frag);
        this.elseFrag.before(this.anchor);
      }
    },

    unbind: function unbind() {
      if (this.frag) {
        this.frag.destroy();
      }
      if (this.elseFrag) {
        this.elseFrag.destroy();
      }
    }
  };

  var show = {

    bind: function bind() {
      // check else block
      var next = this.el.nextElementSibling;
      if (next && getAttr(next, 'v-else') !== null) {
        this.elseEl = next;
      }
    },

    update: function update(value) {
      this.apply(this.el, value);
      if (this.elseEl) {
        this.apply(this.elseEl, !value);
      }
    },

    apply: function apply(el, value) {
      if (inDoc(el)) {
        applyTransition(el, value ? 1 : -1, toggle, this.vm);
      } else {
        toggle();
      }
      function toggle() {
        el.style.display = value ? '' : 'none';
      }
    }
  };

  var text$2 = {

    bind: function bind() {
      var self = this;
      var el = this.el;
      var isRange = el.type === 'range';
      var lazy = this.params.lazy;
      var number = this.params.number;
      var debounce = this.params.debounce;

      // handle composition events.
      //   http://blog.evanyou.me/2014/01/03/composition-event/
      // skip this for Android because it handles composition
      // events quite differently. Android doesn't trigger
      // composition events for language input methods e.g.
      // Chinese, but instead triggers them for spelling
      // suggestions... (see Discussion/#162)
      var composing = false;
      if (!isAndroid && !isRange) {
        this.on('compositionstart', function () {
          composing = true;
        });
        this.on('compositionend', function () {
          composing = false;
          // in IE11 the "compositionend" event fires AFTER
          // the "input" event, so the input handler is blocked
          // at the end... have to call it here.
          //
          // #1327: in lazy mode this is unecessary.
          if (!lazy) {
            self.listener();
          }
        });
      }

      // prevent messing with the input when user is typing,
      // and force update on blur.
      this.focused = false;
      if (!isRange && !lazy) {
        this.on('focus', function () {
          self.focused = true;
        });
        this.on('blur', function () {
          self.focused = false;
          // do not sync value after fragment removal (#2017)
          if (!self._frag || self._frag.inserted) {
            self.rawListener();
          }
        });
      }

      // Now attach the main listener
      this.listener = this.rawListener = function () {
        if (composing || !self._bound) {
          return;
        }
        var val = number || isRange ? toNumber(el.value) : el.value;
        self.set(val);
        // force update on next tick to avoid lock & same value
        // also only update when user is not typing
        nextTick(function () {
          if (self._bound && !self.focused) {
            self.update(self._watcher.value);
          }
        });
      };

      // apply debounce
      if (debounce) {
        this.listener = _debounce(this.listener, debounce);
      }

      // Support jQuery events, since jQuery.trigger() doesn't
      // trigger native events in some cases and some plugins
      // rely on $.trigger()
      //
      // We want to make sure if a listener is attached using
      // jQuery, it is also removed with jQuery, that's why
      // we do the check for each directive instance and
      // store that check result on itself. This also allows
      // easier test coverage control by unsetting the global
      // jQuery variable in tests.
      this.hasjQuery = typeof jQuery === 'function';
      if (this.hasjQuery) {
        var method = jQuery.fn.on ? 'on' : 'bind';
        jQuery(el)[method]('change', this.rawListener);
        if (!lazy) {
          jQuery(el)[method]('input', this.listener);
        }
      } else {
        this.on('change', this.rawListener);
        if (!lazy) {
          this.on('input', this.listener);
        }
      }

      // IE9 doesn't fire input event on backspace/del/cut
      if (!lazy && isIE9) {
        this.on('cut', function () {
          nextTick(self.listener);
        });
        this.on('keyup', function (e) {
          if (e.keyCode === 46 || e.keyCode === 8) {
            self.listener();
          }
        });
      }

      // set initial value if present
      if (el.hasAttribute('value') || el.tagName === 'TEXTAREA' && el.value.trim()) {
        this.afterBind = this.listener;
      }
    },

    update: function update(value) {
      // #3029 only update when the value changes. This prevent
      // browsers from overwriting values like selectionStart
      value = _toString(value);
      if (value !== this.el.value) this.el.value = value;
    },

    unbind: function unbind() {
      var el = this.el;
      if (this.hasjQuery) {
        var method = jQuery.fn.off ? 'off' : 'unbind';
        jQuery(el)[method]('change', this.listener);
        jQuery(el)[method]('input', this.listener);
      }
    }
  };

  var radio = {

    bind: function bind() {
      var self = this;
      var el = this.el;

      this.getValue = function () {
        // value overwrite via v-bind:value
        if (el.hasOwnProperty('_value')) {
          return el._value;
        }
        var val = el.value;
        if (self.params.number) {
          val = toNumber(val);
        }
        return val;
      };

      this.listener = function () {
        self.set(self.getValue());
      };
      this.on('change', this.listener);

      if (el.hasAttribute('checked')) {
        this.afterBind = this.listener;
      }
    },

    update: function update(value) {
      this.el.checked = looseEqual(value, this.getValue());
    }
  };

  var select = {

    bind: function bind() {
      var _this = this;

      var self = this;
      var el = this.el;

      // method to force update DOM using latest value.
      this.forceUpdate = function () {
        if (self._watcher) {
          self.update(self._watcher.get());
        }
      };

      // check if this is a multiple select
      var multiple = this.multiple = el.hasAttribute('multiple');

      // attach listener
      this.listener = function () {
        var value = getValue(el, multiple);
        value = self.params.number ? isArray(value) ? value.map(toNumber) : toNumber(value) : value;
        self.set(value);
      };
      this.on('change', this.listener);

      // if has initial value, set afterBind
      var initValue = getValue(el, multiple, true);
      if (multiple && initValue.length || !multiple && initValue !== null) {
        this.afterBind = this.listener;
      }

      // All major browsers except Firefox resets
      // selectedIndex with value -1 to 0 when the element
      // is appended to a new parent, therefore we have to
      // force a DOM update whenever that happens...
      this.vm.$on('hook:attached', function () {
        nextTick(_this.forceUpdate);
      });
      if (!inDoc(el)) {
        nextTick(this.forceUpdate);
      }
    },

    update: function update(value) {
      var el = this.el;
      el.selectedIndex = -1;
      var multi = this.multiple && isArray(value);
      var options = el.options;
      var i = options.length;
      var op, val;
      while (i--) {
        op = options[i];
        val = op.hasOwnProperty('_value') ? op._value : op.value;
        /* eslint-disable eqeqeq */
        op.selected = multi ? indexOf$1(value, val) > -1 : looseEqual(value, val);
        /* eslint-enable eqeqeq */
      }
    },

    unbind: function unbind() {
      /* istanbul ignore next */
      this.vm.$off('hook:attached', this.forceUpdate);
    }
  };

  /**
   * Get select value
   *
   * @param {SelectElement} el
   * @param {Boolean} multi
   * @param {Boolean} init
   * @return {Array|*}
   */

  function getValue(el, multi, init) {
    var res = multi ? [] : null;
    var op, val, selected;
    for (var i = 0, l = el.options.length; i < l; i++) {
      op = el.options[i];
      selected = init ? op.hasAttribute('selected') : op.selected;
      if (selected) {
        val = op.hasOwnProperty('_value') ? op._value : op.value;
        if (multi) {
          res.push(val);
        } else {
          return val;
        }
      }
    }
    return res;
  }

  /**
   * Native Array.indexOf uses strict equal, but in this
   * case we need to match string/numbers with custom equal.
   *
   * @param {Array} arr
   * @param {*} val
   */

  function indexOf$1(arr, val) {
    var i = arr.length;
    while (i--) {
      if (looseEqual(arr[i], val)) {
        return i;
      }
    }
    return -1;
  }

  var checkbox = {

    bind: function bind() {
      var self = this;
      var el = this.el;

      this.getValue = function () {
        return el.hasOwnProperty('_value') ? el._value : self.params.number ? toNumber(el.value) : el.value;
      };

      function getBooleanValue() {
        var val = el.checked;
        if (val && el.hasOwnProperty('_trueValue')) {
          return el._trueValue;
        }
        if (!val && el.hasOwnProperty('_falseValue')) {
          return el._falseValue;
        }
        return val;
      }

      this.listener = function () {
        var model = self._watcher.value;
        if (isArray(model)) {
          var val = self.getValue();
          if (el.checked) {
            if (indexOf(model, val) < 0) {
              model.push(val);
            }
          } else {
            model.$remove(val);
          }
        } else {
          self.set(getBooleanValue());
        }
      };

      this.on('change', this.listener);
      if (el.hasAttribute('checked')) {
        this.afterBind = this.listener;
      }
    },

    update: function update(value) {
      var el = this.el;
      if (isArray(value)) {
        el.checked = indexOf(value, this.getValue()) > -1;
      } else {
        if (el.hasOwnProperty('_trueValue')) {
          el.checked = looseEqual(value, el._trueValue);
        } else {
          el.checked = !!value;
        }
      }
    }
  };

  var handlers = {
    text: text$2,
    radio: radio,
    select: select,
    checkbox: checkbox
  };

  var model = {

    priority: MODEL,
    twoWay: true,
    handlers: handlers,
    params: ['lazy', 'number', 'debounce'],

    /**
     * Possible elements:
     *   <select>
     *   <textarea>
     *   <input type="*">
     *     - text
     *     - checkbox
     *     - radio
     *     - number
     */

    bind: function bind() {
      // friendly warning...
      this.checkFilters();
      if (this.hasRead && !this.hasWrite) {
        'development' !== 'production' && warn('It seems you are using a read-only filter with ' + 'v-model="' + this.descriptor.raw + '". ' + 'You might want to use a two-way filter to ensure correct behavior.', this.vm);
      }
      var el = this.el;
      var tag = el.tagName;
      var handler;
      if (tag === 'INPUT') {
        handler = handlers[el.type] || handlers.text;
      } else if (tag === 'SELECT') {
        handler = handlers.select;
      } else if (tag === 'TEXTAREA') {
        handler = handlers.text;
      } else {
        'development' !== 'production' && warn('v-model does not support element type: ' + tag, this.vm);
        return;
      }
      el.__v_model = this;
      handler.bind.call(this);
      this.update = handler.update;
      this._unbind = handler.unbind;
    },

    /**
     * Check read/write filter stats.
     */

    checkFilters: function checkFilters() {
      var filters = this.filters;
      if (!filters) return;
      var i = filters.length;
      while (i--) {
        var filter = resolveAsset(this.vm.$options, 'filters', filters[i].name);
        if (typeof filter === 'function' || filter.read) {
          this.hasRead = true;
        }
        if (filter.write) {
          this.hasWrite = true;
        }
      }
    },

    unbind: function unbind() {
      this.el.__v_model = null;
      this._unbind && this._unbind();
    }
  };

  // keyCode aliases
  var keyCodes = {
    esc: 27,
    tab: 9,
    enter: 13,
    space: 32,
    'delete': [8, 46],
    up: 38,
    left: 37,
    right: 39,
    down: 40
  };

  function keyFilter(handler, keys) {
    var codes = keys.map(function (key) {
      var charCode = key.charCodeAt(0);
      if (charCode > 47 && charCode < 58) {
        return parseInt(key, 10);
      }
      if (key.length === 1) {
        charCode = key.toUpperCase().charCodeAt(0);
        if (charCode > 64 && charCode < 91) {
          return charCode;
        }
      }
      return keyCodes[key];
    });
    codes = [].concat.apply([], codes);
    return function keyHandler(e) {
      if (codes.indexOf(e.keyCode) > -1) {
        return handler.call(this, e);
      }
    };
  }

  function stopFilter(handler) {
    return function stopHandler(e) {
      e.stopPropagation();
      return handler.call(this, e);
    };
  }

  function preventFilter(handler) {
    return function preventHandler(e) {
      e.preventDefault();
      return handler.call(this, e);
    };
  }

  function selfFilter(handler) {
    return function selfHandler(e) {
      if (e.target === e.currentTarget) {
        return handler.call(this, e);
      }
    };
  }

  var on$1 = {

    priority: ON,
    acceptStatement: true,
    keyCodes: keyCodes,

    bind: function bind() {
      // deal with iframes
      if (this.el.tagName === 'IFRAME' && this.arg !== 'load') {
        var self = this;
        this.iframeBind = function () {
          on(self.el.contentWindow, self.arg, self.handler, self.modifiers.capture);
        };
        this.on('load', this.iframeBind);
      }
    },

    update: function update(handler) {
      // stub a noop for v-on with no value,
      // e.g. @mousedown.prevent
      if (!this.descriptor.raw) {
        handler = function () {};
      }

      if (typeof handler !== 'function') {
        'development' !== 'production' && warn('v-on:' + this.arg + '="' + this.expression + '" expects a function value, ' + 'got ' + handler, this.vm);
        return;
      }

      // apply modifiers
      if (this.modifiers.stop) {
        handler = stopFilter(handler);
      }
      if (this.modifiers.prevent) {
        handler = preventFilter(handler);
      }
      if (this.modifiers.self) {
        handler = selfFilter(handler);
      }
      // key filter
      var keys = Object.keys(this.modifiers).filter(function (key) {
        return key !== 'stop' && key !== 'prevent' && key !== 'self' && key !== 'capture';
      });
      if (keys.length) {
        handler = keyFilter(handler, keys);
      }

      this.reset();
      this.handler = handler;

      if (this.iframeBind) {
        this.iframeBind();
      } else {
        on(this.el, this.arg, this.handler, this.modifiers.capture);
      }
    },

    reset: function reset() {
      var el = this.iframeBind ? this.el.contentWindow : this.el;
      if (this.handler) {
        off(el, this.arg, this.handler);
      }
    },

    unbind: function unbind() {
      this.reset();
    }
  };

  var prefixes = ['-webkit-', '-moz-', '-ms-'];
  var camelPrefixes = ['Webkit', 'Moz', 'ms'];
  var importantRE = /!important;?$/;
  var propCache = Object.create(null);

  var testEl = null;

  var style = {

    deep: true,

    update: function update(value) {
      if (typeof value === 'string') {
        this.el.style.cssText = value;
      } else if (isArray(value)) {
        this.handleObject(value.reduce(extend, {}));
      } else {
        this.handleObject(value || {});
      }
    },

    handleObject: function handleObject(value) {
      // cache object styles so that only changed props
      // are actually updated.
      var cache = this.cache || (this.cache = {});
      var name, val;
      for (name in cache) {
        if (!(name in value)) {
          this.handleSingle(name, null);
          delete cache[name];
        }
      }
      for (name in value) {
        val = value[name];
        if (val !== cache[name]) {
          cache[name] = val;
          this.handleSingle(name, val);
        }
      }
    },

    handleSingle: function handleSingle(prop, value) {
      prop = normalize(prop);
      if (!prop) return; // unsupported prop
      // cast possible numbers/booleans into strings
      if (value != null) value += '';
      if (value) {
        var isImportant = importantRE.test(value) ? 'important' : '';
        if (isImportant) {
          /* istanbul ignore if */
          if ('development' !== 'production') {
            warn('It\'s probably a bad idea to use !important with inline rules. ' + 'This feature will be deprecated in a future version of Vue.');
          }
          value = value.replace(importantRE, '').trim();
          this.el.style.setProperty(prop.kebab, value, isImportant);
        } else {
          this.el.style[prop.camel] = value;
        }
      } else {
        this.el.style[prop.camel] = '';
      }
    }

  };

  /**
   * Normalize a CSS property name.
   * - cache result
   * - auto prefix
   * - camelCase -> dash-case
   *
   * @param {String} prop
   * @return {String}
   */

  function normalize(prop) {
    if (propCache[prop]) {
      return propCache[prop];
    }
    var res = prefix(prop);
    propCache[prop] = propCache[res] = res;
    return res;
  }

  /**
   * Auto detect the appropriate prefix for a CSS property.
   * https://gist.github.com/paulirish/523692
   *
   * @param {String} prop
   * @return {String}
   */

  function prefix(prop) {
    prop = hyphenate(prop);
    var camel = camelize(prop);
    var upper = camel.charAt(0).toUpperCase() + camel.slice(1);
    if (!testEl) {
      testEl = document.createElement('div');
    }
    var i = prefixes.length;
    var prefixed;
    if (camel !== 'filter' && camel in testEl.style) {
      return {
        kebab: prop,
        camel: camel
      };
    }
    while (i--) {
      prefixed = camelPrefixes[i] + upper;
      if (prefixed in testEl.style) {
        return {
          kebab: prefixes[i] + prop,
          camel: prefixed
        };
      }
    }
  }

  // xlink
  var xlinkNS = 'http://www.w3.org/1999/xlink';
  var xlinkRE = /^xlink:/;

  // check for attributes that prohibit interpolations
  var disallowedInterpAttrRE = /^v-|^:|^@|^(?:is|transition|transition-mode|debounce|track-by|stagger|enter-stagger|leave-stagger)$/;
  // these attributes should also set their corresponding properties
  // because they only affect the initial state of the element
  var attrWithPropsRE = /^(?:value|checked|selected|muted)$/;
  // these attributes expect enumrated values of "true" or "false"
  // but are not boolean attributes
  var enumeratedAttrRE = /^(?:draggable|contenteditable|spellcheck)$/;

  // these attributes should set a hidden property for
  // binding v-model to object values
  var modelProps = {
    value: '_value',
    'true-value': '_trueValue',
    'false-value': '_falseValue'
  };

  var bind$1 = {

    priority: BIND,

    bind: function bind() {
      var attr = this.arg;
      var tag = this.el.tagName;
      // should be deep watch on object mode
      if (!attr) {
        this.deep = true;
      }
      // handle interpolation bindings
      var descriptor = this.descriptor;
      var tokens = descriptor.interp;
      if (tokens) {
        // handle interpolations with one-time tokens
        if (descriptor.hasOneTime) {
          this.expression = tokensToExp(tokens, this._scope || this.vm);
        }

        // only allow binding on native attributes
        if (disallowedInterpAttrRE.test(attr) || attr === 'name' && (tag === 'PARTIAL' || tag === 'SLOT')) {
          'development' !== 'production' && warn(attr + '="' + descriptor.raw + '": ' + 'attribute interpolation is not allowed in Vue.js ' + 'directives and special attributes.', this.vm);
          this.el.removeAttribute(attr);
          this.invalid = true;
        }

        /* istanbul ignore if */
        if ('development' !== 'production') {
          var raw = attr + '="' + descriptor.raw + '": ';
          // warn src
          if (attr === 'src') {
            warn(raw + 'interpolation in "src" attribute will cause ' + 'a 404 request. Use v-bind:src instead.', this.vm);
          }

          // warn style
          if (attr === 'style') {
            warn(raw + 'interpolation in "style" attribute will cause ' + 'the attribute to be discarded in Internet Explorer. ' + 'Use v-bind:style instead.', this.vm);
          }
        }
      }
    },

    update: function update(value) {
      if (this.invalid) {
        return;
      }
      var attr = this.arg;
      if (this.arg) {
        this.handleSingle(attr, value);
      } else {
        this.handleObject(value || {});
      }
    },

    // share object handler with v-bind:class
    handleObject: style.handleObject,

    handleSingle: function handleSingle(attr, value) {
      var el = this.el;
      var interp = this.descriptor.interp;
      if (this.modifiers.camel) {
        attr = camelize(attr);
      }
      if (!interp && attrWithPropsRE.test(attr) && attr in el) {
        var attrValue = attr === 'value' ? value == null // IE9 will set input.value to "null" for null...
        ? '' : value : value;

        if (el[attr] !== attrValue) {
          el[attr] = attrValue;
        }
      }
      // set model props
      var modelProp = modelProps[attr];
      if (!interp && modelProp) {
        el[modelProp] = value;
        // update v-model if present
        var model = el.__v_model;
        if (model) {
          model.listener();
        }
      }
      // do not set value attribute for textarea
      if (attr === 'value' && el.tagName === 'TEXTAREA') {
        el.removeAttribute(attr);
        return;
      }
      // update attribute
      if (enumeratedAttrRE.test(attr)) {
        el.setAttribute(attr, value ? 'true' : 'false');
      } else if (value != null && value !== false) {
        if (attr === 'class') {
          // handle edge case #1960:
          // class interpolation should not overwrite Vue transition class
          if (el.__v_trans) {
            value += ' ' + el.__v_trans.id + '-transition';
          }
          setClass(el, value);
        } else if (xlinkRE.test(attr)) {
          el.setAttributeNS(xlinkNS, attr, value === true ? '' : value);
        } else {
          el.setAttribute(attr, value === true ? '' : value);
        }
      } else {
        el.removeAttribute(attr);
      }
    }
  };

  var el = {

    priority: EL,

    bind: function bind() {
      /* istanbul ignore if */
      if (!this.arg) {
        return;
      }
      var id = this.id = camelize(this.arg);
      var refs = (this._scope || this.vm).$els;
      if (hasOwn(refs, id)) {
        refs[id] = this.el;
      } else {
        defineReactive(refs, id, this.el);
      }
    },

    unbind: function unbind() {
      var refs = (this._scope || this.vm).$els;
      if (refs[this.id] === this.el) {
        refs[this.id] = null;
      }
    }
  };

  var ref = {
    bind: function bind() {
      'development' !== 'production' && warn('v-ref:' + this.arg + ' must be used on a child ' + 'component. Found on <' + this.el.tagName.toLowerCase() + '>.', this.vm);
    }
  };

  var cloak = {
    bind: function bind() {
      var el = this.el;
      this.vm.$once('pre-hook:compiled', function () {
        el.removeAttribute('v-cloak');
      });
    }
  };

  // must export plain object
  var directives = {
    text: text$1,
    html: html,
    'for': vFor,
    'if': vIf,
    show: show,
    model: model,
    on: on$1,
    bind: bind$1,
    el: el,
    ref: ref,
    cloak: cloak
  };

  var vClass = {

    deep: true,

    update: function update(value) {
      if (!value) {
        this.cleanup();
      } else if (typeof value === 'string') {
        this.setClass(value.trim().split(/\s+/));
      } else {
        this.setClass(normalize$1(value));
      }
    },

    setClass: function setClass(value) {
      this.cleanup(value);
      for (var i = 0, l = value.length; i < l; i++) {
        var val = value[i];
        if (val) {
          apply(this.el, val, addClass);
        }
      }
      this.prevKeys = value;
    },

    cleanup: function cleanup(value) {
      var prevKeys = this.prevKeys;
      if (!prevKeys) return;
      var i = prevKeys.length;
      while (i--) {
        var key = prevKeys[i];
        if (!value || value.indexOf(key) < 0) {
          apply(this.el, key, removeClass);
        }
      }
    }
  };

  /**
   * Normalize objects and arrays (potentially containing objects)
   * into array of strings.
   *
   * @param {Object|Array<String|Object>} value
   * @return {Array<String>}
   */

  function normalize$1(value) {
    var res = [];
    if (isArray(value)) {
      for (var i = 0, l = value.length; i < l; i++) {
        var _key = value[i];
        if (_key) {
          if (typeof _key === 'string') {
            res.push(_key);
          } else {
            for (var k in _key) {
              if (_key[k]) res.push(k);
            }
          }
        }
      }
    } else if (isObject(value)) {
      for (var key in value) {
        if (value[key]) res.push(key);
      }
    }
    return res;
  }

  /**
   * Add or remove a class/classes on an element
   *
   * @param {Element} el
   * @param {String} key The class name. This may or may not
   *                     contain a space character, in such a
   *                     case we'll deal with multiple class
   *                     names at once.
   * @param {Function} fn
   */

  function apply(el, key, fn) {
    key = key.trim();
    if (key.indexOf(' ') === -1) {
      fn(el, key);
      return;
    }
    // The key contains one or more space characters.
    // Since a class name doesn't accept such characters, we
    // treat it as multiple classes.
    var keys = key.split(/\s+/);
    for (var i = 0, l = keys.length; i < l; i++) {
      fn(el, keys[i]);
    }
  }

  var component = {

    priority: COMPONENT,

    params: ['keep-alive', 'transition-mode', 'inline-template'],

    /**
     * Setup. Two possible usages:
     *
     * - static:
     *   <comp> or <div v-component="comp">
     *
     * - dynamic:
     *   <component :is="view">
     */

    bind: function bind() {
      if (!this.el.__vue__) {
        // keep-alive cache
        this.keepAlive = this.params.keepAlive;
        if (this.keepAlive) {
          this.cache = {};
        }
        // check inline-template
        if (this.params.inlineTemplate) {
          // extract inline template as a DocumentFragment
          this.inlineTemplate = extractContent(this.el, true);
        }
        // component resolution related state
        this.pendingComponentCb = this.Component = null;
        // transition related state
        this.pendingRemovals = 0;
        this.pendingRemovalCb = null;
        // create a ref anchor
        this.anchor = createAnchor('v-component');
        replace(this.el, this.anchor);
        // remove is attribute.
        // this is removed during compilation, but because compilation is
        // cached, when the component is used elsewhere this attribute
        // will remain at link time.
        this.el.removeAttribute('is');
        this.el.removeAttribute(':is');
        // remove ref, same as above
        if (this.descriptor.ref) {
          this.el.removeAttribute('v-ref:' + hyphenate(this.descriptor.ref));
        }
        // if static, build right now.
        if (this.literal) {
          this.setComponent(this.expression);
        }
      } else {
        'development' !== 'production' && warn('cannot mount component "' + this.expression + '" ' + 'on already mounted element: ' + this.el);
      }
    },

    /**
     * Public update, called by the watcher in the dynamic
     * literal scenario, e.g. <component :is="view">
     */

    update: function update(value) {
      if (!this.literal) {
        this.setComponent(value);
      }
    },

    /**
     * Switch dynamic components. May resolve the component
     * asynchronously, and perform transition based on
     * specified transition mode. Accepts a few additional
     * arguments specifically for vue-router.
     *
     * The callback is called when the full transition is
     * finished.
     *
     * @param {String} value
     * @param {Function} [cb]
     */

    setComponent: function setComponent(value, cb) {
      this.invalidatePending();
      if (!value) {
        // just remove current
        this.unbuild(true);
        this.remove(this.childVM, cb);
        this.childVM = null;
      } else {
        var self = this;
        this.resolveComponent(value, function () {
          self.mountComponent(cb);
        });
      }
    },

    /**
     * Resolve the component constructor to use when creating
     * the child vm.
     *
     * @param {String|Function} value
     * @param {Function} cb
     */

    resolveComponent: function resolveComponent(value, cb) {
      var self = this;
      this.pendingComponentCb = cancellable(function (Component) {
        self.ComponentName = Component.options.name || (typeof value === 'string' ? value : null);
        self.Component = Component;
        cb();
      });
      this.vm._resolveComponent(value, this.pendingComponentCb);
    },

    /**
     * Create a new instance using the current constructor and
     * replace the existing instance. This method doesn't care
     * whether the new component and the old one are actually
     * the same.
     *
     * @param {Function} [cb]
     */

    mountComponent: function mountComponent(cb) {
      // actual mount
      this.unbuild(true);
      var self = this;
      var activateHooks = this.Component.options.activate;
      var cached = this.getCached();
      var newComponent = this.build();
      if (activateHooks && !cached) {
        this.waitingFor = newComponent;
        callActivateHooks(activateHooks, newComponent, function () {
          if (self.waitingFor !== newComponent) {
            return;
          }
          self.waitingFor = null;
          self.transition(newComponent, cb);
        });
      } else {
        // update ref for kept-alive component
        if (cached) {
          newComponent._updateRef();
        }
        this.transition(newComponent, cb);
      }
    },

    /**
     * When the component changes or unbinds before an async
     * constructor is resolved, we need to invalidate its
     * pending callback.
     */

    invalidatePending: function invalidatePending() {
      if (this.pendingComponentCb) {
        this.pendingComponentCb.cancel();
        this.pendingComponentCb = null;
      }
    },

    /**
     * Instantiate/insert a new child vm.
     * If keep alive and has cached instance, insert that
     * instance; otherwise build a new one and cache it.
     *
     * @param {Object} [extraOptions]
     * @return {Vue} - the created instance
     */

    build: function build(extraOptions) {
      var cached = this.getCached();
      if (cached) {
        return cached;
      }
      if (this.Component) {
        // default options
        var options = {
          name: this.ComponentName,
          el: cloneNode(this.el),
          template: this.inlineTemplate,
          // make sure to add the child with correct parent
          // if this is a transcluded component, its parent
          // should be the transclusion host.
          parent: this._host || this.vm,
          // if no inline-template, then the compiled
          // linker can be cached for better performance.
          _linkerCachable: !this.inlineTemplate,
          _ref: this.descriptor.ref,
          _asComponent: true,
          _isRouterView: this._isRouterView,
          // if this is a transcluded component, context
          // will be the common parent vm of this instance
          // and its host.
          _context: this.vm,
          // if this is inside an inline v-for, the scope
          // will be the intermediate scope created for this
          // repeat fragment. this is used for linking props
          // and container directives.
          _scope: this._scope,
          // pass in the owner fragment of this component.
          // this is necessary so that the fragment can keep
          // track of its contained components in order to
          // call attach/detach hooks for them.
          _frag: this._frag
        };
        // extra options
        // in 1.0.0 this is used by vue-router only
        /* istanbul ignore if */
        if (extraOptions) {
          extend(options, extraOptions);
        }
        var child = new this.Component(options);
        if (this.keepAlive) {
          this.cache[this.Component.cid] = child;
        }
        /* istanbul ignore if */
        if ('development' !== 'production' && this.el.hasAttribute('transition') && child._isFragment) {
          warn('Transitions will not work on a fragment instance. ' + 'Template: ' + child.$options.template, child);
        }
        return child;
      }
    },

    /**
     * Try to get a cached instance of the current component.
     *
     * @return {Vue|undefined}
     */

    getCached: function getCached() {
      return this.keepAlive && this.cache[this.Component.cid];
    },

    /**
     * Teardown the current child, but defers cleanup so
     * that we can separate the destroy and removal steps.
     *
     * @param {Boolean} defer
     */

    unbuild: function unbuild(defer) {
      if (this.waitingFor) {
        if (!this.keepAlive) {
          this.waitingFor.$destroy();
        }
        this.waitingFor = null;
      }
      var child = this.childVM;
      if (!child || this.keepAlive) {
        if (child) {
          // remove ref
          child._inactive = true;
          child._updateRef(true);
        }
        return;
      }
      // the sole purpose of `deferCleanup` is so that we can
      // "deactivate" the vm right now and perform DOM removal
      // later.
      child.$destroy(false, defer);
    },

    /**
     * Remove current destroyed child and manually do
     * the cleanup after removal.
     *
     * @param {Function} cb
     */

    remove: function remove(child, cb) {
      var keepAlive = this.keepAlive;
      if (child) {
        // we may have a component switch when a previous
        // component is still being transitioned out.
        // we want to trigger only one lastest insertion cb
        // when the existing transition finishes. (#1119)
        this.pendingRemovals++;
        this.pendingRemovalCb = cb;
        var self = this;
        child.$remove(function () {
          self.pendingRemovals--;
          if (!keepAlive) child._cleanup();
          if (!self.pendingRemovals && self.pendingRemovalCb) {
            self.pendingRemovalCb();
            self.pendingRemovalCb = null;
          }
        });
      } else if (cb) {
        cb();
      }
    },

    /**
     * Actually swap the components, depending on the
     * transition mode. Defaults to simultaneous.
     *
     * @param {Vue} target
     * @param {Function} [cb]
     */

    transition: function transition(target, cb) {
      var self = this;
      var current = this.childVM;
      // for devtool inspection
      if (current) current._inactive = true;
      target._inactive = false;
      this.childVM = target;
      switch (self.params.transitionMode) {
        case 'in-out':
          target.$before(self.anchor, function () {
            self.remove(current, cb);
          });
          break;
        case 'out-in':
          self.remove(current, function () {
            target.$before(self.anchor, cb);
          });
          break;
        default:
          self.remove(current);
          target.$before(self.anchor, cb);
      }
    },

    /**
     * Unbind.
     */

    unbind: function unbind() {
      this.invalidatePending();
      // Do not defer cleanup when unbinding
      this.unbuild();
      // destroy all keep-alive cached instances
      if (this.cache) {
        for (var key in this.cache) {
          this.cache[key].$destroy();
        }
        this.cache = null;
      }
    }
  };

  /**
   * Call activate hooks in order (asynchronous)
   *
   * @param {Array} hooks
   * @param {Vue} vm
   * @param {Function} cb
   */

  function callActivateHooks(hooks, vm, cb) {
    var total = hooks.length;
    var called = 0;
    hooks[0].call(vm, next);
    function next() {
      if (++called >= total) {
        cb();
      } else {
        hooks[called].call(vm, next);
      }
    }
  }

  var propBindingModes = config._propBindingModes;
  var empty = {};

  // regexes
  var identRE$1 = /^[$_a-zA-Z]+[\w$]*$/;
  var settablePathRE = /^[A-Za-z_$][\w$]*(\.[A-Za-z_$][\w$]*|\[[^\[\]]+\])*$/;

  /**
   * Compile props on a root element and return
   * a props link function.
   *
   * @param {Element|DocumentFragment} el
   * @param {Array} propOptions
   * @param {Vue} vm
   * @return {Function} propsLinkFn
   */

  function compileProps(el, propOptions, vm) {
    var props = [];
    var names = Object.keys(propOptions);
    var i = names.length;
    var options, name, attr, value, path, parsed, prop;
    while (i--) {
      name = names[i];
      options = propOptions[name] || empty;

      if ('development' !== 'production' && name === '$data') {
        warn('Do not use $data as prop.', vm);
        continue;
      }

      // props could contain dashes, which will be
      // interpreted as minus calculations by the parser
      // so we need to camelize the path here
      path = camelize(name);
      if (!identRE$1.test(path)) {
        'development' !== 'production' && warn('Invalid prop key: "' + name + '". Prop keys ' + 'must be valid identifiers.', vm);
        continue;
      }

      prop = {
        name: name,
        path: path,
        options: options,
        mode: propBindingModes.ONE_WAY,
        raw: null
      };

      attr = hyphenate(name);
      // first check dynamic version
      if ((value = getBindAttr(el, attr)) === null) {
        if ((value = getBindAttr(el, attr + '.sync')) !== null) {
          prop.mode = propBindingModes.TWO_WAY;
        } else if ((value = getBindAttr(el, attr + '.once')) !== null) {
          prop.mode = propBindingModes.ONE_TIME;
        }
      }
      if (value !== null) {
        // has dynamic binding!
        prop.raw = value;
        parsed = parseDirective(value);
        value = parsed.expression;
        prop.filters = parsed.filters;
        // check binding type
        if (isLiteral(value) && !parsed.filters) {
          // for expressions containing literal numbers and
          // booleans, there's no need to setup a prop binding,
          // so we can optimize them as a one-time set.
          prop.optimizedLiteral = true;
        } else {
          prop.dynamic = true;
          // check non-settable path for two-way bindings
          if ('development' !== 'production' && prop.mode === propBindingModes.TWO_WAY && !settablePathRE.test(value)) {
            prop.mode = propBindingModes.ONE_WAY;
            warn('Cannot bind two-way prop with non-settable ' + 'parent path: ' + value, vm);
          }
        }
        prop.parentPath = value;

        // warn required two-way
        if ('development' !== 'production' && options.twoWay && prop.mode !== propBindingModes.TWO_WAY) {
          warn('Prop "' + name + '" expects a two-way binding type.', vm);
        }
      } else if ((value = getAttr(el, attr)) !== null) {
        // has literal binding!
        prop.raw = value;
      } else if ('development' !== 'production') {
        // check possible camelCase prop usage
        var lowerCaseName = path.toLowerCase();
        value = /[A-Z\-]/.test(name) && (el.getAttribute(lowerCaseName) || el.getAttribute(':' + lowerCaseName) || el.getAttribute('v-bind:' + lowerCaseName) || el.getAttribute(':' + lowerCaseName + '.once') || el.getAttribute('v-bind:' + lowerCaseName + '.once') || el.getAttribute(':' + lowerCaseName + '.sync') || el.getAttribute('v-bind:' + lowerCaseName + '.sync'));
        if (value) {
          warn('Possible usage error for prop `' + lowerCaseName + '` - ' + 'did you mean `' + attr + '`? HTML is case-insensitive, remember to use ' + 'kebab-case for props in templates.', vm);
        } else if (options.required) {
          // warn missing required
          warn('Missing required prop: ' + name, vm);
        }
      }
      // push prop
      props.push(prop);
    }
    return makePropsLinkFn(props);
  }

  /**
   * Build a function that applies props to a vm.
   *
   * @param {Array} props
   * @return {Function} propsLinkFn
   */

  function makePropsLinkFn(props) {
    return function propsLinkFn(vm, scope) {
      // store resolved props info
      vm._props = {};
      var inlineProps = vm.$options.propsData;
      var i = props.length;
      var prop, path, options, value, raw;
      while (i--) {
        prop = props[i];
        raw = prop.raw;
        path = prop.path;
        options = prop.options;
        vm._props[path] = prop;
        if (inlineProps && hasOwn(inlineProps, path)) {
          initProp(vm, prop, inlineProps[path]);
        }if (raw === null) {
          // initialize absent prop
          initProp(vm, prop, undefined);
        } else if (prop.dynamic) {
          // dynamic prop
          if (prop.mode === propBindingModes.ONE_TIME) {
            // one time binding
            value = (scope || vm._context || vm).$get(prop.parentPath);
            initProp(vm, prop, value);
          } else {
            if (vm._context) {
              // dynamic binding
              vm._bindDir({
                name: 'prop',
                def: propDef,
                prop: prop
              }, null, null, scope); // el, host, scope
            } else {
                // root instance
                initProp(vm, prop, vm.$get(prop.parentPath));
              }
          }
        } else if (prop.optimizedLiteral) {
          // optimized literal, cast it and just set once
          var stripped = stripQuotes(raw);
          value = stripped === raw ? toBoolean(toNumber(raw)) : stripped;
          initProp(vm, prop, value);
        } else {
          // string literal, but we need to cater for
          // Boolean props with no value, or with same
          // literal value (e.g. disabled="disabled")
          // see https://github.com/vuejs/vue-loader/issues/182
          value = options.type === Boolean && (raw === '' || raw === hyphenate(prop.name)) ? true : raw;
          initProp(vm, prop, value);
        }
      }
    };
  }

  /**
   * Process a prop with a rawValue, applying necessary coersions,
   * default values & assertions and call the given callback with
   * processed value.
   *
   * @param {Vue} vm
   * @param {Object} prop
   * @param {*} rawValue
   * @param {Function} fn
   */

  function processPropValue(vm, prop, rawValue, fn) {
    var isSimple = prop.dynamic && isSimplePath(prop.parentPath);
    var value = rawValue;
    if (value === undefined) {
      value = getPropDefaultValue(vm, prop);
    }
    value = coerceProp(prop, value, vm);
    var coerced = value !== rawValue;
    if (!assertProp(prop, value, vm)) {
      value = undefined;
    }
    if (isSimple && !coerced) {
      withoutConversion(function () {
        fn(value);
      });
    } else {
      fn(value);
    }
  }

  /**
   * Set a prop's initial value on a vm and its data object.
   *
   * @param {Vue} vm
   * @param {Object} prop
   * @param {*} value
   */

  function initProp(vm, prop, value) {
    processPropValue(vm, prop, value, function (value) {
      defineReactive(vm, prop.path, value);
    });
  }

  /**
   * Update a prop's value on a vm.
   *
   * @param {Vue} vm
   * @param {Object} prop
   * @param {*} value
   */

  function updateProp(vm, prop, value) {
    processPropValue(vm, prop, value, function (value) {
      vm[prop.path] = value;
    });
  }

  /**
   * Get the default value of a prop.
   *
   * @param {Vue} vm
   * @param {Object} prop
   * @return {*}
   */

  function getPropDefaultValue(vm, prop) {
    // no default, return undefined
    var options = prop.options;
    if (!hasOwn(options, 'default')) {
      // absent boolean value defaults to false
      return options.type === Boolean ? false : undefined;
    }
    var def = options['default'];
    // warn against non-factory defaults for Object & Array
    if (isObject(def)) {
      'development' !== 'production' && warn('Invalid default value for prop "' + prop.name + '": ' + 'Props with type Object/Array must use a factory function ' + 'to return the default value.', vm);
    }
    // call factory function for non-Function types
    return typeof def === 'function' && options.type !== Function ? def.call(vm) : def;
  }

  /**
   * Assert whether a prop is valid.
   *
   * @param {Object} prop
   * @param {*} value
   * @param {Vue} vm
   */

  function assertProp(prop, value, vm) {
    if (!prop.options.required && ( // non-required
    prop.raw === null || // abscent
    value == null) // null or undefined
    ) {
        return true;
      }
    var options = prop.options;
    var type = options.type;
    var valid = !type;
    var expectedTypes = [];
    if (type) {
      if (!isArray(type)) {
        type = [type];
      }
      for (var i = 0; i < type.length && !valid; i++) {
        var assertedType = assertType(value, type[i]);
        expectedTypes.push(assertedType.expectedType);
        valid = assertedType.valid;
      }
    }
    if (!valid) {
      if ('development' !== 'production') {
        warn('Invalid prop: type check failed for prop "' + prop.name + '".' + ' Expected ' + expectedTypes.map(formatType).join(', ') + ', got ' + formatValue(value) + '.', vm);
      }
      return false;
    }
    var validator = options.validator;
    if (validator) {
      if (!validator(value)) {
        'development' !== 'production' && warn('Invalid prop: custom validator check failed for prop "' + prop.name + '".', vm);
        return false;
      }
    }
    return true;
  }

  /**
   * Force parsing value with coerce option.
   *
   * @param {*} value
   * @param {Object} options
   * @return {*}
   */

  function coerceProp(prop, value, vm) {
    var coerce = prop.options.coerce;
    if (!coerce) {
      return value;
    }
    if (typeof coerce === 'function') {
      return coerce(value);
    } else {
      'development' !== 'production' && warn('Invalid coerce for prop "' + prop.name + '": expected function, got ' + typeof coerce + '.', vm);
      return value;
    }
  }

  /**
   * Assert the type of a value
   *
   * @param {*} value
   * @param {Function} type
   * @return {Object}
   */

  function assertType(value, type) {
    var valid;
    var expectedType;
    if (type === String) {
      expectedType = 'string';
      valid = typeof value === expectedType;
    } else if (type === Number) {
      expectedType = 'number';
      valid = typeof value === expectedType;
    } else if (type === Boolean) {
      expectedType = 'boolean';
      valid = typeof value === expectedType;
    } else if (type === Function) {
      expectedType = 'function';
      valid = typeof value === expectedType;
    } else if (type === Object) {
      expectedType = 'object';
      valid = isPlainObject(value);
    } else if (type === Array) {
      expectedType = 'array';
      valid = isArray(value);
    } else {
      valid = value instanceof type;
    }
    return {
      valid: valid,
      expectedType: expectedType
    };
  }

  /**
   * Format type for output
   *
   * @param {String} type
   * @return {String}
   */

  function formatType(type) {
    return type ? type.charAt(0).toUpperCase() + type.slice(1) : 'custom type';
  }

  /**
   * Format value
   *
   * @param {*} value
   * @return {String}
   */

  function formatValue(val) {
    return Object.prototype.toString.call(val).slice(8, -1);
  }

  var bindingModes = config._propBindingModes;

  var propDef = {

    bind: function bind() {
      var child = this.vm;
      var parent = child._context;
      // passed in from compiler directly
      var prop = this.descriptor.prop;
      var childKey = prop.path;
      var parentKey = prop.parentPath;
      var twoWay = prop.mode === bindingModes.TWO_WAY;

      var parentWatcher = this.parentWatcher = new Watcher(parent, parentKey, function (val) {
        updateProp(child, prop, val);
      }, {
        twoWay: twoWay,
        filters: prop.filters,
        // important: props need to be observed on the
        // v-for scope if present
        scope: this._scope
      });

      // set the child initial value.
      initProp(child, prop, parentWatcher.value);

      // setup two-way binding
      if (twoWay) {
        // important: defer the child watcher creation until
        // the created hook (after data observation)
        var self = this;
        child.$once('pre-hook:created', function () {
          self.childWatcher = new Watcher(child, childKey, function (val) {
            parentWatcher.set(val);
          }, {
            // ensure sync upward before parent sync down.
            // this is necessary in cases e.g. the child
            // mutates a prop array, then replaces it. (#1683)
            sync: true
          });
        });
      }
    },

    unbind: function unbind() {
      this.parentWatcher.teardown();
      if (this.childWatcher) {
        this.childWatcher.teardown();
      }
    }
  };

  var queue$1 = [];
  var queued = false;

  /**
   * Push a job into the queue.
   *
   * @param {Function} job
   */

  function pushJob(job) {
    queue$1.push(job);
    if (!queued) {
      queued = true;
      nextTick(flush);
    }
  }

  /**
   * Flush the queue, and do one forced reflow before
   * triggering transitions.
   */

  function flush() {
    // Force layout
    var f = document.documentElement.offsetHeight;
    for (var i = 0; i < queue$1.length; i++) {
      queue$1[i]();
    }
    queue$1 = [];
    queued = false;
    // dummy return, so js linters don't complain about
    // unused variable f
    return f;
  }

  var TYPE_TRANSITION = 'transition';
  var TYPE_ANIMATION = 'animation';
  var transDurationProp = transitionProp + 'Duration';
  var animDurationProp = animationProp + 'Duration';

  /**
   * If a just-entered element is applied the
   * leave class while its enter transition hasn't started yet,
   * and the transitioned property has the same value for both
   * enter/leave, then the leave transition will be skipped and
   * the transitionend event never fires. This function ensures
   * its callback to be called after a transition has started
   * by waiting for double raf.
   *
   * It falls back to setTimeout on devices that support CSS
   * transitions but not raf (e.g. Android 4.2 browser) - since
   * these environments are usually slow, we are giving it a
   * relatively large timeout.
   */

  var raf = inBrowser && window.requestAnimationFrame;
  var waitForTransitionStart = raf
  /* istanbul ignore next */
  ? function (fn) {
    raf(function () {
      raf(fn);
    });
  } : function (fn) {
    setTimeout(fn, 50);
  };

  /**
   * A Transition object that encapsulates the state and logic
   * of the transition.
   *
   * @param {Element} el
   * @param {String} id
   * @param {Object} hooks
   * @param {Vue} vm
   */
  function Transition(el, id, hooks, vm) {
    this.id = id;
    this.el = el;
    this.enterClass = hooks && hooks.enterClass || id + '-enter';
    this.leaveClass = hooks && hooks.leaveClass || id + '-leave';
    this.hooks = hooks;
    this.vm = vm;
    // async state
    this.pendingCssEvent = this.pendingCssCb = this.cancel = this.pendingJsCb = this.op = this.cb = null;
    this.justEntered = false;
    this.entered = this.left = false;
    this.typeCache = {};
    // check css transition type
    this.type = hooks && hooks.type;
    /* istanbul ignore if */
    if ('development' !== 'production') {
      if (this.type && this.type !== TYPE_TRANSITION && this.type !== TYPE_ANIMATION) {
        warn('invalid CSS transition type for transition="' + this.id + '": ' + this.type, vm);
      }
    }
    // bind
    var self = this;['enterNextTick', 'enterDone', 'leaveNextTick', 'leaveDone'].forEach(function (m) {
      self[m] = bind(self[m], self);
    });
  }

  var p$1 = Transition.prototype;

  /**
   * Start an entering transition.
   *
   * 1. enter transition triggered
   * 2. call beforeEnter hook
   * 3. add enter class
   * 4. insert/show element
   * 5. call enter hook (with possible explicit js callback)
   * 6. reflow
   * 7. based on transition type:
   *    - transition:
   *        remove class now, wait for transitionend,
   *        then done if there's no explicit js callback.
   *    - animation:
   *        wait for animationend, remove class,
   *        then done if there's no explicit js callback.
   *    - no css transition:
   *        done now if there's no explicit js callback.
   * 8. wait for either done or js callback, then call
   *    afterEnter hook.
   *
   * @param {Function} op - insert/show the element
   * @param {Function} [cb]
   */

  p$1.enter = function (op, cb) {
    this.cancelPending();
    this.callHook('beforeEnter');
    this.cb = cb;
    addClass(this.el, this.enterClass);
    op();
    this.entered = false;
    this.callHookWithCb('enter');
    if (this.entered) {
      return; // user called done synchronously.
    }
    this.cancel = this.hooks && this.hooks.enterCancelled;
    pushJob(this.enterNextTick);
  };

  /**
   * The "nextTick" phase of an entering transition, which is
   * to be pushed into a queue and executed after a reflow so
   * that removing the class can trigger a CSS transition.
   */

  p$1.enterNextTick = function () {
    var _this = this;

    // prevent transition skipping
    this.justEntered = true;
    waitForTransitionStart(function () {
      _this.justEntered = false;
    });
    var enterDone = this.enterDone;
    var type = this.getCssTransitionType(this.enterClass);
    if (!this.pendingJsCb) {
      if (type === TYPE_TRANSITION) {
        // trigger transition by removing enter class now
        removeClass(this.el, this.enterClass);
        this.setupCssCb(transitionEndEvent, enterDone);
      } else if (type === TYPE_ANIMATION) {
        this.setupCssCb(animationEndEvent, enterDone);
      } else {
        enterDone();
      }
    } else if (type === TYPE_TRANSITION) {
      removeClass(this.el, this.enterClass);
    }
  };

  /**
   * The "cleanup" phase of an entering transition.
   */

  p$1.enterDone = function () {
    this.entered = true;
    this.cancel = this.pendingJsCb = null;
    removeClass(this.el, this.enterClass);
    this.callHook('afterEnter');
    if (this.cb) this.cb();
  };

  /**
   * Start a leaving transition.
   *
   * 1. leave transition triggered.
   * 2. call beforeLeave hook
   * 3. add leave class (trigger css transition)
   * 4. call leave hook (with possible explicit js callback)
   * 5. reflow if no explicit js callback is provided
   * 6. based on transition type:
   *    - transition or animation:
   *        wait for end event, remove class, then done if
   *        there's no explicit js callback.
   *    - no css transition:
   *        done if there's no explicit js callback.
   * 7. wait for either done or js callback, then call
   *    afterLeave hook.
   *
   * @param {Function} op - remove/hide the element
   * @param {Function} [cb]
   */

  p$1.leave = function (op, cb) {
    this.cancelPending();
    this.callHook('beforeLeave');
    this.op = op;
    this.cb = cb;
    addClass(this.el, this.leaveClass);
    this.left = false;
    this.callHookWithCb('leave');
    if (this.left) {
      return; // user called done synchronously.
    }
    this.cancel = this.hooks && this.hooks.leaveCancelled;
    // only need to handle leaveDone if
    // 1. the transition is already done (synchronously called
    //    by the user, which causes this.op set to null)
    // 2. there's no explicit js callback
    if (this.op && !this.pendingJsCb) {
      // if a CSS transition leaves immediately after enter,
      // the transitionend event never fires. therefore we
      // detect such cases and end the leave immediately.
      if (this.justEntered) {
        this.leaveDone();
      } else {
        pushJob(this.leaveNextTick);
      }
    }
  };

  /**
   * The "nextTick" phase of a leaving transition.
   */

  p$1.leaveNextTick = function () {
    var type = this.getCssTransitionType(this.leaveClass);
    if (type) {
      var event = type === TYPE_TRANSITION ? transitionEndEvent : animationEndEvent;
      this.setupCssCb(event, this.leaveDone);
    } else {
      this.leaveDone();
    }
  };

  /**
   * The "cleanup" phase of a leaving transition.
   */

  p$1.leaveDone = function () {
    this.left = true;
    this.cancel = this.pendingJsCb = null;
    this.op();
    removeClass(this.el, this.leaveClass);
    this.callHook('afterLeave');
    if (this.cb) this.cb();
    this.op = null;
  };

  /**
   * Cancel any pending callbacks from a previously running
   * but not finished transition.
   */

  p$1.cancelPending = function () {
    this.op = this.cb = null;
    var hasPending = false;
    if (this.pendingCssCb) {
      hasPending = true;
      off(this.el, this.pendingCssEvent, this.pendingCssCb);
      this.pendingCssEvent = this.pendingCssCb = null;
    }
    if (this.pendingJsCb) {
      hasPending = true;
      this.pendingJsCb.cancel();
      this.pendingJsCb = null;
    }
    if (hasPending) {
      removeClass(this.el, this.enterClass);
      removeClass(this.el, this.leaveClass);
    }
    if (this.cancel) {
      this.cancel.call(this.vm, this.el);
      this.cancel = null;
    }
  };

  /**
   * Call a user-provided synchronous hook function.
   *
   * @param {String} type
   */

  p$1.callHook = function (type) {
    if (this.hooks && this.hooks[type]) {
      this.hooks[type].call(this.vm, this.el);
    }
  };

  /**
   * Call a user-provided, potentially-async hook function.
   * We check for the length of arguments to see if the hook
   * expects a `done` callback. If true, the transition's end
   * will be determined by when the user calls that callback;
   * otherwise, the end is determined by the CSS transition or
   * animation.
   *
   * @param {String} type
   */

  p$1.callHookWithCb = function (type) {
    var hook = this.hooks && this.hooks[type];
    if (hook) {
      if (hook.length > 1) {
        this.pendingJsCb = cancellable(this[type + 'Done']);
      }
      hook.call(this.vm, this.el, this.pendingJsCb);
    }
  };

  /**
   * Get an element's transition type based on the
   * calculated styles.
   *
   * @param {String} className
   * @return {Number}
   */

  p$1.getCssTransitionType = function (className) {
    /* istanbul ignore if */
    if (!transitionEndEvent ||
    // skip CSS transitions if page is not visible -
    // this solves the issue of transitionend events not
    // firing until the page is visible again.
    // pageVisibility API is supported in IE10+, same as
    // CSS transitions.
    document.hidden ||
    // explicit js-only transition
    this.hooks && this.hooks.css === false ||
    // element is hidden
    isHidden(this.el)) {
      return;
    }
    var type = this.type || this.typeCache[className];
    if (type) return type;
    var inlineStyles = this.el.style;
    var computedStyles = window.getComputedStyle(this.el);
    var transDuration = inlineStyles[transDurationProp] || computedStyles[transDurationProp];
    if (transDuration && transDuration !== '0s') {
      type = TYPE_TRANSITION;
    } else {
      var animDuration = inlineStyles[animDurationProp] || computedStyles[animDurationProp];
      if (animDuration && animDuration !== '0s') {
        type = TYPE_ANIMATION;
      }
    }
    if (type) {
      this.typeCache[className] = type;
    }
    return type;
  };

  /**
   * Setup a CSS transitionend/animationend callback.
   *
   * @param {String} event
   * @param {Function} cb
   */

  p$1.setupCssCb = function (event, cb) {
    this.pendingCssEvent = event;
    var self = this;
    var el = this.el;
    var onEnd = this.pendingCssCb = function (e) {
      if (e.target === el) {
        off(el, event, onEnd);
        self.pendingCssEvent = self.pendingCssCb = null;
        if (!self.pendingJsCb && cb) {
          cb();
        }
      }
    };
    on(el, event, onEnd);
  };

  /**
   * Check if an element is hidden - in that case we can just
   * skip the transition alltogether.
   *
   * @param {Element} el
   * @return {Boolean}
   */

  function isHidden(el) {
    if (/svg$/.test(el.namespaceURI)) {
      // SVG elements do not have offset(Width|Height)
      // so we need to check the client rect
      var rect = el.getBoundingClientRect();
      return !(rect.width || rect.height);
    } else {
      return !(el.offsetWidth || el.offsetHeight || el.getClientRects().length);
    }
  }

  var transition$1 = {

    priority: TRANSITION,

    update: function update(id, oldId) {
      var el = this.el;
      // resolve on owner vm
      var hooks = resolveAsset(this.vm.$options, 'transitions', id);
      id = id || 'v';
      oldId = oldId || 'v';
      el.__v_trans = new Transition(el, id, hooks, this.vm);
      removeClass(el, oldId + '-transition');
      addClass(el, id + '-transition');
    }
  };

  var internalDirectives = {
    style: style,
    'class': vClass,
    component: component,
    prop: propDef,
    transition: transition$1
  };

  // special binding prefixes
  var bindRE = /^v-bind:|^:/;
  var onRE = /^v-on:|^@/;
  var dirAttrRE = /^v-([^:]+)(?:$|:(.*)$)/;
  var modifierRE = /\.[^\.]+/g;
  var transitionRE = /^(v-bind:|:)?transition$/;

  // default directive priority
  var DEFAULT_PRIORITY = 1000;
  var DEFAULT_TERMINAL_PRIORITY = 2000;

  /**
   * Compile a template and return a reusable composite link
   * function, which recursively contains more link functions
   * inside. This top level compile function would normally
   * be called on instance root nodes, but can also be used
   * for partial compilation if the partial argument is true.
   *
   * The returned composite link function, when called, will
   * return an unlink function that tearsdown all directives
   * created during the linking phase.
   *
   * @param {Element|DocumentFragment} el
   * @param {Object} options
   * @param {Boolean} partial
   * @return {Function}
   */

  function compile(el, options, partial) {
    // link function for the node itself.
    var nodeLinkFn = partial || !options._asComponent ? compileNode(el, options) : null;
    // link function for the childNodes
    var childLinkFn = !(nodeLinkFn && nodeLinkFn.terminal) && !isScript(el) && el.hasChildNodes() ? compileNodeList(el.childNodes, options) : null;

    /**
     * A composite linker function to be called on a already
     * compiled piece of DOM, which instantiates all directive
     * instances.
     *
     * @param {Vue} vm
     * @param {Element|DocumentFragment} el
     * @param {Vue} [host] - host vm of transcluded content
     * @param {Object} [scope] - v-for scope
     * @param {Fragment} [frag] - link context fragment
     * @return {Function|undefined}
     */

    return function compositeLinkFn(vm, el, host, scope, frag) {
      // cache childNodes before linking parent, fix #657
      var childNodes = toArray(el.childNodes);
      // link
      var dirs = linkAndCapture(function compositeLinkCapturer() {
        if (nodeLinkFn) nodeLinkFn(vm, el, host, scope, frag);
        if (childLinkFn) childLinkFn(vm, childNodes, host, scope, frag);
      }, vm);
      return makeUnlinkFn(vm, dirs);
    };
  }

  /**
   * Apply a linker to a vm/element pair and capture the
   * directives created during the process.
   *
   * @param {Function} linker
   * @param {Vue} vm
   */

  function linkAndCapture(linker, vm) {
    /* istanbul ignore if */
    if ('development' === 'production') {}
    var originalDirCount = vm._directives.length;
    linker();
    var dirs = vm._directives.slice(originalDirCount);
    dirs.sort(directiveComparator);
    for (var i = 0, l = dirs.length; i < l; i++) {
      dirs[i]._bind();
    }
    return dirs;
  }

  /**
   * Directive priority sort comparator
   *
   * @param {Object} a
   * @param {Object} b
   */

  function directiveComparator(a, b) {
    a = a.descriptor.def.priority || DEFAULT_PRIORITY;
    b = b.descriptor.def.priority || DEFAULT_PRIORITY;
    return a > b ? -1 : a === b ? 0 : 1;
  }

  /**
   * Linker functions return an unlink function that
   * tearsdown all directives instances generated during
   * the process.
   *
   * We create unlink functions with only the necessary
   * information to avoid retaining additional closures.
   *
   * @param {Vue} vm
   * @param {Array} dirs
   * @param {Vue} [context]
   * @param {Array} [contextDirs]
   * @return {Function}
   */

  function makeUnlinkFn(vm, dirs, context, contextDirs) {
    function unlink(destroying) {
      teardownDirs(vm, dirs, destroying);
      if (context && contextDirs) {
        teardownDirs(context, contextDirs);
      }
    }
    // expose linked directives
    unlink.dirs = dirs;
    return unlink;
  }

  /**
   * Teardown partial linked directives.
   *
   * @param {Vue} vm
   * @param {Array} dirs
   * @param {Boolean} destroying
   */

  function teardownDirs(vm, dirs, destroying) {
    var i = dirs.length;
    while (i--) {
      dirs[i]._teardown();
      if ('development' !== 'production' && !destroying) {
        vm._directives.$remove(dirs[i]);
      }
    }
  }

  /**
   * Compile link props on an instance.
   *
   * @param {Vue} vm
   * @param {Element} el
   * @param {Object} props
   * @param {Object} [scope]
   * @return {Function}
   */

  function compileAndLinkProps(vm, el, props, scope) {
    var propsLinkFn = compileProps(el, props, vm);
    var propDirs = linkAndCapture(function () {
      propsLinkFn(vm, scope);
    }, vm);
    return makeUnlinkFn(vm, propDirs);
  }

  /**
   * Compile the root element of an instance.
   *
   * 1. attrs on context container (context scope)
   * 2. attrs on the component template root node, if
   *    replace:true (child scope)
   *
   * If this is a fragment instance, we only need to compile 1.
   *
   * @param {Element} el
   * @param {Object} options
   * @param {Object} contextOptions
   * @return {Function}
   */

  function compileRoot(el, options, contextOptions) {
    var containerAttrs = options._containerAttrs;
    var replacerAttrs = options._replacerAttrs;
    var contextLinkFn, replacerLinkFn;

    // only need to compile other attributes for
    // non-fragment instances
    if (el.nodeType !== 11) {
      // for components, container and replacer need to be
      // compiled separately and linked in different scopes.
      if (options._asComponent) {
        // 2. container attributes
        if (containerAttrs && contextOptions) {
          contextLinkFn = compileDirectives(containerAttrs, contextOptions);
        }
        if (replacerAttrs) {
          // 3. replacer attributes
          replacerLinkFn = compileDirectives(replacerAttrs, options);
        }
      } else {
        // non-component, just compile as a normal element.
        replacerLinkFn = compileDirectives(el.attributes, options);
      }
    } else if ('development' !== 'production' && containerAttrs) {
      // warn container directives for fragment instances
      var names = containerAttrs.filter(function (attr) {
        // allow vue-loader/vueify scoped css attributes
        return attr.name.indexOf('_v-') < 0 &&
        // allow event listeners
        !onRE.test(attr.name) &&
        // allow slots
        attr.name !== 'slot';
      }).map(function (attr) {
        return '"' + attr.name + '"';
      });
      if (names.length) {
        var plural = names.length > 1;
        warn('Attribute' + (plural ? 's ' : ' ') + names.join(', ') + (plural ? ' are' : ' is') + ' ignored on component ' + '<' + options.el.tagName.toLowerCase() + '> because ' + 'the component is a fragment instance: ' + 'http://vuejs.org/guide/components.html#Fragment-Instance');
      }
    }

    options._containerAttrs = options._replacerAttrs = null;
    return function rootLinkFn(vm, el, scope) {
      // link context scope dirs
      var context = vm._context;
      var contextDirs;
      if (context && contextLinkFn) {
        contextDirs = linkAndCapture(function () {
          contextLinkFn(context, el, null, scope);
        }, context);
      }

      // link self
      var selfDirs = linkAndCapture(function () {
        if (replacerLinkFn) replacerLinkFn(vm, el);
      }, vm);

      // return the unlink function that tearsdown context
      // container directives.
      return makeUnlinkFn(vm, selfDirs, context, contextDirs);
    };
  }

  /**
   * Compile a node and return a nodeLinkFn based on the
   * node type.
   *
   * @param {Node} node
   * @param {Object} options
   * @return {Function|null}
   */

  function compileNode(node, options) {
    var type = node.nodeType;
    if (type === 1 && !isScript(node)) {
      return compileElement(node, options);
    } else if (type === 3 && node.data.trim()) {
      return compileTextNode(node, options);
    } else {
      return null;
    }
  }

  /**
   * Compile an element and return a nodeLinkFn.
   *
   * @param {Element} el
   * @param {Object} options
   * @return {Function|null}
   */

  function compileElement(el, options) {
    // preprocess textareas.
    // textarea treats its text content as the initial value.
    // just bind it as an attr directive for value.
    if (el.tagName === 'TEXTAREA') {
      var tokens = parseText(el.value);
      if (tokens) {
        el.setAttribute(':value', tokensToExp(tokens));
        el.value = '';
      }
    }
    var linkFn;
    var hasAttrs = el.hasAttributes();
    var attrs = hasAttrs && toArray(el.attributes);
    // check terminal directives (for & if)
    if (hasAttrs) {
      linkFn = checkTerminalDirectives(el, attrs, options);
    }
    // check element directives
    if (!linkFn) {
      linkFn = checkElementDirectives(el, options);
    }
    // check component
    if (!linkFn) {
      linkFn = checkComponent(el, options);
    }
    // normal directives
    if (!linkFn && hasAttrs) {
      linkFn = compileDirectives(attrs, options);
    }
    return linkFn;
  }

  /**
   * Compile a textNode and return a nodeLinkFn.
   *
   * @param {TextNode} node
   * @param {Object} options
   * @return {Function|null} textNodeLinkFn
   */

  function compileTextNode(node, options) {
    // skip marked text nodes
    if (node._skip) {
      return removeText;
    }

    var tokens = parseText(node.wholeText);
    if (!tokens) {
      return null;
    }

    // mark adjacent text nodes as skipped,
    // because we are using node.wholeText to compile
    // all adjacent text nodes together. This fixes
    // issues in IE where sometimes it splits up a single
    // text node into multiple ones.
    var next = node.nextSibling;
    while (next && next.nodeType === 3) {
      next._skip = true;
      next = next.nextSibling;
    }

    var frag = document.createDocumentFragment();
    var el, token;
    for (var i = 0, l = tokens.length; i < l; i++) {
      token = tokens[i];
      el = token.tag ? processTextToken(token, options) : document.createTextNode(token.value);
      frag.appendChild(el);
    }
    return makeTextNodeLinkFn(tokens, frag, options);
  }

  /**
   * Linker for an skipped text node.
   *
   * @param {Vue} vm
   * @param {Text} node
   */

  function removeText(vm, node) {
    remove(node);
  }

  /**
   * Process a single text token.
   *
   * @param {Object} token
   * @param {Object} options
   * @return {Node}
   */

  function processTextToken(token, options) {
    var el;
    if (token.oneTime) {
      el = document.createTextNode(token.value);
    } else {
      if (token.html) {
        el = document.createComment('v-html');
        setTokenType('html');
      } else {
        // IE will clean up empty textNodes during
        // frag.cloneNode(true), so we have to give it
        // something here...
        el = document.createTextNode(' ');
        setTokenType('text');
      }
    }
    function setTokenType(type) {
      if (token.descriptor) return;
      var parsed = parseDirective(token.value);
      token.descriptor = {
        name: type,
        def: directives[type],
        expression: parsed.expression,
        filters: parsed.filters
      };
    }
    return el;
  }

  /**
   * Build a function that processes a textNode.
   *
   * @param {Array<Object>} tokens
   * @param {DocumentFragment} frag
   */

  function makeTextNodeLinkFn(tokens, frag) {
    return function textNodeLinkFn(vm, el, host, scope) {
      var fragClone = frag.cloneNode(true);
      var childNodes = toArray(fragClone.childNodes);
      var token, value, node;
      for (var i = 0, l = tokens.length; i < l; i++) {
        token = tokens[i];
        value = token.value;
        if (token.tag) {
          node = childNodes[i];
          if (token.oneTime) {
            value = (scope || vm).$eval(value);
            if (token.html) {
              replace(node, parseTemplate(value, true));
            } else {
              node.data = _toString(value);
            }
          } else {
            vm._bindDir(token.descriptor, node, host, scope);
          }
        }
      }
      replace(el, fragClone);
    };
  }

  /**
   * Compile a node list and return a childLinkFn.
   *
   * @param {NodeList} nodeList
   * @param {Object} options
   * @return {Function|undefined}
   */

  function compileNodeList(nodeList, options) {
    var linkFns = [];
    var nodeLinkFn, childLinkFn, node;
    for (var i = 0, l = nodeList.length; i < l; i++) {
      node = nodeList[i];
      nodeLinkFn = compileNode(node, options);
      childLinkFn = !(nodeLinkFn && nodeLinkFn.terminal) && node.tagName !== 'SCRIPT' && node.hasChildNodes() ? compileNodeList(node.childNodes, options) : null;
      linkFns.push(nodeLinkFn, childLinkFn);
    }
    return linkFns.length ? makeChildLinkFn(linkFns) : null;
  }

  /**
   * Make a child link function for a node's childNodes.
   *
   * @param {Array<Function>} linkFns
   * @return {Function} childLinkFn
   */

  function makeChildLinkFn(linkFns) {
    return function childLinkFn(vm, nodes, host, scope, frag) {
      var node, nodeLinkFn, childrenLinkFn;
      for (var i = 0, n = 0, l = linkFns.length; i < l; n++) {
        node = nodes[n];
        nodeLinkFn = linkFns[i++];
        childrenLinkFn = linkFns[i++];
        // cache childNodes before linking parent, fix #657
        var childNodes = toArray(node.childNodes);
        if (nodeLinkFn) {
          nodeLinkFn(vm, node, host, scope, frag);
        }
        if (childrenLinkFn) {
          childrenLinkFn(vm, childNodes, host, scope, frag);
        }
      }
    };
  }

  /**
   * Check for element directives (custom elements that should
   * be resovled as terminal directives).
   *
   * @param {Element} el
   * @param {Object} options
   */

  function checkElementDirectives(el, options) {
    var tag = el.tagName.toLowerCase();
    if (commonTagRE.test(tag)) {
      return;
    }
    var def = resolveAsset(options, 'elementDirectives', tag);
    if (def) {
      return makeTerminalNodeLinkFn(el, tag, '', options, def);
    }
  }

  /**
   * Check if an element is a component. If yes, return
   * a component link function.
   *
   * @param {Element} el
   * @param {Object} options
   * @return {Function|undefined}
   */

  function checkComponent(el, options) {
    var component = checkComponentAttr(el, options);
    if (component) {
      var ref = findRef(el);
      var descriptor = {
        name: 'component',
        ref: ref,
        expression: component.id,
        def: internalDirectives.component,
        modifiers: {
          literal: !component.dynamic
        }
      };
      var componentLinkFn = function componentLinkFn(vm, el, host, scope, frag) {
        if (ref) {
          defineReactive((scope || vm).$refs, ref, null);
        }
        vm._bindDir(descriptor, el, host, scope, frag);
      };
      componentLinkFn.terminal = true;
      return componentLinkFn;
    }
  }

  /**
   * Check an element for terminal directives in fixed order.
   * If it finds one, return a terminal link function.
   *
   * @param {Element} el
   * @param {Array} attrs
   * @param {Object} options
   * @return {Function} terminalLinkFn
   */

  function checkTerminalDirectives(el, attrs, options) {
    // skip v-pre
    if (getAttr(el, 'v-pre') !== null) {
      return skip;
    }
    // skip v-else block, but only if following v-if
    if (el.hasAttribute('v-else')) {
      var prev = el.previousElementSibling;
      if (prev && prev.hasAttribute('v-if')) {
        return skip;
      }
    }

    var attr, name, value, modifiers, matched, dirName, rawName, arg, def, termDef;
    for (var i = 0, j = attrs.length; i < j; i++) {
      attr = attrs[i];
      name = attr.name.replace(modifierRE, '');
      if (matched = name.match(dirAttrRE)) {
        def = resolveAsset(options, 'directives', matched[1]);
        if (def && def.terminal) {
          if (!termDef || (def.priority || DEFAULT_TERMINAL_PRIORITY) > termDef.priority) {
            termDef = def;
            rawName = attr.name;
            modifiers = parseModifiers(attr.name);
            value = attr.value;
            dirName = matched[1];
            arg = matched[2];
          }
        }
      }
    }

    if (termDef) {
      return makeTerminalNodeLinkFn(el, dirName, value, options, termDef, rawName, arg, modifiers);
    }
  }

  function skip() {}
  skip.terminal = true;

  /**
   * Build a node link function for a terminal directive.
   * A terminal link function terminates the current
   * compilation recursion and handles compilation of the
   * subtree in the directive.
   *
   * @param {Element} el
   * @param {String} dirName
   * @param {String} value
   * @param {Object} options
   * @param {Object} def
   * @param {String} [rawName]
   * @param {String} [arg]
   * @param {Object} [modifiers]
   * @return {Function} terminalLinkFn
   */

  function makeTerminalNodeLinkFn(el, dirName, value, options, def, rawName, arg, modifiers) {
    var parsed = parseDirective(value);
    var descriptor = {
      name: dirName,
      arg: arg,
      expression: parsed.expression,
      filters: parsed.filters,
      raw: value,
      attr: rawName,
      modifiers: modifiers,
      def: def
    };
    // check ref for v-for and router-view
    if (dirName === 'for' || dirName === 'router-view') {
      descriptor.ref = findRef(el);
    }
    var fn = function terminalNodeLinkFn(vm, el, host, scope, frag) {
      if (descriptor.ref) {
        defineReactive((scope || vm).$refs, descriptor.ref, null);
      }
      vm._bindDir(descriptor, el, host, scope, frag);
    };
    fn.terminal = true;
    return fn;
  }

  /**
   * Compile the directives on an element and return a linker.
   *
   * @param {Array|NamedNodeMap} attrs
   * @param {Object} options
   * @return {Function}
   */

  function compileDirectives(attrs, options) {
    var i = attrs.length;
    var dirs = [];
    var attr, name, value, rawName, rawValue, dirName, arg, modifiers, dirDef, tokens, matched;
    while (i--) {
      attr = attrs[i];
      name = rawName = attr.name;
      value = rawValue = attr.value;
      tokens = parseText(value);
      // reset arg
      arg = null;
      // check modifiers
      modifiers = parseModifiers(name);
      name = name.replace(modifierRE, '');

      // attribute interpolations
      if (tokens) {
        value = tokensToExp(tokens);
        arg = name;
        pushDir('bind', directives.bind, tokens);
        // warn against mixing mustaches with v-bind
        if ('development' !== 'production') {
          if (name === 'class' && Array.prototype.some.call(attrs, function (attr) {
            return attr.name === ':class' || attr.name === 'v-bind:class';
          })) {
            warn('class="' + rawValue + '": Do not mix mustache interpolation ' + 'and v-bind for "class" on the same element. Use one or the other.', options);
          }
        }
      } else

        // special attribute: transition
        if (transitionRE.test(name)) {
          modifiers.literal = !bindRE.test(name);
          pushDir('transition', internalDirectives.transition);
        } else

          // event handlers
          if (onRE.test(name)) {
            arg = name.replace(onRE, '');
            pushDir('on', directives.on);
          } else

            // attribute bindings
            if (bindRE.test(name)) {
              dirName = name.replace(bindRE, '');
              if (dirName === 'style' || dirName === 'class') {
                pushDir(dirName, internalDirectives[dirName]);
              } else {
                arg = dirName;
                pushDir('bind', directives.bind);
              }
            } else

              // normal directives
              if (matched = name.match(dirAttrRE)) {
                dirName = matched[1];
                arg = matched[2];

                // skip v-else (when used with v-show)
                if (dirName === 'else') {
                  continue;
                }

                dirDef = resolveAsset(options, 'directives', dirName, true);
                if (dirDef) {
                  pushDir(dirName, dirDef);
                }
              }
    }

    /**
     * Push a directive.
     *
     * @param {String} dirName
     * @param {Object|Function} def
     * @param {Array} [interpTokens]
     */

    function pushDir(dirName, def, interpTokens) {
      var hasOneTimeToken = interpTokens && hasOneTime(interpTokens);
      var parsed = !hasOneTimeToken && parseDirective(value);
      dirs.push({
        name: dirName,
        attr: rawName,
        raw: rawValue,
        def: def,
        arg: arg,
        modifiers: modifiers,
        // conversion from interpolation strings with one-time token
        // to expression is differed until directive bind time so that we
        // have access to the actual vm context for one-time bindings.
        expression: parsed && parsed.expression,
        filters: parsed && parsed.filters,
        interp: interpTokens,
        hasOneTime: hasOneTimeToken
      });
    }

    if (dirs.length) {
      return makeNodeLinkFn(dirs);
    }
  }

  /**
   * Parse modifiers from directive attribute name.
   *
   * @param {String} name
   * @return {Object}
   */

  function parseModifiers(name) {
    var res = Object.create(null);
    var match = name.match(modifierRE);
    if (match) {
      var i = match.length;
      while (i--) {
        res[match[i].slice(1)] = true;
      }
    }
    return res;
  }

  /**
   * Build a link function for all directives on a single node.
   *
   * @param {Array} directives
   * @return {Function} directivesLinkFn
   */

  function makeNodeLinkFn(directives) {
    return function nodeLinkFn(vm, el, host, scope, frag) {
      // reverse apply because it's sorted low to high
      var i = directives.length;
      while (i--) {
        vm._bindDir(directives[i], el, host, scope, frag);
      }
    };
  }

  /**
   * Check if an interpolation string contains one-time tokens.
   *
   * @param {Array} tokens
   * @return {Boolean}
   */

  function hasOneTime(tokens) {
    var i = tokens.length;
    while (i--) {
      if (tokens[i].oneTime) return true;
    }
  }

  function isScript(el) {
    return el.tagName === 'SCRIPT' && (!el.hasAttribute('type') || el.getAttribute('type') === 'text/javascript');
  }

  var specialCharRE = /[^\w\-:\.]/;

  /**
   * Process an element or a DocumentFragment based on a
   * instance option object. This allows us to transclude
   * a template node/fragment before the instance is created,
   * so the processed fragment can then be cloned and reused
   * in v-for.
   *
   * @param {Element} el
   * @param {Object} options
   * @return {Element|DocumentFragment}
   */

  function transclude(el, options) {
    // extract container attributes to pass them down
    // to compiler, because they need to be compiled in
    // parent scope. we are mutating the options object here
    // assuming the same object will be used for compile
    // right after this.
    if (options) {
      options._containerAttrs = extractAttrs(el);
    }
    // for template tags, what we want is its content as
    // a documentFragment (for fragment instances)
    if (isTemplate(el)) {
      el = parseTemplate(el);
    }
    if (options) {
      if (options._asComponent && !options.template) {
        options.template = '<slot></slot>';
      }
      if (options.template) {
        options._content = extractContent(el);
        el = transcludeTemplate(el, options);
      }
    }
    if (isFragment(el)) {
      // anchors for fragment instance
      // passing in `persist: true` to avoid them being
      // discarded by IE during template cloning
      prepend(createAnchor('v-start', true), el);
      el.appendChild(createAnchor('v-end', true));
    }
    return el;
  }

  /**
   * Process the template option.
   * If the replace option is true this will swap the $el.
   *
   * @param {Element} el
   * @param {Object} options
   * @return {Element|DocumentFragment}
   */

  function transcludeTemplate(el, options) {
    var template = options.template;
    var frag = parseTemplate(template, true);
    if (frag) {
      var replacer = frag.firstChild;
      var tag = replacer.tagName && replacer.tagName.toLowerCase();
      if (options.replace) {
        /* istanbul ignore if */
        if (el === document.body) {
          'development' !== 'production' && warn('You are mounting an instance with a template to ' + '<body>. This will replace <body> entirely. You ' + 'should probably use `replace: false` here.');
        }
        // there are many cases where the instance must
        // become a fragment instance: basically anything that
        // can create more than 1 root nodes.
        if (
        // multi-children template
        frag.childNodes.length > 1 ||
        // non-element template
        replacer.nodeType !== 1 ||
        // single nested component
        tag === 'component' || resolveAsset(options, 'components', tag) || hasBindAttr(replacer, 'is') ||
        // element directive
        resolveAsset(options, 'elementDirectives', tag) ||
        // for block
        replacer.hasAttribute('v-for') ||
        // if block
        replacer.hasAttribute('v-if')) {
          return frag;
        } else {
          options._replacerAttrs = extractAttrs(replacer);
          mergeAttrs(el, replacer);
          return replacer;
        }
      } else {
        el.appendChild(frag);
        return el;
      }
    } else {
      'development' !== 'production' && warn('Invalid template option: ' + template);
    }
  }

  /**
   * Helper to extract a component container's attributes
   * into a plain object array.
   *
   * @param {Element} el
   * @return {Array}
   */

  function extractAttrs(el) {
    if (el.nodeType === 1 && el.hasAttributes()) {
      return toArray(el.attributes);
    }
  }

  /**
   * Merge the attributes of two elements, and make sure
   * the class names are merged properly.
   *
   * @param {Element} from
   * @param {Element} to
   */

  function mergeAttrs(from, to) {
    var attrs = from.attributes;
    var i = attrs.length;
    var name, value;
    while (i--) {
      name = attrs[i].name;
      value = attrs[i].value;
      if (!to.hasAttribute(name) && !specialCharRE.test(name)) {
        to.setAttribute(name, value);
      } else if (name === 'class' && !parseText(value) && (value = value.trim())) {
        value.split(/\s+/).forEach(function (cls) {
          addClass(to, cls);
        });
      }
    }
  }

  /**
   * Scan and determine slot content distribution.
   * We do this during transclusion instead at compile time so that
   * the distribution is decoupled from the compilation order of
   * the slots.
   *
   * @param {Element|DocumentFragment} template
   * @param {Element} content
   * @param {Vue} vm
   */

  function resolveSlots(vm, content) {
    if (!content) {
      return;
    }
    var contents = vm._slotContents = Object.create(null);
    var el, name;
    for (var i = 0, l = content.children.length; i < l; i++) {
      el = content.children[i];
      /* eslint-disable no-cond-assign */
      if (name = el.getAttribute('slot')) {
        (contents[name] || (contents[name] = [])).push(el);
      }
      /* eslint-enable no-cond-assign */
      if ('development' !== 'production' && getBindAttr(el, 'slot')) {
        warn('The "slot" attribute must be static.', vm.$parent);
      }
    }
    for (name in contents) {
      contents[name] = extractFragment(contents[name], content);
    }
    if (content.hasChildNodes()) {
      var nodes = content.childNodes;
      if (nodes.length === 1 && nodes[0].nodeType === 3 && !nodes[0].data.trim()) {
        return;
      }
      contents['default'] = extractFragment(content.childNodes, content);
    }
  }

  /**
   * Extract qualified content nodes from a node list.
   *
   * @param {NodeList} nodes
   * @return {DocumentFragment}
   */

  function extractFragment(nodes, parent) {
    var frag = document.createDocumentFragment();
    nodes = toArray(nodes);
    for (var i = 0, l = nodes.length; i < l; i++) {
      var node = nodes[i];
      if (isTemplate(node) && !node.hasAttribute('v-if') && !node.hasAttribute('v-for')) {
        parent.removeChild(node);
        node = parseTemplate(node, true);
      }
      frag.appendChild(node);
    }
    return frag;
  }



  var compiler = Object.freeze({
  	compile: compile,
  	compileAndLinkProps: compileAndLinkProps,
  	compileRoot: compileRoot,
  	transclude: transclude,
  	resolveSlots: resolveSlots
  });

  function stateMixin (Vue) {
    /**
     * Accessor for `$data` property, since setting $data
     * requires observing the new object and updating
     * proxied properties.
     */

    Object.defineProperty(Vue.prototype, '$data', {
      get: function get() {
        return this._data;
      },
      set: function set(newData) {
        if (newData !== this._data) {
          this._setData(newData);
        }
      }
    });

    /**
     * Setup the scope of an instance, which contains:
     * - observed data
     * - computed properties
     * - user methods
     * - meta properties
     */

    Vue.prototype._initState = function () {
      this._initProps();
      this._initMeta();
      this._initMethods();
      this._initData();
      this._initComputed();
    };

    /**
     * Initialize props.
     */

    Vue.prototype._initProps = function () {
      var options = this.$options;
      var el = options.el;
      var props = options.props;
      if (props && !el) {
        'development' !== 'production' && warn('Props will not be compiled if no `el` option is ' + 'provided at instantiation.', this);
      }
      // make sure to convert string selectors into element now
      el = options.el = query(el);
      this._propsUnlinkFn = el && el.nodeType === 1 && props
      // props must be linked in proper scope if inside v-for
      ? compileAndLinkProps(this, el, props, this._scope) : null;
    };

    /**
     * Initialize the data.
     */

    Vue.prototype._initData = function () {
      var dataFn = this.$options.data;
      var data = this._data = dataFn ? dataFn() : {};
      if (!isPlainObject(data)) {
        data = {};
        'development' !== 'production' && warn('data functions should return an object.', this);
      }
      var props = this._props;
      // proxy data on instance
      var keys = Object.keys(data);
      var i, key;
      i = keys.length;
      while (i--) {
        key = keys[i];
        // there are two scenarios where we can proxy a data key:
        // 1. it's not already defined as a prop
        // 2. it's provided via a instantiation option AND there are no
        //    template prop present
        if (!props || !hasOwn(props, key)) {
          this._proxy(key);
        } else if ('development' !== 'production') {
          warn('Data field "' + key + '" is already defined ' + 'as a prop. To provide default value for a prop, use the "default" ' + 'prop option; if you want to pass prop values to an instantiation ' + 'call, use the "propsData" option.', this);
        }
      }
      // observe data
      observe(data, this);
    };

    /**
     * Swap the instance's $data. Called in $data's setter.
     *
     * @param {Object} newData
     */

    Vue.prototype._setData = function (newData) {
      newData = newData || {};
      var oldData = this._data;
      this._data = newData;
      var keys, key, i;
      // unproxy keys not present in new data
      keys = Object.keys(oldData);
      i = keys.length;
      while (i--) {
        key = keys[i];
        if (!(key in newData)) {
          this._unproxy(key);
        }
      }
      // proxy keys not already proxied,
      // and trigger change for changed values
      keys = Object.keys(newData);
      i = keys.length;
      while (i--) {
        key = keys[i];
        if (!hasOwn(this, key)) {
          // new property
          this._proxy(key);
        }
      }
      oldData.__ob__.removeVm(this);
      observe(newData, this);
      this._digest();
    };

    /**
     * Proxy a property, so that
     * vm.prop === vm._data.prop
     *
     * @param {String} key
     */

    Vue.prototype._proxy = function (key) {
      if (!isReserved(key)) {
        // need to store ref to self here
        // because these getter/setters might
        // be called by child scopes via
        // prototype inheritance.
        var self = this;
        Object.defineProperty(self, key, {
          configurable: true,
          enumerable: true,
          get: function proxyGetter() {
            return self._data[key];
          },
          set: function proxySetter(val) {
            self._data[key] = val;
          }
        });
      }
    };

    /**
     * Unproxy a property.
     *
     * @param {String} key
     */

    Vue.prototype._unproxy = function (key) {
      if (!isReserved(key)) {
        delete this[key];
      }
    };

    /**
     * Force update on every watcher in scope.
     */

    Vue.prototype._digest = function () {
      for (var i = 0, l = this._watchers.length; i < l; i++) {
        this._watchers[i].update(true); // shallow updates
      }
    };

    /**
     * Setup computed properties. They are essentially
     * special getter/setters
     */

    function noop() {}
    Vue.prototype._initComputed = function () {
      var computed = this.$options.computed;
      if (computed) {
        for (var key in computed) {
          var userDef = computed[key];
          var def = {
            enumerable: true,
            configurable: true
          };
          if (typeof userDef === 'function') {
            def.get = makeComputedGetter(userDef, this);
            def.set = noop;
          } else {
            def.get = userDef.get ? userDef.cache !== false ? makeComputedGetter(userDef.get, this) : bind(userDef.get, this) : noop;
            def.set = userDef.set ? bind(userDef.set, this) : noop;
          }
          Object.defineProperty(this, key, def);
        }
      }
    };

    function makeComputedGetter(getter, owner) {
      var watcher = new Watcher(owner, getter, null, {
        lazy: true
      });
      return function computedGetter() {
        if (watcher.dirty) {
          watcher.evaluate();
        }
        if (Dep.target) {
          watcher.depend();
        }
        return watcher.value;
      };
    }

    /**
     * Setup instance methods. Methods must be bound to the
     * instance since they might be passed down as a prop to
     * child components.
     */

    Vue.prototype._initMethods = function () {
      var methods = this.$options.methods;
      if (methods) {
        for (var key in methods) {
          this[key] = bind(methods[key], this);
        }
      }
    };

    /**
     * Initialize meta information like $index, $key & $value.
     */

    Vue.prototype._initMeta = function () {
      var metas = this.$options._meta;
      if (metas) {
        for (var key in metas) {
          defineReactive(this, key, metas[key]);
        }
      }
    };
  }

  var eventRE = /^v-on:|^@/;

  function eventsMixin (Vue) {
    /**
     * Setup the instance's option events & watchers.
     * If the value is a string, we pull it from the
     * instance's methods by name.
     */

    Vue.prototype._initEvents = function () {
      var options = this.$options;
      if (options._asComponent) {
        registerComponentEvents(this, options.el);
      }
      registerCallbacks(this, '$on', options.events);
      registerCallbacks(this, '$watch', options.watch);
    };

    /**
     * Register v-on events on a child component
     *
     * @param {Vue} vm
     * @param {Element} el
     */

    function registerComponentEvents(vm, el) {
      var attrs = el.attributes;
      var name, value, handler;
      for (var i = 0, l = attrs.length; i < l; i++) {
        name = attrs[i].name;
        if (eventRE.test(name)) {
          name = name.replace(eventRE, '');
          // force the expression into a statement so that
          // it always dynamically resolves the method to call (#2670)
          // kinda ugly hack, but does the job.
          value = attrs[i].value;
          if (isSimplePath(value)) {
            value += '.apply(this, $arguments)';
          }
          handler = (vm._scope || vm._context).$eval(value, true);
          handler._fromParent = true;
          vm.$on(name.replace(eventRE), handler);
        }
      }
    }

    /**
     * Register callbacks for option events and watchers.
     *
     * @param {Vue} vm
     * @param {String} action
     * @param {Object} hash
     */

    function registerCallbacks(vm, action, hash) {
      if (!hash) return;
      var handlers, key, i, j;
      for (key in hash) {
        handlers = hash[key];
        if (isArray(handlers)) {
          for (i = 0, j = handlers.length; i < j; i++) {
            register(vm, action, key, handlers[i]);
          }
        } else {
          register(vm, action, key, handlers);
        }
      }
    }

    /**
     * Helper to register an event/watch callback.
     *
     * @param {Vue} vm
     * @param {String} action
     * @param {String} key
     * @param {Function|String|Object} handler
     * @param {Object} [options]
     */

    function register(vm, action, key, handler, options) {
      var type = typeof handler;
      if (type === 'function') {
        vm[action](key, handler, options);
      } else if (type === 'string') {
        var methods = vm.$options.methods;
        var method = methods && methods[handler];
        if (method) {
          vm[action](key, method, options);
        } else {
          'development' !== 'production' && warn('Unknown method: "' + handler + '" when ' + 'registering callback for ' + action + ': "' + key + '".', vm);
        }
      } else if (handler && type === 'object') {
        register(vm, action, key, handler.handler, handler);
      }
    }

    /**
     * Setup recursive attached/detached calls
     */

    Vue.prototype._initDOMHooks = function () {
      this.$on('hook:attached', onAttached);
      this.$on('hook:detached', onDetached);
    };

    /**
     * Callback to recursively call attached hook on children
     */

    function onAttached() {
      if (!this._isAttached) {
        this._isAttached = true;
        this.$children.forEach(callAttach);
      }
    }

    /**
     * Iterator to call attached hook
     *
     * @param {Vue} child
     */

    function callAttach(child) {
      if (!child._isAttached && inDoc(child.$el)) {
        child._callHook('attached');
      }
    }

    /**
     * Callback to recursively call detached hook on children
     */

    function onDetached() {
      if (this._isAttached) {
        this._isAttached = false;
        this.$children.forEach(callDetach);
      }
    }

    /**
     * Iterator to call detached hook
     *
     * @param {Vue} child
     */

    function callDetach(child) {
      if (child._isAttached && !inDoc(child.$el)) {
        child._callHook('detached');
      }
    }

    /**
     * Trigger all handlers for a hook
     *
     * @param {String} hook
     */

    Vue.prototype._callHook = function (hook) {
      this.$emit('pre-hook:' + hook);
      var handlers = this.$options[hook];
      if (handlers) {
        for (var i = 0, j = handlers.length; i < j; i++) {
          handlers[i].call(this);
        }
      }
      this.$emit('hook:' + hook);
    };
  }

  function noop$1() {}

  /**
   * A directive links a DOM element with a piece of data,
   * which is the result of evaluating an expression.
   * It registers a watcher with the expression and calls
   * the DOM update function when a change is triggered.
   *
   * @param {Object} descriptor
   *                 - {String} name
   *                 - {Object} def
   *                 - {String} expression
   *                 - {Array<Object>} [filters]
   *                 - {Object} [modifiers]
   *                 - {Boolean} literal
   *                 - {String} attr
   *                 - {String} arg
   *                 - {String} raw
   *                 - {String} [ref]
   *                 - {Array<Object>} [interp]
   *                 - {Boolean} [hasOneTime]
   * @param {Vue} vm
   * @param {Node} el
   * @param {Vue} [host] - transclusion host component
   * @param {Object} [scope] - v-for scope
   * @param {Fragment} [frag] - owner fragment
   * @constructor
   */
  function Directive(descriptor, vm, el, host, scope, frag) {
    this.vm = vm;
    this.el = el;
    // copy descriptor properties
    this.descriptor = descriptor;
    this.name = descriptor.name;
    this.expression = descriptor.expression;
    this.arg = descriptor.arg;
    this.modifiers = descriptor.modifiers;
    this.filters = descriptor.filters;
    this.literal = this.modifiers && this.modifiers.literal;
    // private
    this._locked = false;
    this._bound = false;
    this._listeners = null;
    // link context
    this._host = host;
    this._scope = scope;
    this._frag = frag;
    // store directives on node in dev mode
    if ('development' !== 'production' && this.el) {
      this.el._vue_directives = this.el._vue_directives || [];
      this.el._vue_directives.push(this);
    }
  }

  /**
   * Initialize the directive, mixin definition properties,
   * setup the watcher, call definition bind() and update()
   * if present.
   */

  Directive.prototype._bind = function () {
    var name = this.name;
    var descriptor = this.descriptor;

    // remove attribute
    if ((name !== 'cloak' || this.vm._isCompiled) && this.el && this.el.removeAttribute) {
      var attr = descriptor.attr || 'v-' + name;
      this.el.removeAttribute(attr);
    }

    // copy def properties
    var def = descriptor.def;
    if (typeof def === 'function') {
      this.update = def;
    } else {
      extend(this, def);
    }

    // setup directive params
    this._setupParams();

    // initial bind
    if (this.bind) {
      this.bind();
    }
    this._bound = true;

    if (this.literal) {
      this.update && this.update(descriptor.raw);
    } else if ((this.expression || this.modifiers) && (this.update || this.twoWay) && !this._checkStatement()) {
      // wrapped updater for context
      var dir = this;
      if (this.update) {
        this._update = function (val, oldVal) {
          if (!dir._locked) {
            dir.update(val, oldVal);
          }
        };
      } else {
        this._update = noop$1;
      }
      var preProcess = this._preProcess ? bind(this._preProcess, this) : null;
      var postProcess = this._postProcess ? bind(this._postProcess, this) : null;
      var watcher = this._watcher = new Watcher(this.vm, this.expression, this._update, // callback
      {
        filters: this.filters,
        twoWay: this.twoWay,
        deep: this.deep,
        preProcess: preProcess,
        postProcess: postProcess,
        scope: this._scope
      });
      // v-model with inital inline value need to sync back to
      // model instead of update to DOM on init. They would
      // set the afterBind hook to indicate that.
      if (this.afterBind) {
        this.afterBind();
      } else if (this.update) {
        this.update(watcher.value);
      }
    }
  };

  /**
   * Setup all param attributes, e.g. track-by,
   * transition-mode, etc...
   */

  Directive.prototype._setupParams = function () {
    if (!this.params) {
      return;
    }
    var params = this.params;
    // swap the params array with a fresh object.
    this.params = Object.create(null);
    var i = params.length;
    var key, val, mappedKey;
    while (i--) {
      key = hyphenate(params[i]);
      mappedKey = camelize(key);
      val = getBindAttr(this.el, key);
      if (val != null) {
        // dynamic
        this._setupParamWatcher(mappedKey, val);
      } else {
        // static
        val = getAttr(this.el, key);
        if (val != null) {
          this.params[mappedKey] = val === '' ? true : val;
        }
      }
    }
  };

  /**
   * Setup a watcher for a dynamic param.
   *
   * @param {String} key
   * @param {String} expression
   */

  Directive.prototype._setupParamWatcher = function (key, expression) {
    var self = this;
    var called = false;
    var unwatch = (this._scope || this.vm).$watch(expression, function (val, oldVal) {
      self.params[key] = val;
      // since we are in immediate mode,
      // only call the param change callbacks if this is not the first update.
      if (called) {
        var cb = self.paramWatchers && self.paramWatchers[key];
        if (cb) {
          cb.call(self, val, oldVal);
        }
      } else {
        called = true;
      }
    }, {
      immediate: true,
      user: false
    });(this._paramUnwatchFns || (this._paramUnwatchFns = [])).push(unwatch);
  };

  /**
   * Check if the directive is a function caller
   * and if the expression is a callable one. If both true,
   * we wrap up the expression and use it as the event
   * handler.
   *
   * e.g. on-click="a++"
   *
   * @return {Boolean}
   */

  Directive.prototype._checkStatement = function () {
    var expression = this.expression;
    if (expression && this.acceptStatement && !isSimplePath(expression)) {
      var fn = parseExpression(expression).get;
      var scope = this._scope || this.vm;
      var handler = function handler(e) {
        scope.$event = e;
        fn.call(scope, scope);
        scope.$event = null;
      };
      if (this.filters) {
        handler = scope._applyFilters(handler, null, this.filters);
      }
      this.update(handler);
      return true;
    }
  };

  /**
   * Set the corresponding value with the setter.
   * This should only be used in two-way directives
   * e.g. v-model.
   *
   * @param {*} value
   * @public
   */

  Directive.prototype.set = function (value) {
    /* istanbul ignore else */
    if (this.twoWay) {
      this._withLock(function () {
        this._watcher.set(value);
      });
    } else if ('development' !== 'production') {
      warn('Directive.set() can only be used inside twoWay' + 'directives.');
    }
  };

  /**
   * Execute a function while preventing that function from
   * triggering updates on this directive instance.
   *
   * @param {Function} fn
   */

  Directive.prototype._withLock = function (fn) {
    var self = this;
    self._locked = true;
    fn.call(self);
    nextTick(function () {
      self._locked = false;
    });
  };

  /**
   * Convenience method that attaches a DOM event listener
   * to the directive element and autometically tears it down
   * during unbind.
   *
   * @param {String} event
   * @param {Function} handler
   * @param {Boolean} [useCapture]
   */

  Directive.prototype.on = function (event, handler, useCapture) {
    on(this.el, event, handler, useCapture);(this._listeners || (this._listeners = [])).push([event, handler]);
  };

  /**
   * Teardown the watcher and call unbind.
   */

  Directive.prototype._teardown = function () {
    if (this._bound) {
      this._bound = false;
      if (this.unbind) {
        this.unbind();
      }
      if (this._watcher) {
        this._watcher.teardown();
      }
      var listeners = this._listeners;
      var i;
      if (listeners) {
        i = listeners.length;
        while (i--) {
          off(this.el, listeners[i][0], listeners[i][1]);
        }
      }
      var unwatchFns = this._paramUnwatchFns;
      if (unwatchFns) {
        i = unwatchFns.length;
        while (i--) {
          unwatchFns[i]();
        }
      }
      if ('development' !== 'production' && this.el) {
        this.el._vue_directives.$remove(this);
      }
      this.vm = this.el = this._watcher = this._listeners = null;
    }
  };

  function lifecycleMixin (Vue) {
    /**
     * Update v-ref for component.
     *
     * @param {Boolean} remove
     */

    Vue.prototype._updateRef = function (remove) {
      var ref = this.$options._ref;
      if (ref) {
        var refs = (this._scope || this._context).$refs;
        if (remove) {
          if (refs[ref] === this) {
            refs[ref] = null;
          }
        } else {
          refs[ref] = this;
        }
      }
    };

    /**
     * Transclude, compile and link element.
     *
     * If a pre-compiled linker is available, that means the
     * passed in element will be pre-transcluded and compiled
     * as well - all we need to do is to call the linker.
     *
     * Otherwise we need to call transclude/compile/link here.
     *
     * @param {Element} el
     */

    Vue.prototype._compile = function (el) {
      var options = this.$options;

      // transclude and init element
      // transclude can potentially replace original
      // so we need to keep reference; this step also injects
      // the template and caches the original attributes
      // on the container node and replacer node.
      var original = el;
      el = transclude(el, options);
      this._initElement(el);

      // handle v-pre on root node (#2026)
      if (el.nodeType === 1 && getAttr(el, 'v-pre') !== null) {
        return;
      }

      // root is always compiled per-instance, because
      // container attrs and props can be different every time.
      var contextOptions = this._context && this._context.$options;
      var rootLinker = compileRoot(el, options, contextOptions);

      // resolve slot distribution
      resolveSlots(this, options._content);

      // compile and link the rest
      var contentLinkFn;
      var ctor = this.constructor;
      // component compilation can be cached
      // as long as it's not using inline-template
      if (options._linkerCachable) {
        contentLinkFn = ctor.linker;
        if (!contentLinkFn) {
          contentLinkFn = ctor.linker = compile(el, options);
        }
      }

      // link phase
      // make sure to link root with prop scope!
      var rootUnlinkFn = rootLinker(this, el, this._scope);
      var contentUnlinkFn = contentLinkFn ? contentLinkFn(this, el) : compile(el, options)(this, el);

      // register composite unlink function
      // to be called during instance destruction
      this._unlinkFn = function () {
        rootUnlinkFn();
        // passing destroying: true to avoid searching and
        // splicing the directives
        contentUnlinkFn(true);
      };

      // finally replace original
      if (options.replace) {
        replace(original, el);
      }

      this._isCompiled = true;
      this._callHook('compiled');
    };

    /**
     * Initialize instance element. Called in the public
     * $mount() method.
     *
     * @param {Element} el
     */

    Vue.prototype._initElement = function (el) {
      if (isFragment(el)) {
        this._isFragment = true;
        this.$el = this._fragmentStart = el.firstChild;
        this._fragmentEnd = el.lastChild;
        // set persisted text anchors to empty
        if (this._fragmentStart.nodeType === 3) {
          this._fragmentStart.data = this._fragmentEnd.data = '';
        }
        this._fragment = el;
      } else {
        this.$el = el;
      }
      this.$el.__vue__ = this;
      this._callHook('beforeCompile');
    };

    /**
     * Create and bind a directive to an element.
     *
     * @param {Object} descriptor - parsed directive descriptor
     * @param {Node} node   - target node
     * @param {Vue} [host] - transclusion host component
     * @param {Object} [scope] - v-for scope
     * @param {Fragment} [frag] - owner fragment
     */

    Vue.prototype._bindDir = function (descriptor, node, host, scope, frag) {
      this._directives.push(new Directive(descriptor, this, node, host, scope, frag));
    };

    /**
     * Teardown an instance, unobserves the data, unbind all the
     * directives, turn off all the event listeners, etc.
     *
     * @param {Boolean} remove - whether to remove the DOM node.
     * @param {Boolean} deferCleanup - if true, defer cleanup to
     *                                 be called later
     */

    Vue.prototype._destroy = function (remove, deferCleanup) {
      if (this._isBeingDestroyed) {
        if (!deferCleanup) {
          this._cleanup();
        }
        return;
      }

      var destroyReady;
      var pendingRemoval;

      var self = this;
      // Cleanup should be called either synchronously or asynchronoysly as
      // callback of this.$remove(), or if remove and deferCleanup are false.
      // In any case it should be called after all other removing, unbinding and
      // turning of is done
      var cleanupIfPossible = function cleanupIfPossible() {
        if (destroyReady && !pendingRemoval && !deferCleanup) {
          self._cleanup();
        }
      };

      // remove DOM element
      if (remove && this.$el) {
        pendingRemoval = true;
        this.$remove(function () {
          pendingRemoval = false;
          cleanupIfPossible();
        });
      }

      this._callHook('beforeDestroy');
      this._isBeingDestroyed = true;
      var i;
      // remove self from parent. only necessary
      // if parent is not being destroyed as well.
      var parent = this.$parent;
      if (parent && !parent._isBeingDestroyed) {
        parent.$children.$remove(this);
        // unregister ref (remove: true)
        this._updateRef(true);
      }
      // destroy all children.
      i = this.$children.length;
      while (i--) {
        this.$children[i].$destroy();
      }
      // teardown props
      if (this._propsUnlinkFn) {
        this._propsUnlinkFn();
      }
      // teardown all directives. this also tearsdown all
      // directive-owned watchers.
      if (this._unlinkFn) {
        this._unlinkFn();
      }
      i = this._watchers.length;
      while (i--) {
        this._watchers[i].teardown();
      }
      // remove reference to self on $el
      if (this.$el) {
        this.$el.__vue__ = null;
      }

      destroyReady = true;
      cleanupIfPossible();
    };

    /**
     * Clean up to ensure garbage collection.
     * This is called after the leave transition if there
     * is any.
     */

    Vue.prototype._cleanup = function () {
      if (this._isDestroyed) {
        return;
      }
      // remove self from owner fragment
      // do it in cleanup so that we can call $destroy with
      // defer right when a fragment is about to be removed.
      if (this._frag) {
        this._frag.children.$remove(this);
      }
      // remove reference from data ob
      // frozen object may not have observer.
      if (this._data && this._data.__ob__) {
        this._data.__ob__.removeVm(this);
      }
      // Clean up references to private properties and other
      // instances. preserve reference to _data so that proxy
      // accessors still work. The only potential side effect
      // here is that mutating the instance after it's destroyed
      // may affect the state of other components that are still
      // observing the same object, but that seems to be a
      // reasonable responsibility for the user rather than
      // always throwing an error on them.
      this.$el = this.$parent = this.$root = this.$children = this._watchers = this._context = this._scope = this._directives = null;
      // call the last hook...
      this._isDestroyed = true;
      this._callHook('destroyed');
      // turn off all instance listeners.
      this.$off();
    };
  }

  function miscMixin (Vue) {
    /**
     * Apply a list of filter (descriptors) to a value.
     * Using plain for loops here because this will be called in
     * the getter of any watcher with filters so it is very
     * performance sensitive.
     *
     * @param {*} value
     * @param {*} [oldValue]
     * @param {Array} filters
     * @param {Boolean} write
     * @return {*}
     */

    Vue.prototype._applyFilters = function (value, oldValue, filters, write) {
      var filter, fn, args, arg, offset, i, l, j, k;
      for (i = 0, l = filters.length; i < l; i++) {
        filter = filters[write ? l - i - 1 : i];
        fn = resolveAsset(this.$options, 'filters', filter.name, true);
        if (!fn) continue;
        fn = write ? fn.write : fn.read || fn;
        if (typeof fn !== 'function') continue;
        args = write ? [value, oldValue] : [value];
        offset = write ? 2 : 1;
        if (filter.args) {
          for (j = 0, k = filter.args.length; j < k; j++) {
            arg = filter.args[j];
            args[j + offset] = arg.dynamic ? this.$get(arg.value) : arg.value;
          }
        }
        value = fn.apply(this, args);
      }
      return value;
    };

    /**
     * Resolve a component, depending on whether the component
     * is defined normally or using an async factory function.
     * Resolves synchronously if already resolved, otherwise
     * resolves asynchronously and caches the resolved
     * constructor on the factory.
     *
     * @param {String|Function} value
     * @param {Function} cb
     */

    Vue.prototype._resolveComponent = function (value, cb) {
      var factory;
      if (typeof value === 'function') {
        factory = value;
      } else {
        factory = resolveAsset(this.$options, 'components', value, true);
      }
      /* istanbul ignore if */
      if (!factory) {
        return;
      }
      // async component factory
      if (!factory.options) {
        if (factory.resolved) {
          // cached
          cb(factory.resolved);
        } else if (factory.requested) {
          // pool callbacks
          factory.pendingCallbacks.push(cb);
        } else {
          factory.requested = true;
          var cbs = factory.pendingCallbacks = [cb];
          factory.call(this, function resolve(res) {
            if (isPlainObject(res)) {
              res = Vue.extend(res);
            }
            // cache resolved
            factory.resolved = res;
            // invoke callbacks
            for (var i = 0, l = cbs.length; i < l; i++) {
              cbs[i](res);
            }
          }, function reject(reason) {
            'development' !== 'production' && warn('Failed to resolve async component' + (typeof value === 'string' ? ': ' + value : '') + '. ' + (reason ? '\nReason: ' + reason : ''));
          });
        }
      } else {
        // normal component
        cb(factory);
      }
    };
  }

  var filterRE$1 = /[^|]\|[^|]/;

  function dataAPI (Vue) {
    /**
     * Get the value from an expression on this vm.
     *
     * @param {String} exp
     * @param {Boolean} [asStatement]
     * @return {*}
     */

    Vue.prototype.$get = function (exp, asStatement) {
      var res = parseExpression(exp);
      if (res) {
        if (asStatement) {
          var self = this;
          return function statementHandler() {
            self.$arguments = toArray(arguments);
            var result = res.get.call(self, self);
            self.$arguments = null;
            return result;
          };
        } else {
          try {
            return res.get.call(this, this);
          } catch (e) {}
        }
      }
    };

    /**
     * Set the value from an expression on this vm.
     * The expression must be a valid left-hand
     * expression in an assignment.
     *
     * @param {String} exp
     * @param {*} val
     */

    Vue.prototype.$set = function (exp, val) {
      var res = parseExpression(exp, true);
      if (res && res.set) {
        res.set.call(this, this, val);
      }
    };

    /**
     * Delete a property on the VM
     *
     * @param {String} key
     */

    Vue.prototype.$delete = function (key) {
      del(this._data, key);
    };

    /**
     * Watch an expression, trigger callback when its
     * value changes.
     *
     * @param {String|Function} expOrFn
     * @param {Function} cb
     * @param {Object} [options]
     *                 - {Boolean} deep
     *                 - {Boolean} immediate
     * @return {Function} - unwatchFn
     */

    Vue.prototype.$watch = function (expOrFn, cb, options) {
      var vm = this;
      var parsed;
      if (typeof expOrFn === 'string') {
        parsed = parseDirective(expOrFn);
        expOrFn = parsed.expression;
      }
      var watcher = new Watcher(vm, expOrFn, cb, {
        deep: options && options.deep,
        sync: options && options.sync,
        filters: parsed && parsed.filters,
        user: !options || options.user !== false
      });
      if (options && options.immediate) {
        cb.call(vm, watcher.value);
      }
      return function unwatchFn() {
        watcher.teardown();
      };
    };

    /**
     * Evaluate a text directive, including filters.
     *
     * @param {String} text
     * @param {Boolean} [asStatement]
     * @return {String}
     */

    Vue.prototype.$eval = function (text, asStatement) {
      // check for filters.
      if (filterRE$1.test(text)) {
        var dir = parseDirective(text);
        // the filter regex check might give false positive
        // for pipes inside strings, so it's possible that
        // we don't get any filters here
        var val = this.$get(dir.expression, asStatement);
        return dir.filters ? this._applyFilters(val, null, dir.filters) : val;
      } else {
        // no filter
        return this.$get(text, asStatement);
      }
    };

    /**
     * Interpolate a piece of template text.
     *
     * @param {String} text
     * @return {String}
     */

    Vue.prototype.$interpolate = function (text) {
      var tokens = parseText(text);
      var vm = this;
      if (tokens) {
        if (tokens.length === 1) {
          return vm.$eval(tokens[0].value) + '';
        } else {
          return tokens.map(function (token) {
            return token.tag ? vm.$eval(token.value) : token.value;
          }).join('');
        }
      } else {
        return text;
      }
    };

    /**
     * Log instance data as a plain JS object
     * so that it is easier to inspect in console.
     * This method assumes console is available.
     *
     * @param {String} [path]
     */

    Vue.prototype.$log = function (path) {
      var data = path ? getPath(this._data, path) : this._data;
      if (data) {
        data = clean(data);
      }
      // include computed fields
      if (!path) {
        var key;
        for (key in this.$options.computed) {
          data[key] = clean(this[key]);
        }
        if (this._props) {
          for (key in this._props) {
            data[key] = clean(this[key]);
          }
        }
      }
      console.log(data);
    };

    /**
     * "clean" a getter/setter converted object into a plain
     * object copy.
     *
     * @param {Object} - obj
     * @return {Object}
     */

    function clean(obj) {
      return JSON.parse(JSON.stringify(obj));
    }
  }

  function domAPI (Vue) {
    /**
     * Convenience on-instance nextTick. The callback is
     * auto-bound to the instance, and this avoids component
     * modules having to rely on the global Vue.
     *
     * @param {Function} fn
     */

    Vue.prototype.$nextTick = function (fn) {
      nextTick(fn, this);
    };

    /**
     * Append instance to target
     *
     * @param {Node} target
     * @param {Function} [cb]
     * @param {Boolean} [withTransition] - defaults to true
     */

    Vue.prototype.$appendTo = function (target, cb, withTransition) {
      return insert(this, target, cb, withTransition, append, appendWithTransition);
    };

    /**
     * Prepend instance to target
     *
     * @param {Node} target
     * @param {Function} [cb]
     * @param {Boolean} [withTransition] - defaults to true
     */

    Vue.prototype.$prependTo = function (target, cb, withTransition) {
      target = query(target);
      if (target.hasChildNodes()) {
        this.$before(target.firstChild, cb, withTransition);
      } else {
        this.$appendTo(target, cb, withTransition);
      }
      return this;
    };

    /**
     * Insert instance before target
     *
     * @param {Node} target
     * @param {Function} [cb]
     * @param {Boolean} [withTransition] - defaults to true
     */

    Vue.prototype.$before = function (target, cb, withTransition) {
      return insert(this, target, cb, withTransition, beforeWithCb, beforeWithTransition);
    };

    /**
     * Insert instance after target
     *
     * @param {Node} target
     * @param {Function} [cb]
     * @param {Boolean} [withTransition] - defaults to true
     */

    Vue.prototype.$after = function (target, cb, withTransition) {
      target = query(target);
      if (target.nextSibling) {
        this.$before(target.nextSibling, cb, withTransition);
      } else {
        this.$appendTo(target.parentNode, cb, withTransition);
      }
      return this;
    };

    /**
     * Remove instance from DOM
     *
     * @param {Function} [cb]
     * @param {Boolean} [withTransition] - defaults to true
     */

    Vue.prototype.$remove = function (cb, withTransition) {
      if (!this.$el.parentNode) {
        return cb && cb();
      }
      var inDocument = this._isAttached && inDoc(this.$el);
      // if we are not in document, no need to check
      // for transitions
      if (!inDocument) withTransition = false;
      var self = this;
      var realCb = function realCb() {
        if (inDocument) self._callHook('detached');
        if (cb) cb();
      };
      if (this._isFragment) {
        removeNodeRange(this._fragmentStart, this._fragmentEnd, this, this._fragment, realCb);
      } else {
        var op = withTransition === false ? removeWithCb : removeWithTransition;
        op(this.$el, this, realCb);
      }
      return this;
    };

    /**
     * Shared DOM insertion function.
     *
     * @param {Vue} vm
     * @param {Element} target
     * @param {Function} [cb]
     * @param {Boolean} [withTransition]
     * @param {Function} op1 - op for non-transition insert
     * @param {Function} op2 - op for transition insert
     * @return vm
     */

    function insert(vm, target, cb, withTransition, op1, op2) {
      target = query(target);
      var targetIsDetached = !inDoc(target);
      var op = withTransition === false || targetIsDetached ? op1 : op2;
      var shouldCallHook = !targetIsDetached && !vm._isAttached && !inDoc(vm.$el);
      if (vm._isFragment) {
        mapNodeRange(vm._fragmentStart, vm._fragmentEnd, function (node) {
          op(node, target, vm);
        });
        cb && cb();
      } else {
        op(vm.$el, target, vm, cb);
      }
      if (shouldCallHook) {
        vm._callHook('attached');
      }
      return vm;
    }

    /**
     * Check for selectors
     *
     * @param {String|Element} el
     */

    function query(el) {
      return typeof el === 'string' ? document.querySelector(el) : el;
    }

    /**
     * Append operation that takes a callback.
     *
     * @param {Node} el
     * @param {Node} target
     * @param {Vue} vm - unused
     * @param {Function} [cb]
     */

    function append(el, target, vm, cb) {
      target.appendChild(el);
      if (cb) cb();
    }

    /**
     * InsertBefore operation that takes a callback.
     *
     * @param {Node} el
     * @param {Node} target
     * @param {Vue} vm - unused
     * @param {Function} [cb]
     */

    function beforeWithCb(el, target, vm, cb) {
      before(el, target);
      if (cb) cb();
    }

    /**
     * Remove operation that takes a callback.
     *
     * @param {Node} el
     * @param {Vue} vm - unused
     * @param {Function} [cb]
     */

    function removeWithCb(el, vm, cb) {
      remove(el);
      if (cb) cb();
    }
  }

  function eventsAPI (Vue) {
    /**
     * Listen on the given `event` with `fn`.
     *
     * @param {String} event
     * @param {Function} fn
     */

    Vue.prototype.$on = function (event, fn) {
      (this._events[event] || (this._events[event] = [])).push(fn);
      modifyListenerCount(this, event, 1);
      return this;
    };

    /**
     * Adds an `event` listener that will be invoked a single
     * time then automatically removed.
     *
     * @param {String} event
     * @param {Function} fn
     */

    Vue.prototype.$once = function (event, fn) {
      var self = this;
      function on() {
        self.$off(event, on);
        fn.apply(this, arguments);
      }
      on.fn = fn;
      this.$on(event, on);
      return this;
    };

    /**
     * Remove the given callback for `event` or all
     * registered callbacks.
     *
     * @param {String} event
     * @param {Function} fn
     */

    Vue.prototype.$off = function (event, fn) {
      var cbs;
      // all
      if (!arguments.length) {
        if (this.$parent) {
          for (event in this._events) {
            cbs = this._events[event];
            if (cbs) {
              modifyListenerCount(this, event, -cbs.length);
            }
          }
        }
        this._events = {};
        return this;
      }
      // specific event
      cbs = this._events[event];
      if (!cbs) {
        return this;
      }
      if (arguments.length === 1) {
        modifyListenerCount(this, event, -cbs.length);
        this._events[event] = null;
        return this;
      }
      // specific handler
      var cb;
      var i = cbs.length;
      while (i--) {
        cb = cbs[i];
        if (cb === fn || cb.fn === fn) {
          modifyListenerCount(this, event, -1);
          cbs.splice(i, 1);
          break;
        }
      }
      return this;
    };

    /**
     * Trigger an event on self.
     *
     * @param {String|Object} event
     * @return {Boolean} shouldPropagate
     */

    Vue.prototype.$emit = function (event) {
      var isSource = typeof event === 'string';
      event = isSource ? event : event.name;
      var cbs = this._events[event];
      var shouldPropagate = isSource || !cbs;
      if (cbs) {
        cbs = cbs.length > 1 ? toArray(cbs) : cbs;
        // this is a somewhat hacky solution to the question raised
        // in #2102: for an inline component listener like <comp @test="doThis">,
        // the propagation handling is somewhat broken. Therefore we
        // need to treat these inline callbacks differently.
        var hasParentCbs = isSource && cbs.some(function (cb) {
          return cb._fromParent;
        });
        if (hasParentCbs) {
          shouldPropagate = false;
        }
        var args = toArray(arguments, 1);
        for (var i = 0, l = cbs.length; i < l; i++) {
          var cb = cbs[i];
          var res = cb.apply(this, args);
          if (res === true && (!hasParentCbs || cb._fromParent)) {
            shouldPropagate = true;
          }
        }
      }
      return shouldPropagate;
    };

    /**
     * Recursively broadcast an event to all children instances.
     *
     * @param {String|Object} event
     * @param {...*} additional arguments
     */

    Vue.prototype.$broadcast = function (event) {
      var isSource = typeof event === 'string';
      event = isSource ? event : event.name;
      // if no child has registered for this event,
      // then there's no need to broadcast.
      if (!this._eventsCount[event]) return;
      var children = this.$children;
      var args = toArray(arguments);
      if (isSource) {
        // use object event to indicate non-source emit
        // on children
        args[0] = { name: event, source: this };
      }
      for (var i = 0, l = children.length; i < l; i++) {
        var child = children[i];
        var shouldPropagate = child.$emit.apply(child, args);
        if (shouldPropagate) {
          child.$broadcast.apply(child, args);
        }
      }
      return this;
    };

    /**
     * Recursively propagate an event up the parent chain.
     *
     * @param {String} event
     * @param {...*} additional arguments
     */

    Vue.prototype.$dispatch = function (event) {
      var shouldPropagate = this.$emit.apply(this, arguments);
      if (!shouldPropagate) return;
      var parent = this.$parent;
      var args = toArray(arguments);
      // use object event to indicate non-source emit
      // on parents
      args[0] = { name: event, source: this };
      while (parent) {
        shouldPropagate = parent.$emit.apply(parent, args);
        parent = shouldPropagate ? parent.$parent : null;
      }
      return this;
    };

    /**
     * Modify the listener counts on all parents.
     * This bookkeeping allows $broadcast to return early when
     * no child has listened to a certain event.
     *
     * @param {Vue} vm
     * @param {String} event
     * @param {Number} count
     */

    var hookRE = /^hook:/;
    function modifyListenerCount(vm, event, count) {
      var parent = vm.$parent;
      // hooks do not get broadcasted so no need
      // to do bookkeeping for them
      if (!parent || !count || hookRE.test(event)) return;
      while (parent) {
        parent._eventsCount[event] = (parent._eventsCount[event] || 0) + count;
        parent = parent.$parent;
      }
    }
  }

  function lifecycleAPI (Vue) {
    /**
     * Set instance target element and kick off the compilation
     * process. The passed in `el` can be a selector string, an
     * existing Element, or a DocumentFragment (for block
     * instances).
     *
     * @param {Element|DocumentFragment|string} el
     * @public
     */

    Vue.prototype.$mount = function (el) {
      if (this._isCompiled) {
        'development' !== 'production' && warn('$mount() should be called only once.', this);
        return;
      }
      el = query(el);
      if (!el) {
        el = document.createElement('div');
      }
      this._compile(el);
      this._initDOMHooks();
      if (inDoc(this.$el)) {
        this._callHook('attached');
        ready.call(this);
      } else {
        this.$once('hook:attached', ready);
      }
      return this;
    };

    /**
     * Mark an instance as ready.
     */

    function ready() {
      this._isAttached = true;
      this._isReady = true;
      this._callHook('ready');
    }

    /**
     * Teardown the instance, simply delegate to the internal
     * _destroy.
     *
     * @param {Boolean} remove
     * @param {Boolean} deferCleanup
     */

    Vue.prototype.$destroy = function (remove, deferCleanup) {
      this._destroy(remove, deferCleanup);
    };

    /**
     * Partially compile a piece of DOM and return a
     * decompile function.
     *
     * @param {Element|DocumentFragment} el
     * @param {Vue} [host]
     * @param {Object} [scope]
     * @param {Fragment} [frag]
     * @return {Function}
     */

    Vue.prototype.$compile = function (el, host, scope, frag) {
      return compile(el, this.$options, true)(this, el, host, scope, frag);
    };
  }

  /**
   * The exposed Vue constructor.
   *
   * API conventions:
   * - public API methods/properties are prefixed with `$`
   * - internal methods/properties are prefixed with `_`
   * - non-prefixed properties are assumed to be proxied user
   *   data.
   *
   * @constructor
   * @param {Object} [options]
   * @public
   */

  function Vue(options) {
    this._init(options);
  }

  // install internals
  initMixin(Vue);
  stateMixin(Vue);
  eventsMixin(Vue);
  lifecycleMixin(Vue);
  miscMixin(Vue);

  // install instance APIs
  dataAPI(Vue);
  domAPI(Vue);
  eventsAPI(Vue);
  lifecycleAPI(Vue);

  var slot = {

    priority: SLOT,
    params: ['name'],

    bind: function bind() {
      // this was resolved during component transclusion
      var name = this.params.name || 'default';
      var content = this.vm._slotContents && this.vm._slotContents[name];
      if (!content || !content.hasChildNodes()) {
        this.fallback();
      } else {
        this.compile(content.cloneNode(true), this.vm._context, this.vm);
      }
    },

    compile: function compile(content, context, host) {
      if (content && context) {
        if (this.el.hasChildNodes() && content.childNodes.length === 1 && content.childNodes[0].nodeType === 1 && content.childNodes[0].hasAttribute('v-if')) {
          // if the inserted slot has v-if
          // inject fallback content as the v-else
          var elseBlock = document.createElement('template');
          elseBlock.setAttribute('v-else', '');
          elseBlock.innerHTML = this.el.innerHTML;
          // the else block should be compiled in child scope
          elseBlock._context = this.vm;
          content.appendChild(elseBlock);
        }
        var scope = host ? host._scope : this._scope;
        this.unlink = context.$compile(content, host, scope, this._frag);
      }
      if (content) {
        replace(this.el, content);
      } else {
        remove(this.el);
      }
    },

    fallback: function fallback() {
      this.compile(extractContent(this.el, true), this.vm);
    },

    unbind: function unbind() {
      if (this.unlink) {
        this.unlink();
      }
    }
  };

  var partial = {

    priority: PARTIAL,

    params: ['name'],

    // watch changes to name for dynamic partials
    paramWatchers: {
      name: function name(value) {
        vIf.remove.call(this);
        if (value) {
          this.insert(value);
        }
      }
    },

    bind: function bind() {
      this.anchor = createAnchor('v-partial');
      replace(this.el, this.anchor);
      this.insert(this.params.name);
    },

    insert: function insert(id) {
      var partial = resolveAsset(this.vm.$options, 'partials', id, true);
      if (partial) {
        this.factory = new FragmentFactory(this.vm, partial);
        vIf.insert.call(this);
      }
    },

    unbind: function unbind() {
      if (this.frag) {
        this.frag.destroy();
      }
    }
  };

  var elementDirectives = {
    slot: slot,
    partial: partial
  };

  var convertArray = vFor._postProcess;

  /**
   * Limit filter for arrays
   *
   * @param {Number} n
   * @param {Number} offset (Decimal expected)
   */

  function limitBy(arr, n, offset) {
    offset = offset ? parseInt(offset, 10) : 0;
    n = toNumber(n);
    return typeof n === 'number' ? arr.slice(offset, offset + n) : arr;
  }

  /**
   * Filter filter for arrays
   *
   * @param {String} search
   * @param {String} [delimiter]
   * @param {String} ...dataKeys
   */

  function filterBy(arr, search, delimiter) {
    arr = convertArray(arr);
    if (search == null) {
      return arr;
    }
    if (typeof search === 'function') {
      return arr.filter(search);
    }
    // cast to lowercase string
    search = ('' + search).toLowerCase();
    // allow optional `in` delimiter
    // because why not
    var n = delimiter === 'in' ? 3 : 2;
    // extract and flatten keys
    var keys = Array.prototype.concat.apply([], toArray(arguments, n));
    var res = [];
    var item, key, val, j;
    for (var i = 0, l = arr.length; i < l; i++) {
      item = arr[i];
      val = item && item.$value || item;
      j = keys.length;
      if (j) {
        while (j--) {
          key = keys[j];
          if (key === '$key' && contains(item.$key, search) || contains(getPath(val, key), search)) {
            res.push(item);
            break;
          }
        }
      } else if (contains(item, search)) {
        res.push(item);
      }
    }
    return res;
  }

  /**
   * Filter filter for arrays
   *
   * @param {String|Array<String>|Function} ...sortKeys
   * @param {Number} [order]
   */

  function orderBy(arr) {
    var comparator = null;
    var sortKeys = undefined;
    arr = convertArray(arr);

    // determine order (last argument)
    var args = toArray(arguments, 1);
    var order = args[args.length - 1];
    if (typeof order === 'number') {
      order = order < 0 ? -1 : 1;
      args = args.length > 1 ? args.slice(0, -1) : args;
    } else {
      order = 1;
    }

    // determine sortKeys & comparator
    var firstArg = args[0];
    if (!firstArg) {
      return arr;
    } else if (typeof firstArg === 'function') {
      // custom comparator
      comparator = function (a, b) {
        return firstArg(a, b) * order;
      };
    } else {
      // string keys. flatten first
      sortKeys = Array.prototype.concat.apply([], args);
      comparator = function (a, b, i) {
        i = i || 0;
        return i >= sortKeys.length - 1 ? baseCompare(a, b, i) : baseCompare(a, b, i) || comparator(a, b, i + 1);
      };
    }

    function baseCompare(a, b, sortKeyIndex) {
      var sortKey = sortKeys[sortKeyIndex];
      if (sortKey) {
        if (sortKey !== '$key') {
          if (isObject(a) && '$value' in a) a = a.$value;
          if (isObject(b) && '$value' in b) b = b.$value;
        }
        a = isObject(a) ? getPath(a, sortKey) : a;
        b = isObject(b) ? getPath(b, sortKey) : b;
      }
      return a === b ? 0 : a > b ? order : -order;
    }

    // sort on a copy to avoid mutating original array
    return arr.slice().sort(comparator);
  }

  /**
   * String contain helper
   *
   * @param {*} val
   * @param {String} search
   */

  function contains(val, search) {
    var i;
    if (isPlainObject(val)) {
      var keys = Object.keys(val);
      i = keys.length;
      while (i--) {
        if (contains(val[keys[i]], search)) {
          return true;
        }
      }
    } else if (isArray(val)) {
      i = val.length;
      while (i--) {
        if (contains(val[i], search)) {
          return true;
        }
      }
    } else if (val != null) {
      return val.toString().toLowerCase().indexOf(search) > -1;
    }
  }

  var digitsRE = /(\d{3})(?=\d)/g;

  // asset collections must be a plain object.
  var filters = {

    orderBy: orderBy,
    filterBy: filterBy,
    limitBy: limitBy,

    /**
     * Stringify value.
     *
     * @param {Number} indent
     */

    json: {
      read: function read(value, indent) {
        return typeof value === 'string' ? value : JSON.stringify(value, null, arguments.length > 1 ? indent : 2);
      },
      write: function write(value) {
        try {
          return JSON.parse(value);
        } catch (e) {
          return value;
        }
      }
    },

    /**
     * 'abc' => 'Abc'
     */

    capitalize: function capitalize(value) {
      if (!value && value !== 0) return '';
      value = value.toString();
      return value.charAt(0).toUpperCase() + value.slice(1);
    },

    /**
     * 'abc' => 'ABC'
     */

    uppercase: function uppercase(value) {
      return value || value === 0 ? value.toString().toUpperCase() : '';
    },

    /**
     * 'AbC' => 'abc'
     */

    lowercase: function lowercase(value) {
      return value || value === 0 ? value.toString().toLowerCase() : '';
    },

    /**
     * 12345 => $12,345.00
     *
     * @param {String} sign
     * @param {Number} decimals Decimal places
     */

    currency: function currency(value, _currency, decimals) {
      value = parseFloat(value);
      if (!isFinite(value) || !value && value !== 0) return '';
      _currency = _currency != null ? _currency : '$';
      decimals = decimals != null ? decimals : 2;
      var stringified = Math.abs(value).toFixed(decimals);
      var _int = decimals ? stringified.slice(0, -1 - decimals) : stringified;
      var i = _int.length % 3;
      var head = i > 0 ? _int.slice(0, i) + (_int.length > 3 ? ',' : '') : '';
      var _float = decimals ? stringified.slice(-1 - decimals) : '';
      var sign = value < 0 ? '-' : '';
      return sign + _currency + head + _int.slice(i).replace(digitsRE, '$1,') + _float;
    },

    /**
     * 'item' => 'items'
     *
     * @params
     *  an array of strings corresponding to
     *  the single, double, triple ... forms of the word to
     *  be pluralized. When the number to be pluralized
     *  exceeds the length of the args, it will use the last
     *  entry in the array.
     *
     *  e.g. ['single', 'double', 'triple', 'multiple']
     */

    pluralize: function pluralize(value) {
      var args = toArray(arguments, 1);
      var length = args.length;
      if (length > 1) {
        var index = value % 10 - 1;
        return index in args ? args[index] : args[length - 1];
      } else {
        return args[0] + (value === 1 ? '' : 's');
      }
    },

    /**
     * Debounce a handler function.
     *
     * @param {Function} handler
     * @param {Number} delay = 300
     * @return {Function}
     */

    debounce: function debounce(handler, delay) {
      if (!handler) return;
      if (!delay) {
        delay = 300;
      }
      return _debounce(handler, delay);
    }
  };

  function installGlobalAPI (Vue) {
    /**
     * Vue and every constructor that extends Vue has an
     * associated options object, which can be accessed during
     * compilation steps as `this.constructor.options`.
     *
     * These can be seen as the default options of every
     * Vue instance.
     */

    Vue.options = {
      directives: directives,
      elementDirectives: elementDirectives,
      filters: filters,
      transitions: {},
      components: {},
      partials: {},
      replace: true
    };

    /**
     * Expose useful internals
     */

    Vue.util = util;
    Vue.config = config;
    Vue.set = set;
    Vue['delete'] = del;
    Vue.nextTick = nextTick;

    /**
     * The following are exposed for advanced usage / plugins
     */

    Vue.compiler = compiler;
    Vue.FragmentFactory = FragmentFactory;
    Vue.internalDirectives = internalDirectives;
    Vue.parsers = {
      path: path,
      text: text,
      template: template,
      directive: directive,
      expression: expression
    };

    /**
     * Each instance constructor, including Vue, has a unique
     * cid. This enables us to create wrapped "child
     * constructors" for prototypal inheritance and cache them.
     */

    Vue.cid = 0;
    var cid = 1;

    /**
     * Class inheritance
     *
     * @param {Object} extendOptions
     */

    Vue.extend = function (extendOptions) {
      extendOptions = extendOptions || {};
      var Super = this;
      var isFirstExtend = Super.cid === 0;
      if (isFirstExtend && extendOptions._Ctor) {
        return extendOptions._Ctor;
      }
      var name = extendOptions.name || Super.options.name;
      if ('development' !== 'production') {
        if (!/^[a-zA-Z][\w-]*$/.test(name)) {
          warn('Invalid component name: "' + name + '". Component names ' + 'can only contain alphanumeric characaters and the hyphen.');
          name = null;
        }
      }
      var Sub = createClass(name || 'VueComponent');
      Sub.prototype = Object.create(Super.prototype);
      Sub.prototype.constructor = Sub;
      Sub.cid = cid++;
      Sub.options = mergeOptions(Super.options, extendOptions);
      Sub['super'] = Super;
      // allow further extension
      Sub.extend = Super.extend;
      // create asset registers, so extended classes
      // can have their private assets too.
      config._assetTypes.forEach(function (type) {
        Sub[type] = Super[type];
      });
      // enable recursive self-lookup
      if (name) {
        Sub.options.components[name] = Sub;
      }
      // cache constructor
      if (isFirstExtend) {
        extendOptions._Ctor = Sub;
      }
      return Sub;
    };

    /**
     * A function that returns a sub-class constructor with the
     * given name. This gives us much nicer output when
     * logging instances in the console.
     *
     * @param {String} name
     * @return {Function}
     */

    function createClass(name) {
      /* eslint-disable no-new-func */
      return new Function('return function ' + classify(name) + ' (options) { this._init(options) }')();
      /* eslint-enable no-new-func */
    }

    /**
     * Plugin system
     *
     * @param {Object} plugin
     */

    Vue.use = function (plugin) {
      /* istanbul ignore if */
      if (plugin.installed) {
        return;
      }
      // additional parameters
      var args = toArray(arguments, 1);
      args.unshift(this);
      if (typeof plugin.install === 'function') {
        plugin.install.apply(plugin, args);
      } else {
        plugin.apply(null, args);
      }
      plugin.installed = true;
      return this;
    };

    /**
     * Apply a global mixin by merging it into the default
     * options.
     */

    Vue.mixin = function (mixin) {
      Vue.options = mergeOptions(Vue.options, mixin);
    };

    /**
     * Create asset registration methods with the following
     * signature:
     *
     * @param {String} id
     * @param {*} definition
     */

    config._assetTypes.forEach(function (type) {
      Vue[type] = function (id, definition) {
        if (!definition) {
          return this.options[type + 's'][id];
        } else {
          /* istanbul ignore if */
          if ('development' !== 'production') {
            if (type === 'component' && (commonTagRE.test(id) || reservedTagRE.test(id))) {
              warn('Do not use built-in or reserved HTML elements as component ' + 'id: ' + id);
            }
          }
          if (type === 'component' && isPlainObject(definition)) {
            if (!definition.name) {
              definition.name = id;
            }
            definition = Vue.extend(definition);
          }
          this.options[type + 's'][id] = definition;
          return definition;
        }
      };
    });

    // expose internal transition API
    extend(Vue.transition, transition);
  }

  installGlobalAPI(Vue);

  Vue.version = '1.0.26';

  // devtools global hook
  /* istanbul ignore next */
  setTimeout(function () {
    if (config.devtools) {
      if (devtools) {
        devtools.emit('init', Vue);
      } else if ('development' !== 'production' && inBrowser && /Chrome\/\d+/.test(window.navigator.userAgent)) {
        console.log('Download the Vue Devtools for a better development experience:\n' + 'https://github.com/vuejs/vue-devtools');
      }
    }
  }, 0);

  return Vue;

}));
/*!
 * clipboard.js v1.5.15
 * https://zenorocha.github.io/clipboard.js
 *
 * Licensed MIT © Zeno Rocha
 */
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.Clipboard = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
/**
 * A polyfill for Element.matches()
 */
if (Element && !Element.prototype.matches) {
    var proto = Element.prototype;

    proto.matches = proto.matchesSelector ||
                    proto.mozMatchesSelector ||
                    proto.msMatchesSelector ||
                    proto.oMatchesSelector ||
                    proto.webkitMatchesSelector;
}

/**
 * Finds the closest parent that matches a selector.
 *
 * @param {Element} element
 * @param {String} selector
 * @return {Function}
 */
function closest (element, selector) {
    while (element && element !== document) {
        if (element.matches(selector)) return element;
        element = element.parentNode;
    }
}

module.exports = closest;

},{}],2:[function(require,module,exports){
var closest = require('./closest');

/**
 * Delegates event to a selector.
 *
 * @param {Element} element
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @param {Boolean} useCapture
 * @return {Object}
 */
function delegate(element, selector, type, callback, useCapture) {
    var listenerFn = listener.apply(this, arguments);

    element.addEventListener(type, listenerFn, useCapture);

    return {
        destroy: function() {
            element.removeEventListener(type, listenerFn, useCapture);
        }
    }
}

/**
 * Finds closest match and invokes callback.
 *
 * @param {Element} element
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @return {Function}
 */
function listener(element, selector, type, callback) {
    return function(e) {
        e.delegateTarget = closest(e.target, selector);

        if (e.delegateTarget) {
            callback.call(element, e);
        }
    }
}

module.exports = delegate;

},{"./closest":1}],3:[function(require,module,exports){
/**
 * Check if argument is a HTML element.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.node = function(value) {
    return value !== undefined
        && value instanceof HTMLElement
        && value.nodeType === 1;
};

/**
 * Check if argument is a list of HTML elements.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.nodeList = function(value) {
    var type = Object.prototype.toString.call(value);

    return value !== undefined
        && (type === '[object NodeList]' || type === '[object HTMLCollection]')
        && ('length' in value)
        && (value.length === 0 || exports.node(value[0]));
};

/**
 * Check if argument is a string.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.string = function(value) {
    return typeof value === 'string'
        || value instanceof String;
};

/**
 * Check if argument is a function.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.fn = function(value) {
    var type = Object.prototype.toString.call(value);

    return type === '[object Function]';
};

},{}],4:[function(require,module,exports){
var is = require('./is');
var delegate = require('delegate');

/**
 * Validates all params and calls the right
 * listener function based on its target type.
 *
 * @param {String|HTMLElement|HTMLCollection|NodeList} target
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listen(target, type, callback) {
    if (!target && !type && !callback) {
        throw new Error('Missing required arguments');
    }

    if (!is.string(type)) {
        throw new TypeError('Second argument must be a String');
    }

    if (!is.fn(callback)) {
        throw new TypeError('Third argument must be a Function');
    }

    if (is.node(target)) {
        return listenNode(target, type, callback);
    }
    else if (is.nodeList(target)) {
        return listenNodeList(target, type, callback);
    }
    else if (is.string(target)) {
        return listenSelector(target, type, callback);
    }
    else {
        throw new TypeError('First argument must be a String, HTMLElement, HTMLCollection, or NodeList');
    }
}

/**
 * Adds an event listener to a HTML element
 * and returns a remove listener function.
 *
 * @param {HTMLElement} node
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenNode(node, type, callback) {
    node.addEventListener(type, callback);

    return {
        destroy: function() {
            node.removeEventListener(type, callback);
        }
    }
}

/**
 * Add an event listener to a list of HTML elements
 * and returns a remove listener function.
 *
 * @param {NodeList|HTMLCollection} nodeList
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenNodeList(nodeList, type, callback) {
    Array.prototype.forEach.call(nodeList, function(node) {
        node.addEventListener(type, callback);
    });

    return {
        destroy: function() {
            Array.prototype.forEach.call(nodeList, function(node) {
                node.removeEventListener(type, callback);
            });
        }
    }
}

/**
 * Add an event listener to a selector
 * and returns a remove listener function.
 *
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenSelector(selector, type, callback) {
    return delegate(document.body, selector, type, callback);
}

module.exports = listen;

},{"./is":3,"delegate":2}],5:[function(require,module,exports){
function select(element) {
    var selectedText;

    if (element.nodeName === 'SELECT') {
        element.focus();

        selectedText = element.value;
    }
    else if (element.nodeName === 'INPUT' || element.nodeName === 'TEXTAREA') {
        element.focus();
        element.setSelectionRange(0, element.value.length);

        selectedText = element.value;
    }
    else {
        if (element.hasAttribute('contenteditable')) {
            element.focus();
        }

        var selection = window.getSelection();
        var range = document.createRange();

        range.selectNodeContents(element);
        selection.removeAllRanges();
        selection.addRange(range);

        selectedText = selection.toString();
    }

    return selectedText;
}

module.exports = select;

},{}],6:[function(require,module,exports){
function E () {
  // Keep this empty so it's easier to inherit from
  // (via https://github.com/lipsmack from https://github.com/scottcorgan/tiny-emitter/issues/3)
}

E.prototype = {
  on: function (name, callback, ctx) {
    var e = this.e || (this.e = {});

    (e[name] || (e[name] = [])).push({
      fn: callback,
      ctx: ctx
    });

    return this;
  },

  once: function (name, callback, ctx) {
    var self = this;
    function listener () {
      self.off(name, listener);
      callback.apply(ctx, arguments);
    };

    listener._ = callback
    return this.on(name, listener, ctx);
  },

  emit: function (name) {
    var data = [].slice.call(arguments, 1);
    var evtArr = ((this.e || (this.e = {}))[name] || []).slice();
    var i = 0;
    var len = evtArr.length;

    for (i; i < len; i++) {
      evtArr[i].fn.apply(evtArr[i].ctx, data);
    }

    return this;
  },

  off: function (name, callback) {
    var e = this.e || (this.e = {});
    var evts = e[name];
    var liveEvents = [];

    if (evts && callback) {
      for (var i = 0, len = evts.length; i < len; i++) {
        if (evts[i].fn !== callback && evts[i].fn._ !== callback)
          liveEvents.push(evts[i]);
      }
    }

    // Remove event from queue to prevent memory leak
    // Suggested by https://github.com/lazd
    // Ref: https://github.com/scottcorgan/tiny-emitter/commit/c6ebfaa9bc973b33d110a84a307742b7cf94c953#commitcomment-5024910

    (liveEvents.length)
      ? e[name] = liveEvents
      : delete e[name];

    return this;
  }
};

module.exports = E;

},{}],7:[function(require,module,exports){
(function (global, factory) {
    if (typeof define === "function" && define.amd) {
        define(['module', 'select'], factory);
    } else if (typeof exports !== "undefined") {
        factory(module, require('select'));
    } else {
        var mod = {
            exports: {}
        };
        factory(mod, global.select);
        global.clipboardAction = mod.exports;
    }
})(this, function (module, _select) {
    'use strict';

    var _select2 = _interopRequireDefault(_select);

    function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : {
            default: obj
        };
    }

    var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) {
        return typeof obj;
    } : function (obj) {
        return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj;
    };

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    var _createClass = function () {
        function defineProperties(target, props) {
            for (var i = 0; i < props.length; i++) {
                var descriptor = props[i];
                descriptor.enumerable = descriptor.enumerable || false;
                descriptor.configurable = true;
                if ("value" in descriptor) descriptor.writable = true;
                Object.defineProperty(target, descriptor.key, descriptor);
            }
        }

        return function (Constructor, protoProps, staticProps) {
            if (protoProps) defineProperties(Constructor.prototype, protoProps);
            if (staticProps) defineProperties(Constructor, staticProps);
            return Constructor;
        };
    }();

    var ClipboardAction = function () {
        /**
         * @param {Object} options
         */
        function ClipboardAction(options) {
            _classCallCheck(this, ClipboardAction);

            this.resolveOptions(options);
            this.initSelection();
        }

        /**
         * Defines base properties passed from constructor.
         * @param {Object} options
         */


        _createClass(ClipboardAction, [{
            key: 'resolveOptions',
            value: function resolveOptions() {
                var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

                this.action = options.action;
                this.emitter = options.emitter;
                this.target = options.target;
                this.text = options.text;
                this.trigger = options.trigger;

                this.selectedText = '';
            }
        }, {
            key: 'initSelection',
            value: function initSelection() {
                if (this.text) {
                    this.selectFake();
                } else if (this.target) {
                    this.selectTarget();
                }
            }
        }, {
            key: 'selectFake',
            value: function selectFake() {
                var _this = this;

                var isRTL = document.documentElement.getAttribute('dir') == 'rtl';

                this.removeFake();

                this.fakeHandlerCallback = function () {
                    return _this.removeFake();
                };
                this.fakeHandler = document.body.addEventListener('click', this.fakeHandlerCallback) || true;

                this.fakeElem = document.createElement('textarea');
                // Prevent zooming on iOS
                this.fakeElem.style.fontSize = '12pt';
                // Reset box model
                this.fakeElem.style.border = '0';
                this.fakeElem.style.padding = '0';
                this.fakeElem.style.margin = '0';
                // Move element out of screen horizontally
                this.fakeElem.style.position = 'absolute';
                this.fakeElem.style[isRTL ? 'right' : 'left'] = '-9999px';
                // Move element to the same position vertically
                var yPosition = window.pageYOffset || document.documentElement.scrollTop;
                this.fakeElem.addEventListener('focus', window.scrollTo(0, yPosition));
                this.fakeElem.style.top = yPosition + 'px';

                this.fakeElem.setAttribute('readonly', '');
                this.fakeElem.value = this.text;

                document.body.appendChild(this.fakeElem);

                this.selectedText = (0, _select2.default)(this.fakeElem);
                this.copyText();
            }
        }, {
            key: 'removeFake',
            value: function removeFake() {
                if (this.fakeHandler) {
                    document.body.removeEventListener('click', this.fakeHandlerCallback);
                    this.fakeHandler = null;
                    this.fakeHandlerCallback = null;
                }

                if (this.fakeElem) {
                    document.body.removeChild(this.fakeElem);
                    this.fakeElem = null;
                }
            }
        }, {
            key: 'selectTarget',
            value: function selectTarget() {
                this.selectedText = (0, _select2.default)(this.target);
                this.copyText();
            }
        }, {
            key: 'copyText',
            value: function copyText() {
                var succeeded = void 0;

                try {
                    succeeded = document.execCommand(this.action);
                } catch (err) {
                    succeeded = false;
                }

                this.handleResult(succeeded);
            }
        }, {
            key: 'handleResult',
            value: function handleResult(succeeded) {
                this.emitter.emit(succeeded ? 'success' : 'error', {
                    action: this.action,
                    text: this.selectedText,
                    trigger: this.trigger,
                    clearSelection: this.clearSelection.bind(this)
                });
            }
        }, {
            key: 'clearSelection',
            value: function clearSelection() {
                if (this.target) {
                    this.target.blur();
                }

                window.getSelection().removeAllRanges();
            }
        }, {
            key: 'destroy',
            value: function destroy() {
                this.removeFake();
            }
        }, {
            key: 'action',
            set: function set() {
                var action = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'copy';

                this._action = action;

                if (this._action !== 'copy' && this._action !== 'cut') {
                    throw new Error('Invalid "action" value, use either "copy" or "cut"');
                }
            },
            get: function get() {
                return this._action;
            }
        }, {
            key: 'target',
            set: function set(target) {
                if (target !== undefined) {
                    if (target && (typeof target === 'undefined' ? 'undefined' : _typeof(target)) === 'object' && target.nodeType === 1) {
                        if (this.action === 'copy' && target.hasAttribute('disabled')) {
                            throw new Error('Invalid "target" attribute. Please use "readonly" instead of "disabled" attribute');
                        }

                        if (this.action === 'cut' && (target.hasAttribute('readonly') || target.hasAttribute('disabled'))) {
                            throw new Error('Invalid "target" attribute. You can\'t cut text from elements with "readonly" or "disabled" attributes');
                        }

                        this._target = target;
                    } else {
                        throw new Error('Invalid "target" value, use a valid Element');
                    }
                }
            },
            get: function get() {
                return this._target;
            }
        }]);

        return ClipboardAction;
    }();

    module.exports = ClipboardAction;
});

},{"select":5}],8:[function(require,module,exports){
(function (global, factory) {
    if (typeof define === "function" && define.amd) {
        define(['module', './clipboard-action', 'tiny-emitter', 'good-listener'], factory);
    } else if (typeof exports !== "undefined") {
        factory(module, require('./clipboard-action'), require('tiny-emitter'), require('good-listener'));
    } else {
        var mod = {
            exports: {}
        };
        factory(mod, global.clipboardAction, global.tinyEmitter, global.goodListener);
        global.clipboard = mod.exports;
    }
})(this, function (module, _clipboardAction, _tinyEmitter, _goodListener) {
    'use strict';

    var _clipboardAction2 = _interopRequireDefault(_clipboardAction);

    var _tinyEmitter2 = _interopRequireDefault(_tinyEmitter);

    var _goodListener2 = _interopRequireDefault(_goodListener);

    function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : {
            default: obj
        };
    }

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    var _createClass = function () {
        function defineProperties(target, props) {
            for (var i = 0; i < props.length; i++) {
                var descriptor = props[i];
                descriptor.enumerable = descriptor.enumerable || false;
                descriptor.configurable = true;
                if ("value" in descriptor) descriptor.writable = true;
                Object.defineProperty(target, descriptor.key, descriptor);
            }
        }

        return function (Constructor, protoProps, staticProps) {
            if (protoProps) defineProperties(Constructor.prototype, protoProps);
            if (staticProps) defineProperties(Constructor, staticProps);
            return Constructor;
        };
    }();

    function _possibleConstructorReturn(self, call) {
        if (!self) {
            throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
        }

        return call && (typeof call === "object" || typeof call === "function") ? call : self;
    }

    function _inherits(subClass, superClass) {
        if (typeof superClass !== "function" && superClass !== null) {
            throw new TypeError("Super expression must either be null or a function, not " + typeof superClass);
        }

        subClass.prototype = Object.create(superClass && superClass.prototype, {
            constructor: {
                value: subClass,
                enumerable: false,
                writable: true,
                configurable: true
            }
        });
        if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
    }

    var Clipboard = function (_Emitter) {
        _inherits(Clipboard, _Emitter);

        /**
         * @param {String|HTMLElement|HTMLCollection|NodeList} trigger
         * @param {Object} options
         */
        function Clipboard(trigger, options) {
            _classCallCheck(this, Clipboard);

            var _this = _possibleConstructorReturn(this, (Clipboard.__proto__ || Object.getPrototypeOf(Clipboard)).call(this));

            _this.resolveOptions(options);
            _this.listenClick(trigger);
            return _this;
        }

        /**
         * Defines if attributes would be resolved using internal setter functions
         * or custom functions that were passed in the constructor.
         * @param {Object} options
         */


        _createClass(Clipboard, [{
            key: 'resolveOptions',
            value: function resolveOptions() {
                var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

                this.action = typeof options.action === 'function' ? options.action : this.defaultAction;
                this.target = typeof options.target === 'function' ? options.target : this.defaultTarget;
                this.text = typeof options.text === 'function' ? options.text : this.defaultText;
            }
        }, {
            key: 'listenClick',
            value: function listenClick(trigger) {
                var _this2 = this;

                this.listener = (0, _goodListener2.default)(trigger, 'click', function (e) {
                    return _this2.onClick(e);
                });
            }
        }, {
            key: 'onClick',
            value: function onClick(e) {
                var trigger = e.delegateTarget || e.currentTarget;

                if (this.clipboardAction) {
                    this.clipboardAction = null;
                }

                this.clipboardAction = new _clipboardAction2.default({
                    action: this.action(trigger),
                    target: this.target(trigger),
                    text: this.text(trigger),
                    trigger: trigger,
                    emitter: this
                });
            }
        }, {
            key: 'defaultAction',
            value: function defaultAction(trigger) {
                return getAttributeValue('action', trigger);
            }
        }, {
            key: 'defaultTarget',
            value: function defaultTarget(trigger) {
                var selector = getAttributeValue('target', trigger);

                if (selector) {
                    return document.querySelector(selector);
                }
            }
        }, {
            key: 'defaultText',
            value: function defaultText(trigger) {
                return getAttributeValue('text', trigger);
            }
        }, {
            key: 'destroy',
            value: function destroy() {
                this.listener.destroy();

                if (this.clipboardAction) {
                    this.clipboardAction.destroy();
                    this.clipboardAction = null;
                }
            }
        }]);

        return Clipboard;
    }(_tinyEmitter2.default);

    /**
     * Helper function to retrieve attribute value.
     * @param {String} suffix
     * @param {Element} element
     */
    function getAttributeValue(suffix, element) {
        var attribute = 'data-clipboard-' + suffix;

        if (!element.hasAttribute(attribute)) {
            return;
        }

        return element.getAttribute(attribute);
    }

    module.exports = Clipboard;
});

},{"./clipboard-action":7,"good-listener":4,"tiny-emitter":6}]},{},[8])(8)
});
/* 
* Project: Bootstrap Notify = v3.1.3
* Description: Turns standard Bootstrap alerts into "Growl-like" notifications.
* Author: Mouse0270 aka Robert McIntosh
* License: MIT License
* Website: https://github.com/mouse0270/bootstrap-growl
*/
(function (factory) {
	if (typeof define === 'function' && define.amd) {
		// AMD. Register as an anonymous module.
		define(['jquery'], factory);
	} else if (typeof exports === 'object') {
		// Node/CommonJS
		factory(require('jquery'));
	} else {
		// Browser globals
		factory(jQuery);
	}
}(function ($) {
	// Create the defaults once
	var defaults = {
			element: 'body',
			position: null,
			type: "info",
			allow_dismiss: true,
			newest_on_top: false,
			showProgressbar: false,
			placement: {
				from: "top",
				align: "right"
			},
			offset: 20,
			spacing: 10,
			z_index: 1031,
			delay: 5000,
			timer: 1000,
			url_target: '_blank',
			mouse_over: null,
			animate: {
				enter: 'animated fadeInDown',
				exit: 'animated fadeOutUp'
			},
			onShow: null,
			onShown: null,
			onClose: null,
			onClosed: null,
			icon_type: 'class',
			template: '<div data-notify="container" class="col-xs-11 col-sm-4 alert alert-{0}" role="alert"><button type="button" aria-hidden="true" class="close" data-notify="dismiss">&times;</button><span data-notify="icon"></span> <span data-notify="title">{1}</span> <span data-notify="message">{2}</span><div class="progress" data-notify="progressbar"><div class="progress-bar progress-bar-{0}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div></div><a href="{3}" target="{4}" data-notify="url"></a></div>'
		};

	String.format = function() {
		var str = arguments[0];
		for (var i = 1; i < arguments.length; i++) {
			str = str.replace(RegExp("\\{" + (i - 1) + "\\}", "gm"), arguments[i]);
		}
		return str;
	};

	function Notify ( element, content, options ) {
		// Setup Content of Notify
		var content = {
			content: {
				message: typeof content == 'object' ? content.message : content,
				title: content.title ? content.title : '',
				icon: content.icon ? content.icon : '',
				url: content.url ? content.url : '#',
				target: content.target ? content.target : '-'
			}
		};

		options = $.extend(true, {}, content, options);
		this.settings = $.extend(true, {}, defaults, options);
		this._defaults = defaults;
		if (this.settings.content.target == "-") {
			this.settings.content.target = this.settings.url_target;
		}
		this.animations = {
			start: 'webkitAnimationStart oanimationstart MSAnimationStart animationstart',
			end: 'webkitAnimationEnd oanimationend MSAnimationEnd animationend'
		}

		if (typeof this.settings.offset == 'number') {
		    this.settings.offset = {
		    	x: this.settings.offset,
		    	y: this.settings.offset
		    };
		}

		this.init();
	};

	$.extend(Notify.prototype, {
		init: function () {
			var self = this;

			this.buildNotify();
			if (this.settings.content.icon) {
				this.setIcon();
			}
			if (this.settings.content.url != "#") {
				this.styleURL();
			}
			this.placement();
			this.bind();

			this.notify = {
				$ele: this.$ele,
				update: function(command, update) {
					var commands = {};
					if (typeof command == "string") {					
						commands[command] = update;
					}else{
						commands = command;
					}
					for (var command in commands) {
						switch (command) {
							case "type":
								this.$ele.removeClass('alert-' + self.settings.type);
								this.$ele.find('[data-notify="progressbar"] > .progress-bar').removeClass('progress-bar-' + self.settings.type);
								self.settings.type = commands[command];
								this.$ele.addClass('alert-' + commands[command]).find('[data-notify="progressbar"] > .progress-bar').addClass('progress-bar-' + commands[command]);
								break;
							case "icon":
								var $icon = this.$ele.find('[data-notify="icon"]');
								if (self.settings.icon_type.toLowerCase() == 'class') {
									$icon.removeClass(self.settings.content.icon).addClass(commands[command]);
								}else{
									if (!$icon.is('img')) {
										$icon.find('img');
									}
									$icon.attr('src', commands[command]);
								}
								break;
							case "progress":
								var newDelay = self.settings.delay - (self.settings.delay * (commands[command] / 100));
								this.$ele.data('notify-delay', newDelay);
								this.$ele.find('[data-notify="progressbar"] > div').attr('aria-valuenow', commands[command]).css('width', commands[command] + '%');
								break;
							case "url":
								this.$ele.find('[data-notify="url"]').attr('href', commands[command]);
								break;
							case "target":
								this.$ele.find('[data-notify="url"]').attr('target', commands[command]);
								break;
							default:
								this.$ele.find('[data-notify="' + command +'"]').html(commands[command]);
						};
					}
					var posX = this.$ele.outerHeight() + parseInt(self.settings.spacing) + parseInt(self.settings.offset.y);
					self.reposition(posX);
				},
				close: function() {
					self.close();
				}
			};
		},
		buildNotify: function () {
			var content = this.settings.content;
			this.$ele = $(String.format(this.settings.template, this.settings.type, content.title, content.message, content.url, content.target));
			this.$ele.attr('data-notify-position', this.settings.placement.from + '-' + this.settings.placement.align);		
			if (!this.settings.allow_dismiss) {
				this.$ele.find('[data-notify="dismiss"]').css('display', 'none');
			}
			if ((this.settings.delay <= 0 && !this.settings.showProgressbar) || !this.settings.showProgressbar) {
				this.$ele.find('[data-notify="progressbar"]').remove();
			}
		},
		setIcon: function() {
			if (this.settings.icon_type.toLowerCase() == 'class') {
				this.$ele.find('[data-notify="icon"]').addClass(this.settings.content.icon);
			}else{
				if (this.$ele.find('[data-notify="icon"]').is('img')) {
					this.$ele.find('[data-notify="icon"]').attr('src', this.settings.content.icon);
				}else{
					this.$ele.find('[data-notify="icon"]').append('<img src="'+this.settings.content.icon+'" alt="Notify Icon" />');
				}	
			}
		},
		styleURL: function() {
			this.$ele.find('[data-notify="url"]').css({
				backgroundImage: 'url(data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7)',
				height: '100%',
				left: '0px',
				position: 'absolute',
				top: '0px',
				width: '100%',
				zIndex: this.settings.z_index + 1
			});
			this.$ele.find('[data-notify="dismiss"]').css({
				position: 'absolute',
				right: '10px',
				top: '5px',
				zIndex: this.settings.z_index + 2
			});
		},
		placement: function() {
			var self = this,
				offsetAmt = this.settings.offset.y,
				css = {
					display: 'inline-block',
					margin: '0px auto',
					position: this.settings.position ?  this.settings.position : (this.settings.element === 'body' ? 'fixed' : 'absolute'),
					transition: 'all .5s ease-in-out',
					zIndex: this.settings.z_index
				},
				hasAnimation = false,
				settings = this.settings;

			$('[data-notify-position="' + this.settings.placement.from + '-' + this.settings.placement.align + '"]:not([data-closing="true"])').each(function() {
				return offsetAmt = Math.max(offsetAmt, parseInt($(this).css(settings.placement.from)) +  parseInt($(this).outerHeight()) +  parseInt(settings.spacing));
			});
			if (this.settings.newest_on_top == true) {
				offsetAmt = this.settings.offset.y;
			}
			css[this.settings.placement.from] = offsetAmt+'px';

			switch (this.settings.placement.align) {
				case "left":
				case "right":
					css[this.settings.placement.align] = this.settings.offset.x+'px';
					break;
				case "center":
					css.left = 0;
					css.right = 0;
					break;
			}
			this.$ele.css(css).addClass(this.settings.animate.enter);
			$.each(Array('webkit', 'moz', 'o', 'ms', ''), function(index, prefix) {
				self.$ele[0].style[prefix+'AnimationIterationCount'] = 1;
			});

			$(this.settings.element).append(this.$ele);

			if (this.settings.newest_on_top == true) {
				offsetAmt = (parseInt(offsetAmt)+parseInt(this.settings.spacing)) + this.$ele.outerHeight();
				this.reposition(offsetAmt);
			}
			
			if ($.isFunction(self.settings.onShow)) {
				self.settings.onShow.call(this.$ele);
			}

			this.$ele.one(this.animations.start, function(event) {
				hasAnimation = true;
			}).one(this.animations.end, function(event) {
				if ($.isFunction(self.settings.onShown)) {
					self.settings.onShown.call(this);
				}
			});

			setTimeout(function() {
				if (!hasAnimation) {
					if ($.isFunction(self.settings.onShown)) {
						self.settings.onShown.call(this);
					}
				}
			}, 600);
		},
		bind: function() {
			var self = this;

			this.$ele.find('[data-notify="dismiss"]').on('click', function() {		
				self.close();
			})

			this.$ele.mouseover(function(e) {
				$(this).data('data-hover', "true");
			}).mouseout(function(e) {
				$(this).data('data-hover', "false");
			});
			this.$ele.data('data-hover', "false");

			if (this.settings.delay > 0) {
				self.$ele.data('notify-delay', self.settings.delay);
				var timer = setInterval(function() {
					var delay = parseInt(self.$ele.data('notify-delay')) - self.settings.timer;
					if ((self.$ele.data('data-hover') === 'false' && self.settings.mouse_over == "pause") || self.settings.mouse_over != "pause") {
						var percent = ((self.settings.delay - delay) / self.settings.delay) * 100;
						self.$ele.data('notify-delay', delay);
						self.$ele.find('[data-notify="progressbar"] > div').attr('aria-valuenow', percent).css('width', percent + '%');
					}
					if (delay <= -(self.settings.timer)) {
						clearInterval(timer);
						self.close();
					}
				}, self.settings.timer);
			}
		},
		close: function() {
			var self = this,
				$successors = null,
				posX = parseInt(this.$ele.css(this.settings.placement.from)),
				hasAnimation = false;

			this.$ele.data('closing', 'true').addClass(this.settings.animate.exit);
			self.reposition(posX);			
			
			if ($.isFunction(self.settings.onClose)) {
				self.settings.onClose.call(this.$ele);
			}

			this.$ele.one(this.animations.start, function(event) {
				hasAnimation = true;
			}).one(this.animations.end, function(event) {
				$(this).remove();
				if ($.isFunction(self.settings.onClosed)) {
					self.settings.onClosed.call(this);
				}
			});

			setTimeout(function() {
				if (!hasAnimation) {
					self.$ele.remove();
					if (self.settings.onClosed) {
						self.settings.onClosed(self.$ele);
					}
				}
			}, 600);
		},
		reposition: function(posX) {
			var self = this,
				notifies = '[data-notify-position="' + this.settings.placement.from + '-' + this.settings.placement.align + '"]:not([data-closing="true"])',
				$elements = this.$ele.nextAll(notifies);
			if (this.settings.newest_on_top == true) {
				$elements = this.$ele.prevAll(notifies);
			}
			$elements.each(function() {
				$(this).css(self.settings.placement.from, posX);
				posX = (parseInt(posX)+parseInt(self.settings.spacing)) + $(this).outerHeight();
			});
		}
	});

	$.notify = function ( content, options ) {
		var plugin = new Notify( this, content, options );
		return plugin.notify;
	};
	$.notifyDefaults = function( options ) {
		defaults = $.extend(true, {}, defaults, options);
		return defaults;
	};
	$.notifyClose = function( command ) {
		if (typeof command === "undefined" || command == "all") {
			$('[data-notify]').find('[data-notify="dismiss"]').trigger('click');
		}else{
			$('[data-notify-position="'+command+'"]').find('[data-notify="dismiss"]').trigger('click');
		}
	};

}));