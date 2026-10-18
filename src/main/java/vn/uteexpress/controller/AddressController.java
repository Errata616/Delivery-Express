package vn.uteexpress.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import vn.uteexpress.entity.Address;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;
import vn.uteexpress.service.AddressService;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserRepository userRepository;

    public AddressController(
            AddressService addressService,
            UserRepository userRepository) {

        this.addressService = addressService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "Chưa đăng nhập");
        }

        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy tài khoản"));
    }

    private void checkUserAccess(
            Long userId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        boolean isAdmin = currentUser.getRole() != null
                && "ADMIN".equalsIgnoreCase(
                        currentUser.getRole().getName());

        if (!isAdmin
                && !currentUser.getId().equals(userId)) {

            throw new IllegalArgumentException(
                    "Không được truy cập địa chỉ của người dùng khác");
        }
    }

    // =========================
    // GET ALL
    // =========================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getUserAddresses(
            @PathVariable Long userId,
            Authentication authentication) {

        checkUserAccess(userId, authentication);

        return ResponseEntity.ok(
                addressService.getUserAddresses(userId));
    }

    // =========================
    // GET ONE
    // =========================

    @GetMapping("/{addressId}/user/{userId}")
    public ResponseEntity<Address> getAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            Authentication authentication) {

        checkUserAccess(userId, authentication);

        return ResponseEntity.ok(
                addressService.getAddress(
                        userId,
                        addressId));
    }

    // =========================
    // CREATE
    // =========================

    @PostMapping("/user/{userId}")
    public ResponseEntity<Address> createAddress(
            @PathVariable Long userId,
            @RequestParam String receiverName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String district,
            @RequestParam String city,
            @RequestParam(defaultValue = "false")
            boolean defaultAddress,
            Authentication authentication) {

        checkUserAccess(userId, authentication);

        return ResponseEntity.ok(
                addressService.createAddress(
                        userId,
                        receiverName,
                        phone,
                        address,
                        ward,
                        district,
                        city,
                        defaultAddress));
    }

    // =========================
    // UPDATE
    // =========================

    @PutMapping("/{addressId}/user/{userId}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            @RequestParam String receiverName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String district,
            @RequestParam String city,
            @RequestParam(defaultValue = "false")
            boolean defaultAddress,
            Authentication authentication) {

        checkUserAccess(userId, authentication);

        return ResponseEntity.ok(
                addressService.updateAddress(
                        userId,
                        addressId,
                        receiverName,
                        phone,
                        address,
                        ward,
                        district,
                        city,
                        defaultAddress));
    }

    // =========================
    // SET DEFAULT
    // =========================

    @PutMapping("/{addressId}/user/{userId}/default")
    public ResponseEntity<Address> setDefaultAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            Authentication authentication) {

        checkUserAccess(userId, authentication);

        return ResponseEntity.ok(
                addressService.setDefaultAddress(
                        userId,
                        addressId));
    }

    // =========================
    // DELETE
    // =========================

    @DeleteMapping("/{addressId}/user/{userId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            Authentication authentication) {

        checkUserAccess(userId, authentication);

        addressService.deleteAddress(
                userId,
                addressId);

        return ResponseEntity.noContent().build();
    }
}