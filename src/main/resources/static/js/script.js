const togglesideBar = () => {
	
	if($('.sidebar').is(':visible'))
	{
		$('.sidebar').css('display','none');
		$('.content').css('margin-left','0%');	
		$('#barIcon').css('display','block');
		
	}
	else
	{
		$('.sidebar').css('display','block');
		$('.content').css('margin-left','20%');
		$('#barIcon').css('display','none');
	}
	
}

const search = () => {
	console.log("Hi")
	
	let query = $('#search-input').val();
	if(query != ''){
		let url = `http://localhost:8080/user/search-contact/${query}`;
		fetch(url)
		.then((response) => {
			
			return response.json();
			
		}).then((data)=>{
			alert("hi")
			console.log(data)
			let text = `<div class='list-group'>`;
			 
			 data.forEach((contact)=>{
				 
				 text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'>${contact.name} </a>`;
			 });
			
			text += `</div>`;
			$('.search-result').html(text);
			$('.search-result').show();
		});
		
	}
	else{
		$('.search-result').hide();
	}
	
}
