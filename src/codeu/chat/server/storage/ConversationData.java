package codeu.chat.server.storage;

import java.util.ArrayList;
import java.util.Collections;
import codeu.chat.util.Time;
import codeu.chat.server.storage.MessageData;

/*
* @description Data for a conversation with methods to
* expose fields and allow comparisons for sorting
*/
public final class ConversationData implements Comparable<ConversationData>{
	private String title;
	private Time creation;
	private int id;
	private ArrayList<MessageData> messages;
	
	public ConversationData(String title, Time creation, int id, ArrayList<MessageData> messages){
		this.title = title;
		this.creation = creation;
		this.id = id;
		this.messages = messages;
	}

	public String getTitle(){
		return title;
	}

	public Time getCreation(){
		return creation;
	}

	public int getId(){
		return id;
	}

	public ArrayList<MessageData> getMessages(){
		return messages;
	}

	public int compareTo(ConversationData compareConvo){
		Long myTime = creation.inMs();
		Long compareTime = compareConvo.getCreation().inMs();
		return myTime.compareTo(compareTime);
	}

	/*
	* @description Checks for deep equality, for testing
	*/
	public boolean isEqual(ConversationData compareConvo){
		if (messages.size() == compareConvo.getMessages().size()){
			for (int i = 0; i < messages.size(); ++i){
				if (!messages.get(i).isEqual(compareConvo.getMessages().get(i))){
					return false;
				}
			}
			return title.equals(compareConvo.getTitle()) && id == compareConvo.getId() &&
					creation.compareTo(compareConvo.getCreation()) == 0;
		}
		else return false;
	}
}