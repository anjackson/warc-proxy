/**
 * 
 */
package net.anjackson.warc.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

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
public class RecordingChannelOutboundHandlerAdapter extends
		ChannelOutboundHandlerAdapter {
	
	private static Logger LOG = Logger
			.getLogger(RecordingChannelOutboundHandlerAdapter.class.getName());

	private FileBackedOutputStream fbos;

	public RecordingChannelOutboundHandlerAdapter() {
		fbos = new FileBackedOutputStream(10 * 1024 * 1024);
	}

	public void resetRecorder() throws IOException {
		fbos.reset();
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		if (msg instanceof ByteBuf) {
			ByteBuf bb = (ByteBuf) msg;
			ByteBufInputStream ibin = new ByteBufInputStream(bb.duplicate());
			IOUtils.copy(ibin, fbos);
		} else {
			LOG.info("OTHER-OUT-:" + msg);
		}
		// Pass it on:
		ctx.write(msg, promise);
    }

	@Override
	public void read(ChannelHandlerContext ctx) throws Exception {
		LOG.info("Recorded-out-read:\n" + new String(getRecordedBytes()));
		ctx.read();
	}

	public InputStream getRecordedInputStream() throws IOException {
		return this.fbos.asByteSource().openBufferedStream();
	}

	public byte[] getRecordedSha1Digest() {
		return "Test".getBytes();
		// return this.recordingOutputStream.getDigestValue();
	}

	public byte[] getRecordedBytes() throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(getRecordedInputStream(), output);
		return output.toByteArray();
	}

}
