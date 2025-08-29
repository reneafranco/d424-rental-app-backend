package com.reneafranco.rental_equipment_app.feedback;


import com.reneafranco.rental_equipment_app.common.BaseEntity;
import com.reneafranco.rental_equipment_app.equipment.Equipment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Feedback extends BaseEntity {

    @Column
    private Double note;
    private String comment;
    @ManyToOne
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;
}