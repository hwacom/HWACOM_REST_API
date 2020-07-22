package com.hwacom.rest.dao.primary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;

import com.hwacom.rest.model.entity.ModuleTopologyLink;

@RepositoryRestResource
public interface ModuleTopologyLinkRepository extends JpaRepository<ModuleTopologyLink, Long>{

	//自定義查詢方法
	@RestResource(exported = true, path = "findByUsername", rel = "findByUsername")
    @Query("SELECT link FROM ModuleTopologyLink link WHERE link.username = :username")
    List<ModuleTopologyLink> findByUserame(@Param("username") String username);
	
	//自定義刪除方法
	@RestResource(exported = true, path = "delByUsername", rel = "delByUsername")
	@Transactional
	@Modifying
    @Query("DELETE FROM ModuleTopologyLink link WHERE link.username = :username")   
    void delByUsername(@Param("username") String username);
	
}
