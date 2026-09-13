const { ethers } = require('ethers');
const fs = require('fs');
const path = require('path');
const { loadEnv } = require('./verify-sepolia.js');

async function main() {
  const env = loadEnv();
  const provider = new ethers.JsonRpcProvider(env.SEPOLIA_RPC_URL);
  const abiPath = path.resolve(__dirname, '..', 'blockchain', 'build', 'blockchain_contracts_AgentBudget_sol_AgentBudget.abi');
  const abi = JSON.parse(fs.readFileSync(abiPath, 'utf8'));
  const contract = new ethers.Contract(env.CONTRACT_ADDRESS, abi, provider);

  const budget = await contract.getBudget('agent-demo-001');
  const spent = await contract.getSpent('agent-demo-001');
  const remaining = await contract.getRemaining('agent-demo-001');
  console.log('ON-CHAIN STATE:', JSON.stringify({
    budget: budget.toString(),
    spent: spent.toString(),
    remaining: remaining.toString()
  }, null, 2));
}
main().catch(console.error);
