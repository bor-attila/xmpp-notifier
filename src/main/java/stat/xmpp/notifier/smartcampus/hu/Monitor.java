package stat.xmpp.notifier.smartcampus.hu;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Monitor extends Thread {
	
	private HttpServer server;
	
	private int port;
	
	public Monitor(int port,HttpHandler handler) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/", handler);
	}
	
	public int getPort() {
		return port;
	}

	@Override
	public void run() {
		server.start();
	}

	@Override
	public void interrupt() {
		server.stop(0);
		super.interrupt();
	}
	
}



