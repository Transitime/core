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

<fieldset id="stopsFieldSet" >
    <legend >Stops</legend>
    <div id="stopsDiv"  class="param"  >
       <label for="allStops">All Stops:</label>
       <input id="allStops" type="checkbox" checked />
    </div>
    <div style="padding: 2px;">
      <label for="stopIds">Stops Ids:</label>
      <textarea style="width:500px;" id="stopIds" rows="7" disabled="true">34,42,170,811,1202,1235,1246,1330,1905,1911,1920,3051,3142,6767,8916,9086,11146,11155,11161,11180,11834,11837,11838,11850,11858,11859,11861,11862,11863,11893,12927,13140,13143,13207,13209,13211,13223,13312,14850,15614,15664,16086,16098,16112,16839,16845,16851,16882,16884,16894,16900,17195,17366,17859,17860,17881,17882,17883,17884,17887,17897,17900,17901,17902,17903,17905,17940,17942,17976,17978,17980,17982,17992,17994,17996,17998,18523,19260,19329,40213,40954,42319,43022,43275,48302,49423,49441,49881,50015,50154,50157,50160,50190,50195,50196,51114,51442,51533,51541,51544,51547,51846,52015,52261,52262,52300,52531,52532,52857,53142,53222,53289,53292,53293,53294,53295,53302,53303,53304,53305,53309,53312,53313,53314,53317,53318,53319,53320,53321,53353,53435,53537,53569,53703,53704,53788,53803,56100,56115,56117,56122,56137,56293,56432,56494,56697,56698,56725,56826,56873</textarea>
    </div>
    <input id="stopIdsHidden" type="hidden" name="s"/>
</fieldset>

    
