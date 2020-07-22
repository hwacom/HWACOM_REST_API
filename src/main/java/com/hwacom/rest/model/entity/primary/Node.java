package com.hwacom.rest.model.entity.primary;

import org.springframework.data.rest.core.config.Projection;

import com.hwacom.rest.model.entity.ModuleTopologyNode;

/**
 * @author Alvin Liu
 *  Projection interface的擺放路徑必須可以存取types參考的Entity class，相同package或sub package)
 */
@Projection(name = "node", types = ModuleTopologyNode.class)
public interface Node {
	
	String getId();

	String getX();
	
	String getY();
	
	String getName();
	
	String getDeviceType();
	
	String getDeviceId();
	
}
