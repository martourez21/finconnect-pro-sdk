# Configuration Reference

## Quick Start

### application.yml
```yaml
finconnect:
  connections:
    swift:
      host: swift.bank.com
      port: 5001
      protocol: SWIFT_XML
      username: ${SWIFT_USER}
      password: ${SWIFT_PASS}
    rest:
      host: api.payments.com
      port: 443
      protocol: REST_API
Programmatic Configuration
java
@Configuration
public class BankConfig {
    @Bean
    public Map<ProtocolType, ConnectionConfigDto> connections() {
        return IntegrationGuide.createBankingConfiguration(
            "swift.bank.com",
            new String[]{"user", "pass"},
            "sepa.clearingsystem.com"
        );
    }
}
Connection Settings
Setting	Description	Default
timeoutMs	Connection timeout	30000
retryAttempts	Retry attempts	3
connectionName	Descriptive name	-
```