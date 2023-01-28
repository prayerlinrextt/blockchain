package blockchain_gateway.management;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import blockchain_gateway.adapters.AdapterManager;
import blockchain_gateway.connectionprofiles.ConnectionProfilesManager;
import blockchain_gateway.model.Item;
import blockchain_gateway.model.ItemStatus;
import blockchain_gateway.model.Status;

@Component
public class BlockchainManager {
	private static final Logger logger = LoggerFactory.getLogger(BlockchainManager.class);

	@Autowired
	RestTemplate restTemplate;

	public String getStatus(String id) {
		try {
			Status status;
			if (BatchTransactionExecutor.getInstance().isPendingRecord(id)) {
				status = Status.PENDING;
				return JsonNodeFactory.instance.objectNode().put("status", status.getStatus()).toPrettyString();
			} else {
				try {
					String record = retrieveRecord(id);
					if (!record.isEmpty())
						status = Status.CREATED;
					else
						status = Status.NOT_FOUND;
				} catch (Exception e) {
					status = Status.NOT_FOUND;
					logger.error("Failed to get the record status from blockchain. Reason: " + e.getMessage());
				}
				return JsonNodeFactory.instance.objectNode().put("status", status.getStatus()).toPrettyString();
			}
		} catch (Exception e) {
			logger.error("Failed to get the record status. Reason: " + e.getMessage());
			return null;
		}
	}

	/**
	 * 
	 * @param payload
	 */
	public Boolean addRecord(Item record) {
		try {
			BatchTransactionExecutor.getInstance().addRecord(record);
			return true;
		} catch (Exception e) {
			logger.error("Failed to add record. Reason: " + e.getMessage());
			return false;
		}
	}

	public String testConnection(String blockchainIdentifier) {
		try {
			var result = AdapterManager.getInstance().getAdapter(blockchainIdentifier).testConnection();
			return result;
		} catch (Exception e) {
			logger.error("Failed to test blockchain connection. Reason: " + e.getMessage());
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String validateRecords(List<Item> records) {
		try {
			Object mutex = new Object();
			List<ItemStatus> recordStatus = records.stream().map(p -> new ItemStatus(p.getId()))
					.collect(Collectors.toList());

			var blockchainIds = ConnectionProfilesManager.getInstance().getblockchainIds();

			List<CompletableFuture> futureList = new ArrayList<>();
			for (String id : blockchainIds) {
				futureList.add(CompletableFuture.supplyAsync(() -> validateByAdapter(id, records)));
			}
			futureList.forEach(future -> {
				synchronized (mutex) {
					try {
						List<ItemStatus> status = (List<ItemStatus>) future.get();
						List<ItemStatus> recordsWithNonValidStatus = recordStatus.stream().filter(c -> {
							if (c.getStatus() != Status.VALID && c.getStatus() != Status.IN_VALID)
								return true;
							else
								return false;
						}).collect(Collectors.toList());
						if (recordsWithNonValidStatus != null) {
							for (ItemStatus temp : recordsWithNonValidStatus) {
								ItemStatus record = status.stream()
										.filter(objcet -> temp.getId().equals(objcet.getId())).findAny().orElse(null);
								if (record.getStatus() != Status.NOT_FOUND)
									temp.setStatus(record.getStatus());
							}
						}
					} catch (Exception e) {
						logger.error("Failed to merge validation results. Reason: " + e.getMessage());
					}
				}
			});
			futureList.forEach(CompletableFuture::join);
			ObjectWriter ow = new ObjectMapper().writerFor(new TypeReference<List<ItemStatus>>() {
			});
			return ow.writeValueAsString(recordStatus);
		} catch (Exception e) {
			logger.error("Failed to validate records. Reason: " + e.getMessage());
			return "";
		}
	}

	public String retrieveRecord(String id) throws Exception {
		try {
			String record = "";
			var blockchainIds = ConnectionProfilesManager.getInstance().getblockchainIds();

			for (String blockchainId : blockchainIds) {
				var ret = AdapterManager.getInstance().getAdapter(blockchainId).queryStatus(id);
				if (!ret.isEmpty()) {
					record = ret;
					break;
				}
			}
			return record;
		} catch (Exception e) {
			logger.error("Failed to retrieve record. Reason: " + e.getMessage());
			throw e;
		}
	}

	private List<ItemStatus> validateByAdapter(String blockchainId, List<Item> records) {
		try {
			return AdapterManager.getInstance().getAdapter(blockchainId).validateRecords(records);
		} catch (Exception e) {
			logger.error("Failed to validate records in blockchain ID %s. Reason: %s", blockchainId,
					e.getMessage());
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public String validateRemoteRecords(List<Item> records) {
		try {
			Object mutex = new Object();
			List<ItemStatus> recordStatus = records.stream().map(p -> new ItemStatus(p.getId()))
					.collect(Collectors.toList());

			var blockchainIds = SiteEndPointManager.getInstance().getSiteIDs();

			List<CompletableFuture> futureList = new ArrayList<>();
			for (String id : blockchainIds) {
				futureList.add(CompletableFuture.supplyAsync(() -> validateRecordsOnSite(id, records)));
			}
			futureList.forEach(future -> {
				synchronized (mutex) {
					try {
						String ret = (String) future.get();
						ObjectReader statusMapper = new ObjectMapper()
								.readerFor(new TypeReference<List<ItemStatus>>() {
								});
						List<ItemStatus> status = statusMapper.readValue(ret);
						List<ItemStatus> recordsWithNonValidStatus = recordStatus.stream().filter(c -> {
							if (c.getStatus() != Status.VALID && c.getStatus() != Status.IN_VALID)
								return true;
							else
								return false;
						}).collect(Collectors.toList());
						if (recordsWithNonValidStatus != null) {
							for (ItemStatus temp : recordsWithNonValidStatus) {
								ItemStatus record = status.stream()
										.filter(objcet -> temp.getId().equals(objcet.getId())).findAny().orElse(null);
								if (record.getStatus() != Status.NOT_FOUND)
									temp.setStatus(record.getStatus());
							}
						}
					} catch (Exception e) {
						logger.error("Failed to merge validation results. Reason: " + e.getMessage());
					}
				}
			});
			futureList.forEach(CompletableFuture::join);
			ObjectWriter ow = new ObjectMapper().writerFor(new TypeReference<List<ItemStatus>>() {
			});
			return ow.writeValueAsString(recordStatus);
		} catch (Exception e) {
			logger.error("Failed to validate records. Reason: " + e.getMessage());
			return "";
		}
	}

	public String validateRecordsOnSite(String siteID, List<Item> records) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<List<Item>> entity = new HttpEntity<List<Item>>(records, headers);
			String endpoint = SiteEndPointManager.getInstance().getEndpoint(siteID) + "/validate";
			return restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class).getBody();
		} catch (Exception ex) {
			throw ex;
		}
	}
}
