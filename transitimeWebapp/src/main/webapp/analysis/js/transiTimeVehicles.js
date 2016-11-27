function getVehicleDetailsByRoute(host, routeId, processData)
{
    console.log("Calling getVehicleDetailsByRoute.");
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/vehiclesDetails";
    if(routeId.length > 0)        
        baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/vehiclesDetails?r="+routeId;
                
    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) 
        {                                              
            var vehicleDetails = JSON.parse(this.responseText);
            processData(vehicleDetails);                                                                                       
        }
    };                                                             
    xmlhttp.open("GET", baseurl, true);
    xmlhttp.send();      
};
function getVehicleDetails(host, vehicleId, processData, stopPathData)
{
    console.log("Calling getVehicleDetails.");
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/vehiclesDetails";
    if(vehicleId.length > 0)            
        baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/vehiclesDetails?v="+vehicleId;
                
    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) 
        {                                              
            var vehicleDetails = JSON.parse(this.responseText);
            processData(vehicleDetails, stopPathData);                                                                                       
        }
    };                                                             
    xmlhttp.open("GET", baseurl, true);
    xmlhttp.send();      
};
function getNextVehicleArrival(host, stopId, processData)
{
    console.log("Calling getNextVehicleArrival.");
    
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/predictions?rs=ASC_4560585|4560595&format=json";
                    
    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) 
        {                                              
            var vehicleDetails = JSON.parse(this.responseText);
            processData(vehicleDetails);                                                                                       
        }
    };                                                             
    xmlhttp.open("GET", baseurl, true);
    xmlhttp.send();   
};
function getCurrentStopPathIndex(stopPathData)
{
    for(var i=0; i< stopPathData.trip.schedule.length; i++)
    {
        if(stopPathData.trip.schedule[i].stopId===stopPathData.vehicle.nextStopId)        
            return i;
    }
    return -1;
};