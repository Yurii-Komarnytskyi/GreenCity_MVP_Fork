package greencity.dto.friendship;

import org.springframework.web.bind.annotation.RequestParam;

public class FriendshipsFilterRequestDto {
    private boolean filterByCity;
    private boolean filterByHighestRate;
    private boolean filterRecentlyAddedFriends;

    public FriendshipsFilterRequestDto(
            boolean filterByCity,
            boolean filterByHighestRate,
            boolean filterRecentlyAddedFriends) {
        this.filterByCity = filterByCity;
        this.filterByHighestRate = filterByHighestRate;
        this.filterRecentlyAddedFriends = filterRecentlyAddedFriends;
    }

    public boolean isFilterByCity() {
        return filterByCity;
    }

    public void setFilterByCity(boolean filterByCity) {
        this.filterByCity = filterByCity;
    }

    public boolean isFilterByHighestRate() {
        return filterByHighestRate;
    }

    public void setFilterByHighestRate(boolean filterByHighestRate) {
        this.filterByHighestRate = filterByHighestRate;
    }

    public boolean isFilterRecentlyAddedFriends() {
        return filterRecentlyAddedFriends;
    }

    public void setFilterRecentlyAddedFriends(boolean filterRecentlyAddedFriends) {
        this.filterRecentlyAddedFriends = filterRecentlyAddedFriends;
    }

    public boolean isAnyFilteringNecessary() {
        return filterByCity || filterByHighestRate || filterRecentlyAddedFriends;
    }
}
