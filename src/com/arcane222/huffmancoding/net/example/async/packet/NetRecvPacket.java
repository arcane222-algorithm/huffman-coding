package com.arcane222.huffmancoding.net.example.async.packet;

import java.io.Serializable;

public class NetRecvPacket<T extends Serializable> extends NetPacketImpl<T> {


    protected NetRecvPacket(T data) {
        super(data);
    }
}
