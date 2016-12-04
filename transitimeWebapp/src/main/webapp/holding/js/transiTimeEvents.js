function getStopEvents(host, stopId, processData, data)
{                                       
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/stoparrivaldeparturecachedata?";

    var xmlhttp = new XMLHttpRequest();

    xmlhttp.onreadystatechange = function() {

    	if (this.readyState == 4 && this.status == 200) 
    	{
    		var response = JSON.parse(this.responseText);
                        		
    		processData(data, response);                                                               
        }
    };
    
    var queryObject = {
    		stopid:null    		
    };
    
    queryObject.stopid=stopId;
    
   
   
    var encodedQueryObject = $.param( queryObject );

    xmlhttp.open("GET", baseurl+encodedQueryObject, true);
    xmlhttp.send();                   
};
