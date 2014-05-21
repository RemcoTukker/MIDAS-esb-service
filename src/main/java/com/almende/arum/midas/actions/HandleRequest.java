package com.almende.arum.midas.actions;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.soa.esb.actions.annotation.Process;
import org.jboss.soa.esb.lifecycle.annotation.Initialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.transport.Receiver;
import com.almende.eve.transport.Transport;
import com.almende.eve.transport.TransportFactory;
import com.almende.eve.transport.http.HttpTransportConfig;

import cz.certicon.arum.core.messaging.ARUMMessage;
import cz.certicon.arum.core.messaging.ARUMMessageFactory;
import cz.certicon.arum.core.messaging.ARUMMessageLanguage;
import cz.certicon.arum.core.messaging.ARUMMessagePerformative;
import cz.certicon.arum.core.messaging.ARUMMessageType;
import cz.certicon.arum.core.messaging.client.ARUMServiceInvoker;
import cz.certicon.arum.core.messaging.client.ARUMServiceInvokerFactory;
import cz.certicon.arum.core.messaging.exceptions.ARUMMessageDeliveryException;
import cz.certicon.arum.core.messaging.exceptions.ARUMProtocolException;
import cz.certicon.arum.core.messaging.exceptions.ARUMUnexpectedMessageProcessingException;

//import org.zeromq.ZMQ;
//import org.zeromq.ZMQ.Context;
///import org.zeromq.ZMQ.Socket;

public class HandleRequest {

	private static Logger logger = LoggerFactory.getLogger(HandleRequest.class);

	private static final ScheduledExecutorService worker = 
			  Executors.newSingleThreadScheduledExecutor();

	
	//Context context = ZMQ.context(1);
    //Socket reqSocket = context.socket(ZMQ.REQ); 
       // socket for passing on requests.. hrm actually its blocking and a request can take a while to answer..
    	// either use a pool of req sockets or just use a one-way pattern and wait patiently for a reply
    	// (we can use the message IDs for that)
    //Socket repSocket = context.socket(ZMQ.REP);
	
	Runnable task = new Runnable() {
	    public void run() {
	    	/* Do something… */
	    	///*	
			// send one message to ourselves to see whether this works
			try {
				ARUMServiceInvoker serviceInvoker = 
						ARUMServiceInvokerFactory.createARUMServiceInvoker("MIDAS", "MidasService");
			
				ARUMMessage message = ARUMMessageFactory.createMessage(ARUMMessageType.FIPA_REQUEST_REQUEST,
					ARUMMessageLanguage.APPLICATION_XML, "testID11223");
				message.setContent("duh");
			
				//message.setFrom(endpoint); //hrmmm what the hell is this now, why necessary?
				//serviceInvoker.setDefaultFromHeader("MidasService", "MIDAS");
				
				//NOTE: This is not FIPA compliant!! FIPA only has async..
				ARUMMessage response = serviceInvoker.deliverSync(message, 5000);
				
				//the following is FIPA compliant; I guess the answer then gets delivered to the .._reply queue
					//TODO: check that out
				//serviceInvoker.deliverAsync(message);
			
				logger.debug("got reply!" + response.getContent());
				System.out.println("system reply " + response.getContent());
				
			} catch (ARUMMessageDeliveryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ARUMProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ARUMUnexpectedMessageProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//*/
	    	
	    	
	    }
	 };
	
	@Initialize
	public void checkConfig() {
		
		logger.debug("initializing!");
		//System.out.println("system initializing");
	
		//worker.schedule(task, 5, TimeUnit.SECONDS);
	
		final HttpTransportConfig config = new HttpTransportConfig();
		config.setServletUrl("http://localhost:8080/MIDASGateway/agents/");
		config.setId("gatewayAgent");
		
		final Transport transport = TransportFactory.getTransport(config, new SimpleHandler<Receiver>(new Receiver(){
		
			@Override
			public void receive(Object msg, URI senderUrl, String tag) {
				logger.info("Hi there:"+msg +" from:"+ senderUrl);
			}
		}));
		//reqSocket.connect("tcp://localhost:5556");
		 
		
	}

	/** Note from Tomas:
	 * Processes the messages received from the ESB. Only ARUM compliant
	 * messages are accepted. FIPA_REQUEST protocol is expected for standard
	 * messages. FIPA_QUERY protocol is expected for requesting API
	 * specification (WSDL file).
	 * 
	 * Remco:
	 * FIPA_REQUEST => forward request to MIDAS
	 * FIPA_QUERY => return wsdl. For now, just ignore it...
	 * Event handling should be done in a separate processing pipeline I suppose..
	 */
	
	@Process
	public ARUMMessage handleIncomingRequest(ARUMMessage message) {
		
		logger.debug("got a message! " + message.getContent());
		System.out.println("system got a message "+ message.getContent());
				
		//do something with the message
		// forward it over the zmq socket
	/*	reqSocket.send("hrsrs", 0); 
			logger.debug("sent msg ");
			System.out.println("sent msg ");
			String result = new String(reqSocket.recv(0));
			logger.debug("answer " + result);
			System.out.println("answer "+ result);
	*/	
		ARUMMessage arumResponse = null;
		ARUMMessagePerformative responsePerformative = ARUMMessagePerformative.INFORM_RESULT;
		
		arumResponse = ARUMMessageFactory.createResponseMessage(message, responsePerformative);
		arumResponse.setContent("ha");
		
		logger.debug("replying... " + arumResponse.getType());
		System.out.println("replying... "+ arumResponse.getType());
				
		return arumResponse;
	}

}
