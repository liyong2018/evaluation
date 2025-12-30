const net = require('net');

const client = new net.Socket();
const port = 30314;
const host = '127.0.0.1';

client.connect(port, host, function() {
    console.log('Connected');
});

client.on('data', function(data) {
    console.log('Received: ' + data.length + ' bytes');
    console.log('Hex: ' + data.toString('hex').substring(0, 100));
    console.log('String: ' + data.toString().substring(0, 100));
    client.destroy();
});

client.on('close', function() {
    console.log('Connection closed');
});

client.on('error', function(err) {
    console.error('Error: ' + err.message);
});
