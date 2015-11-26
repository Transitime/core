/**
 * This file contains general Javascript utilities for the Transitime system.
 * It is expected that this file will be loaded for many web pages.
 */

/**
 * For getting parameters from query string 
 */
function getQueryVariable(paramName) {
	var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
    	var pair = vars[i].split("=");
        if (pair[0] == paramName){
        	return pair[1];
        }
    }
    
    // Didn't find the specified param so return false
    return false;
}

/**
 * Enable JQuery tooltips. In order to use html in tooltip need to
 * specify content function. Turning off 'focusin' events is important
 * so that tooltip doesn't popup again if previous or next month
 * buttons are clicked in a datepicker.
 */  
$(function() {
	$( document ).tooltip({
		// Content for tooltip comes from title attribute of html object  
        content: function () {
            return $(this).prop('title');
        },
        
        // Position tooltips to right of element so that not covering
        // up element below as happens with default positioning
        position: { my: "left+10 center", at: "right center" }
    }).off('focusin');
});



