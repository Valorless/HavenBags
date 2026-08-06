package valorless.havenbags.datamodels;

import org.jetbrains.annotations.NotNull;
import valorless.havenbags.annotations.MarkedForRemoval;

/**
 * @deprecated This class is a legacy wrapper and will be removed in a future update.
 *             Use {@link Bag} directly instead.
 * @since 1.43.0
 *
 * <p>{@link Bag} is the full replacement for {@code Data}.
 */
@Deprecated
@MarkedForRemoval("The Data class is no longer used and will be removed in a future update. Please use the Bag class directly instead.")
public class Data extends Bag{
    public Data(@NotNull String uuid, @NotNull String owner) {
        super(uuid, owner);
    }
}
