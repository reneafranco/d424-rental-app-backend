package com.reneafranco.rental_equipment_app.history;

import com.reneafranco.rental_equipment_app.common.BaseEntity;
import com.reneafranco.rental_equipment_app.equipment.Equipment;
import com.reneafranco.rental_equipment_app.user.User;
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
public class EquipmentTransactionHistory extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    private boolean returned;
    private boolean returnApproved;
}
