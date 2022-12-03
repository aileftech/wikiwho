package tech.ailef.wikiwho.storage;

import com.mongodb.MongoCredential;

public class MongoParameters {
	private String host;
	
	private int port;
	
	private String authDatabaseName;
	
	private String username;
	
	private String password;
	
	/**
	 * Default parameters for connecting on a localhost instance with no
	 * authentication on the specified port.
	 * @param port
	 * @return
	 */
	public static MongoParameters localhostNoAuth(int port) {
		MongoParameters r = new MongoParameters();
		
		r.setHost("localhost");
		r.setPort(port);
		
		return r;
	}
	
	public static MongoParameters localhostNoAuth() {
		MongoParameters r = new MongoParameters();
		
		r.setHost("localhost");
		r.setPort(27017);
		
		return r;
	}
	
	public static MongoParameters localhostAuth(String username, String password, String authDb) {
		MongoParameters r = new MongoParameters();
		
		r.setHost("127.0.0.1");
		r.setPort(27017);
		r.setUsername(username);
		r.setPassword(password);
		r.setAuthDatabaseName(authDb);
		
		return r;
	}
	
	public static MongoParameters fromParams(String host, int port, String authDb, String username, String password) {
		MongoParameters r = new MongoParameters();
		
		r.setHost(host);
		r.setPort(port);
		r.setUsername(username);
		r.setPassword(password);
		r.setAuthDatabaseName(authDb);
		
		return r;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAuthDatabaseName() {
		return authDatabaseName;
	}

	public void setAuthDatabaseName(String authDatabaseName) {
		this.authDatabaseName = authDatabaseName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public MongoCredential getMongoCredential() {
		return MongoCredential.createCredential(username, authDatabaseName, password.toCharArray());
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authDatabaseName == null) ? 0 : authDatabaseName.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + port;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MongoParameters other = (MongoParameters) obj;
		if (authDatabaseName == null) {
			if (other.authDatabaseName != null)
				return false;
		} else if (!authDatabaseName.equals(other.authDatabaseName))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (port != other.port)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MongoParameters [host=" + host + ", port=" + port + ", authDatabaseName=" + authDatabaseName
				+ ", username=" + username + ", password=**********]";
	}
	
	
	
}
