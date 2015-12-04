package rcooper.bookmanager.model;

public class TextBook extends Book
{
	
	private final String INFO_LABEL = "Subject:";
	private String subject;
	
	public TextBook(String title, String author, String publisher,
			String pubDate, double retailPrice, String type, String subject)
	{
		super(title, author, publisher, pubDate, retailPrice, type);
		this.info = new AdditionalInfo(INFO_LABEL, subject);
	}

	/* ACCESSORS */
	
	public String getSubject()
	{
		return subject;
	}

	@Override
	public String toString()
	{
		return super.toString() + subject + " ";
	}
	
	/* MUTATORS */

	public void setSubject(String subject)
	{
		this.subject = subject;
	}
	
}