package com.vibecode.coding.service;

import com.vibecode.coding.dto.SheetDtos;
import com.vibecode.coding.entity.DsaSheet;
import com.vibecode.coding.exceptions.ResourceNotFoundException;
import com.vibecode.coding.repository.DsaSheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SheetService {

    private final DsaSheetRepository dsaSheetRepository;

    @Transactional
    public SheetDtos.Response create(SheetDtos.Create req) {
        DsaSheet sheet = DsaSheet.builder()
                .name(req.sheetName())
                .description(req.description())
                .build();
        DsaSheet saved = dsaSheetRepository.save(sheet);
        return new SheetDtos.Response(saved.getId(), saved.getName(), saved.getDescription());
    }

    @Transactional(readOnly = true)
    public DsaSheet getOrThrow(Integer sheetId) {
        return dsaSheetRepository.findById(sheetId)
                .orElseThrow(() -> new ResourceNotFoundException("Sheet not found: " + sheetId));
    }
}

