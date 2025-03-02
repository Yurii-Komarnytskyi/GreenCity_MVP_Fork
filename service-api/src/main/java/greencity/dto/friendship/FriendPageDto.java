package greencity.dto.friendship;

import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public class FriendPageDto {

    private Optional<Long> friendshipId;

    @NotNull
    private FriendCardDto friendCardDto;

    private boolean isOnline;

    @NotNull
    private String credo;

    @NotNull
    private int habitsInProgressCounter;

    @NotNull
    private int habitsAcquiredCounter;

    @NotNull
    private int tipsAndTricksCounter;

    @NotNull
    private int publishedNewsCounter;

    public FriendPageDto() {}

    public Optional<Long> getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(Optional<Long> friendshipId) {
        this.friendshipId = friendshipId;
    }

    public FriendCardDto getFriendCardDto() {
        return friendCardDto;
    }

    public void setFriendCardDto(FriendCardDto friendCardDto) {
        this.friendCardDto = friendCardDto;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean online) {
        isOnline = online;
    }

    public String getCredo() {
        return credo;
    }

    public void setCredo(String credo) {
        this.credo = credo;
    }

    public int getHabitsInProgressCounter() {
        return habitsInProgressCounter;
    }

    public void setHabitsInProgressCounter(int habitsInProgressCounter) {
        this.habitsInProgressCounter = habitsInProgressCounter;
    }

    public int getHabitsAcquiredCounter() {
        return habitsAcquiredCounter;
    }

    public void setHabitsAcquiredCounter(int habitsAcquiredCounter) {
        this.habitsAcquiredCounter = habitsAcquiredCounter;
    }

    public int getTipsAndTricksCounter() {
        return tipsAndTricksCounter;
    }

    public void setTipsAndTricksCounter(int tipsAndTricksCounter) {
        this.tipsAndTricksCounter = tipsAndTricksCounter;
    }

    public int getPublishedNewsCounter() {
        return publishedNewsCounter;
    }

    public void setPublishedNewsCounter(int publishedNewsCounter) {
        this.publishedNewsCounter = publishedNewsCounter;
    }
}
