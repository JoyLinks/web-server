package com.joyzl.webserver.webdav.elements;

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

	@Override
	public Map<String, Object> getProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProp(Map<String, Object> values) {
		// TODO Auto-generated method stub

	}
	/*-
	 * <!ELEMENT set (prop) >
	 */
}