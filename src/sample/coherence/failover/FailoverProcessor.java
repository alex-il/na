package sample.coherence.failover;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import sample.coherence.data.StatusEvent;
import sample.coherence.data.StatusEventKey;

import com.oracle.coherence.common.logging.Logger;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.processor.AbstractProcessor;

public class FailoverProcessor extends AbstractProcessor implements
		PortableObject, EntryProcessor {

	private Long sleepTime = 5000l;
	private String memberName;
	private Long status;
	
	public FailoverProcessor() {

	}

	public FailoverProcessor(Long status, Long sleep) {
		sleepTime = sleep;
		this.status = status;
	}

	@Override
	public Object process(com.tangosol.util.InvocableMap.Entry entry) {


		memberName = ManagementFactory.getRuntimeMXBean().getName();
		StatusEvent event = (StatusEvent) entry.getValue();
		if (event == null || event.getMessageStatus() != status)
		{
//			Logger.log(LOG_DEBUG, "FailoverProcessor: Entry " + entry.getKey() + " has changed status - exiting processor : Expected=" + status + " Received=" + event.getMessageStatus());
			return null;
		}

		// return processWithTransaction(entry);
		return processPartitionTx(entry);

	}

	@Override
	public void readExternal(PofReader pofreader) throws IOException {
		sleepTime = pofreader.readLong(0);
		status = pofreader.readLong(1);
	}

	@Override
	public void writeExternal(PofWriter pofwriter) throws IOException {
		// TODO Auto-generated method stub
		pofwriter.writeLong(0, sleepTime);
		pofwriter.writeLong(1, status);

	}

	private Object processPartitionTx(com.tangosol.util.InvocableMap.Entry entry) {

		StatusEvent e = (StatusEvent) entry.getValue();
		StatusEventKey key = (StatusEventKey) entry.getKey();
		if (e == null){
			Logger.log(LOG_INFO, "FailOver Procesor: entry is null");
			return null;
		}
		long status = e.getMessageStatus();
		Logger.log(LOG_DEBUG, "Member:" + memberName + " : Thread:" + Thread.currentThread().getId() + " : entry:"
				+ ((StatusEventKey) entry.getKey()).getMessageId()
				+ " Status=" + status + " : started");

		BinaryEntry bentry = (BinaryEntry) entry;
		BackingMapManagerContext ctx = bentry.getContext();

		if (status == 2) {
			// BackingMapManagerContext ctx = (BackingMapManagerContext)
			// bentry.getBackingMapContext();
			StatusEventKey childKey = new StatusEventKey(key.getMessageId(),
					key.getMessageId() + "-child");

			for (int n = 1; n <= 10; n++) {
				BinaryEntry childEntry = (BinaryEntry) bentry
						.getContext()
						.getBackingMapContext(StatusEvent.EVENTS_CACHE)
						.getBackingMapEntry(
								ctx.getKeyToInternalConverter().convert(
										childKey));
				childEntry.setValue(new StatusEvent(childKey.getMessageId(),
						(String) childKey.getAssociatedKey(), 9, System
								.currentTimeMillis() + 100000));
			}
			try {
				Thread.sleep(sleepTime);
			} catch (Exception ex) {

			}
		} else {

			if (e.getMessageStatus() < 5) {
				e.setMessageStatus(status + 1);
				entry.setValue(e);
			} else {
				
				Logger.log(LOG_DEBUG, "Failover Processor: Removing Entry;" + e.toString());
				entry.remove(false);
			}
		}

		Logger.log(LOG_DEBUG, "Member:" + memberName + " : entry:"
				+ ((StatusEventKey) entry.getKey()).getMessageId()
				 + " Status=" + status + " : Completed");

		return true;

	}

}
