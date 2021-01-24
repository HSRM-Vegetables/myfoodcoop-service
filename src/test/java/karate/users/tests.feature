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
      And match response = { users: '#array' }
      And match response.users[*].username == 'admin'

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