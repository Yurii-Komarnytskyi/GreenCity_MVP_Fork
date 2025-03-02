package greencity.mapping;

import greencity.dto.friendship.FriendshipVO;
import greencity.dto.user.UserVO;
import greencity.entity.Friendship;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.AbstractConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FriendshipVOMapper extends AbstractConverter<Friendship, FriendshipVO> {

    private final UserVOMapper userVOMapper;

    @Autowired
    public FriendshipVOMapper(UserVOMapper userVOMapper) {
        this.userVOMapper = userVOMapper;
    }

    @Override
    protected FriendshipVO convert(@NotNull Friendship source) {
        Optional<UserVO> user = Optional.ofNullable(userVOMapper.convert(source.getUser()));
        Optional<UserVO> friend = Optional.ofNullable(userVOMapper.convert(source.getFriend()));

        if(user.isEmpty() || friend.isEmpty()) {
            throw new IllegalArgumentException("Neither User nor Friend can be null.");
        }

        return new FriendshipVO(
                source.getId(),
                user.get(),
                friend.get(),
                source.getStatus(),
                source.getRequestedAt(),
                source.getFriendshipRequestExpiration()
        );
    }
}
