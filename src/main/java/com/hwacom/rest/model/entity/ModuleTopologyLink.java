package com.hwacom.rest.model.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity

@Data
public class ModuleTopologyLink {
	
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "data_id", unique = true)
	private Long dataId;
    
	@Column(name = "username", nullable = false)
	private String username;
	
	@Column(name = "group_id", nullable = false)
	private String groupId;
	
	@Column(name = "source", nullable = false)
	private Integer source;
	
	@Column(name = "target", nullable = false)
	private Integer target;
	
	@Column(name = "source_port", nullable = false)
	private String sourcePort;
	
	@Column(name = "target_port", nullable = false)
	private String targetPort;
	
	@Column(name = "create_time", nullable = true, insertable=false)
	private Timestamp createTime;

	@Column(name = "create_by", nullable = true, insertable=false)
	private String createBy;

	@Column(name = "update_time", nullable = true, insertable=false)
	private Timestamp updateTime;

	@Column(name = "update_by", nullable = true, insertable=false)
	private String updateBy;

	
	public ModuleTopologyLink() {
		super();
	}


	public ModuleTopologyLink(Long dataId, String username, String groupId, Integer source, Integer target,
			String sourcePort, String targetPort, Timestamp createTime, String createBy, Timestamp updateTime,
			String updateBy) {
		super();
		this.dataId = dataId;
		this.username = username;
		this.groupId = groupId;
		this.source = source;
		this.target = target;
		this.sourcePort = sourcePort;
		this.targetPort = targetPort;
		this.createTime = createTime;
		this.createBy = createBy;
		this.updateTime = updateTime;
		this.updateBy = updateBy;
	}


	public Long getDataId() {
		return dataId;
	}


	public void setDataId(Long dataId) {
		this.dataId = dataId;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getGroupId() {
		return groupId;
	}


	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}


	public Integer getSource() {
		return source;
	}


	public void setSource(Integer source) {
		this.source = source;
	}


	public Integer getTarget() {
		return target;
	}


	public void setTarget(Integer target) {
		this.target = target;
	}


	public String getSourcePort() {
		return sourcePort;
	}


	public void setSourcePort(String sourcePort) {
		this.sourcePort = sourcePort;
	}


	public String getTargetPort() {
		return targetPort;
	}


	public void setTargetPort(String targetPort) {
		this.targetPort = targetPort;
	}


	public Timestamp getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}


	public String getCreateBy() {
		return createBy;
	}


	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}


	public Timestamp getUpdateTime() {
		return updateTime;
	}


	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}


	public String getUpdateBy() {
		return updateBy;
	}


	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	
}
