package co.poynt.postman.model;

import java.util.List;

public class PostmanItem extends PostmanContainer {
	public List<PostmanEvent> event;
	public PostmanRequest request;
	public List<String> response;
}
