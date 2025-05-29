package net.pedroricardo.bettertext.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.TextFieldElement;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.client.render.Font;
import net.minecraft.core.util.collection.Pair;
import net.pedroricardo.bettertext.BetterTextEditor;
import net.pedroricardo.bettertext.mixin.Accessors.GuiAccessor;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = TextFieldElement.class, remap = false)
public abstract class BetterTextFieldElementMixin {
	@Shadow
	private TextFieldEditor editor;

	@Shadow
	private String text;

	@Shadow
	public int xPosition;

	@Shadow
	public int yPosition;

	@Shadow
	@Final
	private Font font;

	@Shadow
	public abstract String getText();

	@Inject(method = "drawTextBox", at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/TextFieldElement;drawString(Lnet/minecraft/client/render/Font;Ljava/lang/String;III)V", ordinal = 1, shift = At.Shift.AFTER), @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/TextFieldElement;drawString(Lnet/minecraft/client/render/Font;Ljava/lang/String;III)V", ordinal = 2, shift = At.Shift.AFTER), @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/TextFieldElement;drawString(Lnet/minecraft/client/render/Font;Ljava/lang/String;III)V", ordinal = 4, shift = At.Shift.AFTER)})
	private void bettertext$drawSelection(CallbackInfo ci) {
		Pair<Integer, Integer> selection = ((BetterTextEditor)this.editor).getSelectionRange();
		if (selection != null) {
			((GuiAccessor) this).invokeDrawRect(this.xPosition + 4 + this.font.getStringWidth(this.text.substring(0, selection.getLeft())), this.yPosition + (((TextFieldElement)(Object)this).height - 8) / 2 - 2, this.xPosition + 4 + this.font.getStringWidth(this.text.substring(0, selection.getRight())), this.yPosition + (((TextFieldElement)(Object)this).height - 8) / 2 + 10, 0x882929ff);
		}
	}

	@Inject(method = "mouseClicked", at = @At("TAIL"))
	private void bettertext$moveCursorWhenClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
		if (this.getText().isEmpty() || mouseX < this.xPosition + 4 || mouseY < this.yPosition + 4 || mouseY >= this.yPosition + 16) return;
		int i = 0;
		int width = 0;
		int position = this.getText().length();
		while (i < this.getText().length()) {
			int charWidth = this.font.getCharWidth(this.getText().charAt(i));
			width += charWidth;
			if (width - (charWidth / 2) > mouseX - (this.xPosition + 4)) {
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
