package sit.int261.dtodemo.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int261.dtodemo.dtos.JwtRequestUser;
import sit.int261.dtodemo.properties.AppProperties;
import sit.int261.dtodemo.services.FileService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
class FileController {
    @Autowired
    private AppProperties appProperties;

    @Autowired
    private FileService fileService;

    @GetMapping("/test")
    public ResponseEntity<Object> getAllProperties() {
        return ResponseEntity.ok(fileService.getFileStorageLocation()+ " has been created !!!");
    }

    @PostMapping
    public ResponseEntity<Object> fileUpload(@RequestParam("file") MultipartFile file) {
        fileService.store(file);
        return ResponseEntity.ok("You successfully uploaded " + file.getOriginalFilename());
    }

    @GetMapping("/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(
            @PathVariable String filename) {
        Resource file = fileService.loadFileAsResource(filename);
        return ResponseEntity.ok().contentType(MediaType.valueOf(fileService.getFileType(file))).body(file);
    }

    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<Object> removeFile(@PathVariable String filename) {
        fileService.removeFile(filename);
        return ResponseEntity.ok(filename+ " has been removed !");
    }

    @PostMapping("/multiparts")
    public ResponseEntity<Object> createUserDemo2( @ModelAttribute JwtRequestUser user
            , @RequestPart List<MultipartFile> files) {
        List<String> success = fileService.store(files);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("success", success);
        return ResponseEntity.ok(result);
    }
}
