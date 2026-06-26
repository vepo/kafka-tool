Feature: Kafka broker validation
  Broker profiles must be validated before they are saved or used to connect.

  Scenario: Accept a valid broker profile
    Given a broker with name, bootstrap servers, and schema registry URL
    When the broker is validated
    Then validation succeeds

  Scenario: Reject a blank broker name
    Given a broker with an empty name
    When the name is validated
    Then validation fails

  Scenario: Reject a duplicate broker name
    Given an existing broker named Local
    When another broker named local is validated
    Then validation fails

  Scenario: Reject invalid bootstrap server values
    Given bootstrap server strings with missing, malformed, or out-of-range values
    When each value is validated
    Then validation fails for empty, host-only, trailing-comma, and invalid port values

  Scenario: Accept comma-separated bootstrap servers
    Given multiple host:port pairs
    When the bootstrap servers are validated
    Then validation succeeds

  Scenario: Accept a missing schema registry URL
    Given no schema registry URL
    When validation runs
    Then blank and null URLs are accepted

  Scenario: Reject an invalid schema registry URL
    Given URLs without http/https or with an invalid scheme
    When each URL is validated
    Then validation fails
