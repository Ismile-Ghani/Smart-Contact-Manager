package com.example.scm.helper;

import java.util.Random;


public class OneTimePasswordHelper {

	

	    private final static Integer LENGTH = 6;

	    public static Integer createRandomOneTimePassword() {
	        
	            Random random = new Random();
	            StringBuilder oneTimePassword = new StringBuilder();
	            for (int i = 0; i < LENGTH; i++) {
	                int randomNumber = random.nextInt(10);
	                oneTimePassword.append(randomNumber);
	            }
	            return Integer.parseInt(oneTimePassword.toString().trim());
	        
	    }
	}
	

