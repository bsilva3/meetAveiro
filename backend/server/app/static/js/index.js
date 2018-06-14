function delConcept() {
    var e = document.getElementById("conceptlist");
    var concept = e.options[e.selectedIndex].value;
    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/resources/topics/manage', true);

    //Send the proper header information along with the request
    xhr.setRequestHeader("Content-type", "application/json");

    xhr.onreadystatechange = function() {//Call a function when the state changes.
        if(xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
            console.log("Done");
            location.reload(true);
        }
    }
    xhr.send(JSON.stringify({
        topic: concept,
        method: 'POST'
    })); 
}

function getConcept() {
    var e = document.getElementById("conceptlist");
    var concept = e.options[e.selectedIndex].value;
    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/resources/topics/manage', true);

    //Send the proper header information along with the request
    xhr.setRequestHeader("Content-type", "application/json");

    xhr.onload  = function() {
        var jsonResponse = JSON.parse(xhr.responseText);
        // do something with jsonResponse
        location.href = jsonResponse.url;
     };

    xhr.send(JSON.stringify({
        topic: concept,
        method: 'GET'
    })); 
}

function retrain() {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/resources/retrain', true);

    //Send the proper header information along with the request
    xhr.setRequestHeader("Content-type", "application/json");

    xhr.onreadystatechange = function() {//Call a function when the state changes.
        if(xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
            console.log("Done");
            location.reload(true);
        }
    }

    xhr.send(JSON.stringify({
        method: 'retrain'
    })); 
}

