package sample.coherence.failover;

import sample.coherence.scheduler.SchedulerService;

import com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.InvocationService;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.MemberEvent;
import com.tangosol.net.MemberListener;

public class InvocationMemberListener implements MemberListener {
	private static Long sleep = 100l;

	public Long getSleep() {
		return sleep;
	}

	public void setSleep(Long sleeptime) {
		this.sleep = sleeptime;
	}

	public InvocationMemberListener(Long sleeptime) {
		super();
		this.sleep = sleeptime;
	}

	@Override
	public void memberJoined(MemberEvent arg0) {
		System.out.println("	~~~	memberJoined. Starting delay=" + sleep);
		invoke(arg0);
		System.out.println("	~~~	memberJoined. Ended delay=" + sleep);
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

	private static void invoke( MemberEvent memberEvent) {
		try {
			System.out.println("~~~ InvocationMemberListener . runScheduler sleeptime:" + sleep);
			InvocationService iService = (InvocationService) memberEvent.getService();
			System.out.println("\n\n\n\n~~~ before SchedulerService . member id:" + memberEvent.getMember().getId());
			// iService.execute(new SchedulerService(sleeptime), null, new OsbObserver());
			iService.execute(new SchedulerService(sleep), null, null);
			System.out.println("\n\n\n\n~~~ after SchedulerService . member id:" + memberEvent.getMember().getId());
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
