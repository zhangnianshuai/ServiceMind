package com.zhangnianshuai.servicemind.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SecurityProbeController {

    @GetMapping("/api/orders")
    String readOrders() {
        return "orders";
    }

    @PostMapping("/api/orders")
    String createOrder() {
        return "created";
    }

    @GetMapping("/api/knowledge/documents")
    String readKnowledgeDocuments() {
        return "knowledge";
    }

    @PostMapping("/api/knowledge/documents")
    String createKnowledgeDocument() {
        return "created";
    }
}