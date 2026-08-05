package com.zhangnianshuai.servicemind.domain.customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerNoIgnoreCase(String customerNo);

    boolean existsByCustomerNoIgnoreCase(String customerNo);

    boolean existsByEmailIgnoreCase(String email);
}