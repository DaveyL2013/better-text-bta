package net.pedroricardo.bettertext;

import net.minecraft.core.util.collection.Pair;

public class TextStackElement {
	private final String text;
	private final int cursor;
	private final Pair<Integer, Integer> selection;

	public TextStackElement(String text, int cursor, Pair<Integer, Integer> selection) {
		this.text = text;
		this.cursor = cursor;
		this.selection = selection;
	}

	public String getText() {
		return this.text;
	}

	public int getCursor() {
		return this.cursor;
	}

	public Pair<Integer, Integer> getSelection() {
		return this.selection;
	}
}
