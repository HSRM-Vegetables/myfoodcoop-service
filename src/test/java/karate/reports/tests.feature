Feature: Simple Stock management

  Background:
    * url baseUrl
    * def getToday =
    """
    function() {
      var SimpleDateFormat = Java.type('java.text.SimpleDateFormat');
      var sdf = new SimpleDateFormat("yyyy-MM-dd");
      return sdf.format(new Date())
    }
    """

    * def getOffsetDate =
    """
    function(offset) {
      var SimpleDateFormat = Java.type('java.text.SimpleDateFormat');
      var sdf = new SimpleDateFormat("yyyy-MM-dd");
      var offsetDate = new Date()
      offsetDate.setDate(offsetDate.getDate() + offset)
      return sdf.format(offsetDate)
    }
    """

    * def findItemWithId =
    """
    function(arr, id) {
      return arr.find(function(item) { item.id === id });
    }
    """

  Scenario: Generate a sold item report for items sold today
    # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Purchase items
    Given path '/purchase'
    And header X-Username = "Robby"
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
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length > 2
    And def firstItem = findItemWithId(response.items, stockId1)
    And def secondItem = findItemWithId(response.items, stockId2)
    And match firstItem contains { id: #(stockIdId) }
    And match secondItem contains { id: #(stockId2) }

  Scenario: Items sold in separate purchases will appear as single item in report
    # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase item first time
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Purchase item second time
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 3 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length == 1
    And match response.items[0] contains { id: #(stockIdId), quantitySold: 4 }

  Scenario: Item purchased by separate users will appear as a single item in a report
    # Create Balance for User 1
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Create Balance for User 2
    Given path '/balance/Manfred'
    And request { balance: 100 }
    When method PATCH
    Then status 200

    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Robby purchases first item
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Manfred purchases second item
    Given path '/purchase'
    And header X-Username = "Manfred"
    And def item1 = { id: #(stockId1), amount: 3 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length == 1
    And match response.items[0] contains { id: #(stockIdId), quantitySold: 4 }

  Scenario: Patching of an items quantity does not affect report
    # Create Balance for User
    Given path '/balance/Robby'
    And request { balance: 500 }
    When method PATCH
    Then status 200

    # Create item 1
    Given path '/stock'
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase item first time
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # patch quantity
    Given path '/stock/' + stockId
    And def quantityChanged = 120.0
    And request { quantity: 138.0 }
    When method PATCH
    Then status 200

    # Purchase item second time
    Given path '/purchase'
    And header X-Username = "Robby"
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200

    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length == 1
    And match response.items[0] contains { id: #(stockIdId), quantitySold: 2 }

  Scenario: Report list is empty is no purchase was made on that date
    # Generate report
    * def today = getToday()
    Given path '/reports/sold-items'
    And param fromDate = today
    And param toDate = today
    When method GET
    Then status 200
    And assert response.items.length == 0

  Scenario: toDate cannot be after fromDate
    # Generate report
    * def today = getToday()
    * def yesterday = getOffsetDate(-1)
    Given path '/reports/sold-items'
    And param fromDate = today
    And param toDate = yesterday
    When method GET
    Then status 400
    And assert response.errorCode == 400012

  Scenario: toDate cannot be in the future
    # Generate report
    * def today = getToday()
    * def tomorrow = getOffsetDate(1)
    Given path '/reports/sold-items'
    And param fromDate = today
    And param toDate = tomorrow
    When method GET
    Then status 400
    And assert response.errorCode == 400013

  Scenario: toDate cannot be in the future
    # Generate report
    * def tomorrow = getOffsetDate(1)
    Given path '/reports/sold-items'
    And param fromDate = tomorrow
    And param toDate = tomorrow
    When method GET
    Then status 400
    And assert response.errorCode == 400013
