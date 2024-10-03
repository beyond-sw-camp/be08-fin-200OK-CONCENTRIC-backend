package ok.backend.storage.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.team.domain.entity.Team;
import org.hibernate.annotations.CreationTimestamp;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "storage")
public class Storage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorageType storageType;

    @Column(nullable = false)
    private Long capacity;

    @Column(name = "current_size", nullable = false)
    private Long currentSize;

    @CreationTimestamp
    @Column(name = "create_at")
    private Long createDate;

    @OneToMany(mappedBy = "storage")
    private List<StorageFile> storageFiles;
}
