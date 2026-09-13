const http = require('http');

function post(path, data) {
  return new Promise((resolve, reject) => {
    const payload = JSON.stringify(data);
    const req = http.request(
      {
        hostname: 'localhost',
        port: 8080,
        path: path,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(payload)
        }
      },
      (res) => {
        let body = '';
        res.on('data', (chunk) => (body += chunk));
        res.on('end', () => {
          try {
            resolve({ status: res.statusCode, data: JSON.parse(body) });
          } catch (e) {
            resolve({ status: res.statusCode, data: body });
          }
        });
      }
    );
    req.on('error', reject);
    req.write(payload);
    req.end();
  });
}

async function run() {
  console.log('Testing concurrent duplicate requests with identical requestId...');
  let testRequestId = 'REQ-NORMAL-324EEE9D';
  try {
    const listRes = await fetch('http://localhost:8080/api/purchases');
    const list = await listRes.json();
    if (list && list.length > 0) testRequestId = list[0].requestId;
  } catch (e) {}
  console.log('Using settled requestId:', testRequestId);
  const promises = [];
  for (let i = 0; i < 5; i++) {
    promises.push(
      post('/api/payments', {
        requestId: testRequestId,
        agentId: 'agent-demo-001',
        providerId: 'prov-a',
        serviceId: 'svc-trans-a'
      })
    );
  }

  const results = await Promise.all(promises);
  console.log('All 5 concurrent requests returned:');
  results.forEach((r, idx) => {
    console.log(`[${idx}] status=${r.status}, retry=${r.data.retry}, txHash=${r.data.transactionHash ? r.data.transactionHash.substring(0, 16) + '...' : 'null'}`);
  });
}

run().catch(console.error);
