package com.costcodemo.wms.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.costcodemo.wms.core.legacy.OrddtlRecord;

public interface OrddtlRepository extends JpaRepository<OrddtlRecord, Long> {

    List<OrddtlRecord> findByOrderNumberOrderByLineNumber(String orderNumber);

    List<OrddtlRecord> findByItemNumberOrderByOrderNumber(String itemNumber);

    void deleteByOrderNumber(String orderNumber);
}
