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

  Scenario: Retrieve tokens for a user
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And match response contains { token: '#string', refreshToken: '#string' }
    And json accessToken = parseJwtPayload(response.token)

    Given path 'user'
    And header Authorization = "Bearer " + response.token
    When method GET
    Then status 200
    And match accessToken contains { sub: 'member', id: #(response.id) }

  Scenario: Login with incorrect password fails
    Given path 'auth', 'login'
    And request { username: 'member',  password: 'incorrect' }
    When method POST
    Then status 401
    And match response.errorCode == 401004

  Scenario: Login with unknown username fails
    Given path 'auth', 'login'
    And request { username: 'YouDontKnowMe',  password: 'incorrect' }
    When method POST
    Then status 401
    And match response.errorCode == 401004

  Scenario: An expired token cannot be used
    Given path 'user'
    And def token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyb2JieTUiLCJpZCI6IjJjYjAzYjZhLTA4YTAtNGJlZC05NzA3LWUxM2I0ZmZkYWFlOCIsImV4cCI6MTYxMDgyMTMxMiwiaWF0IjoxNjEwODIxMzEyfQ.eCDxY79jxul25oAHh0hbBhGnV0ywLMPvJzqxnlxFxTmW1jKmj_Cjq9C3-B5WdavGLPev2Sm8_Szr_JWQGAG8TA"
    And header Authorization = "Bearer " + token
    When method GET
    Then status 401
    And match response.errorCode == 401003

  Scenario: Use refresh token to retrieve a new set of tokens
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def refreshToken = response.refreshToken

    Given path 'auth', 'refresh'
    And request { refreshToken: #(refreshToken) }
    When method POST
    Then status 200
    And match response contains { token: '#string', refreshToken: '#string' }
    And def newRefreshToken = response.refreshToken
    And def newToken = response.token

    # Check that new token can be used to make a call
    Given path 'user'
    And header Authorization = "Bearer " + newToken
    When method GET
    Then status 200

    # Check that new refresh token can be used to get another set of tokens
    Given path 'auth', 'refresh'
    And request { refreshToken: #(newRefreshToken) }
    When method POST
    Then status 200

  Scenario: Revoke a refresh token
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def refreshToken = response.refreshToken

    # Revoke refresh token
    Given path 'auth', 'refresh'
    And header Authorization = "Bearer " + token
    And request { refreshToken: #(refreshToken) }
    When method DELETE
    Then status 204

    # check that it cannot be used to get new tokens
    Given path 'auth', 'refresh'
    And request { refreshToken: #(refreshToken) }
    When method POST
    Then status 401
    And match response.errorCode == 401009

  Scenario: Revoke all refresh token
    # Generate first refreshToken
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def refreshToken1 = response.refreshToken

    # Generate second refreshToken
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def refreshToken2 = response.refreshToken

    # Revoke all refresh tokens
    Given path 'auth', 'refresh', 'all'
    And header Authorization = "Bearer " + token
    When method DELETE
    Then status 204

    # check that first token was revoked
    Given path 'auth', 'refresh'
    And request { refreshToken: #(refreshToken1) }
    When method POST
    Then status 401
    And match response.errorCode == 401009

    # check that second token was revoked
    Given path 'auth', 'refresh'
    And request { refreshToken: #(refreshToken2) }
    When method POST
    Then status 401
    And match response.errorCode == 401009

  Scenario: Revoke all refresh token for another user
    # Generate first refreshToken
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def refreshToken1 = response.refreshToken

    # Get userID
    Given path 'user'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And def userID = response.id

    # Generate second refreshToken
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def refreshToken2 = response.refreshToken

    # Revoke all as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def adminToken = response.token

    Given path 'auth', 'refresh', 'all', userID
    And header Authorization = "Bearer " + adminToken
    When method DELETE
    Then status 204

    # check that first token was revoked
    Given path 'auth', 'refresh'
    And request { refreshToken: #(refreshToken1) }
    When method POST
    Then status 401
    And match response.errorCode == 401009

    # check that second token was revoked
    Given path 'auth', 'refresh'
    And request { refreshToken: #(refreshToken2) }
    When method POST
    Then status 401
    And match response.errorCode == 401009
