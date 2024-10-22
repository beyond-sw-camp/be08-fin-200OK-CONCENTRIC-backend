package ok.backend.storage.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "storage_file")
public class StorageFile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Storage storage;

    @Column(nullable = false)
    private String name;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private Long size;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDateTime createDate;

    public void updateStatus(Boolean isActive){
        this.isActive = isActive;
    }
}
