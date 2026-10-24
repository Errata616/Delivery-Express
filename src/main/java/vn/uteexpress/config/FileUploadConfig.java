package vn.uteexpress.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		Path commentUploadPath = Paths.get("uploads", "comments").toAbsolutePath();
		Path avatarUploadPath = Paths.get("uploads", "avatars").toAbsolutePath();

		registry.addResourceHandler("/uploads/comments/**")
				.addResourceLocations("file:" + commentUploadPath + "/");
		registry.addResourceHandler("/uploads/avatars/**")
				.addResourceLocations("file:" + avatarUploadPath + "/");
	}
}