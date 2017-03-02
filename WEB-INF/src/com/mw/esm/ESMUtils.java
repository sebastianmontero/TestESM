package com.mw.esm;

import com.microstrategy.utils.log.Level;
import com.microstrategy.web.app.tags.Log;
import com.microstrategy.web.objects.WebCluster;
import com.microstrategy.web.objects.WebClusterAdmin;
import com.microstrategy.web.objects.WebObjectsException;
import com.microstrategy.web.objects.WebObjectsFactory;

public class ESMUtils {


	public static String getIServer() {

		WebClusterAdmin wca = WebObjectsFactory.getInstance().getClusterAdmin();
		String iServer = ESMProperties.getTrimmedString("iServerName");
		// wca.refreshAllClusters();
		try {
			WebCluster wc = wca.getCluster(iServer);
			if (wc != null) {
				if (wc.size() > 0) {
					iServer = wc.get(0).getNodeName();
					Log.logger.logp(Level.INFO, "ESMUtils", "getIServer", String.format("IServer: %1$s ", iServer));// Log
				}
			}
		} catch (WebObjectsException e) {
			Log.logger.logp(Level.WARNING, "ESMUtils", "getIServer",
					"Exception thrown while getting cluster IServerNodes ", e); //$NON-NLS-1$
		}
		return iServer;
	}

}
