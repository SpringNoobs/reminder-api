package br.com.springnoobs.reminderapi.user.entity;

import br.com.springnoobs.reminderapi.user.entity.contact.Contact;
import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @OneToOne(mappedBy = "user")
    private Contact contact;

    public User() {}

    public User(Long id, String firstName, String lastName, Contact contact) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contact = contact;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
