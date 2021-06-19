package com.smart.conact.controllers;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.conact.dao.UserRepository;
import com.smart.conact.entities.User;
import com.smart.conact.helper.Message;
import com.smart.conact.service.EmailService;

@Controller
public class ForgotController {
	private Random random = new Random(1000);
	@Autowired
	private EmailService service;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	
	@GetMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String sendOPT(@RequestParam("email")String email,HttpSession session) {
		System.out.println(email);
		//generating OTP of 4 digit
		
		int otp = random.nextInt(9999);
		System.out.println(otp);
		
		//write code for send email for otp
		boolean flag = service.sendEmail(email, otp);
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			session.setAttribute("msg",new Message("OTP sent to you email", "success"));
			return "verify_form";
		}else {
			session.setAttribute("msg",new Message("Check your email Id.", "danger	"));
			return "forgot_email_form";
		}
	}
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp")Integer otp,HttpSession session) {
		Integer myotp = (Integer)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
	//	System.out.println(myotp+" - "+otp);
		if(myotp.equals(otp)) {
	//		System.out.println("-------------------Correct OPT");
			User user = userRepository.getUserByUserName(email);
			if(user==null) {
				session.setAttribute("msg", new Message("User does not exist with this email!", "danger"));
				return "forgot_email_form";
			}else {
				return "change_password_form";
			}
		}else {
			session.setAttribute("msg", new Message("You have entered wrong OTP!", "danger"));
			return "verify_form";
		}
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword")String newPassword, HttpSession session) {
		String email = session.getAttribute("email").toString();
		User user = userRepository.getUserByUserName(email);
		user.setPassword(encoder.encode(newPassword));
		userRepository.save(user);
		session.setAttribute("msg", new Message("Your Password is changed!!", "success"));
		return "redirect:/signin";
	}
}
