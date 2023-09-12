package com.example.scm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.scm.dao.UserRepository;
import com.example.scm.entities.User;

public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
	
	@Autowired
	private UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user= this.userRepo.getUserByUserName(username);
		if(user==null)
			throw new UsernameNotFoundException("could not find user");
		CustomUserDetails c= new CustomUserDetails(user);
		return c;
	}

}
