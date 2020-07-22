package com.hwacom.rest.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.hwacom.rest.model.entity.ModuleTopologyLink;
import com.hwacom.rest.model.entity.ModuleTopologyNode;

public class TopologyVO {

	private List<ModuleTopologyNode> nodeList = new ArrayList<ModuleTopologyNode>();
	
	private List<ModuleTopologyLink> linkList = new ArrayList<ModuleTopologyLink>();

	public TopologyVO() {
		super();
	}

	public TopologyVO(List<ModuleTopologyNode> nodeList, List<ModuleTopologyLink> linkList) {
		super();
		this.nodeList = nodeList;
		this.linkList = linkList;
	}

	public List<ModuleTopologyNode> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<ModuleTopologyNode> nodeList) {
		this.nodeList = nodeList;
	}

	public List<ModuleTopologyLink> getLinkList() {
		return linkList;
	}

	public void setLinkList(List<ModuleTopologyLink> linkList) {
		this.linkList = linkList;
	}
	
}
