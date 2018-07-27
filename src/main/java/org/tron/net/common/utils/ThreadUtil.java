package org.tron.net.common.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadUtil {

  public static void sleep(long time){
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      logger.error("thread sleep error!",e);
    }
  }

}
