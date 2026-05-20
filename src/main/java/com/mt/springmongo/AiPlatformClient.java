package com.mt.springmongo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiPlatformClient {

	private final WebClient webClient;

	public AiPlatformClient(@Value("${ai.platform.base-url}") String baseUrl) {
		this.webClient = WebClient.builder().baseUrl(baseUrl).build();
	}

	public AiResponse analyzeSentiment(String text) {
		return webClient.post()
				.uri("/predict/sentiment")
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(new AiRequest(text)))
				.retrieve()
				.bodyToMono(AiResponse.class)
				.block();
	}

	public AiResponse summarize(String text) {
		return webClient.post()
				.uri("/predict/summarize")
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(new AiRequest(text)))
				.retrieve()
				.bodyToMono(AiResponse.class)
				.block();
	}
}
