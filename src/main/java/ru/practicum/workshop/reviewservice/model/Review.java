package ru.practicum.workshop.reviewservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    private String title;
    private String content;
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdOn;
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedOn;
    private Long mark; // TODO: возможно, переделать.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id) && Objects.equals(eventId, review.eventId) && Objects.equals(title, review.title) && Objects.equals(content, review.content) && Objects.equals(createdOn, review.createdOn) && Objects.equals(updatedOn, review.updatedOn) && Objects.equals(mark, review.mark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventId, title, content, createdOn, updatedOn, mark);
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                ", mark=" + mark +
                '}';
    }
}
