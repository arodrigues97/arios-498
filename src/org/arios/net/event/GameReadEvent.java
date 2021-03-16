package org.arios.net.event;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.arios.game.system.SystemLogger;
import org.arios.game.world.GameWorld;
import org.arios.net.IoReadEvent;
import org.arios.net.IoSession;
import org.arios.net.packet.IncomingPacket;
import org.arios.net.packet.IoBuffer;
import org.arios.net.packet.PacketRepository;

/**
 * Handles game packet reading.
 * @author Emperor
 */
public final class GameReadEvent extends IoReadEvent {

    /**
     * The incoming packet sizes, sorted by opcode.
     */
    public static final int[] PACKET_SIZES = { -3, -3, -3, -3, -3, -3, -3, 4, -3, -3, -3, -3, -3, 8, -3, -3, -3, -3, -3, -3, 8, -3, -3, -3, -3, -3, -3, -3, 6, -3, -3, -3, 1, -3, -3, 0, -3, -3, -3, 6, -3, -3, -3, -1, -3, -3, -1, 8, -3, -3, 6, -3, 2, -3, 3, 12, 0, 8, 10, -3, 8, 10, -3, -3, -3, -3, -3, 6, 9, -1, -3, -3, -3, -3, -1, -3, 6, -3, -3, -3, -3, -3, 2, -3, -3, 6, -3, 8, 2, 14, -3, -3, 8, -3, -3, 0, -3, -3, -3, -3, -3, -3, -3, -3, -1, 2, -1, -3, 12, 6, -3, -3, 2, -3, -3, -3, -3, -3, -3, -3, -1, -3, 14, -3, -3, -3, -3, 6, 6, -3, -3, -3, -3, 8, -3, -3, -3, 8, -3, 8, -3, 6, -3, -3, 4, 8, -3, -3, -3, -1, -3, -3, 6, -3, -3, -3, 6, 8, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, 6, 8, -3, -3, -3, -3, -3, -3, -1, 9, 8, 4, -3, 2, 4, -3, 2, -3, 6, 16, 6, -3, 12, 4, -3, -3, -3, -3, 2, -3, -3, 4, -3, -3, 6, -3, 6, 10, -3, -3, -3, -3, 6, -3, -3, 2, -3, -3, 6, -3, -3, -3, -3, -3, -3, -3, -3, -3, -1, 2, -3, 6, -3, -3, 8, 12, 6, 8, 2, -3, 2, -1, 2, 2, 8, 8, 2, -1, -3, 8, 2, -3, 14, -3, -3, 2, -3 };

    /**
     * Constructs a new {@code GameReadEvent}.
     * @param session The session.
     * @param buffer The buffer to read from.
     */
    public GameReadEvent(IoSession session, ByteBuffer buffer) {
	super(session, buffer);
    }

    @Override
    public void read(IoSession session, ByteBuffer buffer) {
	int last = -1;
	while (buffer.hasRemaining()) {
	    int opcode = buffer.get() & 0xFF;
	    if (session == null || session.getPlayer() == null) {
		continue;
	    }
	    Map<Integer, Integer> packetLog = session.getPlayer().getAttribute("packet_log");
	    if (packetLog == null) {
		session.getPlayer().setAttribute("packet_log", packetLog = new HashMap<>());
	    }
	    Integer amount = packetLog.get(opcode);
	    if (amount == null) {
		amount = 0;
	    }
	    packetLog.put(opcode, amount + 1);
	    if (amount > 0 && amount % 10000 == 0) {
		// System.out.println("Possible packet flood from IP " +
		// session.getAddress() + ", player " +
		// session.getPlayer().getName() + " op=" + opcode + ", amount="
		// + amount + ".");
	    }
	    if (opcode >= PACKET_SIZES.length) {
		break;
	    }
	    int header = PACKET_SIZES[opcode];
	    int size = header;
	    if (header < 0) {
		size = getPacketSize(buffer, opcode, header, last);
	    }
	    if (size == -1) {
		break;
	    }
	    if (buffer.remaining() < size) {
		switch (header) {
		case -2:
		    queueBuffer(opcode, size >> 8, size);
		    break;
		case -1:
		    queueBuffer(opcode, size);
		    break;
		default:
		    queueBuffer(opcode);
		    break;
		}
		break;
	    }
	    byte[] data = new byte[size];
	    buffer.get(data);
	    IoBuffer buf = new IoBuffer(opcode, null, ByteBuffer.wrap(data));
	    IncomingPacket packet = PacketRepository.getIncoming(opcode);
	    if (packet == null) {
		if (GameWorld.getSettings().isDevMode()) {
		    SystemLogger.log("Unhandled packet [opcode=" + opcode + ", previous=" + last + ", size=" + size + ", header=" + header + "]");
		}
		continue;
	    }
	    last = opcode;
	    try {
		session.setLastPing(System.currentTimeMillis());
		packet.decode(session.getPlayer(), opcode, buf);
	    } catch (Throwable t) {
		t.printStackTrace();
	    }
	}
    }

    /**
     * Gets the packet size for the given opcode.
     * @param buffer The buffer.
     * @param opcode The opcode.
     * @param header The packet header.
     * @param last The last opcode.
     * @return The packet size.
     */
    private int getPacketSize(ByteBuffer buffer, int opcode, int header, int last) {
	if (header == -1) {
	    if (buffer.remaining() < 1) {
		queueBuffer(opcode);
		return -1;
	    }
	    return buffer.get() & 0xFF;
	}
	if (header == -2) {
	    if (buffer.remaining() < 2) {
		queueBuffer(opcode);
		return -1;
	    }
	    return buffer.getShort() & 0xFFFF;
	}
	System.err.println("Invalid packet [opcode=" + opcode + ", last=" + last + ", queued=" + usedQueuedBuffer + "]!");
	return -1;
    }

}