package com.smart.conact.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.conact.dao.ContactRepository;
import com.smart.conact.dao.OrderRepository;
import com.smart.conact.dao.UserRepository;
import com.smart.conact.entities.Contact;
import com.smart.conact.entities.MyOrder;
import com.smart.conact.entities.User;
import com.smart.conact.helper.Message;

import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder encoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private OrderRepository orderRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String name = principal.getName();
		// System.out.println("USERNAME: " + name);
		// get user using username(email)
		User user = userRepository.getUserByUserName(name);
		// System.out.println("USER: " + user);

		model.addAttribute("user", user);
	}

	// User Dashboard
	@GetMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "User Dashboard");

		return "normal/user_dashboard";
	}

	// add contact page
	@GetMapping("/add-contact")
	public String addContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processAddContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			contact.setUser(user);
//	processing and uploading file
			if (file.isEmpty()) {
				System.out.println("File is empty...");
				contact.setImage("bg.jpeg");
			} else {
				contact.setImage(file.getOriginalFilename());
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image Uploaded...");
			}

			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("Added to DB");
			// success msg
			session.setAttribute("msg", new Message("Contact is added", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			// error msg
			session.setAttribute("msg", new Message("Something went wrong", "danger"));
		}
		return "normal/add_contact_form";
	}

	// show contact handler
	// per page 4 contacts
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") int page, Model model, Principal principal) {
		model.addAttribute("title", "Show Contacts");

		String name = principal.getName();
		User u = userRepository.getUserByUserName(name);

		Pageable pageRequest = PageRequest.of(page, 4);

		Page<Contact> contacts = contactRepository.findContactByUser(u.getId(), pageRequest);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

//show contact
	@GetMapping("/{id}/contact")
	public String showContact(@PathVariable("id") int id, Model model, Principal principal) {
		model.addAttribute("title", "Contact Page");
		Optional<Contact> contact = contactRepository.findById(id);
		String email = principal.getName();
		User user = this.userRepository.getUserByUserName(email);
		if (user.getId() == contact.get().getUser().getId()) {
			model.addAttribute("contact", contact.get());
		}
		return "normal/contact";
	}

//delete contact
	@GetMapping("/delete/{id}")
	public String deleteContact(@PathVariable("id") int id, Model model, Principal principal, HttpSession session) {
		Optional<Contact> optional = contactRepository.findById(id);
		Contact contact = optional.get();

		String email = principal.getName();
		User user = this.userRepository.getUserByUserName(email);
		if (user.getId() == contact.getUser().getId()) {
			this.contactRepository.delete(contact);
			session.setAttribute("msg", new Message("Contact Deleted Successfully", "success"));
		}

		return "redirect:/user/show-contacts/0";
	}

//show update conatact
	@PostMapping("/update-contact/{id}")
	public String updateContact(@PathVariable("id") int id, Model model) {
		model.addAttribute("title", "Update Contact");
		model.addAttribute("contact", contactRepository.findById(id).get());
		return "normal/updateForm";
	}

//updating contact
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateContactHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, Model model, HttpSession session, Principal principal) {
		try {
			Contact oldContact = contactRepository.findById(contact.getId()).get();
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			contact.setUser(user);
			if (file.isEmpty()) {
				contact.setImage(oldContact.getImage());
			} else {
				// delete Old File
				if (!oldContact.getImage().equals("bg.jpeg")) {
					File deletefile = new ClassPathResource("static/img").getFile();
					File file1 = new File(deletefile, oldContact.getImage());
					file1.delete();
				}

				contact.setImage(file.getOriginalFilename());
				File savefile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image Uploaded...");
			}
			this.contactRepository.save(contact);
			session.setAttribute("msg", new Message("Contact is updated", "success"));
		} catch (Exception ex) {
			ex.printStackTrace();
			session.setAttribute("msg", new Message("Something went wrong", "danger"));
		}
		System.out.println(contact.getId());
		return "redirect:/user/" + contact.getId() + "/contact";
	}

	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}

	// open setting handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal,HttpSession session) {
		System.out.println("Old Password: " + oldPassword);
		System.out.println("New Password: " + newPassword);
		String name = principal.getName();
		User user = userRepository.getUserByUserName(name);
		
		if(encoder.matches(oldPassword,user.getPassword())) {
			user.setPassword(this.encoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("msg",new Message("Your Password is changed successfully", "success"));
		}else {
			session.setAttribute("msg",new Message("Your Old Password is worng", "warning"));
			return "redirect:/user/settings";
		}
		
		
		System.out.println(user.getPassword());
		return "redirect:/user/index";
	}
	
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws RazorpayException {
		System.out.println("Order function executed..."+data);
		int amt = Integer.parseInt(data.get("amount").toString());
		
		RazorpayClient razorpayClient = new RazorpayClient("rzp_test_yjrHMZgOBhaUVO", "8xq77wQnFYvn5cxR5BNyK73G");
		JSONObject options  = new JSONObject();
		options.put("amount", amt*100);
		options.put("currency", "INR");
		options.put("receipt", "txn_123456");
			
		//creating new order
		Order order = razorpayClient.Orders.create(options);
		System.out.println("Order: "+order);
		//if you want you can save this order to db
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		orderRepository.save(myOrder);	
		
		return order.toString();
	}
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		System.out.println(data);
		MyOrder myOrder = orderRepository.findByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
	//	myOrder.setOrderId(data.get("order_Id").toString());
		myOrder.setStatus(data.get("status").toString());
		this.orderRepository.save(myOrder);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}

}
