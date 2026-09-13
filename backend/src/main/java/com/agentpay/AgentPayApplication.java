package com.agentpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class AgentPayApplication {
    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(AgentPayApplication.class, args);
    }

    private static void loadDotEnv() {
        java.io.File[] candidates = new java.io.File[] {
                new java.io.File(".env"),
                new java.io.File("backend/.env"),
                new java.io.File("../backend/.env"),
                new java.io.File(System.getProperty("user.dir"), ".env"),
                new java.io.File(System.getProperty("user.dir"), "backend/.env")
        };

        for (java.io.File file : candidates) {
            if (file.exists() && file.isFile()) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                        int eq = trimmed.indexOf('=');
                        if (eq > 0) {
                            String key = trimmed.substring(0, eq).trim();
                            String val = trimmed.substring(eq + 1).trim();
                            if (!val.isEmpty()) {
                                System.setProperty(key, val);
                                if ("BLOCKCHAIN_MODE".equals(key)) {
                                    System.setProperty("app.blockchain.mode", val);
                                } else if ("SEPOLIA_RPC_URL".equals(key)) {
                                    System.setProperty("app.blockchain.sepolia.rpc-url", val);
                                } else if ("WEB3_PRIVATE_KEY".equals(key)) {
                                    System.setProperty("app.blockchain.sepolia.private-key", val);
                                } else if ("CONTRACT_ADDRESS".equals(key)) {
                                    System.setProperty("app.blockchain.sepolia.contract-address", val);
                                } else if ("CHAIN_ID".equals(key)) {
                                    System.setProperty("app.blockchain.sepolia.chain-id", val);
                                }
                            }
                        }
                    }
                    break;
                } catch (Exception ignored) {}
            }
        }
    }
}
