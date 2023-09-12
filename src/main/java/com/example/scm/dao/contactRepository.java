package com.example.scm.dao;



import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.scm.entities.Contact;
import com.example.scm.entities.User;

public interface contactRepository extends JpaRepository<Contact, Integer> {
	
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactByUser(@Param("userId") int userId,Pageable pageable);
	
	
	@Query("select t from Contact t order by t.cId desc")
	public List<Contact> findTopByOrderByIdDesc();
	
	@Transactional
	 @Modifying
	@Query("update Contact u set u.image = ?1 where u.cId = ?2")
	public void setUserInfoById(String image,Integer cId);
	
	public List<Contact> findByNameContainingAndUser(String name,User user);
	
	

}
