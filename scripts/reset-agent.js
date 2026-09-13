const { ethers } = require('ethers');
const fs = require('fs');
const path = require('path');
const { loadEnv } = require('./verify-sepolia.js');

async function resetAgent() {
  const env = loadEnv();
  const provider = new ethers.JsonRpcProvider(env.SEPOLIA_RPC_URL);
  const formattedKey = env.WEB3_PRIVATE_KEY.startsWith('0x') ? env.WEB3_PRIVATE_KEY : '0x' + env.WEB3_PRIVATE_KEY;
  const wallet = new ethers.Wallet(formattedKey, provider);

  const abiPath = path.resolve(__dirname, '..', 'blockchain', 'build', 'blockchain_contracts_AgentBudget_sol_AgentBudget.abi');
  const abi = JSON.parse(fs.readFileSync(abiPath, 'utf8'));
  const contract = new ethers.Contract(env.CONTRACT_ADDRESS, abi, wallet);

  console.log('Resetting agent-demo-001 on Sepolia with 100,000 paise (₹1,000.00)...');
  const tx = await contract.registerAgent('agent-demo-001', 100000n);
  console.log('Transaction sent:', tx.hash);
  const receipt = await tx.wait(1);
  console.log(`Confirmed in block ${receipt.blockNumber}, gas used: ${receipt.gasUsed}`);

  const readContract = new ethers.Contract(env.CONTRACT_ADDRESS, abi, provider);
  const budget = await readContract.getBudget('agent-demo-001');
  const spent = await readContract.getSpent('agent-demo-001');
  const remaining = await readContract.getRemaining('agent-demo-001');

  console.log('Agent State after reset:');
  console.log(`  Budget:    ${budget} paise (₹${Number(budget) / 100})`);
  console.log(`  Spent:     ${spent} paise (₹${Number(spent) / 100})`);
  console.log(`  Remaining: ${remaining} paise (₹${Number(remaining) / 100})`);

  return {
    txHash: tx.hash,
    blockNumber: receipt.blockNumber,
    budget: Number(budget),
    spent: Number(spent),
    remaining: Number(remaining)
  };
}

if (require.main === module) {
  resetAgent()
    .then(r => console.log('Reset Agent Result:', JSON.stringify(r, null, 2)))
    .catch(err => {
      console.error('Reset Agent Failed:', err);
      process.exit(1);
    });
}

module.exports = { resetAgent };
