package com.example.scm.service;



import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.example.scm.dao.UserRepository;


import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

@Service
public class OneTimePasswordService {


	private static final Integer EXPIRE_MINS = 1;

	private LoadingCache<String, Integer> otpCache;

	public OneTimePasswordService(){

		super();
		otpCache = CacheBuilder.newBuilder().
				expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES)
				.build(new CacheLoader<String, Integer>() {
					public Integer load(String key) {
						return 0;

					}
				});
	}


	public int generateOTP(String key){

		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		otpCache.put(key, otp);
		return otp;
	}

	public int getOtp(String key){ 
		try{
			return otpCache.get(key); 
		}catch (Exception e){
			return 0; 
		}
	}

	public void clearOTP(String key){ 
		otpCache.invalidate(key);
	}

	public String validateOTP(int otpnum,String username) throws ExecutionException
	{
		final String SUCCESS = "Entered Otp is valid";
		final String FAIL = "Entered Otp is NOT valid. Please Retry!";
		final String ISEXPIRED = "OTP has expired.Please try after clicking resend button";
		System.out.println("otpcache: "+otpCache.get(username));
		
		if(otpCache.get(username) <= 0)
		{
			System.out.println("otpcache: "+otpCache.get(username));
			return (ISEXPIRED);
		}
		else
		{
		if(otpnum >= 0)
		{

			int serverOtp = getOtp(username);
			if(serverOtp > 0)
			{
				
				if(otpnum == serverOtp)
				{
					clearOTP(username);

					return (SUCCESS);
				} 
				else
				{
					return FAIL;
				}
			}
			else
			{
				return FAIL;
			}
		}
		else
		{
			return FAIL;
		}

		}
	}
}
