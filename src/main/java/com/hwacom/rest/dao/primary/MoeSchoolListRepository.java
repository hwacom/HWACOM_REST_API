package com.hwacom.rest.dao.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;


import com.hwacom.rest.model.entity.MoeSchoolList;

@RepositoryRestResource
public interface MoeSchoolListRepository extends JpaRepository<MoeSchoolList, String>{
	
	//自定義查詢方法by UK
	@RestResource(exported = true, path = "findByUk", rel = "findByUk")
	@Query("SELECT school FROM MoeSchoolList school WHERE school.schoolId = :schoolId")
	MoeSchoolList findByUk(String schoolId);
	
	//停用預設的刪除方法
	//@Override
	//@RestResource(exported = false)
	//void delete(ModuleTopologyNode moduleTopologyNode);
}
