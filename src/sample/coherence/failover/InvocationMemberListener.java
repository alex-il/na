package sample.coherence.failover;

import sample.coherence.scheduler.SchedulerService;

import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.InvocationService;
import com.tangosol.net.InvocationObserver;
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
		Long sleeptime = getSleeptime();
		System.out.println("	~~~	memberJoined. Starting delay="+sleeptime);
		runScheduler(sleeptime, arg0);
		System.out.println("	~~~	memberJoined. Ended delay="+sleeptime);
	}

	@Override
	public void memberLeaving(MemberEvent arg0) {
		System.out.println("~~~memberLeaving");
		// TODO Auto-generated method stub

	}

	@Override
	public void memberLeft(MemberEvent arg0) {
		System.out.println("~~~memberLeft");
	}

	private static void runScheduler(long sleeptime, MemberEvent memberEvent) {
		try {
			InvocationService iService = (InvocationService) memberEvent.getService();
			OsbObserver observer = new OsbObserver();
			iService.execute(new SchedulerService(sleeptime), null, observer);
		} catch (Exception ex) {
			System.err.println("~~~ -------runScheduler.exception----- ");
			ex.printStackTrace();
		}
	}

	public static class OsbObserver implements InvocationObserver {
		@Override
		public void invocationCompleted() {
			System.out.println("... OsbObserver invocationCompleted");
		}

		@Override
		public void memberCompleted(com.tangosol.net.Member member, Object arg1) {
			System.out.println("...OsbObserver Completed");
		}

		@Override
		public void memberFailed(com.tangosol.net.Member member, Throwable arg1) {
			System.out.println("...OsbObserver Failed");
		}

		@Override
		public void memberLeft(com.tangosol.net.Member member) {
			System.out.println("...OsbObserver Left");
		}
	}
	
}
