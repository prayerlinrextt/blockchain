package blockchain_gateway.adapters;

import java.util.List;

import blockchain_gateway.model.Item;
import blockchain_gateway.model.ItemStatus;

public interface BlockchainAdapter {

	public Boolean submitRecord(Item record) throws Exception;

	public Boolean submitBulkRecords(List<Item> records) throws Exception;

	public List<ItemStatus> validateRecords(List<Item> records) throws Exception;

	public String queryStatus(String parameter) throws Exception;

	public String testConnection();

}