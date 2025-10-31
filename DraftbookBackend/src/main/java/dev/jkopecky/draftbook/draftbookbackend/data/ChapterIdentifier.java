package dev.jkopecky.draftbook.draftbookbackend.data;

public class ChapterIdentifier implements Comparable<ChapterIdentifier> {
    private int id;
    private String title;
    private int number;

    public ChapterIdentifier(Chapter chapter) {
        this.id = chapter.getId();
        this.title = chapter.getTitle();
        this.number = chapter.getNumber();
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public int compareTo(ChapterIdentifier o) {
        return this.number - o.number;
    }
}
