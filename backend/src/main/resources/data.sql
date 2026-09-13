-- =============================================================================
-- AGENTPAY H2 IN-MEMORY SEED DATA (data.sql)
--
-- This script preloads realistic development and demonstration catalog data
-- into the H2 in-memory database (jdbc:h2:mem:agentpaydb) at application startup.
--
-- ARCHITECTURAL BOUNDARY:
-- This script populates ONLY local application metadata, service catalog,
-- provider definitions, and safe initial audit markers.
-- It DOES NOT define or override authoritative blockchain financial state:
--   - Spending budget, spent amount, and remaining amount are authoritatively
--     enforced by the AgentBudget Solidity contract on Ethereum Sepolia.
--   - Real transaction hashes and settlement proofs originate on-chain.
--   - No fake purchases, fake payments, or pseudo-hashes are seeded here.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. PRIMARY DEMO AGENT
-- -----------------------------------------------------------------------------
-- Authorized agent for autonomous micro-purchasing demonstration.
-- Initial local budget metadata: ₹1,000 (100,000 paise).
-- Authoritative spending balance is validated on-chain via Sepolia.
MERGE INTO agents (external_id, name, wallet_address, budget_paise, status, created_at, updated_at)
KEY (external_id)
VALUES (
    'agent-demo-001',
    'Demo Agent',
    '0x7a23c4d8719f9b329a4310e5f29c8b91',
    100000,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- 2. VERIFIED SERVICE PROVIDERS
-- -----------------------------------------------------------------------------
-- Independent translation service providers with SLA quality scores.

-- Provider A: High-speed neural translation optimized for low-latency micro-tasks
MERGE INTO providers (external_id, name, description, quality_score, status, created_at)
KEY (external_id)
VALUES (
    'prov-a',
    'Provider A (LinguaFast)',
    'High-speed neural translation services optimized for autonomous micro-transactions with guaranteed low latency and 90% SLA compliance.',
    90,
    'ACTIVE',
    CURRENT_TIMESTAMP
);

-- Provider B: Premium enterprise precision translation with context preservation
MERGE INTO providers (external_id, name, description, quality_score, status, created_at)
KEY (external_id)
VALUES (
    'prov-b',
    'Provider B (PolyGlot Ultra)',
    'Enterprise certified contextual translation with guaranteed high fidelity, multi-language support, and 97% zero-hallucination SLA.',
    97,
    'ACTIVE',
    CURRENT_TIMESTAMP
);

-- Provider C: Economy translation tier for routine, high-volume background tasks
MERGE INTO providers (external_id, name, description, quality_score, status, created_at)
KEY (external_id)
VALUES (
    'prov-c',
    'Provider C (BudgetLingua)',
    'Economical translation tier for high-volume background tasks with cost-efficient throughput and 82% SLA compliance.',
    82,
    'ACTIVE',
    CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- 3. AUTHORITATIVE SERVICE CATALOG
-- -----------------------------------------------------------------------------
-- Authoritative catalog prices stored in integer paise (1 INR = 100 paise).
-- Normal service: Fast Neural Translation (₹300 / 30,000 paise)
MERGE INTO services (external_id, provider_id, name, type, description, price_paise, currency, quality_score, status, created_at)
KEY (external_id)
VALUES (
    'svc-trans-a',
    'prov-a',
    'Fast Neural Translation',
    'TRANSLATION',
    'Standard latency translation pipeline (up to 1,000 tokens) with 90% SLA compliance.',
    30000,
    'INR',
    90,
    'ACTIVE',
    CURRENT_TIMESTAMP
);

-- Premium service: Ultra Precision Translation (₹500 / 50,000 paise)
MERGE INTO services (external_id, provider_id, name, type, description, price_paise, currency, quality_score, status, created_at)
KEY (external_id)
VALUES (
    'svc-trans-b',
    'prov-b',
    'Ultra Precision Translation',
    'TRANSLATION',
    'Context-preserving neural translation with 97% SLA and guaranteed zero hallucination.',
    50000,
    'INR',
    97,
    'ACTIVE',
    CURRENT_TIMESTAMP
);

-- Economy service: Economy Translation (₹200 / 20,000 paise)
MERGE INTO services (external_id, provider_id, name, type, description, price_paise, currency, quality_score, status, created_at)
KEY (external_id)
VALUES (
    'svc-trans-c',
    'prov-c',
    'Economy Translation',
    'TRANSLATION',
    'Best-effort translation service for low-criticality autonomous workflows and batch processing.',
    20000,
    'INR',
    82,
    'ACTIVE',
    CURRENT_TIMESTAMP
);

-- Authoritative overspend test service: Enterprise Translation Suite (₹800 / 80,000 paise)
-- NOTE: This is a legitimate backend catalog entry whose authoritative price is ₹800.
-- It is used to demonstrate smart-contract spending boundary enforcement when ₹700 remains.
MERGE INTO services (external_id, provider_id, name, type, description, price_paise, currency, quality_score, status, created_at)
KEY (external_id)
VALUES (
    'svc-trans-overspend',
    'prov-b',
    'Enterprise Translation Suite',
    'TRANSLATION',
    'High-end enterprise language compute cluster priced at ₹800 (used to demonstrate smart-contract spending boundary enforcement).',
    80000,
    'INR',
    99,
    'ACTIVE',
    CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- 4. SAFE INFORMATIONAL AUDIT EVENTS
-- -----------------------------------------------------------------------------
-- Preload safe system-initialization audit entries so the audit feed is populated
-- immediately without fabricating financial transactions or settlements.
-- No fake transaction hashes, no fake PAYMENT_APPROVED, no fake receipts.
MERGE INTO audit_events (request_id, agent_id, event_type, status, message, amount_paise, currency, transaction_hash, created_at)
KEY (request_id)
VALUES (
    'SYS-INIT-001',
    'agent-demo-001',
    'SYSTEM_INITIALIZED',
    'INFO',
    'AgentPay payment gateway core initialized with H2 in-memory persistence and Sepolia smart contract enforcement.',
    NULL,
    'INR',
    NULL,
    CURRENT_TIMESTAMP
);

MERGE INTO audit_events (request_id, agent_id, event_type, status, message, amount_paise, currency, transaction_hash, created_at)
KEY (request_id)
VALUES (
    'SYS-INIT-002',
    'agent-demo-001',
    'AGENT_INITIALIZED',
    'INFO',
    'Primary autonomous agent agent-demo-001 registered with authorized daily spending limit of ₹1,000 (100,000 paise).',
    100000,
    'INR',
    NULL,
    CURRENT_TIMESTAMP
);

MERGE INTO audit_events (request_id, agent_id, event_type, status, message, amount_paise, currency, transaction_hash, created_at)
KEY (request_id)
VALUES (
    'SYS-INIT-003',
    'agent-demo-001',
    'PROVIDER_REGISTERED',
    'INFO',
    'Verified translation service providers (Provider A, Provider B, Provider C) registered in local catalog.',
    NULL,
    'INR',
    NULL,
    CURRENT_TIMESTAMP
);

MERGE INTO audit_events (request_id, agent_id, event_type, status, message, amount_paise, currency, transaction_hash, created_at)
KEY (request_id)
VALUES (
    'SYS-INIT-004',
    'agent-demo-001',
    'SERVICE_CATALOG_LOADED',
    'INFO',
    'Authoritative service catalog preloaded: svc-trans-a (₹300), svc-trans-b (₹500), svc-trans-c (₹200), svc-trans-overspend (₹800).',
    NULL,
    'INR',
    NULL,
    CURRENT_TIMESTAMP
);
