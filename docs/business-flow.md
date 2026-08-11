# ServiceMind 核心业务流程

```mermaid
flowchart LR
    C[客户] --> O[创建订单]
    O --> CREATED[CREATED]
    CREATED -->|支付| PAID[PAID]
    PAID -->|发货| SHIPPED[SHIPPED]
    SHIPPED -->|确认完成| COMPLETED[COMPLETED]

    CREATED -->|取消| CANCELLED[CANCELLED]
    PAID -->|取消| CANCELLED

    O --> AS[售后申请]
    AS --> REVIEW[审核 / 处理]
    REVIEW --> T[客服工单]

    T --> ASSIGN[分配处理人]
    ASSIGN --> PROCESS[IN_PROGRESS]
    PROCESS --> WAIT[WAITING_CUSTOMER]
    WAIT --> PROCESS
    PROCESS --> RESOLVED[RESOLVED]

    KB[知识库] --> RAG[RAG 检索]
    O -.订单数据.-> TOOL[Spring AI Tool Calling]
    AS -.售后数据.-> TOOL
    T -.工单数据.-> TOOL

    RAG --> AI[AI 客服]
    TOOL --> AI
    AI --> AGENT[客服人员辅助决策]
    AI --> CUSTOMER[客户问答]
```

## 业务链路

1. 客户产生订单，订单按照受控状态机流转。
2. 订单产生问题后可进入售后申请流程。
3. 售后问题进一步进入客服工单处理。
4. 工单支持分配、处理中、等待客户和解决等状态。
5. AI 客服通过知识库 RAG 获取政策与说明类知识。
6. AI 客服通过只读 Tool Calling 查询订单、售后和工单数据。
7. 最终为客服人员或客户提供带业务上下文的回答。
