package org.arios.net.packet.out;

import org.arios.net.packet.IoBuffer;
import org.arios.net.packet.OutgoingPacket;
import org.arios.net.packet.context.WindowsPaneContext;

/**
 * Handles the windows pane outgoing packet.
 * @author Emperor
 */
public final class WindowsPane implements OutgoingPacket<WindowsPaneContext> {

    @Override
    public void send(WindowsPaneContext context) {
	IoBuffer buffer = new IoBuffer(222);
	buffer.putShort(context.getWindowId());
	buffer.putS(context.getType());
	context.getPlayer().getDetails().getSession().write(buffer);
    }

}