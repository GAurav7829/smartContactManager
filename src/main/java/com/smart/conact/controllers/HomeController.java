package com.smart.conact.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.conact.dao.UserRepository;
import com.smart.conact.entities.User;
import com.smart.conact.helper.Message;

@Controller
public class HomeController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

//	@GetMapping("/test")
//	public String test() {
//		User user = new User();
//		user.setName("Gaurav");
//		user.setEmail("grvsgh7@gmail.com");
//		Contact contact = new Contact();
//		user.getContacts().add(contact);
//		userRepository.save(user);
//		return "Working";
//	}
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home Page");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About Page");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register");
		model.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping("/do_register")
	public String signupUser(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") Boolean agreement, HttpSession session,
			Model model) {
		try {
			model.addAttribute("title", "Register");

			if (!agreement) {
				System.out.println("You have not agreed Terms & conditions");
				throw new Exception("You have not agreed Terms & conditions");
			}
			if(result.hasErrors()) {
				System.out.println("Error: "+result);
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User res = this.userRepository.save(user);
			System.out.println(res);
			model.addAttribute("user", new User());
			System.out.println("Agreement: " + agreement);
			System.out.println("User: " + user);
			session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong : " + e.getMessage(), "alert-danger"));
		}
		return "signup";
	}
	
	//handler  for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login");
		return "login";
	}

}
