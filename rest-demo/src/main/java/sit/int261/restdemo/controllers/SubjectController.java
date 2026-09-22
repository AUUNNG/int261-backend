package sit.int261.restdemo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int261.restdemo.entities.Subject;
import sit.int261.restdemo.repositories.SubjectRepository;
import sit.int261.restdemo.services.SubjectService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/demo/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepository subjectRepository;

    @GetMapping("")
    public List<Subject> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(@PathVariable Integer id) {
        return ResponseEntity.ok(subjectService.getSubjectById(id));
    }

    @PostMapping("")
    public Subject createSubject(@RequestBody Subject subject) {
        return subjectService.createSubject(subject);
    }

    @PutMapping("/{id}")
    public Subject updateSubject(@PathVariable Integer id, @RequestBody Subject subjectDetails) {
        return subjectService.updateSubject(id, subjectDetails);
    }

    @PostMapping("/init")
    public List<Subject> createSampleData() {
        List<Subject> subjects = new ArrayList<>();
        subjects.add(new Subject(null, "INT261","BE Dev", 3.0));
        subjects.add(new Subject(null, "INT251", "FE Dev", 3.0));
        subjects.add(new Subject(null, "INT271", "Adv FE Dev", 3.0));
        subjects.add(new Subject(null, "INT281", "AdvBE Dev", 3.0));
        subjectService.createSubjectAll(subjects);
        return subjects;
    }

    @DeleteMapping("/{id}")
    public Subject updateSubject(@PathVariable Integer id) {
        return subjectService.deleteSubject(id);
    }
}
