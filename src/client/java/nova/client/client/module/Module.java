
package nova.client.module;

// @name: Module
// @type: Java Class
// @location: src/client/java/nova/client/module/

public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;

    private boolean enabled = false;
    private int keybind = -1;

    public Module(
            String name,
            String description,
            Category category) {

        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;

        this.enabled = enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void onEnable() {}

    public void onDisable() {}

    public void onTick() {}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }
}
