package com.example.scm.controller;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.http.HttpClient.Redirect;
import java.security.Principal;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.example.scm.dao.UserRepository;
import com.example.scm.entities.User;
import com.example.scm.helper.Message;
import com.example.scm.service.EmailService;
import com.example.scm.service.OneTimePasswordService;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder bcryptPassword;
	
	

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailservice;
	
	
	@Autowired
	private OneTimePasswordService otpService;
    

	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/about")
	public String about() {
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping("/doRegister")
	public String doRegister(@Valid @ModelAttribute("user") User user,BindingResult result1,
			@RequestParam(value = "aggrement", defaultValue = "false") boolean aggrement, Model model,HttpSession session) {
		try {
			if (!aggrement) {
				System.out.println();
				throw new Exception("You have not aggreed terms and conditions.");
			}
			if(result1.hasErrors())
			{
				System.out.println(result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(bcryptPassword.encode(user.getPassword()));
			this.userRepository.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered","alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong." + e.getMessage(),"alert-danger"));
			return "signup";
		}
		
	}
	
	@GetMapping("/signin")
	public String doLogin()
	{
		return "login";
	}
	
	@GetMapping("/forgotPassword")
	public String forgotForm()
	{
		return "forgot-form";
	}
	
	
	
	@PostMapping("/doForgot")
	public String doForgot(@RequestParam("email") String useremail,HttpSession session)
	{
		User user = this.userRepository.getUserByUserName(useremail);
	
		int otp = this.otpService.generateOTP(user.getName());
		if(useremail != null)
		{
			
		}
		
		String message = ""
				+"<div style='border:1px solid #e2e2e2;padding:20px;'>"
				+"<h1>"
				+"OTP is:"
				+"<b>"+otp
				+"</n>"
				+"</h1>"
				+"</div>";
		
		boolean flag= this.emailservice.sendEmail(useremail, "This is OTP from SCM",message );
		
		if(flag) {
			session.setAttribute("message", new Message("OTP has been sent to email id","success"));
			//model.addAttribute("message", "Success");
			session.setAttribute("email",useremail);
			session.setAttribute("OTP", otp);
		    return "verify-otp";
			
			
			}
			
		
		else {
			session.setAttribute("message", new Message("OTP has not been sent due to some technical error!!!","danger"));
			//model.addAttribute("message","Failed");
			return "forgot-form";
		}
		
		
		
	}
	
	@PostMapping("/doVerifyEmail")
	@ResponseBody
	public ResponseEntity<String> doVerifyEmail(@RequestBody Map<String,Object> data,Principal p)
	{
		System.out.println("Email:"+data.get("email").toString());
		User user = this.userRepository.getUserByUserName(data.get("email").toString());
		System.out.println(user);
		if(user != null)
		{
			if(data.get("email").toString().equals(user.getEmail())) {
				return new ResponseEntity<>("\"Email is Registered with us\"", HttpStatus.OK);
			}
			else
				return new ResponseEntity<>("\"Email is not Registered with us\"", HttpStatus.OK);
			
		}
		else {
			return new ResponseEntity<>("\"FAILED\"", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
	
	@PostMapping("/doVerifyOTP")
	@ResponseBody
	public ResponseEntity<String> doVerifyOTP(@RequestBody Map<String,Object> data,HttpSession session,Message message1) throws NumberFormatException, ExecutionException
	{
		System.out.print(data.get("OTP").toString());
		System.out.print(session.getAttribute("email").toString());
		System.out.println(session.getAttribute("OTP").toString());
		
		User user = this.userRepository.getUserByUserName(session.getAttribute("email").toString());
		System.out.println(user);
		
		String result = this.otpService.validateOTP(Integer.parseInt(data.get("OTP").toString()),user.getName());
		
		System.out.println(result);

		if(result.equals("Entered Otp is valid"))
		{
			String message = ""
					+"<div style='border:1px solid #e2e2e2;padding:20px;'>"
					+"<h1>"
					+"OTP is:"
					+"<b>"+user.getPassword().toString()
					+"</n>"
					+"</h1>"
					+"</div>";
			
			this.emailservice.sendEmail(session.getAttribute("email").toString(), "Your password at SCM",message );
			//this.otpService.clearOTP(user.getName());
			
			return new ResponseEntity<>("\"OK\"",HttpStatus.OK);
		}
		else if(result.equals("Entered Otp is NOT valid. Please Retry!"))
		{
			
			message1.setContent("Entered OTP is NOT valid. Please Retry!");
			message1.setType("danger");
			
			return new ResponseEntity<>(message1.getContent(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
		else
		{
			System.out.println("EXP");
			message1.setContent("OTP has expired.Please try after clicking resend button.");			System.out.println("Ismile while using git for sample changes to code in Home controller");
			System.out.println("Wow ! experiencing git");
			message1.setType("danger");
			return new ResponseEntity<>(message1.getContent(),HttpStatus.REQUEST_TIMEOUT);
		}
			
		
	}
	
}
