package org.arios.net.packet.out;

import org.arios.net.packet.IoBuffer;
import org.arios.net.packet.OutgoingPacket;
import org.arios.net.packet.context.AccessMaskContext;

/**
 * The access mask outgoing packet.
 * @author Empero
 */
public class AccessMask implements OutgoingPacket<AccessMaskContext> {

    @Override
    public void send(AccessMaskContext context) {
	IoBuffer buffer = new IoBuffer(178);
	buffer.putIntB(context.getInterfaceId() << 16 | context.getChildId());
	buffer.putInt(context.getId());
	buffer.putLEShortA(context.getOffset());
	buffer.putLEShort(context.getLength());
	context.getPlayer().getSession().write(buffer);
    }
}
