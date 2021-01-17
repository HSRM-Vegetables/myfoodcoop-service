Feature: Simple Stock management

  Background:
    * url baseUrl + "/v2"
    * def password = "a_funny_horse**jumps_high778"
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

    * def getOffsetDate =
    """
    function(offset) {
      var dt = new Date();
      dt.setDate(dt.getDate() + offset);
      var month = dt.getMonth() + 1;
      var formattedMonth = ("0" + month).slice(-2);
      var formattedDay = ("0" + dt.getDate()).slice(-2);

      return dt.getFullYear() + "-" + formattedMonth + "-" + formattedDay;
    }
    """

    * def findItemWithId =
    """
    function(arr, id) {
      if (arr === undefined || arr === null || id === undefined || id === null) {
        return null;
      }
      for (var i = 0; i < arr.length; i++) {
        if (arr[i].id === id) {
          return arr[i];
        }
      }
      return null;
    }
    """

  Scenario: Generate a sold item report for items sold today
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby', email: 'Robby@test.com', memberId: '40', password: #(password) }
    When method POST
    Then status 201
    And print response
  
    # Get token
    Given path 'auth', 'login'
    And request { username: 'Robby',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "Bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "Bearer " + token
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Purchase items
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 200
    And assert response.name == "Robby"
    And assert response.price == 5.6

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + token
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length >= 2
    And def firstItem = findItemWithId(response.items, stockId1)
    And def secondItem = findItemWithId(response.items, stockId2)
    And match firstItem contains { id: #(stockId1) }
    And match secondItem contains { id: #(stockId2) }

  Scenario: Items sold in separate purchases will appear as single item in report
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby1', email: 'Robby1@test.com', memberId: '41', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'auth', 'login'
    And request { username: 'Robby1',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "Bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase item first time
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Purchase item second time
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId1), amount: 3 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + token
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length >= 1
    And def item = findItemWithId(response.items, stockId1)
    And match item contains { id: #(stockId1), quantitySold: 4 }

  Scenario: Item purchased by separate users will appear as a single item in a report
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby2', email: 'Robby2@test.com', memberId: '42', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'auth', 'login'
    And request { username: 'Robby2',  password: #(password) }
    When method POST
    Then status 200
    And def robbyToken = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "Bearer " + robbyToken
    And request { amount: 500 }
    When method POST
    Then status 200

    # Register User Manfred
    Given path 'user', 'register'
    And request { username: 'Manfred', email: 'Manfred@test.com', memberId: '112', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'auth', 'login'
    And request { username: 'Manfred',  password: #(password) }
    When method POST
    Then status 200
    And def manfredToken = response.token

    # Topup user Manfreds balance
    Given path '/balance/topup'
    And header Authorization = "Bearer " + manfredToken
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + robbyToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Robby purchases first item
    Given path '/purchase'
    And header Authorization = "Bearer " + robbyToken
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Manfred purchases second item
    Given path '/purchase'
    And header Authorization = "Bearer " + manfredToken
    And def item1 = { id: #(stockId1), amount: 3 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Generate report
    * def today = getToday()
    And header Authorization = "Bearer " + robbyToken
    Given path '/reports/sold-items'
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length >= 1
    And def item = findItemWithId(response.items, stockId1)
    And match item contains { id: #(stockId1), quantitySold: 4 }

  Scenario: Patching of an items quantity does not affect report
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby3', email: 'Robby3@test.com', memberId: '43', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'auth', 'login'
    And request { username: 'Robby3',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "Bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId = response.id

    # Purchase item first time
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # patch quantity
    Given path '/stock/' + stockId
    And header Authorization = "Bearer " + token
    And def quantityChanged = 120.0
    And request { quantity: 138.0 }
    When method PATCH
    Then status 200

    # Purchase item second time
    Given path '/purchase'
    And header Authorization = "Bearer " + token
    And def item1 = { id: #(stockId), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + token
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length >= 1
    And def item = findItemWithId(response.items, stockId)
    And match item contains { id: #(stockId), quantitySold: 2 }

  Scenario: Report list is empty is no purchase was made on that date
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby4', email: 'Robby4@test.com', memberId: '44', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'auth', 'login'
    And request { username: 'Robby4',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + token
    And param fromDate = '2020-01-01'
    And param toDate = '2020-01-01'
    When method GET
    Then status 200
    And assert response.items.length == 0

  Scenario: fromDate cannot be after toDate
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby5', email: 'Robby5@test.com', memberId: '45', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'auth', 'login'
    And request { username: 'Robby5',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Generate report
    * def today = getToday()
    * def yesterday = getOffsetDate(-1)
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + token
    And param fromDate = today
    And param toDate = yesterday
    When method GET
    Then status 400
    And assert response.errorCode == 400012

  Scenario: toDate cannot be in the future
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby6', email: 'Robby6@test.com', memberId: '46', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'auth', 'login'
    And request { username: 'Robby6',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Generate report
    * def today = getToday()
    * def tomorrow = getOffsetDate(1)
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + token
    And param fromDate = today
    And param toDate = tomorrow
    When method GET
    Then status 400
    And assert response.errorCode == 400013

  Scenario: fromDate cannot be in the future
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby7', email: 'Robby7@test.com', memberId: '47', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'auth', 'login'
    And request { username: 'Robby7',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Generate report
    * def today = getToday()
    * def tomorrow = getOffsetDate(1)
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + token
    And param fromDate = tomorrow
    And param toDate = today
    When method GET
    Then status 400
    And assert response.errorCode == 400013

  Scenario: toDate and fromDate cannot be in the future
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby8', email: 'Robby8@test.com', memberId: '48', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'auth', 'login'
    And request { username: 'Robby8',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Generate report
    * def tomorrow = getOffsetDate(1)
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + token
    And param fromDate = tomorrow
    And param toDate = tomorrow
    When method GET
    Then status 400
    And assert response.errorCode == 400013

  Scenario: GET /reports/sold-item needs authorization
    * def today = getToday()
    Given path '/reports/sold-items'
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 401
    And match response.errorCode == 401005
