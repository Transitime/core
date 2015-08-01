<%-- For creating a block ID selector parameter via a jsp include.     
     Reads in IDs via API for the agency specified by the "a" param. --%>

<style type="text/css">
/* Set font for selector. Need to use #select2-drop because of 
 * extra elements that select2 adds 
 */
#select2-drop, #routesDiv {
  font-family: sans-serif; font-size: large;
}
</style>

<script>

$.getJSON(apiUrlPrefix + "/command/blockIds", 
 		function(blockIds) {
	        // Generate list of routes for the selector
	 		var selectorData = [];
	 		for (var i in blockIds.ids) {
	 			var blockId = blockIds.ids[i];
	 			selectorData.push({id: blockId, text: blockId})
	 		}
	 		
	 		// Configure the selector to be a select2 one that has
	 		// search capability
 			$("#block").select2({
 				placeholder: "Select Block", 				
 				data : selectorData});
	 		
	 		// Tooltips for a select2 widget don't automatically go away when 
	 		// item selected so remove the tooltip manually. This is a really 
	 		// complicated interaction between select2 and jquery UI tooltips.
	 		// First need to set the tooltip title content but getting the
	 		// originally configured title for the element.
 			var modifiedRouteElement = $( "#s2id_block" );
	 		var configuredTitle = modifiedRouteElement.attr("title");
	 		$( "#s2id_block" ).tooltip({ content: configuredTitle });
 			
	 		// Now that the title has set need to manually remove the tooltip
	 		// when a select2 item is selected. Sheesh!
 		 	$("#block").on("change", function(e) { $("#s2id_block").tooltip("close") }); 		 	
 	});
 	
</script>

    <div id="blocksDiv"  class="param">
      <label for="block">Block:</label>
      <input id="block" name="b" style="width: 300px" 
      	title="Select which block you want data for."/>
    </div>
    
