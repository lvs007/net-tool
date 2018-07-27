package org.tron.net.services;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tron.net.common.utils.ScheduledUtil;
import org.tron.net.common.utils.ScheduledUtil.TimeOutRunnable;
import org.tron.net.common.utils.ThreadUtil;


@Slf4j
@Service
public class NetAssitUtil {

  private ScheduledExecutorService detectExecutor = Executors.newSingleThreadScheduledExecutor();

  private ScheduledExecutorService statisticsExecutor = Executors
      .newSingleThreadScheduledExecutor();

  @Autowired
  private NodeDetection nodeDetection;

  public void detectAllNode() {
    nodeDetection.beforeDetect();
    nodeDetection.doDetect();
  }

  public void statisticsAllNode() {
    nodeDetection.statisticsAllNode();
  }

  @PostConstruct
  public void detectWork() {
    logger.info("Begin detectWork");
    while (true) {
      if (nodeDetection.getMessageSender() != null) {
        break;
      } else {
        ThreadUtil.sleep(300);
      }
    }
    //
    detectExecutor.scheduleWithFixedDelay(() -> {
      logger.info("当前时间：{}", System.currentTimeMillis() / 1000);
      try {
        detectAllNode();
      } catch (Throwable t) {
        logger.error("Exception in log worker", t);
      }
    }, 10, 20, TimeUnit.SECONDS);

    logger.info("End detectWork getAllNode: {}", nodeDetection.getAllNode().size());
  }
}