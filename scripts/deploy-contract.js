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
  return { env, envPath, rawContent: content };
}

function updateEnvContractAddress(envPath, rawContent, newAddress) {
  let updated;
  if (rawContent.includes('CONTRACT_ADDRESS=')) {
    updated = rawContent.replace(/CONTRACT_ADDRESS=.*(\r?\n|$)/, `CONTRACT_ADDRESS=${newAddress}$1`);
  } else {
    updated = rawContent + `\nCONTRACT_ADDRESS=${newAddress}\n`;
  }
  fs.writeFileSync(envPath, updated, 'utf8');
  console.log(`Updated ${envPath} with CONTRACT_ADDRESS=${newAddress}`);
}

async function deploy() {
  const { env, envPath, rawContent } = loadEnv();
  const rpcUrl = env.SEPOLIA_RPC_URL;
  const privateKey = env.WEB3_PRIVATE_KEY;

  if (!rpcUrl || rpcUrl.includes('YOUR_NEW_API_KEY')) {
    throw new Error('SEPOLIA_RPC_URL contains placeholder. Enter real Alchemy Sepolia RPC URL in backend/.env');
  }
  if (!privateKey || privateKey.includes('YOUR_DEDICATED_SEPOLIA_TEST_WALLET_PRIVATE_KEY')) {
    throw new Error('WEB3_PRIVATE_KEY contains placeholder. Enter real private key in backend/.env');
  }

  const provider = new ethers.JsonRpcProvider(rpcUrl);
  const formattedKey = privateKey.startsWith('0x') ? privateKey : '0x' + privateKey;
  const wallet = new ethers.Wallet(formattedKey, provider);

  const balance = await provider.getBalance(wallet.address);
  console.log(`Deploying from wallet: ${wallet.address}`);
  console.log(`Wallet Balance: ${ethers.formatEther(balance)} SepoliaETH`);

  if (balance === 0n) {
    throw new Error(`Wallet ${wallet.address} has 0 SepoliaETH. Please obtain faucet ETH to deploy.`);
  }

  const abiPath = path.resolve(__dirname, '..', 'blockchain', 'build', 'blockchain_contracts_AgentBudget_sol_AgentBudget.abi');
  const binPath = path.resolve(__dirname, '..', 'blockchain', 'build', 'blockchain_contracts_AgentBudget_sol_AgentBudget.bin');

  if (!fs.existsSync(abiPath) || !fs.existsSync(binPath)) {
    throw new Error('Compiled contract files not found. Run solc compilation first.');
  }

  const abi = JSON.parse(fs.readFileSync(abiPath, 'utf8'));
  const bytecode = '0x' + fs.readFileSync(binPath, 'utf8').trim();

  console.log('Deploying AgentBudget.sol to Ethereum Sepolia...');
  const factory = new ethers.ContractFactory(abi, bytecode, wallet);
  const contract = await factory.deploy();

  const deployTx = contract.deploymentTransaction();
  console.log(`Deployment Transaction Hash: ${deployTx.hash}`);
  console.log('Waiting for deployment confirmation on Sepolia...');

  const receipt = await deployTx.wait(1);
  const contractAddress = await contract.getAddress();

  console.log('\n--- DEPLOYMENT SUCCESSFUL ---');
  console.log(`Contract Address: ${contractAddress}`);
  console.log(`Deployment Block: ${receipt.blockNumber}`);
  console.log(`Gas Used: ${receipt.gasUsed.toString()}`);
  console.log(`Transaction Status: ${receipt.status === 1 ? 'SUCCESS (1)' : 'FAILED (0)'}`);

  // Verify runtime bytecode
  const code = await provider.getCode(contractAddress);
  if (!code || code === '0x') {
    throw new Error(`Verification failed: No bytecode exists at ${contractAddress}`);
  }
  console.log(`Bytecode verified on-chain (${code.length / 2} bytes)`);

  // Update backend/.env
  updateEnvContractAddress(envPath, rawContent, contractAddress);

  // Register demo agent
  console.log('\nRegistering demo agent: agent-demo-001 with budget 100,000 paise (₹1,000)...');
  const regTx = await contract.registerAgent('agent-demo-001', 100000n);
  const regReceipt = await regTx.wait(1);
  console.log(`Agent Registration Tx: ${regTx.hash} (Block ${regReceipt.blockNumber})`);

  // Verify on-chain state
  const onChainBudget = await contract.getBudget('agent-demo-001');
  const onChainSpent = await contract.getSpent('agent-demo-001');
  const onChainRemaining = await contract.getRemaining('agent-demo-001');
  console.log(`\nVerified On-Chain State for 'agent-demo-001':`);
  console.log(`Budget:    ${onChainBudget.toString()} paise (₹${Number(onChainBudget) / 100})`);
  console.log(`Spent:     ${onChainSpent.toString()} paise (₹${Number(onChainSpent) / 100})`);
  console.log(`Remaining: ${onChainRemaining.toString()} paise (₹${Number(onChainRemaining) / 100})`);

  return {
    contractAddress,
    deployTxHash: deployTx.hash,
    deployBlock: receipt.blockNumber,
    registrationTxHash: regTx.hash,
    initialBudget: Number(onChainBudget),
    initialSpent: Number(onChainSpent),
    initialRemaining: Number(onChainRemaining)
  };
}

if (require.main === module) {
  deploy()
    .then((r) => console.log('Deployment Result:', JSON.stringify(r, null, 2)))
    .catch((err) => {
      console.error('Deployment Failed:', err.message);
      process.exit(1);
    });
}

module.exports = { deploy };
