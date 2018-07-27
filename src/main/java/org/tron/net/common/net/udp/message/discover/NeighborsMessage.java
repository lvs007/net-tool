package org.tron.net.common.net.udp.message.discover;

import static org.tron.net.common.net.udp.message.UdpMessageTypeEnum.DISCOVER_NEIGHBORS;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.tron.net.common.net.udp.message.Message;
import org.tron.net.protos.Discover;
import org.tron.net.pojo.Node;
import org.tron.net.common.utils.ByteArray;

@Slf4j
public class NeighborsMessage extends Message {


  private Discover.Neighbours neighbours;

  public NeighborsMessage(byte[] data) throws Exception{
    super(DISCOVER_NEIGHBORS, data);
    this.neighbours = Discover.Neighbours.parseFrom(data);
  }

  public NeighborsMessage(Node from, List<Node> neighbours) {
    super(DISCOVER_NEIGHBORS, null);
    Discover.Neighbours.Builder builder = Discover.Neighbours.newBuilder()
        .setTimestamp(System.currentTimeMillis());

    neighbours.forEach(neighbour -> {
      Discover.Endpoint endpoint = Discover.Endpoint.newBuilder()
          .setAddress(ByteString.copyFrom(ByteArray.fromString(neighbour.getHost())))
          .setPort(neighbour.getPort())
          .setNodeId(ByteString.copyFrom(neighbour.getId()))
          .build();

      builder.addNeighbours(endpoint);
    });

    Discover.Endpoint fromEndpoint = Discover.Endpoint.newBuilder()
        .setAddress(ByteString.copyFrom(ByteArray.fromString(from.getHost())))
        .setPort(from.getPort())
        .setNodeId(ByteString.copyFrom(from.getId()))
        .build();

    builder.setFrom(fromEndpoint);

    this.neighbours = builder.build();

    this.data = this.neighbours.toByteArray();
  }

  public List<Node> getNodes() {
    List<Node> nodes = new ArrayList<>();
    neighbours.getNeighboursList().forEach(neighbour -> nodes.add(
        new Node(neighbour.getNodeId().toByteArray(),
            ByteArray.toStr(neighbour.getAddress().toByteArray()),
            neighbour.getPort())));
    return nodes;
  }

  @Override
  public Node getFrom() {
    return Message.getNode(neighbours.getFrom());
  }

  @Override
  public String toString() {
    return "[neighbours: " + neighbours;
  }

}
