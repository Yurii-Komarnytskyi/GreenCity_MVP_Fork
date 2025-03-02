package greencity.service;

import greencity.dto.friendship.FriendPageDto;
import greencity.dto.friendship.FriendshipVO;

import java.util.Optional;

public interface FriendPageService {
    FriendPageDto assembleFriendPage(Long userId, Optional<Long> friendshipIdOptional);
}
