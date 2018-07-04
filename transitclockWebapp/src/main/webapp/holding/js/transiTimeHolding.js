function alertHolding(paramTest) {
    alert(paramTest.tosay);    
};
function getHoldingTime(host, agencyId, stopId, vehicleId, processHoldingTime, data) 
{                               
    console.log("Calling getHoldingTime.");
    
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/"+agencyId+"/command/getholdingtime?";
                
    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) {

            var holdingTime = JSON.parse(this.responseText);

            $("#result").text(this.responseText);        
           
            processHoldingTime(data, vehicleId, holdingTime);
        }
    };
                
    var queryObject = {
        stopId:null,
        vehicleId:null                    
    };
    queryObject.stopId=stopId;
    queryObject.vehicleId=vehicleId;
                
    var encodedQueryObject = $.param( queryObject );
                               
    xmlhttp.open("GET", baseurl+encodedQueryObject, true);
    xmlhttp.send();                             
    
};
function getHoldingTimeKeys(host, agencyId, processHoldingTimeKeys, data)
{
    console.log("Calling getHoldingTimeKeys.");
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/"+agencyId+"/command/holdingtimecachekeys";
                
    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) 
        {                        
            var response = JSON.parse(this.responseText);
                        
            $("#holdingtimes").text(this.responseText);
            
            processHoldingTimeKeys(data, response);                                                
        }
    };                                                             
    xmlhttp.open("GET", baseurl, true);
    xmlhttp.send();      
}