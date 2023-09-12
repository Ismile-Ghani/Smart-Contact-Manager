package com.example.scm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.scm.entities.MyOrders;

public interface orderRepository extends JpaRepository<MyOrders, Long> {
	
	public MyOrders findByorderId(String orderId);

}
