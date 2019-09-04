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
function getVehicleDetails(host, agencyId, vehicleId, processData, data)
{
    console.log("Calling getVehicleDetails.");
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/"+agencyId+"/command/vehiclesDetails";
    if(vehicleId.length > 0)            
        baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/"+agencyId+"/command/vehiclesDetails?v="+vehicleId;
                
    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) 
        {                                              
            var vehicleDetails = JSON.parse(this.responseText);
            processData(data, vehicleDetails);                                                                                       
        }
    };                                                             
    xmlhttp.open("GET", baseurl, true);
    xmlhttp.send();      
};
function getNextVehicleArrivalPredictions(host, agencyId, routeId, stopId, processData, data)
{
    console.log("Calling getNextVehicleArrivalPredictions.");

    //http://ec2-54-187-31-47.us-west-2.compute.amazonaws.com:8080/api/v1/key/f78a2e9a/agency/ASC/command/predictions?format=json&rs=ASC_4560585%7C4560595
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/"+agencyId+"/command/predictions?format=json&rs="+routeId+"%7C"+stopId;

    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) 
        {                                              
            var vehicleDetails = JSON.parse(this.responseText);
            processData(vehicleDetails, data);                                                                                       
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