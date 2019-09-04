function getTripDetails(host, tripId, processData, stopPathData)
{
    console.log("Calling getVehicleDetailsByRoute.");
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/1/command/trip";
    if(tripId.length > 0)        
        baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/trip?tripId="+tripId;
                
    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) 
        {                                              
            var tripDetails = JSON.parse(this.responseText);
            processData(tripDetails, stopPathData);                                                                                       
        }
    };                                                             
    xmlhttp.open("GET", baseurl, true);
    xmlhttp.send();      
};