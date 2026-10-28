
package nova.client.client.module;

// @name: ModuleManager
// @type: Java Class
// @location: src/client/java/nova/client/client/module/ModuleManager.java

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void initialize() {

        register(new Module(
            "Auto Sprint",
            "Sprint automatically",
            Category.MOVEMENT
        ) {
            @Override
            public void onTick() {
                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null || mc.screen != null) {
                    return;
                }

                if (mc.options.keyUp.isDown()
                    && !mc.player.isCrouching()
                    && !mc.player.isUsingItem()) {
                    mc.player.setSprinting(true);
                }
            }
        });

        register(new Module(
            "Player ESP",
            "Highlight other players",
            Category.RENDER
        ) {
            @Override
            public void onTick() {
                setPlayerGlow(true);
            }

            @Override
            public void onDisable() {
                setPlayerGlow(false);
            }
        });

        register(new Module(
            "Mob ESP",
            "Highlight selected mob types",
            Category.RENDER
        ) {
            @Override
            public void onTick() {
                MobEspSettings.tick();
            }

            @Override
            public void onDisable() {
                MobEspSettings.clear();
            }
        });

        register(new Module(
            "Macro Recorder",
            "F6 record, F7 play, F8 stop",
            Category.MISC
        ) {});
    }

    private void setPlayerGlow(boolean enabled) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player
                && entity != mc.player) {
                entity.setGlowingTag(enabled);
            }
        }
    }

    public void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getByCategory(Category category) {
        return modules.stream()
            .filter(m -> m.getCategory() == category)
            .toList();
    }

    public Module getByName(String name) {
        return modules.stream()
            .filter(m -> m.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public void tick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }
}
