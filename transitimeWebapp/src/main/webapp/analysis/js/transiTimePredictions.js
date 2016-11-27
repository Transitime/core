 


function getStopPathPredictions(host, tripId, stopPathIndex, algorithm, processData, stopPathData)
{    
                
    var baseurl= "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/getstoppathpredictions?";
                
    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
                    if (this.readyState == 4 && this.status == 200) {                        
                        var predictions = JSON.parse(this.responseText);
                        processData(predictions, stopPathData);   
                    }
    };
    var queryObject = {
        tripId:null,
        stopPathIndex:null,
        algorithm:null,
    
    };
    
    queryObject.tripId=tripId;
    queryObject.stopPathIndex=stopPathIndex;
    queryObject.algorithm=algorithm;
    
                
    var encodedQueryObject = $.param( queryObject );
                               
    xmlhttp.open("GET", baseurl+encodedQueryObject, true);
    xmlhttp.send();                        
    
};

function getStopPathTravelTimes(host, tripId, stopPathIndex, processData , stopPathData)
{                                       
    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/ASC/command/triparrivaldeparturecachedata?";

    var xmlhttp = new XMLHttpRequest();

    xmlhttp.onreadystatechange = function() {

        if (this.readyState == 4 && this.status == 200) {

                                var response = JSON.parse(this.responseText);
                                stopPath = findStopPathDuration(response, $("#stopPathIndex").val());            
                                processData(stopPath, stopPathData);                                                               
                            }
        };

    var queryObject = {
                        tripId:null,
                        stopPathIndex:null,
    
        

    };
    
    queryObject.tripId=tripId;
    queryObject.stopPathIndex=stopPathIndex;
    

    var encodedQueryObject = $.param( queryObject );

    xmlhttp.open("GET", baseurl+encodedQueryObject, true);
    xmlhttp.send();                   
};

function findStopPathDuration(results, stopPathIndex) {
    var result, departure, arrival, duration;
    
    for (var i=0; i<results.arrivalDeparture.length ; i++) {        
                
        var result=results.arrivalDeparture[i];
        
        if (result.stopPathIndex == (parseInt(stopPathIndex)-1).toString() && !result.isArrival ) {
            departure = result;    
        }
        if (result.stopPathIndex == (parseInt(stopPathIndex)+0).toString()  && result.isArrival) {
            arrival = result;        
            if(departure!=null)
                duration = new Date(arrival.time)-new Date(departure.time);
        }
    
    }
    if(departure != null && arrival != null)
    {
        duration = new Date(arrival.time)-new Date(departure.time);
    }
        
    var stopPath = {
        departure:departure,
        arrival:arrival,
        duration:duration
    };        
    return stopPath;
};

function findMAPE(predictions, stopPath)
{    
    var totalDifference = 0;
    for (var i=0; i < predictions.prediction.length; i++) {
        var prediction = predictions.prediction[i];
        totalDifference = totalDifference + Math.abs(parseInt(prediction.predictionTime)-stopPath.duration);            
    }
    return Math.round(((Math.round(totalDifference/predictions.prediction.length))/stopPath.duration)*100);     
};
function getRoutes(baseurl)
{
                   
    var xmlhttp = new XMLHttpRequest();

    xmlhttp.onreadystatechange = function() {

        if (this.readyState == 4 && this.status == 200) {

            var response = JSON.parse(this.responseText);                        
        }
    };
      
    xmlhttp.open("GET", baseurl, true);
    
    xmlhttp.send();                             
}
function getActiveTrips ()
{
    // http://127.0.0.1:8080/api/v1/key/f78a2e9a/agency/1/command/vehiclesDetails?r=1
}