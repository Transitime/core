<%-- For creating a route selector parameter via a jsp include.
     User can select all routes (r param then set to " ") or any
     number of routes. 
     Reads in routes via API for the agency specified by the "a" param. --%>


<script>
    var isDisabled = $(".isAllRoutesDisabled");
    function formatRoute (route) {
        if (!route.id || route.id == " ") {
            if(isDisabled && isDisabled.val() === 'true') {
                return 'All Routes';
            }
            return route.text;
        }
        return route.id;
    };

    $.getJSON(apiUrlPrefix + "/command/routes",
        function(routes) {
            // Generate list of routes for the selector
            // For selector2 version 4.0 now can't set id to empty
            // string because then it returns the text 'All Routes'.
            // So need to use a blank string that can be determined
            // to be empty when trimmed.


            var selectorData = [{id: ' ', text: 'All Routes'}];
            if(isDisabled && isDisabled.val() === 'true'){
                selectorData=[];
            }
            for (var i in routes.routes) {
                var route = routes.routes[i];
                var name = route.shortName + " " + route.longName
                selectorData.push({id: route.shortName, text: name, value: route.shortName, innerHTML:route.shortName})
            }

            // Configure the selector to be a select2 one that has
            // search capability
            $("#route").select2({
                placeholder: "All Routes",
                data : selectorData,
                closeOnSelect: false,
                templateSelection: formatRoute
            })
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

<div  class="col-sm-12">
    <select id="route" name="r" multiple="multiple" class="form-select"
            title="Select which routes you want data for. You can use the Ctrl key along
      		   with the mouse to select multiple routes. Note: selecting all routes
      		   indeed reads in data for all routes which means it could be 
      		   somewhat slow." ></select>
</div>


