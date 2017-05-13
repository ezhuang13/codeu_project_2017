package codeu.chat.server.storage;

import java.util.ArrayList;
import java.util.Collections;
import codeu.chat.util.Time;
import codeu.chat.server.storage.MessageData;

/*
* @description Data for a conversation with methods to
* expose fields and allow comparisons for sorting
*/
class ConversationData implements Comparable<ConversationData>{
	private String title;
	private Time creation;
	private ArrayList<MessageData> messages;
	
	public ConversationData(String title, Time creation, ArrayList<MessageData> messages){
		this.title = title;
		this.creation = creation;
		Collections.sort(messages);
		this.messages = messages;
	}

	public String getTitle(){
		return title;
	}

	public Time getCreation(){
		return creation;
	}

	public ArrayList<MessageData> getMessages(){
		return messages;
	}

	public int compareTo(ConversationData compareConvo){
		Long myTime = creation.inMs();
		Long compareTime = compareConvo.getCreation().inMs();
		return myTime.compareTo(compareTime);
	}
}