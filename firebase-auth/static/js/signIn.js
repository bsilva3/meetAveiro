function signIn() {
    var email = document.getElementById('email');
    var passwd = document.getElementById('password'); 
    console.log(email.value)
    console.log(passwd.value)
    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/signIn', true);

    //Send the proper header information along with the request
    xhr.setRequestHeader("Content-type", "application/json");

    xhr.onload  = function() {
        console.log(xhr.responseText);
        var jsonResponse = JSON.parse(xhr.responseText);
        if (jsonResponse.url === '') {
            alert('Invalid credentials');
        }
        // do something with jsonResponse
        else {
            location.href = jsonResponse.url;
        }
        
     };

    xhr.send(JSON.stringify({
        user: email.value,
        password: passwd.value
    })); 
}