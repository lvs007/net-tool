package org.tron.net.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * 可以设置超时时间的定时任务，超过指定超时时间之后，如果任务还没有执行完，那么会中断当前的执行
 */
@Slf4j
public class ScheduledUtil {

  private final ScheduledExecutorService scheduledExecutorService = Executors
      .newScheduledThreadPool(10);

  private final BlockingDeque<Task> taskList = new LinkedBlockingDeque<Task>();

  private static volatile ScheduledUtil scheduledUtil;

  private long sleepTime = 1;

  private ScheduledUtil() {
    init();
  }

  private ScheduledUtil(long sleepTime) {
    this.sleepTime = sleepTime;
    init();
  }

  public static ScheduledUtil getInstance() {
    if (scheduledUtil == null) {
      synchronized (ScheduledUtil.class) {
        if (scheduledUtil == null) {
          scheduledUtil = new ScheduledUtil();
        }
      }
    }
    return scheduledUtil;
  }

  public static ScheduledUtil getInstance(long sleepTime) {
    if (scheduledUtil == null) {
      synchronized (ScheduledUtil.class) {
        if (scheduledUtil == null) {
          scheduledUtil = new ScheduledUtil(sleepTime);
        }
      }
    }
    return scheduledUtil;
  }

  public abstract static class TimeOutRunnable implements Runnable {

    private volatile boolean finish;

    @Override
    public void run() {
      finish = false;
      timeoutRun();
      finish = true;
    }

    public abstract void timeoutRun();

    public boolean isFinish() {
      return finish;
    }

    public TimeOutRunnable setFinish(boolean finish) {
      this.finish = finish;
      return this;
    }
  }

  private class Task {

    private TimeOutRunnable command;
    private long initialDelay;
    private long delay;
    private long timeout;
    private long startTime;
    private ScheduledFuture scheduledFuture;

    public Task(TimeOutRunnable command, long initialDelay, long delay, long timeout,
        long startTime, ScheduledFuture scheduledFuture) {
      this.command = command;
      this.initialDelay = initialDelay;
      this.delay = delay;
      this.timeout = timeout;
      this.startTime = startTime;
      this.scheduledFuture = scheduledFuture;
    }

    public TimeOutRunnable getCommand() {
      return command;
    }

    public long getInitialDelay() {
      return initialDelay;
    }

    public long getDelay() {
      return delay;
    }

    public long getTimeout() {
      return timeout;
    }

    public long getStartTime() {
      return startTime;
    }

    public Task setStartTime(long startTime) {
      this.startTime = startTime;
      return this;
    }

    public ScheduledFuture getScheduledFuture() {
      return scheduledFuture;
    }

  }

  public void scheduleWithFixedDelay(TimeOutRunnable command,
      long initialDelay, long delay, long timeout) {
    logger.info("add to ScheduledThreadPool");
    ScheduledFuture scheduledFuture = scheduledExecutorService
        .scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.MILLISECONDS);
    taskList.add(new Task(command, initialDelay, delay, timeout,
        System.currentTimeMillis() + initialDelay, scheduledFuture));
  }

  private void init() {
    new Thread(() -> {
      while (true) {
        ThreadUtil.sleep(sleepTime);
        List<Task> removeTask = new ArrayList<>();
        for (Task task : taskList) {
          long spendTime = System.currentTimeMillis() - task.getStartTime();
          try {
            if (!task.getCommand().isFinish() && spendTime > task.getTimeout()) {
              //kill time task,and add to agent
              logger.error("task time out, Delay: {}, startTime: {}, timeout: {}, spendTime: {}",
                  task.getDelay(), task.getStartTime(), task.getTimeout(), spendTime);
              task.getScheduledFuture().cancel(true);
              removeTask.add(task);
              scheduleWithFixedDelay(task.getCommand(), task.getInitialDelay(), task.getDelay(),
                  task.getTimeout());
            } else if (task.getCommand().isFinish()) {
              task.setStartTime(System.currentTimeMillis());
            }
          } catch (Throwable e) {
            logger.error("", e);
          }
        }
        taskList.removeAll(removeTask);
      }
    }).start();
  }

  public static void main(String[] args) {
    ScheduledUtil.getInstance().scheduleWithFixedDelay(new TimeOutRunnable() {
      @Override
      public void timeoutRun() {
        ThreadUtil.sleep(1500);
        logger.info("finishxxxxxxx");
      }
    }, 10, 1000, 2000);
  }

}
