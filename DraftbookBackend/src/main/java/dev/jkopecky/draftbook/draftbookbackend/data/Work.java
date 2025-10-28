package dev.jkopecky.draftbook.draftbookbackend.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jkopecky.draftbook.draftbookbackend.exceptions.NonexistentAccountException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

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
    private int chapterCount;

    /**
     * Create a work in the database.
     * @param name The title of the work.
     * @param account The account which owns this work.
     * @param workRepository The table in the database for works.
     */
    public Work(String name, Account account, WorkRepository workRepository) {
        this.title = name;
        this.accountId = account.getId();
        this.subtitle = "";
        this.chapterIds = "";
        workRepository.save(this);
    }

    public Work() {
        this.title = "Unset";
        this.subtitle = "";
        this.chapterIds = "";
        this.accountId = -1;
    }

    /**
     * Insert a chapter at its target location, shifting all chapters after it by one.
     * @param chapter The chapter to insert.
     * @param workRepository The database table containing works.
     * @param chapterRepository The database table containing chapters.
     */
    public void insertChapter(Chapter chapter, WorkRepository workRepository, ChapterRepository chapterRepository) {
        int boundedChapterNum = boundChapterNumber(chapter.getNumber());
        ArrayList<Chapter> chapterList = getChapters(chapterRepository);
        chapterList.add(boundedChapterNum - 1, chapter);
        //for the added chapter and all the ones after, update the chapter number
        for (int i = boundedChapterNum - 1; i < chapterList.size(); i++) {
            Chapter currentChapter = chapterList.get(i);
            currentChapter.setNumber(i);
            chapterRepository.save(currentChapter);
        }
        storeChapterIds(chapterList, workRepository);
    }

    /**
     * Updates the chapter counter variable.
     */
    public void updateChapterCount() {
        this.chapterCount = chapterIds.split(";").length;
    }

    /**
     * Bounds the provided chapter number within the size of this work.
     * @param chapterNumber The chapter number to bound.
     * @return A chapter number safely within this work's bounds.
     */
    public int boundChapterNumber(int chapterNumber) {
        if (chapterNumber <= 0) return 1;
        return Math.min(chapterNumber, getChapterCount() + 1);
    }

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
     * Provide a work based on a request if it exists and the account is allowed to access it.
     * @param requestBody The request from which to identify the work.
     * @param workRepository The table in the database containing works.
     * @param account The account which should own the work in question.
     * @return An array of Objects, where the first element is either a work or null. If null, the second element is an HttpStatusCode describing why.
     */
    public static Object[] getWorkIfAllowed(
            String requestBody, Account account, WorkRepository workRepository) {
        Object[] result = new Object[2];

        //check that the work requested exists
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(requestBody);
            int workId = node.get("work_id").asInt();
            Work targetWork = workRepository.findById(workId).get();
            result[0] = targetWork;
            result[1] = HttpStatus.OK;
        } catch (JsonProcessingException | NullPointerException e) {
            result[1] = HttpStatus.BAD_REQUEST;
            return result;
        } catch (NoSuchElementException e) {
            result[1] = HttpStatus.NOT_FOUND;
            return result;
        }

        //check that the account is allowed to access it
        if (((Work) result[0]).accountId != account.getId()) {
            result[0] = null;
            result[1] = HttpStatus.UNAUTHORIZED;
        }

        return result;
    }

    /**
     * Delete this work.
     * @param workRepository The table containing works in the database.
     * @param chapterRepository The table containing chapters in the database.
     * @param noteRepository The table containing notes in the database.
     */
    public void deleteWork(
            WorkRepository workRepository,
            ChapterRepository chapterRepository,
            NoteRepository noteRepository) {

        //get all chapters and delete them first
        for (Chapter chapter : getChapters(chapterRepository)) {
            chapter.deleteChapter(chapterRepository, noteRepository);
        }

        //delete this work
        workRepository.delete(this);
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
        updateChapterCount();
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
    public int getChapterCount() {
        return chapterCount;
    }
}
