# RIK Capital — Stock Alert (Java / Spring Boot)

This project adds a **Stock Alert** button beside the existing search box and a dedicated `/stock-alert.html` page.

## What it does
- Search companies by **name, code, or symbol**.
- Company records are loaded from an Excel `.xlsx` file with Apache POI.
- Select a maximum of **5 companies**.
- Each selected company has a **×** removal button.
- Collects **Name, Email ID, Phone Number**.
- Sends a simple plain-text notification to the admin using the **Resend HTTP email API**.
- **No SMTP / JavaMail configuration is used.**
- Displays **Successfully Submitted** and redirects to `/` after 1.8 seconds.
- Server validates the selected companies against the Excel data.

## Email sent to admin

**Subject:** `Stock Alert Request – RIK Capital`

```text
Dear Admin,

A new Stock Alert request has been submitted through the RIK Capital website.

User Details

Name: [Name]
Email ID: [Email]
Phone Number: [Phone]

Selected Companies

1. [Company Name]
2. [Company Name]
3. [Company Name]

Please review the request and take the necessary action.

Regards,
RIK Capital
Strategic Investor Relations Advisory
```

## Email delivery — REST API, not SMTP

The backend calls the Resend email REST endpoint over HTTPS:

`POST https://api.resend.com/emails`

Set these environment variables:

```text
RESEND_API_KEY=re_xxxxxxxxxxxxxxxxx
STOCK_ALERT_ADMIN_EMAIL=admin@rikcapital.in
STOCK_ALERT_FROM_EMAIL=RIK Capital <alerts@rikcapital.in>
```

`STOCK_ALERT_FROM_EMAIL` must use a sender/domain that is permitted by your email provider. For production, verify your RIK Capital sending domain in Resend before using `alerts@rikcapital.in`.

You can optionally override the API endpoint with:

```text
STOCK_ALERT_EMAIL_API_URL=https://api.resend.com/emails
```

The API key is never placed in the browser or frontend JavaScript; it stays on the Java backend.

## Excel format
Put your actual Excel file at:
`src/main/resources/data/SE_Listed_Companies - FY27 - Rik_Capital.xlsx`

The first row should contain headers. Recommended headers:
`Company Name | Code | Symbol`

The loader also accepts `Name`, `Company`, `Company Code`, `Ticker`, or `Ticker Symbol` for the corresponding fields.

## Run
Requires Java 17+ and Maven.

```bash
mvn spring-boot:run
```

Open:
`http://localhost:8080/`

Stock Alert page:
`http://localhost:8080/stock-alert.html`

## Production notes
- Keep `RESEND_API_KEY` in environment variables or a secrets manager, never in source control.
- Keep the API key only on the server; never expose it to frontend JavaScript.
- Put the real company Excel file on the server and set `STOCK_ALERT_EXCEL_FILE=/path/to/companies.xlsx` if it should live outside the application JAR.
- For a public deployment, add CAPTCHA/rate limiting and HTTPS to reduce automated form abuse.


## Integrated company data
The project includes the supplied `SE_Listed_Companies - FY27 - Rik_Capital.xlsx` workbook and reads the `Q2_Potential_Client` sheet. Company Name is taken from the workbook; NSE Symbol and BSE Scrip Code are used for symbol/code searches. The sheet has a two-row header, which the backend handles automatically.

## Admin email configuration (REST API - no SMTP)

The website visitor does **not** need to provide the admin email. Keep the destination admin email on the server so it cannot be changed by a visitor.

Set these environment variables before starting the application:

- `RESEND_API_KEY` = your Resend API key
- `STOCK_ALERT_ADMIN_EMAIL` = the RIK Capital admin/operations email address that should receive Stock Alert submissions
- `STOCK_ALERT_FROM_EMAIL` = a verified sender, e.g. `RIK Capital <alerts@rikcapital.in>`

Example:

```text
RESEND_API_KEY=re_xxxxxxxxx
STOCK_ALERT_ADMIN_EMAIL=admin@rikcapital.in
STOCK_ALERT_FROM_EMAIL=RIK Capital <alerts@rikcapital.in>
```

When a user submits the form, the backend sends the user's Name, Email ID, Phone Number and selected companies to `STOCK_ALERT_ADMIN_EMAIL` through the Resend HTTPS API. The user's email is used as Reply-To.

### If you want the admin email to be supplied during installation

Ask the RIK Capital administrator for the destination mailbox (for example, `service@rikcapital.in`) and set that address in `STOCK_ALERT_ADMIN_EMAIL`. Do not put the admin address in the public HTML/JavaScript and do not ask website visitors for it.

### Header changes in this version

- Removed the three-line hamburger icon from the left side.
- Replaced the left-side header branding with the supplied RIK Capital company logo.
- Removed the red cross/clear icon from the right side.
- Removed the account dropdown chevron (`v`).
- The circular account icon remains clickable for Login / Logout.
