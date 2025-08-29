package com.reneafranco.rental_equipment_app.equipment;

import com.reneafranco.rental_equipment_app.common.BaseEntity;
import com.reneafranco.rental_equipment_app.feedback.Feedback;
import com.reneafranco.rental_equipment_app.history.EquipmentTransactionHistory;
import com.reneafranco.rental_equipment_app.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Equipment extends BaseEntity {
    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String equipmentCover;
    private boolean archived;
    private boolean shareable;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "equipment")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "equipment")
    private List<EquipmentTransactionHistory> histories;

    @Transient
    public double getRate() {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return 0.0;
        }
        var rate = this.feedbacks.stream()
                .mapToDouble(Feedback::getNote)
                .average()
                .orElse(0.0);
        double roundedRate = Math.round(rate * 10.0) / 10.0;

        return roundedRate;
    }
}
