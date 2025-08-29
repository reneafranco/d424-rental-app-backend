package com.reneafranco.rental_equipment_app.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EquipmentTransactionHistoryRepository extends JpaRepository<EquipmentTransactionHistory, Integer> {

    @Query("""
            SELECT
            (COUNT(*) > 0) AS isBorrowed
            FROM EquipmentTransactionHistory transaction
            WHERE transaction.user.id = :userId
            AND transaction.equipment.id = :equipmentId
            AND transaction.returnApproved = false
            """)
    boolean isAlreadyBorrowedByUser(@Param("equipmentId") Integer equipmentId, @Param("userId") Integer userId);

    @Query("""
            SELECT
            (COUNT(*) > 0) AS isBorrowed
            FROM EquipmentTransactionHistory transaction
            WHERE transaction.equipment.id = :equipmentId
            AND transaction.returnApproved = false
            """)
    boolean isAlreadyBorrowed(@Param("equipmentId") Integer equipmentId);

    @Query("""
            SELECT transaction
            FROM EquipmentTransactionHistory transaction
            WHERE transaction.user.id = :userId
            AND transaction.equipment.id = :equipmentId
            AND transaction.returned = false
            AND transaction.returnApproved = false
            """)
    Optional<EquipmentTransactionHistory> findByEquipmentIdAndUserId(@Param("equipmentId") Integer equipmentId, @Param("userId") Integer userId);

    @Query("""
            SELECT transaction
            FROM EquipmentTransactionHistory transaction
            WHERE transaction.equipment.owner.id = :userId
            AND transaction.equipment.id = :equipmentId
            AND transaction.returned = true
            AND transaction.returnApproved = false
            """)
    Optional<EquipmentTransactionHistory> findByEquipmentIdAndOwnerId(@Param("equipmentId") Integer equipmentId, @Param("userId") Integer userId);

    @Query("""
            SELECT history
            FROM EquipmentTransactionHistory history
            WHERE history.user.id = :userId
            """)
    Page<EquipmentTransactionHistory> findAllBorrowedEquipment(Pageable pageable, @Param("userId") Integer userId);

    @Query("""
            SELECT history
            FROM EquipmentTransactionHistory history
            WHERE history.equipment.owner.id = :userId
            """)
    Page<EquipmentTransactionHistory> findAllReturnedEquipment(Pageable pageable, @Param("userId") Integer userId);
}

