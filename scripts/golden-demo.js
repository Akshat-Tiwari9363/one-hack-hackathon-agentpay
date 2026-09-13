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

async function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function runGoldenDemo() {
  console.log('============================================================');
  console.log('STARTING AGENTPAY GOLDEN HACKATHON DEMO EXECUTION');
  console.log('============================================================\n');

  // RESET
  console.log('[RESET] Sending POST /api/demo/reset ...');
  const resetRes = await post('/api/demo/reset');
  console.log('Reset response:', resetRes.status, JSON.stringify(resetRes.data));

  // Verify system status
  const statusRes = await get('/api/system/status');
  console.log('System Status:', JSON.stringify(statusRes.data, null, 2));

  // STEP 1 — HTTP 402
  console.log('\n[STEP 1 — HTTP 402 CHALLENGE] Requesting svc-trans-a resource...');
  const challengeRes = await get('/api/services/svc-trans-a/resource');
  console.log('Challenge status:', challengeRes.status);
  console.log('Challenge body:', JSON.stringify(challengeRes.data, null, 2));

  // STEP 2 — REAL PAYMENT
  console.log('\n[STEP 2 — REAL SEPOLIA ₹300 PAYMENT] Executing POST /api/demo/purchase ...');
  const purchaseStart = Date.now();
  const purchaseRes = await post('/api/demo/purchase');
  const purchaseDuration = ((Date.now() - purchaseStart) / 1000).toFixed(1);
  console.log(`Purchase confirmed in ${purchaseDuration}s:`, purchaseRes.status);
  console.log('Purchase details:', JSON.stringify(purchaseRes.data, null, 2));

  const txHash = purchaseRes.data.transactionHash;
  const receiptId = purchaseRes.data.receiptId;

  // STEP 3 — REPLAY IDEMPOTENCY
  console.log('\n[STEP 3 — REPLAY IDEMPOTENCY] Replaying exact same request via POST /api/demo/retry ...');
  const retryRes = await post('/api/demo/retry');
  console.log('Retry response:', retryRes.status);
  console.log('Retry details:', JSON.stringify(retryRes.data, null, 2));

  // STEP 4 — OVERSPEND ATTACK
  console.log('\n[STEP 4 — OVERSPEND ATTACK] Attempting ₹800 overspend against ₹700 remaining...');
  const overspendRes = await post('/api/demo/overspend');
  console.log('Overspend response:', overspendRes.status);
  console.log('Overspend rejection details:', JSON.stringify(overspendRes.data, null, 2));

  // STEP 5 — RECEIPT VERIFICATION
  console.log(`\n[STEP 5 — RECEIPT VERIFICATION] Verifying legitimate receipt ${receiptId}...`);
  const verifyLegRes = await post(`/api/receipts/${receiptId}/verify`, {});
  console.log('Legitimate verification:', verifyLegRes.status, JSON.stringify(verifyLegRes.data, null, 2));

  // STEP 6 — TAMPER TEST
  console.log(`\n[STEP 6 — TAMPER TEST] Verifying tampered content on ${receiptId}...`);
  const verifyTamperRes = await post(`/api/receipts/${receiptId}/verify`, {
    contentOverride: 'MALICIOUS_TAMPERED_CONTENT_TEST'
  });
  console.log('Tamper verification:', verifyTamperRes.status, JSON.stringify(verifyTamperRes.data, null, 2));

  // STEP 7 — RESTORATION
  console.log(`\n[STEP 7 — RESTORATION] Re-verifying original receipt ${receiptId}...`);
  const restoreRes = await post(`/api/receipts/${receiptId}/verify`, {});
  console.log('Restored verification:', restoreRes.status, JSON.stringify(restoreRes.data, null, 2));

  // STEP 8 — FINAL AUDIT & WALLET SUMMARY
  console.log('\n[STEP 8 — FINAL SUMMARY]');
  const latestRes = await get('/api/purchases/latest');
  console.log('Latest purchase:', JSON.stringify(latestRes.data, null, 2));

  const receiptsRes = await get('/api/receipts');
  console.log('Persistent receipts in DB:', JSON.stringify(receiptsRes.data, null, 2));

  const auditsRes = await get('/api/audit');
  console.log(`Total Audit Trail events recorded: ${auditsRes.data.length}`);
  console.log('Latest 5 events:');
  auditsRes.data.slice(0, 5).forEach((ev, i) => {
    console.log(`  [${i + 1}] ${ev.eventType} (${ev.status}) - ${ev.message} (tx: ${ev.transactionHash || 'none'})`);
  });

  console.log('\n============================================================');
  console.log('GOLDEN HACKATHON DEMO EXECUTION COMPLETE');
  console.log('============================================================');
}

runGoldenDemo().catch(console.error);
