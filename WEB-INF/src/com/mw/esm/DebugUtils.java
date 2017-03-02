package com.mw.esm;

import com.microstrategy.web.beans.RequestKeys;

public class DebugUtils {
	
	public static String getRequestKeysAsString(RequestKeys reqKeys){
		StringBuilder requestParamsStr = new StringBuilder();
		for(int i = 0; i < reqKeys.getNameCount(); i++){
			requestParamsStr.append(reqKeys.getName(i));
			requestParamsStr.append(":");
			requestParamsStr.append(reqKeys.getValue(reqKeys.getName(i)));
			requestParamsStr.append(",");
		}
		return requestParamsStr.toString();
	}
	
}
