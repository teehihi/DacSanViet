package com.dacsanviet.repository;

import com.dacsanviet.model.ProductQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Product Q&A
 */
@Repository
public interface ProductQARepository extends JpaRepository<ProductQA, Long> {
    
    /**
     * Find all visible Q&A for a product (only parent questions, no replies), ordered by creation date descending
     */
    List<ProductQA> findByProductIdAndParentIdIsNullAndIsVisibleTrueOrderByCreatedAtDesc(Long productId);
    
    /**
     * Find all replies for a specific question
     */
    List<ProductQA> findByParentIdAndIsVisibleTrueOrderByCreatedAtAsc(Long parentId);
    
    /**
     * Count replies for a question
     */
    int countByParentIdAndIsVisibleTrue(Long parentId);
}
