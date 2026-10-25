package vn.uteexpress.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.uteexpress.dto.ApiResponse;
import vn.uteexpress.dto.WalletResponse;
import vn.uteexpress.dto.WalletTopUpRequest;
import vn.uteexpress.security.CurrentUser;
import vn.uteexpress.service.WalletService;

@RestController
@RequestMapping("/api/users/me/wallet")
public class WalletController {

	private final CurrentUser currentUser;
	private final WalletService walletService;

	public WalletController(CurrentUser currentUser, WalletService walletService) {
		this.currentUser = currentUser;
		this.walletService = walletService;
	}

	@GetMapping
	public ResponseEntity<ApiResponse<WalletResponse>> getWallet() {
		return ResponseEntity.ok(ApiResponse.ok(walletService.getWallet(currentUser.getUser().getId())));
	}

	@PostMapping("/top-up")
	public ResponseEntity<ApiResponse<WalletResponse>> topUp(@Valid @RequestBody WalletTopUpRequest request) {
		WalletResponse wallet = walletService.topUp(currentUser.getUser().getId(), request.amount());
		return ResponseEntity.ok(ApiResponse.ok("Nạp tiền thành công", wallet));
	}
}
