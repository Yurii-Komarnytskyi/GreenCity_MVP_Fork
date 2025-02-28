package greencity.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageableAdvancedDto<T> {
    private List<T> content;
    private long totalElements;
    private int number;
    private int totalPages;
    private int size;
    private boolean hasPrevious;
    private boolean hasNext;
    private boolean first;
    private boolean last;

    /**
     * Constructor.
     */
    @JsonCreator
    public PageableAdvancedDto(@JsonProperty("content") List<T> content,
        @JsonProperty("totalElements") long totalElements,
        @JsonProperty("number") int number,
        @JsonProperty("totalPages") int totalPages,
        @JsonProperty("size") int size,
        @JsonProperty("hasPrevious") boolean hasPrevious,
        @JsonProperty("hasNext") boolean hasNext,
        @JsonProperty("first") boolean first,
        @JsonProperty("last") boolean last) {
        this.content = content;
        this.totalElements = totalElements;
        this.number = number;
        this.totalPages = totalPages;
        this.size = size;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
        this.first = first;
        this.last = last;
    }
}
