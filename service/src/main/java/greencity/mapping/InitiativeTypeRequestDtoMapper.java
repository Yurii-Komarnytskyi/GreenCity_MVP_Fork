package greencity.mapping;

import greencity.dto.event.InitiativeTypeRequestDto;
import greencity.entity.InitiativeType;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class InitiativeTypeRequestDtoMapper extends AbstractConverter<InitiativeTypeRequestDto, InitiativeType> {
    @Override
    protected InitiativeType convert(InitiativeTypeRequestDto initiativeTypeRequestDto) {
        if (initiativeTypeRequestDto == null) {
            return null;
        }

        return InitiativeType.builder()
            .name(initiativeTypeRequestDto.getName())
            .build();
    }
}
