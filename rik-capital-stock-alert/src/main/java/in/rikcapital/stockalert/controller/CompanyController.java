package in.rikcapital.stockalert.controller;

import in.rikcapital.stockalert.model.Company;
import in.rikcapital.stockalert.service.CompanyExcelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyExcelService companyExcelService;

    public CompanyController(
            CompanyExcelService companyExcelService) {

        this.companyExcelService = companyExcelService;
    }

    @GetMapping("/search")
    public List<Company> search(
            @RequestParam(defaultValue = "") String q) {

        return companyExcelService.search(q, 20);
    }
}