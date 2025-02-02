function confirmRequest(elemName, topic, methodType) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/requests/manage', true);

    //Send the proper header information along with the request
    xhr.setRequestHeader("Content-type", "application/json");

    xhr.onreadystatechange = function() {//Call a function when the state changes.
        if(xhr.readyState == XMLHttpRequest.DONE) {
            console.log("Done");
            location.reload(true);
        }
    }
    xhr.send(JSON.stringify({
        filename: elemName,
        path: topic,
        operation: methodType
    })); 
}


function changeRequest(elemName, topic) {
    var e = document.getElementById(elemName);
    var concept = e.options[e.selectedIndex].value;
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/requests/change', true);

    //Send the proper header information along with the request
    xhr.setRequestHeader("Content-type", "application/json");

    xhr.onreadystatechange = function() {//Call a function when the state changes.
        if(xhr.readyState == XMLHttpRequest.DONE) {
            console.log("Done");
            location.reload(true);
        }
    }
    xhr.send(JSON.stringify({
        filename: elemName,
        path: concept,
        old: topic
    })); 
}
