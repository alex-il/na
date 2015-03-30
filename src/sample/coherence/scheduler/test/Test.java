package sample.coherence.scheduler.test;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import sample.coherence.scheduler.SchedulerProcessor;
import sample.coherence.scheduler.SchedulerService;
import sample.coherence.data.StatusEvent;
import sample.coherence.data.StatusEventKey;

import com.tangosol.coherence.component.util.safeService.SafeInvocationService;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

import sample.coherence.data.*;
import sample.coherence.failover.FailoverProcessor;
import sample.coherence.failover.StatusEventListener;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int maxentries = 2;
		Long sleeptime = 2000l;
		String test = "s"; // scheduler

		if (args.length > 0 && args[0] != null) {
			test = args[0];
		}

		NamedCache cache = CacheFactory.getCache(StatusEvent.EVENTS_CACHE);

		StatusEventListener listener = new StatusEventListener(sleeptime);
		cache.addMapListener(listener);

		if (test.equals("p")) {
			if (args.length > 1 && args[1] != null)
				maxentries = Integer.valueOf(args[1]);
			populateCache(cache, maxentries);
		} else if (test.equals("s")) {
			if (args.length > 1 && args[1] != null)
				sleeptime = Long.valueOf(args[1]);
			Scanner scanner = new Scanner(System.in);
			System.out.println("Starting Scheduler with " + sleeptime + "ms delay. Press to continue ...");
			scanner.nextLine();

			runScheduler(cache, sleeptime);
			try {
				Thread.sleep(1000000l);
			} catch (Exception ex) {
			}

		} else if (test.equals("f")) {
			if (args.length > 1 && args[1] != null)
				maxentries = Integer.valueOf(args[1]);
			if (args.length > 2 && args[1] != null)
				sleeptime = Long.valueOf(args[2]);
			listener.setSleepTime(sleeptime);
			runFailOver(cache, maxentries, sleeptime);
		} else {
			System.out.println("usage is messer-test.cmd action maxEntries Sleep time");
			System.out.println("action: p-polulate, s-scheduler, f-fialover");
			System.out.println("Example: 'messer-test.cmds 100' will start the scheduler with a 100 millisecond sleeptime");
		}

	}

	public static class MyObserver implements InvocationObserver {

		@Override
		public void invocationCompleted() {
			System.out.println("Completed");

		}

		@Override
		public void memberCompleted(com.tangosol.net.Member member, Object arg1) {
			System.out.println("Member Completed");

		}

		@Override
		public void memberFailed(com.tangosol.net.Member member, Throwable arg1) {
			System.out.println("Member Failed");
		}

		@Override
		public void memberLeft(com.tangosol.net.Member member) {
			System.out.println("Member Left");

		}

	}

	private static void populateCache(NamedCache cache, int maxentries) {

		cache.clear();

		System.out.println("Populating cache with " + maxentries + " entries - started ..");

		Map<StatusEventKey, StatusEvent> map = new HashMap<StatusEventKey, StatusEvent>();

		for (int i = 0; i < maxentries; i++) {
			StatusEvent e = new StatusEvent(String.valueOf(i), null, 1, (System.currentTimeMillis() + i * 1000));
			StatusEventKey key = new StatusEventKey(e);
			map.put(key, e);
		}
		cache.putAll(map);

		System.out.println("Populating Cache with " + maxentries + " entries - completed");
	}

	private static void runScheduler(NamedCache cache, long sleeptime) {

		try {
			populateCache(cache, 1000);
			SafeInvocationService iService = (SafeInvocationService) CacheFactory.getService("InvocationService");
			Constructor<?> serviceConstructor = SchedulerService.class.getConstructor(Long.class);

			MyObserver observer = new MyObserver();

			Long delay = sleeptime;
			iService.execute((SchedulerService) serviceConstructor.newInstance(delay), null, observer);
		} catch (Exception ex) {

		}
	}

	private static void runFailOver(NamedCache cache, int maxentries, long sleeptime) {
		try {
			System.out.println("Statung failover test with xaentries= " + maxentries + " sleeptime=" + sleeptime);
			long startTest = System.currentTimeMillis();

			int k = 1;
			String s = UUID.randomUUID().toString();

			while (maxentries > 0) {
				long start = System.currentTimeMillis();
				for (int j = 0; j < 100; j++) {
					StatusEvent e2 = new StatusEvent(s + "-" + k, null, 1, (System.currentTimeMillis() + 10000));
					cache.put(new StatusEventKey(e2), e2);
					k++;
					// print stattistics
					if (k % 1000 == 0) {
						long duration = (System.currentTimeMillis() - startTest) / 1000;
						double rate = k / duration;
						System.out.println("Created=" + k + " rate=" + rate);
					}
					maxentries--;
				}

				try {
					long duration = System.currentTimeMillis() - start;
					if (duration < 1000)
						Thread.sleep(1000 - duration);
				} catch (Exception ex) {

				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void runFailOver1(NamedCache cache, int maxentries, long sleeptime) {
		try {
			for (int j = 0; j < maxentries; j++) {
				StatusEvent e2 = new StatusEvent("Failover-" + j, null, 1, (System.currentTimeMillis() + 100000));
				cache.put(new StatusEventKey(e2), e2);

				FailoverProcessor agent = new FailoverProcessor(e2.getMessageStatus(), sleeptime);
				cache.invoke(new StatusEventKey(e2), agent);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
