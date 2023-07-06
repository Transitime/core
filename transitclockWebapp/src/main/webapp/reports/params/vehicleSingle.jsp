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
        // Generate list of routes for the selector.
        // Put in default value of Select Route but need to use
        // an id of ' ' instead of '' since otherwise select2
        // version 4.0.0 uses the text name as the id, which is wrong!
        var selectorData = [];
        for (var id in vehicles.ids) {
            var vehicleId = vehicles.ids[id];
            selectorData.push({id: vehicleId, text: vehicleId});
        }

        // Configure the selector to be a select2 one that has
        // search capability
        $("#vehicle").select2({
            placeholder: "Select Vehicle Id",
            data: selectorData
        })
            // Need to reset tooltip after selector is used. Sheesh!
            .on("select2:select", function(e) {
                var configuredTitle = $( "#vehicle" ).attr("title");
                $( "#select2-vehicle-container" ).tooltip({ content: configuredTitle,
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

<div class="row param">
  <div class="col-sm-5 label">Vehicle:</div>
  <div class="col-sm-7">
    <select type="text" id="vehicle" class="form-control time-input time-input-box"
            name="v" title="Select which vehicle you want data for. " ></select>
  </div>
</div>
