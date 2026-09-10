package org.greenstone.backend.admin.report;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/reports")
public class AdminReportController { private final AdminReportService service; public AdminReportController(AdminReportService s){service=s;} @GetMapping("/summary") public AdminReportSummaryResponse summary(){return service.summary();} }
