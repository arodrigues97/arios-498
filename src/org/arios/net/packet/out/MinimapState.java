package org.arios.net.packet.out;

import org.arios.net.packet.IoBuffer;
import org.arios.net.packet.OutgoingPacket;
import org.arios.net.packet.context.MinimapStateContext;

/**
 * Handles the sending of the minimap state outgoing packet.
 * @author Emperor
 */
public final class MinimapState implements OutgoingPacket<MinimapStateContext> {

    @Override
    public void send(MinimapStateContext context) {
	IoBuffer buffer = new IoBuffer(1).put(context.getState());
	context.getPlayer().getDetails().getSession().write(buffer);
    }

}