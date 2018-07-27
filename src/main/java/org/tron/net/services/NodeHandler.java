package org.tron.net.services;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.net.common.net.udp.handler.UdpEvent;
import org.tron.net.common.net.udp.message.Message;
import org.tron.net.common.net.udp.message.discover.FindNodeMessage;
import org.tron.net.common.net.udp.message.discover.NeighborsMessage;
import org.tron.net.common.utils.NetUtil;
import org.tron.net.pojo.Node;
import org.tron.net.pojo.NodeBucket;

import java.net.InetSocketAddress;

/**
 * @program: java-tron-net
 * @description: node handle
 * @author: shydesky@gmail.com
 * @create: 2018-07-18
 **/

@Slf4j
public class NodeHandler {

    private boolean waitForNeighbors = false;
    private NodeDetection nodeDetection;
    private Node node;

    private NodeBucket buckets[];

    @Setter
    @Getter
    private int FindNeighbourMsgCount;

    public NodeHandler(Node node, NodeDetection nodeDetection){
        this.node = node;
        this.nodeDetection = nodeDetection;
        buckets = new NodeBucket[257];
        for (int i = 0; i <= 256; i++) {
            buckets[i] = new NodeBucket(i);
        }
    }


    public Node getNode() {
        return node;
    }
    public void setNode(Node node) {
        this.node = node;
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(node.getHost(), node.getPort());
    }

    public int getAllNeighbourCount(){
        int count = 0;
        for(NodeBucket b: buckets){
            count = count + b.getSize();
        }
        return count;
    }

    public void sendFindNode(byte[] target) {
        waitForNeighbors = true;
        Message findNode = new FindNodeMessage(nodeDetection.getHomeNode(), target);
        sendMessage(findNode);
        //getNodeStatistics().discoverOutFind.add();
    }

    private void sendMessage(Message msg) {
        nodeDetection.sendOutbound(new UdpEvent(msg, getInetSocketAddress()));
    }


    public void handleNeighbours(NeighborsMessage msg) {
        for (Node n : msg.getNodes()) {
            if (!nodeDetection.getHomeNode().getHexId().equals(n.getHexId())) {
                buckets[NetUtil.distance(node.getId(), n.getId())].addNode(n);
                nodeDetection.getAllNode().put(n.getHexId(), n);
            }
        }
        setFindNeighbourMsgCount(getFindNeighbourMsgCount() - 1);  //receive a reply
    }


    public void handleFindNode(FindNodeMessage msg){

    }

    //just for the ture nodeId
    public void handleNeighboursForTrueNodeId(NeighborsMessage msg){
        Node node = new Node(msg.getFrom().getId(), this.node.getHost(), this.node.getPort());
        node.setIsFakeNodeId(false);
        setNode(node);
        nodeDetection.setNodeHandler(this.node);
    }
}