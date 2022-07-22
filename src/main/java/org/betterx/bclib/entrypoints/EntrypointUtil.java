package org.betterx.bclib.entrypoints;

import net.fabricmc.loader.api.FabricLoader;

import java.util.List;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EntrypointUtil {
    private static <T extends BCLibEntryPoint> List<T> getEntryPoints(boolean client, Class<T> select) {
        return FabricLoader.getInstance()
                           .getEntrypoints(
                                   client ? "bclib_client" : "bclib",
                                   BCLibEntryPoint.class
                           )
                           .stream()
                           .filter(o -> select.isAssignableFrom(o.getClass()))
                           .map(e -> (T) e)
                           .toList();
    }

    @ApiStatus.Internal
    public static <T extends BCLibEntryPoint> List<T> getCommon(Class<T> select) {
        return getEntryPoints(false, select);
    }

    @ApiStatus.Internal
    public static <T extends BCLibEntryPoint> List<T> getClient(Class<T> select) {
        return getEntryPoints(true, select);
    }
}
