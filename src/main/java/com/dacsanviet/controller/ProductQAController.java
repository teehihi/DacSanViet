package com.dacsanviet.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dacsanviet.dto.ProductQAMessage;
import com.dacsanviet.model.ProductQA;
import com.dacsanviet.service.ProductQAService;

/**
 * WebSocket controller for Product Q&A
 */
@Controller
public class ProductQAController {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private ProductQAService productQAService;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * Handle incoming Q&A messages via WebSocket
	 */
	@MessageMapping("/product/qa")
	public void handleQuestion(ProductQAMessage message) {
		// Save to database
		ProductQA savedQA = productQAService.saveQuestion(message);

		// Update message with saved timestamp
		message.setTimestamp(savedQA.getCreatedAt().format(FORMATTER));

		System.out.println("Saved Q&A for product " + message.getProductId() + " from " + message.getUserName() + ": "
				+ message.getQuestion());

		// Broadcast to specific product topic
		String destination = "/topic/product/" + message.getProductId() + "/qa";
		messagingTemplate.convertAndSend(destination, message);
	}

	/**
	 * REST endpoint to get existing Q&A for a product
	 */
	@GetMapping("/api/products/{productId}/qa")
	@ResponseBody
	public ResponseEntity<List<ProductQAMessage>> getProductQA(@PathVariable Long productId) {
		List<ProductQAMessage> qaList = productQAService.getVisibleQAForProduct(productId);
		return ResponseEntity.ok(qaList);
	}

	/**
	 * REST endpoint to get replies for a question
	 */
	@GetMapping("/api/products/qa/{questionId}/replies")
	@ResponseBody
	public ResponseEntity<List<ProductQAMessage>> getQuestionReplies(@PathVariable Long questionId) {
		List<ProductQAMessage> replies = productQAService.getRepliesForQuestion(questionId);
		return ResponseEntity.ok(replies);
	}

	/**
	 * REST endpoint to toggle like and broadcast via WebSocket
	 */
	@GetMapping("/api/products/qa/{qaId}/like")
	@ResponseBody
	public ResponseEntity<Integer> toggleLike(@PathVariable Long qaId, @RequestParam String userIdentifier) {

		ProductQA qa = productQAService.toggleLike(qaId, userIdentifier);

		// Broadcast like update to all users
		ProductQAMessage likeUpdate = new ProductQAMessage();
		likeUpdate.setId(qa.getId());
		likeUpdate.setProductId(qa.getProductId());
		likeUpdate.setLikesCount(qa.getLikesCount());

		String destination = "/topic/product/" + qa.getProductId() + "/qa/like";
		messagingTemplate.convertAndSend(destination, likeUpdate);

		return ResponseEntity.ok(qa.getLikesCount());
	}

	/**
	 * Check if user has liked a Q&A
	 */
	@GetMapping("/api/products/qa/{qaId}/hasLiked")
	@ResponseBody
	public ResponseEntity<Boolean> hasLiked(@PathVariable Long qaId, @RequestParam String userIdentifier) {

		boolean hasLiked = productQAService.hasUserLiked(qaId, userIdentifier);
		return ResponseEntity.ok(hasLiked);
	}
}
