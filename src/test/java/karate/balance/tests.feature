Feature: Balance Tests

  Background:
    * url baseUrl

  Scenario: GET returns 0 for unknown user
    Given path '/balance/MaxMuster'
    When method GET
    Then status 200
    Then assert response.balance == 0

  Scenario: POST allows to set the balance for MaxMuster
    Given path '/balance/MaxMuster'
    And request { balance: 5 }
    When method POST
    Then status 200

  Scenario: GET returns 5 for user MaxMuster
    Given path '/balance/MaxMuster'
    When method GET
    Then status 200
    Then assert response.balance == 5

  Scenario: POST allows to modify the value of MaxMuster
    Given path '/balance/MaxMuster'
    And request { balanceDifference: 2 }
    When method POST
    Then status 200

  Scenario: GET returns 7 for MaxMuster
    Given path '/balance/MaxMuster'
    When method GET
    Then status 200
    Then assert response.balance == 7

  Scenario: POST updating with balance and balanceDifference at the same time should fail
    Given path '/balance/MaxMuster'
    And request { balance: 5, balanceDifference: 2 }
    When method POST
    Then status 400
