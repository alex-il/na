package sample.coherence.failover;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sample.coherence.data.StatusEvent;
import sample.coherence.data.StatusEventKey;

import com.oracle.coherence.common.backingmaplisteners.AbstractMultiplexingBackingMapListener;
import com.oracle.coherence.common.backingmaplisteners.Cause;
import com.oracle.coherence.common.logging.Logger;
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
		Logger.log(LOG_ALWAYS, "BackingMapListener Inititated: Sleeptime=" + sleepTime);
	}

	@Override
	public void onBackingMapEvent(MapEvent mapEvent, Cause cause) {

	}

	@Override
	public void entryDeleted(MapEvent evt) {
		// TODO Auto-generated method stub
		super.entryDeleted(evt);
	}

	@Override
	public void entryInserted(MapEvent evt) {
		// TODO Auto-generated method stub
		processevent(evt);
		// super.entryInserted(evt);
	}

	@Override
	public void entryUpdated(MapEvent evt) {
		// TODO Auto-generated method stub

		processevent(evt);
		// super.entryUpdated(evt);
	}

	private void processevent(MapEvent mapEvent) {

		final StatusEvent event = (StatusEvent) getNewValue(mapEvent);

		final StatusEventKey key = (StatusEventKey) getKey(mapEvent);

		// final int oper = mapEvent.getId();

		final long sleep = this.sleepTime;

		executer.execute(new Runnable() {

			public void run() {
				try {
					// System.out.println("BackingMap: Thread:" +
					// Thread.currentThread().getId() + ":" +
					// MapEvent.getDescription(oper) + " MsgId=" + event.toString() );
					// String clusterMemberName =
					// ManagementFactory.getRuntimeMXBean().getName();
					NamedCache cache = CacheFactory.getCache(StatusEvent.EVENTS_CACHE);
					cache.invoke(key, new FailoverProcessor(event.getMessageStatus(), sleep));

				} catch (Throwable e) {
					System.out.println("Error:" + e.getMessage());

					e.printStackTrace();
				}

			}
		});

	}

	public Object getKey(MapEvent mapEvent) {
		return getBackingMapManagerContext().getKeyFromInternalConverter().convert(mapEvent.getKey());
	}

	public Object getNewValue(MapEvent mapEvent) {
		return getBackingMapManagerContext().getValueFromInternalConverter().convert(mapEvent.getNewValue());
	}

	public BackingMapManagerContext getBackingMapManagerContext() {
		return backingMapManagerContext;
	}
}