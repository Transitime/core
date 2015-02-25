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
        content: function () {
            return $(this).prop('title');
        }
    }).off('focusin');
});


// This needs to match the API key in the database
var apiKey = "5ec0de94";
var apiUrlPrefix = "/api/v1/key/" + apiKey + "/agency/" + getQueryVariable("a");

