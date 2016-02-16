<%-- For creating a route selector parameter via a jsp include. 
     User can select a single route (not all routes).
     Reads in routes via API for the agency specified by the "a" param. --%>

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
	        // Put in default value of Select Route but need to use
	        // an id of ' ' instead of '' since otherwise select2
	        // version 4.0.0 uses the text name as the id, which is wrong!
	 		var selectorData = [];
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
      	title="Select which route you want data for. " ></select>
    </div>
    
