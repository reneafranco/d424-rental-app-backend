package com.reneafranco.rental_equipment_app.equipment;

import org.springframework.data.jpa.domain.Specification;

public class EquipmentSpecification {

    public static Specification<Equipment> withOwnerId(Integer ownerId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("id"), ownerId);
    }
}
