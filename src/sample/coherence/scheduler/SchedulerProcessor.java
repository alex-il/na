package sample.coherence.scheduler;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;

import sample.coherence.data.StatusEventValue;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.processor.AbstractProcessor;

public class SchedulerProcessor extends AbstractProcessor implements PortableObject, EntryProcessor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4062992297006021962L;

	@Override
	public Object process(com.tangosol.util.InvocableMap.Entry entry) {
		StatusEventValue event = (StatusEventValue) entry.getValue();
		Long ttl = event.getTtl();
		String memberName = ManagementFactory.getRuntimeMXBean().getName();
		long now = System.currentTimeMillis();
		long status = event.getMessageStatus();
		long beforeDelay = event.getBeforeDealy();

		System.out.println("==========================="); 
		System.out.println("Now: " + new Date(now)); 
		System.out.println("TTL: " + new Date(ttl)); 
		System.out.println("beforeDelay: "+ beforeDelay); 
		System.out.println("Removing Entry:" + entry.getKey() 
				+ " Status=" + status 
				+ " TTL=" + ttl 
				+ " beforeDelay=" + beforeDelay 
				+ " Now=" + now 
				+ " delayed:"	+ (now - beforeDelay) 
				+ " Member=" + memberName);
		System.out.println("==========================="); 
		entry.remove(false);
		return null;
	}

	@Override
	public void readExternal(PofReader pofreader) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeExternal(PofWriter pofwriter) throws IOException {
		// TODO Auto-generated method stub
	}
}
