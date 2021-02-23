package com.hwacom.rest.model.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity

@Data
public class MoeSchoolList {
	
    @Id
    @Column(name = "school_id", unique = true)
	private String schoolId;
    
	@Column(name = "district", nullable = false)
	private String district;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "bandwidth", nullable = true)
	private Long bandwidth;
	
	@Column(name = "ping_sensor_id", nullable = true)
	private String pingSensorId;
	
	@Column(name = "export_traffic_sensor_id", nullable = true)
	private String exportTrafficSensorId;
	
	@Column(name = "create_time", nullable = true, insertable=false)
	private Timestamp createTime;

	@Column(name = "create_by", nullable = true, insertable=false)
	private String createBy;

	@Column(name = "update_time", nullable = true, insertable=false)
	private Timestamp updateTime;

	@Column(name = "update_by", nullable = true, insertable=false)
	private String updateBy;

	public MoeSchoolList() {
		super();
	}
	
	public MoeSchoolList(String schoolId, String district, String name, Long bandwidth, String pingSensorId,
			String exportTrafficSensorId, Timestamp createTime, String createBy, Timestamp updateTime,
			String updateBy) {
		super();
		this.schoolId = schoolId;
		this.district = district;
		this.name = name;
		this.bandwidth = bandwidth;
		this.pingSensorId = pingSensorId;
		this.exportTrafficSensorId = exportTrafficSensorId;
		this.createTime = createTime;
		this.createBy = createBy;
		this.updateTime = updateTime;
		this.updateBy = updateBy;
	}


	public String getSchoolId() {
		return schoolId;
	}


	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}


	public String getDistrict() {
		return district;
	}


	public void setDistrict(String district) {
		this.district = district;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Long getBandwidth() {
		return bandwidth;
	}


	public void setBandwidth(Long bandwidth) {
		this.bandwidth = bandwidth;
	}


	public String getPingSensorId() {
		return pingSensorId;
	}


	public void setPingSensorId(String pingSensorId) {
		this.pingSensorId = pingSensorId;
	}


	public String getExportTrafficSensorId() {
		return exportTrafficSensorId;
	}


	public void setExportTrafficSensorId(String exportTrafficSensorId) {
		this.exportTrafficSensorId = exportTrafficSensorId;
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
