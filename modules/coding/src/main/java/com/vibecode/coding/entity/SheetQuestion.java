package com.vibecode.coding.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sheet_question", schema = "videcode")
public class SheetQuestion {

    @EmbeddedId
    private SheetQuestionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sheetId")
    @JoinColumn(name = "sheet_id", nullable = false)
    private DsaSheet sheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false, columnDefinition = "char(36)")
    private Question question;
}
