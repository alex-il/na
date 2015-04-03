package sample.coherence.scheduler;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import sample.coherence.data.StatusEvent;

//	import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.common.logging.Logger;
import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.InvocationService;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.GuardSupport;
import com.tangosol.net.PriorityTask;
import com.tangosol.net.Guardian.GuardContext;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.net.partition.PartitionSet;
import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.GreaterEqualsFilter;
import com.tangosol.util.filter.LessEqualsFilter;
import com.tangosol.util.filter.PartitionedFilter;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalRemove;

@SuppressWarnings({ "unchecked", "serial" })
public class SchedulerService extends AbstractInvocable implements
PriorityTask, PortableObject  {

	private Long m_delay = 1l;
	private String memberName;

	public SchedulerService() {
	}

	public SchedulerService(Long delay) {
		m_delay = delay;
		memberName = ManagementFactory.getRuntimeMXBean().getName();

		System.out.println("");
	}

	public void init(InvocationService psrvc) {
		
		System.out.println("in init : "+ SchedulerService.class.toString());
		// srvc = psrvc;
	}

	public void run() {
		NamedCache cache = CacheFactory.getCache(StatusEvent.EVENTS_CACHE);

		System.out.println("in run: "+ SchedulerService.class.toString());
		// useContinousQuery(cache);
		usePolling(cache);

	}

	public void readExternal(PofReader reader) throws IOException {

		m_delay = reader.readLong(0);

	}

	public void writeExternal(PofWriter writer) throws IOException {

		writer.writeLong(0, m_delay);
	}

	private void usePolling(NamedCache cache) {
		boolean doit = true;
	//	Logger.log(LOG_ALWAYS, "SchedulerService Started on member:" + memberName + " : delay=" + m_delay + " : with Polling");

		GuardContext ctx = GuardSupport.getThreadContext();
		if (ctx != null) {
			SchedulerGuardian guard = new SchedulerGuardian();
			GuardContext klg = ctx.getGuardian().guard(guard);
			GuardSupport.setThreadContext(klg);
			// disable current context
			ctx.heartbeat(TimeUnit.DAYS.toMillis(365));
		}

		try {
			while (doit) {
				//LessEqualsFilter
				//GreaterEqualsFilter
				Filter filter = new  LessEqualsFilter("getTtl",
						System.currentTimeMillis());
				SchedulerProcessor agent = new SchedulerProcessor();
				Map result = cache.invokeAll(filter,
						(InvocableMap.EntryProcessor) agent);
				Thread.sleep(m_delay);
			}
		} catch (Exception ex) {

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

		Filter filter = new LessEqualsFilter("getTtl",
				System.currentTimeMillis());

		MapListener listener = new MapListener() {

			@Override
			public void entryUpdated(MapEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("Entry Updated in QueryCache  "
						+ arg0.getKey());

			}

			@Override
			public void entryInserted(MapEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("Entry Inserted into QueryCache "
						+ arg0.getKey());

				ConditionalRemove processor = new ConditionalRemove(
						new AlwaysFilter(), false);
				CacheFactory.getCache(StatusEvent.EVENTS_CACHE).invoke(
						arg0.getKey(), processor);
			}

			@Override
			public void entryDeleted(MapEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("Entry Delete from QueryCache "
						+ arg0.getKey());

			}
		};

		ContinuousQueryCache cacheOpenTrades = new ContinuousQueryCache(cache,
				filter, listener);
	}
	
	public long getExecutionTimeoutMillis(){
		System.out.println("getExecutionTimeoutMillis");
		return TIMEOUT_NONE;
	}

}
