Feature: Balance Tests

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

    * def getToday =
    """
    function() {
      var dt = new Date();
      var month = dt.getMonth() + 1;
      var formattedMonth = ("0" + month).slice(-2);
      var formattedDay = ("0" + dt.getDate()).slice(-2);

      return dt.getFullYear() + "-" + formattedMonth + "-" + formattedDay;
    }
    """

    * def defaultStockPostBody =
    """
    {
      name: "Test Product",
      unitType: "PIECE",
      quantity: 100.0,
      pricePerUnit: 2,
      sustainablyProduced: true,
      originCategory: "UNKNOWN",
      producer: "producer",
      supplier: "supplier",
      stockStatus: "INSTOCK",
      vat: 0.19
    }
    """

  Scenario: GET /balance/:userId/history works for user with empty balance history
    # Login as member
    Given path 'auth', 'login'
    And request { username: 'balance_history_member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Query balance history
    * def today = getToday()
    Given path 'balance', userId, 'history'
    And header Authorization = "Bearer " + token
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And match response contains { pagination: '#object', balanceHistoryItems: '#array' }
    And match response.pagination == { offset: 0, limit: 10, total: 0 }
    And assert response.balanceHistoryItems.length == 0

  Scenario: GET /balance/:userId/history works for user with balance changes
    # Login as member
    Given path 'auth', 'login'
    And request { username: 'balance_history_member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Add money to balance
    Given path 'balance', userId, 'topup'
    And header Authorization = "Bearer " + token
    And request { amount: 10.0 }
    When method POST
    Then status 200

    # Withdraw money from balance
    Given path 'balance', userId, 'withdraw'
    And header Authorization = "Bearer " + token
    And request { amount: 20.0 }
    When method POST
    Then status 200

    # Set balance to new value
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    And request { balance: 30.0 }
    When method PATCH
    Then status 200

    # Query balance history
    * def today = getToday()
    Given path 'balance', userId, 'history'
    And header Authorization = "Bearer " + token
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And match response contains { pagination: '#object', balanceHistoryItems: '#array' }
    And match response.pagination == { offset: 0, limit: 10, total: 3 }
    And assert response.balanceHistoryItems.length == 3
    And match each response.balanceHistoryItems contains { id: '#string', createdOn: '#string', balanceChangeType: '#string', amount: '#number' }

  Scenario: GET /balance/:userId/history works for user with purchases
    # Login as orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def ordererToken = response.token

    # Create 100 items in stock
    Given path '/stock'
    And header Authorization = "Bearer " + ordererToken
    And request defaultStockPostBody
    When method POST
    Then status 201
    And def stockId = response.id

    # Login as member
    Given path 'auth', 'login'
    And request { username: 'balance_history_member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Purchase 5 items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item = { id: #(stockId), amount: 5 }
    And request { items: [#(item)] }
    When method POST
    Then status 200
    And def purchaseId = response.id

    # Query balance history using pagination
    * def today = getToday()
    Given path 'balance', userId, 'history'
    And header Authorization = "Bearer " + token
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And match response contains { pagination: '#object', balanceHistoryItems: '#array' }
    And match response.pagination == { offset: 0, limit: 10, total: 4 }
    And assert response.balanceHistoryItems.length == 4
    And match each response.balanceHistoryItems contains { id: '#string', createdOn: '#string', balanceChangeType: '#string', amount: '#number' }
    And match response.balanceHistoryItems[2] !contains { purchase: '#object' }
    And match response.balanceHistoryItems[3] contains { purchase: '#object', amount: 10.0 }

  Scenario: PATCH allows to set the balance for user
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    Given path 'balance', userId
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
    And def userId = getUserIdFromToken(token)

    Given path '/balance', userId
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
    And def userId = getUserIdFromToken(token)

    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    * def newBalance = response.balance + 2.0

    Given path 'balance', userId, 'topup'
    And header Authorization = "Bearer " + token
    And request { amount: 2 }
    When method POST
    Then status 200
    And match response contains { balance: #(newBalance) }

    Given path 'balance', userId
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
    And def userId = getUserIdFromToken(token)

    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    * def newBalance = response.balance - 3.0

    Given path 'balance', userId, 'withdraw'
    And header Authorization = "Bearer " + token
    And request { amount: 3 }
    When method POST
    Then status 200
    And match response contains { balance: #(newBalance) }

    Given path 'balance', userId
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
    And def userId = getUserIdFromToken(token)

    Given path 'balance', userId, 'topup'
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
    And def userId = getUserIdFromToken(token)

    Given path 'balance', userId, 'withdraw'
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
    And def userId = getUserIdFromToken(token)

    Given path 'balance', userId
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
    And def userId = getUserIdFromToken(token)

    Given path 'balance', userId, 'topup'
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
    And def userId = getUserIdFromToken(token)

    Given path 'balance', userId, 'withdraw'
    And header Authorization = "Bearer " + token
    And request { foo: -3 }
    When method POST
    Then status 400
    And assert response.errorCode == 400005

  Scenario: GET /balance/user-id requires authorization
    Given path 'balance', 1234
    When method GET
    Then status 401
    And match response.errorCode == 401005

  Scenario: PATCH /balance/user-id requires authorization
    Given path 'balance', 1234
    And request { balance: 5 }
    When method PATCH
    Then status 401
    And match response.errorCode == 401005

  Scenario: POST /balance/user-id/topup requires authorization
    Given path 'balance', 1234, 'topup'
    And request { amount: 5 }
    When method POST
    Then status 401
    And match response.errorCode == 401005

  Scenario: POST /balance/user-id/withdraw requires authorization
    Given path 'balance', 1234, 'withdraw'
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
    And def userId = getUserIdFromToken(token)

    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response.balance == 0

  Scenario: Get current balance of another user
    # Login as member and get id
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def userId = getUserIdFromToken(response.token)

    # Login as treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Get balance for member
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { balance: '#number' }

  Scenario: Cannot get balance for another user without login
    Given path 'balance', '123'
    When method GET
    Then status 401
    And match response.errorCode == 401005

  Scenario: Error when trying to get balance for invalid user-id
    # Login as treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Try to get balance for unknown user
    Given path 'balance', 'ZZZZZ9999'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 404
    And match response.errorCode == 404005

  Scenario: Error when trying to get balance for another user as member
    # Login as admin and get userId
    Given path 'auth', 'login'
    And request { username: 'admin',  password: #(password) }
    When method POST
    Then status 200
    And def userId = getUserIdFromToken(response.token)

    # Login as member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Try to get balance for admin
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    When method GET
    Then status 401
    And match response.errorCode == 401005

  Scenario Outline: Error when trying to patch balance for another user
    # Login as member and get userId
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def userId = getUserIdFromToken(response.token)

    # Login
    Given path 'auth', 'login'
    And request { username: '<username>',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Try to patch balance for member
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    And request { balance: 5 }
    When method PATCH
    Then status 401
    And match response.errorCode == 401005

    Examples:
      | username  |
      | treasurer |
      | orderer   |
      | admin     |

  Scenario Outline: Error when trying to topup for another user
    # Login as member and get userId
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def userId = getUserIdFromToken(response.token)

    # Login
    Given path 'auth', 'login'
    And request { username: '<username>',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Try to patch balance for member
    Given path 'balance', userId, 'topup'
    And header Authorization = "Bearer " + token
    And request { amount: 2 }
    When method POST
    Then status 401
    And match response.errorCode == 401005

    Examples:
      | username  |
      | treasurer |
      | orderer   |
      | admin     |

  Scenario Outline: Error when trying to withdraw for another user
    # Login as member and get userId
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def userId = getUserIdFromToken(response.token)

    # Login
    Given path 'auth', 'login'
    And request { username: '<username>',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Try to patch balance for member
    Given path 'balance', userId, 'withdraw'
    And header Authorization = "Bearer " + token
    And request { amount: 2 }
    When method POST
    Then status 401
    And match response.errorCode == 401005

    Examples:
      | username  |
      | treasurer |
      | orderer   |
      | admin     |