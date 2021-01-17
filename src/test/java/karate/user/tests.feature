Feature: User controller

  Background:
    * url baseUrl + "/v2"
    * def password = "a_funny_horse**jumps_high778"
    * def parseJwtPayload =
    """
    function(token) {
        var base64Url = token.split('.')[1];
        var base64Str = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        var Base64 = Java.type('java.util.Base64');
        var decoded = Base64.getDecoder().decode(base64Str);
        var String = Java.type('java.lang.String');
        return new String(decoded);
    }
    """

  Scenario: Create a user
    Given path 'user', 'register'
    * def username = "robby"
    * def email = "robby@test.com"
    * def memberId = "42"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And match response == { id: '#uuid', username: #(username), email: #(email), memberId: #(memberId), password: '#notpresent' }

  Scenario: Cannot create a user with already registered username
    Given path 'user', 'register'
    And request { username: 'robby2', email: "robby2@test.com", memberId: "43", password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'register'
    And request { username: 'robby2', email: 'not-robby2@test.com', memberId: "44", password: #(password) }
    When method POST
    Then status 400
    And match response.errorCode == 400015

  Scenario: Cannot create a user with already registered email
    Given path 'user', 'register'
    And request { username: "robby3", email: "robby3@test.com", memberId: "44", password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'register'
    And request { username: 'not-robby3', email:"robby3@test.com", memberId: "45", password: #(password) }
    When method POST
    Then status 400
    And match response.errorCode == 400014

  Scenario: Cannot create a user with already registered memberId
    Given path 'user', 'register'
    And request { username: "robby4", email: "robby4@test.com", memberId: "46", password: #(password) }
    When method POST
    Then status 201

    Given path 'user', 'register'
    And request { username: 'not-robby4', email: "not-robby4@test.com", memberId: "46", password: #(password) }
    When method POST
    Then status 400
    And print response
    And match response.errorCode == 400016

  Scenario: Retrieve a token for a user
    Given path 'user', 'register'
    And request { username: 'robby5', email: 'robby5@test.com', memberId: '4225', password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    Given path 'auth', 'login'
    And request { username: 'robby5',  password: #(password) }
    When method POST
    Then status 200
    And match response contains { token: '#string', refreshToken: '#string' }
    And json accessToken = parseJwtPayload(response.token)
    And match accessToken contains { sub: 'robby5', id: #(userId) }
    And print response.token

  Scenario: Login with incorrect password fails
    Given path 'user', 'register'
    And request { username: 'robby6', email: 'robby6@test.com', memberId: '42255', password: #(password) }
    When method POST
    Then status 201

    Given path 'auth', 'login'
    And request { username: 'robby6',  password: 'incorrect' }
    When method POST
    Then status 401
    And match response.errorCode == 401004

  Scenario: Login with unknown username fails
    Given path 'auth', 'login'
    And request { username: 'YouDontKnowMe',  password: 'incorrect' }
    When method POST
    Then status 401
    And match response.errorCode == 401004

  Scenario: Get own user data
    Given path 'user', 'register'
    * def username = "robby7"
    * def email = "robby7@test.com"
    * def memberId = "42226"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    Given path 'auth', 'login'
    And request { username: 'robby7',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And print token

    Given path 'user'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response == { id: #(userId), username: #(username), email: #(email), memberId: #(memberId), password: '#notpresent' }

  Scenario: An expired token cannot be used
    Given path 'user'
    And def token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyb2JieTUiLCJpZCI6IjJjYjAzYjZhLTA4YTAtNGJlZC05NzA3LWUxM2I0ZmZkYWFlOCIsImV4cCI6MTYxMDgyMTMxMiwiaWF0IjoxNjEwODIxMzEyfQ.eCDxY79jxul25oAHh0hbBhGnV0ywLMPvJzqxnlxFxTmW1jKmj_Cjq9C3-B5WdavGLPev2Sm8_Szr_JWQGAG8TA"
    And header Authorization = "Bearer " + token
    When method GET
    Then status 401
    And match response.errorCode == 401003

  Scenario: GET /user requires authorization
    Given path 'user'
    When method GET
    Then status 401
    And match response.errorCode == 401005

  Scenario: Delete a user
    Given path 'user', 'register'
    * def username = "robby8"
    * def email = "robby8@test.com"
    * def memberId = "42228"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    Given path 'auth', 'login'
    And request { username: 'robby7',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And print token

    Given path 'user'
    And header Authorization = "Bearer " + token
    When method DELETE
    Then status 204

    Given path 'user'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 401
    And match response.errorCode == 401002
