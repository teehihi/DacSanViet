package com.dacsanviet.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dacsanviet.dao.OrderDao;
import com.dacsanviet.dao.UserDao;
import com.dacsanviet.model.Order;
import com.dacsanviet.model.OrderStatus;
import com.dacsanviet.model.PaymentStatus;
import com.dacsanviet.model.Role;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.OrderRepository;
import com.dacsanviet.repository.UserRepository;
import com.dacsanviet.service.OrderService;
import com.dacsanviet.service.ProductService;
import com.dacsanviet.service.EmailService;

/**
 * Admin API Controller for AJAX requests
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminApiController {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderService orderService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ProductService productService;

	@Autowired
	private EmailService emailService;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	/**
	 * TÍNH NĂNG MỚI: API Quên mật khẩu
	 */
	@PostMapping("/forgot-password")
	@PreAuthorize("permitAll()")
	public ResponseEntity<?> handleForgotPassword(@RequestParam String email) {
		Map<String, Object> response = new HashMap<>();
		try {
			Optional<User> userOpt = userRepository.findByUsernameOrEmail(email);
			if (userOpt.isEmpty()) {
				response.put("success", false);
				response.put("message", "Tài khoản hoặc Email không tồn tại!");
				return ResponseEntity.ok(response);
			}

			User user = userOpt.get();
			String newPassword = UUID.randomUUID().toString().substring(0, 8);
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);

			try {
				emailService.sendSimpleEmail(
						user.getEmail(),
						"Mật khẩu mới - Đặc Sản Việt",
						"Mật khẩu mới của bạn là: " + newPassword + "\nVui lòng đổi lại mật khẩu ngay sau khi đăng nhập."
				);
			} catch (Exception mailError) {
				response.put("success", false);
				response.put("message", "Đã reset mật khẩu nhưng gửi mail thất bại.");
				return ResponseEntity.ok(response);
			}

			response.put("success", true);
			response.put("message", "Mật khẩu mới đã được gửi về email!");
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Get Orders with pagination and filters (AJAX)
	 */
	@GetMapping("/orders")
	@Transactional
	public ResponseEntity<?> getOrders(@RequestParam(defaultValue = "0") int page,
									   @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String search,
									   @RequestParam(required = false) String status, @RequestParam(required = false) String startDate,
									   @RequestParam(required = false) String endDate) {

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());

			Page<Order> orders;

			if (status != null && !status.isEmpty()) {
				OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
				orders = orderRepository.findByStatus(orderStatus, pageable);
			} else if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
				LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
				LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
				orders = orderRepository.findByOrderDateBetween(start, end, pageable);
			} else {
				orders = orderRepository.findAll(pageable);
			}

			// Convert orders to DAO
			Page<OrderDao> orderDaos = orders.map(order -> {
				try {
					OrderDao dao = orderService.convertToDao(order);
					if (dao == null) {
						return createFallbackOrderDao(order);
					}
					return dao;
				} catch (Exception e) {
					return createFallbackOrderDao(order);
				}
			});

			return ResponseEntity.ok(orderDaos);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Get Order by ID (AJAX)
	 */
	@GetMapping("/orders/{id}")
	@Transactional
	public ResponseEntity<?> getOrder(@PathVariable Long id) {
		try {
			Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

			if (order.getOrderItems() != null) {
				order.getOrderItems().size();
			}

			OrderDao dao = orderService.convertToDao(order);
			if (dao == null) {
				dao = createFallbackOrderDao(order);
				dao.setOrderItems(new ArrayList<>());
			}

			return ResponseEntity.ok(dao);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Update Order Status (AJAX) - SỬA LỖI INCOMPATIBLE TYPES
	 */
	@PutMapping("/orders/{id}/status")
	public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
		try {
			Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
			String statusStr = request.get("status");
			OrderStatus oldStatus = order.getStatus();
			OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());

			order.setStatus(newStatus);
			if (request.containsKey("notes")) {
				order.setNotes(request.get("notes"));
			}
			orderRepository.save(order);

			OrderDao dao = orderService.convertToDao(order);
			if (oldStatus != newStatus) {
				if (newStatus == OrderStatus.SHIPPED) emailService.sendShippingNotificationEmail(dao);
				else if (newStatus == OrderStatus.DELIVERED) emailService.sendOrderCompletionEmail(dao);
			}
			return ResponseEntity.ok(dao);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Approve COD Order (AJAX)
	 */
	@PostMapping("/orders/{id}/approve-cod")
	public ResponseEntity<?> approveCODOrder(@PathVariable Long id, @RequestBody Map<String, String> request) {
		try {
			Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
			if (!"COD".equals(order.getPaymentMethod())) {
				return ResponseEntity.status(400).body(Map.of("success", false, "message", "Đây không phải đơn hàng COD"));
			}
			order.setStatus(OrderStatus.SHIPPED);
			if (request.get("shippingCarrier") != null) order.setShippingCarrier(request.get("shippingCarrier"));
			if (request.get("trackingNumber") != null) order.setTrackingNumber(request.get("trackingNumber"));
			orderRepository.save(order);
			return ResponseEntity.ok(Map.of("success", true, "message", "Đơn hàng COD đã được phê duyệt"));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
		}
	}

	/**
	 * Update Order Details (AJAX) - GIỮ LẠI LOGIC GỬI MAIL CHI TIẾT CỦA BẠN
	 */
	@PutMapping("/orders/{id}")
	@Transactional
	public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> request) {
		try {
			Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

			if (request.containsKey("status")) {
				OrderStatus oldS = order.getStatus();
				OrderStatus newS = OrderStatus.valueOf(((String) request.get("status")).toUpperCase());
				order.setStatus(newS);
				if (oldS != newS) {
					OrderDao d = orderService.convertToDao(order);
					if (newS == OrderStatus.SHIPPED) emailService.sendShippingNotificationEmail(d);
					else if (newS == OrderStatus.DELIVERED) emailService.sendOrderCompletionEmail(d);
				}
			}

			if (request.containsKey("paymentStatus")) {
				PaymentStatus oldP = order.getPaymentStatus();
				PaymentStatus newP = PaymentStatus.valueOf(((String) request.get("paymentStatus")).toUpperCase());
				order.setPaymentStatus(newP);
				if (oldP != PaymentStatus.COMPLETED && newP == PaymentStatus.COMPLETED) {
					emailService.sendPaymentConfirmationEmail(orderService.convertToDao(order));
				}
			}

			if (request.get("shippingCarrier") != null) order.setShippingCarrier((String) request.get("shippingCarrier"));
			if (request.get("trackingNumber") != null) order.setTrackingNumber((String) request.get("trackingNumber"));

			orderRepository.save(order);
			return ResponseEntity.ok(Map.of("message", "Order updated successfully"));
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Get Users with pagination (AJAX)
	 */
	@GetMapping("/users")
	public ResponseEntity<Page<UserDao>> getUsers(@RequestParam(defaultValue = "0") int page,
												  @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String search) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<User> users = (search != null && !search.isEmpty())
				? userRepository.searchUsers(search, pageable)
				: userRepository.findAll(pageable);

		return ResponseEntity.ok(users.map(user -> {
			UserDao dao = new UserDao();
			dao.setId(user.getId());
			dao.setUsername(user.getUsername());
			dao.setEmail(user.getEmail());
			dao.setFullName(user.getFullName());
			dao.setPhoneNumber(user.getPhoneNumber());
			dao.setCreatedAt(user.getCreatedAt());
			dao.setTotalOrders(orderRepository.countByUserId(user.getId()));
			BigDecimal totalSpent = orderRepository.sumTotalAmountByUserId(user.getId());
			dao.setTotalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO);
			return dao;
		}));
	}

	/**
	 * Create User (AJAX)
	 */
	@PostMapping("/users")
	public ResponseEntity<?> createUser(@RequestBody UserDao userDao) {
		try {
			if (userRepository.existsByUsername(userDao.getUsername())) {
				return ResponseEntity.status(400).body(Map.of("error", "Username đã tồn tại"));
			}
			User user = new User();
			user.setUsername(userDao.getUsername());
			user.setEmail(userDao.getEmail());
			user.setFullName(userDao.getFullName());
			user.setPassword(passwordEncoder.encode(userDao.getPassword()));
			user.setRole(userDao.getRole() != null ? userDao.getRole() : Role.USER);
			user.setIsActive(true);
			userRepository.save(user);
			return ResponseEntity.ok(Map.of("message", "Tạo người dùng thành công"));
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Get User by ID (AJAX)
	 */
	@GetMapping("/users/{id}")
	public ResponseEntity<?> getUser(@PathVariable Long id) {
		try {
			User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
			UserDao dao = new UserDao();
			dao.setId(user.getId());
			dao.setUsername(user.getUsername());
			dao.setEmail(user.getEmail());
			dao.setFullName(user.getFullName());
			dao.setPhoneNumber(user.getPhoneNumber());
			dao.setTotalOrders(orderRepository.countByUserId(user.getId()));
			return ResponseEntity.ok(dao);
		} catch (Exception e) {
			return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Update User (AJAX)
	 */
	@PutMapping("/users/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDao userDao) {
		try {
			User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
			if (userDao.getFullName() != null) user.setFullName(userDao.getFullName());
			if (userDao.getEmail() != null) user.setEmail(userDao.getEmail());
			userRepository.save(user);
			return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Delete User (AJAX)
	 */
	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		try {
			userRepository.deleteById(id);
			return ResponseEntity.ok(Map.of("message", "Xóa thành công"));
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/orders/export")
	public ResponseEntity<String> exportOrders() { return ResponseEntity.ok("Tính năng đang phát triển"); }

	@GetMapping("/customers/export")
	public ResponseEntity<String> exportCustomers() { return ResponseEntity.ok("Tính năng đang phát triển"); }

	@DeleteMapping("/products/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
		try {
			productService.deleteProduct(id);
			return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công"));
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}

	// Helper fallback
	private OrderDao createFallbackOrderDao(Order order) {
		OrderDao dao = new OrderDao();
		dao.setId(order.getId());
		dao.setOrderNumber(order.getOrderNumber());
		dao.setTotalAmount(order.getTotalAmount());
		dao.setStatus(order.getStatus());
		dao.setOrderDate(order.getOrderDate());
		dao.setPaymentStatus(order.getPaymentStatus());
		dao.setCustomerName(order.getCustomerName());
		dao.setCustomerEmail(order.getCustomerEmail());
		return dao;
	}
}