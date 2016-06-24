package ghcvm.runtime;

import ghcvm.runtime.stg.StgTSO;
import ghcvm.runtime.stg.Capability;
import static ghcvm.runtime.Rts.HaskellResult;
import static ghcvm.runtime.RtsMessages.barf;
import static ghcvm.runtime.RtsScheduler.blockedQueue;
import static ghcvm.runtime.RtsScheduler.sleepingQueue;
import static ghcvm.runtime.RtsScheduler.emptySleepingQueue;
import static ghcvm.runtime.RtsScheduler.schedulerState;
import static ghcvm.runtime.RtsScheduler.SchedulerState.SCHED_INTERRUPTING;
import static ghcvm.runtime.stg.Capability.mainCapability;

public class RtsIO {
    public static void awaitEvent(boolean wait) {
        // TODO: Complete implementation
        do {
            long now = System.currentTimeMillis();
            if (wakeUpSleepingThreads(now)) {
                return;
            }
            for (StgTSO tso: blockedQueue) {
                switch (tso.whyBlocked) {
                    case BlockedOnRead:
                        continue;
                    case BlockedOnWrite:
                        continue;
                    default:
                        barf("AwaitEvent");
                }
            }

            long timeout;
            if (!wait) {
                timeout = -1;
            } else if (!emptySleepingQueue()) {
                timeout = sleepingQueue.peek().wakeTime - now;
            } else {
                timeout = 0;
            }

            while (timeout != 0 /* TODO: Implement NIO Selector */ ) {
                //select loop

                // process signals
                if (schedulerState.compare(SCHED_INTERRUPTING) >= 0) return;
                wakeUpSleepingThreads(System.currentTimeMillis());
                if (!mainCapability.emptyRunQueue()) return;
            }

            StgTSO prev = null;
            for (StgTSO tso: blockedQueue) {
                switch (tso.whyBlocked) {
                    case BlockedOnRead:
                        continue;
                    case BlockedOnWrite:
                        continue;
                    default:
                        barf("awaitEvent");
                }
            }
        } while (true);
    }

    public static boolean wakeUpSleepingThreads(long now) {
        // TODO: Implement
        return false;
    }

    public static void ioManagerStart() {
        // Check file descriptors or SelectKeys
        Capability cap = Rts.lock();
        HaskellResult result = cap.ioManagerStart();
        cap = result.cap;
        Rts.unlock(cap);
    }
}
