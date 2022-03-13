package com.paragon.client.managers;

import com.paragon.Paragon;
import com.paragon.client.systems.module.Module;
import com.paragon.client.systems.module.ModuleCategory;
import com.paragon.client.systems.module.impl.client.*;
import com.paragon.client.systems.module.impl.combat.*;
import com.paragon.client.systems.module.impl.misc.*;
import com.paragon.client.systems.module.impl.movement.*;
import com.paragon.client.systems.module.impl.render.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Wolfsurge
 */
public class ModuleManager {

    private final List<Module> modules;

    public ModuleManager() {
        MinecraftForge.EVENT_BUS.register(this);

        Paragon.INSTANCE.getLogger().info("Initialising Module Manager");

        modules = Arrays.asList(
                new AutoCrystal(),
                new Offhand(),

                new ElytraFlight(),
                new Speed(),
                new Step(),
                new Velocity(),

                new BlockHighlight(),
                new ESP(),
                new Fullbright(),
                new HoleESP(),
                new NoRender(),
                new Tracers(),
                new ViewModel(),

                new ChatModifications(),
                new FakePlayer(),
                new FastUse(),
                new MiddleClick(),
                new OnDeath(),

                new ClientFont(),
                new Colours(),
                new GUI(),
                new HUD()
        );

        modules.forEach(module -> {
            // Load config
            Paragon.INSTANCE.getStorageManager().loadModuleConfiguration(module);
        });
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            if (!(Keyboard.getEventKey() > 1)) {
                return;
            }

            getModules().forEach(module -> {
                if (module.getKeyCode().getKeyCode() == Keyboard.getEventKey()) {
                    module.toggle();
                }
            });
        }
    }

    /**
     * Gets a list of modules
     * @return The modules
     */
    public List<Module> getModules() {
        return modules;
    }

    /**
     * Gets the modules in a category
     * @param moduleCategory The module category to get modules in
     * @return The modules in the given category
     */
    public List<Module> getModulesInCategory(ModuleCategory moduleCategory) {
        List<Module> modulesInCategory = new ArrayList<>();

        getModules().forEach(module -> {
            if (module.getCategory() == moduleCategory) {
                modulesInCategory.add(module);
            }
        });

        return modulesInCategory;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        modules.forEach(module -> {
            if (module.isEnabled()) {
                module.onTick();
            }
        });
    }

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            modules.forEach(module -> {
                if (module.isEnabled()) {
                    module.onRender2D();
                }
            });
        }
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        modules.forEach(module -> {
            if (module.isEnabled()) {
                module.onRender3D();
            }
        });
    }

}
