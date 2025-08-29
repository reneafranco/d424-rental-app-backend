package com.reneafranco.rental_equipment_app.feedback;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedBackRepository extends JpaRepository<Feedback, Integer> {

    @Query("""
            SELECT feedback
            FROM Feedback feedback
            WHERE feedback.equipment.id = :equipmentId
    """)
    Page<Feedback> findAllByEquipmentId(@Param("equipmentId") Integer equipmentId, Pageable pageable);
}
