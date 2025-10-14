package dev.jkopecky.draftbook.draftbook.data;

import dev.jkopecky.draftbook.draftbook.exceptions.NonexistentAccountException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Entity
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String title;
    private String subtitle;
    //chapter ids separated by ;
    private String chapterIds;
    private int accountId;


    /**
     * Get the account which owns this work, if possible.
     * @param accountRepository The account table in the database.
     * @return An Account object representing which account owns this work.
     * @throws NonexistentAccountException Thrown if the account in this.accountId does not exist.
     */
    public Account getOwningAccount(AccountRepository accountRepository)
            throws NonexistentAccountException {
        Optional<Account> owningAccount = accountRepository.findById(accountId);
        if (owningAccount.isPresent()) {
            return owningAccount.get();
        }
        throw new NonexistentAccountException();
    }

    /**
     * Purpose: Retrieve a sorted list of all chapters associated with this work.
     * @param chapterRepository The database repository for chapters.
     * @return A sorted list of chapters.
     */
    public ArrayList<Chapter> getChapters(ChapterRepository chapterRepository) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        //chapter ids are split by ';'
        for (String chapterId : chapterIds.split(";")) {
            int idAsInt = Integer.parseInt(chapterId);
            Optional<Chapter> chapterOptional = chapterRepository.findById(idAsInt);
            chapterOptional.ifPresent(chapters::add);
        }
        Collections.sort(chapters);
        return chapters;
    }

    /**
     * Purpose: Update our stored list of chapter ids.
     * @param chapters A list containing all the chapters associated with this work.
     */
    public void storeChapterIds(ArrayList<Chapter> chapters, WorkRepository workRepository) {
        //work has no chapters
        if (chapters.isEmpty()) {
            chapterIds = "";
            workRepository.save(this);
            return;
        }

        //for each chapter, get its id and save it to a result string
        String result = "" + chapters.getFirst().getId();
        for (int i = 1; i < chapters.size(); i++) {
            result += ";" + chapters.get(i).getId();
        }

        //save the result
        chapterIds = result;
        workRepository.save(this);
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
    public String getSubtitle() {
        return subtitle;
    }
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    public String getChapterIds() {
        return chapterIds;
    }
    public void setChapterIds(String chapterIds) {
        this.chapterIds = chapterIds;
    }
    public int getAccountId() {
        return accountId;
    }
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
