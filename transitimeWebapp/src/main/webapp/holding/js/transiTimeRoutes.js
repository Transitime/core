function getRouteDetails(host, processData)
{
    console.log("Calling getRoutes.");
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/routes";
                
    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) 
        {                                              
            var routeDetails = JSON.parse(this.responseText);
            processData(routeDetails);                                                                                       
        }
    };                                                             
    xmlhttp.open("GET", baseurl, true);
    xmlhttp.send();      
};