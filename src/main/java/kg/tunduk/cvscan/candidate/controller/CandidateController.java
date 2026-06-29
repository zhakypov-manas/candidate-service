package kg.tunduk.cvscan.candidate.controller;

import jakarta.validation.Valid;
import kg.tunduk.cvscan.candidate.dto.*;
import kg.tunduk.cvscan.candidate.model.CandidateStatus;
import kg.tunduk.cvscan.candidate.model.Verdict;
import kg.tunduk.cvscan.candidate.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    public ResponseEntity<CandidatePage> list(@RequestParam(required = false) Verdict verdict,
                                              @RequestParam(required = false) CandidateStatus status,
                                              @RequestParam(required = false) String position,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(candidateService.list(verdict, status, position, search, page, size, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(candidateService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CandidateResponse> create(@Valid @RequestBody CandidateWriteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> update(@PathVariable String id,
                                                    @Valid @RequestBody CandidateWriteRequest req) {
        return ResponseEntity.ok(candidateService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CandidateResponse> changeStatus(@PathVariable String id,
                                                          @Valid @RequestBody StatusChangeRequest req) {
        return ResponseEntity.ok(candidateService.changeStatus(id, req));
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<List<StatusHistoryEntry>> getStatusHistory(@PathVariable String id) {
        return ResponseEntity.ok(candidateService.getStatusHistory(id));
    }
}
