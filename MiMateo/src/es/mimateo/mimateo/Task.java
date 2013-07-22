package es.mimateo.mimateo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Task {
	private String Name;
	private int Id;
	private int UserId;
	private String Deadline;
	private String Description;
	private Boolean Completed;

	public Task() {
		super();
	}
	
	public Task(int id, String name, int userId, String deadline,
			String description, Boolean completed) {
		super();
		Name = name;
		Id = id;
		UserId = userId;
		Deadline = deadline;
		Description = description;
		Completed = completed;
	}

	public Boolean getCompleted() {
		return Completed;
	}

	public void setCompleted(Boolean completed) {
		Completed = completed;
	}

	public int getUserId() {
		return UserId;
	}

	public void setUserId(int userId) {
		UserId = userId;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public void setId(int id) {
		Id = id;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public int getId() {
		return Id;
	}

	public String getDeadline() {
		return Deadline;
	}

	public void setDeadline(String deadline) {
		Deadline = deadline;
	}
	
	public int getDeadlineYear(){
		String[] tokens = Deadline.split("[-]");
		return Integer.valueOf(tokens[0]);
	}
	
	public int getDeadlineMonth(){
		String[] tokens = Deadline.split("[-]");
		return Integer.valueOf(tokens[1]);
	}
	
	public int getDeadlineDay(){
		String[] tokens = Deadline.split("[-]");
		return Integer.valueOf(tokens[2]);
	}
	
	public String getDeadlineAsWord() {
		String format = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		Date today_date = new Date();
		String today = sdf.format(today_date);
		Calendar cal = Calendar.getInstance();
		cal.setTime(today_date);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		Date tomorrow_date = cal.getTime();
		String tomorrow = sdf.format(tomorrow_date);

		if (Deadline.equals("null"))
			return "Indefinido";
		if (Deadline.equals(today))
			return "Hoy";
		if (Deadline.equals(tomorrow))
			return "Mañana";
		return Deadline;
	}
	
	public Boolean isDeadlineMissed(){
		String format = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		Date today_date = new Date();
		try {
			Date deadline_date = sdf.parse(Deadline);
			if (deadline_date.compareTo(today_date) < 0 )
				return true;
			else
				return false;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;	
	}

}