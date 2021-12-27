<%-- For creating a route selector parameter via a jsp include.
     User can select all routes (r param then set to " ") or any
     number of routes. 
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
    $( document ).ready(function() {
        $("#allStops").
        $("#allStops").click(function(){

            if($(this).prop("checked") == true){
                $("#stopIds").prop('disabled', true);
            }

            else if($(this).prop("checked") == false){
                $("#stopIds").prop('disabled', false);
            }

        });
    });

    $("form").submit(function(event){
        console.log("form submitted");
        if($("#allStops").prop("checked") == false){
            console.log("not checked, copy vals");
            $('#stopIdsHidden').val($('#stopIds').val());
            console.log("done");
        }
        else {
            $('#stopIdsHidden').val('');
        }
    });

</script>

<!-- <fieldset id="stopsFieldSet" > <!-->
    <div class="row">
        <label class="col-sm-12 col-form-label">Search</label>
    </div>
    <div class="form-check">
        <input class="form-check-input" type="checkbox"   id="allStops" checked>
        <label class="form-check-label" for="allStops">
            All Stops
        </label>
    </div>
    <div class="row">
        <label class="col-sm-12 col-form-label">Routes</label>
        <textarea  id="stopIds" rows="2" disabled="true"> </textarea>
    </div>

<input id="stopIdsHidden" type="hidden" name="s"/>
<!--
<div style="padding: 2px;">
      <label for="stopIds">Stops Ids:</label>
      <textarea style="width:350px;" id="stopIds" rows="2" disabled="true"></textarea>
    </div>
    <input id="stopIdsHidden" type="hidden" name="s"/>
</fieldset> <!-->

