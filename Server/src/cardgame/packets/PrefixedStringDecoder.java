package cardgame.packets;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class PrefixedStringDecoder extends CumulativeProtocolDecoder {

	@Override
	protected boolean doDecode(IoSession session, ByteBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		if (in.prefixedDataAvailable(4)) {
			int len = in.getInt();
			byte[] payload = new byte[len];
			in.get(payload);
			out.write(new String(payload, "UTF-8"));
			return true;
		}
		return false;
	}

}
