package com.hwacom.rest.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.hwacom.rest.dao.primary.ModuleTopologyLinkRepository;
import com.hwacom.rest.dao.primary.ModuleTopologyNodeRepository;
import com.hwacom.rest.model.entity.ModuleTopologyLink;
import com.hwacom.rest.model.entity.ModuleTopologyNode;

/**
*
* @author Alvin
* @apiNote Annotation為RestController後回傳內容會被自動轉譯成JSON格式
*/
@RestController
public class TopologyController {

	@Autowired
	ModuleTopologyNodeRepository nodeRepository;
	
	@Autowired
	ModuleTopologyLinkRepository linkRepository;
	
	@Autowired
	DataSource dataSource;
	
	@CrossOrigin(maxAge = 3600)
	@RequestMapping(value = "topology/getGraph.json", method = {RequestMethod.POST, RequestMethod.GET})
	public Map<String, Object> findByUsername(@RequestParam(name="username", required=true, defaultValue="") String username) {
		// 宣告container並填充內容，最後會以JSON格式回傳至前端
		Map<String, Object> data = new HashMap<String, Object>();
		List<HashMap<String,Object>> nodeListMap = new ArrayList<HashMap<String,Object>>();
		List<HashMap<String,Object>> linkListMap = new ArrayList<HashMap<String,Object>>();
		
		// EntityRepository取得資料
		List<ModuleTopologyNode> nodeList = nodeRepository.findByUserame(username);
		List<ModuleTopologyLink> linkList = linkRepository.findByUserame(username);
		
		// 將資料填入container
		for(ModuleTopologyNode node:nodeList){
			HashMap<String, Object> dict = new HashMap<String, Object>();
			dict.put("id", node.getId());
			dict.put("x", node.getxAxis());
			dict.put("y", node.getyAxis());
			dict.put("name", node.getName());
			dict.put("brand", node.getBrand());
			dict.put("device_type", node.getDeviceType());
			dict.put("device_id", node.getDeviceId());
			nodeListMap.add(dict);
        }
		for(ModuleTopologyLink link:linkList){
			HashMap<String, Object> dict = new HashMap<String, Object>();
			dict.put("source", link.getSource());
			dict.put("target", link.getTarget());
			dict.put("source_port", link.getSourcePort());
			dict.put("target_port", link.getTargetPort());
			linkListMap.add(dict);
        }
		data.put("nodes", nodeListMap);
		data.put("links", linkListMap);
		// 回傳後輸出為JSON
	    return  data;
	}
	
	@CrossOrigin(maxAge = 3600)
	@RequestMapping(value = "topology/saveNodes", method = {RequestMethod.POST})
	public Map<String, Object> saveNodes(@RequestBody JsonNode jsonData) {
		String infoMsg = "";
		String errMsg = "";
		// 宣告container並填充內容，最後會以JSON格式回傳至前端
		Map<String, Object> data = new HashMap<String, Object>();
		try {
			String username = jsonData.get("username").textValue();
			JsonNode entities = jsonData.get("nodes");
			//填充Entity
			for(JsonNode entity : entities) {
				//當id非int值時會造成parseInt轉型失敗例外
				Integer id = entity.get("id").intValue();
				//find by uk再回寫 僅適用資料筆數少的情境才不會有效能issue
				ModuleTopologyNode node = nodeRepository.findByUk(username, id);
				if (node != null) {
					try {
						Float lastX = node.getxAxis();
						Float lastY = node.getyAxis();
						node.setxAxis(entity.get("x").floatValue());
						node.setyAxis(entity.get("y").floatValue());
						node.setName(entity.get("name").textValue());
						node.setBrand(entity.get("brand").textValue());
						node.setDeviceType(entity.get("device_type").textValue());
						node.setDeviceId(entity.get("device_id").intValue());
						nodeRepository.save(node);
						infoMsg += "UK["+username+","+id+"].Node("+lastX+","+lastY+")has updated to ("+entity.get("x")+","+entity.get("y")+"). \n";
					} catch(Exception e){
						errMsg += "UK["+username+","+id+"].Node("+entity.get("x")+","+entity.get("y")+") occurred error. \n";
						continue;
					}
				}else {
					errMsg += "UK["+username+","+id+"] is not found in DB.\n";
					continue;
				}
			}			
			data.put("infoMsg", infoMsg);
			data.put("errMsg", errMsg);
			return data;
		} catch (Exception e) {
			data.put("infoMsg", "error");
			data.put("errMsg", e.toString());
			return data;
		}
	}
}
