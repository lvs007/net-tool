package org.tron.net.common.net.udp.message;

import org.apache.commons.lang3.ArrayUtils;
import org.tron.net.common.net.udp.message.discover.FindNodeMessage;
import org.tron.net.common.net.udp.message.discover.NeighborsMessage;
import org.tron.net.protos.Discover.Endpoint;
import org.tron.net.pojo.Node;
import org.tron.net.common.utils.ByteArray;
import org.tron.net.common.utils.Sha256Hash;
import org.tron.net.common.exception.P2pException;

public abstract class Message {

  protected UdpMessageTypeEnum type;
  protected byte[] data;

  public Message(UdpMessageTypeEnum type, byte[] data) {
    this.type = type;
    this.data = data;
  }

  public UdpMessageTypeEnum getType() {
    return this.type;
  }

  public byte[] getData() {
    return this.data;
  }

  public byte[] getSendData() {
    return ArrayUtils.add(this.data, 0, type.getType());
  }

  public Sha256Hash getMessageId() {
    return Sha256Hash.of(getData());
  }

  public abstract Node getFrom();

  @Override
  public String toString() {
    return "[Message Type: " + getType() + ", len: " + (data == null ? 0 : data.length) + "]";
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return getMessageId().hashCode();
  }

  public static Node getNode(Endpoint endpoint){
    Node node = new Node(endpoint.getNodeId().toByteArray(),
        ByteArray.toStr(endpoint.getAddress().toByteArray()), endpoint.getPort());
    return node;
  }

  public static Message parse(byte[] encode) throws Exception {
    byte type = encode[0];
    byte[] data = ArrayUtils.subarray(encode, 1, encode.length);
    switch (UdpMessageTypeEnum.fromByte(type)) {
      case DISCOVER_FIND_NODE:
        return new FindNodeMessage(data);
      case DISCOVER_NEIGHBORS:
        return new NeighborsMessage(data);
      default:
        throw new P2pException(P2pException.TypeEnum.NO_SUCH_MESSAGE, "type=" + type);
    }
  }
}
