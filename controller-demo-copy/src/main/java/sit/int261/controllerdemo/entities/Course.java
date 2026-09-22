package sit.int261.controllerdemo.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "subject_id")
    @ManyToOne
    private Subject subject ;

    @JoinColumn(name = "student_id")
    @ManyToOne
    @JsonIgnore
    private Student student ;

    private Double grade;
}
