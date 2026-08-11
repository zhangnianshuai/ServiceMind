# ServiceMind 系统架构

```mermaid
flowchart TB
    U[用户 / 客服人员] --> FE[Angular 21 + Angular Material]

    subgraph Frontend[前端层]
        FE --> AUTHUI[登录 / Session 状态恢复]
        FE --> ORDERUI[订单工作台]
        FE --> ASUI[售后工作台]
        FE --> TICKETUI[工单工作台]
        FE --> KBUI[知识库管理]
        FE --> CHATUI[AI 客服 / BYOK]
    end

    FE -->|HTTP / JSON| API[Spring Boot 3.5 REST API]

    subgraph Backend[后端应用层]
        API --> SEC[Spring Security]
        SEC --> SESSION[HttpSession + BCrypt]
        SEC --> RBAC[ADMIN / AGENT RBAC]

        API --> ORDER[订单领域]
        API --> AFTER[售后领域]
        API --> TICKET[工单领域]
        API --> KB[知识库领域]
        API --> CHAT[AI 客服服务]

        CHAT --> RAG[RAG 检索]
        CHAT --> TOOLS[Spring AI Tool Calling]
        CHAT --> PLATFORM[平台模型]
        CHAT --> BYOK[临时 BYOK Session]

        TOOLS --> ORDER
        TOOLS --> AFTER
        TOOLS --> TICKET
        RAG --> KB
    end

    subgraph Data[数据层]
        PG[(PostgreSQL 16)]
        VECTOR[(pgvector)]
        FLYWAY[Flyway Migration]
    end

    ORDER --> PG
    AFTER --> PG
    TICKET --> PG
    KB --> PG
    KB --> VECTOR
    RAG --> VECTOR
    FLYWAY --> PG

    subgraph DemoSecurity[公网 Demo 安全]
        LIMIT[请求限流 / 每日额度]
        OWNER[BYOK Session 用户绑定]
        UPLOAD[上传大小限制]
        ERROR[生产环境错误信息隐藏]
        HEALTH[Actuator 仅健康检查]
    end

    CHAT --> LIMIT
    BYOK --> OWNER
    API --> UPLOAD
    API --> ERROR
    API --> HEALTH
```

## 说明

- 前端采用 Angular SPA，通过后端 REST API 完成业务操作。
- Spring Security 使用服务端 Session 认证，不在浏览器本地持久化 JWT。
- ADMIN 与 AGENT 采用 RBAC 权限控制。
- 订单、售后、工单、知识库构成核心业务域。
- AI 客服同时使用知识库 RAG 与只读业务 Tool Calling。
- 平台模型与 BYOK 模式共存，BYOK Key 仅用于短期会话。
- PostgreSQL 保存业务数据，pgvector 保存知识向量。
- Flyway 统一管理数据库结构和演示数据迁移。
