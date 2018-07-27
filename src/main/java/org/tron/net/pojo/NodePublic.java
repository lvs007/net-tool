package org.tron.net.pojo;

import org.springframework.stereotype.Component;
import java.util.ArrayList;

/**
 * @program: java-tron-net
 * @description: public node we can detect
 * @author: shydesky@gmail.com
 * @create: 2018-07-18
 **/

@Component
public class NodePublic {

    private ArrayList<String> ipList = new ArrayList<>();

    public NodePublic(){
        if(getIpList().size()==0){
            setIpList();
        }
    }

    public ArrayList<String> getIpList(){
        return ipList;
    }
    public void setIpList(){
        this.ipList.add("52.15.93.92:18888");
        /*this.ipList.add("52.53.189.99:18888");
        this.ipList.add("18.196.99.16:18888");
        this.ipList.add("34.253.187.192:18888");
        this.ipList.add("18.196.99.16:18888");
        this.ipList.add("52.56.56.149:18888");
        this.ipList.add("35.180.51.163:18888");
        this.ipList.add("54.252.224.209:18888");
        this.ipList.add("34.220.77.106:18888");*/
    }
}