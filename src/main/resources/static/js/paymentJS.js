const paymentStart = () => {
	
	let amount = $('#amount').val();
	if(amount == '' || amount == null)
	{
		swal(
  			'Not Good job',
  			'Amount is required',
  			'error'
		)
		return;
	}
	
	// we will use ajax to send request to server to create order using jquery ajax. We will not use slim.min.js because in slim.min.js ajax function is not supported.
	
	$.ajax({
		
		url:'/user/createOrder',
		data:JSON.stringify({amount:amount,info:'order Request'}),
		contentType:'application/json',
		type:'POST',
		dataType:'JSON',
		success:function(response){
			
			console.log(response)
			if(response.status == "created")
			{
				let options = {
					
					key:"rzp_test_0k3pUedLO218Fb",
					amount:response.amount,
					currency:"INR",
					name:"Smart Contact Manager",
					description:"Donation",
					image:"D:/SpringTutorial/SmartContactManager/src/main/resources/static/img/index.jpg",
					order_ID:response.id,
					handler:function(response){
						alert(response)
						console.log(response.razorpay_payment_id)
						console.log(response.razorpay_order_id)
						console.log(response.razorpay_signature)
						updatetoserver(response.razorpay_payment_id,response.razorpay_order_id,'PAID')
						
						
					},
					prefill:{
						name:"",
						email:"",
						contact:""
					},
					notes:{
						address:""
					},
					theme:{
						color:"#3399cc"
					}
					
				};
				let rzp = new Razorpay(options);
				rzp.on('payment.failed',function(response){
					console.log(response.error.code);
					console.log(response.error.description);
					console.log(response.error.source);
					console.log(response.error.step);
					console.log(response.error.reason);
					console.log(response.error.metadata.order_id);
					console.log(response.error.metadata.payment_id);
					swal(
  			'Not Good job',
  			'Payment failed',
  			'error'
		);
				})
				rzp.open();
				
			}
			
		},
		error:function(error){
			console.log(error)
			
		}
		
		
		
		
	})
	
}

function updatetoserver(paymentId,orderId,status)
{
	$.ajax({
		
		url:'/user/updateOrder',
		data:JSON.stringify({payment_Id:paymentId,order_Id:orderId,status:status}),
		contentType:'application/json',
		type:'POST',
		dataType:'JSON',
		
		success:function(response){
			
			swal(
  			'Good job',
  			'Payment successful',
  			'success'
		)
			
		},
		error:function(error){
			swal(
  			'Good job',
  			'Payment successful but did not get on my server.We will contact you as soon as possible.',
  			'error'
		)
		}
		
	})
}