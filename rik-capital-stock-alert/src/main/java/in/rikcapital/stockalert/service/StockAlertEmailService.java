package in.rikcapital.stockalert.service;

import in.rikcapital.stockalert.model.StockAlertRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class StockAlertEmailService {

    private final JavaMailSender mailSender;
    private final String adminEmail;
    private final String fromEmail;

    public StockAlertEmailService(
            JavaMailSender mailSender,
            @Value("${stock-alert.admin-email}") String adminEmail,
            @Value("${spring.mail.username}") String fromEmail) {

        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
        this.fromEmail = fromEmail;
    }

    public void send(StockAlertRequest request) {

        try {

            System.out.println("Sending Stock Alert email...");

            StringBuilder companies = new StringBuilder();

            for (int i = 0; i < request.companies().size(); i++) {
                companies.append(i + 1)
                        .append(". ")
                        .append(request.companies().get(i))
                        .append("\n");
            }

            // Existing mail content kept unchanged.
            String emailBody =
                    "Dear RIK Capital Team,\n\n"
                            + "A new Stock Alert request has been submitted "
                            + "through the RIK Capital website.\n\n"

                            + "USER DETAILS\n"
                            + "--------------------------------\n"
                            + "Name: " + request.name() + "\n"
                            + "Email ID: " + request.email() + "\n"
                            + "Phone Number: " + request.phone() + "\n\n"

                            + "SELECTED COMPANIES\n"
                            + "--------------------------------\n"
                            + companies

                            + "\nPlease review the request and take "
                            + "the necessary action.\n\n"

                            + "Regards,\n"
                            + "RIK Capital\n"
                            + "Strategic Investor Relations Advisory";

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(adminEmail);
            mail.setReplyTo(request.email());
            mail.setSubject("Stock Alert Request – RIK Capital");
            mail.setText(emailBody);

            mailSender.send(mail);

            System.out.println("======================================");
            System.out.println("EMAIL SENT SUCCESSFULLY");
            System.out.println("Sent From: " + fromEmail);
            System.out.println("Sent To: " + adminEmail);
            System.out.println("======================================");

        } catch (Exception e) {

            System.err.println("======================================");
            System.err.println("EMAIL SENDING FAILED");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("======================================");

            throw new RuntimeException(
                    "Unable to send email: " + e.getMessage(), e
            );
        }
    }
}
