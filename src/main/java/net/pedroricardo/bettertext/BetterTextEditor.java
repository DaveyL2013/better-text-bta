package net.pedroricardo.bettertext;

import net.minecraft.core.util.collection.Pair;

public interface BetterTextEditor {
	Pair<Integer, Integer> getSelection();
	Pair<Integer, Integer> getSelectionRange();
	void setSelection(int left, int right);
	void resetSelection();
}
