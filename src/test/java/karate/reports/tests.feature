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

    * url baseUrl + "/v2"
    * def name = 'Bananas'
    * def unitType = 'PIECE'
    * def quantity = 42.0
    * def pricePerUnit = 4.2
    * def description = "this is a lovely piece of produce"
    * def descriptionChanged = "this is a lovely piece of produce with a different description"
    * def nameChanged = 'Avocados'
    * def unitTypeChanged = 'WEIGHT'
    * def quantityChanged = 110.0
    * def pricePerUnitChanged = 4.2
    * def password = "a_funny_horse**jumps_high778"
    * def sustainablyProduced = true
    * def certificates =  [ "Test, "Demeter" ]
    * def originCategory = "LOCAL"
    * def producer = "Farmer Joe"
    * def supplier = "Cargo bike dude"
    * def orderDate = "2021-01-24"
    * def deliveryDate = "2021-01-24"
    * def sustainablyProducedChanged = false
    * def certificatesChanged =  [ "Not Demeter" ]
    * def originCategoryChanged = "SUPRAREGIONAL"
    * def producerChanged = "Farmer Bob"
    * def supplierChanged = "Not the Cargo bike dude"
    * def orderDateChanged = "2020-01-20"
    * def deliveryDateChanged = "2020-01-20"
    * def stockStatus = 'INSTOCK'
    * def vat = 0.19
    * def defaultStockBody =
    """
      {
        name: #(name),
        unitType: #(unitType),
        quantity: #(quantity),
        pricePerUnit: #(pricePerUnit),
        description: #(description),
        sustainablyProduced: #(sustainablyProduced),
        certificates: #(certificates),
        originCategory: #(originCategory),
        producer: #(producer),
        supplier: #(supplier),
        orderDate: #(orderDate),
        deliveryDate: #(deliveryDate),
        stockStatus: #(stockStatus),
        vat: #(vat)
      }
    """


  Scenario: Generate a sold item report for items sold today using pagination
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockBody
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Create item 3
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockBody
    When method POST
    Then status 201
    And def stockId3 = response.id

    # Create item 4
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockBody
    When method POST
    Then status 201
    And def stockId4 = response.id

    # Get token of member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def mToken = response.token

    # Purchase items
    Given path '/purchase'
    And header Authorization = "Bearer " + mToken
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And def item3 = { id: #(stockId3), amount: 1 }
    And def item4 = { id: #(stockId4), amount: 1 }
    And request { items: [#(item1), #(item2), #(item3), #(item4)] }
    When method POST
    Then status 200
    And assert response.name == "member"

    # Get token of treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def tToken = response.token

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + tToken
    And param fromDate = today
    And param toDate = today
    And param offset = 2
    And param limit = 2
    When method GET
    Then status 200
    And assert response.items.length == 2
    And match response contains { pagination: '#object', items: '#array', totalVat: '#number', vatDetails: '#array', grossAmount: '#number' }
    And match response.pagination == { offset: 2, limit: 2, total: '#number' }
    And def firstItem = findItemWithId(response.items, stockId3)
    And def secondItem = findItemWithId(response.items, stockId4)
    And match firstItem contains { id: #(stockId3), totalVat: '#number', vat: '#number', grossAmount: '#number' }
    And match secondItem contains { id: #(stockId4), totalVat: '#number', vat: '#number', grossAmount: '#number' }

  Scenario: Items sold in separate purchases will appear as single item in report
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Get token of member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def mToken = response.token

    # Purchase item first time
    Given path '/purchase'
    And header Authorization = "Bearer " + mToken
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Purchase item second time
    Given path '/purchase'
    And header Authorization = "Bearer " + mToken
    And def item1 = { id: #(stockId1), amount: 3 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Get token of treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def tToken = response.token

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + tToken
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length >= 1
    And def item = findItemWithId(response.items, stockId1)
    And match item contains { id: #(stockId1), quantitySold: 4 }

  Scenario: Item purchased by separate users will appear as a single item in a report
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Get token of member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def mToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockBody
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Orderer purchases first item
    Given path '/purchase'
    And header Authorization = "Bearer " + oToken
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Member purchases second item
    Given path '/purchase'
    And header Authorization = "Bearer " + mToken
    And def item1 = { id: #(stockId1), amount: 3 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Get token of treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def tToken = response.token

    # Generate report
    * def today = getToday()
    And header Authorization = "Bearer " + tToken
    Given path '/reports/sold-items'
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length >= 1
    And def item = findItemWithId(response.items, stockId1)
    And match item contains { id: #(stockId1), quantitySold: 4 }

  Scenario: Patching of an items quantity does not affect report
    # Get token of orderer
    Given path 'auth', 'login'
    And request { username: 'orderer',  password: #(password) }
    When method POST
    Then status 200
    And def oToken = response.token

    # Create item 1
    Given path '/stock'
    And header Authorization = "Bearer " + oToken
    And request defaultStockBody
    When method POST
    Then status 201
    And def stockId = response.id

    # Get token of member
    Given path 'auth', 'login'
    And request { username: 'member',  password: #(password) }
    When method POST
    Then status 200
    And def mToken = response.token

    # Purchase item first time
    Given path '/purchase'
    And header Authorization = "Bearer " + mToken
    And def item1 = { id: #(stockId), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # patch quantity
    Given path '/stock/' + stockId
    And header Authorization = "Bearer " + oToken
    And def quantityChanged = 120.0
    And request { quantity: 138.0 }
    When method PATCH
    Then status 200

    # Purchase item second time
    Given path '/purchase'
    And header Authorization = "Bearer " + mToken
    And def item1 = { id: #(stockId), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Get token of treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def tToken = response.token

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + tToken
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length >= 1
    And def item = findItemWithId(response.items, stockId)
    And match item contains { id: #(stockId), quantitySold: 2 }

  Scenario: Report list is empty is no purchase was made on that date
    # Get token for treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def tToken = response.token

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And header Authorization = "Bearer " + tToken
    And param fromDate = '2020-01-01'
    And param toDate = '2020-01-01'
    When method GET
    Then status 200
    And assert response.items.length == 0

  Scenario: fromDate cannot be after toDate
    # Get token of treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
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
    # Get token
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
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
    # Get token
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
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
    # Get token
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
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

  Scenario: Generate a balance overview report
    # Login
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Generate report
    Given path 'reports', 'balance-overview'
    And header Authorization = "Bearer " + token
    And param offset = 2
    And param limit = 2
    When method GET
    Then status 200
    And match response contains { pagination: '#object', users: '#array'}
    And match response.pagination == { offset: 2, limit: 2, total: '#number' }
    And match each response.users contains { isDeleted: false }
    And match response.users[0].username == 'admin'
    And match response.users[1].username == 'treasurer'

  Scenario: Newly registered user is included in balance overview report
    # Create User
    Given path 'user', 'register'
    * def username = 'mustermann'
    And request { username: #(username), email: "mustermann@test.com", memberId: 1234000, password: #(password) }
    When method POST
    Then status 201
    And def userID = response.id

    # Login as treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Generate report
    Given path 'reports', 'balance-overview'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { users: '#array'}
    And match response.users[*].username contains username

  Scenario: Balance changes
    * def username = 'member'

    # Login
    Given path 'auth', 'login'
    And request { username: #(username),  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token
    And def userId = getUserIdFromToken(token)

    # Set Balance
    Given path 'balance', userId
    And header Authorization = "Bearer " + token
    And request { balance: 987.65 }
    When method PATCH
    Then status 200

    # Login as Treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Generate Report
    Given path 'reports', 'balance-overview'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { users: '#array'}
    And match response.users contains { username: #(username), balance: 987.65, id: '#string', memberId: '#string', isDeleted: false }

  Scenario: Balance overview report for deleted users
    # Create User
    Given path 'user', 'register'
    * def username = 'mustermann2'
    And request { username: #(username), email: "mustermann2@test.com", memberId: "1234002", password: #(password) }
    When method POST
    Then status 201
    And def userID = response.id

    # Login as Admin
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

    # Login as Treasurer
    Given path 'auth', 'login'
    And request { username: 'treasurer',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Generate report for deleted users only
    Given path 'reports', 'balance-overview'
    And header Authorization = "Bearer " + token
    And param deleted = "ONLY"
    When method GET
    Then status 200
    And match response contains { users: '#array'}
    And match response.users[*].username contains username
    And match each response.users contains { isDeleted: true }

    # Deleted user should not be in default report
    Given path 'reports', 'balance-overview'
    And header Authorization = "Bearer " + token
    When method GET
    Then status 200
    And match response contains { users: '#array'}
    And match response.users[*].username !contains username

    # Deleted user should be in included report
    Given path 'reports', 'balance-overview'
    And header Authorization = "Bearer " + token
    And param deleted = "INCLUDE"
    When method GET
    Then status 200
    And match response contains { users: '#array'}
    And match response.users[*].username contains username