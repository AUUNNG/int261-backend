package sit.int261.restdemo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int261.restdemo.entities.Subject;
import sit.int261.restdemo.repositories.SubjectRepository;

import java.util.List;

@Service
public class SubjectService {
    @Autowired
    private SubjectRepository subjectRepository;
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }
    public Subject getSubjectById(Integer id) {
        return subjectRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Subject not found with id:" + id)
                );
    }
    public Subject createSubject (Subject subject) {
        return subjectRepository.save(subject);
    }
    public void createSubjectAll (List<Subject> subject) {
        subjectRepository.saveAll(subject);
    }
    public Subject updateSubject(Integer id, Subject
            subjectDetails) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Subject not found with id: " + id));
        subjectDetails.setId(subject.getId());
        return subjectRepository.save(subjectDetails);
    }
    public Subject deleteSubject(Integer id) {
        subjectRepository.deleteById(id);
        return null;
    }
}
