package net.pedroricardo.bettertext.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.core.util.collection.Pair;
import net.pedroricardo.bettertext.BetterTextEditor;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = GuiChat.class, remap = false)
public abstract class BetterTextChatMixin {
	@Shadow
	@Final
	private TextFieldEditor editor;

	@Shadow
	protected String message;

	@Shadow
	public abstract String getText();

	@Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiChat;drawString(Lnet/minecraft/client/render/FontRenderer;Ljava/lang/String;III)V", ordinal = 0, shift = At.Shift.AFTER))
	private void bettertext$drawSelection(int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		Pair<Integer, Integer> selection = ((BetterTextEditor)this.editor).getSelectionRange();
		if (selection != null) {
			((GuiAccessors.GuiAccessor) this).invokeDrawRect(18 + ((GuiAccessors.GuiScreenAccessor)this).fontRenderer().getStringWidth(this.message.substring(0, selection.getLeft())), ((GuiChat)(Object)this).height - 14, 18 + ((GuiAccessors.GuiScreenAccessor)this).fontRenderer().getStringWidth(this.message.substring(0, selection.getRight())), ((GuiChat)(Object)this).height - 2, 0x882929ff);
		}
	}

	@SuppressWarnings("UnreachableCode")
	@Inject(method = "mouseClicked", at = @At("TAIL"))
	private void bettertext$moveCursorWhenClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
		//noinspection ConstantValue
		if (this.getText().isEmpty() || mouseX < 18 || mouseY < ((GuiChat)(Object)this).height - 13 || mouseY >= ((GuiChat)(Object)this).height - 1) return;
		int i = 0;
		int width = 0;
		int position = this.getText().length();
		while (i < this.getText().length()) {
			int charWidth = ((GuiAccessors.GuiScreenAccessor)this).fontRenderer().getCharWidth(this.getText().charAt(i));
			width += charWidth;
			if (width - (charWidth / 2) > mouseX - 18) {
				position = i;
				break;
			}
			++i;
		}
		this.editor.setCursor(position);
		if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
			((BetterTextEditor)this.editor).setSelection(((BetterTextEditor)this.editor).getSelection().getLeft(), this.editor.getCursor());
		} else {
			((BetterTextEditor)this.editor).resetSelection();
		}
	}
}
