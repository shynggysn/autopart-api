package kz.csi.test_task.mapper;

import kz.csi.test_task.dto.AutopartDto;
import kz.csi.test_task.entity.Autopart;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AutopartMapper {
    Autopart map(AutopartDto part);

    @InheritInverseConfiguration
    AutopartDto map(Autopart dto);
}
