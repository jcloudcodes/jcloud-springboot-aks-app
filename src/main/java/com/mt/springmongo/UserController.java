package com.mt.springmongo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

	private static final String AI_HISTORY_SESSION_KEY = "aiHistory";

	private final UserRepository userRepository;
	private final AiPlatformClient aiPlatformClient;

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	public UserController(final UserRepository userRepository, final AiPlatformClient aiPlatformClient) {
		this.userRepository = userRepository;
		this.aiPlatformClient = aiPlatformClient;
	}

	@PostMapping(value = "/save")
	public String save(@RequestParam("firstName") String firstName,
					   @RequestParam("lastName") String lastName,
					   @RequestParam("email") String email,
					   @RequestParam("password") String password) {

		logger.info("Creating user name: " + firstName);

		User user = new User(firstName, lastName, email, password);
		userRepository.save(user);

		return "redirect:/login";
	}

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@PostMapping("/login")
	public String loginUser(@RequestParam String email,
							@RequestParam String password,
							Model model) {

		User user = userRepository.findByEmail(email);

		if (user == null || !user.getPassword().equals(password)) {
			model.addAttribute("error", "Invalid email or password");
			return "login";
		}

		return "redirect:/dashboard/" + user.getId();
	}

	@GetMapping("/dashboard/{id}")
	public String dashboard(@PathVariable String id, Model model, HttpSession session) {
		return loadDashboard(id, model, session);
	}

	@PostMapping("/dashboard/{id}/ai/sentiment")
	public String analyzeSentiment(@PathVariable String id,
								   @RequestParam("aiText") String aiText,
								   Model model,
								   HttpSession session) {
		return handleAiRequest(id, aiText, "sentiment", model, session);
	}

	@PostMapping("/dashboard/{id}/ai/summarize")
	public String summarizeText(@PathVariable String id,
								@RequestParam("aiText") String aiText,
								Model model,
								HttpSession session) {
		return handleAiRequest(id, aiText, "summarize", model, session);
	}

	private String handleAiRequest(String id, String aiText, String action, Model model, HttpSession session) {
		String dashboard = loadDashboard(id, model, session);

		if (!"dashboard".equals(dashboard)) {
			return dashboard;
		}

		model.addAttribute("aiText", aiText);

		if (aiText == null || aiText.trim().isEmpty()) {
			model.addAttribute("aiError", "Please enter text before calling the AI platform.");
			return "dashboard";
		}

		try {
			AiResponse response = "summarize".equals(action)
					? aiPlatformClient.summarize(aiText)
					: aiPlatformClient.analyzeSentiment(aiText);

			model.addAttribute("aiResponse", response);
			model.addAttribute("aiAction", action);
			model.addAttribute("aiSentimentClass", sentimentClass(response));
			recordAiHistory(session, action, aiText, response);
		} catch (Exception ex) {
			logger.error("AI platform request failed", ex);
			model.addAttribute("aiError", "AI platform call failed: " + ex.getMessage());
		}

		return "dashboard";
	}

	private String loadDashboard(String id, Model model, HttpSession session) {
		User user = userRepository.findById(id).orElse(null);

		if (user == null) {
			return "redirect:/login";
		}

		model.addAttribute("user", user);
		model.addAttribute("aiHistory", getAiHistory(session));
		return "dashboard";
	}

	private void recordAiHistory(HttpSession session, String action, String aiText, AiResponse response) {
		List<AiHistoryItem> history = getAiHistory(session);
		String preview = aiText == null ? "" : aiText.trim();

		if (preview.length() > 120) {
			preview = preview.substring(0, 120) + "...";
		}

		String result;
		if (response.getSummary() != null && !response.getSummary().trim().isEmpty()) {
			result = response.getSummary();
		} else if (response.getPrediction() != null) {
			result = response.getPrediction() + (response.getConfidence() != null ? " (" + response.getConfidence() + ")" : "");
		} else if (response.getError() != null) {
			result = response.getError();
		} else {
			result = "No result returned";
		}

		if (result.length() > 140) {
			result = result.substring(0, 140) + "...";
		}

		AiHistoryItem item = new AiHistoryItem(action, preview, result, response.getProvider(), response.getPrediction());
		history.add(0, item);

		if (history.size() > 5) {
			history.remove(history.size() - 1);
		}

		session.setAttribute(AI_HISTORY_SESSION_KEY, history);
	}

	@SuppressWarnings("unchecked")
	private List<AiHistoryItem> getAiHistory(HttpSession session) {
		Object history = session.getAttribute(AI_HISTORY_SESSION_KEY);

		if (history instanceof List) {
			return (List<AiHistoryItem>) history;
		}

		List<AiHistoryItem> emptyHistory = new ArrayList<AiHistoryItem>();
		session.setAttribute(AI_HISTORY_SESSION_KEY, emptyHistory);
		return emptyHistory;
	}

	private String sentimentClass(AiResponse response) {
		if (response == null || response.getPrediction() == null) {
			return "label-default";
		}

		if ("POSITIVE".equalsIgnoreCase(response.getPrediction())) {
			return "label-success";
		}

		if ("NEGATIVE".equalsIgnoreCase(response.getPrediction())) {
			return "label-danger";
		}

		return "label-warning";
	}

	public static class AiHistoryItem {
		private final String action;
		private final String inputPreview;
		private final String resultPreview;
		private final String provider;
		private final String prediction;

		public AiHistoryItem(String action, String inputPreview, String resultPreview, String provider, String prediction) {
			this.action = action;
			this.inputPreview = inputPreview;
			this.resultPreview = resultPreview;
			this.provider = provider;
			this.prediction = prediction;
		}

		public String getAction() {
			return action;
		}

		public String getInputPreview() {
			return inputPreview;
		}

		public String getResultPreview() {
			return resultPreview;
		}

		public String getProvider() {
			return provider;
		}

		public String getPrediction() {
			return prediction;
		}
	}
}
