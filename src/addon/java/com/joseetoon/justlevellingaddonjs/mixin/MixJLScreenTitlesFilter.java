package com.joseetoon.justlevellingaddonjs.mixin;

import com.joseetoon.justlevellingaddonjs.compat.base121.BackportRegistryState;
import com.joseetoon.justlevellingaddonjs.compat.base121.Base121Bridge;
import com.joseetoon.justlevellingaddonjs.config.AddonClientRuntimeSettings;
import com.seniors.justlevelingfork.client.screen.JustLevelingScreen;
import com.seniors.justlevelingfork.registry.title.Title;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = JustLevelingScreen.class, remap = false)
public abstract class MixJLScreenTitlesFilter {
    @Unique
    private static final String JLFORKADDON_NONE_LABEL = "(none)";
    @Unique
    private static final int JLFORKADDON_NONE_COLOR = 0x9AA0A6;
    @Unique
    private static final ThreadLocal<Boolean> JLFORKADDON_DRAWING_NONE_TITLE = ThreadLocal.withInitial(() -> false);

    @Inject(method = "drawTitles", at = @At("HEAD"), require = 1)
    private void jlforkaddon$resetNoneRenderState(
            GuiGraphics graphics,
            int x,
            int y,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        JLFORKADDON_DRAWING_NONE_TITLE.set(false);
    }

    @Inject(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;sort(Ljava/util/Comparator;)V",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            require = 1
    )
    private void jlforkaddon$applyHideLockedTitlesFilter(
            GuiGraphics graphics,
            int x,
            int y,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci,
            List<Title> titleList,
            List<Title> unlockTitles,
            List<Title> lockTitles
    ) {
        List<Title> filteredUnlocked = jlforkaddon$filterDeletedTitles(unlockTitles);
        jlforkaddon$replaceIfMutable(unlockTitles, filteredUnlocked);

        if (AddonClientRuntimeSettings.current().hideLockedTitles()) {
            jlforkaddon$replaceIfMutable(lockTitles, List.of());
            return;
        }

        List<Title> filteredLocked = new ArrayList<>();
        for (Title title : titleList) {
            if (!jlforkaddon$isHiddenTitle(title)
                    && !title.getRequirement()
                    && !BackportRegistryState.getEffectiveHideRequirements(title)) {
                filteredLocked.add(title);
            }
        }
        jlforkaddon$replaceIfMutable(lockTitles, filteredLocked);
    }

    @Redirect(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/seniors/justlevelingfork/registry/title/Title;getDisplayNameOrFallback()Ljava/lang/String;",
                    ordinal = 0
            ),
            require = 0
    )
    private String jlforkaddon$showNoneInsteadOfTitlelessInSearch(Title title) {
        if (title != null && "titleless".equalsIgnoreCase(title.getName())) {
            return JLFORKADDON_NONE_LABEL;
        }
        return title != null ? Base121Bridge.titleDisplayNameOrFallback(title) : "";
    }

    @Redirect(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/seniors/justlevelingfork/registry/title/Title;getDisplayNameOrFallback()Ljava/lang/String;",
                    ordinal = 1
            ),
            require = 0
    )
    private String jlforkaddon$showNoneInsteadOfTitlelessInListRow(Title title) {
        if (title != null && "titleless".equalsIgnoreCase(title.getName())) {
            JLFORKADDON_DRAWING_NONE_TITLE.set(true);
            return JLFORKADDON_NONE_LABEL;
        }
        JLFORKADDON_DRAWING_NONE_TITLE.set(false);
        return title != null ? Base121Bridge.titleDisplayNameOrFallback(title) : "";
    }

    @Redirect(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/seniors/justlevelingfork/registry/title/Title;getKey()Ljava/lang/String;",
                    ordinal = 0
            ),
            require = 0
    )
    private String jlforkaddon$showNoneInsteadOfTitlelessInSearchByKey(Title title) {
        if (title != null && "titleless".equalsIgnoreCase(title.getName())) {
            return JLFORKADDON_NONE_LABEL;
        }
        return title != null ? title.getKey() : "";
    }

    @Redirect(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/seniors/justlevelingfork/registry/title/Title;getKey()Ljava/lang/String;",
                    ordinal = 1
            ),
            require = 0
    )
    private String jlforkaddon$showNoneInsteadOfTitlelessInListRowByKey(Title title) {
        if (title != null && "titleless".equalsIgnoreCase(title.getName())) {
            JLFORKADDON_DRAWING_NONE_TITLE.set(true);
            return JLFORKADDON_NONE_LABEL;
        }
        JLFORKADDON_DRAWING_NONE_TITLE.set(false);
        return title != null ? title.getKey() : "";
    }

    @ModifyArg(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
                    remap = true
            ),
            index = 1,
            require = 0
    )
    private String jlforkaddon$forceNoneLabelInTitleList(String original) {
        boolean none = original != null && (
                "titleless".equalsIgnoreCase(original)
                        || "Titleless".equalsIgnoreCase(original)
                        || JLFORKADDON_NONE_LABEL.equalsIgnoreCase(original));
        JLFORKADDON_DRAWING_NONE_TITLE.set(none);
        return none ? JLFORKADDON_NONE_LABEL : original;
    }

    @ModifyArg(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
                    remap = true
            ),
            index = 4,
            require = 0
    )
    private int jlforkaddon$noneColorInTitleList(int color) {
        boolean none = JLFORKADDON_DRAWING_NONE_TITLE.get();
        JLFORKADDON_DRAWING_NONE_TITLE.set(false);
        return none ? JLFORKADDON_NONE_COLOR : color;
    }

    @ModifyArg(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
                    remap = true
            ),
            index = 1,
            require = 0
    )
    private Component jlforkaddon$forceNoneComponentInTitleList(Component original) {
        if (original == null) {
            return null;
        }
        String text = original.getString();
        boolean none = JLFORKADDON_DRAWING_NONE_TITLE.get()
                || (text != null && (
                "titleless".equalsIgnoreCase(text)
                        || "Titleless".equalsIgnoreCase(text)
                        || JLFORKADDON_NONE_LABEL.equalsIgnoreCase(text)));
        JLFORKADDON_DRAWING_NONE_TITLE.set(none);
        return none ? Component.literal(JLFORKADDON_NONE_LABEL) : original;
    }

    @ModifyArg(
            method = "drawTitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
                    remap = true
            ),
            index = 4,
            require = 0
    )
    private int jlforkaddon$noneColorInTitleListComponent(int color) {
        boolean none = JLFORKADDON_DRAWING_NONE_TITLE.get();
        JLFORKADDON_DRAWING_NONE_TITLE.set(false);
        return none ? JLFORKADDON_NONE_COLOR : color;
    }

    private static List<Title> jlforkaddon$filterDeletedTitles(List<Title> source) {
        List<Title> filtered = new ArrayList<>();
        if (source == null) {
            return filtered;
        }

        for (Title title : source) {
            if (title != null && !jlforkaddon$isHiddenTitle(title)) {
                filtered.add(title);
            }
        }
        return filtered;
    }

    private static boolean jlforkaddon$isHiddenTitle(Title title) {
        return title == null
                || BackportRegistryState.isTitleDeleted(title.getName())
                || BackportRegistryState.isTitleDisabledByAddonConfig(title.getName());
    }

    private static void jlforkaddon$replaceIfMutable(List<Title> target, List<Title> replacement) {
        if (target == null) {
            return;
        }

        try {
            target.clear();
            target.addAll(replacement);
        } catch (UnsupportedOperationException ignored) {
            // Some upstream builds use immutable lists; skip mutation safely.
        }
    }
}
