Feature: Balance Tests

  Background:
    * url baseUrl + "/v1"

  Scenario: GET returns 0 for unknown user
    Given path '/balance/MaxMuster'
    When method GET
    Then status 200
    Then assert response.balance == 0

  Scenario: PATCH allows to set the balance for MaxMuster
    Given path '/balance/MaxMuster'
    And request { balance: 5 }
    When method PATCH
    Then status 200
    And assert response.balance == 5

  Scenario: GET returns 5 for user MaxMuster
    Given path '/balance/MaxMuster'
    When method GET
    Then status 200
    Then assert response.balance == 5

  Scenario: POST allows to modify the value of MaxMuster
    Given path '/balance/MaxMuster/topup'
    And request { amount: 2 }
    When method POST
    Then status 200

  Scenario: GET returns 7 for MaxMuster
    Given path '/balance/MaxMuster'
    When method GET
    Then status 200
    Then assert response.balance == 7

  Scenario: POST updating with balance and balanceDifference at the same time should fail
    Given path '/balance/MaxMuster/withdraw'
    And request { amount: 3 }
    When method POST
    Then status 200

  Scenario: GET returns 4 for MaxMuster
    Given path '/balance/MaxMuster'
    When method GET
    Then status 200
    Then assert response.balance == 4

  Scenario: POST topup with negativ value should fail
    Given path '/balance/MaxMuster/topup'
    And request { amount: -3 }
    When method POST
    Then status 400
    And assert response.errorCode == 400005

  Scenario: POST withdraw with negative value should fail
    Given path '/balance/MaxMuster/withdraw'
    And request { amount: -3 }
    When method POST
    Then status 400
    And assert response.errorCode == 400005

  Scenario: PATCH with invalid body returns error
    Given path '/balance/MaxMuster'
    And request { foo: -3 }
    When method PATCH
    Then status 400
    And assert response.errorCode == 400005

  Scenario: POST topup with invalid body fails
    Given path '/balance/MaxMuster/topup'
    And request { foo: -3 }
    When method POST
    Then status 400
    And assert response.errorCode == 400005

  Scenario: POST withdraw with invalid body fails
    Given path '/balance/MaxMuster/withdraw'
    And request { foo: -3 }
    When method POST
    Then status 400
    And assert response.errorCode == 400005

  Scenario: POST withdraw with unknown user fails
    Given path '/balance/YouDontKnowMe/withdraw'
    And request { amount: 10 }
    When method POST
    Then status 404
    And assert response.errorCode == 404002

  Scenario: POST topup with unknown user fails
    Given path '/balance/YouDontKnowMe/withdraw'
    And request { amount: 10 }
    When method POST
    Then status 404
    And assert response.errorCode == 404002