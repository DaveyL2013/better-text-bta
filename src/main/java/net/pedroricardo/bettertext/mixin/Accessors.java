package net.pedroricardo.bettertext.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

public class Accessors {
	@Environment(EnvType.CLIENT)
	@Mixin(value = Gui.class, remap = false)
	public interface GuiAccessor {
		@Invoker("drawRect")
		void invokeDrawRect(int minX, int minY, int maxX, int maxY, int argb);
	}

	@Environment(EnvType.CLIENT)
	@Mixin(value = Screen.class, remap = false)
	public interface ScreenAccessor {
		@Accessor("font")
		Font font();

		@Accessor("mc")
		Minecraft mc();
	}
}
