package kz.csi.test_task.dto;

import java.util.List;

public record AutopartDto(
        Long id,
        String detailName,
        Integer price,
        Integer quantity,
        Integer total,
        List<AutopartDto> children
) {}
