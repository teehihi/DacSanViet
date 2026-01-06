package com.dacsanviet.controller;

import com.dacsanviet.model.Category;
import com.dacsanviet.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller để cleanup và reset dữ liệu categories
 */
@RestController
@RequestMapping("/api/demo")
public class DataCleanupController {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanupController.class);

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Reset province categories - Public endpoint for demo
     */
    @PostMapping("/reset-provinces-demo")
    @GetMapping("/reset-provinces-demo") // Support both GET and POST for simplicity
    public ResponseEntity<?> resetProvinceCategoriesDemo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("🧹 Starting province categories cleanup...");
            
            // Tìm các danh mục miền chính
            Optional<Category> mienBacOpt = categoryRepository.findByName("Đặc Sản Miền Bắc");
            Optional<Category> mienTrungOpt = categoryRepository.findByName("Đặc Sản Miền Trung");
            Optional<Category> mienNamOpt = categoryRepository.findByName("Đặc Sản Miền Nam");

            if (mienBacOpt.isEmpty() || mienTrungOpt.isEmpty() || mienNamOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy danh mục miền chính");
                return ResponseEntity.badRequest().body(response);
            }

            Category mienBac = mienBacOpt.get();
            Category mienTrung = mienTrungOpt.get();
            Category mienNam = mienNamOpt.get();

            // Xóa tất cả danh mục con hiện tại
            deleteChildCategories(mienBac);
            deleteChildCategories(mienTrung);
            deleteChildCategories(mienNam);

            // Tạo lại danh mục tỉnh thành mới (chỉ 4 tỉnh cho mỗi miền)
            createNewProvinceCategories();

            response.put("success", true);
            response.put("message", "Đã reset thành công danh mục tỉnh thành. Mỗi miền giờ chỉ có 4 tỉnh tiêu biểu.");
            
            logger.info("✅ Province categories cleanup completed successfully!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error during province cleanup: ", e);
            response.put("success", false);
            response.put("message", "Lỗi khi reset danh mục: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private void deleteChildCategories(Category parentCategory) {
        List<Category> children = categoryRepository.findAll().stream()
                .filter(cat -> cat.getParent() != null && cat.getParent().getId().equals(parentCategory.getId()))
                .toList();
        
        for (Category child : children) {
            logger.info("🗑️ Deleting province category: {}", child.getName());
            categoryRepository.delete(child);
        }
    }

    private void createNewProvinceCategories() {
        // Tìm lại các danh mục miền
        Category mienBac = categoryRepository.findByName("Đặc Sản Miền Bắc").orElse(null);
        Category mienTrung = categoryRepository.findByName("Đặc Sản Miền Trung").orElse(null);
        Category mienNam = categoryRepository.findByName("Đặc Sản Miền Nam").orElse(null);

        if (mienBac == null || mienTrung == null || mienNam == null) {
            throw new RuntimeException("Không tìm thấy danh mục miền chính");
        }

        // Tạo danh mục tỉnh thành mới cho miền Bắc
        createProvinceCategory("Hà Nội", "Đặc sản từ thủ đô Hà Nội - Bánh chưng, bánh giầy, chả cá Lã Vọng", mienBac);
        createProvinceCategory("Hải Phòng", "Đặc sản từ thành phố cảng Hải Phòng - Bánh đa cua, nem cua bể", mienBac);
        createProvinceCategory("Quảng Ninh", "Đặc sản từ Quảng Ninh - Ngọc trai, hải sản tươi sống", mienBac);
        createProvinceCategory("Thái Nguyên", "Đặc sản từ Thái Nguyên - Chè Thái Nguyên, cốm xanh", mienBac);

        // Tạo danh mục tỉnh thành mới cho miền Trung
        createProvinceCategory("Thừa Thiên Huế", "Đặc sản từ Huế - Bún bò Huế, bánh khoái, chè Huế", mienTrung);
        createProvinceCategory("Đà Nẵng", "Đặc sản từ Đà Nẵng - Mì Quảng, bánh tráng cuốn thịt heo", mienTrung);
        createProvinceCategory("Quảng Nam", "Đặc sản từ Quảng Nam - Mì Quảng, bánh xèo, cao lầu", mienTrung);
        createProvinceCategory("Khánh Hòa", "Đặc sản từ Khánh Hòa - Bánh căn, nem nướng Nha Trang", mienTrung);

        // Tạo danh mục tỉnh thành mới cho miền Nam
        createProvinceCategory("TP. Hồ Chí Minh", "Đặc sản từ TP.HCM - Bánh mì, hủ tiếu, bánh xèo", mienNam);
        createProvinceCategory("Cần Thơ", "Đặc sản từ Cần Thơ - Bánh cống, bánh xèo", mienNam);
        createProvinceCategory("An Giang", "Đặc sản từ An Giang - Bánh pía, bánh tét", mienNam);
        createProvinceCategory("Bến Tre", "Đặc sản từ Bến Tre - Kẹo dừa, bánh tráng dừa", mienNam);
    }

    private void createProvinceCategory(String name, String description, Category parent) {
        // Kiểm tra xem đã tồn tại chưa
        Optional<Category> existing = categoryRepository.findByName(name);
        if (existing.isPresent()) {
            logger.info("ℹ️ Province category already exists: {}", name);
            return;
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setParent(parent);
        category.setIsActive(true);
        
        categoryRepository.save(category);
        logger.info("✅ Created province category: {} under {}", name, parent.getName());
    }
}