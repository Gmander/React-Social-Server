package com.revature.users;


import com.revature.groups.Group;
import com.revature.users.usersettings.UserSettings;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
	//ID is coming from firebase, will be unique for each user.

    //Following: join table connection between users
    @Id
    @Column(name="user_id", unique = true)
    @JoinColumn()
    private String id;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UserSettings userSettings;



    @Column(unique = true, nullable = false)
    private String email;



    @ManyToMany
    @JoinTable(name = "follower_following",
        joinColumns = {@JoinColumn(name = "uid_follower_fk")},
        inverseJoinColumns = {@JoinColumn(name = "uid_followee_fk")})
    private List<User> following; // changed followUsers to following in order for lombok to generate getters/setters to hit UserDTO


    
    @ManyToMany(mappedBy = "users")
    private List<Group> groups;

    @ManyToMany(mappedBy = "following", cascade = CascadeType.ALL)
    private List<User> follower;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(userSettings, user.userSettings) && Objects.equals(email, user.email) && Objects.equals(following, user.following) && Objects.equals(groups, user.groups) && Objects.equals(follower, user.follower);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userSettings, email, following, groups, follower);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}