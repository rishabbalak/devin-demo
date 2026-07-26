package com.costcodemo.wms.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.costcodemo.wms.core.legacy.ItemmastRecord;

public interface ItemmastRepository extends JpaRepository<ItemmastRecord, String> {

    List<ItemmastRecord> findByDepartmentOrderByItemNumber(String department);

    List<ItemmastRecord> findByStatusFlagOrderByItemNumber(String statusFlag);

    @Query("select i from ItemmastRecord i where upper(i.description) like upper(concat('%', :text, '%')) "
            + "order by i.itemNumber")
    List<ItemmastRecord> searchByDescription(@Param("text") String text);
}
