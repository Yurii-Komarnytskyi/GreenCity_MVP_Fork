package greencity.mapping;

import greencity.dto.event.InitiativeTypeResponseDto;
import greencity.entity.InitiativeType;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class InitiativeTypeResponseDtoMapper extends AbstractConverter<InitiativeType, InitiativeTypeResponseDto> {
    @Override
    protected InitiativeTypeResponseDto convert(InitiativeType initiativeType) {
        if (initiativeType == null) {
            return null;
        }

        return InitiativeTypeResponseDto.builder()
            .id(initiativeType.getId())
            .name(initiativeType.getName())
            .build();
    }
}
