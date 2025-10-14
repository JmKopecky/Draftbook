package dev.jkopecky.draftbook.draftbookbackend.data;

import jakarta.persistence.*;

@Entity
public class Chapter implements Comparable<Chapter> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String title;
    private int number;
    private int workId;
    String content;
    @OneToOne(optional = true)
    Note note;

    /**
     * Sort chapters in ascending order by chapter number (lower chapters first).
     * @param other The other chapter to compare against.
     * @return A positive int if this chapter should come before the other, negative if it should not.
     */
    @Override
    public int compareTo(Chapter other) {
        return this.number - other.number;
    }

    //getters and setters
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
    public int getWorkId() {
        return workId;
    }
    public void setWorkId(int workId) {
        this.workId = workId;
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
}
