/**
 * 
 */
package net.anjackson.warc.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import org.apache.log4j.Logger;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class ByteBufInReader extends SimpleChannelInboundHandler<ByteBuf> {

	private static Logger LOG = Logger.getLogger(ByteBufInReader.class
			.getName());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf bb)
			throws Exception {
		LOG.debug("Simple-recording-in: "
				+ bb.duplicate().toString(CharsetUtil.UTF_8));
	}
}
