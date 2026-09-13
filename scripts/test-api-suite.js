const http = require('http');

function post(path, data) {
  return new Promise((resolve, reject) => {
    const payload = data ? JSON.stringify(data) : '';
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
    if (payload) req.write(payload);
    req.end();
  });
}

function get(path) {
  return new Promise((resolve, reject) => {
    const req = http.request(
      {
        hostname: 'localhost',
        port: 8080,
        path: path,
        method: 'GET'
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
    req.end();
  });
}

async function run() {
  console.log('=== RUNNING AGENTPAY API VERIFICATION SUITE ===');

  // 1. Get latest receipt
  const receiptsRes = await get('/api/receipts');
  const receipt = receiptsRes.data[0];
  console.log('Receipt fetched:', receipt.receiptId, 'Status:', receipt.verificationStatus);

  // 2. Legitimate verification
  const legRes = await post(`/api/receipts/${receipt.receiptId}/verify`, {});
  console.log('Legitimate verification:', legRes.status, legRes.data.status, 'matches:', legRes.data.hashMatches);

  // 3. Tamper verification
  const tamperRes = await post(`/api/receipts/${receipt.receiptId}/verify`, {
    contentOverride: 'ATTACKER_TAMPERED_CONTENT_XYZ_123'
  });
  console.log('Tamper verification:', tamperRes.status, tamperRes.data.status, 'matches:', tamperRes.data.hashMatches);

  // 4. Legitimate verification AGAIN (must NOT stay FAILED)
  const restoreRes = await post(`/api/receipts/${receipt.receiptId}/verify`, {});
  console.log('Restored verification:', restoreRes.status, restoreRes.data.status, 'matches:', restoreRes.data.hashMatches);

  // 5. Check persistent receipt in DB
  const checkReceipt = await get('/api/receipts');
  console.log('DB receipt status after tamper test:', checkReceipt.data[0].verificationStatus);

  // 6. Check audit trail for tamper event
  const auditsRes = await get('/api/audit');
  const tamperEvent = auditsRes.data.find(e => e.eventType === 'RECEIPT_TAMPER_DETECTED');
  console.log('Audit tamper event recorded:', !!tamperEvent, tamperEvent ? tamperEvent.message : 'NONE');

  console.log('=== API VERIFICATION SUITE COMPLETE ===');
}

run().catch(console.error);
