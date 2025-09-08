package com.olsonsolution.common.spring.application.datasource.order.repository;

import com.olsonsolution.common.spring.application.annotation.JpaSpec;
import com.olsonsolution.common.spring.application.datasource.order.entity.OrderData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JpaSpec("DeliveryIndex")
public interface OrderJpaRepository extends JpaRepository<OrderData, String> {
}
