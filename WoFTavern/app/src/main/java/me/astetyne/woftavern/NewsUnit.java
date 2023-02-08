package me.astetyne.woftavern;

public class NewsUnit {

    NewsThumbnail thumbnail;
    String subject;
    String text;
    String footer;

    public NewsUnit(int thumbnail, String subject, String text, String footer) {
        this.thumbnail = NewsThumbnail.values()[thumbnail];
        this.subject = subject;
        this.text = text;
        this.footer = footer;
    }

}
