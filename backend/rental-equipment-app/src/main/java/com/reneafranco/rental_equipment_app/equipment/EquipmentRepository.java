package com.reneafranco.rental_equipment_app.equipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EquipmentRepository extends JpaRepository<Equipment, Integer>, JpaSpecificationExecutor<Equipment> {
    @Query("""
            SELECT equipment
            FROM Equipment equipment
            WHERE equipment.archived = false
            AND equipment.shareable = true
            AND equipment.owner.id != :userId
            """)
    Page<Equipment> findAllDisplayableEquipment(Pageable pageable, Integer userId);
}
