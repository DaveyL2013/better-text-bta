package net.pedroricardo.bettertext.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.text.TextFieldEditor;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.util.helper.MathHelper;
import net.pedroricardo.bettertext.BetterTextEditor;
import net.pedroricardo.bettertext.TextStackElement;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Stack;

@Environment(EnvType.CLIENT)
@Mixin(value = TextFieldEditor.class, remap = false)
public abstract class TextSelectionMixin implements BetterTextEditor {
	@Shadow
	public abstract String getText();
	@Shadow
	public abstract int getCursor();
	@Shadow
	public abstract void setText(String s);
	@Shadow
	public abstract void setCursor(int i);

	@Shadow
	protected abstract void stringToClipboard(String string);

	@Unique
	private Stack<TextStackElement> undoStack = new Stack<>();
	@Unique
	private Stack<TextStackElement> redoStack = new Stack<>();
	@Unique
	private Pair<Integer, Integer> selection = Pair.of(0, 0);

	@Inject(method = "deleteCharsBeforeCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/text/TextFieldEditor;setText(Ljava/lang/String;)V", ordinal = 0), cancellable = true)
	private void betterText$deleteSelectionBefore(int i, CallbackInfo ci) {
		this.getUndoStack().push(new TextStackElement(this.getText(), this.getCursor(), this.getSelection()));
		this.getRedoStack().clear();
		if (!this.getSelectionRange().getLeft().equals(this.getSelectionRange().getRight())) {
			this.setText(this.getText().substring(0, this.getSelectionRange().getLeft()) + this.getText().substring(this.getSelectionRange().getRight()));
			this.setCursor(this.getSelectionRange().getLeft());
			ci.cancel();
		}
	}

	@Inject(method = "deleteCharsAfterCursor", at = @At("HEAD"), cancellable = true)
	private void betterText$deleteSelectionAfter(int i, CallbackInfo ci) {
		this.getUndoStack().push(new TextStackElement(this.getText(), this.getCursor(), this.getSelection()));
		this.getRedoStack().clear();
		if (!this.getSelectionRange().getLeft().equals(this.getSelectionRange().getRight())) {
			this.setText(this.getText().substring(0, this.getSelectionRange().getLeft()) + this.getText().substring(this.getSelectionRange().getRight()));
			this.setCursor(this.getSelectionRange().getLeft());
			ci.cancel();
		}
	}

	@Inject(method = "addCharAtCursor", at = @At(value = "INVOKE", target = "Ljava/lang/String;length()I", ordinal = 3), cancellable = true)
	private void bettertext$replaceSelectionWithChar(char c, CallbackInfo ci) {
		this.getUndoStack().push(new TextStackElement(this.getText(), this.getCursor(), this.getSelection()));
		this.getRedoStack().clear();
		if (!this.getSelectionRange().getLeft().equals(this.getSelectionRange().getRight())) {
			this.setText(this.getText().substring(0, this.getSelectionRange().getLeft()) + c + this.getText().substring(this.getSelectionRange().getRight()));
			this.setCursor(this.getSelectionRange().getLeft() + 1);
			this.resetSelection();
			ci.cancel();
		}
	}

	@Inject(method = "handleInput", at = @At("RETURN"))
	private void bettertext$resetSelection(int key, char c, CallbackInfoReturnable<Boolean> cir) {
		if (key == 42 || key == 54) return;
		if (this.getSelectionRange().getLeft().equals(this.getSelectionRange().getRight())) {
			this.resetSelection();
		}
	}

	@WrapOperation(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/text/TextFieldEditor;stringToClipboard(Ljava/lang/String;)V"))
	private void bettertext$copySelection(TextFieldEditor instance, String clipboard, Operation<Void> original) {
		if (!this.getSelectionRange().getLeft().equals(this.getSelectionRange().getRight())) {
			String str = this.getText().substring(this.getSelectionRange().getLeft(), this.getSelectionRange().getRight());
			original.call(instance, str);
		}
	}

	@Inject(method = "handleInput", at = @At("HEAD"), cancellable = true)
	private void bettertext$shortcuts(int key, char c, CallbackInfoReturnable<Boolean> cir) {
		if ((Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157))) { // LCTRL / RCTRL
			if (key == 30) { // A
				this.setCursor(this.getText().length());
				this.setSelection(0, this.getText().length());
				cir.setReturnValue(false);
			}
			if (key == 44) { // Z
				if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) { // LSHIFT / RSHIFT
					this.redo();
				} else {
					this.undo();
				}
				cir.setReturnValue(true);
			}
			if (key == 21) { // Y
				this.redo();
				cir.setReturnValue(true);
			}
			if (key == 45) { // X
				if (!this.getSelectionRange().getLeft().equals(this.getSelectionRange().getRight())) {
					this.getUndoStack().push(new TextStackElement(this.getText(), this.getCursor(), this.getSelection()));
					this.getRedoStack().clear();
					String str = this.getText().substring(this.getSelectionRange().getLeft(), this.getSelectionRange().getRight());
					this.stringToClipboard(str);
					this.setText(this.getText().substring(0, this.getSelectionRange().getLeft()) + this.getText().substring(this.getSelectionRange().getRight()));
				}
			}
		}
	}

	@Unique
	private Stack<TextStackElement> getUndoStack() {
		if (this.undoStack == null) this.undoStack = new Stack<>();
		return this.undoStack;
	}

	@Unique
	private Stack<TextStackElement> getRedoStack() {
		if (this.redoStack == null) this.redoStack = new Stack<>();
		return this.redoStack;
	}

	@Unique
	private void redo() {
		if (this.getRedoStack().empty()) return;
		this.getUndoStack().push(new TextStackElement(this.getText(), this.getCursor(), this.getSelection()));
		this.setText(this.getRedoStack().peek().getText());
		this.setCursor(this.getRedoStack().peek().getCursor());
		this.setSelection(this.getRedoStack().peek().getSelection().getLeft(), this.getRedoStack().peek().getSelection().getRight());
		this.getRedoStack().pop();
	}

	@Unique
	private void undo() {
		if (this.getUndoStack().empty()) return;
		this.getRedoStack().push(new TextStackElement(this.getText(), this.getCursor(), this.getSelection()));
		this.setText(this.getUndoStack().peek().getText());
		this.setCursor(this.getUndoStack().peek().getCursor());
		this.setSelection(this.getUndoStack().peek().getSelection().getLeft(), this.getUndoStack().peek().getSelection().getRight());
		this.getUndoStack().pop();
	}

	@Inject(method = "setCursor", at = @At("TAIL"))
	private void bettertext$setSelectionOnCursorChange(int i, CallbackInfo ci) {
		if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
			this.setSelection(this.getSelection().getLeft(), this.getCursor());
		} else {
			this.resetSelection();
		}
	}

	@Override
	public Pair<Integer, Integer> getSelection() {
		this.setSelection(this.selection.getLeft(), this.selection.getRight());
		return this.selection;
	}

	@Override
	public Pair<Integer, Integer> getSelectionRange() {
		Pair<Integer, Integer> selection = this.getSelection();
		if (selection.getLeft() > selection.getRight()) {
			selection = Pair.of(selection.getRight(), selection.getLeft());
		}
		return selection;
	}

	@Override
	public void setSelection(int pivot, int selection) {
		this.selection = Pair.of(MathHelper.clamp(pivot, 0, this.getText().length()), MathHelper.clamp(selection, 0, this.getText().length()));
	}

	@Override
	public void resetSelection() {
		this.setSelection(this.getCursor(), this.getCursor());
	}
}
