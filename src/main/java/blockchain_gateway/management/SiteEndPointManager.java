package blockchain_gateway.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blockchain_gateway.config.StartupConfig;
import blockchain_gateway.model.SiteEndpoints;

public class SiteEndPointManager {

	private static SiteEndPointManager instance = null;
	private Map<String, List<String>> map;

	public static SiteEndPointManager getInstance() {
		if (instance == null) {
			instance = new SiteEndPointManager();
		}
		return instance;
	}

	private SiteEndPointManager() {
		map = new HashMap<>();
		List<SiteEndpoints> endpoints = StartupConfig.getInstance().getGroup();

		for (SiteEndpoints endpoint : endpoints) {
			map.put(endpoint.getSiteID(), endpoint.getEndpoints());
		}
	}

	public String getEndpoint(String id) {
		return map.get(id).get(0);
	}

	public List<String> getSiteIDs() {
		return new ArrayList<String>(map.keySet());
	}
}
