package blockchain_gateway.adapters;

import java.util.List;

import blockchain_gateway.model.MedicalRecord;
import blockchain_gateway.model.MedicalRecordStatus;

public interface BlockchainAdapter {

	public Boolean submitRecord(MedicalRecord record) throws Exception;

	public Boolean submitBulkRecords(List<MedicalRecord> records) throws Exception;

	public List<MedicalRecordStatus> validateRecords(List<MedicalRecord> records) throws Exception;

	public String queryStatus(String parameter) throws Exception;

	public String testConnection();

}