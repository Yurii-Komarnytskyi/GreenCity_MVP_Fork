package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.friendship.FriendCardDto;
import greencity.dto.friendship.RequestedFriendshipDto;
import greencity.dto.notification.NotificationRequestDto;
import greencity.dto.user.UserVO;
import greencity.entity.Friendship;
import greencity.enums.FriendshipStatus;
import greencity.enums.NotificationSection;
import greencity.repository.FriendshipRepo;
import greencity.repository.UserRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepo friendshipRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final Long ONE_WEEK = 1L;
    private final Function<Friendship, Long> getFriendId = friendship -> friendship.getFriend().getId();

    @Autowired
    public FriendshipServiceImpl(
        FriendshipRepo friendshipRepo,
        UserRepo userRepo,
        NotificationService notificationServise,
        ModelMapper modelMapper) {
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationServise;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<FriendCardDto> getAllMutualFriendsByUserId(Long userId, Long targetUserId) {
        List<FriendCardDto> userFriends = new ArrayList<>(getAllFriendCardsOfUserById(userId));
        List<FriendCardDto> targetUserFriends = new ArrayList<>(getAllFriendCardsOfUserById(targetUserId));
        return (userFriends.retainAll(targetUserFriends)) ? userFriends : new ArrayList<>();
    }

    @Override
    public List<FriendCardDto> getAllFriendsByUserId(Long id) {
        return getAllFriendCardsOfUserById(id);
    }

    @Override
    // TODO sprint 4 "Recommendations for friends #109"
    public PageableDto<FriendCardDto> recommendFriendsForUser(UserVO user) {
        return null;
    }

    @Override
    public boolean requestFriendshipByUserId(Long senderId, Long recipientId) {
        Optional<Friendship> friendshipOptional = getFriendshipByUserIdOrderInsensitive(senderId, recipientId);
        Friendship friendship = friendshipOptional.orElseGet(Friendship::new);
        boolean isNotEligibleForResending = friendshipOptional.isPresent()
            && friendshipOptional.get().getStatus() != FriendshipStatus.CANCELLED
            && friendshipOptional.get().getStatus() != FriendshipStatus.DECLINED;

        if (isNotEligibleForResending || senderId.equals(recipientId)) {
            return false;
        } else if (friendshipOptional.isEmpty()) {
            friendship.setUser(userRepo.getReferenceById(senderId));
            friendship.setFriend(userRepo.getReferenceById(recipientId));
        }

        friendship.setStatus(FriendshipStatus.REQUESTED);
        friendship.setRequestedAt(LocalDateTime.now());
        friendship.setFriendshipRequestExpiration(LocalDateTime.now().plusWeeks(ONE_WEEK));
        friendshipRepo.save(friendship);

        sendNotification(senderId, recipientId, "New Friendship Req.");
        return true;
    }

    @Override
    public boolean cancelFriendshipRequestByUserId(Long senderId, Long recipientId) {
        Optional<Friendship> friendshipOptional = getFriendshipByUserIdOrderInsensitive(senderId, recipientId);
        Friendship friendship = friendshipOptional.orElseGet(Friendship::new);
        if (friendshipOptional.isPresent() && friendship.getStatus() == FriendshipStatus.REQUESTED) {
            friendship.setStatus(FriendshipStatus.CANCELLED);
            friendshipRepo.save(friendship);
            sendNotification(senderId, recipientId, "Cancel Friendship Req.");
            return true;
        }
        return false;
    }

    @Override
    public boolean acceptFriendshipRequestByUserId(Long senderId, Long recipientId) {
        Optional<Friendship> friendshipOptional = getFriendshipByUserIdOrderInsensitive(senderId, recipientId);
        Friendship friendship = friendshipOptional.orElseGet(Friendship::new);
        if (friendshipOptional.isPresent() && friendship.getStatus() == FriendshipStatus.REQUESTED) {
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepo.save(friendship);
            sendNotification(senderId, recipientId, "Accepted Friendship Req.");
            return true;
        }
        return false;
    }

    @Override
    public boolean declineFriendshipRequestByUserId(Long senderId, Long recipientId) {
        Optional<Friendship> friendshipOptional = getFriendshipByUserIdOrderInsensitive(senderId, recipientId);
        Friendship friendship = friendshipOptional.orElseGet(Friendship::new);
        if (friendshipOptional.isPresent() && friendship.getStatus() == FriendshipStatus.REQUESTED) {
            friendship.setStatus(FriendshipStatus.DECLINED);
            friendshipRepo.save(friendship);
            sendNotification(senderId, recipientId, "Declined Friendship Req.");
            return true;
        }
        return false;
    }

    @Override
    public Optional<FriendshipStatus> getFriendshipStatusByUserId(Long userId, Long targetUserId) {
        return getFriendshipByUserIdOrderInsensitive(userId, targetUserId).map(Friendship::getStatus);
    }

    @Override
    public boolean deleteFriendByUserId(Long userId, Long friendId) {
        Optional<Friendship> friendshipOptional = getFriendshipByUserIdOrderInsensitive(userId, friendId);
        if (friendshipOptional.isPresent() && friendshipOptional.get().getStatus() == FriendshipStatus.ACCEPTED) {
            friendshipRepo.delete(friendshipOptional.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean areFriends(Long userId, Long friendId) {
        Optional<Friendship> friendshipOptional = getFriendshipByUserIdOrderInsensitive(userId, friendId);
        return friendshipOptional.isPresent() && friendshipOptional.get().getStatus() == FriendshipStatus.ACCEPTED;
    }

    @Override
    public boolean blockFriendshipRequestsFromUserById(Long senderId, Long recipientId) {
        Optional<Friendship> friendshipOptional = getFriendshipByUserIdOrderInsensitive(senderId, recipientId);
        Friendship friendship = friendshipOptional.orElseGet(Friendship::new);
        if (friendshipOptional.isPresent() && friendship.getStatus() == FriendshipStatus.REQUESTED) {
            friendship.setStatus(FriendshipStatus.BLOCKED);
            friendshipRepo.save(friendship);
            sendNotification(senderId, recipientId, "Blocked Friendship Req.");
            return true;
        }
        return false;
    }

    @Override
    public List<RequestedFriendshipDto> getAllFriendshipRequestsForUserById(Long recipientId) {
        return friendshipRepo.getFriendshipRequestsByUserId(recipientId).stream()
            .map(friendship -> modelMapper.map(friendship, RequestedFriendshipDto.class))
            .toList();
    }

    protected Optional<Friendship> getFriendshipByUserIdOrderInsensitive(Long userId, Long friendId) {
        return friendshipRepo.findFriendshipByEitherUserId(userId, friendId);
    }

    protected List<FriendCardDto> getAllFriendCardsOfUserById(Long userId) {
        return friendshipRepo.getAllFriendshipsByUserId(userId).stream()
            .map(friendship -> {
                FriendCardDto friendCard = modelMapper.map(friendship.getFriend(), FriendCardDto.class);
                friendCard.setMutualFriends(getAmountOfMutualFriends(userId, getFriendId.apply(friendship)));
                return friendCard;
            })
            .toList();
    }

    protected int getAmountOfMutualFriends(Long userId, Long targetUserId) {
        List<Long> userFriends =
            new ArrayList<>(friendshipRepo.getAllFriendshipsByUserId(userId).stream().map(getFriendId).toList());
        List<Long> targetFriends =
            new ArrayList<>(friendshipRepo.getAllFriendshipsByUserId(targetUserId).stream().map(getFriendId).toList());
        userFriends.retainAll(targetFriends);
        return userFriends.size();
    }

    private void sendNotification(Long senderId, Long receiverId, String message) {
        NotificationRequestDto notification = NotificationRequestDto.builder()
            .senderId(senderId)
            .receiverId(receiverId)
            .message(message)
            .section(NotificationSection.GreenCity.name())
            .build();
        notificationService.addNotification(notification);
    }
}
