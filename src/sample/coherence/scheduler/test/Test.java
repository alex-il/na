package sample.coherence.scheduler.test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import sample.coherence.data.StatusEventKey;
import sample.coherence.data.StatusEventValue;
import sample.coherence.failover.FailoverProcessor;
import sample.coherence.failover.StatusEventListener;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class Test {
	final static long SECOND = 1000l;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int maxentries = 2;
		Long sleeptime = 2 * SECOND;
		String test = "p"; // scheduler

		if (args.length > 0 && args[0] != null) {
			test = args[0];
		}

		NamedCache cache = CacheFactory.getCache(StatusEventValue.EVENTS_CACHE);

		StatusEventListener listener = new StatusEventListener(sleeptime);
		// cache.addMapListener(listener) ;

		if (test.equals("p")) {
			if (args.length > 1 && args[1] != null){
				maxentries = Integer.valueOf(args[1]);
			}else{
				maxentries = 1000;
			}
			
			populateCache(cache, maxentries);
		
		} else if (test.equals("f")) {
			if (args.length > 1 && args[1] != null)
				maxentries = Integer.valueOf(args[1]);
			if (args.length > 2 && args[2] != null)
				sleeptime = Long.valueOf(args[2]);

			listener.setSleepTime(sleeptime);
			runFailOver(cache, maxentries, sleeptime);
		} else {
			System.out.println("usage is messer-test.cmd action maxEntries Sleep time");
			System.out.println("action: p-polulate, f-fialover");
			System.out.println("Example: 'messer-test.cmds 100' will start the scheduler with a 100 millisecond sleeptime");
		}

	}

	private static void populateCache(NamedCache cache, int maxentries) {
		cache.clear();
		System.out.println("==>> Populating cache #: " + maxentries);
		Map<StatusEventKey, StatusEventValue> map = new HashMap<StatusEventKey, StatusEventValue>();
		long now = System.currentTimeMillis();
		long delay = SECOND;
		long loopTtl = now + delay;
		long eventTTLFactor = 15 * SECOND;
		for (int i = 0; i < maxentries; i++) {
			StatusEventValue e = new StatusEventValue(String.valueOf(i), null, 1, loopTtl, now);
			StatusEventKey key = new StatusEventKey(e);
			map.put(key, e);
			System.out.println("INSERT, Key:" + key.toString() + " now:" + now + " TTL:" + e.getTtl() + " delay:"
			    + (e.getTtl() - now)/1000. +"(sec)");
			loopTtl = loopTtl + eventTTLFactor;
		}
		cache.putAll(map);
		System.out.println("<<== End Populating Cache #:" + maxentries);
	}


/**
 * 
 * @param cache
 * @param maxentries
 * @param sleeptime
 */
	private static void runFailOver(NamedCache cache, int maxentries, long sleeptime) {
		try {
			System.out.println("Statung failover test with xaentries= " + maxentries + " sleeptime=" + sleeptime);
			long startTest = System.currentTimeMillis();

			int k = 1;
			String s = UUID.randomUUID().toString();

			while (maxentries > 0) {
				long start = System.currentTimeMillis();
				for (int j = 0; j < 100; j++) {
					StatusEventValue e2 = new StatusEventValue(s + "-" + k, null, 1, (System.currentTimeMillis() + 10000),
					    System.currentTimeMillis());
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
				StatusEventValue e2 = new StatusEventValue("Failover-" + j, null, 1, (System.currentTimeMillis() + 100000),
				    System.currentTimeMillis());
				cache.put(new StatusEventKey(e2), e2);

				FailoverProcessor agent = new FailoverProcessor(e2.getMessageStatus(), sleeptime);
				cache.invoke(new StatusEventKey(e2), agent);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}



}
