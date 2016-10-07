package org.openmrs.module.ncd.output.extract;

import java.util.ArrayList;

public class DataFeedLog {

	private ArrayList<String> messages;
	private boolean error;
	private long firstRowSent;
	private long lastRowSent;
	private long rowCount;
	
	public DataFeedLog() {
		messages = new ArrayList<String>();
		error = false;
		rowCount = 0;
	}
	
	public ArrayList<String> getMessages() {
		return messages;
	}
	
	public boolean hasError() {
		return error;
	}
	
	public void info(String message) {
		messages.add(message);
	}
	
	public void error(String message) {
		messages.add(message);
		error = true;
	}

	public long getFirstRowSent() {
		return firstRowSent;
	}

	public void setFirstRowSent(long firstRowSent) {
		this.firstRowSent = firstRowSent;
	}

	public long getLastRowSent() {
		return lastRowSent;
	}

	public void setLastRowSent(long lastRowSent) {
		this.lastRowSent = lastRowSent;
	}

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}
	
	public String messagesToString() {
		// Concatenate the log messages into a message body
		String message = new String();
		for (int i=0; i < messages.size(); i++) {
			message += messages.get(i) + "\n";
		}
		return message;
	}
}
