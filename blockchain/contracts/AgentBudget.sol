// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/**
 * @title AgentBudget
 * @notice Authoritative on-chain spending limit and payment enforcement contract for autonomous AI agents.
 * @dev All monetary values are accounted in integer paise (1 INR = 100 paise).
 *      Example: ₹1,000 = 100,000 paise; ₹300 = 30,000 paise; ₹800 = 80,000 paise.
 *      Enforces that an AI agent cannot spend more than its assigned budget.
 *      Idempotency is strictly enforced by tracking unique keccak256(agentId, requestId) hashes.
 */
contract AgentBudget {

    address public owner;

    struct Agent {
        string agentId;
        uint256 budgetPaise;
        uint256 spentPaise;
        bool active;
        bool exists;
    }

    struct PaymentRecord {
        bytes32 requestHash;
        string agentId;
        string requestId;
        uint256 amountPaise;
        string providerId;
        uint256 timestamp;
        bool executed;
    }

    // agentId => Agent details
    mapping(string => Agent) private agents;

    // requestHash => PaymentRecord for strict idempotency
    mapping(bytes32 => PaymentRecord) private payments;

    // requestHash => executed flag
    mapping(bytes32 => bool) public processedRequests;

    // Event definitions
    event AgentRegistered(string indexed agentId, uint256 budgetPaise);
    event BudgetUpdated(string indexed agentId, uint256 newBudgetPaise);
    event PaymentAuthorized(
        bytes32 indexed requestHash,
        string indexed agentId,
        string requestId,
        uint256 amountPaise,
        string providerId,
        uint256 remainingPaise,
        uint256 timestamp
    );
    event PaymentRejected(
        string indexed agentId,
        string requestId,
        uint256 amountPaise,
        uint256 remainingPaise,
        string reason
    );

    modifier onlyOwner() {
        require(msg.sender == owner, "CALLER_NOT_AUTHORIZED_OWNER");
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    /**
     * @notice Registers or resets an agent with a designated spending budget in paise.
     * @param agentId Unique identifier for the agent (e.g. "agent-demo-001")
     * @param budgetPaise Total authorized budget in paise (e.g. 100000 for ₹1,000)
     */
    function registerAgent(string calldata agentId, uint256 budgetPaise) external onlyOwner {
        require(bytes(agentId).length > 0, "AGENT_ID_CANNOT_BE_EMPTY");
        require(budgetPaise > 0, "BUDGET_MUST_BE_GREATER_THAN_ZERO");

        agents[agentId] = Agent({
            agentId: agentId,
            budgetPaise: budgetPaise,
            spentPaise: 0,
            active: true,
            exists: true
        });

        emit AgentRegistered(agentId, budgetPaise);
    }

    /**
     * @notice Updates the budget cap for an active agent.
     */
    function setBudget(string calldata agentId, uint256 newBudgetPaise) external onlyOwner {
        require(agents[agentId].exists, "AGENT_DOES_NOT_EXIST");
        require(newBudgetPaise >= agents[agentId].spentPaise, "NEW_BUDGET_LESS_THAN_CURRENT_SPENT");

        agents[agentId].budgetPaise = newBudgetPaise;
        emit BudgetUpdated(agentId, newBudgetPaise);
    }

    /**
     * @notice Checks if an agent is active and registered.
     */
    function isAgentActive(string calldata agentId) external view returns (bool) {
        return agents[agentId].exists && agents[agentId].active;
    }

    /**
     * @notice Returns the agent's total budget in paise.
     */
    function getBudget(string calldata agentId) external view returns (uint256) {
        require(agents[agentId].exists, "AGENT_DOES_NOT_EXIST");
        return agents[agentId].budgetPaise;
    }

    /**
     * @notice Returns the agent's total settled spend in paise.
     */
    function getSpent(string calldata agentId) external view returns (uint256) {
        require(agents[agentId].exists, "AGENT_DOES_NOT_EXIST");
        return agents[agentId].spentPaise;
    }

    /**
     * @notice Computes remaining spending limit: budgetPaise - spentPaise.
     */
    function getRemaining(string calldata agentId) public view returns (uint256) {
        require(agents[agentId].exists, "AGENT_DOES_NOT_EXIST");
        Agent memory agent = agents[agentId];
        if (agent.spentPaise >= agent.budgetPaise) {
            return 0;
        }
        return agent.budgetPaise - agent.spentPaise;
    }

    /**
     * @notice Checks if a specific payment request ID has already been settled for this agent.
     */
    function isRequestProcessed(string calldata agentId, string calldata requestId) external view returns (bool) {
        bytes32 requestHash = keccak256(abi.encodePacked(agentId, requestId));
        return processedRequests[requestHash];
    }

    /**
     * @notice Authorizes and settles a payment if and only if:
     *         1. The agent exists and is active.
     *         2. The requestId has not been processed yet (strict idempotency).
     *         3. The amount in paise <= remaining budget (HARD NON-NEGOTIABLE BUDGET CAP).
     * @dev Reverts if amount exceeds remaining budget, preventing any state mutation.
     */
    function processPayment(
        string calldata agentId,
        string calldata requestId,
        uint256 amountPaise,
        string calldata providerId
    ) external onlyOwner returns (bytes32) {
        require(bytes(agentId).length > 0, "AGENT_ID_REQUIRED");
        require(bytes(requestId).length > 0, "REQUEST_ID_REQUIRED");
        require(amountPaise > 0, "AMOUNT_MUST_BE_GREATER_THAN_ZERO");
        require(agents[agentId].exists, "AGENT_DOES_NOT_EXIST");
        require(agents[agentId].active, "AGENT_NOT_ACTIVE");

        bytes32 requestHash = keccak256(abi.encodePacked(agentId, requestId));

        // Idempotency check: if already processed, return existing hash without re-charging
        if (processedRequests[requestHash]) {
            return requestHash;
        }

        Agent storage agent = agents[agentId];
        uint256 remaining = agent.budgetPaise > agent.spentPaise ? agent.budgetPaise - agent.spentPaise : 0;

        // HARD ENFORCEMENT CHECK: amount must not exceed remaining budget
        if (amountPaise > remaining) {
            emit PaymentRejected(agentId, requestId, amountPaise, remaining, "SPENDING_LIMIT_EXCEEDED");
            revert("SPENDING_LIMIT_EXCEEDED");
        }

        // Mutation of authoritative state
        agent.spentPaise += amountPaise;
        processedRequests[requestHash] = true;

        payments[requestHash] = PaymentRecord({
            requestHash: requestHash,
            agentId: agentId,
            requestId: requestId,
            amountPaise: amountPaise,
            providerId: providerId,
            timestamp: block.timestamp,
            executed: true
        });

        uint256 newRemaining = agent.budgetPaise - agent.spentPaise;

        emit PaymentAuthorized(
            requestHash,
            agentId,
            requestId,
            amountPaise,
            providerId,
            newRemaining,
            block.timestamp
        );

        return requestHash;
    }

    /**
     * @notice Retrieves historical payment details by request hash.
     */
    function getPaymentRecord(bytes32 requestHash) external view returns (
        string memory agentId,
        string memory requestId,
        uint256 amountPaise,
        string memory providerId,
        uint256 timestamp,
        bool executed
    ) {
        require(processedRequests[requestHash], "PAYMENT_NOT_FOUND");
        PaymentRecord memory p = payments[requestHash];
        return (p.agentId, p.requestId, p.amountPaise, p.providerId, p.timestamp, p.executed);
    }
}
