<%-- For creating a route selector parameter via a jsp include.
     User can select all routes (r param then set to " ") or a single
     route (but not an arbitrary multiple of routes).
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
    var isDisabled = $(".isAllRoutesDisabled");
    $.getJSON(apiUrlPrefix + "/command/routes",
        function(routes) {
            // Generate list of routes for the selector.
            // For selector2 version 4.0 now can't set id to empty
            // string because then it returns the text 'All Routes'.
            // So need to use a blank string that can be determined
            // to be empty when trimmed.
            var selectorData = [{id: ' ', text: 'All Routes'}];
            if(isDisabled && isDisabled.val() === 'true' || isDisabled.val() === 'triggerFirst'){
                selectorData=[];
            }
            for (var i in routes.routes) {
                var route = routes.routes[i];
                var name = route.shortName + " " + route.longName
                var routeIdentifier = route.shortName ? route.shortName : r.id;
                selectorData.push({id: routeIdentifier, text: name})
            }

            // Configure the selector to be a select2 one that has
            // search capability
            $("#route").select2({
                placeholder: 'Select a Route',
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
            if(isDisabled && isDisabled.val() === 'triggerFirst' && selectorData.length){
               $("#route").val(" ").trigger("change");

            }
            if(isDisabled && isDisabled.val() === 'true' ) {
                $(".select2-search__field").attr("placeholder", "Select a Route");
                $(".select2-search__field").css("width","100px");
                $("#route").val(" ").trigger("change");
            }
        });

</script>

<div class="row">
    <label class="col-sm-12 col-form-label">Route</label>
    <div  class="col-sm-12">
    <select id="route" name="r"  class="form-select"
            title="Select which route you want data for. Note: selecting all routes
      		   indeed reads in data for all routes which means it could be
      		   somewhat slow." ></select>
    </div>
</div>

