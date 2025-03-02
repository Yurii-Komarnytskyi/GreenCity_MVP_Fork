package greencity.mapping;

import greencity.dto.friendship.FriendCardDto;
import greencity.entity.User;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class FriendCardDtoMapper extends AbstractConverter<User, FriendCardDto> {

    public FriendCardDtoMapper() {
    }

    @Override
    @NotNull
    protected FriendCardDto convert(User friend) {
        final int DEFAULT_AMOUNT_MUTUAL_FRIENDS = 0;
        Objects.requireNonNull(friend, "User cannot be null");
        return new FriendCardDto(
                friend.getId(),
                Optional.empty(),
                Optional.ofNullable(friend.getProfilePicturePath()).orElse(""),
                friend.getName(),
                friend.getRating(),
                Optional.ofNullable(friend.getCity()).orElse(""),
                DEFAULT_AMOUNT_MUTUAL_FRIENDS);
    }
}
