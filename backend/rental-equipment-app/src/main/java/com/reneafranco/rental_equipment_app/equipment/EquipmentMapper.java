package com.reneafranco.rental_equipment_app.equipment;

import com.reneafranco.rental_equipment_app.file.FileUtils;
import com.reneafranco.rental_equipment_app.history.EquipmentTransactionHistory;
import org.springframework.stereotype.Service;


@Service
public class EquipmentMapper {
    public Equipment toEquipment(EquipmentRequest request) {
        return Equipment.builder()
                .id(request.id())
                .title(request.title())
                .isbn(request.isbn())
                .authorName(request.authorName())
                .synopsis(request.synopsis())
                .archived(false)
                .shareable(request.shareable())
                .build();
    }

    public EquipmentResponse toEquipmentResponse(Equipment equipment) {
        return EquipmentResponse.builder()
                .id(equipment.getId())
                .title(equipment.getTitle())
                .authorName(equipment.getAuthorName())
                .isbn(equipment.getIsbn())
                .synopsis(equipment.getSynopsis())
                .rate(equipment.getRate())
                .archived(equipment.isArchived())
                .shareable(equipment.isShareable())
                .owner(equipment.getOwner().fullName())
                .cover(FileUtils.readFileFromLocation(equipment.getEquipmentCover()))
                .build();
    }

    public BorrowedEquipmentResponse toBorrowedEquipmentResponse(EquipmentTransactionHistory history) {
        return BorrowedEquipmentResponse.builder()
                .id(history.getEquipment().getId())
                .title(history.getEquipment().getTitle())
                .authorName(history.getEquipment().getAuthorName())
                .isbn(history.getEquipment().getIsbn())
                .rate(history.getEquipment().getRate())
                .returned(history.isReturned())
                .returnApproved(history.isReturnApproved())
                .build();
    }
}