package com.mt.springmongo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spring-ai")
public class SpringAiController {

	private final AiPlatformClient aiPlatformClient;

	public SpringAiController(AiPlatformClient aiPlatformClient) {
		this.aiPlatformClient = aiPlatformClient;
	}

	@PostMapping("/sentiment")
	public AiResponse sentiment(@RequestBody String text) {
		return aiPlatformClient.analyzeSentiment(text);
	}

	@PostMapping("/summarize")
	public AiResponse summarize(@RequestBody String text) {
		return aiPlatformClient.summarize(text);
	}
}
