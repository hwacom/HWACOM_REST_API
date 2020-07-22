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
public class ModuleTopologyNode {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "data_id", unique = true)
    private Long dataId;
    
	@Column(name = "username", nullable = false)
	private String username;
	
	@Column(name = "group_id", nullable = false)
	private String groupId;
	
	@Column(name = "id", nullable = false)
	private Integer id;
	
	@Column(name = "x_axis", nullable = true)
	private Float xAxis;
	
	@Column(name = "y_axis", nullable = true)
	private Float yAxis;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "brand", nullable = false)
	private String brand;
	
	@Column(name = "device_type", nullable = false)
	private String deviceType;
	
	@Column(name = "device_id", nullable = false)
	private Integer deviceId;
	
	@Column(name = "create_time", nullable = true, insertable=false)
	private Timestamp createTime;

	@Column(name = "create_by", nullable = true, insertable=false)
	private String createBy;

	@Column(name = "update_time", nullable = true, insertable=false)
	private Timestamp updateTime;

	@Column(name = "update_by", nullable = true, insertable=false)
	private String updateBy;

	public ModuleTopologyNode() {
		super();
	}

	public ModuleTopologyNode(Long dataId, String username, String groupId, Integer id, Float xAxis, Float yAxis,
			String name, String brand, String deviceType, Integer deviceId, Timestamp createTime, String createBy,
			Timestamp updateTime, String updateBy) {
		super();
		this.dataId = dataId;
		this.username = username;
		this.groupId = groupId;
		this.id = id;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.name = name;
		this.brand = brand;
		this.deviceType = deviceType;
		this.deviceId = deviceId;
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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Float getxAxis() {
		return xAxis;
	}

	public void setxAxis(Float xAxis) {
		this.xAxis = xAxis;
	}

	public Float getyAxis() {
		return yAxis;
	}

	public void setyAxis(Float yAxis) {
		this.yAxis = yAxis;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
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
