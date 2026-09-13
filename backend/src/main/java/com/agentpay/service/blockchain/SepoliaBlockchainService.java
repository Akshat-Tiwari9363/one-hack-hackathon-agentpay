package com.agentpay.service.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Live Web3j Sepolia Blockchain enforcement service communicating directly
 * with deployed AgentBudget.sol.
 */
@Service
public class SepoliaBlockchainService implements BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(SepoliaBlockchainService.class);

    private final String rpcUrl;
    private final String privateKey;
    private final String contractAddress;
    private final long chainId;
    private final com.agentpay.repository.PurchaseRepository purchaseRepository;

    private Web3j web3j;
    private Credentials credentials;

    public SepoliaBlockchainService(
            @Value("${app.blockchain.sepolia.rpc-url:https://rpc.sepolia.org}") String rpcUrl,
            @Value("${app.blockchain.sepolia.private-key:}") String privateKey,
            @Value("${app.blockchain.sepolia.contract-address:0x0000000000000000000000000000000000000000}") String contractAddress,
            @Value("${app.blockchain.sepolia.chain-id:11155111}") long chainId,
            com.agentpay.repository.PurchaseRepository purchaseRepository) {

        this.rpcUrl = rpcUrl;
        this.privateKey = privateKey;
        this.contractAddress = contractAddress;
        this.chainId = chainId;
        this.purchaseRepository = purchaseRepository;

        initializeWeb3j();
    }

    private void initializeWeb3j() {
        try {
            if (rpcUrl != null && !rpcUrl.isBlank()) {
                this.web3j = Web3j.build(new HttpService(rpcUrl));

                String sanitizedRpc = rpcUrl.contains("/v2/")
                        ? rpcUrl.substring(0, rpcUrl.indexOf("/v2/") + 4) + "***"
                        : rpcUrl;

                log.info(
                        "[SEPOLIA BLOCKCHAIN] Web3j client initialized for RPC: {}",
                        sanitizedRpc
                );
            }

            if (privateKey != null
                    && !privateKey.isBlank()
                    && !privateKey.contains("YOUR_")
                    && !privateKey.contains("000000")) {

                this.credentials = Credentials.create(privateKey);

                log.info(
                        "[SEPOLIA BLOCKCHAIN] Wallet credentials loaded. Address: {}",
                        credentials.getAddress()
                );

            } else {
                log.info(
                        "[SEPOLIA BLOCKCHAIN] Running in read-only / unauthenticated Sepolia configuration until private key is set."
                );
            }

        } catch (Exception e) {
            log.warn(
                    "[SEPOLIA BLOCKCHAIN] Failed to initialize Web3j: {}. Will report offline status.",
                    e.getMessage()
            );
        }
    }

    public boolean isConnected() {
        try {
            if (web3j == null) {
                return false;
            }

            return web3j
                    .web3ClientVersion()
                    .send()
                    .getWeb3ClientVersion() != null;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidContract() {
        return contractAddress != null
                && contractAddress.length() == 42
                && !contractAddress.equalsIgnoreCase(
                "0x0000000000000000000000000000000000000000"
        );
    }

    public boolean hasBytecode() {
        if (!isValidContract() || web3j == null) {
            return false;
        }

        try {
            String code = web3j
                    .ethGetCode(
                            contractAddress,
                            DefaultBlockParameterName.LATEST
                    )
                    .send()
                    .getCode();

            return code != null
                    && !code.equals("0x")
                    && !code.equals("0x0")
                    && code.length() > 2;

        } catch (Exception e) {
            return false;
        }
    }

    public String getContractAddress() {
        return contractAddress;
    }

    @Override
    public String getMode() {
        return "SEPOLIA";
    }

    @Override
    public String getEnforcementLayer() {
        return "SOLIDITY SMART CONTRACT";
    }

    /**
     * Normal agent registration.
     *
     * This remains idempotent:
     * if the agent is already active with the same budget,
     * no transaction is sent.
     */
    @Override
    public void registerAgent(String agentId, Long budgetPaise) {

        log.info(
                "[SEPOLIA CONTRACT] Registering agent '{}' on Sepolia with budget {} paise (₹{})",
                agentId,
                budgetPaise,
                budgetPaise / 100.0
        );

        if (credentials == null || !isValidContract()) {
            log.warn(
                    "[SEPOLIA CONTRACT] Contract address or private key not configured for live write operation."
            );
            return;
        }

        try {
            if (isAgentActive(agentId)
                    && getBudget(agentId).equals(budgetPaise)) {

                log.info(
                        "[SEPOLIA CONTRACT] Agent '{}' is already registered and active on-chain with budget {} paise.",
                        agentId,
                        budgetPaise
                );

                return;
            }

            registerAgentOnChain(agentId, budgetPaise);

        } catch (Exception e) {
            log.error(
                    "[SEPOLIA CONTRACT] Error registering agent: {}",
                    e.getMessage(),
                    e
            );
        }
    }

    /**
     * Forces an actual on-chain registerAgent transaction.
     *
     * This is intentionally separate from registerAgent() because
     * registerAgent() skips the transaction for already-active agents.
     *
     * The demo reset uses this method so that the contract state is
     * actually reset instead of merely resetting the local H2 state.
     */
    private void registerAgentOnChain(
            String agentId,
            Long budgetPaise
    ) throws Exception {

        Function function = new Function(
                "registerAgent",
                Arrays.asList(
                        new Utf8String(agentId),
                        new Uint256(BigInteger.valueOf(budgetPaise))
                ),
                Collections.emptyList()
        );

        String txHash = executeContractTransaction(function);

        log.info(
                "[SEPOLIA CONTRACT] Agent registered/reset successfully. TxHash: {}",
                txHash
        );
    }

    public boolean isAgentActive(String agentId) {

        if (!isValidContract() || web3j == null) {
            return false;
        }

        try {
            Function function = new Function(
                    "isAgentActive",
                    Collections.singletonList(
                            new Utf8String(agentId)
                    ),
                    Collections.singletonList(
                            new TypeReference<Bool>() {
                            }
                    )
            );

            EthCall response = callViewFunction(function);

            List<Type> result = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            if (!result.isEmpty()) {
                return (Boolean) result.get(0).getValue();
            }

        } catch (Exception e) {
            log.debug(
                    "[SEPOLIA CONTRACT] isAgentActive view failed: {}",
                    e.getMessage()
            );
        }

        return false;
    }

    @Override
    public Long getBudget(String agentId) {

        if (!isValidContract() || web3j == null) {
            return 100000L;
        }

        try {
            Function function = new Function(
                    "getBudget",
                    Collections.singletonList(
                            new Utf8String(agentId)
                    ),
                    Collections.singletonList(
                            new TypeReference<Uint256>() {
                            }
                    )
            );

            EthCall response = callViewFunction(function);

            List<Type> result = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            if (!result.isEmpty()) {
                return ((BigInteger) result.get(0).getValue()).longValue();
            }

        } catch (Exception e) {
            log.debug(
                    "[SEPOLIA CONTRACT] getBudget view failed: {}",
                    e.getMessage()
            );
        }

        return 100000L;
    }

    @Override
    public Long getSpent(String agentId) {

        if (!isValidContract() || web3j == null) {
            return 0L;
        }

        try {
            Function function = new Function(
                    "getSpent",
                    Collections.singletonList(
                            new Utf8String(agentId)
                    ),
                    Collections.singletonList(
                            new TypeReference<Uint256>() {
                            }
                    )
            );

            EthCall response = callViewFunction(function);

            List<Type> result = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            if (!result.isEmpty()) {
                return ((BigInteger) result.get(0).getValue()).longValue();
            }

        } catch (Exception e) {
            log.debug(
                    "[SEPOLIA CONTRACT] getSpent view failed: {}",
                    e.getMessage()
            );
        }

        return 0L;
    }

    @Override
    public Long getRemaining(String agentId) {

        if (!isValidContract() || web3j == null) {
            return 100000L;
        }

        try {
            Function function = new Function(
                    "getRemaining",
                    Collections.singletonList(
                            new Utf8String(agentId)
                    ),
                    Collections.singletonList(
                            new TypeReference<Uint256>() {
                            }
                    )
            );

            EthCall response = callViewFunction(function);

            List<Type> result = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            if (!result.isEmpty()) {
                return ((BigInteger) result.get(0).getValue()).longValue();
            }

        } catch (Exception e) {
            log.debug(
                    "[SEPOLIA CONTRACT] getRemaining view failed: {}",
                    e.getMessage()
            );
        }

        return 100000L;
    }

    @Override
    public boolean isRequestProcessed(
            String agentId,
            String requestId
    ) {

        if (!isValidContract() || web3j == null) {
            return false;
        }

        try {
            Function function = new Function(
                    "isRequestProcessed",
                    Arrays.asList(
                            new Utf8String(agentId),
                            new Utf8String(requestId)
                    ),
                    Collections.singletonList(
                            new TypeReference<Bool>() {
                            }
                    )
            );

            EthCall response = callViewFunction(function);

            List<Type> result = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            if (!result.isEmpty()) {
                return (Boolean) result.get(0).getValue();
            }

        } catch (Exception e) {
            log.debug(
                    "[SEPOLIA CONTRACT] isRequestProcessed view failed: {}",
                    e.getMessage()
            );
        }

        return false;
    }

    @Override
    public BlockchainPaymentResult processPayment(
            String agentId,
            String requestId,
            Long amountPaise,
            String providerId
    ) {

        log.info(
                "[SEPOLIA CONTRACT] Initiating authoritative on-chain payment check for requestId: {}, amount: {} paise (₹{})",
                requestId,
                amountPaise,
                amountPaise / 100.0
        );

        if (!isValidContract() || credentials == null) {

            log.warn(
                    "[SEPOLIA CONTRACT] Active Sepolia credentials or contract address missing."
            );

            return BlockchainPaymentResult.rejected(
                    "SEPOLIA_CREDENTIALS_REQUIRED",
                    getMode(),
                    getEnforcementLayer(),
                    0L,
                    0L
            );
        }

        try {

            /*
             * Check if contract already processed this requestId.
             * This provides on-chain idempotency.
             */
            if (isRequestProcessed(agentId, requestId)) {

                log.info(
                        "[SEPOLIA CONTRACT] On-chain idempotency hit: RequestId {} already processed.",
                        requestId
                );

                String originalTxHash = purchaseRepository.findByRequestId(requestId)
                        .map(com.agentpay.domain.Purchase::getTransactionHash)
                        .orElse(null);

                return BlockchainPaymentResult.approved(
                        originalTxHash,
                        getMode(),
                        getEnforcementLayer(),
                        getSpent(agentId),
                        getRemaining(agentId),
                        true
                );
            }

            Function function = new Function(
                    "processPayment",
                    Arrays.asList(
                            new Utf8String(agentId),
                            new Utf8String(requestId),
                            new Uint256(BigInteger.valueOf(amountPaise)),
                            new Utf8String(providerId)
                    ),
                    Collections.singletonList(
                            new TypeReference<Bytes32>() {
                            }
                    )
            );

            /*
             * First perform an eth_call against the actual contract.
             *
             * This does not modify blockchain state.
             * It lets the backend detect a Solidity revert before
             * broadcasting the real transaction.
             */
            EthCall simCall = callFunctionAsSender(
                    function,
                    credentials.getAddress()
            );

            if (simCall.isReverted() || simCall.getError() != null) {

                String revertReason = simCall.getRevertReason();

                if (revertReason == null && simCall.getError() != null) {
                    revertReason = simCall.getError().getMessage();
                }

                if (revertReason == null || revertReason.isBlank()) {
                    revertReason = "SPENDING_LIMIT_EXCEEDED";
                }

                if (revertReason.contains("SPENDING_LIMIT_EXCEEDED")) {
                    revertReason = "SPENDING_LIMIT_EXCEEDED";
                }

                log.warn(
                        "[SEPOLIA CONTRACT] Solidity contract REJECTED payment on-chain: {}",
                        revertReason
                );

                return BlockchainPaymentResult.rejected(
                        revertReason,
                        getMode(),
                        getEnforcementLayer(),
                        getSpent(agentId),
                        getRemaining(agentId)
                );
            }

            /*
             * Real execution on Sepolia.
             */
            String txHash = executeContractTransaction(function);

            Long newSpent = getSpent(agentId);
            Long newRemaining = getRemaining(agentId);

            return BlockchainPaymentResult.approved(
                    txHash,
                    getMode(),
                    getEnforcementLayer(),
                    newSpent,
                    newRemaining,
                    false
            );

        } catch (Exception e) {

            log.error(
                    "[SEPOLIA CONTRACT] Contract interaction failed: {}",
                    e.getMessage()
            );

            String errorMsg = e.getMessage() != null
                    ? e.getMessage()
                    : "";

            if (errorMsg.contains("SPENDING_LIMIT_EXCEEDED")) {

                return BlockchainPaymentResult.rejected(
                        "SPENDING_LIMIT_EXCEEDED",
                        getMode(),
                        getEnforcementLayer(),
                        getSpent(agentId),
                        getRemaining(agentId)
                );
            }

            return BlockchainPaymentResult.rejected(
                    "ON_CHAIN_REVERT: " + errorMsg,
                    getMode(),
                    getEnforcementLayer(),
                    getSpent(agentId),
                    getRemaining(agentId)
            );
        }
    }

    /**
     * Resets the demo state on the REAL Sepolia contract.
     *
     * Important:
     * We intentionally do NOT call registerAgent() here because
     * registerAgent() skips the transaction when the agent is already
     * active with the same budget.
     *
     * This method therefore forces a real registerAgent transaction.
     */
    @Override
    public void resetDemoState() {

        final String agentId = "agent-demo-001";
        final long budgetPaise = 100000L;

        log.info("[SEPOLIA CONTRACT] Resetting demo state on Sepolia");

        if (credentials == null || !isValidContract()) {

            log.warn(
                    "[SEPOLIA CONTRACT] Cannot reset: credentials or contract address missing."
            );

            return;
        }

        try {

            /*
             * Force an actual on-chain transaction.
             */
            registerAgentOnChain(
                    agentId,
                    budgetPaise
            );

            /*
             * Read the state again from Sepolia after confirmation.
             */
            Long budget = getBudget(agentId);
            Long spent = getSpent(agentId);
            Long remaining = getRemaining(agentId);

            log.info(
                    "[SEPOLIA CONTRACT] Demo state reset completed. Budget={} paise, Spent={} paise, Remaining={} paise",
                    budget,
                    spent,
                    remaining
            );

        } catch (Exception e) {

            log.error(
                    "[SEPOLIA CONTRACT] Failed to reset demo state: {}",
                    e.getMessage(),
                    e
            );
        }
    }

    private EthCall callViewFunction(
            Function function
    ) throws Exception {

        String encoded = FunctionEncoder.encode(function);

        return web3j.ethCall(
                Transaction.createEthCallTransaction(
                        null,
                        contractAddress,
                        encoded
                ),
                DefaultBlockParameterName.LATEST
        ).send();
    }

    private EthCall callFunctionAsSender(
            Function function,
            String fromAddress
    ) throws Exception {

        String encoded = FunctionEncoder.encode(function);

        return web3j.ethCall(
                Transaction.createEthCallTransaction(
                        fromAddress,
                        contractAddress,
                        encoded
                ),
                DefaultBlockParameterName.LATEST
        ).send();
    }

    private String executeContractTransaction(
            Function function
    ) throws Exception {

        String encoded = FunctionEncoder.encode(function);

        EthGetTransactionCount ethGetTransactionCount =
                web3j.ethGetTransactionCount(
                        credentials.getAddress(),
                        DefaultBlockParameterName.LATEST
                ).send();

        BigInteger nonce =
                ethGetTransactionCount.getTransactionCount();

        BigInteger gasPrice =
                web3j.ethGasPrice()
                        .send()
                        .getGasPrice();

        /*
         * 20% gas price buffer for Sepolia.
         */
        gasPrice = gasPrice
                .multiply(BigInteger.valueOf(120))
                .divide(BigInteger.valueOf(100));

        BigInteger gasLimit =
                BigInteger.valueOf(350000);

        RawTransaction rawTransaction =
                RawTransaction.createTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        contractAddress,
                        encoded
                );

        byte[] signedMessage =
                TransactionEncoder.signMessage(
                        rawTransaction,
                        chainId,
                        credentials
                );

        String hexValue =
                Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction =
                web3j.ethSendRawTransaction(hexValue).send();

        if (ethSendTransaction.hasError()) {

            throw new RuntimeException(
                    "Sepolia RPC Error: "
                            + ethSendTransaction.getError().getMessage()
            );
        }

        String txHash =
                ethSendTransaction.getTransactionHash();

        log.info(
                "[SEPOLIA BLOCKCHAIN] Broadcasted tx: {}. Waiting for confirmation on Sepolia...",
                txHash
        );

        /*
         * Wait for on-chain receipt confirmation.
         */
        int maxAttempts = 40;
        int attempts = 0;

        while (attempts < maxAttempts) {

            Thread.sleep(2000);

            attempts++;

            var receiptOpt =
                    web3j
                            .ethGetTransactionReceipt(txHash)
                            .send()
                            .getTransactionReceipt();

            if (receiptOpt.isPresent()) {

                var receipt = receiptOpt.get();

                if (!receipt.isStatusOK()) {

                    throw new RuntimeException(
                            "Transaction reverted on-chain (status 0x0): "
                                    + txHash
                    );
                }

                log.info(
                        "[SEPOLIA BLOCKCHAIN] Tx confirmed in block {} (status: {}). Gas used: {}",
                        receipt.getBlockNumber(),
                        receipt.getStatus(),
                        receipt.getGasUsed()
                );

                return txHash;
            }
        }

        /*
         * Transaction was broadcast successfully but confirmation
         * was not observed within the polling window.
         */
        log.warn(
                "[SEPOLIA BLOCKCHAIN] Tx {} was broadcast but confirmation was not observed within the polling window.",
                txHash
        );

        return txHash;
    }
}