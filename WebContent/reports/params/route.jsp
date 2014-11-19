<%-- For creating a route selector parameter via a jsp include. 
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
var urlPrefix = "/api/v1/key/TEST/agency/<%= request.getParameter("a") %>";

$.getJSON(urlPrefix + "/command/routes", 
 		function(routes) {
	        // Generate list of routes for the selector
	 		var selectorData = [{id: '', text: 'All Routes'}];
	 		for (var i in routes.route) {
	 			var route = routes.route[i];
	 			selectorData.push({id: route.id, text: route.name})
	 		}
	 		
	 		// Configure the selector to be a select2 one that has
	 		// search capability
 			$("#route").select2({
 				placeholder: "All Routes", 				
 				data : selectorData});
 	});
</script>

    <div id="routesDiv"  class="param">
      <label for="route">Route:</label>
      <input id="route" name="r" style="width: 300px" />
    </div>
    
