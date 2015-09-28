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
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/



var StudyViewWindowEvents = (function(){
    
    var chartsTabHeaderTopInitialized = false,
        chartsTabHeaderTop = 0,
        
        //Whether the is scrolled, and here is specific to detect whether
        //the header of Charts Tab is on the page top or not.
        //Will be used outside function
        scrolled = false,
        shiftKeyDown = false;
        
    function initEvents(){
        initScrollEvent();
        listenShiftKeyDown();
    }
    
    function listenShiftKeyDown() {
        $(window).on("keydown", function(event) {
            if (event.keyCode === 16){
                shiftKeyDown = true;
                shiftKeyDownEvent = event;
                console.log("shift key down");
            }
        });
        $(window).on("keyup", function(event) {
            if (event.keyCode === 16){
                shiftKeyDown = false;
                shiftKeyDownEvent = null;
                console.log("shift key up");
            }
        });
    }

    function setShiftDown(){
        shiftKeyDown = true;
        shiftKeyDownEvent = "breadcrumb";
        console.log("down");
    }

    function setShiftUp(){
        shiftKeyDown = false;
        shiftKeyDownEvent = null;
        console.log("up");
    }

    //
    //function setShiftKey(){
    //    if(shiftKeyDownEvent!=="breadcrumb") {
    //        origKeyDownEvent = shiftKeyDownEvent;
    //        origShiftKeyDown = shiftKeyDown;
    //        shiftKeyDownEvent = "breadcrumb";
    //        shiftKeyDown = true;
    //    }
    //    else{
    //        shiftKeyDown = origShiftKeyDown;
    //        shiftKeyDownEvent = origKeyDownEvent
    //    }
    //}

    function initScrollEvent(){
        /*
        $(window).scroll(function(e){
            
            //To get offset position of charts tab header, and only initial once
            if(!chartsTabHeaderTopInitialized){
                chartsTabHeaderTop = $("#study-view-header-function").offset().top;
                chartsTabHeaderTopInitialized = true;
            }
            
            if ($(this).scrollTop() > chartsTabHeaderTop){
                //Use transform to move header
                var _transformY = Number($(this).scrollTop()) - chartsTabHeaderTop;
                
                scrolled = true;
                
                $('#study-view-header-function').css({
                    'left': '-13px',
                    'width': '1276px',
                    'z-index': '99',
                    'background-color': '#2986e2',
                    'transform': 'translate(0,' + _transformY + 'px)',
                    '-webkit-transform': 'translate(0,' + _transformY + 'px)',
                    '-ms-transform': 'translate(0,' + _transformY + 'px)'
                });
                $('.study-view-header').css({
                    'border-width': '0',
                    'border-radius': '0',
                    'color': 'white',
                    'background-color': '#2986e2'
                });
                
                $('#study-view-header-left-3').css({
                    'left': '0',
                    'top': '30px',
                    'color': 'white',
                    'background-color': '#2986e2',
                    'opacity': '0.8'
                });
                
                $('#study-view-tutorial').css('display', 'none');
                $('#study-view-add-chart').css('display', 'none');
            }
            if ($(this).scrollTop() < chartsTabHeaderTop){
                scrolled = false;
                
                $('#study-view-header-function').css({
                    'top': '60px',
                    'left': '',
                    'z-index': '',
                    'background-color': 'white',
                    'width': '1200px',
                    'transform': ''
                }); 
                $('.study-view-header').css({
                    'border-width': '1px',
                    'border-radius': '5px',
                    'color': '#2986e2',
                    'background-color': 'white'
                });
                if($("#study-view-header-left-1").css('display') === 'none'){
                    $('#study-view-header-left-3').css({
                        'left': '170px'
                    });
                }else{
                     $('#study-view-header-left-3').css({
                        'left': '410px'
                    });
                }
                $('#study-view-header-left-3').css({
                    'top': '2px',
                    'color': '#2986e2',
                    'background-color': 'white',
                    'opacity': '1'
                });
                
                $('#study-view-tutorial').css('display', 'block');
                $('#study-view-add-chart').css('display', 'block');
            }
        });
        */
    }
    
    return {
        init: initEvents,
      
        getScrollStatus: function() {
            return scrolled;
        },
        
        getShiftKeyDown: function() {
            return shiftKeyDown;
        },
        setShiftUp: setShiftUp,
        setShiftDown: setShiftDown
    };
})();

