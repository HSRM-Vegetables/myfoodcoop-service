Feature: Balance Tests

  Background:
    * url baseUrl + "/v2"
    * def password = "a_funny_horse**jumps_high778"

  Scenario: PATCH allows to set the balance for user
    Given path 'user', 'register'
    And request { username: 'MaxMuster', email: 'MaxMuster@test.com', memberId: '42', password: #(password) }
    When method POST
    Then status 201
    And print response

    Given path 'user', 'login'
    And request { username: 'MaxMuster',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance'
    And header Authorization = "Bearer " + token
    And request { balance: 5 }
    When method PATCH
    Then status 200
    And assert response.balance == 5

  Scenario: GET returns balance for user
    Given path 'user', 'register'
    And request { username: 'MaxMuster2', email: 'MaxMuster2@test.com', memberId: '43', password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'login'
    And request { username: 'MaxMuster2',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 0

  Scenario: Topup balance of user
    Given path 'user', 'register'
    And request { username: 'MaxMuster3', email: 'MaxMuster3@test.com', memberId: '44', password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'login'
    And request { username: 'MaxMuster3',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance/topup'
    And header Authorization = "Bearer " + token
    And request { amount: 2 }
    When method POST
    Then status 200
    And match response contains { balance: 2.0 }

    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 2.0

  Scenario: Withdraw from balance
    Given path 'user', 'register'
    And request { username: 'MaxMuster4', email: 'MaxMuster4@test.com', memberId: '45', password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'login'
    And request { username: 'MaxMuster4',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance/withdraw'
    And header Authorization = "Bearer " + token
    And request { amount: 3 }
    When method POST
    Then status 200

    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == -3.0

  Scenario: POST topup with negativ value should fail
    Given path 'user', 'register'
    And request { username: 'MaxMuster5', email: 'MaxMuster5@test.com', memberId: '46', password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'login'
    And request { username: 'MaxMuster5',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance/topup'
    And header Authorization = "Bearer " + token
    And request { amount: -3 }
    When method POST
    Then status 400
    And assert response.errorCode == 400005

  Scenario: POST withdraw with negative value should fail
    Given path 'user', 'register'
    And request { username: 'MaxMuster6', email: 'MaxMuster6@test.com', memberId: '47', password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'login'
    And request { username: 'MaxMuster6',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance/withdraw'
    And header Authorization = "Bearer " + token
    And request { amount: -3 }
    When method POST
    Then status 400
    And assert response.errorCode == 400005

  Scenario: PATCH with invalid body returns error
    Given path 'user', 'register'
    And request { username: 'MaxMuster7', email: 'MaxMuster7@test.com', memberId: '48', password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'login'
    And request { username: 'MaxMuster7',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance'
    And header Authorization = "Bearer " + token
    And request { foo: -3 }
    When method PATCH
    Then status 400
    And assert response.errorCode == 400005

  Scenario: POST topup with invalid body fails
    Given path 'user', 'register'
    And request { username: 'MaxMuster8', email: 'MaxMuster8@test.com', memberId: '49', password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'login'
    And request { username: 'MaxMuster8',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance/topup'
    And header Authorization = "Bearer " + token
    And request { foo: -3 }
    When method POST
    Then status 400
    And assert response.errorCode == 400005

  Scenario: POST withdraw with invalid body fails
    Given path 'user', 'register'
    And request { username: 'MaxMuster9', email: 'MaxMuster9@test.com', memberId: '50', password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'login'
    And request { username: 'MaxMuster9',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance/withdraw'
    And header Authorization = "Bearer " + token
    And request { foo: -3 }
    When method POST
    Then status 400
    And assert response.errorCode == 400005

  Scenario: GET /balance requires authorization
    Given path '/balance'
    When method GET
    Then status 401
    And match response.errorCode == 401005

  Scenario: PATCH /balance requires authorization
    Given path '/balance'
    And request { balance: 5 }
    When method PATCH
    Then status 401
    And match response.errorCode == 401005

  Scenario: POST /balance/topup requires authorization
    Given path '/balance/topup'
    And request { amount: 5 }
    When method POST
    Then status 401
    And match response.errorCode == 401005

  Scenario: POST /balance/withdraw requires authorization
    Given path '/balance/withdraw'
    And request { amount: 5 }
    When method POST
    Then status 401
    And match response.errorCode == 401005