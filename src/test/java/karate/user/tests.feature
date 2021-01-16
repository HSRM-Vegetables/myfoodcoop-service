Feature: User controller

  Background:
    * url baseUrl
    * def password = "a_funny_horse**jumps_high778"

  Scenario: Create a user
    Given path 'v2', 'user', 'register'
    * def username = "robby"
    * def email = "robby@test.com"
    * def memberId = "42"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And match response == { id: '#uuid', username: #(username), email: #(email), memberId: #(memberId), password: '#notpresent' }

  Scenario: Cannot create a user with already registered username
    Given path 'v2', 'user', 'register'
    And request { username: 'robby2', email: "robby2@test.com", memberId: "43", password: #(password) }
    When method POST
    Then status 201

    Given path 'v2', 'user', 'register'
    And request { username: 'robby2', email: 'not-robby2@test.com', memberId: "44", password: #(password) }
    When method POST
    Then status 400
    And match response.errorCode == 400015

  Scenario: Cannot create a user with already registered email
    Given path 'v2', 'user', 'register'
    And request { username: "robby3", email: "robby3@test.com", memberId: "44", password: #(password) }
    When method POST
    Then status 201

    Given path 'v2', 'user', 'register'
    And request { username: 'not-robby3', email:"robby3@test.com", memberId: "45", password: #(password) }
    When method POST
    Then status 400
    And match response.errorCode == 400014

  Scenario: Cannot create a user with already registered memberId
    Given path 'v2', 'user', 'register'
    And request { username: "robby4", email: "robby4@test.com", memberId: "46", password: #(password) }
    When method POST
    Then status 201

    Given path 'v2', 'user', 'register'
    And request { username: 'not-robby4', email: "not-robby4@test.com", memberId: "46", password: #(password) }
    When method POST
    Then status 400
    And print response
    And match response.errorCode == 400016
