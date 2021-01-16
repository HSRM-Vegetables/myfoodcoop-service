Feature: Simple Purchases

  Background:
    * url baseUrl + "/v2"
    * def password = "a_funny_horse**jumps_high778"
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

    * def calcPrice =
    """
    function (arr) {
      if (arr === undefined || arr === null) return 0.0;
      var res = 0.0
      for (var i = 0; i < arr.length; i++) {
        res += arr[i].amount * arr[i].pricePerUnit;
      }
      return res;
    }
    """

  Scenario: Purchase a single item
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby', email: 'Robby@test.com', memberId: '40', password: #(password) }
    When method POST
    Then status 201
    And print response
  
    # Get token
    Given path 'user', 'login'
    And request { username: 'Robby',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase item
    Given path '/purchase'
    And header Authorization = "bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "Robby"
    And assert response.price == 1.3
    And def purchaseId = response.id

    # Check that stock was reduced
    Given path '/stock/' + stockId1
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check that the balance was reduced
    Given path '/balance'
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 498.7

    # Check that purchase exists
    Given path '/purchase', purchaseId
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in purchase list
    Given path '/purchase'
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And assert response.purchases.length == 1
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }

  Scenario: Purchase multiple items
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby1', email: 'Robby1@test.com', memberId: '41', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'user', 'login'
    And request { username: 'Robby1',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item 1
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Purchase items
    Given path '/purchase'
    And header Authorization = "bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "Robby1"
    And assert response.price == 5.6
    And def purchaseId = response.id

    # Check stock was reduced on first item
    Given path '/stock/' + stockId1
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 139.0 }

    # Check stock was reduced on second item
    Given path '/stock/' + stockId2
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 19.0 }

    # Check that the balance was reduced
    Given path '/balance'
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 494.4

    # Check that purchase exists
    Given path '/purchase', purchaseId
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in list
    Given path '/purchase'
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And assert purchase.items.length == 2
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }
    And def purchasedItem2 = findItemWithId(purchase.items, stockId2)
    And match purchasedItem2 contains { id: #(stockId2) }

  Scenario: Cannot purchase fractional items when unitType is PIECE
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby2', email: 'Robby2@test.com', memberId: '42', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'user', 'login'
    And request { username: 'Robby2',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item 1
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Purchase item
    Given path '/purchase'
    And header Authorization = "bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1.5 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400008

    # Check quantity didn't change
    Given path '/stock/' + stockId1
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 140.0 }

    # Check quantity didn't change
    Given path '/stock/' + stockId2
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { quantity: 20.0 }

    # Check that the balance wasn't reduced
    Given path '/balance'
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: Cannot purchase deleted item
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby3', email: 'Robby3@test.com', memberId: '43', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'user', 'login'
    And request { username: 'Robby3',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item 1
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Create item 2
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Pumpkin", unitType: "PIECE", quantity: 20.0, pricePerUnit: 4.3 }
    When method POST
    Then status 201
    And def stockId2 = response.id

    # Delete item 2
    Given path '/stock/' + stockId2
    And header Authorization = "bearer " + token
    When method DELETE
    Then status 204

    # Purchase items
    Given path '/purchase'
    And header Authorization = "bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item2 = { id: #(stockId2), amount: 1 }
    And request { items: [#(item1), #(item2)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400011

    # Check that the balance wasn't reduced
    Given path '/balance'
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: Purchase a single item with higher amount than in stock is possible
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby4', email: 'Robby4@test.com', memberId: '44', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'user', 'login'
    And request { username: 'Robby4',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item 1
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase Items
    Given path '/purchase'
    And header Authorization = "bearer " + token
    And def item1 = { id: #(stockId1), amount: 200 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "Robby4"
    And def purchaseId = response.id

    # Check quantity is below 0
    Given path '/stock/' + stockId1
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And assert response.quantity == -60

    # Check that the balance was reduced
    Given path '/balance'
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    Then assert response.balance < 500

    # Check that purchase exists
    Given path '/purchase', purchaseId
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def calculatedPrice = calcPrice(response.items)
    And assert response.totalPrice == calculatedPrice

    # Check that purchase exists in list
    Given path '/purchase'
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And def purchase = findItemWithId(response.purchases, purchaseId)
    And match purchase contains { id: #(purchaseId) }
    And assert purchase.items.length == 1
    And def purchasedItem1 = findItemWithId(purchase.items, stockId1)
    And match purchasedItem1 contains { id: #(stockId1) }

  Scenario: Cannot have multiple items with same id in items array
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby5', email: 'Robby5@test.com', memberId: '45', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'user', 'login'
    And request { username: 'Robby5',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item 1
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase  items
    Given path '/purchase'
    And header Authorization = "bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And def item1AsWell = { id: #(stockId1), amount: 5 }
    And request { items: [#(item1), #(item1AsWell)] }
    When method POST
    Then status 400
    And assert response.errorCode == 400010

    # Check that the balance wasn't reduced
    Given path '/balance'
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    Then assert response.balance == 500

  Scenario: The price of a purchase does not change after a stock items price was updated
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby6', email: 'Robby6@test.com', memberId: '46', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'user', 'login'
    And request { username: 'Robby6',  password: #(password) }
    When method POST
    Then status 200
    And def token = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "bearer " + token
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item
    Given path '/stock'
    And header Authorization = "bearer " + token
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.0 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Purchase item
    Given path '/purchase'
    And header Authorization = "bearer " + token
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And def purchaseId = response.id

    # Get the purchase
    Given path '/purchase', purchaseId
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def totalPriceFirstTime = response.totalPrice
    And def calculatedPriceFirstTime = calcPrice(response.items)

    # Update price of an item
    Given path '/stock/' + stockId1
    And header Authorization = "bearer " + token
    And request { pricePerUnit: 5.0 }
    When method PATCH
    Then status 200

    # Get the purchase a second time
    Given path '/purchase', purchaseId
    And header Authorization = "bearer " + token
    When method GET
    Then status 200
    And match response contains { id: '#uuid', createdOn: '#string', totalPrice: '#number', items: '#array'}
    And match each response.items contains { id: '#uuid', name: '#string', amount: '#number', pricePerUnit: '#number', unitType: '#string' }
    And def totalPriceSecondTime = response.totalPrice
    And def calculatedPriceSecondTime = calcPrice(response.items)

    # Check prices are still the same
    And assert totalPriceFirstTime == totalPriceSecondTime
    And assert calculatedPriceFirstTime == calculatedPriceSecondTime

  Scenario: Request purchase via id from a different user fails
    # Register user
    Given path 'user', 'register'
    And request { username: 'Robby7', email: 'Robby7@test.com', memberId: '47', password: #(password) }
    When method POST
    Then status 201
    And print response

    # Get token
    Given path 'user', 'login'
    And request { username: 'Robby7',  password: #(password) }
    When method POST
    Then status 200
    And def robbyToken = response.token

    # Topup users balance
    Given path '/balance/topup'
    And header Authorization = "bearer " + robbyToken
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
    Given path 'user', 'login'
    And request { username: 'Manfred',  password: #(password) }
    When method POST
    Then status 200
    And def manfredToken = response.token

    # Topup user Manfreds balance
    Given path '/balance/topup'
    And header Authorization = "bearer " + manfredToken
    And request { amount: 500 }
    When method POST
    Then status 200

    # Create item
    Given path '/stock'
    And header Authorization = "bearer " + robbyToken
    And request { name: "Bananas", unitType: "WEIGHT", quantity: 140.0, pricePerUnit: 1.3 }
    When method POST
    Then status 201
    And def stockId1 = response.id

    # Robby purchases item
    Given path '/purchase'
    And header Authorization = "bearer " + robbyToken
    And def item1 = { id: #(stockId1), amount: 1 }
    And request { items: [#(item1)] }
    When method POST
    Then status 200
    And match response contains { id: '#uuid', name: '#string', balance: '#number', price: '#number' }
    And assert response.name == "Robby7"
    And assert response.price == 1.3
    And def purchaseId = response.id

    # Request purchase from different user
    Given path '/purchase', purchaseId
    And header Authorization = "bearer " + manfredToken
    When method GET
    Then status 401
    And assert response.errorCode == 401001
