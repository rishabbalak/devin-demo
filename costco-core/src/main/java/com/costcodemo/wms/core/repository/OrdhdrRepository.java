package com.costcodemo.wms.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.costcodemo.wms.core.legacy.OrdhdrRecord;

public interface OrdhdrRepository extends JpaRepository<OrdhdrRecord, String> {

    List<OrdhdrRecord> findByMemberNumberOrderByOrderNumberDesc(String memberNumber);

    List<OrdhdrRecord> findByWarehouseCodeOrderByOrderNumberDesc(String warehouseCode);

    List<OrdhdrRecord> findByStatusCodeOrderByOrderNumber(String statusCode);

    List<OrdhdrRecord> findAllByOrderByOrderNumberDesc();
}
