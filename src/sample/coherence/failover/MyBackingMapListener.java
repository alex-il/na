package sample.coherence.failover;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sample.coherence.data.StatusEventKey;
import sample.coherence.data.StatusEventValue;

import com.oracle.coherence.common.backingmaplisteners.AbstractMultiplexingBackingMapListener;
import com.oracle.coherence.common.backingmaplisteners.Cause;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;

public class MyBackingMapListener extends AbstractMultiplexingBackingMapListener {

	private BackingMapManagerContext backingMapManagerContext;
	private ExecutorService executer;
	private long sleepTime = 1;

	public MyBackingMapListener(BackingMapManagerContext context, Long sleep) {
		super(context);
		this.backingMapManagerContext = context;
		executer = Executors.newFixedThreadPool(20);
		sleepTime = sleep;
		System.out.println(" BackingMapListener CTOR: Sleeptime=" + sleepTime);
	}

	@Override
	public void onBackingMapEvent(MapEvent mapEvent, Cause cause) {

	}

	@Override
	public void entryDeleted(MapEvent evt) {
//		super.entryDeleted(evt);
		processevent(evt);
	}

	@Override
	public void entryInserted(MapEvent evt) {
		// super.entryInserted(evt);
		processevent(evt);
	}

	@Override
	public void entryUpdated(MapEvent evt) {
		// super.entryUpdated(evt);
		processevent(evt);
	}

	private void processevent(MapEvent mapEvent) {
		final StatusEventValue event = getEventValue(mapEvent);
		final StatusEventKey key = getEventKey(mapEvent);
		final long sleep = this.sleepTime;
		final int oper = mapEvent.getId();
		executer.execute(new Runnable() {
			public void run() {
				try {
					System.out.println(" ===== MyBackingMapListener. ===  oper:" + oper);
					NamedCache cache = CacheFactory.getCache(StatusEventValue.EVENTS_CACHE);
					cache.invoke(key, new FailoverProcessor(event.getMessageStatus(), sleep));
				} catch (Throwable e) {
					System.err.println("Error:" + e.getMessage());
					e.printStackTrace();
				}
			}
		});

	}

	public StatusEventKey getEventKey(MapEvent mapEvent) {
		return (StatusEventKey) getBackingMapManagerContext().getKeyFromInternalConverter().convert(mapEvent.getKey());
	}

	public StatusEventValue getEventValue(MapEvent mapEvent) {
		return (StatusEventValue) getBackingMapManagerContext().getValueFromInternalConverter().convert(mapEvent.getNewValue());
	}

	public BackingMapManagerContext getBackingMapManagerContext() {
		return backingMapManagerContext;
	}
}