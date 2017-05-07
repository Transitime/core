function unique(arr) {
    var u = {}, a = [];
    for(var i = 0, l = arr.length; i < l; ++i){
        if(!u.hasOwnProperty(arr[i])) {
            a.push(arr[i]);
            u[arr[i]] = 1;
        }
    }
    return a;
};
function getCurrentServerTime(host, agencyId, processTime, data)
{
    console.log("Calling getCurrentServerTime.");

    var baseurl = "http://"+host+"/api/v1/key/f78a2e9a/agency/"+agencyId+"/command/currentServerTime";

    var xmlhttp = new XMLHttpRequest();
                                
    xmlhttp.onreadystatechange = function() {
                   
        if (this.readyState == 4 && this.status == 200) 
        {                                              
            var currentservertime = JSON.parse(this.responseText);
            processTime(data, currentservertime);                                                                                       
        }
    };                                                             
    xmlhttp.open("GET", baseurl, true);
    xmlhttp.send();   
};