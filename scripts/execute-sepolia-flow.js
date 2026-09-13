const fs = require('fs');
const path = require('path');
const { ethers } = require('ethers');

function loadEnv() {
  const envPath = path.resolve(__dirname, '..', 'backend', '.env');
  const content = fs.readFileSync(envPath, 'utf8');
  const env = {};
  for (const line of content.split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const eqIdx = trimmed.indexOf('=');
    if (eqIdx !== -1) {
      env[trimmed.substring(0, eqIdx).trim()] = trimmed.substring(eqIdx + 1).trim();
    }
  }
  return env;
}

async function runSepoliaFlow() {
  const env = loadEnv();
  const rpcUrl = env.SEPOLIA_RPC_URL;
  const contractAddress = env.CONTRACT_ADDRESS;

  if (!rpcUrl || rpcUrl.includes('YOUR_NEW_API_KEY')) {
    throw new Error('SEPOLIA_RPC_URL is not configured with real Alchemy URL in backend/.env');
  }
  if (!contractAddress || contractAddress.length !== 42 || contractAddress === '0x0000000000000000000000000000000000000000') {
    throw new Error('CONTRACT_ADDRESS is not deployed yet. Run deploy script first.');
  }

  const provider = new ethers.JsonRpcProvider(rpcUrl);
  const abiPath = path.resolve(__dirname, '..', 'blockchain', 'build', 'blockchain_contracts_AgentBudget_sol_AgentBudget.abi');
  const abi = JSON.parse(fs.readFileSync(abiPath, 'utf8'));
  const contract = new ethers.Contract(contractAddress, abi, provider);

  console.log('==================================================');
  console.log('STARTING REAL SEPOLIA VERIFICATION FLOW');
  console.log('Contract Address:', contractAddress);
  console.log('==================================================');

  // STEP 8: Read initial state on-chain
  console.log('\n--- STEP 8: VERIFY INITIAL ON-CHAIN STATE ---');
  let onChainBudget = await contract.getBudget('agent-demo-001');
  let onChainSpent = await contract.getSpent('agent-demo-001');
  let onChainRemaining = await contract.getRemaining('agent-demo-001');
  console.log(`On-chain Initial Budget:    ${onChainBudget} paise (₹${Number(onChainBudget) / 100})`);
  console.log(`On-chain Initial Spent:     ${onChainSpent} paise (₹${Number(onChainSpent) / 100})`);
  console.log(`On-chain Initial Remaining: ${onChainRemaining} paise (₹${Number(onChainRemaining) / 100})`);

  // STEP 9: Real ₹300 Purchase via backend
  console.log('\n--- STEP 9: EXECUTE REAL ₹300 MICRO-PURCHASE ---');
  const purchaseRes = await fetch('http://localhost:8080/api/demo/purchase', { method: 'POST' });
  const purchase = await purchaseRes.json();
  console.log('Payment Response HTTP Status:', purchaseRes.status);
  console.log('Payment Status:', purchase.status, 'PaymentStatus:', purchase.paymentStatus);
  console.log('Transaction Hash:', purchase.transactionHash);
  console.log('Delivered Content:', purchase.deliveredContent);
  console.log('Receipt ID:', purchase.receiptId);

  // STEP 10: Verify On-Chain State after ₹300 purchase
  console.log('\n--- STEP 10: VERIFY ON-CHAIN STATE AFTER ₹300 ---');
  onChainBudget = await contract.getBudget('agent-demo-001');
  onChainSpent = await contract.getSpent('agent-demo-001');
  onChainRemaining = await contract.getRemaining('agent-demo-001');
  console.log(`On-chain Budget:    ${onChainBudget} paise (₹${Number(onChainBudget) / 100})`);
  console.log(`On-chain Spent:     ${onChainSpent} paise (₹${Number(onChainSpent) / 100})`);
  console.log(`On-chain Remaining: ${onChainRemaining} paise (₹${Number(onChainRemaining) / 100})`);

  // STEP 11: Idempotency Retry Test
  console.log('\n--- STEP 11: RETRY / IDEMPOTENCY TEST ---');
  const retryRes = await fetch('http://localhost:8080/api/demo/retry', { method: 'POST' });
  const retryData = await retryRes.json();
  console.log('Retry Response HTTP Status:', retryRes.status);
  console.log('Retry Detected:', retryData.retry);
  console.log('Reused Tx Hash:', retryData.transactionHash);

  const onChainSpentAfterRetry = await contract.getSpent('agent-demo-001');
  console.log(`On-chain Spent After Replay: ${onChainSpentAfterRetry} paise (Expected 30000, NOT 60000)`);

  // STEP 12: Real ₹800 Overspend Test (70,000 remaining)
  console.log('\n--- STEP 12: REAL ₹800 OVERSPEND TEST ---');
  const overspendRes = await fetch('http://localhost:8080/api/demo/overspend', { method: 'POST' });
  const overspendData = await overspendRes.json();
  console.log('Overspend Response HTTP Status:', overspendRes.status);
  console.log('Overspend Error Code:', overspendData.code);
  console.log('Overspend Message:', overspendData.message);
  console.log('Enforcement Layer:', overspendData.enforcementLayer);

  // STEP 13: Verify State After Revert
  console.log('\n--- STEP 13: VERIFY STATE AFTER REVERT ---');
  onChainBudget = await contract.getBudget('agent-demo-001');
  onChainSpent = await contract.getSpent('agent-demo-001');
  onChainRemaining = await contract.getRemaining('agent-demo-001');
  console.log(`Final On-chain Budget:    ${onChainBudget} paise (₹${Number(onChainBudget) / 100})`);
  console.log(`Final On-chain Spent:     ${onChainSpent} paise (₹${Number(onChainSpent) / 100})`);
  console.log(`Final On-chain Remaining: ${onChainRemaining} paise (₹${Number(onChainRemaining) / 100})`);

  // Verify Audit Log
  const auditRes = await fetch('http://localhost:8080/api/audit');
  const auditLogs = await auditRes.json();
  const overspendAudit = auditLogs.find(a => a.eventType === 'OVERSPEND_BLOCKED');
  console.log(`OVERSPEND_BLOCKED in Audit Trail: ${overspendAudit ? 'YES' : 'NO'}`);
  if (overspendAudit) {
    console.log(`Audit Message: ${overspendAudit.message}`);
  }

  return {
    contractAddress,
    purchaseTxHash: purchase.transactionHash,
    spentAfterPurchase: Number(onChainSpent),
    remainingAfterPurchase: Number(onChainRemaining),
    spentAfterRetry: Number(onChainSpentAfterRetry),
    overspendBlocked: overspendRes.status === 422,
    overspendCode: overspendData.code,
    finalSpent: Number(onChainSpent),
    finalRemaining: Number(onChainRemaining),
    auditRecorded: !!overspendAudit
  };
}

if (require.main === module) {
  runSepoliaFlow()
    .then((r) => console.log('\nSepolia Flow Completed Successfully:', JSON.stringify(r, null, 2)))
    .catch((err) => {
      console.error('\nSepolia Flow Execution Failed:', err.message);
      process.exit(1);
    });
}

module.exports = { runSepoliaFlow };
