package vn.uteexpress.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String home() {
		return "about-us";
	}

	@GetMapping("/about-us")
	public String aboutUs() {
		return "about-us";
	}
}