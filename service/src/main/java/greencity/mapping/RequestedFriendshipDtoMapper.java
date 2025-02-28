package greencity.mapping;

import com.google.api.gax.rpc.InvalidArgumentException;
import greencity.dto.friendship.RequestedFriendshipDto;
import greencity.entity.Friendship;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class RequestedFriendshipDtoMapper extends AbstractConverter<Friendship, RequestedFriendshipDto> {

    @Override
    protected RequestedFriendshipDto convert(Friendship source) {
        if (Objects.isNull(source) || Objects.isNull(source.getUser()) || Objects.isNull(source.getFriend())) {
            throw new IllegalArgumentException(
                "RequestedFriendshipDto::convert argument source or user or friend is null");
        }
        return new RequestedFriendshipDto(source.getUser().getId(), source.getFriend().getId());
    }
}
