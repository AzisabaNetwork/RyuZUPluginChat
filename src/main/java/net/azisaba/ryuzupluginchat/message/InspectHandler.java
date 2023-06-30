package net.azisaba.ryuzupluginchat.message;

import java.util.UUID;

public interface InspectHandler {
    void setDisable(UUID uuid, boolean disable);

    boolean isDisabled(UUID uuid);

    default boolean isVisible(UUID uuid) {
        return !isDisabled(uuid);
    }
}
