# Sante Diagnostics LIMS

Installation and run guide for Apache NetBeans.

## Requirements

- Java 21
- Apache NetBeans
- PostgreSQL
- pgAdmin 4

## Setup

1. Open pgAdmin 4 and create a PostgreSQL database named `lims_db`.
2. Open the database and run the contents of `schema.sql` to create the tables.
3. Open the project in Apache NetBeans.
4. Right-click `DatabaseSeeder.java` and choose **Run File**.
5. Confirm the seeder prints a success message in the Output window.

## Default Login

After running the seeder, log in with:

- Email: `admin@sante.com`
- Password: `admin123`

## Email Setup

If you want Gmail email notifications to work, open `EmailService.java` and set:

- `SMTP_USERNAME` to your Gmail address
- `SMTP_PASSWORD` to your Gmail App Password

If these values are left blank, the app prints the email message to the console instead of sending it.

## Run the App

1. In NetBeans, right-click the project.
2. Click **Run** or **Run Project**.
3. The app starts from `lims.App` and opens the login screen.

## Notes

- The app connects to PostgreSQL at `jdbc:postgresql://localhost:5433/lims_db`.
- Update the database settings in `DatabaseConnection.java` if your local PostgreSQL setup is different.
