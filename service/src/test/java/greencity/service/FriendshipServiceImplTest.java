package greencity.service;

import greencity.dto.friendship.FriendCardDto;
import greencity.dto.friendship.RequestedFriendshipDto;
import greencity.dto.notification.NotificationRequestDto;
import greencity.entity.Friendship;
import greencity.entity.User;
import greencity.enums.FriendshipStatus;
import greencity.repository.FriendshipRepo;
import greencity.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendshipServiceImplTest {

    @Mock
    private FriendshipRepo friendshipRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private FriendshipServiceImpl friendshipService;

    private User user1;
    private User user2;
    private User user3;
    private Friendship friendship1;
    private Friendship friendship2;
    private Friendship friendship3;
    private FriendCardDto friendCardDto1;
    private RequestedFriendshipDto requestedFriendshipDto;

    private Long userId = 1L;
    private Long targetUserId = 2L;

    private Long senderId = 1L;
    private Long recipientId = 2L;
    private Long friendId = 2L;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("User1");

        user2 = new User();
        user2.setId(2L);
        user2.setName("User2");

        user3 = new User();
        user3.setId(3L);
        user3.setName("User3");

        friendship1 = new Friendship();
        friendship1.setUser(user1);
        friendship1.setFriend(user2);
        friendship1.setStatus(FriendshipStatus.REQUESTED);

        friendship2 = new Friendship();
        friendship2.setUser(user1);
        friendship2.setFriend(user3);
        friendship2.setStatus(FriendshipStatus.ACCEPTED);

        friendship3 = new Friendship();
        friendship3.setUser(user2);
        friendship3.setFriend(user3);
        friendship3.setStatus(FriendshipStatus.ACCEPTED);

        friendCardDto1 = new FriendCardDto();
        friendCardDto1.setId(user2.getId());
        friendCardDto1.setName(user2.getName());

        FriendCardDto friendCardDto2 = new FriendCardDto();
        friendCardDto2.setId(user1.getId());
        friendCardDto2.setName(user1.getName());

        requestedFriendshipDto = new RequestedFriendshipDto();
        requestedFriendshipDto.setSenderId(user1.getId());
        requestedFriendshipDto.setRecipientId(user2.getId());
    }

    @Test
    @DisplayName("Finds mutual friends successfully for two users with common friends")
    void testGetAllMutualFriendsByUserId_Positive() {
        FriendCardDto friendCardDto2 = new FriendCardDto();
        friendCardDto2.setId(2L);
        friendCardDto2.setName("User2");

        FriendCardDto friendCardDto3 = new FriendCardDto();
        friendCardDto3.setId(3L);
        friendCardDto3.setName("User3");

        when(friendshipRepo.getAllFriendshipsByUserId(userId)).thenReturn(Arrays.asList(friendship1, friendship2));
        when(friendshipRepo.getAllFriendshipsByUserId(targetUserId)).thenReturn(Arrays.asList(friendship3));

        when(modelMapper.map(user2, FriendCardDto.class)).thenReturn(friendCardDto2);
        when(modelMapper.map(user3, FriendCardDto.class)).thenReturn(friendCardDto3);

        List<FriendCardDto> mutualFriends = friendshipService.getAllMutualFriendsByUserId(userId, targetUserId);

        assertEquals(1, mutualFriends.size());
        assertEquals(3L, mutualFriends.getFirst().getId());
        assertEquals("User3", mutualFriends.getFirst().getName());
    }

    @Test
    @DisplayName("Returns empty list when there are no mutual friends between two users")
    void testGetAllMutualFriendsByUserId_NoMutualFriends() {
        when(friendshipRepo.getAllFriendshipsByUserId(userId)).thenReturn(Collections.emptyList());
        when(friendshipRepo.getAllFriendshipsByUserId(targetUserId)).thenReturn(Collections.emptyList());

        List<FriendCardDto> result = friendshipService.getAllMutualFriendsByUserId(userId, targetUserId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Gets all friends for a user successfully")
    void testGetAllFriendsByUserId_Positive() {
        when(friendshipRepo.getAllFriendshipsByUserId(userId)).thenReturn(Collections.singletonList(friendship1));
        when(modelMapper.map(friendship1.getFriend(), FriendCardDto.class)).thenReturn(friendCardDto1);

        List<FriendCardDto> result = friendshipService.getAllFriendsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(friendCardDto1, result.getFirst());
    }

    @Test
    @DisplayName("Returns empty list when a user has no friends")
    void testGetAllFriendsByUserId_NoFriends() {
        when(friendshipRepo.getAllFriendshipsByUserId(userId)).thenReturn(Collections.emptyList());

        List<FriendCardDto> result = friendshipService.getAllFriendsByUserId(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Requests friendship successfully and sends notification")
    void testRequestFriendshipByUserId_Positive() {
        when(friendshipRepo.findFriendshipByEitherUserId(senderId, recipientId)).thenReturn(Optional.empty());
        when(userRepo.getReferenceById(senderId)).thenReturn(user1);
        when(userRepo.getReferenceById(recipientId)).thenReturn(user2);

        boolean result = friendshipService.requestFriendshipByUserId(senderId, recipientId);

        assertTrue(result);
        verify(friendshipRepo, times(1)).save(any(Friendship.class));
        verify(notificationService, times(1)).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("Fails to request friendship when sender and recipient are the same user")
    void testRequestFriendshipByUserId_Negative_SameUser() {
        boolean result = friendshipService.requestFriendshipByUserId(senderId, senderId);

        assertFalse(result);
        verify(friendshipRepo, never()).save(any(Friendship.class));
        verify(notificationService, never()).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("Cancels friendship request and sends notification")
    void testCancelFriendshipRequestByUserId_Positive() {
        when(friendshipRepo.findFriendshipByEitherUserId(senderId, recipientId)).thenReturn(Optional.of(friendship1));

        boolean result = friendshipService.cancelFriendshipRequestByUserId(senderId, recipientId);

        assertTrue(result);
        verify(friendshipRepo, times(1)).save(friendship1);
        verify(notificationService, times(1)).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("Fails to cancel friendship request if none exists")
    void testCancelFriendshipRequestByUserId_Negative_NoRequest() {
        when(friendshipRepo.findFriendshipByEitherUserId(senderId, recipientId)).thenReturn(Optional.empty());

        boolean result = friendshipService.cancelFriendshipRequestByUserId(senderId, recipientId);

        assertFalse(result);
        verify(friendshipRepo, never()).save(any(Friendship.class));
        verify(notificationService, never()).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("Accepts friendship request and sends notification")
    void testAcceptFriendshipRequestByUserId_Positive() {
        when(friendshipRepo.findFriendshipByEitherUserId(senderId, recipientId)).thenReturn(Optional.of(friendship1));

        boolean result = friendshipService.acceptFriendshipRequestByUserId(senderId, recipientId);

        assertTrue(result);
        verify(friendshipRepo, times(1)).save(friendship1);
        verify(notificationService, times(1)).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("Fails to accept friendship request if none exists")
    void testAcceptFriendshipRequestByUserId_Negative_NoRequest() {
        when(friendshipRepo.findFriendshipByEitherUserId(senderId, recipientId)).thenReturn(Optional.empty());

        boolean result = friendshipService.acceptFriendshipRequestByUserId(senderId, recipientId);

        assertFalse(result);
        verify(friendshipRepo, never()).save(any(Friendship.class));
        verify(notificationService, never()).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("Declines friendship request and sends notification")
    void testDeclineFriendshipRequestByUserId_Positive() {
        when(friendshipRepo.findFriendshipByEitherUserId(senderId, recipientId)).thenReturn(Optional.of(friendship1));

        boolean result = friendshipService.declineFriendshipRequestByUserId(senderId, recipientId);

        assertTrue(result);
        verify(friendshipRepo, times(1)).save(friendship1);
        verify(notificationService, times(1)).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("Fails to decline friendship request if none exists")
    void testDeclineFriendshipRequestByUserId_Negative_NoRequest() {
        when(friendshipRepo.findFriendshipByEitherUserId(senderId, recipientId)).thenReturn(Optional.empty());

        boolean result = friendshipService.declineFriendshipRequestByUserId(senderId, recipientId);

        assertFalse(result);
        verify(friendshipRepo, never()).save(any(Friendship.class));
        verify(notificationService, never()).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    void testGetFriendshipStatusByUserId_Positive() {
        when(friendshipRepo.findFriendshipByEitherUserId(userId, targetUserId)).thenReturn(Optional.of(friendship1));

        Optional<FriendshipStatus> result = friendshipService.getFriendshipStatusByUserId(userId, targetUserId);

        assertTrue(result.isPresent());
        assertEquals(FriendshipStatus.REQUESTED, result.get());
    }

    @Test
    @DisplayName("Fails to get friendship status if no friendship exists")
    void testGetFriendshipStatusByUserId_Negative_NoFriendship() {
        when(friendshipRepo.findFriendshipByEitherUserId(userId, targetUserId)).thenReturn(Optional.empty());

        Optional<FriendshipStatus> result = friendshipService.getFriendshipStatusByUserId(userId, targetUserId);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Successfully deletes a friendship")
    void testDeleteFriendByUserId_Positive() {
        when(friendshipRepo.findFriendshipByEitherUserId(userId, friendId)).thenReturn(Optional.of(friendship2));

        boolean result = friendshipService.deleteFriendByUserId(userId, friendId);

        assertTrue(result);
        verify(friendshipRepo, times(1)).delete(friendship2);
    }

    @Test
    @DisplayName("Fails to delete a friendship if the users are not friends")
    void testDeleteFriendByUserId_Negative_NotFriends() {
        when(friendshipRepo.findFriendshipByEitherUserId(userId, friendId)).thenReturn(Optional.of(friendship1));

        boolean result = friendshipService.deleteFriendByUserId(userId, friendId);

        assertFalse(result);
        verify(friendshipRepo, never()).delete(any(Friendship.class));
    }

    @Test
    @DisplayName("Identifies users as friends successfully")
    void testAreFriends_Positive() {
        when(friendshipRepo.findFriendshipByEitherUserId(userId, friendId)).thenReturn(Optional.of(friendship2));

        boolean result = friendshipService.areFriends(userId, friendId);

        assertTrue(result);
    }

    @Test
    @DisplayName("Identifies users are not friends when no friendship exists")
    void testAreFriends_Negative_NotFriends() {
        when(friendshipRepo.findFriendshipByEitherUserId(userId, friendId)).thenReturn(Optional.of(friendship1));

        boolean result = friendshipService.areFriends(userId, friendId);

        assertFalse(result);
    }

    @Test
    @DisplayName("Blocks friendship request and sends notification")
    void testBlockFriendshipRequestsFromUserById_Positive() {
        when(friendshipRepo.findFriendshipByEitherUserId(senderId, recipientId)).thenReturn(Optional.of(friendship1));

        boolean result = friendshipService.blockFriendshipRequestsFromUserById(senderId, recipientId);

        assertTrue(result);
        verify(friendshipRepo, times(1)).save(friendship1);
        verify(notificationService, times(1)).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("Fails to block friendship requests if no request exists")
    void testBlockFriendshipRequestsFromUserById_Negative_NoRequest() {
        when(friendshipRepo.findFriendshipByEitherUserId(senderId, recipientId)).thenReturn(Optional.empty());

        boolean result = friendshipService.blockFriendshipRequestsFromUserById(senderId, recipientId);

        assertFalse(result);
        verify(friendshipRepo, never()).save(any(Friendship.class));
        verify(notificationService, never()).addNotification(any(NotificationRequestDto.class));
    }

    @Test
    @DisplayName("Gets all friendship requests for a user successfully")
    void testGetAllFriendshipRequestsForUserById_Positive() {
        when(friendshipRepo.getFriendshipRequestsByUserId(recipientId)).thenReturn(Collections.singletonList(friendship1));
        when(modelMapper.map(friendship1, RequestedFriendshipDto.class)).thenReturn(requestedFriendshipDto);

        List<RequestedFriendshipDto> result = friendshipService.getAllFriendshipRequestsForUserById(recipientId);

        assertEquals(1, result.size());
        assertEquals(requestedFriendshipDto, result.getFirst());
    }

    @Test
    @DisplayName("Returns empty list when there are no friendship requests for a user")
    void testGetAllFriendshipRequestsForUserById_NoRequests() {
        when(friendshipRepo.getFriendshipRequestsByUserId(recipientId)).thenReturn(Collections.emptyList());

        List<RequestedFriendshipDto> result = friendshipService.getAllFriendshipRequestsForUserById(recipientId);

        assertTrue(result.isEmpty());
    }
}