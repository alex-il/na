package sample.coherence.scheduler;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sample.coherence.data.StatusEventValue;

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
import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.filter.LessEqualsFilter;

public class SchedulerService extends AbstractInvocable implements PriorityTask, PortableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4748169266801114529L;
	private Long m_delay = 1l;
	private String memberName;

	public SchedulerService() {
	}

	public SchedulerService(Long delay) {
		m_delay = delay;
		memberName = ManagementFactory.getRuntimeMXBean().getName();
		System.out.println("SchedulerService(Long delay):" + delay + " memberName: " + memberName);
	}

	public void init(InvocationService is) {
		System.out.println("init invService name: " + is.get_Name());
	}

	public void run() {
		NamedCache cache = CacheFactory.getCache(StatusEventValue.EVENTS_CACHE);
		System.out.println("in run: " + SchedulerService.class.toString());
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
				// LessEqualsFilter
				// GreaterEqualsFilter
				Filter filter = new LessEqualsFilter("getTtl", System.currentTimeMillis() );
				SchedulerProcessor agent = new SchedulerProcessor();
				Map result = cache.invokeAll(filter, (InvocableMap.EntryProcessor) agent);
				if (m_delay != null)
					Thread.sleep(m_delay);
			}
		} catch (Exception ex) {
			System.err.println("--- usePolling.except");
			ex.printStackTrace();
		}

		finally {
			System.err.println("SchedulerService.finally() ");
			if (ctx != null) {
				GuardContext klg = GuardSupport.getThreadContext();
				GuardSupport.setThreadContext(ctx);
				klg.release();
				// reenable current context
				ctx.heartbeat();
			}
		}
	}

	public long getExecutionTimeoutMillis() {
		System.out.println(" ??? getExecutionTimeoutMillis: " + TIMEOUT_NONE);
		return TIMEOUT_NONE;
	}

}
