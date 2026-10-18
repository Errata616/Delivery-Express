package vn.uteexpress.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.uteexpress.entity.Address;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.AddressRepository;
import vn.uteexpress.repository.UserRepository;

@Service
public class AddressService {

	private final AddressRepository addressRepository;
	private final UserRepository userRepository;

	public AddressService(AddressRepository addressRepository, UserRepository userRepository) {

		this.addressRepository = addressRepository;
		this.userRepository = userRepository;
	}

	// =========================
	// GET ALL USER ADDRESSES
	// =========================

	public List<Address> getUserAddresses(Long userId) {

		userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		return addressRepository.findByUserId(userId);
	}

	// =========================
	// GET ONE ADDRESS
	// =========================

	public Address getAddress(Long userId, Long addressId) {

		return addressRepository.findByIdAndUserId(addressId, userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
	}

	// =========================
	// CREATE ADDRESS
	// =========================

	@Transactional
	public Address createAddress(Long userId, String receiverName, String phone, String address, String ward,
			String district, String city, boolean defaultAddress) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

		validate(receiverName, phone, address, city);

		Address newAddress = new Address();

		newAddress.setReceiverName(receiverName.trim());

		newAddress.setPhone(phone.trim());

		newAddress.setAddress(address.trim());

		newAddress.setWard(ward == null ? null : ward.trim());

		newAddress.setDistrict(district == null ? null : district.trim());

		newAddress.setCity(city == null ? null : city.trim());

		newAddress.setUser(user);

		// Nếu là địa chỉ đầu tiên
		// thì tự động đặt mặc định
		List<Address> addresses = addressRepository.findByUserId(userId);

		if (addresses.isEmpty()) {
			newAddress.setDefaultAddress(true);

		} else if (defaultAddress) {

			clearDefaultAddress(userId);

			newAddress.setDefaultAddress(true);

		} else {

			newAddress.setDefaultAddress(false);
		}

		return addressRepository.save(newAddress);
	}

	// =========================
	// UPDATE ADDRESS
	// =========================

	@Transactional
	public Address updateAddress(Long userId, Long addressId, String receiverName, String phone, String address,
			String ward, String district, String city, boolean defaultAddress) {

		Address existing = getAddress(userId, addressId);

		validate(receiverName, phone, address, city);

		existing.setReceiverName(receiverName.trim());

		existing.setPhone(phone.trim());

		existing.setAddress(address.trim());

		existing.setWard(ward == null ? null : ward.trim());

		existing.setDistrict(district == null ? null : district.trim());

		existing.setCity(city == null ? null : city.trim());

		if (defaultAddress) {

			clearDefaultAddress(userId);

			existing.setDefaultAddress(true);

		}

		return addressRepository.save(existing);
	}

	// =========================
	// SET DEFAULT
	// =========================

	@Transactional
	public Address setDefaultAddress(Long userId, Long addressId) {

		Address address = getAddress(userId, addressId);

		clearDefaultAddress(userId);

		address.setDefaultAddress(true);

		return addressRepository.save(address);
	}

	// =========================
	// DELETE ADDRESS
	// =========================

	@Transactional
	public void deleteAddress(Long userId, Long addressId) {

		Address address = getAddress(userId, addressId);

		boolean wasDefault = address.isDefaultAddress();

		addressRepository.delete(address);

		/*
		 * Nếu xóa địa chỉ mặc định, chọn một địa chỉ khác làm mặc định.
		 */
		if (wasDefault) {

			List<Address> addresses = addressRepository.findByUserId(userId);

			if (!addresses.isEmpty()) {

				Address newDefault = addresses.get(0);

				newDefault.setDefaultAddress(true);

				addressRepository.save(newDefault);
			}
		}
	}

	// =========================
	// CLEAR DEFAULT
	// =========================

	private void clearDefaultAddress(Long userId) {

		addressRepository.findByUserIdAndDefaultAddressTrue(userId).ifPresent(address -> {

			address.setDefaultAddress(false);

			addressRepository.save(address);
		});
	}

	// =========================
	// VALIDATE
	// =========================

	private void validate(String receiverName, String phone, String address, String city) {

		if (receiverName == null || receiverName.trim().isEmpty()) {

			throw new IllegalArgumentException("Tên người nhận không được để trống");
		}

		if (phone == null || phone.trim().isEmpty()) {

			throw new IllegalArgumentException("Số điện thoại không được để trống");
		}

		if (address == null || address.trim().isEmpty()) {

			throw new IllegalArgumentException("Địa chỉ không được để trống");
		}

		if (city == null || city.trim().isEmpty()) {

			throw new IllegalArgumentException("Thành phố không được để trống");
		}
	}
}