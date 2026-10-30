package space.impact.api.net;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Forge 1.7.10 network callbacks run off-thread; apply world/player updates on the server tick. */
public final class ServerTasks {
    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    public static void enqueue(Runnable task) { TASKS.add(task); }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        for (int i = 0; i < 1024; i++) {
            Runnable task = TASKS.poll();
            if (task == null) return;
            task.run();
        }
    }
}
