package greencity.mapping;

import greencity.dto.achievement.AchievementResponseDto;
import greencity.entity.Achievement;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class AchievementMapper extends AbstractConverter<Achievement, AchievementResponseDto> {

    @Override
    public AchievementResponseDto convert(Achievement achievement) {

        return AchievementResponseDto
            .builder()
            .id(achievement.getId())
            .type(String.valueOf(achievement.getType()))
            .conditions(achievement.getConditions())
            .requiredRate(achievement.getRequiredRate())
            .build();
    }
}
