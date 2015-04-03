package sample.coherence.scheduler;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sample.coherence.data.StatusEvent;

import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.InvocationService;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.GuardSupport;
import com.tangosol.net.Guardian.GuardContext;
import com.tangosol.net.NamedCache;
import com.tangosol.net.PriorityTask;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.LessEqualsFilter;
import com.tangosol.util.processor.ConditionalRemove;

//	import com.oracle.coherence.common.logging.Logger;

public class SchedulerService extends AbstractInvocable implements PriorityTask, PortableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8701218282536801888L;
	private Long delay = 1l;

	public SchedulerService() {
	}

	public SchedulerService(Long delay) {
		this.delay = delay;
		ManagementFactory.getRuntimeMXBean().getName();
		System.out.println("ctor SchedulerService(Long delay):"+ delay);
	}

//	public void init(InvocationService psrvc) {
//		System.out.println("in init() : " + SchedulerService.class.toString());
//	}

	public void run() {
		NamedCache cache = CacheFactory.getCache(StatusEvent.EVENTS_CACHE);
		// useContinousQuery(cache);
		usePolling(cache);
	}

	public void readExternal(PofReader reader) throws IOException {
		delay = reader.readLong(0);
	}

	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeLong(0, delay);
	}

	private void usePolling(NamedCache cache) {
		boolean doit = true;
		// Logger.log(LOG_ALWAYS, "SchedulerService Started on member:" + memberName
		// + " : delay=" + m_delay + " : with Polling");

		GuardContext ctx = GuardSupport.getThreadContext();
		if (ctx != null) {
			SchedulerGuardian guard = new SchedulerGuardian();
			GuardContext klg = ctx.getGuardian().guard(guard);
			GuardSupport.setThreadContext(klg);
			// disable current context
			ctx.heartbeat(TimeUnit.DAYS.toMillis(365));
		} else {
			System.err.println("Bad context == null");
		}

		try {
			while (true) {
				// LessEqualsFilter
				// GreaterEqualsFilter
				Filter filter = new LessEqualsFilter("getTtl", System.currentTimeMillis());
				SchedulerProcessor agent = new SchedulerProcessor();
				Map result = cache.invokeAll(filter, (InvocableMap.EntryProcessor) agent);
				Thread.sleep(200l);
			}
		} catch (Exception ex) {
			System.err.println("error:11111");
			ex.printStackTrace();
		}

		finally {
			if (ctx != null) {
				GuardContext klg = GuardSupport.getThreadContext();
				GuardSupport.setThreadContext(ctx);
				klg.release();
				// reenable current context
				ctx.heartbeat();
			}
		}

	}

	private void useContinousQuery(NamedCache cache) {
		Filter filter = new LessEqualsFilter("getTtl", System.currentTimeMillis());
		MapListener listener = new MapListener() {
			@Override
			public void entryUpdated(MapEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("Entry Updated in QueryCache  " + arg0.getKey());
			}

			@Override
			public void entryInserted(MapEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("Entry Inserted into QueryCache " + arg0.getKey());

				ConditionalRemove processor = new ConditionalRemove(new AlwaysFilter(), false);
				CacheFactory.getCache(StatusEvent.EVENTS_CACHE).invoke(arg0.getKey(), processor);
			}

			@Override
			public void entryDeleted(MapEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("Entry Delete from QueryCache " + arg0.getKey());

			}
		};

		ContinuousQueryCache cacheOpenTrades = new ContinuousQueryCache(cache, filter, listener);
	}

	public long getExecutionTimeoutMillis() {
		System.out.println("getExecutionTimeoutMillis");
		return TIMEOUT_NONE;
	}

}
