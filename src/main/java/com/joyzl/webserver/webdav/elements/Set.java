package com.joyzl.webserver.webdav.elements;

import java.util.HashMap;
import java.util.Map;

/**
 * 要为资源设置的属性值
 * 
 * @author ZhangXi 2025年2月9日
 */
public class Set extends Element implements Prop {
	/*-
	 * <!ELEMENT set (prop) >
	 * 
	 * <D:set> 
	    <D:prop> 
	        <Z:Authors> 
	            <Z:Author>Jim Whitehead</Z:Author> 
	            <Z:Author>Roy Fielding</Z:Author> 
	        </Z:Authors> 
	    </D:prop> 
	   </D:set>
	 */

	private final Map<String, Object> prop = new HashMap<>();

	@Override
	public Map<String, Object> getProp() {
		return prop;
	}

	@Override
	public void setProp(Map<String, Object> values) {
		if (prop != values) {
			prop.clear();
			prop.putAll(values);
		}
	}
}