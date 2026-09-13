const fs = require('fs');
const path = require('path');
const { ethers } = require('ethers');

function loadEnv() {
  const envPath = path.resolve(__dirname, '..', 'backend', '.env');
  if (!fs.existsSync(envPath)) {
    console.error('ERROR: backend/.env not found at ' + envPath);
    process.exit(1);
  }
  const content = fs.readFileSync(envPath, 'utf8');
  const env = {};
  for (const line of content.split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const eqIdx = trimmed.indexOf('=');
    if (eqIdx !== -1) {
      const key = trimmed.substring(0, eqIdx).trim();
      const val = trimmed.substring(eqIdx + 1).trim();
      env[key] = val;
    }
  }
  return env;
}

async function verifyConnection() {
  const env = loadEnv();
  const rpcUrl = env.SEPOLIA_RPC_URL;
  const privateKey = env.WEB3_PRIVATE_KEY;
  const expectedChainId = BigInt(env.CHAIN_ID || 11155111);

  if (!rpcUrl || rpcUrl.includes('YOUR_NEW_API_KEY')) {
    console.log('STATUS: SEPOLIA_RPC_URL is not configured yet (still contains placeholder).');
    return { configured: false, reason: 'SEPOLIA_RPC_URL placeholder' };
  }
  if (!privateKey || privateKey.includes('YOUR_DEDICATED_SEPOLIA_TEST_WALLET_PRIVATE_KEY')) {
    console.log('STATUS: WEB3_PRIVATE_KEY is not configured yet (still contains placeholder).');
    return { configured: false, reason: 'WEB3_PRIVATE_KEY placeholder' };
  }

  console.log('Testing connection to Sepolia RPC...');
  const provider = new ethers.JsonRpcProvider(rpcUrl);

  // 1. Verify RPC and Chain ID
  const network = await provider.getNetwork();
  console.log(`Network Name: ${network.name}`);
  console.log(`Chain ID: ${network.chainId}`);
  if (network.chainId !== expectedChainId) {
    throw new Error(`Chain ID mismatch! Expected ${expectedChainId}, got ${network.chainId}`);
  }

  // 2. Load Wallet
  const formattedKey = privateKey.startsWith('0x') ? privateKey : '0x' + privateKey;
  const wallet = new ethers.Wallet(formattedKey, provider);
  console.log(`Wallet Address: ${wallet.address}`);

  // 3. Check Balance
  const balance = await provider.getBalance(wallet.address);
  const balanceEth = ethers.formatEther(balance);
  console.log(`Wallet Balance: ${balanceEth} SepoliaETH`);

  if (balance === 0n) {
    console.log('WARNING: Wallet has 0 SepoliaETH. Gas will be required to deploy contracts and send payment transactions.');
  }

  return {
    configured: true,
    chainId: Number(network.chainId),
    walletAddress: wallet.address,
    balanceEth: balanceEth,
    contractAddress: env.CONTRACT_ADDRESS || ''
  };
}

if (require.main === module) {
  verifyConnection()
    .then((res) => {
      console.log('Sepolia Verification Result:', JSON.stringify(res, null, 2));
    })
    .catch((err) => {
      console.error('Sepolia Verification Failed:', err.message);
      process.exit(1);
    });
}

module.exports = { verifyConnection, loadEnv };
