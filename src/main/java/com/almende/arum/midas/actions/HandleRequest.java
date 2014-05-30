package com.almende.arum.midas.actions;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.soa.esb.actions.annotation.Process;
import org.jboss.soa.esb.lifecycle.annotation.Initialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.almende.eve.capabilities.handler.SimpleHandler;
import com.almende.eve.transform.rpc.RpcTransform;
import com.almende.eve.transform.rpc.RpcTransformFactory;
import com.almende.eve.transform.rpc.formats.JSONMessage;
import com.almende.eve.transform.rpc.formats.JSONRequest;
import com.almende.eve.transform.rpc.formats.JSONResponse;
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

public class HandleRequest {

	private static Logger logger = LoggerFactory.getLogger(HandleRequest.class);

	private static final ScheduledExecutorService worker = 
			  Executors.newSingleThreadScheduledExecutor();

	private Transport transport;
	//private RpcTransform rpc = RpcTransformFactory.get(new SimpleHandler<Object>(this));
	
	
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
		
		//final Transport	transport = TransportFactory.getTransport(config, new SimpleHandler<Receiver>(new Receiver(){
		transport = TransportFactory.getTransport(config, new SimpleHandler<Receiver>(new Receiver(){
			
			@Override
			public void receive(Object msg, URI senderUrl, String tag) {
				
				// incoming message from MIDAS agents
				JSONMessage incoming = RpcTransform.jsonConvert(msg);
				
				// check if we have a reply or a request; 
				// reply either goes to
				
				// TODO; use the stuff for this that is already available from Eve
				
				if (incoming instanceof JSONRequest) {
					
					// either generate event
					
					// or forward request to ARUM service and wait for reply
					
				} else if (incoming instanceof JSONResponse) {
					// we can either keep track of reponses ourselves, or let Eve take care of that
					// note that we may have to keep track of responses ourselves anyway, for the ARUN-side of affairs
					
					
				}
				
				
				
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
	 * 
	 * Also include a listener for events, and forward them to MIDAS
	 */
	
	@Process
	public ARUMMessage handleIncomingRequest(ARUMMessage message) {
		
		//TODO: see how we can make this an async action
		
		logger.debug("got a message! " + message.getContent());
		System.out.println("system got a message "+ message.getContent());
				
		//do something with the message

		
		
		// forward it over http 
		
		// do we even have a blocking wait for response?
		
		// send the answer as soon as we got it..
	
		ARUMMessage arumResponse = null;
		ARUMMessagePerformative responsePerformative = ARUMMessagePerformative.INFORM_RESULT;
		
		arumResponse = ARUMMessageFactory.createResponseMessage(message, responsePerformative);
		arumResponse.setContent("Got it");
		
		logger.debug("replying... " + arumResponse.getType());
		System.out.println("replying... "+ arumResponse.getType());
			
		
		return arumResponse;
	}

}
