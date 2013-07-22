package es.mimateo.mimateo;

public class CurrentUser {
	static private String email;
	static private String token;
	public static String getEmail() {
		return email;
	}
	public static void setEmail(String email) {
		CurrentUser.email = email;
	}
	public static String getToken() {
		return token;
	}
	public static void setToken(String token) {
		CurrentUser.token = token;
	}
}
