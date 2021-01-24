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

  Scenario: Get own user data
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And print token

    Given path 'user'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', username: 'member', email: 'member@mail.com', memberId: 'memberId', password: '#notpresent', roles: '#array' }

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
    And request { username:  #(username),  password: #(password) }
    When method POST
    Then status 200
    And def rToken = response.token

    # Delete user
    Given path 'user'
    And header Authorization = "Bearer " + rToken
    When method DELETE
    Then status 204

    # Check that deletion was successful
    Given path 'user'
    And header Authorization = "Bearer " + rToken
    When method GET
    Then status 401
    And match response.errorCode == 401002

  Scenario: Registering with an empty email works
    Given path 'user', 'register'
    And request { username: "robby9", email: "", memberId: "55555", password: #(password) }
    When method Post
    Then status 201
    And match response.email == '#notpresent'

    # Try it a second time to make sure multiple accounts without an email are permitted
    Given path 'user', 'register'
    And request { username: "robby10", email: "", memberId: "55556", password: #(password) }
    When method Post
    Then status 201
    And match response.email == '#notpresent'

  Scenario: Registering with an invalid email does not work
    Given path 'user', 'register'
    And request { username: "robby11", email: "aaaaaa", memberId: "55557", password: #(password) }
    When method Post
    Then status 400

  Scenario: Add and delete role to a user
    # create new user
    Given path 'user', 'register'
    And request { username: "mustermann", email: "mustermann@test.com", memberId: "123456100", password: #(password) }
    When method POST
    Then status 201
    And def userID = response.id

    # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # add role TREASURER
    Given path 'user', userID, 'roles', 'TREASURER'
    And header Authorization = "Bearer " + oToken
    And request ''
    When method POST
    Then status 200
    And match response.roles contains 'TREASURER'

    # remove role TREASURER
    Given path 'user', userID, 'roles', 'TREASURER'
    And header Authorization = "Bearer " + oToken
    When method DELETE
    Then status 200
    And match response.roles !contains 'TREASURER'


  Scenario Outline: Cannot add or delete roles as member, orderer or treasurer
    # Login
    Given path 'auth', 'login'
    And request { username: '<username>',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Get User ID
    Given path 'user'
    And header Authorization = "Bearer " + oToken
    When method GET
    Then status 200
    And def userID = response.id

    # Can't add role
    Given path 'user', userID, 'roles', 'TREASURER'
    And header Authorization = "Bearer " + oToken
    And request ''
    When method POST
    Then status 401
    And match response.errorCode == 401005

    # Can't remove Role
    Given path 'user', userID, 'roles', 'TREASURER'
    And header Authorization = "Bearer " + oToken
    And request ''
    When method POST
    Then status 401
    And match response.errorCode == 401005

    Examples:
      | username   |
      | member     |
      | orderer    |
      | treasurer  |

  Scenario: Cannot add a role twice to a user
    # create new user
    Given path 'user', 'register'
    And request { username: "mustermann2", email: "mustermann2@test.com", memberId: "12384524456", password: #(password) }
    When method POST
    Then status 201
    And def userID = response.id

    # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Post role TREASURER
    Given path 'user', userID, 'roles', 'TREASURER'
    And header Authorization = "Bearer " + token
    And request ''
    When method POST
    Then status 200
    And match response.roles contains 'TREASURER'

    # Post role TREASURER again
    Given path 'user', userID, 'roles', 'TREASURER'
    And header Authorization = "Bearer " + token
    And request ''
    When method POST
    Then status 400
    And match response.errorCode == 400017

  Scenario: Cannot delete a role from a user that has not been added
    # Create User
    Given path 'user', 'register'
    And request { username: "mustermann3", email: "mustermann3@test.com", memberId: "12384524480", password: #(password) }
    When method POST
    Then status 201
    And def userID = response.id

    # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Delete role ADMIN for new user
    Given path 'user', userID, 'roles', 'ADMIN'
    And header Authorization = "Bearer " + token
    When method DELETE
    Then status 404
    And match response.errorCode == 404006
