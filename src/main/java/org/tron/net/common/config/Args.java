package org.tron.net.common.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tron.net.pojo.NodePublic;

@Slf4j
@Component
public class Args {
    private static final Args INSTANCE = new Args();

    @Getter
    private NodePublic publicNode = new NodePublic();

    public int getNodeP2pVersion(){
        return 11111;
    }
    public static Args getInstance() {
        return INSTANCE;
    }
}