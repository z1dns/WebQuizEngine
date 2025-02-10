package engine.businesslayer;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn (name = "user_id")
    AppUser user;
    private String title;
    private String text;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> options;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> answers;

    public Question(AppUser user, String title, String text, List<String> options, List<Integer> answers) {
        this.user = user;
        this.title = title;
        this.text = text;
        this.options = options;
        this.answers = answers;
    }

    public Question() {

    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public List<Integer> getAnswers() {
        return answers;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUserId() {
        return user;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", user=" + user +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", options=" + options +
                ", answers=" + answers +
                '}';
    }
}
