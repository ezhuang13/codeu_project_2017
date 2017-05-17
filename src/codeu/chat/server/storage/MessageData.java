package codeu.chat.server.storage;

import java.util.ArrayList;
import codeu.chat.util.Time;

/**
* @description Data for a message with methods to
* expose fields and allow comparisons for sorting
*/
public final class MessageData implements Comparable<MessageData>{
	private String content;
	private Time creation;

	public MessageData(String content, Time creation){
		this.content = content;
		this.creation = creation;
	}

	/**
	* @return The message's body
	*/
	public String getContent(){
		return content;
	}

	/**
	* @return The message's timestamp
	*/
	public Time getCreation(){
		return creation;
	}

	/**
	* @description Implements comparison by checking the time of creation
	* so that messages can be sorted chronologically
	*/
	public int compareTo(MessageData compareMessage){
		Long myTime = creation.inMs();
		Long compareTime = compareMessage.getCreation().inMs();
		return myTime.compareTo(compareTime);
	}

	/**
	* @description Checks for deep equality, for testing
	*/
	public boolean isEqual(MessageData compareMessage){
		return creation.compareTo(compareMessage.getCreation()) == 0 && content.equals(compareMessage.getContent());
	}
}