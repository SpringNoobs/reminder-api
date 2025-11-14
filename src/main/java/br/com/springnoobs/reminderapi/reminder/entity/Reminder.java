package br.com.springnoobs.reminderapi.reminder.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class Reminder
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	private Instant remindAt;

	private Instant executedAt;

	private boolean sent = false;

	public Reminder()
	{
	}

	public Reminder(Long id, String title, Instant remindAt, Instant executedAt, boolean sent)
	{
		this.id = id;
		this.title = title;
		this.remindAt = remindAt;
		this.executedAt = executedAt;
		this.sent = sent;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public Instant getRemindAt()
	{
		return remindAt;
	}

	public void setRemindAt(Instant remindAt)
	{
		this.remindAt = remindAt;
	}

	public Instant getExecutedAt()
	{
		return executedAt;
	}

	public void setExecutedAt(Instant executedAt)
	{
		this.executedAt = executedAt;
	}

	public boolean isSent()
	{
		return sent;
	}

	public void setSent(boolean sent)
	{
		this.sent = sent;
	}
}
