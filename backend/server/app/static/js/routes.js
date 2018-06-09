function share_map_2() {
    var e = document.getElementById("routeslist");
    var perc = e.options[e.selectedIndex].value;

    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/resources/routes/user_instances', true);

    //Send the proper header information along with the request
    xhr.setRequestHeader("Content-type", "application/json");

    xhr.onload  = function() {
        console.log(xhr.responseText);
        var jsonResponse = JSON.parse(xhr.responseText);
        // do something with jsonResponse
        location.href = jsonResponse.url;
    };

    xhr.send(JSON.stringify({
        id: perc
    }));
}
