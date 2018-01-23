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
 				data : selectorData})
 			// Need to reset tooltip after selector is used. Sheesh!
 			.on("select2:select", function(e) {
 				var configuredTitle = $( "#block" ).attr("title");
 				$( "#select2-block-container" ).tooltip({ content: configuredTitle,
 						position: { my: "left+10 center", at: "right center" } });
 			});

	 		
	 		// Tooltips for a select2 widget are rather broken. So get
	 		// the tooltip title attribute from the original route element
	 		// and set the tooltip for the newly created element.
	 		var configuredTitle = $( "#block" ).attr("title");
	 		$( "#select2-block-container" ).tooltip({ content: configuredTitle,
	 				position: { my: "left+10 center", at: "right center" } });
 	});
 	
</script>

    <div id="blocksDiv"  class="param">
      <label for="block">Block:</label>
      <select id="block" name="b" style="width: 300px" 
      	title="Select which block you want data for."></select>
    </div>
    
