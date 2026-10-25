package vn.uteexpress.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import vn.uteexpress.dto.UserProfileResponse;
import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;

@Service
public class UserProfileService {

	private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);
	private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
	private static final int MAX_IMAGE_DIMENSION = 6000;
	private static final long MAX_IMAGE_PIXELS = 20_000_000L;
	private static final String AVATAR_URL_PREFIX = "/uploads/avatars/";
	private final UserRepository userRepository;
	private final Path avatarDirectory = Paths.get("uploads", "avatars").toAbsolutePath().normalize();

	public UserProfileService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public UserProfileResponse updateAvatar(User user, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Chưa chọn ảnh đại diện");
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 5 MB");
		}

		byte[] uploadedBytes;
		try {
			uploadedBytes = file.getBytes();
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể đọc tệp ảnh", exception);
		}

		EncodedImage image = decodeAndEncode(uploadedBytes);
		String fileName = UUID.randomUUID() + "." + image.extension();
		Path destination = avatarDirectory.resolve(fileName).normalize();
		if (!destination.startsWith(avatarDirectory)) {
			throw new IllegalStateException("Đường dẫn ảnh đại diện không hợp lệ");
		}

		String previousAvatarUrl = user.getAvatarUrl();
		try {
			Files.createDirectories(avatarDirectory);
			Files.write(destination, image.bytes());
		} catch (IOException exception) {
			deleteNewAvatar(destination);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Không thể lưu ảnh đại diện", exception);
		}

		user.setAvatarUrl(AVATAR_URL_PREFIX + fileName);
		try {
			userRepository.save(user);
		} catch (RuntimeException exception) {
			user.setAvatarUrl(previousAvatarUrl);
			deleteNewAvatar(destination);
			throw exception;
		}

		deletePreviousAvatar(previousAvatarUrl);
		return UserProfileResponse.from(user);
	}

	public UserProfileResponse removeAvatar(User user) {
		String previousAvatarUrl = user.getAvatarUrl();
		if (previousAvatarUrl == null) {
			return UserProfileResponse.from(user);
		}

		user.setAvatarUrl(null);
		try {
			userRepository.save(user);
		} catch (RuntimeException exception) {
			user.setAvatarUrl(previousAvatarUrl);
			throw exception;
		}

		deletePreviousAvatar(previousAvatarUrl);
		return UserProfileResponse.from(user);
	}

	private EncodedImage decodeAndEncode(byte[] uploadedBytes) {
		try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(uploadedBytes))) {
			if (input == null) {
				throw new IllegalArgumentException("Tệp tải lên không phải ảnh PNG hoặc JPEG hợp lệ");
			}
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) {
				throw new IllegalArgumentException("Chỉ chấp nhận ảnh PNG hoặc JPEG");
			}
			ImageReader reader = readers.next();
			try {
				reader.setInput(input, true, true);
				String format = reader.getFormatName().toLowerCase();
				if (!format.equals("png") && !format.equals("jpeg") && !format.equals("jpg")) {
					throw new IllegalArgumentException("Chỉ chấp nhận ảnh PNG hoặc JPEG");
				}
				int width = reader.getWidth(0);
				int height = reader.getHeight(0);
				if (width <= 0 || height <= 0 || width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION
						|| (long) width * height > MAX_IMAGE_PIXELS) {
					throw new IllegalArgumentException("Kích thước ảnh đại diện vượt quá giới hạn cho phép");
				}
				BufferedImage decoded = reader.read(0);
				if (decoded == null) {
					throw new IllegalArgumentException("Không thể giải mã ảnh đại diện");
				}
				boolean png = format.equals("png");
				BufferedImage normalized = decoded;
				if (!png && decoded.getType() != BufferedImage.TYPE_INT_RGB) {
					normalized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					Graphics2D graphics = normalized.createGraphics();
					try {
						graphics.drawImage(decoded, 0, 0, null);
					} finally {
						graphics.dispose();
					}
				}
				try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
					if (!ImageIO.write(normalized, png ? "png" : "jpeg", output)) {
						throw new IllegalStateException("Không có bộ mã hóa ảnh đại diện phù hợp");
					}
					return new EncodedImage(png ? "png" : "jpg", output.toByteArray());
				}
			} finally {
				reader.dispose();
			}
		} catch (IOException exception) {
			throw new IllegalArgumentException("Tệp tải lên không phải ảnh PNG hoặc JPEG hợp lệ", exception);
		}
	}

	private void deletePreviousAvatar(String avatarUrl) {
		if (avatarUrl == null || !avatarUrl.startsWith(AVATAR_URL_PREFIX)) {
			return;
		}
		Path previous = avatarDirectory.resolve(avatarUrl.substring(AVATAR_URL_PREFIX.length())).normalize();
		if (!previous.startsWith(avatarDirectory) || !avatarDirectory.equals(previous.getParent())) {
			return;
		}
		try {
			Files.deleteIfExists(previous);
		} catch (IOException exception) {
			log.warn("Could not remove replaced avatar file {}", previous, exception);
		}
	}

	private void deleteNewAvatar(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException exception) {
			log.warn("Could not remove unsaved avatar file {}", path, exception);
		}
	}

	private record EncodedImage(String extension, byte[] bytes) {
	}
}
