package pl.cooldownhud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import pl.cooldownhud.mixin.CooldownEntryAccessor;
import pl.cooldownhud.mixin.ItemCooldownManagerAccessor;

public final class CooldownHud {
    private static final int ROW_HEIGHT = 18;
    private static final int ICON_WIDTH = 20;

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

        CooldownHudConfig cfg = CooldownHudConfig.get();
        CooldownHudConfig.Corner corner = cfg.corner;
        boolean bottom = corner.isBottom();
        boolean right = corner.isRight();

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        TextRenderer tr = mc.textRenderer;

        int y = bottom ? screenH - cfg.offsetY - ROW_HEIGHT : cfg.offsetY;
        int step = bottom ? -ROW_HEIGHT : ROW_HEIGHT;

        for (Row row : rows) {
            ItemStack stack = findStack(mc, manager, row.id());
            boolean hasIcon = !stack.isEmpty();

            String secs = String.format(Locale.ROOT, "%.1fs", row.seconds());
            String text = hasIcon ? secs : row.id().getPath() + " " + secs;

            int rowWidth = (hasIcon ? ICON_WIDTH : 0) + tr.getWidth(text);
            int x = right ? screenW - cfg.offsetX - rowWidth : cfg.offsetX;

            if (hasIcon) {
                // drawItem używa modelu przedmiotu z aktywnego resource packa
                context.drawItem(stack, x, y);
                x += ICON_WIDTH;
            }
            context.drawText(tr, text, x, y + 5, 0xFFFFFFFF, true);
            y += step;
        }
    }

    /**
     * Szuka w ekwipunku prawdziwego przedmiotu z tą grupą cooldownu, żeby ikona
     * miała jego komponenty (custom_model_data, item_model) i pasowała do texture packa.
     * Gdy przedmiotu już nie ma w ekwipunku, używa domyślnego przedmiotu o tym id.
     */
    private static ItemStack findStack(MinecraftClient mc, ItemCooldownManager manager, Identifier id) {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty() && manager.getGroup(s).equals(id)) {
                return s.copyWithCount(1);
            }
        }
        if (Registries.ITEM.containsId(id)) {
            return new ItemStack(Registries.ITEM.get(id));
        }
        return ItemStack.EMPTY;
    }
}
