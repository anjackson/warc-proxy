/**
 * 
 */
package net.anjackson.warc.proxy;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.archive.util.Base32;
import org.jwat.warc.WarcWriter;
import org.littleshoot.proxy.HttpFiltersAdapter;

import com.google.common.io.FileBackedOutputStream;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class WarcProxyFiltersAdapter extends HttpFiltersAdapter {

	private static Logger LOG = Logger.getLogger(WarcProxyFiltersAdapter.class
			.getName());

	private WarcWriter ww;
	private MessageDigest payloadSha1;
	private FileBackedOutputStream outbuf;

	private RecordingChannelInboundHandlerAdapter recin;

	private RecordingChannelOutboundHandlerAdapter recout;

	public WarcProxyFiltersAdapter(HttpRequest originalRequest,
			ChannelHandlerContext clientCtx, String uri, WarcWriter ww,
			RecordingChannelInboundHandlerAdapter recin,
			RecordingChannelOutboundHandlerAdapter recout) {
		super(originalRequest, clientCtx);
		// TODO Auto-generated constructor stub
		LOG.info("Intialising for " + uri);
		this.ww = ww;
		this.recin = recin;
		this.recout = recout;

		try {
			payloadSha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		outbuf = new FileBackedOutputStream(10 * 1024 * 1024);

		LOG.info("Intialised for " + uri);
	}

	@Override
	public HttpResponse clientToProxyRequest(HttpObject httpObject) {
		if (httpObject instanceof HttpRequest) {
			LOG.error("---\nclientToProxyRequest: "
					+ ((HttpRequest) httpObject));
		}
		return null;
	}

	@Override
	public HttpResponse proxyToServerRequest(HttpObject httpObject) {
		if (httpObject instanceof HttpRequest) {
			LOG.error("---\nproxyToServerRequest: "
					+ ((HttpRequest) httpObject));
		}
		return null;
	}

	@Override
	public HttpObject serverToProxyResponse(HttpObject httpObject) {
		LOG.error("---\nserverToProxyResponse: " + httpObject);
		if (httpObject instanceof HttpResponse) {

		} else if (httpObject instanceof HttpContent) {
			if (httpObject instanceof DefaultHttpContent) {
				DefaultHttpContent httpContent = (DefaultHttpContent) httpObject;
				ByteBufInputStream bb = new ByteBufInputStream(httpContent
						.content().duplicate());
				try {
					DigestInputStream payloadShaStream = new DigestInputStream(
							bb, payloadSha1);

					IOUtils.copyLarge(payloadShaStream, outbuf);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (httpObject == LastHttpContent.EMPTY_LAST_CONTENT) {
				LastHttpContent httpContent = (LastHttpContent) httpObject;

				LOG.info("GOT " + httpContent.content());
				try {
					outbuf.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				byte[] sha = payloadSha1.digest();
				String hash = "sha1:" + Base32.encode(sha);
				LOG.error("THAT WAS THE LAST ONE :B32: " + hash);
				LOG.error("THAT WAS THE LAST ONE :HEX: " + "sha1:"
						+ DatatypeConverter.printHexBinary(sha).toLowerCase());
			}
		}
		return httpObject;
	}

	@Override
	public HttpObject proxyToClientResponse(HttpObject httpObject) {
		LOG.error("------\nproxyToClientOriginalRequest: "
				+ originalRequest + "\n URI:"
				+ originalRequest.getDecoderResult());
		LOG.error("---\nproxyToClientResponse: " + httpObject);
		if (httpObject instanceof HttpResponse) {

		} else if (httpObject instanceof HttpContent) {

		}
		return httpObject;
	}

	@Override
	public void serverToProxyResponseReceived() {
	}

}
