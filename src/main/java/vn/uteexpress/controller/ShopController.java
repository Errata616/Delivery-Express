package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Shop;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.ShopService;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

	private final ShopService shopService;
	private final UserRepository userRepository;

	public ShopController(ShopService shopService, UserRepository userRepository) {

		this.shopService = shopService;
		this.userRepository = userRepository;
	}

	// =========================
	// GET ALL
	// =========================

	@GetMapping
	public ResponseEntity<List<Shop>> getAll() {

		return ResponseEntity.ok(shopService.findActiveShops());
	}

	// =========================
	// GET BY ID
	// =========================

	@GetMapping("/{id}")
	public ResponseEntity<Shop> getById(@PathVariable Long id) {

		return ResponseEntity.ok(shopService.findById(id));
	}

	// =========================
	// GET BY VENDOR
	// =========================

	@GetMapping("/vendor/{vendorId}")
	public ResponseEntity<Shop> getByVendor(@PathVariable Long vendorId, Authentication authentication) {

		checkUserAccess(vendorId, authentication);

		return ResponseEntity.ok(shopService.findByVendorId(vendorId));
	}

	// =========================
	// SEARCH
	// =========================

	@GetMapping("/search")
	public ResponseEntity<List<Shop>> search(@RequestParam(required = false) String keyword) {

		return ResponseEntity.ok(shopService.search(keyword));
	}

	// =========================
	// CREATE SHOP
	// =========================

	@PostMapping
	public ResponseEntity<Shop> create(@RequestBody Shop shop, @RequestParam Long vendorId,
			Authentication authentication) {

		checkUserAccess(vendorId, authentication);

		return ResponseEntity.status(HttpStatus.CREATED).body(shopService.create(shop, vendorId));
	}

	// =========================
	// UPDATE SHOP
	// =========================

	@PutMapping("/{id}")
	public ResponseEntity<Shop> update(@PathVariable Long id, @RequestBody Shop shop, Authentication authentication) {

		Shop existingShop = shopService.findById(id);

		checkShopOwner(existingShop, authentication);

		return ResponseEntity.ok(shopService.update(id, shop));
	}

	// =========================
	// DELETE SHOP
	// =========================

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {

		Shop existingShop = shopService.findById(id);

		checkShopOwner(existingShop, authentication);

		shopService.delete(id);

		return ResponseEntity.noContent().build();
	}

	// =========================
	// REGISTER SHOP
	// =========================

	@PostMapping("/vendor/{vendorId}")
	public ResponseEntity<Shop> registerShop(@PathVariable Long vendorId, @RequestParam String name,
			@RequestParam(required = false) String description, @RequestParam String address,
			@RequestParam String phone, @RequestParam(required = false) String logo, Authentication authentication) {

		checkUserAccess(vendorId, authentication);

		return ResponseEntity.ok(shopService.registerShop(vendorId, name, description, address, phone, logo));
	}

	// =========================
	// UPDATE SHOP BY VENDOR
	// =========================

	@PutMapping("/{shopId}/vendor/{vendorId}")
	public ResponseEntity<Shop> updateShop(@PathVariable Long shopId, @PathVariable Long vendorId,
			@RequestParam String name, @RequestParam(required = false) String description, @RequestParam String address,
			@RequestParam String phone, @RequestParam(required = false) String logo, Authentication authentication) {

		checkUserAccess(vendorId, authentication);

		return ResponseEntity.ok(shopService.updateShop(vendorId, shopId, name, description, address, phone, logo));
	}

	// =========================
	// CHECK USER ACCESS
	// =========================

	private User getCurrentUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()) {

			throw new IllegalArgumentException("Chưa đăng nhập");
		}

		return userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
	}

	private void checkUserAccess(Long vendorId, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		boolean isAdmin = currentUser.getRole() != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		boolean isManager = currentUser.getRole() != null
				&& "MANAGER".equalsIgnoreCase(currentUser.getRole().getName());

		if (!isAdmin && !isManager && !currentUser.getId().equals(vendorId)) {

			throw new IllegalArgumentException("Không được truy cập shop của Vendor khác");
		}
	}

	// =========================
	// CHECK SHOP OWNER
	// =========================

	private void checkShopOwner(Shop shop, Authentication authentication) {

		User currentUser = getCurrentUser(authentication);

		boolean isAdmin = currentUser.getRole() != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().getName());

		boolean isManager = currentUser.getRole() != null
				&& "MANAGER".equalsIgnoreCase(currentUser.getRole().getName());

		if (isAdmin || isManager) {
			return;
		}

		if (shop == null || shop.getVendor() == null || !currentUser.getId().equals(shop.getVendor().getId())) {

			throw new IllegalArgumentException("Không được thao tác shop của Vendor khác");
		}
	}
}