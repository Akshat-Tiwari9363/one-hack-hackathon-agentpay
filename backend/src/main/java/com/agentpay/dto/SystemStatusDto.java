package com.agentpay.dto;

public class SystemStatusDto {
    private String backendStatus;
    private String databaseStatus;
    private String ollamaStatus;
    private String ollamaModel;
    private String blockchainMode;
    private boolean blockchainConnected;
    private String contractAddress;
    private Long chainId;
    private String network;
    private String currency;
    private String timestamp;

    public SystemStatusDto() {
        this.currency = "INR";
    }

    public String getBackendStatus() { return backendStatus; }
    public void setBackendStatus(String backendStatus) { this.backendStatus = backendStatus; }

    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }

    public String getOllamaStatus() { return ollamaStatus; }
    public void setOllamaStatus(String ollamaStatus) { this.ollamaStatus = ollamaStatus; }

    public String getOllamaModel() { return ollamaModel; }
    public void setOllamaModel(String ollamaModel) { this.ollamaModel = ollamaModel; }

    public String getBlockchainMode() { return blockchainMode; }
    public void setBlockchainMode(String blockchainMode) { this.blockchainMode = blockchainMode; }

    public boolean isBlockchainConnected() { return blockchainConnected; }
    public void setBlockchainConnected(boolean blockchainConnected) { this.blockchainConnected = blockchainConnected; }

    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }

    public Long getChainId() { return chainId; }
    public void setChainId(Long chainId) { this.chainId = chainId; }

    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
