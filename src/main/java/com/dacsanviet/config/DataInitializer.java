package com.dacsanviet.config;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dacsanviet.model.Category;
import com.dacsanviet.model.Product;
import com.dacsanviet.model.Role;
import com.dacsanviet.model.Supplier;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.CategoryRepository;
import com.dacsanviet.repository.ProductRepository;
import com.dacsanviet.repository.SupplierRepository;
import com.dacsanviet.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Data Initializer - Khởi tạo dữ liệu mẫu cho demo
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private SupplierRepository supplierRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		if (userRepository.count() == 0) {
			log.info("🚀 Khởi tạo dữ liệu mẫu cho Đặc Sản Việt...");

			initializeUsers();
			initializeSuppliers();
			initializeProducts();

			log.info("✅ Khởi tạo dữ liệu hoàn tất!");
			log.info("👤 Admin: admin / admin123");
			log.info("👤 Staff: staff / staff123");
			log.info("👤 User: user / user123");
		}
	}

	private void initializeUsers() {
		// Check if admin already exists
		if (userRepository.findByUsername("admin").isPresent()) {
			log.info("✅ Admin user already exists, skipping user creation");
			return;
		}

		// Admin user
		User admin = new User();
		admin.setUsername("admin");
		admin.setPassword(passwordEncoder.encode("admin123"));
		admin.setEmail("admin@dacsanviet.com");
		admin.setFullName("Quản Trị Viên");
		admin.setPhoneNumber("0901234567");
		admin.setRole(Role.ADMIN);
		admin.setIsActive(true);
		userRepository.save(admin);

		// Staff user
		User staff = new User();
		staff.setUsername("staff");
		staff.setPassword(passwordEncoder.encode("staff123"));
		staff.setEmail("staff@dacsanviet.com");
		staff.setFullName("Nhân Viên");
		staff.setPhoneNumber("0901234568");
		staff.setRole(Role.STAFF);
		staff.setIsActive(true);
		userRepository.save(staff);

		// Regular user
		User user = new User();
		user.setUsername("user");
		user.setPassword(passwordEncoder.encode("user123"));
		user.setEmail("user@dacsanviet.com");
		user.setFullName("Khách Hàng");
		user.setPhoneNumber("0901234569");
		user.setRole(Role.USER);
		user.setIsActive(true);
		userRepository.save(user);

		log.info("✅ Tạo 3 tài khoản mẫu");
	}

	private void initializeSuppliers() {
		// Check if suppliers already exist
		if (supplierRepository.count() > 0) {
			log.info("✅ Suppliers already exist, skipping supplier creation");
			return;
		}

		List<String[]> supplierData = Arrays.asList(
				new String[] { "Công ty TNHH Khoga Detem", "Phùng Đỗ Thạnh", "0328494207",
						"phungdothanh@khogadetem.com", "120 Yên Lãng, Đống Đa, Hà Nội" },
				new String[] { "Công ty Đặc Sản Miền Bắc", "Nguyễn Văn Nam", "0901111111", "nam@dacsanmienbac.com",
						"45 Hoàng Diệu, Ba Đình, Hà Nội" },
				new String[] { "HTX Nông Sản Sạch Huế", "Trần Thị Lan", "0902222222", "lan@nongsanhue.com",
						"78 Lê Lợi, Thành phố Huế" },
				new String[] { "Công ty Đặc Sản Miền Tây", "Lê Văn Tám", "0903333333", "tam@dacsanmientay.com",
						"123 Nguyễn Văn Cừ, Cần Thơ" });

		for (String[] data : supplierData) {
			Supplier supplier = new Supplier();
			supplier.setName(data[0]);
			supplier.setContactPerson(data[1]);
			supplier.setPhone(data[2]);
			supplier.setEmail(data[3]);
			supplier.setAddress(data[4]);
			supplier.setIsActive(true);
			supplierRepository.save(supplier);
		}

		log.info("✅ Tạo 4 nhà phân phối");
	}

	private void initializeProducts() {
		// Check if products already exist
		if (productRepository.count() > 0) {
			log.info("✅ Products already exist, skipping product creation");
			return;
		}

		List<Category> categories = categoryRepository.findAll();
		List<Supplier> suppliers = supplierRepository.findAll();

		if (categories.isEmpty() || suppliers.isEmpty()) {
			log.warn("⚠️ No categories or suppliers found, skipping product creation");
			return;
		}

		// Sample products data
		List<String[]> productData = Arrays.asList(
				new String[] { "Bánh Chưng Truyền Thống", "Bánh chưng làm từ gạo nếp, đậu xanh, thịt heo thơm ngon",
						"250000", "200000", "images/products/banh-chung.jpg", "true" },
				new String[] { "Chè Thái Nguyên Đặc Biệt", "Chè Thái Nguyên cao cấp, hương vị đậm đà", "180000",
						"150000", "images/products/che-thai-nguyen.jpg", "true" },
				new String[] { "Bánh Tráng Nướng Đà Lạt", "Bánh tráng nướng giòn tan, đặc sản Đà Lạt", "45000", "35000",
						"images/products/banh-trang-nuong.jpg", "true" },
				new String[] { "Mắm Ruốc Huế", "Mắm ruốc truyền thống Huế, đậm đà hương vị", "85000", "75000",
						"images/products/mam-ruoc-hue.jpg", "true" },
				new String[] { "Bánh Pía Sóc Trăng", "Bánh pía thơm ngon, nhân đậu xanh sầu riêng", "120000", "100000",
						"images/products/banh-pia.jpg", "true" },
				new String[] { "Kẹo Dừa Bến Tre", "Kẹo dừa thơm ngon, đặc sản Bến Tre", "65000", "55000",
						"images/products/keo-dua.jpg", "true" },
				new String[] { "Nem Chua Thanh Hóa", "Nem chua truyền thống Thanh Hóa", "95000", "85000",
						"images/products/nem-chua.jpg", "true" },
				new String[] { "Bánh Căn Phan Thiết", "Bánh căn đặc sản Phan Thiết", "75000", "65000",
						"images/products/banh-can.jpg", "true" });

		for (int i = 0; i < productData.size(); i++) {
			String[] data = productData.get(i);
			Product product = new Product();
			product.setName(data[0]);
			product.setDescription(data[1]);
			product.setPrice(new BigDecimal(data[2]));
			product.setImageUrl(data[4]);
			product.setIsFeatured(Boolean.parseBoolean(data[5]));
			product.setIsActive(true);
			product.setStockQuantity(100);
			product.setCategory(categories.get(i % categories.size()));
			product.setSupplier(suppliers.get(i % suppliers.size()));
			productRepository.save(product);
		}

		log.info("✅ Tạo 8 sản phẩm mẫu");
	}
}