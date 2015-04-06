package sample.coherence.failover;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sample.coherence.data.StatusEventKey;
import sample.coherence.data.StatusEventValue;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

public class StatusEventListener implements MapListener {

	private ExecutorService executer;
	private long sleepTime = 1;

	public StatusEventListener(long sleep) {
		executer = Executors.newFixedThreadPool(20);
		sleepTime = sleep;
	}

	public long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	@Override
	public void entryDeleted(MapEvent mapEvent) {
		// TODO Auto-generated method stub
	}

	@Override
	public void entryInserted(MapEvent mapEvent) {
		processEvent(mapEvent);

	}

	@Override
	public void entryUpdated(MapEvent mapEvent) {
		processEvent(mapEvent);

	}

	private void processEvent(MapEvent mapEvent) {
		final StatusEventValue event = (StatusEventValue) mapEvent.getNewValue();
		final StatusEventKey key = (StatusEventKey) mapEvent.getKey();
		final int oper = mapEvent.getId();
		final long sleep = this.sleepTime;

		executer.execute(new Runnable() {
			public void run() {
				try {
					String memberName = ManagementFactory.getRuntimeMXBean().getName();
					System.out.println(
							"processEvent. Sleep: " + sleep
							+ " MsgId=" + event.toString() 
							+ " Thread:" + Thread.currentThread().getId() 
					    + MapEvent.getDescription(oper) 
					    + ", member"+memberName);
					
					NamedCache cache = CacheFactory.getCache(StatusEventValue.EVENTS_CACHE);
					cache.invoke(key, new FailoverProcessor(event.getMessageStatus(), sleep));
				} catch (Throwable e) {
					System.out.println("processEvent.exception:" + e.getMessage());
					e.printStackTrace();
				}

			}
		});

	}

}
