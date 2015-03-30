package sample.coherence.scheduler;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import sample.coherence.data.StatusEvent;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.processor.AbstractProcessor;

public class SchedulerProcessor extends AbstractProcessor implements PortableObject, EntryProcessor {

	@Override
	public Object process(com.tangosol.util.InvocableMap.Entry entry) {

		StatusEvent event = (StatusEvent) entry.getValue();

		Long ttl = event.getTtl();

		String memberName = ManagementFactory.getRuntimeMXBean().getName();
		long now = System.currentTimeMillis();
		long status = event.getMessageStatus();

		System.out.println("Member=" + memberName + " Removed Entry:" + entry.getKey() + " Status=" + status + " TTL=" + ttl
		    + " Now=" + now + " delayed:" + (now - ttl));

		entry.remove(false);

		return null;
	}

	@Override
	public void readExternal(PofReader pofreader) throws IOException {

	}

	@Override
	public void writeExternal(PofWriter pofwriter) throws IOException {
		// TODO Auto-generated method stub

	}
}
