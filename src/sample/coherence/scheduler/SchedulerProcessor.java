package sample.coherence.scheduler;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.processor.AbstractProcessor;
import sample.coherence.data.*;

public class SchedulerProcessor extends AbstractProcessor implements PortableObject, EntryProcessor{

		@Override
		public Object process(com.tangosol.util.InvocableMap.Entry entry) {
			
			StatusEvent event = (StatusEvent) entry.getValue();
			
			Long ttl = event.getTtl();
			
			String memberName = ManagementFactory.getRuntimeMXBean().getName();
			long now = System.currentTimeMillis();
			long status = event.getMessageStatus();
			long beforeDelay=event.getBeforeDealy();
			
			System.out.println("Member=" + memberName +  " Removing Entry:" + entry.getKey() + " Status=" + status +  " TTL=" + ttl + " beforeDelay="+beforeDelay + " Now=" + now + " delayed:" + (now - beforeDelay));
        /*    
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			entry.remove(false);
           // System.out.println("Member=" + memberName +  " Removed Entry:" + entry.getKey() + " Status=" + status +  " TTL=" + ttl + " Now=" + now + " delayed:" + (now - ttl));
			
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

