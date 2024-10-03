package ok.backend.storage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import ok.backend.storage.domain.entity.Storage;

@Getter
public class StorageStatusResponseDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private Long currentSize;

    @JsonProperty
    private Long capacity;

    public StorageStatusResponseDto(Storage storage) {
        this.id = storage.getId();
        this.currentSize = storage.getCurrentSize();
        this.capacity = storage.getCapacity();
    }
}
