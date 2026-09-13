const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export interface DashboardSummary {
  budgetINR: number
  spentINR: number
  remainingINR: number
  utilizationPercent: number
  budgetPaise: number
  spentPaise: number
  remainingPaise: number
  successfulTxnsCount: number
  blockedAttacksCount: number
  totalPurchasesCount: number
  providersCount: number
  agentName: string
  agentStatus: string
  agentWallet: string
  blockchainMode: 'MOCK' | 'SEPOLIA'
  enforcementLayer: string
  aiEngine: string
  aiModel: string
  currency: 'INR'
  dailyCapINR: number
  budgetEnforced: boolean
}

export interface BudgetData {
  agentId: string
  budgetPaise: number
  budgetINR: number
  spentPaise: number
  spentINR: number
  remainingPaise: number
  remainingINR: number
  utilizationPercent: number
  currency: 'INR'
  status: string
  enforced: boolean
  blockchainMode: 'MOCK' | 'SEPOLIA'
  enforcementLayer: string
}

export interface ServiceItem {
  id: number
  externalId: string
  providerId: string
  name: string
  type: string
  description: string
  pricePaise: number
  priceINR: number
  currency: 'INR'
  qualityScore: number
  status: string
}

export interface ProviderItem {
  id: number
  externalId: string
  name: string
  description: string
  qualityScore: number
  status: string
  services: ServiceItem[]
}

export interface PurchaseItem {
  id: number
  requestId: string
  agentId: string
  providerId: string
  serviceId: string
  amountPaise: number
  amountINR: number
  currency: 'INR'
  status: string
  paymentStatus: string
  transactionHash: string
  blockchainMode: string
  createdAt: string
}

export interface ReceiptItem {
  id: number
  receiptId: string
  purchaseId: number
  requestId: string
  agentId: string
  providerId: string
  serviceId: string
  amountPaise: number
  amountINR: number
  currency: 'INR'
  transactionHash: string
  deliveredContent: string
  contentHash: string
  verificationStatus: 'VERIFIED' | 'FAILED' | 'PENDING'
  deliveredAt: string
}

export interface VerifyReceiptResponse {
  receiptId: string
  calculatedContentHash: string
  storedContentHash: string
  hashMatches: boolean
  status: 'VERIFIED' | 'FAILED'
  message: string
}

export interface AuditEventItem {
  id: number
  requestId: string
  agentId: string
  eventType: string
  status: 'SUCCESS' | 'BLOCKED' | 'INFO' | 'FAILED'
  message: string
  amountPaise: number | null
  amountINR: number | null
  currency: 'INR'
  transactionHash: string | null
  createdAt: string
}

export interface Http402Challenge {
  requestId: string
  serviceId: string
  providerId: string
  serviceName: string
  amountPaise: number
  amount: number
  currency: 'INR'
  paymentRequired: boolean
  paymentNetwork: string
  qualityScore: number
}

export interface PaymentResponse {
  requestId: string
  purchaseId: number
  agentId: string
  providerId: string
  serviceId: string
  amountPaise: number
  amountINR: number
  currency: 'INR'
  status: string
  paymentStatus: string
  transactionHash: string
  blockchainMode: string
  enforcementLayer: string
  deliveredContent: string
  contentHash: string
  receiptId: string
  isRetry: boolean
}

export interface AutonomousPaymentOutcome {
  paymentNumber: number
  requestId: string
  serviceId: string
  serviceName: string
  providerId: string
  amountTokens: number
  amountPaise: number
  blockchainStatus: 'ALLOWED' | 'DENIED'
  transactionHash: string | null
  serviceStatus: 'DELIVERED' | 'NOT_DELIVERED'
  deliveredContent: string | null
  contentHash: string | null
  receiptId: string | null
  reason: string
  enforcementLayer: string
}

export interface AutonomousRunResponse {
  runId: string
  agentId: string
  userTask: string
  status: 'COMPLETED' | 'PARTIAL' | 'FAILED'
  budgetTokens: number
  spentTokens: number
  remainingTokens: number
  totalAttempted: number
  totalApproved: number
  totalDenied: number
  payments: AutonomousPaymentOutcome[]
  aiEngine: string
  aiModel: string
  aiSummary: string
  denomination: 'TOKENS'
}

export interface SystemStatus {
  backendStatus: string
  databaseStatus: string
  ollamaStatus: string
  ollamaModel: string
  blockchainMode: string
  blockchainConnected: boolean
  contractAddress: string
  network: string
  currency: string
  timestamp: string
}

export interface ApiError {
  timestamp: string
  requestId: string
  status: number
  code: string
  message: string
  currency: 'INR'
  requestedAmountPaise: number
  requestedAmountINR: number
  remainingAmountPaise: number
  remainingAmountINR: number
  enforcementLayer: string
}

function isExpectedSpendingLimitError(
  err: unknown
): err is ApiError {
  if (typeof err !== 'object' || err === null) {
    return false
  }

  const error = err as Partial<ApiError>

  return (
    error.status === 422 &&
    error.code === 'SPENDING_LIMIT_EXCEEDED'
  )
}

async function request<T>(
  path: string,
  options?: RequestInit
): Promise<T> {
  const url = `${API_BASE_URL}${path}`

  try {
    const res = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
      ...options,
    })

    /*
     * HTTP 402 is an expected payment challenge
     * for protected resources.
     */
    if (res.status === 402) {
      return (await res.json()) as T
    }

    if (!res.ok) {
      let errorData: unknown

      try {
        errorData = await res.json()
      } catch {
        errorData = {
          timestamp: new Date().toISOString(),
          requestId: '',
          status: res.status,
          code: `HTTP_${res.status}`,
          message: `HTTP error ${res.status}: ${res.statusText}`,
          currency: 'INR',
          requestedAmountPaise: 0,
          requestedAmountINR: 0,
          remainingAmountPaise: 0,
          remainingAmountINR: 0,
          enforcementLayer: '',
        } satisfies ApiError
      }

      /*
       * 422 + SPENDING_LIMIT_EXCEEDED is an expected
       * security result for the Judge Mode overspend test.
       *
       * Keep the structured error so page.tsx can render
       * BLOCKED without treating it as an application failure.
       */
      if (isExpectedSpendingLimitError(errorData)) {
        throw errorData
      }

      throw errorData
    }

    if (res.status === 204) {
      return null as unknown as T
    }

    return (await res.json()) as T
  } catch (err) {
    /*
     * Expected Solidity spending-limit rejection.
     * Do not log it as a generic frontend API error.
     */
    if (isExpectedSpendingLimitError(err)) {
      throw err
    }

    console.error(`API Error on ${path}:`, err)
    throw err
  }
}

export const api = {
  // Dashboard
  getSummary: () =>
    request<DashboardSummary>(
      '/api/dashboard/summary'
    ),

  getBudget: (
    agentId = 'agent-demo-001'
  ) =>
    request<BudgetData>(
      `/api/agents/${agentId}/budget`
    ),

  // Providers and services
  getProviders: () =>
    request<ProviderItem[]>(
      '/api/providers'
    ),

  getServices: () =>
    request<ServiceItem[]>(
      '/api/services'
    ),

  // Purchases
  getPurchases: () =>
    request<PurchaseItem[]>(
      '/api/purchases'
    ),

  getLatestPurchase: () =>
    request<PurchaseItem | null>(
      '/api/purchases/latest'
    ),

  // Receipts
  getReceipts: () =>
    request<ReceiptItem[]>(
      '/api/receipts'
    ),

  getReceipt: (id: string) =>
    request<ReceiptItem>(
      `/api/receipts/${id}`
    ),

  verifyReceipt: (
    id: string,
    contentOverride?: string
  ) =>
    request<VerifyReceiptResponse>(
      `/api/receipts/${id}/verify`,
      {
        method: 'POST',
        body: JSON.stringify({
          contentOverride,
        }),
      }
    ),

  // Audit
  getAuditTrail: () =>
    request<AuditEventItem[]>(
      '/api/audit'
    ),

  // System
  getSystemStatus: () =>
    request<SystemStatus>(
      '/api/system/status'
    ),

  /*
   * HTTP 402 Resource Quote
   */
  requestProtectedResource: (
    serviceId: string,
    customRequestId?: string
  ) =>
    request<Http402Challenge>(
      `/api/services/${serviceId}/resource?customRequestId=${customRequestId || ''}`
    ),

  /*
   * Payments
   */
  submitPayment: (req: {
    requestId: string
    agentId: string
    providerId: string
    serviceId: string
    overridePricePaise?: number
  }) =>
    request<PaymentResponse>(
      '/api/payments',
      {
        method: 'POST',
        body: JSON.stringify(req),
      }
    ),

  /*
   * Demo Suite
   */
  runNormalPurchase: () =>
    request<PaymentResponse>(
      '/api/demo/purchase',
      {
        method: 'POST',
      }
    ),

  runRetry: () =>
    request<PaymentResponse>(
      '/api/demo/retry',
      {
        method: 'POST',
      }
    ),

  runOverspend: () =>
    request<PaymentResponse>(
      '/api/demo/overspend',
      {
        method: 'POST',
      }
    ),

  runPromptInjection: () =>
    request<any>(
      '/api/demo/prompt-injection',
      {
        method: 'POST',
      }
    ),

  resetDemo: () =>
    request<any>(
      '/api/demo/reset',
      {
        method: 'POST',
      }
    ),

  toggleMode: (
    mode: 'MOCK' | 'SEPOLIA'
  ) =>
    request<any>(
      `/api/demo/toggle-mode?mode=${mode}`,
      {
        method: 'POST',
      }
    ),

  /*
   * One-Click Multi-Service Autonomous Agent Run
   */
  runAutonomousRun: (req?: {
    agentId?: string
    prompt?: string
    simulateAdversarialOverspend?: boolean
    maxActions?: number
    customRequestId?: string
  }) =>
    request<AutonomousRunResponse>(
      '/api/agent/autonomous-run',
      {
        method: 'POST',
        body: JSON.stringify(req || {}),
      }
    ),
}