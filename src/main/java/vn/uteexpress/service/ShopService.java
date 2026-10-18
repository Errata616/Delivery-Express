package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Shop;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.ShopRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class ShopService {

	private final ShopRepository shopRepository;
	private final UserRepository userRepository;

	public ShopService(ShopRepository shopRepository, UserRepository userRepository) {
		this.shopRepository = shopRepository;
		this.userRepository = userRepository;
	}

	// =========================
	// FIND ALL
	// =========================

	@Transactional(readOnly = true)
	public List<Shop> findAll() {
		return shopRepository.findAll();
	}

	// =========================
	// FIND ACTIVE SHOPS
	// =========================

	@Transactional(readOnly = true)
	public List<Shop> findActiveShops() {
		return shopRepository.findByActiveTrue();
	}

	// =========================
	// FIND BY ID
	// =========================

	@Transactional(readOnly = true)
	public Shop findById(Long id) {

		if (id == null) {
			throw new IllegalArgumentException("Shop ID không được null");
		}

		return shopRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy shop với ID: " + id));
	}

	// =========================
	// FIND BY VENDOR
	// =========================

	@Transactional(readOnly = true)
	public Shop findByVendorId(Long vendorId) {

		if (vendorId == null) {
			throw new IllegalArgumentException("Vendor ID không được null");
		}

		return shopRepository.findByVendorId(vendorId).orElseThrow(() -> new RuntimeException("Vendor chưa có shop"));
	}

	// =========================
	// CREATE
	// =========================

	@Transactional
	public Shop create(Shop shop, Long vendorId) {

		if (shop == null) {
			throw new IllegalArgumentException("Shop không được null");
		}

		if (vendorId == null) {
			throw new IllegalArgumentException("Vendor ID không được null");
		}

		if (shop.getName() == null || shop.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Tên shop không được để trống");
		}

		User vendor = userRepository.findById(vendorId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy vendor"));

		if (vendor.getRole() == null || !"VENDOR".equalsIgnoreCase(vendor.getRole().getName())) {

			throw new IllegalArgumentException("User này không có role VENDOR");
		}

		if (shopRepository.existsByVendorId(vendorId)) {
			throw new IllegalArgumentException("Vendor đã có shop");
		}

		shop.setName(shop.getName().trim());
		shop.setVendor(vendor);

		// Shop mới luôn active
		shop.setActive(true);

		return shopRepository.save(shop);
	}

	// =========================
	// UPDATE
	// =========================

	@Transactional
	public Shop update(Long id, Shop shop) {

		if (shop == null) {
			throw new IllegalArgumentException("Shop không được null");
		}

		Shop existing = findById(id);

		if (shop.getName() == null || shop.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Tên shop không được để trống");
		}

		existing.setName(shop.getName().trim());
		existing.setDescription(shop.getDescription());
		existing.setAddress(shop.getAddress());
		existing.setPhone(shop.getPhone());
		existing.setLogo(shop.getLogo());

		/*
		 * Admin/Manager có thể thay đổi active. Vendor không đi qua method này nếu
		 * không có quyền.
		 */
		existing.setActive(shop.isActive());

		return shopRepository.save(existing);
	}

	// =========================
	// DELETE - SOFT DELETE
	// =========================

	@Transactional
	public void delete(Long id) {

		Shop shop = findById(id);

		if (!shop.isActive()) {
			throw new IllegalArgumentException("Shop đã được ngừng hoạt động");
		}

		/*
		 * Không xóa vật lý Shop khỏi database.
		 *
		 * Existing sender records may still reference this merchant profile.
		 *
		 * Vì vậy chuyển active = false thay vì DELETE.
		 */
		shop.setActive(false);

		shopRepository.save(shop);
	}

	// =========================
	// SEARCH
	// =========================

	@Transactional(readOnly = true)
	public List<Shop> search(String keyword) {

		if (keyword == null || keyword.trim().isEmpty()) {
			return findActiveShops();
		}

		return shopRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword.trim());
	}

	// =========================
	// GET SHOP BY VENDOR
	// =========================

	@Transactional(readOnly = true)
	public Shop getShopByVendor(Long vendorId) {

		return findByVendorId(vendorId);
	}

	// =========================
	// REGISTER SHOP
	// =========================

	@Transactional
	public Shop registerShop(Long vendorId, String name, String description, String address, String phone,
			String logo) {

		if (vendorId == null) {
			throw new IllegalArgumentException("Vendor ID không được null");
		}

		User vendor = userRepository.findById(vendorId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy Vendor"));

		if (vendor.getRole() == null || !"VENDOR".equalsIgnoreCase(vendor.getRole().getName())) {

			throw new IllegalArgumentException("User này không có role VENDOR");
		}

		if (shopRepository.existsByVendorId(vendorId)) {
			throw new IllegalArgumentException("Vendor đã có shop");
		}

		validateShop(name, address, phone);

		Shop shop = new Shop();

		shop.setName(name.trim());

		shop.setDescription(description == null ? null : description.trim());

		shop.setAddress(address.trim());

		shop.setPhone(phone.trim());

		shop.setLogo(logo == null ? null : logo.trim());

		shop.setActive(true);

		shop.setVendor(vendor);

		return shopRepository.save(shop);
	}

	// =========================
	// UPDATE SHOP BY VENDOR
	// =========================

	@Transactional
	public Shop updateShop(Long vendorId, Long shopId, String name, String description, String address, String phone,
			String logo) {

		Shop shop = shopRepository.findByIdAndVendorId(shopId, vendorId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy shop hoặc shop không thuộc Vendor"));

		validateShop(name, address, phone);

		shop.setName(name.trim());

		shop.setDescription(description == null ? null : description.trim());

		shop.setAddress(address.trim());

		shop.setPhone(phone.trim());

		if (logo != null) {
			shop.setLogo(logo.trim());
		}

		return shopRepository.save(shop);
	}

	// =========================
	// VALIDATE
	// =========================

	private void validateShop(String name, String address, String phone) {

		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Tên shop không được để trống");
		}

		if (address == null || address.trim().isEmpty()) {
			throw new IllegalArgumentException("Địa chỉ shop không được để trống");
		}

		if (phone == null || phone.trim().isEmpty()) {
			throw new IllegalArgumentException("Số điện thoại shop không được để trống");
		}
	}
}