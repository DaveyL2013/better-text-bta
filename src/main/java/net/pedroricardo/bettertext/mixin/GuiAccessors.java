package net.pedroricardo.bettertext.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.render.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

public class GuiAccessors {
	@Environment(EnvType.CLIENT)
	@Mixin(value = Gui.class, remap = false)
	public interface GuiAccessor {
		@Invoker("drawRect")
		void invokeDrawRect(int minX, int minY, int maxX, int maxY, int argb);
	}

	@Environment(EnvType.CLIENT)
	@Mixin(value = GuiScreen.class, remap = false)
	public interface GuiScreenAccessor {
		@Accessor("fontRenderer")
		FontRenderer fontRenderer();

		@Accessor("mc")
		Minecraft mc();
	}
}
