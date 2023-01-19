package blockchain_gateway.adapters.fabric;

import java.util.List;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import blockchain_gateway.adapters.BlockchainAdapter;
import blockchain_gateway.model.MedicalRecord;
import blockchain_gateway.model.MedicalRecordStatus;

public class FabricAdapter implements BlockchainAdapter {
	private static final String CHANNEL_NAME = "channelsiteasiteb";
	private static final String CHAINCODE_NAME = "record_ledger";
	private String blockchainId;
	private static final Logger logger = LoggerFactory.getLogger(FabricAdapter.class);
	public GatewayManager m_GatewayManager;

	public FabricAdapter(String blockchainId) {
		this.blockchainId = blockchainId;
	}

	@Override
	public String testConnection() {
		try {
			logger.info("Testing the blockchain connection ..");
			Gateway gateway = GatewayManager.getInstance().getGateway(blockchainId);
			if (gateway.getIdentity() != null)
				return "true";
			else
				return "Cannot get gateway identity!";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public Boolean submitRecord(MedicalRecord record) throws Exception {
		try {
			logger.info("Submitting a record into Blockchain ..");
			Contract contract = GatewayManager.getInstance().getContract(blockchainId, CHANNEL_NAME, CHAINCODE_NAME);
			byte[] resultAsBytes = contract.submitTransaction("AddRecord", record.getId(), record.getHash(),
					record.getTime(), record.getData());
			String stringResult = new String(resultAsBytes);

			if (stringResult.isEmpty())
				return false;
			else
				return true;
		} catch (Exception e) {
			throw new Exception("Cannot submit transaction. Reason: " + e.getMessage());
		}
	}

	@Override
	public Boolean submitBulkRecords(List<MedicalRecord> records) throws Exception {
		try {
			logger.info("Submitting bulk records into Blockchain ..");
			ObjectWriter ow = new ObjectMapper().writerFor(new TypeReference<List<MedicalRecord>>() {
			});
			String payload = ow.writeValueAsString(records);
			Contract contract = GatewayManager.getInstance().getContract(blockchainId, CHANNEL_NAME, CHAINCODE_NAME);
			byte[] resultAsBytes = contract.submitTransaction("AddRecordBulk", payload);
			String stringResult = new String(resultAsBytes);

			if (stringResult.isEmpty())
				return false;
			else
				return true;
		} catch (Exception e) {
			throw new Exception("Cannot submit bulk transactions. Reason: " + e.getMessage());
		}
	}

	@Override
	public List<MedicalRecordStatus> validateRecords(List<MedicalRecord> records) throws Exception {
		try {
			logger.info("Validating medical records ..");
			ObjectWriter ow = new ObjectMapper().writerFor(new TypeReference<List<MedicalRecord>>() {
			});
			String payload = ow.writeValueAsString(records);
			Contract contract = GatewayManager.getInstance().getContract(blockchainId, CHANNEL_NAME, CHAINCODE_NAME);
			byte[] resultAsBytes = contract.evaluateTransaction("ValidateRecords", payload);
			String stringResult = new String(resultAsBytes);

			if (stringResult.isEmpty())
				return null;
			else {
				ObjectReader statusMapper = new ObjectMapper()
						.readerFor(new TypeReference<List<MedicalRecordStatus>>() {
						});
				return statusMapper.readValue(stringResult);
			}
		} catch (Exception e) {
			throw new Exception("Cannot validate records. Reason: " + e.getMessage());
		}
	}

	@Override
	public String queryStatus(String parameter) throws Exception {
		try {
			logger.info("Get a record from blockchain ..");
			Contract contract = GatewayManager.getInstance().getContract(blockchainId, CHANNEL_NAME, CHAINCODE_NAME);

			byte[] Result = contract.evaluateTransaction("ViewRecord", parameter);

			String stringResult = new String(Result);
			return stringResult;
		} catch (Exception e) {
			final String msg = String.format("Cannot retrieve the record. Details: %s", e.getMessage());
			logger.error(msg);
			return "";
		}
	}
}
