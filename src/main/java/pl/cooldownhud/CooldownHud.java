package pl.cooldownhud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import pl.cooldownhud.mixin.CooldownEntryAccessor;
import pl.cooldownhud.mixin.ItemCooldownManagerAccessor;

public final class CooldownHud {
    private static final int MARGIN = 4;
    private static final int ROW_HEIGHT = 18;

    private CooldownHud() {}

    private record Row(Identifier id, float seconds) {}

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        ItemCooldownManager manager = mc.player.getItemCooldownManager();
        ItemCooldownManagerAccessor accessor = (ItemCooldownManagerAccessor) manager;

        int now = accessor.cooldownhud$getTick();
        List<Row> rows = new ArrayList<>();

        for (Map.Entry<Identifier, ?> e : accessor.cooldownhud$getEntries().entrySet()) {
            int end = ((CooldownEntryAccessor) (Object) e.getValue()).cooldownhud$getEndTick();
            float seconds = (end - now) / 20.0f;
            if (seconds > 0) {
                rows.add(new Row(e.getKey(), seconds));
            }
        }
        if (rows.isEmpty()) return;

        rows.sort(Comparator.comparing(r -> r.id().toString()));

        int y = context.getScaledWindowHeight() - MARGIN - ROW_HEIGHT;
        for (Row row : rows) {
            int textX = MARGIN;
            if (Registries.ITEM.containsId(row.id())) {
                ItemStack stack = new ItemStack(Registries.ITEM.get(row.id()));
                context.drawItem(stack, MARGIN, y);
                textX = MARGIN + 20;
            }
            String text = (textX == MARGIN ? row.id().getPath() + " " : "")
                    + String.format(Locale.ROOT, "%.1fs", row.seconds());
            context.drawText(mc.textRenderer, text, textX, y + 5, 0xFFFFFFFF, true);
            y -= ROW_HEIGHT;
        }
    }
}
