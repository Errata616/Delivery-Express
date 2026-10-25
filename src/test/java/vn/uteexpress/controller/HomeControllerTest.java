package vn.uteexpress.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HomeControllerTest {

	private final HomeController controller = new HomeController();

	@Test
	void homeUsesTheAvailableLandingPage() {
		assertEquals("about-us", controller.home());
	}

	@Test
	void aboutUsUsesTheLandingPage() {
		assertEquals("about-us", controller.aboutUs());
	}
}
