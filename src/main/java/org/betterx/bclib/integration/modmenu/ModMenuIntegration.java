package org.betterx.bclib.integration.modmenu;

import org.betterx.bclib.integration.modmenu.ModMenuIntegration.ModMenuScreenFactory;

import net.minecraft.client.gui.screens.Screen;

import com.google.common.collect.ImmutableMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@Deprecated
class ModMenuScreenFactoryImpl {
    record ScreenFactoryInvocationHandler(ModMenuScreenFactory act) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            return switch (method.getName()) {
//                case "toString" -> "";
//                case "equals" -> false;
//                case "hashCode" -> 0;
//                default -> act.create((Screen) args[0]);
//            };
            return null;
        }
    }

    public static ModMenuScreenFactory create(ModMenuScreenFactory act) {
        Class<?> iConfigScreenFactory = null;
        try {
            iConfigScreenFactory = Class.forName("com.terraformersmc.modmenu.api.ConfigScreenFactory");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Object o = Proxy.newProxyInstance(
                ModMenuIntegration.class.getClassLoader(),
                new Class[]{iConfigScreenFactory, ModMenuScreenFactory.class},
                new ScreenFactoryInvocationHandler(act)
        );

        return (ModMenuScreenFactory) o;
    }
}

@Deprecated
/**
 * Integration, to provide a custom Screen for ModMenu.
 * <p>
 * This integration allows you to use ModMenu without adding a dependency to your project. If the
 * Mod is installed on the Client, and the correct ModMenu-EntryPoint is registered in your <i>fabric.mod.json</i>
 * the screen will show up.
 * <p>
 * You only need to subclass this class, and initialize a static Field of Type {@link ModMenuApi} using
 * the {@link #createEntrypoint(ModMenuIntegration)}-Method.
 * <p>
 * Example:
 * <pre>{@code public class ModMenu extends ModMenuIntegration {
 *	 public static final ModMenuApiMarker entrypointObject = createEntrypoint(new EntryPoint());
 *
 * 		public EntryPoint() {
 * 			super(GridScreen::new);
 *      }
 * }}</pre>
 * You'd also need to add the ModMenu-Entrypoint to your <i>fabric.mod.json</i>:
 * <pre>"entrypoints": {
 * 		...
 *	 "modmenu": [ "your.mod.ModMenu::entrypointObject" ]
 * }</pre>
 */
public abstract class ModMenuIntegration {
    /**
     * Creates a new EntryPoint Object that is accepted by ModMenu
     *
     * @param target The delegate Object that will receive calls from ModMenu
     * @return A Proxy that conforms to the ModMenu spec
     */
//    public static ModMenuApi createEntrypoint(ModMenuIntegration target) {
//        Class<?> iModMenuAPI;
//        //Class<?> iModMenuAPIMarker = null;
//        try {
//            iModMenuAPI = Class.forName("com.terraformersmc.modmenu.api.ModMenuApi");
//            //iModMenuAPIMarker = Class.forName("com.terraformersmc.modmenu.util.ModMenuApiMarker");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        Object o = Proxy.newProxyInstance(
//                ModMenuIntegration.class.getClassLoader(),
//                new Class[]{iModMenuAPI},
//                new BCLibModMenuInvocationHandler(target)
//        );
//
//        return (ModMenuApi) o;
//    }

    /**
     * The factory passed to {@link #ModMenuIntegration(ModMenuScreenFactory)}
     */
    protected final ModMenuScreenFactory screenFactory;

    /**
     * Create a new ModMenu delegate
     *
     * @param screenFactory A Factory. The Factory receives the currently visible {@code parent}-Screen
     *                      and must return a new Screen Object.
     */
    public ModMenuIntegration(ModMenuScreenFactory screenFactory) {
        this.screenFactory = screenFactory;
    }

    /**
     * A Helper class to make a BCLib-Factory conform to the ModMenu-Factory Interface.
     *
     * @param factory A BCLib-Type Factory, ie. {@code GridScreen::new }
     * @return A ModMenu Factory for a Screen
     */
    final protected ModMenuScreenFactory createFactory(ModMenuScreenFactory factory) {
        return ModMenuScreenFactoryImpl.create(factory);
    }

    /**
     * Used to construct a new config screen instance when your mod's
     * configuration button is selected on the mod menu screen. The
     * screen instance parameter is the active mod menu screen.
     * (Text copied from ModMenu)
     *
     * @return A factory for constructing config screen instances.
     */
    public ModMenuScreenFactory getModConfigScreenFactory() {
        return createFactory(screenFactory);
    }

    /**
     * Used to provide config screen factories for other mods. This takes second
     * priority to a mod's own config screen factory provider. For example, if
     * mod `xyz` supplies a config screen factory, mod `abc` providing a config
     * screen to `xyz` will be pointless, as the one provided by `xyz` will be
     * used.
     * <p>
     * This method is NOT meant to be used to add a config screen factory to
     * your own mod.
     * (Text copied from ModMenu)
     *
     * @return a map of mod ids to screen factories.
     */
    public Map<String, ModMenuScreenFactory> getProvidedConfigScreenFactories() {
        return ImmutableMap.of();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * A Factory Interface for ModMenu-Screens
     * <p>
     * The Interface matches {@code com.terraformersmc.modmenu.api.ConfigScreenFactory}
     */
    @FunctionalInterface
    public interface ModMenuScreenFactory<S extends Screen>/*extends ConfigScreenFactory*/ {
        S create(Screen parent);
    }

    record BCLibModMenuInvocationHandler(ModMenuIntegration target) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            return switch (method.getName()) {
//                case "getModConfigScreenFactory" -> target.getModConfigScreenFactory();
//                case "getProvidedConfigScreenFactories" -> target.getProvidedConfigScreenFactories();
//                case "toString" -> target.toString();
//                case "equals" -> target.equals(args[0]);
//                case "hashCode" -> target.hashCode();
//                default -> null;
//            };
            return null;
        }
    }

}
