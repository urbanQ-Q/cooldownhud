package pl.cooldownhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CooldownHudMod implements ClientModInitializer {
    private static KeyBinding cycleKey;

    @Override
    public void onInitializeClient() {
        CooldownHudConfig.load();

        cycleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cooldownhud.cycle_position",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.cooldownhud"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (cycleKey.wasPressed()) {
                CooldownHudConfig cfg = CooldownHudConfig.get();
                cfg.corner = cfg.corner.next();
                CooldownHudConfig.save();
                if (client.player != null) {
                    client.player.sendMessage(Text.translatable(
                            "message.cooldownhud.position",
                            Text.translatable("cooldownhud.corner." + cfg.corner.name().toLowerCase())), true);
                }
            }
        });

        HudRenderCallback.EVENT.register(CooldownHud::render);
    }
}
