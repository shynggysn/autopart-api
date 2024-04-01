package kz.csi.test_task.controller;

import jakarta.validation.Valid;
import kz.csi.test_task.dto.AutopartDto;
import kz.csi.test_task.payload.request.AddPartRequest;
import kz.csi.test_task.service.AutopartService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/autopart")
@RequiredArgsConstructor
@CrossOrigin
public class AutopartController {
    private final AutopartService service;

    @GetMapping("/list")
    public ResponseEntity<List<AutopartDto>> getAutopartList() {
        return ResponseEntity.ok(service.getAutopartList());
    }
    @PostMapping
    public ResponseEntity<AutopartDto> addAutopart(@RequestBody @Valid AddPartRequest request) {
        var dto = service.addAutopart(request);
        return Objects.nonNull(dto) ? ResponseEntity.ok(dto) : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletAutopart(@PathVariable Long id) {
        return service.deleteAutopart(id) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @GetMapping(value = "/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> generateExcel() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Workbook workbook = service.generateExcel();
        workbook.write(outputStream);
        workbook.close();
        byte[] excelContent = outputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "excel.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.CREATED);
    }

}
