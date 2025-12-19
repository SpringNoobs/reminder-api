# Reminder API

Reliably notify users about upcoming deadlines via email, with built-in retries and failure handling.
So you don't have to worry about missed notifications.

## Features

- **Automated reminders** sent at 10, 5, and 2 days before due dates
- **Reliable email delivery** with automatic retries on failure
- **User-controlled opt-out** via email links—no account required
- **REST API** ready to integrate with any application
- **Docker-based deployment** for quick setup

## Quick Start

1. **Clone the repository:**
```bash
git clone https://github.com/SpringNoobs/reminder-api.git
cd reminder-api
```

2. **Configure SMTP credentials** in `docker-compose.yml`:
```yaml
MAIL_HOST: smtp.example.com
MAIL_PORT: 587
MAIL_USERNAME: your_email@example.com
MAIL_PASSWORD: your_password
```

3. **Start the application:**
```bash
docker-compose up -d
```

The API will be available at `http://localhost:8080`

## API Examples

**Create a reminder:**
```bash
curl -X POST http://localhost:8080/reminders \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Project Deadline",
    "dueDate": "2025-12-31T23:59:59Z",
    "user": {
      "firstName": "John",
      "lastName": "Doe",
      "contactRequestDTO": {
        "email": "john@example.com",
        "phoneNumber": "+1234567890"
      }
    }
  }'
```

**Disable notifications:**
```bash
curl -X PATCH http://localhost:8080/reminders/1/disable-email
```

**Full API documentation** is available after starting the application:
- 📘 English: `http://localhost:8080/docs/index-en-US.html`
- 📙 Portuguese: `http://localhost:8080/docs/index-pt-BR.html`

For development, use the [requests-example.http](requests-example.http) file with your IDE's HTTP client.

## Configuration

Required environment variables:
- `DB_CONNECTION_URL`, `DB_USERNAME`, `DB_PASSWORD` - Database connection
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` - SMTP credentials

See [docker-compose.yml](docker-compose.yml) for all available options.

## Operational Notes

- Failed email deliveries are automatically logged and retried every 20 minutes
- Users can disable future notifications at any time via the link in their email
- Reminders are scheduled using persistent job storage—server restarts won't lose scheduled notifications
- All reminder triggers are cleaned up when a reminder is deleted

## Testing

Run the test suite:
```bash
./mvnw test
```

View code coverage report:
```bash
./mvnw verify
# Report available at: target/site/jacoco/index.html
```

## Contributing

We welcome contributions! See [CONTRIBUTING.md](docs/CONTRIBUTING.md) for guidelines.

**Quick start for contributors:**
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Run `./mvnw spotless:apply` to format your code
4. Commit your changes (`git commit -m 'feat: add amazing feature'`)
5. Push and open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Architecture

The application follows a layered architecture with a Controller-Service-Repository pattern. Job scheduling is handled by Quartz with JDBC persistence, and email delivery includes a sophisticated retry mechanism with failure tracking. For detailed architectural decisions, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

**Built by [SpringNoobs](https://github.com/SpringNoobs)**

*For questions or support, please open an issue on GitHub.*
