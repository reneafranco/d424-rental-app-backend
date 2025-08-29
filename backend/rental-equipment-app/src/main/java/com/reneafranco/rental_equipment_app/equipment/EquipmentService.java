package com.reneafranco.rental_equipment_app.equipment;

import com.reneafranco.rental_equipment_app.common.PageResponse;
import com.reneafranco.rental_equipment_app.exception.OperationNotPermittedException;
import com.reneafranco.rental_equipment_app.file.FileStorageService;
import com.reneafranco.rental_equipment_app.history.EquipmentTransactionHistory;
import com.reneafranco.rental_equipment_app.history.EquipmentTransactionHistoryRepository;
import com.reneafranco.rental_equipment_app.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.reneafranco.rental_equipment_app.equipment.EquipmentSpecification.withOwnerId;

@Service
@Slf4j
@Transactional

public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;
    private final EquipmentTransactionHistoryRepository transactionHistoryRepository;
    private final FileStorageService fileStorageService;

    public EquipmentService(EquipmentRepository equipmentRepository, EquipmentMapper equipmentMapper,
                            EquipmentTransactionHistoryRepository transactionHistoryRepository,
                            FileStorageService fileStorageService) {
        this.equipmentRepository = equipmentRepository;
        this.equipmentMapper = equipmentMapper;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.fileStorageService = fileStorageService;
    }

    public Integer save(EquipmentRequest request, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Equipment equipment = equipmentMapper.toEquipment(request);
        equipment.setOwner(user);
        return equipmentRepository.save(equipment).getId();
    }

    public EquipmentResponse findById(Integer equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .map(equipmentMapper::toEquipmentResponse)
                .orElseThrow(() -> new EntityNotFoundException("No equipment found with ID:: " + equipmentId));
    }

    public PageResponse<EquipmentResponse> findAllEquipment(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Equipment> equipmentPage = equipmentRepository.findAllDisplayableEquipment(pageable, user.getId());
        List<EquipmentResponse> equipmentResponseList = equipmentPage.stream()
                .map(equipmentMapper::toEquipmentResponse)
                .toList();
        return new PageResponse<>(
                equipmentResponseList,
                equipmentPage.getNumber(),
                equipmentPage.getSize(),
                equipmentPage.getTotalElements(),
                equipmentPage.getTotalPages(),
                equipmentPage.isFirst(),
                equipmentPage.isLast()
        );
    }

    public PageResponse<EquipmentResponse> findAllEquipmentByOwner(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Equipment> equipmentPage = equipmentRepository.findAll(withOwnerId(user.getId()), pageable);
        List<EquipmentResponse> equipmentResponseList = equipmentPage.stream()
                .map(equipmentMapper::toEquipmentResponse)
                .toList();
        return new PageResponse<>(
                equipmentResponseList,
                equipmentPage.getNumber(),
                equipmentPage.getSize(),
                equipmentPage.getTotalElements(),
                equipmentPage.getTotalPages(),
                equipmentPage.isFirst(),
                equipmentPage.isLast()
        );
    }

    public Integer updateShareableStatus(Integer equipmentId, Authentication connectedUser) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EntityNotFoundException("No equipment found with ID:: " + equipmentId));
        User user = (User) connectedUser.getPrincipal();
        if (!Objects.equals(equipment.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others' equipment shareable status");
        }
        equipment.setShareable(!equipment.isShareable());
        equipmentRepository.save(equipment);
        return equipmentId;
    }

    public Integer updateArchivedStatus(Integer equipmentId, Authentication connectedUser) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EntityNotFoundException("No equipment found with ID:: " + equipmentId));
        User user = (User) connectedUser.getPrincipal();
        if (!Objects.equals(equipment.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others' equipment archived status");
        }
        equipment.setArchived(!equipment.isArchived());
        equipmentRepository.save(equipment);
        return equipmentId;
    }

    public Integer borrowEquipment(Integer equipmentId, Authentication connectedUser) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EntityNotFoundException("No equipment found with ID:: " + equipmentId));
        if (equipment.isArchived() || !equipment.isShareable()) {
            throw new OperationNotPermittedException("The requested equipment cannot be borrowed since it is archived or not shareable");
        }
        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(equipment.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own equipment");
        }
        boolean isAlreadyBorrowedByUser = transactionHistoryRepository.isAlreadyBorrowedByUser(equipmentId, user.getId());
        if (isAlreadyBorrowedByUser) {
            throw new OperationNotPermittedException("You already borrowed this equipment and it is still not returned or the return is not approved by the owner");
        }
        boolean isAlreadyBorrowedByOtherUser = transactionHistoryRepository.isAlreadyBorrowed(equipmentId);
        if (isAlreadyBorrowedByOtherUser) {
            throw new OperationNotPermittedException("The requested equipment is already borrowed");
        }

        EquipmentTransactionHistory transactionHistory = EquipmentTransactionHistory.builder()
                .user(user)
                .equipment(equipment)
                .returned(false)
                .returnApproved(false)
                .build();
        return transactionHistoryRepository.save(transactionHistory).getId();
    }

    public Integer returnBorrowedEquipment(Integer equipmentId, Authentication connectedUser) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EntityNotFoundException("No equipment found with ID:: " + equipmentId));
        if (equipment.isArchived() || !equipment.isShareable()) {
            throw new OperationNotPermittedException("The requested equipment is archived or not shareable");
        }
        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(equipment.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own equipment");
        }

        EquipmentTransactionHistory transactionHistory = transactionHistoryRepository.findByEquipmentIdAndUserId(equipmentId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this equipment"));

        transactionHistory.setReturned(true);
        return transactionHistoryRepository.save(transactionHistory).getId();
    }

    public Integer approveReturnBorrowedEquipment(Integer equipmentId, Authentication connectedUser) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EntityNotFoundException("No equipment found with ID:: " + equipmentId));
        if (equipment.isArchived() || !equipment.isShareable()) {
            throw new OperationNotPermittedException("The requested equipment is archived or not shareable");
        }
        User user = (User) connectedUser.getPrincipal();
        if (!Objects.equals(equipment.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot approve the return of equipment you do not own");
        }

        EquipmentTransactionHistory transactionHistory = transactionHistoryRepository.findByEquipmentIdAndOwnerId(equipmentId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The equipment is not returned yet. You cannot approve its return"));

        transactionHistory.setReturnApproved(true);
        return transactionHistoryRepository.save(transactionHistory).getId();
    }

    public void uploadEquipmentPicture(MultipartFile file, Authentication connectedUser, Integer equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EntityNotFoundException("No equipment found with ID:: " + equipmentId));
        User user = (User) connectedUser.getPrincipal();
        var picture = fileStorageService.saveFile(file, equipmentId, user.getId());
        equipment.setEquipmentCover(picture);
        equipmentRepository.save(equipment);
    }

    public PageResponse<BorrowedEquipmentResponse> findAllBorrowedEquipment(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<EquipmentTransactionHistory> borrowedEquipmentPage = transactionHistoryRepository.findAllBorrowedEquipment(pageable, user.getId());
        List<BorrowedEquipmentResponse> responseList = borrowedEquipmentPage.stream()
                .map(equipmentMapper::toBorrowedEquipmentResponse)
                .toList();
        return new PageResponse<>(
                responseList,
                borrowedEquipmentPage.getNumber(),
                borrowedEquipmentPage.getSize(),
                borrowedEquipmentPage.getTotalElements(),
                borrowedEquipmentPage.getTotalPages(),
                borrowedEquipmentPage.isFirst(),
                borrowedEquipmentPage.isLast()
        );
    }

    public PageResponse<BorrowedEquipmentResponse> findAllReturnedEquipment(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<EquipmentTransactionHistory>  allBorrowedEquipment = transactionHistoryRepository.findAllReturnedEquipment(pageable, user.getId());
        List<BorrowedEquipmentResponse> responseList = allBorrowedEquipment.stream()
                .map(equipmentMapper::toBorrowedEquipmentResponse)
                .toList();
        return new PageResponse<>(
                responseList,
                allBorrowedEquipment.getNumber(),
                allBorrowedEquipment.getSize(),
                allBorrowedEquipment.getTotalElements(),
                allBorrowedEquipment.getTotalPages(),
                allBorrowedEquipment.isFirst(),
                allBorrowedEquipment.isLast()
        );
    }
}