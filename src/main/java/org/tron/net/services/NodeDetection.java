package org.tron.net.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tron.net.common.net.udp.handler.EventHandler;
import org.tron.net.common.net.udp.handler.UdpEvent;
import org.tron.net.common.net.udp.message.Message;
import org.tron.net.common.net.udp.message.discover.FindNodeMessage;
import org.tron.net.common.net.udp.message.discover.NeighborsMessage;
import org.tron.net.pojo.Node;
import org.tron.net.common.utils.NetUtil;
import org.tron.net.common.config.Args;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


@Slf4j
@Component
public class NodeDetection implements EventHandler {

    private ArrayList<Node> bootNodes = new ArrayList<>();
    private Args args = Args.getInstance();
    private Consumer<UdpEvent> messageSender;
    private Node homeNode;
    private ConcurrentHashMap<String, Node> allNode = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Node> allDetectedNode = new ConcurrentHashMap<>();
    private Map<String, NodeHandler> nodeHandlerMap = new ConcurrentHashMap<>();
    private int allMsgSendCount = 0;
    private int allMsgRecvCount = 0;
    private boolean forTrueNodeId = true;

    public static HashMap<String, NodeHandler> tempNetNode = new HashMap<>();
    public static HashMap<String, NodeHandler> currentNetNode = new HashMap<>();

    public NodeDetection(){
        homeNode = new Node("c5ee1254039daa38e133642675c4c7713096cb22654be4d893d30cd14a22324c56a13cabc645599092c2fb7e234850ff910b46dcd2ce630a36e4e0e5c2939440".getBytes(), "127.0.0.1",
                18888);
        // initial the public node
        setBootNodes(args.getPublicNode().getIpList());
    }

    public void setBootNodes(ArrayList<String> ipList){
        for(String bootNode: ipList){
            bootNodes.add(Node.instanceOf(bootNode));
        }
    }

    public Consumer<UdpEvent> getMessageSender(){
        return messageSender;
    }

    public ArrayList<Node> getBootNodes(){
        return bootNodes;
    }

     public HashMap<String, NodeHandler> getCurrentNetNode(){
        return currentNetNode;
     }

    public Node getHomeNode(){
        return homeNode;
    }

    public ConcurrentHashMap<String, Node> getAllNode(){
        return allNode;
    }

    public ConcurrentHashMap<String, Node> getAllDetectedNode(){
        return allDetectedNode;
    }

    public void setMessageSender(Consumer<UdpEvent> messageSender) {
        this.messageSender = messageSender;
    }

    public void setNodeHandler(Node node){
        nodeHandlerMap.put(node.getHexId(), new NodeHandler(node, this));
    }

    public NodeHandler getNodeHandler(Node node){
        if(!nodeHandlerMap.containsKey(node.getHexId())){
            setNodeHandler(node);
        }
        return nodeHandlerMap.get(node.getHexId());
    }

    @Override
    public void channelActivated() {
        for (Node node : bootNodes) {
            getNodeHandler(node);
        }
    }

    @Override
    public void handleEvent(UdpEvent udpEvent) {
        Message m = udpEvent.getMessage();
        InetSocketAddress sender = udpEvent.getAddress();

        Node n = new Node(m.getFrom().getId(), sender.getHostString(), sender.getPort());

        // find the node handler
        NodeHandler nodeHandler = getNodeHandler(n);

        switch (m.getType()) {
            case DISCOVER_FIND_NODE:
                nodeHandler.handleFindNode((FindNodeMessage) m);
                break;
            case DISCOVER_NEIGHBORS:
                if(forTrueNodeId){
                    nodeHandler.handleNeighboursForTrueNodeId((NeighborsMessage) m);
                }else{
                    nodeHandler.handleNeighbours((NeighborsMessage) m);
                }
                break;
        }
    }

    public void sendFindNeighbourMessage(NodeHandler nodeHandler, byte[] targetId){
        FindNodeMessage msg = new FindNodeMessage(getHomeNode(), targetId);
        UdpEvent udpEvent = new UdpEvent(msg, nodeHandler.getInetSocketAddress());
        sendOutbound(udpEvent);
    }

    private void getTrueNodeId(){
        byte[] targetId = new byte[64];
        for (Node node : bootNodes) {
            sendFindNeighbourMessage(getNodeHandler(node), targetId);
        }
    }

    private void clear(){
        allNode.clear();
        logger.info("clear allNode:" + allNode.size());
        NodeDetection.tempNetNode = new HashMap<>();
        logger.info("clear tempNetNode:" + NodeDetection.tempNetNode.size());
        currentNetNode.clear();
        logger.info("currentNetNode:" + NodeDetection.currentNetNode.size());
        allDetectedNode.clear();
        logger.info("allDetectedNode:" + allDetectedNode.size());
        nodeHandlerMap.clear();
        logger.info("nodeHandlerMap:" + nodeHandlerMap.size());
    }

    public void beforeDetect(){
        logger.info("before detect start!");
        clear();
        getTrueNodeId();
        try{
            Thread.sleep(10000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        forTrueNodeId = false;
        nodeHandlerMap.entrySet().stream().filter(
                e-> !e.getValue().getNode().getIsFakeNodeId()
        ).forEach(e->allNode.put(e.getValue().getNode().getHexId(), e.getValue().getNode()));
        if(allNode.size() == 0){
            logger.error("before detect failure!");
        }
    }

    public void doDetect(){
        logger.info("启动！启动节点的数量：" + allNode.size());
        long start = System.currentTimeMillis() / 1000;
        while(allNode.size() > 0){
            allNode.values().forEach(node->{
                if(!allDetectedNode.containsKey(node.getHexId())){
                    detectNeighbour(getNodeHandler(node));
                    allDetectedNode.put(node.getHexId(), node);
                }
            });
            long end = System.currentTimeMillis() / 1000;
            if(end - start > 30){
                break;
            }
        }

        allNode.entrySet().forEach(e->{
            String key = e.getValue().getHost() + ":" +  e.getValue().getPort();
            NodeDetection.tempNetNode.put(key, getNodeHandler(e.getValue()));
        });
        NodeDetection.currentNetNode = NodeDetection.tempNetNode;
    }

    public void statisticsAllNode(){
        Iterator<String> iter = nodeHandlerMap.keySet().iterator();
        String keyp;
        while(iter.hasNext()){
            keyp = iter.next();
            logger.info("neighbours:" + nodeHandlerMap.get(keyp).getAllNeighbourCount());
        }
        logger.info("Detect neighbours of the nodes ends!");
        allNode.entrySet().forEach(e->{
            String key = e.getValue().getHost() + ":" +  e.getValue().getPort();
            NodeDetection.tempNetNode.put(key, getNodeHandler(e.getValue()));
        });
        NodeDetection.currentNetNode = NodeDetection.tempNetNode;
    }

    /**
    * @Description: detect one node's neighbours by send FindNodeMessage to the node
    * @Param: * @param nodeHandler
    * @return: void
    * @Author: shydesky@gmail.com
    * @Date: 2018/7/19
    */
    private void detectNeighbour(NodeHandler nodeHandler){
        Node node = nodeHandler.getNode();
        ArrayList<FindNodeMessage> nodeMessages = new ArrayList<>();
        byte[] targetId;
        for (int i = 1; i <= 256; i++) {
            targetId = NetUtil.mockTargetIdWithDistance(node.getId(), i);
            nodeMessages.add(new FindNodeMessage(getHomeNode(), targetId));
        }
        nodeHandler.setFindNeighbourMsgCount(256);
        nodeMessages.forEach(msg ->sendOutbound(new UdpEvent(msg, nodeHandler.getInetSocketAddress())));
    }

    public void sendOutbound(UdpEvent udpEvent) {
        if (messageSender != null) {
            allMsgSendCount++;
            messageSender.accept(udpEvent);
        }
    }
}























































































































































