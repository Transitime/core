<%-- For creating a vehicle selector parameter via a jsp include. 
     User can select a single vehicle (not all vehicles).
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
	 		var selectorData = [];
	 		for (var i in vehicles.ids) {
	 			var id = vehicles.ids[i];
	 			selectorData.push({id: id, text: id})
	 		}
	 		
	 		// Configure the selector to be a select2 one that has
	 		// search capability
 			$("#vehicle").select2({
 				placeholder: "Select Vehicle", 				
 				data : selectorData});
	 		
	 		// Tooltips for a select2 widget don't automatically go away when 
	 		// item selected so remove the tooltip manually. This is a really 
	 		// complicated interaction between select2 and jquery UI tooltips.
	 		// First need to set the tooltip title content but getting the
	 		// originally configured title for the element.
 			var modifiedRouteElement = $( "#s2id_vehicle" );
	 		var configuredTitle = modifiedRouteElement.attr("title");
	 		$( "#s2id_vehicle" ).tooltip({ content: configuredTitle });
 			
	 		// Now that the title has set need to manually remove the tooltip
	 		// when a select2 item is selected. Sheesh!
 		 	$("#vehicle").on("change", function(e) { $("#s2id_vehicle").tooltip("close") }); 		 	
 	});
 	
</script>

    <div id="vehicleDiv"  class="param">
      <label for="vehicle">Vehicle:</label>
      <input id="vehicle" name="v" style="width: 200px" 
      	title="Select which vehicle you want data for. "/>
    </div>
    
