<%-- For creating a route selector parameter via a jsp include.
     User can select no routes (r param then set to " ") or a single 
     route (but not an arbitrary multiple of routes). For when 
     selecting a route is optional.
     Select is created by reading in routes via API for the agency 
     specified by the "a" param. --%>

<style type="text/css">
/* Set font for route selector. Need to use #select2-drop because of 
 * extra elements that select2 adds 
 */
#select2-drop, #routesDiv {
  font-family: sans-serif; font-size: large;
}
</style>

<script>

$.getJSON(apiUrlPrefix + "/command/routes", 
 		function(routes) {
	        // Generate list of routes for the selector.
	        // For selector2 version 4.0 now can't set id to empty
	        // string because then it returns the text 'All Routes'.
	        // So need to use a blank string that can be determined
	        // to be empty when trimmed.
	 		var selectorData = [{id: ' ', text: 'Optionally Select Route'}];
	 		for (var i in routes.routes) {
	 			var route = routes.routes[i];
	 			selectorData.push({id: route.shortName, text: route.name})
	 		}
	 		
	 		// Configure the selector to be a select2 one that has
	 		// search capability
 			$("#route").select2({
 				data : selectorData})
 			// Need to reset tooltip after selector is used. Sheesh!
 			.on("select2:select", function(e) {
 			 		var configuredTitle = $( "#route" ).attr("title");
 			 		$( "#select2-route-container" ).tooltip({ content: configuredTitle,
 			 				position: { my: "left+10 center", at: "right center" } });
 			});

	 		
	 		// Tooltips for a select2 widget are rather broken. So get
	 		// the tooltip title attribute from the original route element
	 		// and set the tooltip for the newly created element.
	 		var configuredTitle = $( "#route" ).attr("title");
	 		$( "#select2-route-container" ).tooltip({ content: configuredTitle,
	 				position: { my: "left+10 center", at: "right center" } });
 	});
 	
</script>

    <div id="routesDiv"  class="param">
      <label for="route">Route:</label>
      <select id="route" name="r" style="width: 380px" 
      	title="For when you want to optionally display information about a route."></select>
    </div>
    
