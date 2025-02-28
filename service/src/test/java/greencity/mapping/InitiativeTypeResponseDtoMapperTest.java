package greencity.mapping;

import greencity.dto.event.InitiativeTypeResponseDto;
import greencity.entity.InitiativeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InitiativeTypeResponseDtoMapperTest {
    private final InitiativeTypeResponseDtoMapper mapper = new InitiativeTypeResponseDtoMapper();

    @Test
    void convertTest() {
        InitiativeType initiativeType = InitiativeType.builder()
            .id(1L)
            .name("Environmental")
            .build();

        InitiativeTypeResponseDto result = mapper.convert(initiativeType);

        assertNotNull(result, "The result should not be null");
        assertEquals(initiativeType.getId(), result.getId(), "The ID should be mapped correctly");
        assertEquals(initiativeType.getName(), result.getName(), "The name should be mapped correctly");
    }
}
