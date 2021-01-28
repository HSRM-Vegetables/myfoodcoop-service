Feature: Users controller

  Background:
    * url baseUrl + "/v2"
    * def password = "a_funny_horse**jumps_high778"

    Scenario: Get all Users as admin
      # Login as admin
      Given path 'auth', 'login'
      And request { username: 'admin',  password: #(password) }
      When method POST
      Then status 200
      And def oToken = response.token

      Given path 'users'
      And header Authorization = "Bearer " + oToken
      When method GET
      Then status 200
      And match response == { users: '#array' }
      And match response.users[*].username contains 'admin'

    Scenario: Cannot get all Users without login
      Given path 'users'
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

      Given path 'users'
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

      Given path 'users'
      And header Authorization = "Bearer " + oToken
      When method GET
      Then status 401
      And match response.errorCode == 401005

    Scenario: Cannot get all Users as treasurer
          # Login as treasurer
      Given path 'auth', 'login'
      And request { username: 'treasurer',  password: #(password) }
      When method POST
      Then status 200
      And def oToken = response.token

      Given path 'users'
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
      Given path 'users'
      And header Authorization = "Bearer " + oToken
      And param deleted = "ONLY"
      When method GET
      Then status 200
      And match response == { users: '#array' }
      And match response.users[*].username contains 'mustermann1'

      # Deleted user should net be in default /users response
      Given path 'users'
      And header Authorization = "Bearer " + oToken
      When method GET
      Then status 200
      And match response == { users: '#array' }
      And match response.users[*].username !contains 'mustermann1'

      # Deleted users should be in included /users resonse
      Given path 'users'
      And header Authorization = "Bearer " + oToken
      And param deleted = "INCLUDE"
      When method GET
      Then status 200
      And match response == { users: '#array' }
      And match response.users[*].username contains 'mustermann1'
