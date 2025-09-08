package com.olsonsolution.common.spring.application.datasource.order.repository;

import com.olsonsolution.common.spring.application.annotation.JpaSpec;
import com.olsonsolution.common.spring.application.datasource.order.entity.DeliveryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JpaSpec("DeliveryIndex")
public interface DeliveryJpaRepository extends JpaRepository<DeliveryData, String> {
}
