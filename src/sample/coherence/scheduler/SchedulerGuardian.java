package sample.coherence.scheduler;

import java.util.Collection;

import com.tangosol.net.Guardable;
import com.tangosol.net.Guardian.GuardContext;

public class SchedulerGuardian implements Guardable {

    GuardContext context;

    public SchedulerGuardian() {
   }

    @Override
    public GuardContext getContext() {
        return context;
    }

    @Override
    public void setContext(GuardContext context) {
        this.context = context;
    }

    @Override
    public void recover() {
        System.out.println("got RECOVER signal");
        context.heartbeat();
    }

    @Override
    public void terminate() {
        System.out.println("got TERMINATE signal");
    }

    @Override
    public String toString() {
        return "KeyLoaderGuard:" ;
    }
}
