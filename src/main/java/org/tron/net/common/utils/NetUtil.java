package org.tron.net.common.utils;

/**
 * @program: java-tron-net
 * @description: tronprotocol net util
 * @author: shydesky@gmail.com
 * @create: 2018-07-18
 **/

public class NetUtil {

    /**
     * @param ownerId  the NodeId of owner
     * @param targetId the NodeId of target
     * @Description: calculate the distance between two the owner Node and the target Node.
     * @Param:
     * @return: int
     * @Author: shydesky@gmail.com
     * @Date: 2018/7/18
     */
    public static int distance(byte[] ownerId, byte[] targetId) {
        byte[] h1 = targetId;
        byte[] h2 = ownerId;

        byte[] hash = new byte[Math.min(h1.length, h2.length)];

        for (int i = 0; i < hash.length; i++) {
            hash[i] = (byte) (((int) h1[i]) ^ ((int) h2[i]));
        }
        int d = 256;
        for (byte b : hash) {
            if (b == 0) {
                d -= 8;
            } else {
                int count = 0;
                for (int i = 7; i >= 0; i--) {
                    boolean a = (b & (1 << i)) == 0;
                    if (a) {
                        count++;
                    } else {
                        break;
                    }
                }
                d -= count;
                break;
            }
        }
        return d;
    }

    /**
     * @param nodeId
     * @param distance
     * @Description: generate a nodeid with given nodeid
     * @Param:
     * @return: java.lang.String
     * @Author: shydesky@gmail.com
     * @Date: 2018/7/18
     */
    public static byte[] mockTargetIdWithDistance(byte[] nodeId, int distance) {
        byte[] targetId = new byte[nodeId.length];

        for(int i=0; i<nodeId.length; i++){
            targetId[i] = nodeId[i];
        }

        int m = (distance - 1) / 8;
        int n = (distance - 1) % 8;
        int start = nodeId.length / 2 - 1;
        start = start - m;
        byte b = targetId[start];
        b ^= 1 << (n);
        targetId[start] = b;
        if (distance(nodeId, targetId) == distance) {
            return targetId;
        }
        return targetId;
    }
}