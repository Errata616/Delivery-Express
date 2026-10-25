package vn.uteexpress.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import vn.uteexpress.entity.User;
import vn.uteexpress.repository.UserRepository;

class UserProfileServiceTest {

	@Test
	void rejectsFilesThatAreNotImages() {
		UserRepository users = mock(UserRepository.class);
		UserProfileService service = new UserProfileService(users);
		User user = new User();
		MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png",
				"not an image".getBytes());

		assertThrows(IllegalArgumentException.class, () -> service.updateAvatar(user, file));

		verify(users, never()).save(user);
	}

	@Test
	void storesReencodedPngAvatarAndReplacesPreviousAvatar() throws Exception {
		UserRepository users = mock(UserRepository.class);
		UserProfileService service = new UserProfileService(users);
		User user = new User();
		user.setUsername("customer");
		Path avatarDirectory = Paths.get("uploads", "avatars").toAbsolutePath().normalize();
		Files.createDirectories(avatarDirectory);
		Path previous = avatarDirectory.resolve("previous-test-avatar.png");
		Files.write(previous, new byte[] { 1 });
		user.setAvatarUrl("/uploads/avatars/previous-test-avatar.png");

		ByteArrayOutputStream png = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB), "png", png);
		MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", png.toByteArray());

		var response = service.updateAvatar(user, file);
		Path saved = avatarDirectory.resolve(response.avatarUrl().substring("/uploads/avatars/".length()));
		try {
			assertEquals(response.avatarUrl(), user.getAvatarUrl());
			assertTrue(Files.exists(saved));
			assertFalse(Files.exists(previous));
			verify(users).save(user);
		} finally {
			Files.deleteIfExists(saved);
		}
	}

	@Test
	void removesAvatarFromProfileAndDisk() throws Exception {
		UserRepository users = mock(UserRepository.class);
		UserProfileService service = new UserProfileService(users);
		User user = new User();
		user.setUsername("customer");
		String fileName = UUID.randomUUID() + ".png";
		Path avatar = Paths.get("uploads", "avatars").toAbsolutePath().normalize().resolve(fileName);
		Files.createDirectories(avatar.getParent());
		Files.write(avatar, new byte[] { 1, 2, 3 });
		user.setAvatarUrl("/uploads/avatars/" + fileName);

		try {
			var response = service.removeAvatar(user);

			assertNull(response.avatarUrl());
			assertNull(user.getAvatarUrl());
			assertFalse(Files.exists(avatar));
			verify(users).save(user);
		} finally {
			Files.deleteIfExists(avatar);
		}
	}

}
