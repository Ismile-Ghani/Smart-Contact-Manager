package com.example.scm.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class MyOrders {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long myorder_Id;
	private String orderId;
	private String amount;
	private String receipt;
	private String status;
	private String payment_Id;
	@ManyToOne
	private User user;
	public long getMyorder_Id() {
		return myorder_Id;
	}
	public void setMyorder_Id(long myorder_Id) {
		this.myorder_Id = myorder_Id;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getReceipt() {
		return receipt;
	}
	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPayment_Id() {
		return payment_Id;
	}
	public void setPayment_Id(String payment_Id) {
		this.payment_Id = payment_Id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	

}
