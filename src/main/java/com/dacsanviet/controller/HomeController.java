package com.dacsanviet.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dacsanviet.dao.CategoryDao;
import com.dacsanviet.dao.ProductDao;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.UserRepository;
import com.dacsanviet.dto.NewsArticleDto;
import com.dacsanviet.dto.ConsultationRequest;
import com.dacsanviet.service.CategoryService;
import com.dacsanviet.service.EmailService;
import com.dacsanviet.service.NewsService;
import com.dacsanviet.service.ProductService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Home controller handles public navigation, homepage data, and security requests.
 */
@Slf4j
@Controller
public class HomeController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private NewsService newsService;

	@Autowired
	private UserRepository userRepository; // Sử dụng UserRepository bạn vừa gửi

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/")
	public String home(Model model) {
		try {
			Pageable featuredPageable = PageRequest.of(0, 8);
			Page<ProductDao> featuredProducts = productService.getFeaturedProducts(featuredPageable);
			model.addAttribute("featuredProducts", featuredProducts.getContent());

			Pageable newPageable = PageRequest.of(0, 20);
			Page<ProductDao> newProducts = productService.getAllProducts(newPageable);
			model.addAttribute("newProducts", newProducts.getContent());

			List<CategoryDao> categories = categoryService.getAllActiveCategories();
			model.addAttribute("categories", categories);

			List<NewsArticleDto> latestNews = newsService.findRecentArticles(3);
			model.addAttribute("newsList", latestNews);

		} catch (Exception e) {
			log.error("Lỗi khi tải dữ liệu trang chủ: {}", e.getMessage());
			model.addAttribute("featuredProducts", List.of());
			model.addAttribute("newProducts", List.of());
			model.addAttribute("categories", List.of());
			model.addAttribute("newsList", List.of());
		}
		return "index";
	}

	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("pageTitle", "Đăng Nhập");
		model.addAttribute("categories", categoryService.getAllActiveCategories());
		return "auth/login";
	}

	/**
	 * API xử lý Quên mật khẩu
	 */
	@PostMapping("/api/forgot-password")
	@ResponseBody
	public ResponseEntity<?> handleForgotPassword(@RequestParam String email) {
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("Yêu cầu quên mật khẩu cho: {}", email);

			// Tìm bằng hàm findByUsernameOrEmail có sẵn trong Repository của bạn
			Optional<User> userOpt = userRepository.findByUsernameOrEmail(email);

			if (userOpt.isEmpty()) {
				response.put("success", false);
				response.put("message", "Tài khoản hoặc Email không tồn tại!");
				return ResponseEntity.ok(response);
			}

			User user = userOpt.get();

			// Tạo mật khẩu mới
			String newPassword = UUID.randomUUID().toString().substring(0, 8);

			// Cập nhật mật khẩu đã mã hóa
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);

			// Gửi Email
			try {
				emailService.sendSimpleEmail(
						user.getEmail(),
						"Mật khẩu mới - Đặc Sản Việt",
						"Chào " + user.getFullName() + ",\n\nMật khẩu mới của bạn là: " + newPassword +
								"\n\nVui lòng đăng nhập và đổi mật khẩu ngay để bảo mật."
				);
			} catch (Exception mailEx) {
				log.error("Lỗi gửi mail: {}", mailEx.getMessage());
				response.put("success", false);
				response.put("message", "Mật khẩu đã reset nhưng không thể gửi email. Hãy liên hệ hỗ trợ.");
				return ResponseEntity.ok(response);
			}

			response.put("success", true);
			response.put("message", "Mật khẩu mới đã được gửi về email của bạn!");
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Lỗi xử lý đặt lại mật khẩu: {}", e.getMessage());
			response.put("success", false);
			response.put("message", "Có lỗi xảy ra, vui lòng thử lại sau.");
			return ResponseEntity.status(500).body(response);
		}
	}

	@PostMapping("/api/consultation")
	@ResponseBody
	public ResponseEntity<?> submitConsultation(@Valid @RequestBody ConsultationRequest request) {
		Map<String, String> response = new HashMap<>();
		try {
			emailService.sendConsultationEmail(request);
			response.put("message", "Cảm ơn bạn! Chúng tôi sẽ liên hệ tư vấn trong vòng 24h.");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.put("error", "Có lỗi xảy ra!");
			return ResponseEntity.status(500).body(response);
		}
	}

	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("pageTitle", "Đăng Ký");
		model.addAttribute("categories", categoryService.getAllActiveCategories());
		return "auth/register";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("pageTitle", "Giới Thiệu");
		model.addAttribute("categories", categoryService.getAllActiveCategories());
		return "pages/about";
	}

	@GetMapping("/contact")
	public String contact(Model model) {
		model.addAttribute("pageTitle", "Liên Hệ");
		model.addAttribute("categories", categoryService.getAllActiveCategories());
		return "pages/contact";
	}
}