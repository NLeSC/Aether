package nl.esciencecenter.aether.impl;

public interface AetherMBean {
	
	public String getIdentifier();
	public long getOutgoingMessageCount();
	public long getBytesWritten();
	public long getBytesSend();
	public long getIncomingMessageCount();
	public long getBytesReceived();
	public long getBytesRead();
	

}
