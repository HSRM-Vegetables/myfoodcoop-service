Feature: User controller

  Background:
    * url baseUrl + "/v2"
    * def password = "a_funny_horse**jumps_high778"
    * def getUserIdFromToken =
    """
    function(token) {
        var base64Url = token.split('.')[1];
        var base64Str = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        var Base64 = Java.type('java.util.Base64');
        var decoded = Base64.getDecoder().decode(base64Str);
        var String = Java.type('java.lang.String');
        var decodedAsString = new String(decoded);
        var decodedAsObject = JSON.parse(decodedAsString);
        return decodedAsObject.id;
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
    And match response == { id: '#uuid', username: #(username), email: #(email), memberId: #(memberId), isDeleted: false, password: '#notpresent' }

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
    * def userId = getUserIdFromToken(token)

    Given path 'user', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', username: 'member', email: 'member@mail.com', memberId: 'memberId', isDeleted: false, password: '#notpresent', roles: '#array' }

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

     # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # add role MEMEBR
    Given path 'user', userId, 'roles', 'MEMBER'
    And header Authorization = "Bearer " + oToken
    And request ''
    When method POST
    Then status 200
    And match response.roles contains 'MEMBER'

    Given path 'auth', 'login'
    And request { username:  #(username),  password: #(password) }
    When method POST
    Then status 200
    And def rToken = response.token

    # Delete user
    Given path 'user', userId
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

  Scenario: User without Member-Role can not access

    Given path 'user', 'register'
    * def username = "robbyTest"
    * def email = "robbyTest@test.com"
    * def memberId = "42228123"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    # try to log in without membership
    Given path 'auth', 'login'
    And request { username:  #(username),  password: #(password) }
    When method POST
    Then status 401
    And def rToken = response.token

  Scenario Outline: Cannot add or delete roles as member, orderer or treasurer
    # Login
    Given path 'auth', 'login'
    And request { username: '<username>',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token
    * def userId = getUserIdFromToken(oToken)

    # Can't add role
    Given path 'user', userId, 'roles', 'TREASURER'
    And header Authorization = "Bearer " + oToken
    And request ''
    When method POST
    Then status 401
    And match response.errorCode == 401005

    # Can't remove Role
    Given path 'user', userId, 'roles', 'TREASURER'
    And header Authorization = "Bearer " + oToken
    And request ''
    When method POST
    Then status 401
    And match response.errorCode == 401005

    Examples:
      | username  |
      | member    |
      | orderer   |
      | treasurer |

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

  Scenario: Cannot remove role admin when user is the last admin
    # depends on class BaseTest that there is only one admin

    # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    * def userId = getUserIdFromToken(token)

    # try to remove role admin
    Given path 'user', userId, 'roles', 'ADMIN'
    And header Authorization = "Bearer " + token
    When method DELETE
    Then status 400
    And match response.errorCode == 400018

  Scenario: Get user data from another user
    # Create User
    Given path 'user', 'register'
    * def username = "mustermann4"
    * def email = "mustermann4@test.com"
    * def memberId = "12384524481"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userID = response.id

    # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Get data from new user
    Given path 'user', userID
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response == { id: '#uuid', username: #(username), email: #(email), memberId: #(memberId), isDeleted: false, password: '#notpresent', roles: '#array' }

  Scenario: Error when retrieving user data from invalid user-id
    # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Get data from unknown user
    Given path 'user', 'ZZZZZ9999'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 404
    And match response.errorCode == 404005

  Scenario: Error when deleting invalid user-id
    # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Delete unknown user
    Given path 'user', 'ZZZZZ9999'
    And header Authorization = "Bearer " + token
    When method DELETE
    Then status 404
    And match response.errorCode == 404005

  Scenario: User is softDeleted after delete
    # Create User
    Given path 'user', 'register'
    * def username = "mustermann5"
    * def email = "mustermann5@test.com"
    * def memberId = "12384524482"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userID = response.id
    And match response == { id: '#uuid', username: #(username), email: #(email), memberId: #(memberId), isDeleted: false, password: '#notpresent' }

    # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Delete new user
    Given path 'user', userID
    And header Authorization = "Bearer " + token
    When method DELETE
    Then status 204

    # Get user data
    Given path 'user', userID
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response == { id: '#uuid', username: #(username), email: #(email), memberId: #(memberId), isDeleted: true, password: '#notpresent', roles: '#array' }

  Scenario: Get all Users as admin
    # Login as admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    Given path 'user'
    And header Authorization = "Bearer " + oToken
    When method GET
    Then status 200
    And match response == { users: '#array' }
    And match response.users[*].username contains 'admin'

  Scenario: Get all Users as treasurer
      # Login as treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    Given path 'user'
    And header Authorization = "Bearer " + oToken
    When method GET
    Then status 200
    And match response == { users: '#array' }
    And match response.users[*].username contains 'treasurer'

  Scenario: Cannot get all Users without login
    Given path 'user'
    When method GET
    Then status 401
    And match response.errorCode == 401005

  Scenario: Cannot get all Users as member
    # Login as member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    Given path 'user'
    And header Authorization = "Bearer " + oToken
    When method GET
    Then status 401
    And match response.errorCode == 401005

  Scenario: Cannot get all Users as orderer
    # Login as orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    Given path 'user'
    And header Authorization = "Bearer " + oToken
    When method GET
    Then status 401
    And match response.errorCode == 401005

  Scenario: Get only deleted users
    # Create User
    Given path 'user', 'register'
    And request { username: "mustermann1", email: "mustermann1@test.com", memberId: "1234", password: #(password) }
    When method POST
    Then status 201
    And def userID = response.id

    # Login as Admin
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Delete new user
    Given path 'user', userID
    And header Authorization = "Bearer " + oToken
    When method DELETE
    Then status 204

    # Get only deleted users
    Given path 'user'
    And header Authorization = "Bearer " + oToken
    And param deleted = "ONLY"
    When method GET
    Then status 200
    And match response == { users: '#array' }
    And match response.users[*].username contains 'mustermann1'

    # Deleted user should net be in default /users response
    Given path 'user'
    And header Authorization = "Bearer " + oToken
    When method GET
    Then status 200
    And match response == { users: '#array' }
    And match response.users[*].username !contains 'mustermann1'

    # Deleted users should be in included /users resonse
    Given path 'user'
    And header Authorization = "Bearer " + oToken
    And param deleted = "INCLUDE"
    When method GET
    Then status 200
    And match response == { users: '#array' }
    And match response.users[*].username contains 'mustermann1'

  Scenario: User(Member) update own email

    # Create User
    Given path 'user', 'register'
    * def username = "mustermannTEST"
    * def email = "mustermannTEST@test.com"
    * def memberId = "4222800"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    # Login as ADMIN
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def rToken = response.token

    # add role MEMBER
    Given path 'user', userId, 'roles', 'MEMBER'
    And header Authorization = "Bearer " + rToken
    And request ''
    When method POST
    Then status 200
    And match response.roles contains 'MEMBER'

    Given path 'auth', 'login'
    And request { username:  #(username),  password: #(password) }
    When method POST
    Then status 200
    And def rToken = response.token
    * def userId = getUserIdFromToken(rToken)

    # User set new email (# Patch it)
    Given path 'user', userId
    And header Authorization = "Bearer " + rToken
    * def emailChanged = "newmustermannTEST@test.com"
    And request { email: #(emailChanged)}
    When method PATCH
    Then status 200

    Given path 'user', userId
    And header Authorization = "Bearer " + rToken
    When method GET
    Then status 200
    And match response contains { id: '#uuid', username: #(username), email: #(emailChanged), memberId: #(memberId), roles: '#array' }

  Scenario: User(Member) update own password
    # Create User
    Given path 'user', 'register'
    * def username = "mustermannTEST2"
    * def email = "mustermannTEST2@test.com"
    * def memberId = "4222811"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    # Login as ADMIN
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def mToken = response.token

    # add role MEMBER
    Given path 'user', userId, 'roles', 'MEMBER'
    And header Authorization = "Bearer " + mToken
    And request ''
    When method POST
    Then status 200
    And match response.roles contains 'MEMBER'

    Given path 'auth', 'login'
    And request { username:  #(username),  password: #(password) }
    When method POST
    Then status 200
    And def mToken = response.token

    # User set new password (# Patch it)
    Given path 'user', userId
    And header Authorization = "Bearer " + mToken
    * def passwordChanged = "3945r8484hbdsnjxcmkciw"
    And request { password: #(passwordChanged) }
    When method PATCH
    Then status 200
    And def mToken = response.token

    # Check that login was successful
    Given path 'auth', 'login'
    And request { username:  #(username),  password: #(passwordChanged) }
    When method POST
    Then status 200
    And def mToken = response.token

  Scenario:  User try update own memberId
    # Create User
    Given path 'user', 'register'
    * def username = "mustermannTEST3"
    * def email = "mustermannTEST3@test.com"
    * def memberId = "4222833"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    # Login as ADMIN
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def rToken = response.token

    # add role MEMBER
    Given path 'user', userId, 'roles', 'MEMBER'
    And header Authorization = "Bearer " + rToken
    And request ''
    When method POST
    Then status 200
    And match response.roles contains 'MEMBER'

    Given path 'auth', 'login'
    And request { username:  #(username),  password: #(password) }
    When method POST
    Then status 200
    And def rToken = response.token
    * def userId = getUserIdFromToken(rToken)

    # User try set new memberId (# Patch it)
    Given path 'user', userId
    And header Authorization = "Bearer " + rToken
    * def memberIdChanged = "00000012456972"
    And request { memberId: #(memberIdChanged)}
    When method PATCH
    Then status 401
    And match response.errorCode == 401005

  Scenario: Admin update user's email and memberId

    # Create User
    Given path 'user', 'register'
    * def username = "mustermannTEST4"
    * def email = "mustermannTEST4@test.com"
    * def memberId = "42228444"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    # Login as ADMIN
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def aToken = response.token

    # add role MEMBER
    Given path 'user', userId, 'roles', 'MEMBER'
    And header Authorization = "Bearer " + aToken
    And request ''
    When method POST
    Then status 200
    And match response.roles contains 'MEMBER'

    Given path 'auth', 'login'
    And request { username:  #(username),  password: #(password) }
    When method POST
    Then status 200
    And def rToken = response.token
    * def userId = getUserIdFromToken(rToken)

    # ADMIN update User´s data (# Patch all)
    Given path 'user', userId
    And header Authorization = "Bearer " + aToken
    * def memberIdChanged = "09772"
    * def emailChanged = "newmannTEST@test.com"
    And request { memberId: #(memberIdChanged), email: #(emailChanged)}
    When method PATCH
    Then status 200

    Given path 'user', userId
    And header Authorization = "Bearer " + aToken
    When method GET
    Then status 200
    And match response contains { id: '#uuid', username: #(username), email: #(emailChanged), memberId: #(memberIdChanged), isDeleted: false}

  Scenario: Admin update user's password

    # Create User
    Given path 'user', 'register'
    * def username = "mustermannTEST5"
    * def email = "mustermannTEST5@test.com"
    * def memberId = "52228555"
    And request { username: #(username), email: #(email), memberId: #(memberId), password: #(password) }
    When method POST
    Then status 201
    And def userId = response.id

    # Login as ADMIN
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def aToken = response.token

    # add role MEMBER
    Given path 'user', userId, 'roles', 'MEMBER'
    And header Authorization = "Bearer " + aToken
    And request ''
    When method POST
    Then status 200
    And match response.roles contains 'MEMBER'

    Given path 'auth', 'login'
    And request { username:  #(username),  password: #(password) }
    When method POST
    Then status 200
    And def uToken = response.token
    * def userId = getUserIdFromToken(uToken)

    # ADMIN update User´s data (# Patch all)
    Given path 'user', userId
    And header Authorization = "Bearer " + aToken
    * def passwordChanged = "keingutespasswort"
    And request { password: #(passwordChanged)}
    When method PATCH
    Then status 200

    # Check that login was successful
    Given path 'auth', 'login'
    And request { username:  #(username),  password: #(passwordChanged) }
    When method POST
    Then status 200
