package org.betterx.worlds.together.entrypoints;

import net.fabricmc.loader.api.FabricLoader;

import java.util.List;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EntrypointUtil {
    private static <T extends WorldsTogetherEntrypoint> List<T> getEntryPoints(boolean client, Class<T> select) {
        return FabricLoader.getInstance()
                           .getEntrypoints(
                                   client ? "worlds_together_client" : "worlds_together",
                                   WorldsTogetherEntrypoint.class
                           )
                           .stream()
                           .filter(o -> select.isAssignableFrom(o.getClass()))
                           .map(e -> (T) e)
                           .toList();
    }

    public static <T extends WorldsTogetherEntrypoint> List<T> getCommon(Class<T> select) {
        return getEntryPoints(false, select);
    }

    public static <T extends WorldsTogetherEntrypoint> List<T> getClient(Class<T> select) {
        return getEntryPoints(true, select);
    }
}
