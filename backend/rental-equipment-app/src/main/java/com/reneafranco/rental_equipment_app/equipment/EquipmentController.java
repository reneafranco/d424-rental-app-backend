package com.reneafranco.rental_equipment_app.equipment;

import com.reneafranco.rental_equipment_app.common.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService service;

    @PostMapping
    public ResponseEntity<Integer> saveEquipment(
            @Valid @RequestBody EquipmentRequest request,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.save(request, connectedUser));
    }

    @GetMapping("/{equipment-id}")
    public ResponseEntity<EquipmentResponse> findEquipmentById(
            @PathVariable("equipment-id") Integer equipmentId
    ) {
        return ResponseEntity.ok(service.findById(equipmentId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<EquipmentResponse>> findAllEquipments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllEquipment(page, size, connectedUser));
    }

    @GetMapping("/owner")
    public ResponseEntity<PageResponse<EquipmentResponse>> findAllEquipmentsByOwner(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllEquipmentByOwner(page, size, connectedUser));
    }

    @GetMapping("/borrowed")
    public ResponseEntity<PageResponse<BorrowedEquipmentResponse>> findAllBorrowedEquipments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllBorrowedEquipment(page, size, connectedUser));
    }

    @GetMapping("/returned")
    public ResponseEntity<PageResponse<BorrowedEquipmentResponse>> findAllReturnedEquipments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllReturnedEquipment(page, size, connectedUser));
    }

    @PatchMapping("/shareable/{equipment-id}")
    public ResponseEntity<Integer> updateShareableStatus(
            @PathVariable("equipment-id") Integer equipmentId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.updateShareableStatus(equipmentId, connectedUser));
    }

    @PatchMapping("/archived/{equipment-id}")
    public ResponseEntity<Integer> updateArchivedStatus(
            @PathVariable("equipment-id") Integer equipmentId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.updateArchivedStatus(equipmentId, connectedUser));
    }

    @PostMapping("borrow/{equipment-id}")
    public ResponseEntity<Integer> borrowEquipment(
            @PathVariable("equipment-id") Integer equipmentId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.borrowEquipment(equipmentId, connectedUser));
    }

    @PatchMapping("borrow/return/{equipment-id}")
    public ResponseEntity<Integer> returnBorrowedEquipment(
            @PathVariable("equipment-id") Integer equipmentId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.returnBorrowedEquipment(equipmentId, connectedUser));
    }

    @PatchMapping("borrow/return/approve/{equipment-id}")
    public ResponseEntity<Integer> approveReturnBorrowedEquipment(
            @PathVariable("equipment-id") Integer equipmentId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.approveReturnBorrowedEquipment(equipmentId, connectedUser));
    }

    @PostMapping(value = "/cover/{equipment-id}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadEquipmentPicture(
            @PathVariable("equipment-id") Integer equipmentId,
            @Parameter() @RequestPart("file") MultipartFile file,
            Authentication connectedUser
    ) {
        service.uploadEquipmentPicture(file, connectedUser, equipmentId);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{equipment-id}")
    public ResponseEntity<Void> deleteEquipment(
            @PathVariable("equipment-id") Integer equipmentId,
            Authentication connectedUser
    ) {
        service.deleteEquipment(equipmentId, connectedUser);
        return ResponseEntity.noContent().build();
    }

}
