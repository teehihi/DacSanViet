package com.dacsanviet.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

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
				OrderStatus orderStatus = OrderStatus.valueOf(status);
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
						// Create a minimal DAO as fallback
						OrderDao fallback = new OrderDao();
						fallback.setId(order.getId());
						fallback.setOrderNumber(order.getOrderNumber());
						fallback.setTotalAmount(order.getTotalAmount());
						fallback.setStatus(order.getStatus());
						fallback.setOrderDate(order.getOrderDate());
						fallback.setPaymentStatus(order.getPaymentStatus());
						fallback.setCustomerName(order.getCustomerName());
						fallback.setCustomerEmail(order.getCustomerEmail());
						return fallback;
					}
					return dao;
				} catch (Exception e) {
					// Create a minimal DAO as fallback
					OrderDao fallback = new OrderDao();
					fallback.setId(order.getId());
					fallback.setOrderNumber(order.getOrderNumber());
					fallback.setTotalAmount(order.getTotalAmount());
					fallback.setStatus(order.getStatus());
					fallback.setOrderDate(order.getOrderDate());
					fallback.setPaymentStatus(order.getPaymentStatus());
					fallback.setCustomerName(order.getCustomerName());
					fallback.setCustomerEmail(order.getCustomerEmail());
					return fallback;
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

			// Force load orderItems to avoid LazyInitializationException
			if (order.getOrderItems() != null) {
				order.getOrderItems().size();
			}

			OrderDao dao = orderService.convertToDao(order);

			if (dao == null) {
				// Create fallback
				dao = new OrderDao();
				dao.setId(order.getId());
				dao.setOrderNumber(order.getOrderNumber());
				dao.setTotalAmount(order.getTotalAmount());
				dao.setShippingFee(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);
				dao.setStatus(order.getStatus());
				dao.setOrderDate(order.getOrderDate());
				dao.setPaymentStatus(order.getPaymentStatus());
				dao.setCustomerName(order.getCustomerName());
				dao.setCustomerEmail(order.getCustomerEmail());
				dao.setCustomerPhone(order.getCustomerPhone());
				dao.setShippingAddressText(order.getShippingAddressText());
				dao.setOrderItems(new ArrayList<>());
			}

			return ResponseEntity.ok(dao);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Update Order Status (AJAX)
	 */
	@PutMapping("/orders/{id}/status")
	public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {

		System.out.println("🔍 [DEBUG] updateOrderStatus called - Order ID: " + id + ", Request: " + request);
		
		Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

		String statusStr = request.get("status");
		OrderStatus oldStatus = order.getStatus();
		OrderStatus newStatus = OrderStatus.valueOf(statusStr);

		System.out.println("🔍 [DEBUG] Status change: " + oldStatus + " → " + newStatus);
		System.out.println("🔍 [DEBUG] Customer email: " + order.getCustomerEmail());

		order.setStatus(newStatus);
		
		// Update notes if provided
		if (request.containsKey("notes")) {
			order.setNotes(request.get("notes"));
		}
		
		orderRepository.save(order);

		// Send email notifications for status changes
		if (oldStatus != newStatus) {
			try {
				OrderDao orderDao = orderService.convertToDao(order);
				
				// Send shipping notification when order is shipped
				if (newStatus == OrderStatus.SHIPPED) {
					emailService.sendShippingNotificationEmail(orderDao);
					System.out.println("✅ [EMAIL] Shipping notification email sent for order: " + order.getOrderNumber());
				}
				// Send completion email when order is delivered
				else if (newStatus == OrderStatus.DELIVERED) {
					emailService.sendOrderCompletionEmail(orderDao);
					System.out.println("✅ [EMAIL] Order completion email sent for order: " + order.getOrderNumber());
				}
			} catch (Exception e) {
				System.err.println("❌ [EMAIL ERROR] Failed to send status notification email for order " + order.getOrderNumber() + ": " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			System.out.println("ℹ️ [DEBUG] No status change, no email sent");
		}

		// Convert to DAO for response
		OrderDao dao = orderService.convertToDao(order);
		return ResponseEntity.ok(dao);
	}

	/**
	 * Approve COD Order (AJAX) - Changes status from PROCESSING to SHIPPED
	 */
	@PostMapping("/orders/{id}/approve-cod")
	public ResponseEntity<?> approveCODOrder(@PathVariable Long id, @RequestBody Map<String, String> request) {
		try {
			Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

			// Validate that this is a COD order
			if (!"COD".equals(order.getPaymentMethod())) {
				return ResponseEntity.status(400).body(Map.of("success", false, "message", "Đây không phải đơn hàng COD"));
			}

			// Validate current status
			if (order.getStatus() != OrderStatus.PROCESSING && order.getStatus() != OrderStatus.PENDING) {
				return ResponseEntity.status(400).body(Map.of("success", false, "message", "Đơn hàng không ở trạng thái có thể phê duyệt"));
			}

			// Get request data
			String shippingCarrier = request.get("shippingCarrier");
			String trackingNumber = request.get("trackingNumber");
			String notes = request.get("notes");

			if (shippingCarrier == null || shippingCarrier.trim().isEmpty()) {
				return ResponseEntity.status(400).body(Map.of("success", false, "message", "Vui lòng chọn nhà vận chuyển"));
			}

			// Update order
			order.setStatus(OrderStatus.SHIPPED);
			order.setShippingCarrier(shippingCarrier);
			if (trackingNumber != null && !trackingNumber.trim().isEmpty()) {
				order.setTrackingNumber(trackingNumber);
			}
			if (notes != null && !notes.trim().isEmpty()) {
				order.setNotes(notes);
			}

			orderRepository.save(order);

			return ResponseEntity.ok(Map.of("success", true, "message", "Đơn hàng COD đã được phê duyệt thành công"));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
		}
	}

	/**
	 * Update Order Details (AJAX) - for editing order
	 */
	@PutMapping("/orders/{id}")
	@Transactional
	public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> request) {
		try {
			System.out.println("🔍 [DEBUG] updateOrder called - Order ID: " + id + ", Request: " + request);
			System.out.println("🔍 [DEBUG] Request headers: " + request);
			System.out.println("🔍 [DEBUG] Timestamp: " + java.time.LocalDateTime.now());
			
			Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

			System.out.println("🔍 [DEBUG] Current order - Email: " + order.getCustomerEmail() + ", Status: " + order.getStatus() + ", PaymentStatus: " + order.getPaymentStatus());

			// Update status if provided
			if (request.containsKey("status")) {
				String statusStr = (String) request.get("status");
				OrderStatus oldStatus = order.getStatus();
				OrderStatus newStatus = OrderStatus.valueOf(statusStr);
				order.setStatus(newStatus);
				
				System.out.println("🔍 [DEBUG] Order status change: " + oldStatus + " → " + newStatus);
				
				// Send email notifications for status changes
				if (oldStatus != newStatus) {
					try {
						System.out.println("📧 [EMAIL] Attempting to send shipping notification email...");
						
						// Create OrderDao directly to avoid LazyInitializationException
						OrderDao orderDao = new OrderDao();
						orderDao.setId(order.getId());
						orderDao.setOrderNumber(order.getOrderNumber());
						orderDao.setCustomerName(order.getCustomerName());
						orderDao.setCustomerEmail(order.getCustomerEmail());
						orderDao.setCustomerPhone(order.getCustomerPhone());
						orderDao.setTotalAmount(order.getTotalAmount());
						orderDao.setPaymentMethod(order.getPaymentMethod());
						orderDao.setOrderDate(order.getOrderDate());
						orderDao.setShippingCarrier(order.getShippingCarrier());
						orderDao.setTrackingNumber(order.getTrackingNumber());
						orderDao.setShippingAddressText(order.getShippingAddressText());
						
						// Send shipping notification when order is shipped
						if (newStatus == OrderStatus.SHIPPED) {
							emailService.sendShippingNotificationEmail(orderDao);
							System.out.println("✅ [EMAIL] Shipping notification email sent for order: " + order.getOrderNumber());
						}
						// Send completion email when order is delivered
						else if (newStatus == OrderStatus.DELIVERED) {
							System.out.println("📧 [EMAIL] Attempting to send completion email...");
							emailService.sendOrderCompletionEmail(orderDao);
							System.out.println("✅ [EMAIL] Order completion email sent for order: " + order.getOrderNumber());
						}
					} catch (Exception e) {
						System.err.println("❌ [EMAIL ERROR] Failed to send status notification email for order " + order.getOrderNumber() + ": " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					System.out.println("ℹ️ [DEBUG] No status change, no email sent");
				}
			}

			// Update payment status if provided
			if (request.containsKey("paymentStatus")) {
				String paymentStatusStr = (String) request.get("paymentStatus");
				PaymentStatus oldPaymentStatus = order.getPaymentStatus();
				PaymentStatus newPaymentStatus = PaymentStatus.valueOf(paymentStatusStr);
				order.setPaymentStatus(newPaymentStatus);
				
				System.out.println("🔍 [DEBUG] Payment status change: " + oldPaymentStatus + " → " + newPaymentStatus);
				
				// Send payment confirmation email if status changed to COMPLETED
				if (oldPaymentStatus != PaymentStatus.COMPLETED && newPaymentStatus == PaymentStatus.COMPLETED) {
					try {
						System.out.println("📧 [EMAIL] Attempting to send payment confirmation email...");
						
						// Create OrderDao directly to avoid LazyInitializationException
						OrderDao orderDao = new OrderDao();
						orderDao.setId(order.getId());
						orderDao.setOrderNumber(order.getOrderNumber());
						orderDao.setCustomerName(order.getCustomerName());
						orderDao.setCustomerEmail(order.getCustomerEmail());
						orderDao.setCustomerPhone(order.getCustomerPhone());
						orderDao.setTotalAmount(order.getTotalAmount());
						orderDao.setPaymentMethod(order.getPaymentMethod());
						orderDao.setOrderDate(order.getOrderDate());
						orderDao.setShippingCarrier(order.getShippingCarrier());
						orderDao.setTrackingNumber(order.getTrackingNumber());
						orderDao.setShippingAddressText(order.getShippingAddressText());
						
						emailService.sendPaymentConfirmationEmail(orderDao);
						System.out.println("✅ [EMAIL] Payment confirmation email sent for order: " + order.getOrderNumber());
					} catch (Exception e) {
						System.err.println("❌ [EMAIL ERROR] Failed to send payment confirmation email for order " + order.getOrderNumber() + ": " + e.getMessage());
						e.printStackTrace();
					}
				}
				// Send payment failure email if status changed to FAILED
				else if (oldPaymentStatus != PaymentStatus.FAILED && newPaymentStatus == PaymentStatus.FAILED) {
					try {
						System.out.println("📧 [EMAIL] Attempting to send payment failure email...");
						
						// Create OrderDao directly to avoid LazyInitializationException
						OrderDao orderDao = new OrderDao();
						orderDao.setId(order.getId());
						orderDao.setOrderNumber(order.getOrderNumber());
						orderDao.setCustomerName(order.getCustomerName());
						orderDao.setCustomerEmail(order.getCustomerEmail());
						orderDao.setCustomerPhone(order.getCustomerPhone());
						orderDao.setTotalAmount(order.getTotalAmount());
						orderDao.setPaymentMethod(order.getPaymentMethod());
						orderDao.setOrderDate(order.getOrderDate());
						orderDao.setShippingCarrier(order.getShippingCarrier());
						orderDao.setTrackingNumber(order.getTrackingNumber());
						orderDao.setShippingAddressText(order.getShippingAddressText());
						
						String retryPaymentLink = frontendUrl + "/checkout/retry/" + order.getOrderNumber();
						emailService.sendPaymentFailureEmail(orderDao, retryPaymentLink);
						System.out.println("✅ [EMAIL] Payment failure email sent for order: " + order.getOrderNumber());
					} catch (Exception e) {
						System.err.println("❌ [EMAIL ERROR] Failed to send payment failure email for order " + order.getOrderNumber() + ": " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					System.out.println("ℹ️ [DEBUG] No payment status change requiring email, or no change occurred");
				}
			}

			// Update shipping carrier if provided
			if (request.containsKey("shippingCarrier")) {
				String carrier = (String) request.get("shippingCarrier");
				order.setShippingCarrier(carrier);
				System.out.println("🔍 [DEBUG] Updated shipping carrier: " + carrier);
			}

			// Update tracking number if provided
			if (request.containsKey("trackingNumber")) {
				String trackingNumber = (String) request.get("trackingNumber");
				order.setTrackingNumber(trackingNumber);
				System.out.println("🔍 [DEBUG] Updated tracking number: " + trackingNumber);
			}

			// Update notes if provided
			if (request.containsKey("notes")) {
				String notes = (String) request.get("notes");
				order.setNotes(notes);
				System.out.println("🔍 [DEBUG] Updated notes: " + notes);
			}

			orderRepository.save(order);
			System.out.println("✅ [DEBUG] Order saved successfully");

			return ResponseEntity.ok(Map.of("message", "Order updated successfully", "timestamp", java.time.LocalDateTime.now().toString()));
		} catch (Exception e) {
			System.err.println("❌ [ERROR] Failed to update order: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage(), "timestamp", java.time.LocalDateTime.now().toString()));
		}
	}

	/**
	 * Get Users with pagination (AJAX)
	 */
	@GetMapping("/users")
	public ResponseEntity<Page<UserDao>> getUsers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String search,
			@RequestParam(required = false) String status, @RequestParam(required = false) String timeRange) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<User> users;

		// Apply filters
		if (search != null && !search.trim().isEmpty()) {
			users = userRepository
					.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
							search.trim(), search.trim(), search.trim(), pageable);
		} else {
			users = userRepository.findAll(pageable);
		}

		Page<UserDao> userDaos = users.map(user -> {
			UserDao dao = new UserDao();
			dao.setId(user.getId());
			dao.setUsername(user.getUsername());
			dao.setEmail(user.getEmail());
			dao.setFullName(user.getFullName());
			dao.setPhoneNumber(user.getPhoneNumber());
			dao.setCreatedAt(user.getCreatedAt());

			// Calculate total orders and spent
			long totalOrders = orderRepository.countByUserId(user.getId());
			dao.setTotalOrders(totalOrders);

			// Calculate total spent
			BigDecimal totalSpent = orderRepository.sumTotalAmountByUserId(user.getId());
			dao.setTotalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO);

			return dao;
		});

		return ResponseEntity.ok(userDaos);
	}

	/**
	 * Create User (AJAX)
	 */
	@PostMapping("/users")
	public ResponseEntity<?> createUser(@RequestBody UserDao userDao) {
		try {
			// Check if username or email already exists
			if (userRepository.existsByUsername(userDao.getUsername())) {
				return ResponseEntity.status(400).body(Map.of("error", "Tên đăng nhập đã tồn tại"));
			}
			if (userRepository.existsByEmail(userDao.getEmail())) {
				return ResponseEntity.status(400).body(Map.of("error", "Email đã tồn tại"));
			}

			User user = new User();
			user.setUsername(userDao.getUsername());
			user.setEmail(userDao.getEmail());
			user.setFullName(userDao.getFullName());
			user.setPhoneNumber(userDao.getPhoneNumber());
			user.setIsActive(true);

			// Set role from DAO or default to USER
			if (userDao.getRole() != null) {
				user.setRole(userDao.getRole());
			} else {
				user.setRole(Role.USER);
			}

			// Hash password using PasswordEncoder (same as registration)
			if (userDao.getPassword() != null && !userDao.getPassword().isEmpty()) {
				user.setPassword(passwordEncoder.encode(userDao.getPassword()));
			}

			user = userRepository.save(user);

			UserDao dao = new UserDao();
			dao.setId(user.getId());
			dao.setUsername(user.getUsername());
			dao.setEmail(user.getEmail());
			dao.setFullName(user.getFullName());
			dao.setPhoneNumber(user.getPhoneNumber());
			dao.setRole(user.getRole());
			dao.setCreatedAt(user.getCreatedAt());

			return ResponseEntity.ok(dao);
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
			dao.setCreatedAt(user.getCreatedAt());

			// Get orders
			long totalOrders = orderRepository.countByUserId(user.getId());
			dao.setTotalOrders(totalOrders);

			BigDecimal totalSpent = orderRepository.sumTotalAmountByUserId(user.getId());
			dao.setTotalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO);

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

			// Update fields
			if (userDao.getFullName() != null) {
				user.setFullName(userDao.getFullName());
			}
			if (userDao.getEmail() != null) {
				user.setEmail(userDao.getEmail());
			}
			if (userDao.getPhoneNumber() != null) {
				user.setPhoneNumber(userDao.getPhoneNumber());
			}

			user = userRepository.save(user);

			UserDao dao = new UserDao();
			dao.setId(user.getId());
			dao.setUsername(user.getUsername());
			dao.setEmail(user.getEmail());
			dao.setFullName(user.getFullName());
			dao.setPhoneNumber(user.getPhoneNumber());
			dao.setCreatedAt(user.getCreatedAt());

			return ResponseEntity.ok(dao);
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Delete User (AJAX) - Soft delete by deactivating Only ADMIN can delete users
	 */
	@DeleteMapping("/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		try {
			User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

			// Soft delete - just mark as inactive or delete
			userRepository.delete(user);

			return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Export Orders CSV
	 */
	@GetMapping("/orders/export")
	public ResponseEntity<String> exportOrders() {
		// TODO: Implement CSV export
		return ResponseEntity.ok("CSV export not implemented yet");
	}

	/**
	 * Export Customers CSV
	 */
	@GetMapping("/customers/export")
	public ResponseEntity<String> exportCustomers() {
		// TODO: Implement CSV export
		return ResponseEntity.ok("CSV export not implemented yet");
	}

	/**
	 * Export Analytics Report
	 */
	@GetMapping("/analytics/export")
	public ResponseEntity<String> exportAnalytics(@RequestParam String period) {
		// TODO: Implement report export
		return ResponseEntity.ok("Analytics export not implemented yet");
	}

	/**
	 * Delete Product (AJAX) - Only ADMIN can delete
	 */
	@DeleteMapping("/products/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
		try {
			productService.deleteProduct(id);
			return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}
}
