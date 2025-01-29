package ddd.application.event;

import lombok.Builder;
import ddd.infrastructure.Dto;
import ddd.application.event.type.AppMailType;

/**
 * Represents a mail delivery event.
 */
@Builder
public record AppMailEvent<T>(
        AppMailType mailType,
        T value) implements Dto {

    public static <T> AppMailEvent<T> of(AppMailType mailType, T value) {
        return AppMailEvent.<T>builder()
                .mailType(mailType)
                .value(value)
                .build();
    }

}
