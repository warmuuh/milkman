package co.poynt.postman.model;

import java.util.List;

public class PostmanContainer {
	public String name;
	public String description;
	
	//not null if folder
	public List<PostmanContainer> item;
	
	//not null if item
	public List<PostmanEvent> event;
	
	//not null if item
	public PostmanRequest request;
	
	//not null if item
	public List<String> response;
}
