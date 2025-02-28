package greencity.validator;

import greencity.annotations.ConsistentDateTime;
import greencity.dto.event.EventDateInfoDto;
import greencity.dto.event.EventDateInfoRequestDto;
import greencity.dto.event.EventDateInfoResponseDto;
import greencity.dto.event.EventDateInfoUpdateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class ConsistentDateTimeFieldsValidator implements ConstraintValidator<ConsistentDateTime, EventDateInfoDto> {

    @Override
    public boolean isValid(EventDateInfoDto dto, ConstraintValidatorContext context) {
        LocalDateTime startTime = dto.getEventTimeStart();
        LocalDateTime endTime = dto.getEventTimeEnd();

        if (startTime == null || endTime == null) {
            return true;
        }

        return startTime.isBefore(endTime);
    }
}
