package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Hub;
import vn.uteexpress.repository.HubRepository;

@Service
public class HubService {

	private final HubRepository hubRepository;

	public HubService(HubRepository hubRepository) {
		this.hubRepository = hubRepository;
	}

	// =========================
	// GET ACTIVE HUBS
	// =========================

	public List<Hub> getHubs(String province) {

		if (province == null || province.isBlank()) {
			return hubRepository.findByActiveTrue();
		}

		return hubRepository.findByProvinceIgnoreCaseAndActiveTrue(province.trim());
	}

	// =========================
	// GET ONE HUB
	// =========================

	public Hub getHub(Long id) {

		return hubRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy bưu cục với ID: " + id));
	}

	// =========================
	// CREATE HUB
	// =========================

	@Transactional
	public Hub createHub(Hub request) {

		normalizeAndValidate(request);

		if (hubRepository.existsByCodeIgnoreCase(request.getCode())) {
			throw new RuntimeException("Mã bưu cục đã tồn tại: " + request.getCode());
		}

		// Build a fresh entity so a client-supplied id is never used
		Hub hub = new Hub();
		copyFields(hub, request);
		hub.setActive(true);

		return hubRepository.save(hub);
	}

	// =========================
	// UPDATE HUB (full replace)
	// =========================

	@Transactional
	public Hub updateHub(Long id, Hub request) {

		Hub hub = getHub(id);

		normalizeAndValidate(request);

		if (hubRepository.existsByCodeIgnoreCaseAndIdNot(request.getCode(), id)) {
			throw new RuntimeException("Mã bưu cục đã tồn tại: " + request.getCode());
		}

		copyFields(hub, request);
		hub.setActive(request.isActive());

		return hubRepository.save(hub);
	}

	// =========================
	// DEACTIVATE HUB (soft delete)
	// =========================

	@Transactional
	public void deactivateHub(Long id) {

		Hub hub = getHub(id);
		hub.setActive(false);

		hubRepository.save(hub);
	}

	// =========================
	// HELPERS
	// =========================

	private void copyFields(Hub target, Hub source) {

		target.setCode(source.getCode());
		target.setName(source.getName());
		target.setAddress(source.getAddress());
		target.setDistrict(source.getDistrict());
		target.setProvince(source.getProvince());
		target.setPhone(source.getPhone());
	}

	private void normalizeAndValidate(Hub hub) {

		hub.setCode(trim(hub.getCode()) == null ? null : trim(hub.getCode()).toUpperCase());
		hub.setName(trim(hub.getName()));
		hub.setAddress(trim(hub.getAddress()));
		hub.setDistrict(trim(hub.getDistrict()));
		hub.setProvince(trim(hub.getProvince()));

		String phone = trim(hub.getPhone());
		hub.setPhone(phone == null ? null : phone.replaceAll("\\s+", ""));

		if (hub.getCode() == null) {
			throw new RuntimeException("Mã bưu cục không được để trống");
		}

		if (hub.getName() == null) {
			throw new RuntimeException("Tên bưu cục không được để trống");
		}

		if (hub.getAddress() == null) {
			throw new RuntimeException("Địa chỉ bưu cục không được để trống");
		}

		if (hub.getProvince() == null) {
			throw new RuntimeException("Tỉnh/thành phố không được để trống");
		}

		if (hub.getPhone() != null && !hub.getPhone().matches("^(\\+84|0)\\d{9,10}$")) {
			throw new RuntimeException("Số điện thoại không hợp lệ");
		}
	}

	private String trim(String value) {

		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}
}