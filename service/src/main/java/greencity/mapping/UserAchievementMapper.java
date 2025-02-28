package greencity.mapping;

import greencity.dto.userachievement.UserAchievementResponseDto;
import greencity.entity.UserAchievement;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserAchievementMapper extends AbstractConverter<UserAchievement, UserAchievementResponseDto> {

    @Override
    public UserAchievementResponseDto convert(UserAchievement userAchievement) {

        return UserAchievementResponseDto.builder()
            .achievementDate(userAchievement.getAchievementDate().toString())
            .userId(userAchievement.getUser().getId())
            .achievementId(userAchievement.getAchievement().getId())
            .achievementType(userAchievement.getAchievement().getType().toString())
            .isActive(userAchievement.getIsActive())
            .id(userAchievement.getId())
            .build();
    }
}
