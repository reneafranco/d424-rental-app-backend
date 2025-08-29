package com.reneafranco.rental_equipment_app.feedback;



import com.reneafranco.rental_equipment_app.common.PageResponse;
import com.reneafranco.rental_equipment_app.equipment.Equipment;
import com.reneafranco.rental_equipment_app.equipment.EquipmentRepository;
import com.reneafranco.rental_equipment_app.exception.OperationNotPermittedException;
import com.reneafranco.rental_equipment_app.user.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedBackRepository feedBackRepository;
    private final EquipmentRepository equipmentRepository;
    private final FeedbackMapper feedbackMapper;

    public Integer save(FeedbackRequest request, Authentication connectedUser) {
        Equipment equipment = equipmentRepository.findById(request.equipmentId())
                .orElseThrow(() -> new EntityNotFoundException("No equipment found with ID:: " + request.equipmentId()));
        if (equipment.isArchived() || !equipment.isShareable()) {
            throw new OperationNotPermittedException("You cannot give feedback for archived or not shareable equipment");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(equipment.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot give feedback to your own equipment");
        }
        Feedback feedback = feedbackMapper.toFeedback(request);
        return feedBackRepository.save(feedback).getId();
    }

    @Transactional
    public PageResponse<FeedbackResponse> findAllFeedbacksByEquipment(Integer equipmentId, int page, int size, Authentication connectedUser) {
        Pageable pageable = (Pageable) PageRequest.of(page, size);
        User user = ((User) connectedUser.getPrincipal());
        Page<Feedback> feedbacks = feedBackRepository.findAllByEquipmentId(equipmentId, (org.springframework.data.domain.Pageable) pageable);
        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(f -> feedbackMapper.toFeedbackResponse(f, user.getId()))
                .toList();
        return new PageResponse<>(
                feedbackResponses,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );

    }
}
