package greencity.service;

import greencity.dto.friendship.FriendCardDto;
import greencity.dto.friendship.FriendPageDto;
import greencity.dto.friendship.FriendshipVO;
import greencity.dto.user.UserVO;
import greencity.enums.HabitAssignStatus;
import greencity.exception.exceptions.WrongIdException;
import greencity.repository.HabitAssignRepo;
import greencity.repository.UserRepo;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FriendPageServiceImpl implements FriendPageService {

    private final EcoNewsService ecoNewsService;
    private final UserService userService;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;
    private final HabitAssignRepo habitAssignRepo;
    private final FriendshipService friendshipService;
    private static final Logger logger = LoggerFactory.getLogger(FriendPageServiceImpl.class);

    @Autowired
    public FriendPageServiceImpl(
            EcoNewsService ecoNewsService,
            UserService userService,
            UserRepo userRepo,
            ModelMapper modelMapper,
            HabitAssignRepo habitAssignRepo,
            FriendshipService friendshipService) {
        this.ecoNewsService = ecoNewsService;
        this.userService = userService;
        this.userRepo = userRepo;
        this.modelMapper = modelMapper;
        this.habitAssignRepo = habitAssignRepo;
        this.friendshipService = friendshipService;
    }

    @Override
    public FriendPageDto assembleFriendPage(@NotNull Long userId, Optional<Long> friendshipIdOptional) {
        final int TIPS_AND_TRICKS_ARE_NOT_YET_IMPLEMENTED = 0;
        int habitsInProgressCounter = (int) habitAssignRepo.findAllByUserId(userId).stream()
                .filter(habit -> habit.getStatus().equals(HabitAssignStatus.INPROGRESS))
                .count();
        int habitsAcquiredCounter = (int) habitAssignRepo.findAllByUserId(userId).stream()
                .filter(habit -> habit.getStatus().equals(HabitAssignStatus.ACQUIRED))
                .count();
        try {
            UserVO userVO = userService.findById(userId);
            FriendCardDto friendCardDto = modelMapper.map(userRepo.findById(userId), FriendCardDto.class);
            friendCardDto.setFriendshipId(friendshipIdOptional);
            int publishedNews = Optional.of(ecoNewsService.getAmountOfPublishedNewsByUserId(userId).intValue()).orElse(0);
            FriendPageDto friendPageDto = new FriendPageDto();

            friendPageDto.setFriendshipId(friendshipIdOptional);
            friendPageDto.setFriendCardDto(friendCardDto);
            friendPageDto.setIsOnline(userService.checkIfTheUserIsOnline(userId));
            friendPageDto.setCredo(userVO.getUserCredo());
            friendPageDto.setTipsAndTricksCounter(TIPS_AND_TRICKS_ARE_NOT_YET_IMPLEMENTED);
            friendPageDto.setHabitsAcquiredCounter(habitsAcquiredCounter);
            friendPageDto.setHabitsInProgressCounter(habitsInProgressCounter);
            friendPageDto.setPublishedNewsCounter(publishedNews);

            return friendPageDto;
        } catch (WrongIdException | NullPointerException exception) {
            logger.error("Failed to assemble friend page for user ID: {}", userId, exception);
            throw new WrongIdException("Failed to assemble friend page for user ID: " + userId);
        }
    }
}
