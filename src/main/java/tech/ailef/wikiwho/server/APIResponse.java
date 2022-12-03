package tech.ailef.wikiwho.server;

import tech.ailef.wikiwho.storage.BasicJsonSerializer;

public class APIResponse {
	private static final BasicJsonSerializer<APIResponse> serializer =
		new BasicJsonSerializer<APIResponse>(APIResponse.class);
	
	private boolean success;
	
	private String message;
	
	private Object payload;

	public APIResponse(boolean success, String message, Object payload) {
		this.success = success;
		this.message = message;
		this.payload = payload;
	}
	
	public static String toJson(boolean success, String message, Object payload) {
		return serializer.serialize(new APIResponse(success, message, payload));
	}
	
	public static String toJson(boolean success, Object payload) {
		return serializer.serialize(new APIResponse(success, null, payload));
	}

	public static BasicJsonSerializer<APIResponse> getSerializer() {
		return serializer;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public Object getPayload() {
		return payload;
	}
	
}
