package com.dacsanviet.repository;

import com.dacsanviet.model.ProductQALike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Product Q&A Likes
 */
@Repository
public interface ProductQALikeRepository extends JpaRepository<ProductQALike, Long> {
    
    /**
     * Check if user already liked this Q&A
     */
    boolean existsByQaIdAndUserIdentifier(Long qaId, String userIdentifier);
    
    /**
     * Find like by Q&A and user
     */
    Optional<ProductQALike> findByQaIdAndUserIdentifier(Long qaId, String userIdentifier);
    
    /**
     * Delete like
     */
    void deleteByQaIdAndUserIdentifier(Long qaId, String userIdentifier);
}
