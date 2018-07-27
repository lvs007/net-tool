package org.tron.net.common.net.udp.message.discover;

import static org.tron.net.common.net.udp.message.UdpMessageTypeEnum.DISCOVER_FIND_NODE;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.tron.net.common.net.udp.message.Message;
import org.tron.net.protos.Discover;
import org.tron.net.pojo.Node;
import org.tron.net.common.utils.ByteArray;

@Slf4j
public class FindNodeMessage extends Message {


  private Discover.FindNeighbours findNeighbours;

  public FindNodeMessage(byte[] data) throws Exception{
    super(DISCOVER_FIND_NODE, data);
    this.findNeighbours = Discover.FindNeighbours.parseFrom(data);
  }

  public FindNodeMessage(Node from, byte[] targetId) {
    super(DISCOVER_FIND_NODE, null);
    Discover.Endpoint fromEndpoint = Discover.Endpoint.newBuilder()
        .setAddress(ByteString.copyFrom(ByteArray.fromString(from.getHost())))
        .setPort(from.getPort())
        .setNodeId(ByteString.copyFrom(from.getId()))
        .build();
    this.findNeighbours = Discover.FindNeighbours.newBuilder()
        .setFrom(fromEndpoint)
        .setTargetId(ByteString.copyFrom(targetId))
        .setTimestamp(System.currentTimeMillis())
        .build();
    this.data = this.findNeighbours.toByteArray();
  }

  public byte[] getTargetId() {
    return this.findNeighbours.getTargetId().toByteArray();
  }

  @Override
  public Node getFrom() {
    return new Node("");
    //return Message.getNode(findNeighbours.getFrom());
  }

  @Override
  public String toString() {
    return "[findNeighbours: " + findNeighbours;
  }
}
