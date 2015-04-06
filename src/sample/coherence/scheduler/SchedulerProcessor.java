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
		System.out.println("beforeDelay: "+ new Date(beforeDelay) ); 
		System.out.println("Removing Entry:" + entry.getKey() 
				+ " Status=" + status 
				+ " delayed:"	+ (now - beforeDelay)/1000.	+ "(sec), "
				+ " TTL=" + ttl 
				+ " beforeDelay=" + beforeDelay 
				+ " Now=" + now 
				+ "Member=" + memberName);
		System.out.println("==========================="); 
		try {
			System.out.println("\n\n\n\n processing alert ........... press kill now!!!!  "+ status +"\n\n\n\n\n");
	    Thread.sleep(60000l);
    } catch (InterruptedException e) {
    	System.err.println("==========================="); 
	    e.printStackTrace();
    }
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
