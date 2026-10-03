package in.rikcapital.stockalert.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record StockAlertRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be 100 characters or fewer")
        String name,

        @NotBlank(message = "Email ID is required")
        @Email(message = "Enter a valid email ID")
        @Size(max = 150, message = "Email must be 150 characters or fewer")
        String email,

        @NotBlank(message = "Phone Number is required")
        @Size(max = 30, message = "Phone number is too long")
        String phone,

        @NotEmpty(message = "Select at least one company")
        @Size(max = 5, message = "You can select up to 5 companies")
        List<String> companies
) {}
