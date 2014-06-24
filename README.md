MIDAS-esb-service
=================

A JBoss ESB service that uses HTTP to communicate with the MIDAS agent system (which is run on NodeJS). 

This service is the gateway for any interaction between MIDAS and the other ARUM services and has 4 distinct functions:

Receving a HTTP JSON RPC request from MIDAS agent that is forwarded to one of the ARUM services, and its reply forwarded back to the agent
Receiving a HTTP JSON RPC request from MIDAS to publish a message on an ARUM topic
Making a request to a MIDAS agent through a HTTP JSON RPC request to the MIDAS ESB proxy agent (with a configurable address) and relaying the answer back to the service invocation this function
Forwarding an event message from an ARUM topic (configurable) to the MIDAS ESB proxy agent (with a configurable address), again with a HTTP JSON RPC request

TSB integration will be accomplished by implementing a functionally identical service on the TSB.

Note that this doesnt work in the deployment environment yet due to Java version (6 and 7) incompatibilities and that not all functionality is completely implemented yet (in particular relaying the answers to the right place).
