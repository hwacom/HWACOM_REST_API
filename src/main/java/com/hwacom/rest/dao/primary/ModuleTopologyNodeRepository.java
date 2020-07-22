package com.hwacom.rest.dao.primary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;

import com.hwacom.rest.model.entity.ModuleTopologyNode;

@RepositoryRestResource
public interface ModuleTopologyNodeRepository extends JpaRepository<ModuleTopologyNode, Long>{
	//自定義查詢方法
	@RestResource(exported = true, path = "findByUserame", rel = "findByUserame")
    @Query("SELECT node FROM ModuleTopologyNode node WHERE node.username = :username ORDER BY node.id")
    List<ModuleTopologyNode> findByUserame(@Param("username") String username);
	
	//自定義查詢方法by UK
	@RestResource(exported = true, path = "findByUk", rel = "findByUk")
	@Query("SELECT node FROM ModuleTopologyNode node WHERE node.username = :username AND node.id = :id")
	ModuleTopologyNode findByUk(String username, Integer id);
	
	//自定義刪除方法
	@RestResource(exported = true, path = "delByUsername", rel = "delByUsername")
	@Transactional
	@Modifying
    @Query("DELETE FROM ModuleTopologyNode node WHERE node.username = :username")   
    void delByUsername(@Param("username") String username);
	
	//停用預設的刪除方法
	//@Override
	//@RestResource(exported = false)
	//void delete(ModuleTopologyNode moduleTopologyNode);
}
