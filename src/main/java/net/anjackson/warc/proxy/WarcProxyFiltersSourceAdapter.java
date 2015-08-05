/**
 * 
 */
package net.anjackson.warc.proxy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.jwat.warc.WarcWriter;
import org.jwat.warc.WarcWriterFactory;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class WarcProxyFiltersSourceAdapter extends HttpFiltersSourceAdapter {

	private static Logger LOG = Logger
			.getLogger(WarcProxyFiltersSourceAdapter.class.getName());

	private static final AttributeKey<String> CONNECTED_URL = AttributeKey
			.valueOf("connected_url");

	private WarcWriter ww;
	private RecordingChannelInboundHandlerAdapter recin;
	private RecordingChannelOutboundHandlerAdapter recout;

	public WarcProxyFiltersSourceAdapter() {
		LOG.info("Init WARC writer");
		try {
			ww = WarcWriterFactory.getWriterUncompressed(new FileOutputStream(
					"test.warc", true));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public HttpFilters filterRequest(HttpRequest originalRequest,
			ChannelHandlerContext clientCtx) {

		LOG.info("Init filterRequest... " + originalRequest.getMethod());

		ChannelPipeline pipeline = clientCtx.pipeline();

		//
		// // Add recorders that capture the raw request and response:
		// if (pipeline.get("recorder-in") == null) {
		// LOG.info("Adding recorder-in...");
		// recin = new RecordingChannelInboundHandlerAdapter();
		// // pipeline.addBefore("decoder", "recorder-in", recin);
		// pipeline.addFirst("recorder-in", recin);
		// pipeline.addFirst("simple-recorder-in", new ByteBufInReader());
		// } else {
		// LOG.info("NOT adding recorder-in");
		// }
		// if (pipeline.get("recorder-out") == null) {
		// LOG.info("Adding recorder-out...");
		// recout = new RecordingChannelOutboundHandlerAdapter();
		// // pipeline.addBefore("encoder", "recorder-out", recout);
		// pipeline.addFirst("recorder-out", recout);
		// } else {
		// LOG.info("NOT adding recorder-out");
		// }

		// Remove the decoder!
		// if (pipeline.get("decoder") != null) {
		// pipeline.remove("decoder");
		// }

		//
		
		// try {
		// recout.resetRecorder();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		
		enumhandlers(clientCtx);

		String uri = originalRequest.getUri();
		// Store the host if this is an SSL CONNECT:
		if (originalRequest.getMethod() == HttpMethod.CONNECT) {
			if (clientCtx != null) {
				String prefix = "https://" + uri.replaceFirst(":443", "");
				clientCtx.channel().attr(CONNECTED_URL).set(prefix);
			}
			return new WarcProxyFiltersAdapter(originalRequest, clientCtx, uri,
					ww, recin, recout);
			// return new HttpFiltersAdapter(originalRequest, clientCtx);
		} else {
			// Re-build the url if this is from an SSL connection:
			String connectedUrl = clientCtx.channel().attr(CONNECTED_URL).get();
			if (connectedUrl != null) {
				uri = connectedUrl + uri;
			}
			// If we are proxying:
			if (uri.startsWith("http://") || uri.startsWith("https://")) {
				return new WarcProxyFiltersAdapter(originalRequest, clientCtx,
						uri, ww, recin, recout);
			}
			// If we are getting a direct request:
			else {
				return new AnswerRequestFilter(originalRequest,
						("Hello " + uri).getBytes());
			}
		}
	}

	public static void enumhandlers(ChannelHandlerContext ctx) {
		System.err.println("----");
		for (String name : ctx.pipeline().names()) {
			ChannelHandler ch = ctx.pipeline().get(name);
			System.err.println("handler " + name + ":" + ch);
		}
		System.err.println("----");

	}

}