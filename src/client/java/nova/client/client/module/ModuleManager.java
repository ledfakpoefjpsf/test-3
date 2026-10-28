
package nova.client.client.module;

// @name: ModuleManager
// @type: Java Class
// @location: src/client/java/nova/client/client/module/ModuleManager.java

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void initialize() {

        register(new Module(
            "Auto Sprint",
            "Automatically sprint while moving forward",
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
            "Highlight loaded players",
            Category.RENDER
        ) {
            @Override
            public void onTick() {
                updateGlow(true, false, true);
            }

            @Override
            public void onDisable() {
                updateGlow(true, false, false);
            }
        });

        register(new Module(
            "Mob ESP",
            "Highlight loaded mobs",
            Category.RENDER
        ) {
            @Override
            public void onTick() {
                updateGlow(false, true, true);
            }

            @Override
            public void onDisable() {
                updateGlow(false, true, false);
            }
        });

        register(new Module(
            "Macro Recorder",
            "F6 record, F7 playback, F8 stop",
            Category.MISC
        ) {});
    }

    private void updateGlow(
        boolean players,
        boolean mobs,
        boolean enabled
    ) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        for (Entity entity : mc.level.entitiesForRendering()) {

            if (entity == mc.player) {
                continue;
            }

            boolean isPlayer = entity instanceof Player;
            boolean isMob =
                entity instanceof LivingEntity && !isPlayer;

            if ((players && isPlayer) || (mobs && isMob)) {
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
            .filter(module -> module.getCategory() == category)
            .toList();
    }

    public Module getByName(String name) {
        return modules.stream()
            .filter(module ->
                module.getName().equalsIgnoreCase(name))
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
