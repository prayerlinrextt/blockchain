package blockchain_gateway.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SiteEndpoints {

	List<String> endpoints;

	@JsonProperty("site_id")
	String siteID;

	public List<String> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<String> endpoints) {
		this.endpoints = endpoints;
	}

	public String getSiteID() {
		return siteID;
	}

	public void setSiteID(String siteID) {
		this.siteID = siteID;
	}

	public SiteEndpoints() {
		endpoints = new ArrayList<String>();
	}
}
