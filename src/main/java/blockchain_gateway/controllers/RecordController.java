package blockchain_gateway.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import blockchain_gateway.management.BlockchainManager;
import blockchain_gateway.model.Item;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class RecordController {

	private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

	@Autowired
	BlockchainManager blockchainManager;

	@PostMapping("/record")
	ResponseEntity<?> newRecord(@RequestBody Item newRecord, UriComponentsBuilder ucb) {
		if(newRecord.getHash()==null){
			logger.error("Failed to add record !!");
			return new ResponseEntity<>("Hash field is mandatory and cannot be null !!",HttpStatus.BAD_REQUEST);
			}
		else if(newRecord.getId()==null) {
			logger.error("Failed to add record !!");
			return new ResponseEntity<>("ID field is mandatory and cannot be null !!",HttpStatus.BAD_REQUEST);
			}
		else {
			Boolean status = blockchainManager.addRecord(newRecord);
			if (status) {
				HttpHeaders headers = new HttpHeaders();
				URI locationUri = ucb.path("/api/v1/record/").path(String.valueOf(newRecord.getId())).path("/status")
						.build().toUri();
				headers.setLocation(locationUri);
				return new ResponseEntity<>(null, headers, HttpStatus.CREATED);
			} else {
				logger.error("Failed to add record !!");
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to accept the record !!");
			}
		}
		
	}

	@GetMapping("/record/{id}/status")
	ResponseEntity<?> getStatus(@PathVariable("id") String id) {
		try {
			String status = blockchainManager.getStatus(id);
			return new ResponseEntity<>(status, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error("Failed to get status. Detals: " + ex.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get the status !!");
		}
	}

	@GetMapping("/record/{id}")
	ResponseEntity<?> retrieveRecord(@PathVariable("id") String id) {
		try {
		String record = blockchainManager.retrieveRecord(id);
		if (record != null) {
			return new ResponseEntity<>(record, HttpStatus.OK);
		} else {
			logger.error("Record not found !!");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found !!");
		}
		} catch (Exception ex) {
			logger.error("Failed to retrieve record !!");
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get the status !!");
		}
	}
}
