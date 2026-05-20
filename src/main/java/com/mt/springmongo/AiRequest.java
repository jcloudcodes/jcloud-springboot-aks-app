package com.mt.springmongo;

public class AiRequest {
	private String text;

	public AiRequest() {
	}

	public AiRequest(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
