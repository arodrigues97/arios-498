package org.arios.net.packet.out;

import org.arios.net.packet.IoBuffer;
import org.arios.net.packet.OutgoingPacket;
import org.arios.net.packet.context.PlayerContext;

/**
 * The run energy outgoing packet.
 * @author Emperor
 */
public class RunEnergy implements OutgoingPacket<PlayerContext> {

    @Override
    public void send(PlayerContext context) {
	IoBuffer buffer = new IoBuffer(205);
	buffer.put((byte) context.getPlayer().getSettings().getRunEnergy());
	context.getPlayer().getDetails().getSession().write(buffer);
    }
}