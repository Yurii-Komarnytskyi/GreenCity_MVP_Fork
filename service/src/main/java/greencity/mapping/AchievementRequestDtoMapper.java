package greencity.mapping;

import greencity.dto.achievement.AchievementRequestDto;
import greencity.entity.Achievement;
import greencity.enums.AchievementType;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AchievementRequestDtoMapper extends AbstractConverter<AchievementRequestDto, Achievement> {

    @Override
    public Achievement convert(AchievementRequestDto achievementRequestDto) {

        return Achievement.builder()
            .conditions(achievementRequestDto.getConditions())
            .type(AchievementType.valueOf(achievementRequestDto.getType()))
            .requiredRate(achievementRequestDto.getRequiredUserRating())
            .build();
    }
}
