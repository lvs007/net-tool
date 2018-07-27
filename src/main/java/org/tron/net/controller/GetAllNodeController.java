package org.tron.net.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tron.net.services.NodeDetection;
import org.tron.net.services.NodeHandler;

@Slf4j
@Controller
public class GetAllNodeController {

  @ResponseBody
  @RequestMapping("/getAllNode")
  public Object getAllNode() {
    HashMap<String, String> outcome = new HashMap<>();
    try {
      HashMap<String, NodeHandler> currentNetNode = NodeDetection.currentNetNode;
      for (Map.Entry<String, NodeHandler> entry : currentNetNode.entrySet()) {
        outcome.put(entry.getKey(), entry.getKey());
      }
    } catch (Exception e) {
      logger.error("Exception: {}", e.getMessage());
    }
    return outcome;
  }
}
