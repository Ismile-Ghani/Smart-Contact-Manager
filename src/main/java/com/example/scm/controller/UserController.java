package com.example.scm.controller;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.scm.dao.UserRepository;
import com.example.scm.dao.contactRepository;
import com.example.scm.dao.orderRepository;
import com.example.scm.entities.Contact;
import com.example.scm.entities.MyOrders;
import com.example.scm.entities.Payment;
import com.example.scm.entities.User;
import com.example.scm.helper.Message;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private contactRepository contactRepo;
	
	@Autowired
	private orderRepository orderRepo;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;

	@ModelAttribute
	public void getUser(Model model, Principal principal) {
		String name = principal.getName();
		User userByUserName = this.userRepo.getUserByUserName(name);
		model.addAttribute("user", userByUserName);
	}

	@GetMapping("/dashboard")
	public String dashboard() {

		return "/users/dashboard";
	}

	@GetMapping("/add-contact")
	public String addContact(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "/users/add-contact";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepo.getUserByUserName(name);
			contact.setUser(user);
			Contact contact2 = null;
			user.getContacts().add(contact);
			this.userRepo.save(user);
			contact2 = this.contactRepo.findTopByOrderByIdDesc().get(0);
			// System.out.println(contact2.getcId());
			
			// System.out.println(file.getOriginalFilename());

			if (!file.isEmpty()) {
				try {
					contact.setImage("_" + contact2.getcId() + file.getOriginalFilename());
					File saveFile = new ClassPathResource("static/img/profile").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "_" + contact2.getcId()
							+ file.getOriginalFilename());
					// System.out.println(path);

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {

					e.printStackTrace();
				}

			}
			else
				contact.setImage("contact.png");
			// user.getContacts().add(contact);
			// System.out.println(contact2.getcId());
			if(!file.isEmpty())
				this.contactRepo.setUserInfoById("_" + contact2.getcId() + file.getOriginalFilename(), contact2.getcId());
			else
				this.contactRepo.setUserInfoById("contact.png", contact2.getcId());
				
			session.setAttribute("message", new Message("Contact Saved Successfully", "success"));
		} catch (Exception ex) {
			ex.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong", "danger"));
		}

		return "/users/add-contact";
	}

	@GetMapping("/show-contact/{page}")
	public String showContact(@PathVariable("page") Integer page, Model model, Principal principal) {

		String name = principal.getName();
		User user = this.userRepo.getUserByUserName(name);
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepo.findContactByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		model.addAttribute("title", "Show Contact");
		return "/users/show-contact";
	}

	@RequestMapping("/{cId}/contact")
	public String viewContact(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		Optional<Contact> findById = this.contactRepo.findById(cId);

		Contact contact = findById.get();
		String name = principal.getName();
		User user = this.userRepo.getUserByUserName(name);
		if (user.getId() == contact.getUser().getId())
			model.addAttribute("contact", contact);
		return "/users/view-contact";
	}

	@RequestMapping("/doDelete/{cId}")
	public String doDelete(@PathVariable("cId") Integer cId, Principal principal, HttpSession session) {

		Contact contact = this.contactRepo.findById(cId).get();
		String name = principal.getName();
		User user = this.userRepo.getUserByUserName(name);
		
		if ((contact.getImage() != null) && (!contact.getImage().equals("contact.png"))) {
			System.out.println(contact.getImage());
			File saveFile = null;
			try {
				saveFile = new ClassPathResource("static/img/profile").getFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + contact.getImage());
			try {
				Files.delete(path);
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		if (user.getId() == contact.getUser().getId()) {
			contact.setUser(null);
			this.contactRepo.delete(contact);
			session.setAttribute("message", new Message("Contacts Deleted Successfully", "success"));
		}
		return "redirect:/user/show-contact/0";
	}

	@PostMapping("/doUpdate/{cId}")
	public String editContact(@PathVariable("cId") Integer cId, Model model) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepo.findById(cId).get();
		model.addAttribute("contact", contact);
		return "/users/edit-contact";
	}

	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, HttpSession session, Principal principal) {
		try {
			String name = principal.getName();
			User user = this.userRepo.getUserByUserName(name);
			contact.setUser(user);
			Contact contact1 = this.contactRepo.findById(contact.getcId()).get();
			if ((contact1.getImage() != null) && (!contact1.getImage().equals("contact.png"))) {
				System.out.println(contact.getImage());
				File saveFile = null;
				try {
					saveFile = new ClassPathResource("static/img/profile").getFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + contact1.getImage());
				try {
					Files.delete(path);
				} catch (IOException e) {

					e.printStackTrace();
				}
			}

			if (!file.isEmpty()) {

				// System.out.println(contact.getcId());
				contact.setImage("_"+contact.getcId() +file.getOriginalFilename());
				
				try {
					File saveFile = new ClassPathResource("static/img/profile").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "_"+contact.getcId() +file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {

					e.printStackTrace();
				}

			} else {
				contact.setImage("contact.png");
			}

			user.getContacts().add(contact);
			this.userRepo.save(user);
			session.setAttribute("message", new Message("Contact Updated Successfully", "success"));
		} catch (Exception ex) {
			ex.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong", "danger"));

		}
		return "redirect:/user/" + contact.getcId() + "/contact";
	}
	
	@GetMapping("/add-payment")
	public String addPayment(Model model) {

		model.addAttribute("title", "Add Payment");
		model.addAttribute("payment", new Payment());
		return "/users/add-payment";
	}
	
	
	@PostMapping("/createOrder")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data,Principal p) throws RazorpayException
	{
		System.out.println(data);
		
		int amount = Integer.parseInt(data.get("amount").toString());
		var client = new RazorpayClient("rzp_test_0k3pUedLO218Fb", "JMpZsA3Amr9pIRA9Zz2pmqNS");
		JSONObject ob = new JSONObject();
		ob.put("amount", amount*100);
		ob.put("currency", "INR");
		ob.put("receipt","txn_12345");
		Order ord = client.Orders.create(ob);
		
		MyOrders myorder = new MyOrders();
		myorder.setOrderId(ord.get("id"));
		myorder.setAmount(ord.get("amount").toString());
		myorder.setPayment_Id(null);
		myorder.setReceipt(ord.get("receipt"));
		myorder.setStatus("created");
		myorder.setUser(this.userRepo.getUserByUserName(p.getName()));
		
		this.orderRepo.save(myorder);
				
		System.out.println(ord);
		return ord.toString();
	}
	@PostMapping("/updateOrder")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data)
	{
		System.out.println(data);
		MyOrders myord = this.orderRepo.findByorderId(data.get("order_Id").toString());
		myord.setPayment_Id(data.get("payment_Id").toString());
		myord.setStatus(data.get("status").toString());
		this.orderRepo.save(myord);
		return ResponseEntity.ok(Map.of("msg","Updated"));
	}
	
	@GetMapping("/search-contact/{query}")
	public ResponseEntity<?> searchContact(@PathVariable("query") String query,Principal principal)
	{
		System.out.println(query);
		User user = this.userRepo.getUserByUserName(principal.getName());
		List<Contact> listContact = this.contactRepo.findByNameContainingAndUser(query, user);
		System.out.println(listContact);
		return ResponseEntity.ok(listContact);
		
	}
	
	@GetMapping("/settings")
	public String openSetting() {
		System.out.println("In Setting");
		return "/users/setting";
	}
	
	@PostMapping("/process-password")
	public String processPassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,@RequestParam("confirmPassword") String confirmPassword
			,Principal principal,HttpSession session)
	{
		
		User user = this.userRepo.getUserByUserName(principal.getName());
		if(this.bcrypt.matches(oldPassword, user.getPassword()))
		{
			if(newPassword.equals(confirmPassword)) {
				user.setPassword(this.bcrypt.encode(newPassword));
				this.userRepo.save(user);
				
				session.setAttribute("message", new Message("You password has been changed","success"));
				return "/users/setting";
			}
			else {
				session.setAttribute("message", new Message("You password did not match","danger"));
				return "/users/setting";
				
			}
			
		}
		else
		{
			session.setAttribute("message", new Message("You entered wrong old password","danger"));

			System.out.print(session.getAttribute("message"));

			System.out.println(session.getAttribute("In dev branch"));

			return "/users/setting";
		}
	}

}
