package sample.coherence.failover;

import java.lang.reflect.Constructor;

import sample.coherence.scheduler.SchedulerService;
import sample.coherence.scheduler.test.Test.MyObserver;

import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.InvocationService;
import com.tangosol.net.MemberEvent;
import com.tangosol.net.MemberListener;

public class InvocationMemberListener implements MemberListener {

	private Long sleeptime = 100l;

	public Long getSleeptime() {
		return sleeptime;
	}

	public void setSleeptime(Long sleeptime) {
		this.sleeptime = sleeptime;
	}

	public InvocationMemberListener(Long sleep) {
		super();
		this.sleeptime = sleep;
	}

	@Override
	public void memberJoined(MemberEvent arg0) {
		System.out.println("+++memberJoined");

		System.out.println("starting to add service for delay  !!!");

		Long sleeptime = getSleeptime();

		runScheduler(sleeptime, arg0);
		System.out.println("finished to add service for delay  !!!");
	}

	@Override
	public void memberLeaving(MemberEvent arg0) {
		System.out.println("--->>>memberLeaving");
		// TODO Auto-generated method stub

	}

	@Override
	public void memberLeft(MemberEvent arg0) {
		System.out.println("-------memberLeft");

	}

	private static void runScheduler(long sleeptime, MemberEvent arg0) {
		try {
			InvocationService iService = (InvocationService) arg0.getService();
			Constructor<?> serviceConstructor = SchedulerService.class.getConstructor(Long.class);
			MyObserver observer = new MyObserver();
			Long delay = sleeptime;
			iService.execute((SchedulerService) serviceConstructor.newInstance(delay), null, observer);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("------------------------\n\n\n\n");
		}
	}

}
