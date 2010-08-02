package cardgame.packets;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class PrefixedStringEncoder implements ProtocolEncoder {

	public void encode(IoSession session, Object msg, ProtocolEncoderOutput out)
			throws Exception {
		String data = (String) msg;
		byte[] payload = data.getBytes("UTF-8");
		ByteBuffer buf = ByteBuffer.allocate(32);
		buf.setAutoExpand(true);
		buf.putInt(payload.length);
		buf.put(payload);
		buf.flip();
		out.write(buf);
	}

	public void dispose(IoSession arg0) throws Exception {
		// nothing to dispose
	}

}
