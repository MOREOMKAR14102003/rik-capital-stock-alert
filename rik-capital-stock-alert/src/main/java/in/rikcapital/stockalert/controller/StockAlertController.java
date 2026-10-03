package in.rikcapital.stockalert.controller;

import in.rikcapital.stockalert.model.StockAlertRequest;
import in.rikcapital.stockalert.service.CompanyExcelService;
import in.rikcapital.stockalert.service.StockAlertEmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-alert")
public class StockAlertController {

    private final StockAlertEmailService emailService;
    private final CompanyExcelService companyService;

    public StockAlertController(
            StockAlertEmailService emailService,
            CompanyExcelService companyService) {

        this.emailService = emailService;
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submit(
            @Valid @RequestBody StockAlertRequest request) {

        try {

            if (request.companies() == null ||
                    request.companies().isEmpty()) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message",
                                "Please select at least one company."
                        )
                );
            }

            if (request.companies().size() > 5) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message",
                                "You can select up to 5 companies."
                        )
                );
            }

            List<String> invalid = request.companies()
                    .stream()
                    .filter(v -> !companyService.exists(v))
                    .toList();

            if (!invalid.isEmpty()) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message",
                                "One or more selected companies are invalid: "
                                        + String.join(", ", invalid)
                        )
                );
            }

            // Send email
            emailService.send(request);

            Map<String, Object> body = new LinkedHashMap<>();

            body.put("success", true);
            body.put("message", "Successfully Submitted");

            return ResponseEntity.ok(body);

        } catch (Exception e) {

            System.err.println("======================================");
            System.err.println("STOCK ALERT SUBMISSION FAILED");
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.err.println("======================================");

            Map<String, Object> body = new LinkedHashMap<>();

            body.put("success", false);
            body.put(
                    "message",
                    "Email could not be sent: "
                            + (e.getMessage() != null
                            ? e.getMessage()
                            : "Unknown error")
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(body);
        }
    }
}