package com.costcodemo.wms.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.costcodemo.wms.core.legacy.WhsmastRecord;

public interface WhsmastRepository extends JpaRepository<WhsmastRecord, String> {

    List<WhsmastRecord> findAllByOrderByWarehouseCode();

    List<WhsmastRecord> findByCountryOrderByWarehouseCode(String country);
}
