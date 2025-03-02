package greencity.dto.friendship;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Objects;
import java.util.Optional;

@Builder
public class FriendCardDto {

    @Min(1)
    @NotNull
    private Long id;

    private Optional<Long> friendshipId;

    @NotEmpty
    private String profilePicturePath;

    @NotEmpty
    private String name;

    @NotNull
    private Double rating;

    private String city;

    @Min(0)
    private int mutualFriends = 0;

    public FriendCardDto() {
    }

    public FriendCardDto(
            Long id,
            Optional<Long> friendshipId,
            String profilePicturePath,
            String name,
            Double rating,
            String city,
            int mutualFriends) {
        this.id = id;
        this.friendshipId = friendshipId;
        this.profilePicturePath = profilePicturePath;
        this.name = name;
        this.rating = rating;
        this.city = city;
        this.mutualFriends = mutualFriends;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Optional<Long> getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(Optional<Long> friendshipId) {
        this.friendshipId = friendshipId;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getMutualFriends() {
        return mutualFriends;
    }

    public void setMutualFriends(int mutualFriends) {
        this.mutualFriends = mutualFriends;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FriendCardDto that))
            return false;
        return Objects.equals(id, that.id) && Objects.equals(profilePicturePath, that.profilePicturePath)
            && Objects.equals(name, that.name) && Objects.equals(rating, that.rating)
            && Objects.equals(city, that.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, profilePicturePath, name, rating, city);
    }

    @Override
    public String toString() {
        return "FriendCardDto{" +
            "city='" + city + '\'' +
            ", rating=" + rating +
            ", name='" + name + '\'' +
            ", profilePicturePath='" + profilePicturePath + '\'' +
            ", id=" + id +
            '}';
    }
}
