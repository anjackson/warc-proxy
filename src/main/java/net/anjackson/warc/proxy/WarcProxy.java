package net.anjackson.warc.proxy;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.RootCertificateException;

public class WarcProxy {
	private static Logger LOG = Logger.getLogger(WarcProxy.class.getName());

	public static void main(String[] args) throws RootCertificateException,
			IOException {

		// TODO Auto-generated method stub
		HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
				.withPort(8080)
				// for both HTTP and HTTPS
				.withManInTheMiddle(new SelfSignedMitmManager())
				// .withManInTheMiddle(new HostNameMitmManager())
				.withListenOnAllAddresses(true).withTransparent(false)
				.withFiltersSource(new WarcProxyFiltersSourceAdapter()).start();
		// OR CertificateSniffingMitmManager

		LOG.info("Started...");

		// Await
		System.in.read();
		LOG.info("Stopping...");
		
		// Stop
		server.stop();
	}

}
