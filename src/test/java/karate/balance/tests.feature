Feature: Balance Tests

  Background:
    * url baseUrl + "/v2"
    * def password = "a_funny_horse**jumps_high778"

  Scenario: PATCH allows to set the balance for user
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
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
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then match response.balance == '#number'

  Scenario: Topup balance of user
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    * def newBalance = response.balance + 2.0

    Given path '/balance/topup'
    And header Authorization = "Bearer " + token
    And request { amount: 2 }
    When method POST
    Then status 200
    And match response contains { balance: #(newBalance) }

    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then match response.balance == newBalance

  Scenario: Withdraw from balance
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    * def newBalance = response.balance - 3.0

    Given path '/balance/withdraw'
    And header Authorization = "Bearer " + token
    And request { amount: 3 }
    When method POST
    Then status 200
    And match response contains { balance: #(newBalance) }

    Given path '/balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    Then match response.balance == newBalance

  Scenario: POST topup with negativ value should fail
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
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
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
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
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
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
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
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
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
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

  Scenario: GET /balance/history requires authorization
    Given path '/balance/history'
    When method GET
    Then status 401
    And match response.errorCode == 401005

  Scenario: New user has a balance of 0
    Given path 'user', 'register'
    And request { username: 'MaxMuster', email: 'MaxMuster8@test.com', memberId: '49', password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    # Login admin to grant MaxMuster MEMBER role
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def cToken = response.token

    # Grant role MEMBER to MaxMuster
    Given path 'user', userId, 'roles', 'MEMBER'
    And header Authorization = "Bearer " + cToken
    And request {}
    When method POST
    Then status 200

    Given path 'auth', 'login'
    And request { username: 'MaxMuster',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    Given path 'balance'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.balance == 0
