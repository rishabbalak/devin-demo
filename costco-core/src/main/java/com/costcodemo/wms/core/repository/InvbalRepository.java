package com.costcodemo.wms.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.costcodemo.wms.core.legacy.InvbalRecord;

public interface InvbalRepository extends JpaRepository<InvbalRecord, Long> {

    List<InvbalRecord> findByWarehouseCodeOrderByLocation(String warehouseCode);

    List<InvbalRecord> findByWarehouseCodeAndItemNumberOrderByLocation(
            String warehouseCode, String itemNumber);

    List<InvbalRecord> findByItemNumberOrderByWarehouseCodeAscLocationAsc(String itemNumber);

    /**
     * Rows at or beyond a location, used to drive the terminal's "Position to" field. The
     * subfile is loaded from this point forward rather than filtered.
     */
    List<InvbalRecord> findByWarehouseCodeAndLocationGreaterThanEqualOrderByLocation(
            String warehouseCode, String location);
}
