package com.seniors.justlevelingfork.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TooltipContext(Screen screen, PoseStack matrixStack, int mouseX, int mouseY, List<Component> components) implements AutoCloseable {
	public TooltipContext(Screen screen, PoseStack matrixStack, int mouseX, int mouseY) {
		this(screen, matrixStack, mouseX, mouseY, new ArrayList<>());
	}

	public void enqueueComponent(Component component) {
		this.components.add(component);
	}
	public void enqueueComponents(List<Component> components) {
		this.components.addAll(components);
	}

	@Override
	public void close() {
		this.matrixStack.pushPose();
		this.screen.renderTooltip(matrixStack, this.components, Optional.empty(), this.mouseX, this.mouseY);
		this.matrixStack.popPose();
	}
}
