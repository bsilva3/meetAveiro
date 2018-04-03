function confirmRequest(elemName, topic, methodType) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/requests/manage', true);

    //Send the proper header information along with the request
    xhr.setRequestHeader("Content-type", "application/json");

    xhr.onreadystatechange = function() {//Call a function when the state changes.
        if(xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
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
