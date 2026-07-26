package com.costcodemo.wms.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.costcodemo.wms.core.legacy.MbrmastRecord;

public interface MbrmastRepository extends JpaRepository<MbrmastRecord, String> {

    List<MbrmastRecord> findByTierCodeOrderByMemberNumber(String tierCode);

    List<MbrmastRecord> findByHomeWarehouseOrderByMemberNumber(String homeWarehouse);

    @Query("select m from MbrmastRecord m where upper(m.name) like upper(concat('%', :name, '%')) "
            + "order by m.memberNumber")
    List<MbrmastRecord> searchByName(@Param("name") String name);

    /** Members whose CYYMMDD renewal date falls on or before the supplied CYYMMDD value. */
    @Query("select m from MbrmastRecord m where m.renewalDate <> 0 and m.renewalDate <= :cutoff "
            + "order by m.renewalDate")
    List<MbrmastRecord> findExpiringOnOrBefore(@Param("cutoff") Integer cutoffCyymmdd);
}
