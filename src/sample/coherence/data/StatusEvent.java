package sample.coherence.data;


import com.tangosol.io.pof.annotation.Portable;
import com.tangosol.io.pof.annotation.PortableProperty;



@Portable
public class StatusEvent {
	
 public static final String EVENTS_CACHE = "dist-status-events";


 
 
 @PortableProperty(0)
	private String  messageId;
 
 @PortableProperty(1)
	private long messageStatus;
@PortableProperty(2)
	private long ttl;
 
@PortableProperty(3)
	private String parentMessageId;

public StatusEvent(){
	
}

public StatusEvent(String msgId, String parentId, long status, long mttl){
	messageId = msgId;
	messageStatus = status;
	parentMessageId = parentId;
	ttl = mttl;
}

 public String getMessageId() {
	return messageId;
}
public void setMessageId(String messageId) {
	this.messageId = messageId;
}
public long getMessageStatus() {
	return messageStatus;
}
public void setMessageStatus(long messageStatus) {
	this.messageStatus = messageStatus;
}
public long getTtl() {
	return ttl;
}
public void setTtl(long ttl) {
	this.ttl = ttl;
}

public String getParentMessageId() {
	return parentMessageId;
}

public void setParentMessageId(String parentMessageId) {
	this.parentMessageId = parentMessageId;
}
	
public String toString(){
	return "messageId=" + messageId + " parent=" + parentMessageId + " status=" + messageStatus + " ttl=" + ttl;
}
}
