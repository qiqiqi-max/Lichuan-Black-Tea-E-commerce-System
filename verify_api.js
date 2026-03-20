const http = require('http');

http.get('http://localhost:8080/api/products', (resp) => {
  let data = '';

  resp.on('data', (chunk) => { data += chunk; });

  resp.on('end', () => {
    console.log("Status Code:", resp.statusCode);
    try {
        const json = JSON.parse(data);
        console.log("Response Structure Keys:", Object.keys(json));
        // Assuming standard Result wrapper
        if (Array.isArray(json)) {
             console.log("Is Array. Length:", json.length);
             if(json.length > 0) console.log("First Item:", json[0].name);
        } else if (json.data && Array.isArray(json.data)) {
             console.log("Wrapped Array. Length:", json.data.length);
             if(json.data.length > 0) console.log("First Item:", json.data[0].name);
        } else {
             console.log("Raw Data:", JSON.stringify(json).substring(0, 200));
        }
    } catch (e) {
        console.log("Response is not JSON:", data.substring(0, 200));
    }
  });

}).on("error", (err) => {
  console.log("Error: " + err.message);
});
