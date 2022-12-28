package blockchain_gateway.management;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import blockchain_gateway.adapters.AdapterManager;
import blockchain_gateway.config.StartupConfig;
import blockchain_gateway.model.MedicalRecord;

public class BatchTransactionExecutor implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(BatchTransactionExecutor.class);
	private LinkedBlockingQueue<MedicalRecord> queue = new LinkedBlockingQueue<>();
	private static BatchTransactionExecutor instance = null;
	public static final int POOL_COUNT = 5;
	private int commitSize;
	private int commitTimeout;

	private final Lock flushLock = new ReentrantLock();
	private final Condition flushCondition = flushLock.newCondition();

	private BatchTransactionExecutor() {
		commitSize = StartupConfig.getInstance().getCommitSize();
		commitTimeout = StartupConfig.getInstance().getCommitTimeOut();
	}

	public static BatchTransactionExecutor getInstance() {
		if (instance == null) {
			instance = new BatchTransactionExecutor();
		}
		return instance;
	}

	@Override
	public void run() {
		logger.debug("Batch Transaction Executor is started ..");
		ExecutorService consumerExecutor = Executors.newFixedThreadPool(StartupConfig.getInstance().getThreadPoolSize());

		while (true) {

			try {
				// wait for timeout or for signal to come
				flushLock.lock();
				flushCondition.await(commitTimeout, TimeUnit.MILLISECONDS);

				final List<MedicalRecord> toFLush = new ArrayList<>();
				queue.drainTo(toFLush);

				if (!toFLush.isEmpty()) {
					consumerExecutor.submit(() -> {
						List<String> ids = toFLush.stream().map(object -> object.getId())
								.collect(Collectors.toList());
//						String result = ids.stream().map(n -> String.valueOf(n))
//								.collect(Collectors.joining(", ", "[", "]"));			
						logger.info("{} submitting {} records", Thread.currentThread(), toFLush.size());
						try {
							 AdapterManager.getInstance().getNextAdapter().submitBulkRecords(toFLush);

						} catch (Exception e) {
	                            logger.error("Failed to submit bulk transcations. Reason :" + e.getMessage());
						}
					});
				}

			} catch (Exception e) {
				logger.error("The Batch Transaction Executor is stoped. Reason :" + e.getMessage());
				Thread.currentThread().interrupt();
				break; // terminate execution in case of external interrupt
			} finally {
				flushLock.unlock();
			}
		}

	}

	public void addRecord(MedicalRecord record) {
		try {
			logger.debug("Adding record into queue ..");
			Instant instant = Instant.now();
			record.setTime(instant.toString());
			queue.add(record);

			// check batch size and flush if necessary
			if (queue.size() >= commitSize) {

				try {
					flushLock.lock();
					if (queue.size() >= commitSize) {
						flush();
					}
				} finally {
					flushLock.unlock();
				}
			}
		} catch (Exception ex) {
			logger.error("Failed to add record into queue. Reason :" + ex.getMessage());
		}
	}

	public void flush() {
		logger.debug("Flushing records ...");
		try {
			flushLock.lock();
			flushCondition.signal();
		} catch (Exception ex) {
			logger.error("Failed to flush records. Reason :" + ex.getMessage());
		} finally {
			flushLock.unlock();
		}
	}

	public Boolean isPendingRecord(String id) {
		try {
			flushLock.lock();
			Iterator<MedicalRecord> iterator = queue.iterator();
			// Returns an iterator over the elements
			while (iterator.hasNext()) {
				if (id.equals(iterator.next().getId())) {
					return true;
				}
			}
			return false;
		} catch (Exception ex) {
			logger.error("Failed to get record status from batch transaction executor. Reason :" + ex.getMessage());
			return false;
		} finally {
			flushLock.unlock();
		}
	}
	
}
