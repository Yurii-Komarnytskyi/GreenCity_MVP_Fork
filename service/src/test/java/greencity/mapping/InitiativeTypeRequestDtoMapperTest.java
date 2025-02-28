package greencity.mapping;

import greencity.dto.event.InitiativeTypeRequestDto;
import greencity.entity.InitiativeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InitiativeTypeRequestDtoMapperTest {
    private final InitiativeTypeRequestDtoMapper mapper = new InitiativeTypeRequestDtoMapper();

    @Test
    void convertTest() {
        InitiativeTypeRequestDto dto = InitiativeTypeRequestDto.builder()
            .name("Environmental")
            .build();

        InitiativeType result = mapper.convert(dto);

        assertNotNull(result, "The result should not be null");
        assertEquals(dto.getName(), result.getName(), "The name should be mapped correctly");
    }
}
