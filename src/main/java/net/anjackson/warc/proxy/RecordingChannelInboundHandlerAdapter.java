/**
 * 
 */
package net.anjackson.warc.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;

import com.google.common.io.FileBackedOutputStream;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class RecordingChannelInboundHandlerAdapter extends
		ChannelInboundHandlerAdapter {

	private static Logger LOG = Logger
			.getLogger(RecordingChannelInboundHandlerAdapter.class.getName());

	private FileBackedOutputStream fbos;

	public RecordingChannelInboundHandlerAdapter() {
		fbos = new FileBackedOutputStream(10 * 1024 * 1024);
	}

	public void resetRecorder() throws IOException {
		fbos.reset();
	}

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		LOG.info("IN-" + ":" + msg + " " + ctx);
		if (msg instanceof ByteBuf) {
			ByteBuf bb = (ByteBuf) msg;
			ByteBufInputStream ibin = new ByteBufInputStream(bb.duplicate());
			try {
				IOUtils.copy(ibin, fbos);
				LOG.debug("Recording-in: "
						+ bb.duplicate().toString(CharsetUtil.UTF_8));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			LOG.info("OTHER-IN-" + ":" + msg);
		}
		// Pass it on:
		ctx.fireChannelRead(msg);
    }

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		WarcProxyFiltersSourceAdapter.enumhandlers(ctx);

		LOG.info("Recorded-in-readComplete:\n" + new String(getRecordedBytes()));
		ctx.fireChannelReadComplete();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		LOG.info("Recorded-in-active:\n" + new String(getRecordedBytes()));
		WarcProxyFiltersSourceAdapter.enumhandlers(ctx);
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LOG.info("Recorded-in-inactive:\n" + new String(getRecordedBytes()));
		ctx.fireChannelInactive();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		//
		cause.printStackTrace();
		//
		ctx.fireExceptionCaught(cause);
	}

	public InputStream getRecordedInputStream() throws IOException {
		return this.fbos.asByteSource().openBufferedStream();
	}

	public byte[] getRecordedBytes() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(getRecordedInputStream(), output);
		return output.toByteArray();
	}

}
