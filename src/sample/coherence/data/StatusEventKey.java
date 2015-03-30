package sample.coherence.data;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.cache.KeyAssociation;

    public  class StatusEventKey implements PortableObject, KeyAssociation {
 
        private String  messageId;
        private String  parentId;
 
        public StatusEventKey() {
        }
 
        public StatusEventKey (StatusEvent event) {
        	
           this.messageId = event.getMessageId();
           this.parentId = event.getParentMessageId();
           if (parentId == null){
        	   parentId = messageId;
           }
        }
 
        public StatusEventKey (String  parentMessageId, String messageId) {
            this.messageId = messageId;
            this.parentId = parentMessageId;
            
        }
 
        @Override
        public Object getAssociatedKey() {
            return parentId;
        }

		@Override
		public void readExternal(PofReader reader) throws IOException {
			// TODO Auto-generated method stub
			messageId = reader.readString(0);
			parentId = reader.readString(1);
		}

		@Override
		public void writeExternal(PofWriter writer) throws IOException {
			// TODO Auto-generated method stub
			writer.writeString(0, messageId);
			writer.writeString(1, parentId);
		}

		public String getMessageId() {
			return messageId;
		}

		public void setMessageId(String messageId) {
			this.messageId = messageId;
		}

		public String getParentId() {
			return parentId;
		}

		public void setParentId(String parentId) {
			this.parentId = parentId;
		}
		
		public String toString(){
			return "messageId=" + messageId + " parent=" + parentId;
		}
 
        // hashCode() , equals().. etc..
    

}
