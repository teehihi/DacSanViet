package com.dacsanviet.dto;

import java.time.LocalDateTime;

/**
 * DTO for Product Q&A messages
 */
public class ProductQAMessage {
    private Long id;
    private Long parentId;
    private Long productId;
    private String userName;
    private String userEmail;
    private String question;
    private String answer;
    private String timestamp;
    private String answerTimestamp;
    private Integer likesCount;
    private Integer repliesCount;
    
    public ProductQAMessage() {}
    
    public ProductQAMessage(Long productId, String userName, String question, String timestamp) {
        this.productId = productId;
        this.userName = userName;
        this.question = question;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getAnswerTimestamp() {
        return answerTimestamp;
    }
    
    public void setAnswerTimestamp(String answerTimestamp) {
        this.answerTimestamp = answerTimestamp;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public Integer getLikesCount() {
        return likesCount;
    }
    
    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }
    
    public Integer getRepliesCount() {
        return repliesCount;
    }
    
    public void setRepliesCount(Integer repliesCount) {
        this.repliesCount = repliesCount;
    }
}
