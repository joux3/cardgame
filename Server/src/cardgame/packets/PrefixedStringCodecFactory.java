package cardgame.packets;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class PrefixedStringCodecFactory implements ProtocolCodecFactory {
	private final ProtocolEncoder encoder;

	private final ProtocolDecoder decoder;

	public PrefixedStringCodecFactory() {
		encoder = new PrefixedStringEncoder();
		decoder = new PrefixedStringDecoder();
	}

	public ProtocolDecoder getDecoder() throws Exception {
		return decoder;
	}

	public ProtocolEncoder getEncoder() throws Exception {
		return encoder;
	}

}
