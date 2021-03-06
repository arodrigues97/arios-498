package org.arios.net.packet.out;

import org.arios.net.packet.IoBuffer;
import org.arios.net.packet.OutgoingPacket;
import org.arios.net.packet.context.PlayerContext;

/**
 * Handles the outgoing weight update packet.
 * @author Emperor
 */
public final class WeightUpdate implements OutgoingPacket<PlayerContext> {

    @Override
    public void send(PlayerContext context) {
	IoBuffer buffer = new IoBuffer(174);
	buffer.putShort((int) context.getPlayer().getSettings().getWeight());
	context.getPlayer().getSession().write(buffer);
    }

}