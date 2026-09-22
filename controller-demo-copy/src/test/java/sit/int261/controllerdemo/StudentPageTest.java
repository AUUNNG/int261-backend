package sit.int261.controllerdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sit.int261.controllerdemo.controllers.StudentController;
import sit.int261.controllerdemo.entities.Student;
import sit.int261.controllerdemo.services.StudentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StudentPageTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentController studentController;

    @Test
    void testGetStudentsPageDefault() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = studentService.findAll(null, null, pageable);

        assertNotNull(page);
        assertEquals(10, page.getContent().size());
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        assertTrue(page.getTotalElements() > 10);
    }

    @Test
    void testGetStudentsPageWithNameFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = studentService.findAll("Su", null, pageable);

        assertNotNull(page);
        assertFalse(page.getContent().isEmpty());
        for (Student s : page.getContent()) {
            assertTrue(s.getName().toLowerCase().contains("su"));
        }
    }

    @Test
    void testGetStudentsPageWithIdsFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = studentService.findAll(null, List.of(67130500003L), pageable);

        assertNotNull(page);
        assertEquals(1, page.getContent().size());
        assertEquals(67130500003L, page.getContent().get(0).getId());
        assertEquals("Kittikorn Phaen", page.getContent().get(0).getName());
    }

    @Test
    void testGetStudentsPageWithMultipleIds() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = studentService.findAll(null, List.of(67130500001L, 67130500003L), pageable);

        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        List<Long> ids = page.getContent().stream().map(Student::getId).toList();
        assertTrue(ids.contains(67130500001L));
        assertTrue(ids.contains(67130500003L));
    }

    @Test
    void testGetStudentsPageWithSorting() {
        Sort sort = Sort.by(Sort.Order.desc("name"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(0, 5, sort);
        Page<Student> page = studentService.findAll(null, null, pageable);

        assertNotNull(page);
        assertEquals(5, page.getContent().size());
        assertEquals(5, page.getSize());
        assertEquals(0, page.getNumber());

        // Check descending name order
        for (int i = 0; i < page.getContent().size() - 1; i++) {
            String currName = page.getContent().get(i).getName();
            String nextName = page.getContent().get(i + 1).getName();
            assertTrue(currName.compareToIgnoreCase(nextName) >= 0);
        }
    }

    @Test
    void testGetStudentsPageWithBothNameAndIdsOrCondition() {
        Sort sort = Sort.by(Sort.Order.desc("name"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(0, 10, sort);
        // Kittikorn Phaen (67130500003) does not contain "Su", but there are 6 students with "Su".
        // With OR condition, it returns 6 + 1 = 7 students.
        Page<Student> page = studentService.findAll("Su", List.of(67130500003L), pageable);

        assertNotNull(page);
        assertEquals(7, page.getContent().size());
        assertEquals(7, page.getTotalElements());
        List<Long> ids = page.getContent().stream().map(Student::getId).toList();
        assertTrue(ids.contains(67130500003L));
    }

    @Test
    void testControllerEndpoint() {
        Sort sort = Sort.by(Sort.Order.desc("name"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Student> page = studentController.getStudentsPage("Su", List.of(67130500003L), pageable);

        assertNotNull(page);
        assertEquals(7, page.getContent().size());
        assertEquals(7, page.getTotalElements());
    }
}
