/**
 * 
 */
package net.anjackson.warc.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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
				// LOG.debug("Recording-in: "
				// + bb.duplicate().toString(CharsetUtil.UTF_8));
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

	public InputStream getRecordedInputStream() throws IOException {
		return this.fbos.asByteSource().openBufferedStream();
	}

	public byte[] getRecordedBytes() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(getRecordedInputStream(), output);
		return output.toByteArray();
	}

	/**
	 * Finaliser to ensure any temp files are closed for release.
	 */
	protected void finalize() throws Throwable {
		try {
			fbos.close();
		} finally {
			super.finalize();
		}
	}

}
