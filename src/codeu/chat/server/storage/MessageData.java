package codeu.chat.server.storage;

import java.util.ArrayList;
import codeu.chat.util.Time;

/*
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

	public String getContent(){
		return content;
	}

	public Time getCreation(){
		return creation;
	}

	public int compareTo(MessageData compareMessage){
		Long myTime = creation.inMs();
		Long compareTime = compareMessage.getCreation().inMs();
		return myTime.compareTo(compareTime);
	}
}