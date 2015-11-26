<%-- For creating a vehicle selector parameter via a jsp include. 
     User can select all vehicles (v set to " ") OR a single vehicle.
     Reads in routes via API for the agency specified by the "a" param. --%>

<style type="text/css">
/* Set font for vehicle selector. Need to use #select2-drop because of 
 * extra elements that select2 adds 
 */
#select2-drop, #vehicleDiv {
  font-family: sans-serif; font-size: large;
}
</style>

<script>

$.getJSON(apiUrlPrefix + "/command/vehicleIds", 
 		function(vehicles) {
	        // Generate list of routes for the selector
	 		var selectorData = [{id: ' ', text: 'All Vehicles'}];
	 		for (var i in vehicles.ids) {
	 			var id = vehicles.ids[i];
	 			selectorData.push({id: id, text: id})
	 		}
	 		
	 		// Configure the selector to be a select2 one that has
	 		// search capability
 			$("#vehicle").select2({
 				/* placeholder: "Select VehicleYYY", */ 				
 				data : selectorData})
 			// Need to reset tooltip after selector is used. Sheesh!
 			.on("select2:select", function(e) {
 				var configuredTitle = $( "#vehicle" ).attr("title");
 			 	$( "#select2-route-container" ).tooltip({ content: configuredTitle,
 			 			position: { my: "left+10 center", at: "right center" } });
 			});

	 		// Tooltips for a select2 widget are rather broken. So get
	 		// the tooltip title attribute from the original route element
	 		// and set the tooltip for the newly created element.
	 		var configuredTitle = $( "#vehicle" ).attr("title");
	 		$( "#select2-vehicle-container" ).tooltip({ content: configuredTitle,
	 				position: { my: "left+10 center", at: "right center" } });
 	});
 	
</script>

    <div id="vehicleDiv"  class="param">
      <label for="vehicle">Vehicle:</label>
      <select id="vehicle" name="v" style="width: 200px" 
      	title="Select which vehicle you want data for. "></select>
    </div>
    
