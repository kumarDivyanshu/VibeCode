package com.vibecode.coding.repository;

import com.vibecode.coding.entity.Question;
import com.vibecode.coding.entity.SheetQuestion;
import com.vibecode.coding.entity.SheetQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SheetQuestionRepository extends JpaRepository<SheetQuestion, SheetQuestionId> {

    List<SheetQuestion> findBySheet_Id(Integer sheetId);

    boolean existsBySheet_IdAndQuestion_Id(Integer sheetId, String questionId);

    void deleteBySheet_IdAndQuestion_Id(Integer sheetId, String questionId);

    @Query("select sq.question from SheetQuestion sq where sq.sheet.id = :sheetId")
    List<Question> findQuestionsForSheet(@Param("sheetId") Integer sheetId);
}

