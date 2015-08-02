/**
 * 
 */
package net.anjackson.warc.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.littleshoot.proxy.HttpFiltersAdapter;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class AnswerRequestFilter extends HttpFiltersAdapter {
	private final byte[] answer;

	public AnswerRequestFilter(HttpRequest originalRequest, byte[] answer) {
		super(originalRequest, null);
		this.answer = answer;
		System.err.println("AnswerRequestFilter!");
	}

	@Override
	public HttpResponse clientToProxyRequest(HttpObject httpObject) {
		ByteBuf buffer = Unpooled.wrappedBuffer(answer);
		HttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buffer);
		HttpHeaders.setContentLength(response, buffer.readableBytes());
		HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE,
				"text/html");
		// If you respond with a redirect
		// HttpHeaders.setHeader(response, Names.CONNECTION, Values.CLOSE);
		return response;
	}
}
