package dev.jkopecky.draftbook.data.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.ArrayList;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String email;
    private String password;


    public static Account getByEmail(String email, AccountRepository repository) {
        for (Account account : repository.findAll()) {
            if (account.getEmail().equals(email)) {
                return account;
            }
        }
        return null;
    }



    public static Account create(String email, String password, AccountRepository repo) {
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(password);
        repo.save(account);
        return account;
    }



    public static Account authenticate(String email, String password, AccountRepository repo) {
        for (Account account : repo.findAll()) {
            if (account.getEmail().equals(email) && account.getPassword().equals(password)) { //todo secure authentication
                return account;
            }
        }
        return null;
    }



    public static boolean exists(String email, AccountRepository repo) {
        for (Account account : repo.findAll()) {
            if (account.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }



    public ArrayList<Work> getOwnedWorks(WorkRepository repo) {
        ArrayList<Work> works = new ArrayList<>();
        for (Work work : repo.findAll()) {
            if (work.getAccount().equals(this)) {
                works.add(work);
            }
        }
        return works;
    }



    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
