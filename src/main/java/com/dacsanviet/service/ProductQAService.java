package com.dacsanviet.service;

import com.dacsanviet.dto.ProductQAMessage;
import com.dacsanviet.model.ProductQA;
import com.dacsanviet.model.ProductQALike;
import com.dacsanviet.repository.ProductQARepository;
import com.dacsanviet.repository.ProductQALikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Product Q&A operations
 */
@Service
public class ProductQAService {
    
    @Autowired
    private ProductQARepository productQARepository;
    
    @Autowired
    private ProductQALikeRepository productQALikeRepository;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Save a new question or reply
     */
    @Transactional
    public ProductQA saveQuestion(ProductQAMessage message) {
        ProductQA qa = new ProductQA();
        qa.setProductId(message.getProductId());
        qa.setParentId(message.getParentId());
        qa.setUserName(message.getUserName());
        qa.setUserEmail(message.getUserEmail());
        qa.setQuestion(message.getQuestion());
        qa.setIsAnswered(false);
        qa.setIsVisible(true);
        qa.setLikesCount(0);
        
        return productQARepository.save(qa);
    }
    
    /**
     * Get all visible Q&A for a product (with replies count)
     */
    public List<ProductQAMessage> getVisibleQAForProduct(Long productId) {
        List<ProductQA> qaList = productQARepository.findByProductIdAndParentIdIsNullAndIsVisibleTrueOrderByCreatedAtDesc(productId);
        return qaList.stream()
                .map(qa -> {
                    ProductQAMessage message = convertToMessage(qa);
                    // Count replies
                    int repliesCount = productQARepository.countByParentIdAndIsVisibleTrue(qa.getId());
                    message.setRepliesCount(repliesCount);
                    return message;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get all replies for a question
     */
    public List<ProductQAMessage> getRepliesForQuestion(Long questionId) {
        List<ProductQA> replies = productQARepository.findByParentIdAndIsVisibleTrueOrderByCreatedAtAsc(questionId);
        return replies.stream()
                .map(this::convertToMessage)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert entity to DTO
     */
    private ProductQAMessage convertToMessage(ProductQA qa) {
        ProductQAMessage message = new ProductQAMessage();
        message.setId(qa.getId());
        message.setParentId(qa.getParentId());
        message.setProductId(qa.getProductId());
        message.setUserName(qa.getUserName());
        message.setUserEmail(qa.getUserEmail());
        message.setQuestion(qa.getQuestion());
        message.setAnswer(qa.getAnswer());
        message.setTimestamp(qa.getCreatedAt().format(FORMATTER));
        message.setLikesCount(qa.getLikesCount() != null ? qa.getLikesCount() : 0);
        if (qa.getAnsweredAt() != null) {
            message.setAnswerTimestamp(qa.getAnsweredAt().format(FORMATTER));
        }
        return message;
    }
    
    /**
     * Toggle like on a question/reply
     * Returns the updated ProductQA with new like count
     */
    @Transactional
    public ProductQA toggleLike(Long qaId, String userIdentifier) {
        ProductQA qa = productQARepository.findById(qaId)
                .orElseThrow(() -> new RuntimeException("Q&A not found"));
        
        // Check if user already liked
        boolean alreadyLiked = productQALikeRepository.existsByQaIdAndUserIdentifier(qaId, userIdentifier);
        
        if (alreadyLiked) {
            // Unlike: remove like record and decrement count
            productQALikeRepository.deleteByQaIdAndUserIdentifier(qaId, userIdentifier);
            int currentLikes = qa.getLikesCount() != null ? qa.getLikesCount() : 0;
            qa.setLikesCount(Math.max(0, currentLikes - 1));
        } else {
            // Like: add like record and increment count
            ProductQALike like = new ProductQALike();
            like.setQaId(qaId);
            like.setUserIdentifier(userIdentifier);
            productQALikeRepository.save(like);
            
            int currentLikes = qa.getLikesCount() != null ? qa.getLikesCount() : 0;
            qa.setLikesCount(currentLikes + 1);
        }
        
        return productQARepository.save(qa);
    }
    
    /**
     * Check if user has liked a Q&A
     */
    public boolean hasUserLiked(Long qaId, String userIdentifier) {
        return productQALikeRepository.existsByQaIdAndUserIdentifier(qaId, userIdentifier);
    }
    
    /**
     * Answer a question (admin function)
     */
    @Transactional
    public ProductQA answerQuestion(Long qaId, String answer, String answeredBy) {
        ProductQA qa = productQARepository.findById(qaId)
                .orElseThrow(() -> new RuntimeException("Q&A not found"));
        
        qa.setAnswer(answer);
        qa.setAnsweredBy(answeredBy);
        qa.setIsAnswered(true);
        qa.setAnsweredAt(LocalDateTime.now());
        
        return productQARepository.save(qa);
    }
}
